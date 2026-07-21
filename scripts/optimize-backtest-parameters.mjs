import { calculateFlatStakeBacktest, calculateSamplingRate } from '../frontend/src/backtest-roi.mjs'

const BASE_URL = 'http://localhost:8080/api/football/recommendation-backtest'
const NON_WORLD_CUP_COMPETITIONS = [
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
  'BRAZIL_SERIE_A',
  'PRIMEIRA_LIGA',
  'EREDIVISIE',
  'ARGENTINE_PRIMERA_DIVISION'
].join(',')
const PROBABILITY_KEYS = ['win', 'draw', 'lose']
const PROBABILITY_MASKS = [1, 2, 4]

const PRESETS = {
  stable: {
    modelMode: 'after',
    hostTeamGoalFactor: 1.10,
    homeTeamGoalFactor: 1.06,
    seedTeamGoalFactor: 1.85,
    handicapSmoothingFactor: 0.274,
    recommendationOdds: 1.03,
    handicapRecommendationThreshold: 68.16,
    handicapReverseThreshold: 46.78,
    singleRecommendationThreshold: 71.72
  },
  aggressive: {
    modelMode: 'after',
    hostTeamGoalFactor: 2.30,
    homeTeamGoalFactor: 1.75,
    seedTeamGoalFactor: 1.55,
    handicapSmoothingFactor: 0.650,
    recommendationOdds: 2.46,
    handicapRecommendationThreshold: 89.09,
    handicapReverseThreshold: 43.41,
    singleRecommendationThreshold: 78.71
  }
}

const argumentsMap = new Map(
  process.argv.slice(2).map(argument => {
    const [key, value = 'true'] = argument.replace(/^--/, '').split('=', 2)
    return [key, value]
  })
)
const simulations = Math.max(1000, Number(argumentsMap.get('simulations')) || 1000)
const phase = argumentsMap.get('phase') || 'baseline'
const modelMode = 'after'
const includePreviousEdition = argumentsMap.get('include-previous') !== 'false'
const requestedScheme = ['stable', 'aggressive'].includes(argumentsMap.get('scheme'))
  ? argumentsMap.get('scheme')
  : null
const coordinatePasses = Math.max(1, Math.min(10, Number(argumentsMap.get('coordinate-passes')) || 4))
const coordinateThresholdStep = Math.max(0.01, Math.min(5, Number(argumentsMap.get('coordinate-step')) || 0.01))
const coordinateSmoothingStep = Math.max(0.001, Math.min(0.1, Number(argumentsMap.get('smoothing-step')) || 0.001))
const factorStep = Math.max(0.01, Math.min(1, Number(argumentsMap.get('factor-step')) || 0.1))
const factorTop = Math.max(1, Math.min(30, Number(argumentsMap.get('factor-top')) || 10))
const factorMinimum = Math.max(0.1, Math.min(3, Number(argumentsMap.get('factor-min')) || 0.1))
const factorMaximum = Math.max(factorMinimum, Math.min(3, Number(argumentsMap.get('factor-max')) || 3))
const hostFactorMinimum = Math.max(0.1, Math.min(3, Number(argumentsMap.get('host-factor-min')) || factorMinimum))
const hostFactorMaximum = Math.max(hostFactorMinimum, Math.min(3, Number(argumentsMap.get('host-factor-max')) || factorMaximum))
const seedFactorMinimum = Math.max(0.1, Math.min(3, Number(argumentsMap.get('seed-factor-min')) || factorMinimum))
const seedFactorMaximum = Math.max(seedFactorMinimum, Math.min(3, Number(argumentsMap.get('seed-factor-max')) || factorMaximum))
const targetedSmoothingCandidates = Math.max(100, Math.min(20000, Number(argumentsMap.get('targeted-smoothing-candidates')) || 800))
const targetedFinalCandidates = Math.max(1000, Math.min(2000000, Number(argumentsMap.get('targeted-final-candidates')) || 50000))
const randomSeedOffset = Number(argumentsMap.get('seed-offset')) || 0
const responseCache = new Map()

function selectedSchemeNames() {
  return Object.keys(PRESETS).filter(name => !requestedScheme || name === requestedScheme)
}

function selectedPresetEntries(presets = PRESETS) {
  return Object.entries(presets).filter(([name]) => !requestedScheme || name === requestedScheme)
}

