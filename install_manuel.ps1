# Installation manuelle des outils manquants
Write-Host "INSTALLATION MANUELLE DES OUTILS" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. TÉLÉCHARGEZ MANUELLEMENT:" -ForegroundColor Yellow
Write-Host "   - checkra1n: https://checkra.in" -ForegroundColor White
Write-Host "   - libimobiledevice: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor White
Write-Host ""

Write-Host "2. PLACEZ LES FICHIERS:" -ForegroundColor Yellow
Write-Host "   - checkra1n.exe dans C:\checkra1n\" -ForegroundColor White
Write-Host "   - libimobiledevice dans C:\libimobiledevice\" -ForegroundColor White
Write-Host ""

Write-Host "3. AJOUTEZ AU PATH:" -ForegroundColor Yellow
$env:PATH += ";C:\platform-tools;C:\libimobiledevice;C:\checkra1n"
Write-Host "   PATH mis à jour pour cette session" -ForegroundColor Green
Write-Host ""

Write-Host "4. VÉRIFICATION:" -ForegroundColor Yellow
if (Test-Path "C:\platform-tools\adb.exe") {
    Write-Host "   ✅ ADB installé" -ForegroundColor Green
} else {
    Write-Host "   ❌ ADB manquant" -ForegroundColor Red
}

if (Test-Path "C:\checkra1n\checkra1n.exe") {
    Write-Host "   ✅ checkra1n installé" -ForegroundColor Green
} else {
    Write-Host "   ❌ checkra1n manquant" -ForegroundColor Red
}

if (Test-Path "C:\libimobiledevice\idevice_id.exe") {
    Write-Host "   ✅ libimobiledevice installé" -ForegroundColor Green
} else {
    Write-Host "   ❌ libimobiledevice manquant" -ForegroundColor Red
}

Write-Host ""
Write-Host "5. TEST FINAL:" -ForegroundColor Yellow
Write-Host "   .\test_simple.ps1" -ForegroundColor Gray
