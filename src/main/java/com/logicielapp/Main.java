package com.logicielapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.logicielapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logiciel de Déblocage Mobile Multi-Plateforme
 * Application principale - Point d'entrée du programme
 * 
 * @author Logiciel App Team
 * @version 1.0.0
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Initialiser la base de données
            DatabaseManager.getInstance().initializeDatabase();
            
            // S'assurer que les utilisateurs par défaut existent
            com.logicielapp.service.AuthenticationService authService = new com.logicielapp.service.AuthenticationService();
            authService.ensureDefaultAdminExists();
            
            // Charger l'écran de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_window.fxml"));
            Parent root = loader.load();
            
            // Créer la scène et charger les styles CSS ultra-modernes
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            // Configuration de la fenêtre de connexion
            primaryStage.setTitle("Authentification - Logiciel de Déblocage Mobile");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            // Icône de l'application (à ajouter plus tard)
            // primaryStage.getIcons().add(new Image("/images/app_icon.png"));
            
            primaryStage.show();
            
            logger.info("Application démarrée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de l'application", e);
            throw e;
        }
    }
    
    @Override
    public void stop() throws Exception {
        try {
            // Fermer les connexions de base de données
            DatabaseManager.getInstance().shutdown();
            logger.info("Application fermée proprement");
        } catch (Exception e) {
            logger.error("Erreur lors de la fermeture de l'application", e);
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        logger.info("Démarrage du Logiciel de Déblocage Mobile Multi-Plateforme...");
        launch(args);
    }
}
