import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
import path from 'node:path'
import { chronologicalSplitIndex } from './chronological-validation.mjs'
import { DEFAULT_MODEL, randomGenerator } from './strategy-optimization-core.mjs'

export const SCORE_POLICY = Object.freeze({
  minimumTrainingMatches: 20, minimumValidationMatches: 12, minimumTrainingDates: 6, minimumValidationDates: 3,
  validationFraction: 0.3, minimumGainMatches: 2, minimumGainRate: 0.02, maximumLogLossIncrease: 0.03,
  bootstrapSamples: 2000, bootstrapLowerQuantile: 0.10
})

const key = match => `${match.matchDate}:${match.matchId}`
const modelKey = model => JSON.stringify(Object.keys(DEFAULT_MODEL)
  .filter(name => name !== 'handicapSmoothingFactor').map(name => model[name]))
const previousDay = date => new Date(Date.parse(date) - 86400000).toISOString().slice(0, 10)

export async function fetchScoreBacktest(options, model, { through = options.asOfDate, simulations } = {}) {
  const url = new URL('/api/football/score-backtest', options.apiBase)
  url.search = new URLSearchParams({ competition: options.competition,
    includePreviousEdition: String(options.range === 'PREVIOUS'), through, ...model })
  if (simulations !== undefined) url.searchParams.set('simulations', simulations)
  const response = await fetch(url, { signal: AbortSignal.timeout(600000) })
  if (!response.ok) throw new Error(`${url.pathname} ${response.status}: ${(await response.text()).slice(0, 1000)}`)
  const matches = await response.json()
  assert.equal(new Set(matches.map(key)).size, matches.length, '比分回测出现重复比赛')
  for (const match of matches) {
    assert.match(match.matchDate, /^\d{4}-\d{2}-\d{2}$/)
    assert.ok(match.matchDate <= through)
    assert.ok([match.homeScore, match.awayScore].every(score => Number.isInteger(score) && score >= 0))
    assert.ok([match.expectedHomeGoals, match.expectedAwayGoals].every(goals => Number.isFinite(goals) && goals > 0))
    if (simulations !== undefined) assert.equal(match.topScores.length, 3, `缺少前3比分 ${key(match)}`)
  }
  return matches
}

export function scorePlan(matches, freshFrom) {
  const sorted = [...matches].sort((a, b) => key(a).localeCompare(key(b)))
  const enough = (training, validation) => training.length >= SCORE_POLICY.minimumTrainingMatches &&
    validation.length >= SCORE_POLICY.minimumValidationMatches &&
    new Set(training.map(match => match.matchDate)).size >= SCORE_POLICY.minimumTrainingDates &&
    new Set(validation.map(match => match.matchDate)).size >= SCORE_POLICY.minimumValidationDates
  const fresh = sorted.findIndex(match => match.matchDate >= freshFrom)
  let index = fresh > 0 && enough(sorted.slice(0, fresh), sorted.slice(fresh)) ? fresh : -1
  const historicalIndex = chronologicalSplitIndex(sorted, SCORE_POLICY.validationFraction,
    SCORE_POLICY.minimumTrainingMatches, SCORE_POLICY.minimumValidationMatches)
  if (index < 0 && historicalIndex >= 0) {
    const boundaryDate = sorted[Math.min(historicalIndex, fresh < 0 ? historicalIndex : fresh)].matchDate
    const boundary = sorted.findIndex(match => match.matchDate === boundaryDate)
    if (enough(sorted.slice(0, boundary), sorted.slice(boundary))) index = boundary
  }
  const training = index >= 0 ? sorted.slice(0, index) : []
  const dates = [...new Set(training.map(match => match.matchDate))]
  const dateToBlock = Object.fromEntries(dates.map((date, i) => [date, Math.floor(i * 3 / dates.length)]))
  return {
    validationFrom: index >= 0 ? sorted[index].matchDate : null,
    validationKind: index < 0 ? 'INSUFFICIENT_DATA' : index === fresh ? 'NEW_MATCHES' : 'HISTORICAL_TAIL',
    trainingMatches: training.length, validationMatches: index >= 0 ? sorted.length - index : 0,
    freshFrom, dateToBlock
  }
}

