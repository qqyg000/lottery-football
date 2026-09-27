import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const API_BASE = process.env.LOTTERY_FOOTBALL_API_BASE || 'http://127.0.0.1:8080'
const SIMULATIONS = 50000
const PRESETS = [['STABLE', '稳健'], ['AGGRESSIVE', '激进']]
const TOTAL_GOALS_MODE = process.argv.includes('--total-goals')
const TOP_N = TOTAL_GOALS_MODE ? 3 : 4
const PREDICTION_FIELD = TOTAL_GOALS_MODE ? 'adjustedTotalGoalsProbabilities' : 'adjustedScoreProbabilities'
const REPORT_NAME = TOTAL_GOALS_MODE ? 'current-total-goals-top3-ranking' : 'current-score-top4-ranking'
const REPORT_TITLE = TOTAL_GOALS_MODE ? '仅本届进球数前三预测命中率排名' : '仅本届前4比分预测命中率排名'
const HIT_DEFINITION = TOTAL_GOALS_MODE
  ? '每场比赛常规时间主客队实际进球数之和出现在概率最高的前3个进球数中即命中；命中率=命中场数/有效已完赛场数'
  : '每场比赛常规时间实际比分出现在概率最高的前4个比分中即命中；命中率=命中场数/有效已完赛场数'
const today = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai' }).format(new Date())
const reportBase = path.join(ROOT, 'reports', `${REPORT_NAME}-${today}`)

async function fetchJson(endpoint, params = {}) {
  const url = new URL(endpoint, API_BASE)
  Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, String(value)))
  const response = await fetch(url, { signal: AbortSignal.timeout(180000) })
  if (!response.ok) throw new Error(`${response.status}: ${url}`)
  return response.json()
}

function digest(value) {
  return createHash('sha256').update(JSON.stringify(value)).digest('hex')
}

async function writeReport(filePath, content) {
  for (let attempt = 0; ; attempt++) {
    try {
      await fs.writeFile(filePath, content)
      return
    } catch (error) {
      if (attempt >= 4 || !['UNKNOWN', 'EBUSY', 'EPERM', 'EACCES'].includes(error.code)) throw error
      await new Promise(resolve => setTimeout(resolve, 300 * (attempt + 1)))
    }
  }
}

async function mapConcurrent(values, mapper, concurrency = 3) {
  const results = new Array(values.length)
  let nextIndex = 0
  await Promise.all(Array.from({ length: Math.min(concurrency, values.length) }, async () => {
    while (nextIndex < values.length) {
      const index = nextIndex++
      results[index] = await mapper(values[index], index)
    }
  }))
  return results
}

function summarize(matches) {
  const hits = matches.filter(match => match.hit).length
  const dates = matches.map(match => match.matchDate).sort()
  return {
    total: matches.length,
    hits,
    hitRate: matches.length ? hits / matches.length * 100 : null,
    firstMatchDate: dates[0] || null,
    lastMatchDate: dates.at(-1) || null
  }
}

function ranking(rows, preset, scope) {
  const sorted = rows.filter(row => row.preset === preset).map(row => ({
    competition: row.competition,
    name: row.name,
    ...row[scope]
  })).sort((left, right) => (right.hitRate ?? -1) - (left.hitRate ?? -1) || right.total - left.total)
  let rank = 0
  sorted.forEach((row, index) => {
    if (index === 0 || row.hitRate !== sorted[index - 1].hitRate) rank = index + 1
    row.rank = row.total ? rank : null
  })
  return sorted
}

const frontend = await fs.readFile(path.join(ROOT, 'frontend/src/App.vue'), 'utf8')
const competitionSection = frontend.match(/const COMPETITIONS = \[([\s\S]*?)\n\]/)?.[1]
assert.ok(competitionSection, 'Cannot find page competitions')
const competitions = [...competitionSection.matchAll(/code: '([^']+)', name: '([^']+)'/g)]
  .filter(match => match[1] !== 'ALL')
  .map(match => ({ code: match[1], name: match[2] }))
