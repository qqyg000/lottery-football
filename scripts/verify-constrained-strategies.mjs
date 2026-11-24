import fs from 'node:fs/promises'
import path from 'node:path'
import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { verifyMetrics, goalMatches } from './constrained-optimization-worker.mjs'
import { fetchScoreBacktest } from './score-top3-optimization.mjs'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const argument = (name, fallback) => {
  const index = process.argv.indexOf(name)
  return index < 0 ? fallback : process.argv[index + 1]
}
const base = argument('--base-url', 'http://127.0.0.1:8080')
const date = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
const reportFile = path.resolve(ROOT, argument('--report-path', `reports/constrained-robust-optimization-${date}.json`))
const outputFile = path.resolve(ROOT, argument('--output', `reports/constrained-robust-release-audit-${date}.json`))
const configPath = path.resolve(ROOT, argument('--config-path', 'config/user-config.json'))
const jarPath = path.resolve(ROOT, argument('--jar-path', 'target/lottery-football-1.0.0.jar'))
const parallelism = Number(argument('--parallelism', '24'))
if (!Number.isSafeInteger(parallelism) || parallelism < 1 || parallelism > 64) throw new Error('--parallelism 必须为1至64的整数')
const report = JSON.parse(await fs.readFile(reportFile, 'utf8'))
const hash = value => createHash('sha256').update(typeof value === 'string' || Buffer.isBuffer(value) ? value : JSON.stringify(value)).digest('hex')
const audit = { startedAt: new Date().toISOString(), base, reportFile, parallelism,
  simulations: report.manifest.options.simulations, checkedProfiles: 0, checkedGoals: 0, checkedScoreProfiles: 0,
  dataDrift: [], violations: [], results: [], scoreResults: [] }
const pause = ms => new Promise(resolve => setTimeout(resolve, ms))

async function request(url, options = {}) {
  const response = await fetch(new URL(url, base), { ...options, signal: AbortSignal.timeout(120000) })
  if (!response.ok) throw new Error(`${response.status}: ${(await response.text()).slice(0, 1000)}`)
  return response.json()
}

async function backtest(competition, range, model) {
  const query = new URLSearchParams({ competition, includePreviousEdition: String(range === 'PREVIOUS'), simulations: String(audit.simulations) })
  let job = await request(`/api/football/recommendation-backtest/jobs?${query}`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ modelFactorsByCompetition: { [competition]: model } })
  })
  const started = Date.now()
  while (['QUEUED', 'RUNNING'].includes(job.status)) {
    if (Date.now() - started > 1800000) throw new Error(`${competition}:${range} 服务复验超时`)
    await pause(1000)
    job = await request(`/api/football/recommendation-backtest/jobs/${job.jobId}`)
  }
  if (job.status !== 'COMPLETED') throw new Error(job.message || '服务回测失败')
  return job.result.matches
}

function compareMetrics(matches, row, plan) {
  const periods = { full: matches, training: matches.filter(match => match.matchDate in plan.dateToBlock),
    validation: matches.filter(match => plan.validationFrom && match.matchDate >= plan.validationFrom) }
  const parameters = row.market === 'WDL' ? row.profile.globalParameters : row.strategy
  const key = `${row.competition}:${row.range}:${row.market}:${row.preset}`
  const metrics = {}
  for (const [period, selected] of Object.entries(periods)) {
    const actual = verifyMetrics(selected, parameters, row.market)
    const expected = row.after[period]
    metrics[period] = actual
    for (const field of ['availableMatchCount', 'recommendedMatchCount', 'recommendedSelectionCount', 'winningSelectionCount',
      'totalStake', 'totalReturn', 'netProfit', 'roi', 'samplingRate', 'selectionHitRate', 'volatility']) {
      if (actual[field] === expected[field]) continue
      if (Number.isFinite(actual[field]) && Number.isFinite(expected[field]) && Math.abs(actual[field] - expected[field]) < 1e-7) continue
      audit.violations.push(`${key}:${period}:${field} ${actual[field]} != ${expected[field]}`)
    }
  }
  audit.results.push({ key, metrics })
}

