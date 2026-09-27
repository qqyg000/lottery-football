import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const historyPath = path.join(root, 'src/main/resources/data/historical_matches.csv')
const mappingPath = path.join(root, 'src/main/resources/data/team_name_mappings.csv')
const cacheRoot = path.join(root, 'target/uefa-nations-league-cache')
const competition = 'UEFA_NATIONS_LEAGUE'
const firstDate = '2014-10-22'
const leagues = ['9806', '9807', '9808', '9809', '10717', '10718', '10719']
const fallbackNames = new Map(Object.entries({
  'Faroe Islands': '法罗群岛', Gibraltar: '直布罗陀', Kosovo: '科索沃',
  Liechtenstein: '列支敦士登', 'San Marino': '圣马力诺', Andorra: '安道尔',
  Malta: '马耳他', Luxembourg: '卢森堡', Cyprus: '塞浦路斯',
  Kazakhstan: '哈萨克斯坦', Moldova: '摩尔多瓦', Estonia: '爱沙尼亚',
  Latvia: '拉脱维亚', Lithuania: '立陶宛', Belarus: '白俄罗斯',
  Montenegro: '黑山', Armenia: '亚美尼亚', Azerbaijan: '阿塞拜疆',
  '哈萨克': '哈萨克斯坦'
}))

export function parseCsv(text) {
  const rows = []
  let row = [], field = '', quoted = false
  for (let i = text.charCodeAt(0) === 0xFEFF ? 1 : 0; i < text.length; i++) {
    const c = text[i]
    if (quoted) {
      if (c === '"' && text[i + 1] === '"') { field += '"'; i++ }
      else if (c === '"') quoted = false
      else field += c
    } else if (c === '"') quoted = true
    else if (c === ',') { row.push(field); field = '' }
    else if (c === '\n') { row.push(field.replace(/\r$/, '')); rows.push(row); row = []; field = '' }
    else field += c
  }
  if (field || row.length) { row.push(field.replace(/\r$/, '')); rows.push(row) }
  const headers = rows.shift()
  return { headers, rows: rows.filter(row => row.length === headers.length)
    .map(row => Object.fromEntries(headers.map((key, i) => [key, row[i]]))) }
}

function serializeCsv(headers, rows) {
  const escape = value => /[",\r\n]/.test(String(value ?? ''))
    ? '"' + String(value).replaceAll('"', '""') + '"' : String(value ?? '')
  return '\uFEFF' + [headers, ...rows.map(row => headers.map(key => row[key]))]
    .map(row => row.map(escape).join(',')).join('\r\n') + '\r\n'
}

export function needsRegulationDetails(match) {
  return /aet|extra.?time|penalt|afterpen/i.test(JSON.stringify(match.status?.reason ?? {}))
}

export function regulationScore(match, details) {
  const score = /^\s*(\d+)\s*-\s*(\d+)\s*$/.exec(match.status?.scoreStr ?? '')
  if (!score) throw new Error('缺少比分：' + match.id)
  if (!needsRegulationDetails(match)) return [Number(score[1]), Number(score[2])]
  const events = details?.content?.matchFacts?.events?.events
  if (!Array.isArray(events)) throw new Error('缺少常规时间赛况：' + match.id)
  const ft = events.find(event => event.type === 'Half' && Number(event.time) === 90
    && event.halfStrShort === 'FT')
  if (ft?.homeScore != null && ft?.awayScore != null) return [Number(ft.homeScore), Number(ft.awayScore)]
  const goals = events.filter(event => event.type === 'Goal' && !event.isPenaltyShootoutEvent)
  if (!goals.length && Number(score[1]) + Number(score[2]) > 0) {
    throw new Error('无法核验常规时间进球：' + match.id)
  }
  return [true, false].map(home => goals.filter(event => event.isHome === home && Number(event.time) <= 90).length)
}

export function shanghaiDate(utc) {
  return new Date(Date.parse(utc) + 8 * 3600_000).toISOString().slice(0, 10)
}

async function jsonFromSource(url, key, refresh) {
  const file = path.join(cacheRoot, key + '.json')
  if (!refresh) {
    try { return JSON.parse(await fs.readFile(file, 'utf8')) }
    catch (error) { if (error.code !== 'ENOENT') throw error }
  }
  let lastError
  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const response = await fetch(url, {
        signal: AbortSignal.timeout(25000), headers: { 'User-Agent': 'Mozilla/5.0' }
      })
      if (!response.ok) throw new Error('HTTP ' + response.status + ': ' + url)
      const json = await response.json()
      await fs.mkdir(cacheRoot, { recursive: true })
      await fs.writeFile(file, JSON.stringify(json))
      return json
    } catch (error) { lastError = error }
  }
  throw lastError
}