function candidateModels(competition, originals) {
  const neutral = { ...DEFAULT_MODEL }
  if (competition === 'WORLD_CUP') neutral.seedTeamGoalFactor = 1.15
  const models = [neutral, ...originals.map(profile => profile.modelFactors)]
  for (const homeTeamGoalFactor of [0.85, 0.95, 1.05, 1.15, 1.25, 1.4, 1.55]) {
    for (const officialMatchWeight of [1, 1.45, 2]) {
      models.push({ ...neutral, homeTeamGoalFactor, officialMatchWeight })
    }
  }
  if (competition === 'WORLD_CUP') {
    for (const seedTeamGoalFactor of [0.95, 1.05, 1.25, 1.4]) {
      for (const hostTeamGoalFactor of [1.0, 1.2]) {
        models.push({ ...neutral, homeTeamGoalFactor: 1.15, hostTeamGoalFactor, seedTeamGoalFactor })
      }
    }
  }
  return [...new Map(models.map(model => [modelKey(model), model])).values()]
}

function poissonLoss(goals, mean) {
  let logFactorial = 0
  for (let i = 2; i <= goals; i++) logFactorial += Math.log(i)
  return mean - goals * Math.log(mean) + logFactorial
}

function scoreObservation(match, simulated) {
  const { homeScore, awayScore, expectedHomeGoals: home, expectedAwayGoals: away } = match
  const loss = poissonLoss(homeScore, home) + poissonLoss(awayScore, away)
  if (simulated) {
    return { hit: Number(match.topScores.some(score => score.homeScore === homeScore && score.awayScore === awayScore)), loss }
  }
  // 现有模型的期望进球上限为4.5，泊松分布前3比分都在0至8球的网格内
  const actual = -loss
  let ahead = 0
  for (let h = 0; h <= 8; h++) for (let a = 0; a <= 8; a++) {
    const mass = -poissonLoss(h, home) - poissonLoss(a, away)
    if (mass > actual + 1e-12 || (Math.abs(mass - actual) <= 1e-12 &&
        (h + a < homeScore + awayScore || h + a === homeScore + awayScore && h < homeScore))) ahead++
    if (ahead >= 3) return { hit: 0, loss }
  }
  return { hit: 1, loss }
}

function stats(observations) {
  const matches = observations.length
  const hits = observations.reduce((total, item) => total + item.hit, 0)
  return { matches, hits, hitRate: matches ? hits / matches : null,
    logLoss: matches ? observations.reduce((total, item) => total + item.loss, 0) / matches : null }
}

function compare(oldMatches, newMatches, seed) {
  assert.deepEqual(newMatches.map(match => [key(match), match.homeScore, match.awayScore]),
    oldMatches.map(match => [key(match), match.homeScore, match.awayScore]), '比分回测场次或实际比分不一致')
  const before = oldMatches.map(match => scoreObservation(match, true))
  const after = newMatches.map(match => scoreObservation(match, true))
  const days = new Map()
  const gains = after.map((item, i) => {
    const gain = item.hit - before[i].hit
    const date = oldMatches[i].matchDate
    const bucket = days.get(date) || { gain: 0, matches: 0 }
    bucket.gain += gain
    bucket.matches++
    days.set(date, bucket)
    return gain
  })
  let lowerBound = null
  if (seed && days.size >= SCORE_POLICY.minimumValidationDates) {
    const blocks = [...days.values()]
    const random = randomGenerator(seed)
    const draws = Array.from({ length: SCORE_POLICY.bootstrapSamples }, () => {
      let gain = 0
      let matches = 0
      for (let i = 0; i < blocks.length; i++) {
        const block = blocks[Math.floor(random() * blocks.length)]
        gain += block.gain
        matches += block.matches
      }
      return gain / matches
    }).sort((a, b) => a - b)
    lowerBound = draws[Math.floor(draws.length * SCORE_POLICY.bootstrapLowerQuantile)]
  }
  const newOnly = gains.filter(gain => gain > 0).length
  const oldOnly = gains.filter(gain => gain < 0).length
  return { before: stats(before), after: stats(after), newOnly, oldOnly, netHits: newOnly - oldOnly,
    dates: days.size, favorableDates: [...days.values()].filter(day => day.gain > 0).length, lowerBound }
}

