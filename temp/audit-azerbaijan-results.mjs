import fs from 'node:fs/promises'
import crypto from 'node:crypto'

const MIN_DATE = '2014-10-22'
const MAX_DATE = '2026-07-22'
const OUT_PATH = new URL('./azerbaijan-results-audit.csv', import.meta.url)

async function getText(url, attempt = 1) {
  const response = await fetch(url, {
    headers: {
      accept: 'application/json,text/html;q=0.9,*/*;q=0.8',
      referer: 'https://www.fotmob.com/',
      'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
    }
  })
  if (!response.ok) {
    if (attempt < 3 && [429, 500, 502, 503, 504].includes(response.status)) {
      await new Promise(resolve => setTimeout(resolve, attempt * 1000))
      return getText(url, attempt + 1)
    }
    throw new Error(`${response.status} ${response.statusText}: ${url}`)
  }
  return response.text()
}

function shanghaiDate(isoText) {
  const date = new Date(isoText)
  date.setUTCHours(date.getUTCHours() + 8)
  return date.toISOString().slice(0, 10)
}

function decodeHtml(value) {
  return String(value ?? '')
    .replaceAll('&amp;', '&')
    .replaceAll('&#x2F;', '/')
    .replaceAll('&#x27;', "'")
    .replaceAll('&quot;', '"')
    .replaceAll('&lt;', '<')
    .replaceAll('&gt;', '>')
    .trim()
}

function futbol24ShanghaiDate(year, month, day, timeText) {
  const [hour, minute] = String(timeText ?? '00:00').split(':').map(Number)
  const utcTime = Date.UTC(year, month - 1, day, hour - 2, minute)
  return shanghaiDate(new Date(utcTime).toISOString())
}

async function fetchFotMob() {
  const rows = []
  const seasons = []
  for (let year = 2014; year <= 2026; year += 1) {
    const season = `${year}/${year + 1}`
    const url = `https://www.fotmob.com/api/data/leagues?id=262&ccode3=CHN&season=${encodeURIComponent(season)}`
    const json = JSON.parse(await getText(url))
    const seasonRows = []
    for (const match of json?.fixtures?.allMatches ?? []) {
      const score = String(match?.status?.scoreStr ?? '').match(/^\s*(\d+)\s*-\s*(\d+)\s*$/)
      if (!match?.status?.finished || match?.status?.cancelled || !score) continue
      const matchDate = shanghaiDate(match.status.utcTime)
      if (matchDate < MIN_DATE || matchDate > MAX_DATE) continue
      seasonRows.push({
        provider: 'FOTMOB',
        provider_id: String(match.id),
        season,
        match_date: matchDate,
        source_date: String(match.status.utcTime).slice(0, 10),
        competition: '阿塞超',
        home_team: match.home.name,
        away_team: match.away.name,
        home_score: Number(score[1]),
        away_score: Number(score[2]),
        result_note: match.status.reason?.short ?? 'FT',
        source_url: url
      })
    }
    rows.push(...seasonRows)
    seasons.push({ season, rows: seasonRows.length, apiFixtures: json?.fixtures?.allMatches?.length ?? 0 })
  }
  return { rows, seasons }
}

function parseFutbol24Season(html, season, url) {
  const rows = []
  const matchPattern = /<a href="(\/match\/(\d{4})\/(\d{2})\/(\d{2})\/[^"]*\/([^\/"]+)\/vs\/([^"]+))"[^>]*>([\s\S]*?)<\/a>/g
  for (const match of html.matchAll(matchPattern)) {
    const content = decodeHtml(match[7].replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' '))
    const score = content.match(/(\d+)\s*-\s*(\d+)/)
    if (!score) continue
    const prefix = html.slice(Math.max(0, match.index - 2000), match.index)
    const timeMatches = [...prefix.matchAll(/f-single-match__cell--time"[^>]*>[\s\S]*?(\d{1,2}:\d{2})/g)]
    const timeText = timeMatches.at(-1)?.[1] ?? '00:00'
    const matchDate = futbol24ShanghaiDate(Number(match[2]), Number(match[3]), Number(match[4]), timeText)
    if (matchDate < MIN_DATE || matchDate > MAX_DATE) continue
    const path = match[1]
    rows.push({
      provider: 'FUTBOL24',
      provider_id: crypto.createHash('sha1').update(path).digest('hex').slice(0, 16).toUpperCase(),
      season,
      match_date: matchDate,
      source_date: `${match[2]}-${match[3]}-${match[4]}`,
      competition: '阿塞杯',
      home_team: decodeURIComponent(match[5]).replaceAll('-', ' '),
      away_team: decodeURIComponent(match[6]).replaceAll('-', ' '),
      home_score: Number(score[1]),
      away_score: Number(score[2]),
      result_note: content.replace(score[0], '').trim() || 'FT',
      source_url: `https://www.futbol24.com${path}`
    })
  }
  return rows
}

async function fetchFutbol24() {
  const rows = []
  const seasons = []
  for (let year = 2014; year <= 2025; year += 1) {
    const season = `${year}-${year + 1}`
    const url = `https://www.futbol24.com/national/Azerbaijan/Kubok/${season}/results`
    const html = await getText(url)
    const seasonRows = parseFutbol24Season(html, season, url)
    rows.push(...seasonRows)
    seasons.push({ season, rows: seasonRows.length })
  }
  return { rows, seasons }
}