function roundToTwo(value) {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

function toProbabilityArray(probability) {
  return PROBABILITY_KEYS.map(key => Number(probability?.[key]) || 0)
}

function findHandicapProbability(match, effectiveModelMode = modelMode) {
  const handicap = Number(match.sportteryHandicap)
  const probabilities = effectiveModelMode === 'before'
    ? match.handicapProbabilities
    : match.adjustedHandicapProbabilities
  const source = Array.isArray(probabilities)
    ? probabilities
    : []
  const probability = source.find(item => Number(item.handicap) === handicap)?.probability
  return probability ? toProbabilityArray(probability) : [0, 0, 0]
}

function parseScore(scoreText) {
  const match = String(scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
  return match ? [Number(match[1]), Number(match[2])] : null
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
  return PROBABILITY_KEYS.map(key => {
    const value = Number(odds?.[key])
    return Number.isFinite(value) && value > 0 ? value : null
  })
}

function prepareMatches(matches, effectiveModelMode = modelMode) {
  return (matches || []).map(match => {
    const handicap = Number(match.sportteryHandicap)
    const score = parseScore(match.scoreText)
    return {
      matchId: match.matchId,
      matchDate: match.matchDate,
      competition: match.competition,
      homeTeamEn: match.homeTeamEn,
      awayTeamEn: match.awayTeamEn,
      normalAvailable: match.sportteryNormalAvailable === true,
      handicapAvailable: Number.isInteger(handicap) && handicap !== 0,
      handicap,
      normalProbability: toProbabilityArray(
        effectiveModelMode === 'before' ? match.normalProbability : match.adjustedNormalProbability
      ),
      rawHandicapProbability: findHandicapProbability(match, effectiveModelMode),
      normalOdds: toOddsArray(match.sportteryNormalOdds),
      handicapOdds: toOddsArray(match.sportteryHandicapOdds),
      normalActualIndex: actualProbabilityIndex(score, 0),
      handicapActualIndex: actualProbabilityIndex(score, handicap)
    }
  })
}

function smoothHandicapProbability(rawProbability, normalProbability, smoothingFactor) {
  const rawWeight = 1 - smoothingFactor
  const values = rawProbability.map((value, index) => {
    return value * rawWeight + normalProbability[index] * smoothingFactor
  })
  const total = values.reduce((sum, value) => sum + value, 0)
  if (total <= 0) {
    return [0, 0, 0]
  }
  const win = roundToTwo(values[0] * 100 / total)
  const draw = roundToTwo(values[1] * 100 / total)
  const lose = roundToTwo(Math.max(0, 100 - win - draw))
  return [win, draw, lose]
}

function withSmoothing(matches, smoothingFactor) {
  const smoothedMatches = matches.map(match => ({
    ...match,
    handicapProbability: smoothHandicapProbability(
      match.rawHandicapProbability,
      match.normalProbability,
      smoothingFactor
    )
  }))
  smoothedMatches.totalMatchCount = Math.max(
    smoothedMatches.length,
    Number(matches.totalMatchCount) || 0
  )
  return smoothedMatches
}

function findMaxCell(rows) {
  let maxCell = null
  rows.forEach(row => {
    row.probability.forEach((value, probabilityIndex) => {
      if (!maxCell || value > maxCell.value) {
        maxCell = { row, probabilityIndex, value }
      }
    })
  })
  return maxCell
}

function adjacentMask(probabilityIndex) {
  if (probabilityIndex === 1) {
    return PROBABILITY_MASKS[1]
  }
  return probabilityIndex === 0
    ? PROBABILITY_MASKS[0] | PROBABILITY_MASKS[1]
    : PROBABILITY_MASKS[1] | PROBABILITY_MASKS[2]
}

function recommendationForMatch(match, parameters) {
  const normalRow = {
    type: 'normal',
    probability: match.normalProbability,
    odds: match.normalOdds,
    actualIndex: match.normalActualIndex
  }
  const handicapRow = {
    type: 'handicap',
    probability: match.handicapProbability,
    odds: match.handicapOdds,
    actualIndex: match.handicapActualIndex
  }
  const rows = []
  if (match.handicap > 0 && match.handicapAvailable) {
    rows.push(handicapRow)
  }
  if (match.normalAvailable) {
    rows.push(normalRow)
  }
  if (match.handicap < 0 && match.handicapAvailable) {
    rows.push(handicapRow)
  }
  if (rows.length === 0) {
    return null
  }

  const maxCell = findMaxCell(rows)
  let recommendationRow = maxCell.row
  let mask = 0
  if (match.normalAvailable && match.handicapAvailable &&
    maxCell.row.type === 'normal' && maxCell.probabilityIndex !== 1) {
    const handicapValue = handicapRow.probability[maxCell.probabilityIndex]
    if (handicapValue >= parameters.handicapRecommendationThreshold && handicapValue < maxCell.value) {
      recommendationRow = handicapRow
      mask = PROBABILITY_MASKS[maxCell.probabilityIndex] | PROBABILITY_MASKS[1]
    }
  }
  if (mask === 0 && match.normalAvailable && match.handicapAvailable &&
    maxCell.row.type === 'handicap' && maxCell.probabilityIndex !== 1 &&
    maxCell.value < parameters.handicapReverseThreshold) {
    mask = PROBABILITY_MASKS.reduce((result, probabilityMask, probabilityIndex) => {
      return probabilityIndex === maxCell.probabilityIndex ? result : result | probabilityMask
    }, 0)
  }
  if (mask === 0) {
    mask = adjacentMask(maxCell.probabilityIndex)
  }

  let selectionCount = PROBABILITY_MASKS.filter(probabilityMask => (mask & probabilityMask) !== 0).length
  if (selectionCount === 2) {
    let strongestIndex = -1
    let strongestValue = -1
    recommendationRow.probability.forEach((value, probabilityIndex) => {
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

  for (let probabilityIndex = 0; probabilityIndex < PROBABILITY_MASKS.length; probabilityIndex++) {
    if ((mask & PROBABILITY_MASKS[probabilityIndex]) === 0) {
      continue
    }
    const odds = recommendationRow.odds[probabilityIndex]
    if (odds === null || odds < parameters.recommendationOdds) {
      return null
    }
  }
  return {
    mask,
    selectionCount,
    actualIndex: recommendationRow.actualIndex,
    odds: recommendationRow.odds
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
  const totalMatchCount = Math.max(
    matches.length,
    Number(matches.totalMatchCount) || 0
  )
  return {
    sampleCount: totalMatchCount,
    samplingRate: calculateSamplingRate(recommendedMatchCount, totalMatchCount),
    recommendedMatchCount,
    recommendedSelectionCount,
    hitMatchCount,
    missMatchCount,
    averageRecommendations,
    hitRate,
    averageOddsIncludingMisses: financials.averageReturnIncludingMisses ?? 0,
    totalStake: financials.totalStake,
    totalReturn: financials.totalReturn,
    netProfit: financials.netProfit,
    roi: financials.roi ?? -1
  }
}

async function fetchBacktest(competition, factors, effectiveModelMode = modelMode) {
  const effectiveFactorKey = competition === 'WORLD_CUP'
    ? [factors.hostTeamGoalFactor, factors.seedTeamGoalFactor]
    : [factors.homeTeamGoalFactor]
  const key = [includePreviousEdition, effectiveModelMode, competition, ...effectiveFactorKey].join('|')
  if (responseCache.has(key)) {
    return responseCache.get(key)
  }
  const url = new URL(BASE_URL)
  url.searchParams.set('competition', competition)
  url.searchParams.set('includePreviousEdition', String(includePreviousEdition))
  url.searchParams.set('simulations', simulations)
  url.searchParams.set('hostTeamGoalFactor', factors.hostTeamGoalFactor)
  url.searchParams.set('homeTeamGoalFactor', factors.homeTeamGoalFactor)
  url.searchParams.set('seedTeamGoalFactor', factors.seedTeamGoalFactor)
  url.searchParams.set('handicapSmoothingFactor', '0')
  const startedAt = Date.now()
  let data = null
  let lastError = null
  for (let attempt = 1; attempt <= 4; attempt++) {
    try {
      const response = await fetch(url)
      if (!response.ok) {
        throw new Error(`回测接口失败 ${response.status}: ${await response.text()}`)
      }
      data = await response.json()
      break
    } catch (error) {
      lastError = error
      if (attempt < 4) {
        process.stderr.write(`接口连接波动，第 ${attempt} 次重试\n`)
        await new Promise(resolve => setTimeout(resolve, attempt * 500))
      }
    }
  }
  if (!data) {
    throw lastError || new Error('回测接口未返回数据')
  }
  const result = prepareMatches(data.matches, effectiveModelMode)
  result.totalMatchCount = Math.max(
    result.length,
    Number(data.completedMatchCount) || 0
  )
  responseCache.set(key, result)
  process.stderr.write(`读取 ${competition} ${result.length} 场，${((Date.now() - startedAt) / 1000).toFixed(1)} 秒\n`)
  return result
}

async function loadPresetMatches(preset, effectiveModelMode = modelMode) {
  const [worldCupMatches, nonWorldCupMatches] = await Promise.all([
    fetchBacktest('WORLD_CUP', preset, effectiveModelMode),
    fetchBacktest(NON_WORLD_CUP_COMPETITIONS, preset, effectiveModelMode)
  ])
  return combineMatchGroups(worldCupMatches, nonWorldCupMatches)
}

function combineMatchGroups(...groups) {
  const matches = groups.flat()
  matches.totalMatchCount = groups.reduce((sum, group) => {
    return sum + Math.max(group.length, Number(group.totalMatchCount) || 0)
  }, 0)
  return matches
}

function printableMetrics(metrics) {
  return {
    sampleCount: metrics.sampleCount,
    samplingRate: metrics.samplingRate === null
      ? null
      : Number((metrics.samplingRate * 100).toFixed(2)),
    recommendedMatchCount: metrics.recommendedMatchCount,
    recommendedSelectionCount: metrics.recommendedSelectionCount,
    hitMatchCount: metrics.hitMatchCount,
    missMatchCount: metrics.missMatchCount,
    averageOddsIncludingMisses: Number(metrics.averageOddsIncludingMisses.toFixed(4)),
    averageRecommendations: Number(metrics.averageRecommendations.toFixed(4)),
    hitRate: Number((metrics.hitRate * 100).toFixed(2)),
    totalStake: Number(metrics.totalStake.toFixed(2)),
    totalReturn: Number(metrics.totalReturn.toFixed(2)),
    netProfit: Number(metrics.netProfit.toFixed(2)),
    roi: Number((metrics.roi * 100).toFixed(2))
  }
}

async function runBaselines() {
  const result = {}
  for (const [name, preset] of selectedPresetEntries()) {
    const matches = await loadPresetMatches(preset, preset.modelMode || modelMode)
    const smoothedMatches = withSmoothing(matches, preset.handicapSmoothingFactor)
    result[name] = {
      parameters: preset,
      metrics: printableMetrics(evaluate(smoothedMatches, preset))
    }
  }
  console.log(JSON.stringify({ simulations, result }, null, 2))
}

function createRandom(seed) {
  let state = seed >>> 0
  return () => {
    state = (state * 1664525 + 1013904223) >>> 0
    return state / 4294967296
  }
}

function randomValue(random, minimum, maximum, scale) {
  const value = minimum + random() * (maximum - minimum)
  const multiplier = 10 ** scale
  return Math.round(value * multiplier) / multiplier
}

function addRankedResult(results, candidate, metrics, limit = 10) {
  results.push({ candidate, metrics })
  results.sort((left, right) => right.metrics.roi - left.metrics.roi)
  if (results.length > limit) {
    results.length = limit
  }
}

const SEARCH_DEFINITIONS = {
  stable: {
    minimumRecommendedMatches: 500,
    minimumAverageOdds: 1.60,
    odds: [1.00, 2.50],
    handicapRecommendation: [0, 100],
    handicapReverse: [0, 100],
    singleRecommendation: [0, 100]
  },
  aggressive: {
    minimumRecommendedMatches: 100,
    minimumAverageOdds: 1.90,
    odds: [1.30, 4.00],
    handicapRecommendation: [0, 100],
    handicapReverse: [0, 100],
    singleRecommendation: [0, 100]
  }
}

const TARGETED_CANDIDATES = {
  stable: {
    modelMode: 'after',
    hostTeamGoalFactor: 1.10,
    homeTeamGoalFactor: 1.06,
    seedTeamGoalFactor: 1.85,
    handicapSmoothingFactor: 0.274,
    recommendationOdds: 1.03,
    handicapRecommendationThreshold: 68.16,
    handicapReverseThreshold: 46.78,
    singleRecommendationThreshold: 71.72
  },
  aggressive: {
    modelMode: 'after',
    hostTeamGoalFactor: 2.30,
    homeTeamGoalFactor: 1.75,
    seedTeamGoalFactor: 1.55,
    handicapSmoothingFactor: 0.650,
    recommendationOdds: 2.46,
    handicapRecommendationThreshold: 89.09,
    handicapReverseThreshold: 43.41,
    singleRecommendationThreshold: 78.71
  }
}

const VERIFICATION_CANDIDATES = {
  stable: {
    modelMode: 'after',
    hostTeamGoalFactor: 1.10,
    homeTeamGoalFactor: 1.06,
    seedTeamGoalFactor: 1.85,
    handicapSmoothingFactor: 0.274,
    recommendationOdds: 1.03,
    handicapRecommendationThreshold: 68.16,
    handicapReverseThreshold: 46.78,
    singleRecommendationThreshold: 71.72
  },
  aggressive: {
    modelMode: 'after',
    hostTeamGoalFactor: 2.30,
    homeTeamGoalFactor: 1.75,
    seedTeamGoalFactor: 1.55,
    handicapSmoothingFactor: 0.650,
    recommendationOdds: 2.46,
    handicapRecommendationThreshold: 89.09,
    handicapReverseThreshold: 43.41,
    singleRecommendationThreshold: 78.71
  }
}

function isEligible(metrics, definition) {
  return metrics.recommendedMatchCount >= definition.minimumRecommendedMatches &&
    metrics.averageOddsIncludingMisses >= definition.minimumAverageOdds
}

function buildThresholdCandidates(name, count, seed, center = PRESETS[name], local = false) {
  const definition = SEARCH_DEFINITIONS[name]
  const random = createRandom(seed)
  const candidates = [{ ...center }, { ...PRESETS[name] }, { ...TARGETED_CANDIDATES[name] }]
  for (let index = 0; index < count; index++) {
    const oddsRange = local
      ? [
          Math.max(definition.odds[0], center.recommendationOdds - 0.18),
          Math.min(definition.odds[1], center.recommendationOdds + 0.18)
        ]
      : definition.odds
    const handicapRecommendationRange = local
      ? [
          Math.max(definition.handicapRecommendation[0], center.handicapRecommendationThreshold - 6),
          Math.min(definition.handicapRecommendation[1], center.handicapRecommendationThreshold + 6)
        ]
      : definition.handicapRecommendation
    const handicapReverseRange = local
      ? [
          Math.max(definition.handicapReverse[0], center.handicapReverseThreshold - 6),
          Math.min(definition.handicapReverse[1], center.handicapReverseThreshold + 6)
        ]
      : definition.handicapReverse
    const singleRecommendationRange = local
      ? [
          Math.max(definition.singleRecommendation[0], center.singleRecommendationThreshold - 8),
          Math.min(definition.singleRecommendation[1], center.singleRecommendationThreshold + 8)
        ]
      : definition.singleRecommendation
    candidates.push({
      ...center,
      recommendationOdds: randomValue(random, ...oddsRange, 2),
      handicapRecommendationThreshold: randomValue(random, ...handicapRecommendationRange, 2),
      handicapReverseThreshold: randomValue(random, ...handicapReverseRange, 2),
      singleRecommendationThreshold: randomValue(random, ...singleRecommendationRange, 2)
    })
  }
  return candidates
}

function findBestThresholds(matches, smoothingFactor, thresholdCandidates, definition, modelFactors) {
  const smoothedMatches = withSmoothing(matches, smoothingFactor)
  let best = null
  thresholdCandidates.forEach(thresholds => {
    const candidate = {
      ...thresholds,
      ...modelFactors,
      handicapSmoothingFactor: smoothingFactor
    }
    const metrics = evaluate(smoothedMatches, candidate)
    if (!isEligible(metrics, definition)) {
      return
    }
    if (!best || metrics.roi > best.metrics.roi) {
      best = { candidate, metrics }
    }
  })
  return best
}

async function mapWithConcurrency(values, concurrency, mapper) {
  const result = new Array(values.length)
  let nextIndex = 0
  async function worker() {
    while (nextIndex < values.length) {
      const index = nextIndex++
      result[index] = await mapper(values[index], index)
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrency, values.length) }, () => worker()))
  return result
}

function factorRange(center, radius, step, minimum, maximum) {
  const values = []
  for (let value = center - radius; value <= center + radius + 0.0001; value += step) {
    const normalized = Number(Math.max(minimum, Math.min(maximum, value)).toFixed(3))
    if (!values.includes(normalized)) {
      values.push(normalized)
    }
  }
  return values
}

async function loadNonWorldCupFactorGrid(homeValues) {
  const entries = await mapWithConcurrency(homeValues, 2, async homeTeamGoalFactor => {
    const factors = {
      hostTeamGoalFactor: 1,
      homeTeamGoalFactor,
      seedTeamGoalFactor: 1
    }
    return [homeTeamGoalFactor, await fetchBacktest(NON_WORLD_CUP_COMPETITIONS, factors)]
  })
  return new Map(entries)
}

async function loadWorldCupFactorGrid(hostValues, seedValues) {
  const combinations = hostValues.flatMap(hostTeamGoalFactor => {
    return seedValues.map(seedTeamGoalFactor => ({ hostTeamGoalFactor, seedTeamGoalFactor }))
  })
  const entries = await mapWithConcurrency(combinations, 2, async factors => {
    const requestFactors = {
      ...factors,
      homeTeamGoalFactor: 1
    }
    const key = `${factors.hostTeamGoalFactor}|${factors.seedTeamGoalFactor}`
    return [key, await fetchBacktest('WORLD_CUP', requestFactors)]
  })
  return new Map(entries)
}

function combinedMatches(nonWorldCupGrid, worldCupGrid, factors) {
  const nonWorldCupMatches = nonWorldCupGrid.get(factors.homeTeamGoalFactor)
  const worldCupMatches = worldCupGrid.get(`${factors.hostTeamGoalFactor}|${factors.seedTeamGoalFactor}`)
  if (!nonWorldCupMatches || !worldCupMatches) {
    throw new Error(`缺少模型因子数据 ${JSON.stringify(factors)}`)
  }
  return combineMatchGroups(worldCupMatches, nonWorldCupMatches)
}

async function runCoarseModelSearch() {
  const homeValues = [0.10, 0.40, 0.70, 1.00, 1.30, 1.60, 2.00, 2.50, 3.00]
  const hostValues = [0.10, 0.40, 0.70, 1.00, 1.30, 1.60, 2.00, 2.50, 3.00]
  const seedValues = [0.10, 0.40, 0.70, 1.00, 1.30, 1.60, 2.00, 2.50, 3.00]
  const smoothingValues = [0.00, 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80]
  const [nonWorldCupGrid, worldCupGrid] = await Promise.all([
    loadNonWorldCupFactorGrid(homeValues),
    loadWorldCupFactorGrid(hostValues, seedValues)
  ])
  const result = {}
  for (const name of selectedSchemeNames()) {
    const definition = SEARCH_DEFINITIONS[name]
    const thresholdCandidates = buildThresholdCandidates(
      name,
      48,
      name === 'stable' ? 20260719 : 20260720
    )
    const ranked = []
    let processed = 0
    for (const homeTeamGoalFactor of homeValues) {
      for (const hostTeamGoalFactor of hostValues) {
        for (const seedTeamGoalFactor of seedValues) {
          const modelFactors = { homeTeamGoalFactor, hostTeamGoalFactor, seedTeamGoalFactor }
          const matches = combinedMatches(nonWorldCupGrid, worldCupGrid, modelFactors)
          for (const smoothingFactor of smoothingValues) {
            const best = findBestThresholds(
              matches,
              smoothingFactor,
              thresholdCandidates,
              definition,
              modelFactors
            )
            if (best) {
              addRankedResult(ranked, best.candidate, best.metrics, 12)
            }
            processed++
            if (processed % 250 === 0) {
              process.stderr.write(`${name} 粗搜 ${processed} / ${homeValues.length * hostValues.length * seedValues.length * smoothingValues.length}\n`)
            }
          }
        }
      }
    }
    result[name] = ranked
  }
  return result
}

async function refineCandidate(name, initial) {
  const definition = SEARCH_DEFINITIONS[name]
  let current = initial
  const localThresholds = buildThresholdCandidates(
    name,
    1200,
    name === 'stable' ? 20260721 : 20260722,
    current.candidate,
    true
  )

  const homeValues = factorRange(current.candidate.homeTeamGoalFactor, 0.35, 0.05, 0.10, 3.00)
  const nonWorldCupGrid = await loadNonWorldCupFactorGrid(homeValues)
  const initialHostValues = [current.candidate.hostTeamGoalFactor]
  const initialSeedValues = [current.candidate.seedTeamGoalFactor]
  let worldCupGrid = await loadWorldCupFactorGrid(initialHostValues, initialSeedValues)
  for (const homeTeamGoalFactor of homeValues) {
    const modelFactors = {
      homeTeamGoalFactor,
      hostTeamGoalFactor: current.candidate.hostTeamGoalFactor,
      seedTeamGoalFactor: current.candidate.seedTeamGoalFactor
    }
    const best = findBestThresholds(
      combinedMatches(nonWorldCupGrid, worldCupGrid, modelFactors),
      current.candidate.handicapSmoothingFactor,
      localThresholds,
      definition,
      modelFactors
    )
    if (best && best.metrics.roi > current.metrics.roi) {
      current = best
    }
  }

  const hostValues = factorRange(current.candidate.hostTeamGoalFactor, 0.35, 0.05, 0.10, 3.00)
  const seedValues = factorRange(current.candidate.seedTeamGoalFactor, 0.35, 0.05, 0.10, 3.00)
  worldCupGrid = await loadWorldCupFactorGrid(hostValues, seedValues)
  const currentNonWorldCupGrid = await loadNonWorldCupFactorGrid([current.candidate.homeTeamGoalFactor])
  for (const hostTeamGoalFactor of hostValues) {
    for (const seedTeamGoalFactor of seedValues) {
      const modelFactors = {
        homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
        hostTeamGoalFactor,
        seedTeamGoalFactor
      }
      const best = findBestThresholds(
        combinedMatches(currentNonWorldCupGrid, worldCupGrid, modelFactors),
        current.candidate.handicapSmoothingFactor,
        localThresholds,
        definition,
        modelFactors
      )
      if (best && best.metrics.roi > current.metrics.roi) {
        current = best
      }
    }
  }

  const finalMatches = combinedMatches(currentNonWorldCupGrid, worldCupGrid, {
    homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
    hostTeamGoalFactor: current.candidate.hostTeamGoalFactor,
    seedTeamGoalFactor: current.candidate.seedTeamGoalFactor
  })
  const smoothingValues = factorRange(current.candidate.handicapSmoothingFactor, 0.10, 0.01, 0, 0.8)
  for (const smoothingFactor of smoothingValues) {
    const best = findBestThresholds(
      finalMatches,
      smoothingFactor,
      localThresholds,
      definition,
      {
        homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
        hostTeamGoalFactor: current.candidate.hostTeamGoalFactor,
        seedTeamGoalFactor: current.candidate.seedTeamGoalFactor
      }
    )
    if (best && best.metrics.roi > current.metrics.roi) {
      current = best
    }
  }

  const finalThresholds = buildThresholdCandidates(
    name,
    200000,
    name === 'stable' ? 20260723 : 20260724,
    current.candidate,
    true
  )
  return findBestThresholds(
    finalMatches,
    current.candidate.handicapSmoothingFactor,
    finalThresholds,
    definition,
    {
      homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
      hostTeamGoalFactor: current.candidate.hostTeamGoalFactor,
      seedTeamGoalFactor: current.candidate.seedTeamGoalFactor
    }
  ) || current
}

async function runFullSearch() {
  const coarseResult = await runCoarseModelSearch()
  const result = {}
  for (const name of selectedSchemeNames()) {
    process.stderr.write(`${name} 开始精调\n`)
    const refined = await refineCandidate(name, coarseResult[name][0])
    result[name] = {
      coarseTop: coarseResult[name].slice(0, 5).map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      })),
      refined: {
        parameters: refined.candidate,
        metrics: printableMetrics(refined.metrics)
      }
    }
  }
  console.log(JSON.stringify({ simulations, result }, null, 2))
}

async function runTargetedSearch() {
  const result = {}
  for (const [name, initial] of selectedPresetEntries(TARGETED_CANDIDATES)) {
    const matches = await loadPresetMatches(initial, initial.modelMode || modelMode)
    const definition = SEARCH_DEFINITIONS[name]
    const smoothingThresholds = buildThresholdCandidates(
      name,
      targetedSmoothingCandidates,
      (name === 'stable' ? 20260725 : 20260726) + randomSeedOffset,
      initial,
      true
    )
    const smoothingValues = factorRange(0.40, 0.40, 0.005, 0, 0.8)
    const ranked = []
    for (const smoothingFactor of smoothingValues) {
      const best = findBestThresholds(
        matches,
        smoothingFactor,
        smoothingThresholds,
        definition,
        {
          homeTeamGoalFactor: initial.homeTeamGoalFactor,
          hostTeamGoalFactor: initial.hostTeamGoalFactor,
          seedTeamGoalFactor: initial.seedTeamGoalFactor
        }
      )
      if (best) {
        addRankedResult(ranked, best.candidate, best.metrics, 10)
      }
    }
    const bestSmoothing = ranked[0]
    const finalThresholds = buildThresholdCandidates(
      name,
      targetedFinalCandidates,
      (name === 'stable' ? 20260727 : 20260728) + randomSeedOffset,
      bestSmoothing.candidate,
      true
    )
    const refined = findBestThresholds(
      matches,
      bestSmoothing.candidate.handicapSmoothingFactor,
      finalThresholds,
      definition,
      {
        homeTeamGoalFactor: initial.homeTeamGoalFactor,
        hostTeamGoalFactor: initial.hostTeamGoalFactor,
        seedTeamGoalFactor: initial.seedTeamGoalFactor
      }
    )
    result[name] = {
      smoothingTop: ranked.slice(0, 5).map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      })),
      refined: {
        parameters: refined.candidate,
        metrics: printableMetrics(refined.metrics)
      }
    }
  }
  console.log(JSON.stringify({ simulations, result }, null, 2))
}

