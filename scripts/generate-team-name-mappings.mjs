import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historicalMatchesPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const historicalOddsPath = path.join(root, 'src/main/resources/data/historical_odds_data.csv')
const outputPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')

const HEADERS = [
  'competition',
  'standard_team_name',
  'alias_team_name',
  'alias_type',
  'source',
  'last_seen_date'
]


const SOURCE_PRIORITY = new Map([
  ['HISTORICAL_MATCHES', 1],
  ['HISTORICAL_ODDS', 2],
  ['VERIFIED_ALIAS', 3],
  ['VERIFIED_SPORTTERY', 4],
  ['MANUAL', 5]
])

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false
  for (let index = 0; index < text.length; index += 1) {
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
      rows.push(row)
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
  const headers = rows.shift().map((header, index) => index === 0 ? header.replace(/^\uFEFF/, '') : header)
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
    .replace(/[^\p{Letter}\p{Number}]/gu, '')
}

function aliasType(value, standardName) {
  if (value === standardName) {
    return 'STANDARD'
  }
  return /\p{Script=Han}/u.test(value) ? 'ZH' : 'EN'
}

function rowIsNewer(candidate, current) {
  const candidatePriority = SOURCE_PRIORITY.get(candidate.source) ?? 0
  const currentPriority = SOURCE_PRIORITY.get(current.source) ?? 0
  return candidatePriority > currentPriority
    || candidatePriority === currentPriority && candidate.last_seen_date >= current.last_seen_date
}

function register(rowsByKey, row) {
  const key = `${row.competition}|${canonicalName(row.alias_team_name)}`
  if (!canonicalName(row.alias_team_name) || !row.standard_team_name) {
    return
  }
  const current = rowsByKey.get(key)
  if (!current || rowIsNewer(row, current)) {
    rowsByKey.set(key, row)
  }
}

function registerAlias(rowsByKey, competition, standardName, aliasName, source, lastSeenDate) {
  const row = {
    competition,
    standard_team_name: standardName,
    alias_team_name: aliasName,
    alias_type: aliasType(aliasName, standardName),
    source,
    last_seen_date: lastSeenDate
  }
  register(rowsByKey, row)
  if (competition !== '*') {
    register(rowsByKey, { ...row, competition: '*' })
  }
}

function resolve(rowsByKey, competition, aliasName) {
  const key = canonicalName(aliasName)
  return rowsByKey.get(`${competition}|${key}`)?.standard_team_name
    ?? rowsByKey.get(`*|${key}`)?.standard_team_name
    ?? null
}

function buildOddsMappings(oddsRows, rowsByKey) {
  const identities = new Map()
  for (const row of oddsRows) {
    for (const side of ['home', 'away']) {
      const chineseName = String(row[`${side}_team_cn`] ?? '').trim()
      const englishName = String(row[`${side}_team_en`] ?? '').trim()
      if (!chineseName) {
        continue
      }
      const identity = canonicalName(englishName) || canonicalName(chineseName)
      const key = `${row.competition}|${identity}`
      const current = identities.get(key) ?? { competition: row.competition, names: new Map(), latest: null }
      for (const name of [chineseName, englishName].filter(Boolean)) {
        const date = current.names.get(name) ?? ''
        if (row.match_date >= date) {
          current.names.set(name, row.match_date)
        }
      }
      if (!current.latest || row.match_date >= current.latest.date) {
        current.latest = { name: chineseName, date: row.match_date }
      }
      identities.set(key, current)
    }
  }
  for (const identity of identities.values()) {
    for (const [name, date] of identity.names.entries()) {
      registerAlias(rowsByKey, identity.competition, identity.latest.name, name, 'HISTORICAL_ODDS', date)
    }
    registerAlias(
      rowsByKey,
      identity.competition,
      identity.latest.name,
      identity.latest.name,
      'HISTORICAL_ODDS',
      identity.latest.date
    )
  }
}

function addHistoricalMatchNames(historyRows, rowsByKey) {
  for (const row of historyRows) {
    for (const name of [row.home_team_cn, row.away_team_cn]) {
      const standardName = resolve(rowsByKey, row.competition, name) ?? name
      registerAlias(rowsByKey, row.competition, standardName, name, 'HISTORICAL_MATCHES', row.match_date)
    }
  }
}

function addPreservedMappings(rowsByKey, existingRows) {
  for (const row of existingRows) {
    if (!['VERIFIED_ALIAS', 'VERIFIED_SPORTTERY', 'MANUAL'].includes(row.source)) {
      continue
    }
    register(rowsByKey, row)
  }
}

function toCsv(rows) {
  return [
    HEADERS.join(','),
    ...rows.map(row => HEADERS.map(header => escapeCsv(row[header])).join(','))
  ].join('\n') + '\n'
}

const [historyText, oddsText, existingMappingText] = await Promise.all([
  fs.readFile(historicalMatchesPath, 'utf8'),
  fs.readFile(historicalOddsPath, 'utf8'),
  fs.readFile(outputPath, 'utf8').catch(() => '')
])
const historyRows = parseCsv(historyText)
const oddsRows = parseCsv(oddsText)
const existingRows = existingMappingText ? parseCsv(existingMappingText) : []
const rowsByKey = new Map()
buildOddsMappings(oddsRows, rowsByKey)
addHistoricalMatchNames(historyRows, rowsByKey)
addPreservedMappings(rowsByKey, existingRows)

const rows = [...rowsByKey.values()].sort((left, right) => (
  left.competition.localeCompare(right.competition)
  || left.standard_team_name.localeCompare(right.standard_team_name, 'zh-CN')
  || left.alias_team_name.localeCompare(right.alias_team_name, 'zh-CN')
))
await fs.writeFile(outputPath, toCsv(rows), 'utf8')

const standards = new Set(rows.map(row => `${row.competition}|${row.standard_team_name}`))
const sources = Object.fromEntries([...new Set(rows.map(row => row.source))]
  .sort()
  .map(source => [source, rows.filter(row => row.source === source).length]))
console.log(JSON.stringify({
  outputPath,
  historicalMatchRows: historyRows.length,
  historicalOddsRows: oddsRows.length,
  mappingRows: rows.length,
  standards: standards.size,
  sources
}, null, 2))