function pflShanghaiDate(startDate) {
  const matched = String(startDate ?? '').match(/^(\d{2})\/(\d{2})\/(\d{4})\s+(\d{2}):(\d{2})$/)
  if (!matched) return null
  const [, day, month, year, hour, minute] = matched
  // PFL stores Azerbaijan local time (UTC+4); Shanghai is four hours ahead.
  return new Date(Date.UTC(Number(year), Number(month) - 1, Number(day), Number(hour) + 4, Number(minute)))
    .toISOString()
    .slice(0, 10)
}

async function fetchPflLeague(leagueId, season, competition) {
  const endpoint = `https://pfl.az/api/v1/tours/get-games?league_id=${leagueId}&withGames=true`
  const json = JSON.parse(await getText(endpoint))
  const games = (json?.data ?? []).flatMap(tour => tour?.games ?? [])
  const rows = []
  for (const game of games) {
    const matchDate = pflShanghaiDate(game.start_date)
    if (game.status !== 3 || matchDate == null || matchDate < MIN_DATE || matchDate > MAX_DATE) continue
    rows.push({
      provider: 'PFL',
      provider_id: String(game.id),
      season,
      match_date: matchDate,
      source_date: String(game.start_date).slice(0, 10).split('/').reverse().join('-'),
      competition,
      home_team: game.club_1.title,
      away_team: game.club_2.title,
      home_score: Number(game.club_score_1),
      away_score: Number(game.club_score_2),
      result_note: game.penalty ? `pen ${game.penalty}` : 'FT',
      source_url: `https://pfl.az/api/v1/games/show/${game.id}?withActions=true`
    })
  }
  return rows
}

async function fetchPflRecent() {
  const [league2024, league2025, cup2024, cup2025] = await Promise.all([
    fetchPflLeague(9, '2024/2025', '阿塞超'),
    fetchPflLeague(47, '2025/2026', '阿塞超'),
    fetchPflLeague(13, '2024-2025', '阿塞杯'),
    fetchPflLeague(48, '2025-2026', '阿塞杯')
  ])
  return {
    leagueRows: [...league2024, ...league2025],
    cupRows: [...cup2024, ...cup2025],
    seasons: {
      league: [
        { season: '2024/2025', rows: league2024.length },
        { season: '2025/2026', rows: league2025.length }
      ],
      cup: [
        { season: '2024-2025', rows: cup2024.length },
        { season: '2025-2026', rows: cup2025.length }
      ]
    }
  }
}

function escapeCsv(value) {
  const text = String(value ?? '')
  return /[",\r\n]/.test(text) ? `"${text.replaceAll('"', '""')}"` : text
}

function duplicateSummary(rows) {
  const counts = new Map()
  for (const row of rows) {
    const key = [row.competition, row.match_date, row.home_team, row.away_team, row.home_score, row.away_score].join('|')
    counts.set(key, (counts.get(key) ?? 0) + 1)
  }
  return [...counts.entries()].filter(([, count]) => count > 1)
}

const [league, cup, pfl] = await Promise.all([fetchFotMob(), fetchFutbol24(), fetchPflRecent()])
// The official PFL API is structured only for the two latest seasons. Prefer it there;
// it includes the Premier League relegation playoff and Cup qualifying rounds omitted
// by FotMob/Futbol24. Older seasons fall back to FotMob/Futbol24.
const rows = [
  ...league.rows.filter(row => !['2024/2025', '2025/2026'].includes(row.season)),
  ...cup.rows.filter(row => !['2024-2025', '2025-2026'].includes(row.season)),
  ...pfl.leagueRows,
  ...pfl.cupRows
].sort((a, b) =>
  a.match_date.localeCompare(b.match_date) || a.competition.localeCompare(b.competition) || a.provider_id.localeCompare(b.provider_id)
)
const headers = Object.keys(rows[0])
await fs.writeFile(OUT_PATH, `\uFEFF${headers.join(',')}\r\n${rows.map(row => headers.map(key => escapeCsv(row[key])).join(',')).join('\r\n')}\r\n`, 'utf8')

const wanted = rows.filter(row =>
  (row.match_date === '2026-05-14' && row.home_team.includes('Sabah') && row.away_team.includes('Zira')) ||
  (row.match_date === '2026-05-22' && row.home_team.includes('Turan') && row.away_team.includes('Sabah'))
)
const result = {
  dateRange: [MIN_DATE, MAX_DATE],
  totalRows: rows.length,
  leagueRows: rows.filter(row => row.competition === '阿塞超').length,
  cupRows: rows.filter(row => row.competition === '阿塞杯').length,
  range: { first: rows[0], last: rows.at(-1) },
  leagueSeasons: league.seasons,
  cupSeasons: cup.seasons,
  pflRecentSeasons: pfl.seasons,
  duplicateFixtureScoreKeys: duplicateSummary(rows),
  wanted,
  output: OUT_PATH.pathname
}
console.log(JSON.stringify(result, null, 2))
