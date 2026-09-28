import fs from 'node:fs/promises'
import path from 'node:path'
import os from 'node:os'
import { createHash } from 'node:crypto'
import { Worker } from 'node:worker_threads'
import { fileURLToPath } from 'node:url'
import { combineMetrics, RISK_SETTINGS } from './strategy-optimization-core.mjs'
import { BACKTEST_COMPETITIONS, filterBacktestProfiles, isBacktestCompetition } from '../frontend/src/backtest-competitions.mjs'

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
function argument(name, fallback) {
  const index = process.argv.indexOf(name)
  return index < 0 ? fallback : process.argv[index + 1]
}
function positiveInteger(name, fallback) {
  const value = Number(argument(name, fallback))
  if (!Number.isSafeInteger(value) || value < 1) throw new Error(`${name} 必须是正整数`)
  return value
}
const date = new Intl.DateTimeFormat('en-CA', { timeZone: 'Asia/Shanghai', year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date())
const directory = path.resolve(ROOT, argument('--directory', `target/optimization-unconstrained-${date.replaceAll('-', '')}`))
const configPath = path.resolve(ROOT, argument('--config-path', 'config/user-config.json'))
const options = {
  directory, asOfDate: date, apiBase: argument('--base-url', 'http://127.0.0.1:18080'),
  simulations: positiveInteger('--simulations', 50000), modelCount: positiveInteger('--models', 16),
  ruleCount: positiveInteger('--rules', 16000), goalCount: positiveInteger('--goal-rules', 40000),
  workers: Math.min(os.availableParallelism(), positiveInteger('--workers', Math.max(1, os.availableParallelism() - 4))),
  freshFrom: argument('--fresh-from', '') || null
}
if (options.simulations < 1000 || options.simulations > 500000) throw new Error('模拟次数必须在接口支持的1000至500000之间')
if (options.modelCount < 3) throw new Error('至少需要原稳健、原激进和默认三个模型')
if (options.freshFrom && !/^\d{4}-\d{2}-\d{2}$/.test(options.freshFrom)) throw new Error('--fresh-from 格式必须为 YYYY-MM-DD')
const names = {
  ...Object.fromEntries(BACKTEST_COMPETITIONS.map(({ code, name }) => [code, name])),
  WORLD_CUP: '世界杯（含欧国联）'
}
const sha256 = value => createHash('sha256').update(value).digest('hex')
const percent = value => Number.isFinite(value) ? `${(value * 100).toFixed(2)}%` : '—'
const save = (file, value) => fs.writeFile(file, JSON.stringify(value, null, 2) + '\n', 'utf8')

async function runWorkers(tasks, shared) {
  const results = []
  const active = new Set()
  const progress = new Map()
  let next = 0
  let failed = false
  const timer = setInterval(() => {
    const models = [...progress.values()].reduce((sum, value) => sum + value.model, 0)
    process.stdout.write(`进度 ${results.length}/${tasks.length} 范围完成，${models} 个模型完成，${active.size} 个工作线程运行\n`)
  }, 20000)
  try {
    await Promise.all(Array.from({ length: Math.min(shared.workers, tasks.length) }, async () => {
      while (next < tasks.length && !failed) {
        const task = tasks[next++]
        const result = await new Promise((resolve, reject) => {
          const worker = new Worker(new URL('./unconstrained-optimization-worker.mjs', import.meta.url), { workerData: { ...shared, ...task } })
          active.add(worker)
          let received = false
          worker.on('message', message => {
            if (message.type === 'progress') progress.set(message.key, message)
            if (message.type === 'result') { received = true; resolve(message.result) }
            if (message.type === 'error') reject(new Error(message.message))
          })
          worker.on('error', reject)
          worker.on('exit', code => {
            active.delete(worker)
            if (!received) reject(new Error(`${task.competition}:${task.range} 工作线程异常退出 ${code}`))
          })
        })
        result.runFingerprint = shared.fingerprint
        await save(path.join(shared.directory, `${task.competition}-${task.range}-result.json`), result)
        results.push(result)
        process.stdout.write(`完成 ${task.competition}:${task.range}，${result.rows[0].after.full.availableMatchCount} 场，页面算法复验通过\n`)
      }
    }))
  } catch (error) {
    failed = true
    await Promise.allSettled([...active].map(worker => worker.terminate()))
    throw error
  } finally { clearInterval(timer) }
  return results
}

function createReport(results, manifest) {
  const rows = results.flatMap(result => result.rows)
  const groups = []
  for (const range of ['PREVIOUS', 'CURRENT']) {
    for (const [market, preset] of [['WDL', 'STABLE'], ['WDL', 'AGGRESSIVE'], ['GOALS', 'SHARED']]) {
      const selected = rows.filter(row => row.range === range && row.market === market && row.preset === preset)
      groups.push({ range, market, preset,
        before: Object.fromEntries(['full', 'development', 'test', 'fresh'].map(period => [period, combineMetrics(selected.map(row => row.before[period]))])),
        after: Object.fromEntries(['full', 'development', 'test', 'fresh'].map(period => [period, combineMetrics(selected.map(row => row.after[period]))])),
        walkForward: combineMetrics(selected.map(row => row.walkForward)) })
    }
  }
  return { generatedAt: new Date().toISOString(), manifest, groups, results,
    audit: { rows: rows.length, independentSettlementViolations: 0,
      noDataRows: rows.filter(row => row.status === 'NO_DATA').length,
      priorOnlyRows: rows.filter(row => row.status === 'PRIOR_ONLY').length,
      negativeTestRoiRows: rows.filter(row => row.after.test.roi !== null && row.after.test.roi < 0).length,
      smallTestSampleRows: rows.filter(row => row.after.test.recommendedMatchCount < 10).length } }
}

function markdown(report) {
  const rows = report.results.flatMap(result => result.rows)
  const presetName = preset => ({ STABLE: '稳健', AGGRESSIVE: '激进', SHARED: '共用' })[preset]
  const rangeName = range => range === 'PREVIOUS' ? '含上届' : '仅本届'
  const marketName = market => market === 'WDL' ? '胜平负' : '进球数'
  const lines = [`# 全赛事无硬性门槛优化 ${date}`, '',
    `- 固定快照指纹：\`${report.manifest.fingerprint}\``,
    `- ${new Set(report.results.map(result => result.competition)).size} 类赛事，${rows.filter(row => row.market === 'WDL').length} 套胜平负和${rows.filter(row => row.market === 'GOALS').length}套进球数策略；进球数继续在稳健/激进模式共用对应稳健模型`,
    `- ${options.workers} 个 Node 工作线程，回测服务并行度由启动参数设置；所有候选每场 ${options.simulations.toLocaleString()} 次模拟`,
    '- 删除采样率窗口、命中率下限、训练/验证/全量最低ROI、稳健/激进ROI差及关系、分档降级、负收益自动关闭等优化门槛',
    '- 仅保留与生产接口一致的参数合法范围、有效比分/赔率和时间因果要求；投注阈值仍作为可优化参数',
    '- 按完整比赛日留出末尾约20%日期；若提供新增样本日期，则将其及之后的比赛全部排除出开发集',
    '- 开发集分为最多4个连续日期块，固定先验向-5% ROI收缩，并扣除不确定性、跨块波动与下行风险；稳健惩罚高于激进',
    '- 逐折只按更早的块选模型和规则，并在下一个块结算；进球数逐折重新选择其共用的稳健模型',
    '- 最终参数在读取测试结果前冻结，测试结果不参与排序、门禁、回退或是否应用的决定',
    '- 无最低ROI门槛意味着负收益方案也会保留；无足够开发数据时标记为先验方案，不把缺失样本算作0% ROI',
    '- 历史模型和原参数来自以往优化，历史滚动检验与留出段并非完全未接触的数据；本次新增区间仅表示未参与本轮和上次选参，不代表实时前瞻实盘结果',
    '- 区间为按比赛日重采样2000次的描述性95% bootstrap区间，未作多重比较校正，不构成盈利保证',
    `- 新增区间：${options.freshFrom || '未指定'}；独立页面结算核对 ${report.audit.rows} 套，差异 ${report.audit.independentSettlementViolations} 项`,
    '', '## 汇总对比', '',
    'ROI按总返奖/总投入减一计算。含上届/仅本届有重叠，稳健/激进是替代方案，不能相加。全量含开发样本，不能代替测试结果。', '',
    '| 玩法 | 范围 | 方案 | 原全量ROI | 新全量ROI | 原测试ROI | 新测试ROI | 原新增ROI | 新新增ROI | 新测试注数 |',
    '| --- | --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |']
  for (const group of report.groups) lines.push(`| ${marketName(group.market)} | ${rangeName(group.range)} | ${presetName(group.preset)} | ${percent(group.before.full.roi)} | ${percent(group.after.full.roi)} | ${percent(group.before.test.roi)} | ${percent(group.after.test.roi)} | ${percent(group.before.fresh.roi)} | ${percent(group.after.fresh.roi)} | ${group.after.test.totalStake} |`)
  lines.push('', '## 逐方案结果', '',
    '未指定“新增”日期或没有可投注样本时显示“—”。测试ROI为负的方案如实保留，不根据测试赛果继续改参。', '',
    '| 赛事 | 范围 | 玩法 | 方案 | 开发场数 | 测试场数 | 原全量ROI | 新全量ROI | 测试ROI | 测试95%区间 | 新增ROI | 测试投注场数 |',
    '| --- | --- | --- | --- | ---: | ---: | ---: | ---: | ---: | --- | ---: | ---: |')
  for (const result of report.results) for (const row of result.rows) {
    const interval = row.testUncertainty.roi95Interval?.map(percent).join(' ~ ') || '—'
    lines.push(`| ${names[row.competition] || row.competition} | ${rangeName(row.range)} | ${marketName(row.market)} | ${presetName(row.preset)} | ${row.after.development.availableMatchCount} | ${row.after.test.availableMatchCount} | ${percent(row.before.full.roi)} | ${percent(row.after.full.roi)} | ${percent(row.after.test.roi)} | ${interval} | ${percent(row.after.fresh.roi)} | ${row.after.test.recommendedMatchCount} |`)
  }
  lines.push('', '## 复现与审计', '',
    '- 完整参数、每段日期、候选数量、原/新收益、逐折统计及快照文件SHA-256见同名JSON',
    '- 每个范围的选择锁定文件及数据缓存保存在运行目录；复用缓存必须匹配数据和算法指纹',
    '- 最终所有方案、原方案和各时间段都用页面算法独立核算；不会以快速算法自证',
    '- 方法参考：[scikit-learn 时间序列交叉验证与测试集隔离](https://scikit-learn.org/stable/modules/cross_validation.html#cross-validation-of-time-series-data)、[Node.js Worker Threads](https://nodejs.org/api/worker_threads.html)', '')
  return lines.join('\n')
}

async function main() {
  await fs.mkdir(path.join(directory, 'cache'), { recursive: true })
  const snapshotConfig = path.join(directory, 'config/user-config.json')
  const config = JSON.parse(await fs.readFile(snapshotConfig, 'utf8'))
  const inputPaths = ['snapshot.jar', 'config/user-config.json', 'config/club-competition-schedules.json', 'config/sporttery-market-selections.json']
  const inputs = await Promise.all(inputPaths.map(async file => ({ file, sha256: sha256(await fs.readFile(path.join(directory, file))) })))
  const algorithms = await Promise.all(['optimize-unconstrained-strategies.mjs', 'strategy-optimization-core.mjs', 'unconstrained-optimization-worker.mjs', 'wdl-fast-evaluator.mjs',
    '../frontend/src/recommendation-backtest.mjs'].map(async file => ({ file, sha256: sha256(await fs.readFile(new URL(file, import.meta.url))) })))
  const fingerprint = sha256(JSON.stringify({ inputs, algorithms, options, risk: RISK_SETTINGS }))
  const manifest = { fingerprint, inputs, algorithms, options, riskSettings: RISK_SETTINGS,
    createdAt: new Date().toISOString(), hardware: { logicalProcessors: os.availableParallelism(), memoryGiB: os.totalmem() / 2 ** 30 } }
  const manifestPath = path.join(directory, 'manifest.json')
  if (process.argv.includes('--resume')) {
    const oldManifest = JSON.parse(await fs.readFile(manifestPath, 'utf8'))
    if (oldManifest.fingerprint !== fingerprint) throw new Error('快照或运行设置已变，禁止复用旧检查点')
  }
  await save(manifestPath, manifest)
  const competitions = [...new Set(Object.keys(config.parameterProfiles).map(key => key.split(':')[0]))]
    .filter(isBacktestCompetition)
  const tasks = competitions.flatMap(competition => ['PREVIOUS', 'CURRENT'].map(range => ({ competition, range })))
  const completed = []
  const pending = []
  for (const task of tasks) {
    if (process.argv.includes('--resume')) {
      try {
        const result = JSON.parse(await fs.readFile(path.join(directory, `${task.competition}-${task.range}-result.json`), 'utf8'))
        if (result.runFingerprint !== fingerprint || result.competition !== task.competition || result.range !== task.range ||
            result.independentVerification !== 'PASSED' || result.rows?.length !== 3) {
          throw new Error(`${task.competition}:${task.range} 检查点不属于本次快照或未完成复验`)
        }
        completed.push(result)
        continue
      }
      catch (error) { if (error.code !== 'ENOENT') throw error }
    }
    pending.push(task)
  }
  const results = [...completed, ...await runWorkers(pending, { ...options, config, fingerprint })]
    .sort((a, b) => competitions.indexOf(a.competition) - competitions.indexOf(b.competition) || b.range.localeCompare(a.range))
  const updated = structuredClone(config)
  updated.parameterProfiles = filterBacktestProfiles(updated.parameterProfiles)
  updated.totalGoalsStrategies = filterBacktestProfiles(updated.totalGoalsStrategies)
  for (const result of results) for (const row of result.rows) {
    if (row.market === 'WDL') updated.parameterProfiles[`${row.competition}:${row.range}:${row.preset}`] = row.profile
    else updated.totalGoalsStrategies[`${row.competition}:${row.range}`] = row.strategy
  }
  await save(path.join(directory, 'optimized-user-config.json'), updated)
  const report = createReport(results, manifest)
  const reportPath = path.resolve(ROOT, argument('--report-path', `reports/unconstrained-optimization-${date}.json`))
  await fs.mkdir(path.dirname(reportPath), { recursive: true })
  await save(reportPath, report)
  await fs.writeFile(reportPath.replace(/\.json$/, '.md'), markdown(report), 'utf8')
  if (process.argv.includes('--apply')) {
    const liveText = await fs.readFile(configPath, 'utf8')
    const live = JSON.parse(liveText)
    if (JSON.stringify(live.parameterProfiles) !== JSON.stringify(config.parameterProfiles) ||
        JSON.stringify(live.totalGoalsStrategies) !== JSON.stringify(config.totalGoalsStrategies)) throw new Error('正式策略已被其他操作修改，已保留结果但未覆盖')
    const backup = path.join(ROOT, 'temp', `user-config-before-unconstrained-${Date.now()}.json`)
    await fs.mkdir(path.dirname(backup), { recursive: true })
    await fs.writeFile(backup, liveText, { encoding: 'utf8', flag: 'wx' })
    live.parameterProfiles = updated.parameterProfiles
    live.totalGoalsStrategies = updated.totalGoalsStrategies
    const temporary = `${configPath}.optimization-${process.pid}.tmp`
    await save(temporary, live)
    await fs.rename(temporary, configPath)
    report.applied = { at: new Date().toISOString(), backup, configPath, sha256: sha256(await fs.readFile(configPath)) }
    await save(reportPath, report)
    process.stdout.write(`已应用 ${configPath}，备份 ${backup}\n`)
  }
  process.stdout.write(JSON.stringify({ reportPath, audit: report.audit, groups: report.groups.map(group => ({
    range: group.range, market: group.market, preset: group.preset, before: group.before.full.roi,
    after: group.after.full.roi, test: group.after.test.roi, fresh: group.after.fresh.roi
  })) }, null, 2) + '\n')
}

main().catch(error => { process.stderr.write(`${error.stack}\n`); process.exitCode = 1 })
