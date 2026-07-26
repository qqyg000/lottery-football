export function calculateFlatStakeBacktest(
  winningMatchReturns,
  recommendedSelectionCount,
  recommendedMatchCount
) {
  const returns = Array.isArray(winningMatchReturns)
    ? winningMatchReturns.map(Number).filter(value => Number.isFinite(value) && value >= 0)
    : []
  const totalStake = normalizeCount(recommendedSelectionCount)
  const settledMatchCount = normalizeCount(recommendedMatchCount)
  const totalReturn = returns.reduce((sum, value) => sum + value, 0)

  return {
    totalStake,
    totalReturn,
    netProfit: totalReturn - totalStake,
    roi: totalStake > 0 ? totalReturn / totalStake - 1 : null,
    averageReturnIncludingMisses: settledMatchCount > 0 ? totalReturn / settledMatchCount : null
  }
}

export function calculateSamplingRate(recommendedMatchCount, oddsMatchCount) {
  const recommendedCount = normalizeCount(recommendedMatchCount)
  const availableOddsCount = normalizeCount(oddsMatchCount)
  return availableOddsCount > 0 ? recommendedCount / availableOddsCount : null
}

export function calculateReturnVolatility(matchReturnRates) {
  const returns = Array.isArray(matchReturnRates)
    ? matchReturnRates.map(Number).filter(value => Number.isFinite(value))
    : []
  if (returns.length < 2) {
    return null
  }

  const averageReturn = returns.reduce((sum, value) => sum + value, 0) / returns.length
  const squaredDeviationTotal = returns.reduce((sum, value) => {
    const deviation = value - averageReturn
    return sum + deviation * deviation
  }, 0)
  return Math.sqrt(squaredDeviationTotal / (returns.length - 1))
}

export function calculateMinimumCoveredMatchCount(oddsMatchCount, minimumSamplingRate) {
  const availableOddsCount = Math.floor(normalizeCount(oddsMatchCount))
  const minimumRate = Number(minimumSamplingRate)
  return availableOddsCount > 0 && Number.isFinite(minimumRate) && minimumRate >= 0
    ? Math.floor(availableOddsCount * minimumRate) + 1
    : 0
}

function normalizeCount(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : 0
}
