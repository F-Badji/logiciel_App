# Script d'installation complète des outils système pour le Logiciel de Déblocage Mobile
# Compatible Windows

Write-Host "🚀 INSTALLATION COMPLÈTE DES OUTILS SYSTÈME" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si on est administrateur
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")

if (-not $isAdmin) {
    Write-Host "⚠️  ATTENTION: Ce script nécessite des privilèges administrateur" -ForegroundColor Yellow
    Write-Host "   Relancez PowerShell en tant qu'administrateur" -ForegroundColor Yellow
    Write-Host "   Ou exécutez: Start-Process PowerShell -Verb RunAs" -ForegroundColor Yellow
    Read-Host "Appuyez sur Entrée pour continuer quand même..."
}

Write-Host "📦 ÉTAPE 1: INSTALLATION DE CHOCOLATEY" -ForegroundColor Blue
Write-Host "----------------------------------------" -ForegroundColor Blue

# Vérifier si Chocolatey est installé
if (Get-Command choco -ErrorAction SilentlyContinue) {
    Write-Host "✅ Chocolatey déjà installé" -ForegroundColor Green
} else {
    Write-Host "📥 Installation de Chocolatey..." -ForegroundColor Yellow
    try {
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        Write-Host "✅ Chocolatey installé avec succès" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors de l'installation de Chocolatey: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "   Installation manuelle requise: https://chocolatey.org/install" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "📱 ÉTAPE 2: INSTALLATION DES OUTILS ANDROID" -ForegroundColor Blue
Write-Host "---------------------------------------------" -ForegroundColor Blue

# Installer Android Platform Tools
Write-Host "📥 Installation d'Android Platform Tools..." -ForegroundColor Yellow
try {
    choco install android-sdk-platform-tools -y
    Write-Host "✅ Android Platform Tools installé" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur avec Chocolatey, tentative d'installation manuelle..." -ForegroundColor Red
    
    # Installation manuelle d'ADB
    Write-Host "📥 Téléchargement manuel d'ADB..." -ForegroundColor Yellow
    $adbUrl = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"
    $adbZip = "$env:TEMP\platform-tools.zip"
    $adbDir = "C:\platform-tools"
    
    try {
        Invoke-WebRequest -Uri $adbUrl -OutFile $adbZip
        Expand-Archive -Path $adbZip -DestinationPath "C:\" -Force
        Remove-Item $adbZip
        
        # Ajouter au PATH
        $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
        if ($currentPath -notlike "*$adbDir*") {
            [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$adbDir", "Machine")
        }
        
        Write-Host "✅ ADB installé manuellement dans C:\platform-tools" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors du téléchargement d'ADB" -ForegroundColor Red
        Write-Host "   Téléchargez manuellement: https://developer.android.com/studio/releases/platform-tools" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🍎 ÉTAPE 3: INSTALLATION DES OUTILS iOS" -ForegroundColor Blue
Write-Host "----------------------------------------" -ForegroundColor Blue

# Installer libimobiledevice
Write-Host "📥 Installation de libimobiledevice..." -ForegroundColor Yellow
try {
    choco install libimobiledevice -y
    Write-Host "✅ libimobiledevice installé" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur avec Chocolatey, tentative d'installation manuelle..." -ForegroundColor Red
    
    # Installation manuelle de libimobiledevice
    Write-Host "📥 Téléchargement manuel de libimobiledevice..." -ForegroundColor Yellow
    $libiUrl = "https://github.com/libimobiledevice/libimobiledevice/releases/download/1.3.0/libimobiledevice-1.3.0-win64.zip"
    $libiZip = "$env:TEMP\libimobiledevice.zip"
    $libiDir = "C:\libimobiledevice"
    
    try {
        Invoke-WebRequest -Uri $libiUrl -OutFile $libiZip
        New-Item -ItemType Directory -Path $libiDir -Force
        Expand-Archive -Path $libiZip -DestinationPath $libiDir -Force
        Remove-Item $libiZip
        
        # Ajouter au PATH
        $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
        if ($currentPath -notlike "*$libiDir*") {
            [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$libiDir", "Machine")
        }
        
        Write-Host "✅ libimobiledevice installé manuellement dans C:\libimobiledevice" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors du téléchargement de libimobiledevice" -ForegroundColor Red
        Write-Host "   Téléchargez manuellement: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🔓 ÉTAPE 4: INSTALLATION DE CHECKRA1N" -ForegroundColor Blue
Write-Host "--------------------------------------" -ForegroundColor Blue

# Télécharger checkra1n
Write-Host "📥 Téléchargement de checkra1n..." -ForegroundColor Yellow
$checkra1nUrl = "https://assets.checkra.in/downloads/windows/checkra1n-win64.exe"
$checkra1nPath = "C:\checkra1n\checkra1n.exe"

try {
    New-Item -ItemType Directory -Path "C:\checkra1n" -Force
    Invoke-WebRequest -Uri $checkra1nUrl -OutFile $checkra1nPath
    
    # Ajouter au PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
    if ($currentPath -notlike "*C:\checkra1n*") {
        [Environment]::SetEnvironmentVariable("PATH", "$currentPath;C:\checkra1n", "Machine")
    }
    
    Write-Host "✅ checkra1n téléchargé dans C:\checkra1n" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors du téléchargement de checkra1n" -ForegroundColor Red
    Write-Host "   Téléchargez manuellement: https://checkra.in" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🌐 ÉTAPE 5: VÉRIFICATION DE CURL" -ForegroundColor Blue
Write-Host "---------------------------------" -ForegroundColor Blue

# Vérifier curl
if (Get-Command curl -ErrorAction SilentlyContinue) {
    Write-Host "✅ curl déjà installé" -ForegroundColor Green
} else {
    Write-Host "📥 Installation de curl..." -ForegroundColor Yellow
    try {
        choco install curl -y
        Write-Host "✅ curl installé" -ForegroundColor Green
    } catch {
        Write-Host "❌ Erreur lors de l'installation de curl" -ForegroundColor Red
        Write-Host "   curl est généralement inclus dans Windows 10/11" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🔄 ÉTAPE 6: RÉFRESH DU PATH" -ForegroundColor Blue
Write-Host "----------------------------" -ForegroundColor Blue

# Rafraîchir les variables d'environnement
Write-Host "🔄 Rafraîchissement des variables d'environnement..." -ForegroundColor Yellow
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","User")

Write-Host ""
Write-Host "✅ INSTALLATION TERMINÉE !" -ForegroundColor Green
Write-Host "=========================" -ForegroundColor Green

Write-Host ""
Write-Host "📋 RÉCAPITULATIF DES INSTALLATIONS:" -ForegroundColor Blue
Write-Host "-----------------------------------" -ForegroundColor Blue

# Vérifier les installations
$tools = @(
    @{Name="curl"; Command="curl"},
    @{Name="ADB"; Command="adb"},
    @{Name="Fastboot"; Command="fastboot"},
    @{Name="idevice_id"; Command="idevice_id"},
    @{Name="ideviceinfo"; Command="ideviceinfo"},
    @{Name="idevicerestore"; Command="idevicerestore"},
    @{Name="ideviceinstaller"; Command="ideviceinstaller"},
    @{Name="idevicediagnostics"; Command="idevicediagnostics"},
    @{Name="idevicebackup2"; Command="idevicebackup2"},
    @{Name="checkra1n"; Command="checkra1n"}
)

foreach ($tool in $tools) {
    if (Get-Command $tool.Command -ErrorAction SilentlyContinue) {
        Write-Host "✅ $($tool.Name) - Installé" -ForegroundColor Green
    } else {
        Write-Host "❌ $($tool.Name) - Non trouvé" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "🎯 PROCHAINES ÉTAPES:" -ForegroundColor Blue
Write-Host "---------------------" -ForegroundColor Blue
Write-Host "1. Redémarrez votre terminal PowerShell" -ForegroundColor White
Write-Host "2. Exécutez: test_simple.ps1 pour vérifier la fiabilité" -ForegroundColor White
Write-Host "3. Lancez votre logiciel depuis IntelliJ IDEA" -ForegroundColor White
Write-Host "4. Testez avec de vrais appareils" -ForegroundColor White

Write-Host ""
Write-Host "📚 DOCUMENTATION:" -ForegroundColor Blue
Write-Host "-----------------" -ForegroundColor Blue
Write-Host "• GUIDE_UTILISATION_FINAL.md - Guide complet d'utilisation" -ForegroundColor White
Write-Host "• RAPPORT_FIABILITE_100_PERCENT.md - Rapport de fiabilité" -ForegroundColor White

Write-Host ""
Write-Host "🚀 Installation terminée le $(Get-Date)" -ForegroundColor Gray
