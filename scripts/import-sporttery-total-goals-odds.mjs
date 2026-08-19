import fs from 'node:fs/promises'
import path from 'node:path'

const RESULT_API = 'https://webapi.sporttery.cn/gateway/uniform/football/getUniformMatchResultV1.qry'
const ODDS_API = 'https://webapi.sporttery.cn/gateway/uniform/football/getOddsHistoryV1.qry'
const CALCULATOR_API = 'https://webapi.sporttery.cn/gateway/uniform/football/getMatchCalculatorV1.qry'
const DEFAULT_START_DATE = '2024-06-14'
const DEFAULT_CONCURRENCY = 6
const MAX_RESULT_RANGE_DAYS = 30
const TOTAL_GOALS_COLUMNS = [
  'total_goals_0',
  'total_goals_1',
  'total_goals_2',
  'total_goals_3',
  'total_goals_4',
  'total_goals_5',
  'total_goals_6',
  'total_goals_7_plus',
  'total_goals_updated_at',
  'total_goals_source_match_id'
]
const LEAGUE_ID_COMPETITIONS = new Map([
  ['72', 'WORLD_CUP'],
  ['27', 'EUROPEAN_CHAMPIONSHIP'],
  ['13', 'COPA_AMERICA'],
  ['14', 'CLUB_WORLD_CUP'],
  ['70', 'EUROPA_LEAGUE'],
  ['69', 'CHAMPIONS_LEAGUE'],
  ['25', 'PREMIER_LEAGUE'],
  ['62', 'LA_LIGA'],
  ['40', 'SERIE_A'],
  ['37', 'BUNDESLIGA'],
  ['32', 'LIGUE_1'],
  ['55', 'PRIMEIRA_LIGA'],
  ['17', 'EREDIVISIE'],
  ['77', 'ARGENTINE_PRIMERA_DIVISION']
])
const LEAGUE_NAME_COMPETITIONS = new Map([
  ['世界杯', 'WORLD_CUP'],
  ['欧洲杯', 'EUROPEAN_CHAMPIONSHIP'],
  ['美洲杯', 'COPA_AMERICA'],
  ['世俱杯', 'CLUB_WORLD_CUP'],
  ['俱世界杯', 'CLUB_WORLD_CUP'],
  ['欧罗巴', 'EUROPA_LEAGUE'],
  ['欧冠', 'CHAMPIONS_LEAGUE'],
  ['英超', 'PREMIER_LEAGUE'],
  ['西甲', 'LA_LIGA'],
  ['意甲', 'SERIE_A'],
  ['德甲', 'BUNDESLIGA'],
  ['法甲', 'LIGUE_1'],
  ['葡超', 'PRIMEIRA_LIGA'],
  ['荷甲', 'EREDIVISIE'],
  ['阿甲', 'ARGENTINE_PRIMERA_DIVISION'],
  ['瑞超', 'SWEDISH_ALLSVENSKAN'],
  ['瑞典超', 'SWEDISH_ALLSVENSKAN'],
  ['芬超', 'FINNISH_VEIKKAUSLIIGA'],
  ['韩职', 'K_LEAGUE_1'],
  ['韩国职业联赛', 'K_LEAGUE_1'],
  ['韩国杯', 'K_LEAGUE_1'],
  ['韩足总杯', 'K_LEAGUE_1'],
  ['苏足总杯', 'SCOTTISH_FA_CUP'],
  ['苏格兰足总杯', 'SCOTTISH_FA_CUP'],
  ['苏格兰杯', 'SCOTTISH_FA_CUP']
])

