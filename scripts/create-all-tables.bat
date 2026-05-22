@echo off
setlocal EnableExtensions
chcp 65001 >nul
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
set "SCHEMA=%~dp0..\src\main\resources\schema"
cd /d "%~dp0.."

echo.
echo === spring_boot_app 전체 테이블 생성 ===
echo.

call "%~dp0ensure-mysql.bat"
if errorlevel 1 (
  echo [중단] MySQL 접속 실패
  pause
  exit /b 1
)

echo [1] DB 확인...
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 < "%SCHEMA%\00_create_database.sql"
if errorlevel 1 goto :fail

echo [2] 핵심 테이블 (user, common_code, screen_list, screen_table_map)...
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\user.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\user_access_log.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\ecm_payment.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\common_code.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\screen_list.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\screen_table_map.sql"
if errorlevel 1 goto :fail

echo [3] 업무 테이블 (e-commerce, lms, email, ...)...
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\tables\biz_schema.sql"
if errorlevel 1 goto :fail

echo.
echo [4] 샘플 데이터 적용? (Y/N)
set /p SEED=선택:
if /i "%SEED%"=="Y" (
  "%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\common_code_seed.sql"
  "%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\common_code_combo_seed.sql"
  "%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%SCHEMA%\tables\biz_seed.sql"
  echo   시드 완료. screen_table_map 은 앱 기동 시 자동 갱신됩니다.
)

echo.
echo === 테이블 생성 완료 ===
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 -N -e "SHOW TABLES;" spring_boot_app
echo.
pause
exit /b 0

:fail
echo [실패] SQL 실행 오류 — DBeaver에서 schema\00_create_all_tables.sql 실행을 시도하세요.
pause
exit /b 1
endlocal
