@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin"
set "MYINI=C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"

echo.
echo === MySQL 8.4 기동 ===

netstat -ano 2>nul | findstr ":3306" | findstr "LISTENING" >nul
if %errorlevel%==0 (
  echo   이미 실행 중입니다 ^(포트 3306^).
  goto :test
)

echo   mysqld 시작 중...
start "mysqld" /B "%MYSQL_BIN%\mysqld.exe" --defaults-file="%MYINI%"
timeout /t 6 /nobreak >nul

netstat -ano 2>nul | findstr ":3306" | findstr "LISTENING" >nul
if not %errorlevel%==0 (
  echo   실패 — my.ini 경로 또는 MySQL 설치를 확인하세요.
  goto :end
)
echo   기동 완료 ^(포트 3306^).

:test
"%MYSQL_BIN%\mysql.exe" -h 127.0.0.1 -u redcroxx -pjonguk0412 -e "SELECT '접속 OK' AS status;" 2>nul
if errorlevel 1 (
  echo   접속 테스트 실패 — root / jonguk0412 또는 redcroxx 계정을 확인하세요.
) else (
  echo   DBeaver: 127.0.0.1:3306, DB spring_boot_app, 사용자 redcroxx
)

:end
echo.
pause
endlocal
