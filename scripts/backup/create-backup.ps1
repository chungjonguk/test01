# PrintMall / 개발환경 백업 — 설치파일·프로젝트 분리 압축
param(
    [string]$BackupRoot = "D:\backup"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

$ts = Get-Date -Format "yyyyMMdd_HHmmss"
$installDir = Join-Path $BackupRoot "install"
$projectDir = Join-Path $BackupRoot "projects"
$dataDir = Join-Path $BackupRoot "database"
$docsDir = Join-Path $BackupRoot "docs"
$devRoot = Join-Path $env:USERPROFILE ".local\dev"
$workspaceRoot = "D:\spirngboot_workspace\spirngboot_workspace\new-workspace"
$projectRoot = Join-Path $workspaceRoot "spring-boot-app-fixed"

New-Item -ItemType Directory -Force -Path $installDir, $projectDir, $dataDir, $docsDir | Out-Null

Write-Host ""
Write-Host "=== 백업 시작 ($ts) ===" -ForegroundColor Cyan
Write-Host "  설치파일 -> $installDir" -ForegroundColor Gray
Write-Host "  프로젝트 -> $projectDir" -ForegroundColor Gray
Write-Host "  DB 덤프   -> $dataDir" -ForegroundColor Gray
Write-Host ""

# --- 1. 설치파일 (JDK, Maven, Node + 설치 zip) ---
$installZip = Join-Path $installDir "dev-tools_$ts.zip"
$installItems = @(
    "jdk-17",
    "apache-maven-3.9.15",
    "node-22",
    "maven.zip",
    "temurin17.zip",
    "node-v22.15.1-win-x64.zip"
)

Push-Location $devRoot
try {
    $existing = @()
    foreach ($item in $installItems) {
        if (Test-Path (Join-Path $devRoot $item)) {
            $existing += $item
        } else {
            Write-Host "  [건너뜀] $item 없음" -ForegroundColor Yellow
        }
    }
    if ($existing.Count -eq 0) {
        throw "설치파일 대상이 없습니다: $devRoot"
    }
    Write-Host "[1/6] 설치파일 압축 중... ($($existing.Count)개)" -ForegroundColor Green
    & tar -acf $installZip @existing
    $installSize = [math]::Round((Get-Item $installZip).Length / 1MB, 1)
    Write-Host "  -> $installZip ($installSize MB)" -ForegroundColor Gray
} finally {
    Pop-Location
}

# --- 1b. MySQL 설치파일 (MSI/ZIP) ---
Write-Host "[1b/6] MySQL 설치파일 확인..." -ForegroundColor Green
$downloadScript = Join-Path $projectRoot "scripts\backup\download-mysql-installer.ps1"
if (Test-Path $downloadScript) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $downloadScript -DestDir $installDir
}
$mysqlInstallFiles = @(
    "mysql-8.4.9-winx64.msi",
    "mysql-8.4.9-winx64.zip"
)
foreach ($mf in $mysqlInstallFiles) {
    $mfPath = Join-Path $installDir $mf
    if (Test-Path $mfPath) {
        $mb = [math]::Round((Get-Item $mfPath).Length / 1MB, 1)
        Write-Host "  -> install\$mf ($mb MB)" -ForegroundColor Gray
    } else {
        Write-Host "  [없음] install\$mf" -ForegroundColor Yellow
    }
}

