# Guide d'Utilisation - Flashage de Firmware

## 🚀 Introduction

Le logiciel de déblocage mobile intègre maintenant des fonctionnalités complètes de flashage de firmware pour les appareils iOS et Android. Cette fonctionnalité permet de :

- **Flasher des firmwares complets** iOS et Android
- **Flasher des partitions spécifiques** (bootloader, recovery, system, boot)
- **Débloquer les bootloaders** Android
- **Restaurer des appareils brickés**

## 🔧 Prérequis Techniques

### Outils Système Requis

#### Pour iOS :
- `libimobiledevice` - Communication avec les appareils iOS
- `idevicerestore` - Restauration de firmware iOS
- `unzip` - Extraction des fichiers IPSW

#### Pour Android :
- `adb` - Android Debug Bridge
- `fastboot` - Mode fastboot pour flashage
- `heimdall` - Flashage Samsung (optionnel)

### Installation Automatique (macOS)

Le logiciel peut installer automatiquement les outils via Homebrew :

```bash
# Vérification automatique au démarrage
# Installation proposée si outils manquants
brew install libimobiledevice idevicerestore
brew install android-platform-tools
brew install heimdall
```

## 📱 Types de Flashage Supportés

### 1. Flashage iOS Complet

**Formats supportés :** `.ipsw`

**Processus :**
1. Vérification du mode DFU/Recovery
2. Validation du firmware IPSW
3. Extraction du firmware
4. Flashage via `idevicerestore`
5. Vérification post-flashage

**Commande utilisée :**
```bash
idevicerestore --latest firmware.ipsw
```

### 2. Flashage Android Complet

**Formats supportés :** `.zip`, `.tar`, `.tar.md5`

**Processus :**
1. Vérification du mode Fastboot/Download
2. Validation du firmware
3. Extraction si nécessaire
4. Flashage via Fastboot ou Heimdall
5. Redémarrage et vérification

**Commandes utilisées :**
```bash
# Fastboot (général)
fastboot flashall

# Heimdall (Samsung)
heimdall flash --BOOTLOADER bootloader.img --RECOVERY recovery.img
```

### 3. Flashage de Partitions Spécifiques

**Partitions supportées :**
- `bootloader` - Chargeur de démarrage
- `recovery` - Mode recovery
- `system` - Système Android
- `boot` - Image de démarrage
- `userdata` - Données utilisateur

**Commandes utilisées :**
```bash
fastboot flash [partition] [image.img]
```

## 🖥️ Utilisation de l'Interface

### Accès à l'Interface de Flashage

1. **Démarrer l'application** principale
2. **Cliquer sur "⚡ Flashage"** dans la barre de navigation
3. **L'interface de flashage s'ouvre** dans une nouvelle fenêtre

### Étapes de Flashage

#### 1. Détection d'Appareil
- Connecter l'appareil en mode approprié
- Cliquer sur **"Détecter Appareils"**
- Vérifier la détection dans la liste déroulante

#### 2. Sélection du Type de Flashage
- **"iOS Firmware"** - Firmware complet iPhone/iPad
- **"Android Firmware"** - Firmware complet Android
- **"Partition Spécifique"** - Flashage d'une partition

#### 3. Sélection du Firmware
- Cliquer sur **"Parcourir"**
- Sélectionner le fichier firmware approprié
- Le chemin s'affiche automatiquement

#### 4. Configuration (si partition)
- Saisir le **nom de la partition** (ex: recovery, boot)
- Vérifier la compatibilité

#### 5. Démarrage du Flashage
- Cliquer sur **"Démarrer Flashage"**
- Suivre la progression via la barre de progression
- Consulter les logs en temps réel

#### 6. Contrôles Disponibles
- **"Annuler"** - Arrêter l'opération en cours
- **"Effacer Logs"** - Nettoyer la zone de logs

## ⚠️ Modes d'Appareils Requis

### iOS
- **Mode DFU** - Device Firmware Upgrade
- **Mode Recovery** - Mode de récupération
- **Mode Normal** - Pour certaines opérations

### Android
- **Mode Fastboot** - Flashage général
- **Mode Download** - Samsung/LG (via Heimdall)
- **Mode Recovery** - Certaines opérations

## 🔒 Sécurité et Précautions

### Vérifications Automatiques
- ✅ **Validation du firmware** - Format et intégrité
- ✅ **Compatibilité appareil** - Modèle et version
- ✅ **Mode appareil** - Vérification du mode requis
- ✅ **Espace disque** - Vérification de l'espace disponible

### Avertissements
- ⚠️ **Risque de brick** - Flashage incorrect peut endommager l'appareil
- ⚠️ **Perte de données** - Sauvegarde recommandée avant flashage
- ⚠️ **Garantie** - Le flashage peut annuler la garantie

## 📊 Suivi et Logs

### Informations Affichées
- **Progression en temps réel** - Barre de progression
- **Logs détaillés** - Chaque étape du processus
- **Statut de l'opération** - Succès/Échec/En cours
- **Temps estimé** - Durée approximative

### Types de Messages
- 🔵 **Info** - Informations générales
- 🟡 **Avertissement** - Attention requise
- 🔴 **Erreur** - Problème critique
- 🟢 **Succès** - Opération réussie

## 🛠️ Dépannage

### Problèmes Courants

#### Appareil Non Détecté
```
Solution :
1. Vérifier la connexion USB
2. Installer les pilotes appropriés
3. Activer le mode développeur (Android)
4. Faire confiance à l'ordinateur (iOS)
```

#### Outils Manquants
```
Solution :
1. Installer Homebrew
2. Exécuter : brew install libimobiledevice android-platform-tools
3. Redémarrer l'application
```

#### Échec de Flashage
```
Solution :
1. Vérifier le mode de l'appareil
2. Valider le firmware (compatibilité)
3. Libérer de l'espace disque
4. Redémarrer en mode approprié
```

## 🔄 Workflow Complet

### Préparation
1. **Sauvegarder** les données importantes
2. **Charger** l'appareil (>50% batterie)
3. **Télécharger** le firmware approprié
4. **Installer** les outils système requis

### Exécution
1. **Connecter** l'appareil
2. **Démarrer** l'interface de flashage
3. **Configurer** les paramètres
4. **Lancer** le processus
5. **Surveiller** la progression

### Post-Flashage
1. **Vérifier** le bon fonctionnement
2. **Restaurer** les données si nécessaire
3. **Configurer** l'appareil
4. **Tester** les fonctionnalités

## 📞 Support

En cas de problème :
- Consulter les **logs détaillés**
- Vérifier la **compatibilité** du firmware
- Contacter le **support technique**
- Consulter la **documentation** en ligne

---

**⚠️ ATTENTION :** Le flashage de firmware est une opération avancée qui peut endommager votre appareil si mal exécutée. Procédez avec précaution et assurez-vous de comprendre les risques.
