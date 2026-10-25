import assert from 'node:assert/strict'
import test from 'node:test'
import { Worker } from 'node:worker_threads'
import {
  POLICY, validationPlan, minimumWdlSampling, wdlPairPass, goalsCandidatePass,
  modelCandidates, wdlRules, goalsRules, parameterNeighbors, selectWdlPair, selectGoals,
  samplingWindow, freezeShortlist
} from './constrained-optimization-core.mjs'
import { searchWdl, searchGoals, verifyMetrics } from './constrained-optimization-worker.mjs'
import { DEFAULT_RULE, DEFAULT_GOALS } from './strategy-optimization-core.mjs'

function matches() {
  return Array.from({ length: 80 }, (_, index) => ({
    matchId: String(index), matchDate: `2026-09-${String(1 + Math.floor(index / 4)).padStart(2, '0')}`,
    sportteryMatchId: String(index), sportteryNormalAvailable: true,
    sportteryNormalOdds: { win: 2.2 + index % 4, draw: 3.5, lose: 3 },
    sportteryHandicap: -1, sportteryHandicapOdds: { win: 3, draw: 4.5, lose: 2.5 },
    adjustedNormalProbability: { win: 55, draw: 25, lose: 20 },
    adjustedHandicapProbabilities: [{ handicap: -1, probability: { win: 30, draw: 30, lose: 40 } }],
    adjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({ totalGoals, probability: [5, 15, 25, 25, 15, 8, 5, 2][totalGoals] })),
    sportteryTotalGoalsOdds: Object.fromEntries(['goal0', 'goal1', 'goal2', 'goal3', 'goal4', 'goal5', 'goal6', 'goal7Plus'].map((key, i) => [key, [12, 6, 4, 4.8, 7, 15, 25, 40][i]])),
    scoreText: `${index % 3}-${index % 2}`
  }))
}

const metrics = (roi, samplingRate = 0.8, selectionHitRate = 0.5) => ({
  roi, samplingRate, selectionHitRate, totalStake: 100, recommendedMatchCount: 80
})
const candidate = (roi, samplingRate = 0.8) => ({
  training: metrics(roi, samplingRate), validation: metrics(roi, samplingRate), full: metrics(roi, samplingRate)
})

test('所有赛事采样率专项规则与严格ROI关系保留', () => {
  assert.equal(minimumWdlSampling('PREMIER_LEAGUE', 'PREVIOUS', 'STABLE'), 0.6)
  assert.equal(minimumWdlSampling('EUROPEAN_CHAMPIONSHIP', 'CURRENT', 'STABLE'), 0.4)
  assert.equal(minimumWdlSampling('K_LEAGUE_1', 'CURRENT', 'AGGRESSIVE'), 0.4)
  assert.equal(minimumWdlSampling('FINNISH_VEIKKAUSLIIGA', 'CURRENT', 'AGGRESSIVE'), 0.333)
  assert.ok(wdlPairPass(candidate(0.05), candidate(0.06, 0.6), 'PREMIER_LEAGUE', 'CURRENT'))
  assert.equal(wdlPairPass(candidate(0.049), candidate(0.06, 0.6), 'PREMIER_LEAGUE', 'CURRENT'), false)
  assert.equal(wdlPairPass(candidate(0.05), candidate(0.05, 0.6), 'PREMIER_LEAGUE', 'CURRENT'), false)
  const invalid = candidate(0.1, 0.6)
  invalid.validation.samplingRate = 0.9
  assert.equal(wdlPairPass(candidate(0.05), invalid, 'PREMIER_LEAGUE', 'CURRENT'), false)
  assert.equal(wdlPairPass(candidate(0.05), candidate(0.1, 0.333), 'EUROPEAN_CHAMPIONSHIP', 'CURRENT'), false)
})

test('进球数同时严格约束采样率和单注命中率，验证ROI允许0', () => {
  const good = candidate(0.1)
  good.validation.roi = 0
  assert.ok(goalsCandidatePass(good, 0.333))
  good.training.selectionHitRate = 0.333
  assert.equal(goalsCandidatePass(good, 0.333), false)
  good.training.selectionHitRate = 0.5
  good.full.roi = 0
  assert.equal(goalsCandidatePass(good, 0.25), false)
  good.full.roi = 0.1
  good.validation.roi = -0.01
  assert.equal(goalsCandidatePass(good, 0.1), false)
})

test('同日不跨70/30分界，小样本不产生训练或虚构验证', () => {
  const sample = matches()
  const plan = validationPlan(sample)
  assert.equal(plan.trainingMatchCount, 56)
  assert.equal(plan.validationMatchCount, 24)
  for (const match of sample) assert.equal(match.matchDate in plan.dateToBlock, match.matchDate < plan.validationFrom)
  assert.equal(validationPlan(sample.slice(0, 15)).available, false)
  assert.equal(validationPlan(sample.map(match => ({ ...match, matchDate: '2026-09-01' }))).available, false)
  assert.equal(validationPlan([]).blockCount, 0)
})

