param(
    [Parameter(Mandatory = $true)]
    [string]$SourcePath,

    [string]$OutputPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\historical_odds_data.csv"),

    [datetime]$StartDate = [datetime]"2014-10-22",

    [int]$SourceRowOffset = 2
)

$ErrorActionPreference = "Stop"

$competitionByLeague = @{
    "世界杯" = "WORLD_CUP"
    "欧洲杯" = "EUROPEAN_CHAMPIONSHIP"
    "美洲杯" = "COPA_AMERICA"
    "世俱杯" = "CLUB_WORLD_CUP"
    "欧罗巴" = "EUROPA_LEAGUE"
    "欧冠" = "CHAMPIONS_LEAGUE"
    "英超" = "PREMIER_LEAGUE"
    "西甲" = "LA_LIGA"
    "意甲" = "SERIE_A"
    "德甲" = "BUNDESLIGA"
    "法甲" = "LIGUE_1"
    "巴甲" = "BRAZIL_SERIE_A"
    "葡超" = "PRIMEIRA_LIGA"
    "荷甲" = "EREDIVISIE"
    "阿甲" = "ARGENTINE_PRIMERA_DIVISION"
}

$supportedCompetitions = [Collections.Generic.HashSet[string]]::new(
    [string[]]@($competitionByLeague.Values)
)

$neutralCompetitions = [Collections.Generic.HashSet[string]]::new(
    [string[]]@("WORLD_CUP", "EUROPEAN_CHAMPIONSHIP", "COPA_AMERICA", "CLUB_WORLD_CUP")
)

function Get-FirstValue {
    param(
        [object]$Row,
        [string[]]$PropertyNames
    )

    foreach ($propertyName in $PropertyNames) {
        $property = $Row.PSObject.Properties[$propertyName]
        if ($null -ne $property -and -not [string]::IsNullOrWhiteSpace([string]$property.Value)) {
            return [string]$property.Value
        }
    }
    return ""
}

function Test-ExactScore {
    param([string]$Score)

    return $Score -match "^\d+:\d+$"
}

function Get-CanonicalChineseName {
    param([string]$Value)

    $normalized = if ($null -eq $Value) { "" } else { $Value }
    $normalized = $normalized.Normalize([Text.NormalizationForm]::FormKC).ToUpperInvariant()
    $normalized = $normalized.Replace("足球俱乐部", "").Replace("俱乐部", "")
    return [Text.RegularExpressions.Regex]::Replace(
        $normalized,
        "[\s·•.．,，'’``´()（）\[\]【】\-_/&]+",
        ""
    )
}

function Get-EnglishName {
    param(
        [hashtable]$Translations,
        [string]$Competition,
        [string]$ChineseName
    )

    $nameKey = Get-CanonicalChineseName $ChineseName
    $competitionKey = "$Competition|$nameKey"
    $globalKey = "*|$nameKey"
    if ($Translations.ContainsKey($competitionKey)) {
        return [string]$Translations[$competitionKey]
    }
    if ($Translations.ContainsKey($globalKey)) {
        return [string]$Translations[$globalKey]
    }
    return ""
}

function Get-ScoreParts {
    param([string]$Score)

    $parts = $Score.Split(":")
    return @([int]$parts[0], [int]$parts[1])
}

function Get-OddsTriplet {
    param(
        [object]$Row,
        [bool]$Handicap
    )

    if ($Handicap) {
        return @(
            (Get-FirstValue $Row @("赔率-让球胜", "让球胜平负初盘赔率-胜")),
            (Get-FirstValue $Row @("赔率-让球平", "让球胜平负初盘赔率-平")),
            (Get-FirstValue $Row @("赔率-让球负", "让球胜平负初盘赔率-负"))
        )
    }
    return @(
        (Get-FirstValue $Row @("赔率-胜", "胜平负初盘赔率-胜")),
        (Get-FirstValue $Row @("赔率-平", "胜平负初盘赔率-平")),
        (Get-FirstValue $Row @("赔率-负", "胜平负初盘赔率-负"))
    )
}

