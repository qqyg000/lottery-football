import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const teamNameMappingsPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')
const cacheRoot = path.join(root, 'target/supplemental-history-cache')
const MINIMUM_HISTORY_DATE = '2014-06-12'

const HISTORY_HEADERS = [
  'match_id',
  'match_date',
  'competition',
  'home_team_cn',
  'away_team_cn',
  'home_score',
  'away_score',
  'neutral',
  'match_type',
  'source_competition'
]

const NATIONAL_COMPETITIONS = new Set([
  'WORLD_CUP',
  'EUROPEAN_CHAMPIONSHIP',
  'COPA_AMERICA'
])

const CLUB_COMPETITIONS = new Set([
  'CLUB_WORLD_CUP',
  'EUROPA_LEAGUE',
  'CHAMPIONS_LEAGUE',
  'PREMIER_LEAGUE',
  'LA_LIGA',
  'SERIE_A',
  'BUNDESLIGA',
  'LIGUE_1',
  'BRAZIL_SERIE_A',
  'PRIMEIRA_LIGA',
  'EREDIVISIE',
  'ARGENTINE_PRIMERA_DIVISION'
])

const COMPETITION_NAMES = new Map(Object.entries({
  WORLD_CUP: '世界杯',
  EUROPEAN_CHAMPIONSHIP: '欧洲杯',
  COPA_AMERICA: '美洲杯',
  INTERNATIONAL_OFFICIAL: '国家队其他正式比赛',
  INTERNATIONAL_FRIENDLY: '国家队国际窗口友谊赛',
  CLUB_WORLD_CUP: '世俱杯',
  CLUB_OFFICIAL_OTHER: '俱乐部其他正式比赛',
  CLUB_FRIENDLY: '俱乐部正常阵容友谊赛',
  EUROPA_LEAGUE: '欧罗巴',
  CHAMPIONS_LEAGUE: '欧冠',
  PREMIER_LEAGUE: '英超',
  LA_LIGA: '西甲',
  SERIE_A: '意甲',
  BUNDESLIGA: '德甲',
  LIGUE_1: '法甲',
  BRAZIL_SERIE_A: '巴甲',
  PRIMEIRA_LIGA: '葡超',
  EREDIVISIE: '荷甲',
  ARGENTINE_PRIMERA_DIVISION: '阿甲'
}))


function parseArguments(argv) {
  const options = {
    write: false,
    compact: false,
    refreshCache: false,
    skipEspn: false,
    sourceRoot: null,
    minDate: MINIMUM_HISTORY_DATE,
    maxDate: localDate(new Date()),
    concurrency: 8
  }
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index]
    if (argument === '--write') {
      options.write = true
    } else if (argument === '--compact') {
      options.compact = true
    } else if (argument === '--refresh-cache') {
      options.refreshCache = true
    } else if (argument === '--skip-espn') {
      options.skipEspn = true
    } else if (argument === '--source-root') {
      options.sourceRoot = path.resolve(requiredArgument(argv, ++index, argument))
    } else if (argument === '--min-date') {
      options.minDate = requiredArgument(argv, ++index, argument)
    } else if (argument === '--max-date') {
      options.maxDate = requiredArgument(argv, ++index, argument)
    } else if (argument === '--concurrency') {
      options.concurrency = Number(requiredArgument(argv, ++index, argument))
    } else {
      throw new Error(`未知参数：${argument}`)
    }
  }
  for (const [name, value] of [['--min-date', options.minDate], ['--max-date', options.maxDate]]) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
      throw new Error(`${name} 必须使用 yyyy-MM-dd 格式：${value}`)
    }
  }
  if (options.minDate > options.maxDate) {
    throw new Error('--min-date 不能晚于 --max-date')
  }
  options.concurrency = Math.max(1, Math.min(16, Number.isFinite(options.concurrency)
    ? Math.floor(options.concurrency)
    : 8))
  return options
}

function requiredArgument(argv, index, optionName) {
  const value = argv[index]
  if (!value || value.startsWith('--')) {
    throw new Error(`${optionName} 缺少参数值`)
  }
  return value
}

