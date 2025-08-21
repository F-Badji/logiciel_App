# Installation des outils manquants pour 100% de fiabilité

Write-Host "INSTALLATION DES OUTILS MANQUANTS" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "OUTILS MANQUANTS POUR 100% DE FIABILITE:" -ForegroundColor Blue
Write-Host "----------------------------------------" -ForegroundColor Blue

Write-Host "1. CHECKRA1N (pour iOS)" -ForegroundColor Yellow
Write-Host "   Lien: https://checkra.in" -ForegroundColor White
Write-Host "   Placez dans: C:\checkra1n\checkra1n.exe" -ForegroundColor White
Write-Host ""

Write-Host "2. LIBIMOBILEDEVICE (pour iOS)" -ForegroundColor Yellow
Write-Host "   Lien: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor White
Write-Host "   Placez dans: C:\libimobiledevice\" -ForegroundColor White
Write-Host ""

Write-Host "COMMANDES AUTOMATIQUES (si les liens fonctionnent):" -ForegroundColor Blue
Write-Host "--------------------------------------------------" -ForegroundColor Blue

Write-Host "Pour checkra1n:" -ForegroundColor Yellow
Write-Host '$checkra1nUrl = "https://checkra.in/assets/checkra1n-win64.exe"' -ForegroundColor Gray
Write-Host 'Invoke-WebRequest -Uri $checkra1nUrl -OutFile "C:\checkra1n\checkra1n.exe"' -ForegroundColor Gray
Write-Host ""

Write-Host "Pour libimobiledevice:" -ForegroundColor Yellow
Write-Host '$libiUrl = "https://github.com/libimobiledevice/libimobiledevice/releases/download/1.3.0/libimobiledevice-1.3.0-win64.zip"' -ForegroundColor Gray
Write-Host 'Invoke-WebRequest -Uri $libiUrl -OutFile "$env:TEMP\libimobiledevice.zip"' -ForegroundColor Gray
Write-Host 'Expand-Archive -Path "$env:TEMP\libimobiledevice.zip" -DestinationPath "C:\libimobiledevice" -Force' -ForegroundColor Gray
Write-Host ""

Write-Host "VERIFICATION APRES INSTALLATION:" -ForegroundColor Blue
Write-Host "--------------------------------" -ForegroundColor Blue

if (Test-Path "C:\checkra1n\checkra1n.exe") {
    Write-Host "✅ checkra1n installé" -ForegroundColor Green
} else {
    Write-Host "❌ checkra1n manquant" -ForegroundColor Red
}

if (Test-Path "C:\libimobiledevice\idevice_id.exe") {
    Write-Host "✅ libimobiledevice installé" -ForegroundColor Green
} else {
    Write-Host "❌ libimobiledevice manquant" -ForegroundColor Red
}

Write-Host ""
Write-Host "TEST FINAL DE FIABILITE:" -ForegroundColor Blue
Write-Host "------------------------" -ForegroundColor Blue
Write-Host "powershell -ExecutionPolicy Bypass -File test_simple.ps1" -ForegroundColor Gray

Write-Host ""
Write-Host "OBJECTIF: 100% DE FIABILITE POUR DÉBLOCAGE iCLOUD !" -ForegroundColor Green
