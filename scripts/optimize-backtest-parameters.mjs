const BASE_URL = 'http://localhost:8080/api/football/recommendation-backtest'
const CLUB_COMPETITIONS = [
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
    hostTeamGoalFactor: 1.21,
    homeTeamGoalFactor: 1.05,
    seedTeamGoalFactor: 1.80,
    handicapSmoothingFactor: 0.200,
    recommendationOdds: 1.62,
    handicapRecommendationThreshold: 58.00,
    handicapReverseThreshold: 52.00,
    singleRecommendationThreshold: 73.00
  },
  aggressive: {
    hostTeamGoalFactor: 1.21,
    homeTeamGoalFactor: 1.05,
    seedTeamGoalFactor: 1.80,
    handicapSmoothingFactor: 0.200,
    recommendationOdds: 2.42,
    handicapRecommendationThreshold: 58.00,
    handicapReverseThreshold: 52.00,
    singleRecommendationThreshold: 71.00
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
const responseCache = new Map()

function roundToTwo(value) {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

function toProbabilityArray(probability) {
  return PROBABILITY_KEYS.map(key => Number(probability?.[key]) || 0)
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

function prepareMatches(matches) {
  return (matches || []).map(match => {
    const handicap = Number(match.sportteryHandicap)
    const score = parseScore(match.scoreText)
    return {
      competition: match.competition,
      homeTeamEn: match.homeTeamEn,
      awayTeamEn: match.awayTeamEn,
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
  return matches.map(match => ({
    ...match,
    handicapProbability: smoothHandicapProbability(
      match.rawHandicapProbability,
      match.normalProbability,
      smoothingFactor
    )
  }))
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
  let settledMatchCount = 0
  let settledOddsTotal = 0
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
      settledMatchCount++
      return
    }
    hitMatchCount++
    if (recommendation.selectionCount === 1) {
      return
    }
    settledMatchCount++
    settledOddsTotal += recommendation.odds[recommendation.actualIndex] || 0
  })
  const missMatchCount = recommendedMatchCount - hitMatchCount
  const averageRecommendations = recommendedMatchCount > 0
    ? recommendedSelectionCount / recommendedMatchCount
    : 0
  const hitRate = recommendedMatchCount > 0 ? hitMatchCount / recommendedMatchCount : 0
  const averageOddsIncludingMisses = settledMatchCount > 0
    ? settledOddsTotal / settledMatchCount
    : 0
  const roi = recommendedMatchCount > 0 && averageRecommendations > 0 && averageOddsIncludingMisses > 0
    ? Math.pow(hitRate * averageOddsIncludingMisses / averageRecommendations, 2) - 1
    : -1
  return {
    recommendedMatchCount,
    recommendedSelectionCount,
    hitMatchCount,
    missMatchCount,
    averageRecommendations,
    hitRate,
    averageOddsIncludingMisses,
    roi
  }
}

async function fetchBacktest(competition, factors) {
  const key = [competition, factors.hostTeamGoalFactor, factors.homeTeamGoalFactor, factors.seedTeamGoalFactor].join('|')
  if (responseCache.has(key)) {
    return responseCache.get(key)
  }
  const url = new URL(BASE_URL)
  url.searchParams.set('competition', competition)
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
  const result = prepareMatches(data.matches)
  responseCache.set(key, result)
  process.stderr.write(`读取 ${competition} ${result.length} 场，${((Date.now() - startedAt) / 1000).toFixed(1)} 秒\n`)
  return result
}

async function loadPresetMatches(preset) {
  const [worldCupMatches, clubMatches] = await Promise.all([
    fetchBacktest('WORLD_CUP', preset),
    fetchBacktest(CLUB_COMPETITIONS, preset)
  ])
  return worldCupMatches.concat(clubMatches)
}

function printableMetrics(metrics) {
  return {
    recommendedMatchCount: metrics.recommendedMatchCount,
    recommendedSelectionCount: metrics.recommendedSelectionCount,
    hitMatchCount: metrics.hitMatchCount,
    missMatchCount: metrics.missMatchCount,
    averageOddsIncludingMisses: Number(metrics.averageOddsIncludingMisses.toFixed(4)),
    averageRecommendations: Number(metrics.averageRecommendations.toFixed(4)),
    hitRate: Number((metrics.hitRate * 100).toFixed(2)),
    roi: Number((metrics.roi * 100).toFixed(2))
  }
}

async function runBaselines() {
  const result = {}
  for (const [name, preset] of Object.entries(PRESETS)) {
    const matches = await loadPresetMatches(preset)
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
    minimumRecommendedMatches: 120,
    minimumAverageOdds: 1.60,
    odds: [1.00, 1.70],
    handicapRecommendation: [35, 75],
    handicapReverse: [35, 75],
    singleRecommendation: [55, 85]
  },
  aggressive: {
    minimumRecommendedMatches: 80,
    minimumAverageOdds: 1.90,
    odds: [1.30, 2.30],
    handicapRecommendation: [35, 75],
    handicapReverse: [35, 75],
    singleRecommendation: [55, 85]
  }
}

const TARGETED_CANDIDATES = {
  stable: {
    ...PRESETS.stable
  },
  aggressive: {
    ...PRESETS.aggressive
  }
}

function isEligible(metrics, definition) {
  return metrics.recommendedMatchCount >= definition.minimumRecommendedMatches &&
    metrics.averageOddsIncludingMisses >= definition.minimumAverageOdds
}

function buildThresholdCandidates(name, count, seed, center = PRESETS[name], local = false) {
  const definition = SEARCH_DEFINITIONS[name]
  const random = createRandom(seed)
  const candidates = [{ ...center }, { ...PRESETS[name] }]
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

async function loadClubFactorGrid(homeValues) {
  const entries = await mapWithConcurrency(homeValues, 2, async homeTeamGoalFactor => {
    const factors = {
      hostTeamGoalFactor: 1,
      homeTeamGoalFactor,
      seedTeamGoalFactor: 1
    }
    return [homeTeamGoalFactor, await fetchBacktest(CLUB_COMPETITIONS, factors)]
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

function combinedMatches(clubGrid, worldCupGrid, factors) {
  const clubMatches = clubGrid.get(factors.homeTeamGoalFactor)
  const worldCupMatches = worldCupGrid.get(`${factors.hostTeamGoalFactor}|${factors.seedTeamGoalFactor}`)
  if (!clubMatches || !worldCupMatches) {
    throw new Error(`缺少模型因子数据 ${JSON.stringify(factors)}`)
  }
  return worldCupMatches.concat(clubMatches)
}

async function runCoarseModelSearch() {
  const homeValues = [0.96, 1.00, 1.04, 1.08, 1.12, 1.16, 1.20]
  const hostValues = [1.10, 1.18, 1.26, 1.34, 1.42]
  const seedValues = [1.41, 1.49, 1.57, 1.65, 1.73]
  const smoothingValues = [0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.61, 0.645, 0.70, 0.75, 0.80]
  const [clubGrid, worldCupGrid] = await Promise.all([
    loadClubFactorGrid(homeValues),
    loadWorldCupFactorGrid(hostValues, seedValues)
  ])
  const result = {}
  for (const name of Object.keys(PRESETS)) {
    const definition = SEARCH_DEFINITIONS[name]
    const thresholdCandidates = buildThresholdCandidates(
      name,
      70,
      name === 'stable' ? 20260719 : 20260720
    )
    const ranked = []
    let processed = 0
    for (const homeTeamGoalFactor of homeValues) {
      for (const hostTeamGoalFactor of hostValues) {
        for (const seedTeamGoalFactor of seedValues) {
          const modelFactors = { homeTeamGoalFactor, hostTeamGoalFactor, seedTeamGoalFactor }
          const matches = combinedMatches(clubGrid, worldCupGrid, modelFactors)
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

  const homeValues = factorRange(current.candidate.homeTeamGoalFactor, 0.04, 0.02, 0.90, 1.30)
  const clubGrid = await loadClubFactorGrid(homeValues)
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
      combinedMatches(clubGrid, worldCupGrid, modelFactors),
      current.candidate.handicapSmoothingFactor,
      localThresholds,
      definition,
      modelFactors
    )
    if (best && best.metrics.roi > current.metrics.roi) {
      current = best
    }
  }

  const hostValues = factorRange(current.candidate.hostTeamGoalFactor, 0.04, 0.02, 1.00, 1.60)
  const seedValues = factorRange(current.candidate.seedTeamGoalFactor, 0.04, 0.02, 1.30, 1.90)
  worldCupGrid = await loadWorldCupFactorGrid(hostValues, seedValues)
  const currentClubGrid = await loadClubFactorGrid([current.candidate.homeTeamGoalFactor])
  for (const hostTeamGoalFactor of hostValues) {
    for (const seedTeamGoalFactor of seedValues) {
      const modelFactors = {
        homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
        hostTeamGoalFactor,
        seedTeamGoalFactor
      }
      const best = findBestThresholds(
        combinedMatches(currentClubGrid, worldCupGrid, modelFactors),
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

  const finalMatches = combinedMatches(currentClubGrid, worldCupGrid, {
    homeTeamGoalFactor: current.candidate.homeTeamGoalFactor,
    hostTeamGoalFactor: current.candidate.hostTeamGoalFactor,
    seedTeamGoalFactor: current.candidate.seedTeamGoalFactor
  })
  const smoothingValues = factorRange(current.candidate.handicapSmoothingFactor, 0.025, 0.005, 0, 0.8)
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
    30000,
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
  for (const name of Object.keys(PRESETS)) {
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
  for (const [name, initial] of Object.entries(TARGETED_CANDIDATES)) {
    const matches = await loadPresetMatches(initial)
    const definition = SEARCH_DEFINITIONS[name]
    const smoothingThresholds = buildThresholdCandidates(
      name,
      800,
      name === 'stable' ? 20260725 : 20260726,
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
      50000,
      name === 'stable' ? 20260727 : 20260728,
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
      iterations: 30000,
      minimumRecommendedMatches: 350,
      odds: [1.00, 2.30],
      handicapRecommendation: [35, 75],
      handicapReverse: [35, 75],
      singleRecommendation: [35, 85]
    },
    aggressive: {
      iterations: 30000,
      minimumRecommendedMatches: 180,
      odds: [1.40, 3.00],
      handicapRecommendation: [35, 75],
      handicapReverse: [35, 75],
      singleRecommendation: [35, 85]
    }
  }
  const result = {}
  for (const [name, preset] of Object.entries(PRESETS)) {
    const definition = definitions[name]
    const matches = withSmoothing(await loadPresetMatches(preset), preset.handicapSmoothingFactor)
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
      if (metrics.recommendedMatchCount >= definition.minimumRecommendedMatches) {
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

if (phase === 'targeted') {
  await runTargetedSearch()
} else if (phase === 'search') {
  await runFullSearch()
} else if (phase === 'threshold') {
  await runThresholdSearch()
} else {
  await runBaselines()
}
