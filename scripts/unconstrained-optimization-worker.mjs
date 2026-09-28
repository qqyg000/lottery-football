import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { parentPort, workerData, isMainThread } from 'node:worker_threads'
import { evaluateRecommendationBacktest, evaluateTotalGoalsBacktest } from '../frontend/src/recommendation-backtest.mjs'
import {
  DEFAULT_MODEL, DEFAULT_RULE, DEFAULT_GOALS, chronologicalPlan, randomGenerator,
  prepareMatches, evaluateBlocks, searchCandidates, ruleCandidates, goalCandidates, combineMetrics
} from './strategy-optimization-core.mjs'

export function modelCandidates(profiles, count, seed) {
  const random = randomGenerator(seed)
  const limits = { hostTeamGoalFactor: [0.1, 3], homeTeamGoalFactor: [0.1, 3], seedTeamGoalFactor: [0.1, 3],
    officialMatchWeight: [1, 3], internationalFriendlyWeight: [0, 1], clubFriendlyWeight: [0, 1], handicapSmoothingFactor: [0, 0.8] }
  const normalize = source => Object.fromEntries(Object.entries(DEFAULT_MODEL).map(([key, fallback]) => [key, source?.[key] ?? fallback]))
  const models = profiles.map(profile => normalize(profile?.modelFactors))
  models.push({ ...DEFAULT_MODEL })
  while (models.length < count) {
    const center = models[Math.floor(random() * Math.min(3, models.length))]
    const local = random() < 0.6
    models.push(Object.fromEntries(Object.entries(limits).map(([key, [minimum, maximum]]) => {
      const value = local ? center[key] + (random() - 0.5) * (maximum - minimum) * 0.5 : minimum + random() * (maximum - minimum)
      return [key, Math.round(Math.max(minimum, Math.min(maximum, value)) * 1000) / 1000]
    })))
  }
  return [...new Map(models.map(model => [JSON.stringify(model), model])).values()]
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, { ...options, signal: AbortSignal.timeout(120000) })
  if (!response.ok) throw new Error(`${response.status}: ${await response.text()}`)
  return response.json()
}

export async function fetchBacktest(options, modelFactors) {
  const { competition, range, simulations, apiBase, directory, fingerprint } = options
  const signature = { fingerprint, competition, range, simulations, modelFactors }
  const hash = createHash('sha256').update(JSON.stringify(signature)).digest('hex')
  const cachePath = path.join(directory, 'cache', `${hash}.json`)
  try {
    const cached = JSON.parse(await fs.readFile(cachePath, 'utf8'))
    if (JSON.stringify(cached.signature) === JSON.stringify(signature)) return cached.result
  } catch (error) { if (error.code !== 'ENOENT') throw error }
  const url = new URL('/api/football/recommendation-backtest/jobs', apiBase)
  url.search = new URLSearchParams({ competition, includePreviousEdition: String(range === 'PREVIOUS'), simulations: String(simulations) })
  let job = await fetchJson(url, { method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ modelFactorsByCompetition: { [competition]: modelFactors } }) })
  const started = Date.now()
  while (['RUNNING', 'QUEUED'].includes(job.status)) {
    if (Date.now() - started > 1800000) throw new Error(`${competition}:${range} 回测超过30分钟`)
    await new Promise(resolve => setTimeout(resolve, 1000))
    job = await fetchJson(new URL(`/api/football/recommendation-backtest/jobs/${job.jobId}`, apiBase))
  }
  if (job.status !== 'COMPLETED' || !job.result) throw new Error(job.message || '回测失败')
  const keys = ['matchId', 'matchDate', 'kickoffTime', 'competition', 'scoreText', 'sportteryMatchId',
    'sportteryNormalAvailable', 'sportteryNormalOdds', 'sportteryHandicap', 'sportteryHandicapOdds',
    'normalProbability', 'adjustedNormalProbability', 'handicapProbabilities', 'adjustedHandicapProbabilities',
    'sportteryTotalGoalsOdds', 'sportteryTotalGoalsProbabilities', 'adjustedSportteryTotalGoalsProbabilities']
  const result = { ...job.result, matches: job.result.matches.map(match =>
    Object.fromEntries(keys.filter(key => key in match).map(key => [key, match[key]]))) }
  await fs.writeFile(cachePath, JSON.stringify({ signature, result }), 'utf8')
  return result
}

