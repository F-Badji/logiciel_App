package com.logicielapp.util;

import com.logicielapp.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Gestionnaire de session utilisateur (Singleton)
 * Maintient l'état de la session actuelle et les préférences utilisateur
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static SessionManager instance;
    
    // Session actuelle
    private User currentUser;
    private LocalDateTime sessionStartTime;
    private String sessionId;
    
    // Préférences utilisateur
    private final Map<String, Object> userPreferences = new ConcurrentHashMap<>();
    
    // Statistiques de session
    private int operationsCount = 0;
    private int successfulOperations = 0;
    private int failedOperations = 0;
    
    // Constructeur privé pour Singleton
    private SessionManager() {
        this.sessionStartTime = LocalDateTime.now();
        this.sessionId = generateSessionId();
        logger.info("Gestionnaire de session initialisé - ID: {}", sessionId);
    }
    
    /**
     * Obtient l'instance unique du gestionnaire de session
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Définit l'utilisateur actuel et démarre la session
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.sessionStartTime = LocalDateTime.now();
        this.sessionId = generateSessionId();
        
        // Charger les préférences utilisateur
        loadUserPreferences();
        
        logger.info("Session démarrée pour: {} ({})", user.getNom(), user.getRole());
        logger.info("ID de session: {}", sessionId);
    }
    
    /**
     * Obtient l'utilisateur actuel
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Termine la session actuelle
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("Déconnexion de: {} (durée: {})", 
                       currentUser.getNom(), getSessionDuration());
            
            // Sauvegarder les préférences
            saveUserPreferences();
            
            // Nettoyer la session
            currentUser = null;
            sessionStartTime = null;
            userPreferences.clear();
            resetStatistics();
        }
    }
    
    /**
     * Obtient la durée de la session actuelle
     */
    public String getSessionDuration() {
        if (sessionStartTime == null) {
            return "0 minutes";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(sessionStartTime, now).toMinutes();
        
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h " + remainingMinutes + "m";
        }
    }
    
    /**
     * Obtient l'heure de début de session formatée
     */
    public String getFormattedSessionStart() {
        if (sessionStartTime == null) {
            return "Non définie";
        }
        return sessionStartTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    // =================== PRÉFÉRENCES UTILISATEUR ===================
    
    /**
     * Définit une préférence utilisateur
     */
    public void setPreference(String key, Object value) {
        userPreferences.put(key, value);
        logger.debug("Préférence mise à jour: {} = {}", key, value);
    }
    
    /**
     * Obtient une préférence utilisateur
     */
    @SuppressWarnings("unchecked")
    public <T> T getPreference(String key, T defaultValue) {
        Object value = userPreferences.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                logger.warn("Erreur de cast pour la préférence {}, utilisation de la valeur par défaut", key);
            }
        }
        return defaultValue;
    }
    
    /**
     * Supprime une préférence
     */
    public void removePreference(String key) {
        userPreferences.remove(key);
    }
    
    /**
     * Récupère l'email mémorisé (option "Se souvenir de moi")
     */
    public String getStoredEmail() {
        return getPreference("storedEmail", (String) null);
    }
    
    /**
     * Mémorise l'email saisi (option "Se souvenir de moi")
     */
    public void storeEmail(String email) {
        if (email == null || email.isEmpty()) {
            removePreference("storedEmail");
        } else {
            setPreference("storedEmail", email);
        }
    }
    
    /**
     * Efface les identifiants mémorisés
     */
    public void clearStoredCredentials() {
        removePreference("storedEmail");
    }
    
    /**
     * Charge les préférences utilisateur depuis la base de données
     */
    private void loadUserPreferences() {
        if (currentUser == null) return;
        
        try {
            // TODO: Charger depuis la base de données
            // Pour l'instant, valeurs par défaut
            setPreference("theme", "default");
            setPreference("language", "fr");
            setPreference("autoSaveInterval", 300); // 5 minutes
            setPreference("showNotifications", true);
            setPreference("debugMode", false);
            
            logger.debug("Préférences utilisateur chargées pour: {}", currentUser.getNom());
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement des préférences utilisateur", e);
        }
    }
    
    /**
     * Sauvegarde les préférences utilisateur
     */
    private void saveUserPreferences() {
        if (currentUser == null || userPreferences.isEmpty()) return;
        
        try {
            // TODO: Sauvegarder en base de données
            logger.debug("Préférences utilisateur sauvegardées pour: {}", currentUser.getNom());
            
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde des préférences", e);
        }
    }
    
    // =================== STATISTIQUES DE SESSION ===================
    
    /**
     * Incrémente le compteur d'opérations
     */
    public void incrementOperationsCount() {
        operationsCount++;
    }
    
    /**
     * Incrémente le compteur d'opérations réussies
     */
    public void incrementSuccessfulOperations() {
        successfulOperations++;
        incrementOperationsCount();
    }
    
    /**
     * Incrémente le compteur d'opérations échouées
     */
    public void incrementFailedOperations() {
        failedOperations++;
        incrementOperationsCount();
    }
    
    /**
     * Remet à zéro les statistiques
     */
    public void resetStatistics() {
        operationsCount = 0;
        successfulOperations = 0;
        failedOperations = 0;
    }
    
    /**
     * Obtient le taux de réussite en pourcentage
     */
    public double getSuccessRate() {
        if (operationsCount == 0) return 0.0;
        return (double) successfulOperations / operationsCount * 100.0;
    }
    
    // =================== SÉCURITÉ ET PERMISSIONS ===================
    
    /**
     * Vérifie si l'utilisateur actuel peut effectuer une opération
     */
    public boolean canPerformOperation(String operationType) {
        if (currentUser == null) {
            return false;
        }
        return currentUser.canPerformOperation(operationType);
    }
    
    /**
     * Vérifie si l'utilisateur actuel est administrateur
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
    
    /**
     * Vérifie si l'utilisateur actuel est technicien ou administrateur
     */
    public boolean isCurrentUserTechnician() {
        return currentUser != null && 
               (currentUser.getRole() == User.Role.TECHNICIEN || 
                currentUser.getRole() == User.Role.ADMIN);
    }
    
    // =================== UTILITAIRES ===================
    
    /**
     * Génère un ID de session unique
     */
    private String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_" + 
               Integer.toHexString((int)(Math.random() * 65536));
    }
    
    /**
     * Obtient un résumé de la session actuelle
     */
    public String getSessionSummary() {
        if (currentUser == null) {
            return "Aucune session active";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Utilisateur: ").append(currentUser.getDisplayName()).append("\n");
        summary.append("Début: ").append(getFormattedSessionStart()).append("\n");
        summary.append("Durée: ").append(getSessionDuration()).append("\n");
        summary.append("Opérations: ").append(operationsCount).append("\n");
        summary.append("Réussites: ").append(successfulOperations).append("\n");
        summary.append("Échecs: ").append(failedOperations).append("\n");
        summary.append("Taux de réussite: ").append(String.format("%.1f%%", getSuccessRate()));
        
        return summary.toString();
    }
    
    /**
     * Vérifie si la session est expirée (optionnel)
     */
    public boolean isSessionExpired() {
        if (sessionStartTime == null) return true;
        
        // Session expire après 8 heures d'inactivité
        long maxSessionDuration = getPreference("maxSessionDuration", 480L); // 8 heures en minutes
        LocalDateTime expiration = sessionStartTime.plusMinutes(maxSessionDuration);
        
        return LocalDateTime.now().isAfter(expiration);
    }
    
    /**
     * Nettoyage global des ressources de session (appelé à la fermeture)
     */
    public void cleanup() {
        try {
            saveUserPreferences();
        } catch (Exception e) {
            logger.warn("Erreur lors du nettoyage de la session", e);
        } finally {
            logout();
        }
    }
    
    // Getters pour les statistiques
    
    public int getOperationsCount() {
        return operationsCount;
    }
    
    public int getSuccessfulOperations() {
        return successfulOperations;
    }
    
    public int getFailedOperations() {
        return failedOperations;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }
}
