#!/bin/bash

# Script d'installation automatique des outils système
# pour le Logiciel de Déblocage Mobile
# Compatible macOS

echo "🚀 Installation des outils système pour le Logiciel de Déblocage Mobile"
echo "=================================================================="

# Vérifier si Homebrew est installé
if ! command -v brew &> /dev/null; then
    echo "📦 Installation de Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    # Ajouter Homebrew au PATH
    echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
    eval "$(/opt/homebrew/bin/brew shellenv)"
else
    echo "✅ Homebrew déjà installé"
fi

echo ""
echo "🔧 Installation des outils iOS..."

# Outils libimobiledevice
echo "📱 Installation de libimobiledevice..."
brew install libimobiledevice

# Outils idevice
echo "🔧 Installation des outils idevice..."
brew install ideviceinstaller

# Outils de restauration
echo "🔄 Installation d'idevicerestore..."
brew install idevicerestore

echo ""
echo "🤖 Installation des outils Android..."

# Android Platform Tools (ADB, Fastboot)
echo "📱 Installation d'Android Platform Tools..."
brew install android-platform-tools

echo ""
echo "🔓 Installation des outils de jailbreak..."

# checkra1n (si disponible)
if brew search checkra1n &> /dev/null; then
    echo "🔓 Installation de checkra1n..."
    brew install checkra1n
else
    echo "⚠️ checkra1n non disponible via Homebrew"
    echo "📥 Téléchargez-le depuis https://checkra.in"
fi

echo ""
echo "🧪 Vérification des installations..."

# Vérifier les outils iOS
echo "📱 Vérification des outils iOS:"
if command -v idevice_id &> /dev/null; then
    echo "  ✅ idevice_id installé"
else
    echo "  ❌ idevice_id manquant"
fi

if command -v ideviceinfo &> /dev/null; then
    echo "  ✅ ideviceinfo installé"
else
    echo "  ❌ ideviceinfo manquant"
fi

if command -v idevicerestore &> /dev/null; then
    echo "  ✅ idevicerestore installé"
else
    echo "  ❌ idevicerestore manquant"
fi

# Vérifier les outils Android
echo "🤖 Vérification des outils Android:"
if command -v adb &> /dev/null; then
    echo "  ✅ ADB installé"
else
    echo "  ❌ ADB manquant"
fi

if command -v fastboot &> /dev/null; then
    echo "  ✅ Fastboot installé"
else
    echo "  ❌ Fastboot manquant"
fi

# Vérifier checkra1n
if command -v checkra1n &> /dev/null; then
    echo "  ✅ checkra1n installé"
else
    echo "  ⚠️ checkra1n non installé (téléchargement manuel requis)"
fi

echo ""
echo "🎉 Installation terminée !"
echo ""
echo "📋 Prochaines étapes:"
echo "1. Connectez un appareil iOS/Android"
echo "2. Lancez le Logiciel de Déblocage Mobile"
echo "3. Testez les fonctionnalités de déblocage"
echo ""
echo "⚠️ Note: Pour checkra1n, vous devrez peut-être l'installer manuellement"
echo "   depuis https://checkra.in si il n'est pas disponible via Homebrew"
