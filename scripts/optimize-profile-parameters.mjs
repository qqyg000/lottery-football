import { mkdir, writeFile } from 'node:fs/promises'
import path from 'node:path'

import { calculateFlatStakeBacktest } from '../frontend/src/backtest-roi.mjs'

const API_ROOT = 'http://localhost:8080/api/football'
const BACKTEST_URL = API_ROOT + '/recommendation-backtest'
const USER_CONFIG_URL = API_ROOT + '/user-config'
const OUTPUT_DIRECTORY = path.resolve('target')
const REPORT_PATH = path.join(OUTPUT_DIRECTORY, 'profile-optimization-report.json')
const VERIFICATION_REPORT_PATH = path.join(OUTPUT_DIRECTORY, 'profile-verification-report.json')
const BEFORE_CONFIG_PATH = path.join(OUTPUT_DIRECTORY, 'user-config-before-profile-optimization.json')
const PROBABILITY_MASKS = [1, 2, 4]
const MODEL_FACTOR_MIN = 0.1
const MODEL_FACTOR_MAX = 3
const SMOOTHING_MIN = 0
const SMOOTHING_MAX = 0.8
const ROI_EPSILON = 1e-12
const MINIMUM_STABLE_ROI = 0.075
const MINIMUM_AGGRESSIVE_ROI = 0.15
const MINIMUM_AGGRESSIVE_ROI_GAP = 0.0001
const PARAMETER_PRESETS = ['STABLE', 'AGGRESSIVE']

const COMPETITIONS = [
  { code: 'WORLD_CUP', name: '世界杯', currentStartDate: '2026-06-11' },
  { code: 'EUROPEAN_CHAMPIONSHIP', name: '欧洲杯', currentStartDate: '2028-06-09' },
  { code: 'COPA_AMERICA', name: '美洲杯', currentStartDate: '2028-06-09' },
  { code: 'CLUB_WORLD_CUP', name: '世俱杯', currentStartDate: '2028-06-09' },
  { code: 'EUROPA_LEAGUE', name: '欧罗巴', currentStartDate: '2026-09-16' },
  { code: 'CHAMPIONS_LEAGUE', name: '欧冠', currentStartDate: '2026-09-08' },
  { code: 'PREMIER_LEAGUE', name: '英超', currentStartDate: '2026-08-21' },
  { code: 'LA_LIGA', name: '西甲', currentStartDate: '2026-08-15' },
  { code: 'SERIE_A', name: '意甲', currentStartDate: '2026-08-21' },
  { code: 'BUNDESLIGA', name: '德甲', currentStartDate: '2026-08-28' },
  { code: 'LIGUE_1', name: '法甲', currentStartDate: '2026-08-21' },
  { code: 'BRAZIL_SERIE_A', name: '巴甲', currentStartDate: '2026-01-28' },
  { code: 'PRIMEIRA_LIGA', name: '葡超', currentStartDate: '2026-08-08' },
  { code: 'EREDIVISIE', name: '荷甲', currentStartDate: '2026-08-07' },
  { code: 'ARGENTINE_PRIMERA_DIVISION', name: '阿甲', currentStartDate: '2026-01-22' }
]

const argumentsMap = new Map(
  process.argv.slice(2).map(argument => {
    const [key, value = 'true'] = argument.replace(/^--/, '').split('=', 2)
    return [key, value]
  })
)
const coarseSimulations = numberArgument('coarse-simulations', 3000, 1000, 500000)
const refineSimulations = numberArgument('refine-simulations', 10000, 1000, 500000)
const finalSimulations = numberArgument('final-simulations', 50000, 1000, 500000)
const quickCandidateCount = numberArgument('quick-candidates', 2500, 100, 100000)
const refineCandidateCount = numberArgument('refine-candidates', 5000, 100, 200000)
const finalCandidateCount = numberArgument('final-candidates', 60000, 1000, 1000000)
const applyResults = argumentsMap.get('apply') !== 'false'
const requestedProfile = argumentsMap.get('profile') || ''
const verifyOnly = argumentsMap.get('verify-only') === 'true'
const verboseRequests = argumentsMap.get('verbose-requests') === 'true'
const requestCache = new Map()

function numberArgument(name, fallback, minimum, maximum) {
  const value = Number(argumentsMap.get(name))
  return Number.isFinite(value)
    ? Math.max(minimum, Math.min(maximum, Math.round(value)))
    : fallback
}

