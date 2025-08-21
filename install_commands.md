# 📱 GUIDE D'INSTALLATION DES OUTILS SYSTÈME

## 🚀 **INSTALLATION AUTOMATIQUE (RECOMMANDÉE)**

Exécutez le script complet :
```powershell
powershell -ExecutionPolicy Bypass -File install_all_tools.ps1
```

---

## 🔧 **INSTALLATION MANUELLE PAR OUTIL**

### **1. CHOCOLATEY (Gestionnaire de paquets)**

```powershell
# Installation de Chocolatey
Set-ExecutionPolicy Bypass -Scope Process -Force
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

### **2. OUTILS ANDROID (ADB & Fastboot)**

#### **Via Chocolatey :**
```powershell
choco install android-sdk-platform-tools -y
```

#### **Installation manuelle :**
```powershell
# Télécharger et installer ADB manuellement
$adbUrl = "https://dl.google.com/android/repository/platform-tools-latest-windows.zip"
$adbZip = "$env:TEMP\platform-tools.zip"
$adbDir = "C:\platform-tools"

Invoke-WebRequest -Uri $adbUrl -OutFile $adbZip
Expand-Archive -Path $adbZip -DestinationPath "C:\" -Force
Remove-Item $adbZip

# Ajouter au PATH
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;$adbDir", "Machine")
```

### **3. OUTILS iOS (libimobiledevice)**

#### **Via Chocolatey :**
```powershell
choco install libimobiledevice -y
```

#### **Installation manuelle :**
```powershell
# Télécharger et installer libimobiledevice manuellement
$libiUrl = "https://github.com/libimobiledevice/libimobiledevice/releases/download/1.3.0/libimobiledevice-1.3.0-win64.zip"
$libiZip = "$env:TEMP\libimobiledevice.zip"
$libiDir = "C:\libimobiledevice"

Invoke-WebRequest -Uri $libiUrl -OutFile $libiZip
New-Item -ItemType Directory -Path $libiDir -Force
Expand-Archive -Path $libiZip -DestinationPath $libiDir -Force
Remove-Item $libiZip

# Ajouter au PATH
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;$libiDir", "Machine")
```

### **4. CHECKRA1N (Jailbreak)**

```powershell
# Télécharger checkra1n
$checkra1nUrl = "https://assets.checkra.in/downloads/windows/checkra1n-win64.exe"
$checkra1nPath = "C:\checkra1n\checkra1n.exe"

New-Item -ItemType Directory -Path "C:\checkra1n" -Force
Invoke-WebRequest -Uri $checkra1nUrl -OutFile $checkra1nPath

# Ajouter au PATH
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;C:\checkra1n", "Machine")
```

### **5. CURL (Outils réseau)**

#### **Via Chocolatey :**
```powershell
choco install curl -y
```

#### **Vérification :**
```powershell
curl --version
```

---

## 🔄 **RÉFRESH DES VARIABLES D'ENVIRONNEMENT**

Après installation, rafraîchissez le PATH :
```powershell
$env:PATH = [System.Environment]::GetEnvironmentVariable("PATH","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH","User")
```

---

## ✅ **VÉRIFICATION DES INSTALLATIONS**

Testez chaque outil :
```powershell
# Test ADB
adb version

# Test Fastboot
fastboot version

# Test libimobiledevice
idevice_id -l

# Test checkra1n
checkra1n --version

# Test curl
curl --version
```

---

## 🧪 **TEST DE FIABILITÉ COMPLET**

Après installation, testez votre logiciel :
```powershell
powershell -ExecutionPolicy Bypass -File test_simple.ps1
```

---

## 📚 **LIENS DE TÉLÉCHARGEMENT MANUEL**

Si les commandes automatiques échouent :

- **Android Platform Tools** : https://developer.android.com/studio/releases/platform-tools
- **libimobiledevice** : https://github.com/libimobiledevice/libimobiledevice/releases
- **checkra1n** : https://checkra.in
- **Chocolatey** : https://chocolatey.org/install

---

## ⚠️ **NOTES IMPORTANTES**

1. **Privilèges administrateur** : Certaines installations nécessitent des droits administrateur
2. **Antivirus** : Désactivez temporairement l'antivirus si nécessaire
3. **Pare-feu** : Autorisez les connexions réseau pour les téléchargements
4. **Redémarrage** : Redémarrez PowerShell après installation pour que le PATH soit mis à jour

---

## 🎯 **OBJECTIF : 100% DE FIABILITÉ**

Une fois tous les outils installés, votre logiciel devrait atteindre **100% de fiabilité** et être comparable aux logiciels professionnels comme **iRemoval Pro** !
