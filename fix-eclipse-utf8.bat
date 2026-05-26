@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo STS/워크스페이스 UTF-8 적용 중...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\eclipse\apply-workspace-utf8.ps1"
if errorlevel 1 pause
echo.
echo 완료. STS를 완전히 종료한 뒤 다시 실행하세요.
pause
