백업 일시: {{TS}}
원본 경로: {{PROJECT_ROOT}}
압축 파일: {{ZIP}}

포함: 소스, pom.xml, Eclipse(.project/.classpath/.settings/.launch), scripts
제외: target, node_modules, logs, .git, .dbeaver

Eclipse 복원:
  1) 압축 해제 후 STS 워크스페이스(new-workspace)에 폴더 배치
  2) open-sts-workspace.bat 또는 Import Maven Project
  3) scripts\eclipse\apply-jdk17.ps1 실행 (JDK 경로)
  4) Maven Update Project (Alt+F5)
