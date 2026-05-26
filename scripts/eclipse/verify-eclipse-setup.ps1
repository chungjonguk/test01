# Eclipse/STS 연동 파일 점검
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$required = @(
    ".project",
    ".classpath",
    ".settings/org.eclipse.jdt.core.prefs",
    ".settings/org.eclipse.core.resources.prefs",
    ".settings/org.eclipse.m2e.core.prefs",
    ".launch/spring-boot-app-java.launch",
    ".launch/spring-boot-app-maven.launch",
    ".launch/spring-boot-app-spring-boot.launch",
    "scripts/eclipse/apply-jdk17.ps1",
    "open-sts-workspace.bat",
    "backup-and-eclipse.bat",
    "pom.xml"
)

Write-Host ""
Write-Host "=== Eclipse 연동 파일 점검 ===" -ForegroundColor Cyan
Write-Host "  프로젝트: $ProjectRoot" -ForegroundColor Gray
Write-Host ""

$ok = $true
foreach ($rel in $required) {
    $path = Join-Path $ProjectRoot $rel
    if (Test-Path $path) {
        Write-Host "  [OK] $rel" -ForegroundColor Green
    } else {
        Write-Host "  [누락] $rel" -ForegroundColor Red
        $ok = $false
    }
}

$paths = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1") -ProjectRoot $ProjectRoot
$workspace = $paths.WorkspaceRoot
$meta = Join-Path $workspace ".metadata"
Write-Host ""
Write-Host "  STS 워크스페이스(상위): $workspace" -ForegroundColor Gray
if (Test-Path $meta) {
    Write-Host "  [OK] .metadata (프로젝트 spring-boot-app 등록됨)" -ForegroundColor Green
} else {
    Write-Host "  [누락] .metadata — sync-eclipse-workspace.bat 실행" -ForegroundColor Yellow
}
Write-Host "  STS 열기: open-sts-workspace.bat" -ForegroundColor Gray
Write-Host ""

if (-not $ok) {
    exit 1
}
Write-Host "점검 완료 — STS에서 프로젝트가 보이면 Maven > Update Project(Alt+F5)만 확인하세요." -ForegroundColor Green
Write-Host ""
