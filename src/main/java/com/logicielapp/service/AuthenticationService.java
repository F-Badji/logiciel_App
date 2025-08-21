package com.logicielapp.service;

import com.logicielapp.model.User;
import com.logicielapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'authentification sécurisé
 * Gère l'authentification, les sessions et les permissions utilisateur
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    /**
     * Authentifie un utilisateur avec username/email et mot de passe
     */
    public User authenticate(String emailOrUsername, String password) throws Exception {
        logger.info("Tentative d'authentification pour: {}", emailOrUsername);
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Rechercher l'utilisateur par email ou nom d'utilisateur
            User user = findUserByEmailOrUsername(conn, emailOrUsername);
            
            // Fallback: accepter les identifiants par défaut demandés par le client
            // Admin / Serignetouba2020
            final String DEFAULT_ADMIN_NAME = "Admin";
            final String DEFAULT_ADMIN_EMAIL = "admin@logiciel-app.com";
            final String DEFAULT_ADMIN_PASSWORD = "Serignetouba2020";
            final boolean isDefaultAdminAttempt =
                (DEFAULT_ADMIN_NAME.equalsIgnoreCase(emailOrUsername)
                 || DEFAULT_ADMIN_EMAIL.equalsIgnoreCase(emailOrUsername))
                && DEFAULT_ADMIN_PASSWORD.equals(password);
            
            if (user == null && isDefaultAdminAttempt) {
                logger.warn("Aucun utilisateur trouvé pour {}, création de l'admin par défaut sur demande.", emailOrUsername);
                // Créer l'admin avec ces identifiants
                return createUser(DEFAULT_ADMIN_NAME, DEFAULT_ADMIN_EMAIL, DEFAULT_ADMIN_PASSWORD, User.Role.ADMIN);
            }
            
            if (user == null) {
                logger.warn("Utilisateur non trouvé: {}", emailOrUsername);
                throw new Exception("Utilisateur introuvable");
            }
            
            // Vérifier si le compte est actif
            if (!user.isActif()) {
                logger.warn("Compte désactivé pour: {}", emailOrUsername);
                throw new Exception("Compte désactivé");
            }
            
            // Vérifier si le compte est bloqué
            if (user.isAccountLocked()) {
                logger.warn("Compte bloqué temporairement pour: {}", emailOrUsername);
                throw new Exception("Compte bloqué temporairement (trop de tentatives)");
            }
            
            // Vérifier le mot de passe
            String hashedPassword = hashPassword(password);
            if (!hashedPassword.equals(user.getMotDePasse())) {
                // Si l'utilisateur tente les identifiants par défaut, mettre à jour le mot de passe ET CONTINUER
                if (isDefaultAdminAttempt && DEFAULT_ADMIN_NAME.equalsIgnoreCase(user.getNom())) {
                    try {
                        updateUserPassword(conn, user.getId(), hashedPassword);
                        user.setMotDePasse(hashedPassword);
                        logger.info("Mot de passe de l'admin par défaut mis à jour, authentification poursuivie.");
                    } catch (SQLException se) {
                        logger.error("Échec de mise à jour du mot de passe admin par défaut", se);
                        // si la mise à jour échoue, considérer comme échec normal
                        incrementFailedAttempts(conn, user.getId());
                        logger.warn("Mot de passe incorrect pour: {}", emailOrUsername);
                        throw new Exception("Mot de passe incorrect");
                    }
                } else {
                    // Cas standard: mot de passe incorrect
                    incrementFailedAttempts(conn, user.getId());
                    logger.warn("Mot de passe incorrect pour: {}", emailOrUsername);
                    throw new Exception("Mot de passe incorrect");
                }
            }
            
            // Authentification réussie
            user.updateLastLogin();
            user.generateSessionToken();
            
            // Mettre à jour en base de données
            updateLastLogin(conn, user.getId());
            
            // Enregistrer l'activité
            logUserActivity(conn, user.getId(), "LOGIN_SUCCESS", "Connexion réussie");
            
            logger.info("Authentification réussie pour: {} ({})", user.getNom(), user.getRole());
            return user;
            
        } catch (SQLException e) {
            logger.error("Erreur de base de données lors de l'authentification", e);
            throw new Exception("Erreur de connexion à la base de données");
        }
    }
    
    /**
     * Recherche un utilisateur par email ou nom d'utilisateur
     */
    private User findUserByEmailOrUsername(Connection conn, String emailOrUsername) throws SQLException {
        String sql = """
            SELECT id, nom, email, mot_de_passe, role, actif, 
                   date_creation, derniere_connexion
            FROM utilisateurs 
            WHERE email = ? OR nom = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emailOrUsername);
            stmt.setString(2, emailOrUsername);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return buildUserFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Construit un objet User à partir d'un ResultSet
     */
    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setNom(rs.getString("nom"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setActif(rs.getBoolean("actif"));
        
        // Convertir le rôle
        String roleStr = rs.getString("role");
        user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
        
        // Dates
        Timestamp creation = rs.getTimestamp("date_creation");
        if (creation != null) {
            user.setDateCreation(creation.toLocalDateTime());
        }
        
        Timestamp derniere = rs.getTimestamp("derniere_connexion");
        if (derniere != null) {
            user.setDerniereConnexion(derniere.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Recherche un utilisateur par email
     */
    private User findUserByEmail(Connection conn, String email) throws SQLException {
        String sql = """
            SELECT id, nom, email, mot_de_passe, role, actif, 
                   date_creation, derniere_connexion
            FROM utilisateurs 
            WHERE email = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setEmail(rs.getString("email"));
                    user.setMotDePasse(rs.getString("mot_de_passe"));
                    user.setActif(rs.getBoolean("actif"));
                    
                    // Convertir le rôle
                    String roleStr = rs.getString("role");
                    user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
                    
                    // Dates
                    Timestamp creation = rs.getTimestamp("date_creation");
                    if (creation != null) {
                        user.setDateCreation(creation.toLocalDateTime());
                    }
                    
                    Timestamp derniere = rs.getTimestamp("derniere_connexion");
                    if (derniere != null) {
                        user.setDerniereConnexion(derniere.toLocalDateTime());
                    }
                    
                    return user;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Incrémente le compteur de tentatives échouées
     */
    private void incrementFailedAttempts(Connection conn, int userId) {
        String sql = """
            INSERT INTO logs_activite (utilisateur_id, action, details, date_action)
            VALUES (?, 'LOGIN_FAILED', 'Tentative de connexion échouée', CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'enregistrement de la tentative échouée", e);
        }
    }
    
    /**
     * Met à jour la dernière connexion
     */
    private void updateLastLogin(Connection conn, int userId) throws SQLException {
        String sql = """
            UPDATE utilisateurs 
            SET derniere_connexion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Met à jour le mot de passe d'un utilisateur
     */
    private void updateUserPassword(Connection conn, int userId, String newHashedPassword) throws SQLException {
        String sql = """
            UPDATE utilisateurs 
            SET mot_de_passe = ?
            WHERE id = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Enregistre une activité utilisateur
     */
    private void logUserActivity(Connection conn, int userId, String action, String details) {
        String sql = """
            INSERT INTO logs_activite (utilisateur_id, action, details, date_action)
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'enregistrement de l'activité", e);
        }
    }
    
    /**
     * Hache un mot de passe avec SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Erreur lors du hachage du mot de passe", e);
            throw new RuntimeException("Erreur de sécurité", e);
        }
    }
    
    /**
     * Crée un nouvel utilisateur
     */
    public User createUser(String nom, String email, String password, User.Role role) throws Exception {
        logger.info("Création d'un nouvel utilisateur: {} ({})", nom, role);
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Vérifier si l'email existe déjà
            if (findUserByEmail(conn, email) != null) {
                throw new Exception("Un utilisateur avec cet email existe déjà");
            }
            
            // Hacher le mot de passe
            String hashedPassword = hashPassword(password);
            
            // Insérer l'utilisateur
            String sql = """
                INSERT INTO utilisateurs (nom, email, mot_de_passe, role, actif, date_creation)
                VALUES (?, ?, ?, ?, TRUE, CURRENT_TIMESTAMP)
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nom);
                stmt.setString(2, email);
                stmt.setString(3, hashedPassword);
                stmt.setString(4, role.name().toLowerCase());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new Exception("Échec de la création de l'utilisateur");
                }
                
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        User user = new User(nom, email, role);
                        user.setId(generatedKeys.getInt(1));
                        user.setMotDePasse(hashedPassword);
                        
                        logger.info("Utilisateur créé avec succès: {} (ID: {})", nom, user.getId());
                        return user;
                    } else {
                        throw new Exception("Impossible de récupérer l'ID de l'utilisateur créé");
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'utilisateur", e);
            throw new Exception("Erreur de base de données: " + e.getMessage());
        }
    }
    
    /**
     * Liste tous les utilisateurs
     */
    public List<User> getAllUsers() throws Exception {
        logger.info("Récupération de la liste des utilisateurs");
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = """
                SELECT id, nom, email, role, actif, date_creation, derniere_connexion
                FROM utilisateurs
                ORDER BY nom
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setNom(rs.getString("nom"));
                    user.setEmail(rs.getString("email"));
                    user.setActif(rs.getBoolean("actif"));
                    
                    // Rôle
                    String roleStr = rs.getString("role");
                    user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
                    
                    // Dates
                    Timestamp creation = rs.getTimestamp("date_creation");
                    if (creation != null) {
                        user.setDateCreation(creation.toLocalDateTime());
                    }
                    
                    Timestamp derniere = rs.getTimestamp("derniere_connexion");
                    if (derniere != null) {
                        user.setDerniereConnexion(derniere.toLocalDateTime());
                    }
                    
                    users.add(user);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des utilisateurs", e);
            throw new Exception("Erreur de base de données");
        }
        
        logger.info("{} utilisateurs récupérés", users.size());
        return users;
    }
    
    /**
     * Met à jour le statut actif d'un utilisateur
     */
    public void updateUserStatus(int userId, boolean active) throws Exception {
        logger.info("Mise à jour du statut utilisateur ID {}: {}", userId, active ? "actif" : "inactif");
        
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String sql = "UPDATE utilisateurs SET actif = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBoolean(1, active);
                stmt.setInt(2, userId);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new Exception("Utilisateur non trouvé");
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du statut utilisateur", e);
            throw new Exception("Erreur de base de données");
        }
    }
    
    /**
     * Vérifie si la base de données contient des utilisateurs
     * et crée l'administrateur par défaut si nécessaire
     */
    public void ensureDefaultAdminExists() {
        try {
            List<User> users = getAllUsers();
            
            if (users.isEmpty()) {
                logger.info("Aucun utilisateur trouvé, création de l'administrateur par défaut");
                createUser("Administrateur", "admin@logiciel-app.com", "admin123", User.Role.ADMIN);
                createUser("Technicien Test", "tech@logiciel-app.com", "tech123", User.Role.TECHNICIEN);
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification/création des utilisateurs par défaut", e);
        }
    }
}
