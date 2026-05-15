$themeMin = Join-Path $PSScriptRoot '..\src\main\resources\static\assets\js\theme.min.js'
$content = [IO.File]::ReadAllText($themeMin)
$brokenPrefix = '.concat(document.location.href.split("/").slice(0,5).join("/"),'
$replacements = @(
    @{ From = $brokenPrefix + "'/app/events/create-an-event.html`""; To = "'/app/events/create-an-event'" },
    @{ From = $brokenPrefix + "'/app/events/event-detail.html'"; To = "'/app/events/event-detail'" },
    @{ From = $brokenPrefix + '"/app/events/create-an-event.html"'; To = '"/app/events/create-an-event"' },
    @{ From = $brokenPrefix + '"/app/events/event-detail.html"'; To = '"/app/events/event-detail"' }
)
foreach ($pair in $replacements) {
    $content = $content.Replace($pair.From, $pair.To)
}
[IO.File]::WriteAllText($themeMin, $content)
if ($content.Contains('slice(0,5)')) {
    Write-Host 'WARN: slice(0,5) still present'
} else {
    Write-Host 'theme.min.js calendar URLs fixed'
}
