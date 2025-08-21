# Installation automatique des outils pour 100% fiabilité
Write-Host "INSTALLATION AUTOMATIQUE FINALE" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. TELECHARGEMENT DE CHECKRA1N (iOS 18.6.2)" -ForegroundColor Yellow

# Essayer plusieurs URLs pour checkra1n
$checkra1nUrls = @(
    "https://assets.checkra.in/downloads/windows/checkra1n-win64.exe",
    "https://checkra.in/assets/checkra1n-win64.exe",
    "https://github.com/checkra1n/checkra1n/releases/latest/download/checkra1n-win64.exe"
)

$checkra1nDownloaded = $false
foreach ($url in $checkra1nUrls) {
    try {
        Write-Host "   Tentative: $url" -ForegroundColor Gray
        Invoke-WebRequest -Uri $url -OutFile "C:\checkra1n\checkra1n.exe" -TimeoutSec 30
        Write-Host "   ✅ checkra1n téléchargé avec succès" -ForegroundColor Green
        $checkra1nDownloaded = $true
        break
    } catch {
        Write-Host "   ❌ Échec: $url" -ForegroundColor Red
    }
}

if (-not $checkra1nDownloaded) {
    Write-Host ""
    Write-Host "   ⚠️  Téléchargement automatique échoué" -ForegroundColor Yellow
    Write-Host "   📥 Téléchargez manuellement depuis: https://checkra.in" -ForegroundColor White
    Write-Host "   📁 Placez dans: C:\checkra1n\checkra1n.exe" -ForegroundColor White
}

Write-Host ""
Write-Host "2. TELECHARGEMENT DE LIBIMOBILEDEVICE" -ForegroundColor Yellow

# Essayer plusieurs URLs pour libimobiledevice
$libiUrls = @(
    "https://github.com/libimobiledevice/libimobiledevice/releases/download/1.3.0/libimobiledevice-1.3.0-win64.zip",
    "https://github.com/libimobiledevice/libimobiledevice/releases/latest/download/libimobiledevice-win64.zip"
)

$libiDownloaded = $false
foreach ($url in $libiUrls) {
    try {
        Write-Host "   Tentative: $url" -ForegroundColor Gray
        $tempZip = "$env:TEMP\libimobiledevice.zip"
        Invoke-WebRequest -Uri $url -OutFile $tempZip -TimeoutSec 30
        Expand-Archive -Path $tempZip -DestinationPath "C:\libimobiledevice" -Force
        Remove-Item $tempZip
        Write-Host "   ✅ libimobiledevice téléchargé avec succès" -ForegroundColor Green
        $libiDownloaded = $true
        break
    } catch {
        Write-Host "   ❌ Échec: $url" -ForegroundColor Red
    }
}

if (-not $libiDownloaded) {
    Write-Host ""
    Write-Host "   ⚠️  Téléchargement automatique échoué" -ForegroundColor Yellow
    Write-Host "   📥 Téléchargez manuellement depuis: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor White
    Write-Host "   📁 Extrayez dans: C:\libimobiledevice\" -ForegroundColor White
}

Write-Host ""
Write-Host "3. CONFIGURATION DU PATH" -ForegroundColor Yellow
$env:PATH += ";C:\platform-tools;C:\libimobiledevice;C:\checkra1n"
Write-Host "   ✅ PATH mis à jour" -ForegroundColor Green

Write-Host ""
Write-Host "4. VERIFICATION FINALE" -ForegroundColor Yellow

$tools = @{
    "ADB" = "C:\platform-tools\adb.exe"
    "checkra1n" = "C:\checkra1n\checkra1n.exe"
    "libimobiledevice" = "C:\libimobiledevice\idevice_id.exe"
}

$successCount = 0
foreach ($tool in $tools.GetEnumerator()) {
    if (Test-Path $tool.Value) {
        Write-Host "   ✅ $($tool.Key) installé" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "   ❌ $($tool.Key) manquant" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "5. RESULTAT: $successCount/3 outils installés" -ForegroundColor Cyan

if ($successCount -eq 3) {
    Write-Host "   🎉 100% FIABLE - Tous les outils installés!" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Installation manuelle requise pour les outils manquants" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "6. TEST DE FIABILITE" -ForegroundColor Yellow
Write-Host "   .\test_simple.ps1" -ForegroundColor Gray
