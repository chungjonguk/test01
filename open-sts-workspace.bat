@echo off
REM STS 4 실행 파일 경로를 본인 PC 설치 위치에 맞게 수정하세요.
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
start "" "%STS_EXE%" -data "%WORKSPACE%"
echo Workspace: %WORKSPACE%
