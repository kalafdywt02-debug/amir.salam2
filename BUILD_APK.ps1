param([switch]$Install)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$gradleVersion = '8.9'
$toolsDir = Join-Path $PSScriptRoot '.build-tools'
$gradleExe = Join-Path $toolsDir "gradle-$gradleVersion\bin\gradle.bat"

if (-not (Test-Path $gradleExe)) {
  throw "Gradle launcher not found: $gradleExe"
}

Write-Host 'در حال Build کردن APK...' -ForegroundColor Cyan
& $gradleExe --no-daemon --stacktrace assembleDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $apk)) { throw 'APK ساخته نشد.' }
Write-Host "`nAPK آماده است: $apk" -ForegroundColor Green

if ($Install) {
  $adbCandidates = @(
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    "$env:LOCALAPPDATA\Android\Sdk"
  ) | Where-Object { $_ -and (Test-Path $_) } | ForEach-Object { Join-Path $_ 'platform-tools\adb.exe' }
  $adb = $adbCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
  if (-not $adb) {
    Write-Host 'adb پیدا نشد؛ APK ساخته شده ولی نصب خودکار انجام نشد.' -ForegroundColor Yellow
    exit 0
  }
  & $adb start-server | Out-Null
  $devices = & $adb devices
  if ($devices -notmatch "\n[^\s]+\s+device") {
    Write-Host 'هیچ گوشی با ADB مجاز پیدا نشد.' -ForegroundColor Yellow
    Write-Host 'USB Debugging را فعال کنید، گوشی را وصل کنید و اجازه RSA را تأیید کنید.'
    exit 0
  }
  Write-Host 'در حال نصب APK...' -ForegroundColor Cyan
  & $adb install -r $apk
  if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
  Write-Host 'نصب با موفقیت انجام شد.' -ForegroundColor Green
}
