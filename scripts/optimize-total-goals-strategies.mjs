import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'
import { meetsMinimumConstraint } from './total-goals-optimization-constraints.mjs'

const ROOT = path.resolve(import.meta.dirname, '..')
const CONFIG_PATH = path.join(ROOT, 'config', 'user-config.json')
const EVALUATE_ONLY = process.argv.includes('--evaluate-only')
const DRY_RUN = process.argv.includes('--dry-run')
const STRICT_CONSTRAINTS = process.argv.includes('--strict-constraints')
const OPTIMIZE_SMALL_CURRENT_SAMPLES = process.argv.includes('--optimize-small-current-samples')
const ROBUST_VALIDATION = process.argv.includes('--robust-validation')
const REPORT_PATH = path.resolve(ROOT, readArgument(
  '--report-path',
  EVALUATE_ONLY
    ? 'reports/total-goals-strategy-current-backtest.json'
    : 'reports/total-goals-strategy-backtest.json'
))
const BACKTEST_CACHE_PREFIX = readArgument('--backtest-cache-prefix', '')
const BASELINE_REPORT_PATH = readArgument('--baseline-report-path', '')
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
  'K_LEAGUE_1',
  'SCOTTISH_FA_CUP'
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
  K_LEAGUE_1: '韩职',
  SCOTTISH_FA_CUP: '苏足总杯'
}
const requestedCompetitions = readArgument('--competitions', 'ALL')
const TARGET_COMPETITIONS = requestedCompetitions === 'ALL'
  ? COMPETITIONS
  : requestedCompetitions.split(',').map(value => value.trim()).filter(value => COMPETITIONS.includes(value))
if (TARGET_COMPETITIONS.length === 0) {
  throw new Error('--competitions 未包含有效赛事代码')
}
const RANGES = ['CURRENT', 'PREVIOUS']
const requestedRanges = readArgument('--ranges', 'ALL').toUpperCase()
const TARGET_RANGES = requestedRanges === 'ALL'
  ? RANGES
  : requestedRanges.split(',').map(value => value.trim()).filter(value => RANGES.includes(value))
