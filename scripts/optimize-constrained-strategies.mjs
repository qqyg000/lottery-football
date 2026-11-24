import fs from 'node:fs/promises'
import path from 'node:path'
import os from 'node:os'
import { createHash } from 'node:crypto'
import { Worker } from 'node:worker_threads'
import { fileURLToPath } from 'node:url'
import { BACKTEST_COMPETITIONS } from '../frontend/src/backtest-competitions.mjs'
import { combineMetrics, RISK_SETTINGS } from './strategy-optimization-core.mjs'
import { POLICY, wdlPairPass, goalsCandidatePass, goalsTrainingPass } from './constrained-optimization-core.mjs'
import { SCORE_POLICY, scorePass } from './score-top3-optimization.mjs'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const date = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
const argument = (name, fallback) => {
  const index = process.argv.indexOf(name)
  return index < 0 ? fallback : process.argv[index + 1]
}
const integer = (name, fallback, minimum = 1, maximum = 1000000) => {
  const value = Number(argument(name, fallback))
  if (!Number.isSafeInteger(value) || value < minimum || value > maximum) throw new Error(`${name} 必须是 ${minimum} 至 ${maximum} 之间的整数`)
  return value
}
const options = {
  directory: path.resolve(ROOT, argument('--directory', `target/optimization-robust-${date.replaceAll('-', '')}`)),
  apiBase: argument('--base-url', 'http://127.0.0.1:18080'), asOfDate: date,
  workers: Math.min(os.availableParallelism(), integer('--workers', Math.max(1, os.availableParallelism() - 4))),
  simulations: integer('--simulations', 50000, 1000, 500000), coarseSimulations: integer('--coarse-simulations', 8000, 1000, 500000),
  modelCount: integer('--models', 12, 3, 100), finalistModels: integer('--finalist-models', 4, 1, 100),
  ruleCount: integer('--rules', 12000, 100), goalCount: integer('--goal-rules', 18000, 100),
  objective: argument('--objective', 'roi'),
  recentFrom: argument('--recent-from', argument('--objective', 'roi') === 'score-top3' ? '2026-10-26' : '2026-09-29')
}
if (!/^\d{4}-\d{2}-\d{2}$/.test(options.recentFrom)) throw new Error('--recent-from 必须为 YYYY-MM-DD')
if (!['roi', 'score-top3'].includes(options.objective)) throw new Error('--objective 必须为 roi 或 score-top3')
if (options.finalistModels > options.modelCount) throw new Error('精搜模型数不能大于粗搜模型数')
const requested = argument('--competitions', 'ALL')
const competitions = requested === 'ALL' ? BACKTEST_COMPETITIONS : BACKTEST_COMPETITIONS.filter(item => requested.split(',').includes(item.code))
if (!competitions.length || (requested !== 'ALL' && requested.split(',').some(code => !competitions.some(item => item.code === code)))) throw new Error('赛事代码无效')
if (process.argv.includes('--apply') && competitions.length !== BACKTEST_COMPETITIONS.length) throw new Error('正式应用要求全部17类赛事完成，禁止应用局部结果')
const hash = value => createHash('sha256').update(value).digest('hex')
const save = (file, value) => fs.writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')
const percent = value => Number.isFinite(value) ? `${(value * 100).toFixed(2)}%` : '—'
const names = Object.fromEntries(BACKTEST_COMPETITIONS.map(item => [item.code, item.name]))

async function runWorkers(tasks, shared) {
  const active = new Set()
  const stages = new Map()
  const results = []
  let next = 0
  let stopped = false
  const started = Date.now()
  const timer = setInterval(() => {
    const stagesText = [...stages.entries()].map(([key, value]) => `${key} ${value.stage} ${value.model}/${value.total}`).join('；')
    process.stdout.write(`已完成 ${results.length}/${tasks.length} 赛事，活动线程 ${active.size}，用时 ${Math.round((Date.now() - started) / 1000)} 秒；${stagesText}\n`)
  }, 20000)
  try {
    await Promise.all(Array.from({ length: Math.min(shared.workers, tasks.length) }, async () => {
      while (!stopped && next < tasks.length) {
        const task = tasks[next++]
        const result = await new Promise((resolve, reject) => {
          const worker = new Worker(new URL('./constrained-optimization-worker.mjs', import.meta.url), { workerData: { ...shared, competition: task.code } })
          active.add(worker)
          let received = false
          worker.on('message', message => {
            if (message.type === 'progress') stages.set(message.key, message)
            if (message.type === 'result') { received = true; resolve(message.result) }
            if (message.type === 'error') reject(new Error(message.message))
          })
          worker.on('error', reject)
          worker.on('exit', code => {
            active.delete(worker)
            if (!received) reject(new Error(`${task.code} 工作线程异常退出 ${code}`))
          })
        })
        result.runFingerprint = shared.runFingerprint
        await save(path.join(shared.directory, `${task.code}-result.json`), result)
        results.push(result)
        for (const key of stages.keys()) if (key.startsWith(task.code)) stages.delete(key)
        process.stdout.write(`完成 ${task.name}，${result.ranges.map(range => `${range.range} ${range.rows.filter(row => row.after.full.totalStake > 0).length}/3套启用`).join('；')}\n`)
      }
    }))
  } catch (error) {
    stopped = true
    await Promise.allSettled([...active].map(worker => worker.terminate()))
    throw error
  } finally { clearInterval(timer) }
  return results
}

