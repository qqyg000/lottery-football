import fs from 'node:fs/promises'
import path from 'node:path'

import { evaluateRecommendationBacktest } from '../frontend/src/recommendation-backtest.mjs'

const ROOT = process.cwd()
const API_BASE = process.env.LOTTERY_FOOTBALL_API_BASE || 'http://127.0.0.1:8080'
const CONFIG_PATH = path.join(ROOT, 'config/user-config.json')
const REPORT_JSON_PATH = path.join(ROOT, 'reports/shared-backtest-parameter-optimization-2026-07-26.json')
const REPORT_MARKDOWN_PATH = path.join(ROOT, 'reports/shared-backtest-parameter-optimization-2026-07-26.md')
const CHECKPOINT_PATH = path.join(ROOT, 'target/shared-backtest-optimizer-checkpoint.json')

const FINAL_SIMULATIONS = numberOption('FINAL_SIMULATIONS', 50000)
const COARSE_SIMULATIONS = numberOption('COARSE_SIMULATIONS', 5000)
const MODEL_VARIANT_COUNT = numberOption('MODEL_VARIANT_COUNT', 5, true)
const COARSE_RULE_CANDIDATES = numberOption('COARSE_RULE_CANDIDATES', 1800)
const FINAL_RULE_CANDIDATES = numberOption('FINAL_RULE_CANDIDATES', 9000)
const SAMPLING_TOLERANCE = 0.03
const MINIMUM_STABLE_ROI = 0.05
const MINIMUM_RELAXED_SAMPLING_RATE = 0.20
const ROI_EPSILON = 1e-9
const APPLY_RESULTS = process.argv.includes('--apply')
const RESUME_FROM_CHECKPOINT = process.argv.includes('--resume')

const COMPETITIONS = [
  ['WORLD_CUP', '世界杯'],
  ['EUROPEAN_CHAMPIONSHIP', '欧洲杯'],
  ['COPA_AMERICA', '美洲杯'],
  ['CLUB_WORLD_CUP', '世俱杯'],
  ['EUROPA_LEAGUE', '欧罗巴'],
  ['CHAMPIONS_LEAGUE', '欧冠'],
  ['PREMIER_LEAGUE', '英超'],
  ['LA_LIGA', '西甲'],
  ['BUNDESLIGA', '德甲'],
  ['SERIE_A', '意甲'],
  ['LIGUE_1', '法甲'],
  ['PRIMEIRA_LIGA', '葡超'],
  ['EREDIVISIE', '荷甲'],
  ['ARGENTINE_PRIMERA_DIVISION', '阿甲'],
  ['SWEDISH_ALLSVENSKAN', '瑞超'],
  ['FINNISH_VEIKKAUSLIIGA', '芬超'],
  ['K_LEAGUE_1', '韩职']
]

function numberOption(name, fallback, allowZero = false) {
  const value = Number(process.env[name])
  return Number.isFinite(value) && (value > 0 || (allowZero && value === 0))
    ? Math.floor(value)
    : fallback
}

function clamp(value, minimum, maximum) {
  return Math.max(minimum, Math.min(maximum, value))
}

function round(value, scale = 2) {
  const factor = 10 ** scale
  return Math.round((Number(value) + Number.EPSILON) * factor) / factor
}

function finite(value, fallback) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) ? numberValue : fallback
}

function createRandom(seed) {
  let state = seed >>> 0
  return () => {
    state = (Math.imul(state, 1664525) + 1013904223) >>> 0
    return state / 4294967296
  }
}

function stringSeed(value) {
  let result = 2166136261
  for (const character of value) {
    result ^= character.charCodeAt(0)
    result = Math.imul(result, 16777619)
  }
  return result >>> 0
}

function randomBetween(random, minimum, maximum, scale = 2) {
  return round(minimum + random() * (maximum - minimum), scale)
}

function pick(random, values) {
  return values[Math.min(values.length - 1, Math.floor(random() * values.length))]
}

