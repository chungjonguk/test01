@echo off
setlocal EnableExtensions
chcp 65001 >nul
set JAVA_HOME=C:\Users\chung\.local\dev\jdk-17
set PATH=%JAVA_HOME%\bin;C:\Users\chung\.local\dev\apache-maven-3.9.15\bin;%PATH%
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
cd /d "%~dp0"

echo.
echo === Spring Boot server (PC + mobile LAN, port 8081) ===
echo.

echo [1/5] MySQL check...
call "%~dp0scripts\ensure-mysql.bat"
if errorlevel 1 (
  echo.
  echo [STOP] DB connection failed.
  echo   Run start-mysql.bat or scripts\ensure-mysql.bat then retry.
  echo.
  pause
  exit /b 1
)
echo [1/5] DB OK

echo.
echo [2/5] Free port 8081...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":8081" ^| findstr "LISTENING"') do (
  echo   kill PID %%P
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo [3/5] LAN IP for mobile...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\mobile-dev\get-lan-ip.ps1"

echo.
echo [4/5] Clean target...
if exist "target" (
  rmdir /s /q "target"
  echo   target removed
) else (
  echo   no target folder
)

echo.
echo [5/5] Starting mvn spring-boot:run...
echo   PC:     http://localhost:8081/
echo   Mobile: http://LAN-IP:8081/  (same Wi-Fi, see step 3)
echo.
mvn clean spring-boot:run -DskipTests "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

endlocal
