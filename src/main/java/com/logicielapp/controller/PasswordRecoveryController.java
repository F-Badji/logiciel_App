package com.logicielapp.controller;

import com.logicielapp.util.ErrorHandler;
import com.logicielapp.service.PasswordRecoveryService;
import com.logicielapp.service.PhoneInfoDatabase;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

/**
 * Contrôleur pour l'interface de récupération de mots de passe
 * Gère les étapes de récupération avec validation et sécurité
 */
public class PasswordRecoveryController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryController.class);
    
    // Header
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    
    // Progress indicators
    @FXML private Label step1, step2, step3, step4;
    
    // Content areas
    @FXML private VBox contentArea;
    @FXML private VBox initialForm;
    @FXML private VBox emailVerificationForm;
    @FXML private VBox securityQuestionsForm;
    @FXML private VBox passwordResultForm;
    
    // Step 1: Initial form
    @FXML private TextField imeiField;
    @FXML private Label imeiValidationLabel;
    @FXML private TextField emailField;
    @FXML private ComboBox<PasswordRecoveryService.PasswordType> passwordTypeComboBox;
    @FXML private Label deviceInfoLabel;
    
    // Step 2: Email verification
    @FXML private Label emailSentLabel;
    @FXML private TextField verificationCodeField;
    @FXML private Button resendCodeButton;
    
    // Step 3: Security questions
    @FXML private VBox questionsContainer;
    
    // Step 4: Password result
    @FXML private TextField newPasswordField;
    @FXML private Button copyPasswordButton;
    @FXML private Button togglePasswordButton;
    @FXML private Label passwordTypeLabel;
    @FXML private Label deviceDetectedLabel;
    @FXML private TextArea instructionsArea;
    
    // Status and controls
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button nextButton;
    @FXML private Label sessionInfoLabel;
    
    // State
    private int currentStep = 1;
    private String currentSessionId = null;
    private List<PasswordRecoveryService.SecurityQuestion> currentSecurityQuestions = new ArrayList<>();
    private Map<PasswordRecoveryService.SecurityQuestion, TextField> questionFields = new HashMap<>();
    private boolean passwordVisible = false;
    private String generatedPassword = "";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializePasswordTypes();
        setupFieldValidation();
        setupEventHandlers();
        updateUIForStep();
        
        logger.info("Interface de récupération de mot de passe initialisée");
    }
    
    /**
     * Initialise les types de mots de passe dans la ComboBox
     */
    private void initializePasswordTypes() {
        passwordTypeComboBox.getItems().addAll(PasswordRecoveryService.PasswordType.values());
        passwordTypeComboBox.setCellFactory(listView -> new ListCell<PasswordRecoveryService.PasswordType>() {
            @Override
            protected void updateItem(PasswordRecoveryService.PasswordType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName() + " - " + item.getDescription());
                }
            }
        });
        
        passwordTypeComboBox.setButtonCell(new ListCell<PasswordRecoveryService.PasswordType>() {
            @Override
            protected void updateItem(PasswordRecoveryService.PasswordType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Sélectionnez le type de mot de passe...");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
    }
    
    /**
     * Configure la validation des champs en temps réel
     */
    private void setupFieldValidation() {
        // Validation IMEI en temps réel
        imeiField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Limiter à 15 chiffres seulement
                String filtered = newValue.replaceAll("[^0-9]", "");
                if (filtered.length() > 15) {
                    filtered = filtered.substring(0, 15);
                }
                
                if (!newValue.equals(filtered)) {
                    final String finalFiltered = filtered;
                    Platform.runLater(() -> {
                        imeiField.setText(finalFiltered);
                        imeiField.positionCaret(finalFiltered.length());
                    });
                    return;
                }
                
                validateIMEIField(filtered);
            }
        });
        
        // Validation code de vérification
        verificationCodeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String filtered = newValue.toUpperCase().replaceAll("[^A-Z0-9]", "");
                if (filtered.length() > 8) {
                    filtered = filtered.substring(0, 8);
                }
                
                if (!newValue.equals(filtered)) {
                    final String finalFiltered = filtered;
                    Platform.runLater(() -> {
                        verificationCodeField.setText(finalFiltered);
                        verificationCodeField.positionCaret(finalFiltered.length());
                    });
                }
            }
        });
    }
    
    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        resendCodeButton.setOnAction(e -> handleResendCode());
        copyPasswordButton.setOnAction(e -> handleCopyPassword());
        togglePasswordButton.setOnAction(e -> handleTogglePasswordVisibility());
    }
    
    /**
     * Valide le champ IMEI et affiche les informations du téléphone
     */
    private void validateIMEIField(String imei) {
        if (imei.isEmpty()) {
            imeiValidationLabel.setText("");
            deviceInfoLabel.setText("");
            return;
        }
        
        if (imei.length() == 15) {
            PhoneInfoDatabase.CompletePhoneInfo phoneInfo = PhoneInfoDatabase.getCompletePhoneInfo(imei);
            
            if (phoneInfo.isValidImei()) {
                imeiValidationLabel.setText("✅ IMEI valide");
                imeiValidationLabel.getStyleClass().clear();
                imeiValidationLabel.getStyleClass().add("validation-success");
                
                if (phoneInfo.getPhoneInfo() != null) {
                    String deviceInfo = String.format("📱 Appareil: %s\n📡 Opérateur: %s", 
                                                    phoneInfo.getPhoneInfo().toString(),
                                                    phoneInfo.getDetectedOperator());
                    deviceInfoLabel.setText(deviceInfo);
                } else {
                    deviceInfoLabel.setText("📱 Appareil: Non reconnu dans la base de données");
                }
            } else {
                imeiValidationLabel.setText("❌ IMEI invalide (échec validation Luhn)");
                imeiValidationLabel.getStyleClass().clear();
                imeiValidationLabel.getStyleClass().add("validation-error");
                deviceInfoLabel.setText("");
            }
        } else {
            imeiValidationLabel.setText("⏳ Saisissez 15 chiffres...");
            imeiValidationLabel.getStyleClass().clear();
            imeiValidationLabel.getStyleClass().add("validation-pending");
            deviceInfoLabel.setText("");
        }
    }
    
    /**
     * Met à jour l'interface selon l'étape actuelle
     */
    private void updateUIForStep() {
        // Reset visibility
        initialForm.setVisible(false);
        emailVerificationForm.setVisible(false);
        securityQuestionsForm.setVisible(false);
        passwordResultForm.setVisible(false);
        
        // Reset step indicators
        resetStepIndicators();
        
        // Update based on current step
        switch (currentStep) {
            case 1:
                initialForm.setVisible(true);
                step1.getStyleClass().add("step-active");
                nextButton.setText("Démarrer ➤");
                backButton.setVisible(false);
                break;
                
            case 2:
                emailVerificationForm.setVisible(true);
                step2.getStyleClass().add("step-active");
                nextButton.setText("Vérifier Code ➤");
                backButton.setVisible(true);
                break;
                
            case 3:
                securityQuestionsForm.setVisible(true);
                step3.getStyleClass().add("step-active");
                nextButton.setText("Valider Réponses ➤");
                backButton.setVisible(true);
                break;
                
            case 4:
                passwordResultForm.setVisible(true);
                step4.getStyleClass().add("step-active");
                nextButton.setText("✅ Terminé");
                backButton.setVisible(false);
                break;
        }
        
        updateSessionInfo();
    }
    
    /**
     * Reset les indicateurs d'étapes
     */
    private void resetStepIndicators() {
        step1.getStyleClass().removeAll("step-active", "step-completed");
        step2.getStyleClass().removeAll("step-active", "step-completed");
        step3.getStyleClass().removeAll("step-active", "step-completed");
        step4.getStyleClass().removeAll("step-active", "step-completed");
        
        // Mark completed steps
        for (int i = 1; i < currentStep; i++) {
            switch (i) {
                case 1: step1.getStyleClass().add("step-completed"); break;
                case 2: step2.getStyleClass().add("step-completed"); break;
                case 3: step3.getStyleClass().add("step-completed"); break;
            }
        }
    }
    
    /**
     * Met à jour les informations de session
     */
    private void updateSessionInfo() {
        if (currentSessionId != null) {
            sessionInfoLabel.setText("Session: " + currentSessionId.substring(0, Math.min(12, currentSessionId.length())) + "...");
        } else {
            sessionInfoLabel.setText("");
        }
    }
    
    /**
     * Gestionnaire pour le bouton Suivant/Action principale
     */
    @FXML
    private void handleNext() {
        switch (currentStep) {
            case 1:
                startRecoveryProcess();
                break;
            case 2:
                verifyEmailCode();
                break;
            case 3:
                processSecurityQuestions();
                break;
            case 4:
                closeDialog();
                break;
        }
    }
    
    /**
     * Démarre le processus de récupération
     */
    private void startRecoveryProcess() {
        String imei = imeiField.getText().trim();
        String email = emailField.getText().trim();
        PasswordRecoveryService.PasswordType passwordType = passwordTypeComboBox.getValue();
        
        // Validation
        if (imei.isEmpty() || email.isEmpty() || passwordType == null) {
            showErrorMessage("Veuillez remplir tous les champs obligatoires.");
            return;
        }
        
        if (imei.length() != 15) {
            showErrorMessage("L'IMEI doit contenir exactement 15 chiffres.");
            return;
        }
        
        showProgress("Démarrage de la récupération de mot de passe...");
        
        Task<PasswordRecoveryService.RecoveryResult> task = new Task<PasswordRecoveryService.RecoveryResult>() {
            @Override
            protected PasswordRecoveryService.RecoveryResult call() throws Exception {
                return PasswordRecoveryService.startPasswordRecovery(imei, email, passwordType);
            }
        };
        
        task.setOnSucceeded(e -> {
            hideProgress();
            PasswordRecoveryService.RecoveryResult result = task.getValue();
            
            if (result.isSuccess()) {
                currentSessionId = result.getSessionId();
                
                @SuppressWarnings("unchecked")
                Map<String, String> data = (Map<String, String>) result.getData();
                String verificationCode = data.get("verificationCode");
                
                emailSentLabel.setText("📧 " + result.getMessage() + 
                                     "\n\n🔑 Code de test (développement): " + verificationCode);
                
                currentStep = 2;
                updateUIForStep();
                showSuccessMessage("Code de vérification envoyé avec succès !");
            } else {
                showErrorMessage("Erreur: " + result.getMessage());
            }
        });
        
        task.setOnFailed(e -> {
            hideProgress();
            Throwable exception = task.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Erreur inconnue";
            showErrorMessage("Erreur lors du démarrage: " + errorMessage);
            logger.error("Erreur lors du démarrage de récupération", exception);
        });
        
        new Thread(task).start();
    }
    
    /**
     * Vérifie le code de vérification email
     */
    private void verifyEmailCode() {
        String code = verificationCodeField.getText().trim();
        
        if (code.isEmpty()) {
            showErrorMessage("Veuillez saisir le code de vérification.");
            return;
        }
        
        if (code.length() != 8) {
            showErrorMessage("Le code de vérification doit contenir 8 caractères.");
            return;
        }
        
        showProgress("Vérification du code...");
        
        Task<PasswordRecoveryService.RecoveryResult> task = new Task<PasswordRecoveryService.RecoveryResult>() {
            @Override
            protected PasswordRecoveryService.RecoveryResult call() throws Exception {
                return PasswordRecoveryService.verifyEmailCode(currentSessionId, code);
            }
        };
        
        task.setOnSucceeded(e -> {
            hideProgress();
            PasswordRecoveryService.RecoveryResult result = task.getValue();
            
            if (result.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<PasswordRecoveryService.SecurityQuestion> questions = 
                    (List<PasswordRecoveryService.SecurityQuestion>) result.getData();
                
                setupSecurityQuestions(questions);
                currentStep = 3;
                updateUIForStep();
                showSuccessMessage("Code vérifié ! Répondez aux questions de sécurité.");
            } else {
                showErrorMessage("Erreur: " + result.getMessage());
            }
        });
        
        task.setOnFailed(e -> {
            hideProgress();
            Throwable exception = task.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Erreur inconnue";
            showErrorMessage("Erreur lors de la vérification: " + errorMessage);
            logger.error("Erreur lors de la vérification du code", exception);
        });
        
        new Thread(task).start();
    }
    
    /**
     * Configure les questions de sécurité dans l'interface
     */
    private void setupSecurityQuestions(List<PasswordRecoveryService.SecurityQuestion> questions) {
        questionsContainer.getChildren().clear();
        questionFields.clear();
        currentSecurityQuestions = questions;
        
        for (PasswordRecoveryService.SecurityQuestion question : questions) {
            VBox questionBox = new VBox(5);
            
            Label questionLabel = new Label(question.getQuestion());
            questionLabel.getStyleClass().add("field-label");
            
            TextField answerField = new TextField();
            answerField.setPromptText("Votre réponse...");
            answerField.getStyleClass().add("input-field");
            
            questionBox.getChildren().addAll(questionLabel, answerField);
            questionsContainer.getChildren().add(questionBox);
            
            questionFields.put(question, answerField);
        }
    }
    
    /**
     * Traite les réponses aux questions de sécurité
     */
    private void processSecurityQuestions() {
        Map<PasswordRecoveryService.SecurityQuestion, String> answers = new HashMap<>();
        
        // Collecter les réponses
        for (Map.Entry<PasswordRecoveryService.SecurityQuestion, TextField> entry : questionFields.entrySet()) {
            String answer = entry.getValue().getText().trim();
            if (answer.isEmpty()) {
                showErrorMessage("Veuillez répondre à toutes les questions de sécurité.");
                return;
            }
            answers.put(entry.getKey(), answer);
        }
        
        showProgress("Validation des réponses de sécurité...");
        
        Task<PasswordRecoveryService.RecoveryResult> task = new Task<PasswordRecoveryService.RecoveryResult>() {
            @Override
            protected PasswordRecoveryService.RecoveryResult call() throws Exception {
                PasswordRecoveryService.RecoveryResult result = 
                    PasswordRecoveryService.processSecurityQuestions(currentSessionId, answers);
                
                if (result.isSuccess()) {
                    // Directement générer le nouveau mot de passe
                    return PasswordRecoveryService.generateNewPassword(currentSessionId);
                } else {
                    return result;
                }
            }
        };
        
        task.setOnSucceeded(e -> {
            hideProgress();
            PasswordRecoveryService.RecoveryResult result = task.getValue();
            
            if (result.isSuccess()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) result.getData();
                displayPasswordResult(data);
                currentStep = 4;
                updateUIForStep();
                showSuccessMessage("Récupération terminée avec succès !");
            } else {
                showErrorMessage("Erreur: " + result.getMessage());
            }
        });
        
        task.setOnFailed(e -> {
            hideProgress();
            Throwable exception = task.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Erreur inconnue";
            showErrorMessage("Erreur lors du traitement: " + errorMessage);
            logger.error("Erreur lors du traitement des questions", exception);
        });
        
        new Thread(task).start();
    }
    
    /**
     * Affiche le résultat de récupération du mot de passe
     */
    private void displayPasswordResult(Map<String, Object> data) {
        generatedPassword = (String) data.get("newPassword");
        PasswordRecoveryService.PasswordType passwordType = 
            (PasswordRecoveryService.PasswordType) data.get("passwordType");
        PhoneInfoDatabase.CompletePhoneInfo phoneInfo = 
            (PhoneInfoDatabase.CompletePhoneInfo) data.get("phoneInfo");
        String instructions = (String) data.get("instructions");
        
        // Masquer le mot de passe par défaut
        newPasswordField.setText("*".repeat(generatedPassword.length()));
        passwordVisible = false;
        togglePasswordButton.setText("👁️ Afficher");
        
        passwordTypeLabel.setText(passwordType.getDisplayName() + " - " + passwordType.getDescription());
        
        if (phoneInfo != null && phoneInfo.getPhoneInfo() != null) {
            deviceDetectedLabel.setText(phoneInfo.getPhoneInfo().toString() + 
                                      " | " + phoneInfo.getDetectedOperator());
        } else {
            deviceDetectedLabel.setText("Appareil non reconnu");
        }
        
        instructionsArea.setText(instructions);
    }
    
    /**
     * Gestionnaire pour revenir à l'étape précédente
     */
    @FXML
    private void handleBack() {
        if (currentStep > 1) {
            currentStep--;
            updateUIForStep();
            clearStatusMessage();
        }
    }
    
    /**
     * Gestionnaire pour annuler la récupération
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Ferme le dialog de récupération
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Gestionnaire pour renvoyer le code de vérification
     */
    private void handleResendCode() {
        // Dans un vrai système, ceci renverrait un nouveau code
        showInfoMessage("Un nouveau code de vérification a été envoyé.");
    }
    
    /**
     * Gestionnaire pour copier le mot de passe
     */
    private void handleCopyPassword() {
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword);
        Clipboard.getSystemClipboard().setContent(content);
        
        showSuccessMessage("Mot de passe copié dans le presse-papiers !");
    }
    
    /**
     * Gestionnaire pour basculer la visibilité du mot de passe
     */
    private void handleTogglePasswordVisibility() {
        if (passwordVisible) {
            newPasswordField.setText("*".repeat(generatedPassword.length()));
            togglePasswordButton.setText("👁️ Afficher");
            passwordVisible = false;
        } else {
            newPasswordField.setText(generatedPassword);
            togglePasswordButton.setText("🙈 Masquer");
            passwordVisible = true;
        }
    }
    
    /**
     * Méthodes utilitaires pour les messages
     */
    private void showErrorMessage(String message) {
        statusLabel.setText("❌ " + message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("error-message");
    }
    
    private void showSuccessMessage(String message) {
        statusLabel.setText("✅ " + message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("success-message");
    }
    
    private void showInfoMessage(String message) {
        statusLabel.setText("ℹ️ " + message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("info-message");
    }
    
    private void showProgress(String message) {
        statusLabel.setText("⏳ " + message);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("info-message");
        progressBar.setVisible(true);
        nextButton.setDisable(true);
    }
    
    private void hideProgress() {
        progressBar.setVisible(false);
        nextButton.setDisable(false);
    }
    
    private void clearStatusMessage() {
        statusLabel.setText("");
        hideProgress();
    }
}
