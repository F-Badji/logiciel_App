# Installation des Outils de Flashage

## 🛠️ Installation Automatique (Recommandée)

Le logiciel peut installer automatiquement tous les outils requis via Homebrew.

### Prérequis
- macOS 10.15 ou plus récent
- Homebrew installé

### Installation de Homebrew (si nécessaire)
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### Installation Automatique via l'Application
1. Démarrer l'application de déblocage
2. Aller dans **Paramètres > Outils Système**
3. Cliquer sur **"Installer Outils de Flashage"**
4. Attendre la fin de l'installation

## 🔧 Installation Manuelle

### Outils iOS

#### libimobiledevice
```bash
brew install libimobiledevice
```

#### idevicerestore
```bash
brew install idevicerestore
```

### Outils Android

#### Android Platform Tools (ADB/Fastboot)
```bash
brew install android-platform-tools
```

#### Heimdall (Samsung)
```bash
brew install heimdall
```

## ✅ Vérification de l'Installation

### Test des Outils iOS
```bash
# Vérifier libimobiledevice
ideviceinfo --help

# Vérifier idevicerestore
idevicerestore --help
```

### Test des Outils Android
```bash
# Vérifier ADB
adb version

# Vérifier Fastboot
fastboot --version

# Vérifier Heimdall
heimdall version
```

## 🚨 Dépannage

### Problème : Command not found
**Solution :** Ajouter les outils au PATH
```bash
echo 'export PATH="/opt/homebrew/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

### Problème : Permission denied
**Solution :** Corriger les permissions
```bash
sudo chmod +x /opt/homebrew/bin/adb
sudo chmod +x /opt/homebrew/bin/fastboot
```

### Problème : Homebrew non installé
**Solution :** Installer Homebrew puis réessayer
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

## 📋 Statut des Outils

L'application affiche le statut d'installation :
- ✅ **Installé** - Outil disponible et fonctionnel
- ❌ **Manquant** - Outil non installé
- ⚠️ **Erreur** - Problème de configuration

## 🔄 Mise à Jour

Pour mettre à jour les outils :
```bash
brew update
brew upgrade libimobiledevice idevicerestore android-platform-tools heimdall
```
