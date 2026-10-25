[CmdletBinding()]
param([int]$Port = 8080)

$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$taskJar = Join-Path $taskRoot 'target\lottery-football-1.0.0.jar'
if (-not (Test-Path -LiteralPath $taskJar)) { throw '请先执行 mvn package，构建成功后再重启' }
$taskExisting = Get-CimInstance Win32_Process | Where-Object {
    $_.Name -match '^java(w)?\.exe$' -and $_.CommandLine -like '*lottery-football-1.0.0.jar*' -and $_.CommandLine.Contains($taskRoot)
}
foreach ($taskApp in $taskExisting) {
    Write-Host "停止本项目服务 PID $($taskApp.ProcessId)"
    Stop-Process -Id $taskApp.ProcessId -Force
}
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) { throw "端口 $Port 由其他程序占用，未启动服务" }
$taskDist = Join-Path $taskRoot 'target\dist'
New-Item -ItemType Directory -Path $taskDist, (Join-Path $taskDist 'config'), (Join-Path $taskRoot 'logs') -Force | Out-Null
Copy-Item -LiteralPath $taskJar, (Join-Path $taskRoot 'run.cmd') -Destination $taskDist
foreach ($taskFile in @('user-config.json', 'club-competition-schedules.json', 'sporttery-market-selections.json')) {
    Copy-Item -LiteralPath (Join-Path $taskRoot "config\$taskFile") -Destination (Join-Path $taskDist "config\$taskFile")
}
$taskStamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$taskProcess = Start-Process -FilePath (Get-Command java).Source -WorkingDirectory $taskRoot -WindowStyle Hidden -PassThru `
    -ArgumentList '-Xms512m', '-Xmx8g', '-Dfile.encoding=UTF-8', '-jar', ('"' + $taskJar + '"'), "--server.port=$Port" `
    -RedirectStandardOutput (Join-Path $taskRoot "logs\service-$taskStamp.out.log") -RedirectStandardError (Join-Path $taskRoot "logs\service-$taskStamp.err.log")
$taskProcess.Id | Set-Content -LiteralPath (Join-Path $taskRoot 'target\service.pid')
$taskReady = $false
for ($taskAttempt = 0; $taskAttempt -lt 120; $taskAttempt++) {
    if ($taskProcess.HasExited) { throw "服务启动失败，请查看 logs\service-$taskStamp.err.log" }
    try {
        $taskHealth = Invoke-RestMethod "http://127.0.0.1:$Port/api/football/health" -TimeoutSec 2
        $taskReady = $true
        break
    } catch { Start-Sleep -Seconds 1 }
}
if (-not $taskReady) { throw '服务健康检查超时，进程和日志已保留供排查' }
Write-Host "服务已启动，PID $($taskProcess.Id)，http://127.0.0.1:$Port"
$taskHealth | ConvertTo-Json -Compress
