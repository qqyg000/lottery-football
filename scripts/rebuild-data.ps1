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

$parameters = @{
    BaseUri = $BaseUri
    Competition = $Competition
}
if ($PSBoundParameters.ContainsKey("Date")) {
    $parameters.Date = $Date
}

Write-Host "The static historical CSV files are preserved; refreshing runtime data instead"
& (Join-Path $PSScriptRoot "update-history-data.ps1") @parameters