function normalizeProfile(profile) {
  const model = profile?.modelFactors || {}
  const global = profile?.globalParameters || {}
  return {
    modelFactors: {
      hostTeamGoalFactor: round(clamp(finite(model.hostTeamGoalFactor, 1.1), 0.1, 3), 2),
      homeTeamGoalFactor: round(clamp(finite(model.homeTeamGoalFactor, 1.06), 0.1, 3), 2),
      seedTeamGoalFactor: round(clamp(finite(model.seedTeamGoalFactor, 1.85), 0.1, 3), 2),
      officialMatchWeight: round(clamp(finite(model.officialMatchWeight, 1), 1, 3), 2),
      internationalFriendlyWeight: round(clamp(finite(model.internationalFriendlyWeight, 0.5), 0, 1), 2),
      clubFriendlyWeight: round(clamp(finite(model.clubFriendlyWeight, 0.3), 0, 1), 2),
      handicapSmoothingFactor: round(clamp(finite(model.handicapSmoothingFactor, 0.274), 0, 0.8), 3)
    },
    globalParameters: {
      recommendationOdds: round(clamp(finite(global.recommendationOdds, 1.03), 1, 100), 2),
      handicapRecommendationThreshold: round(clamp(finite(global.handicapRecommendationThreshold, 68.16), 0, 100), 2),
      handicapReverseThreshold: round(clamp(finite(global.handicapReverseThreshold, 46.78), 0, 100), 2),
      singleRecommendationThreshold: round(clamp(finite(global.singleRecommendationThreshold, 71.72), 0, 100), 2)
    }
  }
}

function modelSignature(model) {
  return [
    model.hostTeamGoalFactor,
    model.homeTeamGoalFactor,
    model.seedTeamGoalFactor,
    model.officialMatchWeight,
    model.internationalFriendlyWeight,
    model.clubFriendlyWeight
  ].join('|')
}

function profileSignature(profile) {
  return JSON.stringify(normalizeProfile(profile))
}

function buildModelVariants(profile, key) {
  const center = normalizeProfile(profile).modelFactors
  const random = createRandom(stringSeed(key + ':model'))
  const variants = [{ ...center }]
  while (variants.length <= MODEL_VARIANT_COUNT) {
    const local = random() < 0.75
    variants.push({
      hostTeamGoalFactor: local
        ? randomBetween(random, Math.max(0.1, center.hostTeamGoalFactor * 0.78), Math.min(3, center.hostTeamGoalFactor * 1.22), 2)
        : randomBetween(random, 0.3, 3, 2),
      homeTeamGoalFactor: local
        ? randomBetween(random, Math.max(0.1, center.homeTeamGoalFactor * 0.78), Math.min(3, center.homeTeamGoalFactor * 1.22), 2)
        : randomBetween(random, 0.3, 3, 2),
      seedTeamGoalFactor: local
        ? randomBetween(random, Math.max(0.1, center.seedTeamGoalFactor * 0.78), Math.min(3, center.seedTeamGoalFactor * 1.22), 2)
        : randomBetween(random, 0.3, 3, 2),
      officialMatchWeight: local
        ? randomBetween(random, Math.max(1, center.officialMatchWeight - 0.4), Math.min(3, center.officialMatchWeight + 0.4), 2)
        : randomBetween(random, 1, 3, 2),
      internationalFriendlyWeight: local
        ? randomBetween(random, Math.max(0, center.internationalFriendlyWeight - 0.25), Math.min(1, center.internationalFriendlyWeight + 0.25), 2)
        : randomBetween(random, 0, 1, 2),
      clubFriendlyWeight: local
        ? randomBetween(random, Math.max(0, center.clubFriendlyWeight - 0.25), Math.min(1, center.clubFriendlyWeight + 0.25), 2)
        : randomBetween(random, 0, 1, 2),
      handicapSmoothingFactor: center.handicapSmoothingFactor
    })
  }
  return [...new Map(variants.map(model => [modelSignature(model), model])).values()]
}

function observedOdds(matches) {
  const values = new Set([1])
  for (const match of matches) {
    for (const odds of [match.sportteryNormalOdds, match.sportteryHandicapOdds]) {
      for (const key of ['win', 'draw', 'lose']) {
        const value = Number(odds?.[key])
        if (Number.isFinite(value) && value > 0) {
          values.add(round(value, 2))
          values.add(round(value + 0.01, 2))
        }
      }
    }
  }
  return [...values].sort((left, right) => left - right)
}

function evaluateProfile(backtest, profile) {
  const normalized = normalizeProfile(profile)
  return evaluateRecommendationBacktest(backtest.matches, {
    oddsMatchCount: backtest.oddsMatchCount,
    modelMode: 'after',
    globalParameters: normalized.globalParameters
  }).summary
}

