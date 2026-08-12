export function meetsMinimumConstraint(value, minimumValue, strict = false) {
  if (!Number.isFinite(value) || !Number.isFinite(minimumValue)) {
    return false
  }
  return strict ? value > minimumValue : value >= minimumValue
}
