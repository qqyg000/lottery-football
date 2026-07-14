@echo off
setlocal
cd /d "%~dp0"

echo [1/5] Check Maven...
where mvn >nul 2>nul
if errorlevel 1 (
  echo ERROR: Maven is not found in PATH.
  echo Please install Maven and add Maven bin directory to PATH.
  pause
  exit /b 1
)

echo [2/5] Stop running app...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$root=(Resolve-Path '.').Path; Get-CimInstance Win32_Process | Where-Object { $_.Name -match '^java(w)?\.exe$' -and $_.CommandLine -like '*lottery-football-1.0.0.jar*' -and $_.CommandLine -like ('*' + $root + '*') } | ForEach-Object { Write-Host ('Stopping PID ' + $_.ProcessId); Stop-Process -Id $_.ProcessId -Force }"
if errorlevel 1 (
  echo WARN: Failed to check or stop a running app process.
  echo Please close any running lottery-football window if Maven clean fails.
)

echo [3/5] Build backend and frontend...
call mvn -DskipTests clean package
if errorlevel 1 (
  echo ERROR: Maven build failed.
  pause
  exit /b 1
)

echo [4/5] Copy files...
if not exist "target\lottery-football-1.0.0.jar" (
  echo ERROR: target\lottery-football-1.0.0.jar not found.
  pause
  exit /b 1
)

if not exist "target\dist" mkdir "target\dist"
copy /Y "target\lottery-football-1.0.0.jar" "target\dist\lottery-football-1.0.0.jar" >nul
if errorlevel 1 (
  echo ERROR: Failed to copy jar file.
  pause
  exit /b 1
)

copy /Y "run.cmd" "target\dist\run.cmd" >nul
if errorlevel 1 (
  echo ERROR: Failed to copy run.cmd.
  pause
  exit /b 1
)

echo [5/5] Done.
echo Output directory: target\dist
echo Double click target\dist\run.cmd to start the program.
pause
endlocal
