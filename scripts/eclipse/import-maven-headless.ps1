# STS/Eclipse headless Maven import (설치 경로 있을 때)
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent),
    [string]$WorkspaceRoot = "",
    [string]$StsExe = ""
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $WorkspaceRoot) {
    $WorkspaceRoot = Split-Path $ProjectRoot -Parent
}

$resolved = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1")
if (-not $WorkspaceRoot) { $WorkspaceRoot = $resolved.WorkspaceRoot }
if (-not $StsExe) { $StsExe = $resolved.StsExe }

$candidates = @(
    $StsExe,
    "C:\Users\chung\.local\dev\sts-5.1.1\sts-5.1.1.RELEASE\SpringToolsForEclipse.exe",
    "C:\Program Files\Spring Tool Suite 4\SpringToolSuite4.exe",
    "C:\sts-4\SpringToolSuite4.exe"
) | Where-Object { $_ -and (Test-Path $_) }

if (-not $candidates) {
    Write-Host "[건너뜀] STS 실행 파일 없음 — sts-path.local.txt 에 경로를 넣거나 open-sts-workspace.bat 경로를 추가하세요." -ForegroundColor Yellow
    exit 0
}

$sts = $candidates[0]
Write-Host "Headless Maven import: $sts" -ForegroundColor Cyan
& $sts -nosplash -consoleLog `
    -data $WorkspaceRoot `
    -application org.eclipse.m2e.core.maven2ProjectJob `
    -import $ProjectRoot

if ($LASTEXITCODE -ne 0) {
    Write-Host "[경고] headless import 종료 코드: $LASTEXITCODE (GUI Import 로 대체)" -ForegroundColor Yellow
}
