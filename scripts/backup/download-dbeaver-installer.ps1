# DBeaver Community Windows 설치파일 다운로드
param(
    [string]$Version = "25.3.1",
    [string]$DestDir = "D:\backup\install"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$exeName = "dbeaver-ce-$Version-x86_64-setup.exe"
$zipName = "dbeaver-ce-$Version-win32.win32.x86_64.zip"
$exePath = Join-Path $DestDir $exeName
$zipPath = Join-Path $DestDir $zipName

New-Item -ItemType Directory -Force -Path $DestDir | Out-Null

$items = @(
    @{ File = $exePath; Url = "https://github.com/dbeaver/dbeaver/releases/download/$Version/$exeName" },
    @{ File = $zipPath; Url = "https://github.com/dbeaver/dbeaver/releases/download/$Version/$zipName" }
)

foreach ($item in $items) {
    if (Test-Path $item.File) {
        $mb = [math]::Round((Get-Item $item.File).Length / 1MB, 1)
        Write-Host "이미 있음: $($item.File) ($mb MB)" -ForegroundColor Green
        continue
    }
    Write-Host "다운로드: $($item.Url)" -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $item.Url -OutFile $item.File -UseBasicParsing
        $mb = [math]::Round((Get-Item $item.File).Length / 1MB, 1)
        Write-Host "  완료: $($item.File) ($mb MB)" -ForegroundColor Green
    } catch {
        Write-Host "  실패: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  수동: https://github.com/dbeaver/dbeaver/releases/tag/$Version" -ForegroundColor Gray
    }
}