function parseArgs(argv) {
  const args = {
    startDate: DEFAULT_START_DATE,
    endDate: formatDate(addDays(new Date(), 7)),
    csvPath: path.resolve('src/main/resources/data/historical_odds_data.csv'),
    mappingsPath: path.resolve('src/main/resources/data/team_name_mappings.csv'),
    concurrency: DEFAULT_CONCURRENCY,
    reportPath: path.resolve('reports/sporttery-total-goals-import.json')
  }
  for (const argument of argv) {
    if (argument.startsWith('--start=')) {
      args.startDate = argument.slice('--start='.length)
    } else if (argument.startsWith('--end=')) {
      args.endDate = argument.slice('--end='.length)
    } else if (argument.startsWith('--csv=')) {
      args.csvPath = path.resolve(argument.slice('--csv='.length))
    } else if (argument.startsWith('--mappings=')) {
      args.mappingsPath = path.resolve(argument.slice('--mappings='.length))
    } else if (argument.startsWith('--concurrency=')) {
      args.concurrency = Math.max(1, Number(argument.slice('--concurrency='.length)) || DEFAULT_CONCURRENCY)
    } else if (argument.startsWith('--report=')) {
      args.reportPath = path.resolve(argument.slice('--report='.length))
    }
  }
  validateDate(args.startDate, '--start')
  validateDate(args.endDate, '--end')
  if (args.endDate < args.startDate) {
    throw new Error('--end 不能早于 --start')
  }
  return args
}

function validateDate(value, name) {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value) || Number.isNaN(Date.parse(value + 'T00:00:00Z'))) {
    throw new Error(`${name} 必须是 yyyy-MM-dd 日期`)
  }
}

function addDays(date, days) {
  const result = new Date(date)
  result.setUTCDate(result.getUTCDate() + days)
  return result
}

function formatDate(date) {
  return date.toISOString().slice(0, 10)
}

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let quoted = false
  for (let index = 0; index < text.length; index += 1) {
    const character = text[index]
    if (quoted) {
      if (character === '"' && text[index + 1] === '"') {
        field += '"'
        index += 1
      } else if (character === '"') {
        quoted = false
      } else {
        field += character
      }
    } else if (character === '"') {
      quoted = true
    } else if (character === ',') {
      row.push(field)
      field = ''
    } else if (character === '\n') {
      row.push(field)
      rows.push(row)
      row = []
      field = ''
    } else if (character !== '\r') {
      field += character
    }
  }
  if (field.length > 0 || row.length > 0) {
    row.push(field)
    rows.push(row)
  }
  return rows
}

function formatCsv(rows) {
  return rows.map(row => row.map(formatCsvValue).join(',')).join('\r\n') + '\r\n'
}

