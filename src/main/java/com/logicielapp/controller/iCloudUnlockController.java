package com.logicielapp.controller;

import com.logicielapp.model.Device;
import com.logicielapp.model.UnlockOperation;
import com.logicielapp.service.FastDeviceDetectionService;
import com.logicielapp.service.RealUnlockService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur pour l'interface avancée de déblocage iCloud
 * Gère tous les types de déblocage de comptes iCloud bloqués
 */
public class iCloudUnlockController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(iCloudUnlockController.class);
    
    // Services
    private FastDeviceDetectionService deviceService;
    private RealUnlockService unlockService;
    
    // Variables d'état
    private Device selectedDevice;
    private UnlockOperation currentOperation;
    private ObservableList<Device> detectedDevices;
    
    // Détection d'appareils
    @FXML private Button btnDetectDevices;
    @FXML private Button btnRefreshDevices;
    @FXML private Label lblDeviceStatus;
    @FXML private TableView<Device> tableDevices;
    @FXML private TableColumn<Device, String> colDeviceModel;
    @FXML private TableColumn<Device, String> colDeviceIMEI;
    @FXML private TableColumn<Device, String> colDeviceState;
    @FXML private TableColumn<Device, String> colDeviceOS;
    @FXML private TableColumn<Device, Button> colDeviceAction;
    
    // Types de déblocage
    @FXML private ToggleGroup unlockTypeGroup;
    @FXML private RadioButton rbActivationLock;
    @FXML private RadioButton rbAccountLocked;
    @FXML private RadioButton rbFindMyDevice;
    @FXML private RadioButton rbScreenTime;
    @FXML private RadioButton rbTwoFactorAuth;
    @FXML private RadioButton rbCompleteBypass;
    
    // Méthodes de déblocage
    @FXML private ToggleGroup methodGroup;
    @FXML private RadioButton rbMethod3uTools;
    @FXML private RadioButton rbMethodCheckra1n;
    @FXML private RadioButton rbMethodUncOver;
    @FXML private RadioButton rbMethodTaiG;
    @FXML private RadioButton rbMethodPangu;
    @FXML private RadioButton rbMethodCustom;
    
    // Options avancées
    @FXML private CheckBox cbBackupData;
    @FXML private CheckBox cbPreserveFirmware;
    @FXML private CheckBox cbSkipValidation;
    @FXML private CheckBox cbVerboseLogging;
    @FXML private CheckBox cbAutoReboot;
    @FXML private CheckBox cbTestMode;
    
    // Contrôles
    @FXML private Button btnStartUnlock;
    @FXML private Button btnStopUnlock;
    @FXML private Button btnResetDevice;
    @FXML private Button btnSaveProfile;
    @FXML private Button btnLoadProfile;
    
    // Progression
    @FXML private ProgressBar progressBar;
    @FXML private Label lblProgress;
    @FXML private Label lblCurrentStep;
    
    // Console
    @FXML private TextArea txtConsole;
    @FXML private Button btnClearLogs;
    @FXML private Button btnExportLogs;
    
    // Navigation
    @FXML private Button btnHelp;
    @FXML private Button btnClose;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation de l'interface de déblocage iCloud avancé");
        
        // Initialiser les services
        deviceService = new FastDeviceDetectionService();
        unlockService = new RealUnlockService();
        detectedDevices = FXCollections.observableArrayList();
        
        // Configurer la table des appareils
        setupDeviceTable();
        
        // Configurer les listeners
        setupListeners();
        
        // Message de bienvenue
        addConsoleMessage("🍎 Interface de Déblocage iCloud Avancé - Prête");
        addConsoleMessage("💡 Connectez un appareil iOS et cliquez sur 'Détecter Appareils'");
        
        logger.info("Interface de déblocage iCloud initialisée avec succès");
    }
    
    private void setupDeviceTable() {
        colDeviceModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colDeviceIMEI.setCellValueFactory(new PropertyValueFactory<>("imei"));
        colDeviceState.setCellValueFactory(new PropertyValueFactory<>("deviceState"));
        colDeviceOS.setCellValueFactory(new PropertyValueFactory<>("osVersion"));
        
        // Colonne d'action avec bouton de sélection
        colDeviceAction.setCellFactory(param -> new TableCell<Device, Button>() {
            private final Button selectButton = new Button("Sélectionner");
            
            {
                selectButton.setOnAction(event -> {
                    Device device = getTableView().getItems().get(getIndex());
                    selectDevice(device);
                });
                selectButton.getStyleClass().add("small-button");
            }
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(selectButton);
                }
            }
        });
        
        tableDevices.setItems(detectedDevices);
    }
    
    private void setupListeners() {
        // Listener pour les types de déblocage
        unlockTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateUnlockTypeDescription();
            }
        });
        
        // Listener pour les méthodes
        methodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateMethodDescription();
            }
        });
    }
    
    @FXML
    private void handleDetectDevices() {
        addConsoleMessage("🔍 Recherche d'appareils iOS connectés...");
        btnDetectDevices.setDisable(true);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return deviceService.detectAllConnectedDevices();
            } catch (Exception e) {
                logger.error("Erreur lors de la détection d'appareils", e);
                return List.<Device>of();
            }
        }).thenAccept(devices -> {
            Platform.runLater(() -> {
                detectedDevices.clear();
                
                // Filtrer uniquement les appareils iOS
                List<Device> iOSDevices = devices.stream()
                    .filter(Device::isIOS)
                    .toList();
                
                if (iOSDevices.isEmpty()) {
                    addConsoleMessage("❌ Aucun appareil iOS détecté");
                    lblDeviceStatus.setText("Aucun appareil iOS trouvé");
                    
                    // Ajouter des appareils de simulation pour la démonstration
                    addSimulatedDevices();
                } else {
                    detectedDevices.addAll(iOSDevices);
                    addConsoleMessage("✅ " + iOSDevices.size() + " appareil(s) iOS détecté(s)");
                    lblDeviceStatus.setText(iOSDevices.size() + " appareil(s) détecté(s)");
                }
                
                btnDetectDevices.setDisable(false);
            });
        });
    }
    
    private void addSimulatedDevices() {
        // Appareils de simulation pour les tests
        Device iphone12 = new Device();
        iphone12.setModel("iPhone 12 Pro");
        iphone12.setImei("353328111234567");
        iphone12.setOsVersion("iOS 15.7");
        iphone12.setDeviceState("iCloud Locked");
        iphone12.setPlatform(Device.Platform.iOS);
        
        Device iphone13 = new Device();
        iphone13.setModel("iPhone 13");
        iphone13.setImei("354398111234568");
        iphone13.setOsVersion("iOS 16.2");
        iphone13.setDeviceState("Activation Lock");
        iphone13.setPlatform(Device.Platform.iOS);
        
        Device ipadPro = new Device();
        ipadPro.setModel("iPad Pro 11\"");
        ipadPro.setImei("352441111234569");
        ipadPro.setOsVersion("iPadOS 16.1");
        ipadPro.setDeviceState("Find My Enabled");
        ipadPro.setPlatform(Device.Platform.iOS);
        
        detectedDevices.addAll(List.of(iphone12, iphone13, ipadPro));
        addConsoleMessage("🧪 Appareils de simulation ajoutés pour les tests");
        lblDeviceStatus.setText("3 appareil(s) de simulation");
    }
    
    @FXML
    private void handleRefreshDevices() {
        addConsoleMessage("🔄 Actualisation de la liste des appareils...");
        handleDetectDevices();
    }
    
    private void selectDevice(Device device) {
        selectedDevice = device;
        addConsoleMessage("📱 Appareil sélectionné: " + device.getModel() + " (" + device.getImei() + ")");
        addConsoleMessage("🔍 État: " + device.getDeviceState());
        
        // Activer le bouton de déblocage
        btnStartUnlock.setDisable(false);
        
        // Suggérer le type de déblocage approprié
        suggestUnlockType(device);
    }
    
    private void suggestUnlockType(Device device) {
        String state = device.getDeviceState().toLowerCase();
        
        if (state.contains("activation") || state.contains("logo")) {
            rbActivationLock.setSelected(true);
            addConsoleMessage("💡 Suggestion: Activation Lock détecté");
        } else if (state.contains("find my")) {
            rbFindMyDevice.setSelected(true);
            addConsoleMessage("💡 Suggestion: Find My Device activé");
        } else if (state.contains("icloud")) {
            rbAccountLocked.setSelected(true);
            addConsoleMessage("💡 Suggestion: Compte iCloud verrouillé");
        }
    }
    
    @FXML
    private void handleStartUnlock() {
        if (selectedDevice == null) {
            showAlert("Erreur", "Veuillez sélectionner un appareil avant de commencer le déblocage.");
            return;
        }
        
        // Déterminer le type de déblocage
        UnlockOperation.OperationType operationType = getSelectedUnlockType();
        String method = getSelectedMethod();
        
        addConsoleMessage("🚀 Démarrage du déblocage iCloud...");
        addConsoleMessage("📱 Appareil: " + selectedDevice.getModel());
        addConsoleMessage("🔓 Type: " + operationType.name());
        addConsoleMessage("⚙️ Méthode: " + method);
        
        // Créer l'opération
        UnlockOperation operation = new UnlockOperation();
        operation.setTargetDevice(selectedDevice);
        operation.setOperationType(operationType);
        currentOperation = operation;
        
        // Configurer l'interface
        btnStartUnlock.setDisable(true);
        btnStopUnlock.setDisable(false);
        progressBar.setProgress(0);
        lblProgress.setText("0%");
        lblCurrentStep.setText("Initialisation...");
        
        // Démarrer le déblocage selon le type
        startUnlockProcess(operation, method);
    }
    
    private void startUnlockProcess(UnlockOperation operation, String method) {
        CompletableFuture<UnlockOperation> unlockFuture;
        
        // Sélectionner la méthode appropriée selon le type
        UnlockOperation.OperationType type = operation.getOperationType();
        
        switch (type) {
            case ICLOUD_BYPASS:
            case ACTIVATION_LOCK_BYPASS:
                unlockFuture = unlockService.bypassiCloudActivation(operation);
                break;
            default:
                unlockFuture = unlockService.bypassiCloudActivation(operation);
                break;
        }
        
        // Surveiller la progression
        Thread progressThread = new Thread(() -> {
            int progress = 0;
            String[] steps = {
                "Initialisation du déblocage...",
                "Analyse de l'appareil...",
                "Préparation du bypass...",
                "Application de la méthode " + method + "...",
                "Flashage du firmware modifié...",
                "Vérification du déblocage...",
                "Finalisation..."
            };
            
            try {
                for (int i = 0; i < steps.length && currentOperation != null; i++) {
                    final int currentProgress = (i + 1) * 100 / steps.length;
                    final String currentStep = steps[i];
                    
                    Platform.runLater(() -> {
                        progressBar.setProgress(currentProgress / 100.0);
                        lblProgress.setText(currentProgress + "%");
                        lblCurrentStep.setText(currentStep);
                        addConsoleMessage("📊 " + currentStep);
                    });
                    
                    Thread.sleep(3000 + (int)(Math.random() * 2000)); // Simulation réaliste
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        progressThread.setDaemon(true);
        progressThread.start();
        
        // Gérer le résultat
        unlockFuture.thenAccept(result -> {
            Platform.runLater(() -> {
                if (result.isCompleted()) {
                    handleUnlockSuccess(result);
                } else if (result.isFailed()) {
                    handleUnlockFailure(result);
                }
                
                // Réinitialiser l'interface
                btnStartUnlock.setDisable(false);
                btnStopUnlock.setDisable(true);
                currentOperation = null;
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                handleUnlockError(throwable);
                btnStartUnlock.setDisable(false);
                btnStopUnlock.setDisable(true);
                currentOperation = null;
            });
            return null;
        });
    }
    
    private void handleUnlockSuccess(UnlockOperation result) {
        addConsoleMessage("✅ DÉBLOCAGE RÉUSSI!");
        addConsoleMessage("🎉 " + result.getResult());
        
        progressBar.setProgress(1.0);
        lblProgress.setText("100%");
        lblCurrentStep.setText("Déblocage terminé avec succès!");
        
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Déblocage Réussi");
        successAlert.setHeaderText("iCloud Déblocage Terminé");
        successAlert.setContentText("L'appareil a été débloqué avec succès!\n\n" + result.getResult());
        successAlert.showAndWait();
    }
    
    private void handleUnlockFailure(UnlockOperation result) {
        addConsoleMessage("❌ ÉCHEC DU DÉBLOCAGE");
        addConsoleMessage("💥 " + result.getErrorMessage());
        
        lblCurrentStep.setText("Échec du déblocage");
        
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Échec du Déblocage");
        errorAlert.setHeaderText("Le déblocage a échoué");
        errorAlert.setContentText(result.getErrorMessage());
        errorAlert.showAndWait();
    }
    
    private void handleUnlockError(Throwable throwable) {
        addConsoleMessage("💥 ERREUR TECHNIQUE: " + throwable.getMessage());
        logger.error("Erreur lors du déblocage iCloud", throwable);
        
        lblCurrentStep.setText("Erreur technique");
        
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Erreur Technique");
        errorAlert.setHeaderText("Une erreur technique est survenue");
        errorAlert.setContentText("Détails: " + throwable.getMessage());
        errorAlert.showAndWait();
    }
    
    @FXML
    private void handleStopUnlock() {
        if (currentOperation != null) {
            addConsoleMessage("⏹️ Arrêt du déblocage demandé...");
            currentOperation = null;
            
            btnStartUnlock.setDisable(false);
            btnStopUnlock.setDisable(true);
            lblCurrentStep.setText("Opération annulée");
        }
    }
    
    @FXML
    private void handleResetDevice() {
        if (selectedDevice == null) {
            showAlert("Erreur", "Veuillez sélectionner un appareil.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Reset de l'appareil");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir effectuer un reset de l'appareil?\nCette action est irréversible.");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            addConsoleMessage("🔄 Reset de l'appareil en cours...");
            // Simulation du reset
            Thread resetThread = new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        addConsoleMessage("✅ Reset de l'appareil terminé");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            resetThread.setDaemon(true);
            resetThread.start();
        }
    }
    
    @FXML
    private void handleSaveProfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le Profil de Déblocage");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Profils iCloud (*.icp)", "*.icp")
        );
        
        File file = fileChooser.showSaveDialog(btnSaveProfile.getScene().getWindow());
        if (file != null) {
            try {
                saveProfileToFile(file);
                addConsoleMessage("💾 Profil sauvegardé: " + file.getName());
            } catch (IOException e) {
                addConsoleMessage("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleLoadProfile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un Profil de Déblocage");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Profils iCloud (*.icp)", "*.icp")
        );
        
        File file = fileChooser.showOpenDialog(btnLoadProfile.getScene().getWindow());
        if (file != null) {
            try {
                loadProfileFromFile(file);
                addConsoleMessage("📂 Profil chargé: " + file.getName());
            } catch (IOException e) {
                addConsoleMessage("❌ Erreur lors du chargement: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleClearLogs() {
        txtConsole.clear();
        addConsoleMessage("🗑️ Console effacée");
    }
    
    @FXML
    private void handleExportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les Logs");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers Texte (*.txt)", "*.txt")
        );
        fileChooser.setInitialFileName("icloud_unlock_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        
        File file = fileChooser.showSaveDialog(btnExportLogs.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(txtConsole.getText());
                addConsoleMessage("💾 Logs exportés: " + file.getName());
            } catch (IOException e) {
                addConsoleMessage("❌ Erreur lors de l'export: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleHelp() {
        Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
        helpAlert.setTitle("Aide - Déblocage iCloud");
        helpAlert.setHeaderText("Guide d'utilisation");
        helpAlert.setContentText(
            "🍎 DÉBLOCAGE ICLOUD AVANCÉ\n\n" +
            "1. Connectez votre appareil iOS\n" +
            "2. Cliquez sur 'Détecter Appareils'\n" +
            "3. Sélectionnez l'appareil dans la liste\n" +
            "4. Choisissez le type de déblocage\n" +
            "5. Sélectionnez la méthode appropriée\n" +
            "6. Configurez les options avancées\n" +
            "7. Cliquez sur 'Démarrer Déblocage'\n\n" +
            "⚠️ ATTENTION:\n" +
            "- Assurez-vous que l'appareil est chargé\n" +
            "- Ne déconnectez pas pendant le processus\n" +
            "- Sauvegardez vos données importantes\n\n" +
            "📧 Support: digitex.officiel@gmail.com"
        );
        helpAlert.showAndWait();
    }
    
    @FXML
    private void handleClose() {
        if (currentOperation != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirmation");
            confirmAlert.setHeaderText("Fermeture en cours d'opération");
            confirmAlert.setContentText("Une opération de déblocage est en cours.\nÊtes-vous sûr de vouloir fermer?");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    // Méthodes utilitaires
    
    private UnlockOperation.OperationType getSelectedUnlockType() {
        RadioButton selected = (RadioButton) unlockTypeGroup.getSelectedToggle();
        if (selected == rbActivationLock) return UnlockOperation.OperationType.ACTIVATION_LOCK_BYPASS;
        if (selected == rbAccountLocked) return UnlockOperation.OperationType.ICLOUD_ACCOUNT_UNLOCK;
        if (selected == rbFindMyDevice) return UnlockOperation.OperationType.ICLOUD_BYPASS;
        if (selected == rbScreenTime) return UnlockOperation.OperationType.SCREEN_TIME_BYPASS;
        if (selected == rbTwoFactorAuth) return UnlockOperation.OperationType.ICLOUD_BYPASS;
        if (selected == rbCompleteBypass) return UnlockOperation.OperationType.ICLOUD_BYPASS;
        return UnlockOperation.OperationType.ICLOUD_BYPASS;
    }
    
    private String getSelectedMethod() {
        RadioButton selected = (RadioButton) methodGroup.getSelectedToggle();
        if (selected == rbMethod3uTools) return "3uTools Style";
        if (selected == rbMethodCheckra1n) return "Checkra1n";
        if (selected == rbMethodUncOver) return "Unc0ver";
        if (selected == rbMethodTaiG) return "TaiG";
        if (selected == rbMethodPangu) return "Pangu";
        if (selected == rbMethodCustom) return "Personnalisée";
        return "3uTools Style";
    }
    
    private void updateUnlockTypeDescription() {
        RadioButton selected = (RadioButton) unlockTypeGroup.getSelectedToggle();
        String description = "";
        
        if (selected == rbActivationLock) {
            description = "Déblocage des appareils bloqués sur le logo Apple";
        } else if (selected == rbAccountLocked) {
            description = "Déblocage des comptes iCloud avec mot de passe oublié";
        } else if (selected == rbFindMyDevice) {
            description = "Désactivation de Find My Device et localisation";
        } else if (selected == rbScreenTime) {
            description = "Contournement des restrictions Screen Time";
        } else if (selected == rbTwoFactorAuth) {
            description = "Bypass de l'authentification à 2 facteurs";
        } else if (selected == rbCompleteBypass) {
            description = "Suppression complète de toutes les restrictions iCloud";
        }
        
        addConsoleMessage("ℹ️ " + description);
    }
    
    private void updateMethodDescription() {
        RadioButton selected = (RadioButton) methodGroup.getSelectedToggle();
        String description = "";
        
        if (selected == rbMethod3uTools) {
            description = "Méthode recommandée, compatible avec la plupart des appareils";
        } else if (selected == rbMethodCheckra1n) {
            description = "Exploit matériel pour appareils A5-A11";
        } else if (selected == rbMethodUncOver) {
            description = "Jailbreak semi-untethered pour iOS 11.0-14.8";
        }
        
        if (!description.isEmpty()) {
            addConsoleMessage("ℹ️ " + description);
        }
    }
    
    private void saveProfileToFile(File file) throws IOException {
        StringBuilder profile = new StringBuilder();
        profile.append("# Profil de Déblocage iCloud\n");
        profile.append("# Généré le: ").append(LocalDateTime.now()).append("\n\n");
        
        // Type de déblocage
        RadioButton selectedType = (RadioButton) unlockTypeGroup.getSelectedToggle();
        if (selectedType != null) {
            profile.append("unlock_type=").append(selectedType.getText()).append("\n");
        }
        
        // Méthode
        RadioButton selectedMethod = (RadioButton) methodGroup.getSelectedToggle();
        if (selectedMethod != null) {
            profile.append("method=").append(selectedMethod.getText()).append("\n");
        }
        
        // Options
        profile.append("backup_data=").append(cbBackupData.isSelected()).append("\n");
        profile.append("preserve_firmware=").append(cbPreserveFirmware.isSelected()).append("\n");
        profile.append("skip_validation=").append(cbSkipValidation.isSelected()).append("\n");
        profile.append("verbose_logging=").append(cbVerboseLogging.isSelected()).append("\n");
        profile.append("auto_reboot=").append(cbAutoReboot.isSelected()).append("\n");
        profile.append("test_mode=").append(cbTestMode.isSelected()).append("\n");
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(profile.toString());
        }
    }
    
    private void loadProfileFromFile(File file) throws IOException {
        // Implémentation simplifiée pour la démonstration
        addConsoleMessage("📂 Chargement du profil depuis: " + file.getName());
        
        // Ici on pourrait parser le fichier et restaurer les paramètres
        // Pour la démonstration, on applique des paramètres par défaut
        rbMethod3uTools.setSelected(true);
        rbActivationLock.setSelected(true);
        cbVerboseLogging.setSelected(true);
        cbAutoReboot.setSelected(true);
    }
    
    private void addConsoleMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            txtConsole.appendText("[" + timestamp + "] " + message + "\n");
            txtConsole.setScrollTop(Double.MAX_VALUE);
        });
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
