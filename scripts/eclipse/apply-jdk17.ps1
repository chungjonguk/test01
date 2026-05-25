# Eclipse .settings — JDK 17 경로를 로컬 dev-tools 에 맞게 기록
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent),
    [string]$JdkHome = ""
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $JdkHome) {
    $JdkHome = $env:JAVA_HOME
}
if (-not $JdkHome -or -not (Test-Path $JdkHome)) {
    $JdkHome = Join-Path $env:USERPROFILE ".local\dev\jdk-17"
}
if (-not (Test-Path $JdkHome)) {
    Write-Host "[오류] JDK 17을 찾지 못했습니다. -JdkHome 또는 JAVA_HOME을 지정하세요." -ForegroundColor Red
    exit 1
}

$jdkPath = (Resolve-Path $JdkHome).Path -replace '\\', '/'
$settingsDir = Join-Path $ProjectRoot ".settings"
New-Item -ItemType Directory -Force -Path $settingsDir | Out-Null

$vmXml = "<?xml version\=`"1.0\`" encoding\=`"UTF-8\`" standalone\=`"no\`"?><vmConfig defaultVM\=`"57,0,17,0\`" vmType\=`"0\`" vmVersion\=`"0\`"><vmInstall id\=`"57,0,17,0\`" name\=`"jdk-17\`" path\=`"$jdkPath\`"/></vmConfig>"

$prefs = @"
eclipse.preferences.version=1
org.eclipse.jdt.launching.PREF_VM_XML=$vmXml
org.eclipse.jdt.launching.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE=warning
"@

$prefsPath = Join-Path $settingsDir "org.eclipse.jdt.launching.prefs"
[System.IO.File]::WriteAllText($prefsPath, $prefs + [Environment]::NewLine, [System.Text.UTF8Encoding]::new($false))

Write-Host "JDK 17 적용: $jdkPath" -ForegroundColor Green
Write-Host "  -> $prefsPath" -ForegroundColor Gray
