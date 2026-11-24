import fs from 'node:fs/promises'
import path from 'node:path'
import { createHash } from 'node:crypto'
import { isMainThread, parentPort, workerData } from 'node:worker_threads'
import { evaluateRecommendationBacktest, evaluateTotalGoalsBacktest } from '../frontend/src/recommendation-backtest.mjs'
import { prepareMatches, evaluateBlocks, combineMetrics, DEFAULT_GOALS, DEFAULT_MODEL } from './strategy-optimization-core.mjs'
import {
  POLICY, marketValidationPlan, modelCandidates, wdlRules, goalsRules, samplingWindow,
  wdlTrainingPass, goalsTrainingPass, trainingScore, offerCandidate, checkNeighborhood,
  freezeShortlist, selectWdlPair, selectGoals, goalsCandidatePass, wdlPairPass
} from './constrained-optimization-core.mjs'
import { optimizeScoreModels } from './score-top3-optimization.mjs'

const digest = value => createHash('sha256').update(typeof value === 'string' ? value : JSON.stringify(value)).digest('hex')
const modelKey = model => JSON.stringify(Object.keys(DEFAULT_MODEL).map(key => model[key]))
const save = (file, value) => fs.writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')
const pause = ms => new Promise(resolve => setTimeout(resolve, ms))
const progress = (key, stage, model, total) => parentPort?.postMessage({ type: 'progress', key, stage, model, total })

async function jsonRequest(url, options = {}) {
  const response = await fetch(url, { ...options, signal: AbortSignal.timeout(120000) })
  if (!response.ok) throw new Error(`${response.status}: ${(await response.text()).slice(0, 1000)}`)
  return response.json()
}

export async function fetchBacktest(options, modelFactors, simulations = options.simulations) {
  const { competition, range, directory, dataFingerprint, apiBase } = options
  const signature = { dataFingerprint, competition, range, simulations, modelFactors }
  const file = path.join(directory, 'cache', `${digest(signature)}.json`)
  try {
    const cached = JSON.parse(await fs.readFile(file, 'utf8'))
    if (digest(cached.signature) !== digest(signature) || digest(cached.result) !== cached.resultSha256) throw new Error(`回测缓存校验失败: ${file}`)
    return cached.result
  } catch (error) { if (error.code !== 'ENOENT') throw error }
  const url = new URL('/api/football/recommendation-backtest/jobs', apiBase)
  url.search = new URLSearchParams({ competition, includePreviousEdition: String(range === 'PREVIOUS'), simulations: String(simulations) })
  let job = await jsonRequest(url, { method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ modelFactorsByCompetition: { [competition]: modelFactors } }) })
  const started = Date.now()
  while (['QUEUED', 'RUNNING'].includes(job.status)) {
    if (Date.now() - started > 3600000) throw new Error(`${competition}:${range} 回测超过60分钟`)
    await pause(1500)
    job = await jsonRequest(new URL(`/api/football/recommendation-backtest/jobs/${job.jobId}`, apiBase))
  }
  if (job.status !== 'COMPLETED' || !Array.isArray(job.result?.matches)) throw new Error(job.message || '回测任务未完成')
  const keys = ['matchId', 'matchDate', 'competition', 'scoreText', 'sportteryMatchId', 'sportteryNormalAvailable',
    'sportteryNormalOdds', 'sportteryHandicap', 'sportteryHandicapOdds', 'normalProbability', 'adjustedNormalProbability',
    'handicapProbabilities', 'adjustedHandicapProbabilities', 'sportteryTotalGoalsOdds', 'sportteryTotalGoalsProbabilities',
    'adjustedSportteryTotalGoalsProbabilities']
  const result = { completedMatchCount: job.result.completedMatchCount, oddsMatchCount: job.result.oddsMatchCount,
    matches: job.result.matches.map(match => Object.fromEntries(keys.filter(key => key in match).map(key => [key, match[key]]))) }
  await save(file, { signature, resultSha256: digest(result), result })
  return result
}

