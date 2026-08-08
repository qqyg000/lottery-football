import {
  calculateFlatStakeBacktest,
  calculateReturnVolatility,
  calculateSamplingRate
} from './backtest-roi.mjs'

export const PROBABILITY_KEYS = ['win', 'draw', 'lose']

export const TOTAL_GOALS_KEYS = [0, 1, 2, 3, 4, 5, 6, 7]

const TOTAL_GOALS_ODDS_KEYS = [
  'goal0',
  'goal1',
  'goal2',
  'goal3',
  'goal4',
  'goal5',
  'goal6',
  'goal7Plus'
]

const DEFAULT_TOTAL_GOALS_STRATEGY = {
  minimumProbability: 5,
  minimumExpectedValue: 1.05,
  minimumOdds: 1.01,
  maximumOdds: 50,
  maximumSelections: 2
}

const DEFAULT_GLOBAL_PARAMETERS = {
  recommendationOdds: 1.03,
  handicapRecommendationThreshold: 68.16,
  handicapReverseThreshold: 46.78,
  singleRecommendationThreshold: 71.72
}

export function hasAvailableMarketOdds(odds) {
  return Boolean(odds && PROBABILITY_KEYS.some(key => {
    const value = Number(odds[key])
    return Number.isFinite(value) && value > 0
  }))
}

export function getAutomaticMarketSelection(match) {
  if (!match || !match.sportteryMatchId) {
    return null
  }
  const selection = {}
  if (match.sportteryNormalAvailable === true && hasAvailableMarketOdds(match.sportteryNormalOdds)) {
    selection.normal = true
  }
  const handicap = Number(match.sportteryHandicap)
  if (
    Number.isInteger(handicap) &&
    handicap !== 0 &&
    hasAvailableMarketOdds(match.sportteryHandicapOdds)
  ) {
    selection.handicap = 'handicap-' + handicap
  }
  return selection
}

export function getEffectiveMarketSelection(match, selectedRows = {}) {
  const manualSelection = selectedRows && match
    ? selectedRows[match.matchId]
    : null
  if (manualSelection && manualSelection.manualOverride === true) {
    return manualSelection
  }
  return getAutomaticMarketSelection(match) || {}
}

export function createProbabilityRows(match, modelMode = 'after') {
  const useAdjusted = modelMode === 'after'
  const handicapProbabilities = useAdjusted && Array.isArray(match?.adjustedHandicapProbabilities)
    ? match.adjustedHandicapProbabilities
    : (Array.isArray(match?.handicapProbabilities) ? match.handicapProbabilities : [])
  const normalProbability = useAdjusted && match?.adjustedNormalProbability
    ? match.adjustedNormalProbability
    : (match?.normalProbability || { win: 0, draw: 0, lose: 0 })
  const handicapRows = handicapProbabilities.map(item => ({
    key: 'handicap-' + item.handicap,
    handicap: Number(item.handicap),
    label: item.handicapName,
    probability: item.probability || { win: 0, draw: 0, lose: 0 },
    rowClass: 'handicap-row'
  }))
  return handicapRows.concat({
    key: 'normal',
    handicap: 0,
    label: '不让球（0）',
    probability: normalProbability,
    rowClass: 'normal-row'
  }).sort((left, right) => right.handicap - left.handicap)
}

export function getRecommendationKeys(match, options = {}) {
  const rows = createProbabilityRows(match, options.modelMode)
  const globalParameters = normalizeGlobalParameters(options.globalParameters)
  const selectedRows = rows.filter(row => isRowSelected(match, row, options.selectedRows))
  const baseKeys = getBaseRecommendationKeys(selectedRows, globalParameters)
  const recommendationKeys = applySingleRecommendationThreshold(rows, baseKeys, globalParameters)
  return hasQualifiedRecommendationOdds(match, rows, recommendationKeys, globalParameters)
    ? recommendationKeys
    : new Set()
}

export function getRecommendationOddsDetails(match, options = {}) {
  const score = parseScore(match)
  const recommendationKeys = getRecommendationKeys(match, options)
  if (!score || recommendationKeys.size === 0) {
    return []
  }
  return createProbabilityRows(match, options.modelMode).reduce((result, row) => {
    const actualProbabilityKey = getActualProbabilityKey(score, row.handicap)
    PROBABILITY_KEYS.forEach(probabilityKey => {
      if (!recommendationKeys.has(getRecommendationCellKey(row, probabilityKey))) {
        return
      }
      const odds = sportteryOddsValue(match, row, probabilityKey)
      if (odds !== null) {
        result.push({
          odds,
          winning: probabilityKey === actualProbabilityKey
        })
      }
    })
    return result
  }, [])
}