function todayInShanghai() {
  return new Intl.DateTimeFormat('sv-SE', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(new Date())
}

function baseProfileKey(competition, range) {
  return competition + ':' + range
}

function profileKey(competition, range, preset) {
  return baseProfileKey(competition, range) + ':' + preset
}

function targetLabel(target) {
  return target.preset
    ? profileKey(target.code, target.range, target.preset)
    : baseProfileKey(target.code, target.range)
}

function buildTargets() {
  const today = todayInShanghai()
  const targets = []
  COMPETITIONS.forEach(competition => {
    targets.push({ ...competition, range: 'PREVIOUS', includePreviousEdition: true })
    if (competition.currentStartDate <= today) {
      targets.push({ ...competition, range: 'CURRENT', includePreviousEdition: false })
    }
  })
  return targets.filter(target => {
    const baseKey = baseProfileKey(target.code, target.range)
    return !requestedProfile || requestedProfile === baseKey || requestedProfile.startsWith(baseKey + ':')
  })
}

function round(value, scale) {
  const multiplier = 10 ** scale
  return Math.round((value + Number.EPSILON) * multiplier) / multiplier
}

function clamp(value, minimum, maximum) {
  return Math.max(minimum, Math.min(maximum, value))
}

function range(minimum, maximum, step, scale = 2) {
  const values = []
  const multiplier = 10 ** scale
  const start = Math.round(minimum * multiplier)
  const end = Math.round(maximum * multiplier)
  const increment = Math.max(1, Math.round(step * multiplier))
  for (let value = start; value <= end; value += increment) {
    values.push(value / multiplier)
  }
  if (values[values.length - 1] !== maximum) {
    values.push(maximum)
  }
  return values
}

function uniqueNumbers(values, scale = 2) {
  return [...new Set(values.map(value => round(value, scale)))]
    .sort((left, right) => left - right)
}

function createRandom(seed) {
  let state = seed >>> 0
  return () => {
    state = (state * 1664525 + 1013904223) >>> 0
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

function randomBetween(random, minimum, maximum, scale) {
  return round(minimum + random() * (maximum - minimum), scale)
}

function pick(random, values) {
  return values[Math.min(values.length - 1, Math.floor(random() * values.length))]
}

function toProbabilityArray(probability) {
  return [
    Number(probability?.win) || 0,
    Number(probability?.draw) || 0,
    Number(probability?.lose) || 0
  ]
}

function findHandicapProbability(match) {
  const handicap = Number(match.sportteryHandicap)
  const source = Array.isArray(match.adjustedHandicapProbabilities)
    ? match.adjustedHandicapProbabilities
    : []
  const probability = source.find(item => Number(item.handicap) === handicap)?.probability
  return probability ? toProbabilityArray(probability) : [0, 0, 0]
}

function parseScore(scoreText) {
  const result = String(scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
  return result ? [Number(result[1]), Number(result[2])] : null
}

function actualProbabilityIndex(score, handicap) {
  if (!score) {
    return -1
  }
  const adjustedHomeScore = score[0] + handicap
  if (adjustedHomeScore > score[1]) {
    return 0
  }
  return adjustedHomeScore === score[1] ? 1 : 2
}

function toOddsArray(odds) {
  return ['win', 'draw', 'lose'].map(key => {
    const value = Number(odds?.[key])
    return Number.isFinite(value) && value > 0 ? value : null
  })
}

function prepareMatches(matches) {
  return (matches || []).map(match => {
    const handicap = Number(match.sportteryHandicap)
    const score = parseScore(match.scoreText)
    return {
      matchId: match.matchId,
      matchDate: match.matchDate,
      competition: match.competition,
      normalAvailable: match.sportteryNormalAvailable === true,
      handicapAvailable: Number.isInteger(handicap) && handicap !== 0,
      handicap,
      normalProbability: toProbabilityArray(match.adjustedNormalProbability),
      rawHandicapProbability: findHandicapProbability(match),
      normalOdds: toOddsArray(match.sportteryNormalOdds),
      handicapOdds: toOddsArray(match.sportteryHandicapOdds),
      normalActualIndex: actualProbabilityIndex(score, 0),
      handicapActualIndex: actualProbabilityIndex(score, handicap)
    }
  })
}

function smoothHandicapProbability(match, smoothingFactor) {
  const rawWeight = 1 - smoothingFactor
  const winValue = match.rawHandicapProbability[0] * rawWeight + match.normalProbability[0] * smoothingFactor
  const drawValue = match.rawHandicapProbability[1] * rawWeight + match.normalProbability[1] * smoothingFactor
  const loseValue = match.rawHandicapProbability[2] * rawWeight + match.normalProbability[2] * smoothingFactor
  const total = winValue + drawValue + loseValue
  if (total <= 0) {
    return [0, 0, 0]
  }
  const win = round(winValue * 100 / total, 2)
  const draw = round(drawValue * 100 / total, 2)
  return [win, draw, round(Math.max(0, 100 - win - draw), 2)]
}

function recommendationForMatch(match, parameters) {
  const handicapProbability = smoothHandicapProbability(match, parameters.handicapSmoothingFactor)
  let maxType = ''
  let maxProbabilityIndex = -1
  let maxValue = -1

  function inspectRow(type, probability) {
    probability.forEach((value, probabilityIndex) => {
      if (value > maxValue) {
        maxType = type
        maxProbabilityIndex = probabilityIndex
        maxValue = value
      }
    })
  }

  if (match.handicap > 0 && match.handicapAvailable) {
    inspectRow('handicap', handicapProbability)
  }
  if (match.normalAvailable) {
    inspectRow('normal', match.normalProbability)
  }
  if (match.handicap < 0 && match.handicapAvailable) {
    inspectRow('handicap', handicapProbability)
  }
  if (!maxType) {
    return null
  }

  let recommendationType = maxType
  let mask = 0
  if (match.normalAvailable && match.handicapAvailable && maxType === 'normal' && maxProbabilityIndex !== 1) {
    const handicapValue = handicapProbability[maxProbabilityIndex]
    if (handicapValue >= parameters.handicapRecommendationThreshold && handicapValue < maxValue) {
      recommendationType = 'handicap'
      mask = PROBABILITY_MASKS[maxProbabilityIndex] | PROBABILITY_MASKS[1]
    }
  }
  if (mask === 0 && match.normalAvailable && match.handicapAvailable &&
    maxType === 'handicap' && maxProbabilityIndex !== 1 && maxValue < parameters.handicapReverseThreshold) {
    mask = PROBABILITY_MASKS.reduce((result, probabilityMask, probabilityIndex) => {
      return probabilityIndex === maxProbabilityIndex ? result : result | probabilityMask
    }, 0)
  }
  if (mask === 0) {
    mask = maxProbabilityIndex === 1
      ? PROBABILITY_MASKS[1]
      : PROBABILITY_MASKS[maxProbabilityIndex] | PROBABILITY_MASKS[1]
  }

  const recommendationProbability = recommendationType === 'handicap'
    ? handicapProbability
    : match.normalProbability
  let selectionCount = PROBABILITY_MASKS.filter(probabilityMask => (mask & probabilityMask) !== 0).length
  if (selectionCount === 2) {
    let strongestIndex = -1
    let strongestValue = -1
    recommendationProbability.forEach((value, probabilityIndex) => {
      if ((mask & PROBABILITY_MASKS[probabilityIndex]) !== 0 && value > strongestValue) {
        strongestIndex = probabilityIndex
        strongestValue = value
      }
    })
    if (strongestValue > parameters.singleRecommendationThreshold) {
      mask = PROBABILITY_MASKS[strongestIndex]
      selectionCount = 1
    }
  }

  const odds = recommendationType === 'handicap' ? match.handicapOdds : match.normalOdds
  for (let probabilityIndex = 0; probabilityIndex < PROBABILITY_MASKS.length; probabilityIndex++) {
    if ((mask & PROBABILITY_MASKS[probabilityIndex]) === 0) {
      continue
    }
    if (odds[probabilityIndex] === null || odds[probabilityIndex] < parameters.recommendationOdds) {
      return null
    }
  }
  return {
    mask,
    selectionCount,
    actualIndex: recommendationType === 'handicap'
      ? match.handicapActualIndex
      : match.normalActualIndex,
    odds
  }
}

function evaluate(matches, parameters) {
  let recommendedMatchCount = 0
  let recommendedSelectionCount = 0
  let hitMatchCount = 0
  const winningMatchReturns = []
  matches.forEach(match => {
    const recommendation = recommendationForMatch(match, parameters)
    if (!recommendation) {
      return
    }
    recommendedMatchCount++
    recommendedSelectionCount += recommendation.selectionCount
    const actualMask = recommendation.actualIndex >= 0
      ? PROBABILITY_MASKS[recommendation.actualIndex]
      : 0
    if ((recommendation.mask & actualMask) === 0) {
      return
    }
    hitMatchCount++
    winningMatchReturns.push(recommendation.odds[recommendation.actualIndex] || 0)
  })
  const missMatchCount = recommendedMatchCount - hitMatchCount
  const averageRecommendations = recommendedMatchCount > 0
    ? recommendedSelectionCount / recommendedMatchCount
    : 0
  const hitRate = recommendedMatchCount > 0 ? hitMatchCount / recommendedMatchCount : 0
  const financials = calculateFlatStakeBacktest(
    winningMatchReturns,
    recommendedSelectionCount,
    recommendedMatchCount
  )
  return {
    sampleCount: matches.length,
    recommendedMatchCount,
    recommendedSelectionCount,
    hitMatchCount,
    missMatchCount,
    settledMatchCount: recommendedMatchCount,
    averageRecommendations,
    hitRate,
    averageOddsIncludingMisses: financials.averageReturnIncludingMisses ?? 0,
    totalStake: financials.totalStake,
    totalReturn: financials.totalReturn,
    netProfit: financials.netProfit,
    roi: financials.roi ?? -1
  }
}

function minimumRecommendedMatches(sampleCount) {
  return sampleCount > 0 ? 1 : 0
}

function createObjective(preset, minimumRoiExclusive) {
  return {
    preset,
    minimumRoiExclusive,
    isEligible(metrics) {
      return metrics.recommendedMatchCount > 0 &&
        metrics.hitMatchCount > 0 &&
        Number.isFinite(metrics.roi) &&
        metrics.roi > minimumRoiExclusive + ROI_EPSILON
    }
  }
}

function aggressiveMinimumRoiExclusive(stableRoi) {
  return Math.max(
    MINIMUM_AGGRESSIVE_ROI,
    stableRoi + MINIMUM_AGGRESSIVE_ROI_GAP - ROI_EPSILON
  )
}

function isBetter(left, right, objective) {
  if (!left || !objective.isEligible(left.metrics)) {
    return false
  }
  if (!right || !objective.isEligible(right.metrics)) {
    return true
  }
  if (left.metrics.recommendedMatchCount !== right.metrics.recommendedMatchCount) {
    return left.metrics.recommendedMatchCount > right.metrics.recommendedMatchCount
  }
  if (Math.abs(left.metrics.roi - right.metrics.roi) > ROI_EPSILON) {
    return left.metrics.roi > right.metrics.roi
  }
  if (left.metrics.recommendedSelectionCount !== right.metrics.recommendedSelectionCount) {
    return left.metrics.recommendedSelectionCount > right.metrics.recommendedSelectionCount
  }
  return left.metrics.hitRate > right.metrics.hitRate + ROI_EPSILON
}

function compareResults(left, right, objective) {
  if (isBetter(left, right, objective)) {
    return -1
  }
  if (isBetter(right, left, objective)) {
    return 1
  }
  return 0
}

function addRankedResult(results, candidate, metrics, objective, limit = 10) {
  if (!objective.isEligible(metrics)) {
    return
  }
  results.push({ candidate, metrics })
  results.sort((left, right) => compareResults(left, right, objective))
  if (results.length > limit) {
    results.length = limit
  }
}

function observedOdds(matches) {
  const values = [1]
  matches.forEach(match => {
    match.normalOdds.concat(match.handicapOdds).forEach(value => {
      if (value !== null && value >= 1 && value <= 100) {
        values.push(value)
      }
    })
  })
  return uniqueNumbers(values, 2)
}

function normalizeParameters(parameters) {
  return {
    hostTeamGoalFactor: round(clamp(Number(parameters.hostTeamGoalFactor), MODEL_FACTOR_MIN, MODEL_FACTOR_MAX), 2),
    homeTeamGoalFactor: round(clamp(Number(parameters.homeTeamGoalFactor), MODEL_FACTOR_MIN, MODEL_FACTOR_MAX), 2),
    seedTeamGoalFactor: round(clamp(Number(parameters.seedTeamGoalFactor), MODEL_FACTOR_MIN, MODEL_FACTOR_MAX), 2),
    handicapSmoothingFactor: round(clamp(Number(parameters.handicapSmoothingFactor), SMOOTHING_MIN, SMOOTHING_MAX), 3),
    recommendationOdds: round(clamp(Number(parameters.recommendationOdds), 1, 100), 2),
    handicapRecommendationThreshold: round(clamp(Number(parameters.handicapRecommendationThreshold), 0, 100), 2),
    handicapReverseThreshold: round(clamp(Number(parameters.handicapReverseThreshold), 0, 100), 2),
    singleRecommendationThreshold: round(clamp(Number(parameters.singleRecommendationThreshold), 0, 100), 2)
  }
}

function flattenProfile(profile) {
  return normalizeParameters({
    ...profile.modelFactors,
    ...profile.globalParameters
  })
}

function expandProfile(parameters) {
  const normalized = normalizeParameters(parameters)
  return {
    modelFactors: {
      hostTeamGoalFactor: normalized.hostTeamGoalFactor,
      homeTeamGoalFactor: normalized.homeTeamGoalFactor,
      seedTeamGoalFactor: normalized.seedTeamGoalFactor,
      handicapSmoothingFactor: normalized.handicapSmoothingFactor
    },
    globalParameters: {
      recommendationOdds: normalized.recommendationOdds,
      handicapRecommendationThreshold: normalized.handicapRecommendationThreshold,
      handicapReverseThreshold: normalized.handicapReverseThreshold,
      singleRecommendationThreshold: normalized.singleRecommendationThreshold
    }
  }
}

function buildRandomCandidates(matches, modelFactors, center, count, seed, localRatio = 0.65) {
  const random = createRandom(seed)
  const allOdds = observedOdds(matches)
  const localOdds = allOdds.filter(value => Math.abs(value - center.recommendationOdds) <= 0.75)
  const candidates = [normalizeParameters({ ...center, ...modelFactors })]
  for (let index = 0; index < count; index++) {
    const local = random() < localRatio
    const oddsValues = local && localOdds.length > 0 ? localOdds : allOdds
    candidates.push(normalizeParameters({
      ...modelFactors,
      handicapSmoothingFactor: local
        ? randomBetween(random, Math.max(0, center.handicapSmoothingFactor - 0.18), Math.min(0.8, center.handicapSmoothingFactor + 0.18), 3)
        : randomBetween(random, 0, 0.8, 3),
      recommendationOdds: pick(random, oddsValues),
      handicapRecommendationThreshold: local
        ? randomBetween(random, Math.max(0, center.handicapRecommendationThreshold - 20), Math.min(100, center.handicapRecommendationThreshold + 20), 2)
        : randomBetween(random, 0, 100, 2),
      handicapReverseThreshold: local
        ? randomBetween(random, Math.max(0, center.handicapReverseThreshold - 20), Math.min(100, center.handicapReverseThreshold + 20), 2)
        : randomBetween(random, 0, 100, 2),
      singleRecommendationThreshold: local
        ? randomBetween(random, Math.max(0, center.singleRecommendationThreshold - 20), Math.min(100, center.singleRecommendationThreshold + 20), 2)
        : randomBetween(random, 0, 100, 2)
    }))
  }
  return candidates
}

function findBestCandidate(matches, candidates, objective) {
  let best = null
  candidates.forEach(candidate => {
    const metrics = evaluate(matches, candidate)
    const result = { candidate, metrics }
    if (isBetter(result, best, objective)) {
      best = result
    }
  })
  return best
}

function bestCoordinateValue(matches, current, key, values, objective) {
  let best = current
  values.forEach(value => {
    if (value === current.candidate[key]) {
      return
    }
    const candidate = normalizeParameters({ ...current.candidate, [key]: value })
    const metrics = evaluate(matches, candidate)
    const result = { candidate, metrics }
    if (isBetter(result, best, objective)) {
      best = result
    }
  })
  return best
}

function coordinateOptimize(matches, initial, objective) {
  let current = { candidate: normalizeParameters(initial), metrics: evaluate(matches, initial) }
  const oddsValues = observedOdds(matches)
  for (let pass = 0; pass < 3; pass++) {
    const before = current
    current = bestCoordinateValue(matches, current, 'handicapSmoothingFactor', range(0, 0.8, 0.01, 2), objective)
    current = bestCoordinateValue(matches, current, 'recommendationOdds', oddsValues, objective)
    current = bestCoordinateValue(matches, current, 'handicapRecommendationThreshold', range(0, 100, 1, 0), objective)
    current = bestCoordinateValue(matches, current, 'handicapReverseThreshold', range(0, 100, 1, 0), objective)
    current = bestCoordinateValue(matches, current, 'singleRecommendationThreshold', range(0, 100, 1, 0), objective)
    if (!isBetter(current, before, objective)) {
      break
    }
  }
  current = bestCoordinateValue(
    matches,
    current,
    'handicapSmoothingFactor',
    range(
      Math.max(0, current.candidate.handicapSmoothingFactor - 0.03),
      Math.min(0.8, current.candidate.handicapSmoothingFactor + 0.03),
      0.001,
      3
    ),
    objective
  )
  for (const key of ['handicapRecommendationThreshold', 'handicapReverseThreshold', 'singleRecommendationThreshold']) {
    current = bestCoordinateValue(
      matches,
      current,
      key,
      range(Math.max(0, current.candidate[key] - 2), Math.min(100, current.candidate[key] + 2), 0.05, 2),
      objective
    )
  }
  return current
}

function deepOptimize(matches, modelFactors, center, seed, objective) {
  const globalCandidates = buildRandomCandidates(
    matches,
    modelFactors,
    center,
    finalCandidateCount,
    seed,
    0.55
  )
  const ranked = []
  globalCandidates.forEach(candidate => {
    const metrics = evaluate(matches, candidate)
    addRankedResult(ranked, candidate, metrics, objective, 8)
  })
  if (ranked.length === 0) {
    return null
  }
  const coordinated = ranked.slice(0, 4).map(result => coordinateOptimize(matches, result.candidate, objective))
  coordinated.forEach(result => addRankedResult(ranked, result.candidate, result.metrics, objective, 8))
  const centerResult = ranked[0]
  const localCandidates = buildRandomCandidates(
    matches,
    modelFactors,
    centerResult.candidate,
    Math.max(5000, Math.floor(finalCandidateCount / 2)),
    seed + 1,
    0.92
  )
  const localBest = findBestCandidate(matches, localCandidates, objective)
  if (!localBest) {
    return centerResult
  }
  const refined = coordinateOptimize(matches, localBest.candidate, objective)
  return isBetter(refined, centerResult, objective) ? refined : centerResult
}

function modelFactorValues(center, step = 0.2) {
  return uniqueNumbers([
    ...range(MODEL_FACTOR_MIN, MODEL_FACTOR_MAX, step, 2),
    center
  ], 2)
}

function localModelFactorValues(centers, radius = 0.25, step = 0.05) {
  return uniqueNumbers(centers.flatMap(center => {
    return range(
      Math.max(MODEL_FACTOR_MIN, center - radius),
      Math.min(MODEL_FACTOR_MAX, center + radius),
      step,
      2
    )
  }), 2)
}

async function fetchJson(url, options = {}) {
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt++) {
    try {
      const response = await fetch(url, {
        ...options,
        signal: AbortSignal.timeout(300000)
      })
      if (!response.ok) {
        throw new Error(`${response.status} ${await response.text()}`)
      }
      return await response.json()
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        await new Promise(resolve => setTimeout(resolve, attempt * 500))
      }
    }
  }
  throw lastError
}

async function fetchBacktest(target, factors, simulations) {
  const normalizedFactors = normalizeParameters({
    ...factors,
    handicapSmoothingFactor: 0,
    recommendationOdds: 1,
    handicapRecommendationThreshold: 0,
    handicapReverseThreshold: 0,
    singleRecommendationThreshold: 0
  })
  const cacheKey = [
    target.code,
    target.range,
    simulations,
    normalizedFactors.hostTeamGoalFactor,
    normalizedFactors.homeTeamGoalFactor,
    normalizedFactors.seedTeamGoalFactor
  ].join('|')
  if (requestCache.has(cacheKey)) {
    return requestCache.get(cacheKey)
  }
  const url = new URL(BACKTEST_URL)
  url.searchParams.set('competition', target.code)
  url.searchParams.set('includePreviousEdition', String(target.includePreviousEdition))
  url.searchParams.set('simulations', simulations)
  url.searchParams.set('hostTeamGoalFactor', normalizedFactors.hostTeamGoalFactor)
  url.searchParams.set('homeTeamGoalFactor', normalizedFactors.homeTeamGoalFactor)
  url.searchParams.set('seedTeamGoalFactor', normalizedFactors.seedTeamGoalFactor)
  url.searchParams.set('handicapSmoothingFactor', 0)
  const startedAt = Date.now()
  const response = await fetchJson(url)
  const prepared = prepareMatches(response.matches)
  requestCache.set(cacheKey, prepared)
  if (verboseRequests) {
    process.stderr.write(
      `${targetLabel(target)} ${simulations} 次模拟，模型因子 ` +
      `${normalizedFactors.hostTeamGoalFactor}/${normalizedFactors.homeTeamGoalFactor}/${normalizedFactors.seedTeamGoalFactor}，` +
      `${prepared.length} 场，${((Date.now() - startedAt) / 1000).toFixed(1)} 秒\n`
    )
  }
  return prepared
}

async function rankFactorCandidates(
  target,
  original,
  factorCandidates,
  simulations,
  candidateCount,
  seed,
  objective
) {
  const ranked = []
  for (let index = 0; index < factorCandidates.length; index++) {
    const factors = factorCandidates[index]
    const matches = await fetchBacktest(target, factors, simulations)
    const candidates = buildRandomCandidates(
      matches,
      factors,
      original,
      candidateCount,
      seed + index,
      0.45
    )
    const best = findBestCandidate(matches, candidates, objective)
    if (best) {
      addRankedResult(ranked, best.candidate, best.metrics, objective, 8)
    }
    if ((index + 1) % 10 === 0 || index === factorCandidates.length - 1) {
      process.stderr.write(
        `${targetLabel(target)} 模型候选 ${index + 1}/${factorCandidates.length}` +
        `，当前最佳推荐 ${ranked[0]?.metrics.recommendedMatchCount ?? '--'} 场` +
        `，ROI ${ranked[0] ? round(ranked[0].metrics.roi * 100, 2) : '--'}%\n`
      )
    }
  }
  return ranked
}

async function optimizeNonWorldCupFactors(target, original, seed, objective) {
  const coarseFactors = modelFactorValues(original.homeTeamGoalFactor).map(homeTeamGoalFactor => ({
    ...original,
    homeTeamGoalFactor
  }))
  const coarseRanked = await rankFactorCandidates(
    target,
    original,
    coarseFactors,
    coarseSimulations,
    quickCandidateCount,
    seed,
    objective
  )
  const refineValues = localModelFactorValues(
    coarseRanked.slice(0, 4).map(result => result.candidate.homeTeamGoalFactor)
      .concat(original.homeTeamGoalFactor)
  )
  const refineFactors = refineValues.map(homeTeamGoalFactor => ({ ...original, homeTeamGoalFactor }))
  const refinedRanked = await rankFactorCandidates(
    target,
    original,
    refineFactors,
    refineSimulations,
    refineCandidateCount,
    seed + 1000,
    objective
  )
  return {
    coarseTop: coarseRanked.slice(0, 5),
    refinedTop: refinedRanked.slice(0, 3)
  }
}

async function optimizeWorldCupFactors(target, original, seed, objective) {
  const factorValues = modelFactorValues(original.seedTeamGoalFactor)
  const seedFactors = factorValues.map(seedTeamGoalFactor => ({
    ...original,
    seedTeamGoalFactor
  }))
  const seedRanked = await rankFactorCandidates(
    target,
    original,
    seedFactors,
    coarseSimulations,
    quickCandidateCount,
    seed,
    objective
  )
  const hostValues = modelFactorValues(original.hostTeamGoalFactor)
  const hostFactors = seedRanked.slice(0, 3).flatMap(result => {
    return hostValues.map(hostTeamGoalFactor => ({
      ...original,
      hostTeamGoalFactor,
      seedTeamGoalFactor: result.candidate.seedTeamGoalFactor
    }))
  })
  const hostRanked = await rankFactorCandidates(
    target,
    original,
    hostFactors,
    coarseSimulations,
    quickCandidateCount,
    seed + 1000,
    objective
  )
  const localPairs = []
  hostRanked.slice(0, 3).forEach(result => {
    const localHosts = localModelFactorValues([result.candidate.hostTeamGoalFactor], 0.2, 0.1)
    const localSeeds = localModelFactorValues([result.candidate.seedTeamGoalFactor], 0.2, 0.1)
    localHosts.forEach(hostTeamGoalFactor => {
      localSeeds.forEach(seedTeamGoalFactor => {
        localPairs.push({ ...original, hostTeamGoalFactor, seedTeamGoalFactor })
      })
    })
  })
  const uniquePairs = [...new Map(localPairs.map(factors => [
    factors.hostTeamGoalFactor + '|' + factors.seedTeamGoalFactor,
    factors
  ])).values()]
  const refinedRanked = await rankFactorCandidates(
    target,
    original,
    uniquePairs,
    refineSimulations,
    refineCandidateCount,
    seed + 2000,
    objective
  )
  return {
    coarseTop: hostRanked.slice(0, 5),
    refinedTop: refinedRanked.slice(0, 3)
  }
}

function chronologicalMetrics(matches, parameters) {
  const sorted = [...matches].sort((left, right) => {
    const dateOrder = String(left.matchDate).localeCompare(String(right.matchDate))
    return dateOrder || String(left.matchId).localeCompare(String(right.matchId))
  })
  return Array.from({ length: 3 }, (_, index) => {
    const start = Math.floor(sorted.length * index / 3)
    const end = Math.floor(sorted.length * (index + 1) / 3)
    const fold = sorted.slice(start, end)
    return {
      period: index + 1,
      startDate: fold[0]?.matchDate || null,
      endDate: fold[fold.length - 1]?.matchDate || null,
      metrics: printableMetrics(evaluate(fold, parameters))
    }
  })
}

function printableMetrics(metrics) {
  return {
    sampleCount: metrics.sampleCount,
    minimumRecommendedMatches: minimumRecommendedMatches(metrics.sampleCount),
    recommendedMatchCount: metrics.recommendedMatchCount,
    recommendedSelectionCount: metrics.recommendedSelectionCount,
    hitMatchCount: metrics.hitMatchCount,
    missMatchCount: metrics.missMatchCount,
    settledMatchCount: metrics.settledMatchCount,
    averageOddsIncludingMisses: round(metrics.averageOddsIncludingMisses, 4),
    averageRecommendations: round(metrics.averageRecommendations, 4),
    hitRate: round(metrics.hitRate * 100, 2),
    totalStake: round(metrics.totalStake, 2),
    totalReturn: round(metrics.totalReturn, 2),
    netProfit: round(metrics.netProfit, 2),
    roi: round(metrics.roi * 100, 2)
  }
}

function printableRanked(results) {
  return results.map(result => ({
    parameters: result.candidate,
    metrics: printableMetrics(result.metrics)
  }))
}

async function optimizeProfile(target, preset, originalProfile, objective) {
  const scopedTarget = { ...target, preset }
  const key = profileKey(target.code, target.range, preset)
  const original = flattenProfile(originalProfile)
  const seed = stringSeed(key)
  const sampleMatches = await fetchBacktest(scopedTarget, original, coarseSimulations)
  if (sampleMatches.length === 0) {
    return {
      key,
      competitionName: target.name,
      range: target.range,
      preset,
      status: 'SKIPPED_NO_SAMPLES',
      sampleCount: 0
    }
  }

  process.stderr.write(
    `开始优化 ${key}，样本 ${sampleMatches.length} 场` +
    `，ROI 下限 ${(objective.minimumRoiExclusive * 100).toFixed(4)}%\n`
  )
  const factorSearch = target.code === 'WORLD_CUP'
    ? await optimizeWorldCupFactors(scopedTarget, original, seed, objective)
    : await optimizeNonWorldCupFactors(scopedTarget, original, seed, objective)

  const finalFactorCandidates = factorSearch.refinedTop.slice(0, 2)
    .map(result => result.candidate)
  if (!finalFactorCandidates.some(candidate => {
    return candidate.hostTeamGoalFactor === original.hostTeamGoalFactor &&
      candidate.homeTeamGoalFactor === original.homeTeamGoalFactor &&
      candidate.seedTeamGoalFactor === original.seedTeamGoalFactor
  })) {
    finalFactorCandidates.push(original)
  }

  const finalRanked = []
  for (let index = 0; index < finalFactorCandidates.length; index++) {
    const factorCandidate = finalFactorCandidates[index]
    const matches = await fetchBacktest(scopedTarget, factorCandidate, finalSimulations)
    const modelFactors = {
      hostTeamGoalFactor: factorCandidate.hostTeamGoalFactor,
      homeTeamGoalFactor: factorCandidate.homeTeamGoalFactor,
      seedTeamGoalFactor: factorCandidate.seedTeamGoalFactor
    }
    const optimized = deepOptimize(
      matches,
      modelFactors,
      factorCandidate,
      seed + 5000 + index * 10,
      objective
    )
    if (optimized) {
      addRankedResult(finalRanked, optimized.candidate, optimized.metrics, objective, 5)
    }
  }

  const best = finalRanked[0]
  const originalMatches = await fetchBacktest(scopedTarget, original, finalSimulations)
  const baselineMetrics = evaluate(originalMatches, original)
  if (!best) {
    const failedResult = {
      key,
      competitionName: target.name,
      range: target.range,
      preset,
      status: 'FAILED_ROI_CONSTRAINT',
      minimumRoiExclusive: objective.minimumRoiExclusive,
      sampleCount: originalMatches.length,
      originalParameters: original,
      baselineMetrics: printableMetrics(baselineMetrics),
      coarseTop: printableRanked(factorSearch.coarseTop),
      refinedTop: printableRanked(factorSearch.refinedTop)
    }
    process.stderr.write(`未找到满足 ROI 约束的 ${key}\n`)
    return failedResult
  }
  const finalMatches = await fetchBacktest(scopedTarget, best.candidate, finalSimulations)
  const result = {
    key,
    competitionName: target.name,
    range: target.range,
    preset,
    status: 'OPTIMIZED',
    minimumRoiExclusive: objective.minimumRoiExclusive,
    optimizedRoi: best.metrics.roi,
    lowSampleWarning: finalMatches.length < 20,
    originalParameters: original,
    optimizedParameters: best.candidate,
    baselineMetrics: printableMetrics(baselineMetrics),
    optimizedMetrics: printableMetrics(best.metrics),
    chronologicalMetrics: chronologicalMetrics(finalMatches, best.candidate),
    coarseTop: printableRanked(factorSearch.coarseTop),
    refinedTop: printableRanked(factorSearch.refinedTop),
    finalTop: printableRanked(finalRanked)
  }
  process.stderr.write(
    `完成 ${key}：ROI ${result.baselineMetrics.roi}% -> ${result.optimizedMetrics.roi}%` +
    `，推荐 ${result.optimizedMetrics.recommendedMatchCount}/${result.optimizedMetrics.sampleCount} 场` +
    `，命中 ${result.optimizedMetrics.hitMatchCount} 场\n`
  )
  return result
}

async function writeReport(report) {
  await mkdir(OUTPUT_DIRECTORY, { recursive: true })
  await writeFile(REPORT_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
}

async function verifySavedProfiles(config, targets) {
  const report = {
    generatedAt: new Date().toISOString(),
    today: todayInShanghai(),
    simulations: finalSimulations,
    savedProfileCount: Object.keys(config.parameterProfiles || {}).length,
    minimumStableRoi: MINIMUM_STABLE_ROI,
    minimumAggressiveRoi: MINIMUM_AGGRESSIVE_ROI,
    minimumAggressiveRoiGap: MINIMUM_AGGRESSIVE_ROI_GAP,
    results: []
  }
  for (const target of targets) {
    const pair = {
      key: baseProfileKey(target.code, target.range),
      competitionName: target.name,
      range: target.range,
      profiles: {}
    }
    let missingProfile = false
    for (const preset of PARAMETER_PRESETS) {
      const key = profileKey(target.code, target.range, preset)
      const profile = config.parameterProfiles?.[key]
      if (!profile) {
        pair.profiles[preset] = { key, status: 'SKIPPED_MISSING_CONFIG' }
        missingProfile = true
        continue
      }
      const parameters = flattenProfile(profile)
      const matches = await fetchBacktest({ ...target, preset }, parameters, finalSimulations)
      const metrics = evaluate(matches, parameters)
      pair.profiles[preset] = {
        key,
        status: matches.length > 0 ? 'VERIFIED' : 'SKIPPED_NO_SAMPLES',
        parameters,
        rawRoi: metrics.roi,
        metrics: printableMetrics(metrics),
        chronologicalMetrics: chronologicalMetrics(matches, parameters)
      }
    }
    if (missingProfile) {
      pair.status = 'SKIPPED_MISSING_CONFIG'
      pair.constraintsSatisfied = false
      report.results.push(pair)
      continue
    }
    const stable = pair.profiles.STABLE
    const aggressive = pair.profiles.AGGRESSIVE
    if (stable.status !== 'VERIFIED' || aggressive.status !== 'VERIFIED') {
      pair.status = 'SKIPPED_NO_SAMPLES'
      pair.constraintsSatisfied = null
      report.results.push(pair)
      process.stderr.write(`复核 ${pair.key}：无可用样本\n`)
      continue
    }
    const stableObjective = createObjective('STABLE', MINIMUM_STABLE_ROI)
    const aggressiveObjective = createObjective(
      'AGGRESSIVE',
      aggressiveMinimumRoiExclusive(stable.rawRoi)
    )
    stable.eligible = stableObjective.isEligible({
      recommendedMatchCount: stable.metrics.recommendedMatchCount,
      hitMatchCount: stable.metrics.hitMatchCount,
      roi: stable.rawRoi
    })
    aggressive.minimumRoiExclusive = aggressiveObjective.minimumRoiExclusive
    aggressive.eligible = aggressiveObjective.isEligible({
      recommendedMatchCount: aggressive.metrics.recommendedMatchCount,
      hitMatchCount: aggressive.metrics.hitMatchCount,
      roi: aggressive.rawRoi
    })
    pair.status = 'VERIFIED'
    pair.constraintsSatisfied = stable.eligible && aggressive.eligible
    report.results.push(pair)
    process.stderr.write(
      `复核 ${pair.key}：稳健推荐 ${stable.metrics.recommendedMatchCount} 场 / ROI ${stable.metrics.roi}%` +
      `，激进推荐 ${aggressive.metrics.recommendedMatchCount} 场 / ROI ${aggressive.metrics.roi}%` +
      `，约束${pair.constraintsSatisfied ? '通过' : '失败'}\n`
    )
  }
  report.completedAt = new Date().toISOString()
  await mkdir(OUTPUT_DIRECTORY, { recursive: true })
  await writeFile(VERIFICATION_REPORT_PATH, JSON.stringify(report, null, 2) + '\n', 'utf8')
  console.log(JSON.stringify({
    reportPath: VERIFICATION_REPORT_PATH,
    savedProfileCount: report.savedProfileCount,
    verifiedPairCount: report.results.filter(result => result.status === 'VERIFIED').length,
    constraintSatisfiedPairCount: report.results.filter(result => result.constraintsSatisfied === true).length,
    constraintFailedPairs: report.results
      .filter(result => result.constraintsSatisfied === false)
      .map(result => result.key),
    skipped: report.results
      .filter(result => result.status !== 'VERIFIED')
      .map(result => ({ key: result.key, status: result.status }))
  }, null, 2))
}

async function applyOptimizedProfiles(config, results) {
  const nextConfig = structuredClone(config)
  results
    .filter(result => result.status === 'OPTIMIZED')
    .forEach(result => {
      nextConfig.parameterProfiles[result.key] = expandProfile(result.optimizedParameters)
    })
  return fetchJson(USER_CONFIG_URL, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json; charset=utf-8'
    },
    body: JSON.stringify(nextConfig)
  })
}

async function main() {
  const config = await fetchJson(USER_CONFIG_URL)
  await mkdir(OUTPUT_DIRECTORY, { recursive: true })
  const targets = buildTargets()
  if (verifyOnly) {
    await verifySavedProfiles(config, targets)
    return
  }
  await writeFile(BEFORE_CONFIG_PATH, JSON.stringify(config, null, 2) + '\n', 'utf8')
  const report = {
    generatedAt: new Date().toISOString(),
    today: todayInShanghai(),
    options: {
      coarseSimulations,
      refineSimulations,
      finalSimulations,
      quickCandidateCount,
      refineCandidateCount,
      finalCandidateCount,
      applyResults,
      stableObjective: 'ROI > 7.5% 后优先最大化推荐场次数',
      aggressiveObjective: 'ROI > 15% 且至少高于稳健档 0.01 个百分点后优先最大化推荐场次数',
      minimumStableRoi: MINIMUM_STABLE_ROI,
      minimumAggressiveRoi: MINIMUM_AGGRESSIVE_ROI,
      minimumAggressiveRoiGap: MINIMUM_AGGRESSIVE_ROI_GAP
    },
    skippedFutureCurrentProfiles: COMPETITIONS
      .filter(competition => competition.currentStartDate > todayInShanghai())
      .flatMap(competition => PARAMETER_PRESETS.map(preset => {
        return profileKey(competition.code, 'CURRENT', preset)
      })),
    results: []
  }
  for (const target of targets) {
    const stableKey = profileKey(target.code, target.range, 'STABLE')
    const stableProfile = config.parameterProfiles?.[stableKey]
    if (!stableProfile) {
      report.results.push({ key: stableKey, status: 'SKIPPED_MISSING_CONFIG' })
      continue
    }
    const stableObjective = createObjective('STABLE', MINIMUM_STABLE_ROI)
    const stableResult = await optimizeProfile(target, 'STABLE', stableProfile, stableObjective)
    report.results.push(stableResult)
    await writeReport(report)

    const aggressiveKey = profileKey(target.code, target.range, 'AGGRESSIVE')
    const aggressiveProfile = config.parameterProfiles?.[aggressiveKey]
    if (!aggressiveProfile) {
      report.results.push({ key: aggressiveKey, status: 'SKIPPED_MISSING_CONFIG' })
      await writeReport(report)
      continue
    }
    if (stableResult.status === 'SKIPPED_NO_SAMPLES') {
      report.results.push({
        key: aggressiveKey,
        competitionName: target.name,
        range: target.range,
        preset: 'AGGRESSIVE',
        status: 'SKIPPED_NO_SAMPLES',
        sampleCount: 0
      })
      await writeReport(report)
      continue
    }
    if (stableResult.status !== 'OPTIMIZED') {
      report.results.push({
        key: aggressiveKey,
        competitionName: target.name,
        range: target.range,
        preset: 'AGGRESSIVE',
        status: 'SKIPPED_STABLE_CONSTRAINT'
      })
      await writeReport(report)
      continue
    }
    const aggressiveObjective = createObjective(
      'AGGRESSIVE',
      aggressiveMinimumRoiExclusive(stableResult.optimizedRoi)
    )
    report.results.push(await optimizeProfile(
      target,
      'AGGRESSIVE',
      aggressiveProfile,
      aggressiveObjective
    ))
    await writeReport(report)
  }
  if (applyResults) {
    const savedConfig = await applyOptimizedProfiles(config, report.results)
    report.appliedProfileCount = report.results.filter(result => result.status === 'OPTIMIZED').length
    report.savedProfileCount = Object.keys(savedConfig.parameterProfiles || {}).length
  }
  report.completedAt = new Date().toISOString()
  await writeReport(report)
  console.log(JSON.stringify({
    reportPath: REPORT_PATH,
    appliedProfileCount: report.appliedProfileCount || 0,
    optimized: report.results
      .filter(result => result.status === 'OPTIMIZED')
      .map(result => ({
        key: result.key,
        sampleCount: result.optimizedMetrics.sampleCount,
        baselineRoi: result.baselineMetrics.roi,
        optimizedRoi: result.optimizedMetrics.roi,
        recommendedMatchCount: result.optimizedMetrics.recommendedMatchCount,
        parameters: result.optimizedParameters
      })),
    skipped: report.results
      .filter(result => result.status !== 'OPTIMIZED')
      .map(result => ({ key: result.key, status: result.status }))
  }, null, 2))
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