if (TARGET_RANGES.length === 0) {
  throw new Error('--ranges 未包含有效范围，可选值为 CURRENT、PREVIOUS 或 ALL')
}
const OPTIMIZATION_OBJECTIVE = readArgument('--objective', 'ROI').toUpperCase().replaceAll('-', '_')
if (!['ROI', 'HIT_RATE'].includes(OPTIMIZATION_OBJECTIVE)) {
  throw new Error('--objective 可选值为 ROI 或 HIT_RATE')
}
const ODDS_KEYS = ['goal0', 'goal1', 'goal2', 'goal3', 'goal4', 'goal5', 'goal6', 'goal7Plus']
const SMALL_CURRENT_SAMPLE_MAX = 10
const PRIMARY_ROI_TARGET = 0.25
const MINIMUM_ROI = Number(readArgument(
  '--minimum-roi',
  OPTIMIZATION_OBJECTIVE === 'HIT_RATE' ? String(PRIMARY_ROI_TARGET) : '0'
))
if (!Number.isFinite(MINIMUM_ROI)) {
  throw new Error('--minimum-roi 必须是有效数字')
}
const TOP_CANDIDATE_LIMIT = Number(readArgument('--candidate-limit', '8'))
if (!Number.isInteger(TOP_CANDIDATE_LIMIT) || TOP_CANDIDATE_LIMIT <= 0) {
  throw new Error('--candidate-limit 必须是正整数')
}
const VALIDATION_FRACTION = Number(readArgument('--validation-fraction', '0.3'))
if (!Number.isFinite(VALIDATION_FRACTION) || VALIDATION_FRACTION <= 0 || VALIDATION_FRACTION >= 0.5) {
  throw new Error('--validation-fraction 必须是大于0且小于0.5的数字')
}
const MINIMUM_VALIDATION_MATCHES = Number(readArgument('--minimum-validation-matches', '6'))
if (!Number.isInteger(MINIMUM_VALIDATION_MATCHES) || MINIMUM_VALIDATION_MATCHES < 3) {
  throw new Error('--minimum-validation-matches 必须是不小于3的整数')
}
const MINIMUM_TRAINING_MATCHES = Number(readArgument('--minimum-training-matches', '10'))
if (!Number.isInteger(MINIMUM_TRAINING_MATCHES) || MINIMUM_TRAINING_MATCHES < 5) {
  throw new Error('--minimum-training-matches 必须是不小于5的整数')
}
const MINIMUM_VALIDATION_ROI = Number(readArgument('--minimum-validation-roi', '0'))
if (!Number.isFinite(MINIMUM_VALIDATION_ROI)) {
  throw new Error('--minimum-validation-roi 必须是有效数字')
}
const MINIMUM_SAMPLING_RATE = Number(readArgument('--minimum-sampling-rate', '0.333'))
if (!Number.isFinite(MINIMUM_SAMPLING_RATE) || MINIMUM_SAMPLING_RATE <= 0 || MINIMUM_SAMPLING_RATE > 1) {
  throw new Error('--minimum-sampling-rate 必须是大于0且不大于1的数字')
}
const MINIMUM_HIT_RATE = Number(readArgument('--minimum-hit-rate', '0'))
if (!Number.isFinite(MINIMUM_HIT_RATE) || MINIMUM_HIT_RATE < 0 || MINIMUM_HIT_RATE > 1) {
  throw new Error('--minimum-hit-rate 必须是不小于0且不大于1的数字')
}
const FALLBACK_MINIMUM_SAMPLING_RATE = Number(readArgument('--fallback-minimum-sampling-rate', '0.25'))
if (
  !Number.isFinite(FALLBACK_MINIMUM_SAMPLING_RATE) ||
  FALLBACK_MINIMUM_SAMPLING_RATE <= 0 ||
  FALLBACK_MINIMUM_SAMPLING_RATE > MINIMUM_SAMPLING_RATE
) {
  throw new Error('--fallback-minimum-sampling-rate 必须是大于0且不大于主采样率的数字')
}
const FALLBACK_MINIMUM_HIT_RATE = Number(readArgument(
  '--fallback-minimum-hit-rate',
  String(Math.min(MINIMUM_HIT_RATE, 0.25))
))
if (
  !Number.isFinite(FALLBACK_MINIMUM_HIT_RATE) ||
  FALLBACK_MINIMUM_HIT_RATE < 0 ||
  FALLBACK_MINIMUM_HIT_RATE > MINIMUM_HIT_RATE
) {
  throw new Error('--fallback-minimum-hit-rate 必须是不小于0且不大于主命中率的数字')
}
const SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE = Number(readArgument(
  '--secondary-fallback-minimum-sampling-rate',
  '0.2'
))
if (
  !Number.isFinite(SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE) ||
  SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE <= 0 ||
  SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE > FALLBACK_MINIMUM_SAMPLING_RATE
) {
  throw new Error('--secondary-fallback-minimum-sampling-rate 必须是大于0且不大于25%降级采样率的数字')
}
const SECONDARY_FALLBACK_MINIMUM_HIT_RATE = Number(readArgument(
  '--secondary-fallback-minimum-hit-rate',
  String(Math.min(FALLBACK_MINIMUM_HIT_RATE, 0.2))
))
if (
  !Number.isFinite(SECONDARY_FALLBACK_MINIMUM_HIT_RATE) ||
  SECONDARY_FALLBACK_MINIMUM_HIT_RATE < 0 ||
  SECONDARY_FALLBACK_MINIMUM_HIT_RATE > FALLBACK_MINIMUM_HIT_RATE
) {
  throw new Error('--secondary-fallback-minimum-hit-rate 必须是不小于0且不大于25%降级命中率的数字')
}
const TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE = Number(readArgument(
  '--tertiary-fallback-minimum-sampling-rate',
  String(SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE)
))
if (
  !Number.isFinite(TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE) ||
  TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE <= 0 ||
  TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE > SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE
) {
  throw new Error('--tertiary-fallback-minimum-sampling-rate 必须是大于0且不大于二级降级采样率的数字')
}
const TERTIARY_FALLBACK_MINIMUM_HIT_RATE = Number(readArgument(
  '--tertiary-fallback-minimum-hit-rate',
  String(Math.min(SECONDARY_FALLBACK_MINIMUM_HIT_RATE, TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE))
))
if (
  !Number.isFinite(TERTIARY_FALLBACK_MINIMUM_HIT_RATE) ||
  TERTIARY_FALLBACK_MINIMUM_HIT_RATE < 0 ||
  TERTIARY_FALLBACK_MINIMUM_HIT_RATE > SECONDARY_FALLBACK_MINIMUM_HIT_RATE
) {
  throw new Error('--tertiary-fallback-minimum-hit-rate 必须是不小于0且不大于二级降级命中率的数字')
}
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
  const baselineStrategies = await readBaselineStrategies(config.totalGoalsStrategies || {})
  const existingReport = TARGET_COMPETITIONS.length === COMPETITIONS.length && TARGET_RANGES.length === RANGES.length
    ? null
    : await readExistingReport()
  const rangeResults = {}
  for (const range of TARGET_RANGES) {
    const includePreviousEdition = range === 'PREVIOUS'
    process.stdout.write(`开始回测${includePreviousEdition ? '含上届' : '仅本届'}策略\n`)
    const result = await runBacktest(config, range, includePreviousEdition)
    rangeResults[range] = optimizeRange(result, range, baselineStrategies)
  }

  const strategies = { ...(config.totalGoalsStrategies || {}) }
  const reportRows = existingReport
    ? existingReport.strategies.filter(row => !(
        TARGET_COMPETITIONS.includes(row.competition) && TARGET_RANGES.includes(row.range)
      ))
    : []
  for (const competition of TARGET_COMPETITIONS) {
    for (const range of TARGET_RANGES) {
      const direct = rangeResults[range][competition]
      const fallback = rangeResults.PREVIOUS?.[competition]
      const usePreviousStrategy = !EVALUATE_ONLY && range === 'CURRENT' &&
        shouldFallbackToPreviousEdition(direct?.metrics.availableMatchCount || 0)
      const selected = usePreviousStrategy ? fallback : direct
      const disabledBecauseConstraintsNotMet = !selected?.strategyAvailable
      const strategy = disabledBecauseConstraintsNotMet
        ? { ...(selected?.strategy || DEFAULT_STRATEGY), maximumSelections: 0 }
        : selected.strategy
      const currentEditionFallbackMetrics = usePreviousStrategy
        ? evaluateStrategy(direct?.evaluationMatches || [], strategy)
        : null
      const key = `${competition}:${range}`
      strategies[key] = strategy
      reportRows.push({
        key,
        competition,
        competitionName: COMPETITION_NAMES[competition],
        range,
        rangeName: range === 'CURRENT' ? '仅本届' : '含上届',
        fallbackToPreviousEdition: selected !== direct,
        disabledBecauseRoiTargetNotMet: disabledBecauseConstraintsNotMet,
        disabledBecauseSamplingTargetNotMet: disabledBecauseConstraintsNotMet,
        disabledBecauseConstraintsNotMet,
        samplingRateTargetMet: Boolean(selected?.samplingRateTargetMet),
        hitRateTargetMet: Boolean(selected?.hitRateTargetMet),
        primaryRoiTargetMet: Boolean(selected?.primaryRoiTargetMet),
        degradationLevel: selected?.degradationLevel || 'NO_STRATEGY',
        samplingRateFloor: selected?.samplingRateFloor ?? null,
        roiFloor: selected?.roiFloor ?? null,
        hitRateFloor: selected?.hitRateFloor ?? null,
        reliabilityTier: selected?.reliabilityTier || null,
        strategy,
        metrics: selected?.metrics || emptyMetrics(),
        directMetrics: direct?.metrics || emptyMetrics(),
        currentEditionFallbackMetrics,
        robustnessMetrics: selected?.robustnessMetrics || emptyRobustnessMetrics(),
        overfittingDetected: Boolean(selected?.overfittingAudit?.overfittingDetected),
        overfittingReasons: selected?.overfittingAudit?.reasons || [],
        previousStrategy: direct?.baselineStrategy || null,
        previousStrategyMetrics: direct?.baselineMetrics || emptyMetrics(),
        previousStrategyRobustnessMetrics: direct?.baselineRobustnessMetrics || emptyRobustnessMetrics(),
        previousStrategyOverfittingDetected: Boolean(direct?.baselineOverfittingAudit?.overfittingDetected),
        previousStrategyOverfittingReasons: direct?.baselineOverfittingAudit?.reasons || [],
        minimumRecommendedMatchConstraint: selected?.minimumRecommendedMatchConstraint || 0,
        minimumSelectionConstraint: selected?.minimumSelectionConstraint || 0,
        minimumWinningSelectionConstraint: selected?.minimumWinningSelectionConstraint || 0
      })
    }
  }
  if (!EVALUATE_ONLY && TARGET_RANGES.includes('CURRENT')) {
    applySmallCurrentSampleFallback(reportRows, strategies)
  }
  reportRows.sort((left, right) => {
    const competitionCompare = COMPETITIONS.indexOf(left.competition) - COMPETITIONS.indexOf(right.competition)
    if (competitionCompare !== 0) {
      return competitionCompare
    }
    return left.range === 'CURRENT' ? -1 : 1
  })

  if (!EVALUATE_ONLY && !DRY_RUN) {
    await fs.writeFile(
      CONFIG_PATH,
      replaceConfigObject(configText, 'totalGoalsStrategies', strategies),
      'utf8'
    )
  }
  const report = {
    generatedAt: new Date().toISOString(),
    source: '中国体彩网总进球数固定赔率初始值',
    modelMode: 'after',
    simulations: SIMULATIONS,
    stakeMethod: '每个推荐项作为独立单关等额投注1单位',
    roiFormula: 'ROI = (命中单关赔率返奖合计 - 推荐总注数) / 推荐总注数',
    hitRateFormula: '命中率 = 命中单关注数 / 推荐总注数',
    optimizationObjective: EVALUATE_ONLY
      ? '使用当前已保存的进球数策略重新计算全部指标，不执行参数搜索'
      : `${optimizationObjectiveDescription()}${DRY_RUN ? '；本次为试运行，不写入策略配置' : ''}`,
    constraints: {
      strictGreaterThan: STRICT_CONSTRAINTS,
      minimumSamplingRate: MINIMUM_SAMPLING_RATE,
      minimumRecommendedMatches: minimumRecommendedMatchesDescription(),
      minimumRoi: MINIMUM_ROI,
      minimumHitRate: MINIMUM_HIT_RATE,
      fallbackMinimumSamplingRate: FALLBACK_MINIMUM_SAMPLING_RATE,
      fallbackMinimumHitRate: FALLBACK_MINIMUM_HIT_RATE,
      secondaryFallbackMinimumSamplingRate: SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE,
      secondaryFallbackMinimumHitRate: SECONDARY_FALLBACK_MINIMUM_HIT_RATE,
      tertiaryFallbackMinimumSamplingRate: TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE,
      tertiaryFallbackMinimumHitRate: TERTIARY_FALLBACK_MINIMUM_HIT_RATE,
      fallbackRule: '按主档、25%降级档、20%二级降级档、最低降级档顺序搜索，仅在上一档无可行解时进入下一档',
      robustValidation: {
        enabled: ROBUST_VALIDATION,
        split: `按比赛日期升序，前${formatPercent(1 - VALIDATION_FRACTION)}为训练集，后${formatPercent(VALIDATION_FRACTION)}为验证集`,
        minimumTrainingMatches: MINIMUM_TRAINING_MATCHES,
        minimumValidationMatches: MINIMUM_VALIDATION_MATCHES,
        minimumValidationRoi: MINIMUM_VALIDATION_ROI,
        rule: '候选参数仅在训练集搜索和细化，最终策略必须同时满足训练集、验证集和完整样本的同档采样率与命中率约束，且验证集ROI不得低于下限'
      },
      maximumSelectionsPerMatch: '0-4',
      strategySearch: ROBUST_VALIDATION
        ? `仅使用训练集粗网格搜索，取目标最优的${TOP_CANDIDATE_LIMIT}个达标候选并执行最多3轮坐标细化，再通过时间留出验证集过滤过拟合候选`
        : `先粗网格搜索，取目标最优的${TOP_CANDIDATE_LIMIT}个达标候选，再基于实际概率、期望值和赔率分布执行最多3轮坐标细化`,
      smallCurrentSampleRule: OPTIMIZE_SMALL_CURRENT_SAMPLES
        ? '仅本届存在样本时直接优化；无样本时沿用含上届策略'
        : `仅本届可用样本数不超过${smallCurrentSampleLimit()}场时跳过独立优化，直接沿用含上届策略`,
      selectionPreference: optimizationSelectionPreference()
    },
    strategies: reportRows
  }
  await fs.writeFile(REPORT_PATH, serializeJson(report), 'utf8')
  if (!EVALUATE_ONLY && !DRY_RUN) {
    process.stdout.write(`策略已写入 ${CONFIG_PATH}\n`)
  }
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
  const cacheSignature = {
    simulations: SIMULATIONS,
    competitions: TARGET_COMPETITIONS,
    range,
    includePreviousEdition,
    modelFactorsByCompetition
  }
  const cachedResult = await readBacktestCache(range, cacheSignature)
  if (cachedResult) {
    process.stdout.write(`  复用${range === 'CURRENT' ? '仅本届' : '含上届'}回测缓存，共 ${cachedResult.matches?.length || 0} 场\n`)
    return cachedResult
  }
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
  await writeBacktestCache(range, cacheSignature, job.result)
  return job.result
}

