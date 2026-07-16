import fs from 'node:fs/promises'
import path from 'node:path'

const ROOT = process.cwd()
const API_BASE = process.env.WORLDCUP_API_BASE || 'http://127.0.0.1:8080'
const CONFIG_PATH = path.join(ROOT, 'config/user-config.json')
const SCHEDULE_PATH = path.join(ROOT, 'src/main/resources/data/schedule_2026.csv')
const PROBABILITY_KEYS = ['win', 'draw', 'lose']

const DEFAULT_FACTORS = {
  hostTeamGoalFactor: 1.42,
  seedTeamGoalFactor: 1.67,
  handicapSmoothingFactor: 0.185
}

const CURRENT_PAIR_SWITCH_THRESHOLD = 52
const CURRENT_HANDICAP_INVERT_THRESHOLD = 50

function parseCsv(text) {
  const rows = []
  let row = []
  let field = ''
  let inQuotes = false

  for (let index = 0; index < text.length; index += 1) {
    const char = text[index]
    const next = text[index + 1]

    if (inQuotes) {
      if (char === '"' && next === '"') {
        field += '"'
        index += 1
      } else if (char === '"') {
        inQuotes = false
      } else {
        field += char
      }
      continue
    }

    if (char === '"') {
      inQuotes = true
    } else if (char === ',') {
      row.push(field)
      field = ''
    } else if (char === '\n') {
      row.push(field.replace(/\r$/, ''))
      rows.push(row)
      row = []
      field = ''
    } else {
      field += char
    }
  }

  if (field.length > 0 || row.length > 0) {
    row.push(field.replace(/\r$/, ''))
    rows.push(row)
  }

  const header = rows.shift().map((name, index) => (
    index === 0 ? name.replace(/^\uFEFF/, '') : name
  ))

  return rows
    .filter(item => item.length === header.length)
    .map(item => Object.fromEntries(header.map((name, index) => [name, item[index]])))
}

function parseArgs(argv) {
  const args = {
    mode: 'current',
    simulations: 50000,
    concurrency: 4,
    strategy: 'current'
  }

  argv.forEach(arg => {
    if (arg.startsWith('--simulations=')) {
      args.simulations = Number(arg.split('=')[1]) || args.simulations
    } else if (arg.startsWith('--concurrency=')) {
      args.concurrency = Number(arg.split('=')[1]) || args.concurrency
    } else if (arg.startsWith('--strategy=')) {
      args.strategy = arg.split('=')[1] || args.strategy
    } else if (!arg.startsWith('--')) {
      args.mode = arg
    }
  })

  return args
}

async function loadState() {
  const [configText, scheduleText] = await Promise.all([
    fs.readFile(CONFIG_PATH, 'utf8'),
    fs.readFile(SCHEDULE_PATH, 'utf8')
  ])
  const config = JSON.parse(configText)
  const schedule = parseCsv(scheduleText)
  const completed = schedule.filter(row => row.status === 'COMPLETED')

  return {
    config,
    factors: { ...DEFAULT_FACTORS, ...(config.modelFactors || {}) },
    modelMode: config.modelMode || 'after',
    selectedRows: config.selectedRows || {},
    completed,
    completedById: new Map(completed.map(row => [row.match_id, row])),
    completedIds: new Set(completed.map(row => row.match_id)),
    dates: [...new Set(completed.map(row => row.match_date))].sort()
  }
}

function activeNormalProbability(match, modelMode) {
  if (modelMode === 'before') {
    return match.normalProbability || {}
  }
  return match.adjustedNormalProbability || match.normalProbability || {}
}

function activeHandicapProbabilities(match, modelMode) {
  if (modelMode === 'before') {
    return match.handicapProbabilities || []
  }
  return match.adjustedHandicapProbabilities || match.handicapProbabilities || []
}

function probabilityRows(match, modelMode) {
  const handicapRows = activeHandicapProbabilities(match, modelMode).map(item => ({
    key: `handicap-${item.handicap}`,
    handicap: Number(item.handicap),
    probability: item.probability || {},
    rowClass: 'handicap-row'
  }))

  return [
    ...handicapRows,
    {
      key: 'normal',
      handicap: 0,
      probability: activeNormalProbability(match, modelMode),
      rowClass: 'normal-row'
    }
  ].sort((left, right) => right.handicap - left.handicap)
}

function isRowSelected(match, item, selectedRows) {
  const selection = selectedRows[match.matchId] || {}
  if (Number(item.handicap) === 0) {
    return selection.normal === true
  }
  return selection.handicap === item.key
}

function cellKey(row, probabilityKey) {
  return `${row.key}-${probabilityKey}`
}