function auditResults(results) {
  const violations = []
  let checkedRows = 0
  let checkedScoreProfiles = 0
  for (const result of results) for (const range of result.ranges) {
    const { competition } = result
    if (range.independentVerification !== 'PASSED') violations.push(`${competition}:${range.range} 未完成独立结算`)
    if (range.holdoutCandidates.stable > POLICY.wdlHoldoutLimit || range.holdoutCandidates.aggressive > POLICY.wdlHoldoutLimit || range.holdoutCandidates.goals > POLICY.goalsHoldoutLimit) violations.push(`${competition}:${range.range} 超过留出候选上限`)
    const [stable, aggressive, goals] = range.rows
    checkedRows += range.rows.length
    if (range.scores) for (const [i, row] of [stable, aggressive].entries()) {
      const score = range.scores.profiles[i]
      checkedScoreProfiles++
      if (Object.keys(score.model).some(key => key !== 'handicapSmoothingFactor' && score.model[key] !== row.profile.modelFactors[key]) ||
          score.accepted !== Boolean(range.scores.finalist && scorePass(score.candidate.training, score.candidate.validation))) {
        violations.push(`${competition}:${range.range}:${row.preset} 比分模型或门槛不一致`)
      }
    }
    const candidate = row => ({ training: row.after.training, validation: row.after.validation, full: row.after.full })
    if (stable.after.full.totalStake || aggressive.after.full.totalStake) {
      if (!wdlPairPass(candidate(stable), candidate(aggressive), competition, range.range)) violations.push(`${competition}:${range.range} 胜平负门槛未通过`)
    } else if ([stable, aggressive].some(row => row.profile.globalParameters.recommendationOdds !== 100)) violations.push(`${competition}:${range.range} 关闭参数错误`)
    if (goals.strategy.maximumSelections > 0) {
      if (goals.status === 'PREVIOUS_EDITION_FALLBACK') {
        const previous = result.ranges.find(item => item.range === 'PREVIOUS').rows.find(row => row.market === 'GOALS')
        if (!goalsCandidatePass(candidate(previous), previous.tier) ||
            goals.after.full.availableMatchCount > 10 ||
            goals.after.full.availableMatchCount > 0 && !goalsTrainingPass(goals.after.full, goals.tier)) violations.push(`${competition}:CURRENT 小样本回退不合规`)
      } else if (!goalsCandidatePass(candidate(goals), goals.tier)) violations.push(`${competition}:${range.range} 进球数门槛未通过`)
    } else if (goals.after.full.totalStake) violations.push(`${competition}:${range.range} 进球数关闭后仍投注`)
  }
  return { checkedRows, checkedScoreProfiles, violations, independentSettlementViolations: 0 }
}

function summarizeScores(results) {
  const groups = []
  for (const range of ['CURRENT', 'PREVIOUS']) for (const [i, preset] of ['STABLE', 'AGGRESSIVE'].entries()) {
    const selected = results.flatMap(result => result.ranges.filter(row => row.range === range && row.scores).map(row => row.scores.profiles[i]))
    if (!selected.length) continue
    const periods = Object.fromEntries(['full', 'training', 'validation', 'fresh'].map(period => {
      const sum = (side, key) => selected.reduce((total, row) => total + row.periods[period][side][key], 0)
      const matches = sum('before', 'matches')
      const before = sum('before', 'hits')
      const after = sum('after', 'hits')
      return [period, { matches, before, after }]
    }))
    groups.push({ range, preset, updated: selected.filter(row => row.accepted).length, periods })
  }
  return groups
}