export function verifyMetrics(matches, parameters, market) {
  const plan = { dateToBlock: Object.fromEntries(matches.map(match => [match.matchDate, 0])) }
  const actual = evaluateBlocks(prepareMatches(matches, market, plan), parameters, market)[0]
  const expected = market === 'WDL'
    ? evaluateRecommendationBacktest(matches, { modelMode: 'after', oddsMatchCount: matches.length, globalParameters: parameters }).summary
    : evaluateTotalGoalsBacktest(matches, { modelMode: 'after', oddsMatchCount: matches.length, strategy: parameters })
  for (const key of ['recommendedMatchCount', 'recommendedSelectionCount', 'winningSelectionCount', 'totalStake', 'totalReturn', 'netProfit', 'roi', 'volatility', 'samplingRate']) {
    if (actual[key] === expected[key]) continue
    if (!Number.isFinite(actual[key]) || !Number.isFinite(expected[key]) || Math.abs(actual[key] - expected[key]) > 1e-7) {
      throw new Error(`${market} 独立页面结算不一致 ${key}: ${actual[key]} != ${expected[key]}`)
    }
  }
  if (market === 'GOALS' && actual.selectionHitRate !== expected.hitRate) throw new Error('进球数单注命中率与页面不一致')
  return actual
}

export function goalMatches(matches) {
  return matches.filter(match => /\d+\s*-\s*\d+/.test(String(match.scoreText)) && match.sportteryTotalGoalsOdds &&
    Array.isArray(match.adjustedSportteryTotalGoalsProbabilities) && match.adjustedSportteryTotalGoalsProbabilities.length &&
    Object.values(match.sportteryTotalGoalsOdds).some(value => Number.isFinite(Number(value)) && Number(value) > 0))
}

function byPeriod(matches, parameters, market, plan, recentFrom) {
  return Object.fromEntries(Object.entries({
    full: matches,
    training: matches.filter(match => match.matchDate in plan.dateToBlock),
    validation: matches.filter(match => plan.validationFrom && match.matchDate >= plan.validationFrom),
    recent: matches.filter(match => recentFrom && match.matchDate >= recentFrom)
  }).map(([period, subset]) => [period, verifyMetrics(subset, parameters, market)]))
}

function settlementDataHash(matches) {
  return digest(matches.map(match => [match.matchId, match.matchDate, match.scoreText, match.sportteryMatchId,
    match.sportteryNormalAvailable, match.sportteryNormalOdds, match.sportteryHandicap, match.sportteryHandicapOdds, match.sportteryTotalGoalsOdds])
    .sort((a, b) => String(a[0]).localeCompare(String(b[0]))))
}

function checkedCandidate(candidate, data, market, plan) {
  const matches = market === 'GOALS' ? goalMatches(data[candidate.modelIndex].matches) : data[candidate.modelIndex].matches
  const validation = verifyMetrics(matches.filter(match => match.matchDate >= plan.validationFrom), candidate.parameters, market)
  const training = verifyMetrics(matches.filter(match => match.matchDate in plan.dateToBlock), candidate.parameters, market)
  return { ...candidate, training, validation, full: combineMetrics([training, validation]) }
}

function newPools() {
  return { STABLE: [], AGGRESSIVE: [] }
}

export function searchWdl(matches, rules, plan, modelIndex, context, pools, rolling = null) {
  if (!plan.available) return
  const prepared = prepareMatches(matches, 'WDL', plan)
  for (const parameters of rules) {
    const blocks = evaluateBlocks(prepared, parameters, 'WDL', plan.blockCount)
    const training = combineMetrics(blocks)
    for (const preset of context.presets ?? ['STABLE', 'AGGRESSIVE']) {
      if (wdlTrainingPass(training, context.competition, context.range, preset)) offerCandidate(pools[preset], {
        parameters, modelIndex, blocks, training, score: trainingScore(blocks, preset)
      })
      if (!rolling) continue
      for (let prefix = 1; prefix < plan.blockCount; prefix++) {
        const past = blocks.slice(0, prefix)
        const metrics = combineMetrics(past)
        if (!wdlTrainingPass(metrics, context.competition, context.range, preset)) continue
        const score = trainingScore(past, preset)
        const old = rolling[preset][prefix - 1]
        if (!old || score > old.score + 1e-12) rolling[preset][prefix - 1] = {
          parameters, modelIndex, training: metrics, score, prefix
        }
      }
    }
  }
}

function perturbedPool(pool, data, market, plan, preset) {
  const prepared = new Map()
  return pool.map(candidate => {
    if (!prepared.has(candidate.modelIndex)) {
      const matches = market === 'GOALS' ? goalMatches(data[candidate.modelIndex].matches) : data[candidate.modelIndex].matches
      prepared.set(candidate.modelIndex, prepareMatches(matches, market, plan))
    }
    return checkNeighborhood(candidate, prepared.get(candidate.modelIndex), market, plan.blockCount, preset)
  }).sort((a, b) => b.score - a.score)
}

