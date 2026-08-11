import test from 'node:test'
import assert from 'node:assert/strict'

import {
  evaluateTotalGoalsBacktest,
  evaluateRecommendationBacktest,
  getAutomaticMarketSelection,
  getRecommendationKeys,
  getTotalGoalsRecommendations
} from '../src/recommendation-backtest.mjs'

const PARAMETERS = {
  recommendationOdds: 1.5,
  handicapRecommendationThreshold: 80,
  handicapReverseThreshold: 30,
  singleRecommendationThreshold: 80
}

function createMatch(overrides = {}) {
  return {
    matchId: 'match-1',
    sportteryMatchId: 'sporttery-1',
    sportteryNormalAvailable: true,
    sportteryHandicap: -1,
    sportteryNormalOdds: { win: 2.2, draw: 3.1, lose: 2.8 },
    sportteryHandicapOdds: null,
    scoreText: '2 - 0',
    adjustedNormalProbability: { win: 60, draw: 25, lose: 15 },
    adjustedHandicapProbabilities: [{
      handicap: -1,
      handicapName: '主队让1球',
      probability: { win: 45, draw: 30, lose: 25 }
    }],
    ...overrides
  }
}

test('自动选盘不会选中没有赔率的让球盘口', () => {
  assert.deepEqual(getAutomaticMarketSelection(createMatch()), { normal: true })
})

test('有让球赔率时自动选中对应让球盘口', () => {
  const selection = getAutomaticMarketSelection(createMatch({
    sportteryHandicapOdds: { win: 2.5, draw: 3.2, lose: 2.3 }
  }))

  assert.deepEqual(selection, { normal: true, handicap: 'handicap--1' })
})

test('推荐计算使用实际有赔率的盘口', () => {
  const keys = getRecommendationKeys(createMatch(), {
    modelMode: 'after',
    globalParameters: PARAMETERS
  })

  assert.deepEqual([...keys], ['normal-win', 'normal-draw'])
})

test('界面和优化器共用的回测入口计算采样率与ROI', () => {
  const result = evaluateRecommendationBacktest([createMatch()], {
    oddsMatchCount: 1,
    modelMode: 'after',
    globalParameters: PARAMETERS
  })

  assert.equal(result.summary.recommendedMatchCount, 1)
  assert.equal(result.summary.recommendedSelectionCount, 2)
  assert.equal(result.summary.hitMatchCount, 1)
  assert.equal(result.summary.samplingRate, 1)
  assert.ok(Math.abs(result.summary.roi - 0.1) < 1e-12)
})

test('进球数策略按照命中概率排序且每场最多推荐4项', () => {
  const match = createMatch({
    sportteryTotalGoalsOdds: {
      goal0: 10,
      goal1: 5,
      goal2: 4,
      goal3: 4,
      goal4: 6,
      goal5: 10,
      goal6: 20,
      goal7Plus: 30
    },
    adjustedSportteryTotalGoalsProbabilities: [
      { totalGoals: 0, probability: 3 },
      { totalGoals: 1, probability: 8 },
      { totalGoals: 2, probability: 24 },
      { totalGoals: 3, probability: 27 },
      { totalGoals: 4, probability: 18 },
      { totalGoals: 5, probability: 10 },
      { totalGoals: 6, probability: 6 },
      { totalGoals: 7, probability: 4 }
    ]
  })
  const recommendations = getTotalGoalsRecommendations(match, {
    modelMode: 'after',
    strategy: {
      minimumProbability: 0,
      minimumExpectedValue: 0,
      minimumOdds: 1,
      maximumOdds: 100,
      maximumSelections: 4
    }
  })

  assert.equal(recommendations.length, 4)
  assert.deepEqual(recommendations.map(item => item.totalGoals), [3, 2, 4, 5])
})

test('进球数推荐优先使用独立固定模型的概率', () => {
  const match = createMatch({
    sportteryTotalGoalsOdds: {
      goal0: 10,
      goal1: 5,
      goal2: 4,
      goal3: 4,
      goal4: 6,
      goal5: 10,
      goal6: 20,
      goal7Plus: 30
    },
    adjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({
      totalGoals,
      probability: totalGoals === 2 ? 40 : 5
    })),
    fixedAdjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({
      totalGoals,
      probability: totalGoals === 4 ? 35 : 5
    }))
  })

  const recommendations = getTotalGoalsRecommendations(match, {
    modelMode: 'after',
    strategy: {
      minimumProbability: 0,
      minimumExpectedValue: 0,
      minimumOdds: 1,
      maximumOdds: 100,
      maximumSelections: 1
    }
  })

  assert.deepEqual(recommendations.map(item => item.totalGoals), [4])
})

test('进球数策略允许每场0项并按单项等额投注计算ROI', () => {
  const match = createMatch({
    scoreText: '2 - 1',
    sportteryTotalGoalsOdds: {
      goal0: 10,
      goal1: 5,
      goal2: 4,
      goal3: 4,
      goal4: 6,
      goal5: 10,
      goal6: 20,
      goal7Plus: 30
    },
    adjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({
      totalGoals,
      probability: totalGoals === 3 ? 30 : 10
    }))
  })
  assert.deepEqual(getTotalGoalsRecommendations(match, {
    strategy: { maximumSelections: 0 }
  }), [])

  const result = evaluateTotalGoalsBacktest([match], {
    modelMode: 'after',
    oddsMatchCount: 1,
    strategy: {
      minimumProbability: 20,
      minimumExpectedValue: 1,
      minimumOdds: 1,
      maximumOdds: 100,
      maximumSelections: 1
    }
  })
  assert.equal(result.recommendedSelectionCount, 1)
  assert.equal(result.winningSelectionCount, 1)
  assert.equal(result.totalReturn, 4)
  assert.equal(result.roi, 3)
})

test('进球数多选按独立单关的总注数计算ROI和命中率', () => {
  const match = createMatch({
    scoreText: '2 - 1',
    sportteryTotalGoalsOdds: {
      goal0: 10,
      goal1: 5,
      goal2: 4,
      goal3: 4,
      goal4: 6,
      goal5: 10,
      goal6: 20,
      goal7Plus: 30
    },
    adjustedSportteryTotalGoalsProbabilities: Array.from({ length: 8 }, (_, totalGoals) => ({
      totalGoals,
      probability: totalGoals === 3 ? 30 : (totalGoals === 2 ? 20 : 10)
    }))
  })

  const result = evaluateTotalGoalsBacktest([match], {
    modelMode: 'after',
    oddsMatchCount: 1,
    strategy: {
      minimumProbability: 0,
      minimumExpectedValue: 0,
      minimumOdds: 1,
      maximumOdds: 100,
      maximumSelections: 2
    }
  })

  assert.equal(result.recommendedSelectionCount, 2)
  assert.equal(result.winningSelectionCount, 1)
  assert.equal(result.totalStake, 2)
  assert.equal(result.totalReturn, 4)
  assert.equal(result.netProfit, 2)
  assert.equal(result.roi, 1)
  assert.equal(result.hitRate, 0.5)
})