function candidateFrom(profile, metrics) {
  return {
    profile: normalizeProfile(profile),
    metrics
  }
}

function collectCandidate(buckets, candidate) {
  if (!Number.isFinite(candidate.metrics.roi) || candidate.metrics.recommendedMatchCount <= 0) {
    return
  }
  const count = candidate.metrics.recommendedMatchCount
  const bucket = buckets.get(count) || []
  const signature = profileSignature(candidate.profile)
  if (!bucket.some(item => profileSignature(item.profile) === signature)) {
    bucket.push(candidate)
    bucket.sort((left, right) => right.metrics.roi - left.metrics.roi || right.metrics.samplingRate - left.metrics.samplingRate)
    bucket.splice(10)
    buckets.set(count, bucket)
  }
}

function buildRuleCandidate(center, modelFactors, random, oddsValues) {
  const local = random() < 0.72
  return normalizeProfile({
    modelFactors: {
      ...modelFactors,
      handicapSmoothingFactor: modelFactors.handicapSmoothingFactor
    },
    globalParameters: {
      recommendationOdds: pick(random, oddsValues),
      handicapRecommendationThreshold: local
        ? randomBetween(random, Math.max(0, center.globalParameters.handicapRecommendationThreshold - 25), Math.min(100, center.globalParameters.handicapRecommendationThreshold + 25), 2)
        : randomBetween(random, 0, 100, 2),
      handicapReverseThreshold: local
        ? randomBetween(random, Math.max(0, center.globalParameters.handicapReverseThreshold - 25), Math.min(100, center.globalParameters.handicapReverseThreshold + 25), 2)
        : randomBetween(random, 0, 100, 2),
      singleRecommendationThreshold: local
        ? randomBetween(random, Math.max(0, center.globalParameters.singleRecommendationThreshold - 25), Math.min(100, center.globalParameters.singleRecommendationThreshold + 25), 2)
        : randomBetween(random, 0, 100, 2)
    }
  })
}

function searchRules(backtest, baseProfile, modelFactors, count, seed) {
  const center = normalizeProfile(baseProfile)
  const random = createRandom(seed)
  const oddsValues = observedOdds(backtest.matches)
  const buckets = new Map()
  const initialProfiles = [
    normalizeProfile({ ...center, modelFactors: { ...modelFactors, handicapSmoothingFactor: center.modelFactors.handicapSmoothingFactor } }),
    normalizeProfile({
      modelFactors: { ...modelFactors },
      globalParameters: {
        recommendationOdds: 1,
        handicapRecommendationThreshold: 100,
        handicapReverseThreshold: 0,
        singleRecommendationThreshold: 0
      }
    })
  ]
  initialProfiles.forEach(profile => collectCandidate(buckets, candidateFrom(profile, evaluateProfile(backtest, profile))))
  for (let index = 0; index < count; index++) {
    const profile = buildRuleCandidate(center, modelFactors, random, oddsValues)
    collectCandidate(buckets, candidateFrom(profile, evaluateProfile(backtest, profile)))
  }
  return [...buckets.values()].flat()
}

function withinSamplingBand(candidate, baselineSamplingRate) {
  return candidate.metrics.samplingRate + ROI_EPSILON >= Math.max(0, baselineSamplingRate - SAMPLING_TOLERANCE) &&
    candidate.metrics.samplingRate <= Math.min(1, baselineSamplingRate + SAMPLING_TOLERANCE) + ROI_EPSILON
}

function bestForModelRanking(pool, baselineSamplingRate) {
  const constrained = pool.filter(candidate => withinSamplingBand(candidate, baselineSamplingRate))
  const source = constrained.length > 0
    ? constrained
    : pool.filter(candidate => candidate.metrics.samplingRate >= MINIMUM_RELAXED_SAMPLING_RATE)
  return source.sort((left, right) => right.metrics.roi - left.metrics.roi)[0] || null
}

async function fetchJson(url, options = {}, attempts = 3) {
  let lastError = null
  for (let attempt = 1; attempt <= attempts; attempt++) {
    try {
      const response = await fetch(url, options)
      if (!response.ok) {
        throw new Error(`${response.status} ${response.statusText}: ${await response.text()}`)
      }
      return await response.json()
    } catch (error) {
      lastError = error
      if (attempt < attempts) {
        await new Promise(resolve => setTimeout(resolve, 500 * attempt))
      }
    }
  }
  throw lastError
}

