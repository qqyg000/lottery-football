import test from 'node:test'
import assert from 'node:assert/strict'

import {
  evaluateRecommendationBacktest,
  getAutomaticMarketSelection,
  getRecommendationKeys
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
