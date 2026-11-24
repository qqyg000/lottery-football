[CmdletBinding()]
param(
    [string]$Directory,
    [string]$JarPath,
    [int]$Workers = 0,
    [int]$BackendParallelism = 0,
    [int]$Port = 18080,
    [ValidateSet('roi', 'score-top3')][string]$Objective = 'roi',
    [string]$RecentFrom,
    [switch]$Resume,
    [switch]$Apply
)

$ErrorActionPreference = 'Stop'
$taskRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
if (-not $Directory) {
    $Directory = Join-Path $taskRoot ('target\optimization-robust-' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
}
$taskRun = if ([IO.Path]::IsPathRooted($Directory)) { [IO.Path]::GetFullPath($Directory) } else { [IO.Path]::GetFullPath((Join-Path $taskRoot $Directory)) }
if ($taskRun -eq $taskRoot) { throw '运行快照目录不能等于项目目录' }
$taskNode = Join-Path $taskRoot 'target\node\node.exe'
$taskJar = if ($JarPath) { (Resolve-Path -LiteralPath $JarPath).Path } else { Join-Path $taskRoot 'target\lottery-football-1.0.0.jar' }
if (-not (Test-Path -LiteralPath $taskNode) -or -not (Test-Path -LiteralPath $taskJar)) {
    throw '请先在项目目录运行 mvn package，生成JAR和Node运行时'
}
$taskProcessors = [Environment]::ProcessorCount
if ($Workers -le 0) { $Workers = [Math]::Max(1, $taskProcessors - 4) }
if ($BackendParallelism -le 0) { $BackendParallelism = [Math]::Min(24, [Math]::Max(1, $taskProcessors - 4)) }
$taskTotalGiB = (Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB
$taskHeapGiB = [Math]::Max(1, [Math]::Min(20, [Math]::Floor($taskTotalGiB * 0.35)))
if (-not $Resume) {
    if (Test-Path -LiteralPath (Join-Path $taskRun 'manifest.json')) { throw '已有运行清单，请使用 -Resume 或指定新的快照目录' }
    New-Item -ItemType Directory -Path (Join-Path $taskRun 'config'), (Join-Path $taskRun 'cache') -Force | Out-Null
    Copy-Item -LiteralPath $taskJar -Destination (Join-Path $taskRun 'snapshot.jar')
    foreach ($taskFile in @('user-config.json', 'club-competition-schedules.json', 'sporttery-market-selections.json')) {
        Copy-Item -LiteralPath (Join-Path $taskRoot "config\$taskFile") -Destination (Join-Path $taskRun "config\$taskFile")
    }
}
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) { throw "端口 $Port 已被使用，请选择其他 -Port" }
$taskSnapshot = Join-Path $taskRun 'snapshot.jar'
$taskJava = (Get-Command java).Source
$taskJavaSettings = & $taskJava -XshowSettings:properties -version 2>&1 | ForEach-Object { $_.ToString() }
$taskJavaHome = $taskJavaSettings | Select-String '^\s*java\.home\s*=\s*(.+)$' | Select-Object -First 1
if ($taskJavaHome) {
    $taskJavaBinary = Join-Path $taskJavaHome.Matches[0].Groups[1].Value.Trim() 'bin\java.exe'
    if (Test-Path -LiteralPath $taskJavaBinary) { $taskJava = $taskJavaBinary }
}
$taskArguments = @("-Xmx${taskHeapGiB}g", '-Dfile.encoding=UTF-8', '-jar', ('"' + $taskSnapshot + '"'), '--server.address=127.0.0.1',
    "--server.port=$Port", "--recommendation-backtest.parallelism=$BackendParallelism",
    '--worldcup.schedule-update.enabled=false', '--worldcup.espn-update.enabled=false',
    '--champions-league.espn-update.enabled=false', '--club-competitions.schedule-update.enabled=false', '--sporttery.result-update.enabled=false')
$taskProcess = Start-Process -FilePath $taskJava -ArgumentList $taskArguments -WorkingDirectory $taskRun -WindowStyle Hidden -PassThru `
    -RedirectStandardOutput (Join-Path $taskRun 'server.out.log') -RedirectStandardError (Join-Path $taskRun 'server.err.log')
try {
    $taskReady = $false
    for ($taskAttempt = 0; $taskAttempt -lt 90; $taskAttempt++) {
        if ($taskProcess.HasExited) { throw '隔离回测服务启动失败，请查看快照目录中的server日志' }
        try {
            $null = Invoke-RestMethod "http://127.0.0.1:$Port/api/football/health" -TimeoutSec 2
            $taskReady = $true
            break
        } catch { Start-Sleep -Seconds 1 }
    }
    if (-not $taskReady) { throw '隔离服务健康检查超时' }
    $taskOptimizerArguments = @((Join-Path $PSScriptRoot 'optimize-constrained-strategies.mjs'), '--directory', $taskRun,
        '--workers', $Workers, '--base-url', "http://127.0.0.1:$Port", '--objective', $Objective)
    if ($RecentFrom) { $taskOptimizerArguments += @('--recent-from', $RecentFrom) }
    if ($Resume) { $taskOptimizerArguments += '--resume' }
    if ($Apply) { $taskOptimizerArguments += '--apply' }
    Push-Location $taskRoot
    try {
        & $taskNode @taskOptimizerArguments
        if ($LASTEXITCODE -ne 0) { throw "优化失败，退出码 $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    if (-not $taskProcess.HasExited) { Stop-Process -Id $taskProcess.Id -Force }
}