function mergeSearch(target, source, modelIndex) {
  for (const preset of Object.keys(source)) {
    target[preset] ||= { best: null, folds: [], evaluatedCandidates: 0 }
    const result = target[preset]
    result.evaluatedCandidates += source[preset].evaluatedCandidates
    const better = (old, next) => next && (!old || next.score > old.score + 1e-12)
    if (better(result.best, source[preset].best)) result.best = { ...source[preset].best, modelIndex }
    source[preset].folds.forEach((fold, index) => {
      if (better(result.folds[index], fold)) result.folds[index] = { ...fold, modelIndex }
    })
  }
}

export function verifyMetrics(matches, parameters, market) {
  const prepared = prepareMatches(matches, market, { dateToBlock: Object.fromEntries(matches.map(match => [match.matchDate, 0])) })
  const actual = evaluateBlocks(prepared, parameters, market)[0]
  const expected = market === 'WDL'
    ? evaluateRecommendationBacktest(matches, { modelMode: 'after', globalParameters: parameters }).summary
    : evaluateTotalGoalsBacktest(matches, { modelMode: 'after', strategy: parameters })
  for (const key of ['recommendedMatchCount', 'recommendedSelectionCount', 'winningSelectionCount',
    'totalStake', 'totalReturn', 'netProfit', 'roi', 'volatility']) {
    if (actual[key] === expected[key]) continue
    if (!Number.isFinite(actual[key]) || !Number.isFinite(expected[key]) || Math.abs(actual[key] - expected[key]) > 1e-7) {
      throw new Error(`${market} 页面算法复验失败 ${key}: ${actual[key]} != ${expected[key]}`)
    }
  }
  return actual
}

function metricsByPeriod(matches, parameters, market, plan) {
  return Object.fromEntries(Object.entries({
    full: matches,
    development: matches.filter(match => match.matchDate in plan.dateToBlock),
    test: matches.filter(match => plan.testFrom && match.matchDate >= plan.testFrom),
    fresh: matches.filter(match => plan.freshFrom && match.matchDate >= plan.freshFrom)
  }).map(([period, subset]) => [period, verifyMetrics(subset, parameters, market)]))
}

function bootstrapTest(matches, parameters, market, seed) {
  const byDate = new Map()
  for (const match of matches) {
    if (!byDate.has(match.matchDate)) byDate.set(match.matchDate, [])
    byDate.get(match.matchDate).push(match)
  }
  const days = [...byDate.values()].map(group => verifyMetrics(group, parameters, market))
  if (days.length < 2) return { dateCount: days.length, roi95Interval: null }
  const random = randomGenerator(seed)
  const rois = []
  for (let iteration = 0; iteration < 2000; iteration++) {
    let stake = 0
    let amount = 0
    for (let index = 0; index < days.length; index++) {
      const selected = days[Math.floor(random() * days.length)]
      stake += selected.totalStake
      amount += selected.totalReturn
    }
    if (stake > 0) rois.push(amount / stake - 1)
  }
  rois.sort((a, b) => a - b)
  return { dateCount: days.length, validResamples: rois.length,
    roi95Interval: rois.length ? [rois[Math.floor(rois.length * 0.025)], rois[Math.min(rois.length - 1, Math.floor(rois.length * 0.975))]] : null }
}

