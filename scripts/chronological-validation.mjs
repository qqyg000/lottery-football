// 输入按比赛日期升序排列，边界不能为了满足最少场次而切开同一天
export function chronologicalSplitIndex(matches, validationFraction, minimumTrainingMatches, minimumValidationMatches) {
  if (matches.length < minimumTrainingMatches + minimumValidationMatches) return -1
  const validationCount = Math.max(minimumValidationMatches, Math.ceil(matches.length * validationFraction))
  const target = Math.min(matches.length - minimumValidationMatches,
    Math.max(minimumTrainingMatches, matches.length - validationCount))
  const boundaryDate = matches[target]?.matchDate
  let before = target
  while (before > 0 && matches[before - 1]?.matchDate === boundaryDate) before--
  if (before >= minimumTrainingMatches && matches.length - before >= minimumValidationMatches) return before
  let after = target
  while (after < matches.length && matches[after]?.matchDate === boundaryDate) after++
  return after >= minimumTrainingMatches && matches.length - after >= minimumValidationMatches ? after : -1
}