async function runThresholdSearch() {
  const definitions = {
    stable: {
      iterations: 300000,
      minimumRecommendedMatches: 500,
      minimumAverageOdds: 1.60,
      odds: [1.00, 2.50],
      handicapRecommendation: [0, 100],
      handicapReverse: [0, 100],
      singleRecommendation: [0, 100]
    },
    aggressive: {
      iterations: 300000,
      minimumRecommendedMatches: 100,
      minimumAverageOdds: 1.90,
      odds: [1.30, 4.00],
      handicapRecommendation: [0, 100],
      handicapReverse: [0, 100],
      singleRecommendation: [0, 100]
    }
  }
  const result = {}
  for (const [name, preset] of selectedPresetEntries()) {
    const definition = definitions[name]
    const matches = withSmoothing(
      await loadPresetMatches(preset, preset.modelMode || modelMode),
      preset.handicapSmoothingFactor
    )
    const random = createRandom(name === 'stable' ? 20260717 : 20260718)
    const ranked = []
    for (let index = 0; index < definition.iterations; index++) {
      const candidate = {
        ...preset,
        recommendationOdds: randomValue(random, ...definition.odds, 2),
        handicapRecommendationThreshold: randomValue(random, ...definition.handicapRecommendation, 2),
        handicapReverseThreshold: randomValue(random, ...definition.handicapReverse, 2),
        singleRecommendationThreshold: randomValue(random, ...definition.singleRecommendation, 2)
      }
      const metrics = evaluate(matches, candidate)
      if (isEligible(metrics, definition)) {
        addRankedResult(ranked, candidate, metrics)
      }
    }
    result[name] = ranked.map(item => ({
      parameters: item.candidate,
      metrics: printableMetrics(item.metrics)
    }))
  }
  console.log(JSON.stringify({ simulations, result }, null, 2))
}