async function fetchBacktest(competition, range, modelFactors, simulations, smoothingFactor = 0) {
  const url = new URL('/api/football/recommendation-backtest', API_BASE)
  url.searchParams.set('competition', competition)
  url.searchParams.set('includePreviousEdition', String(range === 'PREVIOUS'))
  url.searchParams.set('simulations', String(simulations))
  url.searchParams.set('clearModelCacheBefore', 'true')
  url.searchParams.set('hostTeamGoalFactor', modelFactors.hostTeamGoalFactor)
  url.searchParams.set('homeTeamGoalFactor', modelFactors.homeTeamGoalFactor)
  url.searchParams.set('seedTeamGoalFactor', modelFactors.seedTeamGoalFactor)
  url.searchParams.set('officialMatchWeight', modelFactors.officialMatchWeight)
  url.searchParams.set('internationalFriendlyWeight', modelFactors.internationalFriendlyWeight)
  url.searchParams.set('clubFriendlyWeight', modelFactors.clubFriendlyWeight)
  url.searchParams.set('handicapSmoothingFactor', smoothingFactor)
  const response = await fetchJson(url)
  return {
    completedMatchCount: Number(response.completedMatchCount) || 0,
    oddsMatchCount: Number(response.oddsMatchCount) || 0,
    matches: Array.isArray(response.matches) ? response.matches : []
  }
}

async function optimizePreset(competition, range, preset, originalProfile) {
  const key = `${competition}:${range}:${preset}`
  const normalizedOriginal = normalizeProfile(originalProfile)
  process.stdout.write(`  ${preset} baseline ${FINAL_SIMULATIONS}\n`)
  const baselineBacktest = await fetchBacktest(
    competition,
    range,
    normalizedOriginal.modelFactors,
    FINAL_SIMULATIONS,
    normalizedOriginal.modelFactors.handicapSmoothingFactor
  )
  if (baselineBacktest.oddsMatchCount === 0) {
    return {
      key,
      status: 'NO_DATA',
      originalProfile: normalizedOriginal,
      baselineMetrics: null,
      pool: []
    }
  }
  const baselineMetrics = evaluateProfile(baselineBacktest, normalizedOriginal)
  let finalPool = searchRules(
    baselineBacktest,
    normalizedOriginal,
    normalizedOriginal.modelFactors,
    FINAL_RULE_CANDIDATES,
    stringSeed(key + ':final:current')
  )
  const rankedModels = []
  const variants = buildModelVariants(normalizedOriginal, key).slice(1)
  for (let index = 0; index < variants.length; index++) {
    process.stdout.write(`  ${preset} model ${index + 1}/${variants.length}\n`)
    const model = variants[index]
    const coarseBacktest = await fetchBacktest(
      competition,
      range,
      model,
      COARSE_SIMULATIONS,
      model.handicapSmoothingFactor
    )
    if (coarseBacktest.oddsMatchCount === 0) {
      continue
    }
    const pool = searchRules(
      coarseBacktest,
      normalizedOriginal,
      model,
      COARSE_RULE_CANDIDATES,
      stringSeed(key + ':coarse:' + index)
    )
    const best = bestForModelRanking(pool, baselineMetrics.samplingRate)
    if (best) {
      rankedModels.push({ model, best })
    }
  }
  rankedModels.sort((left, right) => right.best.metrics.roi - left.best.metrics.roi)
  const finalist = rankedModels[0]
  if (finalist) {
    process.stdout.write(`  ${preset} finalist ${FINAL_SIMULATIONS}\n`)
    const finalistBacktest = await fetchBacktest(
      competition,
      range,
      finalist.model,
      FINAL_SIMULATIONS,
      finalist.model.handicapSmoothingFactor
    )
    finalPool = finalPool.concat(searchRules(
      finalistBacktest,
      normalizedOriginal,
      finalist.model,
      FINAL_RULE_CANDIDATES,
      stringSeed(key + ':final:alternative')
    ))
  }
  return {
    key,
    status: 'OPTIMIZED',
    originalProfile: normalizedOriginal,
    baselineMetrics,
    pool: deduplicatePool(finalPool)
  }
}