$hasOfficialInstaller = (Test-Path (Join-Path $installDir "mysql-8.4.9-winx64.msi")) -or `
    (Test-Path (Join-Path $installDir "mysql-8.4.9-winx64.zip"))
$mysqlInstalled = "C:\Program Files\MySQL\MySQL Server 8.4"
if (-not $hasOfficialInstaller -and (Test-Path $mysqlInstalled)) {
    $installedZip = Join-Path $installDir "mysql-server-8.4-installed_$ts.zip"
    Write-Host "  공식 MSI/ZIP 없음 — 설치된 MySQL 폴더 백업 중..." -ForegroundColor Yellow
    Push-Location "C:\Program Files\MySQL"
    try {
        & tar -acf $installedZip "MySQL Server 8.4"
        $instMb = [math]::Round((Get-Item $installedZip).Length / 1MB, 1)
        Write-Host "  -> mysql-server-8.4-installed_$ts.zip ($instMb MB)" -ForegroundColor Gray
    } finally {
        Pop-Location
    }
    $myIni = "C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"
    if (Test-Path $myIni) {
        Copy-Item $myIni (Join-Path $installDir "my.ini.backup") -Force
        Write-Host "  -> install\my.ini.backup" -ForegroundColor Gray
    }
}

# --- 1c. MySQL 셋업 스크립트 묶음 ---
Write-Host "[1c/6] MySQL 셋업 스크립트 압축..." -ForegroundColor Green
$mysqlSetupDir = Join-Path $projectRoot "scripts\backup\mysql-setup"
$mysqlSetupZip = Join-Path $installDir "mysql-setup_$ts.zip"
if (Test-Path $mysqlSetupDir) {
    Push-Location $mysqlSetupDir
    try {
        & tar -acf $mysqlSetupZip .
        $setupSize = [math]::Round((Get-Item $mysqlSetupZip).Length / 1KB, 1)
        Write-Host "  -> $mysqlSetupZip ($setupSize KB)" -ForegroundColor Gray
    } finally {
        Pop-Location
    }
}

# --- 1d. DBeaver 설치파일 + 셋업 ---
Write-Host "[1d/6] DBeaver 설치파일..." -ForegroundColor Green
$dbDownload = Join-Path $projectRoot "scripts\backup\download-dbeaver-installer.ps1"
if (Test-Path $dbDownload) {
    & powershell -NoProfile -ExecutionPolicy Bypass -File $dbDownload -DestDir $installDir
}
$dbExe = Join-Path $installDir "dbeaver-ce-25.3.1-x86_64-setup.exe"
$dbZip = Join-Path $installDir "dbeaver-ce-25.3.1-win32.win32.x86_64.zip"
foreach ($df in @($dbExe, $dbZip)) {
    if (Test-Path $df) {
        $mb = [math]::Round((Get-Item $df).Length / 1MB, 1)
        Write-Host "  -> install\$([System.IO.Path]::GetFileName($df)) ($mb MB)" -ForegroundColor Gray
    }
}
$dbSetupDir = Join-Path $projectRoot "scripts\backup\dbeaver-setup"
$dbSetupZip = Join-Path $installDir "dbeaver-setup_$ts.zip"
if (Test-Path $dbSetupDir) {
    Push-Location $dbSetupDir
    try {
        & tar -acf $dbSetupZip .
        $dbSetupKb = [math]::Round((Get-Item $dbSetupZip).Length / 1KB, 1)
        Write-Host "  -> dbeaver-setup_$ts.zip ($dbSetupKb KB)" -ForegroundColor Gray
    } finally {
        Pop-Location
    }
}

# --- 2. MySQL DB 덤프 (기동 중일 때) ---
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqldump.exe"
$dumpFile = Join-Path $dataDir "spring_boot_app_$ts.sql"
if (Test-Path $mysqlBin) {
    $listening = netstat -ano 2>$null | Select-String ":3306" | Select-String "LISTENING"
    if ($listening) {
        Write-Host "[2/6] DB 덤프 중..." -ForegroundColor Green
        $dumpArgs = @(
            "-h", "127.0.0.1",
            "-u", "redcroxx",
            "-pjonguk0412",
            "--single-transaction",
            "--routines",
            "--triggers",
            "spring_boot_app"
        )
        $prevEap = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        & $mysqlBin @dumpArgs 2>$null | Out-File -FilePath $dumpFile -Encoding utf8
        $ErrorActionPreference = $prevEap
        if (Test-Path $dumpFile) {
            $dumpSize = [math]::Round((Get-Item $dumpFile).Length / 1KB, 1)
            Write-Host "  -> $dumpFile ($dumpSize KB)" -ForegroundColor Gray
        }
    } else {
        Write-Host "[2/6] MySQL 미기동 — DB 덤프 건너뜀" -ForegroundColor Yellow
    }
} else {
    Write-Host "[2/6] mysqldump 없음 — DB 덤프 건너뜀" -ForegroundColor Yellow
}

# --- 3. 프로젝트 소스 (target, node_modules 제외) ---
$projects = @("spring-boot-app-fixed", "stock-mock-trading", "kotlin-hello")
$exclude = @(
    "--exclude=target",
    "--exclude=node_modules",
    "--exclude=frontend/node_modules",
    "--exclude=frontend/dist",
    "--exclude=frontend/.vite",
    "--exclude=logs",
    "--exclude=.metadata",
    "--exclude=.recommenders"
)

Write-Host "[3/6] 프로젝트 압축 중..." -ForegroundColor Green
foreach ($proj in $projects) {
    $src = Join-Path $workspaceRoot $proj
    if (-not (Test-Path $src)) {
        Write-Host "  [건너뜀] $proj 없음" -ForegroundColor Yellow
        continue
    }
    $projZip = Join-Path $projectDir "${proj}_$ts.zip"
    Push-Location $src
    try {
        & tar -acf $projZip @exclude .
        $projSize = [math]::Round((Get-Item $projZip).Length / 1MB, 1)
        Write-Host "  -> $projZip ($projSize MB)" -ForegroundColor Gray
    } finally {
        Pop-Location
    }
}

# --- 4. 설치·셋팅 가이드 복사 ---
Write-Host "[4/6] 설치·셋팅 가이드 복사..." -ForegroundColor Green
$guideSources = @(
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\00-백업폴더-구성.txt"; Dst = "00-백업폴더-구성.txt" },
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\01-설치방법.txt"; Dst = "01-설치방법.txt" },
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\02-셋팅방법.txt"; Dst = "02-셋팅방법.txt" },
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\03-mysql-설치방법.txt"; Dst = "03-mysql-설치방법.txt" },
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\04-dbeaver-설치방법.txt"; Dst = "04-dbeaver-설치방법.txt" },
    @{ Src = Join-Path $projectRoot "scripts\backup\guides\05-계정정보.txt"; Dst = "05-계정정보.txt" },
    @{ Src = Join-Path $projectRoot "docs\project-accounts.txt"; Dst = "project-accounts.txt" },
    @{ Src = Join-Path $projectRoot "docs\eclipse-setup.txt"; Dst = "eclipse-setup.txt" },
    @{ Src = Join-Path $projectRoot "docs\react-dev-setup.txt"; Dst = "react-dev-setup.txt" },
    @{ Src = Join-Path $projectRoot "docs\mobile-dev-setup.txt"; Dst = "mobile-dev-setup.txt" }
)
foreach ($g in $guideSources) {
    if (Test-Path $g.Src) {
        Copy-Item $g.Src (Join-Path $docsDir $g.Dst) -Force
        Write-Host "  -> docs\$($g.Dst)" -ForegroundColor Gray
    }
}
$readmeMain = Join-Path $projectRoot "scripts\backup\guides\README.txt"
if (Test-Path $readmeMain) {
    Copy-Item $readmeMain (Join-Path $BackupRoot "README.txt") -Force
}

# --- README (백업 시점) ---
$readme = @"
백업 일시: $ts
생성: scripts/backup/create-backup.ps1

[install/]
  dev-tools_$ts.zip
    - jdk-17, apache-maven-3.9.15, node-22
    - maven.zip, temurin17.zip, node-v22.15.1-win-x64.zip
  mysql-8.4.9-winx64.msi / .zip  — MySQL Server 8.4 공식 설치파일 (있을 때)
  mysql-server-8.4-installed_*.zip — PC에 설치된 MySQL 8.4 폴더 백업 (MSI 없을 때)
  mysql-setup_$ts.zip            — DB 초기화 SQL·my.ini·설치 배치
  dbeaver-ce-25.3.1-x86_64-setup.exe / .zip — DBeaver Community 설치파일
  dbeaver-setup_$ts.zip          — MySQL 연결 설정·가이드

[projects/]
  spring-boot-app-fixed_$ts.zip  — PrintMall (target/node_modules 제외)
  stock-mock-trading_$ts.zip     — 모의투자
  kotlin-hello_$ts.zip           — Kotlin 샘플

[database/]
  spring_boot_app_$ts.sql        — MySQL 덤프 (기동 시에만 생성)

[docs/]
  01-설치방법.txt, 02-셋팅방법.txt, 03~05, project-accounts.txt 등

복원:
  가이드   -> D:\backup\docs\01-설치방법.txt, 03-mysql-설치방법.txt
  MySQL    -> install\mysql-8.4.9-winx64.msi 설치 후 mysql-setup zip 실행
  설치파일 -> %USERPROFILE%\.local\dev\ 에 압축 해제
  프로젝트 -> workspace 폴더에 압축 해제
  DB       -> mysql -u redcroxx -p spring_boot_app < spring_boot_app_*.sql
"@
$readme | Out-File -FilePath (Join-Path $BackupRoot "README_$ts.txt") -Encoding utf8

Write-Host ""
Write-Host "=== 백업 완료 ===" -ForegroundColor Cyan
Write-Host "  $BackupRoot" -ForegroundColor Green
Write-Host ""
