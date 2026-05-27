# modules/forms/wizard.html — gender/country select -> 공통코드 fragment
$ErrorActionPreference = "Stop"
$wizard = Join-Path (Split-Path $PSScriptRoot -Parent) "src\main\resources\templates\modules\forms\wizard.html"
$text = [System.IO.File]::ReadAllText($wizard, [System.Text.UTF8Encoding]::new($false))

$genderIds = @(
    'bootstrap-wizard-gender',
    'bootstrap-wizard-validation-gender',
    'form-wizard-progress-gender',
    'form-wizard-gender'
)
$countryIds = @(
    'bootstrap-wizard-card-holder-country',
    'bootstrap-wizard-validation-card-holder-country',
    'form-wizard-progress-card-holder-country',
    'form-wizard-card-holder-country'
)

foreach ($id in $genderIds) {
    $pat = "(?s)<select class=`"form-select`" name=`"gender`" id=`"$id`">.*?</select>"
    $rep = "<th:block th:replace=`"~{fragments/wizard-form-selects :: gender('$id')}`"></th:block>"
    $text = [regex]::Replace($text, $pat, $rep, 1)
}

foreach ($id in $countryIds) {
    $pat = "(?s)<select class=`"form-select`" name=`"customSelectCountry`" id=`"$id`">.*?</select>"
    $rep = "<th:block th:replace=`"~{fragments/wizard-form-selects :: country('$id')}`"></th:block>"
    $text = [regex]::Replace($text, $pat, $rep, 1)
}

[System.IO.File]::WriteAllText($wizard, $text, [System.Text.UTF8Encoding]::new($false))
Write-Host "patched $wizard"
