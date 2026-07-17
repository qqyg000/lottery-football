param(
    [datetime]$StartDate,

    [datetime]$EndDate,

    [string]$SchedulePath = (Join-Path $PSScriptRoot "..\config\club-competition-schedules.json"),

    [string]$CachePath = (Join-Path $PSScriptRoot "..\config\sporttery-market-selections.json"),

    [string]$TranslatorPath = (Join-Path $PSScriptRoot "..\src\main\java\com\eason\worldcup\util\ClubTeamNameTranslator.java"),

    [string]$OutputPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\historical_odds.csv")
)

$ErrorActionPreference = "Stop"

function Get-CanonicalTeamName {
    param([string]$Value)

    $normalized = if ($null -eq $Value) { "" } else { $Value }
    $normalized = $normalized.Normalize([Text.NormalizationForm]::FormKC).ToUpperInvariant()
    $normalized = $normalized.Replace("足球俱乐部", "").Replace("俱乐部", "")
    $normalized = $normalized -replace "[\s·•.．,，'’``´()（）\[\]【】\-_/&]+", ""
    $normalized = $normalized -replace "^(FC|SC|CF)(?=[\u4e00-\u9fff])", ""
    $normalized = $normalized -replace "(AIF|FC|SC|CF|SK|FK|IF|BK|FF)$", ""
    switch ($normalized) {
        { $_ -in @("尤尔加登", "佐加顿斯") } { return "佐加顿斯" }
        { $_ -in @("桑德菲杰", "桑纳菲尤尔") } { return "桑纳菲尤尔" }
        { $_ -in @("萨普斯堡", "萨尔普斯堡") } { return "萨尔普斯堡" }
        { $_ -in @("哈伊杜克斯普利特", "斯普利特海杜克") } { return "斯普利特海杜克" }
        { $_ -in @("杰尔ETO", "杰尔") } { return "杰尔" }
        { $_ -in @("阿特尔特比森", "比森阿泰尔") } { return "比森阿泰尔" }
        { $_ -in @("刚果民主共和国", "民主刚果", "刚果金") } { return "刚果金" }
        { $_ -in @("蔚山HD", "蔚山现代") } { return "蔚山现代" }
        { $_ -in @("浦项钢铁", "浦项制铁") } { return "浦项制铁" }
        { $_ -in @("尚州尚武", "金泉尚武") } { return "金泉尚武" }
        default { return $normalized }
    }
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
    $translatedName = if ($Translations.ContainsKey($englishName)) { $Translations[$englishName] } else { $englishName }
    $target = Get-CanonicalTeamName $sportteryName
    return (Test-CanonicalNamesMatch (Get-CanonicalTeamName $chineseName) $target) `
        -or (Test-CanonicalNamesMatch (Get-CanonicalTeamName $translatedName) $target)
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
$resolvedTranslatorPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($TranslatorPath)
$resolvedOutputPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
foreach ($path in @($resolvedSchedulePath, $resolvedCachePath, $resolvedTranslatorPath, $resolvedOutputPath)) {
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Required file not found: $path"
    }
}
if ($EndDate.Date -lt $StartDate.Date) {
    throw "EndDate cannot be earlier than StartDate"
}

$translations = @{}
$translatorSource = Get-Content -LiteralPath $resolvedTranslatorPath -Raw -Encoding UTF8
[regex]::Matches($translatorSource, 'teamNames\.put\("([^"]+)",\s*"([^"]+)"\)') | ForEach-Object {
    $translations[$_.Groups[1].Value] = $_.Groups[2].Value
}

$schedules = Get-Content -LiteralPath $resolvedSchedulePath -Raw -Encoding UTF8 | ConvertFrom-Json
$cache = Get-Content -LiteralPath $resolvedCachePath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = @($cache.entries | Where-Object {
    ([datetime]$_.matchDate).Date -ge $StartDate.Date `
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
    if (-not $entriesByCompetition.ContainsKey($competition)) {
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
        group_name = $schedule.groupName
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
        source_row = "SPORTTERY-$($bestEntry.sportteryMatchId)"
    })
}

$newSourceRows = [Collections.Generic.HashSet[string]]::new()
foreach ($row in $newRows) {
    $null = $newSourceRows.Add([string]$row.source_row)
}
$existingSourceRows = [Collections.Generic.HashSet[string]]::new()
$rowsByFixture = [ordered]@{}
foreach ($row in @(Import-Csv -LiteralPath $resolvedOutputPath -Encoding UTF8)) {
    $null = $existingSourceRows.Add([string]$row.source_row)
    if ($newSourceRows.Contains([string]$row.source_row)) {
        continue
    }
    $rowsByFixture[(Get-FixtureKey $row)] = $row
}
$addedCount = 0
$updatedCount = 0
foreach ($row in $newRows) {
    $key = Get-FixtureKey $row
    if ($existingSourceRows.Contains([string]$row.source_row) -or $rowsByFixture.Contains($key)) {
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
