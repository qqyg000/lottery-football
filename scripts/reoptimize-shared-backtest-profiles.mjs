import fs from 'node:fs/promises'
import path from 'node:path'

import { evaluateRecommendationBacktest } from '../frontend/src/recommendation-backtest.mjs'

const ROOT = process.cwd()
const API_BASE = process.env.LOTTERY_FOOTBALL_API_BASE || 'http://127.0.0.1:8080'
const CONFIG_PATH = path.join(ROOT, 'config/user-config.json')
const REPORT_JSON_PATH = path.resolve(
  ROOT,
  process.env.REPORT_JSON_PATH || 'reports/shared-backtest-parameter-optimization-2026-07-27.json'
)
const REPORT_MARKDOWN_PATH = path.resolve(
  ROOT,
  process.env.REPORT_MARKDOWN_PATH || 'reports/shared-backtest-parameter-optimization-2026-07-27.md'
)
const CHECKPOINT_PATH = path.resolve(
  ROOT,
  process.env.CHECKPOINT_PATH || 'target/shared-backtest-optimizer-checkpoint-2026-07-27.json'
)

const FINAL_SIMULATIONS = numberOption('FINAL_SIMULATIONS', 50000)
const COARSE_SIMULATIONS = numberOption('COARSE_SIMULATIONS', 5000)
const MODEL_VARIANT_COUNT = numberOption('MODEL_VARIANT_COUNT', 5, true)
const FINALIST_MODEL_COUNT = numberOption('FINALIST_MODEL_COUNT', 1)
const COARSE_RULE_CANDIDATES = numberOption('COARSE_RULE_CANDIDATES', 1800)
const FINAL_RULE_CANDIDATES = numberOption('FINAL_RULE_CANDIDATES', 9000)
const REFINEMENT_PASSES = numberOption('REFINEMENT_PASSES', 1, true)
const SAMPLING_TOLERANCE = decimalOption('SAMPLING_TOLERANCE', 0.03)
const SAMPLING_MAX_INCREASE = decimalOption('SAMPLING_MAX_INCREASE', SAMPLING_TOLERANCE)
const STRICT_SAMPLING_BAND = booleanOption('STRICT_SAMPLING_BAND', false)
const MINIMUM_STABLE_ROI = 0.05
const MINIMUM_AGGRESSIVE_SAMPLING_RATE = decimalOption('MINIMUM_AGGRESSIVE_SAMPLING_RATE', 0.333)
const MINIMUM_AGGRESSIVE_ROI_GAP = decimalOption('MINIMUM_AGGRESSIVE_ROI_GAP', 0)
const ROBUST_VALIDATION = booleanOption('ROBUST_VALIDATION', true)
const VALIDATION_FRACTION = decimalOption('VALIDATION_FRACTION', 0.3)
const MINIMUM_TRAINING_MATCHES = numberOption('MINIMUM_TRAINING_MATCHES', 10)
const MINIMUM_VALIDATION_MATCHES = numberOption('MINIMUM_VALIDATION_MATCHES', 6)
const MINIMUM_VALIDATION_ROI = decimalOption('MINIMUM_VALIDATION_ROI', 0)
const MINIMUM_TRAINING_ROI = decimalOption('MINIMUM_TRAINING_ROI', 0)
const ROI_EPSILON = 1e-9
const APPLY_RESULTS = process.argv.includes('--apply')
const RESUME_FROM_CHECKPOINT = process.argv.includes('--resume')
const AUDIT_ONLY = process.argv.includes('--audit-only')
const REOPTIMIZE_ALL = process.argv.includes('--reoptimize-all')
const APPLY_VERIFIED_CHECKPOINT = process.argv.includes('--apply-verified-checkpoint')
const RETRY_RANGES = new Set(
  String(process.env.RETRY_RANGES || '')
    .split(',')
    .map(value => value.trim())
    .filter(Boolean)
)
const TARGET_RANGES = stringSetOption('TARGET_RANGES')
const BACKTEST_MEMORY_CACHE = new Map()

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
  ['K_LEAGUE_1', '韩职'],
  ['SCOTTISH_FA_CUP', '苏足总杯']
]

const FORCE_REOPTIMIZE_RANGES = process.env.FORCE_REOPTIMIZE_RANGES == null
  ? new Set([
      'SWEDISH_ALLSVENSKAN:CURRENT',
      'FINNISH_VEIKKAUSLIIGA:CURRENT',
      'K_LEAGUE_1:CURRENT'
    ])
  : stringSetOption('FORCE_REOPTIMIZE_RANGES')

if (VALIDATION_FRACTION <= 0 || VALIDATION_FRACTION >= 0.5) {
  throw new Error('VALIDATION_FRACTION must be greater than 0 and less than 0.5')
}

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
    model.clubFriendlyWeight,
    model.handicapSmoothingFactor
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
      handicapSmoothingFactor: local
        ? randomBetween(random, Math.max(0, center.handicapSmoothingFactor - 0.2), Math.min(0.8, center.handicapSmoothingFactor + 0.2), 3)
        : randomBetween(random, 0, 0.8, 3)
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

function booleanOption(name, fallback) {
  const value = String(process.env[name] || '').trim().toLowerCase()
  if (!value) {
    return fallback
  }
  return ['1', 'true', 'yes', 'on'].includes(value)
}

function stringSetOption(name) {
  return new Set(
    String(process.env[name] || '')
      .split(',')
      .map(value => value.trim())
      .filter(Boolean)
  )
}

function decimalOption(name, fallback) {
  const value = Number(process.env[name])
  return Number.isFinite(value) && value >= 0 ? value : fallback
}

