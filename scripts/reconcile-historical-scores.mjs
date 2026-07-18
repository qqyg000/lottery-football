import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const historicalOddsPath = path.join(root, 'src/main/resources/data/historical_odds_data.csv')
const shouldWrite = process.argv.includes('--write')
const compactOutput = process.argv.includes('--compact')
const MINIMUM_HISTORY_DATE = '2014-06-12'

const competitionNames = new Map(Object.entries({
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

const verifiedOddsOverrides = new Map([
  ['HIS-2843', { home_score: '4', away_score: '3' }],
  ['HIS-3020', { home_team_en: 'Odds BK', home_score: '3', away_score: '4' }],
  ['HIS-3137', { away_team_en: 'Odds BK', home_score: '7', away_score: '2' }],
  ['HIS-6545', {
    home_team_en: 'Olimpija Ljubljana',
    away_team_en: 'AS Trencin',
    home_score: '3',
    away_score: '4'
  }],
  ['HIS-6751', { home_score: '5', away_score: '2' }],
  ['HIS-6848', { home_score: '6', away_score: '1' }],
  ['HIS-10606', { home_score: '4', away_score: '3' }],
  ['HIS-13950', { away_team_en: 'Crusaders', home_score: '7', away_score: '0' }],
  ['HIS-14219', { home_score: '5', away_score: '2' }],
  ['HIS-17087', { away_team_en: 'KR Reykjavik', home_score: '7', away_score: '1' }],
  ['HIS-17228', { home_score: '3', away_score: '4' }],
  ['HIS-23261', { away_team_en: 'Linfield', home_score: '8', away_score: '0' }],
  ['HIS-26630', { home_score: '2', away_score: '5' }],
  ['HIS-26634', { home_score: '3', away_score: '4' }],
  ['HIS-28781', { home_score: '5', away_score: '2' }],
  ['HIS-30445', { match_date: '2026-03-15', home_score: '5', away_score: '2' }]
])

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
  if (!/[",\r\n]/.test(text)) {
    return text
  }
  return `"${text.replaceAll('"', '""')}"`
}

function toCsv(rows, headers) {
  return [
    headers.join(','),
    ...rows.map(row => headers.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

function normalizeHistoricalRow(row) {
  const competition = String(row.competition ?? '').trim()
  const defaultType = competition === 'INTERNATIONAL_FRIENDLY'
    ? 'INTERNATIONAL_FRIENDLY'
    : competition === 'CLUB_FRIENDLY'
      ? 'CLUB_FRIENDLY'
      : 'OFFICIAL'
  return {
    ...row,
    match_type: String(row.match_type ?? '').trim() || defaultType,
    source_competition: String(row.source_competition ?? '').trim()
      || competitionNames.get(competition)
      || competition
  }
}

function canonicalChineseName(value) {
  return String(value ?? '')
    .normalize('NFKC')
    .toUpperCase()
    .replaceAll('足球俱乐部', '')
    .replaceAll('俱乐部', '')
    .replace(/[\s·•.．,，'’`´()（）\[\]【】\-_/&]+/gu, '')
    .replace(/^(FC|SC|CF)(?=\p{Script=Han})/u, '')
    .replace(/(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$/u, '')
}

function canonicalEnglishName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function dateWithOffset(dateText, days) {
  if (days === 0) {
    return dateText
  }
  const date = new Date(`${dateText}T00:00:00Z`)
  date.setUTCDate(date.getUTCDate() + days)
  return date.toISOString().slice(0, 10)
}

function scoreIsValid(row) {
  return /^\d+$/.test(row.home_score) && /^\d+$/.test(row.away_score)
}

function latestChineseMappings(oddsRows) {
  const mappings = new Map()
  const dates = new Map()
  for (const row of oddsRows) {
    for (const side of ['home', 'away']) {
      const englishKey = canonicalEnglishName(row[`${side}_team_en`])
      const chineseName = String(row[`${side}_team_cn`] ?? '').trim()
      if (!englishKey || !chineseName) {
        continue
      }
      for (const key of [`${row.competition}|${englishKey}`, `*|${englishKey}`]) {
        if (!dates.has(key) || row.match_date >= dates.get(key)) {
          mappings.set(key, chineseName)
          dates.set(key, row.match_date)
        }
      }
    }
  }
  return mappings
}

function mappedChineseName(row, side, mappings) {
  const englishName = row[`${side}_team_en`]
  const englishKey = canonicalEnglishName(englishName)
  if (englishKey) {
    const competitionName = mappings.get(`${row.competition}|${englishKey}`)
    const globalName = mappings.get(`*|${englishKey}`)
    if (competitionName || globalName) {
      return competitionName || globalName
    }
  }
  return String(row[`${side}_team_cn`] ?? '').trim()
}

function fixtureKey(competition, date, homeTeam, awayTeam) {
  return [
    competition,
    date,
    canonicalChineseName(homeTeam),
    canonicalChineseName(awayTeam)
  ].join('|')
}

function addToIndex(index, key, row) {
  const rows = index.get(key) ?? []
  rows.push(row)
  index.set(key, rows)
}

const [historicalMatchesText, historicalOddsText] = await Promise.all([
  fs.readFile(historicalMatchesPath, 'utf8'),
  fs.readFile(historicalOddsPath, 'utf8')
])
const originalHistoricalRows = parseCsv(historicalMatchesText).map(normalizeHistoricalRow)
const historicalRows = originalHistoricalRows.filter(row => row.match_date >= MINIMUM_HISTORY_DATE)
const oddsRows = parseCsv(historicalOddsText)
let verifiedOverrideCount = 0
for (const row of oddsRows) {
  const override = verifiedOddsOverrides.get(row.match_id)
  if (!override) {
    continue
  }
  Object.assign(row, override)
  verifiedOverrideCount += 1
}
const verifiedRows = historicalRows.filter(row => !row.match_id.startsWith('ODDS-'))
const mappings = latestChineseMappings(oddsRows)
const verifiedByFixture = new Map()
for (const row of verifiedRows) {
  addToIndex(verifiedByFixture, fixtureKey(
    row.competition,
    row.match_date,
    row.home_team_cn,
    row.away_team_cn
  ), row)
}

let matchedCount = 0
let correctedScoreCount = 0
let shiftedDateCount = 0
let reversedTeamCount = 0
let ambiguousCount = 0
const correctedScoreExamples = []
const shiftedOrReversedExamples = []
const unmatchedRows = []

for (const row of oddsRows) {
  const homeTeam = mappedChineseName(row, 'home', mappings)
  const awayTeam = mappedChineseName(row, 'away', mappings)
  const candidates = []
  for (const dayOffset of [0, -1, 1]) {
    const candidateDate = dateWithOffset(row.match_date, dayOffset)
    for (const reversed of [false, true]) {
      const key = fixtureKey(
        row.competition,
        candidateDate,
        reversed ? awayTeam : homeTeam,
        reversed ? homeTeam : awayTeam
      )
      for (const match of verifiedByFixture.get(key) ?? []) {
        candidates.push({ match, dayOffset, reversed })
      }
    }
    if (candidates.length > 0) {
      break
    }
  }

  const uniqueCandidates = new Map(candidates.map(candidate => [candidate.match.match_id, candidate]))
  if (uniqueCandidates.size !== 1) {
    if (uniqueCandidates.size > 1) {
      ambiguousCount += 1
    }
    unmatchedRows.push({ ...row, home_team_cn: homeTeam, away_team_cn: awayTeam })
    continue
  }

  const candidate = [...uniqueCandidates.values()][0]
  const correctedHomeScore = candidate.reversed
    ? candidate.match.away_score
    : candidate.match.home_score
  const correctedAwayScore = candidate.reversed
    ? candidate.match.home_score
    : candidate.match.away_score
  if (row.home_score !== correctedHomeScore || row.away_score !== correctedAwayScore) {
    correctedScoreCount += 1
    if (correctedScoreExamples.length < 20) {
      correctedScoreExamples.push({
        match_id: row.match_id,
        match_date: row.match_date,
        competition: row.competition,
        fixture: `${homeTeam}-${awayTeam}`,
        original_score: `${row.home_score}-${row.away_score}`,
        corrected_score: `${correctedHomeScore}-${correctedAwayScore}`,
        verified_match_id: candidate.match.match_id
      })
    }
  }
  row.home_score = correctedHomeScore
  row.away_score = correctedAwayScore
  matchedCount += 1
  if (candidate.dayOffset !== 0) {
    shiftedDateCount += 1
  }
  if (candidate.reversed) {
    reversedTeamCount += 1
  }
  if ((candidate.dayOffset !== 0 || candidate.reversed) && shiftedOrReversedExamples.length < 50) {
    shiftedOrReversedExamples.push({
      match_id: row.match_id,
      match_date: row.match_date,
      fixture: `${homeTeam}-${awayTeam}`,
      verified_match_id: candidate.match.match_id,
      verified_date: candidate.match.match_date,
      reversed: candidate.reversed
    })
  }
}

const unmatchedHistoryByFixture = new Map()
const excludedCappedRows = []
let excludedCappedScoreCount = 0
for (const row of unmatchedRows) {
  if (!scoreIsValid(row)) {
    continue
  }
  const totalGoals = Number(row.home_score) + Number(row.away_score)
  if (totalGoals === 7 && !verifiedOddsOverrides.has(row.match_id)) {
    excludedCappedScoreCount += 1
    excludedCappedRows.push({
      match_id: row.match_id,
      match_date: row.match_date,
      competition: row.competition,
      home_team_cn: row.home_team_cn,
      away_team_cn: row.away_team_cn,
      score: `${row.home_score}-${row.away_score}`
    })
    continue
  }
  const historyRow = {
    match_id: `ODDS-${row.match_id}`,
    match_date: row.match_date,
    competition: row.competition,
    home_team_cn: row.home_team_cn,
    away_team_cn: row.away_team_cn,
    home_score: row.home_score,
    away_score: row.away_score,
    neutral: row.neutral,
    match_type: 'OFFICIAL',
    source_competition: competitionNames.get(row.competition) ?? row.competition
  }
  const key = fixtureKey(
    historyRow.competition,
    historyRow.match_date,
    historyRow.home_team_cn,
    historyRow.away_team_cn
  )
  if (!unmatchedHistoryByFixture.has(key)) {
    unmatchedHistoryByFixture.set(key, historyRow)
  }
}

const rebuiltHistoryRows = [
  ...verifiedRows,
  ...unmatchedHistoryByFixture.values()
].sort((left, right) => (
  left.match_date.localeCompare(right.match_date)
  || left.competition.localeCompare(right.competition)
  || left.match_id.localeCompare(right.match_id)
))

const summary = {
  minimumHistoryDate: MINIMUM_HISTORY_DATE,
  originalHistoricalRows: originalHistoricalRows.length,
  removedBeforeMinimumDate: originalHistoricalRows.length - historicalRows.length,
  verifiedHistoricalRows: verifiedRows.length,
  oddsRows: oddsRows.length,
  verifiedOddsOverrides: verifiedOverrideCount,
  matchedOddsRows: matchedCount,
  correctedOddsScores: correctedScoreCount,
  correctedScoreExamples,
  shiftedDateMatches: shiftedDateCount,
  reversedTeamMatches: reversedTeamCount,
  shiftedOrReversedExamples,
  ambiguousOddsRows: ambiguousCount,
  unmatchedOddsRows: unmatchedRows.length,
  excludedUnverifiedCappedScores: excludedCappedScoreCount,
  excludedCappedFixtures: excludedCappedRows,
  retainedUnmatchedHistoryRows: unmatchedHistoryByFixture.size,
  rebuiltHistoricalRows: rebuiltHistoryRows.length,
  wroteFiles: shouldWrite
}

if (shouldWrite) {
  const historyHeaders = [
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
  const oddsHeaders = [
    'match_id',
    'match_date',
    'competition',
    'home_team_cn',
    'away_team_cn',
    'home_team_en',
    'away_team_en',
    'home_score',
    'away_score',
    'neutral',
    'sporttery_match_number',
    'handicap',
    'normal_win',
    'normal_draw',
    'normal_lose',
    'handicap_win',
    'handicap_draw',
    'handicap_lose'
  ]
  await Promise.all([
    fs.writeFile(historicalMatchesPath, toCsv(rebuiltHistoryRows, historyHeaders), 'utf8'),
    fs.writeFile(historicalOddsPath, toCsv(oddsRows, oddsHeaders), 'utf8')
  ])
}

if (compactOutput) {
  delete summary.correctedScoreExamples
  delete summary.shiftedOrReversedExamples
}
console.log(JSON.stringify(summary, null, 2))
