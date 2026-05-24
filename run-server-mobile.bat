@echo off
setlocal EnableExtensions
chcp 65001 >nul
set JAVA_HOME=C:\Users\chung\.local\dev\jdk-17
set PATH=%JAVA_HOME%\bin;C:\Users\chung\.local\dev\apache-maven-3.9.15\bin;%PATH%
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
cd /d "%~dp0"

echo.
echo === PrintMall 모바일 개발 서버 (8081, LAN 접속 허용) ===
echo.

echo [1/5] MySQL 확인...
call "%~dp0scripts\ensure-mysql.bat"
if errorlevel 1 (
  echo [중단] DB 접속 실패
  pause
  exit /b 1
)

echo.
echo [2/5] 8081 포트 정리...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":8081" ^| findstr "LISTENING"') do (
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo [3/5] PC IP (휴대폰 접속 주소)...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\mobile-dev\get-lan-ip.ps1"

echo [4/5] target 정리...
if exist "target" rmdir /s /q "target"

echo.
echo [5/5] 서버 시작 (profile=mobile, 0.0.0.0:8081)...
echo   PC:    http://localhost:8081/
echo   폰:    http://^<위 IP^>:8081/  ^(같은 Wi-Fi^)
echo.
mvn clean spring-boot:run -DskipTests ^
  "-Dspring-boot.run.profiles=mobile" ^
  "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

endlocal
