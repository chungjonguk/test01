# wizard.html 국가 옵션 -> common_code_wizard_seed.sql
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$wizard = Join-Path $root "src\main\resources\templates\modules\forms\wizard.html"
$out = Join-Path $root "src\main\resources\schema\common_code_wizard_seed.sql"

$text = Get-Content $wizard -Raw -Encoding UTF8
if ($text -notmatch 'id="bootstrap-wizard-card-holder-country">([\s\S]*?)</select>') {
    throw "country block not found"
}
$block = $Matches[1]
$countries = [regex]::Matches($block, '<option value="([^"]*)">([^<]*)</option>') |
    ForEach-Object { @{ v = $_.Groups[1].Value; l = $_.Groups[2].Value.Trim() } } |
    Where-Object { $_.v }

$vals = @(
    "('WIZARD_GENDER', '|Select your gender ...', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Male|Male', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Female|Female', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Other|Other', 'Y', 'SYSTEM', 'SYSTEM')",
    "('BIRTH_MONTH', '|' + [char]0xC6D4 + '', 'Y', 'SYSTEM', 'SYSTEM')"
)
1..12 | ForEach-Object {
    $v = '{0:D2}' -f $_
    $label = ('{0}월' -f $_)
    $vals += "('BIRTH_MONTH', '$v|$label', 'Y', 'SYSTEM', 'SYSTEM')"
}
$vals += "('COUNTRY_LIST', '|Select your country ...', 'Y', 'SYSTEM', 'SYSTEM')"
foreach ($c in $countries) {
    $esc = $c.v -replace "'", "''"
    $vals += "('COUNTRY_LIST', '$esc|$esc', 'Y', 'SYSTEM', 'SYSTEM')"
}

$sql = @(
    "-- wizard / authentication wizard form-select 공통코드",
    "",
    "INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES",
    "('WIZARD_GENDER', 'Wizard-Gender', 'Y', 'SYSTEM', 'SYSTEM'),",
    "('COUNTRY_LIST', 'Country-List', 'Y', 'SYSTEM', 'SYSTEM'),",
    "('BIRTH_MONTH', 'Birth-Month', 'Y', 'SYSTEM', 'SYSTEM')",
    "ON DUPLICATE KEY UPDATE code_nm = VALUES(code_nm), use_yn = VALUES(use_yn), update_id = VALUES(update_id);",
    "",
    "INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES",
    ($vals -join ",`n"),
    "ON DUPLICATE KEY UPDATE use_yn = VALUES(use_yn), update_id = VALUES(update_id);",
    ""
) -join "`n"

[System.IO.File]::WriteAllText($out, $sql, [System.Text.UTF8Encoding]::new($false))
Write-Host "wrote $out ($($countries.Count) countries)"