function localDate(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index]
    const nextCharacter = text[index + 1]
    if (inQuotes) {
      if (character === '"' && nextCharacter === '"') {
        field += '"'
        index += 1
      } else if (character === '"') {
        inQuotes = false
      } else {
        field += character
      }
    } else if (character === '"') {
      inQuotes = true
    } else if (character === ',') {
      row.push(field)
      field = ''
    } else if (character === '\n') {
      row.push(field.replace(/\r$/, ''))
      rows.push(row)
      row = []
      field = ''
    } else {
      field += character
    }
  }
  if (field.length > 0 || row.length > 0) {
    row.push(field.replace(/\r$/, ''))
    rows.push(row)
  }
  const headers = rows.shift().map((name, index) => (
    index === 0 ? name.replace(/^\uFEFF/, '') : name
  ))
  return rows
    .filter(values => values.length === headers.length)
    .map(values => Object.fromEntries(headers.map((name, index) => [name, values[index]])))
}

function escapeCsv(value) {
  const text = String(value ?? '')
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function toCsv(rows) {
  return [
    HISTORY_HEADERS.join(','),
    ...rows.map(row => HISTORY_HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

function canonicalName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function canonicalClubName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]+/gu, ' ')
    .replace(/^(AFC|AC|AS|CA|CD|CF|FC|FK|NK|SC|SV|TSG)\s+/u, '')
    .replace(/\s+(AFC|AC|AS|CA|CD|CF|FC|FK|NK|SC|SV|TSG)$/u, '')
    .replaceAll(' ', '')
}

function nameKeys(value) {
  return [...new Set([canonicalName(value), canonicalClubName(value)].filter(Boolean))]
}

function canonicalChineseName(value) {
  return String(value ?? '')
    .normalize('NFKC')
    .toUpperCase()
    .replace(/[\s·•，,.'’`´()（）\[\]【】_\/&-]+/gu, '')
    .replace(/^(FC|SC|CF)(?=\p{Script=Han})/u, '')
    .replace(/(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$/u, '')
}

function normalizeHistoryRow(row) {
  const competition = String(row.competition ?? '').trim()
  const defaultType = competition === 'INTERNATIONAL_FRIENDLY'
    ? 'INTERNATIONAL_FRIENDLY'
    : competition === 'CLUB_FRIENDLY'
      ? 'CLUB_FRIENDLY'
      : 'OFFICIAL'
  return {
    match_id: String(row.match_id ?? '').trim(),
    match_date: String(row.match_date ?? '').trim(),
    competition,
    home_team_cn: String(row.home_team_cn ?? '').trim(),
    away_team_cn: String(row.away_team_cn ?? '').trim(),
    home_score: String(row.home_score ?? '').trim(),
    away_score: String(row.away_score ?? '').trim(),
    neutral: String(row.neutral ?? '').trim().toLowerCase(),
    match_type: String(row.match_type ?? '').trim() || defaultType,
    source_competition: String(row.source_competition ?? '').trim()
      || COMPETITION_NAMES.get(competition)
      || competition
  }
}

function buildMappings(mappingRows, allowedCompetitions) {
  const mappings = new Map()
  for (const row of mappingRows) {
    if (row.competition !== '*' && !allowedCompetitions.has(row.competition)) {
      continue
    }
    const aliasName = String(row.alias_team_name ?? '').trim()
    const standardName = String(row.standard_team_name ?? '').trim()
    if (!aliasName || !standardName) {
      continue
    }
    for (const key of nameKeys(aliasName)) {
      mappings.set(key, standardName)
    }
  }
  return mappings
}

function mappedChineseName(sourceName, mappings) {
  for (const key of nameKeys(sourceName)) {
    const mapped = mappings.get(key)
    if (mapped) {
      return mapped
    }
  }
  return null
}

function extraTimeGoalCounts(goalRows) {
  const counts = new Map()
  for (const row of goalRows) {
    const minute = Number(row.minute)
    if (!Number.isFinite(minute) || minute <= 90) {
      continue
    }
    const key = [row.date, row.home_team, row.away_team, row.team].join('|')
    counts.set(key, (counts.get(key) ?? 0) + 1)
  }
  return counts
}

function regulationNationalScore(result, extraTimeGoals) {
  const homeExtraTimeGoals = extraTimeGoals.get([
    result.date,
    result.home_team,
    result.away_team,
    result.home_team
  ].join('|')) ?? 0
  const awayExtraTimeGoals = extraTimeGoals.get([
    result.date,
    result.home_team,
    result.away_team,
    result.away_team
  ].join('|')) ?? 0
  const homeScore = Number(result.home_score) - homeExtraTimeGoals
  const awayScore = Number(result.away_score) - awayExtraTimeGoals
  if (!Number.isInteger(homeScore) || !Number.isInteger(awayScore) || homeScore < 0 || awayScore < 0) {
    return null
  }
  return {
    homeScore,
    awayScore,
    correctedExtraTimeGoals: homeExtraTimeGoals + awayExtraTimeGoals
  }
}

function nationalCompetition(tournament) {
  if (tournament === 'FIFA World Cup') {
    return 'WORLD_CUP'
  }
  if (tournament === 'UEFA Euro') {
    return 'EUROPEAN_CHAMPIONSHIP'
  }
  if (tournament === 'Copa América') {
    return 'COPA_AMERICA'
  }
  return tournament === 'Friendly' ? 'INTERNATIONAL_FRIENDLY' : 'INTERNATIONAL_OFFICIAL'
}

function parseNationalRows(resultRows, goalRows) {
  const extraTimeGoals = extraTimeGoalCounts(goalRows)
  const rows = []
  for (const result of resultRows) {
    const score = regulationNationalScore(result, extraTimeGoals)
    if (!score) {
      continue
    }
    const competition = nationalCompetition(result.tournament)
    rows.push({
      provider: 'INTL',
      source: result.tournament === 'Friendly' ? 'INTL-FRIENDLY' : 'INTL-OFFICIAL',
      competition,
      matchType: result.tournament === 'Friendly' ? 'INTERNATIONAL_FRIENDLY' : 'OFFICIAL',
      sourceCompetition: NATIONAL_TOURNAMENT_NAMES.get(result.tournament) ?? result.tournament,
      matchDate: result.date,
      homeTeam: result.home_team,
      awayTeam: result.away_team,
      homeScore: score.homeScore,
      awayScore: score.awayScore,
      neutral: String(result.neutral ?? '').toLowerCase() === 'true',
      allowNationalFallback: true,
      correctedExtraTimeGoals: score.correctedExtraTimeGoals
    })
  }
  return rows
}

function monthRanges(minDate, maxDate) {
  const ranges = []
  const cursor = new Date(`${minDate.slice(0, 7)}-01T00:00:00Z`)
  while (cursor.toISOString().slice(0, 10) <= maxDate) {
    const year = cursor.getUTCFullYear()
    const month = cursor.getUTCMonth()
    const nextMonth = new Date(Date.UTC(year, month + 1, 1))
    const monthStart = `${year}-${String(month + 1).padStart(2, '0')}-01`
    const monthEndDate = new Date(nextMonth.getTime() - 86_400_000).toISOString().slice(0, 10)
    ranges.push({
      key: monthStart.slice(0, 7),
      start: monthStart < minDate ? minDate : monthStart,
      end: monthEndDate > maxDate ? maxDate : monthEndDate
    })
    cursor.setUTCMonth(cursor.getUTCMonth() + 1)
  }
  return ranges
}

function yearRanges(minDate, maxDate) {
  const ranges = []
  for (let year = Number(minDate.slice(0, 4)); year <= Number(maxDate.slice(0, 4)); year += 1) {
    const yearStart = `${year}-01-01`
    const yearEnd = `${year}-12-31`
    ranges.push({
      key: String(year),
      start: yearStart < minDate ? minDate : yearStart,
      end: yearEnd > maxDate ? maxDate : yearEnd
    })
  }
  return ranges
}

function basicDate(dateText) {
  return dateText.replaceAll('-', '')
}

async function fetchJsonWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { 'User-Agent': 'lottery-football-history-importer/1.0' },
        signal: AbortSignal.timeout(45_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.json()
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        await new Promise(resolve => setTimeout(resolve, attempt * 750))
      }
    }
  }
  throw new Error(`${lastError?.message ?? '未知错误'}`)
}

async function fetchTextWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { 'User-Agent': 'lottery-football-history-importer/1.0' },
        signal: AbortSignal.timeout(45_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.text()
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        await new Promise(resolve => setTimeout(resolve, attempt * 750))
      }
    }
  }
  throw new Error(`下载失败：${url}，${lastError?.message ?? '未知错误'}`)
}