function coordinateValues(minimum, maximum, step, scale) {
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

function isBetterCoordinateCandidate(candidate, current, definition) {
  if (!isEligible(current.metrics, definition)) {
    return true
  }
  const roiDifference = candidate.metrics.roi - current.metrics.roi
  if (roiDifference > 1e-12) {
    return true
  }
  if (Math.abs(roiDifference) > 1e-12) {
    return false
  }
  return candidate.metrics.recommendedMatchCount > current.metrics.recommendedMatchCount
}

function bestCoordinateValue(matches, current, definition, key, values) {
  let best = current
  for (const value of values) {
    if (value === current.candidate[key]) {
      continue
    }
    const candidate = {
      ...current.candidate,
      [key]: value
    }
    const metrics = evaluate(matches, candidate)
    if (!isEligible(metrics, definition)) {
      continue
    }
    const evaluated = { candidate, metrics }
    if (isBetterCoordinateCandidate(evaluated, best, definition)) {
      best = evaluated
    }
  }
  return best
}

function optimizeThresholdCoordinates(name, matches, initial) {
  const definition = SEARCH_DEFINITIONS[name]
  let current = {
    candidate: { ...initial },
    metrics: evaluate(withSmoothing(matches, initial.handicapSmoothingFactor), initial)
  }
  const coordinateDefinitions = [
    ['recommendationOdds', coordinateValues(1, 100, coordinateThresholdStep, 2)],
    ['handicapRecommendationThreshold', coordinateValues(0, 100, coordinateThresholdStep, 2)],
    ['handicapReverseThreshold', coordinateValues(0, 100, coordinateThresholdStep, 2)],
    ['singleRecommendationThreshold', coordinateValues(0, 100, coordinateThresholdStep, 2)]
  ]
  const smoothingValues = coordinateValues(0, 0.8, coordinateSmoothingStep, 3)

  for (let pass = 1; pass <= coordinatePasses; pass++) {
    const before = current
    let bestSmoothing = current
    for (const smoothingFactor of smoothingValues) {
      const candidate = {
        ...current.candidate,
        handicapSmoothingFactor: smoothingFactor
      }
      const metrics = evaluate(withSmoothing(matches, smoothingFactor), candidate)
      if (!isEligible(metrics, definition)) {
        continue
      }
      const evaluated = { candidate, metrics }
      if (isBetterCoordinateCandidate(evaluated, bestSmoothing, definition)) {
        bestSmoothing = evaluated
      }
    }
    current = bestSmoothing
    let smoothedMatches = withSmoothing(matches, current.candidate.handicapSmoothingFactor)
    for (const [key, values] of coordinateDefinitions) {
      current = bestCoordinateValue(smoothedMatches, current, definition, key, values)
    }
    process.stderr.write(`${name} 坐标穷举第 ${pass} 轮：${JSON.stringify(printableMetrics(current.metrics))}\n`)
    if (current.candidate.handicapSmoothingFactor !== before.candidate.handicapSmoothingFactor) {
      smoothedMatches = withSmoothing(matches, current.candidate.handicapSmoothingFactor)
    }
    if (current.metrics.roi <= before.metrics.roi + 1e-12) {
      break
    }
  }
  return current
}

async function runCoordinateSearch() {
  const result = {}
  for (const [name, preset] of selectedPresetEntries()) {
    const matches = await loadPresetMatches(preset, preset.modelMode || modelMode)
    const optimized = optimizeThresholdCoordinates(name, matches, preset)
    result[name] = {
      parameters: optimized.candidate,
      metrics: printableMetrics(optimized.metrics),
      verification: verificationMetrics(matches, optimized.candidate)
    }
  }
  console.log(JSON.stringify({
    simulations,
    coordinatePasses,
    coordinateThresholdStep,
    coordinateSmoothingStep,
    result
  }, null, 2))
}

function uniqueSortedNumbers(values) {
  return [...new Set(values.map(value => Number(value.toFixed(3))))]
    .sort((left, right) => left - right)
}

function isExplorationEligible(metrics, definition) {
  return metrics.recommendedMatchCount >= Math.floor(definition.minimumRecommendedMatches * 0.75) &&
    metrics.averageOddsIncludingMisses >= definition.minimumAverageOdds * 0.8
}

async function runHomeFactorGrid() {
  const presetEntries = selectedPresetEntries()
  const homeValues = uniqueSortedNumbers([
    ...coordinateValues(factorMinimum, factorMaximum, factorStep, 2),
    ...presetEntries.map(([, preset]) => preset.homeTeamGoalFactor)
  ])
  const nonWorldCupGrid = await loadNonWorldCupFactorGrid(homeValues)
  const result = {}
  for (const [name, preset] of presetEntries) {
    const definition = SEARCH_DEFINITIONS[name]
    const worldCupMatches = await fetchBacktest('WORLD_CUP', preset, preset.modelMode || modelMode)
    const coarseRanked = []
    for (const homeTeamGoalFactor of homeValues) {
      const candidate = { ...preset, homeTeamGoalFactor }
      const matches = combineMatchGroups(worldCupMatches, nonWorldCupGrid.get(homeTeamGoalFactor))
      const metrics = evaluate(withSmoothing(matches, candidate.handicapSmoothingFactor), candidate)
      if (isExplorationEligible(metrics, definition)) {
        addRankedResult(coarseRanked, candidate, metrics, factorTop)
      }
    }
    const refinedRanked = []
    for (const item of coarseRanked) {
      const matches = combineMatchGroups(
        worldCupMatches,
        nonWorldCupGrid.get(item.candidate.homeTeamGoalFactor)
      )
      const optimized = optimizeThresholdCoordinates(name, matches, item.candidate)
      if (isEligible(optimized.metrics, definition)) {
        addRankedResult(refinedRanked, optimized.candidate, optimized.metrics, factorTop)
      }
    }
    result[name] = {
      coarseTop: coarseRanked.map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      })),
      refinedTop: refinedRanked.map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      }))
    }
  }
  console.log(JSON.stringify({ simulations, factorStep, factorTop, result }, null, 2))
}