const config = await fetchJson('/api/football/user-config')
const profilesHash = digest(config.parameterProfiles)
let report = {
  createdAt: new Date().toISOString(),
  asOfDate: today,
  timeZone: 'Asia/Shanghai',
  apiBase: API_BASE,
  simulations: SIMULATIONS,
  range: 'CURRENT',
  modelMode: 'after',
  predictionKind: TOTAL_GOALS_MODE ? 'totalGoals' : 'score',
  probabilityField: PREDICTION_FIELD,
  ...(!TOTAL_GOALS_MODE ? { scoreField: PREDICTION_FIELD } : {}),
  topN: TOP_N,
  definition: HIT_DEFINITION,
  competitionScope: '当前页面赛事列表',
  parameterProfilesSha256: profilesHash,
  rows: []
}
await fs.mkdir(path.dirname(reportBase), { recursive: true })
try {
  const saved = JSON.parse(await fs.readFile(`${reportBase}.partial.json`, 'utf8'))
  assert.equal(saved.parameterProfilesSha256, profilesHash, 'Saved results use different parameter profiles')
  assert.equal(saved.asOfDate, today)
  assert.equal(saved.topN, TOP_N)
  report = saved
  console.log(`继续已完成的 ${report.rows.length} 项统计`)
} catch (error) {
  if (error.code !== 'ENOENT') throw error
}

for (const competition of competitions) {
  const overview = await fetchJson('/api/football/overview', {
    competition: competition.code,
    includePreviousEdition: false
  })
  const dates = overview.scheduleDates.filter(date => date <= today)
  let stableResults
  for (const [preset, presetName] of PRESETS) {
    const profileKey = `${competition.code}:CURRENT:${preset}`
    const savedRow = report.rows.find(row => row.profileKey === profileKey)
    if (savedRow) {
      if (preset === 'STABLE') stableResults = savedRow.matches.map(match => `${match.matchDate}:${match.matchId}:${match.actualScore}`)
      continue
    }
    const modelFactors = config.parameterProfiles[profileKey]?.modelFactors
    assert.ok(modelFactors, `Missing profile: ${profileKey}`)
    const responses = await mapConcurrent(dates, date => fetchJson('/api/football/predictions', {
      competition: competition.code,
      date,
      simulations: SIMULATIONS,
      ...modelFactors
    }))
    const matches = []
    const excludedMatches = []
    const ids = new Set()
    for (const response of responses) {
      assert.ok(Array.isArray(response.matches), `Missing matches: ${profileKey}`)
      for (const match of response.matches) {
        assert.equal(match.competition, competition.code)
        assert.equal(match.simulations, SIMULATIONS)
        if (match.status !== '已完赛' || match.matchDate > today) continue
        const actual = String(match.scoreText || '').match(/^(\d+)\s*-\s*(\d+)$/)
        if (!actual) {
          excludedMatches.push({ matchId: match.matchId, matchDate: match.matchDate, homeTeam: match.homeTeamCn, awayTeam: match.awayTeamCn, reason: match.scoreText || '缺少常规时间比分' })
          continue
        }
        assert.ok(Array.isArray(match[PREDICTION_FIELD]), `Missing probabilities: ${match.matchId}`)
        const topPredictions = match[PREDICTION_FIELD].slice(0, TOP_N)
        assert.equal(topPredictions.length, TOP_N, `Incomplete top${TOP_N}: ${match.matchId}`)
        assert.equal(new Set(topPredictions.map(item => TOTAL_GOALS_MODE
          ? item.totalGoals
          : `${item.homeScore}-${item.awayScore}`)).size, TOP_N)
        if (TOTAL_GOALS_MODE) {
          assert.ok(topPredictions.every(item => Number.isInteger(item.totalGoals) && item.totalGoals >= 0))
        }
        const identity = `${match.matchDate}:${match.matchId}`
        assert.ok(!ids.has(identity), `Duplicate match: ${identity}`)
        ids.add(identity)
        const actualTotalGoals = Number(actual[1]) + Number(actual[2])
        const hit = topPredictions.some(item => TOTAL_GOALS_MODE
          ? item.totalGoals === actualTotalGoals
          : item.homeScore === Number(actual[1]) && item.awayScore === Number(actual[2]))
        matches.push({
          matchId: match.matchId,
          matchDate: match.matchDate,
          homeTeam: match.homeTeamCn,
          awayTeam: match.awayTeamCn,
          actualScore: `${actual[1]}-${actual[2]}`,
          ...(TOTAL_GOALS_MODE ? { actualTotalGoals, top3: topPredictions } : { top4: topPredictions }),
          hit,
          sportteryMatchId: match.sportteryMatchId || null,
          hasBacktestOdds: Boolean(match.sportteryMatchId && (
            match.sportteryNormalOdds || match.sportteryHandicapOdds || match.sportteryTotalGoalsOdds
          ))
        })
      }
    }
    matches.sort((a, b) => a.matchDate.localeCompare(b.matchDate) || a.matchId.localeCompare(b.matchId))
    const actualResults = matches.map(match => `${match.matchDate}:${match.matchId}:${match.actualScore}`)
    if (preset === 'STABLE') stableResults = actualResults
    else assert.deepEqual(actualResults, stableResults, `${competition.name} presets must use the same matches`)
    const row = {
      competition: competition.code,
      name: competition.name,
      preset,
      presetName,
      profileKey,
      modelFactors,
      overviewCompletedMatchCount: overview.completedMatchCount,
      allCompleted: summarize(matches),
      withOdds: summarize(matches.filter(match => match.hasBacktestOdds)),
      excludedMatches,
      matches
    }
    report.rows.push(row)
    await writeReport(`${reportBase}.partial.json`, JSON.stringify(report, null, 2) + '\n')
    console.log(`${competition.name} ${presetName}: ${row.allCompleted.hits}/${row.allCompleted.total} = ${row.allCompleted.hitRate?.toFixed(2) ?? '-'}%; 有赔率 ${row.withOdds.hits}/${row.withOdds.total}; 排除比分缺失 ${excludedMatches.length}; 概览已完赛 ${overview.completedMatchCount}`)
  }
}

