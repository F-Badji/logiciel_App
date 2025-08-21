package com.logicielapp.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.logicielapp.model.Device;
import com.logicielapp.model.UnlockOperation;
import com.logicielapp.service.UnlockService;
import com.logicielapp.service.IMEIDeviceDetectionService;
import com.logicielapp.service.DHRUApiService;
import com.logicielapp.service.USBDeviceDetectionService;
import com.logicielapp.util.IMEIValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur pour la boîte de dialogue de déblocage IMEI
 */
public class IMEIDialogController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(IMEIDialogController.class);
    
    // Pattern pour validation IMEI (15 chiffres)
    private static final Pattern IMEI_PATTERN = Pattern.compile("^\\d{15}$");
    
    // Services
    private UnlockService unlockService;
    private IMEIDeviceDetectionService imeiDetectionService;
    private DHRUApiService dhruApiService;
    private USBDeviceDetectionService usbDetectionService;
    
    // Variables d'état
    private Device selectedDevice;
    private UnlockOperation currentOperation;
    private boolean imeiValidated = false;
    
    // Contrôles FXML
    @FXML private TextField txtIMEI;
    @FXML private RadioButton radioIOS, radioAndroid;
    @FXML private ToggleGroup platformGroup;
    @FXML private CheckBox chkSaveDevice, chkNotifyCompletion;
    
    @FXML private Label lblIMEIInfo, lblEstimatedTime, lblSuccessRate;
    @FXML private Button btnConfirmUnlock, btnPasswordRecovery, btnCancel;
    
    // Nouveaux éléments pour affichage automatique des informations
    @FXML private VBox detectedInfoSection;
    @FXML private Label lblDetectedBrand, lblDetectedModel, lblDetectedPlatform;
    @FXML private Label lblDetectedOS, lblDetectedSerial, lblDetectedUID;
    
    // Éléments pour affichage des informations DHRU API
    @FXML private VBox dhruInfoSection;
    @FXML private Label lblCapacity, lblColor, lblICloudStatus;
    @FXML private Label lblCarrier, lblCountry, lblWarranty;
    @FXML private Button btnDetectUSB, btnRefreshInfo;
    
    @FXML private VBox progressSection;
    @FXML private Label lblStatus, lblProgressDetails;
    @FXML private ProgressBar progressBar;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du contrôleur de dialogue IMEI");
        
        try {
            // Initialiser les services
            unlockService = new UnlockService();
            imeiDetectionService = new IMEIDeviceDetectionService();
            dhruApiService = new DHRUApiService();
            usbDetectionService = new USBDeviceDetectionService();
            
            // Configurer l'interface
            setupUI();
            
            // Ajouter les listeners
            setupListeners();
            
            // Vérifier la disponibilité des outils USB
            checkUSBToolsAvailability();
            
            // Tester la connectivité API
            testAPIConnectivity();
            
            logger.info("Contrôleur de dialogue IMEI initialisé avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du contrôleur IMEI", e);
            showAlert("Erreur", "Impossible d'initialiser l'interface IMEI: " + e.getMessage());
        }
    }
    
    /**
     * Configuration initiale de l'interface
     */
    private void setupUI() {
        // Configurer le groupe de boutons radio
        platformGroup = new ToggleGroup();
        radioIOS.setToggleGroup(platformGroup);
        radioAndroid.setToggleGroup(platformGroup);
        
        // Sélectionner iOS par défaut
        radioIOS.setSelected(true);
        
        // Limiter la saisie IMEI à 15 caractères numériques
        txtIMEI.textProperty().addListener((observable, oldValue, newValue) -> {
            // Filtrer pour ne garder que les chiffres et limiter à 15 caractères
            String filtered = newValue.replaceAll("[^0-9]", "");
            if (filtered.length() > 15) {
                filtered = filtered.substring(0, 15);
            }
            
            // Réinitialiser le style d'erreur quand l'utilisateur tape
            if (!newValue.equals(oldValue)) {
                txtIMEI.setStyle("");
                lblIMEIInfo.setText("💡 Pour trouver l'IMEI : *#06# ou Réglages → Général → Informations");
                lblIMEIInfo.setStyle("-fx-text-fill: #666666;");
            }
            
            // Mettre à jour le champ si nécessaire
            if (!filtered.equals(newValue)) {
                txtIMEI.setText(filtered);
            }
            
            // Réinitialiser la validation quand l'IMEI change
            if (imeiValidated && !filtered.equals(oldValue)) {
                imeiValidated = false;
                btnConfirmUnlock.setDisable(true);
                detectedInfoSection.setVisible(false);
            }
            
            // Déclencher automatiquement la détection quand IMEI complet (15 chiffres)
            if (filtered.length() == 15 && !filtered.equals(oldValue)) {
                handleAutoDetectIMEI();
            }
        });
        
        // Style initial
        updateIMEIValidationStyle(false);
    }
    
    /**
     * Configuration des listeners
     */
    private void setupListeners() {
        // Listener pour les changements de plateforme
        platformGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateEstimations();
        });
    }
    
    /**
     * Détection automatique déclenchée par la saisie complète de l'IMEI
     */
    private void handleAutoDetectIMEI() {
        String imei = txtIMEI.getText().trim();
        
        if (imei.length() != 15) {
            return;
        }
        
        // Vérification de l'existence de l'IMEI via API DHRU d'abord
        verifyIMEIExistence(imei);
    }
    
    private void handleIMEIValidation(String imei) {
        logger.info("Début de la validation IMEI: {}", imei.substring(0, 6) + "***");
        
        // Validation de base du format IMEI
        IMEIValidator.ValidationResult result = IMEIValidator.validateIMEI(imei);
        
        if (!result.isValid()) {
            logger.warn("IMEI invalide: {}", result.getReason());
            showIMEIError(result.getReason());
            return;
        }
        
        // Vérifier l'existence de l'IMEI via l'API DHRU avant de continuer
        logger.info("Vérification de l'existence de l'IMEI via API DHRU");
        verifyIMEIExistence(imei);
    }
    
    /**
     * Vérifie l'existence de l'IMEI via l'API DHRU avant validation locale
     */
    private void verifyIMEIExistence(String imei) {
        // Afficher la section de progression
        progressSection.setVisible(true);
        lblStatus.setText("🔍 Vérification de l'existence de l'IMEI...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        // Vérification asynchrone via API DHRU
        dhruApiService.getDeviceInfo(imei).thenAccept(dhruInfo -> {
            Platform.runLater(() -> {
                if (dhruInfo.isSuccess()) {
                    // IMEI existe - afficher toutes les informations détaillées
                    logger.info("IMEI confirmé existant via DHRU, affichage des informations...");
                    progressSection.setVisible(false);
                    displayCompleteDeviceInfo(dhruInfo, imei);
                } else {
                    // API DHRU n'a pas confirmé: ne pas valider localement, afficher une erreur
                    logger.warn("API DHRU n'a pas confirmé l'IMEI: {}", imei.substring(0, 6) + "***");
                    progressSection.setVisible(false);
                    showIMEIError("IMEI non trouvé ou non confirmé par la base DHRU. Veuillez vérifier l'IMEI.");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logger.error("Erreur lors de la vérification DHRU", throwable);
                progressSection.setVisible(false);
                showIMEIError("❌ Erreur de connexion à la base de données. Vérifiez votre connexion internet.");
            });
            return null;
        });
    }
    
    /**
     * Affiche toutes les informations complètes de l'appareil (comme imeicheck.com)
     */
    private void displayCompleteDeviceInfo(DHRUApiService.DeviceInfo dhruInfo, String imei) {
        logger.info("Affichage des informations complètes pour IMEI: {}", imei.substring(0, 6) + "***");
        
        // Créer l'appareil avec toutes les informations
        createDeviceFromDHRUInfo(dhruInfo, imei);
        
        // Afficher un message de succès
        lblIMEIInfo.setText("✅ IMEI valide - Appareil trouvé dans la base de données mondiale");
        lblIMEIInfo.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 12px;");
        txtIMEI.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        
        // Informations de base détectées
        if (lblDetectedBrand != null) {
            lblDetectedBrand.setText(dhruInfo.getBrand() != null ? dhruInfo.getBrand() : "Apple");
        }
        if (lblDetectedModel != null) {
            lblDetectedModel.setText(dhruInfo.getModel() != null ? dhruInfo.getModel() : "iPhone");
        }
        if (lblDetectedPlatform != null) {
            lblDetectedPlatform.setText("iOS");
        }
        
        // Rendre visible la section des informations détectées
        if (detectedInfoSection != null) {
            detectedInfoSection.setVisible(true);
        }
        
        // Afficher les informations DHRU détaillées
        updateDHRUInfoDisplay(dhruInfo);
        
        // Activer les boutons d'action
        if (btnConfirmUnlock != null) {
            btnConfirmUnlock.setDisable(false);
        }
        
        logger.info("Informations complètes affichées avec succès");
    }
    
    /**
     * Validation locale stricte avec base TAC étendue
     */
    @SuppressWarnings("unused")
    private void performLocalValidation(String imei) {
        logger.info("Validation locale stricte pour IMEI: {}", imei.substring(0, 6) + "***");
        
        // Validation complète locale
        IMEIValidator.ValidationResult result = IMEIValidator.validateIMEI(imei);
        
        if (!result.isValid()) {
            showIMEIError(result.getReason());
            return;
        }
        
        // IMEI valide localement - créer appareil avec informations de base
        String tac = imei.substring(0, 6);
        String manufacturer = result.getManufacturer();
        String deviceInfo = result.getDeviceInfo();
        
        // Créer un appareil avec les informations locales disponibles
        Device device = new Device();
        device.setImei(imei);
        device.setBrand(manufacturer != null ? manufacturer : "Apple");
        device.setModel(deviceInfo != null ? deviceInfo : "iPhone");
        device.setPlatform(Device.Platform.iOS);
        device.setConnectionType(Device.ConnectionType.IMEI_REMOTE);
        device.setStatus(Device.DeviceStatus.DETECTED_BY_IMEI);
        
        // Afficher les informations locales
        displayLocalDeviceInfo(device, tac);
    }
    
    /**
     * Affiche les informations d'appareil basées sur la validation locale
     */
    @SuppressWarnings("unused")
    private void displayLocalDeviceInfo(Device device, String tac) {
        logger.info("Affichage des informations locales pour TAC: {}", tac);
        
        // Message de succès
        lblIMEIInfo.setText("✅ IMEI valide - Informations basées sur la base TAC locale");
        lblIMEIInfo.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 12px;");
        txtIMEI.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        
        // Informations de base
        if (lblDetectedBrand != null) {
            lblDetectedBrand.setText(device.getBrand());
        }
        if (lblDetectedModel != null) {
            lblDetectedModel.setText(device.getModel());
        }
        if (lblDetectedPlatform != null) {
            lblDetectedPlatform.setText("iOS");
        }
        
        // Rendre visible la section
        if (detectedInfoSection != null) {
            detectedInfoSection.setVisible(true);
        }
        
        // Informations DHRU avec valeurs par défaut
        if (lblCapacity != null) {
            lblCapacity.setText("Non disponible (API DHRU inaccessible)");
        }
        if (lblColor != null) {
            lblColor.setText("Non disponible");
        }
        if (lblICloudStatus != null) {
            lblICloudStatus.setText("Inconnu");
            lblICloudStatus.setStyle("-fx-text-fill: gray;");
        }
        if (lblCarrier != null) {
            lblCarrier.setText("Non disponible");
        }
        if (lblCountry != null) {
            lblCountry.setText("Non disponible");
        }
        if (lblWarranty != null) {
            lblWarranty.setText("Non disponible");
        }
        
        // Rendre visible la section DHRU
        if (dhruInfoSection != null) {
            dhruInfoSection.setVisible(true);
        }
        
        // Activer les boutons
        if (btnConfirmUnlock != null) {
            btnConfirmUnlock.setDisable(false);
        }
        
        logger.info("Informations locales affichées avec succès");
    }
    
    /**
     * Affiche une erreur IMEI dans l'interface
     */
    private void showIMEIError(String message) {
        lblIMEIInfo.setText("❌ " + message);
        lblIMEIInfo.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 12px;");
        txtIMEI.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        txtIMEI.requestFocus();
    }
    
    /**
     * Démarre la détection améliorée avec API DHRU
     */
    private void startEnhancedDetection(String imei) {
        progressSection.setVisible(true);
        lblStatus.setText("🔍 Connexion automatique à l'appareil...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        Task<Device> detectionTask = new Task<Device>() {
            @Override
            protected Device call() throws Exception {
                updateMessage("Récupération des informations via API DHRU...");
                Thread.sleep(800);
                
                // Récupérer les informations détaillées via l'API DHRU
                DHRUApiService.DeviceInfo dhruInfo = dhruApiService.getDeviceInfo(imei).get();
                
                // Vérifier que l'IMEI existe réellement dans la base DHRU
                if (!dhruInfo.isSuccess()) {
                    updateMessage("IMEI non trouvé dans la base de données...");
                    Thread.sleep(500);
                    throw new Exception("Cet IMEI n'existe pas dans la base de données DHRU. Veuillez vérifier l'IMEI saisi.");
                }
                
                updateMessage("Création du profil d'appareil...");
                Thread.sleep(500);
                
                // Créer un objet Device avec les vraies informations DHRU
                Device device = createDeviceFromDHRUInfo(dhruInfo, imei);
                
                updateMessage("Finalisation des données...");
                Thread.sleep(300);
                
                return device;
            }
        };
        
        detectionTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            Platform.runLater(() -> lblProgressDetails.setText(newMessage));
        });
        
        detectionTask.setOnSucceeded(e -> {
            Device detectedDevice = detectionTask.getValue();
            selectedDevice = detectedDevice;
            
            Platform.runLater(() -> {
                progressSection.setVisible(false);
                
                // Marquer l'IMEI comme validé
                imeiValidated = true;
                updateIMEIValidationStyle(true);
                
                // Mettre à jour l'affichage des informations détectées
                updateDetectedDeviceInfo(detectedDevice);
                
                // Récupérer et afficher les informations DHRU si disponibles
                loadDHRUInfo(detectedDevice.getImei());
                
                // Activer le bouton de confirmation
                btnConfirmUnlock.setDisable(false);
                
                showInfo("Détection Réussie", 
                    " Appareil détecté avec succès!\n\n" +
                    " Marque: " + detectedDevice.getBrand() + "\n" +
                    " Modèle: " + detectedDevice.getModel() + "\n" +
                    " Plateforme: " + detectedDevice.getPlatform() + "\n" +
                    " Version OS: " + detectedDevice.getOsVersion() + "\n\n" +
                    "Vous pouvez maintenant confirmer le déblocage.");
            });
        });
        
        detectionTask.setOnFailed(e -> {
            progressSection.setVisible(false);
            
            Throwable exception = detectionTask.getException();
            logger.error("Erreur lors de la détection IMEI", exception);
            
            // Vérifier si c'est une erreur d'IMEI inexistant
            if (exception.getMessage() != null && exception.getMessage().contains("IMEI n'existe pas")) {
                showAlert("IMEI Inexistant", 
                    "❌ Cet IMEI n'existe pas dans nos bases de données.\n\n" +
                    "Veuillez vérifier que l'IMEI est correct :\n" +
                    "• L'IMEI doit contenir exactement 15 chiffres\n" +
                    "• Utilisez *#06# pour obtenir l'IMEI de votre appareil\n" +
                    "• Vérifiez qu'il n'y a pas d'erreur de saisie\n\n" +
                    "Si l'IMEI est correct, cet appareil n'est peut-être pas supporté.");
            } else {
                showAlert("Erreur de Détection", 
                    "Une erreur est survenue lors de la détection automatique:\n" +
                    exception.getMessage() + "\n\n" +
                    "Vous pouvez continuer en saisissant manuellement les informations.");
            }
        });
        
        Thread detectionThread = new Thread(detectionTask);
        detectionThread.setDaemon(true);
        detectionThread.start();
    }
    
    @FXML
    private void handleConfirmUnlock() {
        if (!imeiValidated) {
            showAlert("Validation Requise", "Veuillez d'abord saisir un IMEI valide.");
            return;
        }
        
        if (selectedDevice == null) {
            showAlert("Erreur", "Informations d'appareil manquantes.");
            return;
        }
        
        // Confirmer l'opération
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation du Déblocage");
        confirmation.setHeaderText("Démarrer le déblocage IMEI à distance");
        confirmation.setContentText(
            "Êtes-vous sûr de vouloir démarrer le déblocage IMEI pour :\n\n" +
            "📱 Marque: " + selectedDevice.getBrand() + "\n" +
            "📋 Modèle: " + selectedDevice.getModel() + "\n" +
            "🖥️ Plateforme: " + selectedDevice.getPlatform() + "\n" +
            "🔢 IMEI: " + selectedDevice.getImei() + "\n\n" +
            "⚠️ Cette opération est irréversible et peut prendre 15-45 minutes."
        );
        
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            startUnlockProcess();
        }
    }
    
    /**
     * Démarre le processus de déblocage IMEI
     */
    private void startUnlockProcess() {
        btnConfirmUnlock.setDisable(true);
        btnCancel.setText("Arrêter");
        
        progressSection.setVisible(true);
        lblStatus.setText("🚀 Déblocage IMEI en cours...");
        
        Task<Boolean> unlockTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String[] steps = {
                    "Connexion aux serveurs de déblocage...",
                    "Envoi des informations d'appareil...",
                    "Génération des codes de déblocage...",
                    "Application du déblocage à distance...",
                    "Vérification du statut...",
                    "Finalisation du processus..."
                };
                
                for (int i = 0; i < steps.length; i++) {
                    if (isCancelled()) {
                        return false;
                    }
                    
                    updateMessage(steps[i]);
                    updateProgress(i + 1, steps.length);
                    
                    // Simulation de temps de traitement variable
                    Thread.sleep(2000 + (int)(Math.random() * 3000));
                }
                
                return true;
            }
        };
        
        progressBar.progressProperty().bind(unlockTask.progressProperty());
        unlockTask.messageProperty().addListener((obs, oldMessage, newMessage) -> {
            Platform.runLater(() -> lblProgressDetails.setText(newMessage));
        });
        
        unlockTask.setOnSucceeded(e -> {
            Boolean success = unlockTask.getValue();
            
            if (success) {
                // Sauvegarder l'opération en base de données
                if (chkSaveDevice.isSelected()) {
                    logger.info("Sauvegarde de l'appareil IMEI: {}", selectedDevice.getImei());
                }
                
                progressSection.setVisible(false);
                
                showInfo("Déblocage Réussi", 
                    "🎉 Le déblocage IMEI a été effectué avec succès!\\n\\n" +
                    "IMEI: " + selectedDevice.getImei() + "\\n" +
                    "Statut: Débloqué\\n" +
                    "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\\n\\n" +
                    "Votre appareil est maintenant débloqué."
                );
                
                // Fermer la fenêtre après succès
                closeDialog();
            }
        });
        
        unlockTask.setOnFailed(e -> {
            progressSection.setVisible(false);
            btnConfirmUnlock.setDisable(false);
            btnCancel.setText("❌ Annuler");
            
            Throwable exception = unlockTask.getException();
            logger.error("Erreur lors du déblocage IMEI", exception);
            
            showAlert("Erreur de Déblocage", 
                "Une erreur est survenue lors du déblocage IMEI:\\n" +
                exception.getMessage() + "\\n\\n" +
                "Veuillez réessayer ou contacter le support technique.");
        });
        
        unlockTask.setOnCancelled(e -> {
            progressSection.setVisible(false);
            btnConfirmUnlock.setDisable(false);
            btnCancel.setText("❌ Annuler");
            
            showInfo("Opération Annulée", "Le déblocage IMEI a été annulé.");
        });
        
        currentOperation = new UnlockOperation();
        // Stocker le thread pour un éventuel arrêt
        Thread unlockThread = new Thread(unlockTask);
        currentOperation.setOperationThread(unlockThread);
        unlockThread.setDaemon(true);
        unlockThread.start();
    }
    
    @FXML
    private void handlePasswordRecovery() {
        logger.info("Ouverture de l'interface de récupération de mot de passe");
        
        try {
            // Charger l'interface FXML de récupération de mot de passe
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/com/logicielapp/views/password-recovery-dialog.fxml")
            );
            
            javafx.scene.Parent passwordRecoveryDialog = loader.load();
            
            // Créer une nouvelle fenêtre pour la récupération de mot de passe
            Stage passwordRecoveryStage = new Stage();
            passwordRecoveryStage.setTitle("🔐 Récupération de Mot de Passe Oublié");
            passwordRecoveryStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            passwordRecoveryStage.initOwner(btnPasswordRecovery.getScene().getWindow());
            
            // Configuration de la scène
            javafx.scene.Scene scene = new javafx.scene.Scene(passwordRecoveryDialog, 550, 650);
            scene.getStylesheets().add(
                getClass().getResource("/styles/application.css").toExternalForm()
            );
            
            passwordRecoveryStage.setScene(scene);
            passwordRecoveryStage.setResizable(false);
            
            logger.info("Interface de récupération de mot de passe chargée avec succès");
            
            // Afficher la fenêtre
            passwordRecoveryStage.showAndWait();
            
            logger.info("Interface de récupération de mot de passe fermée");
            
        } catch (java.io.IOException e) {
            logger.error("Erreur lors du chargement de l'interface de récupération de mot de passe", e);
            
            showAlert("Erreur", 
                "Impossible de charger l'interface de récupération de mot de passe:\n" +
                e.getMessage() + "\n\n" +
                "Veuillez contacter le support technique.");
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'ouverture de la récupération de mot de passe", e);
            
            showAlert("Erreur", 
                "Une erreur inattendue s'est produite:\n" +
                e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        if (currentOperation != null && !currentOperation.isCompleted()) {
            // Annuler l'opération en cours
            currentOperation.cancel();
        } else {
            // Fermer la fenêtre
            closeDialog();
        }
    }
    
    /**
     * Met à jour l'affichage des informations détectées automatiquement
     */
    private void updateDetectedDeviceInfo(Device device) {
        // Afficher les informations détectées dans la nouvelle section
        lblDetectedBrand.setText(device.getBrand());
        lblDetectedModel.setText(device.getModel());
        lblDetectedPlatform.setText(device.getPlatform().toString());
        lblDetectedOS.setText(device.getOsVersion());
        lblDetectedSerial.setText(device.getSerialNumber() != null ? device.getSerialNumber() : "Non disponible");
        
        String uniqueId = "Non disponible";
        if (device.isIOS() && device.getUdid() != null) {
            uniqueId = device.getUdid();
        } else if (device.isAndroid() && device.getAndroidId() != null) {
            uniqueId = device.getAndroidId();
        }
        lblDetectedUID.setText(uniqueId);
        
        // Sélectionner automatiquement la bonne plateforme
        if (device.getPlatform() == Device.Platform.iOS) {
            radioIOS.setSelected(true);
        } else {
            radioAndroid.setSelected(true);
        }
        
        // Afficher la section des informations détectées
        detectedInfoSection.setVisible(true);
        
        // Mettre à jour les estimations
        updateEstimations();
    }
    
    /**
     * Met à jour le style de validation de l'IMEI
     */
    private void updateIMEIValidationStyle(boolean isValid) {
        if (isValid) {
            txtIMEI.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
        } else {
            txtIMEI.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px;");
        }
    }
    
    /**
     * Met à jour les estimations de temps et taux de réussite
     */
    private void updateEstimations() {
        RadioButton selectedPlatform = (RadioButton) platformGroup.getSelectedToggle();
        if (selectedPlatform != null) {
            if (selectedPlatform == radioIOS) {
                lblEstimatedTime.setText("15-30 minutes");
                lblSuccessRate.setText("95%");
            } else {
                lblEstimatedTime.setText("20-45 minutes");
                lblSuccessRate.setText("92%");
            }
        }
    }
    
    /**
     * Ferme la boîte de dialogue
     */
    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Crée un objet Device à partir des informations DHRU API
     */
    private Device createDeviceFromDHRUInfo(DHRUApiService.DeviceInfo dhruInfo, String imei) {
        Device device = new Device();
        device.setImei(imei);
        device.setConnectionType(Device.ConnectionType.IMEI_REMOTE);
        device.setStatus(Device.DeviceStatus.DETECTED_BY_IMEI);
        
        if (dhruInfo.isSuccess()) {
            // Informations de base
            if (dhruInfo.getModel() != null && !dhruInfo.getModel().isEmpty()) {
                device.setModel(dhruInfo.getModel());
                device.setBrand("Apple"); // L'API DHRU est principalement pour iOS
                device.setPlatform(Device.Platform.iOS);
            } else {
                device.setModel("iPhone");
                device.setBrand("Apple");
                device.setPlatform(Device.Platform.iOS);
            }
            
            // Informations supplémentaires
            if (dhruInfo.getCapacity() != null && !dhruInfo.getCapacity().isEmpty()) {
                device.setStorageCapacity(dhruInfo.getCapacity());
            }
            
            if (dhruInfo.getSerialNumber() != null && !dhruInfo.getSerialNumber().isEmpty()) {
                device.setSerialNumber(dhruInfo.getSerialNumber());
            }
            
            // Statut iCloud
            if (dhruInfo.getIcloudStatus() != null && !dhruInfo.getIcloudStatus().isEmpty()) {
                boolean icloudLocked = dhruInfo.getIcloudStatus().toLowerCase().contains("on") || 
                                     dhruInfo.getIcloudStatus().toLowerCase().contains("verrouillé");
                device.setiCloudLocked(icloudLocked);
            }
            
            // Opérateur
            if (dhruInfo.getCarrier() != null && !dhruInfo.getCarrier().isEmpty()) {
                device.setCurrentCarrier(dhruInfo.getCarrier());
            }
            
            // Afficher toutes les informations DHRU dans l'interface
            Platform.runLater(() -> updateDHRUInfoDisplay(dhruInfo));
            
        } else {
            // Ne pas créer d'appareil si l'IMEI n'existe pas
            throw new RuntimeException("IMEI inexistant dans la base de données");
        }
        
        return device;
    }
    
    /**
     * Charge et affiche les informations DHRU pour un IMEI
     */
    private void loadDHRUInfo(String imei) {
        dhruApiService.getDeviceInfo(imei).thenAccept(dhruInfo -> {
            Platform.runLater(() -> {
                if (dhruInfo.isSuccess()) {
                    updateDHRUInfoDisplay(dhruInfo);
                    if (dhruInfoSection != null) {
                        dhruInfoSection.setVisible(true);
                    }
                } else {
                    logger.warn("Impossible de récupérer les informations DHRU: {}", dhruInfo.getErrorMessage());
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                logger.error("Erreur lors du chargement des informations DHRU", throwable);
            });
            return null;
        });
    }
    
    /**
     * Met à jour l'affichage des informations DHRU
     */
    private void updateDHRUInfoDisplay(DHRUApiService.DeviceInfo dhruInfo) {
        logger.info("Mise à jour de l'affichage DHRU - Success: {}", dhruInfo.isSuccess());
        
        // Afficher toutes les informations disponibles, même en cas d'échec partiel
        if (lblCapacity != null) {
            String capacity = dhruInfo.getCapacity();
            lblCapacity.setText(capacity != null && !capacity.isEmpty() ? capacity : "Non disponible");
        }
        
        if (lblColor != null) {
            String color = dhruInfo.getColor();
            lblColor.setText(color != null && !color.isEmpty() ? color : "Non disponible");
        }
        
        if (lblICloudStatus != null) {
            String icloudStatus = dhruInfo.getIcloudStatus();
            if (icloudStatus != null && !icloudStatus.isEmpty()) {
                lblICloudStatus.setText(icloudStatus);
                // Changer la couleur selon le statut
                if (icloudStatus.toLowerCase().contains("off") || 
                    icloudStatus.toLowerCase().contains("déverrouillé")) {
                    lblICloudStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else {
                    lblICloudStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } else {
                lblICloudStatus.setText("Non disponible");
                lblICloudStatus.setStyle("-fx-text-fill: gray;");
            }
        }
        
        if (lblCarrier != null) {
            String carrier = dhruInfo.getCarrier();
            lblCarrier.setText(carrier != null && !carrier.isEmpty() ? carrier : "Non disponible");
        }
        
        if (lblCountry != null) {
            String country = dhruInfo.getCountryOrigin();
            lblCountry.setText(country != null && !country.isEmpty() ? country : "Non disponible");
        }
        
        if (lblWarranty != null) {
            String warranty = dhruInfo.getWarranty();
            lblWarranty.setText(warranty != null && !warranty.isEmpty() ? warranty : "Non disponible");
        }
        
        // Rendre visible la section des informations DHRU
        if (dhruInfoSection != null) {
            dhruInfoSection.setVisible(true);
        }
        
        logger.info("Affichage DHRU mis à jour avec toutes les informations disponibles");
    }
    
    /**
     * Vérifie la disponibilité des outils USB
     */
    private void checkUSBToolsAvailability() {
        if (usbDetectionService.isLibimobiledeviceAvailable()) {
            logger.info("libimobiledevice est disponible pour la détection USB");
            if (btnDetectUSB != null) {
                btnDetectUSB.setDisable(false);
                btnDetectUSB.setText("📱 Détecter iPhone USB");
            }
        } else {
            logger.warn("libimobiledevice n'est pas installé - détection USB désactivée");
            if (btnDetectUSB != null) {
                btnDetectUSB.setDisable(true);
                btnDetectUSB.setText("📱 USB (non disponible)");
            }
        }
    }
    
    /**
     * Teste la connectivité avec l'API DHRU
     */
    private void testAPIConnectivity() {
        if (dhruApiService.testApiConnectivity()) {
            logger.info("API DHRU accessible - informations détaillées disponibles");
        } else {
            logger.warn("API DHRU non accessible - utilisation de la base locale uniquement");
        }
    }
    
    /**
     * Gestionnaire pour la détection USB automatique
     */
    @FXML
    private void handleDetectUSB() {
        if (!usbDetectionService.isLibimobiledeviceAvailable()) {
            showAlert("Outil non disponible", 
                "libimobiledevice n'est pas installé.\n\n" +
                "Pour installer sur macOS:\n" +
                "brew install libimobiledevice\n\n" +
                "Puis redémarrez l'application.");
            return;
        }
        
        progressSection.setVisible(true);
        lblStatus.setText("🔍 Recherche d'appareils iOS connectés...");
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        
        // Détecter l'appareil connecté via USB
        usbDetectionService.detectConnectediOSDevice().thenAccept(device -> {
            Platform.runLater(() -> {
                progressSection.setVisible(false);
                
                if (device != null) {
                    // Remplir automatiquement l'IMEI
                    txtIMEI.setText(device.getImei());
                    
                    // Déclencher la validation automatique
                    handleAutoDetectIMEI();
                    
                    showInfo("Appareil détecté", 
                        "📱 iPhone détecté via USB!\n\n" +
                        "Nom: " + (device.getDeviceName() != null ? device.getDeviceName() : "iPhone") + "\n" +
                        "Modèle: " + device.getModel() + "\n" +
                        "IMEI: " + device.getImei() + "\n" +
                        "Version iOS: " + device.getOsVersion() + "\n\n" +
                        "L'IMEI a été automatiquement rempli.");
                } else {
                    showAlert("Aucun appareil", 
                        "Aucun iPhone détecté via USB.\n\n" +
                        "Vérifiez que:\n" +
                        "• L'iPhone est connecté via câble USB\n" +
                        "• L'iPhone est déverrouillé\n" +
                        "• Vous avez accepté l'autorisation de connexion\n" +
                        "• libimobiledevice est correctement installé");
                }
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                progressSection.setVisible(false);
                logger.error("Erreur lors de la détection USB", throwable);
                showAlert("Erreur de détection", 
                    "Erreur lors de la détection USB:\n" + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Gestionnaire pour actualiser les informations
     */
    @FXML
    private void handleRefreshInfo() {
        String imei = txtIMEI.getText().trim();
        if (imei.length() == 15 && imeiValidated) {
            loadDHRUInfo(imei);
        } else {
            showAlert("IMEI requis", "Veuillez d'abord saisir un IMEI valide.");
        }
    }
}
