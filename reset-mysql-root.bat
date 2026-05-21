@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin"
set "MYINI=C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
set "INIT_SQL=%~dp0scripts\mysql-reset-root.sql"

echo.
echo === MySQL root 비밀번호 초기화 (init-file) ===
echo    새 root 비밀번호: jonguk0412
echo    (spring Boot DB 계정 redcroxx 와 동일하게 설정)
echo.

echo [1/4] MySQL 종료 (3306)...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":3306" ^| findstr "LISTENING"') do (
  echo   PID %%P 종료
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 3 /nobreak >nul

echo [2/4] init-file 로 root 비밀번호 재설정 중...
start "mysqld-init" /B "%MYSQL_BIN%\mysqld.exe" --defaults-file="%MYINI%" --init-file="%INIT_SQL%"
timeout /t 12 /nobreak >nul

echo [3/4] 임시 mysqld 종료...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":3306" ^| findstr "LISTENING"') do (
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 3 /nobreak >nul

echo [4/4] MySQL 일반 기동...
start "mysqld" /B "%MYSQL_BIN%\mysqld.exe" --defaults-file="%MYINI%"
timeout /t 6 /nobreak >nul

echo.
echo root 접속 테스트...
"%MYSQL_BIN%\mysql.exe" -h 127.0.0.1 -u root -pjonguk0412 -e "SELECT 'root OK' AS status;" 2>nul
if errorlevel 1 (
  echo   실패 — DBeaver에서 root / jonguk0412 로 다시 시도하세요.
) else (
  echo   성공 — DBeaver root 연결: 사용자 root, 비밀번호 jonguk0412
  echo   이어서 scripts\mysql-init-redcroxx.sql 을 root 로 실행하세요.
)

echo.
pause
endlocal
