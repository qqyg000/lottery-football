import assert from 'node:assert/strict'
import test from 'node:test'
import { evaluateRecommendationBacktest } from '../frontend/src/recommendation-backtest.mjs'
import { evaluatePreparedRecommendationSummary } from './wdl-fast-evaluator.mjs'

test('预计算与页面算法在随机参数、阈值边界和不完整赔率下保持一致', () => {
  let seed = 19092026
  const random = () => ((seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0) / 4294967296)
  const probability = () => ({ win: Math.floor(random() * 101), draw: Math.floor(random() * 101), lose: Math.floor(random() * 101) })
  const odds = () => Object.fromEntries(['win', 'draw', 'lose'].map(key => [key,
    random() < 0.1 ? null : Math.round((1 + random() * 12) * 100) / 100
  ]))
  const matches = Array.from({ length: 180 }, (_, index) => ({
    matchId: String(index),
    sportteryMatchId: index % 19 === 0 ? null : String(index),
    sportteryNormalAvailable: index % 3 !== 0,
    sportteryNormalOdds: odds(),
    sportteryHandicap: index % 2 ? -1 : 1,
    sportteryHandicapOdds: odds(),
    adjustedNormalProbability: probability(),
    adjustedHandicapProbabilities: [-2, -1, 1, 2].map(handicap => ({ handicap, probability: probability() })),
    scoreText: index % 17 === 0 ? '' : `${Math.floor(random() * 5)}-${Math.floor(random() * 5)}`
  }))
  for (let index = 0; index < 600; index++) {
    const globalParameters = {
      recommendationOdds: index % 11 === 0 ? 100 : Math.round((1 + random() * 6) * 100) / 100,
      handicapRecommendationThreshold: Math.floor(random() * 101),
      handicapReverseThreshold: Math.floor(random() * 101),
      singleRecommendationThreshold: Math.floor(random() * 101)
    }
    const expected = evaluateRecommendationBacktest(matches, { modelMode: 'after', globalParameters }).summary
    const actual = evaluatePreparedRecommendationSummary(matches, globalParameters)
    assert.deepEqual(actual, expected, JSON.stringify(globalParameters))
  }
  assert.deepEqual(
    evaluatePreparedRecommendationSummary([], {}, 0),
    evaluateRecommendationBacktest([], {}).summary
  )
})
