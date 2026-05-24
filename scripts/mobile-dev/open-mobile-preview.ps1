# Edge 모바일 에뮬레이션으로 로컬 앱 미리보기
param(
    [ValidateSet("printmall", "stock", "printmall-analytics")]
    [string]$Target = "printmall",
    [int]$Width = 390,
    [int]$Height = 844
)

$edgePaths = @(
    "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
    "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe"
)
$edge = $edgePaths | Where-Object { Test-Path $_ } | Select-Object -First 1

if (-not $edge) {
    Write-Host "Microsoft Edge를 찾을 수 없습니다." -ForegroundColor Red
    exit 1
}

$urls = @{
    "printmall"            = "http://localhost:8081/"
    "printmall-analytics"  = "http://localhost:8081/dashboard/analytics"
    "stock"                = "http://localhost:8082/"
}
$url = $urls[$Target]

Write-Host "모바일 미리보기: $url (${Width}x${Height})" -ForegroundColor Cyan
Start-Process -FilePath $edge -ArgumentList @(
    "--new-window",
    "--window-size=$Width,$Height",
    $url
)

Write-Host ""
Write-Host "F12 → Ctrl+Shift+M (디바이스 툴바) 로 iPhone/Android 크기 테스트" -ForegroundColor Gray
