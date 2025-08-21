# Script d'installation simple des outils système
# Compatible Windows

Write-Host "INSTALLATION DES OUTILS SYSTÈME" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan
Write-Host ""

# ÉTAPE 1: CHOCOLATEY
Write-Host "ETAPE 1: INSTALLATION DE CHOCOLATEY" -ForegroundColor Blue
Write-Host "------------------------------------" -ForegroundColor Blue

if (Get-Command choco -ErrorAction SilentlyContinue) {
    Write-Host "OK: Chocolatey deja installe" -ForegroundColor Green
} else {
    Write-Host "Installation de Chocolatey..." -ForegroundColor Yellow
    try {
        Set-ExecutionPolicy Bypass -Scope Process -Force
        [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
        iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        Write-Host "OK: Chocolatey installe" -ForegroundColor Green
    } catch {
        Write-Host "ERREUR: Installation de Chocolatey echouee" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "ETAPE 2: INSTALLATION DES OUTILS ANDROID" -ForegroundColor Blue
Write-Host "------------------------------------------" -ForegroundColor Blue

# Installation manuelle d'ADB
Write-Host "Telechargement d'ADB..." -ForegroundColor Yellow
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
    
    Write-Host "OK: ADB installe dans C:\platform-tools" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Telechargement d'ADB echoue" -ForegroundColor Red
    Write-Host "   Telechargez manuellement: https://developer.android.com/studio/releases/platform-tools" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ETAPE 3: INSTALLATION DES OUTILS iOS" -ForegroundColor Blue
Write-Host "--------------------------------------" -ForegroundColor Blue

# Installation manuelle de libimobiledevice
Write-Host "Telechargement de libimobiledevice..." -ForegroundColor Yellow
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
    
    Write-Host "OK: libimobiledevice installe dans C:\libimobiledevice" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Telechargement de libimobiledevice echoue" -ForegroundColor Red
    Write-Host "   Telechargez manuellement: https://github.com/libimobiledevice/libimobiledevice/releases" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ETAPE 4: INSTALLATION DE CHECKRA1N" -ForegroundColor Blue
Write-Host "------------------------------------" -ForegroundColor Blue

# Télécharger checkra1n
Write-Host "Telechargement de checkra1n..." -ForegroundColor Yellow
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
    
    Write-Host "OK: checkra1n telecharge dans C:\checkra1n" -ForegroundColor Green
} catch {
    Write-Host "ERREUR: Telechargement de checkra1n echoue" -ForegroundColor Red
    Write-Host "   Telechargez manuellement: https://checkra.in" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ETAPE 5: VERIFICATION DE CURL" -ForegroundColor Blue
Write-Host "-------------------------------" -ForegroundColor Blue

if (Get-Command curl -ErrorAction SilentlyContinue) {
    Write-Host "OK: curl deja installe" -ForegroundColor Green
} else {
    Write-Host "ERREUR: curl non trouve" -ForegroundColor Red
    Write-Host "   curl est generalement inclus dans Windows 10/11" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "ETAPE 6: REFRESH DU PATH" -ForegroundColor Blue
Write-Host "-------------------------" -ForegroundColor Blue

# Rafraîchir les variables d'environnement
Write-Host "Rafraichissement des variables d'environnement..." -ForegroundColor Yellow
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","User")

Write-Host ""
Write-Host "INSTALLATION TERMINEE !" -ForegroundColor Green
Write-Host "=======================" -ForegroundColor Green

Write-Host ""
Write-Host "RECAPITULATIF DES INSTALLATIONS:" -ForegroundColor Blue
Write-Host "--------------------------------" -ForegroundColor Blue

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
        Write-Host "OK: $($tool.Name) - Installe" -ForegroundColor Green
    } else {
        Write-Host "ERREUR: $($tool.Name) - Non trouve" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "PROCHAINES ETAPES:" -ForegroundColor Blue
Write-Host "------------------" -ForegroundColor Blue
Write-Host "1. Redemarrez votre terminal PowerShell" -ForegroundColor White
Write-Host "2. Executez: test_simple.ps1 pour verifier la fiabilite" -ForegroundColor White
Write-Host "3. Lancez votre logiciel depuis IntelliJ IDEA" -ForegroundColor White
Write-Host "4. Testez avec de vrais appareils" -ForegroundColor White

Write-Host ""
Write-Host "Installation terminee le $(Get-Date)" -ForegroundColor Gray