function createReport(results, manifest) {
  const rows = results.flatMap(result => result.ranges.flatMap(range => range.rows))
  const groups = []
  for (const range of ['PREVIOUS', 'CURRENT']) for (const [market, preset] of [['WDL', 'STABLE'], ['WDL', 'AGGRESSIVE'], ['GOALS', 'SHARED']]) {
    const selected = rows.filter(row => row.range === range && row.market === market && row.preset === preset)
    const periods = ['full', 'training', 'validation', 'recent']
    groups.push({ range, market, preset, activeBefore: selected.filter(row => row.before.full.totalStake > 0).length,
      activeAfter: selected.filter(row => row.after.full.totalStake > 0).length,
      before: Object.fromEntries(periods.map(period => [period, combineMetrics(selected.map(row => row.before[period]))])),
      after: Object.fromEntries(periods.map(period => [period, combineMetrics(selected.map(row => row.after[period]))])),
      walkForward: combineMetrics(selected.map(row => row.walkForward)) })
  }
  const audit = auditResults(results)
  return { generatedAt: new Date().toISOString(), manifest, groups, scoreGroups: summarizeScores(results), results, audit,
    activeWdl: rows.filter(row => row.market === 'WDL' && row.after.full.totalStake > 0).length,
    activeGoals: rows.filter(row => row.market === 'GOALS' && row.after.full.totalStake > 0).length,
    noDataRows: rows.filter(row => row.status === 'NO_DATA').length,
    insufficientDataRows: rows.filter(row => row.status === 'INSUFFICIENT_DATA').length }
}

function scoreMarkdown(report) {
  const scoreCell = comparison => comparison.matches
    ? `${comparison.before} → ${comparison.after}/${comparison.matches}（${percent(comparison.before / comparison.matches)} → ${percent(comparison.after / comparison.matches)}）` : '—'
  const lines = ['## 前 3 预测比分命中率', '',
    '每场按常规时间实际比分与修正后前 3 个预测比分比较，涵盖全部具备有效比分的完赛场次。进球数使用同范围的稳健模型。', '',
    '| 范围 | 档位 | 比分模型更新 | 全量 | 时间验证段 | 新增赛程 |', '| --- | --- | ---: | --- | --- | --- |']
  for (const { range, preset, updated, periods } of report.scoreGroups) {
    lines.push(`| ${range === 'CURRENT' ? '仅本届' : '含上届'} | ${preset === 'STABLE' ? '稳健' : '激进'} | ${updated} | ${scoreCell(periods.full)} | ${scoreCell(periods.validation)} | ${scoreCell(periods.fresh)} |`)
  }
  lines.push('', '两种范围有重叠，稳健和激进是替代档位。时间验证段参与模型准入，历史尾段可能在旧版优化中使用过。', '',
    '### 各赛事前 3 比分变化', '', '| 赛事 | 范围 | 验证开始 | 验证样本 | 稳健全量 | 激进全量 | 稳健验证 | 激进验证 |',
    '| --- | --- | --- | --- | --- | --- | --- | --- |')
  for (const result of report.results) for (const { range, scores } of result.ranges) {
    const cell = comparison => scoreCell({ matches: comparison.before.matches, before: comparison.before.hits, after: comparison.after.hits })
    const [stable, aggressive] = scores.profiles
    const kind = { NEW_MATCHES: '新增', HISTORICAL_TAIL: '历史尾段', INSUFFICIENT_DATA: '样本不足' }[scores.plan.validationKind]
    lines.push(`| ${names[result.competition]} | ${range === 'CURRENT' ? '仅本届' : '含上届'} | ${scores.plan.validationFrom ?? '—'} | ${kind} | ${cell(stable.periods.full)} | ${cell(aggressive.periods.full)} | ${cell(stable.periods.validation)} | ${cell(aggressive.periods.validation)} |`)
  }
  return lines
}

