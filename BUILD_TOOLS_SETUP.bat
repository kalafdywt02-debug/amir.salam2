@echo off
setlocal
cd /d "%~dp0"
call ".build-tools\gradle-8.9\bin\gradle.bat" --version
if errorlevel 1 (
  echo.
  echo Gradle setup failed.
  pause
  exit /b 1
)
echo.
echo Gradle 8.9 is ready.
pause
