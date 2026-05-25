# PrintMall 프로젝트 빠른 백업 → D:\backup\projects
param(
    [string]$BackupRoot = "D:\backup",
    [string]$ProjectRoot = ""
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $ProjectRoot) {
    $ProjectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
}

$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$projectDir = Join-Path $BackupRoot "projects"
$projName = Split-Path $ProjectRoot -Leaf
$projZip = Join-Path $projectDir "${projName}_$ts.zip"

New-Item -ItemType Directory -Force -Path $projectDir | Out-Null

$exclude = @(
    "--exclude=target",
    "--exclude=node_modules",
    "--exclude=frontend/node_modules",
    "--exclude=frontend/dist",
    "--exclude=frontend/.vite",
    "--exclude=logs",
    "--exclude=.metadata",
    "--exclude=.recommenders",
    "--exclude=.dbeaver",
    "--exclude=.git"
)

Write-Host ""
Write-Host "=== 프로젝트 백업 ($ts) ===" -ForegroundColor Cyan
Write-Host "  원본: $ProjectRoot" -ForegroundColor Gray
Write-Host "  대상: $projZip" -ForegroundColor Gray
Write-Host ""

Push-Location $ProjectRoot
try {
    & tar -acf $projZip @exclude .
    $mb = [math]::Round((Get-Item $projZip).Length / 1MB, 1)
    Write-Host "완료: $projZip ($mb MB)" -ForegroundColor Green
} finally {
    Pop-Location
}

$readme = Join-Path $projectDir "${projName}_$ts.txt"
@"
백업 일시: $ts
원본 경로: $ProjectRoot
압축 파일: $projZip

포함: 소스, pom.xml, Eclipse(.project/.classpath/.settings/.launch), scripts
제외: target, node_modules, logs, .git, .dbeaver

Eclipse 복원:
  1) 압축 해제 후 STS 워크스페이스(new-workspace)에 폴더 배치
  2) open-sts-workspace.bat 또는 Import Maven Project
  3) scripts\eclipse\apply-jdk17.ps1 실행 (JDK 경로)
  4) Maven Update Project (Alt+F5)
"@ | Out-File -FilePath $readme -Encoding utf8

Write-Host "  안내: $readme" -ForegroundColor Gray
Write-Host ""