function markdown(report) {
  const scoreOptimization = options.objective === 'score-top3'
  const method = scoreOptimization ? [
    `- 优先使用 ${options.recentFrom} 起的新赛程做验证。样本不足时按完整比赛日留出历史尾段`,
    `- 比分候选用训练段实际比分、泊松概率和 3 个连续时间块筛选。每范围冻结 1 个候选，再用页面相同种子和排序做 ${options.simulations.toLocaleString('en-US')} 次模拟`,
    '- 每档分别比较原模型。训练段须提升，验证段须净增至少 2 场和 2 个百分点，至少 2 个比赛日净改善，按日成对重抽样的单侧 90% 下界不低于 0，平均对数损失最多增加 0.03',
    '- 比分模型通过后，胜平负只搜索投注阈值和让球平滑系数。进球数共用稳健模型。各玩法用训练段排序，先冻结候选，再按时间验证门槛准入',
    '- 更新比分模型的方案沿用比分验证分界。沿用原比分模型且该分界缺少赔率样本时，按该玩法的赔率样本留出历史尾段',
    '- 比分与投注门槛可能使用同一验证期。验证期指标含准入选择，全量指标还包含训练数据',
    '- 无法验证投注方案时关闭相应推荐。进球数只在本届无赔率样本时允许沿用含上届规则',
    '', ...scoreMarkdown(report)
  ] : [
    '- 按完整比赛日划分约70%训练、30%验证，至少10场训练、6场验证；同日不跨分区，样本不足不做独立优化',
    '- 候选使用固定种子生成，粗搜和精搜只读取训练段；3个连续训练块参与收益收缩、收益波动和下行风险惩罚；优先选择参数轻微扰动后仍稳定的区域',
    '- 先冻结训练排序和候选文件，再读取验证段；胜平负每档最多24个，进球数全部层级合计最多12个，验证只通过或拒绝，不按验证ROI重排、细化或追加搜索',
    '- 滚动诊断逐折只用更早训练块选模型和规则，再在下一块结算；进球数逐折重新选择较早的稳健模型；滚动结果不冒充最终固定参数的收益',
    '- 保留既有进球数本届不超过10场时沿用含上届的规则，同时按本届实际样本检查采样率、单注命中率和ROI；无本届样本明确标记，不能当作本届已验证盈利',
    '- 无可行参数时关闭对应推荐，不降低原有门槛；新旧方案及关闭结果均用页面算法独立复核',
    '- 验证段已被历史优化反复使用，本轮仍作为门禁，不能称为独立测试；全量ROI含训练样本，近期区间也可能已参与训练或门禁',
    '- 日期bootstrap为描述性95%区间，未校正多重尝试；正的回测ROI不能证明未来盈利'
  ]
  const lines = [`# 全赛事${scoreOptimization ? '前 3 比分优先' : '有约束稳健'}优化 ${date}`, '',
    `- 数据快照 \`${report.manifest.dataFingerprint}\`，算法与设置 \`${report.manifest.runFingerprint}\``,
    `- 覆盖 ${competitions.length} 类赛事，${competitions.length * 4} 套胜平负参数和 ${competitions.length * 2} 套稳健/激进共用进球数策略`,
    `- 赛事工作线程 ${Math.min(options.workers, competitions.length)}，${scoreOptimization ? '' : `粗搜每场 ${options.coarseSimulations} 次模拟，`}结算每场 ${options.simulations} 次模拟`,
    '- 保留原有采样率、单注命中率、稳健ROI至少5%、激进ROI高于稳健及激进采样率不高于稳健等门槛；优先原采样率上下3个百分点，无解后允许退至赛事硬下限',
    '- 进球数按33.3%、25%、20%、10%的采样率与单注命中率逐档检查，各档严格大于门槛；训练和全量ROI严格大于0，验证ROI不低于0',
    ...method,
    '', '## 新旧方案同快照对比', '',
    'ROI按总返奖/总投注减一汇总。两种范围有重叠，稳健/激进为替代方案，以下各行不能叠加。关闭方案没有投注，ROI显示“—”。进球数命中率按单注统计。', '',
    '| 玩法 | 范围 | 方案 | 旧全量ROI | 新全量ROI | 旧验证ROI | 新验证ROI | 新验证注数 | 新命中率 | 启用数 |',
    '| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |']
  const label = row => [row.market === 'WDL' ? '胜平负' : '进球数', row.range === 'PREVIOUS' ? '含上届' : '仅本届', ({ STABLE: '稳健', AGGRESSIVE: '激进', SHARED: '共用' })[row.preset]]
  for (const group of report.groups) lines.push(`| ${label(group).join(' | ')} | ${percent(group.before.full.roi)} | ${percent(group.after.full.roi)} | ${percent(group.before.validation.roi)} | ${percent(group.after.validation.roi)} | ${group.after.validation.totalStake} | ${percent(group.market === 'WDL' ? group.after.full.hitRate : group.after.full.selectionHitRate)} | ${group.activeBefore} → ${group.activeAfter} |`)
  lines.push('', '## 逐方案结果', '', '| 赛事 | 玩法 | 范围 | 方案 | 状态 | 样本数 | 旧ROI | 新ROI | 验证ROI | 验证95%区间 | 采样率 | 命中率 |',
    '| --- | --- | --- | --- | --- | ---: | ---: | ---: | ---: | --- | ---: | ---: |')
  for (const result of report.results) for (const range of result.ranges) for (const row of range.rows) {
    const interval = row.validationUncertainty.roi95Interval?.map(percent).join(' ~ ') || '—'
    lines.push(`| ${names[row.competition]} | ${label(row).join(' | ')} | ${row.status} | ${row.after.full.availableMatchCount} | ${percent(row.before.full.roi)} | ${percent(row.after.full.roi)} | ${percent(row.after.validation.roi)} | ${interval} | ${percent(row.after.full.samplingRate)} | ${percent(row.market === 'WDL' ? row.after.full.hitRate : row.after.full.selectionHitRate)} |`)
  }
  lines.push('', '## 审计与复现', '', `- 页面算法独立结算 ${report.audit.checkedRows} 套，门槛冲突 ${report.audit.violations.length} 项`,
    '- 同名 JSON 保存输入 JAR、缓存、配置和算法的 SHA-256、分区指标、参数扰动指标和完整参数。候选冻结文件与结算缓存位于运行目录',
    '- 使用 `scripts/run-constrained-optimization.ps1` 复现，使用 `-Apply` 应用配置，使用 `-Resume` 恢复同一轮运行',
    '- 方法参考 [时间序列交叉验证与测试集隔离](https://scikit-learn.org/stable/modules/cross_validation.html#cross-validation-of-time-series-data)、[Node.js 20.17 Worker Threads](https://nodejs.org/download/release/v20.17.0/docs/api/worker_threads.html)', '')
  return lines.join('\n')
}