export function getRecommendationResult(match, options = {}) {
  const score = parseScore(match)
  if (!score) {
    return ''
  }
  const recommendationKeys = getRecommendationKeys(match, options)
  if (recommendationKeys.size === 0) {
    return ''
  }
  const hit = createProbabilityRows(match, options.modelMode).some(row => {
    const actualKey = getActualProbabilityKey(score, row.handicap)
    return recommendationKeys.has(getRecommendationCellKey(row, actualKey))
  })
  return hit ? 'hit' : 'miss'
}

export function evaluateRecommendationBacktest(matches, options = {}) {
  const sourceMatches = Array.isArray(matches) ? matches : []
  const recommendedMatches = []
  const hitMatches = []
  const winningMatchOdds = []
  const matchReturnRates = []
  let recommendedSelectionCount = 0
  let winningSelectionCount = 0

  sourceMatches.forEach(match => {
    const matchOptions = {
      modelMode: options.modelMode,
      selectedRows: options.selectedRows,
      globalParameters: typeof options.resolveGlobalParameters === 'function'
        ? options.resolveGlobalParameters(match)
        : options.globalParameters
    }
    const recommendationKeys = getRecommendationKeys(match, matchOptions)
    if (recommendationKeys.size === 0) {
      return
    }
    recommendedMatches.push(match)
    recommendedSelectionCount += recommendationKeys.size
    const recommendationOdds = getRecommendationOddsDetails(match, matchOptions)
    const winningSelections = recommendationOdds.filter(item => item.winning)
    const winningOdds = winningSelections.reduce((sum, item) => sum + item.odds, 0)
    matchReturnRates.push(winningOdds / recommendationKeys.size - 1)
    if (winningSelections.length === 0) {
      return
    }
    hitMatches.push(match)
    winningSelectionCount += winningSelections.length
    winningMatchOdds.push(winningOdds)
  })

  const financials = calculateFlatStakeBacktest(
    winningMatchOdds,
    recommendedSelectionCount,
    recommendedMatches.length
  )
  const oddsMatchCount = Number(options.oddsMatchCount) || sourceMatches.length
  return {
    recommendedMatches,
    hitMatches,
    summary: {
      samplingRate: calculateSamplingRate(recommendedMatches.length, oddsMatchCount),
      recommendedMatchCount: recommendedMatches.length,
      recommendedSelectionCount,
      hitMatchCount: hitMatches.length,
      missMatchCount: recommendedMatches.length - hitMatches.length,
      winningSelectionCount,
      averageWinningOdds: average(winningMatchOdds),
      averageOddsIncludingMisses: financials.averageReturnIncludingMisses,
      totalStake: financials.totalStake,
      totalReturn: financials.totalReturn,
      netProfit: financials.netProfit,
      volatility: calculateReturnVolatility(matchReturnRates),
      roi: financials.roi
    }
  }
}

export function getTotalGoalsRecommendations(match, options = {}) {
  const strategy = normalizeTotalGoalsStrategy(options.strategy)
  if (strategy.maximumSelections === 0 || !match?.sportteryTotalGoalsOdds) {
    return []
  }
  const probabilities = options.modelMode === 'after' && Array.isArray(match.adjustedSportteryTotalGoalsProbabilities)
    ? match.adjustedSportteryTotalGoalsProbabilities
    : (Array.isArray(match.sportteryTotalGoalsProbabilities)
        ? match.sportteryTotalGoalsProbabilities
        : [])
  const probabilityByGoals = new Map(probabilities.map(item => [Number(item.totalGoals), Number(item.probability)]))
  return TOTAL_GOALS_KEYS.reduce((recommendations, totalGoals, index) => {
    const probability = probabilityByGoals.get(totalGoals)
    const odds = Number(match.sportteryTotalGoalsOdds[TOTAL_GOALS_ODDS_KEYS[index]])
    const expectedValue = probability / 100 * odds
    if (
      Number.isFinite(probability) &&
      Number.isFinite(odds) &&
      odds > 0 &&
      probability >= strategy.minimumProbability &&
      odds >= strategy.minimumOdds &&
      odds <= strategy.maximumOdds &&
      expectedValue >= strategy.minimumExpectedValue
    ) {
      recommendations.push({
        key: 'total-goals-' + totalGoals,
        totalGoals,
        resultName: totalGoals === 7 ? '7+球' : totalGoals + '球',
        probability,
        odds,
        expectedValue
      })
    }
    return recommendations
  }, [])
    .sort((left, right) => right.probability - left.probability || right.expectedValue - left.expectedValue || left.totalGoals - right.totalGoals)
    .slice(0, strategy.maximumSelections)
}