function deduplicatePool(pool) {
  const unique = new Map()
  pool.forEach(candidate => {
    const signature = profileSignature(candidate.profile)
    const existing = unique.get(signature)
    if (!existing || candidate.metrics.roi > existing.metrics.roi) {
      unique.set(signature, candidate)
    }
  })
  return [...unique.values()]
}

function choosePair(stableResult, aggressiveResult) {
  if (stableResult.status === 'NO_DATA' || aggressiveResult.status === 'NO_DATA') {
    return { status: 'NO_DATA' }
  }
  const constrained = choosePairFromPools(stableResult, aggressiveResult, false)
  if (constrained) {
    return { status: 'OPTIMIZED', samplingPolicy: 'CURRENT_PLUS_MINUS_3_PERCENT', ...constrained }
  }
  const relaxed = choosePairFromPools(stableResult, aggressiveResult, true)
  if (relaxed) {
    return { status: 'OPTIMIZED', samplingPolicy: 'RELAXED_MINIMUM_20_PERCENT', ...relaxed }
  }
  return { status: 'CONSTRAINT_FAILED' }
}

function choosePairFromPools(stableResult, aggressiveResult, relaxed) {
  const stableCandidates = stableResult.pool.filter(candidate => {
    const samplingEligible = relaxed
      ? candidate.metrics.samplingRate >= MINIMUM_RELAXED_SAMPLING_RATE
      : withinSamplingBand(candidate, stableResult.baselineMetrics.samplingRate)
    return samplingEligible && candidate.metrics.roi + ROI_EPSILON >= MINIMUM_STABLE_ROI
  })
  const aggressiveCandidates = aggressiveResult.pool.filter(candidate => {
    return relaxed
      ? candidate.metrics.samplingRate >= MINIMUM_RELAXED_SAMPLING_RATE
      : withinSamplingBand(candidate, aggressiveResult.baselineMetrics.samplingRate)
  })
  let best = null
  for (const stable of stableCandidates) {
    for (const aggressive of aggressiveCandidates) {
      if (aggressive.metrics.roi <= stable.metrics.roi + ROI_EPSILON) {
        continue
      }
      const score = stable.metrics.roi + aggressive.metrics.roi
      if (!best || score > best.score + ROI_EPSILON ||
        (Math.abs(score - best.score) <= ROI_EPSILON && aggressive.metrics.roi > best.aggressive.metrics.roi)) {
        best = { stable, aggressive, score }
      }
    }
  }
  return best
}

async function saveConfig(config) {
  return fetchJson(new URL('/api/football/user-config', API_BASE), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  })
}

async function startUiBacktest(competitions, range, preset, parameterProfiles) {
  const modelFactorsByCompetition = Object.fromEntries(competitions.map(([competition]) => [
    competition,
    parameterProfiles[`${competition}:${range}:${preset}`].modelFactors
  ]))
  const url = new URL('/api/football/recommendation-backtest/jobs', API_BASE)
  url.searchParams.set('competition', competitions.map(([competition]) => competition).join(','))
  url.searchParams.set('includePreviousEdition', String(range === 'PREVIOUS'))
  url.searchParams.set('simulations', String(FINAL_SIMULATIONS))
  let job = await fetchJson(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ modelFactorsByCompetition })
  })
  while (job.status === 'QUEUED' || job.status === 'RUNNING') {
    await new Promise(resolve => setTimeout(resolve, 500))
    job = await fetchJson(new URL(`/api/football/recommendation-backtest/jobs/${job.jobId}`, API_BASE))
  }
  if (job.status !== 'COMPLETED' || !job.result) {
    throw new Error(job.message || `UI backtest job failed: ${range}:${preset}`)
  }
  return job.result
}

