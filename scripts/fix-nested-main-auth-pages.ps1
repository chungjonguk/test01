# layout :: body + 중첩 <main> 구조를 auth-simple 단독 레이아웃으로 변환
$templatesRoot = Join-Path $PSScriptRoot "..\src\main\resources\templates"
$files = @(
    "pages\authentication\card\card-login.html",
    "pages\authentication\card\confirm-mail.html",
    "pages\authentication\card\forgot-password.html",
    "pages\authentication\card\lock-screen.html",
    "pages\authentication\card\logout.html",
    "pages\authentication\card\register.html",
    "pages\authentication\card\reset-password.html",
    "pages\authentication\split\confirm-mail.html",
    "pages\authentication\split\forgot-password.html",
    "pages\authentication\split\lock-screen.html",
    "pages\authentication\split\login.html",
    "pages\authentication\split\logout.html",
    "pages\authentication\split\register.html",
    "pages\authentication\split\reset-password.html",
    "pages\errors\404.html",
    "pages\errors\500.html"
)

$bodyClass = @{
    "pages\authentication\card\" = "auth-simple-page auth-card-page"
    "pages\authentication\split\" = "auth-simple-page auth-split-page"
    "pages\errors\" = "auth-simple-page"
}

foreach ($rel in $files) {
    $path = Join-Path $templatesRoot $rel
    if (-not (Test-Path $path)) { Write-Warning "Skip missing: $rel"; continue }

    $content = Get-Content $path -Raw -Encoding UTF8
    if ($content -notmatch '<main class="main"') { Write-Host "Skip (no nested main): $rel"; continue }

    $inner = $null
    if ($content -match '(?s)<main class="main" id="top">\s*(.*?)\s*(?:<div th:replace="~\{fragments/footer :: footer\}"></div>\s*)?</div>\s*</th:block>') {
        $inner = $matches[1].Trim()
    } else {
        Write-Warning "Could not extract inner HTML: $rel"
        continue
    }

    $class = "auth-simple-page"
    foreach ($key in $bodyClass.Keys) {
        if ($rel -like "$key*") { $class = $bodyClass[$key]; break }
    }

    $inner = $inner -replace 'href="\.\./\.\./\.\./index\.html"', 'th:href="@{/}"'
    $inner = $inner -replace 'href="\.\./\.\./\.\./pages/authentication/simple/login\.html"', 'th:href="@{/pages/authentication/simple/login}"'
    $inner = $inner -replace 'href="\.\./\.\./\.\./pages/authentication/card/login\.html"', 'th:href="@{/pages/authentication/card/card-login}"'
    $inner = $inner -replace 'href="\.\./\.\./\.\./pages/authentication/card/register\.html"', 'th:href="@{/pages/authentication/card/register}"'
    $inner = $inner -replace 'href="\.\./\.\./\.\./pages/authentication/card/forgot-password\.html"', 'th:href="@{/pages/authentication/card/forgot-password}"'
    $inner = $inner -replace 'href="\.\./\.\./\.\./pages/authentication/split/register\.html"', 'th:href="@{/pages/authentication/split/register}"'
    $inner = $inner -replace 'src="/assets/', 'th:src="@{/assets/'
    $inner = $inner -replace 'src="/assets/([^"]+)"', 'th:src="@{/assets/$1}"'
    $inner = $inner -replace 'th:src="@\{/assets/([^"]+)"(?! )', 'th:src="@{/assets/$1}"'
    # Fix double th:src if any
    $inner = $inner -replace 'th:src="@\{/assets/([^"]+)"\)"', 'th:src="@{/assets/$1}"'

    $out = @"
<!DOCTYPE html>
<html lang="ko" dir="ltr" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{fragments/auth-simple :: head}"></head>
<body class="$class">
    <th:block th:replace="~{fragments/app-loading :: bar}"></th:block>

    <main class="main" id="top">
$inner
    </main>

    <th:block th:replace="~{fragments/settings :: settings}"></th:block>
    <th:block th:replace="~{fragments/auth-simple :: scripts}"></th:block>
</body>
</html>
"@

    [System.IO.File]::WriteAllText($path, $out, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Fixed: $rel"
}
