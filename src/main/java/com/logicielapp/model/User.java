package com.logicielapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modèle représentant un utilisateur du système
 * Contient les informations d'authentification et d'autorisation
 */
public class User {
    
    public enum Role {
        ADMIN("Administrateur"),
        TECHNICIEN("Technicien"),
        UTILISATEUR("Utilisateur");
        
        private final String displayName;
        
        Role(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Identifiants
    private int id;
    private String nom;
    private String email;
    private String motDePasse; // Haché
    private Role role;
    
    // Informations de session
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime derniereConnexion;
    private String sessionToken;
    
    // Métadonnées
    private String adresseIP;
    private String userAgent;
    private int tentativesConnexion;
    private LocalDateTime derniereTentative;
    
    // Permissions spécifiques
    private boolean peutDebloquerIOS;
    private boolean peutDebloquerAndroid;
    private boolean peutUtiliserIMEI;
    private boolean peutVoirStatistiques;
    private boolean peutGererUtilisateurs;
    
    // Constructeurs
    public User() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
        this.tentativesConnexion = 0;
    }
    
    public User(String nom, String email, Role role) {
        this();
        this.nom = nom;
        this.email = email;
        this.role = role;
        setupDefaultPermissions();
    }
    
    // Méthodes utilitaires
    
    /**
     * Configure les permissions par défaut selon le rôle
     */
    private void setupDefaultPermissions() {
        switch (role) {
            case ADMIN:
                peutDebloquerIOS = true;
                peutDebloquerAndroid = true;
                peutUtiliserIMEI = true;
                peutVoirStatistiques = true;
                peutGererUtilisateurs = true;
                break;
                
            case TECHNICIEN:
                peutDebloquerIOS = true;
                peutDebloquerAndroid = true;
                peutUtiliserIMEI = true;
                peutVoirStatistiques = true;
                peutGererUtilisateurs = false;
                break;
                
            case UTILISATEUR:
                peutDebloquerIOS = true;
                peutDebloquerAndroid = true;
                peutUtiliserIMEI = false;
                peutVoirStatistiques = false;
                peutGererUtilisateurs = false;
                break;
        }
    }
    
    /**
     * Met à jour la dernière connexion
     */
    public void updateLastLogin() {
        this.derniereConnexion = LocalDateTime.now();
        this.tentativesConnexion = 0;
    }
    
    /**
     * Incrémente le compteur de tentatives
     */
    public void incrementLoginAttempts() {
        this.tentativesConnexion++;
        this.derniereTentative = LocalDateTime.now();
    }
    
    /**
     * Vérifie si l'utilisateur peut effectuer une opération
     */
    public boolean canPerformOperation(String operationType) {
        if (!actif) return false;
        
        switch (operationType.toLowerCase()) {
            case "ios":
                return peutDebloquerIOS;
            case "android":
                return peutDebloquerAndroid;
            case "imei":
                return peutUtiliserIMEI;
            case "statistics":
                return peutVoirStatistiques;
            case "users":
                return peutGererUtilisateurs;
            default:
                return false;
        }
    }
    
    /**
     * Vérifie si le compte est bloqué (trop de tentatives)
     */
    public boolean isAccountLocked() {
        return tentativesConnexion >= 5 && 
               derniereTentative != null &&
               derniereTentative.isAfter(LocalDateTime.now().minusMinutes(15));
    }
    
    /**
     * Retourne un nom d'affichage formaté
     */
    public String getDisplayName() {
        return nom + " (" + role + ")";
    }
    
    /**
     * Génère un nouveau token de session
     */
    public void generateSessionToken() {
        this.sessionToken = "SESSION_" + System.currentTimeMillis() + "_" + 
                           Integer.toHexString(hashCode());
    }
    
    // Getters et Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMotDePasse() {
        return motDePasse;
    }
    
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
        setupDefaultPermissions();
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }
    
    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }
    
    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public String getAdresseIP() {
        return adresseIP;
    }
    
    public void setAdresseIP(String adresseIP) {
        this.adresseIP = adresseIP;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public int getTentativesConnexion() {
        return tentativesConnexion;
    }
    
    public void setTentativesConnexion(int tentativesConnexion) {
        this.tentativesConnexion = tentativesConnexion;
    }
    
    public LocalDateTime getDerniereTentative() {
        return derniereTentative;
    }
    
    public void setDerniereTentative(LocalDateTime derniereTentative) {
        this.derniereTentative = derniereTentative;
    }
    
    public boolean isPeutDebloquerIOS() {
        return peutDebloquerIOS;
    }
    
    public void setPeutDebloquerIOS(boolean peutDebloquerIOS) {
        this.peutDebloquerIOS = peutDebloquerIOS;
    }
    
    public boolean isPeutDebloquerAndroid() {
        return peutDebloquerAndroid;
    }
    
    public void setPeutDebloquerAndroid(boolean peutDebloquerAndroid) {
        this.peutDebloquerAndroid = peutDebloquerAndroid;
    }
    
    public boolean isPeutUtiliserIMEI() {
        return peutUtiliserIMEI;
    }
    
    public void setPeutUtiliserIMEI(boolean peutUtiliserIMEI) {
        this.peutUtiliserIMEI = peutUtiliserIMEI;
    }
    
    public boolean isPeutVoirStatistiques() {
        return peutVoirStatistiques;
    }
    
    public void setPeutVoirStatistiques(boolean peutVoirStatistiques) {
        this.peutVoirStatistiques = peutVoirStatistiques;
    }
    
    public boolean isPeutGererUtilisateurs() {
        return peutGererUtilisateurs;
    }
    
    public void setPeutGererUtilisateurs(boolean peutGererUtilisateurs) {
        this.peutGererUtilisateurs = peutGererUtilisateurs;
    }
    
    // Méthodes Object
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%d, nom='%s', email='%s', role=%s, actif=%s}", 
                           id, nom, email, role, actif);
    }
}