async function main() {
  const args = process.argv.slice(2)
  const write = args.includes('--write')
  const refresh = args.includes('--refresh')
  const endDate = args.find(value => value.startsWith('--end='))?.slice(6)
    ?? shanghaiDate(new Date().toISOString())
  if (!/^\d{4}-\d{2}-\d{2}$/.test(endDate) || endDate < firstDate
      || endDate > shanghaiDate(new Date().toISOString())) throw new Error('截止日期必须在历史起点和今天之间')
  const history = parseCsv(await fs.readFile(historyPath, 'utf8'))
  const mappings = parseCsv(await fs.readFile(mappingPath, 'utf8'))
  const aliases = new Map(fallbackNames)
  const priorities = { HISTORICAL_MATCHES: 1, HISTORICAL_ODDS: 2, VERIFIED_ALIAS: 4, VERIFIED_SPORTTERY: 6, MANUAL: 7 }
  const nationalScopes = new Set(['*', 'WORLD_CUP', 'EUROPEAN_CHAMPIONSHIP', 'COPA_AMERICA', competition])
  mappings.rows.filter(row => nationalScopes.has(row.competition))
    .sort((a, b) => (priorities[a.source] ?? 0) - (priorities[b.source] ?? 0))
    .forEach(row => { if (/\p{Script=Han}/u.test(row.standard_team_name)) aliases.set(row.alias_team_name, row.standard_team_name) })
  aliases.set('哈萨克', '哈萨克斯坦')
  aliases.set('斯洛文尼亚', '斯洛文尼')
  aliases.set('阿尔巴尼亚', '阿尔巴尼')
  const canonicalTeam = name => {
    const visited = new Set()
    while (aliases.has(name) && !visited.has(name)) { visited.add(name); name = aliases.get(name) }
    return name
  }
  const teamName = name => {
    const translated = canonicalTeam(name)
    if (!/\p{Script=Han}/u.test(translated)) throw new Error('缺少球队中文映射：' + name)
    return translated
  }
  const imported = new Map()
  const sourceSummary = []
  const newMappings = new Map()
  const recordMapping = (english, chinese) => {
    const key = competition + '|' + english
    newMappings.set(key, { competition, standard_team_name: chinese, alias_team_name: english,
      alias_type: /\p{Script=Han}/u.test(english) ? 'CN' : 'EN',
      source: /\p{Script=Han}/u.test(english) ? 'VERIFIED_SPORTTERY' : 'VERIFIED_ALIAS', last_seen_date: endDate })
  }
  recordMapping('哈萨克', '哈萨克斯坦')
  recordMapping('斯洛文尼亚', '斯洛文尼')
  recordMapping('阿尔巴尼亚', '阿尔巴尼')
  for (let year = 2018; year <= Number(endDate.slice(0, 4)); year += 2) {
    const season = year + '/' + (year + 1)
    const batches = await Promise.all(leagues.filter(league => !league.startsWith('107') || year >= 2024).map(async league => {
      const json = await jsonFromSource('https://www.fotmob.com/api/data/leagues?id=' + league
        + '&ccode3=CHN&season=' + encodeURIComponent(season), league + '-' + year,
      refresh || year >= Number(endDate.slice(0, 4)) - 2)
      if (league.startsWith('107') && year > Number(String(json.details?.latestSeason).slice(0, 4))) {
        return { league, matches: [] }
      }
      if (json.details?.selectedSeason !== season || !Array.isArray(json.fixtures?.allMatches)) {
        throw new Error('来源赛季不符或缺少赛程：' + league + ' ' + season)
      }
      return { league, matches: json.fixtures.allMatches }
    }))
    for (const { league, matches } of batches) {
      let completed = 0
      for (const match of matches) {
        if (!match.status?.finished || match.status.cancelled) continue
        const date = shanghaiDate(match.status.utcTime)
        if (date < firstDate || date > endDate) continue
        const details = needsRegulationDetails(match)
          ? await jsonFromSource('https://www.fotmob.com/api/data/matchDetails?matchId=' + match.id,
            'match-' + match.id, refresh) : null
        const [homeScore, awayScore] = regulationScore(match, details)
        const home = match.home.name, away = match.away.name
        const homeCn = teamName(home), awayCn = teamName(away)
        recordMapping(home, homeCn)
        recordMapping(away, awayCn)
        // 决赛周在集中举办地进行，联赛阶段和两回合附加赛按主客场处理
        const neutral = league === '9806' && /semi.?final|^final$|third|3rd|bronze/i.test(String(match.roundName ?? match.round ?? ''))
        const row = { match_id: 'FOTMOB-' + match.id, match_date: date, competition,
          home_team_cn: homeCn, away_team_cn: awayCn, home_score: String(homeScore), away_score: String(awayScore),
          neutral: String(neutral), match_type: 'OFFICIAL', source_competition: '欧国联' }
        const key = [date, homeCn, awayCn].join('|')
        const existing = imported.get(key)
        if (existing && (existing.home_score !== row.home_score || existing.away_score !== row.away_score)) {
          throw new Error('来源比分冲突：' + key)
        }
        imported.set(key, row)
        completed++
      }
      sourceSummary.push({ league, season, completed })
    }
    console.error(season + ' 赛季已核验，累计 ' + imported.size + ' 场')
  }
  const targetRow = row => row.competition === competition || /^(欧国联|欧洲国家联赛|UEFA Nations League)/i.test(row.source_competition)
  const oldTargets = history.rows.filter(targetRow)
  const missing = oldTargets.filter(row => ![...imported.values()].some(candidate =>
    candidate.home_team_cn === canonicalTeam(row.home_team_cn) && candidate.away_team_cn === canonicalTeam(row.away_team_cn)
    && Math.abs(Date.parse(candidate.match_date) - Date.parse(row.match_date)) <= 86400_000))
  if (missing.length) throw new Error('新来源未覆盖 ' + missing.length + ' 条已有记录：' + JSON.stringify(missing.slice(0, 5)))
  const retained = history.rows.filter(row => !targetRow(row))
  const importedRows = [...imported.values()].sort((a, b) => a.match_date.localeCompare(b.match_date)
    || a.match_id.localeCompare(b.match_id))
  const output = []
  let importedIndex = 0
  // 保持其他赛事的原始顺序，仅按日期插入本次导入的比赛
  for (const row of retained) {
    while (importedIndex < importedRows.length && importedRows[importedIndex].match_date < row.match_date) {
      output.push(importedRows[importedIndex++])
    }
    output.push(row)
  }
  output.push(...importedRows.slice(importedIndex))
  const mappingsByKey = new Map(mappings.rows.map(row => [row.competition + '|' + row.alias_team_name, row]))
  const addedMappings = []
  let updatedMappings = 0
  for (const [key, row] of newMappings) {
    const existing = mappingsByKey.get(key)
    if (!existing) addedMappings.push(row)
    else if ((priorities[existing.source] ?? 0) <= (priorities[row.source] ?? 0)
        && (existing.standard_team_name !== row.standard_team_name || existing.source !== row.source)) {
      Object.assign(existing, row)
      updatedMappings++
    }
  }
  const report = { startDate: firstDate, endDate, previousCount: oldTargets.length, importedCount: imported.size,
    firstMatchDate: [...imported.values()].map(row => row.match_date).sort()[0],
    lastMatchDate: [...imported.values()].map(row => row.match_date).sort().at(-1),
    addedMappings: addedMappings.length, updatedMappings, sourceSummary, written: write }
  if (write) {
    await fs.writeFile(historyPath, serializeCsv(history.headers, output))
    if (addedMappings.length || updatedMappings) await fs.writeFile(mappingPath, serializeCsv(mappings.headers, mappings.rows.concat(addedMappings)))
  }
  await fs.mkdir(path.join(root, 'reports'), { recursive: true })
  await fs.writeFile(path.join(root, 'reports/uefa-nations-league-import.json'), JSON.stringify(report, null, 2) + '\n')
  console.log(JSON.stringify(report, null, 2))
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch(error => { console.error(error); process.exitCode = 1 })
}