export function searchGoals(matches, rules, plan, modelIndex, blockCount = plan.blockCount) {
  const pools = Object.fromEntries(POLICY.goalTiers.map(tier => [tier, []]))
  if (!plan.available || !blockCount) return pools
  const prepared = prepareMatches(matches, 'GOALS', plan)
  for (const parameters of rules) {
    const blocks = evaluateBlocks(prepared, parameters, 'GOALS', blockCount)
    const training = combineMetrics(blocks)
    for (const tier of POLICY.goalTiers) if (goalsTrainingPass(training, tier)) offerCandidate(pools[tier], {
      parameters, modelIndex, blocks, training, score: trainingScore(blocks, 'STABLE')
    }, 32)
  }
  return pools
}

function uncertainty(matches, parameters, market, seed) {
  const days = [...new Set(matches.map(match => match.matchDate))].map(date => verifyMetrics(matches.filter(match => match.matchDate === date), parameters, market))
  if (days.length < 2) return { dateCount: days.length, roi95Interval: null }
  let state = Number.parseInt(digest(seed).slice(0, 8), 16)
  const random = () => ((state = (Math.imul(state, 1664525) + 1013904223) >>> 0) / 4294967296)
  const values = []
  for (let i = 0; i < 1000; i++) {
    let stake = 0
    let amount = 0
    for (let j = 0; j < days.length; j++) {
      const selected = days[Math.floor(random() * days.length)]
      stake += selected.totalStake
      amount += selected.totalReturn
    }
    if (stake) values.push(amount / stake - 1)
  }
  values.sort((a, b) => a - b)
  return { dateCount: days.length, validResamples: values.length,
    roi95Interval: values.length ? [values[Math.floor(values.length * 0.025)], values[Math.min(values.length - 1, Math.floor(values.length * 0.975))]] : null }
}

