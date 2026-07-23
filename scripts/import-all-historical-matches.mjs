import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const outputPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const mappingPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')

const HEADERS = [
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

const DIRECT_COMPETITIONS = new Map([
  ['世界杯', 'WORLD_CUP'],
  ['欧洲杯', 'EUROPEAN_CHAMPIONSHIP'],
  ['美洲杯', 'COPA_AMERICA'],
  ['世俱杯', 'CLUB_WORLD_CUP'],
  ['欧罗巴', 'EUROPA_LEAGUE'],
  ['欧冠', 'CHAMPIONS_LEAGUE'],
  ['英超', 'PREMIER_LEAGUE'],
  ['西甲', 'LA_LIGA'],
  ['意甲', 'SERIE_A'],
  ['德甲', 'BUNDESLIGA'],
  ['法甲', 'LIGUE_1'],
  ['巴甲', 'BRAZIL_SERIE_A'],
  ['葡超', 'PRIMEIRA_LIGA'],
  ['荷甲', 'EREDIVISIE'],
  ['阿甲', 'ARGENTINE_PRIMERA_DIVISION']
])

const INTERNATIONAL_OFFICIAL_LEAGUES = new Set([
  '世预赛',
  '欧预赛',
  '欧国联',
  '非洲杯',
  '世青赛',
  '金杯赛',
  '女世界杯',
  '亚洲杯',
  '亚奥赛',
  '欧青赛',
  '奥运男足',
  '奥运女足',
  '欧青预赛',
  '亚运男足',
  '亚洲杯23',
  '女欧洲杯',
  '四强赛',
  '女四强赛',
  '女亚洲杯',
  '亚预赛',
  '亚运女足',
  '东南亚锦'
])

const INTERNATIONAL_FRIENDLY_LEAGUES = new Set(['国际赛'])
const CLUB_FRIENDLY_LEAGUES = new Set(['俱乐部赛', '杯赛'])
const NEUTRAL_COMPETITIONS = new Set([
  'WORLD_CUP',
  'EUROPEAN_CHAMPIONSHIP',
  'COPA_AMERICA',
  'CLUB_WORLD_CUP'
])

function parseArguments(argv) {
  const options = {
    sourcePath: '',
    startDate: '2014-10-22',
    sourceRowOffset: 2,
    statsOutputPath: '',
    write: false
  }
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index]
    if (argument === '--write') {
      options.write = true
    } else if (argument === '--source') {
      options.sourcePath = argv[++index] ?? ''
    } else if (argument.startsWith('--source=')) {
      options.sourcePath = argument.slice('--source='.length)
    } else if (argument.startsWith('--start-date=')) {
      options.startDate = argument.slice('--start-date='.length)
    } else if (argument.startsWith('--source-row-offset=')) {
      options.sourceRowOffset = Number(argument.slice('--source-row-offset='.length))
    } else if (argument.startsWith('--stats-output=')) {
      options.statsOutputPath = path.resolve(argument.slice('--stats-output='.length))
    }
  }
  if (!options.sourcePath) {
    throw new Error('Missing required --source CSV path')
  }
  return options
}

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false
  for (let index = text.charCodeAt(0) === 0xFEFF ? 1 : 0; index < text.length; index += 1) {
    const character = text[index]
    const next = text[index + 1]
    if (inQuotes) {
      if (character === '"' && next === '"') {
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
      if (row.some(value => value !== '')) {
        rows.push(row)
      }
      row = []
      field = ''
    } else {
      field += character
    }
  }
  if (field || row.length) {
    row.push(field.replace(/\r$/, ''))
    rows.push(row)
  }
  const headers = rows.shift() ?? []
  return rows
    .filter(values => values.length === headers.length)
    .map(values => Object.fromEntries(headers.map((header, index) => [header, values[index]])))
}