async function readBacktestCache(range, signature) {
  const cachePath = backtestCachePath(range)
  if (!cachePath) {
    return null
  }
  try {
    const cached = JSON.parse(await fs.readFile(cachePath, 'utf8'))
    return JSON.stringify(cached.signature) === JSON.stringify(signature)
      ? cached.result
      : null
  } catch {
    return null
  }
}

async function writeBacktestCache(range, signature, result) {
  const cachePath = backtestCachePath(range)
  if (!cachePath) {
    return
  }
  await fs.mkdir(path.dirname(cachePath), { recursive: true })
  await fs.writeFile(cachePath, serializeJson({ signature, result }), 'utf8')
  process.stdout.write(`  回测缓存已写入 ${cachePath}\n`)
}

function backtestCachePath(range) {
  return BACKTEST_CACHE_PREFIX
    ? path.resolve(ROOT, `${BACKTEST_CACHE_PREFIX}-${range.toLowerCase()}.json`)
    : null
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
    const matches = matchesByCompetition[competition].sort(compareMatchesChronologically)
    const skipSmallCurrentSample = range === 'CURRENT' && shouldFallbackToPreviousEdition(matches.length)
    const baselineStrategy = existingStrategies[`${competition}:${range}`] || DEFAULT_STRATEGY
    const optimized = EVALUATE_ONLY
      ? evaluateConfiguredStrategy(matches, baselineStrategy)
      : skipSmallCurrentSample
      ? {
          strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
          metrics: emptyMetrics(matches.length),
          minimumRecommendedMatchConstraint: 0,
          minimumSelectionConstraint: 0,
          minimumWinningSelectionConstraint: 0,
          strategyAvailable: false,
          samplingRateTargetMet: false,
          hitRateTargetMet: false,
          primaryRoiTargetMet: false,
          degradationLevel: 'SMALL_CURRENT_SAMPLE_SKIPPED'
        }
      : optimizeCompetition(matches, baselineStrategy)
    optimized.baselineStrategy = { ...baselineStrategy }
    optimized.baselineMetrics = !EVALUATE_ONLY && skipSmallCurrentSample
      ? emptyMetrics(matches.length)
      : evaluateStrategy(matches, baselineStrategy)
    optimized.baselineOverfittingAudit = auditStrategyOverfitting(matches, baselineStrategy)
    optimized.baselineRobustnessMetrics = optimized.baselineOverfittingAudit.robustnessMetrics
    optimized.overfittingAudit = auditStrategyOverfitting(
      matches,
      optimized.strategy,
      optimized.samplingRateFloor,
      optimized.hitRateFloor
    )
    optimized.robustnessMetrics = optimized.overfittingAudit.robustnessMetrics
    optimized.evaluationMatches = matches
    process.stdout.write(
      `  ${COMPETITION_NAMES[competition]} ${range}: ${matches.length} 场，` +
      `${optimized.metrics.recommendedSelectionCount} 注，` +
      `命中率 ${formatPercent(optimized.metrics.hitRate)}，ROI ${formatPercent(optimized.metrics.roi)}` +
      `${optimized.overfittingAudit.overfittingDetected ? '（样本外检查未通过）' : ''}` +
      `${!EVALUATE_ONLY && skipSmallCurrentSample ? '（小样本跳过独立优化）' : ''}\n`
    )
    return [competition, optimized]
  }))
}

