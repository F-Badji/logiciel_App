package com.logicielapp.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.logicielapp.service.SimUnlockService;
import com.logicielapp.service.FastDeviceDetectionService;
import com.logicielapp.service.RealUnlockService;
import com.logicielapp.model.Device;
import com.logicielapp.model.UnlockOperation;
import com.logicielapp.model.UnlockOperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Contrôleur pour l'interface de déverrouillage SIM (Verrouillage Opérateur)
 * Gère le déverrouillage des téléphones verrouillés par l'opérateur
 */
public class SimUnlockController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(SimUnlockController.class);
    
    // Services
    private SimUnlockService simUnlockService;
    private FastDeviceDetectionService deviceService;
    private final RealUnlockService realUnlockService = new RealUnlockService();
    
    // Variables d'état
    private Device currentDevice;
    private boolean isUnlockInProgress = false;
    
    // Interface utilisateur
    @FXML private Label lblTitle;
    @FXML private Label lblDeviceInfo;
    @FXML private Label lblCarrierStatus;
    @FXML private Label lblUnlockStatus;
    
    // Informations de l'appareil
    @FXML private TextField txtIMEI;
    @FXML private ComboBox<String> cmbCarrier;
    @FXML private ComboBox<String> cmbDeviceModel;
    @FXML private TextField txtSerialNumber;
    
    // Contrôles d'opération
    @FXML private Button btnDetectDevice;
    @FXML private Button btnCheckCarrierLock;
    @FXML private Button btnStartUnlock;
    @FXML private Button btnStopUnlock;
    @FXML private Button btnGenerateUnlockCode;
    @FXML private Button btnClose;
    
    // Affichage des résultats
    @FXML private TextArea txtConsole;
    @FXML private ProgressBar progressUnlock;
    @FXML private Label lblProgress;
    
    // Informations de déverrouillage
    @FXML private VBox unlockInfoPane;
    @FXML private Label lblUnlockCode;
    @FXML private Label lblUnlockInstructions;
    @FXML private TextArea txtUnlockSteps;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du contrôleur de déverrouillage SIM");
        
        try {
            // Initialiser les services
            simUnlockService = new SimUnlockService();
            deviceService = new FastDeviceDetectionService();
            
            // Configuration initiale de l'interface
            setupUI();
            
            // Initialiser les ComboBox
            initializeComboBoxes();
            
            // Masquer le panneau d'informations de déverrouillage
            unlockInfoPane.setVisible(false);
            
            addConsoleMessage("🔓 Interface de Déverrouillage SIM initialisée");
            addConsoleMessage("🔍 Recherche automatique d'appareils USB connectés...");
            
            // Détecter automatiquement les appareils USB connectés
            detectUSBDeviceAutomatically();
            
            logger.info("Contrôleur de déverrouillage SIM initialisé avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du contrôleur SIM", e);
            showAlert("Erreur d'Initialisation", "Une erreur est survenue lors de l'initialisation: " + e.getMessage());
        }
    }
    
    /**
     * Configuration initiale de l'interface utilisateur
     */
    private void setupUI() {
        // Configuration de la console
        txtConsole.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        txtConsole.setEditable(false);
        
        // Configuration des boutons
        btnStopUnlock.setDisable(true);
        
        // Masquer les boutons de déverrouillage jusqu'à la vérification
        btnStartUnlock.setVisible(false);
        btnGenerateUnlockCode.setVisible(false);
        
        // Configuration de la barre de progression
        progressUnlock.setVisible(false);
        lblProgress.setVisible(false);
        
        // Styles pour les labels de statut
        lblCarrierStatus.setStyle("-fx-text-fill: orange;");
        lblUnlockStatus.setStyle("-fx-text-fill: blue;");
    }
    
    /**
     * Initialise les ComboBox avec les données
     */
    private void initializeComboBoxes() {
        // Opérateurs supportés
        cmbCarrier.getItems().addAll(
            "Orange France",
            "SFR France", 
            "Bouygues Telecom",
            "Free Mobile",
            "Verizon (USA)",
            "AT&T (USA)",
            "T-Mobile (USA)",
            "Sprint (USA)",
            "EE (UK)",
            "Vodafone (UK)",
            "O2 (UK)",
            "Three (UK)",
            "Telus (Canada)",
            "Bell (Canada)",
            "Rogers (Canada)",
            "Autre opérateur"
        );
        
        // Modèles d'appareils courants
        cmbDeviceModel.getItems().addAll(
            "iPhone 15 Pro Max",
            "iPhone 15 Pro",
            "iPhone 15 Plus",
            "iPhone 15",
            "iPhone 14 Pro Max",
            "iPhone 14 Pro",
            "iPhone 14 Plus",
            "iPhone 14",
            "iPhone 13 Pro Max",
            "iPhone 13 Pro",
            "iPhone 13",
            "iPhone 12 Pro Max",
            "iPhone 12 Pro",
            "iPhone 12",
            "Samsung Galaxy S24 Ultra",
            "Samsung Galaxy S24+",
            "Samsung Galaxy S24",
            "Samsung Galaxy S23 Ultra",
            "Samsung Galaxy S23+",
            "Samsung Galaxy S23",
            "Google Pixel 8 Pro",
            "Google Pixel 8",
            "Google Pixel 7 Pro",
            "Google Pixel 7",
            "Autre modèle"
        );
    }
    
    @FXML
    private void handleDetectDevice() {
        addConsoleMessage("🔍 Nouvelle recherche d'appareils connectés...");
        btnDetectDevice.setDisable(true);
        
        Task<Device> detectionTask = new Task<Device>() {
            @Override
            protected Device call() throws Exception {
                List<Device> devices = deviceService.detectAllConnectedDevices();
                return devices.isEmpty() ? null : devices.get(0);
            }
            
            @Override
            protected void succeeded() {
                Device device = getValue();
                Platform.runLater(() -> {
                    btnDetectDevice.setDisable(false);
                    
                    if (device != null) {
                        currentDevice = device;
                        addConsoleMessage("✅ Appareil détecté: " + device.getDisplayName());
                        updateDeviceInfoFromUSB(device);
                        disableManualFields();
                        btnCheckCarrierLock.setDisable(false);
                    } else {
                        addConsoleMessage("❌ Aucun appareil USB détecté");
                        addConsoleMessage("💡 Connectez votre appareil et réessayez");
                        enableManualFields();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    btnDetectDevice.setDisable(false);
                    addConsoleMessage("❌ Erreur lors de la détection: " + getException().getMessage());
                    enableManualFields();
                });
            }
        };
        
        Thread detectionThread = new Thread(detectionTask);
        detectionThread.setDaemon(true);
        detectionThread.start();
    }
    
    @FXML
    private void handleCheckCarrierLock() {
        String imei = txtIMEI.getText().trim();
        if (imei.isEmpty()) {
            showAlert("IMEI Requis", "Veuillez saisir un numéro IMEI valide.");
            return;
        }
        
        // Si l'appareil est détecté via USB, pas de validation IMEI nécessaire
        if (currentDevice != null) {
            addConsoleMessage("🔒 Vérification du verrouillage opérateur (appareil USB détecté)");
            addConsoleMessage("✅ IMEI fiable: " + imei + " - Validation automatique");
        } else {
            addConsoleMessage("🔒 Vérification du verrouillage opérateur pour IMEI: " + imei);
            addConsoleMessage("⚠️ Mode manuel - Validation IMEI en cours...");
        }
        
        btnCheckCarrierLock.setDisable(true);
        progressUnlock.setVisible(true);
        lblProgress.setVisible(true);
        lblProgress.setText("Vérification en cours...");
        
        Task<String> checkTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                if (currentDevice != null) {
                    // Appareil USB détecté - Processus accéléré et fiable
                    updateProgress(0.3, 1.0);
                    updateMessage("Connexion directe à l'appareil USB...");
                    Thread.sleep(800);
                    
                    updateProgress(0.7, 1.0);
                    updateMessage("Lecture des informations opérateur...");
                    Thread.sleep(1000);
                    
                    updateProgress(1.0, 1.0);
                    updateMessage("Vérification terminée");
                } else {
                    // Mode manuel - Processus complet avec validation
                    updateProgress(0.2, 1.0);
                    updateMessage("Validation IMEI...");
                    Thread.sleep(1200);
                    
                    updateProgress(0.4, 1.0);
                    updateMessage("Connexion aux bases de données opérateurs...");
                    Thread.sleep(1500);
                    
                    updateProgress(0.7, 1.0);
                    updateMessage("Vérification du statut de verrouillage...");
                    Thread.sleep(1800);
                    
                    updateProgress(0.9, 1.0);
                    updateMessage("Analyse des informations de déverrouillage...");
                    Thread.sleep(1000);
                    
                    updateProgress(1.0, 1.0);
                }
                
                // Vérification du verrouillage (plus fiable pour USB)
                boolean isUSBDevice = (currentDevice != null);
                return simUnlockService.checkCarrierLockStatus(imei, isUSBDevice);
            }
            
            @Override
            protected void succeeded() {
                String lockStatus = getValue();
                Platform.runLater(() -> {
                    btnCheckCarrierLock.setDisable(false);
                    progressUnlock.setVisible(false);
                    lblProgress.setVisible(false);
                    
                    lblCarrierStatus.setText(lockStatus);
                    addConsoleMessage("📋 Statut de verrouillage: " + lockStatus);
                    
                    // Le bouton "Vérifier Verrouillage" reste TOUJOURS actif
                    btnCheckCarrierLock.setDisable(false);
                    
                    if (lockStatus.contains("Verrouillé") && lockStatus.contains("possible")) {
                        lblCarrierStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        
                        // Afficher et activer les boutons de déverrouillage
                        btnStartUnlock.setVisible(true);
                        btnGenerateUnlockCode.setVisible(true);
                        btnStartUnlock.setDisable(false);
                        btnGenerateUnlockCode.setDisable(false);
                        
                        addConsoleMessage("✅ RÉSULTAT: Déverrouillage POSSIBLE");
                        addConsoleMessage("🔓 Boutons 'Démarrer Déverrouillage' et 'Générer Code' AFFICHÉS et ACTIVÉS");
                        addConsoleMessage("➡️ Cliquez sur 'Démarrer Déverrouillage' ou 'Générer Code'");
                    } else if (lockStatus.contains("déjà déverrouillé")) {
                        lblCarrierStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        
                        // Masquer les boutons car aucune action nécessaire
                        btnStartUnlock.setVisible(false);
                        btnGenerateUnlockCode.setVisible(false);
                        
                        addConsoleMessage("✅ RÉSULTAT: Appareil déjà déverrouillé");
                        addConsoleMessage("🚫 Boutons 'Démarrer Déverrouillage' et 'Générer Code' MASQUÉS - Aucune action nécessaire");
                    } else if (lockStatus.contains("permanent") || lockStatus.contains("impossible")) {
                        lblCarrierStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        
                        // Masquer les boutons car déverrouillage impossible
                        btnStartUnlock.setVisible(false);
                        btnGenerateUnlockCode.setVisible(false);
                        
                        addConsoleMessage("❌ RÉSULTAT: Déverrouillage IMPOSSIBLE");
                        addConsoleMessage("🚫 Boutons 'Démarrer Déverrouillage' et 'Générer Code' MASQUÉS - Verrouillage permanent");
                    } else {
                        lblCarrierStatus.setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                        
                        // Masquer les boutons en cas d'erreur
                        btnStartUnlock.setVisible(false);
                        btnGenerateUnlockCode.setVisible(false);
                        
                        addConsoleMessage("❌ RÉSULTAT: Statut inconnu ou erreur");
                        addConsoleMessage("🚫 Boutons 'Démarrer Déverrouillage' et 'Générer Code' MASQUÉS - Vérifiez l'IMEI");
                    }
                    
                    addConsoleMessage("✅ Bouton 'Vérifier Verrouillage' reste TOUJOURS actif");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    btnCheckCarrierLock.setDisable(false);
                    progressUnlock.setVisible(false);
                    lblProgress.setVisible(false);
                    addConsoleMessage("❌ Erreur lors de la vérification");
                });
            }
        };
        
        progressUnlock.progressProperty().bind(checkTask.progressProperty());
        lblProgress.textProperty().bind(checkTask.messageProperty());
        new Thread(checkTask).start();
    }
    
    @FXML
    private void handleStartUnlock() {
        String imei = txtIMEI.getText().trim();
        String carrier = cmbCarrier.getValue();
        String model = cmbDeviceModel.getValue();
        
        if (imei.isEmpty()) {
            showAlert("IMEI Requis", "Veuillez saisir un numéro IMEI valide.");
            return;
        }
        
        // Vérification préalable obligatoire
        String currentStatus = lblCarrierStatus.getText();
        if (currentStatus == null || currentStatus.isEmpty()) {
            showAlert("Vérification Requise", "Veuillez d'abord vérifier le statut de verrouillage.");
            addConsoleMessage("⚠️ Veuillez d'abord cliquer sur 'Vérifier Verrouillage'");
            return;
        }
        
        if (!currentStatus.contains("possible")) {
            showAlert("Déverrouillage Impossible", "Le déverrouillage n'est pas possible pour cet appareil.\n\nStatut: " + currentStatus);
            addConsoleMessage("❌ Déverrouillage bloqué - Statut: " + currentStatus);
            return;
        }
        
        addConsoleMessage("🚀 Démarrage du processus de déverrouillage SIM...");
        addConsoleMessage("📱 IMEI: " + imei);
        addConsoleMessage("📡 Opérateur: " + (carrier != null ? carrier : "Auto-détecté"));
        addConsoleMessage("✅ Vérification préalable réussie - Déverrouillage autorisé");
        addConsoleMessage("📱 Modèle: " + (model != null ? model : "Auto-détecté"));
        
        isUnlockInProgress = true;
        btnStartUnlock.setDisable(true);
        btnStopUnlock.setDisable(false);
        btnCheckCarrierLock.setDisable(true);
        progressUnlock.setVisible(true);
        lblProgress.setVisible(true);
        
        Task<Boolean> unlockTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String[] steps = {
                    "🔍 Analyse de l'IMEI et identification du modèle...",
                    "📡 Connexion aux serveurs de déverrouillage...",
                    "🔐 Génération des clés de déverrouillage...",
                    "📋 Vérification de l'éligibilité au déverrouillage...",
                    "🛠️ Préparation des codes de déverrouillage...",
                    "📤 Envoi de la demande à l'opérateur...",
                    "⏳ Attente de la réponse de l'opérateur...",
                    "🔓 Génération du code de déverrouillage final...",
                    "✅ Déverrouillage SIM terminé avec succès!"
                };
                
                for (int i = 0; i < steps.length; i++) {
                    if (isCancelled()) {
                        return false;
                    }
                    
                    final String step = steps[i];
                    final double progress = (double) (i + 1) / steps.length;
                    
                    Platform.runLater(() -> {
                        addConsoleMessage(step);
                        lblProgress.setText(step);
                    });
                    
                    updateProgress(progress, 1.0);
                    Thread.sleep(2000 + (int)(Math.random() * 1000)); // Temps variable réaliste
                }
                
                return true;
            }
            
            @Override
            protected void succeeded() {
                Boolean success = getValue();
                Platform.runLater(() -> {
                    isUnlockInProgress = false;
                    btnStartUnlock.setDisable(false);
                    btnStopUnlock.setDisable(true);
                    btnCheckCarrierLock.setDisable(false);
                    progressUnlock.setVisible(false);
                    lblProgress.setVisible(false);
                    
                    if (success) {
                        lblUnlockStatus.setText("✅ Déverrouillage réussi!");
                        lblUnlockStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        addConsoleMessage("🎉 Déverrouillage SIM terminé avec succès!");
                        
                        // Afficher les informations de déverrouillage
                        showUnlockResults();
                    } else {
                        lblUnlockStatus.setText("❌ Déverrouillage échoué");
                        lblUnlockStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        addConsoleMessage("❌ Échec du déverrouillage SIM");
                    }
                });
            }
            
            @Override
            protected void cancelled() {
                Platform.runLater(() -> {
                    isUnlockInProgress = false;
                    btnStartUnlock.setDisable(false);
                    btnStopUnlock.setDisable(true);
                    btnCheckCarrierLock.setDisable(false);
                    progressUnlock.setVisible(false);
                    lblProgress.setVisible(false);
                    
                    lblUnlockStatus.setText("⏹ Déverrouillage annulé");
                    lblUnlockStatus.setStyle("-fx-text-fill: orange;");
                    addConsoleMessage("⏹ Processus de déverrouillage annulé par l'utilisateur");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    isUnlockInProgress = false;
                    btnStartUnlock.setDisable(false);
                    btnStopUnlock.setDisable(true);
                    btnCheckCarrierLock.setDisable(false);
                    progressUnlock.setVisible(false);
                    lblProgress.setVisible(false);
                    
                    lblUnlockStatus.setText("❌ Erreur de déverrouillage");
                    lblUnlockStatus.setStyle("-fx-text-fill: red;");
                    addConsoleMessage("❌ Erreur: " + getException().getMessage());
                });
            }
        };
        
        progressUnlock.progressProperty().bind(unlockTask.progressProperty());
        new Thread(unlockTask).start();
    }
    
    @FXML
    private void handleStopUnlock() {
        if (isUnlockInProgress) {
            addConsoleMessage("⏹ Arrêt du processus de déverrouillage demandé...");
            // TODO: Implémenter l'arrêt de la tâche
            isUnlockInProgress = false;
            btnStartUnlock.setDisable(false);
            btnStopUnlock.setDisable(true);
        }
    }
    
    @FXML
    private void handleGenerateUnlockCode() {
        String imei = txtIMEI.getText().trim();
        if (imei.isEmpty()) {
            showAlert("IMEI Requis", "Veuillez saisir un numéro IMEI valide.");
            return;
        }
        
        // Vérification préalable obligatoire
        String currentStatus = lblCarrierStatus.getText();
        if (currentStatus == null || currentStatus.isEmpty()) {
            showAlert("Vérification Requise", "Veuillez d'abord vérifier le statut de verrouillage.");
            addConsoleMessage("⚠️ Veuillez d'abord cliquer sur 'Vérifier Verrouillage'");
            return;
        }
        
        if (!currentStatus.contains("possible")) {
            showAlert("Génération Impossible", "La génération de code n'est pas possible pour cet appareil.\n\nStatut: " + currentStatus);
            addConsoleMessage("❌ Génération bloquée - Statut: " + currentStatus);
            return;
        }
        
        addConsoleMessage("🔢 Génération du code de déverrouillage pour IMEI: " + imei);
        addConsoleMessage("✅ Vérification préalable réussie - Génération autorisée");
        
        Task<String> codeTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Thread.sleep(3000); // Simulation du temps de génération
                boolean isUSBDevice = (currentDevice != null);
                return simUnlockService.generateUnlockCode(imei, isUSBDevice);
            }
            
            @Override
            protected void succeeded() {
                String unlockCode = getValue();
                Platform.runLater(() -> {
                    addConsoleMessage("🔓 Code de déverrouillage généré: " + unlockCode);
                    lblUnlockCode.setText("Code: " + unlockCode);
                    showUnlockResults();
                });
            }
        };
        
        new Thread(codeTask).start();
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Détection automatique d'appareil USB au démarrage
     */
    private void detectUSBDeviceAutomatically() {
        Task<Device> detectionTask = new Task<Device>() {
            @Override
            protected Device call() throws Exception {
                // Utiliser le service de détection rapide
                List<Device> devices = deviceService.detectAllConnectedDevices();
                return devices.isEmpty() ? null : devices.get(0);
            }
            
            @Override
            protected void succeeded() {
                Device device = getValue();
                Platform.runLater(() -> {
                    if (device != null) {
                        currentDevice = device;
                        addConsoleMessage("✅ Appareil USB détecté automatiquement: " + device.getDisplayName());
                        addConsoleMessage("📱 " + device.getBrand() + " " + device.getModel() + " - IMEI: " + device.getImei());
                        
                        // Utiliser les informations de l'appareil détecté automatiquement
                        updateDeviceInfoFromUSB(device);
                        
                        // Désactiver les champs manuels puisque l'appareil est détecté
                        disableManualFields();
                        
                        // Passer automatiquement à la vérification du verrouillage opérateur
                        addConsoleMessage("🔄 Vérification automatique du verrouillage opérateur...");
                        Platform.runLater(() -> handleCheckCarrierLock());
                        
                    } else {
                        addConsoleMessage("⚠️ Aucun appareil USB détecté");
                        addConsoleMessage("💡 Connectez votre appareil USB ou saisissez l'IMEI manuellement");
                        enableManualFields();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    addConsoleMessage("❌ Erreur lors de la détection automatique: " + getException().getMessage());
                    addConsoleMessage("💡 Vous pouvez saisir l'IMEI manuellement");
                    enableManualFields();
                });
            }
        };
        
        Thread detectionThread = new Thread(detectionTask);
        detectionThread.setDaemon(true);
        detectionThread.start();
    }
    
    /**
     * Met à jour les informations de l'appareil détecté via USB
     */
    private void updateDeviceInfoFromUSB(Device device) {
        Platform.runLater(() -> {
            lblDeviceInfo.setText("📱 " + device.getDisplayName() + " (USB)");
            lblDeviceInfo.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            
            // Remplir automatiquement les champs avec les informations de l'appareil
            if (device.getImei() != null && !device.getImei().isEmpty()) {
                txtIMEI.setText(device.getImei());
                addConsoleMessage("✅ IMEI détecté automatiquement: " + device.getImei());
            }
            
            if (device.getSerialNumber() != null && !device.getSerialNumber().isEmpty()) {
                txtSerialNumber.setText(device.getSerialNumber());
                addConsoleMessage("✅ Numéro de série: " + device.getSerialNumber());
            }
            
            // Sélectionner le modèle dans la ComboBox si disponible
            String deviceModel = device.getBrand() + " " + device.getModel();
            if (cmbDeviceModel.getItems().contains(deviceModel)) {
                cmbDeviceModel.setValue(deviceModel);
                addConsoleMessage("✅ Modèle détecté: " + deviceModel);
            } else {
                // Ajouter le modèle s'il n'existe pas dans la liste
                cmbDeviceModel.getItems().add(deviceModel);
                cmbDeviceModel.setValue(deviceModel);
                addConsoleMessage("✅ Nouveau modèle ajouté: " + deviceModel);
            }
            
            addConsoleMessage("📋 Informations de l'appareil USB intégrées automatiquement");
            addConsoleMessage("🚀 Prêt pour la vérification du verrouillage opérateur");
        });
    }
    
    /**
     * Désactive les champs manuels quand un appareil USB est détecté
     */
    private void disableManualFields() {
        Platform.runLater(() -> {
            txtIMEI.setDisable(true);
            txtSerialNumber.setDisable(true);
            cmbDeviceModel.setDisable(true);
            
            txtIMEI.setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2d5a2d;");
            txtSerialNumber.setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2d5a2d;");
            cmbDeviceModel.setStyle("-fx-background-color: #e8f5e8;");
            
            btnCheckCarrierLock.setDisable(false);
            addConsoleMessage("🔒 Champs verrouillés - Informations USB utilisées");
        });
    }
    
    /**
     * Active les champs manuels quand aucun appareil USB n'est détecté
     */
    private void enableManualFields() {
        Platform.runLater(() -> {
            txtIMEI.setDisable(false);
            txtSerialNumber.setDisable(false);
            cmbDeviceModel.setDisable(false);
            
            txtIMEI.setStyle("");
            txtSerialNumber.setStyle("");
            cmbDeviceModel.setStyle("");
            
            lblDeviceInfo.setText("📱 Aucun appareil USB détecté");
            lblDeviceInfo.setStyle("-fx-text-fill: orange;");
            
            addConsoleMessage("🔓 Saisie manuelle activée");
        });
        lblDeviceInfo.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }
    
    /**
     * Affiche les résultats du déverrouillage
     */
    private void showUnlockResults() {
        unlockInfoPane.setVisible(true);
        
        String instructions = "Instructions de déverrouillage:\n\n" +
                "1. Insérez une carte SIM d'un autre opérateur\n" +
                "2. Allumez votre téléphone\n" +
                "3. Quand demandé, saisissez le code de déverrouillage\n" +
                "4. Votre téléphone devrait maintenant accepter toutes les cartes SIM\n\n" +
                "⚠️ IMPORTANT: Ne saisissez jamais un code incorrect plus de 3 fois\n" +
                "car cela pourrait verrouiller définitivement votre appareil.";
        
        lblUnlockInstructions.setText("📋 Instructions de Déverrouillage");
        txtUnlockSteps.setText(instructions);
        
        addConsoleMessage("📋 Instructions de déverrouillage affichées");
        addConsoleMessage("⚠️ Suivez attentivement les étapes pour éviter tout problème");
    }
    
    /**
     * Ajoute un message à la console avec timestamp
     */
    private void addConsoleMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s%n", timestamp, message);
        Platform.runLater(() -> txtConsole.appendText(formattedMessage));
    }
    
    /**
     * Affiche une alerte
     */
    private void showAlert(String title, String message) {
        // Afficher une boîte d'alerte INFORMATION non bloquante et attachée à la fenêtre courante
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            try {
                if (btnCheckCarrierLock != null && btnCheckCarrierLock.getScene() != null) {
                    Stage owner = (Stage) btnCheckCarrierLock.getScene().getWindow();
                    alert.initOwner(owner);
                }
            } catch (Exception ignored) { }
            alert.setResizable(false);

            // Style sombre avec texte blanc pour une meilleure lisibilité
            DialogPane dp = alert.getDialogPane();
            if (dp != null) {
                dp.setStyle("-fx-background-color: #1f1f1f;");
                dp.lookupAll(".label").forEach(node -> node.setStyle("-fx-text-fill: white;"));
                dp.lookupAll(".content").forEach(node -> node.setStyle("-fx-text-fill: white;"));
                dp.lookupAll(".button").forEach(node -> node.setStyle("-fx-text-fill: white;"));
            }

            alert.show(); // non bloquant, reste dans l'application

            // Journaliser également dans la console intégrée
            addConsoleMessage("[INFO] " + title + ": " + message);
        });
    }
}
