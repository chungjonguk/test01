@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"

echo.
echo === MySQL 8.4 기동 및 접속 확인 ===
echo.

call "%~dp0scripts\ensure-mysql.bat"
if errorlevel 1 (
  echo.
  echo MySQL 기동 또는 접속에 실패했습니다.
) else (
  echo.
  echo DBeaver: 127.0.0.1:3306, DB spring_boot_app, 사용자 redcroxx
)

echo.
pause
endlocal
