package com.logicielapp.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gestionnaire d'erreurs centralisé et professionnel
 * Gère les exceptions, logs, alertes utilisateur et récupération d'erreurs
 */
public class ErrorHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);
    
    // Types d'erreurs
    public enum ErrorType {
        VALIDATION("Erreur de Validation", Alert.AlertType.WARNING),
        DATABASE("Erreur de Base de Données", Alert.AlertType.ERROR),
        NETWORK("Erreur Réseau", Alert.AlertType.ERROR),
        AUTHENTICATION("Erreur d'Authentification", Alert.AlertType.WARNING),
        UNLOCK_OPERATION("Erreur d'Opération", Alert.AlertType.ERROR),
        SYSTEM("Erreur Système", Alert.AlertType.ERROR),
        USER_INPUT("Erreur de Saisie", Alert.AlertType.WARNING),
        CONFIGURATION("Erreur de Configuration", Alert.AlertType.ERROR),
        CRITICAL("Erreur Critique", Alert.AlertType.ERROR);
        
        private final String displayName;
        private final Alert.AlertType alertType;
        
        ErrorType(String displayName, Alert.AlertType alertType) {
            this.displayName = displayName;
            this.alertType = alertType;
        }
        
        public String getDisplayName() { return displayName; }
        public Alert.AlertType getAlertType() { return alertType; }
    }
    
    // Compteur d'erreurs par type
    private static final ConcurrentHashMap<ErrorType, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private static final int MAX_ERRORS_PER_TYPE = 10;
    private static final long ERROR_RESET_INTERVAL_MS = 300000; // 5 minutes
    
    static {
        // Initialiser les compteurs
        for (ErrorType type : ErrorType.values()) {
            errorCounts.put(type, new AtomicInteger(0));
        }
    }
    
    /**
     * Gère une exception de manière complète avec log, alerte et récupération
     * @param errorType Type d'erreur
     * @param message Message d'erreur utilisateur
     * @param exception Exception technique (peut être null)
     * @param context Contexte d'exécution
     * @param showToUser Si true, affiche une alerte à l'utilisateur
     * @return true si l'erreur a été gérée avec succès
     */
    public static boolean handleError(ErrorType errorType, String message, Throwable exception, 
                                    String context, boolean showToUser) {
        
        // Vérifier si nous avons trop d'erreurs du même type
        AtomicInteger count = errorCounts.get(errorType);
        if (count.incrementAndGet() > MAX_ERRORS_PER_TYPE) {
            logger.error("Trop d'erreurs du type {} détectées. Arrêt des notifications.", errorType);
            return false;
        }
        
        // Créer l'entrée de log complète
        String logMessage = buildLogMessage(errorType, message, context, exception);
        
        // Logger selon le niveau de sévérité
        switch (errorType) {
            case CRITICAL:
            case SYSTEM:
            case DATABASE:
                logger.error(logMessage, exception);
                break;
            case NETWORK:
            case UNLOCK_OPERATION:
            case CONFIGURATION:
                logger.warn(logMessage, exception);
                break;
            case VALIDATION:
            case USER_INPUT:
            case AUTHENTICATION:
                logger.info(logMessage);
                break;
        }
        
        // Afficher à l'utilisateur si demandé
        if (showToUser) {
            Platform.runLater(() -> showUserAlert(errorType, message, exception));
        }
        
        // Tentative de récupération automatique
        attemptRecovery(errorType, exception, context);
        
        return true;
    }
    
    /**
     * Méthode simplifiée pour les erreurs courantes
     */
    public static void handleError(ErrorType errorType, String message, Throwable exception) {
        handleError(errorType, message, exception, "Unknown", true);
    }
    
    /**
     * Méthode pour les erreurs sans exception
     */
    public static void handleError(ErrorType errorType, String message) {
        handleError(errorType, message, null, "Unknown", true);
    }
    
    /**
     * Construit un message de log détaillé
     */
    private static String buildLogMessage(ErrorType errorType, String message, String context, Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorType.getDisplayName()).append("] ");
        sb.append("Context: ").append(context).append(" | ");
        sb.append("Message: ").append(message);
        
        if (exception != null) {
            sb.append(" | Exception: ").append(exception.getClass().getSimpleName());
            sb.append(" - ").append(exception.getMessage());
        }
        
        sb.append(" | Timestamp: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return sb.toString();
    }
    
    /**
     * Affiche une alerte utilisateur appropriée
     */
    private static void showUserAlert(ErrorType errorType, String message, Throwable exception) {
        try {
            Alert alert = new Alert(errorType.getAlertType());
            alert.setTitle(errorType.getDisplayName());
            
            // Message principal
            if (errorType == ErrorType.CRITICAL || errorType == ErrorType.SYSTEM) {
                alert.setHeaderText("Une erreur critique est survenue");
                alert.setContentText(message + "\n\nVeuillez redémarrer l'application.");
            } else {
                alert.setHeaderText(null);
                alert.setContentText(message);
            }
            
            // Ajouter les détails techniques si disponibles
            if (exception != null && (errorType == ErrorType.CRITICAL || errorType == ErrorType.SYSTEM)) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                
                TextArea textArea = new TextArea(sw.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                alert.getDialogPane().setExpandableContent(textArea);
            }
            
            // Personnaliser les boutons selon le type d'erreur
            if (errorType == ErrorType.CRITICAL) {
                alert.getButtonTypes().setAll(ButtonType.OK);
            }
            
            alert.showAndWait();
            
        } catch (Exception e) {
            // En cas d'erreur lors de l'affichage de l'alerte
            logger.error("Impossible d'afficher l'alerte d'erreur", e);
            System.err.println("ERREUR CRITIQUE: " + message);
        }
    }
    
    /**
     * Tentative de récupération automatique selon le type d'erreur
     */
    private static void attemptRecovery(ErrorType errorType, Throwable exception, String context) {
        switch (errorType) {
            case DATABASE:
                attemptDatabaseRecovery();
                break;
            case NETWORK:
                attemptNetworkRecovery();
                break;
            case UNLOCK_OPERATION:
                attemptOperationRecovery(context);
                break;
            default:
                // Pas de récupération spécifique
                break;
        }
    }
    
    /**
     * Tentative de récupération de base de données
     */
    private static void attemptDatabaseRecovery() {
        try {
            logger.info("Tentative de récupération de la connexion base de données...");
            
            // Vérifier si la base de données est disponible
            if (DatabaseManager.getInstance().isDatabaseAvailable()) {
                logger.info("Base de données récupérée avec succès");
            } else {
                logger.warn("Impossible de récupérer la connexion base de données");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération base de données", e);
        }
    }
    
    /**
     * Tentative de récupération réseau
     */
    private static void attemptNetworkRecovery() {
        logger.info("Tentative de récupération réseau - Vérification de la connectivité...");
        // Dans un vrai système, on pourrait ping des serveurs, vérifier la connectivité, etc.
    }
    
    /**
     * Tentative de récupération d'opération de déblocage
     */
    private static void attemptOperationRecovery(String context) {
        logger.info("Tentative de récupération d'opération: {}", context);
        // Nettoyer les ressources, réinitialiser les états, etc.
    }
    
    /**
     * Valide et sanitise les entrées utilisateur
     * @param input Entrée utilisateur
     * @param fieldName Nom du champ
     * @param maxLength Longueur maximale
     * @param allowedPattern Pattern autorisé (peut être null)
     * @return Entrée nettoyée ou null si invalide
     */
    public static String validateAndSanitizeInput(String input, String fieldName, int maxLength, String allowedPattern) {
        if (input == null) {
            handleError(ErrorType.USER_INPUT, "Le champ " + fieldName + " ne peut pas être vide");
            return null;
        }
        
        String trimmed = input.trim();
        
        if (trimmed.isEmpty()) {
            handleError(ErrorType.USER_INPUT, "Le champ " + fieldName + " ne peut pas être vide");
            return null;
        }
        
        if (trimmed.length() > maxLength) {
            handleError(ErrorType.USER_INPUT, 
                "Le champ " + fieldName + " ne peut pas dépasser " + maxLength + " caractères");
            return null;
        }
        
        // Supprimer les caractères potentiellement dangereux
        String sanitized = trimmed
            .replaceAll("[<>\"'&]", "") // XSS prevention
            .replaceAll("[;\\|`$]", ""); // Command injection prevention
        
        if (allowedPattern != null && !sanitized.matches(allowedPattern)) {
            handleError(ErrorType.USER_INPUT, 
                "Le format du champ " + fieldName + " n'est pas valide");
            return null;
        }
        
        return sanitized;
    }
    
    /**
     * Réinitialise les compteurs d'erreurs (appelé périodiquement)
     */
    public static void resetErrorCounts() {
        errorCounts.values().forEach(count -> count.set(0));
        logger.debug("Compteurs d'erreurs réinitialisés");
    }
    
    /**
     * Retourne les statistiques d'erreurs
     */
    public static String getErrorStatistics() {
        StringBuilder stats = new StringBuilder("Statistiques d'erreurs:\\n");
        errorCounts.forEach((type, count) -> {
            stats.append(type.getDisplayName()).append(": ").append(count.get()).append("\\n");
        });
        return stats.toString();
    }
    
    /**
     * Masque les données sensibles pour les logs de sécurité
     * @param sensitiveData Données sensibles à masquer
     * @return Données masquées
     */
    public static String maskSensitiveData(String sensitiveData) {
        if (sensitiveData == null || sensitiveData.isEmpty()) {
            return "[EMPTY]";
        }
        
        // Pour IMEI (15 chiffres)
        if (sensitiveData.length() == 15 && sensitiveData.matches("\\d{15}")) {
            return sensitiveData.substring(0, 6) + "XXXXX" + sensitiveData.substring(11);
        }
        
        // Pour emails
        if (sensitiveData.contains("@")) {
            String[] parts = sensitiveData.split("@");
            if (parts.length == 2 && parts[0].length() > 2) {
                return parts[0].charAt(0) + "***" + parts[0].charAt(parts[0].length()-1) + "@" + parts[1];
            }
        }
        
        // Masquage générique
        if (sensitiveData.length() > 6) {
            return sensitiveData.substring(0, 2) + "***" + sensitiveData.substring(sensitiveData.length()-2);
        } else if (sensitiveData.length() > 2) {
            return sensitiveData.charAt(0) + "***" + sensitiveData.charAt(sensitiveData.length()-1);
        } else {
            return "***";
        }
    }
    
    /**
     * Gestionnaire global d'exceptions non capturées
     */
    public static void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            logger.error("Exception non capturée dans le thread: {}", thread.getName(), exception);
            handleError(ErrorType.CRITICAL, 
                "Une erreur critique non gérée est survenue", 
                exception, 
                "Thread: " + thread.getName(), 
                true);
        });
        
        // Pour JavaFX
        Platform.runLater(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((thread, exception) -> {
                logger.error("Exception non capturée dans JavaFX thread", exception);
                handleError(ErrorType.CRITICAL, 
                    "Erreur critique dans l'interface utilisateur", 
                    exception, 
                    "JavaFX Thread", 
                    true);
            });
        });
    }
    
    /**
     * Démarre le service de réinitialisation périodique des compteurs
     */
    public static void startErrorCountResetService() {
        Thread resetThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(ERROR_RESET_INTERVAL_MS);
                    resetErrorCounts();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        resetThread.setDaemon(true);
        resetThread.setName("ErrorHandler-Reset-Service");
        resetThread.start();
    }
}
