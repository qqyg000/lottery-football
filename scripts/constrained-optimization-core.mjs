import { chronologicalSplitIndex } from './chronological-validation.mjs'
import {
  DEFAULT_MODEL, DEFAULT_RULE, DEFAULT_GOALS, randomGenerator,
  evaluateBlocks, combineMetrics, riskAdjustedScore
} from './strategy-optimization-core.mjs'

export const POLICY = Object.freeze({
  validationFraction: 0.3, minimumTrainingMatches: 10, minimumValidationMatches: 6,
  trainingBlocks: 3, wdlHoldoutLimit: 24, goalsHoldoutLimit: 12,
  samplingTolerance: 0.03, stableMinimumRoi: 0.05,
  goalTiers: [0.333, 0.25, 0.2, 0.1]
})
const EPSILON = 1e-9
const signature = value => JSON.stringify(value)
const round = (value, digits = 2) => Number(value.toFixed(digits))
const clamp = (value, low, high) => Math.max(low, Math.min(high, value))

export function validationPlan(matches) {
  const sorted = [...matches].sort((a, b) => a.matchDate.localeCompare(b.matchDate) || String(a.matchId).localeCompare(String(b.matchId)))
  if (sorted.some(match => !/^\d{4}-\d{2}-\d{2}$/.test(match.matchDate))) throw new Error('回测比赛缺少有效日期')
  const index = chronologicalSplitIndex(sorted, POLICY.validationFraction, POLICY.minimumTrainingMatches, POLICY.minimumValidationMatches)
  const training = index < 0 ? [] : sorted.slice(0, index)
  const dates = [...new Set(training.map(match => match.matchDate))]
  const blockCount = Math.min(POLICY.trainingBlocks, dates.length, Math.floor(training.length / 3))
  const dateToBlock = Object.fromEntries(dates.map((date, i) => [date, Math.min(blockCount - 1, Math.floor(i * blockCount / dates.length))]))
  return { available: index >= 0, validationFrom: index < 0 ? null : sorted[index].matchDate,
    trainingMatchCount: training.length, validationMatchCount: index < 0 ? 0 : sorted.length - index,
    blockCount, dateToBlock }
}

export function minimumWdlSampling(competition, range, preset) {
  if (preset === 'STABLE') return competition === 'EUROPEAN_CHAMPIONSHIP' && range === 'CURRENT' ? 0.4 : 0.6
  if (range === 'CURRENT' && ['EUROPEAN_CHAMPIONSHIP', 'FINNISH_VEIKKAUSLIIGA'].includes(competition)) return 0.333
  return competition === 'K_LEAGUE_1' && range === 'CURRENT' ? 0.4 : 0.5
}

function samplingPass(metrics, competition, range, preset) {
  const rate = metrics?.samplingRate
  if (!Number.isFinite(rate)) return false
  const minimum = minimumWdlSampling(competition, range, preset)
  const strict = preset === 'AGGRESSIVE' && range === 'CURRENT' && ['EUROPEAN_CHAMPIONSHIP', 'FINNISH_VEIKKAUSLIIGA'].includes(competition)
  return strict ? rate > minimum + EPSILON : rate + EPSILON >= minimum
}

export function wdlTrainingPass(metrics, competition, range, preset) {
  return metrics?.totalStake > 0 && samplingPass(metrics, competition, range, preset) && metrics.roi + EPSILON >= 0
}

export function wdlCandidatePass(candidate, competition, range, preset) {
  if (!candidate || ![candidate.training, candidate.validation, candidate.full].every(metrics => wdlTrainingPass(metrics, competition, range, preset))) return false
  return candidate.full.roi + EPSILON >= (preset === 'STABLE' ? POLICY.stableMinimumRoi : 0)
}

export function pairTrainingPass(stable, aggressive) {
  return aggressive.training.roi > stable.training.roi + EPSILON && aggressive.training.samplingRate <= stable.training.samplingRate + EPSILON
}

export function wdlPairPass(stable, aggressive, competition, range) {
  return wdlCandidatePass(stable, competition, range, 'STABLE') && wdlCandidatePass(aggressive, competition, range, 'AGGRESSIVE') &&
    pairTrainingPass(stable, aggressive) && aggressive.full.roi > stable.full.roi + EPSILON &&
    ['full', 'validation'].every(period => aggressive[period].samplingRate <= stable[period].samplingRate + EPSILON)
}

export function samplingWindow(competition, range, preset, baselineRate) {
  const floor = minimumWdlSampling(competition, range, preset)
  const preferred = range === 'CURRENT' && preset === 'AGGRESSIVE'
    ? competition === 'EUROPEAN_CHAMPIONSHIP' ? [0.49, 0.51] : competition === 'FINNISH_VEIKKAUSLIIGA' ? [0.48, 0.52] : null : null
  const minimum = Math.max(floor, Number.isFinite(baselineRate) ? baselineRate - POLICY.samplingTolerance : 0, preferred?.[0] || 0)
  const maximum = Math.min(1, Number.isFinite(baselineRate) ? baselineRate + POLICY.samplingTolerance : 1, preferred?.[1] || 1)
  return minimum <= maximum + EPSILON ? { minimum, maximum } : { minimum: preferred?.[0] ?? floor, maximum: preferred?.[1] ?? 1 }
}

