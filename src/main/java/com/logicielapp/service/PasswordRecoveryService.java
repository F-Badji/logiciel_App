package com.logicielapp.service;

import com.logicielapp.util.ErrorHandler;
import com.logicielapp.util.IMEIValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Service de récupération de mots de passe oubliés pour Apple et Android
 * Inclut la validation d'identité et la génération de nouveaux mots de passe
 */
public class PasswordRecoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryService.class);
    
    // Tentatives de récupération par IMEI (protection contre le brute force)
    private static final Map<String, Integer> recoveryAttempts = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> lastAttemptTime = new ConcurrentHashMap<>();
    
    // Codes de récupération temporaires (valables 10 minutes)
    private static final Map<String, RecoverySession> activeRecoverySessions = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int MAX_RECOVERY_ATTEMPTS = 3;
    private static final int RECOVERY_CODE_LENGTH = 8;
    private static final int SESSION_TIMEOUT_MINUTES = 10;
    
    // Patterns de validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /**
     * Types de mots de passe supportés
     */
    public enum PasswordType {
        APPLE_ID("Apple ID", "Mot de passe Apple ID"),
        ICLOUD("iCloud", "Mot de passe iCloud"), 
        SCREEN_LOCK_IOS("Verrouillage iOS", "Code de verrouillage d'écran iOS"),
        RESTRICTION_IOS("Restrictions iOS", "Code de restrictions parentales iOS"),
        GOOGLE_ACCOUNT("Compte Google", "Mot de passe Google"),
        SCREEN_LOCK_ANDROID("Verrouillage Android", "Code de verrouillage Android"),
        PATTERN_ANDROID("Motif Android", "Motif de déverrouillage Android"),
        PIN_ANDROID("PIN Android", "Code PIN Android");
        
        private final String displayName;
        private final String description;
        
        PasswordType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Questions de sécurité prédéfinies
     */
    public enum SecurityQuestion {
        FIRST_PET("Quel était le nom de votre premier animal de compagnie ?"),
        BIRTH_CITY("Dans quelle ville êtes-vous né(e) ?"),
        MOTHER_MAIDEN("Quel est le nom de jeune fille de votre mère ?"),
        FIRST_SCHOOL("Quelle était votre première école ?"),
        FAVORITE_COLOR("Quelle est votre couleur préférée ?"),
        FIRST_CAR("Quelle était la marque de votre première voiture ?"),
        CHILDHOOD_FRIEND("Quel était le prénom de votre meilleur ami d'enfance ?"),
        FAVORITE_BOOK("Quel est le titre de votre livre préféré ?");
        
        private final String question;
        
        SecurityQuestion(String question) {
            this.question = question;
        }
        
        public String getQuestion() { return question; }
    }
    
    /**
     * Session de récupération de mot de passe
     */
    public static class RecoverySession {
        private final String sessionId;
        private final String imei;
        private final String email;
        private final PasswordType passwordType;
        private final LocalDateTime creationTime;
        private final Map<SecurityQuestion, String> securityAnswers;
        private boolean emailVerified;
        private boolean securityQuestionsAnswered;
        private String recoveryCode;
        
        public RecoverySession(String sessionId, String imei, String email, PasswordType passwordType) {
            this.sessionId = sessionId;
            this.imei = imei;
            this.email = email;
            this.passwordType = passwordType;
            this.creationTime = LocalDateTime.now();
            this.securityAnswers = new HashMap<>();
            this.emailVerified = false;
            this.securityQuestionsAnswered = false;
        }
        
        // Getters et setters
        public String getSessionId() { return sessionId; }
        public String getImei() { return imei; }
        public String getEmail() { return email; }
        public PasswordType getPasswordType() { return passwordType; }
        public LocalDateTime getCreationTime() { return creationTime; }
        public boolean isEmailVerified() { return emailVerified; }
        public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
        public boolean isSecurityQuestionsAnswered() { return securityQuestionsAnswered; }
        public void setSecurityQuestionsAnswered(boolean answered) { this.securityQuestionsAnswered = answered; }
        public String getRecoveryCode() { return recoveryCode; }
        public void setRecoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; }
        public Map<SecurityQuestion, String> getSecurityAnswers() { return securityAnswers; }
        
        public boolean isExpired() {
            return creationTime.plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(LocalDateTime.now());
        }
    }
    
    /**
     * Résultat d'une tentative de récupération
     */
    public static class RecoveryResult {
        private final boolean success;
        private final String message;
        private final String sessionId;
        private final RecoveryStep nextStep;
        private final Object data;
        
        public RecoveryResult(boolean success, String message, String sessionId, RecoveryStep nextStep, Object data) {
            this.success = success;
            this.message = message;
            this.sessionId = sessionId;
            this.nextStep = nextStep;
            this.data = data;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
        public RecoveryStep getNextStep() { return nextStep; }
        public Object getData() { return data; }
    }
    
    /**
     * Étapes du processus de récupération
     */
    public enum RecoveryStep {
        EMAIL_VERIFICATION("Vérification email"),
        SECURITY_QUESTIONS("Questions de sécurité"),
        IMEI_VALIDATION("Validation IMEI"),
        PASSWORD_GENERATION("Génération nouveau mot de passe"),
        COMPLETED("Récupération terminée");
        
        private final String description;
        
        RecoveryStep(String description) {
            this.description = description;
        }
        
        public String getDescription() { return description; }
    }
    
    /**
     * Démarre le processus de récupération de mot de passe
     */
    public static RecoveryResult startPasswordRecovery(String imei, String email, PasswordType passwordType) {
        // Validation des paramètres
        if (!isValidInput(imei, email)) {
            return new RecoveryResult(false, "IMEI ou email invalide", null, null, null);
        }
        
        // Vérifier les tentatives de récupération
        if (!checkRecoveryAttempts(imei)) {
            return new RecoveryResult(false, 
                "Trop de tentatives de récupération. Réessayez dans 24 heures.", 
                null, null, null);
        }
        
        // Valider l'IMEI
        if (!IMEIValidator.isValidIMEI(imei)) {
            incrementRecoveryAttempts(imei);
            return new RecoveryResult(false, "IMEI invalide", null, null, null);
        }
        
        // Créer une nouvelle session de récupération
        String sessionId = generateSessionId();
        RecoverySession session = new RecoverySession(sessionId, imei, email, passwordType);
        activeRecoverySessions.put(sessionId, session);
        
        logger.info("Nouvelle session de récupération créée: {} pour IMEI: {}", 
                   sessionId, ErrorHandler.maskSensitiveData(imei));
        
        // Simuler l'envoi d'un email de vérification
        String verificationCode = generateRecoveryCode();
        session.setRecoveryCode(verificationCode);
        
        return new RecoveryResult(true, 
            "Un code de vérification a été envoyé à " + maskEmail(email), 
            sessionId, 
            RecoveryStep.EMAIL_VERIFICATION, 
            Collections.singletonMap("verificationCode", verificationCode));
    }
    
    /**
     * Vérifie le code email de récupération
     */
    public static RecoveryResult verifyEmailCode(String sessionId, String verificationCode) {
        RecoverySession session = activeRecoverySessions.get(sessionId);
        
        if (session == null || session.isExpired()) {
            return new RecoveryResult(false, "Session expirée ou invalide", null, null, null);
        }
        
        if (!session.getRecoveryCode().equals(verificationCode)) {
            return new RecoveryResult(false, "Code de vérification incorrect", sessionId, 
                                    RecoveryStep.EMAIL_VERIFICATION, null);
        }
        
        session.setEmailVerified(true);
        logger.info("Email vérifié pour session: {}", sessionId);
        
        // Préparer les questions de sécurité
        List<SecurityQuestion> randomQuestions = getRandomSecurityQuestions(3);
        
        return new RecoveryResult(true, 
            "Email vérifié avec succès. Répondez aux questions de sécurité.", 
            sessionId, 
            RecoveryStep.SECURITY_QUESTIONS, 
            randomQuestions);
    }
    
    /**
     * Traite les réponses aux questions de sécurité
     */
    public static RecoveryResult processSecurityQuestions(String sessionId, 
                                                        Map<SecurityQuestion, String> answers) {
        RecoverySession session = activeRecoverySessions.get(sessionId);
        
        if (session == null || session.isExpired()) {
            return new RecoveryResult(false, "Session expirée ou invalide", null, null, null);
        }
        
        if (!session.isEmailVerified()) {
            return new RecoveryResult(false, "Email non vérifié", sessionId, 
                                    RecoveryStep.EMAIL_VERIFICATION, null);
        }
        
        // Stocker les réponses (dans un vrai système, elles seraient vérifiées contre une base)
        session.getSecurityAnswers().putAll(answers);
        session.setSecurityQuestionsAnswered(true);
        
        logger.info("Questions de sécurité traitées pour session: {}", sessionId);
        
        return new RecoveryResult(true, 
            "Questions de sécurité validées. Génération du nouveau mot de passe...", 
            sessionId, 
            RecoveryStep.PASSWORD_GENERATION, 
            null);
    }
    
    /**
     * Génère un nouveau mot de passe sécurisé
     */
    public static RecoveryResult generateNewPassword(String sessionId) {
        RecoverySession session = activeRecoverySessions.get(sessionId);
        
        if (session == null || session.isExpired()) {
            return new RecoveryResult(false, "Session expirée ou invalide", null, null, null);
        }
        
        if (!session.isEmailVerified() || !session.isSecurityQuestionsAnswered()) {
            return new RecoveryResult(false, "Étapes de vérification incomplètes", sessionId, 
                                    RecoveryStep.SECURITY_QUESTIONS, null);
        }
        
        // Générer le nouveau mot de passe selon le type
        String newPassword = generatePasswordByType(session.getPasswordType());
        
        // Obtenir les informations du téléphone
        PhoneInfoDatabase.CompletePhoneInfo phoneInfo = 
            PhoneInfoDatabase.getCompletePhoneInfo(session.getImei());
        
        // Créer le résultat de récupération
        Map<String, Object> result = new HashMap<>();
        result.put("newPassword", newPassword);
        result.put("passwordType", session.getPasswordType());
        result.put("imei", session.getImei());
        result.put("phoneInfo", phoneInfo);
        result.put("instructions", getRecoveryInstructions(session.getPasswordType()));
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        // Marquer la session comme terminée
        activeRecoverySessions.remove(sessionId);
        
        logger.info("Nouveau mot de passe généré pour session: {} - Type: {}", 
                   sessionId, session.getPasswordType());
        
        return new RecoveryResult(true, 
            "Nouveau mot de passe généré avec succès !", 
            sessionId, 
            RecoveryStep.COMPLETED, 
            result);
    }
    
    /**
     * Génère un mot de passe selon le type demandé
     */
    private static String generatePasswordByType(PasswordType type) {
        SecureRandom random = new SecureRandom();
        
        switch (type) {
            case APPLE_ID:
            case ICLOUD:
            case GOOGLE_ACCOUNT:
                return generateStrongPassword(12);
                
            case SCREEN_LOCK_IOS:
            case SCREEN_LOCK_ANDROID:
            case PIN_ANDROID:
                return generateNumericCode(6);
                
            case RESTRICTION_IOS:
                return generateNumericCode(4);
                
            case PATTERN_ANDROID:
                return generatePatternSequence();
                
            default:
                return generateStrongPassword(10);
        }
    }
    
    /**
     * Génère un mot de passe fort alphanumérique
     */
    private static String generateStrongPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Génère un code numérique
     */
    private static String generateNumericCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        
        return code.toString();
    }
    
    /**
     * Génère une séquence de motif Android
     */
    private static String generatePatternSequence() {
        // Motifs Android basés sur une grille 3x3 (positions 1-9)
        String[] patterns = {
            "1-2-3-6-9-8-7-4", // L inversé
            "1-5-9-6-3-2", // Diagonale + courbe
            "7-4-1-2-3-6-9", // L
            "2-5-8-9-6-3", // S
            "1-4-7-8-9-6-3-2", // U
            "4-5-6-9-8-7", // Vague
            "1-2-5-8-7-4", // Triangle
            "3-6-5-4-7-8-9" // Spiral
        };
        
        SecureRandom random = new SecureRandom();
        return patterns[random.nextInt(patterns.length)];
    }
    
    /**
     * Retourne les instructions spécifiques pour chaque type de mot de passe
     */
    private static String getRecoveryInstructions(PasswordType type) {
        switch (type) {
            case APPLE_ID:
                return "1. Allez dans Réglages > [Votre nom]\n" +
                       "2. Touchez 'Mot de passe et sécurité'\n" +
                       "3. Touchez 'Modifier le mot de passe'\n" +
                       "4. Saisissez le nouveau mot de passe";
                       
            case ICLOUD:
                return "1. Connectez-vous sur icloud.com\n" +
                       "2. Utilisez le nouveau mot de passe\n" +
                       "3. Mettez à jour sur tous vos appareils Apple";
                       
            case SCREEN_LOCK_IOS:
                return "1. Allez dans Réglages > Face ID et code (ou Touch ID et code)\n" +
                       "2. Touchez 'Modifier le code'\n" +
                       "3. Saisissez le nouveau code à 6 chiffres";
                       
            case RESTRICTION_IOS:
                return "1. Allez dans Réglages > Temps d'écran\n" +
                       "2. Touchez 'Modifier le code Temps d'écran'\n" +
                       "3. Saisissez le nouveau code à 4 chiffres";
                       
            case GOOGLE_ACCOUNT:
                return "1. Allez sur myaccount.google.com\n" +
                       "2. Sélectionnez 'Sécurité' > 'Mot de passe'\n" +
                       "3. Connectez-vous et changez votre mot de passe";
                       
            case SCREEN_LOCK_ANDROID:
                return "1. Allez dans Paramètres > Sécurité\n" +
                       "2. Touchez 'Verrouillage de l'écran'\n" +
                       "3. Choisissez 'Code PIN' et saisissez le nouveau code";
                       
            case PATTERN_ANDROID:
                return "1. Allez dans Paramètres > Sécurité\n" +
                       "2. Touchez 'Verrouillage de l'écran'\n" +
                       "3. Choisissez 'Motif' et tracez la séquence indiquée\n" +
                       "Séquence: " + "Suivez les numéros dans l'ordre sur la grille 3x3";
                       
            case PIN_ANDROID:
                return "1. Allez dans Paramètres > Sécurité\n" +
                       "2. Touchez 'Verrouillage de l'écran'\n" +
                       "3. Choisissez 'Code PIN' et saisissez le nouveau code";
                       
            default:
                return "Suivez les instructions de votre appareil pour changer le mot de passe.";
        }
    }
    
    /**
     * Méthodes utilitaires
     */
    private static boolean isValidInput(String imei, String email) {
        return imei != null && imei.length() == 15 && imei.matches("\\d+") &&
               email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    private static boolean checkRecoveryAttempts(String imei) {
        int attempts = recoveryAttempts.getOrDefault(imei, 0);
        LocalDateTime lastAttempt = lastAttemptTime.get(imei);
        
        if (lastAttempt != null && lastAttempt.plusHours(24).isAfter(LocalDateTime.now())) {
            return attempts < MAX_RECOVERY_ATTEMPTS;
        } else {
            // Reset après 24h
            recoveryAttempts.remove(imei);
            lastAttemptTime.remove(imei);
            return true;
        }
    }
    
    private static void incrementRecoveryAttempts(String imei) {
        int attempts = recoveryAttempts.getOrDefault(imei, 0) + 1;
        recoveryAttempts.put(imei, attempts);
        lastAttemptTime.put(imei, LocalDateTime.now());
    }
    
    private static String generateSessionId() {
        return "REC-" + System.currentTimeMillis() + "-" + 
               Integer.toHexString(new SecureRandom().nextInt());
    }
    
    private static String generateRecoveryCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        
        for (int i = 0; i < RECOVERY_CODE_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) return email;
        
        return username.charAt(0) + "*".repeat(username.length() - 2) + 
               username.charAt(username.length() - 1) + "@" + domain;
    }
    
    private static List<SecurityQuestion> getRandomSecurityQuestions(int count) {
        List<SecurityQuestion> allQuestions = Arrays.asList(SecurityQuestion.values());
        Collections.shuffle(allQuestions);
        return allQuestions.subList(0, Math.min(count, allQuestions.size()));
    }
    
    /**
     * Nettoie les sessions expirées
     */
    public static void cleanupExpiredSessions() {
        activeRecoverySessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Retourne les statistiques de récupération
     */
    public static Map<String, Object> getRecoveryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", activeRecoverySessions.size());
        stats.put("totalAttempts", recoveryAttempts.values().stream().mapToInt(Integer::intValue).sum());
        stats.put("blockedIMEIs", recoveryAttempts.entrySet().stream()
            .filter(entry -> entry.getValue() >= MAX_RECOVERY_ATTEMPTS)
            .count());
        return stats;
    }
}
