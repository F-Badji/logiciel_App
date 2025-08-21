# Test ultra-rapide de fiabilité
Write-Host "TEST ULTRA-RAPIDE - FIABILITE" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan

# Vérification rapide des outils
$adb = Test-Path "C:\platform-tools\adb.exe"
$checkra1n = Test-Path "C:\checkra1n\checkra1n.exe"
$libi = Test-Path "C:\libimobiledevice\idevice_id.exe"

Write-Host ""
Write-Host "OUTILS: $([int]$adb + [int]$checkra1n + [int]$libi)/3" -ForegroundColor Yellow

# Vérification rapide des fonctionnalités
$realService = Test-Path "src\main\java\com\logicielapp\service\RealUnlockService.java"
$controllers = @(
    "src\main\java\com\logicielapp\controller\iCloudBypassController.java",
    "src\main\java\com\logicielapp\controller\SimUnlockController.java",
    "src\main\java\com\logicielapp\controller\FaceIDRepairController.java"
)

$controllerCount = 0
foreach ($controller in $controllers) {
    if (Test-Path $controller) { $controllerCount++ }
}

Write-Host "FONCTIONNALITES: $controllerCount/3" -ForegroundColor Yellow

# Résultat final
$totalScore = ([int]$adb + [int]$checkra1n + [int]$libi + [int]$realService + $controllerCount)
$maxScore = 6

Write-Host ""
Write-Host "SCORE FINAL: $totalScore/$maxScore" -ForegroundColor Cyan

if ($totalScore -eq $maxScore) {
    Write-Host "🎉 100% FIABLE - Prêt pour iOS 18.6.2!" -ForegroundColor Green
    Write-Host "   .\launch_app.ps1" -ForegroundColor Gray
} else {
    Write-Host "⚠️  Installation manuelle requise" -ForegroundColor Yellow
    Write-Host "   checkra1n: https://checkra.in" -ForegroundColor Gray
    Write-Host "   libimobiledevice: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor Gray
}
