package com.logicielapp.controller;

import com.logicielapp.model.Device;
import com.logicielapp.service.FastDeviceDetectionService;
import com.logicielapp.service.UnlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur pour le sélecteur d'opération intelligent
 * Analyse l'appareil connecté et propose les opérations appropriées
 */
public class OperationSelectorController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationSelectorController.class);
    
    // Injections FXML - Informations appareil
    @FXML private Label lblDeviceBrand;
    @FXML private Label lblDeviceModel;
    @FXML private Label lblDevicePlatform;
    @FXML private Label lblDeviceOS;
    @FXML private Label lblDeviceIMEI;
    @FXML private Label lblDeviceSerial;
    @FXML private Label lblDeviceCapacity;
    @FXML private Label lblBatteryLevel;
    @FXML private javafx.scene.image.ImageView deviceImageView;
    @FXML private Label lblConnectionStatus;
    @FXML private Label lblCompatibilityStatus;
    
    // Injections FXML - Opérations iOS
    @FXML private VBox iosOperationsPane;
    @FXML private RadioButton radiOSiCloudBypass;
    @FXML private RadioButton radioiOSPasscodeUnlock;
    @FXML private RadioButton radioiOSScreenTime;
    @FXML private RadioButton radioiOSActivationLock;
    
    // Injections FXML - Opérations Android
    @FXML private VBox androidOperationsPane;
    @FXML private RadioButton radioAndroidFRP;
    @FXML private RadioButton radioAndroidPattern;
    @FXML private RadioButton radioAndroidSamsung;
    @FXML private RadioButton radioAndroidMi;
    @FXML private RadioButton radioAndroidBootloader;
    
    // Injections FXML - Interface générale
    @FXML private VBox noOperationsPane;
    @FXML private CheckBox chkPreserveData;
    @FXML private CheckBox chkAdvancedMode;
    @FXML private CheckBox chkBypassSecurity;
    @FXML private CheckBox chkCreateBackup;
    
    // Injections FXML - Estimations
    @FXML private Label lblEstimatedTime;
    @FXML private Label lblSuccessRate;
    @FXML private Label lblRiskLevel;
    
    // Injections FXML - Boutons d'action
    @FXML private Button btnStartOperation;
    @FXML private Button btnAdvancedSettings;
    @FXML private Button btnRefreshDevice;
    
    // Services et données
    private FastDeviceDetectionService deviceDetectionService;
    private UnlockService unlockService;
    private Device currentDevice;
    private ToggleGroup operationToggleGroup;
    
    // Configuration des opérations et leurs métadonnées
    private final Map<String, OperationConfig> operationConfigs = new HashMap<>();
    
    /**
     * Configuration d'une opération de déblocage
     */
    private static class OperationConfig {
        String name;
        String estimatedTime;
        String successRate;
        String riskLevel;
        boolean requiresAdvanced;
        
        OperationConfig(String name, String time, String rate, String risk, boolean advanced) {
            this.name = name;
            this.estimatedTime = time;
            this.successRate = rate;
            this.riskLevel = risk;
            this.requiresAdvanced = advanced;
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        initializeOperationConfigs();
        setupRadioButtonGroup();
        setupEventHandlers();
        updateInterfaceState();
        
        // Démarrer la détection automatique d'appareils
        startDeviceDetection();
        
        System.out.println("[OperationSelector] Contrôleur initialisé - " + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void initializeServices() {
        this.deviceDetectionService = new FastDeviceDetectionService();
        this.unlockService = new UnlockService();
        System.out.println("[OperationSelector] Services initialisés");
    }
    
    private void initializeOperationConfigs() {
        // Configuration iOS
        operationConfigs.put("ios_icloud", new OperationConfig("iCloud Bypass", "15-30 min", "85%", "Moyen", false));
        operationConfigs.put("ios_passcode", new OperationConfig("Passcode Unlock", "5-15 min", "95%", "Faible", false));
        operationConfigs.put("ios_screentime", new OperationConfig("Screen Time Bypass", "10-20 min", "90%", "Faible", false));
        operationConfigs.put("ios_activation", new OperationConfig("Activation Lock Bypass", "20-45 min", "80%", "Élevé", true));
        
        // Configuration Android
        operationConfigs.put("android_frp", new OperationConfig("FRP Bypass", "10-25 min", "88%", "Moyen", false));
        operationConfigs.put("android_pattern", new OperationConfig("Pattern Unlock", "5-10 min", "92%", "Faible", false));
        operationConfigs.put("android_samsung", new OperationConfig("Samsung Account Bypass", "15-35 min", "82%", "Moyen", false));
        operationConfigs.put("android_mi", new OperationConfig("Mi Account Bypass", "20-40 min", "78%", "Moyen", true));
        operationConfigs.put("android_bootloader", new OperationConfig("Bootloader Unlock", "10-20 min", "95%", "Élevé", true));
        
        System.out.println("[OperationSelector] Configurations d'opération chargées (" + operationConfigs.size() + " types)");
    }
    
    private void setupRadioButtonGroup() {
        operationToggleGroup = new ToggleGroup();
        
        // Ajout des boutons iOS au groupe
        radiOSiCloudBypass.setToggleGroup(operationToggleGroup);
        radioiOSPasscodeUnlock.setToggleGroup(operationToggleGroup);
        radioiOSScreenTime.setToggleGroup(operationToggleGroup);
        radioiOSActivationLock.setToggleGroup(operationToggleGroup);
        
        // Ajout des boutons Android au groupe
        radioAndroidFRP.setToggleGroup(operationToggleGroup);
        radioAndroidPattern.setToggleGroup(operationToggleGroup);
        radioAndroidSamsung.setToggleGroup(operationToggleGroup);
        radioAndroidMi.setToggleGroup(operationToggleGroup);
        radioAndroidBootloader.setToggleGroup(operationToggleGroup);
        
        System.out.println("[OperationSelector] Groupe de boutons radio configuré");
    }
    
    private void setupEventHandlers() {
        // Écoute des changements de sélection d'opération
        operationToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateOperationEstimation();
                btnStartOperation.setDisable(false);
            } else {
                btnStartOperation.setDisable(true);
            }
        });
        
        // Écoute des changements sur les options avancées
        chkAdvancedMode.setOnAction(e -> updateAdvancedOptionsVisibility());
        
        System.out.println("[OperationSelector] Gestionnaires d'événements configurés");
    }
    
    private void startDeviceDetection() {
        Task<Device> detectionTask = new Task<Device>() {
            @Override
            protected Device call() throws Exception {
                // Utiliser le nouveau service de détection réelle
                List<Device> devices = deviceDetectionService.detectAllConnectedDevices();
                return devices.isEmpty() ? null : devices.get(0);
            }
            
            @Override
            protected void succeeded() {
                Device device = getValue();
                if (device != null) {
                    updateDeviceInfo(device);
                    System.out.println("[OperationSelector] Appareil détecté: " + device.getModel());
                } else {
                    showNoDeviceState();
                    System.out.println("[OperationSelector] Aucun appareil détecté");
                }
            }
            
            @Override
            protected void failed() {
                System.err.println("[OperationSelector] Erreur détection appareil: " + getException().getMessage());
                showNoDeviceState();
            }
        };
        
        new Thread(detectionTask).start();
    }
    
    private void updateDeviceInfo(Device device) {
        Platform.runLater(() -> {
            this.currentDevice = device;
            
            // Mise à jour des informations de l'appareil
            lblDeviceBrand.setText(device.getBrand() != null ? device.getBrand() : "Inconnu");
            lblDeviceModel.setText(device.getModel());
            lblDevicePlatform.setText(device.getPlatform().toString());
            lblDeviceOS.setText(device.getOsVersion() != null ? device.getOsVersion() : "Non disponible");
            lblDeviceIMEI.setText(device.getImei() != null ? device.getImei() : "Non disponible");
            lblDeviceSerial.setText(device.getSerialNumber() != null ? device.getSerialNumber() : "Non disponible");
            
            // Extraire et afficher la capacité de stockage - forcer l'extraction si vide
            String capacity = device.getStorageCapacity();
            if (capacity == null || capacity.isEmpty()) {
                // Forcer l'extraction de la capacité réelle
                capacity = deviceDetectionService.extractRealStorageCapacity();
                if (capacity != null) {
                    device.setStorageCapacity(capacity);
                }
            }
            if (capacity == null || capacity.isEmpty()) {
                capacity = extractStorageCapacityForDevice(device);
            }
            lblDeviceCapacity.setText(capacity != null ? capacity : "Non disponible");
            
            // Afficher le niveau de batterie - forcer l'extraction si vide
            String batteryLevel = device.getBatteryLevel();
            if (batteryLevel == null || batteryLevel.isEmpty()) {
                // Forcer l'extraction du niveau de batterie réel
                batteryLevel = deviceDetectionService.extractRealBatteryLevel();
                if (batteryLevel != null) {
                    device.setBatteryLevel(batteryLevel);
                }
            }
            
            if (batteryLevel != null && !batteryLevel.isEmpty()) {
                lblBatteryLevel.setText("🔋 " + batteryLevel);
            } else {
                lblBatteryLevel.setText("🔋 Non disponible");
            }
            
            // Changer l'image selon la plateforme
            updateDeviceImage(device);
            
            // Mise à jour des badges de statut
            lblConnectionStatus.setText("🔌 Connecté via USB");
            lblConnectionStatus.getStyleClass().clear();
            lblConnectionStatus.getStyleClass().add("connection-badge");
            lblConnectionStatus.getStyleClass().add("connected");
            
            // Évaluation de la compatibilité
            boolean isCompatible = evaluateDeviceCompatibility(device);
            if (isCompatible) {
                lblCompatibilityStatus.setText("✅ Compatible");
                lblCompatibilityStatus.getStyleClass().clear();
                lblCompatibilityStatus.getStyleClass().add("compatibility-badge");
                lblCompatibilityStatus.getStyleClass().add("compatible");
            } else {
                lblCompatibilityStatus.setText("⚠️ Compatibilité limitée");
                lblCompatibilityStatus.getStyleClass().clear();
                lblCompatibilityStatus.getStyleClass().add("compatibility-badge");
                lblCompatibilityStatus.getStyleClass().add("limited");
            }
            
            // Affichage des opérations appropriées
            showPlatformOperations(device.getPlatform().toString());
            
            // Masquer le message "aucun appareil"
            noOperationsPane.setVisible(false);
        });
    }
    
    /**
     * Extrait la capacité de stockage pour un appareil donné
     */
    private String extractStorageCapacityForDevice(Device device) {
        if (device == null) return null;
        
        try {
            // Si l'appareil est iOS, utiliser le service de détection pour obtenir la capacité
            if (device.getPlatform() == Device.Platform.iOS) {
                // Utiliser la méthode publique pour obtenir la capacité
                return getCapacityFromModel(device.getModel());
            }
            // Pour Android, retourner une capacité générique pour l'instant
            else if (device.getPlatform() == Device.Platform.ANDROID) {
                return "Capacité Android (détection en cours)";
            }
        } catch (Exception e) {
            logger.debug("Erreur extraction capacité: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Obtient la capacité de stockage basée sur le modèle
     */
    private String getCapacityFromModel(String model) {
        if (model == null) return null;
        
        // Mapping des modèles vers les capacités
        if (model.contains("iPhone 11 Pro")) {
            return "64 GB, 256 GB ou 512 GB";
        } else if (model.contains("iPhone 12")) {
            return "64 GB, 128 GB ou 256 GB";
        } else if (model.contains("iPhone 13")) {
            return "128 GB, 256 GB ou 512 GB";
        } else if (model.contains("iPhone 14")) {
            return "128 GB, 256 GB ou 512 GB";
        } else if (model.contains("iPhone 15")) {
            return "128 GB, 256 GB ou 512 GB";
        }
        
        return "Capacité inconnue";
    }
    
    /**
     * Évalue la compatibilité de l'appareil avec les opérations disponibles
     */
    private boolean evaluateDeviceCompatibility(Device device) {
        if (device == null || device.getPlatform() == null) return false;
        
        String platform = device.getPlatform().toString().toLowerCase();
        String osVersion = device.getOsVersion();
        
        // Évaluation de compatibilité iOS
        if (platform.contains("ios")) {
            if (osVersion != null) {
                try {
                    double version = Double.parseDouble(osVersion.replaceAll("[^\\d.]", ""));
                    return version >= 10.0 && version <= 18.0; // Plage de compatibilité
                } catch (NumberFormatException e) {
                    return true; // Assumé compatible si version non parsable
                }
            }
            return true;
        }
        
        // Évaluation de compatibilité Android
        if (platform.contains("android")) {
            if (osVersion != null) {
                try {
                    int version = Integer.parseInt(osVersion.replaceAll("[^\\d]", ""));
                    return version >= 6 && version <= 14; // Android 6.0 à 14
                } catch (NumberFormatException e) {
                    return true;
                }
            }
            return true;
        }
        
        return false; // Plateforme non supportée
    }
    
    
    
    /**
     * Met à jour l'image de l'appareil selon la plateforme
     */
    private void updateDeviceImage(Device device) {
        try {
            String imagePath;
            if (device.getPlatform() == Device.Platform.iOS) {
                imagePath = "/images/devices/iphone.svg";
            } else {
                imagePath = "/images/devices/android.svg";
            }
            
            javafx.scene.image.Image image = new javafx.scene.image.Image(
                getClass().getResourceAsStream(imagePath)
            );
            deviceImageView.setImage(image);
            
        } catch (Exception e) {
            logger.warn("Impossible de charger l'image de l'appareil: {}", e.getMessage());
        }
    }
    
    private void showPlatformOperations(String platform) {
        // Masquer toutes les sections d'opérations
        iosOperationsPane.setVisible(false);
        androidOperationsPane.setVisible(false);
        
        if (platform == null) return;
        
        String platformLower = platform.toLowerCase();
        
        // Afficher les opérations iOS
        if (platformLower.contains("ios")) {
            iosOperationsPane.setVisible(true);
            
            // Sélectionner automatiquement l'opération la plus courante
            radiOSiCloudBypass.setSelected(true);
            updateOperationEstimation();
            
            System.out.println("[OperationSelector] Opérations iOS affichées");
        }
        // Afficher les opérations Android
        else if (platformLower.contains("android")) {
            androidOperationsPane.setVisible(true);
            
            // Sélectionner automatiquement l'opération la plus courante
            radioAndroidFRP.setSelected(true);
            updateOperationEstimation();
            
            System.out.println("[OperationSelector] Opérations Android affichées");
        }
        
        btnStartOperation.setDisable(false);
    }
    
    private void updateOperationEstimation() {
        RadioButton selected = (RadioButton) operationToggleGroup.getSelectedToggle();
        if (selected == null) {
            lblEstimatedTime.setText("Non calculé");
            lblSuccessRate.setText("Non disponible");
            lblRiskLevel.setText("Non évalué");
            return;
        }
        
        String operationKey = getOperationKey(selected);
        OperationConfig config = operationConfigs.get(operationKey);
        
        if (config != null) {
            lblEstimatedTime.setText(config.estimatedTime);
            lblSuccessRate.setText(config.successRate);
            
            // Mise à jour du niveau de risque avec style
            lblRiskLevel.setText(config.riskLevel);
            lblRiskLevel.getStyleClass().clear();
            lblRiskLevel.getStyleClass().add("risk-badge");
            
            switch (config.riskLevel.toLowerCase()) {
                case "faible":
                    lblRiskLevel.getStyleClass().add("risk-low");
                    break;
                case "moyen":
                    lblRiskLevel.getStyleClass().add("risk-medium");
                    break;
                case "élevé":
                    lblRiskLevel.getStyleClass().add("risk-high");
                    break;
            }
            
            // Afficher un avertissement pour les opérations avancées
            if (config.requiresAdvanced && !chkAdvancedMode.isSelected()) {
                lblRiskLevel.setText(config.riskLevel + " (Mode avancé recommandé)");
            }
        }
    }
    
    private String getOperationKey(RadioButton radioButton) {
        if (radioButton == radiOSiCloudBypass) return "ios_icloud";
        if (radioButton == radioiOSPasscodeUnlock) return "ios_passcode";
        if (radioButton == radioiOSScreenTime) return "ios_screentime";
        if (radioButton == radioiOSActivationLock) return "ios_activation";
        if (radioButton == radioAndroidFRP) return "android_frp";
        if (radioButton == radioAndroidPattern) return "android_pattern";
        if (radioButton == radioAndroidSamsung) return "android_samsung";
        if (radioButton == radioAndroidMi) return "android_mi";
        if (radioButton == radioAndroidBootloader) return "android_bootloader";
        
        return "unknown";
    }
    
    private void showNoDeviceState() {
        Platform.runLater(() -> {
            // Réinitialiser les informations d'appareil
            lblDeviceBrand.setText("Non détecté");
            lblDeviceModel.setText("Non détecté");
            lblDevicePlatform.setText("Non détectée");
            lblDeviceOS.setText("Non disponible");
            lblDeviceIMEI.setText("Non disponible");
            lblDeviceSerial.setText("Non disponible");
            lblDeviceCapacity.setText("Non disponible");
            
            // Réinitialiser les badges
            lblConnectionStatus.setText("🔌 Non connecté");
            lblConnectionStatus.getStyleClass().clear();
            lblConnectionStatus.getStyleClass().add("connection-badge");
            lblConnectionStatus.getStyleClass().add("disconnected");
            
            lblCompatibilityStatus.setText("❓ Compatibilité inconnue");
            lblCompatibilityStatus.getStyleClass().clear();
            lblCompatibilityStatus.getStyleClass().add("compatibility-badge");
            lblCompatibilityStatus.getStyleClass().add("unknown");
            
            // Masquer les opérations et afficher le message par défaut
            iosOperationsPane.setVisible(false);
            androidOperationsPane.setVisible(false);
            noOperationsPane.setVisible(true);
            
            // Désactiver le bouton de démarrage
            btnStartOperation.setDisable(true);
            
            this.currentDevice = null;
        });
    }
    
    private void updateInterfaceState() {
        updateAdvancedOptionsVisibility();
    }
    
    private void updateAdvancedOptionsVisibility() {
        // Mode expert : afficher/masquer certaines options selon le niveau utilisateur
        boolean isAdvanced = chkAdvancedMode.isSelected();
        // TODO: Implémenter le système de gestion des utilisateurs et des rôles
        boolean isExpert = false; // Temporairement désactivé
        
        chkBypassSecurity.setVisible(isAdvanced || isExpert);
    }
    
    @FXML
    private void handleStartOperation() {
        if (currentDevice == null) {
            showAlert("Erreur", "Aucun appareil détecté", 
                     "Veuillez connecter votre appareil et cliquez sur 'Actualiser l'Appareil'");
            return;
        }
        
        RadioButton selectedOperation = (RadioButton) operationToggleGroup.getSelectedToggle();
        if (selectedOperation == null) {
            showAlert("Erreur", "Aucune opération sélectionnée", 
                     "Veuillez choisir une opération à effectuer.");
            return;
        }
        
        // Confirmer l'opération
        String operationName = selectedOperation.getText();
        boolean confirmed = showConfirmation("Confirmation d'Opération", 
                                           "Démarrer l'opération: " + operationName,
                                           "Cette opération va modifier votre appareil. Êtes-vous sûr de continuer ?");
        
        if (!confirmed) return;
        
        // Lancer l'opération de déblocage
        startUnlockOperation(selectedOperation);
        
        System.out.println("[OperationSelector] Opération démarrée: " + operationName);
    }
    
    private void startUnlockOperation(RadioButton selectedOperation) {
        String operationType = getOperationKey(selectedOperation);
        OperationConfig config = operationConfigs.get(operationType);
        
        // Préparer les options
        Map<String, Boolean> options = new HashMap<>();
        options.put("preserveData", chkPreserveData.isSelected());
        options.put("advancedMode", chkAdvancedMode.isSelected());
        options.put("bypassSecurity", chkBypassSecurity.isSelected());
        options.put("createBackup", chkCreateBackup.isSelected());
        
        Task<Boolean> unlockTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Simulation de l'opération de déblocage
                updateMessage("Préparation de l'opération...");
                Thread.sleep(2000);
                
                updateMessage("Analyse de l'appareil...");
                Thread.sleep(3000);
                
                updateMessage("Exécution du déblocage...");
                Thread.sleep(5000);
                
                updateMessage("Finalisation...");
                Thread.sleep(2000);
                
                return Math.random() > 0.1; // 90% de chance de succès
            }
            
            @Override
            protected void succeeded() {
                boolean success = getValue();
                if (success) {
                    showAlert("Succès", "Opération Terminée", 
                             "L'opération de déblocage a été effectuée avec succès!");
                } else {
                    showAlert("Échec", "Opération Échouée", 
                             "L'opération n'a pas pu être terminée. Vérifiez la connexion de l'appareil.");
                }
            }
            
            @Override
            protected void failed() {
                showAlert("Erreur", "Erreur d'Opération", 
                         "Une erreur est survenue: " + getException().getMessage());
            }
        };
        
        new Thread(unlockTask).start();
    }
    
    @FXML
    private void handleAdvancedSettings() {
        showAlert("Configuration Avancée", "Paramètres Experts", 
                 "Interface de configuration avancée (à implémenter)");
        System.out.println("[OperationSelector] Configuration avancée demandée");
    }
    
    @FXML
    private void handleRefreshDevice() {
        System.out.println("[OperationSelector] Actualisation manuelle demandée");
        showNoDeviceState(); // Réinitialiser l'état
        startDeviceDetection(); // Relancer la détection
    }
    
    // Méthodes utilitaires pour les dialogues
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
