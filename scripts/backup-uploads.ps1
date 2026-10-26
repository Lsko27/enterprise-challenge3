param(
    [string]$SourceDirectory = (
        Join-Path $PSScriptRoot "..\uploads"
    ),

    [string]$BackupDirectory = (
        Join-Path $PSScriptRoot "..\backups"
    ),

    [ValidateRange(1, 3650)]
    [int]$RetentionDays = 30
)

$ErrorActionPreference = "Stop"

function Get-NormalizedPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    return [System.IO.Path]::GetFullPath($Path)
}

$sourcePath = Get-NormalizedPath `
    -Path $SourceDirectory

$backupPath = Get-NormalizedPath `
    -Path $BackupDirectory

if (-not (Test-Path -LiteralPath $sourcePath -PathType Container)) {
    throw "Diretório de origem não encontrado: $sourcePath"
}

if ($sourcePath -eq $backupPath) {
    throw "O diretório de backup não pode ser igual ao diretório de origem."
}

$backupRoot = [System.IO.Path]::GetPathRoot($backupPath)

if ($backupPath -eq $backupRoot) {
    throw "O diretório raiz do sistema não pode ser utilizado para backups."
}

if (-not (Test-Path -LiteralPath $backupPath)) {
    New-Item `
        -ItemType Directory `
        -Path $backupPath `
        -Force |
        Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

$archiveName =
    "govatende-uploads-$timestamp.zip"

$archivePath =
    Join-Path $backupPath $archiveName

$manifestPath =
    "$archivePath.sha256.json"

if (Test-Path -LiteralPath $archivePath) {
    throw "Já existe um backup com o mesmo nome: $archivePath"
}

Add-Type `
    -AssemblyName System.IO.Compression.FileSystem

[System.IO.Compression.ZipFile]::CreateFromDirectory(
    $sourcePath,
    $archivePath,
    [System.IO.Compression.CompressionLevel]::Optimal,
    $false
)

$archiveFile =
    Get-Item -LiteralPath $archivePath

$hash =
    Get-FileHash `
        -LiteralPath $archivePath `
        -Algorithm SHA256

$fileCount = @(
    Get-ChildItem `
        -LiteralPath $sourcePath `
        -File `
        -Recurse
).Count

$manifest = [ordered]@{
    application = "GovAtende"
    createdAtUtc = (
        Get-Date
    ).ToUniversalTime().ToString("o")
    sourceDirectory = "uploads"
    archive = $archiveFile.Name
    sizeBytes = $archiveFile.Length
    fileCount = $fileCount
    algorithm = "SHA256"
    sha256 = $hash.Hash
    retentionDays = $RetentionDays
}

$manifest |
    ConvertTo-Json |
    Set-Content `
        -LiteralPath $manifestPath `
        -Encoding UTF8

$cutoffDate =
    (Get-Date).ToUniversalTime().AddDays(
        -$RetentionDays
    )

$expiredArchives =
    Get-ChildItem `
        -LiteralPath $backupPath `
        -File `
        -Filter "govatende-uploads-*.zip" |
    Where-Object {
        $_.LastWriteTimeUtc -lt $cutoffDate
    }

foreach ($expiredArchive in $expiredArchives) {
    $expiredManifest =
        "$($expiredArchive.FullName).sha256.json"

    Remove-Item `
        -LiteralPath $expiredArchive.FullName `
        -Force

    if (Test-Path -LiteralPath $expiredManifest) {
        Remove-Item `
            -LiteralPath $expiredManifest `
            -Force
    }
}

Write-Host ""
Write-Host "Backup concluído com sucesso." `
    -ForegroundColor Green

Write-Host "Arquivo: $archivePath"
Write-Host "Manifesto: $manifestPath"
Write-Host "Arquivos incluídos: $fileCount"
Write-Host "SHA-256: $($hash.Hash)"
Write-Host "Retenção: $RetentionDays dias"