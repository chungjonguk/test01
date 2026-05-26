# 백업용 텍스트 파일 — Windows 메모장·기본 편집기에서 한글이 깨지지 않도록 UTF-8 BOM 저장

function Write-BackupUtf8Text {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )
    $dir = [System.IO.Path]::GetDirectoryName($Path)
    if ($dir -and -not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    $enc = New-Object System.Text.UTF8Encoding $true
    [System.IO.File]::WriteAllText($Path, $Content, $enc)
}

function Read-BackupUtf8Template {
    param(
        [Parameter(Mandatory = $true)][string]$TemplatePath,
        [hashtable]$Replacements = @{}
    )
    if (-not (Test-Path $TemplatePath)) {
        throw "템플릿 없음: $TemplatePath"
    }
    $text = Get-Content -LiteralPath $TemplatePath -Raw -Encoding UTF8
    foreach ($key in $Replacements.Keys) {
        $text = $text.Replace("{{$key}}", [string]$Replacements[$key])
    }
    return $text
}

function Copy-BackupUtf8Text {
    param(
        [Parameter(Mandatory = $true)][string]$SourcePath,
        [Parameter(Mandatory = $true)][string]$DestPath
    )
    if (-not (Test-Path $SourcePath)) {
        return $false
    }
    $content = Get-Content -LiteralPath $SourcePath -Raw -Encoding UTF8
    Write-BackupUtf8Text -Path $DestPath -Content $content
    return $true
}