async function verifyScores(row, scores, oddsMatches) {
  const options = report.manifest.options
  const matches = await fetchScoreBacktest({ apiBase: base, competition: row.competition, range: row.range, asOfDate: options.asOfDate },
    row.profile.modelFactors, { simulations: audit.simulations })
  const regularScores = new Map(matches.map(match => [`${match.matchDate}:${match.matchId}`, `${match.homeScore}-${match.awayScore}`]))
  const key = `${row.competition}:${row.range}:${row.preset}`
  for (const match of oddsMatches) {
    if (regularScores.get(`${match.matchDate}:${match.matchId}`) !== match.scoreText.replace(/\s/g, '')) {
      audit.violations.push(`${key}:${match.matchId} 比分与投注样本不一致`)
    }
  }
  const { validationFrom, freshFrom } = scores.plan
  const periods = { full: matches, training: matches.filter(match => validationFrom && match.matchDate < validationFrom),
    validation: matches.filter(match => validationFrom && match.matchDate >= validationFrom),
    fresh: matches.filter(match => match.matchDate >= freshFrom) }
  const expected = scores.profiles[row.preset === 'STABLE' ? 0 : 1].periods
  const metrics = {}
  for (const [period, subset] of Object.entries(periods)) {
    const hits = subset.filter(match => match.topScores.some(score => score.homeScore === match.homeScore && score.awayScore === match.awayScore)).length
    metrics[period] = { matches: subset.length, hits }
    if (subset.length !== expected[period].after.matches || hits !== expected[period].after.hits) {
      audit.violations.push(`${key}:${period} 比分复验 ${hits}/${subset.length} != ${expected[period].after.hits}/${expected[period].after.matches}`)
    }
  }
  audit.scoreResults.push({ key, metrics })
  audit.checkedScoreProfiles++
}

async function main() {
  const config = await request('/api/football/user-config')
  const disk = JSON.parse(await fs.readFile(configPath, 'utf8'))
  assert.deepEqual(config.parameterProfiles, disk.parameterProfiles, '服务读取的胜平负配置与磁盘不一致')
  assert.deepEqual(config.totalGoalsStrategies, disk.totalGoalsStrategies, '服务读取的进球数配置与磁盘不一致')
  const tasks = []
  for (const result of report.results) for (const range of result.ranges) {
    for (const preset of ['STABLE', 'AGGRESSIVE']) {
      const row = range.rows.find(item => item.market === 'WDL' && item.preset === preset)
      assert.deepEqual(config.parameterProfiles[`${row.competition}:${row.range}:${row.preset}`], row.profile)
      tasks.push({ row, range, goals: preset === 'STABLE' ? range.rows.find(item => item.market === 'GOALS') : null })
    }
  }
  let next = 0
  let stopped = false
  const timer = setInterval(() => process.stdout.write(`服务复验 ${audit.checkedProfiles}/68 胜平负，${audit.checkedGoals}/34 进球数\n`), 20000)
  try {
    await Promise.all(Array.from({ length: Math.min(parallelism, tasks.length) }, async () => {
      while (!stopped && next < tasks.length) {
        const { row, range, goals } = tasks[next++]
        const matches = await backtest(row.competition, row.range, row.profile.modelFactors)
        const dataHash = hash(matches.map(match => [match.matchId, match.matchDate, match.scoreText, match.sportteryMatchId,
          match.sportteryNormalAvailable, match.sportteryNormalOdds, match.sportteryHandicap, match.sportteryHandicapOdds, match.sportteryTotalGoalsOdds])
          .sort((a, b) => String(a[0]).localeCompare(String(b[0]))))
        if (dataHash !== range.dataHash) audit.dataDrift.push(`${row.competition}:${row.range}:${row.preset}`)
        compareMetrics(matches, row, range.plan)
        audit.checkedProfiles++
        if (goals) {
          assert.deepEqual(config.totalGoalsStrategies[`${row.competition}:${row.range}`], goals.strategy)
          compareMetrics(goalMatches(matches), goals, range.goalsPlan)
          audit.checkedGoals++
        }
        if (range.scores) await verifyScores(row, range.scores, matches)
      }
    }))
  } catch (error) {
    stopped = true
    throw error
  } finally { clearInterval(timer) }
  audit.completedAt = new Date().toISOString()
  audit.status = audit.violations.length || audit.dataDrift.length ? 'FAILED' : 'PASSED'
  audit.configSha256 = hash(await fs.readFile(configPath))
  audit.jarSha256 = hash(await fs.readFile(jarPath))
  await fs.writeFile(outputFile, JSON.stringify(audit, null, 2) + '\n', 'utf8')
  process.stdout.write(JSON.stringify({ outputFile, status: audit.status, checkedProfiles: audit.checkedProfiles,
    checkedGoals: audit.checkedGoals, checkedScoreProfiles: audit.checkedScoreProfiles,
    dataDrift: audit.dataDrift, violations: audit.violations }, null, 2) + '\n')
  if (audit.status !== 'PASSED') process.exitCode = 1
}

main().catch(error => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1 })
