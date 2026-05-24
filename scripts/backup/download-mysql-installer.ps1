# MySQL 8.4 Windows 설치 zip 다운로드 (백업 install 폴더용)
param(
    [string]$Version = "8.4.9",
    [string]$DestDir = "D:\backup\install"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$zipName = "mysql-$Version-winx64.zip"
$msiName = "mysql-$Version-winx64.msi"
$zipPath = Join-Path $DestDir $zipName
$msiPath = Join-Path $DestDir $msiName

New-Item -ItemType Directory -Force -Path $DestDir | Out-Null

function Get-IfExists($path) {
    if (Test-Path $path) {
        $mb = [math]::Round((Get-Item $path).Length / 1MB, 1)
        Write-Host "이미 있음: $path ($mb MB)" -ForegroundColor Green
        return $true
    }
    return $false
}

if ((Get-IfExists $zipPath) -and (Get-IfExists $msiPath)) {
    exit 0
}

$urls = @(
    @{ File = $msiPath; Url = "https://dev.mysql.com/get/Downloads/MySQL-8.4/$msiName" },
    @{ File = $zipPath; Url = "https://dev.mysql.com/get/Downloads/MySQL-8.4/$zipName" }
)

foreach ($item in $urls) {
    if (Test-Path $item.File) {
        continue
    }
    Write-Host "다운로드: $($item.Url)" -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $item.Url -OutFile $item.File -UseBasicParsing
        $mb = [math]::Round((Get-Item $item.File).Length / 1MB, 1)
        Write-Host "  완료: $($item.File) ($mb MB)" -ForegroundColor Green
    } catch {
        Write-Host "  실패: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  수동 다운로드: https://dev.mysql.com/downloads/mysql/8.4.html" -ForegroundColor Gray
    }
}
