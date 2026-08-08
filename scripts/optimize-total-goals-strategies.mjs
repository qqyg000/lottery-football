import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'

const ROOT = path.resolve(import.meta.dirname, '..')
const CONFIG_PATH = path.join(ROOT, 'config', 'user-config.json')
const REPORT_PATH = path.join(ROOT, 'reports', 'total-goals-strategy-backtest.json')
const BASE_URL = readArgument('--base-url', 'http://127.0.0.1:18080')
const SIMULATIONS = Number(readArgument('--simulations', '5000'))
const COMPETITIONS = [
  'WORLD_CUP',
  'EUROPEAN_CHAMPIONSHIP',
  'COPA_AMERICA',
  'CLUB_WORLD_CUP',
  'EUROPA_LEAGUE',
  'CHAMPIONS_LEAGUE',
  'PREMIER_LEAGUE',
  'LA_LIGA',
  'SERIE_A',
  'BUNDESLIGA',
  'LIGUE_1',
  'PRIMEIRA_LIGA',
  'EREDIVISIE',
  'ARGENTINE_PRIMERA_DIVISION',
  'SWEDISH_ALLSVENSKAN',
  'FINNISH_VEIKKAUSLIIGA',
  'K_LEAGUE_1'
]
const COMPETITION_NAMES = {
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
  PRIMEIRA_LIGA: '葡超',
  EREDIVISIE: '荷甲',
  ARGENTINE_PRIMERA_DIVISION: '阿甲',
  SWEDISH_ALLSVENSKAN: '瑞超',
  FINNISH_VEIKKAUSLIIGA: '芬超',
  K_LEAGUE_1: '韩职'
}
const requestedCompetitions = readArgument('--competitions', 'ALL')
const TARGET_COMPETITIONS = requestedCompetitions === 'ALL'
  ? COMPETITIONS
  : requestedCompetitions.split(',').map(value => value.trim()).filter(value => COMPETITIONS.includes(value))
if (TARGET_COMPETITIONS.length === 0) {
  throw new Error('--competitions 未包含有效赛事代码')
}
const ODDS_KEYS = ['goal0', 'goal1', 'goal2', 'goal3', 'goal4', 'goal5', 'goal6', 'goal7Plus']
const TARGET_ROI = 0.20
const MINIMUM_PROBABILITIES = [0, 5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 35]
const MINIMUM_EXPECTED_VALUES = [0.8, 0.9, 1, 1.05, 1.1, 1.15, 1.2, 1.25, 1.3, 1.4]
const MINIMUM_ODDS = [1.01, 1.5, 2, 2.5, 3, 3.5, 4]
const MAXIMUM_ODDS = [3, 4, 5, 6, 8, 10, 20, 50]
const MAXIMUM_SELECTIONS = [1, 2, 3, 4]
const DEFAULT_STRATEGY = {
  minimumProbability: 15,
  minimumExpectedValue: 1,
  minimumOdds: 1.01,
  maximumOdds: 8,
  maximumSelections: 1
}

