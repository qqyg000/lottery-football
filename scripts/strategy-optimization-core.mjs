import { prepareRecommendationMatch } from './wdl-fast-evaluator.mjs'

export const DEFAULT_MODEL = {
  hostTeamGoalFactor: 1.1, homeTeamGoalFactor: 1.06, seedTeamGoalFactor: 1.85,
  officialMatchWeight: 1, internationalFriendlyWeight: 0.5, clubFriendlyWeight: 0.3,
  handicapSmoothingFactor: 0.274
}
export const DEFAULT_RULE = {
  recommendationOdds: 1, handicapRecommendationThreshold: 68,
  handicapReverseThreshold: 47, singleRecommendationThreshold: 72
}
export const DEFAULT_GOALS = {
  minimumProbability: 5, minimumExpectedValue: 1.05,
  minimumOdds: 1, maximumOdds: 1000, maximumSelections: 2
}
const ODDS_KEYS = ['goal0', 'goal1', 'goal2', 'goal3', 'goal4', 'goal5', 'goal6', 'goal7Plus']
export const RISK_SETTINGS = {
  STABLE: { priorMatches: 30, uncertainty: 1.28, dispersion: 0.3, downside: 0.2 },
  AGGRESSIVE: { priorMatches: 15, uncertainty: 0.45, dispersion: 0.1, downside: 0.05 }
}

export function randomGenerator(text) {
  let seed = 2166136261
  for (const character of String(text)) seed = Math.imul(seed ^ character.charCodeAt(0), 16777619)
  return () => ((seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0) / 4294967296)
}

export function chronologicalPlan(matches, freshFrom, testFraction = 0.2) {
  const dates = [...new Set(matches.map(match => match.matchDate))].sort()
  if (dates.some(date => !/^\d{4}-\d{2}-\d{2}$/.test(date))) throw new Error('比赛日期不完整，不能划分时间样本')
  let testFrom = dates.length > 1 ? dates[Math.max(1, Math.floor(dates.length * (1 - testFraction)))] : null
  if (freshFrom && dates.some(date => date >= freshFrom) && (!testFrom || freshFrom < testFrom)) testFrom = freshFrom
  const developmentDates = dates.filter(date => !testFrom || date < testFrom)
  const blockCount = Math.min(4, developmentDates.length)
  const dateToBlock = Object.fromEntries(developmentDates.map((date, index) => [
    date, Math.min(blockCount - 1, Math.floor(index * blockCount / developmentDates.length))
  ]))
  return { testFrom, freshFrom, blockCount, dateToBlock, developmentDates, testFraction }
}

export function prepareMatches(matches, market, plan) {
  return [...matches].sort((a, b) => String(a.matchDate).localeCompare(String(b.matchDate)) ||
    String(a.matchId).localeCompare(String(b.matchId))).map(match => {
    let prepared
    if (market === 'WDL') {
      prepared = prepareRecommendationMatch(match)
    } else {
      const score = String(match.scoreText || '').match(/(\d+)\s*-\s*(\d+)/)
      const actual = score ? Math.min(7, Number(score[1]) + Number(score[2])) : -1
      const probabilities = new Map((match.adjustedSportteryTotalGoalsProbabilities ||
        match.sportteryTotalGoalsProbabilities || []).map(item => [Number(item.totalGoals), Number(item.probability)]))
      prepared = ODDS_KEYS.map((key, goals) => ({
        probability: probabilities.get(goals), odds: Number(match.sportteryTotalGoalsOdds?.[key]),
        goals, winning: goals === actual
      })).filter(item => Number.isFinite(item.probability) && Number.isFinite(item.odds) && item.odds > 0)
        .map(item => ({ ...item, expectedValue: item.probability / 100 * item.odds }))
        .sort((a, b) => b.probability - a.probability || b.expectedValue - a.expectedValue || a.goals - b.goals)
    }
    return { date: match.matchDate, id: match.matchId, block: plan.dateToBlock[match.matchDate] ?? -1, prepared }
  })
}

function blankMetrics() {
  return { availableMatchCount: 0, recommendedMatchCount: 0, recommendedSelectionCount: 0,
    winningSelectionCount: 0, hitMatchCount: 0, totalStake: 0, totalReturn: 0,
    returnRateSum: 0, returnRateSquaredSum: 0 }
}