const currentConfig = await fetchJson('/api/football/user-config')
assert.equal(digest(currentConfig.parameterProfiles), profilesHash, 'Parameter profiles changed during calculation')
report.finishedAt = new Date().toISOString()
report.rankings = Object.fromEntries(PRESETS.map(([preset]) => [preset, {
  allCompleted: ranking(report.rows, preset, 'allCompleted'),
  withOdds: ranking(report.rows, preset, 'withOdds')
}]))
const lines = [
  `# ${REPORT_TITLE}（${today}）`,
  '',
  `按当前页面的赛事列表、CURRENT:STABLE / CURRENT:AGGRESSIVE 参数分别计算；每场模拟50,000次，使用页面修正后的${TOTAL_GOALS_MODE ? '进球数前三' : '前4比分'}。${HIT_DEFINITION}。`,
  '',
  '“仅本届”沿用项目内置届次与北京时间范围。主表包含当前服务已有且常规时间比分完整的全部已完赛比赛，不要求有赔率，也不按胜平负推荐开关或阈值筛选。未完赛和缺少常规时间比分的比赛不计入分母。',
  '',
  '附表限定为有体彩比赛ID和至少一类赔率的已完赛样本，便于对照页面回测。',
  '',
  '这是用当前保存参数重新计算的历史样本命中率，不是独立留出验证结果；数据截止时间见各赛事明细，缺失比赛未补全。',
  ''
]
if (TOTAL_GOALS_MODE) {
  lines.push('进球数与页面的“进球数”前三展示一致，采用精确总进球数（7球即7球，不将7球以上合并）；页面单独的体彩进球数推荐策略及其固定稳健参数不影响本项统计。', '')
}
for (const row of report.rows.filter(row => row.preset === 'STABLE' && row.excludedMatches?.length)) {
  lines.push(`${row.name}：排除 ${row.excludedMatches.length} 场缺少常规时间比分的比赛`, '')
}
for (const [scope, scopeName] of [['allCompleted', '全部已完赛'], ['withOdds', '有体彩赔率样本']]) {
  for (const [preset, presetName] of PRESETS) {
    lines.push(`## 仅本届${presetName}（${scopeName}）`, '', '| 排名 | 赛事 | 命中/样本 | 命中率 | 样本日期 |', '|---:|---|---:|---:|---|')
    for (const row of report.rankings[preset][scope]) {
      lines.push(`| ${row.rank ?? '—'} | ${row.name} | ${row.hits}/${row.total} | ${row.hitRate == null ? '无样本' : row.hitRate.toFixed(2) + '%'} | ${row.firstMatchDate || '—'} ～ ${row.lastMatchDate || '—'} |`)
    }
    lines.push('')
  }
}
await writeReport(`${reportBase}.json`, JSON.stringify(report, null, 2) + '\n')
await writeReport(`${reportBase}.md`, lines.join('\n'))
await fs.unlink(`${reportBase}.partial.json`)
console.log(JSON.stringify({ report: `${reportBase}.md`, evidence: `${reportBase}.json`, completedRows: report.rows.length }, null, 2))
