import assert from 'node:assert/strict'
import test from 'node:test'
import {
  chronologicalPlan, prepareMatches, evaluateBlocks, searchCandidates, ruleCandidates,
  goalCandidates, riskAdjustedScore, DEFAULT_RULE, DEFAULT_GOALS
} from './strategy-optimization-core.mjs'
import { verifyMetrics, modelCandidates } from './unconstrained-optimization-worker.mjs'

function matches(count = 80) {
  return Array.from({ length: count }, (_, index) => ({
    matchId: String(index), matchDate: `2026-09-${String(Math.floor(index / 4) + 1).padStart(2, '0')}`,
    sportteryMatchId: String(index + 1), sportteryNormalAvailable: true,
    sportteryNormalOdds: { win: 1.7 + index % 7, draw: 3.4, lose: 2.8 },
    sportteryHandicap: index % 2 ? -1 : 1,
    sportteryHandicapOdds: { win: 2.2, draw: 4.6, lose: 2.6 },
    adjustedNormalProbability: { win: 45 + index % 9, draw: 30, lose: 25 - index % 9 },
    adjustedHandicapProbabilities: [-1, 1].map(handicap => ({ handicap, probability: { win: 30, draw: 25, lose: 45 } })),
    adjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({ totalGoals, probability: [5, 15, 25, 25, 15, 8, 5, 2][totalGoals] })),
    sportteryTotalGoalsOdds: Object.fromEntries(['goal0', 'goal1', 'goal2', 'goal3', 'goal4', 'goal5', 'goal6', 'goal7Plus'].map((key, i) => [key, [12, 6, 3, 3.8, 7, 15, 25, 40][i]])),
    scoreText: `${index % 4}-${index % 3}`
  }))
}

test('同日比赛不跨分区，新增样本及之后日期全部隔离', () => {
  const sample = matches()
  const plan = chronologicalPlan(sample, '2026-09-14')
  assert.equal(plan.testFrom, '2026-09-14')
  for (const match of sample) assert.equal(match.matchDate in plan.dateToBlock, match.matchDate < plan.testFrom)
  const blocks = Array.from({ length: plan.blockCount }, (_, index) => Object.keys(plan.dateToBlock).filter(date => plan.dateToBlock[date] === index))
  for (let index = 1; index < blocks.length; index++) assert.ok(blocks[index - 1].at(-1) < blocks[index][0])
  assert.equal(chronologicalPlan([], '2026-09-14').blockCount, 0)
  assert.equal(chronologicalPlan(sample, '2026-08-01').blockCount, 0)
})

test('任意改变测试段赛果和赔率不能改变最终参数或训练评分', () => {
  const sample = matches()
  const plan = chronologicalPlan(sample, '2026-09-17')
  const poisoned = structuredClone(sample)
  for (const match of poisoned) if (match.matchDate >= plan.testFrom) {
    match.scoreText = '50-50'
    match.sportteryNormalOdds = { win: 999, draw: 999, lose: 999 }
    match.sportteryTotalGoalsOdds.goal7Plus = 999
  }
  for (const market of ['WDL', 'GOALS']) {
    const candidates = market === 'WDL' ? ruleCandidates(200, 'test') : goalCandidates(200, 'test').slice(0, 200)
    assert.deepEqual(
      searchCandidates(prepareMatches(sample, market, plan), candidates, market, plan.blockCount),
      searchCandidates(prepareMatches(poisoned, market, plan), candidates, market, plan.blockCount)
    )
  }
})

test('滚动选参不读取下一时间块赛果', () => {
  const sample = matches()
  const plan = chronologicalPlan(sample)
  const poisoned = structuredClone(sample)
  for (const match of poisoned) if (plan.dateToBlock[match.matchDate] >= 1) match.scoreText = '19-19'
  const candidates = ruleCandidates(100, 'rolling')
  const run = values => searchCandidates(prepareMatches(values, 'WDL', plan), candidates, 'WDL', plan.blockCount).STABLE.folds[0]
  assert.deepEqual(run(sample).parameters, run(poisoned).parameters)
  assert.deepEqual(run(sample).training, run(poisoned).training)
})

test('负ROI与低采样率候选仍可入选，不存在正收益或最低采样率硬门槛', () => {
  const sample = matches().map((match, index) => ({ ...match, scoreText: '0-10',
    sportteryNormalOdds: { win: index === 0 ? 10 : 1.1, draw: 1.1, lose: 1.1 }, sportteryHandicapOdds: null }))
  const plan = chronologicalPlan(sample)
  const candidate = { ...DEFAULT_RULE, recommendationOdds: 5, singleRecommendationThreshold: 0 }
  const result = searchCandidates(prepareMatches(sample, 'WDL', plan), [candidate], 'WDL', plan.blockCount).STABLE.best
  assert.ok(result)
  assert.equal(result.training.roi, -1)
  assert.equal(result.training.recommendedMatchCount, 1)
})

test('空样本没有虚构ROI；孤立高收益受到先验和波动惩罚', () => {
  const empty = evaluateBlocks([], DEFAULT_RULE, 'WDL')[0]
  assert.equal(empty.roi, null)
  assert.equal(riskAdjustedScore([empty], 'STABLE'), -Infinity)
  const sample = matches(1)
  sample[0].scoreText = '1-0'
  sample[0].sportteryNormalOdds.win = 20
  const metrics = verifyMetrics(sample, { ...DEFAULT_RULE, singleRecommendationThreshold: 0 }, 'WDL')
  assert.ok(riskAdjustedScore([metrics], 'STABLE') < metrics.roi)
  assert.ok(riskAdjustedScore([metrics], 'STABLE') < riskAdjustedScore([metrics], 'AGGRESSIVE'))
})

test('胜平负和进球数快速结算均与页面算法一致，包含7+球、缺失赔率和关闭方案', () => {
  const sample = matches()
  sample[0].scoreText = '7-2'
  sample[1].sportteryTotalGoalsOdds = null
  sample[2].sportteryNormalOdds.win = null
  sample[3].sportteryMatchId = null
  for (const rule of ruleCandidates(150, 'equivalence')) verifyMetrics(sample, rule, 'WDL')
  for (const strategy of [...goalCandidates(200, 'equivalence').filter((_, index) => index % 50 === 0), { ...DEFAULT_GOALS, maximumSelections: 0 }]) {
    verifyMetrics(sample, strategy, 'GOALS')
  }
})

test('模型候选和规则候选可重复生成，不依赖留出赛果', () => {
  assert.deepEqual(modelCandidates([], 10, 'seed'), modelCandidates([], 10, 'seed'))
  assert.deepEqual(ruleCandidates(100, 'seed'), ruleCandidates(100, 'seed'))
})
