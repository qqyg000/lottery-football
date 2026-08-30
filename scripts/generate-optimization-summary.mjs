import fs from 'node:fs/promises'
import path from 'node:path'

const ROOT = path.resolve(import.meta.dirname, '..')
const WDL_REPORT_PATH = resolveArgument(
  '--wdl-report',
  'reports/win-draw-loss-robust-optimization-2026-08-22.json'
)
const GOALS_OPTIMIZATION_REPORT_PATH = resolveArgument(
  '--goals-optimization-report',
  'reports/total-goals-robust-optimization-2026-08-22.json'
)
const GOALS_VERIFICATION_REPORT_PATH = resolveArgument(
  '--goals-verification-report',
  'reports/total-goals-final-verification-2026-08-22.json'
)
const OUTPUT_PATH = resolveArgument(
  '--output',
  'reports/all-competition-strategy-summary-2026-08-22.md'
)
const COMPETITION_ORDER = [
  'WORLD_CUP',
  'EUROPEAN_CHAMPIONSHIP',
  'COPA_AMERICA',
  'CLUB_WORLD_CUP',
  'EUROPA_LEAGUE',
  'CHAMPIONS_LEAGUE',
  'PREMIER_LEAGUE',
  'LA_LIGA',
  'BUNDESLIGA',
  'SERIE_A',
  'LIGUE_1',
  'PRIMEIRA_LIGA',
  'EREDIVISIE',
  'ARGENTINE_PRIMERA_DIVISION',
  'SWEDISH_ALLSVENSKAN',
  'FINNISH_VEIKKAUSLIIGA',
  'K_LEAGUE_1',
  'SCOTTISH_FA_CUP'
]
const RANGE_ORDER = ['PREVIOUS', 'CURRENT']
const PRESET_ORDER = ['STABLE', 'AGGRESSIVE']
const JSON_EOL = process.platform === 'win32' ? '\r\n' : '\n'

const [wdlReport, goalsOptimizationReport, goalsVerificationReport] = await Promise.all([
  readJson(WDL_REPORT_PATH),
  readJson(GOALS_OPTIMIZATION_REPORT_PATH),
  readJson(GOALS_VERIFICATION_REPORT_PATH)
])

const wdlResultByRange = new Map((wdlReport.optimizationResults || []).map(result => [
  `${result.competition}:${result.range}`,
  result
]))
const goalsOptimizationByKey = new Map((goalsOptimizationReport.strategies || []).map(row => [
  row.key,
  row
]))
const competitionIndex = new Map(COMPETITION_ORDER.map((competition, index) => [competition, index]))
const rangeIndex = new Map(RANGE_ORDER.map((range, index) => [range, index]))
const presetIndex = new Map(PRESET_ORDER.map((preset, index) => [preset, index]))
const wdlRows = [...(wdlReport.verification || [])].sort((left, right) => (
  orderOf(competitionIndex, left.competition) - orderOf(competitionIndex, right.competition) ||
  orderOf(rangeIndex, left.range) - orderOf(rangeIndex, right.range) ||
  orderOf(presetIndex, left.preset) - orderOf(presetIndex, right.preset)
))
const goalsRows = [...(goalsVerificationReport.strategies || [])].sort((left, right) => (
  orderOf(competitionIndex, left.competition) - orderOf(competitionIndex, right.competition) ||
  orderOf(rangeIndex, left.range) - orderOf(rangeIndex, right.range)
))

const wdlActiveRows = wdlRows.filter(row => (row.metrics?.recommendedMatchCount || 0) > 0)
const goalsActiveRows = goalsRows.filter(row => (row.metrics?.recommendedMatchCount || 0) > 0)
const goalsOverfittingRows = goalsRows.filter(row => row.overfittingDetected)
const lines = [
  '# 全赛事推荐方案优化结果汇总',
  '',
  `- 生成时间：${new Date().toISOString()}`,
  `- 胜平负：${wdlRows.length} 套方案，${wdlActiveRows.length} 套有实际推荐，报告约束违规 ${wdlReport.violations?.length || 0} 项`,
  `- 进球数：${goalsRows.length} 套方案，${goalsActiveRows.length} 套有实际推荐，过拟合标记 ${goalsOverfittingRows.length} 项`,
  '- 采样数统一表示“推荐场次/可结算赔率场次”；进球数的稳健/激进界面共用同一策略',
  '- 命中率按命中比赛数除以推荐比赛数计算，ROI 按总返奖除以总投入减一计算',
  '- 仅本届小样本沿用含上届参数时，本表仍展示仅本届直接样本表现，并在状态列标明参数来源',
  '',
  '## 胜平负方案（72 套）',
  '',
  '| 赛事 | 范围 | 方案 | 状态 | 采样数 | 采样率 | 命中率 | ROI |',
  '| --- | --- | --- | --- | ---: | ---: | ---: | ---: |'
]

for (const row of wdlRows) {
  const metrics = row.metrics || {}
  const result = wdlResultByRange.get(`${row.competition}:${row.range}`)
  const hitRate = metrics.recommendedMatchCount > 0
    ? metrics.hitMatchCount / metrics.recommendedMatchCount
    : null
  lines.push([
    row.competitionName,
    rangeName(row.range),
    presetName(row.preset),
    wdlStatus(result?.status),
    sampleText(metrics.recommendedMatchCount, row.oddsMatchCount),
    percent(metrics.samplingRate, row.oddsMatchCount > 0),
    percent(hitRate),
    percent(metrics.roi)
  ].map(tableCell).join(' | ').replace(/^/, '| ').concat(' |'))
}