test('污染验证段比分和赔率不能改变粗搜、排序或冻结的候选', () => {
  const sample = matches()
  const plan = validationPlan(sample)
  const poisoned = structuredClone(sample)
  for (const match of poisoned) if (match.matchDate >= plan.validationFrom) {
    match.scoreText = '50-50'
    match.sportteryNormalOdds = { win: 999, draw: 999, lose: 999 }
    match.sportteryTotalGoalsOdds.goal7Plus = 999
  }
  const rules = wdlRules(1000, 'isolation', [DEFAULT_RULE])
  const run = values => {
    const pools = { STABLE: [], AGGRESSIVE: [] }
    searchWdl(values, rules, plan, 0, { competition: 'WORLD_CUP', range: 'PREVIOUS' }, pools)
    return pools
  }
  assert.deepEqual(run(sample), run(poisoned))
  const goals = goalsRules(1000, 'isolation', DEFAULT_GOALS)
  assert.deepEqual(searchGoals(sample, goals, plan, 0), searchGoals(poisoned, goals, plan, 0))
})

test('滚动模型和参数选择不读取下一块赛果', () => {
  const sample = matches()
  const plan = validationPlan(sample)
  const poisoned = structuredClone(sample)
  for (const match of poisoned) if (plan.dateToBlock[match.matchDate] >= 1) match.scoreText = '50-50'
  const rules = wdlRules(1000, 'rolling', [DEFAULT_RULE])
  const run = values => {
    const rolling = { STABLE: [null, null], AGGRESSIVE: [null, null] }
    searchWdl(values, rules, plan, 0, { competition: 'WORLD_CUP', range: 'PREVIOUS' }, { STABLE: [], AGGRESSIVE: [] }, rolling)
    return [rolling.STABLE[0], rolling.AGGRESSIVE[0]]
  }
  assert.deepEqual(run(sample), run(poisoned))
})

test('验证门禁按训练排序选择，不会追逐验证段最高ROI', () => {
  const stable = { ...candidate(0.1), score: 0.2, parameters: { id: 1 } }
  const first = { ...candidate(0.15, 0.7), score: 0.3, parameters: { id: 2 } }
  const second = { ...candidate(0.2, 0.6), score: 0.1, parameters: { id: 3 } }
  const shortlist = { STABLE: [stable], AGGRESSIVE: [first, second] }
  const context = { competition: 'WORLD_CUP', range: 'PREVIOUS', windows: { STABLE: { minimum: 0.6, maximum: 1 }, AGGRESSIVE: { minimum: 0.5, maximum: 1 } } }
  const selected = selectWdlPair(shortlist, context, value => value === second ? { ...value, validation: metrics(50), full: metrics(10, 0.6) } : value)
  assert.equal(selected.aggressive.parameters.id, 2)
  const goalA = { ...candidate(0.1), score: 1 }
  const goalB = { ...candidate(100), score: 0 }
  assert.equal(selectGoals({ '0.333': [goalA, goalB] }, value => value).candidate, goalA)
})

test('候选、参数扰动和多线程计算可重复，冻结名额有硬上限', async () => {
  assert.deepEqual(modelCandidates([], 12, 'seed'), modelCandidates([], 12, 'seed'))
  assert.deepEqual(goalsRules(1000, 'seed', DEFAULT_GOALS), goalsRules(1000, 'seed', DEFAULT_GOALS))
  for (const next of parameterNeighbors({ ...DEFAULT_GOALS, minimumOdds: 100, maximumOdds: 100 }, 'GOALS')) assert.ok(next.maximumOdds >= next.minimumOdds)
  const pool = Array.from({ length: 100 }, (_, i) => ({ score: i, modelIndex: i, parameters: { i }, training: metrics(0.2) }))
  assert.equal(freezeShortlist(pool, null, samplingWindow('WORLD_CUP', 'CURRENT', 'STABLE', 0.8), POLICY.wdlHoldoutLimit).length, 24)
  const url = new URL('./constrained-optimization-core.mjs', import.meta.url).href
  const parallel = await new Promise((resolve, reject) => {
    const worker = new Worker(`import(${JSON.stringify(url)}).then(module => require('node:worker_threads').parentPort.postMessage(module.wdlRules(1000, 'parallel', [${JSON.stringify(DEFAULT_RULE)}])))`, { eval: true })
    worker.on('message', resolve)
    worker.on('error', reject)
  })
  assert.deepEqual(parallel, wdlRules(1000, 'parallel', [DEFAULT_RULE]))
})

test('两类玩法均以页面算法复验，关闭与7+球结算正确', () => {
  const sample = matches()
  sample[0].scoreText = '8-1'
  sample[1].sportteryNormalOdds = null
  sample[2].sportteryTotalGoalsOdds = null
  for (const rule of wdlRules(1000, 'settlement', [DEFAULT_RULE]).filter((_, i) => i % 20 === 0)) verifyMetrics(sample, rule, 'WDL')
  for (const strategy of goalsRules(1000, 'settlement', DEFAULT_GOALS).filter((_, i) => i % 20 === 0)) verifyMetrics(sample, strategy, 'GOALS')
  assert.equal(verifyMetrics(sample, { ...DEFAULT_GOALS, maximumSelections: 0 }, 'GOALS').totalStake, 0)
  assert.equal(verifyMetrics([], DEFAULT_RULE, 'WDL').roi, null)
})
