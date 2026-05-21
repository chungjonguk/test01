$edge = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$docs = $PSScriptRoot
$shot = Join-Path $docs "screenshots"
New-Item -ItemType Directory -Force -Path $shot | Out-Null

function Capture($name, $url, $w, $h, $budget) {
  $out = Join-Path $shot $name
  Start-Process -FilePath $edge -Wait -NoNewWindow -ArgumentList @(
    "--headless", "--disable-gpu", "--window-size=$w,$h",
    "--virtual-time-budget=$budget", "--screenshot=$out", $url
  )
  Start-Sleep -Milliseconds 400
}

$htmlUri = "file:///" + ((Join-Path $docs "responsive-overview.html") -replace '\\', '/')
$thUri = "file:///" + ((Join-Path $docs "thymeleaf-architecture.html") -replace '\\', '/')

# Desktop
Capture "01-home.png" "http://localhost:8081/" 1400 900 10000
Capture "02-dashboard-analytics.png" "http://localhost:8081/dashboard/analytics" 1400 900 10000
Capture "03-wizard.png" "http://localhost:8081/pages/authentication/wizard" 1400 1000 20000
Capture "04-admin-menus.png" "http://localhost:8081/admin/menus" 1400 900 10000
Capture "05-admin-codes.png" "http://localhost:8081/admin/codes" 1400 900 10000
Capture "06-order-list.png" "http://localhost:8081/app/e-commerce/orders/order-list" 1400 900 10000
Capture "07-thymeleaf-architecture.png" $thUri 1200 800 5000
Capture "08-pages-starter.png" "http://localhost:8081/pages/starter" 1400 900 10000
Capture "09-email-compose.png" "http://localhost:8081/app/email/compose" 1400 900 10000

# Responsive docs + mobile
Capture "10-responsive-overview.png" $htmlUri 1100 750 5000
Capture "11-responsive-mobile-home.png" "http://localhost:8081/" 390 844 12000
Capture "12-responsive-mobile-analytics.png" "http://localhost:8081/dashboard/analytics" 390 844 12000
Capture "13-responsive-mobile-wizard.png" "http://localhost:8081/pages/authentication/wizard" 390 900 20000

Write-Host "Screenshots: $shot"
