param(
    [string]$SourceUrl = "https://raw.githubusercontent.com/martj42/international_results/master/results.csv",
    [string]$OutputPath = (Join-Path $PSScriptRoot "..\src\main\resources\data\history_matches.csv")
)

$ErrorActionPreference = "Stop"

function Save-RemoteFile {
    param(
        [string]$SourceUrl,
        [string]$OutputPath
    )

    $parameters = @{
        Uri = $SourceUrl
        OutFile = $OutputPath
    }

    if ((Get-Command Invoke-WebRequest).Parameters.ContainsKey("UseBasicParsing")) {
        $parameters["UseBasicParsing"] = $true
    }

    Invoke-WebRequest @parameters
}

function Normalize-TeamName {
    param([string]$Name)

    $trimmed = $Name.Trim()
    switch -Exact ($trimmed) {
        "Curaçao" { return "Curacao" }
        "Czech Republic" { return "Czechia" }
        default { return $trimmed }
    }
}

function Normalize-Tournament {
    param(
        [string]$Tournament,
        [string]$MatchDate
    )

    $trimmed = $Tournament.Trim()
    if ($trimmed -eq "FIFA World Cup" -and $MatchDate.Length -ge 4) {
        return "FIFA World Cup $($MatchDate.Substring(0, 4))"
    }
    return $trimmed
}

function Test-Score {
    param([string]$Value)
    return $Value -match "^\d+$"
}

function Convert-Neutral {
    param([string]$Value)

    if ($Value -match "^(?i:true|1)$") {
        return "true"
    }
    return "false"
}

function Escape-CsvValue {
    param([object]$Value)

    $text = [string]$Value
    if ($text.Contains('"')) {
        $text = $text.Replace('"', '""')
    }
    if ($text.Contains(',') -or $text.Contains('"') -or $text.Contains("`n") -or $text.Contains("`r")) {
        return '"' + $text + '"'
    }
    return $text
}

function New-HistoryRow {
    param(
        [string]$MatchDate,
        [string]$Tournament,
        [string]$HomeTeam,
        [string]$AwayTeam,
        [string]$HomeScore,
        [string]$AwayScore,
        [string]$Neutral
    )

    [pscustomobject]@{
        match_date = $MatchDate.Trim()
        tournament = Normalize-Tournament $Tournament $MatchDate
        home_team = Normalize-TeamName $HomeTeam
        away_team = Normalize-TeamName $AwayTeam
        home_score = [int]$HomeScore
        away_score = [int]$AwayScore
        neutral = Convert-Neutral $Neutral
    }
}

function Get-RowKey {
    param($Row)
    return "$($Row.match_date)|$($Row.home_team)|$($Row.away_team)"
}

$resolvedOutputPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputPath)
$outputDirectory = Split-Path -Parent $resolvedOutputPath
if (-not (Test-Path $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}

$tempFile = New-TemporaryFile
try {
    Save-RemoteFile -SourceUrl $SourceUrl -OutputPath $tempFile.FullName

    $downloadedRows = Import-Csv -Path $tempFile.FullName |
        Where-Object { (Test-Score $_.home_score) -and (Test-Score $_.away_score) } |
        ForEach-Object {
            New-HistoryRow $_.date $_.tournament $_.home_team $_.away_team $_.home_score $_.away_score $_.neutral
        }

    $rowsByKey = [ordered]@{}
    foreach ($row in $downloadedRows) {
        $rowsByKey[(Get-RowKey $row)] = $row
    }

    $downloadedCount = $rowsByKey.Count
    $keptLocalCount = 0
    if (Test-Path $resolvedOutputPath) {
        Import-Csv -Path $resolvedOutputPath |
            Where-Object { (Test-Score ([string]$_.home_score)) -and (Test-Score ([string]$_.away_score)) } |
            ForEach-Object {
                $row = New-HistoryRow $_.match_date $_.tournament $_.home_team $_.away_team $_.home_score $_.away_score $_.neutral
                $key = Get-RowKey $row
                if (-not $rowsByKey.Contains($key)) {
                    $rowsByKey[$key] = $row
                    $keptLocalCount++
                }
            }
    }

    $rows = $rowsByKey.Values | Sort-Object match_date, tournament, home_team, away_team
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("match_date,tournament,home_team,away_team,home_score,away_score,neutral")
    foreach ($row in $rows) {
        $fields = @(
            $row.match_date,
            $row.tournament,
            $row.home_team,
            $row.away_team,
            $row.home_score,
            $row.away_score,
            $row.neutral
        ) | ForEach-Object { Escape-CsvValue $_ }
        $lines.Add(($fields -join ","))
    }

    Set-Content -Path $resolvedOutputPath -Value $lines -Encoding UTF8

    Write-Host "Downloaded completed rows: $downloadedCount"
    Write-Host "Kept local-only rows: $keptLocalCount"
    Write-Host "Wrote rows: $($rows.Count)"
    Write-Host "Output: $resolvedOutputPath"
}
finally {
    Remove-Item -LiteralPath $tempFile.FullName -Force -ErrorAction SilentlyContinue
}
