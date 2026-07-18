param(
    [datetime]$StartDate,

    [datetime]$EndDate,

    [string]$SchedulePath = (Join-Path $PSScriptRoot "..\config\club-competition-schedules.json"),

    [string]$CachePath = (Join-Path $PSScriptRoot "..\config\sporttery-market-selections.json"),

    [string]$OutputPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\historical_odds_data.csv")
)

$ErrorActionPreference = "Stop"

$supportedCompetitions = [Collections.Generic.HashSet[string]]::new([string[]]@(
    "WORLD_CUP",
    "EUROPEAN_CHAMPIONSHIP",
    "COPA_AMERICA",
    "CLUB_WORLD_CUP",
    "EUROPA_LEAGUE",
    "CHAMPIONS_LEAGUE",
    "PREMIER_LEAGUE",
    "LA_LIGA",
    "SERIE_A",
    "BUNDESLIGA",
    "LIGUE_1",
    "BRAZIL_SERIE_A",
    "PRIMEIRA_LIGA",
    "EREDIVISIE",
    "ARGENTINE_PRIMERA_DIVISION"
))

function Get-CanonicalTeamName {
    param([string]$Value)

    $normalized = if ($null -eq $Value) { "" } else { $Value }
    $normalized = $normalized.Normalize([Text.NormalizationForm]::FormKC).ToUpperInvariant()
    $normalized = $normalized.Replace("足球俱乐部", "").Replace("俱乐部", "")
    $normalized = $normalized -replace "[\s·•.．,，'’``´()（）\[\]【】\-_/&]+", ""
    $normalized = $normalized -replace "^(FC|SC|CF)(?=[\u4e00-\u9fff])", ""
    $normalized = $normalized -replace "(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$", ""
    return $normalized
}

function Test-CanonicalNamesMatch {
    param(
        [string]$Source,
        [string]$Target
    )

    if ([string]::IsNullOrWhiteSpace($Source) -or [string]::IsNullOrWhiteSpace($Target)) {
        return $false
    }
    if ($Source -eq $Target) {
        return $true
    }
    return [Math]::Min($Source.Length, $Target.Length) -ge 4 `
        -and ($Source.Contains($Target) -or $Target.Contains($Source))
}

function Test-TeamNamesMatch {
    param(
        [object]$Schedule,
        [object]$Entry,
        [bool]$IsHome,
        [hashtable]$Translations
    )

    $chineseName = if ($IsHome) { [string]$Schedule.homeTeamCn } else { [string]$Schedule.awayTeamCn }
    $englishName = if ($IsHome) { [string]$Schedule.homeTeamEn } else { [string]$Schedule.awayTeamEn }
    $sportteryName = if ($IsHome) { [string]$Entry.homeTeam } else { [string]$Entry.awayTeam }
    $englishKey = Get-CanonicalSourceName $englishName
    $competitionKey = [string]$Schedule.competition + "|" + $englishKey
    $globalKey = "*|" + $englishKey
    $translatedName = if ($Translations.ContainsKey($competitionKey)) {
        $Translations[$competitionKey]
    } elseif ($Translations.ContainsKey($globalKey)) {
        $Translations[$globalKey]
    } else {
        $englishName
    }
    $target = Get-CanonicalTeamName $sportteryName
    return (Test-CanonicalNamesMatch (Get-CanonicalTeamName $chineseName) $target) `
        -or (Test-CanonicalNamesMatch (Get-CanonicalTeamName $translatedName) $target)
}

function Get-CanonicalSourceName {
    param([string]$Value)

    $normalized = if ($null -eq $Value) { "" } else { $Value }
    $normalized = $normalized.Normalize([Text.NormalizationForm]::FormKD).ToUpperInvariant()
    $normalized = [Text.RegularExpressions.Regex]::Replace($normalized, "\p{M}", "")
    return [Text.RegularExpressions.Regex]::Replace($normalized, "[^\p{L}\p{N}]", "")
}

