# SpringToolsForEclipse.ini — IDE·콘솔 UTF-8 (한글 깨짐 방지)
param(
    [string]$StsIni = ""
)

$ErrorActionPreference = "Stop"
chcp 65001 | Out-Null

if (-not $StsIni) {
    $paths = & (Join-Path $PSScriptRoot "Resolve-EclipsePaths.ps1")
    if ($paths.StsExe) {
        $StsIni = Join-Path (Split-Path $paths.StsExe -Parent) "SpringToolsForEclipse.ini"
    }
}
if (-not $StsIni -or -not (Test-Path $StsIni)) {
    Write-Host "[오류] SpringToolsForEclipse.ini 를 찾지 못했습니다." -ForegroundColor Red
    exit 1
}

$vmArgs = @(
    "-Dclient.encoding.override=UTF-8",
    "-Dfile.encoding=UTF-8",
    "-Dsun.jnu.encoding=UTF-8",
    "-Dsun.stdout.encoding=UTF-8",
    "-Dsun.stderr.encoding=UTF-8"
)

$lines = [System.IO.File]::ReadAllLines($StsIni)
$out = New-Object System.Collections.Generic.List[string]
$inVmArgs = $false
$added = @{}

foreach ($line in $lines) {
    $out.Add($line)
    if ($line.Trim() -eq "-vmargs") { $inVmArgs = $true }
    if ($inVmArgs -and $line.Trim().StartsWith("-Dfile.encoding=UTF-8")) {
        foreach ($a in $vmArgs) { $added[$a] = $true }
    }
}

$changed = $false
$needClient = -not ($lines | Where-Object { $_.Trim() -eq "-Dclient.encoding.override=UTF-8" })
if ($needClient -and ($lines | Where-Object { $_.Trim() -eq "-Dfile.encoding=UTF-8" })) {
    $out.Add("-Dclient.encoding.override=UTF-8")
    $changed = $true
}

if (-not $added.ContainsKey("-Dfile.encoding=UTF-8")) {
    if (-not $inVmArgs) { $out.Add("-vmargs") }
    foreach ($a in $vmArgs) { $out.Add($a) }
    $changed = $true
}

if ($changed) {
    $backup = "$StsIni.bak-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    Copy-Item $StsIni $backup -Force
    [System.IO.File]::WriteAllLines($StsIni, $out.ToArray())
    Write-Host "STS ini UTF-8 적용: $StsIni" -ForegroundColor Green
    Write-Host "  백업: $backup" -ForegroundColor Gray
    Write-Host "  STS를 완전히 종료한 뒤 다시 실행하세요." -ForegroundColor Yellow
} else {
    Write-Host "STS ini UTF-8 설정이 이미 반영되어 있습니다." -ForegroundColor Gray
}