async function readEspnRange(request, options) {
  const cachePath = path.join(cacheRoot, 'espn', request.source.slug, `${request.range.key}.json`)
  const refreshCurrentRange = options.maxDate === localDate(new Date())
    && request.range.end === options.maxDate
  if (!options.refreshCache && !refreshCurrentRange) {
    try {
      return JSON.parse(await fs.readFile(cachePath, 'utf8'))
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://site.api.espn.com/apis/site/v2/sports/soccer/${request.source.slug}`
    + `/scoreboard?limit=1000&dates=${basicDate(request.range.start)}-${basicDate(request.range.end)}`
  const json = await fetchJsonWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, JSON.stringify(json), 'utf8')
  return json
}

async function mapWithConcurrency(items, concurrency, worker) {
  const results = new Array(items.length)
  let nextIndex = 0
  async function runWorker() {
    while (nextIndex < items.length) {
      const index = nextIndex
      nextIndex += 1
      results[index] = await worker(items[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrency, items.length) }, runWorker))
  return results
}

function shanghaiDate(isoText) {
  const date = new Date(isoText)
  if (Number.isNaN(date.getTime())) {
    return null
  }
  const parts = Object.fromEntries(new Intl.DateTimeFormat('en', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).formatToParts(date).filter(part => part.type !== 'literal').map(part => [part.type, part.value]))
  return `${parts.year}-${parts.month}-${parts.day}`
}

function eventCompetitors(event) {
  const competition = event?.competitions?.[0]
  const competitors = Array.isArray(competition?.competitors) ? competition.competitors : []
  const home = competitors.find(competitor => competitor.homeAway === 'home')
  const away = competitors.find(competitor => competitor.homeAway === 'away')
  return { competition, home, away }
}

function competitorTeamName(competitor) {
  return String(competitor?.team?.displayName ?? competitor?.team?.location ?? '').trim()
}

function integerScore(value) {
  const numberValue = Number(value)
  return Number.isInteger(numberValue) && numberValue >= 0 ? numberValue : null
}

function detailMinute(detail) {
  const displayValue = String(detail?.clock?.displayValue ?? '')
  const displayMatch = displayValue.match(/^(\d+)/)
  if (displayMatch) {
    return Number(displayMatch[1])
  }
  const clockValue = Number(detail?.clock?.value)
  return Number.isFinite(clockValue) ? clockValue / 60 : null
}

function regulationEspnScore(event, competition, home, away) {
  const rawHomeScore = integerScore(home?.score)
  const rawAwayScore = integerScore(away?.score)
  if (rawHomeScore === null || rawAwayScore === null) {
    return null
  }
  const statusText = [
    event?.status?.type?.description,
    event?.status?.type?.detail,
    event?.status?.type?.shortDetail
  ].join(' ').toLowerCase()
  const beyondRegulation = Number(event?.status?.period ?? 0) > 2
    || statusText.includes('extra time')
    || statusText.includes('penalt')
  if (!beyondRegulation) {
    return { homeScore: rawHomeScore, awayScore: rawAwayScore }
  }
  const details = Array.isArray(competition?.details) ? competition.details : []
  let homeScore = 0
  let awayScore = 0
  let sawGoal = false
  for (const detail of details) {
    if (!detail?.scoringPlay || detail?.shootout || Number(detail?.scoreValue ?? 0) <= 0) {
      continue
    }
    sawGoal = true
    const minute = detailMinute(detail)
    if (minute !== null && minute > 90) {
      continue
    }
    const scoreValue = Number(detail.scoreValue)
    const teamId = String(detail?.team?.id ?? '')
    if (teamId === String(home?.team?.id ?? home?.id ?? '')) {
      homeScore += scoreValue
    } else if (teamId === String(away?.team?.id ?? away?.id ?? '')) {
      awayScore += scoreValue
    }
  }
  if (sawGoal || rawHomeScore + rawAwayScore === 0) {
    return { homeScore, awayScore }
  }
  return null
}

function parseEspnEvents(json, source) {
  const rows = []
  for (const event of json?.events ?? []) {
    if (!event?.status?.type?.completed) {
      continue
    }
    const { competition, home, away } = eventCompetitors(event)
    const homeTeam = competitorTeamName(home)
    const awayTeam = competitorTeamName(away)
    const matchDate = shanghaiDate(event?.date)
    const score = regulationEspnScore(event, competition, home, away)
    if (!homeTeam || !awayTeam || !matchDate || !score) {
      continue
    }
    rows.push({
      provider: 'ESPN',
      providerId: String(event.id ?? ''),
      source: `ESPN-${source.slug}`,
      competition: source.competition,
      matchType: source.matchType,
      sourceCompetition: source.sourceCompetition,
      matchDate,
      homeTeam,
      awayTeam,
      homeScore: score.homeScore,
      awayScore: score.awayScore,
      neutral: Boolean(competition?.neutralSite),
      allowNationalFallback: false
    })
  }
  return rows
}

async function loadEspnRows(options) {
  if (options.skipEspn) {
    return { rows: [], errors: [] }
  }
  const sources = [...ESPN_PRIMARY_SOURCES, ...ESPN_OFFICIAL_SOURCES, ...ESPN_FRIENDLY_SOURCES]
  const requests = sources.flatMap(source => (
    (source.monthly ? monthRanges(options.minDate, options.maxDate) : yearRanges(options.minDate, options.maxDate))
      .map(range => ({ source, range }))
  ))
  let completedRequests = 0
  const errors = []
  const batches = await mapWithConcurrency(requests, options.concurrency, async request => {
    try {
      const json = await readEspnRange(request, options)
      return parseEspnEvents(json, request.source)
    } catch (error) {
      errors.push({
        source: request.source.slug,
        range: request.range.key,
        message: error.message
      })
      return []
    } finally {
      completedRequests += 1
      if (completedRequests % 25 === 0 || completedRequests === requests.length) {
        console.error(`ESPN 历史数据进度 ${completedRequests}/${requests.length}`)
      }
    }
  })
  return { rows: batches.flat(), errors }
}

async function readInternationalSource(options, fileName) {
  if (options.sourceRoot) {
    const localPath = path.join(
      options.sourceRoot,
      'international-results-source/international_results-master',
      fileName
    )
    return await fs.readFile(localPath, 'utf8')
  }
  const cachePath = path.join(cacheRoot, 'international', fileName)
  if (!options.refreshCache) {
    try {
      return await fs.readFile(cachePath, 'utf8')
    } catch (error) {
      if (error.code !== 'ENOENT') {
        throw error
      }
    }
  }
  const url = `https://raw.githubusercontent.com/martj42/international_results/master/${fileName}`
  const text = await fetchTextWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, text, 'utf8')
  return text
}

