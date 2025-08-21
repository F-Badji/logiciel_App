package com.logicielapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.application.Platform;
import com.logicielapp.util.DatabaseManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametersController {

    @FXML private Button homeBtn;
    @FXML private Button statisticsBtn;
    @FXML private Button parametersBtn;
    @FXML private Button helpBtn;
    @FXML private Button saveBtn;
    @FXML private Button resetBtn;
    
    @FXML private Label saveAlert;
    
    // Configuration API
    @FXML private CheckBox dhruEnabledCheck;
    @FXML private PasswordField dhruApiKeyField;
    @FXML private CheckBox ifreeEnabledCheck;
    @FXML private Spinner<Integer> apiTimeoutSpinner;
    
    // Paramètres de validation
    @FXML private CheckBox luhnValidationCheck;
    @FXML private CheckBox fakeDetectionCheck;
    @FXML private CheckBox gsmaDbCheck;
    @FXML private ComboBox<String> validationModeCombo;
    
    // Interface utilisateur
    @FXML private CheckBox darkThemeCheck;
    @FXML private CheckBox animationsCheck;
    @FXML private ComboBox<String> languageCombo;
    @FXML private CheckBox notificationsCheck;
    
    // Sécurité
    @FXML private CheckBox detailedLogsCheck;
    @FXML private CheckBox maskImeiCheck;
    @FXML private Spinner<Integer> sessionDurationSpinner;

    @FXML
    private void initialize() {
        // Initialiser les spinners
        if (apiTimeoutSpinner != null) {
            apiTimeoutSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 15));
        }
        if (sessionDurationSpinner != null) {
            sessionDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 480, 60));
        }
        
        // Initialiser les ComboBox
        if (validationModeCombo != null) {
            validationModeCombo.getItems().addAll("Strict", "Normal", "Permissif");
            validationModeCombo.setValue("Normal");
        }
        
        if (languageCombo != null) {
            languageCombo.getItems().addAll("Français", "English", "Español", "العربية");
            languageCombo.setValue("Français");
        }
        
        // Charger les paramètres sauvegardés
        loadSettings();
        
        // Configuration de la taille de fenêtre après initialisation complète
        Platform.runLater(this::setupWindowSize);
    }
    
    private void setupWindowSize() {
        try {
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) homeBtn.getScene().getWindow();
            
            // Définir seulement les tailles minimales sans changer la taille actuelle
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Ne pas changer la taille si la fenêtre est en plein écran
            if (!stage.isMaximized()) {
                stage.setWidth(900);
                stage.setHeight(700);
                stage.centerOnScreen();
            }
            
            System.out.println("🎯 Taille de fenêtre Paramètres configurée: 900x700 (état préservé)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        // Charger depuis la base de données directement
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Configuration API
            if (dhruEnabledCheck != null) {
                dhruEnabledCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "dhru_enabled", "true")));
            }
            if (dhruApiKeyField != null) {
                dhruApiKeyField.setText(loadConfigValue(conn, "dhru_api_key", "8AE-VC2-G18-1K7-K73-8FI-4H4-2AU"));
            }
            if (ifreeEnabledCheck != null) {
                ifreeEnabledCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "ifree_enabled", "true")));
            }
            if (apiTimeoutSpinner != null) {
                apiTimeoutSpinner.getValueFactory().setValue(Integer.parseInt(loadConfigValue(conn, "api_timeout", "15")));
            }
            
            // Validation
            if (luhnValidationCheck != null) {
                luhnValidationCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "luhn_validation", "true")));
            }
            if (fakeDetectionCheck != null) {
                fakeDetectionCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "fake_detection", "true")));
            }
            if (gsmaDbCheck != null) {
                gsmaDbCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "gsma_db", "true")));
            }
            if (validationModeCombo != null) {
                validationModeCombo.setValue(loadConfigValue(conn, "validation_mode", "Normal"));
            }
            
            // Interface
            if (darkThemeCheck != null) {
                darkThemeCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "dark_theme", "false")));
            }
            if (animationsCheck != null) {
                animationsCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "animations", "true")));
            }
            if (languageCombo != null) {
                languageCombo.setValue(loadConfigValue(conn, "language", "Français"));
            }
            if (notificationsCheck != null) {
                notificationsCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "notifications", "true")));
            }
            
            // Sécurité
            if (detailedLogsCheck != null) {
                detailedLogsCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "detailed_logs", "true")));
            }
            if (maskImeiCheck != null) {
                maskImeiCheck.setSelected(Boolean.parseBoolean(loadConfigValue(conn, "mask_imei", "true")));
            }
            if (sessionDurationSpinner != null) {
                sessionDurationSpinner.getValueFactory().setValue(Integer.parseInt(loadConfigValue(conn, "session_duration", "60")));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            // Utiliser les valeurs par défaut en cas d'erreur
            setDefaultValues();
        }
    }
    
    private String loadConfigValue(Connection conn, String key, String defaultValue) {
        String sql = "SELECT valeur FROM configurations WHERE cle_config = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("valeur");
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement config " + key + ": " + e.getMessage());
        }
        return defaultValue;
    }
    
    private void setDefaultValues() {
        if (dhruEnabledCheck != null) dhruEnabledCheck.setSelected(true);
        if (dhruApiKeyField != null) dhruApiKeyField.setText("8AE-VC2-G18-1K7-K73-8FI-4H4-2AU");
        if (ifreeEnabledCheck != null) ifreeEnabledCheck.setSelected(true);
        if (apiTimeoutSpinner != null) apiTimeoutSpinner.getValueFactory().setValue(15);
        if (luhnValidationCheck != null) luhnValidationCheck.setSelected(true);
        if (fakeDetectionCheck != null) fakeDetectionCheck.setSelected(true);
        if (gsmaDbCheck != null) gsmaDbCheck.setSelected(true);
        if (validationModeCombo != null) validationModeCombo.setValue("Normal");
        if (darkThemeCheck != null) darkThemeCheck.setSelected(false);
        if (animationsCheck != null) animationsCheck.setSelected(true);
        if (languageCombo != null) languageCombo.setValue("Français");
        if (notificationsCheck != null) notificationsCheck.setSelected(true);
        if (detailedLogsCheck != null) detailedLogsCheck.setSelected(true);
        if (maskImeiCheck != null) maskImeiCheck.setSelected(true);
        if (sessionDurationSpinner != null) sessionDurationSpinner.getValueFactory().setValue(60);
    }
    
    private boolean saveConfigValue(Connection conn, String key, String value) {
        String sql = "INSERT INTO configurations (cle_config, valeur, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE valeur = VALUES(valeur)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, getConfigDescription(key));
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde config " + key + ": " + e.getMessage());
            return false;
        }
    }
    
    private String getConfigDescription(String key) {
        switch (key) {
            case "dhru_enabled": return "Activation du service DHRU";
            case "dhru_api_key": return "Clé API DHRU";
            case "ifree_enabled": return "Activation du service iFree";
            case "api_timeout": return "Timeout API en secondes";
            case "luhn_validation": return "Validation Luhn des IMEI";
            case "fake_detection": return "Détection des IMEI factices";
            case "gsma_db": return "Utilisation base GSMA";
            case "validation_mode": return "Mode de validation";
            case "dark_theme": return "Thème sombre";
            case "animations": return "Animations interface";
            case "language": return "Langue interface";
            case "notifications": return "Notifications système";
            case "detailed_logs": return "Logs détaillés";
            case "mask_imei": return "Masquage IMEI dans logs";
            case "session_duration": return "Durée session en minutes";
            default: return "Configuration système";
        }
    }
    
    private boolean resetAllConfigurations() {
        String sql = "DELETE FROM configurations";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Configurations supprimées: " + rowsAffected);
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur réinitialisation configurations: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void saveSettings() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            
            // Validation des paramètres
            if (apiTimeoutSpinner != null && (apiTimeoutSpinner.getValue() < 5 || apiTimeoutSpinner.getValue() > 60)) {
                showAlert("Le timeout API doit être entre 5 et 60 secondes", false);
                return;
            }
            
            if (sessionDurationSpinner != null && (sessionDurationSpinner.getValue() < 15 || sessionDurationSpinner.getValue() > 480)) {
                showAlert("La durée de session doit être entre 15 et 480 minutes", false);
                return;
            }
            
            // Sauvegarder tous les paramètres dans la base de données
            boolean allSaved = true;
            
            // Configuration API
            if (dhruEnabledCheck != null) {
                allSaved &= saveConfigValue(conn, "dhru_enabled", String.valueOf(dhruEnabledCheck.isSelected()));
            }
            if (dhruApiKeyField != null) {
                allSaved &= saveConfigValue(conn, "dhru_api_key", dhruApiKeyField.getText());
            }
            if (ifreeEnabledCheck != null) {
                allSaved &= saveConfigValue(conn, "ifree_enabled", String.valueOf(ifreeEnabledCheck.isSelected()));
            }
            if (apiTimeoutSpinner != null) {
                allSaved &= saveConfigValue(conn, "api_timeout", String.valueOf(apiTimeoutSpinner.getValue()));
            }
            
            // Validation
            if (luhnValidationCheck != null) {
                allSaved &= saveConfigValue(conn, "luhn_validation", String.valueOf(luhnValidationCheck.isSelected()));
            }
            if (fakeDetectionCheck != null) {
                allSaved &= saveConfigValue(conn, "fake_detection", String.valueOf(fakeDetectionCheck.isSelected()));
            }
            if (gsmaDbCheck != null) {
                allSaved &= saveConfigValue(conn, "gsma_db", String.valueOf(gsmaDbCheck.isSelected()));
            }
            if (validationModeCombo != null) {
                allSaved &= saveConfigValue(conn, "validation_mode", validationModeCombo.getValue());
            }
            
            // Interface
            if (darkThemeCheck != null) {
                allSaved &= saveConfigValue(conn, "dark_theme", String.valueOf(darkThemeCheck.isSelected()));
            }
            if (animationsCheck != null) {
                allSaved &= saveConfigValue(conn, "animations", String.valueOf(animationsCheck.isSelected()));
            }
            if (languageCombo != null) {
                allSaved &= saveConfigValue(conn, "language", languageCombo.getValue());
            }
            if (notificationsCheck != null) {
                allSaved &= saveConfigValue(conn, "notifications", String.valueOf(notificationsCheck.isSelected()));
            }
            
            // Sécurité
            if (detailedLogsCheck != null) {
                allSaved &= saveConfigValue(conn, "detailed_logs", String.valueOf(detailedLogsCheck.isSelected()));
            }
            if (maskImeiCheck != null) {
                allSaved &= saveConfigValue(conn, "mask_imei", String.valueOf(maskImeiCheck.isSelected()));
            }
            if (sessionDurationSpinner != null) {
                allSaved &= saveConfigValue(conn, "session_duration", String.valueOf(sessionDurationSpinner.getValue()));
            }
            
            if (allSaved) {
                showAlert("Paramètres sauvegardés avec succès dans la base de données !", true);
                System.out.println("✅ Tous les paramètres ont été sauvegardés dans la base de données");
            } else {
                showAlert("Erreur partielle lors de la sauvegarde des paramètres", false);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de la sauvegarde des paramètres", false);
        }
    }

    @FXML
    private void resetSettings() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Réinitialiser les paramètres");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir réinitialiser tous les paramètres ?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (resetAllConfigurations()) {
                    loadSettings(); // Recharger les valeurs par défaut
                    showAlert("Paramètres réinitialisés avec succès !", true);
                } else {
                    showAlert("Erreur lors de la réinitialisation des paramètres", false);
                }
            }
        });
    }

    private void showAlert(String message, boolean success) {
        saveAlert.setText((success ? "✅ " : "❌ ") + message);
        saveAlert.setStyle(success ? 
            "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #c3e6cb; -fx-border-radius: 8;" :
            "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #f5c6cb; -fx-border-radius: 8;"
        );
        saveAlert.setVisible(true);
        
        // Masquer l'alerte après 3 secondes
        Platform.runLater(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> saveAlert.setVisible(false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    // Les méthodes de navigation ont été supprimées car cette interface s'ouvre maintenant dans une fenêtre séparée
    // La navigation se fait en fermant la fenêtre et en retournant à l'interface principale
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) homeBtn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void openStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics_window.fxml"));
            Parent statisticsWindow = loader.load();
            
            Stage statisticsStage = new Stage();
            statisticsStage.setTitle("📊 Statistiques - Logiciel de Déblocage Mobile");
            statisticsStage.initModality(Modality.APPLICATION_MODAL);
            statisticsStage.initOwner(homeBtn.getScene().getWindow());
            
            Scene scene = new Scene(statisticsWindow, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            statisticsStage.setScene(scene);
            
            statisticsStage.setResizable(true);
            statisticsStage.setMinWidth(800);
            statisticsStage.setMinHeight(600);
            
            statisticsStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openHelp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/help_window.fxml"));
            Parent helpWindow = loader.load();
            
            Stage helpStage = new Stage();
            helpStage.setTitle("❓ Aide - Logiciel de Déblocage Mobile");
            helpStage.initModality(Modality.APPLICATION_MODAL);
            helpStage.initOwner(homeBtn.getScene().getWindow());
            
            Scene scene = new Scene(helpWindow, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            helpStage.setScene(scene);
            
            helpStage.setResizable(true);
            helpStage.setMinWidth(800);
            helpStage.setMinHeight(600);
            
            helpStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
