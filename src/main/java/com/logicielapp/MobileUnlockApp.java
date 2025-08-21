package com.logicielapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import com.logicielapp.util.DatabaseManager;
import com.logicielapp.util.ErrorHandler;
import com.logicielapp.service.AuthenticationService;
import com.logicielapp.ui.SplashScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Classe principale de l'application Mobile Unlock
 * Point d'entrée de l'application JavaFX
 */
public class MobileUnlockApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MobileUnlockApp.class);
    
    // Configuration de l'application
    private static final String APP_TITLE = "Logiciel de Déblocage Mobile Multi-Plateforme";
    private static final String VERSION = "1.0.0";
    private static final int MIN_WIDTH = 1200;
    private static final int MIN_HEIGHT = 800;
    private static final int PREFERRED_WIDTH = 1400;
    private static final int PREFERRED_HEIGHT = 900;
    
    // Références aux stages
    private Stage primaryStage;
    private SplashScreen splashScreen;

    @Override
    public void init() throws Exception {
        logger.info("Initialisation de l'application Mobile Unlock v{}", VERSION);
        
        try {
            // Initialiser le gestionnaire d'erreurs global
            ErrorHandler.setupGlobalExceptionHandler();
            ErrorHandler.startErrorCountResetService();
            logger.info("Gestionnaire d'erreurs initialisé");
            
            // Initialiser la base de données
            initializeDatabase();
            
            // Créer les utilisateurs par défaut si nécessaire
            createDefaultUsers();
            
            logger.info("Initialisation terminée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation", e);
            ErrorHandler.handleError(ErrorHandler.ErrorType.CRITICAL, 
                "Erreur fatale lors de l'initialisation", e);
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        logger.info("Démarrage de l'interface utilisateur");
        
        try {
            // Configuration de la fenêtre principale (cachée initialement)
            setupPrimaryStage();
            
            // Créer et afficher le splash screen
            showSplashScreen();
            
            // Effectuer le chargement en arrière-plan
            performBackgroundLoading();
            
            logger.info("Application démarrée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de l'application", e);
            showErrorDialog("Erreur de Démarrage", "Impossible de démarrer l'application", e.getMessage());
            Platform.exit();
        }
    }
    
    /**
     * Affiche l'écran de démarrage
     */
    private void showSplashScreen() {
        logger.info("Affichage de l'écran de démarrage");
        
        splashScreen = new SplashScreen();
        splashScreen.show(primaryStage, () -> {
            logger.info("SplashScreen fermé automatiquement");
        });
    }
    
    /**
     * Effectue le chargement en arrière-plan
     */
    private void performBackgroundLoading() {
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Simuler les étapes de chargement
                Thread.sleep(500);
                updateMessage("Initialisation de la base de données...");
                initializeDatabase();
                
                Thread.sleep(300);
                updateMessage("Vérification des utilisateurs...");
                createDefaultUsers();
                
                Thread.sleep(300);
                updateMessage("Chargement de l'interface...");
                
                Thread.sleep(200);
                updateMessage("Finalisation...");
                
                Thread.sleep(500);
                return null;
            }
        };
        
        // Mettre à jour les messages du splash screen
        loadingTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            if (splashScreen != null) {
                Platform.runLater(() -> splashScreen.updateLoadingMessage(newMessage));
            }
        });
        
        // Gérer la fin du chargement
        loadingTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                try {
                    // Fermer le splash screen avec animation
                    if (splashScreen != null) {
                        splashScreen.close(() -> {
                            try {
                                // Charger l'écran de connexion après la fermeture du splash
                                loadLoginScreen();
                            } catch (IOException e) {
                                logger.error("Erreur lors du chargement de l'écran de connexion", e);
                                showErrorDialog("Erreur", "Impossible de charger l'écran de connexion", e.getMessage());
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors de la transition", e);
                }
            });
        });
        
        // Gérer les erreurs
        loadingTask.setOnFailed(event -> {
            Throwable error = loadingTask.getException();
            logger.error("Erreur pendant le chargement", error);
            Platform.runLater(() -> {
                if (splashScreen != null) {
                    splashScreen.close(null);
                }
                showErrorDialog("Erreur de Chargement", 
                    "Une erreur est survenue pendant le chargement", 
                    error.getMessage());
                Platform.exit();
            });
        });
        
        // Démarrer le chargement en arrière-plan
        Thread loadingThread = new Thread(loadingTask);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    @Override
    public void stop() throws Exception {
        logger.info("Arrêt de l'application");
        
        try {
            // Fermeture propre de la base de données
            DatabaseManager.getInstance().shutdown();
            logger.info("Base de données fermée proprement");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt", e);
        }
        
        logger.info("Application arrêtée");
    }
    
    /**
     * Initialise la base de données
     */
    private void initializeDatabase() throws Exception {
        logger.info("Initialisation de la base de données");
        
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            
            // Initialiser la base de données et le pool de connexions
            dbManager.initializeDatabase();
            
            // Vérifier que la connexion fonctionne
            if (!dbManager.isDatabaseAvailable()) {
                throw new Exception("Impossible de se connecter à la base de données MySQL");
            }
            
            logger.info("Connexion à la base de données établie");
            
        } catch (Exception e) {
            logger.error("Erreur de connexion à la base de données", e);
            throw new Exception("Échec de l'initialisation de la base de données: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crée les utilisateurs par défaut
     */
    private void createDefaultUsers() {
        logger.info("Vérification des utilisateurs par défaut");
        
        try {
            AuthenticationService authService = new AuthenticationService();
            
            // Utiliser la méthode ensureDefaultAdminExists qui gère déjà la création
            // des utilisateurs par défaut avec vérification d'existence
            authService.ensureDefaultAdminExists();
            logger.info("Utilisateurs par défaut vérifiés/créés");
            
        } catch (Exception e) {
            logger.warn("Impossible de créer les utilisateurs par défaut: {}", e.getMessage());
        }
    }
    
    /**
     * Configure la fenêtre principale
     */
    private void setupPrimaryStage() {
        primaryStage.setTitle(APP_TITLE + " v" + VERSION);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setWidth(PREFERRED_WIDTH);
        primaryStage.setHeight(PREFERRED_HEIGHT);
        
        // Centrer la fenêtre sur l'écran
        primaryStage.centerOnScreen();
        
        // Gérer la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            logger.info("Demande de fermeture de l'application");
            Platform.exit();
        });
        
        logger.info("Fenêtre principale configurée ({}x{})", PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }
    
    /**
     * Charge l'écran de connexion
     */
    private void loadLoginScreen() throws IOException {
        logger.info("Chargement de l'écran de connexion");
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_window.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            // Charger les styles CSS
            loginScene.getStylesheets().add(
                getClass().getResource("/styles/application.css").toExternalForm()
            );
            
            primaryStage.setScene(loginScene);
            primaryStage.show();
            
            logger.info("Écran de connexion affiché");
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de l'écran de connexion", e);
            throw e;
        }
    }
    
    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showErrorDialog(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Point d'entrée principal de l'application
     */
    public static void main(String[] args) {
        logger.info("=== DÉMARRAGE DE L'APPLICATION MOBILE UNLOCK ===");
        logger.info("Version: {}", VERSION);
        logger.info("Java Version: {}", System.getProperty("java.version"));
        logger.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        logger.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        
        try {
            // Vérifier la version Java
            String javaVersion = System.getProperty("java.version");
            if (!javaVersion.startsWith("17") && !javaVersion.startsWith("18") && 
                !javaVersion.startsWith("19") && !javaVersion.startsWith("20") && !javaVersion.startsWith("21")) {
                logger.warn("Version Java non testée: {}. Java 17+ recommandé.", javaVersion);
            }
            
            // Lancer l'application JavaFX
            launch(args);
            
        } catch (Exception e) {
            logger.error("Erreur fatale lors du démarrage", e);
            System.err.println("Erreur fatale: " + e.getMessage());
            System.exit(1);
        }
    }
}
