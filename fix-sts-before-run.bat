@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"

set "JAVA_HOME=C:\Users\chung\.local\dev\jdk-17"
set "PATH=%JAVA_HOME%\bin;C:\Users\chung\.local\dev\apache-maven-3.9.15\bin;%PATH%"
set "APP_STORAGE_NAS_BASE_PATH=E:/nas-storage/printmall"

echo.
echo === STS 실행 전 준비 (Maven compile + 8081 포트) ===
echo.

echo [1/3] 포트 8081·8082 사용 프로세스 종료...
for %%P in (8081 8082) do (
  for /f "tokens=5" %%Q in ('netstat -ano 2^>nul ^| findstr ":%%P" ^| findstr "LISTENING"') do (
    echo   kill port %%P PID %%Q
    taskkill /F /PID %%Q >nul 2>&1
  )
)
timeout /t 2 /nobreak >nul

echo [2/3] NAS 폴더 확인...
if not exist "E:\nas-storage\printmall\uploads\images" mkdir "E:\nas-storage\printmall\uploads\images"
if not exist "E:\nas-storage\printmall\uploads\documents" mkdir "E:\nas-storage\printmall\uploads\documents"
if not exist "E:\nas-storage\printmall\uploads\videos" mkdir "E:\nas-storage\printmall\uploads\videos"
if not exist "E:\nas-storage\printmall\uploads\products" mkdir "E:\nas-storage\printmall\uploads\products"

echo [3/3] Maven clean compile...
call mvn -q clean compile -DskipTests
if errorlevel 1 (
  echo [실패] Maven compile 오류 — STS에서 Maven ^> Update Project 후 재시도
  pause
  exit /b 1
)

echo.
echo 완료. STS에서 spring-boot-app ^> Maven ^> Update Project (Alt+F5) 후 실행하세요.
echo.
endlocal
