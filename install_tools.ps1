# Script PowerShell d'installation automatique des outils système
# pour le Logiciel de Déblocage Mobile
# Compatible Windows

Write-Host "🚀 Installation des outils système pour le Logiciel de Déblocage Mobile" -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# Vérifier si Chocolatey est installé
if (!(Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "📦 Installation de Chocolatey..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
} else {
    Write-Host "✅ Chocolatey déjà installé" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔧 Installation des outils Android..." -ForegroundColor Yellow

# Android Platform Tools (ADB, Fastboot)
Write-Host "📱 Installation d'Android Platform Tools..." -ForegroundColor Yellow
choco install android-sdk-platform-tools -y

Write-Host ""
Write-Host "📱 Installation des outils iOS..." -ForegroundColor Yellow

# Télécharger et installer libimobiledevice pour Windows
Write-Host "📱 Téléchargement de libimobiledevice..." -ForegroundColor Yellow
$libimobiledeviceUrl = "https://github.com/libimobiledevice/libimobiledevice/releases/latest/download/libimobiledevice-windows.zip"
$libimobiledevicePath = "$env:TEMP\libimobiledevice-windows.zip"
$libimobiledeviceDir = "C:\Program Files\libimobiledevice"

try {
    Invoke-WebRequest -Uri $libimobiledeviceUrl -OutFile $libimobiledevicePath
    Expand-Archive -Path $libimobiledevicePath -DestinationPath $libimobiledeviceDir -Force
    Remove-Item $libimobiledevicePath
    
    # Ajouter au PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
    if ($currentPath -notlike "*$libimobiledeviceDir*") {
        [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$libimobiledeviceDir", "Machine")
    }
    
    Write-Host "✅ libimobiledevice installé" -ForegroundColor Green
} catch {
    Write-Host "❌ Erreur lors de l'installation de libimobiledevice" -ForegroundColor Red
    Write-Host "📥 Téléchargez manuellement depuis: https://github.com/libimobiledevice/libimobiledevice" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🔓 Installation des outils de jailbreak..." -ForegroundColor Yellow

# Télécharger checkra1n pour Windows
Write-Host "🔓 Téléchargement de checkra1n..." -ForegroundColor Yellow
$checkra1nUrl = "https://checkra.in/downloads/windows"
$checkra1nDir = "C:\Program Files\checkra1n"

try {
    # Créer le répertoire
    if (!(Test-Path $checkra1nDir)) {
        New-Item -ItemType Directory -Path $checkra1nDir -Force
    }
    
    Write-Host "📥 Téléchargez checkra1n manuellement depuis: https://checkra.in" -ForegroundColor Yellow
    Write-Host "📁 Placez-le dans: $checkra1nDir" -ForegroundColor Yellow
    
} catch {
    Write-Host "❌ Erreur lors de l'installation de checkra1n" -ForegroundColor Red
}

Write-Host ""
Write-Host "🧪 Vérification des installations..." -ForegroundColor Yellow

# Vérifier les outils Android
Write-Host "🤖 Vérification des outils Android:" -ForegroundColor Cyan
if (Get-Command adb -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ ADB installé" -ForegroundColor Green
} else {
    Write-Host "  ❌ ADB manquant" -ForegroundColor Red
}

if (Get-Command fastboot -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ Fastboot installé" -ForegroundColor Green
} else {
    Write-Host "  ❌ Fastboot manquant" -ForegroundColor Red
}

# Vérifier les outils iOS
Write-Host "📱 Vérification des outils iOS:" -ForegroundColor Cyan
if (Get-Command idevice_id -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ idevice_id installé" -ForegroundColor Green
} else {
    Write-Host "  ❌ idevice_id manquant" -ForegroundColor Red
}

if (Get-Command ideviceinfo -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ ideviceinfo installé" -ForegroundColor Green
} else {
    Write-Host "  ❌ ideviceinfo manquant" -ForegroundColor Red
}

if (Get-Command idevicerestore -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ idevicerestore installé" -ForegroundColor Green
} else {
    Write-Host "  ❌ idevicerestore manquant" -ForegroundColor Red
}

# Vérifier checkra1n
if (Get-Command checkra1n -ErrorAction SilentlyContinue) {
    Write-Host "  ✅ checkra1n installé" -ForegroundColor Green
} else {
    Write-Host "  ⚠️ checkra1n non installé (téléchargement manuel requis)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 Installation terminée !" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Prochaines étapes:" -ForegroundColor Cyan
Write-Host "1. Redémarrez votre terminal/PowerShell" -ForegroundColor White
Write-Host "2. Connectez un appareil iOS/Android" -ForegroundColor White
Write-Host "3. Lancez le Logiciel de Déblocage Mobile" -ForegroundColor White
Write-Host "4. Testez les fonctionnalités de déblocage" -ForegroundColor White
Write-Host ""
Write-Host "⚠️ Notes importantes:" -ForegroundColor Yellow
Write-Host "- Pour checkra1n, téléchargez-le manuellement depuis https://checkra.in" -ForegroundColor White
Write-Host "- Pour libimobiledevice, téléchargez-le depuis https://github.com/libimobiledevice" -ForegroundColor White
Write-Host "- Redémarrez votre terminal pour que les changements de PATH prennent effet" -ForegroundColor White
