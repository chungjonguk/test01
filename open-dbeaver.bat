@echo off
chcp 65001 >nul
setlocal EnableExtensions

set "DBEAVER="
if exist "C:\Users\chung\AppData\Local\DBeaver\dbeaver.exe" set "DBEAVER=C:\Users\chung\AppData\Local\DBeaver\dbeaver.exe"
if exist "C:\Program Files\DBeaver\dbeaver.exe" set "DBEAVER=C:\Program Files\DBeaver\dbeaver.exe"
if "%DBEAVER%"=="" (
  for /f "delims=" %%P in ('where dbeaver 2^>nul') do set "DBEAVER=%%P"
)

if "%DBEAVER%"=="" (
  echo DBeaver를 찾지 못했습니다. winget install DBeaver.DBeaver.Community
  pause
  exit /b 1
)

echo.
echo === DBeaver 실행 ===
echo   연결 가이드: scripts\dbeaver-postgresql-connection.txt
echo   Host: localhost:5432  DB: spring_boot_app  User: postgres
echo.
start "" "%DBEAVER%"
endlocal
