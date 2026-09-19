import {
  createProbabilityRows,
  getAutomaticMarketSelection
} from '../frontend/src/recommendation-backtest.mjs'
import { calculateReturnVolatility } from '../frontend/src/backtest-roi.mjs'

const KEYS = ['win', 'draw', 'lose']
const preparedMatches = new WeakMap()

// 仅供固定比赛快照的离线搜索使用，最终验收仍使用页面回测算法
function prepareMatch(match) {
  const cached = preparedMatches.get(match)
  if (cached) return cached
  const selection = getAutomaticMarketSelection(match)
  const rows = createProbabilityRows(match, 'after')
  const selected = rows.filter(row => row.handicap === 0
    ? selection?.normal === true
    : selection?.handicap === row.key)
  if (!selected.length) return null
  let maximum = null
  for (const row of selected) {
    for (const key of KEYS) {
      const value = Number(row.probability[key]) || 0
      if (!maximum || value > maximum.value) maximum = { row, key, value }
    }
  }
  const score = String(match.scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
  function outcome(row, keys) {
    const odds = row.handicap === 0 ? match.sportteryNormalOdds : match.sportteryHandicapOdds
    const actual = score
      ? Math.sign(Number(score[1]) + row.handicap - Number(score[2]))
      : null
    const cells = KEYS.filter(key => keys.includes(key)).map(key => ({
      probability: Number(row.probability[key]) || 0,
      odds: Number(odds?.[key]),
      winning: actual !== null && key === (actual > 0 ? 'win' : actual === 0 ? 'draw' : 'lose')
    }))
    function summarize(items) {
      const valid = items.every(item => Number.isFinite(item.odds) && item.odds > 0)
      const winning = items.filter(item => item.winning)
      return {
        count: items.length,
        minimumOdds: valid ? Math.min(...items.map(item => item.odds)) : -Infinity,
        returnValue: valid ? winning.reduce((sum, item) => sum + item.odds, 0) : 0,
        wins: valid ? winning.length : 0
      }
    }
    let strongest = cells[0]
    for (const cell of cells) if (cell.probability > strongest.probability) strongest = cell
    return {
      pair: summarize(cells),
      single: summarize([strongest]),
      strongest: strongest.probability
    }
  }
  const normal = selected.find(row => row.handicap === 0)
  const handicap = selected.find(row => row.handicap !== 0)
  const adjacent = maximum.key === 'draw' ? ['draw'] : [maximum.key, 'draw']
  const prepared = {
    base: outcome(maximum.row, adjacent),
    reverse: null,
    switch: null,
    maximum: maximum.value,
    switchProbability: -Infinity
  }
  if (normal && handicap) {
    if (maximum.row === normal && maximum.key !== 'draw') {
      const probability = Number(handicap.probability[maximum.key]) || 0
      if (probability < maximum.value) {
        prepared.switch = outcome(handicap, adjacent)
        prepared.switchProbability = probability
      }
    } else if (maximum.row.handicap !== 0 && maximum.key !== 'draw') {
      prepared.reverse = outcome(maximum.row, KEYS.filter(key => key !== maximum.key))
    }
  }
  preparedMatches.set(match, prepared)
  return prepared
}

export function evaluatePreparedRecommendationSummary(matches, globalParameters, oddsMatchCount = matches.length) {
  let recommendedMatchCount = 0
  let recommendedSelectionCount = 0
  let hitMatchCount = 0
  let winningSelectionCount = 0
  let totalReturn = 0
  const returns = []
  for (const match of matches) {
    const prepared = prepareMatch(match)
    if (!prepared) continue
    const choice = prepared.switch && prepared.switchProbability >= globalParameters.handicapRecommendationThreshold
      ? prepared.switch
      : prepared.reverse && prepared.maximum < globalParameters.handicapReverseThreshold
        ? prepared.reverse
        : prepared.base
    const selected = choice.pair.count === 2 && choice.strongest > globalParameters.singleRecommendationThreshold
      ? choice.single
      : choice.pair
    if (selected.minimumOdds < globalParameters.recommendationOdds) continue
    recommendedMatchCount++
    recommendedSelectionCount += selected.count
    winningSelectionCount += selected.wins
    totalReturn += selected.returnValue
    if (selected.wins > 0) hitMatchCount++
    returns.push(selected.returnValue / selected.count - 1)
  }
  return {
    samplingRate: oddsMatchCount > 0 ? recommendedMatchCount / oddsMatchCount : null,
    recommendedMatchCount,
    recommendedSelectionCount,
    hitMatchCount,
    missMatchCount: recommendedMatchCount - hitMatchCount,
    winningSelectionCount,
    averageWinningOdds: hitMatchCount > 0 ? totalReturn / hitMatchCount : null,
    averageOddsIncludingMisses: recommendedMatchCount > 0 ? totalReturn / recommendedMatchCount : null,
    totalStake: recommendedSelectionCount,
    totalReturn,
    netProfit: totalReturn - recommendedSelectionCount,
    volatility: calculateReturnVolatility(returns),
    roi: recommendedSelectionCount > 0 ? totalReturn / recommendedSelectionCount - 1 : null
  }
}
