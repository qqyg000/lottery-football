import fs from 'node:fs/promises'
import path from 'node:path'
import os from 'node:os'
import { createHash } from 'node:crypto'
import { Worker } from 'node:worker_threads'
import { fileURLToPath } from 'node:url'
import { BACKTEST_COMPETITIONS } from '../frontend/src/backtest-competitions.mjs'
import { combineMetrics, RISK_SETTINGS } from './strategy-optimization-core.mjs'
import { POLICY, wdlPairPass, goalsCandidatePass, goalsTrainingPass } from './constrained-optimization-core.mjs'

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
  recentFrom: argument('--recent-from', '2026-09-29')
}
if (!/^\d{4}-\d{2}-\d{2}$/.test(options.recentFrom)) throw new Error('--recent-from 必须为 YYYY-MM-DD')
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
  for (const result of results) for (const range of result.ranges) {
    const { competition } = result
    if (range.independentVerification !== 'PASSED') violations.push(`${competition}:${range.range} 未完成独立结算`)
    if (range.holdoutCandidates.stable > POLICY.wdlHoldoutLimit || range.holdoutCandidates.aggressive > POLICY.wdlHoldoutLimit || range.holdoutCandidates.goals > POLICY.goalsHoldoutLimit) violations.push(`${competition}:${range.range} 超过留出候选上限`)
    const [stable, aggressive, goals] = range.rows
    checkedRows += range.rows.length
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
  return { checkedRows, violations, independentSettlementViolations: 0 }
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
  return { generatedAt: new Date().toISOString(), manifest, groups, results, audit,
    activeWdl: rows.filter(row => row.market === 'WDL' && row.after.full.totalStake > 0).length,
    activeGoals: rows.filter(row => row.market === 'GOALS' && row.after.full.totalStake > 0).length,
    noDataRows: rows.filter(row => row.status === 'NO_DATA').length,
    insufficientDataRows: rows.filter(row => row.status === 'INSUFFICIENT_DATA').length }
}

