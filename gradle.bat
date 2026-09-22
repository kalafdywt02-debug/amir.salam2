@echo off
setlocal EnableExtensions
set "ROOT=%~dp0..\.."
set "TOOLS=%ROOT%"
set "VER=8.9"
set "ZIP=%TOOLS%\gradle-%VER%-bin.zip"
set "REAL=%TOOLS%\gradle-%VER%\bin\gradle.bat"
if exist "%REAL%" (
  call "%REAL%" %*
  exit /b %ERRORLEVEL%
)
set "DL=https://services.gradle.org/distributions/gradle-%VER%-bin.zip"
echo Gradle %VER% is not installed locally.
echo Downloading the official Gradle distribution...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri '%DL%' -OutFile '%ZIP%'"
if errorlevel 1 exit /b 1
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TOOLS%' -Force"
if errorlevel 1 exit /b 1
if not exist "%REAL%" (
  echo Gradle extraction failed: %REAL% not found.
  exit /b 2
)
del /q "%ZIP%" >nul 2>&1
call "%REAL%" %*
exit /b %ERRORLEVEL%