lines.push(
  '',
  '## 进球数方案（36 套，稳健/激进共用）',
  '',
  '| 赛事 | 范围 | 状态 | 采样数 | 采样率 | 命中率 | ROI |',
  '| --- | --- | --- | --- | ---: | ---: | ---: | ---: |'
)

for (const row of goalsRows) {
  const metrics = row.metrics || {}
  const optimization = goalsOptimizationByKey.get(row.key)
  const samplingRate = metrics.availableMatchCount > 0
    ? metrics.recommendedMatchCount / metrics.availableMatchCount
    : null
  lines.push([
    row.competitionName,
    rangeName(row.range),
    goalsStatus(row, optimization),
    sampleText(metrics.recommendedMatchCount, metrics.availableMatchCount),
    percent(samplingRate, metrics.availableMatchCount > 0),
    percent(metrics.hitRate),
    percent(metrics.roi)
  ].map(tableCell).join(' | ').replace(/^/, '| ').concat(' |'))
}

lines.push(
  '',
  '## 防过拟合与约束审计',
  '',
  '- 胜平负按比赛日期执行 70% 训练、30% 最终留出验证，同一天比赛不跨分区；训练段再按连续时间块计算 ROI 稳定性并固定有限候选池，最终验证集只作门禁',
  '- 胜平负启用方案均满足训练 ROI、验证 ROI 非负；稳健 ROI 不低于 5%，激进 ROI 严格高于对应稳健方案',
  '- 进球数按训练段连续时间块稳定性筛选候选；启用方案均满足对应采样率/命中率分档、训练及全样本 ROI 为正、验证 ROI 非负',
  '- 无数据、验证样本不足或约束无法同时满足的方案均关闭，不以降低门禁换取表面 ROI',
  '',
  '## 原始报告',
  '',
  `- 胜平负完整报告：${path.relative(ROOT, WDL_REPORT_PATH)}`,
  `- 进球数优化报告：${path.relative(ROOT, GOALS_OPTIMIZATION_REPORT_PATH)}`,
  `- 进球数最终复核：${path.relative(ROOT, GOALS_VERIFICATION_REPORT_PATH)}`,
  ''
)

await fs.mkdir(path.dirname(OUTPUT_PATH), { recursive: true })
await fs.writeFile(OUTPUT_PATH, lines.join(JSON_EOL), 'utf8')
process.stdout.write(`汇总报告已写入 ${OUTPUT_PATH}${JSON_EOL}`)

function resolveArgument(name, fallback) {
  const index = process.argv.indexOf(name)
  const value = index >= 0 ? process.argv[index + 1] : fallback
  if (!value) {
    throw new Error(`${name} 缺少路径`)
  }
  return path.resolve(ROOT, value)
}

async function readJson(filePath) {
  return JSON.parse(await fs.readFile(filePath, 'utf8'))
}

function orderOf(index, value) {
  return index.has(value) ? index.get(value) : Number.MAX_SAFE_INTEGER
}

function rangeName(range) {
  return range === 'PREVIOUS' ? '含上届' : '仅本届'
}

function presetName(preset) {
  return preset === 'STABLE' ? '稳健' : '激进'
}

function wdlStatus(status) {
  const labels = {
    OPTIMIZED: '启用',
    CLOSED_CONSTRAINTS_NOT_MET: '关闭（稳健约束未通过）',
    CLOSED_INSUFFICIENT_VALIDATION_SAMPLE: '关闭（验证样本不足或门禁失败）',
    NO_DATA: '无数据'
  }
  return labels[status] || status || '未知'
}

function goalsStatus(row, optimization) {
  const availableMatchCount = row.metrics?.availableMatchCount || 0
  const recommendedMatchCount = row.metrics?.recommendedMatchCount || 0
  if (optimization?.degradationLevel === 'NO_DATA') {
    return '无数据'
  }
  if (optimization?.disabledBecauseConstraintsNotMet) {
    return optimization.fallbackToPreviousEdition
      ? '关闭（含上届或本届门禁未通过）'
      : '关闭（稳健约束未通过）'
  }
  if (optimization?.fallbackToPreviousEdition) {
    return availableMatchCount === 0
      ? '沿用含上届参数（本届无数据）'
      : '沿用含上届参数（本届门禁通过）'
  }
  if (recommendedMatchCount === 0) {
    return availableMatchCount === 0 ? '无数据' : '关闭（无合格推荐）'
  }
  return `启用（${goalsTier(optimization?.degradationLevel)}）`
}

function goalsTier(degradationLevel) {
  if (degradationLevel?.includes('33_3')) {
    return '33.3%档'
  }
  if (degradationLevel?.includes('25')) {
    return '25%档'
  }
  if (degradationLevel?.includes('20')) {
    return '20%档'
  }
  if (degradationLevel?.includes('10')) {
    return '10%档'
  }
  return '稳健门禁'
}

function sampleText(recommended, available) {
  const numerator = Number.isFinite(Number(recommended)) ? Number(recommended) : 0
  const denominator = Number.isFinite(Number(available)) ? Number(available) : 0
  return `${numerator}/${denominator}`
}

function percent(value, zeroWhenNull = false) {
  if (value === null || value === undefined || !Number.isFinite(Number(value))) {
    return zeroWhenNull ? '0.00%' : '--'
  }
  return `${(Number(value) * 100).toFixed(2)}%`
}

function tableCell(value) {
  return String(value ?? '--').replaceAll('|', '\\|').replaceAll('\r', ' ').replaceAll('\n', ' ')
}
