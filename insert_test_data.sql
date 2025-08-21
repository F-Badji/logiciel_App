-- Script pour insérer des données de test réalistes dans la base de données
-- À exécuter après la création du schéma principal

USE logiciel_App;

-- Insérer des sessions de déblocage récentes avec vraies dates/heures
INSERT INTO sessions_deblocage (utilisateur_id, imei, modele_appareil, plateforme, type_operation, methode_connexion, statut, date_debut, date_fin, details_operation) VALUES
(1, '353328111234567', 'iPhone 15 Pro Max', 'iOS', 'icloud_bypass', 'USB', 'reussi', DATE_SUB(NOW(), INTERVAL 3 MINUTE), DATE_SUB(NOW(), INTERVAL 1 MINUTE), 'iCloud Bypass réussi via checkm8'),
(1, '354569111234567', 'Galaxy S24 Ultra', 'Android', 'frp_bypass', 'USB', 'reussi', DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 12 MINUTE), 'FRP Bypass terminé avec succès'),
(2, '111111111111111', 'Appareil Inconnu', 'iOS', 'unlock_passcode', 'IMEI_distant', 'echec', DATE_SUB(NOW(), INTERVAL 22 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE), 'IMEI invalide détecté'),
(1, '354398111234567', 'iPad Pro 12.9', 'iOS', 'icloud_bypass', 'USB', 'reussi', DATE_SUB(NOW(), INTERVAL 35 MINUTE), DATE_SUB(NOW(), INTERVAL 32 MINUTE), 'iCloud Bypass iPad terminé'),
(2, '352441111234567', 'iPhone 14', 'iOS', 'unlock_passcode', 'USB', 'en_cours', DATE_SUB(NOW(), INTERVAL 72 MINUTE), NULL, 'Déblocage en cours'),
(1, '354570111234567', 'Xiaomi 13 Pro', 'Android', 'pattern_unlock', 'USB', 'reussi', DATE_SUB(NOW(), INTERVAL 105 MINUTE), DATE_SUB(NOW(), INTERVAL 100 MINUTE), 'Pattern Unlock réussi'),
(2, '353***784', 'Samsung Galaxy S23', 'Android', 'frp_bypass', 'IMEI_distant', 'reussi', DATE_SUB(NOW(), INTERVAL 8 MINUTE), DATE_SUB(NOW(), INTERVAL 5 MINUTE), 'Vérification IMEI terminée');

-- Insérer des logs d'activité correspondants
INSERT INTO logs_activite (session_id, utilisateur_id, action, details, date_action) VALUES
(1, 1, 'Déblocage iPhone 15 Pro Max réussi', 'iCloud Bypass terminé avec succès', DATE_SUB(NOW(), INTERVAL 3 MINUTE)),
(2, 1, 'Galaxy S24 Ultra - FRP Bypass réussi', 'Déblocage Android terminé', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(3, 2, 'IMEI invalide détecté', 'IMEI 111111111111111 rejeté', DATE_SUB(NOW(), INTERVAL 22 MINUTE)),
(4, 1, 'iPad Pro 12.9 - iCloud Bypass terminé', 'Déblocage iPad réussi', DATE_SUB(NOW(), INTERVAL 35 MINUTE)),
(5, 2, 'Connexion USB détectée', 'iPhone 14 connecté via USB', DATE_SUB(NOW(), INTERVAL 72 MINUTE)),
(6, 1, 'Xiaomi 13 Pro - Pattern Unlock réussi', 'Déblocage pattern Android', DATE_SUB(NOW(), INTERVAL 105 MINUTE)),
(7, 2, 'Vérification IMEI 353***784 terminée', 'Informations appareil récupérées', DATE_SUB(NOW(), INTERVAL 8 MINUTE)),
(NULL, 1, 'Mise à jour base TAC', '150 nouveaux codes TAC ajoutés', DATE_SUB(NOW(), INTERVAL 150 MINUTE)),
(NULL, 1, 'Système démarré', 'Application lancée avec succès', DATE_SUB(NOW(), INTERVAL 180 MINUTE)),
(NULL, 2, 'Connexion utilisateur', 'Technicien connecté', DATE_SUB(NOW(), INTERVAL 200 MINUTE));

-- Insérer des statistiques quotidiennes pour les 7 derniers jours
INSERT INTO statistiques (date_stat, total_operations, operations_reussies, operations_echec, plateforme_ios, plateforme_android, methode_usb, methode_imei) VALUES
(CURDATE(), 12, 10, 2, 7, 5, 8, 4),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 23, 20, 3, 14, 9, 15, 8),
(DATE_SUB(CURDATE(), INTERVAL 2 DAY), 18, 16, 2, 11, 7, 12, 6),
(DATE_SUB(CURDATE(), INTERVAL 3 DAY), 31, 28, 3, 18, 13, 22, 9),
(DATE_SUB(CURDATE(), INTERVAL 4 DAY), 27, 25, 2, 16, 11, 19, 8),
(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 15, 13, 2, 9, 6, 10, 5),
(DATE_SUB(CURDATE(), INTERVAL 6 DAY), 8, 7, 1, 5, 3, 6, 2);

-- Insérer des configurations système
INSERT INTO configurations (cle_config, valeur, description) VALUES 
('derniere_maj_tac', NOW(), 'Dernière mise à jour de la base TAC'),
('total_imei_verifies', '1247', 'Nombre total d\'IMEI vérifiés'),
('total_appareils_debloques', '892', 'Nombre total d\'appareils débloqués'),
('total_imei_invalides', '156', 'Nombre total d\'IMEI invalides détectés')
ON DUPLICATE KEY UPDATE valeur = VALUES(valeur);

-- Mettre à jour les statistiques en temps réel
UPDATE configurations SET valeur = (
    SELECT COUNT(*) FROM logs_activite WHERE action LIKE '%IMEI%' OR action LIKE '%vérification%'
) WHERE cle_config = 'total_imei_verifies';

UPDATE configurations SET valeur = (
    SELECT COUNT(*) FROM sessions_deblocage WHERE statut = 'reussi'
) WHERE cle_config = 'total_appareils_debloques';

UPDATE configurations SET valeur = (
    SELECT COUNT(*) FROM logs_activite WHERE action LIKE '%invalide%' OR action LIKE '%erreur%'
) WHERE cle_config = 'total_imei_invalides';
