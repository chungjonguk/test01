# PC LAN IP 조회 — 휴대폰 브라우저 접속 URL 출력
$ErrorActionPreference = "SilentlyContinue"
chcp 65001 | Out-Null

Write-Host ""
Write-Host "=== 모바일 테스트용 PC IP ===" -ForegroundColor Cyan
Write-Host ""

$addrs = Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object {
        $_.IPAddress -notlike "127.*" -and
        $_.IPAddress -notlike "169.254.*" -and
        $_.PrefixOrigin -ne "WellKnown"
    } |
    Sort-Object InterfaceMetric

if (-not $addrs) {
    Write-Host "LAN IP를 찾지 못했습니다. Wi-Fi/유선 연결을 확인하세요." -ForegroundColor Yellow
    exit 1
}

foreach ($a in $addrs) {
    $ip = $a.IPAddress
    Write-Host "  PrintMall   : http://${ip}:8081/" -ForegroundColor Green
    Write-Host "  모의투자    : http://${ip}:8082/" -ForegroundColor Green
    Write-Host ""
}

Write-Host "휴대폰과 PC가 같은 Wi-Fi에 연결되어 있어야 합니다." -ForegroundColor Gray
Write-Host "방화벽 차단 시: scripts\mobile-dev\setup-mobile-firewall.bat (관리자)" -ForegroundColor Gray
Write-Host ""