async function runWorldFactorGrid() {
  const presetEntries = selectedPresetEntries()
  const hostValues = uniqueSortedNumbers([
    ...coordinateValues(hostFactorMinimum, hostFactorMaximum, factorStep, 2),
    ...presetEntries.map(([, preset]) => preset.hostTeamGoalFactor)
  ])
  const seedValues = uniqueSortedNumbers([
    ...coordinateValues(seedFactorMinimum, seedFactorMaximum, factorStep, 2),
    ...presetEntries.map(([, preset]) => preset.seedTeamGoalFactor)
  ])
  const worldCupGrid = await loadWorldCupFactorGrid(hostValues, seedValues)
  const result = {}
  for (const [name, preset] of presetEntries) {
    const definition = SEARCH_DEFINITIONS[name]
    const nonWorldCupMatches = await fetchBacktest(NON_WORLD_CUP_COMPETITIONS, preset, preset.modelMode || modelMode)
    const coarseRanked = []
    for (const hostTeamGoalFactor of hostValues) {
      for (const seedTeamGoalFactor of seedValues) {
        const candidate = { ...preset, hostTeamGoalFactor, seedTeamGoalFactor }
        const worldCupMatches = worldCupGrid.get(`${hostTeamGoalFactor}|${seedTeamGoalFactor}`)
        const matches = combineMatchGroups(worldCupMatches, nonWorldCupMatches)
        const metrics = evaluate(withSmoothing(matches, candidate.handicapSmoothingFactor), candidate)
        if (isExplorationEligible(metrics, definition)) {
          addRankedResult(coarseRanked, candidate, metrics, factorTop)
        }
      }
    }
    const refinedRanked = []
    for (const item of coarseRanked) {
      const key = `${item.candidate.hostTeamGoalFactor}|${item.candidate.seedTeamGoalFactor}`
      const matches = combineMatchGroups(worldCupGrid.get(key), nonWorldCupMatches)
      const optimized = optimizeThresholdCoordinates(name, matches, item.candidate)
      if (isEligible(optimized.metrics, definition)) {
        addRankedResult(refinedRanked, optimized.candidate, optimized.metrics, factorTop)
      }
    }
    result[name] = {
      coarseTop: coarseRanked.map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      })),
      refinedTop: refinedRanked.map(item => ({
        parameters: item.candidate,
        metrics: printableMetrics(item.metrics)
      }))
    }
  }
  console.log(JSON.stringify({ simulations, factorStep, factorTop, result }, null, 2))
}

