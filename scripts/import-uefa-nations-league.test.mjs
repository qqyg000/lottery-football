import test from 'node:test'
import assert from 'node:assert/strict'
import { regulationScore, shanghaiDate, parseCsv } from './import-uefa-nations-league.mjs'

test('欧国联比分排除加时和点球，保留90分钟补时进球', () => {
  const match = { id: 'fixture', status: { scoreStr: '3 - 1', reason: { short: 'AET' } } }
  const details = { content: { matchFacts: { events: { events: [
    { type: 'Goal', time: 32, isHome: false },
    { type: 'Goal', time: 90, addedTime: 3, isHome: true },
    { type: 'Goal', time: 105, isHome: true },
    { type: 'Goal', time: 120, isHome: true },
    { type: 'Goal', time: 120, isHome: false, isPenaltyShootoutEvent: true }
  ] } } } }
  assert.deepEqual(regulationScore(match, details), [1, 1])
  assert.throws(() => regulationScore(match, null), /常规时间/)
  assert.equal(shanghaiDate('2019-06-06T18:45:00Z'), '2019-06-07')
})

test('常规时间比分优先读取FT节点，点球0比0不转为罚球比分', () => {
  const match = { id: 'fixture', status: { scoreStr: '0 - 0', reason: { short: 'Pen' } } }
  const details = { content: { matchFacts: { events: { events: [
    { type: 'Half', time: 90, halfStrShort: 'FT', homeScore: 0, awayScore: 0 }
  ] } } } }
  assert.deepEqual(regulationScore(match, details), [0, 0])
  assert.deepEqual(parseCsv('\uFEFFhome,away\r\n"A, B","C""D"\r\n').rows,
    [{ home: 'A, B', away: 'C"D' }])
})
