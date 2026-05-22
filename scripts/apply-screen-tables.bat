@echo off
setlocal EnableExtensions
chcp 65001 >nul
set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
set "ROOT=%~dp0.."
cd /d "%ROOT%"

echo === 업무 테이블만 빠르게 적용 (전체는 create-all-tables.bat) ===
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%ROOT%\src\main\resources\schema\common_code.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%ROOT%\src\main\resources\schema\screen_table_map.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%ROOT%\src\main\resources\schema\user_access_log.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%ROOT%\src\main\resources\schema\tables\biz_schema.sql"
"%MYSQL_BIN%" -h 127.0.0.1 -u redcroxx -pjonguk0412 --default-character-set=utf8mb4 spring_boot_app < "%ROOT%\src\main\resources\schema\tables\biz_seed.sql"
echo 완료. 전체 생성: scripts\create-all-tables.bat
pause
endlocal