export function evaluateTotalGoalsBacktest(matches, options = {}) {
  const sourceMatches = Array.isArray(matches) ? matches : []
  let recommendedMatchCount = 0
  let recommendedSelectionCount = 0
  let winningSelectionCount = 0
  let totalReturn = 0
  const matchReturnRates = []

  sourceMatches.forEach(match => {
    const score = parseScore(match)
    if (!score) {
      return
    }
    const recommendations = getTotalGoalsRecommendations(match, {
      modelMode: options.modelMode,
      strategy: typeof options.resolveStrategy === 'function'
        ? options.resolveStrategy(match)
        : options.strategy
    })
    if (recommendations.length === 0) {
      return
    }
    recommendedMatchCount += 1
    recommendedSelectionCount += recommendations.length
    const actualTotalGoals = score.home + score.away
    const winningRecommendation = recommendations.find(item => (
      item.totalGoals === actualTotalGoals || (item.totalGoals === 7 && actualTotalGoals >= 7)
    ))
    const matchReturn = winningRecommendation ? winningRecommendation.odds : 0
    totalReturn += matchReturn
    winningSelectionCount += winningRecommendation ? 1 : 0
    matchReturnRates.push(matchReturn / recommendations.length - 1)
  })

  const totalStake = recommendedSelectionCount
  const oddsMatchCount = Number(options.oddsMatchCount) || sourceMatches.length
  return {
    recommendedMatchCount,
    recommendedSelectionCount,
    winningSelectionCount,
    totalStake,
    totalReturn,
    netProfit: totalReturn - totalStake,
    roi: totalStake > 0 ? totalReturn / totalStake - 1 : null,
    hitRate: totalStake > 0 ? winningSelectionCount / totalStake : null,
    samplingRate: calculateSamplingRate(recommendedMatchCount, oddsMatchCount),
    volatility: calculateReturnVolatility(matchReturnRates)
  }
}

function normalizeTotalGoalsStrategy(strategy) {
  const source = strategy && typeof strategy === 'object' ? strategy : {}
  const minimumOdds = normalizeNumber(
    source.minimumOdds,
    DEFAULT_TOTAL_GOALS_STRATEGY.minimumOdds,
    1,
    100
  )
  return {
    minimumProbability: normalizeNumber(
      source.minimumProbability,
      DEFAULT_TOTAL_GOALS_STRATEGY.minimumProbability,
      0,
      100
    ),
    minimumExpectedValue: normalizeNumber(
      source.minimumExpectedValue,
      DEFAULT_TOTAL_GOALS_STRATEGY.minimumExpectedValue,
      0,
      10
    ),
    minimumOdds,
    maximumOdds: normalizeNumber(
      source.maximumOdds,
      DEFAULT_TOTAL_GOALS_STRATEGY.maximumOdds,
      minimumOdds,
      1000
    ),
    maximumSelections: Math.round(normalizeNumber(
      source.maximumSelections,
      DEFAULT_TOTAL_GOALS_STRATEGY.maximumSelections,
      0,
      4
    ))
  }
}

function normalizeGlobalParameters(parameters) {
  const source = parameters && typeof parameters === 'object' ? parameters : {}
  return {
    recommendationOdds: normalizeNumber(source.recommendationOdds, DEFAULT_GLOBAL_PARAMETERS.recommendationOdds, 1, 100),
    handicapRecommendationThreshold: normalizeNumber(source.handicapRecommendationThreshold, DEFAULT_GLOBAL_PARAMETERS.handicapRecommendationThreshold, 0, 100),
    handicapReverseThreshold: normalizeNumber(source.handicapReverseThreshold, DEFAULT_GLOBAL_PARAMETERS.handicapReverseThreshold, 0, 100),
    singleRecommendationThreshold: normalizeNumber(source.singleRecommendationThreshold, DEFAULT_GLOBAL_PARAMETERS.singleRecommendationThreshold, 0, 100)
  }
}

function normalizeNumber(value, fallback, minimum, maximum) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue)
    ? Math.max(minimum, Math.min(maximum, numberValue))
    : fallback
}

function isRowSelected(match, row, selectedRows) {
  const selection = getEffectiveMarketSelection(match, selectedRows)
  return row.handicap === 0
    ? selection.normal === true
    : selection.handicap === row.key
}

function getBaseRecommendationKeys(selectedRows, globalParameters) {
  if (selectedRows.length === 0) {
    return new Set()
  }
  const normalRows = selectedRows.filter(row => row.handicap === 0)
  const handicapRows = selectedRows.filter(row => row.handicap !== 0)
  if (normalRows.length > 0 && handicapRows.length > 0) {
    const pairSwitchKeys = buildHandicapPairSwitchKeys(normalRows[0], handicapRows[0], selectedRows, globalParameters)
    if (pairSwitchKeys) {
      return pairSwitchKeys
    }
    return buildRecommendationKeys(selectedRows, true, globalParameters)
  }
  if (handicapRows.length > 0) {
    return buildRecommendationKeys(handicapRows, false, globalParameters)
  }
  return buildRecommendationKeys(normalRows, false, globalParameters)
}

