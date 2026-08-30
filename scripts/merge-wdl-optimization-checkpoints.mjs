import fs from 'node:fs/promises'
import path from 'node:path'

const ROOT = process.cwd()
const args = process.argv.slice(2)
const inputPaths = []
const closedRanges = new Set()
let outputPath = null

for (let index = 0; index < args.length; index += 1) {
  const argument = args[index]
  if (argument === '--output') {
    outputPath = args[index + 1]
    index += 1
  } else if (argument === '--close-range') {
    closedRanges.add(args[index + 1])
    index += 1
  } else {
    inputPaths.push(argument)
  }
}

if (!outputPath || inputPaths.length === 0) {
  throw new Error(
    'usage: node scripts/merge-wdl-optimization-checkpoints.mjs ' +
    '--output <checkpoint> [--close-range <competition:range>] <shard>...'
  )
}

const config = JSON.parse(
  await fs.readFile(path.join(ROOT, 'config/user-config.json'), 'utf8')
)
const parameterProfiles = structuredClone(config.parameterProfiles || {})
const expectedProfileKeys = Object.keys(parameterProfiles)
const expectedRangeKeys = new Set(
  expectedProfileKeys.map(key => key.split(':').slice(0, 2).join(':'))
)
const baselineByKey = new Map()
const resultByRange = new Map()

for (const inputPath of inputPaths) {
  const checkpoint = JSON.parse(
    await fs.readFile(path.resolve(ROOT, inputPath), 'utf8')
  )
  for (const item of checkpoint.baselineVerification || []) {
    baselineByKey.set(item.key, item)
  }
  for (const result of checkpoint.optimizationResults || []) {
    const rangeKey = `${result.competition}:${result.range}`
    if (resultByRange.has(rangeKey)) {
      throw new Error(`duplicate optimization result: ${rangeKey}`)
    }
    resultByRange.set(rangeKey, structuredClone(result))
    for (const preset of ['STABLE', 'AGGRESSIVE']) {
      const profileKey = `${rangeKey}:${preset}`
      const profile = checkpoint.parameterProfiles?.[profileKey]
      if (!profile) {
        throw new Error(`missing parameter profile: ${profileKey}`)
      }
      parameterProfiles[profileKey] = structuredClone(profile)
    }
  }
}

for (const rangeKey of closedRanges) {
  const result = resultByRange.get(rangeKey)
  if (!result) {
    throw new Error(`cannot close missing optimization result: ${rangeKey}`)
  }
  result.status = 'CLOSED_FINAL_VERIFICATION_FAILED'
  result.samplingPolicy = 'CLOSED_FINAL_VERIFICATION_FAILED'
  result.reasons = [
    ...new Set([
      ...(result.reasons || []),
      '最终执行层训练集或留出验证集稳健性复验失败'
    ])
  ]
  for (const preset of ['STABLE', 'AGGRESSIVE']) {
    const profileKey = `${rangeKey}:${preset}`
    parameterProfiles[profileKey] = {
      ...parameterProfiles[profileKey],
      globalParameters: {
        ...parameterProfiles[profileKey].globalParameters,
        recommendationOdds: 100
      }
    }
  }
}

const missingRanges = [...expectedRangeKeys].filter(key => !resultByRange.has(key))
const unexpectedRanges = [...resultByRange.keys()].filter(key => !expectedRangeKeys.has(key))
if (missingRanges.length > 0 || unexpectedRanges.length > 0) {
  throw new Error(
    `optimization range mismatch: missing=${missingRanges.join(',') || '--'} ` +
    `unexpected=${unexpectedRanges.join(',') || '--'}`
  )
}
if (Object.keys(parameterProfiles).length !== expectedProfileKeys.length) {
  throw new Error(
    `parameter profile count mismatch: expected=${expectedProfileKeys.length} ` +
    `actual=${Object.keys(parameterProfiles).length}`
  )
}
if (baselineByKey.size !== expectedProfileKeys.length) {
  throw new Error(
    `baseline verification count mismatch: expected=${expectedProfileKeys.length} ` +
    `actual=${baselineByKey.size}`
  )
}

const merged = {
  generatedAt: new Date().toISOString(),
  baselineVerification: [...baselineByKey.values()],
  optimizationResults: [...resultByRange.values()],
  parameterProfiles
}
const absoluteOutputPath = path.resolve(ROOT, outputPath)
await fs.mkdir(path.dirname(absoluteOutputPath), { recursive: true })
await fs.writeFile(absoluteOutputPath, JSON.stringify(merged, null, 2) + '\n', 'utf8')
process.stdout.write(
  `merged ranges=${resultByRange.size} profiles=${Object.keys(parameterProfiles).length} ` +
  `closed=${closedRanges.size} output=${absoluteOutputPath}\n`
)