export function withinWindow(metrics, window) {
  return metrics.samplingRate + EPSILON >= window.minimum && metrics.samplingRate <= window.maximum + EPSILON
}

export function goalsTrainingPass(metrics, tier) {
  return metrics?.totalStake > 0 && metrics.samplingRate > tier + EPSILON && metrics.selectionHitRate > tier + EPSILON && metrics.roi > EPSILON
}

export function goalsCandidatePass(candidate, tier) {
  return goalsTrainingPass(candidate?.training, tier) && goalsTrainingPass(candidate?.full, tier) &&
    candidate.validation?.totalStake > 0 && candidate.validation.samplingRate > tier + EPSILON &&
    candidate.validation.selectionHitRate > tier + EPSILON && candidate.validation.roi + EPSILON >= 0
}

// 候选模型和阈值只由固定种子、原配置及合法取值范围生成
export function modelCandidates(profiles, count, seed) {
  const random = randomGenerator(seed)
  const limits = { hostTeamGoalFactor: [0.1, 3], homeTeamGoalFactor: [0.1, 3], seedTeamGoalFactor: [0.1, 3],
    officialMatchWeight: [1, 3], internationalFriendlyWeight: [0, 1], clubFriendlyWeight: [0, 1], handicapSmoothingFactor: [0, 0.8] }
  const initial = profiles.map(profile => ({ ...DEFAULT_MODEL, ...profile?.modelFactors }))
  initial.push({ ...DEFAULT_MODEL })
  const models = [...new Map(initial.map(model => [signature(model), model])).values()]
  for (let attempt = 0; models.length < count && attempt < count * 10; attempt++) {
    const center = initial[attempt % initial.length]
    const local = attempt % 4 !== 3
    const model = Object.fromEntries(Object.entries(limits).map(([key, [low, high]]) => [key,
      round(clamp(local ? center[key] + (random() - 0.5) * (high - low) * 0.35 : low + random() * (high - low), low, high), key === 'handicapSmoothingFactor' ? 3 : 2)
    ]))
    if (!models.some(old => signature(old) === signature(model))) models.push(model)
  }
  return models
}

export function wdlRules(count, seed, originals) {
  const random = randomGenerator(seed)
  const rules = [...originals, { ...DEFAULT_RULE }]
  const thresholds = [0, 30, 40, 45, 50, 55, 60, 65, 70, 75, 80, 90, 100]
  const odds = [1, 1.05, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.8, 2, 2.2, 2.5, 3, 4, 5, 8]
  for (const center of [...originals, DEFAULT_RULE]) for (const recommendationOdds of odds) {
    for (const singleRecommendationThreshold of thresholds) rules.push({ ...center, recommendationOdds, singleRecommendationThreshold })
  }
  while (rules.length < count) {
    const center = originals[Math.floor(random() * originals.length)] || DEFAULT_RULE
    const threshold = key => round(random() < 0.6 ? clamp(center[key] + (random() - 0.5) * 40, 0, 100) : thresholds[Math.floor(random() * thresholds.length)])
    rules.push({ recommendationOdds: random() < 0.75 ? odds[Math.floor(random() * odds.length)] : round(1 + random() * 3),
      handicapRecommendationThreshold: threshold('handicapRecommendationThreshold'), handicapReverseThreshold: threshold('handicapReverseThreshold'),
      singleRecommendationThreshold: threshold('singleRecommendationThreshold') })
  }
  return [...new Map(rules.map(rule => [signature(rule), rule])).values()].slice(0, count)
}

export function goalsRules(count, seed, original) {
  const random = randomGenerator(seed)
  const rules = [original, { ...DEFAULT_GOALS }]
  const pick = values => values[Math.floor(random() * values.length)]
  while (rules.length < count) {
    const minimumOdds = pick([1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5, 6, 8, 10])
    rules.push({ minimumProbability: pick([0, 2.5, 5, 7.5, 10, 12.5, 15, 17.5, 20, 22.5, 25, 30, 35]),
      minimumExpectedValue: pick([0, 0.5, 0.7, 0.8, 0.9, 1, 1.05, 1.1, 1.15, 1.2, 1.3, 1.5, 1.8, 2]),
      minimumOdds, maximumOdds: Math.max(minimumOdds, pick([4, 5, 6, 8, 10, 15, 20, 30, 50, 100, 1000])), maximumSelections: pick([1, 2, 3, 4]) })
  }
  return [...new Map(rules.map(rule => [signature(rule), rule])).values()]
}

export function trainingScore(blocks, preset) {
  return riskAdjustedScore(blocks, preset)
}

// 按逐块投注数、返奖和命中数去重，避免留出名额被同一结算的微小阈值变体占满
export function settlementSignature(candidate) {
  return candidate.blocks.map(block => [block.recommendedMatchCount, block.totalStake, round(block.totalReturn, 6), block.winningSelectionCount].join('/')).join('|')
}

