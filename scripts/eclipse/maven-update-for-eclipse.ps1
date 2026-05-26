# Maven 의존성·컴파일 동기화 (STS Maven Update Project 와 동일한 기반)
param(
    [string]$ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent),
    [switch]$SkipTests = $true
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$jdk = Join-Path $env:USERPROFILE ".local\dev\jdk-17"
if (Test-Path $jdk) { $env:JAVA_HOME = (Resolve-Path $jdk).Path }

$mvn = Join-Path $env:USERPROFILE ".local\dev\apache-maven-3.9.15\bin\mvn.cmd"
if (-not (Test-Path $mvn)) {
    $mvn = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
}
if (-not $mvn) { throw "mvn 을 찾지 못했습니다." }

Push-Location $ProjectRoot
try {
    $args = @("clean", "compile", "-q")
    if ($SkipTests) { $args += "-DskipTests" }
    Write-Host "Maven: $($args -join ' ')" -ForegroundColor Cyan
    & $mvn @args
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host "Maven compile 완료 (target/classes 반영)" -ForegroundColor Green
} finally {
    Pop-Location
}
