# Portfolio: screenshots + Word (+ PDF)
$ErrorActionPreference = "Stop"
$docs = $PSScriptRoot
& (Join-Path $docs "capture-all-screenshots.ps1")
& (Join-Path $docs "build-portfolio-word.ps1")
$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$html = Join-Path $docs "portfolio-spring-boot-app.html"
$pdf = Join-Path $docs "portfolio-spring-boot-app.pdf"
$uri = "file:///" + ($html -replace '\\', '/')
Start-Process -FilePath $edge -Wait -NoNewWindow -ArgumentList @(
  "--headless", "--disable-gpu", "--no-pdf-header-footer",
  "--print-to-pdf=$pdf", $uri
)
Write-Host "Done:"
Write-Host "  Word: $(Join-Path $docs 'portfolio-spring-boot-app.docx')"
Write-Host "  PDF:  $pdf"
