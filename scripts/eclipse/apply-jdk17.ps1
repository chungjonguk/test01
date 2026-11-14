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

$vmXml = "<?xml version\=`"1.0\`" encoding\=`"UTF-8\`" standalone\=`"no\`"?><vmSettings defaultVM\=`"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType,jdk-17\`" defaultVMConnector\=`"`"><vmType id\=`"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\`"><vm id\=`"jdk-17\`" name\=`"jdk-17\`" path\=`"$jdkPath\`"/></vmType></vmSettings>"
$eeXml = "<?xml version\=`"1.0\`" encoding\=`"UTF-8\`" standalone\=`"no\`"?><defaultEnvironments><defaultEnvironment id\=`"JavaSE-17\`" vm\=`"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType,jdk-17\`"/></defaultEnvironments>"

$prefs = @"
eclipse.preferences.version=1
org.eclipse.jdt.launching.PREF_VM_XML=$vmXml
org.eclipse.jdt.launching.PREF_DEFAULT_ENVIRONMENTS_XML=$eeXml
org.eclipse.jdt.launching.PREF_STRICTLY_COMPATIBLE_JRE_NOT_AVAILABLE=warning
"@

$utf8 = [System.Text.UTF8Encoding]::new($false)
$prefsPath = Join-Path $settingsDir "org.eclipse.jdt.launching.prefs"
[System.IO.File]::WriteAllText($prefsPath, $prefs + [Environment]::NewLine, $utf8)

$paths = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1") -ProjectRoot $ProjectRoot
$wsSettings = Join-Path $paths.WorkspaceRoot ".metadata\.plugins\org.eclipse.core.runtime\.settings"
if (Test-Path (Split-Path $wsSettings -Parent)) {
    New-Item -ItemType Directory -Force -Path $wsSettings | Out-Null
    $wsPrefs = Join-Path $wsSettings "org.eclipse.jdt.launching.prefs"
    [System.IO.File]::WriteAllText($wsPrefs, $prefs + [Environment]::NewLine, $utf8)
    Write-Host "  -> $wsPrefs" -ForegroundColor Gray
}

Write-Host "JDK 17 적용: $jdkPath" -ForegroundColor Green
Write-Host "  -> $prefsPath" -ForegroundColor Gray