function Test-CompleteOdds {
    param([object[]]$Odds)

    return $Odds.Count -eq 3 `
        -and -not [string]::IsNullOrWhiteSpace([string]$Odds[0]) `
        -and -not [string]::IsNullOrWhiteSpace([string]$Odds[1]) `
        -and -not [string]::IsNullOrWhiteSpace([string]$Odds[2])
}

$resolvedSourcePath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($SourcePath)
$resolvedOutputPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
if (-not (Test-Path -LiteralPath $resolvedSourcePath)) {
    throw "Historical odds source not found: $resolvedSourcePath"
}

$existingRows = @()
$existingEnglishByChinese = @{}
if (Test-Path -LiteralPath $resolvedOutputPath) {
    $existingRows = @(Import-Csv -LiteralPath $resolvedOutputPath -Encoding UTF8)
    foreach ($existingRow in @($existingRows | Sort-Object match_date)) {
        foreach ($side in @("home", "away")) {
            $chineseName = [string]$existingRow.PSObject.Properties["${side}_team_cn"].Value
            $englishName = [string]$existingRow.PSObject.Properties["${side}_team_en"].Value
            if ([string]::IsNullOrWhiteSpace($chineseName) -or [string]::IsNullOrWhiteSpace($englishName)) {
                continue
            }
            $nameKey = Get-CanonicalChineseName $chineseName
            $existingEnglishByChinese[([string]$existingRow.competition + "|" + $nameKey)] = $englishName
            $existingEnglishByChinese[("*|" + $nameKey)] = $englishName
        }
    }
}

$sourceRows = @(Import-Csv -LiteralPath $resolvedSourcePath -Encoding UTF8)
$outputRows = New-Object System.Collections.Generic.List[object]
$skippedCompetitionCount = 0
$skippedScoreCount = 0
$skippedOddsCount = 0

for ($index = 0; $index -lt $sourceRows.Count; $index++) {
    $sourceRow = $sourceRows[$index]
    $league = [string]$sourceRow.联赛
    if (-not $competitionByLeague.ContainsKey($league)) {
        $skippedCompetitionCount++
        continue
    }

    $matchDate = [datetime]$sourceRow.比赛时间
    if ($matchDate.Date -lt $StartDate.Date) {
        continue
    }

    $scoreText = [string]$sourceRow.比分
    if (-not (Test-ExactScore $scoreText)) {
        $skippedScoreCount++
        continue
    }

    $normalOdds = Get-OddsTriplet $sourceRow $false
    $handicapOdds = Get-OddsTriplet $sourceRow $true
    $hasNormalOdds = Test-CompleteOdds $normalOdds
    $hasHandicapOdds = Test-CompleteOdds $handicapOdds
    if (-not $hasNormalOdds -and -not $hasHandicapOdds) {
        $skippedOddsCount++
        continue
    }

    $competition = $competitionByLeague[$league]
    $score = Get-ScoreParts $scoreText
    $sourceRowNumber = $index + $SourceRowOffset
    $outputRows.Add([pscustomobject][ordered]@{
        match_id = "HIS-$sourceRowNumber"
        match_date = $matchDate.ToString("yyyy-MM-dd")
        competition = $competition
        home_team_cn = [string]$sourceRow.主场球队
        away_team_cn = [string]$sourceRow.客场球队
        home_team_en = Get-EnglishName $existingEnglishByChinese $competition ([string]$sourceRow.主场球队)
        away_team_en = Get-EnglishName $existingEnglishByChinese $competition ([string]$sourceRow.客场球队)
        home_score = $score[0]
        away_score = $score[1]
        neutral = $neutralCompetitions.Contains($competition).ToString().ToLowerInvariant()
        sporttery_match_number = ""
        handicap = [string]$sourceRow.让球数
        normal_win = if ($hasNormalOdds) { $normalOdds[0] } else { "" }
        normal_draw = if ($hasNormalOdds) { $normalOdds[1] } else { "" }
        normal_lose = if ($hasNormalOdds) { $normalOdds[2] } else { "" }
        handicap_win = if ($hasHandicapOdds) { $handicapOdds[0] } else { "" }
        handicap_draw = if ($hasHandicapOdds) { $handicapOdds[1] } else { "" }
        handicap_lose = if ($hasHandicapOdds) { $handicapOdds[2] } else { "" }
    })
}

$outputMatchIds = [Collections.Generic.HashSet[string]]::new()
foreach ($row in $outputRows) {
    $null = $outputMatchIds.Add([string]$row.match_id)
}
$preservedExistingCount = 0
foreach ($existingRow in $existingRows) {
    $existingMatchId = [string]$existingRow.match_id
    if ([string]::IsNullOrWhiteSpace($existingMatchId) -or $outputMatchIds.Contains($existingMatchId)) {
        continue
    }
    if (-not $supportedCompetitions.Contains([string]$existingRow.competition) `
            -or ([datetime]$existingRow.match_date).Date -lt $StartDate.Date `
            -or [string]$existingRow.home_score -notmatch "^\d+$" `
            -or [string]$existingRow.away_score -notmatch "^\d+$") {
        continue
    }
    $hasNormalOdds = -not [string]::IsNullOrWhiteSpace([string]$existingRow.normal_win) `
        -and -not [string]::IsNullOrWhiteSpace([string]$existingRow.normal_draw) `
        -and -not [string]::IsNullOrWhiteSpace([string]$existingRow.normal_lose)
    $hasHandicapOdds = -not [string]::IsNullOrWhiteSpace([string]$existingRow.handicap_win) `
        -and -not [string]::IsNullOrWhiteSpace([string]$existingRow.handicap_draw) `
        -and -not [string]::IsNullOrWhiteSpace([string]$existingRow.handicap_lose)
    if (-not $hasNormalOdds -and -not $hasHandicapOdds) {
        continue
    }
    $outputRows.Add($existingRow)
    $null = $outputMatchIds.Add($existingMatchId)
    $preservedExistingCount++
}

$duplicates = @($outputRows | Group-Object match_id | Where-Object { $_.Count -gt 1 })
if ($duplicates.Count -gt 0) {
    throw "Duplicate historical odds match IDs: $($duplicates.Name -join ', ')"
}

$outputDirectory = Split-Path -Parent $resolvedOutputPath
if (-not (Test-Path -LiteralPath $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}
$outputRows |
    Sort-Object match_date, competition, match_id |
    Export-Csv -LiteralPath $resolvedOutputPath -NoTypeInformation -Encoding UTF8

Write-Host "Imported odds rows: $($outputRows.Count)"
Write-Host "Preserved enriched or supplemental rows: $preservedExistingCount"
Write-Host "Skipped non-target competitions: $skippedCompetitionCount"
Write-Host "Skipped rows without full-time score: $skippedScoreCount"
Write-Host "Skipped rows without complete odds: $skippedOddsCount"
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
