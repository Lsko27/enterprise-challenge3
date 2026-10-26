param(
    [string]$BackupDirectory = (
Join-Path $PSScriptRoot "..\backups"
),

    [string]$RestoreDirectory = (
Join-Path $PSScriptRoot "..\restore-tests"
),

    [string]$ArchivePath
)

$ErrorActionPreference = "Stop"

function Get-NormalizedPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    return [System.IO.Path]::GetFullPath($Path)
}

$backupPath = Get-NormalizedPath `
    -Path $BackupDirectory

$restoreRoot = Get-NormalizedPath `
    -Path $RestoreDirectory

if (-not (Test-Path -LiteralPath $backupPath -PathType Container)) {
    throw "Diretório de backups não encontrado: $backupPath"
}

if ([string]::IsNullOrWhiteSpace($ArchivePath)) {
    $latestArchive =
    Get-ChildItem `
            -LiteralPath $backupPath `
            -File `
            -Filter "govatende-uploads-*.zip" |
            Sort-Object LastWriteTimeUtc -Descending |
            Select-Object -First 1

    if ($null -eq $latestArchive) {
        throw "Nenhum backup foi encontrado."
    }

    $selectedArchive =
    $latestArchive.FullName
}
else {
    $selectedArchive =
    Get-NormalizedPath `
            -Path $ArchivePath
}

if (-not (Test-Path -LiteralPath $selectedArchive -PathType Leaf)) {
    throw "Arquivo de backup não encontrado: $selectedArchive"
}

$manifestPath =
"$selectedArchive.sha256.json"

if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "Manifesto de integridade não encontrado: $manifestPath"
}

$manifest =
Get-Content `
        -LiteralPath $manifestPath `
        -Raw |
        ConvertFrom-Json

$calculatedHash =
Get-FileHash `
        -LiteralPath $selectedArchive `
        -Algorithm SHA256

if ($calculatedHash.Hash -ne $manifest.sha256) {
    throw (
    "Falha de integridade: o SHA-256 do backup " +
            "não corresponde ao manifesto."
    )
}

if (-not (Test-Path -LiteralPath $restoreRoot)) {
    New-Item `
        -ItemType Directory `
        -Path $restoreRoot `
        -Force |
            Out-Null
}

$timestamp =
Get-Date -Format "yyyyMMdd-HHmmss"

$testDirectory =
Join-Path `
        $restoreRoot `
        "govatende-restore-$timestamp"

if (Test-Path -LiteralPath $testDirectory) {
    throw "O diretório de teste já existe: $testDirectory"
}

New-Item `
    -ItemType Directory `
    -Path $testDirectory |
        Out-Null

Expand-Archive `
    -LiteralPath $selectedArchive `
    -DestinationPath $testDirectory

$restoredFileCount = @(
    Get-ChildItem `
        -LiteralPath $testDirectory `
        -File `
        -Recurse
).Count

if ($restoredFileCount -ne [int]$manifest.fileCount) {
    throw (
    "Falha na restauração: eram esperados " +
            "$($manifest.fileCount) arquivos, mas foram restaurados " +
            "$restoredFileCount."
    )
}

Write-Host ""
Write-Host "Teste de restauração concluído." `
    -ForegroundColor Green

Write-Host "Backup verificado: $selectedArchive"
Write-Host "SHA-256: $($calculatedHash.Hash)"
Write-Host "Arquivos restaurados: $restoredFileCount"
Write-Host "Diretório de teste: $testDirectory"
Write-Host ""
Write-Host (
"Os arquivos originais não foram sobrescritos."
) -ForegroundColor Yellow