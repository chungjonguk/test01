# Eclipse/STS 워크스페이스 ↔ spring-boot-app-fixed 프로젝트 연동
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent),
    [string]$WorkspaceRoot = "",
    [switch]$SkipBackup
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$paths = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1") -ProjectRoot $ProjectRoot
if (-not $WorkspaceRoot) {
    $WorkspaceRoot = $paths.WorkspaceRoot
}

$workspaceSettings = Join-Path $WorkspaceRoot ".metadata\.plugins\org.eclipse.core.runtime\.settings"
$templateDir = Join-Path $PSScriptRoot "workspace"

Write-Host ""
Write-Host "=== Eclipse 워크스페이스 연동 ===" -ForegroundColor Cyan
Write-Host "  워크스페이스 : $WorkspaceRoot" -ForegroundColor Gray
Write-Host "  프로젝트     : $ProjectRoot" -ForegroundColor Gray
Write-Host "  Eclipse 이름 : spring-boot-app" -ForegroundColor Gray
Write-Host ""

# 1) 프로젝트 JDK 17
Write-Host "[1/6] 프로젝트 JDK 17 (.settings)..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "apply-jdk17.ps1") -ProjectRoot $ProjectRoot

# 2) Maven compile (classpath / target 동기화)
Write-Host "[2/6] Maven compile (Update Project 기반)..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "maven-update-for-eclipse.ps1") -ProjectRoot $ProjectRoot

# 3) 워크스페이스 .metadata + 프로젝트 등록
Write-Host "[3/6] 워크스페이스 .metadata + spring-boot-app 등록..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "bootstrap-workspace-metadata.ps1") -WorkspaceRoot $WorkspaceRoot -ProjectRoot $ProjectRoot

# 4) 워크스페이스 prefs + STS ini UTF-8
Write-Host "[4/6] 워크스페이스·STS UTF-8 (한글 깨짐 방지)..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "apply-workspace-utf8.ps1") -ProjectRoot $ProjectRoot

# 5) STS headless import (설치 시)
Write-Host "[5/6] STS Maven import (headless, 있으면)..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "import-maven-headless.ps1") -ProjectRoot $ProjectRoot -WorkspaceRoot $WorkspaceRoot

# 6) 점검
Write-Host "[6/6] Eclipse 연동 파일 점검..." -ForegroundColor Green
& (Join-Path $PSScriptRoot "verify-eclipse-setup.ps1") -ProjectRoot $ProjectRoot

# 선택 백업
if (-not $SkipBackup) {
    Write-Host "[4/4] 프로젝트 빠른 백업 (D:\backup\projects)..." -ForegroundColor Green
    $backupScript = Join-Path $ProjectRoot "scripts\backup\backup-project-quick.ps1"
    if (Test-Path $backupScript) {
        & powershell -NoProfile -ExecutionPolicy Bypass -File $backupScript
    } else {
        Write-Host "  [건너뜀] backup-project-quick.ps1 없음" -ForegroundColor Yellow
    }
} else {
    Write-Host "[+] 백업 건너뜀 (-SkipBackup)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "연동 완료" -ForegroundColor Green
Write-Host "  STS 열기  : open-sts-workspace.bat" -ForegroundColor Gray
Write-Host "  Import    : scripts\eclipse\import-maven-project.bat 안내" -ForegroundColor Gray
Write-Host "  실행      : .launch\spring-boot-app-java.launch" -ForegroundColor Gray
Write-Host "  Cursor    : 같은 폴더 spring-boot-app-fixed 열기" -ForegroundColor Gray
Write-Host ""
