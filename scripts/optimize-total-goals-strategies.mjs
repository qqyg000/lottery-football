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
const SMALL_CURRENT_SAMPLE_MAX = 10
const PRIMARY_ROI_TARGET = 0.25
const MINIMUM_SAMPLING_RATE = 0.333
const MINIMUM_PROBABILITIES = [0, 5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 35, 40]
const MINIMUM_EXPECTED_VALUES = [0, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.05, 1.1, 1.15, 1.2, 1.25, 1.3, 1.4]
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
const JSON_EOL = process.platform === 'win32' ? '\r\n' : '\n'

async function main() {
  const configText = await fs.readFile(CONFIG_PATH, 'utf8')
  const config = JSON.parse(configText)
  const existingReport = TARGET_COMPETITIONS.length === COMPETITIONS.length
    ? null
    : await readExistingReport()
  const rangeResults = {}
  for (const range of ['CURRENT', 'PREVIOUS']) {
    const includePreviousEdition = range === 'PREVIOUS'
    process.stdout.write(`开始回测${includePreviousEdition ? '含上届' : '仅本届'}策略\n`)
    const result = await runBacktest(config, range, includePreviousEdition)
    rangeResults[range] = optimizeRange(result, range, config.totalGoalsStrategies || {})
  }

  const strategies = { ...(config.totalGoalsStrategies || {}) }
  const reportRows = existingReport
    ? existingReport.strategies.filter(row => !TARGET_COMPETITIONS.includes(row.competition))
    : []
  for (const competition of TARGET_COMPETITIONS) {
    for (const range of ['CURRENT', 'PREVIOUS']) {
      const direct = rangeResults[range][competition]
      const fallback = rangeResults.PREVIOUS[competition]
      const usePreviousStrategy = range === 'CURRENT' &&
        (direct?.metrics.availableMatchCount || 0) <= SMALL_CURRENT_SAMPLE_MAX
      const selected = usePreviousStrategy ? fallback : direct
      const disabledBecauseSamplingTargetNotMet = !selected?.strategyAvailable
      const strategy = disabledBecauseSamplingTargetNotMet
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
        disabledBecauseRoiTargetNotMet: disabledBecauseSamplingTargetNotMet,
        disabledBecauseSamplingTargetNotMet,
        samplingRateTargetMet: Boolean(selected?.samplingRateTargetMet),
        primaryRoiTargetMet: Boolean(selected?.primaryRoiTargetMet),
        degradationLevel: selected?.degradationLevel || 'NO_STRATEGY',
        roiFloor: selected?.roiFloor ?? null,
        hitRateFloor: selected?.hitRateFloor ?? null,
        reliabilityTier: selected?.reliabilityTier || null,
        strategy,
        metrics: selected?.metrics || emptyMetrics(),
        directMetrics: direct?.metrics || emptyMetrics(),
        previousStrategy: direct?.baselineStrategy || null,
        previousStrategyMetrics: direct?.baselineMetrics || emptyMetrics(),
        minimumRecommendedMatchConstraint: selected?.minimumRecommendedMatchConstraint || 0,
        minimumSelectionConstraint: selected?.minimumSelectionConstraint || 0,
        minimumWinningSelectionConstraint: selected?.minimumWinningSelectionConstraint || 0
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

  await fs.writeFile(
    CONFIG_PATH,
    replaceConfigObject(configText, 'totalGoalsStrategies', strategies),
    'utf8'
  )
  const report = {
    generatedAt: new Date().toISOString(),
    source: '中国体彩网总进球数固定赔率初始值',
    modelMode: 'after',
    simulations: SIMULATIONS,
    stakeMethod: '每个推荐项作为独立单关等额投注1单位',
    roiFormula: 'ROI = (命中单关赔率返奖合计 - 推荐总注数) / 推荐总注数',
    hitRateFormula: '命中率 = 命中单关注数 / 推荐总注数',
    optimizationObjective: '所有有进球数赔率样本的策略采样率必须大于等于33.3%，在达标候选中优先最大化ROI，再比较单注命中率和采样率',
    constraints: {
      minimumSamplingRateInclusive: MINIMUM_SAMPLING_RATE,
      minimumRecommendedMatches: `ceil(可用比赛数 * ${MINIMUM_SAMPLING_RATE})`,
      minimumRoi: null,
      minimumHitRate: null,
      maximumSelectionsPerMatch: '0-4',
      strategySearch: '先粗网格搜索，取ROI最高的8个达标候选，再基于实际概率、期望值和赔率分布执行最多3轮坐标细化',
      smallCurrentSampleRule: `仅本届可用样本数不超过${SMALL_CURRENT_SAMPLE_MAX}场时跳过独立优化，直接沿用含上届策略`,
      selectionPreference: '采样率达标后先比较ROI，再比较单注命中率、采样率、每场命中率和有效注数'
    },
    strategies: reportRows
  }
  await fs.writeFile(REPORT_PATH, serializeJson(report), 'utf8')
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

function optimizeRange(result, range, existingStrategies) {
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
    const skipSmallCurrentSample = range === 'CURRENT' && matches.length <= SMALL_CURRENT_SAMPLE_MAX
    const optimized = skipSmallCurrentSample
      ? {
          strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
          metrics: emptyMetrics(matches.length),
          minimumRecommendedMatchConstraint: 0,
          minimumSelectionConstraint: 0,
          minimumWinningSelectionConstraint: 0,
          strategyAvailable: false,
          samplingRateTargetMet: false,
          primaryRoiTargetMet: false,
          degradationLevel: 'SMALL_CURRENT_SAMPLE_SKIPPED'
        }
      : optimizeCompetition(matches)
    const baselineStrategy = existingStrategies[`${competition}:${range}`] || DEFAULT_STRATEGY
    optimized.baselineStrategy = { ...baselineStrategy }
    optimized.baselineMetrics = skipSmallCurrentSample
      ? emptyMetrics(matches.length)
      : evaluateStrategy(matches, baselineStrategy)
    process.stdout.write(
      `  ${COMPETITION_NAMES[competition]} ${range}: ${matches.length} 场，` +
      `${optimized.metrics.recommendedSelectionCount} 注，` +
      `命中率 ${formatPercent(optimized.metrics.hitRate)}，ROI ${formatPercent(optimized.metrics.roi)}` +
      `${skipSmallCurrentSample ? '（小样本跳过独立优化）' : ''}\n`
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
    if (directMetrics.availableMatchCount > SMALL_CURRENT_SAMPLE_MAX) {
      continue
    }
    current.fallbackToPreviousEdition = true
    current.disabledBecauseRoiTargetNotMet = false
    current.disabledBecauseSamplingTargetNotMet = false
    current.strategy = { ...previous.strategy }
    current.metrics = { ...previous.metrics }
    current.minimumRecommendedMatchConstraint = previous.minimumRecommendedMatchConstraint
    current.minimumSelectionConstraint = previous.minimumSelectionConstraint
    current.minimumWinningSelectionConstraint = previous.minimumWinningSelectionConstraint
    current.samplingRateTargetMet = previous.samplingRateTargetMet
    current.primaryRoiTargetMet = previous.primaryRoiTargetMet
    current.degradationLevel = previous.degradationLevel
    current.roiFloor = previous.roiFloor
    current.hitRateFloor = previous.hitRateFloor
    current.reliabilityTier = previous.reliabilityTier
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
  items.sort((left, right) => (
    right.probability - left.probability
    || right.expectedValue - left.expectedValue
    || left.totalGoals - right.totalGoals
  ))
  return items.length > 0 ? { items } : null
}

function optimizeCompetition(matches) {
  if (matches.length === 0) {
    return {
      strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
      metrics: emptyMetrics(),
      minimumRecommendedMatchConstraint: 0,
      minimumSelectionConstraint: 0,
      minimumWinningSelectionConstraint: 0,
      strategyAvailable: false,
      samplingRateTargetMet: false,
      primaryRoiTargetMet: false,
      degradationLevel: 'NO_DATA'
    }
  }
  const minimumRecommendedMatches = Math.ceil(matches.length * MINIMUM_SAMPLING_RATE)
  const candidates = enumerateStrategies(matches)
  const coarseCandidates = selectTopRoiStrategies(candidates, minimumRecommendedMatches, 8)
  if (coarseCandidates.length > 0) {
    let best = null
    for (const coarseCandidate of coarseCandidates) {
      const refined = refineStrategy(matches, coarseCandidate, minimumRecommendedMatches)
      if (isHigherRoi(refined.metrics, best?.metrics)) {
        best = refined
      }
    }
    return {
      ...best,
      minimumRecommendedMatchConstraint: minimumRecommendedMatches,
      minimumSelectionConstraint: minimumRecommendedMatches,
      minimumWinningSelectionConstraint: 0,
      reliabilityTier: 'SAMPLING_RATE_33_3',
      hitRateFloor: null,
      roiFloor: null,
      strategyAvailable: true,
      samplingRateTargetMet: true,
      primaryRoiTargetMet: best.metrics.roi > PRIMARY_ROI_TARGET,
      degradationLevel: 'SAMPLING_RATE_33_3'
    }
  }
  return {
    strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
    metrics: emptyMetrics(matches.length),
    minimumRecommendedMatchConstraint: 0,
    minimumSelectionConstraint: 0,
    minimumWinningSelectionConstraint: 0,
    strategyAvailable: false,
    samplingRateTargetMet: false,
    primaryRoiTargetMet: false,
    degradationLevel: 'SAMPLING_RATE_TARGET_NOT_MET'
  }
}

function enumerateStrategies(matches) {
  const candidates = []
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
            if (metrics.recommendedSelectionCount > 0 && metrics.roi !== null) {
              candidates.push({ strategy, metrics })
            }
          }
        }
      }
    }
  }
  return candidates
}