async function optimizeRange(options) {
  const { competition, range, config, modelCount, ruleCount, goalCount, directory } = options
  const key = `${competition}:${range}`
  const presets = ['STABLE', 'AGGRESSIVE']
  const originals = presets.map(preset => config.parameterProfiles[`${key}:${preset}`])
  const scoreOptimization = options.objective === 'score-top3'
  if (scoreOptimization) progress(key, '筛选比分模型', 0, 1)
  const scores = scoreOptimization ? await optimizeScoreModels(options, originals) : null
  const marketModels = scores?.profiles.map(({ model }) => [model,
    { ...model, handicapSmoothingFactor: 0.274 }, { ...model, handicapSmoothingFactor: 0.65 }])
  const models = marketModels
    ? [...new Map([...originals.map(profile => profile.modelFactors), ...marketModels.flat()].map(model => [modelKey(model), model])).values()]
    : modelCandidates(originals, modelCount, `${key}:models:v2`)
  const findModel = model => models.findIndex(candidate => modelKey(candidate) === modelKey(model))
  const originalIndices = originals.map(profile => findModel(profile.modelFactors))
  if (originalIndices.some(index => index < 0)) throw new Error(`${key} 原模型未纳入候选`)
  const scoreIndices = marketModels?.map(candidates => candidates.map(findModel))
  const rules = wdlRules(ruleCount, `${key}:rules:v2`, originals.map(profile => profile.globalParameters))
  const coarse = []
  const coarsePools = newPools()
  let plan
  let dataHash
  let rolling
  function checkData(result) {
    if (!plan) {
      plan = marketValidationPlan(result.matches, scores?.plan, scores?.profiles.some(profile => profile.accepted))
      dataHash = settlementDataHash(result.matches)
      rolling = Object.fromEntries(presets.map(preset => [preset, Array(scoreOptimization ? 0 : Math.max(0, plan.blockCount - 1)).fill(null)]))
    }
    if (settlementDataHash(result.matches) !== dataHash) throw new Error(`${key} 模型间数据快照发生变化`)
  }
  for (const [index, model] of (scoreOptimization ? [] : models).entries()) {
    const result = await fetchBacktest(options, model, options.coarseSimulations)
    coarse[index] = result
    checkData(result)
    searchWdl(result.matches, rules, plan, index, options, coarsePools, rolling)
    progress(key, '粗搜', index + 1, models.length)
    if (!plan.available) break
  }
  const finalists = new Set([...originalIndices, ...(scoreIndices?.flat() ?? [])])
  const orderedModels = []
  for (let rank = 0; rank < 96; rank++) for (const preset of ['STABLE', 'AGGRESSIVE']) {
    const candidate = coarsePools[preset][rank]
    if (candidate && !orderedModels.includes(candidate.modelIndex)) orderedModels.push(candidate.modelIndex)
  }
  orderedModels.slice(0, options.finalistModels).forEach(index => finalists.add(index))
  const data = []
  const pools = newPools()
  for (const index of finalists) {
    data[index] = await fetchBacktest(options, models[index])
    checkData(data[index])
    const eligible = scoreIndices ? presets.filter((_, i) => scoreIndices[i].includes(index)) : presets
    if (eligible.length) searchWdl(data[index].matches, rules, plan, index, { ...options, presets: eligible }, pools)
    if (scoreOptimization && !data[index].matches.length) break
    progress(key, '精搜', data.filter(Boolean).length, finalists.size)
  }
  const windows = {}
  const shortlists = {}
  const matchesAt = index => (data[index] ?? data[originalIndices[0]]).matches
  for (const [presetIndex, preset] of presets.entries()) {
    const oldMatches = matchesAt(originalIndices[presetIndex])
    const windowMatches = scoreOptimization ? oldMatches.filter(match => match.matchDate in plan.dateToBlock) : oldMatches
    const baselineMetrics = verifyMetrics(windowMatches, originals[presetIndex].globalParameters, 'WDL')
    windows[preset] = samplingWindow(competition, range, preset, baselineMetrics.samplingRate)
    const perturbed = perturbedPool(pools[preset], data, 'WDL', plan, preset)
    let baseline = null
    if (plan.available && (!scoreIndices || scoreIndices[presetIndex].includes(originalIndices[presetIndex]))) {
      const blocks = evaluateBlocks(prepareMatches(oldMatches, 'WDL', plan), originals[presetIndex].globalParameters, 'WDL', plan.blockCount)
      const training = combineMetrics(blocks)
      if (wdlTrainingPass(training, competition, range, preset)) baseline = perturbedPool([{
        parameters: originals[presetIndex].globalParameters, modelIndex: originalIndices[presetIndex], blocks, training, score: trainingScore(blocks, preset), original: true
      }], data, 'WDL', plan, preset)[0]
    }
    shortlists[preset] = freezeShortlist(perturbed, baseline, windows[preset], POLICY.wdlHoldoutLimit)
  }
  await save(path.join(directory, `${competition}-${range}-wdl-frozen.json`), { dataHash, plan, models, windows, shortlists,
    note: '训练段冻结候选，验证段只执行门槛检查，禁止根据验证ROI重排或追加候选' })
  const pair = selectWdlPair(shortlists, { competition, range, windows }, candidate => checkedCandidate(candidate, data, 'WDL', plan))
  const stableIndex = pair.stable?.modelIndex ?? scoreIndices?.[0][0] ?? originalIndices[0]
  const stableMatches = goalMatches(matchesAt(stableIndex))
  const goalsPlan = marketValidationPlan(stableMatches, scores?.plan, scores?.profiles[0].accepted)
  const originalStrategy = config.totalGoalsStrategies[key] || { ...DEFAULT_GOALS, maximumSelections: 0 }
  const goalRules = goalsRules(goalCount, `${key}:goals:v2`, originalStrategy)
  const goalsPools = searchGoals(stableMatches, goalRules, goalsPlan, stableIndex)
  const goalUnion = []
  for (const tier of POLICY.goalTiers) {
    const checked = perturbedPool(goalsPools[tier], data, 'GOALS', goalsPlan, 'STABLE')
    for (const candidate of checked.slice(0, 3)) {
      if (!goalUnion.some(item => JSON.stringify(item.parameters) === JSON.stringify(candidate.parameters))) goalUnion.push(candidate)
    }
  }
  const baselineGoalBlocks = goalsPlan.available
    ? evaluateBlocks(prepareMatches(stableMatches, 'GOALS', goalsPlan), originalStrategy, 'GOALS', goalsPlan.blockCount) : []
  const baselineGoalTraining = combineMetrics(baselineGoalBlocks)
  if (goalsPlan.available && POLICY.goalTiers.some(tier => goalsTrainingPass(baselineGoalTraining, tier))) {
    const baseline = perturbedPool([{ parameters: originalStrategy, modelIndex: stableIndex, blocks: baselineGoalBlocks,
      training: baselineGoalTraining, score: trainingScore(baselineGoalBlocks, 'STABLE'), original: true }], data, 'GOALS', goalsPlan, 'STABLE')[0]
    if (!goalUnion.some(item => JSON.stringify(item.parameters) === JSON.stringify(baseline.parameters))) goalUnion.unshift(baseline)
  }
  const goalFrozen = goalUnion.slice(0, POLICY.goalsHoldoutLimit).sort((a, b) => b.score - a.score)
  const goalShortlists = Object.fromEntries(POLICY.goalTiers.map(tier => [tier, goalFrozen.filter(candidate => goalsTrainingPass(candidate.training, tier))]))
  await save(path.join(directory, `${competition}-${range}-goals-frozen.json`), { dataHash, plan: goalsPlan, model: models[stableIndex], shortlists: goalShortlists })
  const selectedGoals = selectGoals(goalShortlists, candidate => checkedCandidate(candidate, data, 'GOALS', goalsPlan))
  const rows = []
  for (const [presetIndex, preset] of presets.entries()) {
    const selected = preset === 'STABLE' ? pair.stable : pair.aggressive
    const index = selected?.modelIndex ?? scoreIndices?.[presetIndex][0] ?? originalIndices[presetIndex]
    const parameters = selected?.parameters ?? { ...originals[presetIndex].globalParameters, recommendationOdds: 100 }
    const chosen = matchesAt(index)
    const after = byPeriod(chosen, parameters, 'WDL', plan, options.recentFrom)
    if (!selected && after.full.totalStake) throw new Error(`${key}:${preset} 关闭后仍产生投注`)
    const folds = []
    for (const fold of rolling[preset].filter(Boolean)) {
      if (!data[fold.modelIndex]) data[fold.modelIndex] = await fetchBacktest(options, models[fold.modelIndex])
      const next = data[fold.modelIndex].matches.filter(match => plan.dateToBlock[match.matchDate] === fold.prefix)
      folds.push({ ...fold, model: models[fold.modelIndex], validation: verifyMetrics(next, fold.parameters, 'WDL') })
    }
    rows.push({ competition, range, market: 'WDL', preset,
      status: selected ? (selected.original ? 'RETAINED' : 'OPTIMIZED') : !chosen.length ? 'NO_DATA' : !plan.available ? 'INSUFFICIENT_DATA' : 'CLOSED',
      profile: { modelFactors: models[index], globalParameters: parameters }, originalProfile: originals[presetIndex],
      before: byPeriod(matchesAt(originalIndices[presetIndex]), originals[presetIndex].globalParameters, 'WDL', plan, options.recentFrom), after,
      trainingScore: selected?.score ?? null, neighborhood: selected?.neighborhood ?? null,
      walkForward: combineMetrics(folds.map(fold => fold.validation)), folds,
      validationUncertainty: uncertainty(chosen.filter(match => plan.validationFrom && match.matchDate >= plan.validationFrom), parameters, 'WDL', `${key}:${preset}`) })
  }
  const strategy = selectedGoals.candidate?.parameters ?? { ...originalStrategy, maximumSelections: 0 }
  const goalFolds = []
  if (!scoreOptimization && goalsPlan.available) for (let prefix = 1; prefix < goalsPlan.blockCount; prefix++) {
    const targetDate = Object.keys(goalsPlan.dateToBlock).find(date => goalsPlan.dateToBlock[date] === prefix)
    const safeFold = rolling.STABLE.filter(Boolean).filter(fold => {
      const dates = Object.keys(plan.dateToBlock).filter(date => plan.dateToBlock[date] < fold.prefix)
      return dates.length && dates.at(-1) < targetDate
    }).at(-1)
    // 无足够更早的胜平负滚动模型时，使用固定中性先验，不能偷看全训练段选出的模型
    const foldModel = safeFold ? models[safeFold.modelIndex] : { ...DEFAULT_MODEL }
    let foldIndex = models.findIndex(model => modelKey(model) === modelKey(foldModel))
    if (foldIndex < 0) { foldIndex = models.length; models.push(foldModel) }
    if (!data[foldIndex]) data[foldIndex] = await fetchBacktest(options, foldModel)
    const foldMatches = goalMatches(data[foldIndex].matches)
    const foldPools = searchGoals(foldMatches, goalRules, goalsPlan, foldIndex, prefix)
    const best = POLICY.goalTiers.map(tier => foldPools[tier][0]).find(Boolean)
    if (best) goalFolds.push({ parameters: best.parameters, model: foldModel, prefix, training: best.training,
      validation: verifyMetrics(foldMatches.filter(match => goalsPlan.dateToBlock[match.matchDate] === prefix), best.parameters, 'GOALS') })
  }
  rows.push({ competition, range, market: 'GOALS', preset: 'SHARED',
    status: selectedGoals.candidate ? (selectedGoals.candidate.original ? 'RETAINED' : 'OPTIMIZED') : !stableMatches.length ? 'NO_DATA' : !goalsPlan.available ? 'INSUFFICIENT_DATA' : 'CLOSED',
    strategy, originalStrategy, modelFactors: models[stableIndex], tier: selectedGoals.tier,
    before: byPeriod(goalMatches(matchesAt(originalIndices[0])), originalStrategy, 'GOALS', goalsPlan, options.recentFrom),
    after: byPeriod(stableMatches, strategy, 'GOALS', goalsPlan, options.recentFrom),
    trainingScore: selectedGoals.candidate?.score ?? null, neighborhood: selectedGoals.candidate?.neighborhood ?? null,
    walkForward: combineMetrics(goalFolds.map(fold => fold.validation)), folds: goalFolds,
    validationUncertainty: uncertainty(stableMatches.filter(match => goalsPlan.validationFrom && match.matchDate >= goalsPlan.validationFrom), strategy, 'GOALS', `${key}:goals`) })
  if (pair.stable && !wdlPairPass(pair.stable, pair.aggressive, competition, range)) throw new Error(`${key} 胜平负最终门槛未通过`)
  if (selectedGoals.candidate && !goalsCandidatePass(selectedGoals.candidate, selectedGoals.tier)) throw new Error(`${key} 进球数最终门槛未通过`)
  return { competition, range, plan, goalsPlan, rows, dataHash, windows, scores, samplingWindowRelaxed: pair.relaxed,
    coarseModels: coarse.filter(Boolean).length, finalModels: data.filter(Boolean).length,
    searchedRules: rules.length, searchedGoalRules: goalRules.length,
    holdoutCandidates: { stable: shortlists.STABLE.length, aggressive: shortlists.AGGRESSIVE.length, goals: goalFrozen.length },
    holdoutEvaluated: { wdl: pair.evaluatedCandidates, goals: selectedGoals.evaluatedCandidates },
    independentVerification: 'PASSED', stableMatches: matchesAt(stableIndex) }
}

