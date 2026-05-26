# PrintMall 프로젝트 빠른 백업 → D:\backup\projects
param(
    [string]$BackupRoot = "D:\backup",
    [string]$ProjectRoot = ""
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null
. (Join-Path $PSScriptRoot "BackupTextEncoding.ps1")

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
$readmeTemplate = Join-Path $PSScriptRoot "guides\project-backup-readme.template.txt"
$readmeContent = Read-BackupUtf8Template -TemplatePath $readmeTemplate -Replacements @{
    TS           = $ts
    PROJECT_ROOT = $ProjectRoot
    ZIP          = $projZip
}
Write-BackupUtf8Text -Path $readme -Content $readmeContent

Write-Host "  안내: $readme" -ForegroundColor Gray
Write-Host ""