function selectTopRoiStrategies(candidates, minimumRecommendedMatches, limit) {
  const best = []
  for (const candidate of candidates) {
    const metrics = candidate.metrics
    if (metrics.recommendedMatchCount < minimumRecommendedMatches) {
      continue
    }
    const insertionIndex = best.findIndex(existing => isHigherRoi(metrics, existing.metrics))
    if (insertionIndex >= 0) {
      best.splice(insertionIndex, 0, candidate)
    } else if (best.length < limit) {
      best.push(candidate)
    }
    if (best.length > limit) {
      best.pop()
    }
  }
  return best
}

function refineStrategy(matches, initialBest, minimumRecommendedMatches) {
  let best = initialBest
  const parameterValues = [
    ['minimumProbability', createRefinementValues(matches, item => item.probability, best.strategy.minimumProbability)],
    ['minimumExpectedValue', createRefinementValues(matches, item => item.expectedValue, best.strategy.minimumExpectedValue)],
    ['minimumOdds', createRefinementValues(matches, item => item.odds, best.strategy.minimumOdds)],
    ['maximumOdds', createRefinementValues(matches, item => item.odds, best.strategy.maximumOdds)],
    ['maximumSelections', MAXIMUM_SELECTIONS]
  ]
  for (let pass = 0; pass < 3; pass += 1) {
    let improved = false
    for (const [parameter, values] of parameterValues) {
      for (const value of values) {
        const strategy = { ...best.strategy, [parameter]: value }
        if (strategy.maximumOdds < strategy.minimumOdds) {
          continue
        }
        const metrics = evaluateStrategy(matches, strategy)
        if (
          metrics.recommendedMatchCount >= minimumRecommendedMatches
          && isHigherRoi(metrics, best.metrics)
        ) {
          best = { strategy, metrics }
          improved = true
        }
      }
    }
    if (!improved) {
      break
    }
  }
  return best
}

