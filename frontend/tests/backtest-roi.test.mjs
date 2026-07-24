import test from 'node:test'
import assert from 'node:assert/strict'

import {
  calculateFlatStakeBacktest,
  calculateMinimumCoveredMatchCount,
  calculateSamplingRate
} from '../src/backtest-roi.mjs'

test('单选命中时按一个推荐项计算投入和返奖', () => {
  const result = calculateFlatStakeBacktest([2.2], 1, 1)

  assert.equal(result.totalStake, 1)
  assert.equal(result.totalReturn, 2.2)
  assert.ok(Math.abs(result.netProfit - 1.2) < 1e-12)
  assert.ok(Math.abs(result.roi - 1.2) < 1e-12)
  assert.equal(result.averageReturnIncludingMisses, 2.2)
})

test('多选按推荐项总数计算投入', () => {
  const result = calculateFlatStakeBacktest([2.5], 2, 1)

  assert.equal(result.totalStake, 2)
  assert.equal(result.totalReturn, 2.5)
  assert.equal(result.netProfit, 0.5)
  assert.equal(result.roi, 0.25)
})

test('未中奖场次以零返奖计入场均返奖', () => {
  const result = calculateFlatStakeBacktest([2.4], 3, 2)

  assert.equal(result.totalReturn, 2.4)
  assert.equal(result.averageReturnIncludingMisses, 1.2)
  assert.ok(Math.abs(result.roi + 0.2) < 1e-12)
})

test('全部未中奖时 ROI 为负百分之百', () => {
  const result = calculateFlatStakeBacktest([], 3, 2)

  assert.equal(result.totalReturn, 0)
  assert.equal(result.netProfit, -3)
  assert.equal(result.roi, -1)
  assert.equal(result.averageReturnIncludingMisses, 0)
})

test('没有推荐项时不计算 ROI', () => {
  const result = calculateFlatStakeBacktest([], 0, 0)

  assert.equal(result.totalStake, 0)
  assert.equal(result.netProfit, 0)
  assert.equal(result.roi, null)
  assert.equal(result.averageReturnIncludingMisses, null)
})

test('采样率等于推荐比赛数除以已完赛且有赔率比赛数', () => {
  assert.equal(calculateSamplingRate(11, 12), 11 / 12)
})

test('没有已完赛且有赔率比赛时采样率为空', () => {
  assert.equal(calculateSamplingRate(0, 0), null)
})

test('最少推荐场次按有赔率比赛数保证采样率严格大于约束值', () => {
  const minimumCoveredMatchCount = calculateMinimumCoveredMatchCount(12, 0.666)

  assert.equal(minimumCoveredMatchCount, 8)
  assert.ok(calculateSamplingRate(minimumCoveredMatchCount, 12) > 0.666)
})