function buildRecommendationKeys(rows, applyHandicapThreshold, globalParameters) {
  const maxCell = findMaxProbabilityCell(rows)
  if (!maxCell) {
    return new Set()
  }
  if (
    applyHandicapThreshold &&
    maxCell.row.handicap !== 0 &&
    maxCell.probabilityKey !== 'draw' &&
    maxCell.value < globalParameters.handicapReverseThreshold
  ) {
    return new Set(PROBABILITY_KEYS
      .filter(key => key !== maxCell.probabilityKey)
      .map(key => getRecommendationCellKey(maxCell.row, key)))
  }
  return new Set(getAdjacentProbabilityKeys(maxCell.probabilityKey)
    .map(key => getRecommendationCellKey(maxCell.row, key)))
}

function buildHandicapPairSwitchKeys(normalRow, handicapRow, rows, globalParameters) {
  const maxCell = findMaxProbabilityCell(rows)
  if (!maxCell || maxCell.row !== normalRow) {
    return null
  }
  if (maxCell.probabilityKey !== 'win' && maxCell.probabilityKey !== 'lose') {
    return null
  }
  const handicapValue = Number(handicapRow.probability[maxCell.probabilityKey]) || 0
  if (
    handicapValue >= globalParameters.handicapRecommendationThreshold &&
    handicapValue < maxCell.value
  ) {
    return new Set([maxCell.probabilityKey, 'draw']
      .map(key => getRecommendationCellKey(handicapRow, key)))
  }
  return null
}

function applySingleRecommendationThreshold(rows, recommendationKeys, globalParameters) {
  if (!recommendationKeys || recommendationKeys.size !== 2) {
    return recommendationKeys
  }
  let strongestRecommendation = null
  rows.forEach(row => {
    PROBABILITY_KEYS.forEach(probabilityKey => {
      const key = getRecommendationCellKey(row, probabilityKey)
      if (!recommendationKeys.has(key)) {
        return
      }
      const value = Number(row.probability[probabilityKey]) || 0
      if (!strongestRecommendation || value > strongestRecommendation.value) {
        strongestRecommendation = { key, value }
      }
    })
  })
  return strongestRecommendation && strongestRecommendation.value > globalParameters.singleRecommendationThreshold
    ? new Set([strongestRecommendation.key])
    : recommendationKeys
}

function hasQualifiedRecommendationOdds(match, rows, recommendationKeys, globalParameters) {
  if (!recommendationKeys || recommendationKeys.size === 0) {
    return false
  }
  const recommendationOdds = []
  rows.forEach(row => {
    PROBABILITY_KEYS.forEach(probabilityKey => {
      if (recommendationKeys.has(getRecommendationCellKey(row, probabilityKey))) {
        recommendationOdds.push(sportteryOddsValue(match, row, probabilityKey))
      }
    })
  })
  return recommendationOdds.length === recommendationKeys.size &&
    recommendationOdds.every(odds => odds !== null && odds >= globalParameters.recommendationOdds)
}

function sportteryOddsValue(match, row, probabilityKey) {
  const odds = row.handicap === 0
    ? match?.sportteryNormalOdds
    : (Number(match?.sportteryHandicap) === Number(row.handicap)
        ? match?.sportteryHandicapOdds
        : null)
  const value = odds ? Number(odds[probabilityKey]) : NaN
  return Number.isFinite(value) && value > 0 ? value : null
}

function findMaxProbabilityCell(rows) {
  let maxCell = null
  rows.forEach(row => {
    PROBABILITY_KEYS.forEach(probabilityKey => {
      const value = Number(row.probability[probabilityKey]) || 0
      if (!maxCell || value > maxCell.value) {
        maxCell = { row, probabilityKey, value }
      }
    })
  })
  return maxCell
}

function getAdjacentProbabilityKeys(probabilityKey) {
  if (probabilityKey === 'draw') {
    return ['draw']
  }
  const index = PROBABILITY_KEYS.indexOf(probabilityKey)
  return index < 0
    ? []
    : PROBABILITY_KEYS.filter((key, keyIndex) => Math.abs(keyIndex - index) <= 1)
}

function getRecommendationCellKey(row, probabilityKey) {
  return row.key + '-' + probabilityKey
}

function parseScore(match) {
  const result = String(match?.scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
  return result ? { home: Number(result[1]), away: Number(result[2]) } : null
}

function getActualProbabilityKey(score, handicap) {
  const adjustedHomeScore = score.home + Number(handicap)
  if (adjustedHomeScore > score.away) {
    return 'win'
  }
  return adjustedHomeScore === score.away ? 'draw' : 'lose'
}

function average(values) {
  return values.length > 0
    ? values.reduce((sum, value) => sum + value, 0) / values.length
    : null
}
