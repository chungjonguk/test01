# 카카오 REST API 키를 application-local.properties 에 저장
# 사용: .\scripts\setup-kakao-rest-key.ps1
#   또는: .\scripts\setup-kakao-rest-key.ps1 -Key "발급받은_REST_API_키"

param(
    [string]$Key
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$localFile = Join-Path $root "src\main\resources\application-local.properties"

if (-not $Key) {
    $Key = Read-Host "카카오 REST API 키 입력 (developers.kakao.com > 앱 키)"
}
$Key = $Key.Trim()
if (-not $Key -or $Key -eq "YOUR_KAKAO_REST_API_KEY") {
    Write-Host "[중단] 유효한 REST API 키가 필요합니다." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $localFile)) {
    Copy-Item (Join-Path $root "src\main\resources\application-local.properties.example") $localFile
}

$content = Get-Content $localFile -Raw -Encoding UTF8
if ($content -match "(?m)^kakao\.client-id=.*$") {
    $content = $content -replace "(?m)^kakao\.client-id=.*$", "kakao.client-id=$Key"
} else {
    $content += "`n# --- 카카오 ---`nkakao.client-id=$Key`nkakao.local.mock-enabled=false`n"
}
if ($content -match "(?m)^kakao\.rest-api-key=.*$") {
    $content = $content -replace "(?m)^kakao\.rest-api-key=.*$", "kakao.rest-api-key=$Key"
} else {
    $content += "kakao.rest-api-key=$Key`n"
}
Set-Content -Path $localFile -Value $content.TrimEnd() -Encoding UTF8 -NoNewline
Add-Content -Path $localFile -Value "" -Encoding UTF8

Write-Host "[완료] $localFile 에 kakao.client-id 를 저장했습니다." -ForegroundColor Green
Write-Host "서버를 재시작한 뒤 http://localhost:8081/api/kakao/local/status 에서 configured:true 를 확인하세요."
