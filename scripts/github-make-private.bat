@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0.."

set "REPO=chungjonguk/test01"

echo.
echo === GitHub 저장소 비공개(Private) 전환 ===
echo 저장소: %REPO%
echo.

where gh >nul 2>&1
if errorlevel 1 (
  echo [오류] GitHub CLI(gh)가 없습니다.
  echo   winget install GitHub.cli
  echo   또는 docs\github-private-repository-guide.txt 방법 A 웹 설정
  pause
  exit /b 1
)

gh auth status >nul 2>&1
if errorlevel 1 (
  echo [1] GitHub 로그인이 필요합니다. 브라우저 안내를 따르세요.
  gh auth login -h github.com -p https -w
  if errorlevel 1 (
    echo 로그인 실패. 웹에서 설정: https://github.com/%REPO%/settings
    pause
    exit /b 1
  )
)

echo [2] 현재 공개 여부 확인...
gh repo view %REPO% --json visibility,isPrivate 2>nul

echo.
echo [3] Private 로 변경합니다...
gh repo edit %REPO% --visibility private --accept-visibility-change-consequences
if errorlevel 1 (
  echo.
  echo [실패] 소유자 계정으로 로그인했는지 확인하세요.
  echo 웹: https://github.com/%REPO%/settings ^> Danger Zone ^> Make private
  pause
  exit /b 1
)

echo.
echo [완료] 저장소가 Private 입니다. 권한 없는 사용자는 clone/pull 할 수 없습니다.
gh repo view %REPO% --json visibility,isPrivate
echo.
pause
