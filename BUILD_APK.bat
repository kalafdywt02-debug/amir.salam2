@echo off
setlocal
cd /d "%~dp0"
echo ================================================
echo   امیر.سلام - ساخت APK بدون Android Studio
echo ================================================
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0BUILD_APK.ps1"
if errorlevel 1 (
  echo.
  echo BUILD FAILED.
  pause
  exit /b 1
)
echo.
echo APK ساخته شد:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
pause
