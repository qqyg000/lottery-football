import assert from 'node:assert/strict'
import { execFileSync } from 'node:child_process'
import fs from 'node:fs/promises'
import os from 'node:os'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import test from 'node:test'
import { filterBacktestProfiles } from '../frontend/src/backtest-competitions.mjs'

test('旧配置的苏足总杯档案被过滤，其他赛事原值及输入保持不变', () => {
  const profiles = {
    'SCOTTISH_FA_CUP:CURRENT:STABLE': { recommendationOdds: 2 },
    'SCOTTISH_FA_CUP:PREVIOUS': { maximumSelections: 1 },
    'PREMIER_LEAGUE:CURRENT:STABLE': { recommendationOdds: 1.83 },
    'WORLD_CUP:PREVIOUS': { maximumSelections: 2 }
  }
  const before = structuredClone(profiles)
  assert.deepEqual(filterBacktestProfiles(profiles), {
    'PREMIER_LEAGUE:CURRENT:STABLE': { recommendationOdds: 1.83 },
    'WORLD_CUP:PREVIOUS': { maximumSelections: 2 }
  })
  assert.deepEqual(profiles, before)
})

test('旧回测报告中的苏足总杯不再出现在汇总中，标题数量随结果更新', async t => {
  const directory = await fs.mkdtemp(path.join(os.tmpdir(), 'backtest-scope-'))
  const files = ['wdl.json', 'goals.json', 'summary.md'].map(file => path.join(directory, file))
  t.after(async () => {
    await Promise.all(files.map(file => fs.rm(file, { force: true })))
    await fs.rmdir(directory)
  })
  const metrics = { availableMatchCount: 10, recommendedMatchCount: 6, hitMatchCount: 4,
    samplingRate: 0.6, hitRate: 0.5, roi: 0.2 }
  const rows = [['PREMIER_LEAGUE', '英超'], ['SCOTTISH_FA_CUP', '苏足总杯']].map(([competition, competitionName]) => ({
    competition, competitionName, range: 'CURRENT', preset: 'STABLE',
    key: `${competition}:CURRENT`, oddsMatchCount: 10, metrics
  }))
  await fs.writeFile(files[0], JSON.stringify({ verification: rows, optimizationResults: [], violations: [] }))
  await fs.writeFile(files[1], JSON.stringify({ strategies: rows }))
  execFileSync(process.execPath, [fileURLToPath(new URL('./generate-optimization-summary.mjs', import.meta.url)),
    '--wdl-report', files[0], '--goals-optimization-report', files[1],
    '--goals-verification-report', files[1], '--output', files[2]], { windowsHide: true })
  const summary = await fs.readFile(files[2], 'utf8')
  assert.doesNotMatch(summary, /苏足总杯|SCOTTISH_FA_CUP/)
  assert.match(summary, /胜平负方案（1 套）/)
  assert.match(summary, /进球数方案（1 套，稳健\/激进共用）/)
  assert.match(summary, /英超.*6\/10.*60\.00%.*20\.00%/)
})
