@echo off
chcp 65001 >nul
setlocal EnableExtensions
cd /d "%~dp0"

echo.
echo === 백업 + Eclipse 연동 점검 ===
echo.

echo [1/3] JDK 17 Eclipse 설정...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\eclipse\apply-jdk17.ps1"
if errorlevel 1 goto :fail

echo.
echo [2/3] 프로젝트 백업 (D:\backup\projects)...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\backup\backup-project-quick.ps1"
if errorlevel 1 goto :fail

echo.
echo [3/3] Eclipse 연동 파일 점검...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\eclipse\verify-eclipse-setup.ps1"
if errorlevel 1 goto :fail

echo.
echo 완료. STS: open-sts-workspace.bat ^> Maven Update Project ^> spring-boot-app-java 실행
echo.
pause
exit /b 0

:fail
echo.
echo [STOP] 위 단계를 확인한 뒤 다시 실행하세요.
pause
exit /b 1
