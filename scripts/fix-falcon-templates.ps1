$root = Split-Path -Parent $PSScriptRoot
$templates = Join-Path $root "src\main\resources\templates"
$suffix = @"
        <div th:replace="~{fragments/footer :: footer}"></div>
    </div>
    </th:block>
</body>
</html>
"@

$markers = @(
    '<footer class="footer"',
    '<div class="modal fade" id="authentication-modal"',
    '</main>',
    '<!--    End of Main Content-->',
    'id="settings-offcanvas"',
    'class="card setting-toggle"'
)

$headPattern = '(?s)(<!DOCTYPE html>.*?</head>\s*<body>\s*<th:block\s+th:replace="~\{layout\s*::\s*body\}">\s*<div\s+th:fragment="page-content">)'

$fixed = 0
foreach ($dir in @("app", "pages", "modules")) {
    Get-ChildItem -Path (Join-Path $templates $dir) -Filter "*.html" -Recurse | ForEach-Object {
        $path = $_.FullName
        $content = [IO.File]::ReadAllText($path)
        $orig = $content

        $content = $content -replace '\.\./(?:\.\./)*assets/', '/assets/'
        $content = $content -replace '\.\./assets/', '/assets/'
        $content = $content -replace 'url\((\.\./)+assets/', 'url(/assets/'

        if ($content -notmatch 'layout :: body') { 
            if ($content -ne $orig) { [IO.File]::WriteAllText($path, $content); $fixed++ }
            return 
        }
        if ($content -notmatch '</main>' -and $content -notmatch 'settings-offcanvas') {
            if ($content -ne $orig) { [IO.File]::WriteAllText($path, $content); $fixed++ }
            return 
        }

        if ($content -notmatch $headPattern) {
            if ($content -ne $orig) { [IO.File]::WriteAllText($path, $content); $fixed++ }
            return 
        }

        $m = [regex]::Match($content, $headPattern)
        $head = $m.Groups[1].Value
        $start = $m.Index + $m.Length
        $sub = $content.Substring($start)
        $cut = $sub.Length
        foreach ($mark in $markers) {
            $idx = $sub.IndexOf($mark)
            if ($idx -ge 0 -and $idx -lt $cut) { $cut = $idx }
        }
        if ($cut -eq $sub.Length) {
            if ($content -ne $orig) { [IO.File]::WriteAllText($path, $content); $fixed++ }
            return 
        }

        $body = $sub.Substring(0, $cut).TrimEnd()
        $body = $body -replace '(?s)<footer\s+class="footer"[\s\S]*?</footer>\s*$', ''
        $body = $body.TrimEnd()

        $newContent = $head + "`n" + $body + "`n" + $suffix
        [IO.File]::WriteAllText($path, $newContent)
        $fixed++
        Write-Host $_.FullName.Replace($root + '\', '')
    }
}
Write-Host "Updated $fixed file(s)."
