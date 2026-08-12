import test from 'node:test'
import assert from 'node:assert/strict'

import { meetsMinimumConstraint } from '../../scripts/total-goals-optimization-constraints.mjs'

test('严格约束要求采样率和命中率大于阈值', () => {
  assert.equal(meetsMinimumConstraint(0.334, 0.333, true), true)
  assert.equal(meetsMinimumConstraint(0.333, 0.333, true), false)
  assert.equal(meetsMinimumConstraint(0.3329, 0.333, true), false)
})

test('严格正收益约束拒绝零ROI和负ROI', () => {
  assert.equal(meetsMinimumConstraint(0.000001, 0, true), true)
  assert.equal(meetsMinimumConstraint(0, 0, true), false)
  assert.equal(meetsMinimumConstraint(-0.01, 0, true), false)
})

test('非严格验证集约束允许ROI等于下限', () => {
  assert.equal(meetsMinimumConstraint(0, 0, false), true)
  assert.equal(meetsMinimumConstraint(-0.000001, 0, false), false)
})

test('无效指标不会通过约束', () => {
  assert.equal(meetsMinimumConstraint(null, 0, true), false)
  assert.equal(meetsMinimumConstraint(Number.NaN, 0, true), false)
  assert.equal(meetsMinimumConstraint(Number.POSITIVE_INFINITY, 0, true), false)
})
