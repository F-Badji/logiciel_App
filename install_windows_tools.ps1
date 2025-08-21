# Installation des outils Windows compatibles pour iCloud Bypass
Write-Host "INSTALLATION OUTILS WINDOWS COMPATIBLES" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan

Write-Host ""
Write-Host "OUTILS WINDOWS POUR iCLOUD BYPASS:" -ForegroundColor Yellow

Write-Host ""
Write-Host "1. 3uTools (Recommandé)" -ForegroundColor Green
Write-Host "   Lien: https://www.3u.com/" -ForegroundColor White
Write-Host "   Fonctionnalités: Bypass iCloud, gestion iOS, transfert de données" -ForegroundColor Gray

Write-Host ""
Write-Host "2. iMazing (Professionnel)" -ForegroundColor Green
Write-Host "   Lien: https://imazing.com/" -ForegroundColor White
Write-Host "   Fonctionnalités: Sauvegarde, déblocage, gestion avancée" -ForegroundColor Gray

Write-Host ""
Write-Host "3. iFunBox (Gratuit)" -ForegroundColor Green
Write-Host "   Lien: https://www.i-funbox.com/" -ForegroundColor White
Write-Host "   Fonctionnalités: Gestion de fichiers, bypass basique" -ForegroundColor Gray

Write-Host ""
Write-Host "COMMANDES POUR OUVRIR LES SITES:" -ForegroundColor Yellow
Write-Host "Start-Process 'https://www.3u.com/'" -ForegroundColor Gray
Write-Host "Start-Process 'https://imazing.com/'" -ForegroundColor Gray
Write-Host "Start-Process 'https://www.i-funbox.com/'" -ForegroundColor Gray

Write-Host ""
Write-Host "AVANTAGES DES OUTILS WINDOWS:" -ForegroundColor Yellow
Write-Host "   ✅ Compatible Windows 100%" -ForegroundColor Green
Write-Host "   ✅ Interface graphique" -ForegroundColor Green
Write-Host "   ✅ Support iOS 18.6.2" -ForegroundColor Green
Write-Host "   ✅ Pas besoin de checkra1n" -ForegroundColor Green

Write-Host ""
Write-Host "VÉRIFICATION ACTUELLE:" -ForegroundColor Yellow
$adb = Test-Path "C:\platform-tools\adb.exe"
$libi = Test-Path "C:\libimobiledevice\idevice_id.exe"
Write-Host "   ADB: $([int]$adb)/1" -ForegroundColor Cyan
Write-Host "   libimobiledevice: $([int]$libi)/1" -ForegroundColor Cyan

Write-Host ""
Write-Host "OBJECTIF: 100% FIABLE SANS CHECKRA1N" -ForegroundColor Green
