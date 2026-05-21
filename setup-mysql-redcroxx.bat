@echo off
chcp 65001 >nul
setlocal
set MYSQL="C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
set SQL=%~dp0scripts\mysql-init-redcroxx.sql

echo.
echo === MySQL redcroxx 계정 생성 (root 비밀번호 입력) ===
echo.
%MYSQL% -h 127.0.0.1 -u root -p --default-character-set=utf8mb4 < "%SQL%"
if errorlevel 1 (
  echo.
  echo 실패: root 로그인 또는 권한을 확인하세요.
  echo MySQL Workbench에서 scripts\mysql-init-redcroxx.sql 을 실행해도 됩니다.
  pause
  exit /b 1
)

echo.
echo 완료. application.properties 에서 redcroxx 계정 주석을 해제한 뒤 서버를 재시작하세요.
echo.
pause
endlocal
