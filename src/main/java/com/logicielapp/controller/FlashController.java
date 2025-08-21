package com.logicielapp.controller;

import com.logicielapp.model.Device;
import com.logicielapp.model.UnlockOperation;
import com.logicielapp.service.RealUnlockService;
import com.logicielapp.service.RealDeviceConnectionService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'interface de flashage de firmware
 */
public class FlashController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(FlashController.class);
    
    // Services
    private RealUnlockService unlockService;
    private RealDeviceConnectionService deviceService;
    
    // Contrôles FXML
    @FXML private ComboBox<Device> deviceComboBox;
    @FXML private ComboBox<String> flashTypeComboBox;
    @FXML private TextField firmwarePathField;
    @FXML private TextField partitionNameField;
    @FXML private Button browseFirmwareBtn;
    @FXML private Button detectDevicesBtn;
    @FXML private Button startFlashBtn;
    @FXML private Button cancelBtn;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private TextArea logArea;
    
    private UnlockOperation currentOperation;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        setupUI();
        detectConnectedDevices();
    }
    
    private void initializeServices() {
        unlockService = new RealUnlockService();
        deviceService = new RealDeviceConnectionService();
    }
    
    private void setupUI() {
        // Configuration du ComboBox des types de flashage
        flashTypeComboBox.getItems().addAll(
            "Firmware iOS Complet",
            "Firmware Android Complet", 
            "Partition Bootloader",
            "Partition Recovery",
            "Partition System",
            "Partition Boot"
        );
        flashTypeComboBox.setValue("Firmware iOS Complet");
        
        // Configuration des événements
        browseFirmwareBtn.setOnAction(e -> browseFirmware());
        detectDevicesBtn.setOnAction(e -> detectConnectedDevices());
        startFlashBtn.setOnAction(e -> startFlashing());
        cancelBtn.setOnAction(e -> cancelFlashing());
        
        // Gestion des changements de type de flashage
        flashTypeComboBox.setOnAction(e -> updateUIForFlashType());
        
        // État initial
        startFlashBtn.setDisable(true);
        cancelBtn.setDisable(true);
        partitionNameField.setVisible(false);
        
        updateUIForFlashType();
    }
    
    private void updateUIForFlashType() {
        String selectedType = flashTypeComboBox.getValue();
        boolean isPartitionFlash = selectedType != null && selectedType.startsWith("Partition");
        
        partitionNameField.setVisible(isPartitionFlash);
        
        if (isPartitionFlash) {
            String partitionName = selectedType.replace("Partition ", "").toLowerCase();
            partitionNameField.setText(partitionName);
        }
    }
    
    @FXML
    private void detectConnectedDevices() {
        updateStatus("Détection des appareils connectés...");
        deviceComboBox.getItems().clear();
        
        Task<List<Device>> detectionTask = new Task<List<Device>>() {
            @Override
            protected List<Device> call() throws Exception {
                return deviceService.detectAllRealDevices().get();
            }
            
            @Override
            protected void succeeded() {
                List<Device> devices = getValue();
                Platform.runLater(() -> {
                    deviceComboBox.getItems().addAll(devices);
                    if (!devices.isEmpty()) {
                        deviceComboBox.setValue(devices.get(0));
                        startFlashBtn.setDisable(false);
                        updateStatus(devices.size() + " appareil(s) détecté(s)");
                    } else {
                        updateStatus("Aucun appareil détecté");
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    updateStatus("Erreur lors de la détection des appareils");
                    showAlert("Erreur", "Impossible de détecter les appareils connectés", Alert.AlertType.ERROR);
                });
            }
        };
        
        new Thread(detectionTask).start();
    }
    
    @FXML
    private void browseFirmware() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le firmware");
        
        String selectedType = flashTypeComboBox.getValue();
        if (selectedType != null && selectedType.contains("iOS")) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Firmware iOS (*.ipsw)", "*.ipsw")
            );
        } else if (selectedType != null && selectedType.contains("Android")) {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Firmware Android", "*.zip", "*.tar", "*.md5"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );
        } else {
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.img", "*.bin"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );
        }
        
        Stage stage = (Stage) browseFirmwareBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            firmwarePathField.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void startFlashing() {
        if (!validateInputs()) {
            return;
        }
        
        Device selectedDevice = deviceComboBox.getValue();
        String firmwarePath = firmwarePathField.getText();
        String flashType = flashTypeComboBox.getValue();
        
        // Créer l'opération de flashage
        currentOperation = new UnlockOperation();
        currentOperation.setTargetDevice(selectedDevice);
        
        // Définir le type d'opération selon la sélection
        if (flashType.contains("iOS")) {
            currentOperation.setOperationType(UnlockOperation.OperationType.FLASH_IOS_FIRMWARE);
        } else if (flashType.contains("Android")) {
            currentOperation.setOperationType(UnlockOperation.OperationType.FLASH_ANDROID_FIRMWARE);
        } else {
            currentOperation.setOperationType(UnlockOperation.OperationType.FLASH_PARTITION);
        }
        
        updateStatus("Flashage en cours...");
        progressBar.setProgress(0);
        logArea.clear();
        
        // Désactiver les contrôles
        startFlashBtn.setDisable(true);
        cancelBtn.setDisable(false);
        detectDevicesBtn.setDisable(true);
        
        // Configurer l'opération
        currentOperation.setFirmwarePath(firmwarePath);
        if (flashType.startsWith("Partition")) {
            currentOperation.setPartitionName(partitionNameField.getText());
        }
        
        // Démarrer le flashage selon le type
        if (flashType.contains("iOS")) {
            unlockService.flashIOSFirmware(currentOperation, firmwarePath);
        } else if (flashType.contains("Android")) {
            unlockService.flashAndroidFirmware(currentOperation, firmwarePath);
        } else {
            String partitionName = partitionNameField.getText();
            unlockService.flashPartition(currentOperation, partitionName, firmwarePath);
        }
        
        // Surveiller la progression
        Task<Void> progressTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!currentOperation.isCompleted() && !currentOperation.isFailed() && !currentOperation.isCancelled()) {
                    Platform.runLater(() -> {
                        progressBar.setProgress(currentOperation.getProgressPercentage() / 100.0);
                        updateStatus(currentOperation.getCurrentStep());
                        
                        // Ajouter les logs
                        if (currentOperation.getErrorMessage() != null) {
                            logArea.appendText("ERREUR: " + currentOperation.getErrorMessage() + "\n");
                        }
                    });
                    
                    Thread.sleep(500);
                }
                
                Platform.runLater(() -> {
                    if (currentOperation.isCompleted()) {
                        progressBar.setProgress(1.0);
                        updateStatus("Flashage terminé avec succès");
                        logArea.appendText("SUCCÈS: " + currentOperation.getResult() + "\n");
                        showAlert("Succès", currentOperation.getResult(), Alert.AlertType.INFORMATION);
                    } else if (currentOperation.isFailed()) {
                        updateStatus("Flashage échoué");
                        logArea.appendText("ÉCHEC: " + currentOperation.getErrorMessage() + "\n");
                        showAlert("Erreur", currentOperation.getErrorMessage(), Alert.AlertType.ERROR);
                    }
                    
                    // Réactiver les contrôles
                    startFlashBtn.setDisable(false);
                    cancelBtn.setDisable(true);
                    detectDevicesBtn.setDisable(false);
                });
                
                return null;
            }
        };
        
        new Thread(progressTask).start();
    }
    
    @FXML
    private void cancelFlashing() {
        if (currentOperation != null) {
            currentOperation.cancel();
            updateStatus("Flashage annulé");
            
            // Réactiver les contrôles
            startFlashBtn.setDisable(false);
            cancelBtn.setDisable(true);
            detectDevicesBtn.setDisable(false);
        }
    }
    
    private boolean validateInputs() {
        Device selectedDevice = deviceComboBox.getValue();
        if (selectedDevice == null) {
            showAlert("Erreur", "Veuillez sélectionner un appareil", Alert.AlertType.WARNING);
            return false;
        }
        
        String firmwarePath = firmwarePathField.getText();
        if (firmwarePath == null || firmwarePath.trim().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner un fichier firmware", Alert.AlertType.WARNING);
            return false;
        }
        
        File firmwareFile = new File(firmwarePath);
        if (!firmwareFile.exists()) {
            showAlert("Erreur", "Le fichier firmware sélectionné n'existe pas", Alert.AlertType.WARNING);
            return false;
        }
        
        String flashType = flashTypeComboBox.getValue();
        if (flashType.startsWith("Partition")) {
            String partitionName = partitionNameField.getText();
            if (partitionName == null || partitionName.trim().isEmpty()) {
                showAlert("Erreur", "Veuillez spécifier le nom de la partition", Alert.AlertType.WARNING);
                return false;
            }
        }
        
        return true;
    }
    
    private void updateStatus(String status) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            logArea.appendText("[" + java.time.LocalTime.now() + "] " + status + "\n");
        });
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) startFlashBtn.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            logger.error("Erreur fermeture fenêtre", e);
        }
    }
}
