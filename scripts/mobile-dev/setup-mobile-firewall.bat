@echo off
setlocal EnableExtensions
chcp 65001 >nul

net session >nul 2>&1
if errorlevel 1 (
  echo [오류] 관리자 권한이 필요합니다.
  echo   이 파일을 우클릭 → "관리자 권한으로 실행"
  pause
  exit /b 1
)

echo.
echo === 모바일 테스트용 Windows 방화벽 규칙 추가 ===
echo   TCP 8081 PrintMall
echo   TCP 8082 모의투자
echo.

netsh advfirewall firewall delete rule name="PrintMall Mobile 8081" >nul 2>&1
netsh advfirewall firewall delete rule name="StockMock Mobile 8082" >nul 2>&1

netsh advfirewall firewall add rule name="PrintMall Mobile 8081" dir=in action=allow protocol=TCP localport=8081
netsh advfirewall firewall add rule name="StockMock Mobile 8082" dir=in action=allow protocol=TCP localport=8082

if errorlevel 1 (
  echo [실패] 방화벽 규칙 추가 실패
  pause
  exit /b 1
)

echo [완료] 방화벽 규칙이 추가되었습니다.
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0get-lan-ip.ps1"
pause