async function verifyAllProfiles(config, optimizedRanges) {
  const verification = []
  for (const optimizedRange of optimizedRanges) {
    const { competition, competitionName, range } = optimizedRange
    for (const preset of ['STABLE', 'AGGRESSIVE']) {
      process.stdout.write(`verify UI job ${competition}:${range}:${preset}\n`)
      const response = await startUiBacktest(
        [[competition, competitionName]],
        range,
        preset,
        config.parameterProfiles
      )
      const matches = Array.isArray(response.matches) ? response.matches : []
        const profile = config.parameterProfiles[`${competition}:${range}:${preset}`]
        const result = evaluateRecommendationBacktest(matches, {
          oddsMatchCount: matches.length,
          modelMode: 'after',
          globalParameters: profile.globalParameters
        })
        verification.push({
          key: `${competition}:${range}:${preset}`,
          competition,
          competitionName,
          range,
          preset,
          metrics: matches.length > 0 ? result.summary : null
        })
    }
  }
  const violations = []
  for (const optimizedRange of optimizedRanges) {
    if (optimizedRange.status !== 'OPTIMIZED') {
      continue
    }
    const stable = verification.find(item => item.key === `${optimizedRange.competition}:${optimizedRange.range}:STABLE`)
    const aggressive = verification.find(item => item.key === `${optimizedRange.competition}:${optimizedRange.range}:AGGRESSIVE`)
    if (!stable?.metrics || !aggressive?.metrics) {
      violations.push(`${optimizedRange.competition}:${optimizedRange.range}:missing metrics`)
      continue
    }
    if (stable.metrics.roi + ROI_EPSILON < MINIMUM_STABLE_ROI) {
      violations.push(`${stable.key}: stable ROI below 5%`)
    }
    if (aggressive.metrics.roi <= stable.metrics.roi + ROI_EPSILON) {
      violations.push(`${aggressive.key}: aggressive ROI not above stable ROI`)
    }
    for (const item of [stable, aggressive]) {
      const weight = Number(config.parameterProfiles[item.key].modelFactors.officialMatchWeight)
      if (!Number.isFinite(weight) || weight < 1) {
        violations.push(`${item.key}: official match weight below 1`)
      }
    }
  }
  return { verification, violations }
}

function percent(value) {
  return Number.isFinite(value) ? `${(value * 100).toFixed(2)}%` : '--'
}

function buildMarkdown(report) {
  const rows = report.verification
    .filter(item => item.preset === 'STABLE')
    .map(stable => {
      const aggressive = report.verification.find(item => (
        item.competition === stable.competition &&
        item.range === stable.range &&
        item.preset === 'AGGRESSIVE'
      ))
      const rangeName = stable.range === 'CURRENT' ? '仅本届' : '含上届'
      const policy = report.optimizedRanges.find(item => (
        item.competition === stable.competition && item.range === stable.range
      ))?.samplingPolicy || '--'
      return `| ${stable.competitionName}·${rangeName} | ${percent(stable.metrics?.samplingRate)} | ${percent(stable.metrics?.roi)} | ${percent(aggressive?.metrics?.samplingRate)} | ${percent(aggressive?.metrics?.roi)} | ${policy} |`
    })
  return [
    '# 共享界面口径全赛事参数重优化报告',
    '',
    `- 生成时间：${report.completedAt}`,
    `- 最终模拟次数：${FINAL_SIMULATIONS.toLocaleString('en-US')}`,
    '- 正式比赛权重下限：1.00',
    '- 稳健方案ROI下限：5.00%',
    '- 激进方案ROI必须严格高于同赛事同时段稳健方案',
    '- 优先保持优化前采样率上下3个百分点，无法满足ROI关系时放宽至最低20%采样率',
    '- 验收逐赛事使用与界面相同的异步回测接口和共享推荐计算模块',
    '',
    '| 赛事时段 | 稳健采样率 | 稳健ROI | 激进采样率 | 激进ROI | 采样策略 |',
    '| --- | ---: | ---: | ---: | ---: | --- |',
    ...rows,
    '',
    `约束违规：${report.violations.length === 0 ? '0' : report.violations.join('；')}`,
    ''
  ].join('\n')
}

