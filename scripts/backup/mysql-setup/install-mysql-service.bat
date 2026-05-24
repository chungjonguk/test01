@echo off
setlocal EnableExtensions
chcp 65001 >nul

rem MySQL ZIP 설치 후 Windows 서비스 등록 (관리자 CMD에서 실행)
set MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 8.4
set MYINI=C:\ProgramData\MySQL\MySQL Server 8.4\my.ini

if not exist "%MYSQL_HOME%\bin\mysqld.exe" (
  echo mysqld.exe 없음: %MYSQL_HOME%
  echo ZIP을 해당 경로에 압축 해제했는지 확인하세요.
  pause
  exit /b 1
)

if not exist "%MYINI%" (
  echo my.ini 없음: %MYINI%
  echo my.ini.example 을 복사한 뒤 basedir/datadir 을 수정하세요.
  pause
  exit /b 1
)

echo MySQL84 서비스 등록...
"%MYSQL_HOME%\bin\mysqld.exe" --install MySQL84 --defaults-file="%MYINI%"
if errorlevel 1 (
  echo 서비스 등록 실패 — 관리자 권한으로 실행했는지 확인하세요.
  pause
  exit /b 1
)

echo 서비스 시작...
net start MySQL84
echo.
echo 완료. mysql -u root -p 로 접속 후 mysql-init-redcroxx.sql 을 실행하세요.
pause
endlocal