function evaluateConfiguredStrategy(matches, strategy) {
  const metrics = evaluateStrategy(matches, strategy)
  return {
    strategy: { ...strategy },
    metrics,
    minimumRecommendedMatchConstraint: 0,
    minimumSelectionConstraint: 0,
    minimumWinningSelectionConstraint: 0,
    strategyAvailable: Number(strategy.maximumSelections) > 0 && metrics.recommendedSelectionCount > 0,
    samplingRateTargetMet: meetsRateConstraint(metrics.samplingRate, MINIMUM_SAMPLING_RATE),
    hitRateTargetMet: meetsRateConstraint(metrics.hitRate, MINIMUM_HIT_RATE),
    primaryRoiTargetMet: metrics.roi !== null && metrics.roi >= PRIMARY_ROI_TARGET,
    reliabilityTier: 'CONFIGURED_STRATEGY_EVALUATION',
    samplingRateFloor: null,
    hitRateFloor: null,
    roiFloor: null,
    degradationLevel: 'CONFIGURED_STRATEGY_EVALUATION'
  }
}

async function readExistingReport() {
  try {
    return JSON.parse(await fs.readFile(REPORT_PATH, 'utf8'))
  } catch {
    throw new Error('定向优化需要已有的完整回测报告')
  }
}

async function readBaselineStrategies(configuredStrategies) {
  if (!BASELINE_REPORT_PATH) {
    return configuredStrategies
  }
  const baselinePath = path.resolve(ROOT, BASELINE_REPORT_PATH)
  const report = JSON.parse(await fs.readFile(baselinePath, 'utf8'))
  const reportStrategies = Object.fromEntries((report.strategies || []).map(row => [row.key, row.strategy]))
  process.stdout.write(`基准策略读取自 ${baselinePath}\n`)
  return {
    ...configuredStrategies,
    ...reportStrategies
  }
}

