# 이 PC에서 PrintMall을 로컬 실행한다.
# JDK / Maven / PostgreSQL: %USERPROFILE%\.local\dev
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$dev = Join-Path $env:USERPROFILE ".local\dev"
$env:JAVA_HOME = Join-Path $dev "jdk-17"
$mvnBin = Join-Path $dev "apache-maven-3.9.15\bin"
$pgbin = Join-Path $dev "pgsql\bin"
$pgdata = Join-Path $dev "pgdata"
$env:Path = "$env:JAVA_HOME\bin;$mvnBin;$pgbin;$env:Path"
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    throw "JDK 17 없음: $env:JAVA_HOME"
}
if (-not (Test-Path $pgdata)) {
    throw "PostgreSQL 데이터 없음: $pgdata"
}

& "$pgbin\pg_isready.exe" -h 127.0.0.1 -p 5432 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "PostgreSQL 기동 (localhost:5432)..."
    & "$pgbin\pg_ctl.exe" -D $pgdata -l (Join-Path $pgdata "server.log") -w start
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL 기동 실패. 로그: $pgdata\server.log" }
}

Set-Location $root
Write-Host ""
Write-Host "Spring Boot 시작: http://localhost:8082/"
Write-Host ""
& mvn spring-boot:run -DskipTests "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