function createRefinementValues(matches, valueSelector, currentValue) {
  const values = Array.from(new Set(matches.flatMap(match => (
    match.items.map(item => round(valueSelector(item), 6))
  )))).filter(Number.isFinite).sort((left, right) => left - right)
  if (!values.includes(currentValue)) {
    values.push(currentValue)
    values.sort((left, right) => left - right)
  }
  if (values.length <= 121) {
    return values
  }
  const selected = new Set([values[0], values.at(-1), currentValue])
  for (let index = 0; index < 101; index += 1) {
    selected.add(values[Math.round(index * (values.length - 1) / 100)])
  }
  const currentIndex = values.reduce((closestIndex, value, index) => (
    Math.abs(value - currentValue) < Math.abs(values[closestIndex] - currentValue) ? index : closestIndex
  ), 0)
  for (let offset = -10; offset <= 10; offset += 1) {
    const index = currentIndex + offset
    if (index >= 0 && index < values.length) {
      selected.add(values[index])
    }
  }
  return Array.from(selected).sort((left, right) => left - right)
}

function evaluateStrategy(matches, strategy) {
  if (!Number.isInteger(strategy.maximumSelections) || strategy.maximumSelections <= 0) {
    return emptyMetrics(matches.length)
  }
  let recommendedMatchCount = 0
  let recommendedSelectionCount = 0
  let winningSelectionCount = 0
  let totalReturn = 0
  for (const match of matches) {
    let matchSelectionCount = 0
    let winningOdds = null
    for (const item of match.items) {
      if (
        item.probability >= strategy.minimumProbability &&
        item.expectedValue >= strategy.minimumExpectedValue &&
        item.odds >= strategy.minimumOdds &&
        item.odds <= strategy.maximumOdds
      ) {
        matchSelectionCount += 1
        if (item.winning) {
          winningOdds = item.odds
        }
        if (matchSelectionCount >= strategy.maximumSelections) {
          break
        }
      }
    }
    if (matchSelectionCount === 0) {
      continue
    }
    recommendedMatchCount += 1
    recommendedSelectionCount += matchSelectionCount
    if (winningOdds != null) {
      winningSelectionCount += 1
      totalReturn += winningOdds
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

function isHigherRoi(candidate, current) {
  if (!current) {
    return true
  }
  if (candidate.roi !== current.roi) {
    return candidate.roi > current.roi
  }
  if (candidate.hitRate !== current.hitRate) {
    return candidate.hitRate > current.hitRate
  }
  if (candidate.samplingRate !== current.samplingRate) {
    return candidate.samplingRate > current.samplingRate
  }
  if (candidate.matchHitRate !== current.matchHitRate) {
    return candidate.matchHitRate > current.matchHitRate
  }
  return candidate.recommendedSelectionCount > current.recommendedSelectionCount
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
      `${row.degradationLevel && row.degradationLevel !== 'PRIMARY' ? `（${row.degradationLevel}）` : ''}` +
      `${row.fallbackToPreviousEdition ? '（沿用含上届样本）' : ''}` +
      `${row.disabledBecauseRoiTargetNotMet ? '（无可用样本或无满足采样率约束的策略，不投注）' : ''}\n`
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

function serializeJson(value) {
  return JSON.stringify(value, null, 2).replace(/\n/g, JSON_EOL) + JSON_EOL
}

function replaceConfigObject(configText, propertyName, value) {
  const marker = `"${propertyName}"`
  const propertyStart = configText.indexOf(marker)
  if (propertyStart < 0) {
    throw new Error(`配置文件缺少 ${propertyName} 字段`)
  }
  const colonIndex = configText.indexOf(':', propertyStart + marker.length)
  const objectStart = configText.indexOf('{', colonIndex + 1)
  if (colonIndex < 0 || objectStart < 0) {
    throw new Error(`配置文件中 ${propertyName} 不是有效对象`)
  }

  let depth = 0
  let inString = false
  let escaped = false
  let objectEnd = -1
  for (let index = objectStart; index < configText.length; index += 1) {
    const character = configText[index]
    if (inString) {
      if (escaped) {
        escaped = false
      } else if (character === '\\') {
        escaped = true
      } else if (character === '"') {
        inString = false
      }
      continue
    }
    if (character === '"') {
      inString = true
    } else if (character === '{') {
      depth += 1
    } else if (character === '}') {
      depth -= 1
      if (depth === 0) {
        objectEnd = index + 1
        break
      }
    }
  }
  if (objectEnd < 0) {
    throw new Error(`配置文件中 ${propertyName} 对象未正确结束`)
  }

  const eol = configText.includes('\r\n') ? '\r\n' : '\n'
  const lineStart = configText.lastIndexOf('\n', propertyStart) + 1
  const indent = configText.slice(lineStart, propertyStart)
  const replacement = JSON.stringify(value, null, 2)
    .replace(/^(\s*)"((?:\\.|[^"])*)":/gm, '$1"$2" :')
    .split('\n')
    .map((line, index) => index === 0 ? line : `${indent}${line}`)
    .join(eol)
  return configText.slice(0, objectStart) + replacement + configText.slice(objectEnd)
}

main().catch(error => {
  process.stderr.write(`${error.stack || error.message}\n`)
  process.exitCode = 1
})