async function optimizeRange(options) {
  const { competition, range, config, directory, modelCount, ruleCount, goalCount, freshFrom } = options
  const key = `${competition}:${range}`
  const presets = ['STABLE', 'AGGRESSIVE']
  const originalProfiles = presets.map(preset => config.parameterProfiles[`${key}:${preset}`])
  const models = modelCandidates(originalProfiles, modelCount, `${key}:models:v1`)
  const candidates = ruleCandidates(ruleCount, `${key}:rules:v1`, originalProfiles.flatMap(profile => [
    profile.globalParameters, { ...profile.globalParameters, recommendationOdds: 1 }
  ]))
  const data = []
  const wdl = {}
  let plan
  for (const [modelIndex, model] of models.entries()) {
    const result = await fetchBacktest(options, model)
    data.push(result)
    if (!plan) plan = chronologicalPlan(result.matches, freshFrom)
    if (modelIndex && JSON.stringify(result.matches.map(match => match.matchId)) !== JSON.stringify(data[0].matches.map(match => match.matchId))) {
      throw new Error(`${key} 不同模型的比赛快照不一致`)
    }
    const prepared = prepareMatches(result.matches, 'WDL', plan)
    mergeSearch(wdl, searchCandidates(prepared, candidates, 'WDL', plan.blockCount), modelIndex)
    parentPort?.postMessage({ type: 'progress', key, model: modelIndex + 1, total: models.length, matches: result.matches.length })
  }
  const neutralModelIndex = models.findIndex(model => JSON.stringify(model) === JSON.stringify(DEFAULT_MODEL))
  for (const preset of presets) {
    wdl[preset].best ||= { parameters: DEFAULT_RULE, modelIndex: neutralModelIndex, score: null, priorOnly: true }
  }
  const stableModelIndex = wdl.STABLE.best.modelIndex
  const goalRules = goalCandidates(goalCount, `${key}:goals:v1`, config.totalGoalsStrategies[key])
  const preparedGoals = prepareMatches(data[stableModelIndex].matches, 'GOALS', plan)
  const goals = searchCandidates(preparedGoals, goalRules, 'GOALS', plan.blockCount, ['STABLE']).STABLE
  goals.best ||= { parameters: DEFAULT_GOALS, score: null, priorOnly: true }
  goals.best.modelIndex = stableModelIndex
  // 进球数与稳健方案共用模型，逐折重新选择模型和规则，不能沿用全开发段选出的模型
  goals.folds = []
  for (let prefix = 1; prefix < plan.blockCount; prefix++) {
    const foldModelIndex = wdl.STABLE.folds[prefix - 1]?.modelIndex ?? neutralModelIndex
    const prepared = prepareMatches(data[foldModelIndex].matches, 'GOALS', plan)
    const fold = searchCandidates(prepared, goalRules, 'GOALS', prefix, ['STABLE']).STABLE.best
    if (fold) goals.folds.push({ ...fold, modelIndex: foldModelIndex,
      validation: evaluateBlocks(prepared.filter(match => match.block === prefix).map(match => ({ ...match, block: 0 })), fold.parameters, 'GOALS')[0] })
  }
  const selection = { competition, range, plan, models, wdl, goals }
  // 参数先落盘冻结，再读取测试结果，测试段不负责通过、拒绝或替换候选
  await fs.writeFile(path.join(directory, `${key.replaceAll(':', '-')}-selection.json`), JSON.stringify(selection, null, 2), 'utf8')
  const rows = []
  for (const preset of presets) {
    const selected = wdl[preset].best
    const original = originalProfiles[presets.indexOf(preset)]
    const originalIndex = models.findIndex(model => JSON.stringify(model) === JSON.stringify(original.modelFactors))
    const chosen = data[selected.modelIndex].matches
    rows.push({ competition, range, market: 'WDL', preset,
      status: chosen.length === 0 ? 'NO_DATA' : selected.priorOnly ? 'PRIOR_ONLY' : 'OPTIMIZED',
      profile: { modelFactors: models[selected.modelIndex], globalParameters: selected.parameters },
      originalProfile: original, trainingScore: selected.score, evaluatedCandidates: wdl[preset].evaluatedCandidates,
      before: metricsByPeriod(data[originalIndex].matches, original.globalParameters, 'WDL', plan),
      after: metricsByPeriod(chosen, selected.parameters, 'WDL', plan),
      walkForward: combineMetrics(wdl[preset].folds.filter(Boolean).map(fold => fold.validation)),
      testUncertainty: bootstrapTest(chosen.filter(match => plan.testFrom && match.matchDate >= plan.testFrom), selected.parameters, 'WDL', `${key}:${preset}`) })
  }
  const stableOriginalIndex = models.findIndex(model => JSON.stringify(model) === JSON.stringify(originalProfiles[0].modelFactors))
  rows.push({ competition, range, market: 'GOALS', preset: 'SHARED',
    status: data[stableModelIndex].matches.length === 0 ? 'NO_DATA' : goals.best.priorOnly ? 'PRIOR_ONLY' : 'OPTIMIZED',
    strategy: goals.best.parameters, originalStrategy: config.totalGoalsStrategies[key],
    trainingScore: goals.best.score, evaluatedCandidates: goals.evaluatedCandidates,
    before: metricsByPeriod(data[stableOriginalIndex].matches, config.totalGoalsStrategies[key], 'GOALS', plan),
    after: metricsByPeriod(data[stableModelIndex].matches, goals.best.parameters, 'GOALS', plan),
    walkForward: combineMetrics(goals.folds.map(fold => fold.validation)),
    testUncertainty: bootstrapTest(data[stableModelIndex].matches.filter(match => plan.testFrom && match.matchDate >= plan.testFrom), goals.best.parameters, 'GOALS', `${key}:goals`) })
  const result = { competition, range, plan, models: models.length, rows, independentVerification: 'PASSED' }
  await fs.writeFile(path.join(directory, `${key.replaceAll(':', '-')}-result.json`), JSON.stringify(result, null, 2), 'utf8')
  return result
}

if (!isMainThread) {
  optimizeRange(workerData).then(result => parentPort.postMessage({ type: 'result', result }))
    .catch(error => { parentPort.postMessage({ type: 'error', message: error.stack }); process.exitCode = 1 })
}
