-- Base de données pour le Logiciel de Déblocage Mobile Multi-Plateforme
-- Nom de la base de données : logiciel_App

DROP DATABASE IF EXISTS logiciel_App;
CREATE DATABASE logiciel_App CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE logiciel_App;

-- Table des utilisateurs/techniciens
CREATE TABLE utilisateurs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role ENUM('admin', 'technicien', 'utilisateur') DEFAULT 'utilisateur',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    derniere_connexion TIMESTAMP NULL,
    actif BOOLEAN DEFAULT TRUE
);

-- Table des appareils supportés
CREATE TABLE appareils_supportes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marque VARCHAR(50) NOT NULL,
    modele VARCHAR(100) NOT NULL,
    plateforme ENUM('iOS', 'Android') NOT NULL,
    version_os_min VARCHAR(20),
    version_os_max VARCHAR(20),
    support_usb BOOLEAN DEFAULT TRUE,
    support_imei BOOLEAN DEFAULT FALSE,
    support_icloud BOOLEAN DEFAULT FALSE,
    support_frp BOOLEAN DEFAULT FALSE,
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des sessions de déblocage
CREATE TABLE sessions_deblocage (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    imei VARCHAR(20),
    modele_appareil VARCHAR(100),
    plateforme ENUM('iOS', 'Android') NOT NULL,
    type_operation ENUM('icloud_bypass', 'unlock_passcode', 'frp_bypass', 'pattern_unlock') NOT NULL,
    methode_connexion ENUM('USB', 'IMEI_distant') NOT NULL,
    statut ENUM('en_cours', 'reussi', 'echec', 'annule') DEFAULT 'en_cours',
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_fin TIMESTAMP NULL,
    details_operation TEXT,
    message_erreur TEXT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- Table des logs d'activité
CREATE TABLE logs_activite (
    id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT,
    utilisateur_id INT NOT NULL,
    action VARCHAR(200) NOT NULL,
    details TEXT,
    adresse_ip VARCHAR(45),
    user_agent TEXT,
    date_action TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions_deblocage(id) ON DELETE SET NULL,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- Table des configurations système
CREATE TABLE configurations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cle_config VARCHAR(100) UNIQUE NOT NULL,
    valeur TEXT NOT NULL,
    description TEXT,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des statistiques
CREATE TABLE statistiques (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_stat DATE NOT NULL,
    total_operations INT DEFAULT 0,
    operations_reussies INT DEFAULT 0,
    operations_echec INT DEFAULT 0,
    plateforme_ios INT DEFAULT 0,
    plateforme_android INT DEFAULT 0,
    methode_usb INT DEFAULT 0,
    methode_imei INT DEFAULT 0,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_date_stat (date_stat)
);

-- Table des serveurs IMEI distants
CREATE TABLE serveurs_imei (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_serveur VARCHAR(100) NOT NULL,
    url_api VARCHAR(255) NOT NULL,
    cle_api VARCHAR(255),
    actif BOOLEAN DEFAULT TRUE,
    plateforme_supportee ENUM('iOS', 'Android', 'Both') DEFAULT 'Both',
    priorite INT DEFAULT 1,
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertion de données de base
INSERT INTO utilisateurs (nom, email, mot_de_passe, role) VALUES 
('Administrateur', 'admin@logiciel-app.com', SHA2('admin123', 256), 'admin'),
('Technicien Test', 'tech@logiciel-app.com', SHA2('tech123', 256), 'technicien');

-- Configuration de base
INSERT INTO configurations (cle_config, valeur, description) VALUES 
('version_logiciel', '1.0.0', 'Version actuelle du logiciel'),
('max_sessions_simultanees', '5', 'Nombre maximum de sessions simultanées'),
('timeout_session', '1800', 'Timeout de session en secondes'),
('serveur_principal_actif', 'true', 'État du serveur principal pour opérations IMEI'),
('mode_debug', 'false', 'Mode debug pour développement');

-- Appareils supportés (exemples)
INSERT INTO appareils_supportes (marque, modele, plateforme, version_os_min, version_os_max, support_usb, support_imei, support_icloud, support_frp) VALUES
-- iOS
('Apple', 'iPhone 5', 'iOS', '6.0', '12.5.7', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 6', 'iOS', '8.0', '12.5.7', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 7', 'iOS', '10.0', '15.8', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 8', 'iOS', '11.0', '16.7', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone X', 'iOS', '11.0', '16.7', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 11', 'iOS', '13.0', '17.2', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 12', 'iOS', '14.0', '17.2', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 13', 'iOS', '15.0', '17.2', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 14', 'iOS', '16.0', '17.2', TRUE, TRUE, TRUE, FALSE),
('Apple', 'iPhone 15', 'iOS', '17.0', '17.2', TRUE, TRUE, TRUE, FALSE),

-- Android
('Samsung', 'Galaxy S Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Samsung', 'Galaxy Note Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Samsung', 'Galaxy A Series', 'Android', '6.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Huawei', 'P Series', 'Android', '5.0', '12.0', TRUE, TRUE, FALSE, TRUE),
('Huawei', 'Mate Series', 'Android', '5.0', '12.0', TRUE, TRUE, FALSE, TRUE),
('Xiaomi', 'Mi Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Xiaomi', 'Redmi Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Oppo', 'Find Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Vivo', 'V Series', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Realme', 'All Models', 'Android', '6.0', '14.0', TRUE, TRUE, FALSE, TRUE),
('Infinix', 'All Models', 'Android', '5.0', '14.0', TRUE, TRUE, FALSE, TRUE);

-- Serveurs IMEI de test
INSERT INTO serveurs_imei (nom_serveur, url_api, plateforme_supportee, priorite) VALUES
('Serveur Principal iOS', 'https://api-ios.logiciel-app.com', 'iOS', 1),
('Serveur Principal Android', 'https://api-android.logiciel-app.com', 'Android', 1),
('Serveur Backup', 'https://backup-api.logiciel-app.com', 'Both', 2);

-- Vues utiles
CREATE VIEW vue_sessions_actives AS
SELECT 
    s.id,
    u.nom as utilisateur,
    s.imei,
    s.modele_appareil,
    s.plateforme,
    s.type_operation,
    s.methode_connexion,
    s.date_debut,
    TIMESTAMPDIFF(MINUTE, s.date_debut, NOW()) as duree_minutes
FROM sessions_deblocage s
JOIN utilisateurs u ON s.utilisateur_id = u.id
WHERE s.statut = 'en_cours';

CREATE VIEW vue_statistiques_quotidiennes AS
SELECT 
    DATE(date_debut) as date_operation,
    COUNT(*) as total_operations,
    SUM(CASE WHEN statut = 'reussi' THEN 1 ELSE 0 END) as operations_reussies,
    SUM(CASE WHEN statut = 'echec' THEN 1 ELSE 0 END) as operations_echec,
    SUM(CASE WHEN plateforme = 'iOS' THEN 1 ELSE 0 END) as operations_ios,
    SUM(CASE WHEN plateforme = 'Android' THEN 1 ELSE 0 END) as operations_android
FROM sessions_deblocage 
WHERE date_debut >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(date_debut)
ORDER BY date_operation DESC;
