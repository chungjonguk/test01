# new-workspace .metadata 생성 + spring-boot-app 프로젝트 등록 (STS 최초 오픈 전 가능)
param(
    [string]$WorkspaceRoot = "",
    [string]$ProjectRoot = "",
    [string]$ProjectName = "spring-boot-app"
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $ProjectRoot) {
    $ProjectRoot = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
}
if (-not $WorkspaceRoot) {
    $WorkspaceRoot = Split-Path $ProjectRoot -Parent
}

$meta = Join-Path $WorkspaceRoot ".metadata"
$settingsDest = Join-Path $meta ".plugins\org.eclipse.core.runtime\.settings"
$projectsRoot = Join-Path $meta ".plugins\org.eclipse.core.resources\.projects"
$projectMeta = Join-Path $projectsRoot $ProjectName
$templateDir = Join-Path $PSScriptRoot "workspace"

function Write-JavaDataOutputUtf([string]$Text, [string]$OutFile) {
    $enc = [System.Text.Encoding]::UTF8
    $bytes = $enc.GetBytes($Text)
    if ($bytes.Length -gt 65535) { throw "URI too long for Eclipse .location" }
    $fs = [System.IO.File]::Create($OutFile)
    try {
        $bw = New-Object System.IO.BinaryWriter($fs)
        $bw.Write([uint16]$bytes.Length)
        $bw.Write($bytes)
        $bw.Flush()
    } finally {
        $fs.Close()
    }
}

New-Item -ItemType Directory -Force -Path $settingsDest | Out-Null
New-Item -ItemType Directory -Force -Path $projectMeta | Out-Null

$versionIni = Join-Path $meta "version.ini"
if (-not (Test-Path $versionIni)) {
    $stamp = (Get-Date).ToString("ddd MMM dd HH:mm:ss K yyyy")
    @(
        "#$stamp",
        "org.eclipse.core.runtime=1",
        "org.eclipse.platform=4.31.0"
    ) | Set-Content -Path $versionIni -Encoding ASCII
}

foreach ($file in Get-ChildItem $templateDir -Filter "*.prefs") {
    Copy-Item $file.FullName (Join-Path $settingsDest $file.Name) -Force
}

$projectUri = "file:///" + ($ProjectRoot -replace '\\', '/')
Write-JavaDataOutputUtf $projectUri (Join-Path $projectMeta ".location")

# 빈 마커 — Eclipse가 인덱스를 첫 기동 시 보완
New-Item -ItemType File -Force -Path (Join-Path $projectMeta ".markers") | Out-Null

Write-Host "워크스페이스 메타 생성: $meta" -ForegroundColor Green
Write-Host "  프로젝트 등록: $ProjectName -> $ProjectRoot" -ForegroundColor Gray
