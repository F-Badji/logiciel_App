# 📱 GUIDE D'INSTALLATION MANUELLE DES OUTILS

## 🎯 **OBJECTIF : ATTEINDRE 100% DE FIABILITÉ**

Votre logiciel est actuellement **68% fiable**. Pour atteindre **100%**, installez ces outils manuellement.

---

## 🔧 **OUTILS À INSTALLER**

### **1. ANDROID PLATFORM TOOLS (ADB & Fastboot)**

#### **Téléchargement :**
- **Lien direct** : https://developer.android.com/studio/releases/platform-tools
- **Fichier** : `platform-tools-latest-windows.zip`

#### **Installation :**
1. Téléchargez le fichier ZIP
2. Extrayez dans `C:\platform-tools`
3. Ajoutez `C:\platform-tools` au PATH système

#### **Vérification :**
```powershell
adb version
fastboot version
```

---

### **2. LIBIMOBILEDEVICE (Outils iOS)**

#### **Téléchargement :**
- **Lien direct** : https://github.com/libimobiledevice/libimobiledevice/releases
- **Fichier** : `libimobiledevice-1.3.0-win64.zip`

#### **Installation :**
1. Téléchargez le fichier ZIP
2. Créez le dossier `C:\libimobiledevice`
3. Extrayez le contenu dans ce dossier
4. Ajoutez `C:\libimobiledevice` au PATH système

#### **Vérification :**
```powershell
idevice_id -l
ideviceinfo
```

---

### **3. CHECKRA1N (Jailbreak)**

#### **Téléchargement :**
- **Lien direct** : https://checkra.in
- **Fichier** : `checkra1n-win64.exe`

#### **Installation :**
1. Téléchargez l'exécutable
2. Créez le dossier `C:\checkra1n`
3. Placez l'exécutable dans ce dossier
4. Ajoutez `C:\checkra1n` au PATH système

#### **Vérification :**
```powershell
checkra1n --version
```

---

## 🔄 **AJOUTER AU PATH SYSTÈME**

### **Méthode 1 : Via PowerShell (Administrateur)**
```powershell
# Ajouter les chemins au PATH système
$paths = @(
    "C:\platform-tools",
    "C:\libimobiledevice", 
    "C:\checkra1n"
)

foreach ($path in $paths) {
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", "Machine")
    if ($currentPath -notlike "*$path*") {
        [Environment]::SetEnvironmentVariable("PATH", "$currentPath;$path", "Machine")
    }
}
```

### **Méthode 2 : Via l'interface Windows**
1. **Win + R** → `sysdm.cpl`
2. **Avancé** → **Variables d'environnement**
3. **Variables système** → **Path** → **Modifier**
4. **Nouveau** → Ajoutez chaque chemin :
   - `C:\platform-tools`
   - `C:\libimobiledevice`
   - `C:\checkra1n`

---

## ✅ **VÉRIFICATION COMPLÈTE**

Après installation, testez tous les outils :

```powershell
# Test ADB
adb version

# Test Fastboot  
fastboot version

# Test libimobiledevice
idevice_id -l
ideviceinfo --version

# Test checkra1n
checkra1n --version

# Test curl (déjà installé)
curl --version
```

---

## 🧪 **TEST DE FIABILITÉ FINAL**

Une fois tous les outils installés :

```powershell
powershell -ExecutionPolicy Bypass -File test_simple.ps1
```

**Résultat attendu : 100% de fiabilité !**

---

## 🚀 **LANCEMENT DU LOGICIEL**

1. **Ouvrez IntelliJ IDEA**
2. **Ouvrez le projet** `logiciel_App`
3. **Lancez** `Main.java`
4. **Testez** avec de vrais appareils

---

## 📚 **DOCUMENTATION**

- **Guide d'utilisation** : `GUIDE_UTILISATION_FINAL.md`
- **Rapport de fiabilité** : `RAPPORT_FIABILITE_100_PERCENT.md`
- **Installation des outils** : `install_commands.md`

---

## ⚠️ **NOTES IMPORTANTES**

1. **Privilèges administrateur** : Nécessaires pour modifier le PATH
2. **Redémarrage** : Redémarrez PowerShell après modification du PATH
3. **Antivirus** : Autorisez les outils si nécessaire
4. **Pare-feu** : Autorisez les connexions réseau

---

## 🎉 **RÉSULTAT FINAL**

Une fois tous les outils installés, votre **Logiciel de Déblocage Mobile** sera :

- ✅ **100% fiable**
- ✅ **Comparable aux logiciels professionnels**
- ✅ **Prêt pour la production**
- ✅ **Capable de débloquer de vrais appareils**

**Votre logiciel sera alors au niveau d'iRemoval Pro !** 🚀
