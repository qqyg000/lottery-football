import crypto from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const teamNameMappingsPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')
const cacheRoot = path.join(root, 'target/public-history-cache')
const MINIMUM_HISTORY_DATE = '2014-06-12'

const COMPETITION_NAMES = new Map(Object.entries({
  WORLD_CUP: '世界杯',
  EUROPEAN_CHAMPIONSHIP: '欧洲杯',
  COPA_AMERICA: '美洲杯',
  CLUB_WORLD_CUP: '世俱杯',
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

const WORLD_CUP_YEARS = [
  1930, 1934, 1938, 1950, 1954, 1958, 1962, 1966, 1970, 1974, 1978, 1982,
  1986, 1990, 1994, 1998, 2002, 2006, 2010, 2014, 2018, 2022, 2026
]

const CLUB_WORLD_CUP_YEARS = [
  2000, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014,
  2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2025
]

const MONTHS = new Map([
  ['JAN', 1], ['FEB', 2], ['MAR', 3], ['APR', 4], ['MAY', 5], ['JUN', 6],
  ['JUL', 7], ['AUG', 8], ['SEP', 9], ['OCT', 10], ['NOV', 11], ['DEC', 12]
])


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

function parseArguments(argv) {
  const options = {
    write: false,
    sourceRoot: null,
    minDate: MINIMUM_HISTORY_DATE,
    maxDate: localDate(new Date())
  }
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index]
    if (argument === '--write') {
      options.write = true
    } else if (argument === '--source-root') {
      options.sourceRoot = path.resolve(requiredArgument(argv, ++index, '--source-root'))
    } else if (argument === '--min-date') {
      options.minDate = requiredArgument(argv, ++index, '--min-date')
    } else if (argument === '--max-date') {
      options.maxDate = requiredArgument(argv, ++index, '--max-date')
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
      continue
    }
    if (character === '"') {
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

function normalizeHistoricalRow(row) {
  const competition = String(row.competition ?? '').trim()
  return {
    ...row,
    match_type: String(row.match_type ?? '').trim() || 'OFFICIAL',
    source_competition: String(row.source_competition ?? '').trim()
      || COMPETITION_NAMES.get(competition)
      || competition
  }
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
    .replace(/^(AFC|AC|AS|CA|CD|CF|FC|FK|SC|SV|TSG)\s+/u, '')
    .replace(/\s+(AFC|AC|AS|CA|CD|CF|FC|FK|SC|SV|TSG)$/u, '')
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

function buildMappings(mappingRows) {
  const mappings = new Map()
  for (const row of mappingRows) {
    const competition = String(row.competition ?? '').trim()
    const aliasName = String(row.alias_team_name ?? '').trim()
    const standardName = String(row.standard_team_name ?? '').trim()
    if (!competition || !aliasName || !standardName) {
      continue
    }
    for (const nameKey of nameKeys(aliasName)) {
      mappings.set(`${competition}|${nameKey}`, standardName)
    }
  }
  return mappings
}

function mappedChineseName(competition, sourceName, mappings) {
  for (const nameKey of nameKeys(sourceName)) {
    const mapped = mappings.get(`${competition}|${nameKey}`) ?? mappings.get(`*|${nameKey}`)
    if (mapped) {
      return mapped
    }
  }
  return null
}

function dateWithOffset(dateText, days) {
  if (days === 0) {
    return dateText
  }
  const date = new Date(`${dateText}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

function fixtureKey(competition, date, homeTeam, awayTeam) {
  const teams = [canonicalChineseName(homeTeam), canonicalChineseName(awayTeam)].sort()
  return `${competition}|${date}|${teams[0]}|${teams[1]}`
}

function sourceMatchId(row) {
  const digest = crypto.createHash('sha1')
    .update([
      row.source,
      row.competition,
      row.matchDate,
      canonicalName(row.homeTeam),
      canonicalName(row.awayTeam)
    ].join('|'))
    .digest('hex')
    .slice(0, 16)
    .toUpperCase()
  return `OPEN-${row.source}-${digest}`
}

function scorePair(value) {
  const match = String(value ?? '').match(/(\d+)\s*-\s*(\d+)/)
  return match ? [Number(match[1]), Number(match[2])] : null
}

function parseUclDate(value) {
  const match = String(value ?? '').match(/\)\s+(\d{1,2})\s+([A-Za-z]{3})\s+(\d{4})/)
  if (!match) {
    return null
  }
  const month = MONTHS.get(match[2].toUpperCase())
  if (!month) {
    return null
  }
  return `${match[3]}-${String(month).padStart(2, '0')}-${String(match[1]).padStart(2, '0')}`
}

function stripUclTeam(value) {
  return String(value ?? '').split('›')[0].trim()
}

function parseBoolean(value) {
  return String(value ?? '').trim().toLowerCase() === 'true'
}

function seasonName(startYear) {
  return `${startYear}-${String((startYear + 1) % 100).padStart(2, '0')}`
}

function parseWorldCup(text) {
  const tournament = JSON.parse(text)
  return (tournament.matches ?? []).flatMap(match => {
    const fullTime = match.score?.ft
    if (!Array.isArray(fullTime) || fullTime.length !== 2 || !fullTime.every(Number.isFinite)) {
      return []
    }
    return [{
      source: 'WC',
      competition: 'WORLD_CUP',
      matchDate: match.date,
      homeTeam: match.team1,
      awayTeam: match.team2,
      homeScore: fullTime[0],
      awayScore: fullTime[1],
      neutral: true,
      allowNationalFallback: true
    }]
  })
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

function parseNationalTournaments(resultRows, goalRows) {
  const competitionByTournament = new Map([
    ['UEFA Euro', 'EUROPEAN_CHAMPIONSHIP'],
    ['Copa América', 'COPA_AMERICA']
  ])
  const extraTimeGoals = extraTimeGoalCounts(goalRows)
  const rows = []
  for (const result of resultRows) {
    const competition = competitionByTournament.get(result.tournament)
    if (!competition) {
      continue
    }
    const rawHomeScore = Number(result.home_score)
    const rawAwayScore = Number(result.away_score)
    const homeExtraTimeGoals = extraTimeGoals.get([
      result.date, result.home_team, result.away_team, result.home_team
    ].join('|')) ?? 0
    const awayExtraTimeGoals = extraTimeGoals.get([
      result.date, result.home_team, result.away_team, result.away_team
    ].join('|')) ?? 0
    const homeScore = rawHomeScore - homeExtraTimeGoals
    const awayScore = rawAwayScore - awayExtraTimeGoals
    if (!Number.isInteger(homeScore) || !Number.isInteger(awayScore) || homeScore < 0 || awayScore < 0) {
      continue
    }
    rows.push({
      source: competition === 'EUROPEAN_CHAMPIONSHIP' ? 'EURO' : 'COPA',
      competition,
      matchDate: result.date,
      homeTeam: result.home_team,
      awayTeam: result.away_team,
      homeScore,
      awayScore,
      neutral: parseBoolean(result.neutral),
      allowNationalFallback: true,
      correctedExtraTimeGoals: homeExtraTimeGoals + awayExtraTimeGoals
    })
  }
  return rows
}

function parseChampionsLeague(text) {
  return parseCsv(text).flatMap(match => {
    const date = parseUclDate(match.Date)
    const score = scorePair(match.FT)
    const homeTeam = stripUclTeam(match['Team 1'])
    const awayTeam = stripUclTeam(match['Team 2'])
    if (!date || !score || !homeTeam || !awayTeam) {
      return []
    }
    return [{
      source: 'UCL',
      competition: 'CHAMPIONS_LEAGUE',
      matchDate: date,
      homeTeam,
      awayTeam,
      homeScore: score[0],
      awayScore: score[1],
      neutral: /^final\b/i.test(String(match.Round ?? '').trim()),
      allowNationalFallback: false
    }]
  })
}

function parseClubWorldCup(text, fileYear) {
  const rows = []
  let currentDate = null
  for (const rawLine of text.split(/\r?\n/)) {
    const line = rawLine.trim()
    const dateMatch = line.match(/^(?:Sun|Mon|Tue|Wed|Thu|Fri|Sat)\s+([A-Za-z]{3})\s+(\d{1,2})(?:\s+(\d{4}))?$/)
    if (dateMatch) {
      const month = MONTHS.get(dateMatch[1].toUpperCase())
      const year = Number(dateMatch[3] ?? fileYear)
      currentDate = month
        ? `${year}-${String(month).padStart(2, '0')}-${String(dateMatch[2]).padStart(2, '0')}`
        : null
      continue
    }
    if (!currentDate || !line.includes(' v ')) {
      continue
    }
    const withoutTime = line.replace(/^\d{1,2}:\d{2}\s+/, '')
    const match = withoutTime.match(/^(.+?)\s+v\s+(.+?)\s+((?:\d+\s*-\s*\d+\s+pen\.\s+)?\d+\s*-\s*\d+(?:\s+a\.e\.t\.)?(?:\s+\([^)]*\))?)$/)
    if (!match) {
      continue
    }
    const scoreText = match[3]
    let score = null
    if (scoreText.includes('pen.')) {
      const parenthetical = scoreText.match(/\((\d+\s*-\s*\d+)/)
      score = scorePair(parenthetical?.[1])
    } else if (scoreText.includes('a.e.t.')) {
      const regulation = scoreText.match(/\((\d+\s*-\s*\d+)/)
      score = scorePair(regulation?.[1])
    } else {
      score = scorePair(scoreText)
    }
    if (!score) {
      continue
    }
    rows.push({
      source: 'CWC',
      competition: 'CLUB_WORLD_CUP',
      matchDate: currentDate,
      homeTeam: match[1].replace(/\s+\([A-Z]{3}\)$/, '').trim(),
      awayTeam: match[2].replace(/\s+\([A-Z]{3}\)$/, '').trim(),
      homeScore: score[0],
      awayScore: score[1],
      neutral: true,
      allowNationalFallback: false
    })
  }
  return rows
}

function localSourcePath(sourceRoot, sourceType, relativePath) {
  const bases = {
    worldCup: 'worldcup-json-src/worldcup.json-master',
    international: 'international-results-source/international_results-master',
    championsLeague: 'europe-champions-league-src/europe-champions-league-master',
    clubWorldCup: 'club-worldcup-src/club-worldcup-master'
  }
  return path.join(sourceRoot, bases[sourceType], relativePath)
}

async function fetchWithRetry(url) {
  let lastError = null
  for (let attempt = 1; attempt <= 5; attempt += 1) {
    try {
      const response = await fetch(url, {
        headers: { 'User-Agent': 'lottery-football-history-importer' },
        signal: AbortSignal.timeout(30_000)
      })
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      return await response.text()
    } catch (error) {
      lastError = error
      if (attempt < 5) {
        await new Promise(resolve => setTimeout(resolve, attempt * 1_000))
      }
    }
  }
  throw new Error(`下载失败：${url}，${lastError?.message ?? '未知错误'}`)
}

async function readSource(options, sourceType, relativePath, url) {
  if (options.sourceRoot) {
    return await fs.readFile(localSourcePath(options.sourceRoot, sourceType, relativePath), 'utf8')
  }
  const cachePath = path.join(cacheRoot, sourceType, relativePath)
  try {
    return await fs.readFile(cachePath, 'utf8')
  } catch (error) {
    if (error.code !== 'ENOENT') {
      throw error
    }
  }
  const text = await fetchWithRetry(url)
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, text, 'utf8')
  return text
}

async function loadSourceRows(options) {
  const rows = []

  for (const year of WORLD_CUP_YEARS) {
    const relativePath = `${year}/worldcup.json`
    const text = await readSource(
      options,
      'worldCup',
      relativePath,
      `https://raw.githubusercontent.com/openfootball/worldcup.json/master/${relativePath}`
    )
    rows.push(...parseWorldCup(text))
  }

  const [resultsText, goalscorersText] = await Promise.all([
    readSource(
      options,
      'international',
      'results.csv',
      'https://raw.githubusercontent.com/martj42/international_results/master/results.csv'
    ),
    readSource(
      options,
      'international',
      'goalscorers.csv',
      'https://raw.githubusercontent.com/martj42/international_results/master/goalscorers.csv'
    )
  ])
  const resultRows = parseCsv(resultsText)
  const goalRows = parseCsv(goalscorersText)
  rows.push(...parseNationalTournaments(resultRows, goalRows))

  for (let startYear = 1955; startYear <= 2015; startYear += 1) {
    const season = seasonName(startYear)
    const relativePath = `${season}/champs.csv`
    const text = await readSource(
      options,
      'championsLeague',
      relativePath,
      `https://raw.githubusercontent.com/footballcsv/europe-champions-league/master/${relativePath}`
    )
    rows.push(...parseChampionsLeague(text))
  }

  for (const year of CLUB_WORLD_CUP_YEARS) {
    const relativePath = `${year}/clubworldcup.txt`
    const text = await readSource(
      options,
      'clubWorldCup',
      relativePath,
      `https://raw.githubusercontent.com/openfootball/club-world-cup/master/${relativePath}`
    )
    rows.push(...parseClubWorldCup(text, year))
  }

  return rows
}

function emptySourceSummary() {
  return {
    parsedRows: 0,
    addedRows: 0,
    existingRows: 0,
    updatedRows: 0,
    futureRows: 0,
    unmappedRows: 0,
    correctedExtraTimeGoals: 0,
    unmappedTeams: new Map()
  }
}

function summarizeForOutput(sourceSummaries) {
  return Object.fromEntries([...sourceSummaries.entries()].map(([source, summary]) => [
    source,
    {
      parsedRows: summary.parsedRows,
      addedRows: summary.addedRows,
      existingRows: summary.existingRows,
      updatedRows: summary.updatedRows,
      futureRows: summary.futureRows,
      unmappedRows: summary.unmappedRows,
      correctedExtraTimeGoals: summary.correctedExtraTimeGoals,
      topUnmappedTeams: [...summary.unmappedTeams.entries()]
        .sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0]))
        .slice(0, 50)
        .map(([team, count]) => ({ team, count }))
    }
  ]))
}

const options = parseArguments(process.argv.slice(2))
const [historicalText, mappingText, sourceRows] = await Promise.all([
  fs.readFile(historicalMatchesPath, 'utf8'),
  fs.readFile(teamNameMappingsPath, 'utf8'),
  loadSourceRows(options)
])

const originalHistoricalRows = parseCsv(historicalText).map(normalizeHistoricalRow)
const historicalRows = originalHistoricalRows.filter(row => (
  row.match_date >= options.minDate && row.match_date <= options.maxDate
))
const mappingRows = parseCsv(mappingText)
const mappings = buildMappings(mappingRows)
const exactFixtureIndex = new Set(historicalRows.map(row => fixtureKey(
  row.competition,
  row.match_date,
  row.home_team_cn,
  row.away_team_cn
)))
const shiftableFixtureIndex = new Set(historicalRows
  .filter(row => !row.match_id.startsWith('OPEN-'))
  .map(row => fixtureKey(
    row.competition,
    row.match_date,
    row.home_team_cn,
    row.away_team_cn
  )))
const matchIds = new Set(historicalRows.map(row => row.match_id))
const historicalRowByMatchId = new Map(historicalRows.map(row => [row.match_id, row]))
const sourceSummaries = new Map()
const addedRows = []

for (const sourceRow of sourceRows) {
  const summary = sourceSummaries.get(sourceRow.source) ?? emptySourceSummary()
  sourceSummaries.set(sourceRow.source, summary)
  summary.parsedRows += 1
  summary.correctedExtraTimeGoals += sourceRow.correctedExtraTimeGoals ?? 0

  if (!sourceRow.matchDate
      || sourceRow.matchDate < options.minDate
      || sourceRow.matchDate > options.maxDate) {
    summary.futureRows += 1
    continue
  }

  const homeTeam = mappedChineseName(
    sourceRow.competition,
    sourceRow.homeTeam,
    mappings,
    sourceRow.allowNationalFallback
  )
  const awayTeam = mappedChineseName(
    sourceRow.competition,
    sourceRow.awayTeam,
    mappings,
    sourceRow.allowNationalFallback
  )
  if (!homeTeam || !awayTeam) {
    summary.unmappedRows += 1
    for (const [englishName, chineseName] of [
      [sourceRow.homeTeam, homeTeam],
      [sourceRow.awayTeam, awayTeam]
    ]) {
      if (!chineseName) {
        summary.unmappedTeams.set(
          englishName,
          (summary.unmappedTeams.get(englishName) ?? 0) + 1
        )
      }
    }
    continue
  }

  const matchId = sourceMatchId(sourceRow)
  const expectedHistoryRow = {
    match_id: matchId,
    match_date: sourceRow.matchDate,
    competition: sourceRow.competition,
    home_team_cn: homeTeam,
    away_team_cn: awayTeam,
    home_score: String(sourceRow.homeScore),
    away_score: String(sourceRow.awayScore),
    neutral: String(sourceRow.neutral),
    match_type: 'OFFICIAL',
    source_competition: COMPETITION_NAMES.get(sourceRow.competition) ?? sourceRow.competition
  }
  const existingSourceRow = historicalRowByMatchId.get(matchId)
  if (existingSourceRow) {
    const changed = HISTORY_HEADERS.some(header => (
      String(existingSourceRow[header]) !== String(expectedHistoryRow[header])
    ))
    if (changed) {
      Object.assign(existingSourceRow, expectedHistoryRow)
      summary.updatedRows += 1
    }
    summary.existingRows += 1
    continue
  }

  const exactKey = fixtureKey(
    sourceRow.competition,
    sourceRow.matchDate,
    homeTeam,
    awayTeam
  )
  const alreadyExists = exactFixtureIndex.has(exactKey)
    || [-1, 1].some(offset => shiftableFixtureIndex.has(fixtureKey(
      sourceRow.competition,
      dateWithOffset(sourceRow.matchDate, offset),
      homeTeam,
      awayTeam
    )))
  if (alreadyExists) {
    summary.existingRows += 1
    continue
  }

  const historyRow = expectedHistoryRow
  addedRows.push(historyRow)
  matchIds.add(matchId)
  historicalRowByMatchId.set(matchId, historyRow)
  exactFixtureIndex.add(fixtureKey(
    historyRow.competition,
    historyRow.match_date,
    historyRow.home_team_cn,
    historyRow.away_team_cn
  ))
  summary.addedRows += 1
}

const rebuiltRows = [...historicalRows, ...addedRows].sort((left, right) => (
  left.match_date.localeCompare(right.match_date)
  || left.competition.localeCompare(right.competition)
  || left.match_id.localeCompare(right.match_id)
))

if (options.write) {
  await fs.writeFile(historicalMatchesPath, toCsv(rebuiltRows), 'utf8')
  await import('./generate-team-name-mappings.mjs')
}

console.log(JSON.stringify({
  minDate: options.minDate,
  maxDate: options.maxDate,
  originalRows: originalHistoricalRows.length,
  removedOutsideDateRange: originalHistoricalRows.length - historicalRows.length,
  importedRows: addedRows.length,
  updatedRows: [...sourceSummaries.values()].reduce((total, summary) => (
    total + summary.updatedRows
  ), 0),
  rebuiltRows: rebuiltRows.length,
  firstDate: rebuiltRows.at(0)?.match_date ?? null,
  lastDate: rebuiltRows.at(-1)?.match_date ?? null,
  sources: summarizeForOutput(sourceSummaries),
  wroteFile: options.write
}, null, 2))
