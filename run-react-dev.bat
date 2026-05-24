@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"

set NODE_HOME=C:\Users\chung\.local\dev\node-22
set PATH=%NODE_HOME%;%PATH%

if not exist "frontend\package.json" (
  echo frontend 프로젝트가 없습니다. 먼저 scripts\react-dev\setup-react-dev.bat 를 실행하세요.
  pause
  exit /b 1
)

where node >nul 2>&1
if errorlevel 1 (
  echo Node.js가 없습니다. scripts\react-dev\setup-react-dev.bat 를 실행하세요.
  pause
  exit /b 1
)

echo.
echo === React 개발 서버 (Vite) ===
echo   UI   : http://localhost:5173/
echo   API  : Spring Boot http://localhost:8081/ (proxy /api)
echo   백엔드가 꺼져 있으면 run-server.bat 으로 먼저 기동하세요.
echo.

cd frontend
call npm run dev
endlocal