export function offerCandidate(pool, candidate, limit = 96) {
  if (!Number.isFinite(candidate.score)) return
  candidate.signature ||= settlementSignature(candidate)
  const duplicate = pool.findIndex(item => item.signature === candidate.signature)
  if (duplicate >= 0) {
    if (pool[duplicate].score >= candidate.score - 1e-12) return
    pool.splice(duplicate, 1)
  }
  if (pool.length >= limit && candidate.score <= pool.at(-1).score) return
  const index = pool.findIndex(item => item.score < candidate.score - 1e-12)
  pool.splice(index < 0 ? pool.length : index, 0, candidate)
  if (pool.length > limit) pool.pop()
}

export function parameterNeighbors(parameters, market) {
  const changes = market === 'WDL'
    ? { recommendationOdds: [0.05, 1, 100], handicapRecommendationThreshold: [2, 0, 100], handicapReverseThreshold: [2, 0, 100], singleRecommendationThreshold: [2, 0, 100] }
    : { minimumProbability: [1, 0, 100], minimumExpectedValue: [0.05, 0, 10], minimumOdds: [0.1, 1, 100], maximumOdds: [0.5, parameters.minimumOdds, 1000] }
  return Object.entries(changes).flatMap(([key, [step, low, high]]) => [-1, 1].map(direction => {
    const next = { ...parameters, [key]: round(clamp(parameters[key] + direction * step, low, high)) }
    if (market === 'GOALS') next.maximumOdds = Math.max(next.minimumOdds, next.maximumOdds)
    return next
  }))
}

export function checkNeighborhood(candidate, prepared, market, blockCount, preset) {
  const metrics = parameterNeighbors(candidate.parameters, market).map(parameters => combineMetrics(evaluateBlocks(prepared, parameters, market, blockCount)))
  const values = metrics.map(item => item.roi ?? -1).sort((a, b) => a - b)
  const medianRoi = (values[3] + values[4]) / 2
  const fragilityPenalty = 0.25 * Math.max(0, candidate.training.roi - medianRoi)
  return { ...candidate, score: candidate.score - fragilityPenalty,
    neighborhood: { minimumRoi: values[0], medianRoi, positiveFraction: values.filter(value => value >= 0).length / values.length, fragilityPenalty }, preset }
}

export function freezeShortlist(pool, baseline, window, limit) {
  const sorted = [...pool].sort((a, b) => b.score - a.score || a.modelIndex - b.modelIndex || signature(a.parameters).localeCompare(signature(b.parameters)))
  const preferred = window ? sorted.filter(candidate => withinWindow(candidate.training, window)).slice(0, Math.floor(limit / 2)) : []
  const selected = []
  const seen = new Set()
  for (const candidate of [baseline, ...preferred, ...sorted].filter(Boolean)) {
    const key = `${candidate.modelIndex}:${signature(candidate.parameters)}`
    if (seen.has(key)) continue
    seen.add(key)
    selected.push(candidate)
    if (selected.length === limit) break
  }
  return selected.sort((a, b) => b.score - a.score)
}

// 验证段只执行通过或拒绝，不参与候选顺序或分数
export function selectWdlPair(shortlists, context, evaluate) {
  const { competition, range, windows } = context
  const pairs = []
  for (const stable of shortlists.STABLE) for (const aggressive of shortlists.AGGRESSIVE) {
    if (pairTrainingPass(stable, aggressive)) pairs.push({ stable, aggressive, score: stable.score * 0.6 + aggressive.score * 0.4 })
  }
  pairs.sort((a, b) => b.score - a.score)
  const evaluated = new Map()
  const get = candidate => {
    if (!evaluated.has(candidate)) evaluated.set(candidate, evaluate(candidate))
    return evaluated.get(candidate)
  }
  for (const relaxed of [false, true]) for (const pair of pairs) {
    if (!relaxed && !['STABLE', 'AGGRESSIVE'].every(preset => withinWindow(pair[preset === 'STABLE' ? 'stable' : 'aggressive'].training, windows[preset]))) continue
    const stable = get(pair.stable)
    const aggressive = get(pair.aggressive)
    if (!wdlPairPass(stable, aggressive, competition, range)) continue
    if (!relaxed && !['STABLE', 'AGGRESSIVE'].every(preset => withinWindow((preset === 'STABLE' ? stable : aggressive).full, windows[preset]))) continue
    return { stable, aggressive, relaxed, evaluatedCandidates: evaluated.size }
  }
  return { stable: null, aggressive: null, relaxed: true, evaluatedCandidates: evaluated.size }
}

export function selectGoals(shortlists, evaluate) {
  const evaluated = new Map()
  for (const tier of POLICY.goalTiers) for (const candidate of shortlists[String(tier)] || []) {
    if (!evaluated.has(candidate)) evaluated.set(candidate, evaluate(candidate))
    const checked = evaluated.get(candidate)
    if (goalsCandidatePass(checked, tier)) return { candidate: checked, tier, evaluatedCandidates: evaluated.size }
  }
  return { candidate: null, tier: null, evaluatedCandidates: evaluated.size }
}
