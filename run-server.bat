@echo off
setlocal EnableExtensions
chcp 65001 >nul
set JAVA_HOME=C:\Users\chung\.local\dev\jdk-17
set PATH=%JAVA_HOME%\bin;C:\Users\chung\.local\dev\apache-maven-3.9.15\bin;%PATH%
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
cd /d "%~dp0"

echo.
echo === Spring Boot 서버 재시작 (UTF-8, 캐시 정리 포함) ===
echo.

echo [1/3] 8081 포트 사용 프로세스 종료...
for /f "tokens=5" %%P in ('netstat -ano 2^>nul ^| findstr ":8081" ^| findstr "LISTENING"') do (
  echo   PID %%P 종료
  taskkill /F /PID %%P >nul 2>&1
)
timeout /t 2 /nobreak >nul

echo [2/3] 빌드/템플릿 캐시 정리 (target 삭제)...
if exist "target" (
  rmdir /s /q "target"
  echo   target 폴더 삭제 완료
) else (
  echo   target 없음 — 건너뜀
)

echo [3/3] 서버 시작 (mvn clean spring-boot:run, UTF-8)...
echo.
mvn clean spring-boot:run -DskipTests "-Dspring-boot.run.jvmArguments=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

endlocal
