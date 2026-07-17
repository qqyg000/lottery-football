param(
    [Parameter(Mandatory = $true)]
    [string]$SourcePath,

    [string]$ClubHistoryPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\club_history_matches.csv"),

    [string]$OutputPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\historical_odds.csv"),

    [datetime]$StartDate = [datetime]"2022-11-20",

    [int]$SourceRowOffset = 4
)

$ErrorActionPreference = "Stop"

function Test-OddsTriplet {
    param(
        [object]$Row,
        [string]$Prefix
    )

    $winProperty = "$Prefix-胜"
    $drawProperty = "$Prefix-平"
    $loseProperty = "$Prefix-负"
    return -not [string]::IsNullOrWhiteSpace([string]$Row.$winProperty) `
        -and -not [string]::IsNullOrWhiteSpace([string]$Row.$drawProperty) `
        -and -not [string]::IsNullOrWhiteSpace([string]$Row.$loseProperty)
}

function Test-ExactScore {
    param([string]$Score)

    return $Score -match "^\d+:\d+$"
}

function Get-ScoreParts {
    param([string]$Score)

    $parts = $Score.Split(":")
    return @([int]$parts[0], [int]$parts[1])
}

function Test-LeagueMatch {
    param(
        [string]$SourceLeague,
        [string]$MappedLeague
    )

    if ($SourceLeague -eq $MappedLeague) {
        return $true
    }
    return $MappedLeague -eq "美职" -and $SourceLeague -eq "美职足"
}

function New-HistoricalOddsRow {
    param(
        [object]$SourceRow,
        [int]$SourceRowNumber,
        [string]$Competition,
        [string]$GroupName,
        [string]$HomeTeamCn,
        [string]$AwayTeamCn,
        [string]$HomeTeamEn,
        [string]$AwayTeamEn,
        [bool]$Neutral,
        [string]$SportteryMatchNumber
    )

    $score = Get-ScoreParts ([string]$SourceRow.比分)
    $hasNormalOdds = Test-OddsTriplet $SourceRow "胜平负初盘赔率"
    $hasHandicapOdds = Test-OddsTriplet $SourceRow "让球胜平负初盘赔率"
    return [pscustomobject][ordered]@{
        match_id = "HIS-$SourceRowNumber"
        match_date = [string]$SourceRow.比赛时间
        competition = $Competition
        group_name = $GroupName
        home_team_cn = $HomeTeamCn
        away_team_cn = $AwayTeamCn
        home_team_en = $HomeTeamEn
        away_team_en = $AwayTeamEn
        home_score = $score[0]
        away_score = $score[1]
        neutral = $Neutral.ToString().ToLowerInvariant()
        sporttery_match_number = $SportteryMatchNumber
        handicap = [string]$SourceRow.让球数
        normal_win = if ($hasNormalOdds) { [string]$SourceRow.'胜平负初盘赔率-胜' } else { "" }
        normal_draw = if ($hasNormalOdds) { [string]$SourceRow.'胜平负初盘赔率-平' } else { "" }
        normal_lose = if ($hasNormalOdds) { [string]$SourceRow.'胜平负初盘赔率-负' } else { "" }
        handicap_win = if ($hasHandicapOdds) { [string]$SourceRow.'让球胜平负初盘赔率-胜' } else { "" }
        handicap_draw = if ($hasHandicapOdds) { [string]$SourceRow.'让球胜平负初盘赔率-平' } else { "" }
        handicap_lose = if ($hasHandicapOdds) { [string]$SourceRow.'让球胜平负初盘赔率-负' } else { "" }
        source_row = $SourceRowNumber
    }
}

$nationalTeams = @{
    "阿根廷" = @{ Cn = "阿根廷"; En = "Argentina" }
    "澳大利亚" = @{ Cn = "澳大利亚"; En = "Australia" }
    "巴西" = @{ Cn = "巴西"; En = "Brazil" }
    "比利时" = @{ Cn = "比利时"; En = "Belgium" }
    "波兰" = @{ Cn = "波兰"; En = "Poland" }
    "丹麦" = @{ Cn = "丹麦"; En = "Denmark" }
    "德国" = @{ Cn = "德国"; En = "Germany" }
    "厄瓜多尔" = @{ Cn = "厄瓜多尔"; En = "Ecuador" }
    "法国" = @{ Cn = "法国"; En = "France" }
    "哥斯达" = @{ Cn = "哥斯达黎加"; En = "Costa Rica" }
    "韩国" = @{ Cn = "韩国"; En = "South Korea" }
    "荷兰" = @{ Cn = "荷兰"; En = "Netherlands" }
    "加拿大" = @{ Cn = "加拿大"; En = "Canada" }
    "加纳" = @{ Cn = "加纳"; En = "Ghana" }
    "喀麦隆" = @{ Cn = "喀麦隆"; En = "Cameroon" }
    "卡塔尔" = @{ Cn = "卡塔尔"; En = "Qatar" }
    "克罗地亚" = @{ Cn = "克罗地亚"; En = "Croatia" }
    "美国" = @{ Cn = "美国"; En = "United States" }
    "摩洛哥" = @{ Cn = "摩洛哥"; En = "Morocco" }
    "墨西哥" = @{ Cn = "墨西哥"; En = "Mexico" }
    "葡萄牙" = @{ Cn = "葡萄牙"; En = "Portugal" }
    "日本" = @{ Cn = "日本"; En = "Japan" }
    "瑞士" = @{ Cn = "瑞士"; En = "Switzerland" }
    "塞尔维亚" = @{ Cn = "塞尔维亚"; En = "Serbia" }
    "塞内加尔" = @{ Cn = "塞内加尔"; En = "Senegal" }
    "沙特" = @{ Cn = "沙特阿拉伯"; En = "Saudi Arabia" }
    "突尼斯" = @{ Cn = "突尼斯"; En = "Tunisia" }
    "威尔士" = @{ Cn = "威尔士"; En = "Wales" }
    "乌拉圭" = @{ Cn = "乌拉圭"; En = "Uruguay" }
    "西班牙" = @{ Cn = "西班牙"; En = "Spain" }
    "伊朗" = @{ Cn = "伊朗"; En = "Iran" }
    "英格兰" = @{ Cn = "英格兰"; En = "England" }
}

