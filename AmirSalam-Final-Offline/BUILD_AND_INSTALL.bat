@echo off
setlocal
cd /d "%~dp0"
echo ================================================
echo   امیر.سلام - ساخت و نصب APK
echo ================================================
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0BUILD_APK.ps1" -Install
if errorlevel 1 (
  echo.
  echo BUILD/INSTALL FAILED.
  pause
  exit /b 1
)
echo.
echo تمام شد. برنامه روی دستگاه نصب شد، اگر ADB مجاز بوده باشد.
pause