export function finishMetrics(metrics) {
  const n = metrics.recommendedMatchCount
  return { ...metrics,
    netProfit: metrics.totalReturn - metrics.totalStake,
    roi: metrics.totalStake > 0 ? metrics.totalReturn / metrics.totalStake - 1 : null,
    samplingRate: metrics.availableMatchCount > 0 ? n / metrics.availableMatchCount : null,
    hitRate: n > 0 ? metrics.hitMatchCount / n : null,
    selectionHitRate: metrics.totalStake > 0 ? metrics.winningSelectionCount / metrics.totalStake : null,
    volatility: n > 1 ? Math.sqrt(Math.max(0,
      (metrics.returnRateSquaredSum - metrics.returnRateSum ** 2 / n) / (n - 1))) : null
  }
}

export function combineMetrics(metrics) {
  const combined = blankMetrics()
  for (const item of metrics) for (const key of Object.keys(combined)) combined[key] += item[key] || 0
  return finishMetrics(combined)
}

// 仅约束合法参数取值，采样率、命中率、最低 ROI 和两个方案的相互关系均不作为门槛
export function evaluateBlocks(matches, parameters, market, blockCount = 1) {
  const blocks = Array.from({ length: blockCount }, blankMetrics)
  for (const match of matches) {
    if (match.block < 0 || match.block >= blockCount) continue
    const metrics = blocks[match.block]
    metrics.availableMatchCount++
    let count = 0
    let amount = 0
    let wins = 0
    const prepared = match.prepared
    if (market === 'WDL') {
      if (!prepared) continue
      const choice = prepared.switch && prepared.switchProbability >= parameters.handicapRecommendationThreshold
        ? prepared.switch : prepared.reverse && prepared.maximum < parameters.handicapReverseThreshold
          ? prepared.reverse : prepared.base
      const selected = choice.pair.count === 2 && choice.strongest > parameters.singleRecommendationThreshold
        ? choice.single : choice.pair
      if (selected.minimumOdds < parameters.recommendationOdds) continue
      count = selected.count
      amount = selected.returnValue
      wins = selected.wins
    } else {
      for (const item of prepared) {
        if (count >= parameters.maximumSelections) break
        if (item.probability < parameters.minimumProbability || item.expectedValue < parameters.minimumExpectedValue ||
            item.odds < parameters.minimumOdds || item.odds > parameters.maximumOdds) continue
        count++
        if (item.winning) { amount += item.odds; wins++ }
      }
    }
    if (!count) continue
    metrics.recommendedMatchCount++
    metrics.recommendedSelectionCount += count
    metrics.totalStake += count
    metrics.totalReturn += amount
    metrics.winningSelectionCount += wins
    metrics.hitMatchCount += Number(wins > 0)
    const rate = amount / count - 1
    metrics.returnRateSum += rate
    metrics.returnRateSquaredSum += rate * rate
  }
  return blocks.map(finishMetrics)
}

// 固定先验与风险系数在运行前确定，不根据测试段表现调节
export function riskAdjustedScore(blocks, preset) {
  const metrics = combineMetrics(blocks)
  if (!metrics.totalStake) return -Infinity
  const risk = RISK_SETTINGS[preset]
  const n = metrics.recommendedMatchCount
  const shrink = value => (value.totalReturn - value.totalStake - 0.05 * risk.priorMatches) /
    (value.totalStake + risk.priorMatches)
  const shrunk = shrink(metrics)
  const variance = ((metrics.volatility || 0) ** 2 * Math.max(0, n - 1) + risk.priorMatches * 2.25) /
    (n + risk.priorMatches)
  const uncertainty = Math.sqrt(variance / (n + risk.priorMatches))
  const blockRois = blocks.map(shrink)
  const mean = blockRois.reduce((sum, value) => sum + value, 0) / blockRois.length
  const dispersion = Math.sqrt(blockRois.reduce((sum, value) => sum + (value - mean) ** 2, 0) / blockRois.length)
  return shrunk - risk.uncertainty * uncertainty - risk.dispersion * dispersion -
    risk.downside * Math.max(0, -Math.min(...blockRois))
}