$resolvedSourcePath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($SourcePath)
$resolvedClubHistoryPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($ClubHistoryPath)
$resolvedOutputPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
if (-not (Test-Path -LiteralPath $resolvedSourcePath)) {
    throw "Historical odds source not found: $resolvedSourcePath"
}
if (-not (Test-Path -LiteralPath $resolvedClubHistoryPath)) {
    throw "Club history mapping not found: $resolvedClubHistoryPath"
}

$sourceRows = @(Import-Csv -LiteralPath $resolvedSourcePath)
$clubHistoryRows = @(Import-Csv -LiteralPath $resolvedClubHistoryPath -Encoding UTF8)
$outputRows = New-Object System.Collections.Generic.List[object]
$clubCount = 0
$worldCupCount = 0

foreach ($clubRow in $clubHistoryRows) {
    if ([datetime]$clubRow.match_date -lt $StartDate) {
        continue
    }
    $sourceRowNumber = [int]$clubRow.source_row
    $sourceIndex = $sourceRowNumber - $SourceRowOffset
    if ($sourceIndex -lt 0 -or $sourceIndex -ge $sourceRows.Count) {
        throw "Source row $sourceRowNumber is outside the input CSV"
    }
    $sourceRow = $sourceRows[$sourceIndex]
    if (-not (Test-ExactScore ([string]$sourceRow.比分))) {
        throw "Source row $sourceRowNumber does not contain an exact score"
    }
    $score = Get-ScoreParts ([string]$sourceRow.比分)
    $matchesMapping = [string]$sourceRow.比赛时间 -eq [string]$clubRow.match_date `
        -and (Test-LeagueMatch ([string]$sourceRow.联赛) ([string]$clubRow.source_league)) `
        -and $score[0] -eq [int]$clubRow.home_score `
        -and $score[1] -eq [int]$clubRow.away_score
    if (-not $matchesMapping) {
        throw "Source row $sourceRowNumber does not match club history mapping"
    }
    if (-not (Test-OddsTriplet $sourceRow "胜平负初盘赔率") `
            -and -not (Test-OddsTriplet $sourceRow "让球胜平负初盘赔率")) {
        continue
    }
    $outputRows.Add((New-HistoricalOddsRow `
        -SourceRow $sourceRow `
        -SourceRowNumber $sourceRowNumber `
        -Competition ([string]$clubRow.competition) `
        -GroupName ([string]$clubRow.source_league) `
        -HomeTeamCn ([string]$clubRow.home_team_cn) `
        -AwayTeamCn ([string]$clubRow.away_team_cn) `
        -HomeTeamEn "" `
        -AwayTeamEn "" `
        -Neutral ([bool]::Parse([string]$clubRow.neutral)) `
        -SportteryMatchNumber ([string]$clubRow.source_match_number)))
    $clubCount++
}

for ($index = 0; $index -lt $sourceRows.Count; $index++) {
    $sourceRow = $sourceRows[$index]
    if ([string]$sourceRow.联赛 -ne "世界杯" `
            -or [datetime]$sourceRow.比赛时间 -lt $StartDate `
            -or -not (Test-ExactScore ([string]$sourceRow.比分))) {
        continue
    }
    if (-not (Test-OddsTriplet $sourceRow "胜平负初盘赔率") `
            -and -not (Test-OddsTriplet $sourceRow "让球胜平负初盘赔率")) {
        continue
    }
    $homeTeam = $nationalTeams[[string]$sourceRow.主场球队]
    $awayTeam = $nationalTeams[[string]$sourceRow.客场球队]
    if ($null -eq $homeTeam -or $null -eq $awayTeam) {
        throw "Missing World Cup team mapping for $($sourceRow.主场球队) vs $($sourceRow.客场球队)"
    }
    $sourceRowNumber = $index + $SourceRowOffset
    $outputRows.Add((New-HistoricalOddsRow `
        -SourceRow $sourceRow `
        -SourceRowNumber $sourceRowNumber `
        -Competition "WORLD_CUP" `
        -GroupName "世界杯" `
        -HomeTeamCn ([string]$homeTeam.Cn) `
        -AwayTeamCn ([string]$awayTeam.Cn) `
        -HomeTeamEn ([string]$homeTeam.En) `
        -AwayTeamEn ([string]$awayTeam.En) `
        -Neutral $true `
        -SportteryMatchNumber ""))
    $worldCupCount++
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
    Sort-Object match_date, competition, source_row |
    Export-Csv -LiteralPath $resolvedOutputPath -NoTypeInformation -Encoding UTF8

Write-Host "Imported club odds rows: $clubCount"
Write-Host "Imported World Cup odds rows: $worldCupCount"
Write-Host "Imported total odds rows: $($outputRows.Count)"
Write-Host "Output: $resolvedOutputPath"
