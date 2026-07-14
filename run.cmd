@echo off
setlocal
cd /d "%~dp0"

set "APP_JAR=lottery-football-1.0.0.jar"
set "JAR_PATH=%~dp0%APP_JAR%"

if not exist "%JAR_PATH%" (
  set "JAR_PATH=%~dp0target\%APP_JAR%"
)

if not exist "%JAR_PATH%" (
  echo ERROR: %APP_JAR% not found.
  echo Please run build.cmd first, or put run.cmd and the jar file in the same directory.
  pause
  exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java is not found in PATH.
  echo Please install JDK 17 or later and add Java bin directory to PATH.
  pause
  exit /b 1
)

start "lottery-football" cmd /k java -Dfile.encoding=UTF-8 -jar "%JAR_PATH%"
timeout /t 3 /nobreak >nul
start "" "http://127.0.0.1:8080"
endlocal
