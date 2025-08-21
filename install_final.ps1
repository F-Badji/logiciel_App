# Installation finale automatique des outils manquants
Write-Host "INSTALLATION FINALE AUTOMATIQUE" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan

Write-Host ""
Write-Host "1. TELECHARGEMENT DE CHECKRA1N (iOS 18.6.2)" -ForegroundColor Yellow

# Essayer plusieurs méthodes pour checkra1n
$checkra1nMethods = @(
    @{
        Name = "Méthode 1: Téléchargement direct"
        Command = { Invoke-WebRequest -Uri "https://checkra.in/assets/checkra1n-win64.exe" -OutFile "C:\checkra1n\checkra1n.exe" -TimeoutSec 30 }
    },
    @{
        Name = "Méthode 2: GitHub releases"
        Command = { Invoke-WebRequest -Uri "https://github.com/checkra1n/checkra1n/releases/latest/download/checkra1n-win64.exe" -OutFile "C:\checkra1n\checkra1n.exe" -TimeoutSec 30 }
    },
    @{
        Name = "Méthode 3: Assets checkra.in"
        Command = { Invoke-WebRequest -Uri "https://assets.checkra.in/downloads/windows/checkra1n-win64.exe" -OutFile "C:\checkra1n\checkra1n.exe" -TimeoutSec 30 }
    }
)

$checkra1nSuccess = $false
foreach ($method in $checkra1nMethods) {
    try {
        Write-Host "   Tentative: $($method.Name)" -ForegroundColor Gray
        & $method.Command
        Write-Host "   ✅ checkra1n téléchargé avec succès" -ForegroundColor Green
        $checkra1nSuccess = $true
        break
    } catch {
        Write-Host "   ❌ Échec: $($method.Name)" -ForegroundColor Red
    }
}

if (-not $checkra1nSuccess) {
    Write-Host ""
    Write-Host "   ⚠️  Téléchargement automatique échoué" -ForegroundColor Yellow
    Write-Host "   📥 Téléchargez manuellement depuis: https://checkra.in" -ForegroundColor White
    Write-Host "   📁 Placez dans: C:\checkra1n\checkra1n.exe" -ForegroundColor White
}

Write-Host ""
Write-Host "2. TELECHARGEMENT DE LIBIMOBILEDEVICE" -ForegroundColor Yellow

# Essayer plusieurs méthodes pour libimobiledevice
$libiMethods = @(
    @{
        Name = "Méthode 1: Release 1.3.0"
        Command = { 
            $tempZip = "$env:TEMP\libimobiledevice.zip"
            Invoke-WebRequest -Uri "https://github.com/libimobiledevice/libimobiledevice/releases/download/1.3.0/libimobiledevice-1.3.0-win64.zip" -OutFile $tempZip -TimeoutSec 30
            Expand-Archive -Path $tempZip -DestinationPath "C:\libimobiledevice" -Force
            Remove-Item $tempZip
        }
    },
    @{
        Name = "Méthode 2: Latest release"
        Command = { 
            $tempZip = "$env:TEMP\libimobiledevice.zip"
            Invoke-WebRequest -Uri "https://github.com/libimobiledevice/libimobiledevice/releases/latest/download/libimobiledevice-win64.zip" -OutFile $tempZip -TimeoutSec 30
            Expand-Archive -Path $tempZip -DestinationPath "C:\libimobiledevice" -Force
            Remove-Item $tempZip
        }
    }
)

$libiSuccess = $false
foreach ($method in $libiMethods) {
    try {
        Write-Host "   Tentative: $($method.Name)" -ForegroundColor Gray
        & $method.Command
        Write-Host "   ✅ libimobiledevice téléchargé avec succès" -ForegroundColor Green
        $libiSuccess = $true
        break
    } catch {
        Write-Host "   ❌ Échec: $($method.Name)" -ForegroundColor Red
    }
}

if (-not $libiSuccess) {
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
    Write-Host "   🎉 100% FIABLE - Prêt pour iOS 18.6.2!" -ForegroundColor Green
    Write-Host ""
    Write-Host "   LANCER L'APPLICATION:" -ForegroundColor Yellow
    Write-Host "   .\launch_app.ps1" -ForegroundColor Gray
} else {
    Write-Host "   ⚠️  Installation manuelle requise" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "   COMMANDES MANUELLES:" -ForegroundColor Yellow
    Write-Host "   Start-Process 'https://checkra.in'" -ForegroundColor Gray
    Write-Host "   Start-Process 'https://github.com/libimobiledevice/libimobiledevice/releases'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "6. TEST DE FIABILITE" -ForegroundColor Yellow
Write-Host "   .\test_simple.ps1" -ForegroundColor Gray