function getAdjacentProbabilityKeys(probabilityKey) {
  const index = PROBABILITY_KEYS.indexOf(probabilityKey)
  if (index < 0) {
    return []
  }
  return PROBABILITY_KEYS.filter((_, keyIndex) => Math.abs(keyIndex - index) <= 1)
}

function findMaxProbabilityCell(rows) {
  let maxCell = null
  rows.forEach(row => {
    PROBABILITY_KEYS.forEach(probabilityKey => {
      const value = Number(row.probability[probabilityKey]) || 0
      if (!maxCell || value > maxCell.value) {
        maxCell = { row, probabilityKey, value }
      }
    })
  })
  return maxCell
}

function buildRecommendationKeys(rows, applyHandicapThreshold, options) {
  const maxCell = findMaxProbabilityCell(rows)
  if (!maxCell) {
    return new Set()
  }

  if (
    applyHandicapThreshold
    && maxCell.row.handicap !== 0
    && maxCell.value < options.handicapInvertThreshold
  ) {
    return new Set(
      PROBABILITY_KEYS
        .filter(key => key !== maxCell.probabilityKey)
        .map(key => cellKey(maxCell.row, key))
    )
  }

  return new Set(
    getAdjacentProbabilityKeys(maxCell.probabilityKey)
      .map(key => cellKey(maxCell.row, key))
  )
}

function buildHandicapPairSwitchKeys(normalRow, handicapRow, rows, options) {
  const maxCell = findMaxProbabilityCell(rows)
  if (!maxCell || maxCell.row !== normalRow) {
    return null
  }
  if (maxCell.probabilityKey !== 'win' && maxCell.probabilityKey !== 'lose') {
    return null
  }

  const handicapValue = Number(handicapRow.probability[maxCell.probabilityKey]) || 0
  if (handicapValue >= options.pairSwitchThreshold && handicapValue < maxCell.value) {
    return new Set(
      [maxCell.probabilityKey, 'draw']
        .map(key => cellKey(handicapRow, key))
    )
  }
  return null
}

function getRecommendationKeys(match, state, options = {}) {
  const mergedOptions = {
    pairSwitchThreshold: CURRENT_PAIR_SWITCH_THRESHOLD,
    handicapInvertThreshold: CURRENT_HANDICAP_INVERT_THRESHOLD,
    disablePairSwitch: false,
    strategy: 'current',
    ...options
  }
  const selected = probabilityRows(match, state.modelMode)
    .filter(item => isRowSelected(match, item, state.selectedRows))

  if (selected.length === 0) {
    return new Set()
  }

  const normalRows = selected.filter(item => item.handicap === 0)
  const handicapRows = selected.filter(item => item.handicap !== 0)

  if (
    mergedOptions.strategy === 'primary-top2'
    || mergedOptions.strategy === 'each-row-top2'
    || mergedOptions.strategy === 'each-row-hybrid-top2'
  ) {
    return getRecommendationKeysByStrategy(selected, mergedOptions.strategy)
  }
  if (
    mergedOptions.strategy === 'single-handicap-top2'
    && normalRows.length === 0
    && handicapRows.length > 0
  ) {
    return getRecommendationKeysByStrategy(selected, mergedOptions.strategy)
  }
  if (
    mergedOptions.strategy === 'both-rows-top2'
    && normalRows.length > 0
    && handicapRows.length > 0
  ) {
    return getRecommendationKeysByStrategy(selected, mergedOptions.strategy)
  }

  if (normalRows.length > 0 && handicapRows.length > 0) {
    if (!mergedOptions.disablePairSwitch) {
      const pairSwitchKeys = buildHandicapPairSwitchKeys(normalRows[0], handicapRows[0], selected, mergedOptions)
      if (pairSwitchKeys) {
        return pairSwitchKeys
      }
    }
    return buildRecommendationKeys(selected, true, mergedOptions)
  }

  if (handicapRows.length > 0) {
    return buildRecommendationKeys(handicapRows, false, mergedOptions)
  }

  return buildRecommendationKeys(normalRows, false, mergedOptions)
}

