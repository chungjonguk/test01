# PostgreSQL 로컬 기동·DB 생성 확인 (Windows)
# 사용: powershell -ExecutionPolicy Bypass -File scripts\ensure-postgres.ps1
$ErrorActionPreference = "Stop"
$dev = Join-Path $env:USERPROFILE ".local\dev"
$pgbin = Join-Path $dev "pgsql\bin"
$pgdata = Join-Path $dev "pgdata"
$dbName = "spring_boot_app"
$host = "127.0.0.1"
$port = "5432"
$user = "postgres"

if (-not (Test-Path (Join-Path $pgbin "psql.exe"))) {
    Write-Error "psql 없음: $pgbin"
}
$env:Path = "$pgbin;$env:Path"

& "$pgbin\pg_isready.exe" -h $host -p $port | Out-Null
if ($LASTEXITCODE -ne 0) {
    if (-not (Test-Path $pgdata)) {
        Write-Error "PostgreSQL 데이터 디렉터리 없음: $pgdata"
    }
    Write-Host "PostgreSQL 기동 중..."
    & "$pgbin\pg_ctl.exe" -D $pgdata -l (Join-Path $pgdata "server.log") -w start
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL 기동 실패" }
}

$exists = & "$pgbin\psql.exe" -h $host -p $port -U $user -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$dbName';"
if (-not $exists) {
    Write-Host "CREATE DATABASE $dbName"
    & "$pgbin\psql.exe" -h $host -p $port -U $user -d postgres -c "CREATE DATABASE $dbName ENCODING 'UTF8';"
}

Write-Host "PostgreSQL OK — $host`:$port/$dbName"
exit 0
