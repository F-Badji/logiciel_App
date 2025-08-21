package com.logicielapp.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.logicielapp.service.AuthenticationService;
import com.logicielapp.model.User;
import com.logicielapp.util.SessionManager;
import com.logicielapp.util.DatabaseManager;
import com.logicielapp.util.TransitionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran d'authentification
 * Gère la connexion des utilisateurs avec validation et sécurité
 */
public class LoginController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    // Services
    private AuthenticationService authService;
    private Timeline clockTimer;
    
    // Champs du formulaire
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRememberMe;
    @FXML private Hyperlink linkForgotPassword;
    
    // Boutons d'action
    @FXML private Button btnLogin;
    @FXML private Button btnCancel;
    
    // Éléments d'état
    @FXML private Label lblConnectionStatus;
    @FXML private ProgressIndicator progressLogin;
    @FXML private StackPane loadingOverlay;
    @FXML private ProgressIndicator loadingSpinner;
    @FXML private Label lblLoadingText;
    
    // Barre de statut
    @FXML private Label lblServerStatus;
    @FXML private Label lblDateTime;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation de l'écran de connexion");
        
        try {
            // Initialiser le service d'authentification
            authService = new AuthenticationService();
            
            // Configurer l'interface
            setupUI();
            
            // Démarrer l'horloge
            startClock();
            
            // Vérifier l'état du serveur
            checkServerStatus();
            
            // Charger les préférences utilisateur
            loadUserPreferences();
            
            logger.info("Écran de connexion initialisé avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de l'écran de connexion", e);
            showError("Erreur d'Initialisation", "Impossible d'initialiser l'écran de connexion: " + e.getMessage());
        }
    }
    
    /**
     * Configuration initiale de l'interface utilisateur
     */
    private void setupUI() {
        // Configurer les champs de connexion
        txtEmail.setPromptText("Entrez votre email");
        txtPassword.setPromptText("Entrez votre mot de passe");
        
        // Configurer les événements clavier
        txtPassword.setOnAction(e -> handleLogin());
        
        // Désactiver le bouton de connexion si les champs sont vides
        btnLogin.disableProperty().bind(
            txtEmail.textProperty().isEmpty()
            .or(txtPassword.textProperty().isEmpty())
        );
        
        // Configuration du statut initial
        lblConnectionStatus.setText("🔒 Veuillez vous authentifier");
        updateServerStatus(true);
        
        // Ajouter des tooltips
        btnLogin.setTooltip(new Tooltip("Se connecter à l'application"));
        btnCancel.setTooltip(new Tooltip("Fermer l'application"));
        chkRememberMe.setTooltip(new Tooltip("Mémoriser vos identifiants"));
    }
    
    /**
     * Démarre l'horloge en temps réel
     */
    private void startClock() {
        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            lblDateTime.setText(currentTime);
        }));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
    }
    
    /**
     * Vérifie l'état du serveur de base de données
     */
    private void checkServerStatus() {
        Task<Boolean> statusTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return DatabaseManager.getInstance().isDatabaseAvailable();
            }
        };
        
        statusTask.setOnSucceeded(e -> {
            boolean connected = statusTask.getValue();
            updateServerStatus(connected);
        });
        
        statusTask.setOnFailed(e -> {
            updateServerStatus(false);
            logger.error("Erreur lors de la vérification du serveur", statusTask.getException());
        });
        
        Thread statusThread = new Thread(statusTask, "ServerStatusCheck");
        statusThread.setDaemon(true);
        statusThread.start();
    }
    
    /**
     * Met à jour le statut du serveur
     */
    private void updateServerStatus(boolean connected) {
        if (connected) {
            lblServerStatus.setText("🌐 Serveur: ✅ Connecté");
            lblServerStatus.setStyle("-fx-text-fill: #28a745;");
        } else {
            lblServerStatus.setText("🌐 Serveur: ❌ Déconnecté");
            lblServerStatus.setStyle("-fx-text-fill: #dc3545;");
        }
    }
    
    /**
     * Charge les préférences utilisateur sauvegardées
     */
    private void loadUserPreferences() {
        // Charger les préférences depuis le stockage local
        String savedEmail = SessionManager.getInstance().getStoredEmail();
        if (savedEmail != null && !savedEmail.isEmpty()) {
            txtEmail.setText(savedEmail);
            chkRememberMe.setSelected(true);
        }
    }
    
    /**
     * Sauvegarde les préférences utilisateur
     */
    private void saveUserPreferences() {
        if (chkRememberMe.isSelected()) {
            SessionManager.getInstance().storeEmail(txtEmail.getText().trim());
        } else {
            SessionManager.getInstance().clearStoredCredentials();
        }
    }
    
    // ======================== ACTIONS UTILISATEUR ========================
    
    @FXML
    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        
        if (!validateInput(email, password)) {
            return;
        }
        
        // Afficher l'overlay de chargement
        showLoadingOverlay("Vérification des identifiants...");
        
        // Désactiver les contrôles pendant l'authentification
        setControlsEnabled(false);
        
        // Authentification asynchrone
        Task<User> loginTask = new Task<User>() {
            @Override
            protected User call() throws Exception {
                // Simuler un délai réseau (à supprimer en production)
                Thread.sleep(1000);
                
                // Authentifier l'utilisateur
                return authService.authenticate(email, password);
            }
        };
        
        loginTask.setOnSucceeded(e -> handleLoginSuccess(loginTask.getValue()));
        loginTask.setOnFailed(e -> handleLoginFailure(loginTask.getException().getMessage()));
        
        Thread loginThread = new Thread(loginTask, "LoginProcess");
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    /**
     * Valide les données de connexion
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            showError("Champs requis", "Veuillez remplir tous les champs.");
            return false;
        }
        
        // Autoriser Email OU Nom d'utilisateur
        // Si l'entrée contient '@', on valide le format email. Sinon, on accepte comme nom d'utilisateur.
        if (email.contains("@")) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showError("Format invalide", "Veuillez entrer une adresse email valide.");
                return false;
            }
        } else {
            // Validation simple du nom d'utilisateur (ex: Admin)
            if (email.length() < 3) {
                showError("Nom d'utilisateur invalide", "Le nom d'utilisateur doit contenir au moins 3 caractères.");
                return false;
            }
        }
        
        if (password.length() < 6) {
            showError("Mot de passe invalide", "Le mot de passe doit contenir au moins 6 caractères.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Gère le succès de la connexion
     */
    private void handleLoginSuccess(User user) {
        Platform.runLater(() -> {
            try {
                hideLoadingOverlay();
                
                // Enregistrer la session
                SessionManager.getInstance().setCurrentUser(user);
                
                // Sauvegarder les préférences
                saveUserPreferences();
                
                // Log de connexion
                logger.info("Connexion réussie pour l'utilisateur: {} ({})", user.getEmail(), user.getRole());
                
                // Charger l'écran principal
                loadMainScreen();
                
            } catch (Exception ex) {
                logger.error("Erreur post-connexion", ex);
                showError("Erreur", "Une erreur est survenue après la connexion: " + ex.getMessage());
                setControlsEnabled(true);
            }
        });
    }