function getRecommendationKeysByStrategy(selected, strategy) {
  const normalRows = selected.filter(item => item.handicap === 0)
  const handicapRows = selected.filter(item => item.handicap !== 0)
  const maxCell = findMaxProbabilityCell(selected)

  if (!maxCell) {
    return new Set()
  }

  if (strategy === 'primary-top2') {
    return topProbabilityKeys(maxCell.row, 2)
  }

  if (strategy === 'single-handicap-top2' && normalRows.length === 0 && handicapRows.length > 0) {
    return new Set(handicapRows.flatMap(row => [...topProbabilityKeys(row, 2)]))
  }

  if (strategy === 'both-rows-top2' && normalRows.length > 0 && handicapRows.length > 0) {
    return new Set(selected.flatMap(row => [...topProbabilityKeys(row, 2)]))
  }

  if (strategy === 'each-row-top2') {
    return new Set(selected.flatMap(row => [...topProbabilityKeys(row, 2)]))
  }

  if (strategy === 'each-row-hybrid-top2') {
    return new Set(selected.flatMap(row => [...hybridTopProbabilityKeys(row)]))
  }

  return new Set()
}

function topProbabilityKeys(row, count) {
  return new Set(
    PROBABILITY_KEYS
      .map(probabilityKey => ({
        probabilityKey,
        value: Number(row.probability[probabilityKey]) || 0
      }))
      .sort((left, right) => right.value - left.value)
      .slice(0, count)
      .map(item => cellKey(row, item.probabilityKey))
  )
}

function hybridTopProbabilityKeys(row) {
  const sorted = PROBABILITY_KEYS
    .map(probabilityKey => ({
      probabilityKey,
      value: Number(row.probability[probabilityKey]) || 0
    }))
    .sort((left, right) => right.value - left.value)
  const top = sorted[0]

  if (top.probabilityKey === 'lose' && top.value >= 55) {
    return new Set(['lose', 'draw'].map(key => cellKey(row, key)))
  }
  if (top.probabilityKey === 'win' && row.handicap > 0 && top.value >= 60) {
    return new Set(['win', 'lose'].map(key => cellKey(row, key)))
  }

  return new Set(sorted.slice(0, 2).map(item => cellKey(row, item.probabilityKey)))
}

function getActualProbabilityKey(score, handicap) {
  const adjustedHomeScore = score.home + handicap
  if (adjustedHomeScore > score.away) {
    return 'win'
  }
  if (adjustedHomeScore === score.away) {
    return 'draw'
  }
  return 'lose'
}

async function fetchPredictions(state, simulations, factorOverrides = {}) {
  const matches = []
  const factors = { ...state.factors, ...factorOverrides }

  for (const date of state.dates) {
    const params = new URLSearchParams({
      date,
      simulations: String(simulations),
      seedTeamGoalFactor: String(factors.seedTeamGoalFactor),
      hostTeamGoalFactor: String(factors.hostTeamGoalFactor),
      handicapSmoothingFactor: String(factors.handicapSmoothingFactor)
    })
    const response = await fetch(`${API_BASE}/api/worldcup/predictions?${params}`)
    if (!response.ok) {
      throw new Error(`Prediction API failed: ${response.status} ${response.statusText}`)
    }
    const data = await response.json()
    ;(data.matches || []).forEach(match => {
      if (state.completedIds.has(match.matchId)) {
        matches.push(match)
      }
    })
  }

  return matches.sort((left, right) => left.matchId.localeCompare(right.matchId))
}

function evaluate(matches, state, options = {}) {
  const details = []
  let hit = 0
  let selectedCount = 0
  let recommendationSizeSum = 0

  matches.forEach(match => {
    const schedule = state.completedById.get(match.matchId)
    if (!schedule) {
      return
    }

    const score = {
      home: Number(schedule.home_score),
      away: Number(schedule.away_score)
    }
    const rows = probabilityRows(match, state.modelMode)
    const selected = rows.filter(item => isRowSelected(match, item, state.selectedRows))
    const recommendationKeys = getRecommendationKeys(match, state, options)
    const actualCells = rows.map(item => {
      const probabilityKey = getActualProbabilityKey(score, item.handicap)
      return {
        row: item,
        probabilityKey,
        key: cellKey(item, probabilityKey)
      }
    })
    const isHit = actualCells.some(cell => recommendationKeys.has(cell.key))
    const maxCell = findMaxProbabilityCell(selected)

    if (recommendationKeys.size > 0) {
      selectedCount += 1
      recommendationSizeSum += recommendationKeys.size
    }
    if (isHit) {
      hit += 1
    }

    details.push({
      matchId: match.matchId,
      date: schedule.match_date,
      fixture: `${schedule.home_team_cn} ${score.home}-${score.away} ${schedule.away_team_cn}`,
      selected: selected.map(item => item.key).join('+'),
      max: maxCell ? `${maxCell.row.key}-${maxCell.probabilityKey}:${maxCell.value.toFixed(2)}` : '',
      recommendation: [...recommendationKeys].join(','),
      actualKeys: actualCells.map(item => item.key).join(','),
      probabilities: selected
        .map(item => `${item.key}[W${formatPct(item.probability.win)} D${formatPct(item.probability.draw)} L${formatPct(item.probability.lose)}]`)
        .join(' | '),
      hit: isHit
    })
  })

  return {
    total: details.length,
    selectedCount,
    hit,
    miss: details.length - hit,
    rate: details.length > 0 ? hit / details.length : 0,
    avgRecommendationSize: selectedCount > 0 ? recommendationSizeSum / selectedCount : 0,
    details,
    misses: details.filter(item => !item.hit)
  }
}