export async function optimizeCompetition(options) {
  const previous = await optimizeRange({ ...options, range: 'PREVIOUS' })
  progress(options.competition, '含上届完成', 1, 2)
  const current = await optimizeRange({ ...options, range: 'CURRENT' })
  const currentGoals = current.rows.find(row => row.market === 'GOALS')
  const previousGoals = previous.rows.find(row => row.market === 'GOALS')
  const matches = goalMatches(current.stableMatches)
  if (matches.length <= (options.objective === 'score-top3' ? 0 : 10) && previousGoals.strategy.maximumSelections > 0) {
    const after = byPeriod(matches, previousGoals.strategy, 'GOALS', current.goalsPlan, options.recentFrom)
    if (!matches.length || goalsTrainingPass(after.full, previousGoals.tier)) {
      currentGoals.strategy = previousGoals.strategy
      currentGoals.tier = previousGoals.tier
      currentGoals.after = after
      currentGoals.status = 'PREVIOUS_EDITION_FALLBACK'
      currentGoals.fallbackValidation = { source: `${options.competition}:PREVIOUS`,
        sourceTraining: previousGoals.after.training, sourceValidation: previousGoals.after.validation,
        currentFull: after.full, noCurrentEvidence: matches.length === 0 }
      currentGoals.validationUncertainty = { dateCount: 0, roi95Interval: null }
    }
  }
  delete previous.stableMatches
  delete current.stableMatches
  return { competition: options.competition, ranges: [previous, current], independentVerification: 'PASSED' }
}

if (!isMainThread) {
  optimizeCompetition(workerData).then(result => parentPort.postMessage({ type: 'result', result }))
    .catch(error => { parentPort.postMessage({ type: 'error', message: error.stack }); process.exitCode = 1 })
}