async function main() {
  const directory = options.directory
  await fs.mkdir(path.join(directory, 'cache'), { recursive: true })
  const config = JSON.parse(await fs.readFile(path.join(directory, 'config/user-config.json'), 'utf8'))
  for (const { code } of competitions) for (const range of ['PREVIOUS', 'CURRENT']) {
    for (const preset of ['STABLE', 'AGGRESSIVE']) if (!config.parameterProfiles?.[`${code}:${range}:${preset}`]) throw new Error(`缺少配置 ${code}:${range}:${preset}`)
  }
  const inputs = await Promise.all(['snapshot.jar', 'config/user-config.json', 'config/club-competition-schedules.json', 'config/sporttery-market-selections.json'].map(async file => ({ file, sha256: hash(await fs.readFile(path.join(directory, file))) })))
  const algorithms = await Promise.all(['optimize-constrained-strategies.mjs', 'constrained-optimization-worker.mjs', 'constrained-optimization-core.mjs',
    'score-top3-optimization.mjs', 'strategy-optimization-core.mjs', 'chronological-validation.mjs', 'wdl-fast-evaluator.mjs', '../frontend/src/recommendation-backtest.mjs',
    '../frontend/src/backtest-roi.mjs', '../frontend/src/backtest-competitions.mjs'].map(async file => ({ file, sha256: hash(await fs.readFile(new URL(file, import.meta.url))) })))
  const dataFingerprint = hash(JSON.stringify({ inputs, asOfDate: options.asOfDate }))
  const scorePolicy = options.objective === 'score-top3' ? SCORE_POLICY : undefined
  const runFingerprint = hash(JSON.stringify({ dataFingerprint, algorithms, options, competitions, policy: POLICY, scorePolicy, risk: RISK_SETTINGS }))
  const manifest = { dataFingerprint, runFingerprint, inputs, algorithms, options, policy: POLICY, riskSettings: RISK_SETTINGS,
    scorePolicy, createdAt: new Date().toISOString(), hardware: { logicalProcessors: os.availableParallelism(), totalMemoryGiB: os.totalmem() / 2 ** 30 },
    actualWorkers: Math.min(options.workers, competitions.length) }
  const manifestFile = path.join(directory, 'manifest.json')
  if (process.argv.includes('--resume')) {
    const old = JSON.parse(await fs.readFile(manifestFile, 'utf8'))
    if (old.runFingerprint !== runFingerprint) throw new Error('数据、代码或设置发生变化，禁止复用旧结果；可移除 --resume 重新执行，匹配的数据缓存仍可复用')
  }
  await save(manifestFile, manifest)
  const completed = []
  const pending = []
  for (const task of competitions) {
    if (process.argv.includes('--resume')) {
      try {
        const result = JSON.parse(await fs.readFile(path.join(directory, `${task.code}-result.json`), 'utf8'))
        if (result.runFingerprint !== runFingerprint || result.competition !== task.code || result.independentVerification !== 'PASSED' ||
            result.ranges?.length !== 2 || result.ranges.some(range => range.rows?.length !== 3)) throw new Error(`${task.code} 检查点不完整或不属于本轮`)
        completed.push(result)
        continue
      } catch (error) { if (error.code !== 'ENOENT') throw error }
    }
    pending.push(task)
  }
  const results = [...completed, ...await runWorkers(pending, { ...options, config, dataFingerprint, runFingerprint })]
    .sort((a, b) => competitions.findIndex(item => item.code === a.competition) - competitions.findIndex(item => item.code === b.competition))
  const report = createReport(results, manifest)
  if (report.audit.violations.length) throw new Error(`审计失败：${report.audit.violations.join('；')}`)
  if (report.audit.checkedRows !== competitions.length * 6) throw new Error('回测结果数量不完整')
  if (scorePolicy && report.audit.checkedScoreProfiles !== competitions.length * 4) throw new Error('比分核验结果数量不完整')
  const updated = structuredClone(config)
  for (const result of results) for (const range of result.ranges) for (const row of range.rows) {
    if (row.market === 'WDL') updated.parameterProfiles[`${row.competition}:${row.range}:${row.preset}`] = row.profile
    else updated.totalGoalsStrategies[`${row.competition}:${row.range}`] = row.strategy
  }
  await save(path.join(directory, 'optimized-user-config.json'), updated)
  const reportName = scorePolicy ? 'score-top3-optimization' : 'constrained-robust-optimization'
  const reportPath = path.resolve(ROOT, argument('--report-path', `reports/${reportName}-${date}.json`))
  await fs.mkdir(path.dirname(reportPath), { recursive: true })
  await save(reportPath, report)
  await fs.writeFile(reportPath.replace(/\.json$/, '.md'), markdown(report), 'utf8')
  if (process.argv.includes('--apply')) {
    const configPath = path.resolve(ROOT, argument('--config-path', 'config/user-config.json'))
    const liveText = await fs.readFile(configPath, 'utf8')
    const live = JSON.parse(liveText)
    if (JSON.stringify(live.parameterProfiles) !== JSON.stringify(config.parameterProfiles) ||
        JSON.stringify(live.totalGoalsStrategies) !== JSON.stringify(config.totalGoalsStrategies)) throw new Error('正式参数已被其他操作修改，结果已保存但未覆盖')
    const backup = scorePolicy ? path.join(directory, `user-config-before-top3-${Date.now()}.json`)
      : path.join(ROOT, 'temp', `user-config-before-robust-${Date.now()}.json`)
    await fs.mkdir(path.dirname(backup), { recursive: true })
    await fs.writeFile(backup, liveText, { encoding: 'utf8', flag: 'wx' })
    live.parameterProfiles = updated.parameterProfiles
    live.totalGoalsStrategies = updated.totalGoalsStrategies
    const temporary = `${configPath}.optimization-${process.pid}.tmp`
    if (scorePolicy) {
      const formatted = JSON.stringify(live, null, 2).replace(/^(\s*"[^"]+"):/gm, '$1 :')
      await fs.writeFile(temporary, formatted + (liveText.endsWith('\n') ? '\n' : ''), 'utf8')
    } else await save(temporary, live)
    await fs.rename(temporary, configPath)
    report.applied = { at: new Date().toISOString(), configPath, backup, sha256: hash(await fs.readFile(configPath)) }
    await save(reportPath, report)
    process.stdout.write(`已应用全部参数，备份 ${backup}\n`)
  }
  process.stdout.write(JSON.stringify({ reportPath, audit: report.audit, activeWdl: report.activeWdl, activeGoals: report.activeGoals, scoreGroups: report.scoreGroups,
    groups: report.groups.map(group => ({ range: group.range, market: group.market, preset: group.preset,
      oldRoi: group.before.full.roi, newRoi: group.after.full.roi, validationRoi: group.after.validation.roi,
      oldStake: group.before.full.totalStake, newStake: group.after.full.totalStake })) }, null, 2) + '\n')
}

main().catch(error => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1 })
