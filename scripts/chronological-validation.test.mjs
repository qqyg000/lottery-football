import assert from 'node:assert/strict'
import test from 'node:test'
import { chronologicalSplitIndex } from './chronological-validation.mjs'

const dated = counts => counts.flatMap((count, index) => Array.from({ length: count }, () => ({
  matchDate: `2026-09-${String(index + 1).padStart(2, '0')}`
})))

test('训练至少10场不能导致同一天跨越训练和验证', () => {
  assert.equal(chronologicalSplitIndex(dated([8, 8]), 0.3, 10, 6), -1)
  assert.equal(chronologicalSplitIndex(dated([8, 8, 5]), 0.3, 10, 6), -1)
})

test('向前移动不足训练场次时改用下一个完整比赛日边界', () => {
  assert.equal(chronologicalSplitIndex(dated([9, 9, 7]), 0.3, 10, 6), 18)
})

test('已合法的上一版70/30边界保持不变', () => {
  assert.equal(chronologicalSplitIndex(dated([10, 4, 6]), 0.3, 10, 6), 14)
  assert.equal(chronologicalSplitIndex(dated([10, 6]), 0.3, 10, 6), 10)
  assert.equal(chronologicalSplitIndex(dated([20]), 0.3, 10, 6), -1)
  assert.equal(chronologicalSplitIndex([], 0.3, 10, 6), -1)
})

test('各种日期分组下的可用边界均满足原场次下限和严格时间隔离', () => {
  for (let a = 1; a <= 18; a++) for (let b = 1; b <= 18; b++) for (let c = 1; c <= 9; c++) {
    const matches = dated([a, b, c])
    const index = chronologicalSplitIndex(matches, 0.3, 10, 6)
    if (index < 0) continue
    assert.ok(index >= 10)
    assert.ok(matches.length - index >= 6)
    assert.ok(matches[index - 1].matchDate < matches[index].matchDate)
  }
})
