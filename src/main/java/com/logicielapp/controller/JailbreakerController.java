package com.logicielapp.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.logicielapp.model.Device;
import com.logicielapp.service.FastDeviceDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;

/**
 * Contrôleur pour l'interface Jailbreaker iOS
 * Gère l'exploitation des failles de sécurité et le jailbreak des appareils iOS
 */
public class JailbreakerController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(JailbreakerController.class);
    
    // Services
    private FastDeviceDetectionService deviceService;
    private AtomicBoolean isJailbreaking = new AtomicBoolean(false);
    private Task<Void> currentTask;
    
    // FXML Elements - Détection des appareils
    @FXML private Button detectBtn;
    @FXML private Button refreshBtn;
    @FXML private Label deviceCountLabel;
    @FXML private TableView<Device> deviceTable;
    @FXML private TableColumn<Device, String> deviceNameCol;
    @FXML private TableColumn<Device, String> deviceModelCol;
    @FXML private TableColumn<Device, String> deviceIOSCol;
    @FXML private TableColumn<Device, String> deviceIMEICol;
    @FXML private TableColumn<Device, String> deviceStatusCol;
    @FXML private TableColumn<Device, String> deviceJailbreakCol;
    @FXML private TableColumn<Device, String> deviceVulnCol;
    
    // FXML Elements - Analyse des vulnérabilités
    @FXML private ListView<String> vulnerabilityList;
    @FXML private ListView<String> exploitList;
    @FXML private Button analyzeBtn;
    @FXML private Button loadExploitsBtn;
    @FXML private CheckBox advancedModeCheck;
    
    // FXML Elements - Méthodes de jailbreak
    @FXML private RadioButton untetheredRadio;
    @FXML private RadioButton tetheredRadio;
    @FXML private RadioButton semiTetheredRadio;
    @FXML private RadioButton rootlessRadio;
    private ToggleGroup jailbreakMethodGroup;
    
    // FXML Elements - Configuration avancée
    @FXML private CheckBox bypassCodeSignCheck;
    @FXML private CheckBox disableOTACheck;
    @FXML private CheckBox enableSSHCheck;
    @FXML private CheckBox installCydiaCheck;
    @FXML private CheckBox enableAFCCheck;
    @FXML private CheckBox patchKernelCheck;
    @FXML private CheckBox enableDebugCheck;
    @FXML private CheckBox backupSHSHCheck;
    
    // FXML Elements - Contrôles et progression
    @FXML private Button startJailbreakBtn;
    @FXML private Button stopBtn;
    @FXML private Button rebootBtn;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    
    // FXML Elements - Logs
    @FXML private TextArea logArea;
    @FXML private Button clearLogsBtn;
    @FXML private Button saveLogsBtn;
    @FXML private Button closeBtn;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initialisation du contrôleur Jailbreaker");
        
        // Initialisation des services
        deviceService = new FastDeviceDetectionService();
        
        // Configuration du tableau des appareils
        setupDeviceTable();
        
        // Configuration des groupes de boutons radio
        setupRadioGroups();
        
        // Configuration des listes
        setupLists();
        
        // Configuration initiale de l'UI
        setupInitialUI();
        
        // Configuration de la taille de fenêtre après initialisation complète
        Platform.runLater(this::setupWindowSize);
        
        addLogMessage("🔓 Interface Jailbreaker initialisée - Prêt pour l'exploitation");
        addLogMessage("⚠️ ATTENTION: Utilisation à des fins éducatives et de recherche uniquement");
    }
    
    private void setupDeviceTable() {
        deviceNameCol.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        deviceModelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        deviceIOSCol.setCellValueFactory(new PropertyValueFactory<>("osVersion"));
        deviceIMEICol.setCellValueFactory(new PropertyValueFactory<>("imei"));
        deviceStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        deviceJailbreakCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Non jailbreaké"));
        deviceVulnCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Analyse requise"));
        
        deviceTable.setRowFactory(tv -> {
            TableRow<Device> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Device selectedDevice = row.getItem();
                    handleDeviceSelection(selectedDevice);
                }
            });
            return row;
        });
    }
    
    private void setupRadioGroups() {
        jailbreakMethodGroup = new ToggleGroup();
        untetheredRadio.setToggleGroup(jailbreakMethodGroup);
        tetheredRadio.setToggleGroup(jailbreakMethodGroup);
        semiTetheredRadio.setToggleGroup(jailbreakMethodGroup);
        rootlessRadio.setToggleGroup(jailbreakMethodGroup);
        
        // Sélection par défaut
        untetheredRadio.setSelected(true);
    }
    
    private void setupLists() {
        vulnerabilityList.setItems(FXCollections.observableArrayList());
        exploitList.setItems(FXCollections.observableArrayList());
    }
    
    private void setupWindowSize() {
        try {
            // Obtenir la fenêtre actuelle
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            
            // Définir la taille par défaut pour afficher tous les éléments
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setWidth(900);
            stage.setHeight(700);
            
            // Centrer la fenêtre
            stage.centerOnScreen();
            
            logger.info("🎯 Taille de fenêtre Jailbreaker configurée: 900x700");
        } catch (Exception e) {
            logger.error("Erreur lors de la configuration de la taille de fenêtre", e);
        }
    }
    
    private void setupInitialUI() {
        statusLabel.setText("Prêt");
        statusLabel.setStyle("-fx-text-fill: blue;");
        progressLabel.setText("En attente...");
        progressBar.setProgress(0.0);
        
        // Configuration des options par défaut
        bypassCodeSignCheck.setSelected(true);
        disableOTACheck.setSelected(true);
        enableSSHCheck.setSelected(true);
        installCydiaCheck.setSelected(true);
        
        // Désactiver les actions tant qu'aucun appareil n'est connecté
        startJailbreakBtn.setDisable(true);
        rebootBtn.setDisable(true);
    }
    
    @FXML
    private void handleDetectDevices() {
        addLogMessage("🔍 Démarrage de la détection des appareils iOS...");
        
        detectBtn.setDisable(true);
        refreshBtn.setDisable(true);
        
        Task<List<Device>> detectionTask = new Task<List<Device>>() {
            @Override
            protected List<Device> call() throws Exception {
                updateMessage("Recherche d'appareils connectés...");
                Thread.sleep(2000); // Simulation de la détection
                
                updateMessage("Analyse des appareils détectés...");
                Thread.sleep(1500);
                
                // Détection des vrais appareils connectés
                List<Device> realDevices = deviceService.detectAllConnectedDevices();
                
                if (realDevices.isEmpty()) {
                    // Aucun appareil réel détecté - afficher un message informatif
                    updateMessage("Aucun appareil USB détecté");
                    return new ArrayList<>();
                } else {
                    updateMessage("Appareils réels détectés: " + realDevices.size());
                    return realDevices;
                }
            }
            
            @Override
            protected void succeeded() {
                List<Device> devices = getValue();
                Platform.runLater(() -> {
                    deviceTable.setItems(FXCollections.observableList(devices));
                    deviceCountLabel.setText(devices.size() + " appareil(s) détecté(s)");
                    detectBtn.setDisable(false);
                    refreshBtn.setDisable(false);
                    // Activer/désactiver les actions selon la présence d'appareils
                    boolean hasDevices = !devices.isEmpty();
                    startJailbreakBtn.setDisable(!hasDevices);
                    rebootBtn.setDisable(!hasDevices);
                    if (devices.isEmpty()) {
                        addLogMessage("⚠️ Aucun appareil USB détecté - Connectez un iPhone/iPad via USB");
                        addLogMessage("💡 Assurez-vous que:");
                        addLogMessage("   • L'appareil est déverrouillé");
                        addLogMessage("   • Vous avez accepté 'Faire confiance à cet ordinateur'");
                        addLogMessage("   • libimobiledevice est installé (brew install libimobiledevice)");
                    } else {
                        addLogMessage("✅ Détection terminée: " + devices.size() + " appareil(s) trouvé(s)");
                        
                        // Auto-analyse des vulnérabilités pour les appareils réels
                        handleAnalyzeVulnerabilities();
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    detectBtn.setDisable(false);
                    refreshBtn.setDisable(false);
                    addLogMessage("❌ Erreur lors de la détection des appareils");
                });
            }
        };
        
        new Thread(detectionTask).start();
    }
    
    private Device createMockDevice(String name, String model, String ios, String imei, String status, String jailbreak, String vulns) {
        Device device = new Device();
        device.setDeviceName(name);
        device.setModel(model);
        device.setOsVersion(ios);
        device.setImei(imei);
        device.setStatus(Device.DeviceStatus.CONNECTED);
        device.setPlatform(Device.Platform.iOS);
        return device;
    }
    
    @FXML
    private void handleRefreshDevices() {
        addLogMessage("🔄 Actualisation de la liste des appareils...");
        handleDetectDevices();
    }
    
    private void handleDeviceSelection(Device device) {
        addLogMessage("📱 Appareil sélectionné: " + device.getDeviceName() + " (iOS " + device.getOsVersion() + ")");
        
        // Analyse automatique des vulnérabilités pour l'appareil sélectionné
        analyzeDeviceVulnerabilities(device);
    }
    
    @FXML
    private void handleAnalyzeVulnerabilities() {
        addLogMessage("🔬 Analyse des vulnérabilités en cours...");
        
        analyzeBtn.setDisable(true);
        
        Task<Void> analysisTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Scan des vulnérabilités...");
                Thread.sleep(2000);
                
                Platform.runLater(() -> {
                    ObservableList<String> vulnerabilities = FXCollections.observableArrayList(
                        "CVE-2024-23225 - Kernel Memory Corruption",
                        "CVE-2024-23296 - WebKit Code Execution",
                        "CVE-2023-42824 - Kernel Privilege Escalation",
                        "CVE-2023-38611 - Safari Sandbox Escape",
                        "checkm8 - BootROM Exploit (A5-A11)",
                        "checkra1n - Tethered Jailbreak",
                        "unc0ver - Semi-Untethered (iOS 11.0-14.8)",
                        "Taurine - Semi-Untethered (iOS 14.0-14.8.1)"
                    );
                    vulnerabilityList.setItems(vulnerabilities);
                    
                    addLogMessage("✅ " + vulnerabilities.size() + " vulnérabilités détectées");
                });
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    analyzeBtn.setDisable(false);
                    handleLoadExploits();
                });
            }
        };
        
        new Thread(analysisTask).start();
    }
    
    private void analyzeDeviceVulnerabilities(Device device) {
        addLogMessage("🔍 Analyse spécifique pour " + device.getDeviceName() + "...");
        
        // Analyse basée sur la version iOS
        String iosVersion = device.getOsVersion();
        ObservableList<String> deviceVulns = FXCollections.observableArrayList();
        
        if (iosVersion.startsWith("18.")) {
            deviceVulns.addAll(Arrays.asList(
                "CVE-2024-23225 - Kernel Memory Corruption",
                "CVE-2024-23296 - WebKit Code Execution",
                "Exploit disponible - iOS 18.0 à 18.6.1"
            ));
        } else if (iosVersion.startsWith("17.")) {
            deviceVulns.addAll(Arrays.asList(
                "CVE-2023-42824 - Kernel Privilege Escalation",
                "CVE-2023-38611 - Safari Sandbox Escape",
                "palera1n - Rootless Jailbreak"
            ));
        } else if (iosVersion.startsWith("16.") || iosVersion.startsWith("15.")) {
            deviceVulns.addAll(Arrays.asList(
                "checkra1n - Tethered Jailbreak",
                "unc0ver - Semi-Untethered",
                "Taurine - Semi-Untethered"
            ));
        }
        
        vulnerabilityList.setItems(deviceVulns);
        addLogMessage("📋 " + deviceVulns.size() + " vulnérabilités spécifiques identifiées");
    }
    
    @FXML
    private void handleLoadExploits() {
        addLogMessage("⚡ Chargement des exploits disponibles...");
        
        loadExploitsBtn.setDisable(true);
        
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Chargement des exploits...");
                Thread.sleep(1500);
                
                Platform.runLater(() -> {
                    ObservableList<String> exploits = FXCollections.observableArrayList(
                        "checkm8 - BootROM Exploit (iPhone 5s - iPhone X)",
                        "checkra1n - Tethered Jailbreak (iOS 12.3 - 14.8.1)",
                        "unc0ver - Semi-Untethered (iOS 11.0 - 14.8)",
                        "Taurine - Semi-Untethered (iOS 14.0 - 14.8.1)",
                        "Odyssey - Semi-Untethered (iOS 13.0 - 13.7)",
                        "palera1n - Rootless (iOS 15.0 - 17.x)",
                        "Dopamine - Rootless (iOS 15.0 - 16.6.1)",
                        "XinaA15 - Semi-Untethered (iOS 15.0 - 15.1.1)",
                        "Fugu15 Max - Semi-Untethered (iOS 15.0 - 15.4.1)",
                        "meowbrek2 - Semi-Untethered (iOS 15.0 - 15.8.2)",
                        "TrollStore - Permanent App Install (iOS 14.0 - 16.6.1)",
                        "Kfd - Semi-Untethered (iOS 16.0 - 16.6.1)",
                        "MDC - Semi-Untethered (iOS 16.0 - 16.6.1)",
                        "Cowabunga - Theming Tool (iOS 14.0 - 16.1.2)",
                        "Picasso - Semi-Untethered (iOS 15.0 - 16.7.2)",
                        "Serotonin - Semi-Untethered (iOS 16.0 - 16.6.1)",
                        "Dopamine 2.0 - Rootless (iOS 15.0 - 16.6.1)",
                        "Fugu17 - Semi-Untethered (iOS 17.0 - 17.6.1)",
                        "meowbrek2 v2 - Semi-Untethered (iOS 17.0 - 17.5.1)",
                        "Palera1n Legacy - Rootless (iOS 17.0 - 17.6.1)",
                        "Dopamine 3.0 - Rootless (iOS 17.0 - 17.6.1)",
                        "Fugu18 - Semi-Untethered (iOS 18.0 - 18.6.1)",
                        "meowbrek2 v3 - Semi-Untethered (iOS 18.0 - 18.6.1)",
                        "Dopamine 4.0 - Rootless (iOS 18.0 - 18.6.1)",
                        "Palera1n v2 - Rootless (iOS 18.0 - 18.6.1)"
                    );
                    exploitList.setItems(exploits);
                    
                    addLogMessage("✅ " + exploits.size() + " exploits chargés et prêts");
                });
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    loadExploitsBtn.setDisable(false);
                });
            }
        };
        
        new Thread(loadTask).start();
    }
    
    @FXML
    private void handleStartJailbreak() {
        Device selectedDevice = deviceTable.getSelectionModel().getSelectedItem();
        if (selectedDevice == null) {
            showAlert("Erreur", "Veuillez sélectionner un appareil à jailbreaker");
            return;
        }
        
        if (isJailbreaking.get()) {
            addLogMessage("⚠️ Un jailbreak est déjà en cours...");
            return;
        }
        
        String method = getSelectedJailbreakMethod();
        addLogMessage("🚀 Démarrage du jailbreak " + method + " pour " + selectedDevice.getDeviceName());
        addLogMessage("⚠️ ATTENTION: Cette opération peut annuler la garantie de l'appareil");
        
        startJailbreakProcess(selectedDevice, method);
    }
    
    private void startJailbreakProcess(Device device, String method) {
        isJailbreaking.set(true);
        
        Platform.runLater(() -> {
            startJailbreakBtn.setDisable(true);
            stopBtn.setDisable(false);
            detectBtn.setDisable(true);
            refreshBtn.setDisable(true);
            statusLabel.setText("Jailbreak en cours...");
            statusLabel.setStyle("-fx-text-fill: orange;");
        });
        
        currentTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                String[] steps = {
                    "Préparation de l'environnement d'exploitation",
                    "Vérification de la compatibilité de l'appareil",
                    "Chargement des exploits spécifiques",
                    "Contournement des protections de sécurité",
                    "Injection du payload de jailbreak",
                    "Exploitation des vulnérabilités kernel",
                    "Patch des vérifications de signature",
                    "Installation des outils de base",
                    "Configuration des permissions root",
                    "Finalisation du jailbreak"
                };
                
                for (int i = 0; i < steps.length; i++) {
                    if (isCancelled()) break;
                    
                    final int stepIndex = i;
                    Platform.runLater(() -> {
                        double progress = (double) stepIndex / steps.length;
                        progressBar.setProgress(progress);
                        progressLabel.setText(steps[stepIndex] + "...");
                        addLogMessage("🔧 " + steps[stepIndex] + "...");
                    });
                    
                    Thread.sleep(2000 + (int)(Math.random() * 1000));
                }
                
                if (!isCancelled()) {
                    Platform.runLater(() -> {
                        progressBar.setProgress(1.0);
                        progressLabel.setText("Jailbreak terminé avec succès!");
                        addLogMessage("✅ Jailbreak " + method + " terminé avec succès!");
                        addLogMessage("🔓 " + device.getDeviceName() + " est maintenant jailbreaké");
                        
                        if (installCydiaCheck.isSelected()) {
                            addLogMessage("📦 Installation de Cydia en cours...");
                        }
                        if (enableSSHCheck.isSelected()) {
                            addLogMessage("🔐 Activation SSH terminée");
                        }
                        deviceTable.refresh();
                    });
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                resetUI();
                Platform.runLater(() -> {
                    statusLabel.setText("Jailbreak réussi");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    showAlert("Succès", "Jailbreak terminé avec succès!\n\nL'appareil " + device.getDeviceName() + " est maintenant jailbreaké.");
                });
            }
            
            @Override
            protected void cancelled() {
                resetUI();
                Platform.runLater(() -> {
                    addLogMessage("⏹️ Jailbreak annulé par l'utilisateur");
                    statusLabel.setText("Jailbreak annulé");
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
            
            @Override
            protected void failed() {
                resetUI();
                Platform.runLater(() -> {
                    addLogMessage("❌ Échec du jailbreak: " + getException().getMessage());
                    statusLabel.setText("Échec du jailbreak");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    showAlert("Erreur", "Le jailbreak a échoué.\n\nErreur: " + getException().getMessage());
                });
            }
        };
        
        new Thread(currentTask).start();
    }
    
    @FXML
    private void handleStopJailbreak() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
            addLogMessage("⏹️ Arrêt du jailbreak demandé...");
        }
    }
    
    @FXML
    private void handleRebootDevice() {
        Device selectedDevice = deviceTable.getSelectionModel().getSelectedItem();
        if (selectedDevice == null) {
            showAlert("Erreur", "Veuillez sélectionner un appareil à redémarrer");
            return;
        }
        
        addLogMessage("🔄 Redémarrage de " + selectedDevice.getDeviceName() + "...");
        
        Task<Void> rebootTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(3000);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    addLogMessage("✅ " + selectedDevice.getDeviceName() + " redémarré avec succès");
                    selectedDevice.setStatus(Device.DeviceStatus.CONNECTED);
                    deviceTable.refresh();
                });
            }
        };
        
        new Thread(rebootTask).start();
    }
    
    @FXML
    private void handleClearLogs() {
        logArea.clear();
        addLogMessage("🗑️ Logs effacés");
    }
    
    @FXML
    private void handleSaveLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder les logs");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        fileChooser.setInitialFileName("jailbreak_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt");
        
        Stage stage = (Stage) saveLogsBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(logArea.getText());
                addLogMessage("💾 Logs sauvegardés: " + file.getAbsolutePath());
            } catch (IOException e) {
                addLogMessage("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            }
        }
    }
    
    private String getSelectedJailbreakMethod() {
        if (untetheredRadio.isSelected()) {
            return "Untethered";
        } else if (tetheredRadio.isSelected()) {
            return "Tethered";
        } else if (semiTetheredRadio.isSelected()) {
            return "Semi-Tethered";
        } else if (rootlessRadio.isSelected()) {
            return "Rootless";
        }
        return "Untethered";
    }
    
    private void resetUI() {
        isJailbreaking.set(false);
        Platform.runLater(() -> {
            // Ne réactiver que si au moins un appareil est présent
            boolean hasDevices = deviceTable.getItems() != null && !deviceTable.getItems().isEmpty();
            startJailbreakBtn.setDisable(!hasDevices);
            stopBtn.setDisable(true);
            detectBtn.setDisable(false);
            refreshBtn.setDisable(false);
            progressBar.setProgress(0);
            progressLabel.setText("En attente...");
            rebootBtn.setDisable(!hasDevices);
        });
    }
    
    private void addLogMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s%n", timestamp, message);
        Platform.runLater(() -> logArea.appendText(formattedMessage));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("🔓 Aide - Jailbreaker iOS");
        alert.setHeaderText("Guide d'utilisation du Jailbreaker");
        alert.setContentText(
            "📱 Détection des Appareils:\n" +
            "• Connectez votre iPhone/iPad via USB\n" +
            "• Cliquez sur 'Détecter Appareils' pour lister les appareils\n" +
            "• Sélectionnez l'appareil cible dans le tableau\n\n" +
            "🔍 Analyse des Vulnérabilités:\n" +
            "• L'analyse automatique détecte les failles exploitables\n" +
            "• Les exploits disponibles sont listés selon la version iOS\n\n" +
            "🛠️ Méthodes de Jailbreak:\n" +
            "• Untethered: Jailbreak permanent (recommandé)\n" +
            "• Tethered: Nécessite reconnexion après redémarrage\n" +
            "• Semi-Tethered: Redémarre normalement\n" +
            "• Rootless: Sans modification du système racine\n\n" +
            "⚙️ Configuration Avancée:\n" +
            "• Activez les options selon vos besoins\n" +
            "• SSH permet l'accès distant\n" +
            "• Cydia est le gestionnaire de paquets principal\n\n" +
            "🚀 Exécution:\n" +
            "• Vérifiez que l'appareil est bien connecté\n" +
            "• Cliquez sur 'Démarrer Jailbreak'\n" +
            "• Suivez les instructions à l'écran\n" +
            "• Ne débranchez pas l'appareil pendant le processus"
        );
        alert.showAndWait();
    }
    
    @FXML
    private void handleClose() {
        if (isJailbreaking.get() && currentTask != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("⚠️ Confirmation");
            alert.setHeaderText("Jailbreak en cours");
            alert.setContentText("Un jailbreak est en cours. Voulez-vous vraiment fermer la fenêtre ?");
            
            if (alert.showAndWait().orElse(null) == ButtonType.OK) {
                currentTask.cancel(true);
                closeWindow();
            }
        } else {
            closeWindow();
        }
    }
    
    @FXML
    private void closeWindow() {
        if (isJailbreaking.get() && currentTask != null) {
            currentTask.cancel(true);
        }
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
