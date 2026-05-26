# Apply sys_table_sequence / sys_table_random_id DDL, seed, and sync next_val
param(
    [string]$DbHost = "127.0.0.1",
    [int]$Port = 3306,
    [string]$Database = "spring_boot_app",
    [string]$User = "redcroxx",
    [string]$Password = "jonguk0412"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$schemaDir = Join-Path $projectRoot "src\main\resources\schema"
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"

if (-not (Test-Path $mysqlBin)) {
    $mysqlBin = "mysql"
}

function Invoke-MySqlFile {
    param([string]$FilePath)
    if (-not (Test-Path $FilePath)) {
        throw "Missing SQL file: $FilePath"
    }
    Write-Host "  -> $([System.IO.Path]::GetFileName($FilePath))" -ForegroundColor Gray
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $sql = Get-Content -LiteralPath $FilePath -Raw -Encoding UTF8
    $out = $sql | & $mysqlBin -h $DbHost -P $Port -u $User "-p$Password" --default-character-set=utf8mb4 $Database 2>&1
    $ErrorActionPreference = $prev
    foreach ($line in $out) {
        if ($line -match "ERROR") {
            throw $line
        }
    }
}

Write-Host "=== Apply table sequence scripts ===" -ForegroundColor Cyan
Invoke-MySqlFile (Join-Path $schemaDir "sys_table_sequence.sql")
Invoke-MySqlFile (Join-Path $schemaDir "sys_table_random_id.sql")
Invoke-MySqlFile (Join-Path $schemaDir "sys_table_sequence_seed.sql")

function Invoke-MySqlQuery {
    param([string]$Query)
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $result = & $mysqlBin -h $DbHost -P $Port -u $User "-p$Password" -N -B $Database -e $Query 2>$null
    $ErrorActionPreference = $prev
    return $result
}

Write-Host "Syncing next_val..." -ForegroundColor Green
$rows = Invoke-MySqlQuery "SELECT table_name, column_name FROM sys_table_sequence WHERE use_yn = 'Y';"
$synced = 0
foreach ($line in $rows) {
    if (-not $line) { continue }
    $parts = $line -split "`t"
    if ($parts.Count -lt 2) { continue }
    $table = $parts[0]
    $column = $parts[1]
    $exists = Invoke-MySqlQuery "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '$table';"
    if ($exists -ne "1") { continue }
    $sql = "UPDATE sys_table_sequence SET next_val = GREATEST(next_val, (SELECT COALESCE(MAX($column), 0) FROM $table)), update_id = 'SYSTEM' WHERE table_name = '$table';"
    Invoke-MySqlQuery $sql | Out-Null
    $synced++
}

$count = Invoke-MySqlQuery "SELECT COUNT(*) FROM sys_table_sequence;"
$domain = Invoke-MySqlQuery "SELECT seq_name, next_val FROM sys_table_sequence WHERE table_name = 'biz_company_domain';"

Write-Host "Done: sys_table_sequence rows=$count, synced=$synced" -ForegroundColor Green
if ($domain) {
    Write-Host "  biz_company_domain: $domain" -ForegroundColor Gray
}
