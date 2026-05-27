# STS/Eclipse Maven 연동 — CLI 동기화만 수행 (잘못된 m2e headless application 미사용)
# org.eclipse.m2e.core.maven2ProjectJob 은 STS 레지스트리에 없어 RuntimeException 발생함.
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent),
    [string]$WorkspaceRoot = "",
    [switch]$TryWorkbenchImport
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $WorkspaceRoot) {
    $WorkspaceRoot = Split-Path $ProjectRoot -Parent
}

$resolved = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1")
if (-not $WorkspaceRoot) { $WorkspaceRoot = $resolved.WorkspaceRoot }

Write-Host "[Maven 연동] m2e headless application 은 STS에 없습니다 — Maven CLI + 워크스페이스 등록으로 대체합니다." -ForegroundColor Yellow
Write-Host "  워크스페이스: $WorkspaceRoot" -ForegroundColor Gray
Write-Host "  프로젝트    : $ProjectRoot" -ForegroundColor Gray
Write-Host "  STS에서 필요 시: Package Explorer > spring-boot-app > Maven > Update Project (Alt+F5)" -ForegroundColor Gray

& (Join-Path $PSScriptRoot "maven-update-for-eclipse.ps1") -ProjectRoot $ProjectRoot
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if (-not $TryWorkbenchImport) {
    exit 0
}

$candidates = @(
    $resolved.StsExe,
    "C:\Users\chung\.local\dev\sts-5.1.1\sts-5.1.1.RELEASE\SpringToolsForEclipse.exe",
    "C:\Program Files\Spring Tool Suite 4\SpringToolSuite4.exe",
    "C:\sts-4\SpringToolSuite4.exe"
) | Where-Object { $_ -and (Test-Path $_) }

if (-not $candidates) {
    Write-Host "[건너뜀] STS 실행 파일 없음" -ForegroundColor Yellow
    exit 0
}

$sts = $candidates[0]
Write-Host "STS 프로젝트 import 시도 (-import, GUI 잠깐 열릴 수 있음): $sts" -ForegroundColor Cyan
& $sts -nosplash -consoleLog -data $WorkspaceRoot -import $ProjectRoot
if ($LASTEXITCODE -ne 0) {
    Write-Host "[경고] STS -import 종료 코드: $LASTEXITCODE — File > Import > Maven 으로 수동 Import 하세요." -ForegroundColor Yellow
}