function Get-MatchScore {
    param(
        [object]$Schedule,
        [object]$Entry,
        [hashtable]$Translations
    )

    if (-not (Test-TeamNamesMatch $Schedule $Entry $true $Translations) `
            -or -not (Test-TeamNamesMatch $Schedule $Entry $false $Translations)) {
        return -1
    }
    $score = 100
    if ($null -ne $Schedule.homeScore -and $null -ne $Schedule.awayScore `
            -and $null -ne $Entry.homeScore -and $null -ne $Entry.awayScore `
            -and [int]$Schedule.homeScore -eq [int]$Entry.homeScore `
            -and [int]$Schedule.awayScore -eq [int]$Entry.awayScore) {
        $score += 20
    }
    $distance = [Math]::Abs((([datetime]$Schedule.matchDate) - ([datetime]$Entry.matchDate)).Days)
    return $score + $(if ($distance -eq 0) { 6 } else { 2 })
}

function Get-OddsValue {
    param(
        [object]$Odds,
        [string]$Property
    )

    if ($null -eq $Odds) {
        return ""
    }
    return [string]$Odds.$Property
}

function Get-FixtureKey {
    param([object]$Row)

    return [string]$Row.competition `
        + "|" + [string]$Row.match_date `
        + "|" + (Get-CanonicalTeamName ([string]$Row.home_team_cn)) `
        + "|" + (Get-CanonicalTeamName ([string]$Row.away_team_cn))
}

$resolvedSchedulePath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($SchedulePath)
$resolvedCachePath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($CachePath)
$resolvedOutputPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
foreach ($path in @($resolvedSchedulePath, $resolvedCachePath, $resolvedOutputPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required file not found: $path"
    }
}
if ($EndDate.Date -lt $StartDate.Date) {
    throw "EndDate cannot be earlier than StartDate"
}

$existingRows = @(Import-Csv -LiteralPath $resolvedOutputPath -Encoding UTF8)
$translations = @{}
foreach ($row in $existingRows) {
    foreach ($side in @("home", "away")) {
        $englishName = [string]$row.PSObject.Properties["${side}_team_en"].Value
        $chineseName = [string]$row.PSObject.Properties["${side}_team_cn"].Value
        if ([string]::IsNullOrWhiteSpace($englishName) -or [string]::IsNullOrWhiteSpace($chineseName)) {
            continue
        }
        $englishKey = Get-CanonicalSourceName $englishName
        $translations[([string]$row.competition + "|" + $englishKey)] = $chineseName
        $translations[("*|" + $englishKey)] = $chineseName
    }
}

$schedules = Get-Content -LiteralPath $resolvedSchedulePath -Raw -Encoding UTF8 | ConvertFrom-Json
$cache = Get-Content -LiteralPath $resolvedCachePath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = @($cache.entries | Where-Object {
    $supportedCompetitions.Contains([string]$_.competition) `
        -and ([datetime]$_.matchDate).Date -ge $StartDate.Date `
        -and ([datetime]$_.matchDate).Date -le $EndDate.Date
})
$entriesByCompetition = @{}
foreach ($entry in $entries) {
    $competition = [string]$entry.competition
    if (-not $entriesByCompetition.ContainsKey($competition)) {
        $entriesByCompetition[$competition] = New-Object System.Collections.Generic.List[object]
    }
    $entriesByCompetition[$competition].Add($entry)
}

$usedMatchIds = [Collections.Generic.HashSet[string]]::new()
$newRows = New-Object System.Collections.Generic.List[object]
foreach ($schedule in $schedules) {
    $matchDate = ([datetime]$schedule.matchDate).Date
    if ($matchDate -lt $StartDate.Date -or $matchDate -gt $EndDate.Date `
            -or [string]$schedule.status -ne "COMPLETED") {
        continue
    }
    $competition = [string]$schedule.competition
    if (-not $supportedCompetitions.Contains($competition) `
            -or -not $entriesByCompetition.ContainsKey($competition)) {
        continue
    }

    $bestEntry = $null
    $bestScore = -1
    $ambiguous = $false
    foreach ($entry in $entriesByCompetition[$competition]) {
        if ($usedMatchIds.Contains([string]$entry.sportteryMatchId)) {
            continue
        }
        $dateDistance = [Math]::Abs(($matchDate - ([datetime]$entry.matchDate).Date).Days)
        if ($dateDistance -gt 1) {
            continue
        }
        $score = Get-MatchScore $schedule $entry $translations
        if ($score -gt $bestScore) {
            $bestEntry = $entry
            $bestScore = $score
            $ambiguous = $false
        } elseif ($score -ge 0 -and $score -eq $bestScore) {
            $ambiguous = $true
        }
    }
    if ($bestScore -lt 70 -or $ambiguous -or $null -eq $bestEntry `
            -or ($null -eq $bestEntry.normalOdds -and $null -eq $bestEntry.handicapOdds)) {
        continue
    }

    $null = $usedMatchIds.Add([string]$bestEntry.sportteryMatchId)
    $newRows.Add([pscustomobject][ordered]@{
        match_id = "HIS-SPT-$($bestEntry.sportteryMatchId)"
        match_date = $schedule.matchDate
        competition = $schedule.competition
        home_team_cn = $schedule.homeTeamCn
        away_team_cn = $schedule.awayTeamCn
        home_team_en = $schedule.homeTeamEn
        away_team_en = $schedule.awayTeamEn
        home_score = $schedule.homeScore
        away_score = $schedule.awayScore
        neutral = ([bool]$schedule.neutral).ToString().ToLowerInvariant()
        sporttery_match_number = $bestEntry.sportteryMatchNumber
        handicap = if ($null -eq $bestEntry.handicap) { "" } else { [string]$bestEntry.handicap }
        normal_win = Get-OddsValue $bestEntry.normalOdds "win"
        normal_draw = Get-OddsValue $bestEntry.normalOdds "draw"
        normal_lose = Get-OddsValue $bestEntry.normalOdds "lose"
        handicap_win = Get-OddsValue $bestEntry.handicapOdds "win"
        handicap_draw = Get-OddsValue $bestEntry.handicapOdds "draw"
        handicap_lose = Get-OddsValue $bestEntry.handicapOdds "lose"
    })
}

