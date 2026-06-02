# 호스트 PostgreSQL(비서비스) 클러스터 기동 스크립트
# 관리자 권한 없이 현재 사용자 권한으로 PostgreSQL 16을 포트 5433에 띄운다.
# (이 PC는 포트 5432 바인딩이 거부되어 5433 사용)

$ErrorActionPreference = 'Stop'
$PGBIN = 'C:\Program Files\PostgreSQL\16\bin'
$DATA  = 'D:\pgdata-springboot'
$PORT  = 5433

if (-not (Test-Path "$PGBIN\pg_ctl.exe")) { throw "pg_ctl not found: $PGBIN" }
if (-not (Test-Path "$DATA\PG_VERSION")) {
    Write-Host "데이터 디렉터리가 없어 초기화합니다: $DATA"
    & "$PGBIN\initdb.exe" -D $DATA -U postgres -A trust -E UTF8 --locale=C
}

& "$PGBIN\pg_ctl.exe" -D $DATA -o "-p $PORT" -l "$DATA\server.log" -w start
Start-Sleep -Seconds 2
& "$PGBIN\pg_isready.exe" -h 127.0.0.1 -p $PORT

# spring_boot_app DB가 없으면 생성
$exists = & "$PGBIN\psql.exe" -h 127.0.0.1 -p $PORT -U postgres -d postgres -tA -c "SELECT 1 FROM pg_database WHERE datname='spring_boot_app';"
if (-not $exists) {
    & "$PGBIN\createdb.exe" -h 127.0.0.1 -p $PORT -U postgres spring_boot_app
    Write-Host "spring_boot_app 데이터베이스 생성 완료"
}

Write-Host "PostgreSQL 기동 완료: jdbc:postgresql://localhost:$PORT/spring_boot_app (user=postgres)"
Write-Host "앱 실행: `$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:$PORT/spring_boot_app'; mvn spring-boot:run"