function chronologicalFolds(matches, foldCount = 3) {
  const sortedMatches = [...matches].sort((left, right) => {
    const dateOrder = String(left.matchDate || '').localeCompare(String(right.matchDate || ''))
    return dateOrder !== 0 ? dateOrder : String(left.matchId || '').localeCompare(String(right.matchId || ''))
  })
  return Array.from({ length: foldCount }, (_, foldIndex) => {
    const startIndex = Math.floor(sortedMatches.length * foldIndex / foldCount)
    const endIndex = Math.floor(sortedMatches.length * (foldIndex + 1) / foldCount)
    return sortedMatches.slice(startIndex, endIndex)
  })
}

function verificationMetrics(matches, parameters) {
  const smoothedMatches = withSmoothing(matches, parameters.handicapSmoothingFactor)
  const byCompetition = Object.fromEntries(
    [...new Set(smoothedMatches.map(match => match.competition))]
      .sort()
      .map(competition => [
        competition,
        printableMetrics(evaluate(
          smoothedMatches.filter(match => match.competition === competition),
          parameters
        ))
      ])
  )
  const byPeriod = chronologicalFolds(smoothedMatches).map((foldMatches, index) => ({
    period: index + 1,
    startDate: foldMatches[0]?.matchDate || null,
    endDate: foldMatches[foldMatches.length - 1]?.matchDate || null,
    metrics: printableMetrics(evaluate(foldMatches, parameters))
  }))
  return {
    full: printableMetrics(evaluate(smoothedMatches, parameters)),
    byCompetition,
    byPeriod
  }
}

