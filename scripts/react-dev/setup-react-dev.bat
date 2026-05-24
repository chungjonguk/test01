@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0..\.."

set NODE_HOME=C:\Users\chung\.local\dev\node-22
set PATH=%NODE_HOME%;%PATH%

echo.
echo === React 개발환경 셋업 (Node.js + frontend npm install) ===
echo.

where node >nul 2>&1
if errorlevel 1 (
  echo [1/3] Node.js 설치 중...
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-node.ps1"
  if errorlevel 1 (
    echo [실패] Node.js 설치에 실패했습니다.
    pause
    exit /b 1
  )
) else (
  echo [1/3] Node.js 확인 — OK
  node -v
  npm -v
)

if not exist "frontend\package.json" (
  echo [2/3] frontend\package.json 없음 — docs\react-dev-setup.txt 참고
  pause
  exit /b 1
) else (
  echo [2/3] frontend\package.json — OK
)

echo.
echo [3/3] npm install (frontend)...
cd frontend
call npm install
if errorlevel 1 (
  echo [실패] npm install 실패
  cd ..
  pause
  exit /b 1
)
cd ..

echo.
echo === React 개발환경 셋업 완료 ===
echo   Node  : %NODE_HOME%
echo   앱    : frontend\
echo   실행  : run-react-dev.bat
echo.
pause
endlocal
