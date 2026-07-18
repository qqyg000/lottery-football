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
        "SERIE_A",
        "BUNDESLIGA",
        "LIGUE_1",
        "BRAZIL_SERIE_A",
        "PRIMEIRA_LIGA",
        "EREDIVISIE",
        "ARGENTINE_PRIMERA_DIVISION"
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