function formatCsvValue(value) {
  const text = value == null ? '' : String(value)
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function canonicalName(value) {
  return String(value || '')
    .normalize('NFKC')
    .toUpperCase()
    .replaceAll('足球俱乐部', '')
    .replaceAll('俱乐部', '')
    .replace(/[\s·•.．,，'’`´()（）\[\]【】\-_/&]+/g, '')
    .replace(/^(FC|SC|CF)(?=\p{Script=Han})/u, '')
    .replace(/(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$/u, '')
}

function buildAliasMaps(mappingRows) {
  const headers = mappingRows[0]
  const indexes = indexColumns(headers)
  const maps = new Map()
  for (const row of mappingRows.slice(1)) {
    const competition = row[indexes.competition] || '*'
    const standardName = canonicalName(row[indexes.standard_team_name])
    const aliasName = canonicalName(row[indexes.alias_team_name])
    if (!standardName || !aliasName) {
      continue
    }
    if (!maps.has(competition)) {
      maps.set(competition, new Map())
    }
    maps.get(competition).set(aliasName, standardName)
  }
  return maps
}

function resolveStandardTeamName(aliasMaps, competition, teamName) {
  const normalized = canonicalName(teamName)
  const globalAliases = aliasMaps.get('*')
  const globalName = globalAliases?.get(normalized)
  if (globalName && globalName !== normalized) {
    return globalName
  }
  const competitionName = aliasMaps.get(competition)?.get(normalized)
  return globalAliases?.get(competitionName)
    || competitionName
    || globalName
    || normalized
}

function indexColumns(headers) {
  return Object.fromEntries(headers.map((header, index) => [
    String(header || '').replace(/^\uFEFF/, '').trim(),
    index
  ]))
}

function resolveCompetition(match) {
  const leagueId = String(match.leagueId ?? '')
  if (LEAGUE_ID_COMPETITIONS.has(leagueId)) {
    return LEAGUE_ID_COMPETITIONS.get(leagueId)
  }
  const leagueName = match.leagueNameAbbr || match.leagueAbbName || ''
  return LEAGUE_NAME_COMPETITIONS.get(leagueName) || null
}

function buildFixtureKey(aliasMaps, competition, matchDate, homeTeam, awayTeam) {
  return [
    competition,
    matchDate,
    resolveStandardTeamName(aliasMaps, competition, homeTeam),
    resolveStandardTeamName(aliasMaps, competition, awayTeam)
  ].join('|')
}

function buildCompetitionDateKey(competition, matchDate) {
  return `${competition}|${matchDate}`
}

function editDistance(left, right) {
  const previous = Array.from({ length: right.length + 1 }, (_, index) => index)
  for (let leftIndex = 1; leftIndex <= left.length; leftIndex += 1) {
    const current = [leftIndex]
    for (let rightIndex = 1; rightIndex <= right.length; rightIndex += 1) {
      const substitutionCost = left[leftIndex - 1] === right[rightIndex - 1] ? 0 : 1
      current[rightIndex] = Math.min(
        current[rightIndex - 1] + 1,
        previous[rightIndex] + 1,
        previous[rightIndex - 1] + substitutionCost
      )
    }
    previous.splice(0, previous.length, ...current)
  }
  return previous[right.length]
}

function teamNameDistance(left, right) {
  if (left === right) {
    return 0
  }
  if (left.includes(right) || right.includes(left)) {
    return Math.abs(left.length - right.length)
  }
  return editDistance(left, right)
}

function isCloseTeamName(left, right) {
  const longestLength = Math.max(left.length, right.length)
  return longestLength > 0
    && teamNameDistance(left, right) <= Math.max(1, Math.ceil(longestLength * 0.25))
}

function findFuzzyCsvMatch(aliasMaps, match, candidates) {
  const officialHome = resolveStandardTeamName(aliasMaps, match.competition, match.homeTeam)
  const officialAway = resolveStandardTeamName(aliasMaps, match.competition, match.awayTeam)
  const ranked = candidates.map(candidate => {
    const csvHome = resolveStandardTeamName(aliasMaps, match.competition, candidate.homeTeam)
    const csvAway = resolveStandardTeamName(aliasMaps, match.competition, candidate.awayTeam)
    return {
      candidate,
      homeDistance: teamNameDistance(officialHome, csvHome),
      awayDistance: teamNameDistance(officialAway, csvAway),
      close: isCloseTeamName(officialHome, csvHome) && isCloseTeamName(officialAway, csvAway)
    }
  }).filter(item => item.close)
    .sort((left, right) => (
      left.homeDistance + left.awayDistance - right.homeDistance - right.awayDistance
    ))
  if (ranked.length === 0) {
    return null
  }
  const bestScore = ranked[0].homeDistance + ranked[0].awayDistance
  const nextScore = ranked[1]
    ? ranked[1].homeDistance + ranked[1].awayDistance
    : Number.POSITIVE_INFINITY
  return bestScore < nextScore ? ranked[0].candidate : null
}

async function fetchJson(url, headers, attempts = 4) {
  let lastError
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    try {
      const response = await fetch(url, { headers, signal: AbortSignal.timeout(30000) })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const root = await response.json()
      if (String(root.errorCode) !== '0') {
        throw new Error(root.errorMessage || '体彩接口返回失败')
      }
      return root
    } catch (error) {
      lastError = error
      if (attempt < attempts) {
        await new Promise(resolve => setTimeout(resolve, attempt * 350))
      }
    }
  }
  throw lastError
}

async function downloadResults(startDate, endDate) {
  const matches = []
  let cursor = new Date(startDate + 'T00:00:00Z')
  const limit = new Date(endDate + 'T00:00:00Z')
  while (cursor <= limit) {
    const rangeStart = formatDate(cursor)
    const rangeEnd = formatDate(new Date(Math.min(
      addDays(cursor, MAX_RESULT_RANGE_DAYS - 1).getTime(),
      limit.getTime()
    )))
    let pageNo = 1
    let pages = 1
    let rangeMatchCount = 0
    do {
      const params = new URLSearchParams({
        matchBeginDate: rangeStart,
        matchEndDate: rangeEnd,
        leagueId: '',
        pageSize: '100',
        pageNo: String(pageNo),
        isFix: '0',
        matchPage: '1',
        pcOrWap: '1'
      })
      const root = await fetchJson(`${RESULT_API}?${params}`, {
        Accept: 'application/json,text/plain,*/*',
        Origin: 'https://www.lottery.gov.cn',
        Referer: 'https://www.lottery.gov.cn/jc/zqsgkj/',
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36'
      })
      const pageMatches = root.value?.matchResult || []
      matches.push(...pageMatches)
      rangeMatchCount += pageMatches.length
      pages = Math.max(1, Number(root.value?.pages) || 1)
      pageNo += 1
    } while (pageNo <= pages)
    console.error(`赛果 ${rangeStart} 至 ${rangeEnd}: ${rangeMatchCount} 场`)
    cursor = addDays(new Date(rangeEnd + 'T00:00:00Z'), 1)
  }
  return matches
}

async function downloadCalculatorMatches() {
  const root = await fetchJson(`${CALCULATOR_API}?channel=c`, {
    Accept: 'application/json,text/plain,*/*',
    Origin: 'https://www.sporttery.cn',
    Referer: 'https://www.sporttery.cn/jc/jsq/zqzjq/',
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36'
  })
  return (root.value?.matchInfoList || []).flatMap(group => group.subMatchList || [])
}

function normalizeOfficialMatch(match) {
  const competition = resolveCompetition(match)
  const matchId = String(match.matchId || '')
  const matchDate = match.matchDate || ''
  const homeTeam = match.allHomeTeam || match.homeTeamAllName || match.homeTeam || match.homeTeamAbbName || ''
  const awayTeam = match.allAwayTeam || match.awayTeamAllName || match.awayTeam || match.awayTeamAbbName || ''
  const score = parseOfficialScore(match)
  const neutral = parseOfficialNeutral(match)
  return competition && matchId && matchDate && homeTeam && awayTeam
    ? {
        competition,
        matchId,
        matchDate,
        homeTeam,
        awayTeam,
        homeScore: score?.homeScore ?? null,
        awayScore: score?.awayScore ?? null,
        neutral,
        sportteryMatchNumber: match.matchNumStr || match.matchNum || ''
      }
    : null
}

function parseOfficialScore(match) {
  const scoreText = match.sectionsNo999 || match.fullScore || match.score || ''
  const matchResult = String(scoreText).match(/^(\d+)\s*[:：-]\s*(\d+)$/)
  if (matchResult) {
    return {
      homeScore: Number(matchResult[1]),
      awayScore: Number(matchResult[2])
    }
  }
  if (match.homeScore == null || match.awayScore == null
      || match.homeScore === '' || match.awayScore === '') {
    return null
  }
  const homeScore = Number(match.homeScore)
  const awayScore = Number(match.awayScore)
  return Number.isInteger(homeScore) && Number.isInteger(awayScore)
    ? { homeScore, awayScore }
    : null
}

function parseOfficialNeutral(match) {
  const value = match.neutral ?? match.isNeutral ?? match.neutralFlag
  if (value == null || value === '') {
    return null
  }
  return value === true || value === 1 || value === '1' || String(value).toLowerCase() === 'true'
}

function mergeOfficialMatches(existing, candidate) {
  if (!existing) {
    return candidate
  }
  return {
    ...existing,
    ...candidate,
    homeScore: candidate.homeScore ?? existing.homeScore,
    awayScore: candidate.awayScore ?? existing.awayScore,
    neutral: candidate.neutral ?? existing.neutral,
    sportteryMatchNumber: candidate.sportteryMatchNumber || existing.sportteryMatchNumber
  }
}

async function downloadInitialOdds(matchId) {
  const root = await fetchJson(`${ODDS_API}?matchId=${encodeURIComponent(matchId)}`, {
    Accept: 'application/json,text/plain,*/*',
    Origin: 'https://www.sporttery.cn',
    Referer: 'https://www.sporttery.cn/jc/jsq/zqzjq/',
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/138.0.0.0 Safari/537.36'
  })
  return {
    normal: selectInitialOdds(root.value?.hadList, hasCompleteThreeWayOdds),
    handicap: selectInitialOdds(root.value?.hhadList, hasCompleteThreeWayOdds),
    totalGoals: selectInitialOdds(root.value?.ttgList, hasCompleteTotalGoalsOdds)
  }
}

function selectInitialOdds(history, predicate) {
  return (Array.isArray(history) ? history : [])
    .filter(predicate)
    .sort((left, right) => oddsUpdatedAt(left).localeCompare(oddsUpdatedAt(right)))[0] || null
}

function hasCompleteThreeWayOdds(item) {
  return ['h', 'd', 'a'].map(key => Number(item?.[key]))
    .every(value => Number.isFinite(value) && value > 0)
}

function hasCompleteTotalGoalsOdds(item) {
  return Array.from({ length: 8 }, (_, index) => Number(item?.['s' + index]))
    .every(value => Number.isFinite(value) && value > 0)
}

function oddsUpdatedAt(item) {
  return [item?.updateDate || '', item?.updateTime || ''].filter(Boolean).join(' ')
}

function createCsvMatch(csvHeaders, csvIndexes, match) {
  const row = Array.from({ length: csvHeaders.length }, () => '')
  row[csvIndexes.match_id] = `HIS-SPT-${match.matchId}`
  row[csvIndexes.match_date] = match.matchDate
  row[csvIndexes.competition] = match.competition
  row[csvIndexes.home_team_cn] = match.homeTeam
  row[csvIndexes.away_team_cn] = match.awayTeam
  applyOfficialMetadata(row, csvIndexes, match)
  return {
    row,
    rowNumber: null,
    homeTeam: match.homeTeam,
    awayTeam: match.awayTeam
  }
}

function applyOfficialMetadata(row, csvIndexes, match) {
  if (match.homeScore != null && match.awayScore != null) {
    row[csvIndexes.home_score] = String(match.homeScore)
    row[csvIndexes.away_score] = String(match.awayScore)
  }
  if (!row[csvIndexes.neutral]) {
    row[csvIndexes.neutral] = String(match.neutral ?? false)
  }
  if (match.sportteryMatchNumber) {
    row[csvIndexes.sporttery_match_number] = match.sportteryMatchNumber
  }
}

function needsTotalGoalsRefresh(row, csvIndexes) {
  return !hasCompleteCsvOdds(row, csvIndexes, TOTAL_GOALS_COLUMNS.slice(0, 8))
}

function hasCompleteCsvOdds(row, csvIndexes, columns) {
  return columns.map(column => Number(row[csvIndexes[column]]))
    .every(value => Number.isFinite(value) && value > 0)
}

function applyInitialOdds(row, csvIndexes, odds, matchId) {
  if (odds.normal) {
    row[csvIndexes.normal_win] = String(odds.normal.h)
    row[csvIndexes.normal_draw] = String(odds.normal.d)
    row[csvIndexes.normal_lose] = String(odds.normal.a)
  }
  if (odds.handicap) {
    row[csvIndexes.handicap] = String(Number(odds.handicap.goalLine || 0))
    row[csvIndexes.handicap_win] = String(odds.handicap.h)
    row[csvIndexes.handicap_draw] = String(odds.handicap.d)
    row[csvIndexes.handicap_lose] = String(odds.handicap.a)
  }
  if (!odds.totalGoals) {
    return false
  }
  for (let goal = 0; goal <= 7; goal += 1) {
    row[csvIndexes[TOTAL_GOALS_COLUMNS[goal]]] = String(odds.totalGoals['s' + goal])
  }
  row[csvIndexes.total_goals_updated_at] = oddsUpdatedAt(odds.totalGoals)
  row[csvIndexes.total_goals_source_match_id] = matchId
  return true
}

async function mapLimit(items, limit, iterator) {
  const results = new Array(items.length)
  let cursor = 0
  const workers = Array.from({ length: Math.min(limit, items.length) }, async () => {
    while (cursor < items.length) {
      const index = cursor
      cursor += 1
      results[index] = await iterator(items[index], index)
    }
  })
  await Promise.all(workers)
  return results
}

async function main() {
  const args = parseArgs(process.argv.slice(2))
  const [csvText, mappingsText] = await Promise.all([
    fs.readFile(args.csvPath, 'utf8'),
    fs.readFile(args.mappingsPath, 'utf8')
  ])
  const csvRows = parseCsv(csvText)
  const mappingRows = parseCsv(mappingsText)
  const csvIndexes = indexColumns(csvRows[0])
  const aliasMaps = buildAliasMaps(mappingRows)

  for (const column of TOTAL_GOALS_COLUMNS) {
    if (!(column in csvIndexes)) {
      csvIndexes[column] = csvRows[0].length
      csvRows[0].push(column)
    }
  }
  for (const row of csvRows.slice(1)) {
    while (row.length < csvRows[0].length) {
      row.push('')
    }
  }

  const csvRowsByFixture = new Map()
  const csvRowsByCompetitionDate = new Map()
  csvRows.slice(1).forEach((row, rowOffset) => {
    const matchDate = row[csvIndexes.match_date]
    if (matchDate < args.startDate || matchDate > args.endDate) {
      return
    }
    const competition = row[csvIndexes.competition]
    const fixtureKey = buildFixtureKey(
      aliasMaps,
      competition,
      matchDate,
      row[csvIndexes.home_team_cn] || row[csvIndexes.home_team_en],
      row[csvIndexes.away_team_cn] || row[csvIndexes.away_team_en]
    )
    const csvMatch = {
      row,
      rowNumber: rowOffset + 2,
      homeTeam: row[csvIndexes.home_team_cn] || row[csvIndexes.home_team_en],
      awayTeam: row[csvIndexes.away_team_cn] || row[csvIndexes.away_team_en]
    }
    if (!csvRowsByFixture.has(fixtureKey)) {
      csvRowsByFixture.set(fixtureKey, [])
    }
    csvRowsByFixture.get(fixtureKey).push(csvMatch)
    const competitionDateKey = buildCompetitionDateKey(competition, matchDate)
    if (!csvRowsByCompetitionDate.has(competitionDateKey)) {
      csvRowsByCompetitionDate.set(competitionDateKey, [])
    }
    csvRowsByCompetitionDate.get(competitionDateKey).push(csvMatch)
  })

  const [historicalMatches, calculatorMatches] = await Promise.all([
    downloadResults(args.startDate, args.endDate),
    downloadCalculatorMatches().catch(error => {
      console.error(`在售接口读取失败: ${error.message}`)
      return []
    })
  ])
  const officialMatchesById = new Map()
  for (const sourceMatch of historicalMatches.concat(calculatorMatches)) {
    const match = normalizeOfficialMatch(sourceMatch)
    if (!match || match.matchDate < args.startDate || match.matchDate > args.endDate) {
      continue
    }
    officialMatchesById.set(
      match.matchId,
      mergeOfficialMatches(officialMatchesById.get(match.matchId), match)
    )
  }

  const matchedOfficialMatches = []
  const appendedOfficialMatches = []
  for (const match of officialMatchesById.values()) {
    const fixtureKey = buildFixtureKey(
      aliasMaps,
      match.competition,
      match.matchDate,
      match.homeTeam,
      match.awayTeam
    )
    const csvMatches = csvRowsByFixture.get(fixtureKey) || []
    const exactCsvMatch = csvMatches.length === 1 ? csvMatches[0] : null
    const fuzzyCsvMatch = exactCsvMatch ? null : findFuzzyCsvMatch(
      aliasMaps,
      match,
      csvRowsByCompetitionDate.get(buildCompetitionDateKey(match.competition, match.matchDate)) || []
    )
    let csvMatch = exactCsvMatch || fuzzyCsvMatch
    let appended = false
    if (!csvMatch) {
      csvMatch = createCsvMatch(csvRows[0], csvIndexes, match)
      csvRows.push(csvMatch.row)
      appended = true
      appendedOfficialMatches.push({
        competition: match.competition,
        matchId: match.matchId,
        matchDate: match.matchDate,
        homeTeam: match.homeTeam,
        awayTeam: match.awayTeam
      })
    }
    applyOfficialMetadata(csvMatch.row, csvIndexes, match)
    matchedOfficialMatches.push({
      ...match,
      csvMatch,
      appended,
      matchMethod: appended ? 'APPENDED' : exactCsvMatch ? 'EXACT' : 'FUZZY'
    })
  }

  const oddsTargets = matchedOfficialMatches.filter(match => (
    match.appended || needsTotalGoalsRefresh(match.csvMatch.row, csvIndexes)
  ))
  console.error(
    `体彩目标赛事 ${officialMatchesById.size} 场，` +
    `匹配既有 CSV ${matchedOfficialMatches.length - appendedOfficialMatches.length} 场，` +
    `新增 ${appendedOfficialMatches.length} 场，待补赔率 ${oddsTargets.length} 场`
  )
  let completedCount = 0
  const failures = []
  const oddsResults = await mapLimit(oddsTargets, args.concurrency, async match => {
    try {
      const odds = await downloadInitialOdds(match.matchId)
      completedCount += 1
      if (completedCount % 100 === 0 || completedCount === oddsTargets.length) {
        console.error(`初盘赔率 ${completedCount}/${oddsTargets.length}`)
      }
      return { match, odds }
    } catch (error) {
      failures.push({ matchId: match.matchId, message: error.message })
      completedCount += 1
      return { match, odds: null }
    }
  })

  let updatedCount = 0
  let unavailableCount = 0
  for (const { match, odds } of oddsResults) {
    if (!odds || !odds.totalGoals) {
      unavailableCount += 1
    }
    const row = match.csvMatch.row
    if (odds && applyInitialOdds(row, csvIndexes, odds, match.matchId)) {
      updatedCount += 1
    }
  }

  const csvDataRows = csvRows.slice(1)
  csvDataRows.sort((left, right) => (
    String(left[csvIndexes.match_date]).localeCompare(String(right[csvIndexes.match_date]))
  ))
  csvRows.splice(1, csvRows.length - 1, ...csvDataRows)
  await fs.writeFile(args.csvPath, formatCsv(csvRows), 'utf8')
  const totalGoalsOddsMatchCount = csvRows.slice(1).filter(row => (
    row[csvIndexes.match_date] >= args.startDate
    && row[csvIndexes.match_date] <= args.endDate
    && hasCompleteCsvOdds(row, csvIndexes, TOTAL_GOALS_COLUMNS.slice(0, 8))
  )).length
  const report = {
    generatedAt: new Date().toISOString(),
    startDate: args.startDate,
    endDate: args.endDate,
    csvRowCount: csvRows.length - 1,
    officialMatchCount: officialMatchesById.size,
    matchedCsvMatchCount: matchedOfficialMatches.length,
    matchedExistingCsvMatchCount: matchedOfficialMatches.length - appendedOfficialMatches.length,
    appendedCsvMatchCount: appendedOfficialMatches.length,
    oddsRefreshTargetCount: oddsTargets.length,
    updatedTotalGoalsMatchCount: updatedCount,
    totalGoalsOddsMatchCount,
    unavailableTotalGoalsMatchCount: unavailableCount,
    failedRequestCount: failures.length,
    unmatchedOfficialMatches: [],
    appendedOfficialMatches,
    failures
  }
  await fs.mkdir(path.dirname(args.reportPath), { recursive: true })
  await fs.writeFile(args.reportPath, JSON.stringify(report, null, 2) + '\n', 'utf8')
  console.log(JSON.stringify(report, null, 2))
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
