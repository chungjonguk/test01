# 로그인 후 주요 화면 렌더 스모크 (Thymeleaf 500 탐지)
param(
  [string]$Base = "http://localhost:8081",
  [string]$UserId = "admin",
  [string]$Password = "admin"
)
$paths = @(
  "/dashboard",
  "/app/e-commerce/product/product-register",
  "/app/e-commerce/product/product-manage",
  "/admin/menus",
  "/admin/codes",
  "/admin/companies",
  "/app/events/create-an-event",
  "/modules/forms/wizard"
)
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
  Invoke-WebRequest -Uri "$Base/auth/login" -Method POST -Body @{ id = $UserId; pw = $Password } -WebSession $session -MaximumRedirection 5 | Out-Null
} catch {
  Write-Host "Login failed (try other id/pw): $($_.Exception.Message)"
}
$fail = @()
foreach ($p in $paths) {
  try {
    $r = Invoke-WebRequest -Uri "$Base$p" -WebSession $session -MaximumRedirection 5 -UseBasicParsing
    $body = $r.Content
    $code = [int]$r.StatusCode
    if ($body -match "Whitelabel Error|TemplateInputException|TemplateProcessingException") {
      $fail += [PSCustomObject]@{ Path = $p; Status = $code; Reason = "Template error" }
      Write-Host "FAIL $code $p - Template error"
    } else {
      Write-Host "OK   $code $p"
    }
  } catch {
    $code = 0
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
    $fail += [PSCustomObject]@{ Path = $p; Status = $code; Reason = $_.Exception.Message }
    Write-Host "FAIL $code $p - $($_.Exception.Message)"
  }
}
if ($fail.Count -gt 0) {
  $fail | Format-Table -AutoSize
  exit 1
}
Write-Host "Authenticated smoke OK."
exit 0
