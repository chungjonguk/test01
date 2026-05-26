백업 일시: {{TS}}
생성: scripts/backup/create-backup.ps1

[install/]
  dev-tools_{{TS}}.zip
    - jdk-17, apache-maven-3.9.15, node-22
    - maven.zip, temurin17.zip, node-v22.15.1-win-x64.zip
  mysql-8.4.9-winx64.msi / .zip  — MySQL Server 8.4 공식 설치파일 (있을 때)
  mysql-server-8.4-installed_*.zip — PC에 설치된 MySQL 8.4 폴더 백업 (MSI 없을 때)
  mysql-setup_{{TS}}.zip            — DB 초기화 SQL·my.ini·설치 배치
  dbeaver-ce-25.3.1-x86_64-setup.exe / .zip — DBeaver Community 설치파일
  dbeaver-setup_{{TS}}.zip          — MySQL 연결 설정·가이드

[projects/]
  spring-boot-app-fixed_{{TS}}.zip  — PrintMall (target/node_modules 제외)
  stock-mock-trading_{{TS}}.zip     — 모의투자
  kotlin-hello_{{TS}}.zip           — Kotlin 샘플

[database/]
  spring_boot_app_{{TS}}.sql        — MySQL 덤프 (기동 시에만 생성)

[docs/]
  01-설치방법.txt, 02-셋팅방법.txt, 03~05, project-accounts.txt 등

복원:
  가이드   -> D:\backup\docs\01-설치방법.txt, 03-mysql-설치방법.txt
  MySQL    -> install\mysql-8.4.9-winx64.msi 설치 후 mysql-setup zip 실행
  설치파일 -> %USERPROFILE%\.local\dev\ 에 압축 해제
  프로젝트 -> workspace 폴더에 압축 해제
  DB       -> mysql -u redcroxx -p spring_boot_app < spring_boot_app_*.sql