function escapeCsv(value) {
  const text = String(value ?? '')
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function canonicalName(value) {
  return String(value ?? '')
    .normalize('NFKD')
    .replace(/\p{Mark}/gu, '')
    .toUpperCase()
    .replaceAll('足球俱乐部', '')
    .replaceAll('俱乐部', '')
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function competitionFor(sourceCompetition) {
  if (DIRECT_COMPETITIONS.has(sourceCompetition)) {
    return DIRECT_COMPETITIONS.get(sourceCompetition)
  }
  if (INTERNATIONAL_OFFICIAL_LEAGUES.has(sourceCompetition)) {
    return 'INTERNATIONAL_OFFICIAL'
  }
  if (INTERNATIONAL_FRIENDLY_LEAGUES.has(sourceCompetition)) {
    return 'INTERNATIONAL_FRIENDLY'
  }
  if (CLUB_FRIENDLY_LEAGUES.has(sourceCompetition)) {
    return 'CLUB_FRIENDLY'
  }
  return 'CLUB_OFFICIAL_OTHER'
}

function matchTypeFor(competition) {
  if (competition === 'INTERNATIONAL_FRIENDLY') {
    return 'INTERNATIONAL_FRIENDLY'
  }
  if (competition === 'CLUB_FRIENDLY') {
    return 'CLUB_FRIENDLY'
  }
  return 'OFFICIAL'
}

function buildAliasMap(mappingRows) {
  const aliases = new Map()
  for (const row of mappingRows) {
    const alias = canonicalName(row.alias_team_name)
    if (!alias || !row.standard_team_name) {
      continue
    }
    aliases.set(`${row.competition}|${alias}`, row.standard_team_name)
  }
  return aliases
}

function standardName(aliases, competition, name) {
  const canonical = canonicalName(name)
  return aliases.get(`${competition}|${canonical}`)
    ?? aliases.get(`*|${canonical}`)
    ?? name
}

function fixtureKey(aliases, row) {
  return [
    row.match_date,
    row.competition,
    canonicalName(standardName(aliases, row.competition, row.home_team_cn)),
    canonicalName(standardName(aliases, row.competition, row.away_team_cn)),
    row.home_score,
    row.away_score
  ].join('|')
}

function teamFixtureKey(aliases, row) {
  return [
    row.match_date,
    row.competition,
    canonicalName(standardName(aliases, row.competition, row.home_team_cn)),
    canonicalName(standardName(aliases, row.competition, row.away_team_cn))
  ].join('|')
}

function toCsv(rows) {
  return [
    HEADERS.join(','),
    ...rows.map(row => HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

const options = parseArguments(process.argv.slice(2))
const sourcePath = path.resolve(options.sourcePath)
const [sourceText, existingText, mappingText] = await Promise.all([
  fs.readFile(sourcePath, 'utf8'),
  fs.readFile(outputPath, 'utf8').catch(() => HEADERS.join(',')),
  fs.readFile(mappingPath, 'utf8').catch(() => '')
])
const sourceRows = parseCsv(sourceText)
const existingRows = parseCsv(existingText)
const mappingRows = mappingText ? parseCsv(mappingText) : []
const aliases = buildAliasMap(mappingRows)
const importedRows = []
const sourceCompetitionCounts = new Map()
let skippedBeforeStart = 0
let skippedInvalidScore = 0

for (let index = 0; index < sourceRows.length; index += 1) {
  const source = sourceRows[index]
  if (source['比赛时间'] < options.startDate) {
    skippedBeforeStart += 1
    continue
  }
  const score = String(source['比分'] ?? '').match(/^(\d+):(\d+)$/)
  if (!score) {
    skippedInvalidScore += 1
    continue
  }
  const sourceCompetition = String(source['联赛'] ?? '').trim()
  const competition = competitionFor(sourceCompetition)
  sourceCompetitionCounts.set(sourceCompetition, (sourceCompetitionCounts.get(sourceCompetition) ?? 0) + 1)
  importedRows.push({
    match_id: `EXCEL-${index + options.sourceRowOffset}`,
    match_date: source['比赛时间'],
    competition,
    home_team_cn: String(source['主场球队'] ?? '').trim(),
    away_team_cn: String(source['客场球队'] ?? '').trim(),
    home_score: score[1],
    away_score: score[2],
    neutral: NEUTRAL_COMPETITIONS.has(competition) ? 'true' : 'false',
    match_type: matchTypeFor(competition),
    source_competition: sourceCompetition
  })
}

const rowsByFixture = new Map()
const fixtureKeyByTeamFixture = new Map()
const matchIds = new Set()
const latestImportedDate = importedRows.reduce(
  (latest, row) => row.match_date > latest ? row.match_date : latest,
  options.startDate
)
let duplicateSourceRows = 0
const duplicateSourceExamples = []
for (const row of importedRows) {
  const key = fixtureKey(aliases, row)
  if (rowsByFixture.has(key)) {
    duplicateSourceRows += 1
    if (duplicateSourceExamples.length < 20) {
      duplicateSourceExamples.push({ retained: rowsByFixture.get(key), removed: row })
    }
    continue
  }
  rowsByFixture.set(key, row)
  fixtureKeyByTeamFixture.set(teamFixtureKey(aliases, row), key)
  matchIds.add(row.match_id)
}

let preservedExistingRows = 0
let correctedSourceRows = 0
const preservedExistingExamples = []
for (const row of existingRows) {
  if (row.match_date < options.startDate || matchIds.has(row.match_id)) {
    continue
  }
  const key = fixtureKey(aliases, row)
  if (rowsByFixture.has(key)) {
    continue
  }
  const teamKey = teamFixtureKey(aliases, row)
  const sourceKey = fixtureKeyByTeamFixture.get(teamKey)
  if (sourceKey && row.match_date <= latestImportedDate) {
    const sourceRow = rowsByFixture.get(sourceKey)
    rowsByFixture.delete(sourceKey)
    matchIds.delete(sourceRow.match_id)
    correctedSourceRows += 1
  }
  rowsByFixture.set(key, row)
  fixtureKeyByTeamFixture.set(teamKey, key)
  matchIds.add(row.match_id)
  preservedExistingRows += 1
  if (preservedExistingExamples.length < 20) {
    preservedExistingExamples.push(row)
  }
}

const outputRows = [...rowsByFixture.values()].sort((left, right) => (
  left.match_date.localeCompare(right.match_date)
  || left.competition.localeCompare(right.competition)
  || left.match_id.localeCompare(right.match_id)
))

if (options.write) {
  await fs.writeFile(outputPath, `\uFEFF${toCsv(outputRows)}`, 'utf8')
}

if (options.statsOutputPath) {
  const statsRows = [...sourceCompetitionCounts]
    .sort((left, right) => right[1] - left[1] || left[0].localeCompare(right[0], 'zh-CN'))
    .map(([sourceCompetition, count]) => {
      const competition = competitionFor(sourceCompetition)
      return [sourceCompetition, count, competition, matchTypeFor(competition)]
    })
  const statsCsv = [
    ['source_competition', 'match_count', 'competition', 'match_type'],
    ...statsRows
  ].map(row => row.map(escapeCsv).join(',')).join('\n') + '\n'
  await fs.mkdir(path.dirname(options.statsOutputPath), { recursive: true })
  await fs.writeFile(options.statsOutputPath, `\uFEFF${statsCsv}`, 'utf8')
}

console.log(JSON.stringify({
  sourcePath,
  startDate: options.startDate,
  sourceRows: sourceRows.length,
  importedRows: importedRows.length,
  sourceCompetitionTypes: sourceCompetitionCounts.size,
  sourceCompetitionCounts: Object.fromEntries([...sourceCompetitionCounts].sort((left, right) => (
    right[1] - left[1] || left[0].localeCompare(right[0], 'zh-CN')
  ))),
  duplicateSourceRows,
  duplicateSourceExamples,
  correctedSourceRows,
  preservedExistingRows,
  preservedExistingExamples,
  outputRows: outputRows.length,
  skippedBeforeStart,
  skippedInvalidScore,
  outputPath,
  statsOutputPath: options.statsOutputPath,
  wroteFile: options.write
}, null, 2))
