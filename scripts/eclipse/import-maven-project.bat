@echo off
chcp 65001 >nul
setlocal EnableExtensions
cd /d "%~dp0..\.."

echo.
echo === Eclipse / STS — Maven 프로젝트 Import (1회) ===
echo.
echo  [워크스페이스]  %~dp0..\..\..   (new-workspace)
echo  [프로젝트 루트]  %CD%
echo  [Eclipse 이름]   spring-boot-app  (폴더명: spring-boot-app-fixed)
echo.
echo  1) open-sts-workspace.bat 실행 — STS가 new-workspace 로 열립니다.
echo  2) File ^> Import ^> Maven ^> Existing Maven Projects
echo  3) Root Directory 에 위 [프로젝트 루트] 선택
echo  4) Projects: spring-boot-app 체크 ^> Finish
echo  5) 프로젝트 우클릭 ^> Maven ^> Update Project (Alt+F5, Force Update)
echo  6) Run ^> spring-boot-app-java 또는 spring-boot-app-maven
echo.
echo  상세: docs\eclipse-setup.txt
echo.
pause
