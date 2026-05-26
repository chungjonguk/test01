@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem application.properties 와 동일한 접속 정보
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin"
set "MYINI=C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
set "MYSQL_HOST=127.0.0.1"
set "MYSQL_PORT=3306"
set "MYSQL_USER=redcroxx"
set "MYSQL_PASS=jonguk0412"
set "MYSQL_DB=spring_boot_app"
set "MAX_WAIT_SEC=45"
set "RETRY_SEC=3"

if not exist "%MYSQL_BIN%\mysql.exe" (
  echo [DB] mysql.exe 없음: "%MYSQL_BIN%"
  exit /b 1
)

rem --- 3306 포트: 미기동 시 mysqld 시작 ---
netstat -ano 2>nul | findstr ":%MYSQL_PORT%" | findstr "LISTENING" >nul
if errorlevel 1 (
  echo [DB] MySQL 미기동 — mysqld 시작 시도...
  if not exist "%MYINI%" (
    echo [DB] my.ini 없음: "%MYINI%"
    exit /b 1
  )
  start "mysqld" /B "%MYSQL_BIN%\mysqld.exe" --defaults-file="%MYINI%"
) else (
  echo [DB] 포트 %MYSQL_PORT% LISTENING 확인
)

rem --- 접속 대기 (spring_boot_app SELECT 1) ---
set /a ELAPSED=0
:wait_connect
"%MYSQL_BIN%\mysql.exe" -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% -D %MYSQL_DB% --default-character-set=utf8mb4 -N -e "SELECT 1;" >nul 2>&1
if not errorlevel 1 goto :connected

if %ELAPSED% GEQ %MAX_WAIT_SEC% (
  echo [DB] 접속 실패 — %MAX_WAIT_SEC%초 대기 후에도 %MYSQL_HOST%:%MYSQL_PORT%/%MYSQL_DB% 연결 불가
  echo [DB] 계정 %MYSQL_USER% / 비밀번호 / DB 생성 여부를 확인하세요. setup-mysql-redcroxx.bat 참고
  exit /b 1
)

echo [DB] 접속 대기 중... (%ELAPSED%/%MAX_WAIT_SEC%초)
timeout /t %RETRY_SEC% /nobreak >nul
set /a ELAPSED+=RETRY_SEC
goto :wait_connect

:connected
echo [DB] 접속 OK — %MYSQL_HOST%:%MYSQL_PORT% / %MYSQL_DB% (%MYSQL_USER%)
for /f "delims=" %%V in ('"%MYSQL_BIN%\mysql.exe" -h %MYSQL_HOST% -P %MYSQL_PORT% -u %MYSQL_USER% -p%MYSQL_PASS% -D %MYSQL_DB% -N -e "SELECT VERSION();" 2^>nul') do (
  echo [DB] MySQL 버전: %%V
)
echo [DB] JDBC: jdbc:mysql://%MYSQL_HOST%:%MYSQL_PORT%/%MYSQL_DB%
exit /b 0
