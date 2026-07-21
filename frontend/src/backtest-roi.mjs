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

export function calculateSamplingRate(coveredMatchCount, totalMatchCount) {
  const coveredCount = normalizeCount(coveredMatchCount)
  const totalCount = normalizeCount(totalMatchCount)
  return totalCount > 0 ? coveredCount / totalCount : null
}

export function calculateMinimumCoveredMatchCount(totalMatchCount, minimumSamplingRate) {
  const totalCount = Math.floor(normalizeCount(totalMatchCount))
  const minimumRate = Number(minimumSamplingRate)
  return totalCount > 0 && Number.isFinite(minimumRate) && minimumRate >= 0
    ? Math.floor(totalCount * minimumRate) + 1
    : 0
}

function normalizeCount(value) {
  const numberValue = Number(value)
  return Number.isFinite(numberValue) && numberValue > 0 ? numberValue : 0
}