function formatPct(value) {
  return (Number(value) || 0).toFixed(1)
}

function formatRate(value) {
  return `${(value * 100).toFixed(2)}%`
}

function range(start, end, step, digits = 3) {
  const values = []
  for (let value = start; value <= end + 1e-9; value += step) {
    values.push(Number(value.toFixed(digits)))
  }
  return values
}

function factorDistance(combo, currentFactors) {
  return Object.entries(combo).reduce((sum, [name, value]) => {
    return sum + Math.abs(Number(value) - Number(currentFactors[name] ?? 0))
  }, 0)
}

async function evaluateCombo(state, simulations, combo, options = {}) {
  const matches = await fetchPredictions(state, simulations, combo)
  const result = evaluate(matches, state, options)
  return {
    ...combo,
    total: result.total,
    hit: result.hit,
    miss: result.miss,
    rate: result.rate,
    misses: result.misses.map(item => item.matchId)
  }
}

async function mapLimit(items, limit, iterator) {
  const results = new Array(items.length)
  let cursor = 0
  const workers = Array.from({ length: Math.min(limit, items.length) }, async () => {
    while (cursor < items.length) {
      const current = cursor
      cursor += 1
      results[current] = await iterator(items[current], current)
    }
  })
  await Promise.all(workers)
  return results
}

function sortComboResults(results, currentFactors) {
  return results.sort((left, right) => {
    if (right.hit !== left.hit) {
      return right.hit - left.hit
    }
    return factorDistance(left, currentFactors) - factorDistance(right, currentFactors)
  })
}

async function runCurrent(state, simulations, strategy) {
  const matches = await fetchPredictions(state, simulations)
  const result = evaluate(matches, state, strategy === 'current' ? {} : { strategy })
  printJson({
    mode: 'current',
    simulations,
    strategy,
    modelMode: state.modelMode,
    factors: state.factors,
    completed: state.completed.length,
    result: summarizeResult(result),
    misses: result.misses.map(item => ({
      matchId: item.matchId,
      date: item.date,
      fixture: item.fixture,
      selected: item.selected,
      max: item.max,
      recommendation: item.recommendation,
      probabilities: item.probabilities
    }))
  })
}

async function runRules(state, simulations) {
  const matches = await fetchPredictions(state, simulations)
  const results = []

  for (const pairSwitchThreshold of range(35, 65, 1, 0)) {
    for (const handicapInvertThreshold of range(35, 60, 1, 0)) {
      const result = evaluate(matches, state, { pairSwitchThreshold, handicapInvertThreshold })
      results.push({
        pairSwitchThreshold,
        handicapInvertThreshold,
        hit: result.hit,
        miss: result.miss,
        rate: result.rate
      })
    }
  }

  for (const handicapInvertThreshold of range(35, 60, 1, 0)) {
    const result = evaluate(matches, state, { disablePairSwitch: true, handicapInvertThreshold })
    results.push({
      pairSwitchThreshold: 'disabled',
      handicapInvertThreshold,
      hit: result.hit,
      miss: result.miss,
      rate: result.rate
    })
  }

  results.sort((left, right) => right.hit - left.hit || left.miss - right.miss)
  printJson({
    mode: 'rules',
    current: summarizeResult(evaluate(matches, state)),
    top: results.slice(0, 20).map(formatRuleResult)
  })
}

async function runVariants(state, simulations) {
  const matches = await fetchPredictions(state, simulations)
  const variants = [
    { name: 'current', options: {} },
    { name: 'primary-top2', options: { strategy: 'primary-top2' } },
    { name: 'single-handicap-top2', options: { strategy: 'single-handicap-top2' } },
    { name: 'both-rows-top2', options: { strategy: 'both-rows-top2' } },
    { name: 'each-row-top2', options: { strategy: 'each-row-top2' } },
    { name: 'each-row-hybrid-top2', options: { strategy: 'each-row-hybrid-top2' } }
  ]
  const results = variants.map(variant => {
    const result = evaluate(matches, state, variant.options)
    return {
      strategy: variant.name,
      ...summarizeResult(result),
      misses: result.misses.map(item => item.matchId).join(',')
    }
  }).sort((left, right) => right.hit - left.hit || left.avgRecommendationSize - right.avgRecommendationSize)

  printJson({
    mode: 'variants',
    simulations,
    results
  })
}

