import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { BACKTEST_COMPETITIONS } from '../frontend/src/backtest-competitions.mjs'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const argument = (name, fallback) => {
  const index = process.argv.indexOf(name)
  return index < 0 ? fallback : process.argv[index + 1]
}
const hash = value => createHash('sha256').update(typeof value === 'string' || Buffer.isBuffer(value) ? value : JSON.stringify(value)).digest('hex')
const asOfDate = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
const base = argument('--base-url', 'http://127.0.0.1:8080')
const preset = argument('--preset', 'AGGRESSIVE')
const parallelism = Number(argument('--parallelism', '24'))
const simulations = 50000
assert.ok(['STABLE', 'AGGRESSIVE'].includes(preset), '--preset 必须为 STABLE 或 AGGRESSIVE')
assert.ok(Number.isSafeInteger(parallelism) && parallelism >= 1 && parallelism <= 64, '--parallelism 必须为1至64之间的整数')
const presetName = preset === 'AGGRESSIVE' ? '激进' : '稳健'
const reportBase = path.resolve(ROOT, argument('--report-base', `reports/current-${preset.toLowerCase()}-score-top2-top3-ranking-${asOfDate}`))

async function request(endpoint, parameters = {}) {
  const url = new URL(endpoint, base)
  for (const [key, value] of Object.entries(parameters)) url.searchParams.set(key, String(value))
  const response = await fetch(url, { signal: AbortSignal.timeout(180000) })
  if (!response.ok) throw new Error(`${response.status}: ${url}`)
  return response.json()
}

function summary(matches, topN) {
  const hits = matches.filter(match => match.actualRank !== null && match.actualRank <= topN).length
  const dates = matches.map(match => match.matchDate).sort()
  return { total: matches.length, hits, hitRate: matches.length ? hits / matches.length : null,
    firstMatchDate: dates[0] || null, lastMatchDate: dates.at(-1) || null }
}

function ranking(rows, scope, topN) {
  return rows.map(row => ({ competition: row.competition, name: row.name,
    ...row[scope][`top${topN}`] })).sort((a, b) => (b.hitRate ?? -1) - (a.hitRate ?? -1) || b.total - a.total || a.competition.localeCompare(b.competition))
    .map((row, index) => ({ position: row.total ? index + 1 : null, ...row }))
}

