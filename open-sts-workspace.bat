@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM STS 4 실행 파일 — 본인 PC 설치 경로에 맞게 추가하세요.
set "STS_EXE="
if exist "C:\Program Files\Spring Tool Suite 4\SpringToolSuite4.exe" set "STS_EXE=C:\Program Files\Spring Tool Suite 4\SpringToolSuite4.exe"
if exist "C:\sts-4\SpringToolSuite4.exe" set "STS_EXE=C:\sts-4\SpringToolSuite4.exe"
if exist "C:\Users\chung\sts-4\SpringToolSuite4.exe" set "STS_EXE=C:\Users\chung\sts-4\SpringToolSuite4.exe"

if "%STS_EXE%"=="" (
  echo STS 실행 파일을 찾지 못했습니다.
  echo open-sts-workspace.bat 상단 STS_EXE 경로를 직접 지정한 뒤 다시 실행하세요.
  pause
  exit /b 1
)

set "WORKSPACE=%~dp0.."
set "PROJECT=%~dp0"

echo.
echo === STS 워크스페이스 열기 ===
echo   Workspace : %WORKSPACE%
echo   Project   : %PROJECT%
echo   Eclipse명 : spring-boot-app
echo.
echo  [최초 1회] scripts\eclipse\import-maven-project.bat 안내 참고
echo  [Cursor 연동] STS Preferences ^> General ^> Workspace
echo    - Refresh using native hooks or polling
echo    - Refresh on access
echo.

start "" "%STS_EXE%" -data "%WORKSPACE%"
endlocal