async function writeCheckpoint(report) {
  await fs.mkdir(path.dirname(CHECKPOINT_PATH), { recursive: true })
  await fs.writeFile(CHECKPOINT_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
}

async function main() {
  const config = JSON.parse(await fs.readFile(CONFIG_PATH, 'utf8'))
  let optimizedRanges = []
  if (RESUME_FROM_CHECKPOINT) {
    try {
      const checkpoint = JSON.parse(await fs.readFile(CHECKPOINT_PATH, 'utf8'))
      optimizedRanges = Array.isArray(checkpoint.optimizedRanges)
        ? checkpoint.optimizedRanges
        : []
      optimizedRanges.forEach(item => {
        if (item.status !== 'OPTIMIZED' || !item.optimized) {
          return
        }
        config.parameterProfiles[`${item.competition}:${item.range}:STABLE`] = item.optimized.stable.profile
        config.parameterProfiles[`${item.competition}:${item.range}:AGGRESSIVE`] = item.optimized.aggressive.profile
      })
      process.stdout.write(`resumed ${optimizedRanges.length}/34 ranges\n`)
    } catch (error) {
      if (error?.code !== 'ENOENT') {
        throw error
      }
    }
  }
  for (let index = 0; index < COMPETITIONS.length; index++) {
    const [competition, competitionName] = COMPETITIONS[index]
    for (const range of ['CURRENT', 'PREVIOUS']) {
      if (optimizedRanges.some(item => item.competition === competition && item.range === range)) {
        continue
      }
      process.stdout.write(`[${optimizedRanges.length + 1}/34] ${competitionName}:${range}\n`)
      const stableKey = `${competition}:${range}:STABLE`
      const aggressiveKey = `${competition}:${range}:AGGRESSIVE`
      const stableResult = await optimizePreset(competition, range, 'STABLE', config.parameterProfiles[stableKey])
      const aggressiveResult = await optimizePreset(competition, range, 'AGGRESSIVE', config.parameterProfiles[aggressiveKey])
      const pair = choosePair(stableResult, aggressiveResult)
      const result = {
        competition,
        competitionName,
        range,
        status: pair.status,
        samplingPolicy: pair.samplingPolicy || null,
        baseline: {
          stable: stableResult.baselineMetrics,
          aggressive: aggressiveResult.baselineMetrics
        },
        optimized: pair.status === 'OPTIMIZED'
          ? {
              stable: pair.stable,
              aggressive: pair.aggressive
            }
          : null
      }
      optimizedRanges.push(result)
      if (pair.status === 'OPTIMIZED') {
        config.parameterProfiles[stableKey] = pair.stable.profile
        config.parameterProfiles[aggressiveKey] = pair.aggressive.profile
        process.stdout.write(`  selected stable=${percent(pair.stable.metrics.roi)} aggressive=${percent(pair.aggressive.metrics.roi)} policy=${pair.samplingPolicy}\n`)
      } else {
        process.stdout.write(`  status=${pair.status}\n`)
      }
      await writeCheckpoint({ generatedAt: new Date().toISOString(), optimizedRanges })
    }
  }

  const constraintFailures = optimizedRanges.filter(item => item.status === 'CONSTRAINT_FAILED')
  if (constraintFailures.length > 0) {
    const keys = constraintFailures.map(item => `${item.competition}:${item.range}`).join(', ')
    throw new Error(`unable to satisfy profile constraints: ${keys}`)
  }

  if (APPLY_RESULTS) {
    process.stdout.write('saving user config\n')
    const saved = await saveConfig(config)
    config.parameterProfiles = saved.parameterProfiles
  }

  const { verification, violations } = await verifyAllProfiles(config, optimizedRanges)
  const report = {
    generatedAt: new Date().toISOString(),
    completedAt: new Date().toISOString(),
    applied: APPLY_RESULTS,
    options: {
      finalSimulations: FINAL_SIMULATIONS,
      coarseSimulations: COARSE_SIMULATIONS,
      modelVariantCount: MODEL_VARIANT_COUNT,
      coarseRuleCandidates: COARSE_RULE_CANDIDATES,
      finalRuleCandidates: FINAL_RULE_CANDIDATES,
      samplingTolerance: SAMPLING_TOLERANCE,
      minimumStableRoi: MINIMUM_STABLE_ROI,
      minimumRelaxedSamplingRate: MINIMUM_RELAXED_SAMPLING_RATE
    },
    optimizedRanges,
    verification,
    violations
  }
  await fs.mkdir(path.dirname(REPORT_JSON_PATH), { recursive: true })
  await fs.writeFile(REPORT_JSON_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
  await fs.writeFile(REPORT_MARKDOWN_PATH, buildMarkdown(report), 'utf8')
  if (violations.length > 0) {
    throw new Error(`verification failed: ${violations.join('; ')}`)
  }
  process.stdout.write(`completed report=${REPORT_MARKDOWN_PATH}\n`)
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
