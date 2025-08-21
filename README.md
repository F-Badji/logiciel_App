# 📱 Logiciel de Déblocage Mobile Multi-Plateforme

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/logicielapp/deblocage-mobile)
[![Java](https://img.shields.io/badge/java-17-red.svg)](https://openjdk.java.net/)
[![JavaFX](https://img.shields.io/badge/javafx-20.0.2-orange.svg)](https://openjfx.io/)
[![MySQL](https://img.shields.io/badge/mysql-8.0-lightblue.svg)](https://mysql.com/)

**Un logiciel professionnel complet pour le déblocage d'iPhone/iPad et appareils Android avec interface moderne JavaFX**

---

## 🎯 **Fonctionnalités Principales**

### 🍏 **Support iOS/iPadOS**
- ✅ **Déblocage iCloud** - Contournement d'Activation Lock
- ✅ **Déverrouillage Code d'Accès** - Suppression PIN/Face ID/Touch ID
- ✅ **Bypass Screen Time** - Désactivation temps d'écran
- ✅ **Activation Lock Bypass** - Contournement verrouillage d'activation
- ✅ **Compatibilité complète** - iPhone 5 jusqu'aux iPhone 15+
- ✅ **Support toutes versions iOS** - iOS 6 à iOS 17+

### 🤖 **Support Android**
- ✅ **FRP Bypass** - Contournement Factory Reset Protection
- ✅ **Pattern/PIN Unlock** - Déblocage motif et codes
- ✅ **Samsung Account Bypass** - Contournement compte Samsung
- ✅ **Mi Account Bypass** - Contournement compte Xiaomi
- ✅ **Multi-marques** - Samsung, Huawei, Xiaomi, Oppo, Vivo, etc.
- ✅ **Android 5.0 à 14+** - Large compatibilité versions

### 🔧 **Méthodes de Connexion**
- 🔌 **Connexion USB locale** - Détection automatique des appareils
- 📡 **Déblocage IMEI à distance** - Opérations via serveurs sécurisés
- 🔍 **Auto-détection** - Reconnaissance automatique marque/modèle

---

## 🏗️ **Architecture Technique**

### **Stack Technologique**
- **Frontend**: JavaFX 20.0.2 avec interface moderne et responsive
- **Backend**: Java 17 avec architecture MVC
- **Base de données**: MySQL 8.0 avec pool de connexions HikariCP
- **Build**: Maven 3.9+ avec gestion automatique des dépendances
- **USB**: Détection via libusb4java pour communication appareils
- **Logging**: SLF4J + Logback pour traçabilité complète

### **Structure du Projet**
```
logiciel_App/
├── 📁 src/main/java/
│   └── com/logicielapp/
│       ├── 📁 controller/     # Contrôleurs JavaFX
│       ├── 📁 model/         # Modèles de données  
│       ├── 📁 service/       # Services métier
│       └── 📁 util/          # Utilitaires (DB, etc.)
├── 📁 src/main/resources/
│   ├── 📁 fxml/             # Interfaces FXML
│   ├── 📁 styles/           # CSS modernes
│   └── database.properties  # Config DB
├── 📁 database/             # Scripts SQL
├── pom.xml                  # Configuration Maven
└── setup_database.sh       # Script d'installation
```

---

## ⚡ **Installation & Configuration**

### **Prérequis**
- ✅ **Java 17+** (OpenJDK recommandé)
- ✅ **Maven 3.9+** pour la compilation
- ✅ **MySQL 8.0+** ou XAMPP/MAMP
- ✅ **macOS/Windows/Linux** support multi-plateforme

### **1. Installation MySQL/XAMPP**
```bash
# Démarrer XAMPP (si utilisé)
sudo /Applications/XAMPP/xamppfiles/xampp start

# Ou démarrer MySQL directement
brew services start mysql  # macOS
# sudo systemctl start mysql  # Linux
```

### **2. Configuration Base de Données**
```bash
cd /Applications/XAMPP/xamppfiles/htdocs/logiciel_App

# Rendre le script exécutable
chmod +x setup_database.sh

# Exécuter l'installation
./setup_database.sh
```

### **3. Compilation et Lancement**
```bash
# Compilation du projet
mvn clean compile

# Exécution de l'application
mvn javafx:run

# Ou créer un JAR exécutable
mvn package
java -jar target/logiciel-deblocage-mobile-1.0.0.jar
```

---

## 🎨 **Interface Utilisateur**

### **Design Moderne**
- 🎨 **Interface Material Design** avec CSS personnalisés
- 🌈 **Thème professionnel** avec dégradés et ombres
- 📱 **Responsive** adaptatif à toutes les résolutions
- 🔥 **Animations fluides** et transitions élégantes

### **Navigation Intuitive**
- 🏠 **Page d'Accueil** - Sélection plateforme et méthode
- 🔧 **Opérations** - Console temps réel et contrôles
- 📊 **Statistiques** - Rapports et historiques
- ⚙️ **Paramètres** - Configuration avancée
- ❓ **Aide** - Documentation intégrée

---

## 🔒 **Base de Données**

### **Schéma Complet**
- 👥 **utilisateurs** - Gestion des comptes (admin/technicien)
- 📱 **appareils_supportes** - Base de données des appareils
- 🔄 **sessions_deblocage** - Historique des opérations
- 📝 **logs_activite** - Traçabilité complète
- ⚙️ **configurations** - Paramètres système
- 📊 **statistiques** - Données analytiques
- 🌐 **serveurs_imei** - Configuration serveurs distants

### **Sécurité & Performance**
- 🔐 **Mots de passe chiffrés** SHA-256
- 🏊 **Pool de connexions** optimisé HikariCP
- 📈 **Requêtes optimisées** avec index appropriés
- 🔄 **Transactions ACID** pour intégrité des données

---

## 🚀 **Utilisation**

### **1. Déblocage USB Local**
1. 🔌 Connecter l'appareil via USB
2. 🔍 Cliquer sur "Analyser Appareils USB"
3. ✅ Sélectionner le type d'opération
4. 🚀 Lancer le processus de déblocage
5. 👀 Suivre la progression en temps réel

### **2. Déblocage IMEI Distant**
1. 📱 Choisir "Déblocage IMEI"
2. ⌨️ Saisir le numéro IMEI
3. 🌐 Sélectionner le serveur approprié
4. 🚀 Démarrer l'opération à distance
5. ⏳ Attendre la confirmation

### **3. Console Temps Réel**
```
[14:32:15] 🚀 Logiciel de Déblocage Mobile v1.0 - Prêt
[14:32:16] 💡 Conseil: Connectez votre appareil et cliquez sur 'Analyser Appareils USB'
[14:32:45] 📱 Appareil détecté: iPhone 13 (iOS)
[14:33:02] 🍏 Mode iOS/iPadOS - Connexion USB sélectionné
[14:33:05] 🚀 Démarrage du processus de déblocage...
[14:33:06] 🔍 Analyse de l'appareil...
[14:33:08] 📋 Vérification de la compatibilité...
[14:33:12] 🔧 Préparation des outils de déblocage...
[14:33:15] ✅ Déblocage terminé avec succès!
```

---

## 📊 **Statistiques & Monitoring**

### **Tableaux de Bord**
- 📈 **Taux de réussite** par plateforme
- ⏱️ **Temps moyen** des opérations
- 📱 **Top appareils** les plus traités
- 👥 **Activité utilisateurs** et techniciens

### **Rapports Automatiques**
- 📅 **Rapports quotidiens** envoyés par email
- 📊 **Graphiques de performance** exportables
- 🔍 **Logs détaillés** pour débogage
- 💾 **Sauvegarde automatique** des données

---

## 🛠️ **Configuration Avancée**

### **Paramètres Système**
```properties
# database.properties
db.url=jdbc:mysql://localhost:3306/logiciel_App
db.username=root
db.password=your_password

# Application
max_sessions_simultanees=5
timeout_session=1800
mode_debug=false
serveur_principal_actif=true
```

### **Personnalisation Interface**
- 🎨 **Thèmes personnalisés** via CSS
- 🌐 **Multi-langues** (FR/EN/ES)
- ⚙️ **Configuration utilisateur** sauvegardée
- 🔧 **Mode expert** avec options avancées

---

## 🔧 **Développement & API**

### **Extensions Possibles**
- 🌐 **API REST** pour intégration externe
- 📱 **App mobile** de contrôle à distance
- ☁️ **Cloud sync** pour synchronisation
- 🔌 **Plugins** pour nouveaux appareils

### **Structure Modulaire**
```java
// Exemple d'ajout de nouveau service
public class NewBrandUnlockService extends UnlockService {
    @Override
    public UnlockOperation unlock(Device device) {
        // Implémentation spécifique
    }
}
```

---

## 📞 **Support & Communauté**

### **Documentation**
- 📖 **Wiki complet** avec guides détaillés
- 🎥 **Tutoriels vidéo** pas-à-pas
- 💡 **FAQ** questions fréquentes
- 🔧 **Troubleshooting** résolution de problèmes

### **Contact**
- 📧 **Email**: support@logiciel-app.com
- 💬 **Discord**: [Communauté Logiciel App](https://discord.gg/logicielapp)
- 🐛 **Issues**: [GitHub Issues](https://github.com/logicielapp/issues)
- 📱 **Telegram**: @LogicielAppSupport

---

## 📝 **Licence & Légal**

### **Licence Professionnelle**
- ✅ **Usage commercial** autorisé
- ✅ **Support technique** inclus
- ✅ **Mises à jour** gratuites 1 an
- ✅ **Formation** personnalisée disponible

### **Disclaimer**
⚠️ **Important**: Ce logiciel est destiné à un usage professionnel légitime uniquement. L'utilisateur est responsable du respect des lois locales et des droits de propriété des appareils traités.

---

## 🏆 **Crédits**

Développé avec ❤️ par l'équipe **Logiciel App**

**Technologies utilisées**:
- ☕ Java & JavaFX pour interface native
- 🐬 MySQL pour persistance des données
- 🔧 Maven pour gestion de projet
- 🎨 CSS3 pour design moderne
- 📚 SLF4J/Logback pour logging

---

*Dernière mise à jour: 17 août 2025*
