@echo off
chcp 65001 >nul
setlocal EnableExtensions
cd /d "%~dp0..\.."

echo.
echo === Eclipse / STS — Maven 프로젝트 Import (1회) ===
echo.
echo  [워크스페이스]  sts-workspace.local.txt (기본 C:\workplace\sts-workspace)
echo  [프로젝트 루트]  %CD%
echo  [Eclipse 이름]   spring-boot-app  (폴더: workspace 루트)
echo.
echo  [자동] sync-eclipse-workspace.bat — Maven compile + .metadata + 프로젝트 등록
echo  1) open-sts-workspace.bat — STS가 C:\util\sts-5.1.1.RELEASE 로 워크스페이스를 엽니다.
echo  2) Package Explorer에 spring-boot-app 이 보이면 Import 생략
echo     없으면: File ^> Import ^> Maven ^> Existing Maven Projects
echo     Root Directory = [프로젝트 루트], spring-boot-app 체크 ^> Finish
echo  3) Maven ^> Update Project (Alt+F5, Force Update) — sync 후 1회 확인
echo  4) Run ^> spring-boot-app-java 또는 spring-boot-app-maven
echo.
echo  상세: docs\eclipse-setup.txt
echo.
pause