export function ruleCandidates(count, seed, originals = []) {
  const random = randomGenerator(seed)
  const pick = values => values[Math.floor(random() * values.length)]
  const values = [0, 10, 20, 30, 40, 45, 50, 55, 60, 65, 70, 75, 80, 90, 100]
  const candidates = [...originals, DEFAULT_RULE]
  for (const threshold of [0, 50, 70, 100]) for (const odds of [1, 1.5, 2, 3, 5, 10]) {
    candidates.push({ ...DEFAULT_RULE, recommendationOdds: odds, singleRecommendationThreshold: threshold })
  }
  while (candidates.length < count) {
    const threshold = () => random() < 0.5 ? pick(values) : Math.round(random() * 200) / 2
    candidates.push({
      recommendationOdds: Math.min(100, Math.round(Math.exp(random() * Math.log(100)) * 20) / 20),
      handicapRecommendationThreshold: threshold(), handicapReverseThreshold: threshold(),
      singleRecommendationThreshold: threshold()
    })
  }
  return [...new Map(candidates.map(item => [JSON.stringify(item), item])).values()]
}

export function goalCandidates(count, seed, original = DEFAULT_GOALS) {
  const random = randomGenerator(seed)
  const pick = values => values[Math.floor(random() * values.length)]
  const candidates = [original, DEFAULT_GOALS]
  for (const minimumProbability of [0, 5, 10, 15, 20, 25, 30, 40]) {
    for (const minimumExpectedValue of [0, 0.5, 0.8, 1, 1.1, 1.25, 1.5, 2]) {
      for (const minimumOdds of [1, 2, 3, 4, 5]) {
        for (const maximumOdds of [4, 6, 10, 20, 50, 1000]) {
          if (minimumOdds > maximumOdds) continue
          for (const maximumSelections of [1, 2, 3, 4]) candidates.push({
            minimumProbability, minimumExpectedValue, minimumOdds, maximumOdds, maximumSelections
          })
        }
      }
    }
  }
  while (candidates.length < count) {
    const minimumOdds = Math.round(Math.exp(random() * Math.log(100)) * 10) / 10
    candidates.push({
      minimumProbability: random() < 0.8 ? Math.round(random() * 80) / 2 : Math.round(random() * 200) / 2,
      minimumExpectedValue: random() < 0.8 ? Math.round(random() * 50) / 20 : Math.round(random() * 200) / 20,
      minimumOdds, maximumOdds: Math.max(minimumOdds, pick([4, 5, 6, 8, 10, 15, 20, 50, 100, 1000])),
      maximumSelections: pick([1, 2, 3, 4])
    })
  }
  return [...new Map(candidates.map(item => [JSON.stringify(item), item])).values()]
}

export function searchCandidates(matches, candidates, market, blockCount, presets = ['STABLE', 'AGGRESSIVE']) {
  const results = Object.fromEntries(presets.map(preset => [preset, {
    best: null, folds: Array.from({ length: Math.max(0, blockCount - 1) }, () => null),
    evaluatedCandidates: candidates.length
  }]))
  if (!blockCount) return results
  // 测试比赛在搜索入口就剔除，任何候选和排序均无法读取其赛果
  const development = matches.filter(match => match.block >= 0 && match.block < blockCount)
  for (const parameters of candidates) {
    const blocks = evaluateBlocks(development, parameters, market, blockCount)
    for (const preset of presets) {
      const result = results[preset]
      const score = riskAdjustedScore(blocks, preset)
      if (Number.isFinite(score) && (!result.best || score > result.best.score + 1e-12)) {
        result.best = { parameters, score, blocks, training: combineMetrics(blocks) }
      }
      for (let prefix = 1; prefix < blockCount; prefix++) {
        const training = blocks.slice(0, prefix)
        const foldScore = riskAdjustedScore(training, preset)
        if (Number.isFinite(foldScore) && (!result.folds[prefix - 1] || foldScore > result.folds[prefix - 1].score + 1e-12)) {
          result.folds[prefix - 1] = { parameters, score: foldScore,
            training: combineMetrics(training), validation: blocks[prefix] }
        }
      }
    }
  }
  return results
}