function applySmallCurrentSampleFallback(reportRows, strategies) {
  for (const competition of TARGET_COMPETITIONS) {
    const current = reportRows.find(row => row.competition === competition && row.range === 'CURRENT')
    const previous = reportRows.find(row => row.competition === competition && row.range === 'PREVIOUS')
    if (!current || !previous || previous.disabledBecauseRoiTargetNotMet) {
      continue
    }
    const directMetrics = current.directMetrics || emptyMetrics()
    if (!shouldFallbackToPreviousEdition(directMetrics.availableMatchCount)) {
      continue
    }
    const fallbackMetrics = current.currentEditionFallbackMetrics || emptyMetrics()
    if (
      fallbackMetrics.availableMatchCount > 0 &&
      (
        !meetsDualRateConstraint(
          fallbackMetrics,
          previous.samplingRateFloor,
          previous.hitRateFloor
        ) ||
        !meetsRoiConstraint(fallbackMetrics.roi)
      )
    ) {
      current.fallbackToPreviousEdition = false
      current.disabledBecauseRoiTargetNotMet = true
      current.disabledBecauseSamplingTargetNotMet = true
      current.disabledBecauseConstraintsNotMet = true
      current.strategy = { ...previous.strategy, maximumSelections: 0 }
      current.metrics = emptyMetrics(fallbackMetrics.availableMatchCount)
      current.minimumRecommendedMatchConstraint = 0
      current.minimumSelectionConstraint = 0
      current.minimumWinningSelectionConstraint = 0
      current.samplingRateTargetMet = false
      current.hitRateTargetMet = false
      current.primaryRoiTargetMet = false
      current.degradationLevel = 'CURRENT_EDITION_FALLBACK_VALIDATION_NOT_MET'
      current.samplingRateFloor = previous.samplingRateFloor
      current.roiFloor = MINIMUM_ROI
      current.hitRateFloor = previous.hitRateFloor
      current.reliabilityTier = null
      current.overfittingDetected = true
      current.overfittingReasons = [
        `少样本本届回测未同时达到${constraintLabel(previous.samplingRateFloor)}采样率、` +
        `${constraintLabel(previous.hitRateFloor)}命中率和${roiConstraintLabel()}ROI`
      ]
      strategies[`${competition}:CURRENT`] = current.strategy
      continue
    }
    current.fallbackToPreviousEdition = true
    current.disabledBecauseRoiTargetNotMet = false
    current.disabledBecauseSamplingTargetNotMet = false
    current.disabledBecauseConstraintsNotMet = false
    current.strategy = { ...previous.strategy }
    current.metrics = { ...previous.metrics }
    current.minimumRecommendedMatchConstraint = previous.minimumRecommendedMatchConstraint
    current.minimumSelectionConstraint = previous.minimumSelectionConstraint
    current.minimumWinningSelectionConstraint = previous.minimumWinningSelectionConstraint
    current.samplingRateTargetMet = previous.samplingRateTargetMet
    current.hitRateTargetMet = previous.hitRateTargetMet
    current.primaryRoiTargetMet = previous.primaryRoiTargetMet
    current.degradationLevel = previous.degradationLevel
    current.samplingRateFloor = previous.samplingRateFloor
    current.roiFloor = previous.roiFloor
    current.hitRateFloor = previous.hitRateFloor
    current.reliabilityTier = previous.reliabilityTier
    current.robustnessMetrics = previous.robustnessMetrics
    current.overfittingDetected = previous.overfittingDetected
    current.overfittingReasons = previous.overfittingReasons
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
  return items.length > 0
    ? {
        matchId: String(match.matchId || match.sportteryMatchId || ''),
        matchDate: String(match.matchDate || ''),
        kickoffTime: String(match.kickoffTime || ''),
        items
      }
    : null
}

function compareMatchesChronologically(left, right) {
  return left.matchDate.localeCompare(right.matchDate) ||
    left.kickoffTime.localeCompare(right.kickoffTime) ||
    left.matchId.localeCompare(right.matchId)
}

function createChronologicalValidationSplit(matches) {
  if (matches.length < MINIMUM_TRAINING_MATCHES + MINIMUM_VALIDATION_MATCHES) {
    return {
      available: false,
      trainingMatches: matches,
      validationMatches: []
    }
  }
  const requestedValidationCount = Math.max(
    MINIMUM_VALIDATION_MATCHES,
    Math.ceil(matches.length * VALIDATION_FRACTION)
  )
  let splitIndex = Math.max(
    MINIMUM_TRAINING_MATCHES,
    matches.length - requestedValidationCount
  )
  splitIndex = Math.min(splitIndex, matches.length - MINIMUM_VALIDATION_MATCHES)
  const boundaryDate = matches[splitIndex]?.matchDate
  if (boundaryDate) {
    let sameDateStart = splitIndex
    while (sameDateStart > MINIMUM_TRAINING_MATCHES && matches[sameDateStart - 1]?.matchDate === boundaryDate) {
      sameDateStart -= 1
    }
    if (matches.length - sameDateStart >= MINIMUM_VALIDATION_MATCHES) {
      splitIndex = sameDateStart
    }
  }
  return {
    available: true,
    trainingMatches: matches.slice(0, splitIndex),
    validationMatches: matches.slice(splitIndex)
  }
}

function auditStrategyOverfitting(
  matches,
  strategy,
  configuredSamplingRate = null,
  configuredHitRate = null
) {
  const fullMetrics = evaluateStrategy(matches, strategy)
  if (!ROBUST_VALIDATION) {
    return {
      overfittingDetected: false,
      reasons: [],
      robustnessMetrics: {
        ...emptyRobustnessMetrics(),
        fullMetrics
      }
    }
  }
  const split = createChronologicalValidationSplit(matches)
  if (!split.available || Number(strategy?.maximumSelections) <= 0) {
    return {
      overfittingDetected: false,
      reasons: [],
      robustnessMetrics: {
        ...emptyRobustnessMetrics(),
        validationAvailable: false,
        fullMetrics,
        trainingMetrics: evaluateStrategy(split.trainingMatches, strategy),
        trainingMatchCount: split.trainingMatches.length
      }
    }
  }
  const trainingMetrics = evaluateStrategy(split.trainingMatches, strategy)
  const validationMetrics = evaluateStrategy(split.validationMatches, strategy)
  const fullMeetsPrimary = meetsDualRateConstraint(
    fullMetrics,
    MINIMUM_SAMPLING_RATE,
    MINIMUM_HIT_RATE
  )
  const fullMeetsFirstFallback = meetsDualRateConstraint(
    fullMetrics,
    FALLBACK_MINIMUM_SAMPLING_RATE,
    FALLBACK_MINIMUM_HIT_RATE
  )
  const fullMeetsSecondFallback = meetsDualRateConstraint(
    fullMetrics,
    SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE,
    SECONDARY_FALLBACK_MINIMUM_HIT_RATE
  )
  const targetSamplingRate = Number.isFinite(configuredSamplingRate)
    ? configuredSamplingRate
    : fullMeetsPrimary
    ? MINIMUM_SAMPLING_RATE
    : fullMeetsFirstFallback
    ? FALLBACK_MINIMUM_SAMPLING_RATE
    : fullMeetsSecondFallback
    ? SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE
    : TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE
  const targetHitRate = Number.isFinite(configuredHitRate)
    ? configuredHitRate
    : fullMeetsPrimary
    ? MINIMUM_HIT_RATE
    : fullMeetsFirstFallback
    ? FALLBACK_MINIMUM_HIT_RATE
    : fullMeetsSecondFallback
    ? SECONDARY_FALLBACK_MINIMUM_HIT_RATE
    : TERTIARY_FALLBACK_MINIMUM_HIT_RATE
  const reasons = []
  if (!meetsRateConstraint(validationMetrics.samplingRate, targetSamplingRate)) {
    reasons.push(`验证集采样率${formatPercent(validationMetrics.samplingRate)}未达到${constraintLabel(targetSamplingRate)}`)
  }
  if (!meetsRateConstraint(validationMetrics.hitRate, targetHitRate)) {
    reasons.push(`验证集命中率${formatPercent(validationMetrics.hitRate)}未达到${constraintLabel(targetHitRate)}`)
  }
  if (!Number.isFinite(validationMetrics.roi) || validationMetrics.roi < MINIMUM_VALIDATION_ROI) {
    reasons.push(`验证集ROI${formatPercent(validationMetrics.roi)}低于${formatPercent(MINIMUM_VALIDATION_ROI)}`)
  }
  if (!meetsRoiConstraint(fullMetrics.roi)) {
    reasons.push(`完整样本ROI${formatPercent(fullMetrics.roi)}未达到${roiConstraintLabel()}`)
  }
  return {
    overfittingDetected: reasons.length > 0,
    reasons,
    robustnessMetrics: {
      validationAvailable: true,
      trainingMatchCount: split.trainingMatches.length,
      validationMatchCount: split.validationMatches.length,
      targetSamplingRate,
      targetHitRate,
      minimumValidationRoi: MINIMUM_VALIDATION_ROI,
      fullMetrics,
      trainingMetrics,
      validationMetrics
    }
  }
}

function optimizeCompetitionWithRobustValidation(matches, baselineStrategy) {
  if (matches.length === 0) {
    return unavailableStrategy('NO_DATA', 0)
  }
  const split = createChronologicalValidationSplit(matches)
  if (!split.available) {
    return unavailableStrategy('INSUFFICIENT_ROBUST_VALIDATION_SAMPLE', matches.length)
  }
  const candidates = enumerateStrategies(split.trainingMatches)
  candidates.push({
    strategy: { ...baselineStrategy },
    metrics: evaluateStrategy(split.trainingMatches, baselineStrategy)
  })
  const primary = optimizeCompetitionForRobustConstraints(
    matches,
    split,
    candidates,
    baselineStrategy,
    MINIMUM_SAMPLING_RATE,
    MINIMUM_HIT_RATE
  )
  if (primary) {
    return primary
  }
  const fallback = optimizeCompetitionForRobustConstraints(
    matches,
    split,
    candidates,
    baselineStrategy,
    FALLBACK_MINIMUM_SAMPLING_RATE,
    FALLBACK_MINIMUM_HIT_RATE
  )
  if (fallback) {
    return fallback
  }
  const secondaryFallback = optimizeCompetitionForRobustConstraints(
    matches,
    split,
    candidates,
    baselineStrategy,
    SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE,
    SECONDARY_FALLBACK_MINIMUM_HIT_RATE
  )
  if (secondaryFallback) {
    return secondaryFallback
  }
  const tertiaryFallback = optimizeCompetitionForRobustConstraints(
    matches,
    split,
    candidates,
    baselineStrategy,
    TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE,
    TERTIARY_FALLBACK_MINIMUM_HIT_RATE
  )
  if (tertiaryFallback) {
    return tertiaryFallback
  }
  return unavailableStrategy('ROBUST_VALIDATION_TARGET_NOT_MET', matches.length)
}

function optimizeCompetitionForRobustConstraints(
  matches,
  split,
  candidates,
  baselineStrategy,
  minimumSamplingRate,
  minimumHitRate
) {
  const minimumTrainingMatches = calculateMinimumCount(split.trainingMatches.length, minimumSamplingRate)
  const coarseCandidates = selectTopRobustStrategies(
    candidates,
    TOP_CANDIDATE_LIMIT,
    minimumSamplingRate,
    minimumHitRate
  )
  const candidatePool = []
  for (const coarseCandidate of coarseCandidates) {
    candidatePool.push(coarseCandidate)
    candidatePool.push(refineStrategy(
      split.trainingMatches,
      coarseCandidate,
      minimumTrainingMatches,
      minimumSamplingRate,
      minimumHitRate
    ))
  }
  candidatePool.push({
    strategy: { ...baselineStrategy },
    trainingMetrics: evaluateStrategy(split.trainingMatches, baselineStrategy)
  })
  let best = null
  const seenStrategies = new Set()
  for (const candidate of candidatePool) {
    const strategyKey = JSON.stringify(candidate.strategy)
    if (seenStrategies.has(strategyKey)) {
      continue
    }
    seenStrategies.add(strategyKey)
    const trainingMetrics = candidate.trainingMetrics ||
      evaluateStrategy(split.trainingMatches, candidate.strategy)
    const validationMetrics = evaluateStrategy(split.validationMatches, candidate.strategy)
    const fullMetrics = combinePartitionMetrics(trainingMetrics, validationMetrics)
    if (!meetsRobustOptimizationConstraints(
      trainingMetrics,
      validationMetrics,
      fullMetrics,
      minimumSamplingRate,
      minimumHitRate
    )) {
      continue
    }
    const evaluated = {
      strategy: candidate.strategy,
      metrics: fullMetrics,
      trainingMetrics,
      validationMetrics
    }
    if (isPreferredRobustCandidate(evaluated, best)) {
      best = evaluated
    }
  }
  if (!best) {
    return null
  }
  const minimumRecommendedMatches = calculateMinimumCount(matches.length, minimumSamplingRate)
  return {
    strategy: best.strategy,
    metrics: best.metrics,
    minimumRecommendedMatchConstraint: minimumRecommendedMatches,
    minimumSelectionConstraint: minimumRecommendedMatches,
    minimumWinningSelectionConstraint: calculateMinimumCount(
      best.metrics.recommendedSelectionCount,
      minimumHitRate
    ),
    reliabilityTier: `${samplingRateTier(minimumSamplingRate, minimumHitRate)}_ROBUST_VALIDATED`,
    samplingRateFloor: minimumSamplingRate,
    hitRateFloor: minimumHitRate,
    roiFloor: MINIMUM_ROI,
    strategyAvailable: true,
    samplingRateTargetMet: true,
    hitRateTargetMet: true,
    primaryRoiTargetMet: best.metrics.roi > PRIMARY_ROI_TARGET,
    degradationLevel: `${samplingRateTier(minimumSamplingRate, minimumHitRate)}_ROBUST_VALIDATED`
  }
}

function selectTopRobustStrategies(
  candidates,
  limit,
  minimumSamplingRate,
  minimumHitRate
) {
  const best = []
  for (const candidate of candidates) {
    const trainingMetrics = candidate.metrics
    if (
      !meetsDualRateConstraint(trainingMetrics, minimumSamplingRate, minimumHitRate) ||
      !meetsRoiConstraint(trainingMetrics.roi)
    ) {
      continue
    }
    const evaluated = {
      strategy: candidate.strategy,
      metrics: trainingMetrics,
      trainingMetrics
    }
    const insertionIndex = best.findIndex(existing => (
      isPreferredMetrics(evaluated.trainingMetrics, existing.trainingMetrics)
    ))
    if (insertionIndex >= 0) {
      best.splice(insertionIndex, 0, evaluated)
    } else if (best.length < limit) {
      best.push(evaluated)
    }
    if (best.length > limit) {
      best.pop()
    }
  }
  return best
}

function combinePartitionMetrics(left, right) {
  const availableMatchCount = left.availableMatchCount + right.availableMatchCount
  const recommendedMatchCount = left.recommendedMatchCount + right.recommendedMatchCount
  const recommendedSelectionCount = left.recommendedSelectionCount + right.recommendedSelectionCount
  const winningSelectionCount = left.winningSelectionCount + right.winningSelectionCount
  const totalReturn = round(left.totalReturn + right.totalReturn)
  const totalStake = recommendedSelectionCount
  return {
    availableMatchCount,
    recommendedMatchCount,
    recommendedSelectionCount,
    winningSelectionCount,
    hitRate: totalStake > 0 ? winningSelectionCount / totalStake : null,
    matchHitRate: recommendedMatchCount > 0 ? winningSelectionCount / recommendedMatchCount : null,
    samplingRate: availableMatchCount > 0 ? recommendedMatchCount / availableMatchCount : null,
    totalStake,
    totalReturn,
    netProfit: round(totalReturn - totalStake),
    roi: totalStake > 0 ? round(totalReturn / totalStake - 1, 6) : null
  }
}

function meetsRobustOptimizationConstraints(
  trainingMetrics,
  validationMetrics,
  fullMetrics,
  minimumSamplingRate,
  minimumHitRate
) {
  const partitions = [trainingMetrics, validationMetrics, fullMetrics]
  if (!partitions.every(metrics => meetsDualRateConstraint(
    metrics,
    minimumSamplingRate,
    minimumHitRate
  ))) {
    return false
  }
  if (!Number.isFinite(validationMetrics.roi) || validationMetrics.roi < MINIMUM_VALIDATION_ROI) {
    return false
  }
  return meetsRoiConstraint(trainingMetrics.roi) && meetsRoiConstraint(fullMetrics.roi)
}

function meetsDualRateConstraint(metrics, minimumSamplingRate, minimumHitRate) {
  return meetsRateConstraint(metrics.samplingRate, minimumSamplingRate) &&
    meetsRateConstraint(metrics.hitRate, minimumHitRate)
}

function isPreferredRobustCandidate(candidate, current) {
  if (!current) {
    return true
  }
  if (isPreferredMetrics(candidate.trainingMetrics, current.trainingMetrics)) {
    return true
  }
  if (isPreferredMetrics(current.trainingMetrics, candidate.trainingMetrics)) {
    return false
  }
  return JSON.stringify(candidate.strategy).localeCompare(JSON.stringify(current.strategy)) < 0
}

function unavailableStrategy(degradationLevel, availableMatchCount) {
  return {
    strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
    metrics: emptyMetrics(availableMatchCount),
    minimumRecommendedMatchConstraint: 0,
    minimumSelectionConstraint: 0,
    minimumWinningSelectionConstraint: 0,
    strategyAvailable: false,
    samplingRateFloor: null,
    samplingRateTargetMet: false,
    hitRateTargetMet: false,
    primaryRoiTargetMet: false,
    degradationLevel
  }
}

function optimizeCompetition(matches, baselineStrategy) {
  if (ROBUST_VALIDATION) {
    return optimizeCompetitionWithRobustValidation(matches, baselineStrategy)
  }
  return optimizeCompetitionInSample(matches, baselineStrategy)
}

function optimizeCompetitionInSample(matches, baselineStrategy) {
  if (matches.length === 0) {
    return {
      strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
      metrics: emptyMetrics(),
      minimumRecommendedMatchConstraint: 0,
      minimumSelectionConstraint: 0,
      minimumWinningSelectionConstraint: 0,
      strategyAvailable: false,
      samplingRateTargetMet: false,
      hitRateTargetMet: false,
      primaryRoiTargetMet: false,
      degradationLevel: 'NO_DATA'
    }
  }
  const candidates = enumerateStrategies(matches)
  candidates.push({
    strategy: { ...baselineStrategy },
    metrics: evaluateStrategy(matches, baselineStrategy)
  })
  const primary = optimizeCompetitionForConstraints(
    matches,
    candidates,
    MINIMUM_SAMPLING_RATE,
    MINIMUM_HIT_RATE
  )
  if (primary) {
    return primary
  }
  const fallback = optimizeCompetitionForConstraints(
    matches,
    candidates,
    FALLBACK_MINIMUM_SAMPLING_RATE,
    FALLBACK_MINIMUM_HIT_RATE
  )
  if (fallback) {
    return fallback
  }
  const secondaryFallback = optimizeCompetitionForConstraints(
    matches,
    candidates,
    SECONDARY_FALLBACK_MINIMUM_SAMPLING_RATE,
    SECONDARY_FALLBACK_MINIMUM_HIT_RATE
  )
  if (secondaryFallback) {
    return secondaryFallback
  }
  const tertiaryFallback = optimizeCompetitionForConstraints(
    matches,
    candidates,
    TERTIARY_FALLBACK_MINIMUM_SAMPLING_RATE,
    TERTIARY_FALLBACK_MINIMUM_HIT_RATE
  )
  if (tertiaryFallback) {
    return tertiaryFallback
  }
  return {
    strategy: { ...DEFAULT_STRATEGY, maximumSelections: 0 },
    metrics: emptyMetrics(matches.length),
    minimumRecommendedMatchConstraint: 0,
    minimumSelectionConstraint: 0,
    minimumWinningSelectionConstraint: 0,
    strategyAvailable: false,
    samplingRateTargetMet: false,
    hitRateTargetMet: false,
    primaryRoiTargetMet: false,
    degradationLevel: 'SAMPLING_OR_HIT_RATE_TARGET_NOT_MET'
  }
}

function optimizeCompetitionForConstraints(matches, candidates, minimumSamplingRate, minimumHitRate) {
  const minimumRecommendedMatches = calculateMinimumCount(matches.length, minimumSamplingRate)
  const coarseCandidates = selectTopStrategies(
    candidates,
    minimumRecommendedMatches,
    TOP_CANDIDATE_LIMIT,
    minimumSamplingRate,
    minimumHitRate
  )
  if (coarseCandidates.length > 0) {
    let best = null
    for (const coarseCandidate of coarseCandidates) {
      const refined = refineStrategy(
        matches,
        coarseCandidate,
        minimumRecommendedMatches,
        minimumSamplingRate,
        minimumHitRate
      )
      if (
        meetsOptimizationConstraints(
          refined.metrics,
          minimumRecommendedMatches,
          minimumSamplingRate,
          minimumHitRate
        ) &&
        isPreferredMetrics(refined.metrics, best?.metrics)
      ) {
        best = refined
      }
    }
    if (best) {
      return {
        ...best,
        minimumRecommendedMatchConstraint: minimumRecommendedMatches,
        minimumSelectionConstraint: minimumRecommendedMatches,
        minimumWinningSelectionConstraint: calculateMinimumCount(best.metrics.recommendedSelectionCount, minimumHitRate),
        reliabilityTier: samplingRateTier(minimumSamplingRate, minimumHitRate),
        samplingRateFloor: minimumSamplingRate,
        hitRateFloor: minimumHitRate,
        roiFloor: MINIMUM_ROI,
        strategyAvailable: true,
        samplingRateTargetMet: true,
        hitRateTargetMet: true,
        primaryRoiTargetMet: best.metrics.roi > PRIMARY_ROI_TARGET,
        degradationLevel: samplingRateTier(minimumSamplingRate, minimumHitRate)
      }
    }
  }
  return null
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

function selectTopStrategies(
  candidates,
  minimumRecommendedMatches,
  limit,
  minimumSamplingRate,
  minimumHitRate
) {
  const best = []
  for (const candidate of candidates) {
    const metrics = candidate.metrics
    const insertionIndex = best.findIndex(existing => (
      isPreferredCandidateMetrics(
        metrics,
        existing.metrics,
        minimumRecommendedMatches,
        minimumSamplingRate,
        minimumHitRate
      )
    ))
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

function refineStrategy(
  matches,
  initialBest,
  minimumRecommendedMatches,
  minimumSamplingRate,
  minimumHitRate
) {
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
        if (isPreferredCandidateMetrics(
          metrics,
          best.metrics,
          minimumRecommendedMatches,
          minimumSamplingRate,
          minimumHitRate
        )) {
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

function meetsOptimizationConstraints(
  metrics,
  minimumRecommendedMatches,
  minimumSamplingRate,
  minimumHitRate
) {
  if (metrics.recommendedMatchCount < minimumRecommendedMatches) {
    return false
  }
  if (!meetsRateConstraint(metrics.samplingRate, minimumSamplingRate)) {
    return false
  }
  if (!meetsRateConstraint(metrics.hitRate, minimumHitRate)) {
    return false
  }
  return meetsRoiConstraint(metrics.roi)
}

function isPreferredCandidateMetrics(
  candidate,
  current,
  minimumRecommendedMatches,
  minimumSamplingRate,
  minimumHitRate
) {
  if (!current) {
    return true
  }
  const candidateFeasible = meetsOptimizationConstraints(
    candidate,
    minimumRecommendedMatches,
    minimumSamplingRate,
    minimumHitRate
  )
  const currentFeasible = meetsOptimizationConstraints(
    current,
    minimumRecommendedMatches,
    minimumSamplingRate,
    minimumHitRate
  )
  if (candidateFeasible !== currentFeasible) {
    return candidateFeasible
  }
  if (candidateFeasible) {
    return isPreferredMetrics(candidate, current)
  }
  const candidateViolation = constraintViolationScore(candidate, minimumSamplingRate, minimumHitRate)
  const currentViolation = constraintViolationScore(current, minimumSamplingRate, minimumHitRate)
  if (candidateViolation !== currentViolation) {
    return candidateViolation < currentViolation
  }
  if ((candidate.hitRate ?? -1) !== (current.hitRate ?? -1)) {
    return (candidate.hitRate ?? -1) > (current.hitRate ?? -1)
  }
  if ((candidate.samplingRate ?? -1) !== (current.samplingRate ?? -1)) {
    return (candidate.samplingRate ?? -1) > (current.samplingRate ?? -1)
  }
  return (candidate.roi ?? Number.NEGATIVE_INFINITY) > (current.roi ?? Number.NEGATIVE_INFINITY)
}

function constraintViolationScore(metrics, minimumSamplingRate, minimumHitRate) {
  const strictOffset = STRICT_CONSTRAINTS ? 1e-12 : 0
  const samplingRate = Number.isFinite(metrics.samplingRate) ? metrics.samplingRate : 0
  const hitRate = Number.isFinite(metrics.hitRate) ? metrics.hitRate : 0
  const samplingDeficit = Math.max(0, minimumSamplingRate + strictOffset - samplingRate)
  const hitRateDeficit = Math.max(0, minimumHitRate + strictOffset - hitRate)
  return samplingDeficit + hitRateDeficit
}

function isPreferredMetrics(candidate, current) {
  if (!current) {
    return true
  }
  if (OPTIMIZATION_OBJECTIVE === 'HIT_RATE' && candidate.hitRate !== current.hitRate) {
    return candidate.hitRate > current.hitRate
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

function optimizationObjectiveDescription() {
  const comparison = STRICT_CONSTRAINTS ? '严格大于' : '大于等于'
  if (OPTIMIZATION_OBJECTIVE === 'HIT_RATE') {
    return `所有有进球数赔率样本的策略采样率必须${comparison}${formatPercent(MINIMUM_SAMPLING_RATE)}、命中率必须${comparison}${formatPercent(MINIMUM_HIT_RATE)}、ROI必须${comparison}${formatPercent(MINIMUM_ROI)}，在达标候选中优先最大化单注命中率，再比较ROI和采样率`
  }
  return `所有有进球数赔率样本的策略采样率必须${comparison}${formatPercent(MINIMUM_SAMPLING_RATE)}、命中率必须${comparison}${formatPercent(MINIMUM_HIT_RATE)}、ROI必须${comparison}${formatPercent(MINIMUM_ROI)}，在达标候选中优先最大化ROI，再比较单注命中率和采样率`
}

function optimizationSelectionPreference() {
  return OPTIMIZATION_OBJECTIVE === 'HIT_RATE'
    ? '采样率、命中率和ROI达标后先比较单注命中率，再比较ROI、采样率、每场命中率和有效注数'
    : '采样率、命中率和ROI达标后先比较ROI，再比较单注命中率、采样率、每场命中率和有效注数'
}

function meetsRoiConstraint(value) {
  return meetsMinimumConstraint(value, MINIMUM_ROI, STRICT_CONSTRAINTS)
}

function roiConstraintLabel() {
  return `${STRICT_CONSTRAINTS ? '严格大于' : '大于等于'}${formatPercent(MINIMUM_ROI)}`
}

function samplingRateTier(minimumSamplingRate, minimumHitRate) {
  const samplingRate = String(round(minimumSamplingRate * 100, 3)).replace('.', '_')
  const hitRate = String(round(minimumHitRate * 100, 3)).replace('.', '_')
  return `DUAL_CONSTRAINT_SAMPLING_${samplingRate}_HIT_${hitRate}${STRICT_CONSTRAINTS ? '_STRICT' : ''}`
}

function shouldFallbackToPreviousEdition(availableMatchCount) {
  return availableMatchCount === 0 || (
    !OPTIMIZE_SMALL_CURRENT_SAMPLES && availableMatchCount <= smallCurrentSampleLimit()
  )
}

function smallCurrentSampleLimit() {
  return ROBUST_VALIDATION
    ? Math.max(SMALL_CURRENT_SAMPLE_MAX, MINIMUM_TRAINING_MATCHES + MINIMUM_VALIDATION_MATCHES - 1)
    : SMALL_CURRENT_SAMPLE_MAX
}

function calculateMinimumCount(totalCount, minimumRate) {
  if (STRICT_CONSTRAINTS) {
    return Math.floor(totalCount * minimumRate) + 1
  }
  return Math.ceil(totalCount * minimumRate)
}

function meetsRateConstraint(value, minimumRate) {
  return meetsMinimumConstraint(value, minimumRate, STRICT_CONSTRAINTS)
}

function minimumRecommendedMatchesDescription() {
  return STRICT_CONSTRAINTS
    ? `floor(可用比赛数 * ${MINIMUM_SAMPLING_RATE}) + 1`
    : `ceil(可用比赛数 * ${MINIMUM_SAMPLING_RATE})`
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

function emptyRobustnessMetrics() {
  return {
    validationAvailable: false,
    trainingMatchCount: 0,
    validationMatchCount: 0,
    targetSamplingRate: null,
    targetHitRate: null,
    minimumValidationRoi: MINIMUM_VALIDATION_ROI,
    fullMetrics: emptyMetrics(),
    trainingMetrics: emptyMetrics(),
    validationMetrics: emptyMetrics()
  }
}

function constraintLabel(rate) {
  return `${STRICT_CONSTRAINTS ? '严格大于' : '大于等于'}${formatPercent(rate)}`
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
      `${row.disabledBecauseConstraintsNotMet ? '（无可用样本或无满足采样率、命中率双约束的策略，不投注）' : ''}\n`
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