/**
 * Gère l'échec de la connexion
 */
private void handleLoginFailure(String errorMessage) {
    Platform.runLater(() -> {
        hideLoadingOverlay();
        setControlsEnabled(true);
        
        lblConnectionStatus.setText(" Échec de l'authentification");
        lblConnectionStatus.setStyle("-fx-text-fill: #dc3545;");
        
        // Animation d'erreur
        animateErrorField(txtPassword);
        
        // Effacer le mot de passe
        txtPassword.clear();
        txtPassword.requestFocus();
        
        showError("Authentification échouée", 
                 "Email ou mot de passe incorrect.\n" + errorMessage);
        
        logger.warn("Tentative de connexion échouée pour: {}", txtEmail.getText());
    });
}

/**
 * Charge l'écran principal après authentification
 */
private void loadMainScreen() {
    try {
        logger.info("Chargement de l'écran principal");
        
        // Charger le FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_window.fxml"));
        Parent mainRoot = loader.load();
        
        // Créer la nouvelle scène
        Scene mainScene = new Scene(mainRoot);
        mainScene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        
        // Obtenir le stage actuel
        Stage stage = (Stage) btnLogin.getScene().getWindow();
        Scene currentScene = stage.getScene();
        
        // Transition entre scènes
        TransitionManager.crossFadeScenes(stage, currentScene, mainScene, () -> {
            logger.info("Transition vers l'écran principal terminée");
            
            // Initialiser le contrôleur principal
            Object controller = loader.getController();
            if (controller instanceof MainController mainController) {
                mainController.initializeUserData();
            }
        });
        
    } catch (Exception e) {
        logger.error("Erreur lors du chargement de l'écran principal", e);
        showError("Erreur", "Impossible de charger l'écran principal: " + e.getMessage());
    }
}

@FXML
private void handleCancel() {
    // Arrêter l'horloge
    if (clockTimer != null) {
        clockTimer.stop();
    }
    
    // Fermer l'application proprement
    try {
        // Nettoyer les ressources si nécessaire
        SessionManager.getInstance().cleanup();
        DatabaseManager.getInstance().shutdown();
        
        Platform.exit();
    } catch (Exception e) {
        logger.error("Erreur lors de la fermeture de l'application", e);
        Platform.exit();
    }
}

@FXML
private void handleForgotPassword() {
    try {
        // TODO: Implémenter la récupération de mot de passe
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/password_recovery.fxml"));
        Parent recoveryRoot = loader.load();
        
        Stage recoveryStage = new Stage();
        recoveryStage.setTitle("Récupération de mot de passe");
        recoveryStage.setScene(new Scene(recoveryRoot));
        recoveryStage.show();
        
    } catch (Exception e) {
        logger.error("Erreur lors de l'ouverture de la récupération de mot de passe", e);
        showError("Fonctionnalité non disponible", 
                 "La récupération de mot de passe n'est pas encore implémentée.\n" +
                 "Veuillez contacter votre administrateur.");
    }
}

// ======================== MÉTHODES UTILITAIRES ========================

/**
 * Affiche l'overlay de chargement
 */
private void showLoadingOverlay(String message) {
    lblLoadingText.setText(message);
    loadingOverlay.setVisible(true);
    loadingSpinner.setVisible(true);
}

/**
 * Cache l'overlay de chargement
 */
private void hideLoadingOverlay() {
    loadingOverlay.setVisible(false);
    loadingSpinner.setVisible(false);
}

/**
 * Active/désactive les contrôles
 */
private void setControlsEnabled(boolean enabled) {
    txtEmail.setDisable(!enabled);
    txtPassword.setDisable(!enabled);
    btnCancel.setDisable(!enabled);
    chkRememberMe.setDisable(!enabled);
    linkForgotPassword.setDisable(!enabled);
}

/**
 * Animation d'erreur pour un champ
 */
private void animateErrorField(Control field) {
    field.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2px;");
    
    Timeline resetStyle = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
        field.setStyle("");
    }));
    resetStyle.play();
}

/**
 * Affiche une erreur
 */
private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

/**
 * Nettoyage des ressources à la fermeture
 */
public void cleanup() {
    if (clockTimer != null) {
        clockTimer.stop();
    }
}
}

