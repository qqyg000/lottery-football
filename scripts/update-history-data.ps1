param(
    [string]$BaseUri = "http://127.0.0.1:8080",

    [ValidateSet(
        "WORLD_CUP",
        "EUROPEAN_CHAMPIONSHIP",
        "COPA_AMERICA",
        "CLUB_WORLD_CUP",
        "EUROPA_LEAGUE",
        "CHAMPIONS_LEAGUE",
        "PREMIER_LEAGUE",
        "LA_LIGA",
        "BUNDESLIGA",
        "SERIE_A",
        "LIGUE_1",
        "PRIMEIRA_LIGA",
        "EREDIVISIE",
        "ARGENTINE_PRIMERA_DIVISION",
        "SWEDISH_ALLSVENSKAN",
        "FINNISH_VEIKKAUSLIIGA",
        "K_LEAGUE_1"
    )]
    [string]$Competition = "WORLD_CUP",

    [datetime]$Date
)

$ErrorActionPreference = "Stop"

$endpoint = $BaseUri.TrimEnd("/") `
    + "/api/football/data/refresh?competition=" `
    + [Uri]::EscapeDataString($Competition)
if ($PSBoundParameters.ContainsKey("Date")) {
    $endpoint += "&date=" + $Date.ToString("yyyy-MM-dd")
}

Write-Host "Refreshing the supported competitions through: $endpoint"
$response = Invoke-RestMethod -Method Post -Uri $endpoint
$response | ConvertTo-Json -Depth 10