function localParameterVariants(parameters) {
  const variants = []
  const definitions = [
    ['handicapSmoothingFactor', 0.05, 0, 0.8, 3],
    ['recommendationOdds', 0.05, 1, 100, 2],
    ['handicapRecommendationThreshold', 5, 0, 100, 2],
    ['handicapReverseThreshold', 5, 0, 100, 2],
    ['singleRecommendationThreshold', 5, 0, 100, 2]
  ]
  definitions.forEach(([key, delta, minimum, maximum, scale]) => {
    for (const direction of [-1, 1]) {
      variants.push({
        key,
        direction,
        parameters: {
          ...parameters,
          [key]: Number(Math.max(minimum, Math.min(maximum, parameters[key] + direction * delta)).toFixed(scale))
        }
      })
    }
  })
  return variants
}

function goalFactorVariants(parameters) {
  return ['hostTeamGoalFactor', 'homeTeamGoalFactor', 'seedTeamGoalFactor'].flatMap(key => {
    return [-1, 1].map(direction => ({
      key,
      direction,
      parameters: {
        ...parameters,
        [key]: Number(Math.max(0.1, Math.min(3, parameters[key] + direction * 0.1)).toFixed(2))
      }
    }))
  })
}

async function runVerification() {
  const result = {}
  for (const [name, parameters] of selectedPresetEntries(VERIFICATION_CANDIDATES)) {
    const effectiveModelMode = parameters.modelMode || modelMode
    const matches = await loadPresetMatches(parameters, effectiveModelMode)
    const localSensitivity = localParameterVariants(parameters).map(variant => ({
      parameter: variant.key,
      value: variant.parameters[variant.key],
      metrics: verificationMetrics(matches, variant.parameters).full
    }))
    const factorSensitivity = []
    for (const variant of goalFactorVariants(parameters)) {
      const variantMatches = await loadPresetMatches(variant.parameters, effectiveModelMode)
      factorSensitivity.push({
        parameter: variant.key,
        value: variant.parameters[variant.key],
        metrics: verificationMetrics(variantMatches, variant.parameters).full
      })
    }
    result[name] = {
      parameters,
      metrics: verificationMetrics(matches, parameters),
      localSensitivity,
      factorSensitivity
    }
  }
  console.log(JSON.stringify({ simulations, result }, null, 2))
}

if (phase === 'verify') {
  await runVerification()
} else if (phase === 'home-grid') {
  await runHomeFactorGrid()
} else if (phase === 'world-grid') {
  await runWorldFactorGrid()
} else if (phase === 'coordinate') {
  await runCoordinateSearch()
} else if (phase === 'targeted') {
  await runTargetedSearch()
} else if (phase === 'search') {
  await runFullSearch()
} else if (phase === 'threshold') {
  await runThresholdSearch()
} else {
  await runBaselines()
}