function dateWithOffset(dateText, days) {
  const date = new Date(`${dateText}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

function fixturePairKey(date, homeTeam, awayTeam) {
  const teams = [canonicalChineseName(homeTeam), canonicalChineseName(awayTeam)].sort()
  return `${date}|${teams[0]}|${teams[1]}`
}

function fixtureResultKey(date, homeTeam, awayTeam, homeScore, awayScore) {
  const home = canonicalChineseName(homeTeam)
  const away = canonicalChineseName(awayTeam)
  if (home <= away) {
    return `${date}|${home}|${away}|${homeScore}|${awayScore}`
  }
  return `${date}|${away}|${home}|${awayScore}|${homeScore}`
}

function sourceMatchId(row) {
  if (row.provider === 'ESPN' && row.providerId) {
    return `ESPN-${row.providerId}`
  }
  const digest = crypto.createHash('sha1')
    .update([
      row.source,
      row.matchDate,
      canonicalName(row.homeTeam),
      canonicalName(row.awayTeam)
    ].join('|'))
    .digest('hex')
    .slice(0, 16)
    .toUpperCase()
  return `OPEN-${row.source.replaceAll(/[^A-Za-z0-9]+/g, '-').toUpperCase()}-${digest}`
}

function emptySourceSummary() {
  return {
    parsedRows: 0,
    addedRows: 0,
    updatedRows: 0,
    duplicateRows: 0,
    outsideDateRows: 0,
    outsideTargetRows: 0,
    unmappedRows: 0,
    correctedExtraTimeGoals: 0,
    unmappedTeams: new Map()
  }
}

function outputSourceSummaries(summaries, compact) {
  return Object.fromEntries([...summaries.entries()].map(([source, summary]) => [source, {
    parsedRows: summary.parsedRows,
    addedRows: summary.addedRows,
    updatedRows: summary.updatedRows,
    duplicateRows: summary.duplicateRows,
    outsideDateRows: summary.outsideDateRows,
    outsideTargetRows: summary.outsideTargetRows,
    unmappedRows: summary.unmappedRows,
    correctedExtraTimeGoals: summary.correctedExtraTimeGoals,
    ...(compact ? {} : {
      topUnmappedTeams: [...summary.unmappedTeams.entries()]
        .sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0]))
        .slice(0, 30)
        .map(([team, count]) => ({ team, count }))
    })
  }]))
}

const options = parseArguments(process.argv.slice(2))
const [historyText, mappingText, resultsText, goalsText, espnData] = await Promise.all([
  fs.readFile(historicalMatchesPath, 'utf8'),
  fs.readFile(teamNameMappingsPath, 'utf8'),
  readInternationalSource(options, 'results.csv'),
  readInternationalSource(options, 'goalscorers.csv'),
  loadEspnRows(options)
])

const originalRows = parseCsv(historyText).map(normalizeHistoryRow)
const retainedRows = originalRows.filter(row => (
  row.match_date >= options.minDate && row.match_date <= options.maxDate
))
const mappingRows = parseCsv(mappingText)
const nationalMappings = buildMappings(mappingRows, NATIONAL_COMPETITIONS)
const clubMappings = buildMappings(mappingRows, CLUB_COMPETITIONS)
const targetNationalTeams = new Set(originalRows
  .filter(row => NATIONAL_COMPETITIONS.has(row.competition))
  .flatMap(row => [row.home_team_cn, row.away_team_cn])
  .map(canonicalChineseName))
const targetClubTeams = new Set(originalRows
  .filter(row => CLUB_COMPETITIONS.has(row.competition))
  .flatMap(row => [row.home_team_cn, row.away_team_cn])
  .map(canonicalChineseName))

const sourceRows = [
  ...parseNationalRows(parseCsv(resultsText), parseCsv(goalsText)),
  ...espnData.rows
]
const rowsById = new Map(retainedRows.map(row => [row.match_id, row]))
const fixturePairs = new Set(retainedRows.map(row => fixturePairKey(
  row.match_date,
  row.home_team_cn,
  row.away_team_cn
)))
const fixtureResults = new Set(retainedRows.map(row => fixtureResultKey(
  row.match_date,
  row.home_team_cn,
  row.away_team_cn,
  row.home_score,
  row.away_score
)))
const sourceSummaries = new Map()

for (const sourceRow of sourceRows) {
  const summary = sourceSummaries.get(sourceRow.source) ?? emptySourceSummary()
  sourceSummaries.set(sourceRow.source, summary)
  summary.parsedRows += 1
  summary.correctedExtraTimeGoals += sourceRow.correctedExtraTimeGoals ?? 0
  if (!sourceRow.matchDate
      || sourceRow.matchDate < options.minDate
      || sourceRow.matchDate > options.maxDate) {
    summary.outsideDateRows += 1
    continue
  }

  const national = sourceRow.provider === 'INTL'
  const mappings = national ? nationalMappings : clubMappings
  const homeTeam = mappedChineseName(sourceRow.homeTeam, mappings, national)
  const awayTeam = mappedChineseName(sourceRow.awayTeam, mappings, national)
  const targets = national ? targetNationalTeams : targetClubTeams
  const homeIsTarget = homeTeam && targets.has(canonicalChineseName(homeTeam))
  const awayIsTarget = awayTeam && targets.has(canonicalChineseName(awayTeam))
  if (!homeIsTarget && !awayIsTarget) {
    summary.outsideTargetRows += 1
    continue
  }
  if (!homeTeam || !awayTeam) {
    summary.unmappedRows += 1
    for (const [sourceName, mappedName] of [
      [sourceRow.homeTeam, homeTeam],
      [sourceRow.awayTeam, awayTeam]
    ]) {
      if (!mappedName) {
        summary.unmappedTeams.set(sourceName, (summary.unmappedTeams.get(sourceName) ?? 0) + 1)
      }
    }
    continue
  }
  if (canonicalChineseName(homeTeam) === canonicalChineseName(awayTeam)) {
    summary.unmappedRows += 1
    continue
  }

  const matchId = sourceMatchId(sourceRow)
  const expectedRow = {
    match_id: matchId,
    match_date: sourceRow.matchDate,
    competition: sourceRow.competition,
    home_team_cn: homeTeam,
    away_team_cn: awayTeam,
    home_score: String(sourceRow.homeScore),
    away_score: String(sourceRow.awayScore),
    neutral: String(sourceRow.neutral),
    match_type: sourceRow.matchType,
    source_competition: sourceRow.sourceCompetition
  }
  const existingById = rowsById.get(matchId)
  if (existingById) {
    const changed = HISTORY_HEADERS.some(header => (
      String(existingById[header] ?? '') !== String(expectedRow[header] ?? '')
    ))
    if (changed) {
      Object.assign(existingById, expectedRow)
      summary.updatedRows += 1
    }
    continue
  }

  const exactPair = fixturePairKey(sourceRow.matchDate, homeTeam, awayTeam)
  const shiftedDuplicate = [-1, 1].some(offset => fixtureResults.has(fixtureResultKey(
    dateWithOffset(sourceRow.matchDate, offset),
    homeTeam,
    awayTeam,
    sourceRow.homeScore,
    sourceRow.awayScore
  )))
  if (fixturePairs.has(exactPair) || shiftedDuplicate) {
    summary.duplicateRows += 1
    continue
  }

  retainedRows.push(expectedRow)
  rowsById.set(matchId, expectedRow)
  fixturePairs.add(exactPair)
  fixtureResults.add(fixtureResultKey(
    sourceRow.matchDate,
    homeTeam,
    awayTeam,
    sourceRow.homeScore,
    sourceRow.awayScore
  ))
  summary.addedRows += 1
}

const rebuiltRows = retainedRows.sort((left, right) => (
  left.match_date.localeCompare(right.match_date)
  || left.competition.localeCompare(right.competition)
  || left.match_id.localeCompare(right.match_id)
))

if (options.write) {
  await fs.writeFile(historicalMatchesPath, toCsv(rebuiltRows), 'utf8')
  await import('./generate-team-name-mappings.mjs')
}

const matchTypes = Object.fromEntries([...new Set(rebuiltRows.map(row => row.match_type))]
  .sort()
  .map(matchType => [matchType, rebuiltRows.filter(row => row.match_type === matchType).length]))
const competitions = Object.fromEntries([...new Set(rebuiltRows.map(row => row.competition))]
  .sort()
  .map(competition => [competition, rebuiltRows.filter(row => row.competition === competition).length]))

console.log(JSON.stringify({
  minDate: options.minDate,
  maxDate: options.maxDate,
  originalRows: originalRows.length,
  removedBeforeOrAfterRange: originalRows.length - originalRows.filter(row => (
    row.match_date >= options.minDate && row.match_date <= options.maxDate
  )).length,
  rebuiltRows: rebuiltRows.length,
  importedRows: [...sourceSummaries.values()].reduce((sum, summary) => sum + summary.addedRows, 0),
  updatedRows: [...sourceSummaries.values()].reduce((sum, summary) => sum + summary.updatedRows, 0),
  targetNationalTeams: targetNationalTeams.size,
  targetClubTeams: targetClubTeams.size,
  matchTypes,
  competitions,
  sources: outputSourceSummaries(sourceSummaries, options.compact),
  espnRequestErrors: espnData.errors,
  wroteFile: options.write
}, null, 2))
