# 주요 화면 HTTP 스모크 (로컬 8081, Thymeleaf 500·Whitelabel 탐지)
$base = "http://localhost:8081"
$paths = @(
  "/dashboard",
  "/app/e-commerce/product/product-register",
  "/app/e-commerce/product/product-manage",
  "/app/e-commerce/product/product-images",
  "/app/e-commerce/orders/order-list",
  "/app/e-commerce/billing",
  "/app/e-commerce/checkout",
  "/admin/companies",
  "/admin/inventory",
  "/admin/menus",
  "/admin/codes",
  "/admin/user-access-logs",
  "/admin/table-sequences",
  "/admin/dashboard-config",
  "/admin/company-page-images",
  "/admin/company-domains",
  "/users",
  "/pages/user/settings",
  "/app/events/create-an-event",
  "/app/e-learning/course/create-a-course",
  "/modules/forms/wizard",
  "/pages/authentication/wizard"
)
$fail = @()
foreach ($p in $paths) {
  $url = "$base$p"
  $bodyFile = [System.IO.Path]::GetTempFileName()
  $code = curl.exe -s -o $bodyFile -w "%{http_code}" $url
  $body = Get-Content $bodyFile -Raw -ErrorAction SilentlyContinue
  Remove-Item $bodyFile -Force -ErrorAction SilentlyContinue
  $bad = $false
  $reason = ""
  if ($code -eq "500") { $bad = $true; $reason = "HTTP 500" }
  elseif ($body -match "Whitelabel Error|TemplateInputException|TemplateProcessingException|ParseException") {
    $bad = $true; $reason = "Template error in body"
  }
  if ($bad) {
    $fail += [PSCustomObject]@{ Path = $p; Status = $code; Reason = $reason }
    Write-Host "FAIL $code $p - $reason"
  } else {
    Write-Host "OK   $code $p"
  }
}
if ($fail.Count -gt 0) {
  Write-Host "`n=== $($fail.Count) failure(s) ==="
  $fail | Format-Table -AutoSize
  exit 1
}
Write-Host "`nAll $($paths.Count) paths OK (no 500/template error in body)."
exit 0
