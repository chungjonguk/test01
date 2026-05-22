@echo off
setlocal EnableExtensions
chcp 65001 >nul
set JAVA_HOME=C:\Users\chung\.local\dev\jdk-17
set PATH=%JAVA_HOME%\bin;C:\Users\chung\.local\dev\apache-maven-3.9.15\bin;%PATH%
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
cd /d "%~dp0"

echo.
echo === Spring Boot 서버 재시작 (MySQL 확인/기동 후 앱 실행) ===
echo.

echo [1/4] MySQL 기동 및 DB 접속 확인 (미기동 시 mysqld 자동 시작)...
call "%~dp0scripts\ensure-mysql.bat"
if errorlevel 1 (
  echo.
  echo [중단] DB 접속에 실패하여 Spring Boot를 시작하지 않습니다.
  echo   - start-mysql.bat 또는 scripts\ensure-mysql.bat 실행 후 다시 시도하세요.
  echo   - DBeaver: 127.0.0.1:3306, DB spring_boot_app, 사용자 redcroxx
  echo.
  pause
  exit /b 1
)
echo [1/4] DB 접속 OK — 이어서 앱 서버를 기동합니다.

echo.
echo [2/4] 8081 포트 사용 프로세스 종료...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":8081" ^| findstr "LISTENING"') do (
  echo   PID %%P 종료
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo.
echo [3/4] 빌드/템플릿 캐시 정리 (target 삭제)...
if exist "target" (
  rmdir /s /q "target"
  echo   target 폴더 삭제 완료
) else (
  echo   target 없음 — 건너뜀
)

echo.
echo [4/4] 서버 시작 (mvn clean spring-boot:run, UTF-8)...
echo.
mvn clean spring-boot:run -DskipTests "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

endlocal