function markdown(report) {
  const lines = [`# 全赛事有约束稳健优化 ${date}`, '',
    `- 数据快照：\`${report.manifest.dataFingerprint}\`；算法与设置：\`${report.manifest.runFingerprint}\``,
    `- 覆盖 ${competitions.length} 类赛事，${competitions.length * 4} 套胜平负参数和 ${competitions.length * 2} 套稳健/激进共用进球数策略`,
    `- 实际赛事工作线程 ${Math.min(options.workers, competitions.length)}，粗搜每场 ${options.coarseSimulations} 次模拟，精搜和结算每场 ${options.simulations} 次模拟`,
    '- 保留原有采样率、单注命中率、稳健ROI至少5%、激进ROI高于稳健及激进采样率不高于稳健等门槛；优先原采样率上下3个百分点，无解后允许退至赛事硬下限',
    '- 进球数按33.3%、25%、20%、10%的采样率与单注命中率逐档检查，各档严格大于门槛；训练和全量ROI严格大于0，验证ROI不低于0',
    '- 按完整比赛日划分约70%训练、30%验证，至少10场训练、6场验证；同日不跨分区，样本不足不做独立优化',
    '- 候选使用固定种子生成，粗搜和精搜只读取训练段；3个连续训练块参与收益收缩、收益波动和下行风险惩罚；优先选择参数轻微扰动后仍稳定的区域',
    '- 先冻结训练排序和候选文件，再读取验证段；胜平负每档最多24个，进球数全部层级合计最多12个，验证只通过或拒绝，不按验证ROI重排、细化或追加搜索',
    '- 滚动诊断逐折只用更早训练块选模型和规则，再在下一块结算；进球数逐折重新选择较早的稳健模型；滚动结果不冒充最终固定参数的收益',
    '- 保留既有进球数本届不超过10场时沿用含上届的规则，同时按本届实际样本检查采样率、单注命中率和ROI；无本届样本明确标记，不能当作本届已验证盈利',
    '- 无可行参数时关闭对应推荐，不降低原有门槛；新旧方案及关闭结果均用页面算法独立复核',
    '- 验证段已被历史优化反复使用，本轮仍作为门禁，不能称为独立测试；全量ROI含训练样本，近期区间也可能已参与训练或门禁',
    '- 日期bootstrap为描述性95%区间，未校正多重尝试；正的回测ROI不能证明未来盈利',
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
    '- 输入JAR、缓存、配置和算法SHA-256、所有分区指标、逐折结果、扰动指标及完整参数见同名JSON，候选冻结文件与带校验缓存位于运行目录',
    '- 复现入口：`scripts/run-constrained-optimization.ps1`；仅生成报告不应用时不传 `-Apply`；中断后使用 `-Resume`',
    '- 方法参考：[时间序列交叉验证与测试集隔离](https://scikit-learn.org/stable/modules/cross_validation.html#cross-validation-of-time-series-data)、[Node.js 20.17 Worker Threads](https://nodejs.org/download/release/v20.17.0/docs/api/worker_threads.html)', '')
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
    'strategy-optimization-core.mjs', 'chronological-validation.mjs', 'wdl-fast-evaluator.mjs', '../frontend/src/recommendation-backtest.mjs',
    '../frontend/src/backtest-roi.mjs', '../frontend/src/backtest-competitions.mjs'].map(async file => ({ file, sha256: hash(await fs.readFile(new URL(file, import.meta.url))) })))
  const dataFingerprint = hash(JSON.stringify({ inputs, asOfDate: options.asOfDate }))
  const runFingerprint = hash(JSON.stringify({ dataFingerprint, algorithms, options, competitions, policy: POLICY, risk: RISK_SETTINGS }))
  const manifest = { dataFingerprint, runFingerprint, inputs, algorithms, options, policy: POLICY, riskSettings: RISK_SETTINGS,
    createdAt: new Date().toISOString(), hardware: { logicalProcessors: os.availableParallelism(), totalMemoryGiB: os.totalmem() / 2 ** 30 },
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
  const updated = structuredClone(config)
  for (const result of results) for (const range of result.ranges) for (const row of range.rows) {
    if (row.market === 'WDL') updated.parameterProfiles[`${row.competition}:${row.range}:${row.preset}`] = row.profile
    else updated.totalGoalsStrategies[`${row.competition}:${row.range}`] = row.strategy
  }
  await save(path.join(directory, 'optimized-user-config.json'), updated)
  const reportPath = path.resolve(ROOT, argument('--report-path', `reports/constrained-robust-optimization-${date}.json`))
  await fs.mkdir(path.dirname(reportPath), { recursive: true })
  await save(reportPath, report)
  await fs.writeFile(reportPath.replace(/\.json$/, '.md'), markdown(report), 'utf8')
  if (process.argv.includes('--apply')) {
    const configPath = path.resolve(ROOT, argument('--config-path', 'config/user-config.json'))
    const liveText = await fs.readFile(configPath, 'utf8')
    const live = JSON.parse(liveText)
    if (JSON.stringify(live.parameterProfiles) !== JSON.stringify(config.parameterProfiles) ||
        JSON.stringify(live.totalGoalsStrategies) !== JSON.stringify(config.totalGoalsStrategies)) throw new Error('正式参数已被其他操作修改，结果已保存但未覆盖')
    const backup = path.join(ROOT, 'temp', `user-config-before-robust-${Date.now()}.json`)
    await fs.mkdir(path.dirname(backup), { recursive: true })
    await fs.writeFile(backup, liveText, { encoding: 'utf8', flag: 'wx' })
    live.parameterProfiles = updated.parameterProfiles
    live.totalGoalsStrategies = updated.totalGoalsStrategies
    const temporary = `${configPath}.optimization-${process.pid}.tmp`
    await save(temporary, live)
    await fs.rename(temporary, configPath)
    report.applied = { at: new Date().toISOString(), configPath, backup, sha256: hash(await fs.readFile(configPath)) }
    await save(reportPath, report)
    process.stdout.write(`已应用全部参数，备份 ${backup}\n`)
  }
  process.stdout.write(JSON.stringify({ reportPath, audit: report.audit, activeWdl: report.activeWdl, activeGoals: report.activeGoals,
    groups: report.groups.map(group => ({ range: group.range, market: group.market, preset: group.preset,
      oldRoi: group.before.full.roi, newRoi: group.after.full.roi, validationRoi: group.after.validation.roi,
      oldStake: group.before.full.totalStake, newStake: group.after.full.totalStake })) }, null, 2) + '\n')
}

main().catch(error => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1 })