function observedProbabilityThresholds(matches) {
  const values = new Set([0, 100])
  const collectProbability = probability => {
    for (const key of ['win', 'draw', 'lose']) {
      const value = Number(probability?.[key])
      if (!Number.isFinite(value)) {
        continue
      }
      for (const candidate of [value - 0.01, value, value + 0.01]) {
        values.add(round(clamp(candidate, 0, 100), 2))
      }
    }
  }
  for (const match of matches) {
    collectProbability(match.adjustedNormalProbability || match.normalProbability)
    const probabilities = Array.isArray(match.adjustedHandicapProbabilities)
      ? match.adjustedHandicapProbabilities
      : match.handicapProbabilities
    for (const item of probabilities || []) {
      collectProbability(item?.probability)
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

function compareMatchesChronologically(left, right) {
  return String(left.matchDate || '').localeCompare(String(right.matchDate || '')) ||
    String(left.kickoffTime || '').localeCompare(String(right.kickoffTime || '')) ||
    String(left.matchId || left.sportteryMatchId || '').localeCompare(
      String(right.matchId || right.sportteryMatchId || '')
    )
}

function backtestPartition(matches) {
  return {
    completedMatchCount: matches.length,
    oddsMatchCount: matches.length,
    matches
  }
}

function createChronologicalValidationSplit(backtest) {
  const matches = [...backtest.matches].sort(compareMatchesChronologically)
  if (!ROBUST_VALIDATION || matches.length < MINIMUM_TRAINING_MATCHES + MINIMUM_VALIDATION_MATCHES) {
    return {
      available: false,
      fullBacktest: backtestPartition(matches),
      trainingBacktest: backtestPartition(matches),
      validationBacktest: backtestPartition([])
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
    while (
      sameDateStart > MINIMUM_TRAINING_MATCHES &&
      matches[sameDateStart - 1]?.matchDate === boundaryDate
    ) {
      sameDateStart -= 1
    }
    if (matches.length - sameDateStart >= MINIMUM_VALIDATION_MATCHES) {
      splitIndex = sameDateStart
    }
  }
  return {
    available: true,
    fullBacktest: backtestPartition(matches),
    trainingBacktest: backtestPartition(matches.slice(0, splitIndex)),
    validationBacktest: backtestPartition(matches.slice(splitIndex))
  }
}

function evaluateProfileRobustly(backtest, profile) {
  const split = createChronologicalValidationSplit(backtest)
  const fullMetrics = evaluateProfile(split.fullBacktest, profile)
  const trainingMetrics = evaluateProfile(split.trainingBacktest, profile)
  const validationMetrics = split.available
    ? evaluateProfile(split.validationBacktest, profile)
    : null
  return {
    validationAvailable: split.available,
    trainingMatchCount: split.trainingBacktest.oddsMatchCount,
    validationMatchCount: split.validationBacktest.oddsMatchCount,
    fullMetrics,
    trainingMetrics,
    validationMetrics
  }
}

function enrichCandidate(backtest, candidate) {
  const robustness = evaluateProfileRobustly(backtest, candidate.profile)
  return {
    ...candidate,
    metrics: robustness.fullMetrics,
    ...robustness
  }
}

function candidateFrom(profile, metrics, robustness = {}) {
  return {
    profile: normalizeProfile(profile),
    metrics,
    ...robustness
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

function limitCandidateValues(values, maximum = 100) {
  if (values.length <= maximum) {
    return values
  }
  const limited = []
  for (let index = 0; index < maximum; index++) {
    limited.push(values[Math.round(index * (values.length - 1) / (maximum - 1))])
  }
  return [...new Set(limited)]
}

function refineRulePool(backtest, pool, samplingBounds, passes = 1) {
  if (!samplingBounds) {
    return pool
  }
  const oddsValues = limitCandidateValues(observedOdds(backtest.matches))
  const probabilityValues = limitCandidateValues(observedProbabilityThresholds(backtest.matches))
  const valueSets = {
    recommendationOdds: oddsValues,
    handicapRecommendationThreshold: probabilityValues,
    handicapReverseThreshold: probabilityValues,
    singleRecommendationThreshold: probabilityValues
  }
  let refined = deduplicatePool(pool)
  for (let pass = 0; pass < passes; pass++) {
    const buckets = new Map()
    refined.forEach(candidate => collectCandidate(buckets, candidate))
    const seedsByCount = new Map()
    refined
      .filter(candidate => (
        candidate.metrics.samplingRate + ROI_EPSILON >= samplingBounds.minimum &&
        candidate.metrics.samplingRate <= samplingBounds.maximum + ROI_EPSILON
      ))
      .sort(candidateRanking)
      .forEach(candidate => {
        const count = candidate.metrics.recommendedMatchCount
        const seeds = seedsByCount.get(count) || []
        if (seeds.length < 1) {
          seeds.push(candidate)
          seedsByCount.set(count, seeds)
        }
      })
    const seeds = [...seedsByCount.values()].flat()
    for (const seed of seeds) {
      for (const [parameter, values] of Object.entries(valueSets)) {
        for (const value of values) {
          if (value === seed.profile.globalParameters[parameter]) {
            continue
          }
          const profile = normalizeProfile({
            modelFactors: seed.profile.modelFactors,
            globalParameters: {
              ...seed.profile.globalParameters,
              [parameter]: value
            }
          })
          collectCandidate(buckets, candidateFrom(profile, evaluateProfile(backtest, profile)))
        }
      }
    }
    refined = [...buckets.values()].flat()
  }
  return deduplicatePool(refined)
}

function withinSamplingBand(candidate, baselineSamplingRate) {
  return candidate.metrics.samplingRate + ROI_EPSILON >= Math.max(0, baselineSamplingRate - SAMPLING_TOLERANCE) &&
    candidate.metrics.samplingRate <= Math.min(1, baselineSamplingRate + SAMPLING_MAX_INCREASE) + ROI_EPSILON
}

function samplingBoundsForBaseline(baselineSamplingRate) {
  return {
    minimum: Math.max(0, baselineSamplingRate - SAMPLING_TOLERANCE),
    maximum: Math.min(1, baselineSamplingRate + SAMPLING_MAX_INCREASE)
  }
}

function withinSamplingBounds(candidate, samplingBounds) {
  return candidate.metrics.samplingRate + ROI_EPSILON >= samplingBounds.minimum &&
    candidate.metrics.samplingRate <= samplingBounds.maximum + ROI_EPSILON
}

function minimumSamplingRateFor(competition, range, preset) {
  if (preset === 'STABLE') {
    return competition === 'EUROPEAN_CHAMPIONSHIP' && range === 'CURRENT' ? 0.4 : 0.6
  }
  if (
    range === 'CURRENT' &&
    ['EUROPEAN_CHAMPIONSHIP', 'FINNISH_VEIKKAUSLIIGA'].includes(competition)
  ) {
    return MINIMUM_AGGRESSIVE_SAMPLING_RATE
  }
  if (competition === 'K_LEAGUE_1' && range === 'CURRENT') {
    return 0.4
  }
  return 0.5
}

function preferredSamplingWindow(competition, range, preset) {
  if (range !== 'CURRENT' || preset !== 'AGGRESSIVE') {
    return null
  }
  if (competition === 'EUROPEAN_CHAMPIONSHIP') {
    return { minimum: 0.49, maximum: 0.51 }
  }
  if (competition === 'FINNISH_VEIKKAUSLIIGA') {
    return { minimum: 0.48, maximum: 0.52 }
  }
  return null
}

function samplingBoundsForProfile(competition, range, preset, baselineSamplingRate, relaxed = false) {
  const hardMinimum = minimumSamplingRateFor(competition, range, preset)
  const preferred = preferredSamplingWindow(competition, range, preset)
  if (relaxed) {
    return { minimum: hardMinimum, maximum: 1 }
  }
  if (!Number.isFinite(baselineSamplingRate)) {
    return {
      minimum: preferred?.minimum ?? hardMinimum,
      maximum: preferred?.maximum ?? 1
    }
  }
  const minimum = Math.max(
    hardMinimum,
    baselineSamplingRate - SAMPLING_TOLERANCE,
    preferred?.minimum ?? 0
  )
  const maximum = Math.min(
    1,
    baselineSamplingRate + SAMPLING_MAX_INCREASE,
    preferred?.maximum ?? 1
  )
  return minimum <= maximum + ROI_EPSILON
    ? { minimum, maximum }
    : {
        minimum: preferred?.minimum ?? hardMinimum,
        maximum: preferred?.maximum ?? 1
      }
}

function candidatePartitions(candidate) {
  return [candidate.trainingMetrics, candidate.validationMetrics, candidate.metrics]
}

function meetsMinimumSamplingRate(rate, competition, range, preset) {
  const minimum = minimumSamplingRateFor(competition, range, preset)
  const strict = preset === 'AGGRESSIVE' &&
    range === 'CURRENT' &&
    ['EUROPEAN_CHAMPIONSHIP', 'FINNISH_VEIKKAUSLIIGA'].includes(competition)
  return strict
    ? rate > minimum + ROI_EPSILON
    : rate + ROI_EPSILON >= minimum
}

function isRobustCandidate(candidate, competition, range, preset) {
  if (!candidate || !candidate.validationAvailable) {
    return false
  }
  const partitions = candidatePartitions(candidate)
  if (partitions.some(metrics => (
    !metrics ||
    metrics.recommendedMatchCount <= 0 ||
    !meetsMinimumSamplingRate(metrics.samplingRate, competition, range, preset)
  ))) {
    return false
  }
  if (
    !Number.isFinite(candidate.trainingMetrics.roi) ||
    candidate.trainingMetrics.roi + ROI_EPSILON < MINIMUM_TRAINING_ROI ||
    !Number.isFinite(candidate.validationMetrics.roi) ||
    candidate.validationMetrics.roi + ROI_EPSILON < MINIMUM_VALIDATION_ROI ||
    !Number.isFinite(candidate.metrics.roi)
  ) {
    return false
  }
  const minimumFullRoi = preset === 'STABLE' ? MINIMUM_STABLE_ROI : 0
  return candidate.metrics.roi + ROI_EPSILON >= minimumFullRoi
}

function isRobustPair(stable, aggressive, competition, range) {
  if (
    !isRobustCandidate(stable, competition, range, 'STABLE') ||
    !isRobustCandidate(aggressive, competition, range, 'AGGRESSIVE')
  ) {
    return false
  }
  return aggressive.metrics.roi > stable.metrics.roi + MINIMUM_AGGRESSIVE_ROI_GAP + ROI_EPSILON &&
    aggressive.trainingMetrics.roi > stable.trainingMetrics.roi + MINIMUM_AGGRESSIVE_ROI_GAP + ROI_EPSILON &&
    aggressive.metrics.samplingRate <= stable.metrics.samplingRate + ROI_EPSILON &&
    aggressive.trainingMetrics.samplingRate <= stable.trainingMetrics.samplingRate + ROI_EPSILON &&
    aggressive.validationMetrics.samplingRate <= stable.validationMetrics.samplingRate + ROI_EPSILON
}

function isFullSamplePairValid(stableMetrics, aggressiveMetrics, competition, range) {
  return Boolean(
    stableMetrics &&
    aggressiveMetrics &&
    stableMetrics.recommendedMatchCount > 0 &&
    aggressiveMetrics.recommendedMatchCount > 0 &&
    meetsMinimumSamplingRate(stableMetrics.samplingRate, competition, range, 'STABLE') &&
    meetsMinimumSamplingRate(aggressiveMetrics.samplingRate, competition, range, 'AGGRESSIVE') &&
    stableMetrics.roi + ROI_EPSILON >= MINIMUM_STABLE_ROI &&
    aggressiveMetrics.roi > stableMetrics.roi + MINIMUM_AGGRESSIVE_ROI_GAP + ROI_EPSILON &&
    aggressiveMetrics.samplingRate <= stableMetrics.samplingRate + ROI_EPSILON
  )
}

function summarizeCandidate(candidate) {
  return candidate
    ? {
        profile: candidate.profile,
        trainingMetrics: candidate.trainingMetrics,
        validationMetrics: candidate.validationMetrics,
        fullMetrics: candidate.metrics
      }
    : null
}

function summarizeRobustPool(pool, competition, range, preset) {
  const robustCandidates = pool
    .filter(candidate => isRobustCandidate(candidate, competition, range, preset))
    .sort(robustCandidateRanking)
  return {
    candidateCount: pool.length,
    robustCandidateCount: robustCandidates.length,
    bestRobustCandidate: summarizeCandidate(robustCandidates[0] || null)
  }
}

function closedProfile(profile) {
  const normalized = normalizeProfile(profile)
  return normalizeProfile({
    modelFactors: normalized.modelFactors,
    globalParameters: {
      ...normalized.globalParameters,
      recommendationOdds: 100
    }
  })
}

function bestForModelRanking(pool, baselineSamplingRate) {
  const constrained = pool.filter(candidate => withinSamplingBand(candidate, baselineSamplingRate))
  const source = constrained.length > 0
    ? constrained
    : pool.filter(candidate => candidate.metrics.samplingRate >= MINIMUM_AGGRESSIVE_SAMPLING_RATE)
  return source.sort((left, right) => right.metrics.roi - left.metrics.roi)[0] || null
}

function bestForTargetSampling(pool, samplingBounds) {
  return pool
    .filter(candidate => withinSamplingBounds(candidate, samplingBounds))
    .sort(candidateRanking)[0] || null
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
  const normalizedModel = normalizeProfile({
    modelFactors: { ...modelFactors, handicapSmoothingFactor: smoothingFactor }
  }).modelFactors
  const cacheKey = `${competition}:${range}:${simulations}:${modelSignature(normalizedModel)}`
  const cached = BACKTEST_MEMORY_CACHE.get(cacheKey)
  if (cached) {
    return cached
  }
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
  const backtest = {
    completedMatchCount: Number(response.completedMatchCount) || 0,
    oddsMatchCount: Number(response.oddsMatchCount) || 0,
    matches: Array.isArray(response.matches) ? response.matches : []
  }
  BACKTEST_MEMORY_CACHE.set(cacheKey, backtest)
  return backtest
}

async function optimizePreset(competition, range, preset, originalProfile, options = {}) {
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
  const baselineRobustness = evaluateProfileRobustly(baselineBacktest, normalizedOriginal)
  const baselineMetrics = baselineRobustness.fullMetrics
  const baselineSplit = createChronologicalValidationSplit(baselineBacktest)
  if (ROBUST_VALIDATION && !baselineSplit.available) {
    return {
      key,
      status: 'INSUFFICIENT_VALIDATION_SAMPLE',
      originalProfile: normalizedOriginal,
      baselineMetrics,
      baselineRobustness,
      pool: []
    }
  }
  let finalPool = refineRulePool(
    baselineSplit.trainingBacktest,
    searchRules(
      baselineSplit.trainingBacktest,
      normalizedOriginal,
      normalizedOriginal.modelFactors,
      FINAL_RULE_CANDIDATES,
      stringSeed(key + ':final:current')
    ),
    options.samplingBounds,
    REFINEMENT_PASSES
  ).map(candidate => enrichCandidate(baselineBacktest, candidate))
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
    const coarseSplit = createChronologicalValidationSplit(coarseBacktest)
    if (ROBUST_VALIDATION && !coarseSplit.available) {
      continue
    }
    const pool = searchRules(
      coarseSplit.trainingBacktest,
      normalizedOriginal,
      model,
      COARSE_RULE_CANDIDATES,
      stringSeed(key + ':coarse:' + index)
    )
    const best = options.samplingBounds
      ? bestForTargetSampling(pool, options.samplingBounds)
      : bestForModelRanking(pool, baselineMetrics.samplingRate)
    if (best) {
      rankedModels.push({ model, best })
    }
  }
  rankedModels.sort((left, right) => right.best.metrics.roi - left.best.metrics.roi)
  const finalists = rankedModels.slice(0, FINALIST_MODEL_COUNT)
  for (let index = 0; index < finalists.length; index++) {
    const finalist = finalists[index]
    process.stdout.write(`  ${preset} finalist ${index + 1}/${finalists.length} ${FINAL_SIMULATIONS}\n`)
    const finalistBacktest = await fetchBacktest(
      competition,
      range,
      finalist.model,
      FINAL_SIMULATIONS,
      finalist.model.handicapSmoothingFactor
    )
    const finalistSplit = createChronologicalValidationSplit(finalistBacktest)
    if (ROBUST_VALIDATION && !finalistSplit.available) {
      continue
    }
    finalPool = finalPool.concat(refineRulePool(
      finalistSplit.trainingBacktest,
      searchRules(
        finalistSplit.trainingBacktest,
        normalizedOriginal,
        finalist.model,
        FINAL_RULE_CANDIDATES,
        stringSeed(key + ':final:alternative:' + index)
      ),
      options.samplingBounds,
      REFINEMENT_PASSES
    ).map(candidate => enrichCandidate(finalistBacktest, candidate)))
  }
  return {
    key,
    status: 'OPTIMIZED',
    originalProfile: normalizedOriginal,
    baselineMetrics,
    baselineRobustness,
    pool: deduplicatePool(finalPool)
  }
}

function deduplicatePool(pool) {
  const unique = new Map()
  pool.forEach(candidate => {
    const signature = profileSignature(candidate.profile)
    const existing = unique.get(signature)
    const candidateRoi = candidate.trainingMetrics?.roi ?? candidate.metrics.roi
    const existingRoi = existing?.trainingMetrics?.roi ?? existing?.metrics.roi
    if (!existing || candidateRoi > existingRoi) {
      unique.set(signature, candidate)
    }
  })
  return [...unique.values()]
}

function allCompetitionRanges() {
  const ranges = COMPETITIONS.flatMap(([competition, competitionName]) => (
    ['PREVIOUS', 'CURRENT'].map(range => ({ competition, competitionName, range }))
  ))
  return TARGET_RANGES.size === 0
    ? ranges
    : ranges.filter(item => TARGET_RANGES.has(`${item.competition}:${item.range}`))
}

function metricFor(verification, competition, range, preset) {
  return verification.find(item => (
    item.competition === competition &&
    item.range === range &&
    item.preset === preset
  ))?.metrics || null
}

function verificationFor(verification, competition, range, preset) {
  return verification.find(item => (
    item.competition === competition &&
    item.range === range &&
    item.preset === preset
  )) || null
}

function determineOptimizationAction(rangeItem, verification) {
  const { competition, range } = rangeItem
  const key = `${competition}:${range}`
  const stableVerification = verificationFor(verification, competition, range, 'STABLE')
  const aggressiveVerification = verificationFor(verification, competition, range, 'AGGRESSIVE')
  const stable = stableVerification?.metrics || null
  const aggressive = aggressiveVerification?.metrics || null
  if (!stable || !aggressive) {
    return {
      action: 'NO_DATA',
      reasons: ['没有可结算赔率样本'],
      stable,
      aggressive,
      stableVerification,
      aggressiveVerification
    }
  }

  if (
    ROBUST_VALIDATION &&
    (!stableVerification.robustness?.validationAvailable || !aggressiveVerification.robustness?.validationAvailable)
  ) {
    return {
      action: range === 'CURRENT' ? 'FALLBACK_TO_PREVIOUS' : 'INSUFFICIENT_VALIDATION_SAMPLE',
      reasons: ['样本不足以拆分训练集和时间留出验证集'],
      stable,
      aggressive,
      stableVerification,
      aggressiveVerification
    }
  }

  const reasons = []
  const transferRequired = aggressive.samplingRate > stable.samplingRate + ROI_EPSILON &&
    aggressive.roi > stable.roi + ROI_EPSILON
  if (transferRequired) {
    reasons.push('激进方案采样率和ROI均高于稳健方案')
  }
  if (aggressive.samplingRate + ROI_EPSILON < MINIMUM_AGGRESSIVE_SAMPLING_RATE) {
    reasons.push('激进方案采样率低于33.3%')
  }
  if (aggressive.roi <= stable.roi + ROI_EPSILON) {
    reasons.push('激进方案ROI未高于稳健方案')
  }
  if (stable.roi + ROI_EPSILON < MINIMUM_STABLE_ROI) {
    reasons.push('稳健方案ROI低于5%')
  }
  if (FORCE_REOPTIMIZE_RANGES.has(key)) {
    reasons.push('赛事数据已更新，强制重新优化')
  }
  if (REOPTIMIZE_ALL) {
    reasons.push('全赛事重新优化')
  }

  let action = 'KEEP'
  if (REOPTIMIZE_ALL) {
    action = 'REOPTIMIZE_PAIR'
  } else if (transferRequired) {
    action = 'TRANSFER_AND_REOPTIMIZE_AGGRESSIVE'
  } else if (
    REOPTIMIZE_ALL ||
    FORCE_REOPTIMIZE_RANGES.has(key) ||
    stable.roi + ROI_EPSILON < MINIMUM_STABLE_ROI
  ) {
    action = 'REOPTIMIZE_PAIR'
  } else if (
    aggressive.samplingRate + ROI_EPSILON < MINIMUM_AGGRESSIVE_SAMPLING_RATE ||
    aggressive.roi <= stable.roi + ROI_EPSILON
  ) {
    action = 'REOPTIMIZE_AGGRESSIVE'
  }
  return {
    action,
    reasons,
    stable,
    aggressive,
    stableVerification,
    aggressiveVerification
  }
}

function candidateRanking(left, right) {
  return right.metrics.roi - left.metrics.roi ||
    right.metrics.samplingRate - left.metrics.samplingRate
}

function robustCandidateRanking(left, right) {
  return right.trainingMetrics.roi - left.trainingMetrics.roi ||
    right.trainingMetrics.samplingRate - left.trainingMetrics.samplingRate ||
    right.trainingMetrics.recommendedSelectionCount - left.trainingMetrics.recommendedSelectionCount ||
    profileSignature(left.profile).localeCompare(profileSignature(right.profile))
}

function chooseAggressiveCandidate(pool, stable, competition, range, samplingBounds) {
  return pool
    .filter(candidate => (
      withinSamplingBounds(candidate, samplingBounds) &&
      isRobustPair(stable, candidate, competition, range)
    ))
    .sort(robustCandidateRanking)[0] || null
}

function bestAggressiveInSamplingWindow(pool, stable, competition, range, samplingBounds) {
  return pool
    .filter(candidate => (
      withinSamplingBounds(candidate, samplingBounds) &&
      isRobustCandidate(candidate, competition, range, 'AGGRESSIVE') &&
      candidate.metrics.samplingRate <= stable.metrics.samplingRate + ROI_EPSILON
    ))
    .sort(robustCandidateRanking)[0] || null
}

function chooseReoptimizedPair(stableResult, aggressiveResult, competition, range) {
  if (stableResult.status !== 'OPTIMIZED' || aggressiveResult.status !== 'OPTIMIZED') {
    return null
  }
  const preferredStableBounds = samplingBoundsForProfile(
    competition,
    range,
    'STABLE',
    stableResult.baselineMetrics.samplingRate
  )
  const preferredAggressiveBounds = samplingBoundsForProfile(
    competition,
    range,
    'AGGRESSIVE',
    aggressiveResult.baselineMetrics.samplingRate
  )
  const candidateGroups = [[
    'BASELINE_BAND_ROBUST_VALIDATED',
    preferredStableBounds,
    preferredAggressiveBounds
  ]]
  if (!STRICT_SAMPLING_BAND) {
    candidateGroups.push([
      'HARD_SAMPLING_FLOOR_ROBUST_VALIDATED',
      samplingBoundsForProfile(competition, range, 'STABLE', null, true),
      samplingBoundsForProfile(competition, range, 'AGGRESSIVE', null, true)
    ])
  }
  for (const [samplingPolicy, stableBounds, aggressiveBounds] of candidateGroups) {
    const stableCandidates = stableResult.pool.filter(candidate => (
      withinSamplingBounds(candidate, stableBounds) &&
      isRobustCandidate(candidate, competition, range, 'STABLE')
    ))
    let best = null
    for (const stable of stableCandidates) {
      const aggressive = chooseAggressiveCandidate(
        aggressiveResult.pool,
        stable,
        competition,
        range,
        aggressiveBounds
      )
      if (!aggressive) {
        continue
      }
      const score = stable.trainingMetrics.roi + aggressive.trainingMetrics.roi
      if (!best || score > best.score + ROI_EPSILON ||
        (Math.abs(score - best.score) <= ROI_EPSILON &&
          aggressive.trainingMetrics.roi > best.aggressive.trainingMetrics.roi)) {
        best = { stable, aggressive, score, samplingPolicy }
      }
    }
    if (best) {
      return best
    }
  }
  return null
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
  let lastReportedProgress = -1
  while (job.status === 'QUEUED' || job.status === 'RUNNING') {
    const progress = Math.floor(Number(job.progress) || 0)
    if (progress >= lastReportedProgress + 10) {
      process.stdout.write(
        `  job ${range}:${preset} progress=${progress}% ` +
        `matches=${job.processedMatchCount || 0}/${job.totalMatchCount || 0}\n`
      )
      lastReportedProgress = progress
    }
    await new Promise(resolve => setTimeout(resolve, 500))
    job = await fetchJson(new URL(`/api/football/recommendation-backtest/jobs/${job.jobId}`, API_BASE))
  }
  if (job.status !== 'COMPLETED' || !job.result) {
    throw new Error(job.message || `UI backtest job failed: ${range}:${preset}`)
  }
  return job.result
}

async function verifyAllProfiles(config, ranges = allCompetitionRanges()) {
  const verification = []
  for (const range of ['PREVIOUS', 'CURRENT']) {
    const competitions = ranges
      .filter(item => item.range === range)
      .map(({ competition, competitionName }) => [competition, competitionName])
    if (competitions.length === 0) {
      continue
    }
    for (const preset of ['STABLE', 'AGGRESSIVE']) {
      process.stdout.write(`verify UI batch ${range}:${preset} competitions=${competitions.length}\n`)
      const response = await startUiBacktest(competitions, range, preset, config.parameterProfiles)
      const allMatches = Array.isArray(response.matches) ? response.matches : []
      for (const [competition, competitionName] of competitions) {
        const matches = allMatches.filter(match => match.competition === competition)
        const oddsMatchCount = matches.length
        const profile = config.parameterProfiles[`${competition}:${range}:${preset}`]
        const normalizedModel = normalizeProfile(profile).modelFactors
        const cacheKey = `${competition}:${range}:${FINAL_SIMULATIONS}:${modelSignature(normalizedModel)}`
        BACKTEST_MEMORY_CACHE.set(cacheKey, {
          completedMatchCount: matches.length,
          oddsMatchCount,
          matches
        })
        const robustness = oddsMatchCount > 0
          ? evaluateProfileRobustly({
              completedMatchCount: matches.length,
              oddsMatchCount,
              matches
            }, profile)
          : null
        verification.push({
          key: `${competition}:${range}:${preset}`,
          competition,
          competitionName,
          range,
          preset,
          oddsMatchCount,
          metrics: robustness?.fullMetrics || null,
          robustness
        })
      }
    }
  }
  return verification
}

function findVerificationViolations(config, verification, baselineVerification, optimizationResults = []) {
  const violations = []
  for (const rangeItem of allCompetitionRanges()) {
    const { competition, range } = rangeItem
    const stable = verification.find(item => item.key === `${competition}:${range}:STABLE`)
    const aggressive = verification.find(item => item.key === `${competition}:${range}:AGGRESSIVE`)
    if (!stable?.metrics && !aggressive?.metrics) {
      continue
    }
    if (!stable?.metrics || !aggressive?.metrics) {
      violations.push(`${competition}:${range}:missing metrics`)
      continue
    }
    const optimizationResult = optimizationResults.find(item => (
      item.competition === competition && item.range === range
    ))
    if (String(optimizationResult?.status || '').startsWith('CLOSED_')) {
      if (
        stable.metrics.recommendedMatchCount !== 0 ||
        aggressive.metrics.recommendedMatchCount !== 0
      ) {
        violations.push(`${competition}:${range}: closed profile still produced recommendations`)
      }
      continue
    }
    if (stable.metrics.roi + ROI_EPSILON < MINIMUM_STABLE_ROI) {
      violations.push(`${stable.key}: stable ROI below 5%`)
    }
    if (aggressive.metrics.roi <= stable.metrics.roi + ROI_EPSILON) {
      violations.push(`${aggressive.key}: aggressive ROI not above stable ROI`)
    }
    for (const item of [stable, aggressive]) {
      if (!meetsMinimumSamplingRate(item.metrics.samplingRate, competition, range, item.preset)) {
        violations.push(
          `${item.key}: sampling rate below hard floor ${percent(minimumSamplingRateFor(competition, range, item.preset))}`
        )
      }
    }
    if (STRICT_SAMPLING_BAND) {
      for (const item of [stable, aggressive]) {
        const baseline = metricFor(baselineVerification, competition, range, item.preset)
        if (baseline && !withinSamplingBounds(item, samplingBoundsForBaseline(baseline.samplingRate))) {
          violations.push(`${item.key}: sampling rate outside allowed baseline band`)
        }
      }
    }
    if (aggressive.metrics.samplingRate > stable.metrics.samplingRate + ROI_EPSILON) {
      violations.push(`${aggressive.key}: aggressive sampling rate above stable sampling rate`)
    }
    if (stable.robustness?.validationAvailable && aggressive.robustness?.validationAvailable) {
      const stableCandidate = candidateFrom(
        config.parameterProfiles[stable.key],
        stable.metrics,
        stable.robustness
      )
      const aggressiveCandidate = candidateFrom(
        config.parameterProfiles[aggressive.key],
        aggressive.metrics,
        aggressive.robustness
      )
      if (!isRobustCandidate(stableCandidate, competition, range, 'STABLE')) {
        violations.push(`${stable.key}: training or validation robustness gate failed`)
      }
      if (!isRobustCandidate(aggressiveCandidate, competition, range, 'AGGRESSIVE')) {
        violations.push(`${aggressive.key}: training or validation robustness gate failed`)
      }
      if (
        aggressive.robustness.trainingMetrics.roi <=
        stable.robustness.trainingMetrics.roi + MINIMUM_AGGRESSIVE_ROI_GAP + ROI_EPSILON
      ) {
        violations.push(`${aggressive.key}: training ROI not above stable training ROI`)
      }
      if (
        aggressive.robustness.trainingMetrics.samplingRate >
        stable.robustness.trainingMetrics.samplingRate + ROI_EPSILON ||
        aggressive.robustness.validationMetrics.samplingRate >
        stable.robustness.validationMetrics.samplingRate + ROI_EPSILON
      ) {
        violations.push(`${aggressive.key}: partition sampling rate above stable sampling rate`)
      }
    }
    for (const item of [stable, aggressive]) {
      const weight = Number(config.parameterProfiles[item.key].modelFactors.officialMatchWeight)
      if (!Number.isFinite(weight) || weight < 1) {
        violations.push(`${item.key}: official match weight below 1`)
      }
    }
  }
  return violations
}

function percent(value) {
  return Number.isFinite(value) ? `${(value * 100).toFixed(2)}%` : '--'
}

function percentagePointDelta(before, after) {
  return Number.isFinite(before) && Number.isFinite(after)
    ? `${((after - before) * 100).toFixed(2)}`
    : '--'
}

function decimal(value) {
  return Number.isFinite(value) ? value.toFixed(2) : '--'
}

function buildMarkdown(report) {
  const rows = report.verification.map(item => {
    const baseline = report.baselineVerification.find(candidate => candidate.key === item.key)
    const optimizationResult = report.optimizationResults.find(candidate => (
      candidate.competition === item.competition && candidate.range === item.range
    ))
    const action = optimizationResult
      ? `${optimizationResult.action}/${optimizationResult.status}`
      : '--'
    const rangeName = item.range === 'CURRENT' ? '仅本届' : '含上届'
    const presetName = item.preset === 'STABLE' ? '稳健' : '激进'
    const hitRate = item.metrics?.recommendedMatchCount > 0
      ? item.metrics.hitMatchCount / item.metrics.recommendedMatchCount
      : null
    return `| ${item.competitionName}·${rangeName} | ${presetName} | ${item.oddsMatchCount} | ${item.robustness?.trainingMatchCount ?? '--'} | ${item.robustness?.validationMatchCount ?? '--'} | ${percent(baseline?.metrics?.samplingRate)} | ${percent(item.metrics?.samplingRate)} | ${percentagePointDelta(baseline?.metrics?.samplingRate, item.metrics?.samplingRate)} | ${percent(item.robustness?.trainingMetrics?.roi)} | ${percent(item.robustness?.validationMetrics?.roi)} | ${percent(item.metrics?.roi)} | ${percent(hitRate)} | ${item.metrics?.recommendedMatchCount ?? '--'} | ${item.metrics?.recommendedSelectionCount ?? '--'} | ${decimal(item.metrics?.netProfit)} | ${decimal(item.metrics?.volatility)} | ${action} |`
    })
  const samplingConstraint = report.options.strictSamplingBand
    ? `- 每套方案采样率最多比原方案降低 ${(report.options.samplingTolerance * 100).toFixed(2)} 个百分点，且不得高于原方案`
    : '- 默认稳健/激进采样率下限为60.00%/50.00%；欧洲杯、芬超、韩职仅本届使用现行专项下限，且激进不得高于稳健'
  const targetScope = report.options.targetRanges?.length > 0
    ? `- 本次优化范围：${report.options.targetRanges.join('、')}`
    : '- 本次检查全部赛事的仅本届与含上届方案'
  return [
    '# 共享界面口径赛事方案参数检查与重优化报告',
    '',
    `- 生成时间：${report.completedAt}`,
    `- 配置状态：${report.applied ? `已应用（${report.appliedAt || report.completedAt}）` : '未应用'}`,
    `- 最终模拟次数：${FINAL_SIMULATIONS.toLocaleString('en-US')}`,
    '- 正式比赛权重下限：1.00',
    '- 稳健方案ROI下限：5.00%',
    '- 激进方案ROI必须严格高于同赛事同时段稳健方案',
    `- 时间留出：按日期升序，前${percent(1 - report.options.validationFraction)}训练、后${percent(report.options.validationFraction)}验证，同日比赛不跨分区`,
    `- 最少训练/验证样本：${report.options.minimumTrainingMatches}/${report.options.minimumValidationMatches}，训练ROI下限${percent(report.options.minimumTrainingRoi)}，验证ROI下限${percent(report.options.minimumValidationRoi)}`,
    '- 候选只按训练集ROI排序，验证集仅作通过或拒绝门禁',
    '- 无法通过稳健门禁的方案关闭，不产生投注推荐',
    samplingConstraint,
    '- 激进方案采样率和ROI均高于稳健方案时，将激进参数移植给稳健方案，再优化激进方案',
    targetScope,
    '- 验收逐赛事使用与界面相同的异步回测接口和共享推荐计算模块',
    '',
    '| 赛事时段 | 方案 | 赔率样本 | 训练样本 | 验证样本 | 原采样率 | 新采样率 | 变化百分点 | 训练ROI | 验证ROI | 全量ROI | 命中率 | 推荐场次 | 投注数 | 净收益 | 波动率 | 处理方式 |',
    '| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- |',
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

async function applyVerifiedCheckpoint() {
  const [configText, checkpointText, reportText] = await Promise.all([
    fs.readFile(CONFIG_PATH, 'utf8'),
    fs.readFile(CHECKPOINT_PATH, 'utf8'),
    fs.readFile(REPORT_JSON_PATH, 'utf8')
  ])
  const config = JSON.parse(configText)
  const checkpoint = JSON.parse(checkpointText)
  const report = JSON.parse(reportText)
  if (report.applied) {
    throw new Error('verified report has already been applied')
  }
  if (!Array.isArray(report.violations) || report.violations.length > 0) {
    throw new Error('verified report contains constraint violations')
  }
  if (
    !Array.isArray(report.verification) ||
    report.verification.length !== allCompetitionRanges().length * 2
  ) {
    throw new Error('verified report does not cover every competition range and preset')
  }
  if (
    JSON.stringify(report.optimizationResults) !==
    JSON.stringify(checkpoint.optimizationResults)
  ) {
    throw new Error('checkpoint optimization results do not match the verified report')
  }
  const profileKeys = Object.keys(checkpoint.parameterProfiles || {})
  if (profileKeys.length !== COMPETITIONS.length * 2 * 2) {
    throw new Error(`checkpoint parameter profile count is invalid: ${profileKeys.length}`)
  }
  config.parameterProfiles = checkpoint.parameterProfiles
  process.stdout.write('saving independently verified checkpoint\n')
  const saved = await saveConfig(config)
  config.parameterProfiles = saved.parameterProfiles
  report.applied = true
  report.appliedAt = new Date().toISOString()
  await fs.writeFile(REPORT_JSON_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
  await fs.writeFile(REPORT_MARKDOWN_PATH, buildMarkdown(report), 'utf8')
  await writeCheckpoint({
    ...checkpoint,
    appliedAt: report.appliedAt,
    parameterProfiles: config.parameterProfiles
  })
  process.stdout.write(`applied verified report=${REPORT_MARKDOWN_PATH}\n`)
}

async function main() {
  if (APPLY_VERIFIED_CHECKPOINT) {
    await applyVerifiedCheckpoint()
    return
  }
  const config = JSON.parse(await fs.readFile(CONFIG_PATH, 'utf8'))
  let baselineVerification = null
  let optimizationResults = []
  if (RESUME_FROM_CHECKPOINT) {
    try {
      const checkpoint = JSON.parse(await fs.readFile(CHECKPOINT_PATH, 'utf8'))
      baselineVerification = Array.isArray(checkpoint.baselineVerification)
        ? checkpoint.baselineVerification
        : null
      optimizationResults = Array.isArray(checkpoint.optimizationResults)
        ? checkpoint.optimizationResults.filter(item => (
            item.status !== 'CONSTRAINT_FAILED' &&
            !RETRY_RANGES.has(`${item.competition}:${item.range}`)
          ))
        : []
      if (checkpoint.parameterProfiles) {
        config.parameterProfiles = checkpoint.parameterProfiles
      }
      process.stdout.write(`resumed ${optimizationResults.length}/${allCompetitionRanges().length} ranges\n`)
    } catch (error) {
      if (error?.code !== 'ENOENT') {
        throw error
      }
    }
  }

  if (!baselineVerification) {
    process.stdout.write('auditing current profiles with UI backtest jobs\n')
    baselineVerification = await verifyAllProfiles(config)
  }
  const ranges = allCompetitionRanges()
  const optimizationPlan = ranges.map(rangeItem => ({
    ...rangeItem,
    ...determineOptimizationAction(rangeItem, baselineVerification)
  }))
  process.stdout.write('\noptimization plan\n')
  optimizationPlan.forEach(item => {
    process.stdout.write(
      `  ${item.competitionName}:${item.range} ${item.action}` +
      `${item.reasons.length > 0 ? ` (${item.reasons.join('; ')})` : ''}` +
      ` stable=${percent(item.stable?.samplingRate)}/${percent(item.stable?.roi)}` +
      ` aggressive=${percent(item.aggressive?.samplingRate)}/${percent(item.aggressive?.roi)}\n`
    )
  })
  if (AUDIT_ONLY) {
    return
  }

  for (const planItem of optimizationPlan) {
    const { competition, competitionName, range, action, reasons } = planItem
    if (optimizationResults.some(item => item.competition === competition && item.range === range)) {
      continue
    }
    process.stdout.write(`\n[${optimizationResults.length + 1}/${ranges.length}] ${competitionName}:${range} ${action}\n`)
    const stableKey = `${competition}:${range}:STABLE`
    const aggressiveKey = `${competition}:${range}:AGGRESSIVE`
    let result = {
      competition,
      competitionName,
      range,
      action,
      reasons,
      status: 'UNCHANGED',
      samplingPolicy: 'UNCHANGED',
      baseline: {
        stable: planItem.stable,
        aggressive: planItem.aggressive
      },
      optimized: null
    }

    if (action === 'NO_DATA') {
      result.status = 'NO_DATA'
      result.samplingPolicy = null
    } else if (action === 'INSUFFICIENT_VALIDATION_SAMPLE') {
      config.parameterProfiles[stableKey] = closedProfile(config.parameterProfiles[stableKey])
      config.parameterProfiles[aggressiveKey] = closedProfile(config.parameterProfiles[aggressiveKey])
      result.status = 'CLOSED_INSUFFICIENT_VALIDATION_SAMPLE'
      result.samplingPolicy = 'CLOSED_INSUFFICIENT_VALIDATION_SAMPLE'
    } else if (action === 'FALLBACK_TO_PREVIOUS') {
      const previousStableKey = `${competition}:PREVIOUS:STABLE`
      const previousAggressiveKey = `${competition}:PREVIOUS:AGGRESSIVE`
      config.parameterProfiles[stableKey] = normalizeProfile(config.parameterProfiles[previousStableKey])
      config.parameterProfiles[aggressiveKey] = normalizeProfile(config.parameterProfiles[previousAggressiveKey])
      const fallbackEvaluations = {}
      for (const preset of ['STABLE', 'AGGRESSIVE']) {
        const key = `${competition}:${range}:${preset}`
        const profile = config.parameterProfiles[key]
        const backtest = await fetchBacktest(
          competition,
          range,
          profile.modelFactors,
          FINAL_SIMULATIONS,
          profile.modelFactors.handicapSmoothingFactor
        )
        fallbackEvaluations[preset.toLowerCase()] = candidateFrom(
          profile,
          evaluateProfile(backtest, profile)
        )
      }
      if (isFullSamplePairValid(
        fallbackEvaluations.stable.metrics,
        fallbackEvaluations.aggressive.metrics,
        competition,
        range
      )) {
        result.status = 'FALLBACK_TO_PREVIOUS'
        result.samplingPolicy = 'PREVIOUS_PROFILE_OUT_OF_SAMPLE_FALLBACK'
        result.optimized = fallbackEvaluations
      } else {
        config.parameterProfiles[stableKey] = closedProfile(config.parameterProfiles[stableKey])
        config.parameterProfiles[aggressiveKey] = closedProfile(config.parameterProfiles[aggressiveKey])
        result.status = 'CLOSED_INSUFFICIENT_VALIDATION_SAMPLE'
        result.samplingPolicy = 'CLOSED_AFTER_PREVIOUS_PROFILE_GATE_FAILED'
        result.diagnostics = { fallbackEvaluations }
      }
    } else if (action === 'TRANSFER_AND_REOPTIMIZE_AGGRESSIVE') {
      const migratedStableProfile = normalizeProfile(config.parameterProfiles[aggressiveKey])
      const aggressiveResult = await optimizePreset(
        competition,
        range,
        'AGGRESSIVE',
        config.parameterProfiles[aggressiveKey],
        {
          samplingBounds: samplingBoundsForProfile(
            competition,
            range,
            'AGGRESSIVE',
            planItem.aggressive.samplingRate
          )
        }
      )
      const migratedStable = candidateFrom(
        migratedStableProfile,
        aggressiveResult.baselineMetrics || planItem.aggressive,
        aggressiveResult.baselineRobustness || planItem.aggressiveVerification?.robustness
      )
      const aggressiveBounds = samplingBoundsForProfile(
        competition,
        range,
        'AGGRESSIVE',
        planItem.aggressive.samplingRate
      )
      const aggressive = chooseAggressiveCandidate(
        aggressiveResult.pool,
        migratedStable,
        competition,
        range,
        aggressiveBounds
      )
      if (!aggressive) {
        const bestInWindow = bestAggressiveInSamplingWindow(
          aggressiveResult.pool,
          migratedStable,
          competition,
          range,
          aggressiveBounds
        )
        result.status = 'CONSTRAINT_FAILED'
        result.samplingPolicy = null
        result.diagnostics = { bestAggressiveInSamplingWindow: bestInWindow }
        if (bestInWindow) {
          process.stdout.write(
            `  best in sampling window=${percent(bestInWindow.metrics.samplingRate)}` +
            `/${percent(bestInWindow.metrics.roi)} required>${percent(migratedStable.metrics.roi)}\n`
          )
        }
      } else {
        config.parameterProfiles[stableKey] = migratedStable.profile
        config.parameterProfiles[aggressiveKey] = aggressive.profile
        result.status = 'OPTIMIZED'
        result.samplingPolicy = 'MIGRATE_AGGRESSIVE_TO_STABLE_AND_LOWER_AGGRESSIVE'
        result.optimized = { stable: migratedStable, aggressive }
      }
    } else if (action === 'REOPTIMIZE_AGGRESSIVE') {
      const stable = candidateFrom(
        normalizeProfile(config.parameterProfiles[stableKey]),
        planItem.stable,
        planItem.stableVerification?.robustness
      )
      const aggressiveBounds = samplingBoundsForProfile(
        competition,
        range,
        'AGGRESSIVE',
        planItem.aggressive.samplingRate
      )
      const aggressiveResult = await optimizePreset(
        competition,
        range,
        'AGGRESSIVE',
        config.parameterProfiles[aggressiveKey],
        { samplingBounds: aggressiveBounds }
      )
      const aggressive = chooseAggressiveCandidate(
        aggressiveResult.pool,
        stable,
        competition,
        range,
        aggressiveBounds
      )
      if (!aggressive) {
        process.stdout.write('  aggressive-only search failed, retrying as a pair\n')
        const stableResult = await optimizePreset(
          competition,
          range,
          'STABLE',
          config.parameterProfiles[stableKey]
        )
        const pair = chooseReoptimizedPair(stableResult, aggressiveResult, competition, range)
        if (!pair) {
          const bestInWindow = bestAggressiveInSamplingWindow(
            aggressiveResult.pool,
            stable,
            competition,
            range,
            aggressiveBounds
          )
          result.status = 'CONSTRAINT_FAILED'
          result.samplingPolicy = null
          result.diagnostics = { bestAggressiveInSamplingWindow: bestInWindow }
        } else {
          config.parameterProfiles[stableKey] = pair.stable.profile
          config.parameterProfiles[aggressiveKey] = pair.aggressive.profile
          result.action = 'REOPTIMIZE_PAIR'
          result.reasons = reasons.concat('仅重做激进方案无可行解，改为成对重优化')
          result.status = 'OPTIMIZED'
          result.samplingPolicy = pair.samplingPolicy
          result.optimized = { stable: pair.stable, aggressive: pair.aggressive }
        }
      } else {
        config.parameterProfiles[aggressiveKey] = aggressive.profile
        result.status = 'OPTIMIZED'
        result.samplingPolicy = 'AGGRESSIVE_ROBUST_VALIDATED'
        result.optimized = { stable, aggressive }
      }
    } else if (action === 'REOPTIMIZE_PAIR') {
      const stableResult = await optimizePreset(
        competition,
        range,
        'STABLE',
        config.parameterProfiles[stableKey],
        {
          samplingBounds: samplingBoundsForProfile(
            competition,
            range,
            'STABLE',
            planItem.stable.samplingRate
          )
        }
      )
      const aggressiveResult = await optimizePreset(
        competition,
        range,
        'AGGRESSIVE',
        config.parameterProfiles[aggressiveKey],
        {
          samplingBounds: samplingBoundsForProfile(
            competition,
            range,
            'AGGRESSIVE',
            planItem.aggressive.samplingRate
          )
        }
      )
      const pair = chooseReoptimizedPair(stableResult, aggressiveResult, competition, range)
      if (!pair) {
        config.parameterProfiles[stableKey] = closedProfile(config.parameterProfiles[stableKey])
        config.parameterProfiles[aggressiveKey] = closedProfile(config.parameterProfiles[aggressiveKey])
        result.status = 'CLOSED_CONSTRAINTS_NOT_MET'
        result.samplingPolicy = 'CLOSED_ROBUST_CONSTRAINTS_NOT_MET'
        result.diagnostics = {
          stablePool: summarizeRobustPool(stableResult.pool, competition, range, 'STABLE'),
          aggressivePool: summarizeRobustPool(aggressiveResult.pool, competition, range, 'AGGRESSIVE')
        }
      } else {
        config.parameterProfiles[stableKey] = pair.stable.profile
        config.parameterProfiles[aggressiveKey] = pair.aggressive.profile
        result.status = 'OPTIMIZED'
        result.samplingPolicy = pair.samplingPolicy
        result.optimized = { stable: pair.stable, aggressive: pair.aggressive }
      }
    }

    optimizationResults.push(result)
    if (result.status === 'OPTIMIZED') {
      process.stdout.write(
        `  selected stable=${percent(result.optimized.stable.metrics.samplingRate)}` +
        `/${percent(result.optimized.stable.metrics.roi)}` +
        ` aggressive=${percent(result.optimized.aggressive.metrics.samplingRate)}` +
        `/${percent(result.optimized.aggressive.metrics.roi)}\n`
      )
    } else {
      process.stdout.write(`  status=${result.status}\n`)
    }
    await writeCheckpoint({
      generatedAt: new Date().toISOString(),
      baselineVerification,
      optimizationResults,
      parameterProfiles: config.parameterProfiles
    })
  }

  const constraintFailures = optimizationResults.filter(item => item.status === 'CONSTRAINT_FAILED')
  if (constraintFailures.length > 0) {
    const keys = constraintFailures.map(item => `${item.competition}:${item.range}`).join(', ')
    throw new Error(`unable to satisfy profile constraints: ${keys}`)
  }

  const verification = await verifyAllProfiles(config)
  const violations = findVerificationViolations(
    config,
    verification,
    baselineVerification,
    optimizationResults
  )
  let applied = false
  if (violations.length === 0 && APPLY_RESULTS) {
    process.stdout.write('saving user config after verification\n')
    const saved = await saveConfig(config)
    config.parameterProfiles = saved.parameterProfiles
    applied = true
  }
  const report = {
    generatedAt: new Date().toISOString(),
    completedAt: new Date().toISOString(),
    applied,
    options: {
      finalSimulations: FINAL_SIMULATIONS,
      coarseSimulations: COARSE_SIMULATIONS,
      modelVariantCount: MODEL_VARIANT_COUNT,
      finalistModelCount: FINALIST_MODEL_COUNT,
      coarseRuleCandidates: COARSE_RULE_CANDIDATES,
      finalRuleCandidates: FINAL_RULE_CANDIDATES,
      refinementPasses: REFINEMENT_PASSES,
      samplingTolerance: SAMPLING_TOLERANCE,
      samplingMaxIncrease: SAMPLING_MAX_INCREASE,
      strictSamplingBand: STRICT_SAMPLING_BAND,
      minimumStableRoi: MINIMUM_STABLE_ROI,
      minimumAggressiveSamplingRate: MINIMUM_AGGRESSIVE_SAMPLING_RATE,
      minimumAggressiveRoiGap: MINIMUM_AGGRESSIVE_ROI_GAP,
      robustValidation: ROBUST_VALIDATION,
      validationFraction: VALIDATION_FRACTION,
      minimumTrainingMatches: MINIMUM_TRAINING_MATCHES,
      minimumValidationMatches: MINIMUM_VALIDATION_MATCHES,
      minimumTrainingRoi: MINIMUM_TRAINING_ROI,
      minimumValidationRoi: MINIMUM_VALIDATION_ROI,
      reoptimizeAll: REOPTIMIZE_ALL,
      targetRanges: [...TARGET_RANGES]
    },
    baselineVerification,
    optimizationResults,
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
