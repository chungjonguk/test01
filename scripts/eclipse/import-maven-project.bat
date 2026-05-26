@echo off
chcp 65001 >nul
setlocal EnableExtensions
cd /d "%~dp0..\.."

echo.
echo === Eclipse / STS — Maven 프로젝트 Import (1회) ===
echo.
echo  [워크스페이스]  D:\sts-workspace  (sts-workspace.local.txt)
echo  [프로젝트 루트]  %CD%
echo  [Eclipse 이름]   spring-boot-app  (폴더명: spring-boot-app-fixed)
echo.
echo  [자동] sync-eclipse-workspace.bat — Maven compile + .metadata + 프로젝트 등록
echo  1) open-sts-workspace.bat — STS가 D:\sts-workspace 로 열립니다.
echo  2) Package Explorer에 spring-boot-app 이 보이면 Import 생략
echo     없으면: File ^> Import ^> Maven ^> Existing Maven Projects
echo     Root Directory = [프로젝트 루트], spring-boot-app 체크 ^> Finish
echo  3) Maven ^> Update Project (Alt+F5, Force Update) — sync 후 1회 확인
echo  4) Run ^> spring-boot-app-java 또는 spring-boot-app-maven
echo.
echo  상세: docs\eclipse-setup.txt
echo.
pause
