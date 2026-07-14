param(
    [string]$HistorySourceUrl = "https://raw.githubusercontent.com/martj42/international_results/master/results.csv",
    [string]$ScheduleSourceUrl = "https://raw.githubusercontent.com/openfootball/worldcup/master/2026--usa/cup.txt",
    [string]$DataDirectory = (Join-Path $PSScriptRoot "..\src\main\resources\data")
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

function Remove-DataFile {
    param(
        [string]$Path,
        [string]$DataRoot
    )

    $resolvedRoot = [System.IO.Path]::GetFullPath($DataRoot)
    $resolvedPath = [System.IO.Path]::GetFullPath($Path)
    if (-not $resolvedPath.StartsWith($resolvedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to delete outside data directory: $resolvedPath"
    }
    if (Test-Path -LiteralPath $resolvedPath) {
        Remove-Item -LiteralPath $resolvedPath -Force
        Write-Host "Deleted: $resolvedPath"
    }
}

function Normalize-TeamName {
    param([string]$Name)

    $cleaned = ([string]$Name).Trim() -replace "\s+", " "
    $normalized = $cleaned.Normalize([System.Text.NormalizationForm]::FormD)
    $ascii = [System.Text.RegularExpressions.Regex]::Replace($normalized, "\p{M}", "")
    switch -Exact ($ascii) {
        "USA" { return "United States" }
        "Czech Republic" { return "Czechia" }
        "Bosnia & Herzegovina" { return "Bosnia and Herzegovina" }
        "Bosnia-Herzegovina" { return "Bosnia and Herzegovina" }
        "Curacao" { return "Curacao" }
        "Cote d'Ivoire" { return "Ivory Coast" }
        "Korea Republic" { return "South Korea" }
        "IR Iran" { return "Iran" }
        "Cabo Verde" { return "Cape Verde" }
        "Congo DR" { return "DR Congo" }
        "Turkiye" { return "Turkey" }
        default { return $ascii }
    }
}

function Get-TeamNameCn {
    param([string]$TeamName)

    $teamNames = @{}
    $teamNames["Mexico"] = "墨西哥"
    $teamNames["South Africa"] = "南非"
    $teamNames["South Korea"] = "韩国"
    $teamNames["Czechia"] = "捷克"
    $teamNames["Canada"] = "加拿大"
    $teamNames["Bosnia and Herzegovina"] = "波黑"
    $teamNames["Qatar"] = "卡塔尔"
    $teamNames["Switzerland"] = "瑞士"
    $teamNames["Brazil"] = "巴西"
    $teamNames["Morocco"] = "摩洛哥"
    $teamNames["Haiti"] = "海地"
    $teamNames["Scotland"] = "苏格兰"
    $teamNames["United States"] = "美国"
    $teamNames["Paraguay"] = "巴拉圭"
    $teamNames["Australia"] = "澳大利亚"
    $teamNames["Turkey"] = "土耳其"
    $teamNames["Germany"] = "德国"
    $teamNames["Curacao"] = "库拉索"
    $teamNames["Ivory Coast"] = "科特迪瓦"
    $teamNames["Ecuador"] = "厄瓜多尔"
    $teamNames["Netherlands"] = "荷兰"
    $teamNames["Japan"] = "日本"
    $teamNames["Sweden"] = "瑞典"
    $teamNames["Tunisia"] = "突尼斯"
    $teamNames["Belgium"] = "比利时"
    $teamNames["Egypt"] = "埃及"
    $teamNames["Iran"] = "伊朗"
    $teamNames["New Zealand"] = "新西兰"
    $teamNames["Spain"] = "西班牙"
    $teamNames["Cape Verde"] = "佛得角"
    $teamNames["Saudi Arabia"] = "沙特阿拉伯"
    $teamNames["Uruguay"] = "乌拉圭"
    $teamNames["France"] = "法国"
    $teamNames["Senegal"] = "塞内加尔"
    $teamNames["Iraq"] = "伊拉克"
    $teamNames["Norway"] = "挪威"
    $teamNames["Argentina"] = "阿根廷"
    $teamNames["Algeria"] = "阿尔及利亚"
    $teamNames["Austria"] = "奥地利"
    $teamNames["Jordan"] = "约旦"
    $teamNames["Portugal"] = "葡萄牙"
    $teamNames["DR Congo"] = "刚果民主共和国"
    $teamNames["Uzbekistan"] = "乌兹别克斯坦"
    $teamNames["Colombia"] = "哥伦比亚"
    $teamNames["England"] = "英格兰"
    $teamNames["Croatia"] = "克罗地亚"
    $teamNames["Ghana"] = "加纳"
    $teamNames["Panama"] = "巴拿马"
    if ($teamNames.ContainsKey($TeamName)) {
        return $teamNames[$TeamName]
    }
    return $TeamName
}

function Convert-ToEasternDateTime {
    param(
        [datetime]$SourceDate,
        [string]$TimeText,
        [string]$OffsetText
    )

    $parts = $TimeText.Split(":")
    $offset = [System.TimeSpan]::FromHours([int]$OffsetText)
    $dateTimeOffset = [System.DateTimeOffset]::new(
        $SourceDate.Year,
        $SourceDate.Month,
        $SourceDate.Day,
        [int]$parts[0],
        [int]$parts[1],
        0,
        $offset
    )
    $targetZone = [System.TimeZoneInfo]::FindSystemTimeZoneById("Eastern Standard Time")
    return [System.TimeZoneInfo]::ConvertTime($dateTimeOffset, $targetZone)
}

function Test-ScoreValue {
    param([string]$Value)
    return $Value -match "^\d+$"
}

function Get-ScheduleQueryDate {
    param(
        [string]$MatchDate,
        [string]$KickoffTime
    )

    $date = [datetime]::ParseExact($MatchDate, "yyyy-MM-dd", [System.Globalization.CultureInfo]::InvariantCulture)
    if ($KickoffTime -eq "00:00") {
        return $date.AddDays(-1).ToString("yyyy-MM-dd")
    }
    return $date.ToString("yyyy-MM-dd")
}

function Get-FixtureKey {
    param(
        [string]$MatchDate,
        [string]$HomeTeam,
        [string]$AwayTeam
    )

    $normalizedHomeTeam = (Normalize-TeamName $HomeTeam).ToLowerInvariant()
    $normalizedAwayTeam = (Normalize-TeamName $AwayTeam).ToLowerInvariant()
    return "$MatchDate|$normalizedHomeTeam|$normalizedAwayTeam"
}

function Update-ScheduleRowsFromHistory {
    param(
        [System.Collections.Generic.List[object]]$Rows,
        [string]$HistoryPath
    )

    if ([string]::IsNullOrWhiteSpace($HistoryPath) -or -not (Test-Path -LiteralPath $HistoryPath)) {
        return 0
    }

    $historyByFixture = @{}
    Import-Csv -Path $HistoryPath |
        Where-Object {
            $_.tournament -match "World Cup" `
                -and $_.tournament -match "2026" `
                -and (Test-ScoreValue $_.home_score) `
                -and (Test-ScoreValue $_.away_score)
        } |
        ForEach-Object {
            $historyByFixture[(Get-FixtureKey $_.match_date $_.home_team $_.away_team)] = $_
        }

    $updatedCount = 0
    foreach ($row in $Rows) {
        $queryDate = Get-ScheduleQueryDate $row.match_date $row.kickoff_time
        $history = $historyByFixture[(Get-FixtureKey $queryDate $row.home_team_en $row.away_team_en)]
        $reversed = $false
        if ($null -eq $history) {
            $history = $historyByFixture[(Get-FixtureKey $queryDate $row.away_team_en $row.home_team_en)]
            $reversed = $null -ne $history
        }
        if ($null -eq $history) {
            continue
        }

        $row.status = "COMPLETED"
        if ($reversed) {
            $row.home_score = $history.away_score
            $row.away_score = $history.home_score
        } else {
            $row.home_score = $history.home_score
            $row.away_score = $history.away_score
        }
        $updatedCount++
    }

    return $updatedCount
}

function Add-ScheduledFixture {
    param(
        [System.Collections.Generic.List[object]]$Rows,
        [int]$Index,
        [string]$MatchDate,
        [string]$KickoffTime,
        [string]$Stage,
        [string]$HomeTeam,
        [string]$AwayTeam,
        [string]$Venue
    )

    $normalizedHomeTeam = Normalize-TeamName $HomeTeam
    $normalizedAwayTeam = Normalize-TeamName $AwayTeam
    $Rows.Add([pscustomobject]@{
        match_id = "OF2026-{0:D3}" -f $Index
        match_date = $MatchDate
        kickoff_time = $KickoffTime
        group_name = $Stage
        home_team_cn = Get-TeamNameCn $normalizedHomeTeam
        away_team_cn = Get-TeamNameCn $normalizedAwayTeam
        home_team_en = $normalizedHomeTeam
        away_team_en = $normalizedAwayTeam
        venue = $Venue
        neutral = "true"
        status = "SCHEDULED"
        home_score = ""
        away_score = ""
    })
}

function Add-RoundOf32Fixtures {
    param([System.Collections.Generic.List[object]]$Rows)

    $stage = "32强淘汰赛"
    $fixtures = @(
        @{ Index = 73; MatchDate = "2026-06-28"; KickoffTime = "15:00"; HomeTeam = "South Africa"; AwayTeam = "Canada"; Venue = "Los Angeles (Inglewood)" },
        @{ Index = 74; MatchDate = "2026-06-29"; KickoffTime = "16:30"; HomeTeam = "Germany"; AwayTeam = "Paraguay"; Venue = "Boston (Foxborough)" },
        @{ Index = 75; MatchDate = "2026-06-29"; KickoffTime = "21:00"; HomeTeam = "Netherlands"; AwayTeam = "Morocco"; Venue = "Monterrey (Guadalupe)" },
        @{ Index = 76; MatchDate = "2026-06-29"; KickoffTime = "13:00"; HomeTeam = "Brazil"; AwayTeam = "Japan"; Venue = "Houston" },
        @{ Index = 77; MatchDate = "2026-06-30"; KickoffTime = "17:00"; HomeTeam = "France"; AwayTeam = "Sweden"; Venue = "New York/New Jersey (East Rutherford)" },
        @{ Index = 78; MatchDate = "2026-06-30"; KickoffTime = "13:00"; HomeTeam = "Ivory Coast"; AwayTeam = "Norway"; Venue = "Dallas (Arlington)" },
        @{ Index = 79; MatchDate = "2026-06-30"; KickoffTime = "21:00"; HomeTeam = "Mexico"; AwayTeam = "Ecuador"; Venue = "Mexico City" },
        @{ Index = 80; MatchDate = "2026-07-01"; KickoffTime = "12:00"; HomeTeam = "England"; AwayTeam = "DR Congo"; Venue = "Atlanta" },
        @{ Index = 81; MatchDate = "2026-07-01"; KickoffTime = "20:00"; HomeTeam = "United States"; AwayTeam = "Bosnia and Herzegovina"; Venue = "San Francisco Bay Area (Santa Clara)" },
        @{ Index = 82; MatchDate = "2026-07-01"; KickoffTime = "16:00"; HomeTeam = "Belgium"; AwayTeam = "Senegal"; Venue = "Seattle" },
        @{ Index = 83; MatchDate = "2026-07-02"; KickoffTime = "19:00"; HomeTeam = "Portugal"; AwayTeam = "Croatia"; Venue = "Toronto" },
        @{ Index = 84; MatchDate = "2026-07-02"; KickoffTime = "15:00"; HomeTeam = "Spain"; AwayTeam = "Austria"; Venue = "Los Angeles (Inglewood)" },
        @{ Index = 85; MatchDate = "2026-07-02"; KickoffTime = "23:00"; HomeTeam = "Switzerland"; AwayTeam = "Algeria"; Venue = "Vancouver" },
        @{ Index = 86; MatchDate = "2026-07-03"; KickoffTime = "18:00"; HomeTeam = "Argentina"; AwayTeam = "Cape Verde"; Venue = "Miami (Miami Gardens)" },
        @{ Index = 87; MatchDate = "2026-07-03"; KickoffTime = "21:30"; HomeTeam = "Colombia"; AwayTeam = "Ghana"; Venue = "Kansas City" },
        @{ Index = 88; MatchDate = "2026-07-03"; KickoffTime = "14:00"; HomeTeam = "Australia"; AwayTeam = "Egypt"; Venue = "Dallas (Arlington)" }
    )

    foreach ($fixture in $fixtures) {
        Add-ScheduledFixture `
            -Rows $Rows `
            -Index $fixture.Index `
            -MatchDate $fixture.MatchDate `
            -KickoffTime $fixture.KickoffTime `
            -Stage $stage `
            -HomeTeam $fixture.HomeTeam `
            -AwayTeam $fixture.AwayTeam `
            -Venue $fixture.Venue
    }
}

function New-ScheduleCsv {
    param(
        [string]$SourceUrl,
        [string]$OutputPath,
        [string]$HistoryPath
    )

    $tempFile = New-TemporaryFile
    try {
        Save-RemoteFile -SourceUrl $SourceUrl -OutputPath $tempFile.FullName
        $lines = Get-Content -Path $tempFile.FullName -Encoding UTF8

        $groupByTeam = @{}
        foreach ($line in $lines) {
            if ($line -match "^Group\s+([A-L])\s+\|\s+(.+)$") {
                $group = $Matches[1]
                foreach ($team in ($Matches[2] -split "\s{2,}")) {
                    if (-not [string]::IsNullOrWhiteSpace($team)) {
                        $groupByTeam[(Normalize-TeamName $team)] = $group
                    }
                }
            }
        }

        $rows = New-Object System.Collections.Generic.List[object]
        $currentGroup = $null
        $currentDate = $null
        $index = 1

        foreach ($line in $lines) {
            $trimmed = $line.Trim()
            if ($trimmed -match "^(?:▪\s*)?Group\s+([A-L])$") {
                $currentGroup = $Matches[1]
                continue
            }
            if ($trimmed -match "^(?:Mon|Tue|Wed|Thu|Fri|Sat|Sun)\s+([A-Za-z]+)\s+(\d{1,2})$") {
                $currentDate = [datetime]::ParseExact("2026 $($Matches[1]) $($Matches[2])", "yyyy MMMM d", [System.Globalization.CultureInfo]::InvariantCulture)
                continue
            }
            if ($null -eq $currentDate) {
                continue
            }
            if ($line -notmatch "^\s*(\d{1,2}:\d{2})\s+UTC([+-]\d{1,2})\s+(.+?)\s+@\s+(.+?)\s*$") {
                continue
            }

            $timeText = $Matches[1]
            $offsetText = $Matches[2]
            $matchText = $Matches[3].Trim()
            $venue = $Matches[4].Trim()
            $completed = $false
            $homeScore = ""
            $awayScore = ""
            $homeTeam = $null
            $awayTeam = $null

            if ($matchText -match "^(.+?)\s+(\d+)-(\d+)(?:\s+\([^)]+\))?\s+(.+?)$") {
                $homeTeam = Normalize-TeamName $Matches[1]
                $homeScore = $Matches[2]
                $awayScore = $Matches[3]
                $awayTeam = Normalize-TeamName $Matches[4]
                $completed = $true
            } elseif ($matchText -match "^(.+?)\s+v\s+(.+?)$") {
                $homeTeam = Normalize-TeamName $Matches[1]
                $awayTeam = Normalize-TeamName $Matches[2]
            } else {
                continue
            }

            $targetDateTime = Convert-ToEasternDateTime $currentDate $timeText $offsetText
            $group = if ($currentGroup) { $currentGroup } else { $groupByTeam[$homeTeam] }
            $status = if ($completed) { "COMPLETED" } else { "SCHEDULED" }
            $rows.Add([pscustomobject]@{
                match_id = "OF2026-{0:D3}" -f $index
                match_date = $targetDateTime.DateTime.ToString("yyyy-MM-dd")
                kickoff_time = $targetDateTime.DateTime.ToString("HH:mm")
                group_name = "${group}组"
                home_team_cn = Get-TeamNameCn $homeTeam
                away_team_cn = Get-TeamNameCn $awayTeam
                home_team_en = $homeTeam
                away_team_en = $awayTeam
                venue = $venue
                neutral = "true"
                status = $status
                home_score = $homeScore
                away_score = $awayScore
            })
            $index++
        }

        if ($rows.Count -ne 72) {
            throw "Expected 72 schedule rows from OpenFootball, got $($rows.Count)."
        }
        Add-RoundOf32Fixtures $rows
        $historyBackfillCount = Update-ScheduleRowsFromHistory -Rows $rows -HistoryPath $HistoryPath

        $csvLines = New-Object System.Collections.Generic.List[string]
        $csvLines.Add("match_id,match_date,kickoff_time,group_name,home_team_cn,away_team_cn,home_team_en,away_team_en,venue,neutral,status,home_score,away_score")
        foreach ($row in ($rows | Sort-Object match_date, kickoff_time, match_id)) {
            $fields = @(
                $row.match_id,
                $row.match_date,
                $row.kickoff_time,
                $row.group_name,
                $row.home_team_cn,
                $row.away_team_cn,
                $row.home_team_en,
                $row.away_team_en,
                $row.venue,
                $row.neutral,
                $row.status,
                $row.home_score,
                $row.away_score
            ) | ForEach-Object { Escape-CsvValue $_ }
            $csvLines.Add(($fields -join ","))
        }

        Set-Content -Path $OutputPath -Value $csvLines -Encoding UTF8
        Write-Host "Downloaded schedule rows: $($rows.Count)"
        Write-Host "Backfilled schedule results from history: $historyBackfillCount"
        Write-Host "Output: $OutputPath"
    }
    finally {
        Remove-Item -LiteralPath $tempFile.FullName -Force -ErrorAction SilentlyContinue
    }
}

$resolvedDataDirectory = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($DataDirectory)
if (-not (Test-Path -LiteralPath $resolvedDataDirectory)) {
    New-Item -ItemType Directory -Path $resolvedDataDirectory | Out-Null
}

$historyPath = Join-Path $resolvedDataDirectory "history_matches.csv"
$schedulePath = Join-Path $resolvedDataDirectory "schedule_2026.csv"

Remove-DataFile $historyPath $resolvedDataDirectory
Remove-DataFile $schedulePath $resolvedDataDirectory

& (Join-Path $PSScriptRoot "update-history-data.ps1") -SourceUrl $HistorySourceUrl -OutputPath $historyPath
New-ScheduleCsv -SourceUrl $ScheduleSourceUrl -OutputPath $schedulePath -HistoryPath $historyPath