function trainingScore(matches, plan, model) {
  const observations = matches.map(match => scoreObservation(match, false))
  const full = stats(observations)
  const blocks = [0, 1, 2].map(block => stats(observations.filter((_, i) => plan.dateToBlock[matches[i].matchDate] === block)))
  const average = blocks.reduce((sum, block) => sum + block.hitRate, 0) / blocks.length
  const spread = Math.sqrt(blocks.reduce((sum, block) => sum + (block.hitRate - average) ** 2, 0) / blocks.length)
  const distance = Math.abs(model.homeTeamGoalFactor - 1.1) + 0.25 * Math.abs(model.officialMatchWeight - 1)
  return { full, blocks, score: full.hitRate - 0.25 * spread - 0.05 * full.logLoss - 0.01 * distance }
}

export function scorePass(training, validation) {
  return training.netHits > 0 && validation.netHits >= Math.max(SCORE_POLICY.minimumGainMatches,
    Math.ceil(validation.before.matches * SCORE_POLICY.minimumGainRate)) && validation.favorableDates >= 2 &&
    validation.lowerBound !== null && validation.lowerBound >= 0 &&
    validation.after.logLoss - validation.before.logLoss <= SCORE_POLICY.maximumLogLossIncrease
}

export async function optimizeScoreModels(options, originals) {
  const models = candidateModels(options.competition, originals)
  const reference = await fetchScoreBacktest(options, originals[0].modelFactors)
  const plan = scorePlan(reference, options.recentFrom)
  const ranked = []
  if (plan.validationFrom) {
    const through = previousDay(plan.validationFrom)
    const expected = reference.filter(match => match.matchDate <= through).map(key)
    for (const model of models) {
      const matches = modelKey(model) === modelKey(originals[0].modelFactors)
        ? reference.filter(match => match.matchDate <= through) : await fetchScoreBacktest(options, model, { through })
      assert.deepEqual(matches.map(key), expected, '比分候选的训练场次不一致')
      ranked.push({ model, ...trainingScore(matches, plan, model) })
    }
  }
  ranked.sort((a, b) => b.score - a.score)
  const finalist = ranked[0]?.model ?? null
  const label = `${options.competition}-${options.range}`
  await fs.writeFile(path.join(options.directory, `${label}-top3-frozen.json`), JSON.stringify({ plan, ranked, finalist }, null, 2) + '\n')
  const observed = new Map()
  for (const model of [...originals.map(profile => profile.modelFactors), ...(finalist ? [finalist] : [])]) {
    if (!observed.has(modelKey(model))) {
      observed.set(modelKey(model), await fetchScoreBacktest(options, model, { simulations: options.simulations }))
    }
  }
  const seed = `${options.competition}:${options.range}:top3:v1`
  const periods = {
    full: () => true,
    training: match => Boolean(plan.validationFrom && match.matchDate < plan.validationFrom),
    validation: match => Boolean(plan.validationFrom && match.matchDate >= plan.validationFrom),
    fresh: match => match.matchDate >= options.recentFrom
  }
  const profiles = originals.map((original, index) => {
    const baseline = observed.get(modelKey(original.modelFactors))
    const candidate = finalist ? observed.get(modelKey(finalist)) : baseline
    const subset = (other, period) => compare(baseline.filter(periods[period]), other.filter(periods[period]), `${seed}:${index}:${period}`)
    const training = subset(candidate, 'training')
    const validation = subset(candidate, 'validation')
    const accepted = Boolean(finalist && scorePass(training, validation))
    const model = accepted ? { ...finalist, handicapSmoothingFactor: original.modelFactors.handicapSmoothingFactor } : original.modelFactors
    const deployed = accepted ? candidate : baseline
    return { model, accepted, candidate: { training, validation },
      periods: Object.fromEntries(Object.keys(periods).map(period => [period, subset(deployed, period)])) }
  })
  return { plan, finalist, candidateCount: ranked.length, profiles }
}
