# D:\sts-workspace 등 워크스페이스 + 프로젝트 UTF-8 prefs 적용
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$paths = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1") -ProjectRoot $ProjectRoot
$settingsDest = Join-Path $paths.WorkspaceRoot ".metadata\.plugins\org.eclipse.core.runtime\.settings"
$templateDir = Join-Path $PSScriptRoot "workspace"

if (-not (Test-Path $settingsDest)) {
    Write-Host "[안내] .metadata 없음 — sync-eclipse-workspace.bat 먼저 실행" -ForegroundColor Yellow
    exit 1
}

New-Item -ItemType Directory -Force -Path $settingsDest | Out-Null
foreach ($file in Get-ChildItem $templateDir -Filter "*.prefs") {
    Copy-Item $file.FullName (Join-Path $settingsDest $file.Name) -Force
    Write-Host "  -> $($file.Name)" -ForegroundColor Gray
}

# 프로젝트 .settings (에디터·properties UTF-8, 워크스페이스와 별도)
$projSettings = Join-Path $ProjectRoot ".settings"
$runtimePrefs = Join-Path $PSScriptRoot "workspace\org.eclipse.core.runtime.prefs"
if (Test-Path $runtimePrefs) {
    New-Item -ItemType Directory -Force -Path $projSettings | Out-Null
    Copy-Item $runtimePrefs (Join-Path $projSettings "org.eclipse.core.runtime.prefs") -Force
    Write-Host "  -> 프로젝트 .settings/org.eclipse.core.runtime.prefs" -ForegroundColor Gray
}

& (Join-Path $PSScriptRoot "patch-sts-utf8.ps1")
Write-Host "워크스페이스 UTF-8 적용: $($paths.WorkspaceRoot)" -ForegroundColor Green