async function runSweep(state, simulations, concurrency, strategy) {
  const definitions = {
    seedTeamGoalFactor: range(1.2, 2.4, 0.05),
    hostTeamGoalFactor: range(1.0, 1.8, 0.05),
    handicapSmoothingFactor: range(0.05, 0.35, 0.01)
  }
  const summary = {}

  for (const [name, values] of Object.entries(definitions)) {
    const startedAt = Date.now()
    console.error(`sweep ${name}: ${values.length} values`)
    const results = await mapLimit(values, concurrency, value => (
      evaluateCombo(state, simulations, { [name]: value }, strategy === 'current' ? {} : { strategy })
    ))
    sortComboResults(results, state.factors)
    summary[name] = results.slice(0, 10).map(item => ({
      value: item[name],
      hit: item.hit,
      miss: item.miss,
      rate: formatRate(item.rate)
    }))
    console.error(`done ${name}: ${Date.now() - startedAt}ms`)
  }

  printJson({
    mode: 'sweep',
    simulations,
    strategy,
    currentFactors: state.factors,
    summary
  })
}

async function runGoalGrid(state, simulations, concurrency, strategy) {
  const combos = []
  for (const seedTeamGoalFactor of [1.75, 1.8, 1.85, 1.9, 1.95, 2.0, 2.05]) {
    for (const hostTeamGoalFactor of [1.34, 1.38, 1.42, 1.46, 1.5, 1.54]) {
      for (const handicapSmoothingFactor of [0.16, 0.18, 0.185, 0.2, 0.24, 0.28, 0.3]) {
        combos.push({ seedTeamGoalFactor, hostTeamGoalFactor, handicapSmoothingFactor })
      }
    }
  }
  await runComboGrid(state, simulations, concurrency, 'goal-grid', combos, strategy)
}

async function runComboGrid(state, simulations, concurrency, mode, combos, strategy) {
  const startedAt = Date.now()
  console.error(`${mode}: ${combos.length} combos, simulations=${simulations}, concurrency=${concurrency}, strategy=${strategy}`)
  const results = await mapLimit(combos, concurrency, async (combo, index) => {
    const result = await evaluateCombo(state, simulations, combo, strategy === 'current' ? {} : { strategy })
    if ((index + 1) % 25 === 0 || index === combos.length - 1) {
      console.error(`${mode}: ${index + 1}/${combos.length}`)
    }
    return result
  })
  sortComboResults(results, state.factors)
  printJson({
    mode,
    simulations,
    strategy,
    count: combos.length,
    elapsedMs: Date.now() - startedAt,
    top: results.slice(0, 25).map(formatComboResult)
  })
}

function summarizeResult(result) {
  return {
    total: result.total,
    selectedCount: result.selectedCount,
    hit: result.hit,
    miss: result.miss,
    rate: formatRate(result.rate),
    avgRecommendationSize: Number(result.avgRecommendationSize.toFixed(2))
  }
}

function formatComboResult(result) {
  const formatted = {}
  Object.entries(result).forEach(([name, value]) => {
    if (!['total', 'hit', 'miss', 'rate', 'misses'].includes(name)) {
      formatted[name] = value
    }
  })
  formatted.hit = result.hit
  formatted.miss = result.miss
  formatted.rate = formatRate(result.rate)
  formatted.misses = result.misses.join(',')
  return formatted
}

function formatRuleResult(result) {
  return {
    pairSwitchThreshold: result.pairSwitchThreshold,
    handicapInvertThreshold: result.handicapInvertThreshold,
    hit: result.hit,
    miss: result.miss,
    rate: formatRate(result.rate)
  }
}

function printJson(value) {
  console.log(JSON.stringify(value, null, 2))
}

async function main() {
  const args = parseArgs(process.argv.slice(2))
  const state = await loadState()

  if (args.mode === 'current') {
    await runCurrent(state, args.simulations, args.strategy)
  } else if (args.mode === 'rules') {
    await runRules(state, args.simulations)
  } else if (args.mode === 'sweep') {
    await runSweep(state, args.simulations, args.concurrency, args.strategy)
  } else if (args.mode === 'variants') {
    await runVariants(state, args.simulations)
  } else if (args.mode === 'goal-grid') {
    await runGoalGrid(state, args.simulations, args.concurrency, args.strategy)
  } else {
    throw new Error(`Unknown mode: ${args.mode}`)
  }
}

main().catch(error => {
  console.error(error)
  process.exitCode = 1
})
