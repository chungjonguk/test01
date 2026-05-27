$ErrorActionPreference = "Stop"
$billing = Join-Path (Split-Path $PSScriptRoot -Parent) "src\main\resources\templates\app\e-commerce\billing.html"
$text = [System.IO.File]::ReadAllText($billing, [System.Text.UTF8Encoding]::new($false))
$pat = '(?s)(<datalist class="scrollbar" id="country-list">).*?(</datalist>)'
$rep = '<th:block th:replace="~{fragments/code-datalist :: countryList(''country-list'')}"></th:block>'
$new = [regex]::Replace($text, $pat, $rep, 1)
if ($new -eq $text) { throw "billing datalist not patched" }
[System.IO.File]::WriteAllText($billing, $new, [System.Text.UTF8Encoding]::new($false))
Write-Host "patched billing.html datalist"