async function main() {
  const config = JSON.parse(await fs.readFile(CONFIG_PATH, 'utf8'))
  const existingReport = TARGET_COMPETITIONS.length === COMPETITIONS.length
    ? null
    : await readExistingReport()
  const rangeResults = {}
  for (const range of ['CURRENT', 'PREVIOUS']) {
    const includePreviousEdition = range === 'PREVIOUS'
    process.stdout.write(`开始回测${includePreviousEdition ? '含上届' : '仅本届'}策略\n`)
    const result = await runBacktest(config, range, includePreviousEdition)
    rangeResults[range] = optimizeRange(result, range)
  }

  const strategies = { ...(config.totalGoalsStrategies || {}) }
  const reportRows = existingReport
    ? existingReport.strategies.filter(row => !TARGET_COMPETITIONS.includes(row.competition))
    : []
  for (const competition of TARGET_COMPETITIONS) {
    for (const range of ['CURRENT', 'PREVIOUS']) {
      const direct = rangeResults[range][competition]
      const fallback = rangeResults.PREVIOUS[competition]
      const directSampleIsSufficient = direct?.roiTargetMet &&
        direct.metrics.availableMatchCount >= 8 &&
        direct.metrics.recommendedSelectionCount >= 8
      const selected = directSampleIsSufficient || range === 'PREVIOUS' ? direct : fallback
      const disabledBecauseRoiTargetNotMet = !selected?.roiTargetMet
      const strategy = disabledBecauseRoiTargetNotMet
        ? { ...(selected?.strategy || DEFAULT_STRATEGY), maximumSelections: 0 }
        : selected.strategy
      const key = `${competition}:${range}`
      strategies[key] = strategy
      reportRows.push({
        key,
        competition,
        competitionName: COMPETITION_NAMES[competition],
        range,
        rangeName: range === 'CURRENT' ? '仅本届' : '含上届',
        fallbackToPreviousEdition: selected !== direct,
        disabledBecauseRoiTargetNotMet,
        strategy,
        metrics: selected?.metrics || emptyMetrics(),
        directMetrics: direct?.metrics || emptyMetrics(),
        minimumSelectionConstraint: selected?.minimumSelectionConstraint || 0
      })
    }
  }
  applySmallCurrentSampleFallback(reportRows, strategies)
  reportRows.sort((left, right) => {
    const competitionCompare = COMPETITIONS.indexOf(left.competition) - COMPETITIONS.indexOf(right.competition)
    if (competitionCompare !== 0) {
      return competitionCompare
    }
    return left.range === 'CURRENT' ? -1 : 1
  })

  config.totalGoalsStrategies = strategies
  await fs.writeFile(CONFIG_PATH, JSON.stringify(config, null, 2) + '\n', 'utf8')
  const report = {
    generatedAt: new Date().toISOString(),
    source: '中国体彩网总进球数固定赔率初始值',
    modelMode: 'after',
    simulations: SIMULATIONS,
    stakeMethod: '每个推荐项作为独立单关等额投注1单位',
    roiFormula: 'ROI = (命中单关赔率返奖合计 - 推荐总注数) / 推荐总注数',
    hitRateFormula: '命中率 = 命中单关注数 / 推荐总注数',
    optimizationObjective: 'ROI严格大于20%的前提下优先最大化单注命中率，其次最大化有效注数',
    constraints: {
      minimumRoiExclusive: TARGET_ROI,
      maximumSelectionsPerMatch: '0-4',
      minimumRecommendedSelections: '优先max(8, 可用比赛数的10%)，无解时依次降到5%和至少3注'
    },
    strategies: reportRows
  }
  await fs.writeFile(REPORT_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
  process.stdout.write(`策略已写入 ${CONFIG_PATH}\n`)
  process.stdout.write(`回测报告已写入 ${REPORT_PATH}\n`)
  printSummary(reportRows)
}

async function runBacktest(config, range, includePreviousEdition) {
  const modelFactorsByCompetition = Object.fromEntries(TARGET_COMPETITIONS.map(competition => {
    const key = `${competition}:${range}:STABLE`
    const fallbackKey = `${competition}:CURRENT:STABLE`
    const profile = config.parameterProfiles?.[key] || config.parameterProfiles?.[fallbackKey] || {}
    return [competition, profile.modelFactors || {}]
  }))
  const params = new URLSearchParams({
    simulations: String(SIMULATIONS),
    competition: TARGET_COMPETITIONS.length === COMPETITIONS.length ? 'ALL' : TARGET_COMPETITIONS.join(','),
    includePreviousEdition: String(includePreviousEdition)
  })
  const response = await fetch(`${BASE_URL}/api/football/recommendation-backtest/jobs?${params}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ modelFactorsByCompetition })
  })
  if (!response.ok) {
    throw new Error(`创建回测任务失败 ${response.status}: ${await response.text()}`)
  }
  let job = await response.json()
  let lastProgress = -1
  while (job.status === 'QUEUED' || job.status === 'RUNNING') {
    const progress = Math.floor(Number(job.progress) || 0)
    if (progress >= lastProgress + 10 || progress === 100) {
      process.stdout.write(`  ${progress}% (${job.processedMatchCount}/${job.totalMatchCount})\n`)
      lastProgress = progress
    }
    await wait(750)
    const progressResponse = await fetch(`${BASE_URL}/api/football/recommendation-backtest/jobs/${encodeURIComponent(job.jobId)}`, {
      cache: 'no-store'
    })
    if (!progressResponse.ok) {
      throw new Error(`读取回测任务失败 ${progressResponse.status}: ${await progressResponse.text()}`)
    }
    job = await progressResponse.json()
  }
  if (job.status !== 'COMPLETED' || !job.result) {
    throw new Error(job.message || `回测任务状态异常: ${job.status}`)
  }
  process.stdout.write(`  完成，共 ${job.result.matches?.length || 0} 场\n`)
  return job.result
}

function optimizeRange(result, range) {
  const matchesByCompetition = Object.fromEntries(TARGET_COMPETITIONS.map(competition => [competition, []]))
  for (const match of result.matches || []) {
    if (matchesByCompetition[match.competition]) {
      const prepared = prepareMatch(match)
      if (prepared) {
        matchesByCompetition[match.competition].push(prepared)
      }
    }
  }
  return Object.fromEntries(TARGET_COMPETITIONS.map(competition => {
    const matches = matchesByCompetition[competition]
    const optimized = optimizeCompetition(matches)
    process.stdout.write(
      `  ${COMPETITION_NAMES[competition]} ${range}: ${matches.length} 场，` +
      `${optimized.metrics.recommendedSelectionCount} 注，` +
      `命中率 ${formatPercent(optimized.metrics.hitRate)}，ROI ${formatPercent(optimized.metrics.roi)}\n`
    )
    return [competition, optimized]
  }))
}

async function readExistingReport() {
  try {
    return JSON.parse(await fs.readFile(REPORT_PATH, 'utf8'))
  } catch {
    throw new Error('定向优化需要已有的完整回测报告')
  }
}

function applySmallCurrentSampleFallback(reportRows, strategies) {
  for (const competition of COMPETITIONS) {
    const current = reportRows.find(row => row.competition === competition && row.range === 'CURRENT')
    const previous = reportRows.find(row => row.competition === competition && row.range === 'PREVIOUS')
    if (!current || !previous || previous.disabledBecauseRoiTargetNotMet) {
      continue
    }
    const directMetrics = current.directMetrics || emptyMetrics()
    if (directMetrics.availableMatchCount >= 8 && directMetrics.recommendedSelectionCount >= 8) {
      continue
    }
    current.fallbackToPreviousEdition = true
    current.disabledBecauseRoiTargetNotMet = false
    current.strategy = { ...previous.strategy }
    current.metrics = { ...previous.metrics }
    current.minimumSelectionConstraint = previous.minimumSelectionConstraint
    strategies[`${competition}:CURRENT`] = current.strategy
  }
}

function prepareMatch(match) {
  const scoreMatch = String(match.scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
  const probabilities = Array.isArray(match.adjustedSportteryTotalGoalsProbabilities)
    ? match.adjustedSportteryTotalGoalsProbabilities
    : []
  if (!scoreMatch || !match.sportteryTotalGoalsOdds || probabilities.length === 0) {
    return null
  }
  const actualTotalGoals = Number(scoreMatch[1]) + Number(scoreMatch[2])
  const probabilityByGoals = new Map(probabilities.map(item => [Number(item.totalGoals), Number(item.probability)]))
  const items = ODDS_KEYS.map((oddsKey, totalGoals) => {
    const probability = probabilityByGoals.get(totalGoals)
    const odds = Number(match.sportteryTotalGoalsOdds[oddsKey])
    return {
      totalGoals,
      probability,
      odds,
      expectedValue: probability / 100 * odds,
      winning: totalGoals === actualTotalGoals || (totalGoals === 7 && actualTotalGoals >= 7)
    }
  }).filter(item => Number.isFinite(item.probability) && Number.isFinite(item.odds) && item.odds > 0)
  return items.length > 0 ? { items } : null
}

function optimizeCompetition(matches) {
  if (matches.length === 0) {
    return {
      strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
      metrics: emptyMetrics(),
      minimumSelectionConstraint: 0,
      roiTargetMet: false
    }
  }
  const minimumSelectionConstraints = Array.from(new Set([
    Math.min(matches.length, Math.max(8, Math.ceil(matches.length * 0.10))),
    Math.min(matches.length, Math.max(8, Math.ceil(matches.length * 0.05))),
    Math.min(matches.length, 3)
  ])).sort((left, right) => right - left)
  for (const minimumSelectionConstraint of minimumSelectionConstraints) {
    const best = findBestStrategy(matches, minimumSelectionConstraint)
    if (best) {
      return {
        ...best,
        minimumSelectionConstraint,
        roiTargetMet: true
      }
    }
  }
  return {
    strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
    metrics: emptyMetrics(matches.length),
    minimumSelectionConstraint: 0,
    roiTargetMet: false
  }
}

function findBestStrategy(matches, minimumRecommendedSelections) {
  let best = null
  for (const minimumProbability of MINIMUM_PROBABILITIES) {
    for (const minimumExpectedValue of MINIMUM_EXPECTED_VALUES) {
      for (const minimumOdds of MINIMUM_ODDS) {
        for (const maximumOdds of MAXIMUM_ODDS) {
          if (maximumOdds < minimumOdds) {
            continue
          }
          for (const maximumSelections of MAXIMUM_SELECTIONS) {
            const strategy = {
              minimumProbability,
              minimumExpectedValue,
              minimumOdds,
              maximumOdds,
              maximumSelections
            }
            const metrics = evaluateStrategy(matches, strategy)
            if (
              metrics.recommendedSelectionCount < minimumRecommendedSelections ||
              metrics.roi === null ||
              metrics.roi <= TARGET_ROI
            ) {
              continue
            }
            if (isBetter(metrics, best?.metrics)) {
              best = { strategy, metrics }
            }
          }
        }
      }
    }
  }
  return best
}

function evaluateStrategy(matches, strategy) {
  let recommendedMatchCount = 0
  let recommendedSelectionCount = 0
  let winningSelectionCount = 0
  let totalReturn = 0
  for (const match of matches) {
    const recommendations = match.items
      .filter(item => (
        item.probability >= strategy.minimumProbability &&
        item.expectedValue >= strategy.minimumExpectedValue &&
        item.odds >= strategy.minimumOdds &&
        item.odds <= strategy.maximumOdds
      ))
      .sort((left, right) => right.probability - left.probability || right.expectedValue - left.expectedValue || left.totalGoals - right.totalGoals)
      .slice(0, strategy.maximumSelections)
    if (recommendations.length === 0) {
      continue
    }
    recommendedMatchCount += 1
    recommendedSelectionCount += recommendations.length
    const winner = recommendations.find(item => item.winning)
    if (winner) {
      winningSelectionCount += 1
      totalReturn += winner.odds
    }
  }
  const totalStake = recommendedSelectionCount
  return {
    availableMatchCount: matches.length,
    recommendedMatchCount,
    recommendedSelectionCount,
    winningSelectionCount,
    hitRate: totalStake > 0 ? winningSelectionCount / totalStake : null,
    matchHitRate: recommendedMatchCount > 0 ? winningSelectionCount / recommendedMatchCount : null,
    samplingRate: matches.length > 0 ? recommendedMatchCount / matches.length : null,
    totalStake,
    totalReturn: round(totalReturn),
    netProfit: round(totalReturn - totalStake),
    roi: totalStake > 0 ? round(totalReturn / totalStake - 1, 6) : null
  }
}

function isBetter(candidate, current) {
  if (!current) {
    return true
  }
  if (candidate.hitRate !== current.hitRate) {
    return candidate.hitRate > current.hitRate
  }
  if (candidate.recommendedSelectionCount !== current.recommendedSelectionCount) {
    return candidate.recommendedSelectionCount > current.recommendedSelectionCount
  }
  return candidate.roi > current.roi
}

function emptyMetrics(availableMatchCount = 0) {
  return {
    availableMatchCount,
    recommendedMatchCount: 0,
    recommendedSelectionCount: 0,
    winningSelectionCount: 0,
    hitRate: null,
    matchHitRate: null,
    samplingRate: null,
    totalStake: 0,
    totalReturn: 0,
    netProfit: 0,
    roi: null
  }
}

function printSummary(rows) {
  for (const row of rows) {
    process.stdout.write(
      `${row.competitionName} ${row.rangeName}: ` +
      `${row.metrics.recommendedMatchCount}/${row.metrics.availableMatchCount} 场，` +
      `${row.metrics.recommendedSelectionCount} 注，` +
      `命中率 ${formatPercent(row.metrics.hitRate)}，ROI ${formatPercent(row.metrics.roi)}` +
      `${row.fallbackToPreviousEdition ? '（沿用含上届样本）' : ''}` +
      `${row.disabledBecauseRoiTargetNotMet ? '（ROI未超过20%，策略不投注）' : ''}\n`
    )
  }
}

function formatPercent(value) {
  return Number.isFinite(value) ? `${(value * 100).toFixed(2)}%` : '--'
}

function round(value, scale = 4) {
  const factor = 10 ** scale
  return Math.round((value + Number.EPSILON) * factor) / factor
}

function readArgument(name, fallback) {
  const index = process.argv.indexOf(name)
  return index >= 0 && process.argv[index + 1] ? process.argv[index + 1] : fallback
}

function wait(milliseconds) {
  return new Promise(resolve => setTimeout(resolve, milliseconds))
}

main().catch(error => {
  process.stderr.write(`${error.stack || error.message}\n`)
  process.exitCode = 1
})
