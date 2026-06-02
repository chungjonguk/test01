# 호스트 PostgreSQL(비서비스) 클러스터 중지 스크립트
$PGBIN = 'C:\Program Files\PostgreSQL\16\bin'
$DATA  = 'D:\pgdata-springboot'
& "$PGBIN\pg_ctl.exe" -D $DATA -m fast stop
Write-Host "PostgreSQL 중지 완료"
