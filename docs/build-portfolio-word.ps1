# portfolio Word (.docx) - requires Microsoft Word
$ErrorActionPreference = "Stop"
$docsDir = $PSScriptRoot
$savePath = [System.IO.Path]::GetFullPath((Join-Path $docsDir "portfolio-spring-boot-app.docx"))
$jsonPath = Join-Path $docsDir "portfolio-sections.json"
$sections = Get-Content -LiteralPath $jsonPath -Encoding UTF8 -Raw | ConvertFrom-Json

$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Add()
$sel = $word.Selection

$sel.ParagraphFormat.Alignment = 1
$sel.Font.Size = 22
$sel.Font.Bold = $true
$sel.TypeText("Spring Boot App (Falcon UI)")
$sel.TypeParagraph()
$sel.Font.Size = 14
$sel.Font.Bold = $false
$sel.TypeText("Responsive Web App Portfolio")
$sel.TypeParagraph()
$sel.Font.Size = 12
$sel.TypeText([string]$sections[0].body[1])
$sel.TypeParagraph()
$sel.Font.Size = 11
$sel.TypeText((Get-Date -Format "yyyy-MM-dd"))
$sel.TypeParagraph()
$sel.TypeParagraph()
$sel.InsertBreak(7)

foreach ($sec in $sections) {
  $sel.ParagraphFormat.Alignment = 0
  $sel.Font.Size = 14
  $sel.Font.Bold = $true
  $sel.TypeText([string]$sec.title)
  $sel.TypeParagraph()
  $sel.Font.Size = 11
  $sel.Font.Bold = $false
  foreach ($line in $sec.body) {
    $sel.TypeText([string]$line)
    $sel.TypeParagraph()
  }
  if ($sec.image) {
    $imgPath = [System.IO.Path]::GetFullPath((Join-Path $docsDir ($sec.image -replace "/", "\")))
    if (Test-Path -LiteralPath $imgPath) {
      $sel.TypeParagraph()
      try {
        $pic = $sel.InlineShapes.AddPicture($imgPath)
        if ($pic.Width -gt 480) {
          $r = 480 / $pic.Width
          $pic.Width = 480
          $pic.Height = $pic.Height * $r
        }
      } catch {
        $sel.TypeText("[Image: $($sec.image)]")
        $sel.TypeParagraph()
      }
      $sel.TypeParagraph()
    }
  }
  $sel.TypeParagraph()
}

$tmpPath = $savePath + ".tmp.docx"
if (Test-Path -LiteralPath $tmpPath) { Remove-Item -LiteralPath $tmpPath -Force -ErrorAction SilentlyContinue }
# wdFormatXMLDocument = 12
$doc.SaveAs($tmpPath, 12)
$doc.Close($false)
$word.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($doc) | Out-Null
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
[System.GC]::Collect()
Start-Sleep -Milliseconds 500
if (Test-Path -LiteralPath $savePath) { Remove-Item -LiteralPath $savePath -Force -ErrorAction SilentlyContinue }
Move-Item -LiteralPath $tmpPath -Destination $savePath -Force
Write-Output "Created: $savePath"
