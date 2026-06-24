@echo off
chcp 65001 >nul
setlocal EnableExtensions

set "PROJECT=%~dp0"
set "STS_EXE="
set "WORKSPACE=E:\sts-workspace"

if exist "%PROJECT%scripts\eclipse\sts-path.local.txt" (
  set /p STS_EXE=<"%PROJECT%scripts\eclipse\sts-path.local.txt"
)
if exist "%PROJECT%scripts\eclipse\sts-workspace.local.txt" (
  set /p WORKSPACE=<"%PROJECT%scripts\eclipse\sts-workspace.local.txt"
)

if "%STS_EXE%"=="" set "STS_EXE=C:\Users\chung\.local\dev\sts-5.1.1\sts-5.1.1.RELEASE\SpringToolsForEclipse.exe"

if not exist "%STS_EXE%" (
  echo STS 실행 파일을 찾지 못했습니다.
  echo   %STS_EXE%
  echo scripts\eclipse\sts-path.local.txt 에 경로를 넣으세요.
  pause
  exit /b 1
)

echo.
echo === STS 워크스페이스 열기 ===
echo   STS       : %STS_EXE%
echo   Workspace : %WORKSPACE%
echo   Project   : %PROJECT%
echo   Eclipse명 : spring-boot-app
echo.
echo  [연동] sync-eclipse-workspace.bat
echo.

start "" "%STS_EXE%" -data "%WORKSPACE%"
endlocal
