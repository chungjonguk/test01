# sts-path.local.txt / sts-workspace.local.txt 로 STS·워크스페이스 경로 해석
param(
    [string]$ScriptsDir = $PSScriptRoot,
    [string]$ProjectRoot = (Split-Path (Split-Path $ScriptsDir -Parent) -Parent)
)

function Read-OneLinePath([string]$File) {
    if (-not (Test-Path $File)) { return $null }
    $line = (Get-Content $File -TotalCount 1 -ErrorAction SilentlyContinue)
    if ($line) { return $line.Trim() }
    return $null
}

$stsFile = Join-Path $ScriptsDir "sts-path.local.txt"
$wsFile = Join-Path $ScriptsDir "sts-workspace.local.txt"

$stsExe = Read-OneLinePath $stsFile
if (-not $stsExe) {
    $candidates = @(
        "C:\Users\chung\.local\dev\sts-5.1.1\sts-5.1.1.RELEASE\SpringToolsForEclipse.exe",
        "C:\Users\chung\.local\dev\sts-5.0.1\sts-5.0.1.RELEASE\SpringToolsForEclipse.exe",
        "C:\Program Files\Spring Tool Suite 4\SpringToolSuite4.exe",
        "C:\sts-4\SpringToolSuite4.exe"
    )
    $stsExe = ($candidates | Where-Object { Test-Path $_ } | Select-Object -First 1)
}

$workspaceRoot = Read-OneLinePath $wsFile
if (-not $workspaceRoot) {
    $workspaceRoot = "E:\sts-workspace"
    if (-not (Test-Path (Split-Path $workspaceRoot -Parent))) {
        $workspaceRoot = Split-Path $ProjectRoot -Parent
    }
}

[PSCustomObject]@{
    StsExe          = $stsExe
    WorkspaceRoot   = $workspaceRoot
    ProjectRoot     = $ProjectRoot
    ProjectName     = "spring-boot-app"
}