async function main() {
  const config = await request('/api/football/user-config')
  const profileHash = hash(config.parameterProfiles)
  const rows = await Promise.all(BACKTEST_COMPETITIONS.map(async competition => {
    const profileKey = `${competition.code}:CURRENT:${preset}`
    const profile = config.parameterProfiles[profileKey]
    assert.ok(profile?.modelFactors, `缺少参数 ${profileKey}`)
    const overview = await request('/api/football/overview', { competition: competition.code, includePreviousEdition: false })
    return { competition: competition.code, name: competition.code === 'WORLD_CUP' ? '世界杯（含欧国联）' : competition.name,
      profileKey, modelFactors: profile.modelFactors, overviewCompletedMatchCount: overview.completedMatchCount,
      dates: overview.scheduleDates.filter(date => date <= asOfDate), matches: [], excludedMatches: [] }
  }))
  assert.equal(rows.length, 17)
  const tasks = rows.flatMap(row => row.dates.map(date => ({ row, date })))
  let next = 0
  let completed = 0
  let stopped = false
  const startedAt = new Date().toISOString()
  const timer = setInterval(() => process.stdout.write(`已计算 ${completed}/${tasks.length} 个赛事比赛日，${rows.reduce((sum, row) => sum + row.matches.length, 0)} 场有效比赛\n`), 20000)
  try {
    await Promise.all(Array.from({ length: Math.min(parallelism, tasks.length) }, async () => {
      while (!stopped && next < tasks.length) {
        const { row, date } = tasks[next++]
        const response = await request('/api/football/predictions', { competition: row.competition, date, simulations, ...row.modelFactors })
        assert.ok(Array.isArray(response.matches))
        for (const match of response.matches) {
          assert.equal(match.competition, row.competition)
          assert.equal(match.simulations, simulations)
          if (match.status !== '已完赛' || match.matchDate > asOfDate) continue
          const actual = /^(\d+)\s*-\s*(\d+)$/.exec(String(match.scoreText || ''))
          if (!actual) {
            row.excludedMatches.push({ matchId: match.matchId, matchDate: match.matchDate, homeTeam: match.homeTeamCn, awayTeam: match.awayTeamCn, reason: match.scoreText || '缺少常规时间比分' })
            continue
          }
          const top3 = match.adjustedScoreProbabilities?.slice(0, 3)
          assert.equal(top3?.length, 3, `缺少前3比分 ${match.matchId}`)
          assert.equal(new Set(top3.map(score => `${score.homeScore}-${score.awayScore}`)).size, 3)
          assert.ok(top3.every(score => Number.isInteger(score.homeScore) && Number.isInteger(score.awayScore) && score.homeScore >= 0 && score.awayScore >= 0))
          // 保留接口的原始排序，不能按四舍五入后的显示概率重新处理并列项
          const index = top3.findIndex(score => score.homeScore === Number(actual[1]) && score.awayScore === Number(actual[2]))
          row.matches.push({ matchId: match.matchId, matchDate: match.matchDate, homeTeam: match.homeTeamCn, awayTeam: match.awayTeamCn,
            groupName: match.groupName, actualScore: `${Number(actual[1])}-${Number(actual[2])}`, top3, actualRank: index < 0 ? null : index + 1,
            hasBacktestOdds: Boolean(match.sportteryMatchId && (match.sportteryNormalOdds || match.sportteryHandicapOdds || match.sportteryTotalGoalsOdds)) })
        }
        completed++
      }
    }))
  } catch (error) {
    stopped = true
    throw error
  } finally { clearInterval(timer) }
  for (const row of rows) {
    row.matches.sort((a, b) => a.matchDate.localeCompare(b.matchDate) || a.matchId.localeCompare(b.matchId))
    assert.equal(new Set(row.matches.map(match => `${match.matchDate}:${match.matchId}`)).size, row.matches.length, `${row.competition} 出现重复比赛`)
    for (const [scope, matches] of [['allCompleted', row.matches], ['withOdds', row.matches.filter(match => match.hasBacktestOdds)]]) {
      row[scope] = { top2: summary(matches, 2), top3: summary(matches, 3) }
      assert.ok(row[scope].top3.hits >= row[scope].top2.hits)
      assert.equal(row[scope].top2.total, row[scope].top3.total)
    }
  }
  const finalConfig = await request('/api/football/user-config')
  assert.equal(hash(finalConfig.parameterProfiles), profileHash, '计算期间参数发生变化')
  const liveConfig = JSON.parse(await fs.readFile(path.join(ROOT, 'config/user-config.json'), 'utf8'))
  assert.deepEqual(finalConfig.parameterProfiles, liveConfig.parameterProfiles, '统计参数与正式服务的磁盘参数不一致')
  const rankings = Object.fromEntries(['allCompleted', 'withOdds'].map(scope => [scope, { top2: ranking(rows, scope, 2), top3: ranking(rows, scope, 3) }]))
  const snapshotDirectory = argument('--snapshot-directory', '')
  const inputs = snapshotDirectory ? await Promise.all(['snapshot.jar', 'config/user-config.json', 'config/club-competition-schedules.json', 'config/sporttery-market-selections.json'].map(async file => ({ file, sha256: hash(await fs.readFile(path.resolve(ROOT, snapshotDirectory, file))) }))) : []
  const report = { startedAt, completedAt: new Date().toISOString(), asOfDate, timeZone: 'Asia/Shanghai', range: 'CURRENT', preset, presetName,
    apiBase: base, simulations, parallelism, probabilityField: 'adjustedScoreProbabilities', parameterProfilesSha256: profileHash,
    definition: '前N个比分中任一与90分钟加伤停补时实际比分一致，该场记为命中一次；命中率=命中场数/有效已完赛场数，前2和前3分别统计，不能相加',
    scope: '17类赛事全部常规时间比分完整的已完赛样本，不要求赔率，不按胜平负推荐开关和投注阈值筛选',
    snapshotDirectory, inputs, scriptSha256: hash(await fs.readFile(fileURLToPath(import.meta.url))),
    totalMatches: rows.reduce((sum, row) => sum + row.matches.length, 0),
    excludedMatches: rows.reduce((sum, row) => sum + row.excludedMatches.length, 0), rankings, rows }
  const lines = [`# 仅本届${presetName}比分前2、前3合计命中率排名（${asOfDate}）`, '',
    `使用当前保存的 CURRENT:${preset} 参数、修正后比分概率，每场模拟50,000次，按完整比赛日并发${parallelism}个请求复算。`, '', report.definition, '',
    '主表按全部有效已完赛样本统计，不要求有体彩赔率，不按投注推荐是否启用筛选。本届范围沿用后端overview的届次和北京时间口径，世界杯分组包含欧国联。', '',
    '这是当前参数对历史比赛的复算，不是独立留出测试结果。样本覆盖取决于现有数据；以下前2、前3是比分候选集合，不是场次或注数。相同命中率按样本数从多到少排序。', '']
  for (const [scope, scopeName] of [['allCompleted', '全部有效已完赛'], ['withOdds', '有体彩赔率样本']]) for (const topN of [2, 3]) {
    lines.push(`## 前${topN}比分（${scopeName}）`, '', '| 排序 | 赛事 | 命中/场数 | 合计命中率 | 样本日期 |', '| ---: | --- | ---: | ---: | --- |')
    for (const row of rankings[scope][`top${topN}`]) lines.push(`| ${row.position ?? '—'} | ${row.name} | ${row.hits}/${row.total} | ${row.hitRate === null ? '无样本' : (row.hitRate * 100).toFixed(2) + '%'} | ${row.firstMatchDate || '—'} ～ ${row.lastMatchDate || '—'} |`)
    lines.push('')
  }
  lines.push('## 数据核验', '', `有效已完赛共${report.totalMatches}场，缺少常规时间比分排除${report.excludedMatches}场，全部17类赛事的前2命中集合均包含于前3命中集合，且计算前后参数指纹一致。`, '')
  for (const row of rows.filter(row => row.excludedMatches.length)) lines.push(`- ${row.name}排除${row.excludedMatches.length}场，具体比赛见JSON明细`)
  await fs.mkdir(path.dirname(reportBase), { recursive: true })
  await fs.writeFile(`${reportBase}.json`, JSON.stringify(report, null, 2) + '\n', 'utf8')
  await fs.writeFile(`${reportBase}.md`, lines.join('\n') + '\n', 'utf8')
  process.stdout.write(JSON.stringify({ report: `${reportBase}.md`, totalMatches: report.totalMatches, excludedMatches: report.excludedMatches, rankings: rankings.allCompleted }, null, 2) + '\n')
}

main().catch(error => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1 })