$newMatchIds = [Collections.Generic.HashSet[string]]::new()
foreach ($row in $newRows) {
    $null = $newMatchIds.Add([string]$row.match_id)
}
$existingMatchIds = [Collections.Generic.HashSet[string]]::new()
$rowsByFixture = [ordered]@{}
foreach ($row in $existingRows) {
    $null = $existingMatchIds.Add([string]$row.match_id)
    if ($newMatchIds.Contains([string]$row.match_id)) {
        continue
    }
    $rowsByFixture[(Get-FixtureKey $row)] = $row
}
$addedCount = 0
$updatedCount = 0
foreach ($row in $newRows) {
    $key = Get-FixtureKey $row
    if ($existingMatchIds.Contains([string]$row.match_id) -or $rowsByFixture.Contains($key)) {
        $updatedCount++
    } else {
        $addedCount++
    }
    $rowsByFixture[$key] = $row
}

$rowsByFixture.Values |
    Sort-Object match_date, competition, match_id |
    Export-Csv -LiteralPath $resolvedOutputPath -NoTypeInformation -Encoding UTF8

Write-Host "Matched cache odds rows: $($newRows.Count)"
Write-Host "Added odds rows: $addedCount"
Write-Host "Updated odds rows: $updatedCount"
Write-Host "Output rows: $($rowsByFixture.Count)"
Write-Host "Output: $resolvedOutputPath"

$defaultOutputPath = [IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot "..\src\main\resources\data\historical_odds_data.csv")
)
if ([string]::Equals($resolvedOutputPath, $defaultOutputPath, [StringComparison]::OrdinalIgnoreCase)) {
    $nodeCommand = Get-Command node -ErrorAction SilentlyContinue
    $nodePath = if ($null -ne $nodeCommand) {
        $nodeCommand.Source
    } else {
        Join-Path $PSScriptRoot "..\target\node\node.exe"
    }
    if (-not (Test-Path -LiteralPath $nodePath)) {
        throw "Node.js is required to reconcile official full-time scores"
    }
    & $nodePath (Join-Path $PSScriptRoot "reconcile-historical-scores.mjs") --write --compact
    if ($LASTEXITCODE -ne 0) {
        throw "Historical score reconciliation failed with exit code $LASTEXITCODE"
    }
}
