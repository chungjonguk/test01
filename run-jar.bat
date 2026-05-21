@echo off
setlocal EnableExtensions
chcp 65001 >nul
set JAVA_HOME=C:\Users\chung\.local\dev\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"

set JAR=target\spring-boot-app-0.0.1-SNAPSHOT.jar
if not exist "%JAR%" (
  echo JAR 없음. 먼저 빌드: mvn clean package -DskipTests
  exit /b 1
)

echo.
echo === JAR 실행 (UTF-8) ===
echo.
java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "%JAR%"

endlocal
