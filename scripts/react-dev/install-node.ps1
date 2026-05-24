# Node.js LTS portable 설치 — C:\Users\<user>\.local\dev\node-22
param(
    [string]$NodeVersion = "22.15.1"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$DevRoot = Join-Path $env:USERPROFILE ".local\dev"
$NodeDir = Join-Path $DevRoot "node-22"
$ZipName = "node-v$NodeVersion-win-x64.zip"
$ZipPath = Join-Path $DevRoot $ZipName
$ExtractDir = Join-Path $DevRoot "node-v$NodeVersion-win-x64"
$NodeExe = Join-Path $NodeDir "node.exe"
$NpmCmd = Join-Path $NodeDir "npm.cmd"

function Test-NodeReady {
    return (Test-Path $NodeExe) -and (Test-Path $NpmCmd)
}

Write-Host ""
Write-Host "=== Node.js 개발환경 설치 ===" -ForegroundColor Cyan
Write-Host "  대상: $NodeDir" -ForegroundColor Gray
Write-Host ""

if (Test-NodeReady) {
    $ver = & $NodeExe -v
    $npmVer = & $NpmCmd -v
    Write-Host "이미 설치됨 — Node $ver, npm $npmVer" -ForegroundColor Green
    exit 0
}

New-Item -ItemType Directory -Force -Path $DevRoot | Out-Null

if (-not (Test-Path $ZipPath)) {
    $url = "https://nodejs.org/dist/v$NodeVersion/$ZipName"
    Write-Host "다운로드: $url" -ForegroundColor Yellow
    Invoke-WebRequest -Uri $url -OutFile $ZipPath -UseBasicParsing
}

if (-not (Test-Path $ExtractDir)) {
    Write-Host "압축 해제 중..." -ForegroundColor Yellow
    Expand-Archive -Path $ZipPath -DestinationPath $DevRoot -Force
}

if (Test-Path $NodeDir) {
    Remove-Item -Recurse -Force $NodeDir
}
Rename-Item -Path $ExtractDir -NewName "node-22"

if (-not (Test-NodeReady)) {
    Write-Host "설치 실패 — node.exe 또는 npm.cmd 를 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "설치 완료 — Node $(& $NodeExe -v), npm $(& $NpmCmd -v)" -ForegroundColor Green
Write-Host "  NODE_HOME=$NodeDir" -ForegroundColor Gray
Write-Host ""
