package com.logicielapp.model;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Date;

/**
 * Modèle représentant une opération de déblocage d'appareil mobile
 * Contient les informations sur le processus, le statut et les résultats
 */
public class UnlockOperation {
    
    public enum OperationType {
        ICLOUD_BYPASS("Contournement iCloud"),
        ICLOUD_ACCOUNT_UNLOCK("Déblocage Compte iCloud Bloqué"),
        PASSCODE_UNLOCK("Déblocage Code d'Accès"),
        FRP_BYPASS("Contournement FRP"),
        PATTERN_UNLOCK("Déblocage Motif/PIN"),
        SAMSUNG_ACCOUNT_BYPASS("Contournement Compte Samsung"),
        MI_ACCOUNT_BYPASS("Contournement Compte Mi"),
        SCREEN_TIME_BYPASS("Contournement Temps d'Écran"),
        ACTIVATION_LOCK_BYPASS("Contournement Verrouillage d'Activation"),
        BOOTLOADER_UNLOCK("Déblocage Bootloader"),
        FLASH_IOS_FIRMWARE("Flashage Firmware iOS"),
        FLASH_ANDROID_FIRMWARE("Flashage Firmware Android"),
        FLASH_PARTITION("Flashage Partition");
        
        private final String displayName;
        
        OperationType(String displayName) {
            this.displayName = displayName;
        }
        
    }
    
    public enum OperationStatus {
        PENDING("En attente"),
        INITIALIZING("Initialisation"),
        IN_PROGRESS("En cours"),
        COMPLETED("Terminé"),
        FAILED("Échec"),
        CANCELLED("Annulé"),
        TIMEOUT("Timeout");
        
        private final String displayName;
        
        OperationStatus(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Identifiants
    private String operationId;
    private int userId;
    private Device targetDevice;
    
    // Type et configuration d'opération
    private OperationType operationType;
    private Device.ConnectionType connectionType;
    private String imeiForRemoteOperation;
    
    // Statut et progression
    private OperationStatus status;
    private double progressPercentage;
    private String currentStep;
    private String statusMessage;
    
    // Temps
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long estimatedDurationSeconds;
    
    // Résultats
    private boolean successful;
    private String resultMessage;
    private String errorMessage;
    private String errorCode;
    private String result;
    private Date createdAt;
    private Date completedAt;
    private String firmwarePath;
    private String partitionName;
    private boolean useAdvancedMode;
    private boolean bypassSecurity;
    private boolean preserveUserData;
    private String customParameters;
    
    // Contrôle de l'opération
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private Thread operationThread;
    
    // Logs et détails
    private StringBuilder operationLog;
    private String errorDetails;
    
    // Constructeurs
    public UnlockOperation() {
        this.operationId = generateOperationId();
        this.status = OperationStatus.PENDING;
        this.progressPercentage = 0.0;
        this.operationLog = new StringBuilder();
        this.preserveUserData = true; // Par défaut, préserver les données
    }
    
    public UnlockOperation(Device device, OperationType operationType, int userId) {
        this();
        this.targetDevice = device;
        this.operationType = operationType;
        this.userId = userId;
        this.connectionType = device.getConnectionType();
    }
    
    // Méthodes utilitaires
    
    /**
     * Démarre l'opération
     */
    public void start() {
        this.startTime = LocalDateTime.now();
        this.status = OperationStatus.INITIALIZING;
        addLogEntry("Démarrage de l'opération: " + operationType);
    }
    
    /**
     * Annule l'opération
     */
    public void cancel() {
        cancelled.set(true);
        this.status = OperationStatus.CANCELLED;
        this.endTime = LocalDateTime.now();
        addLogEntry("Opération annulée par l'utilisateur");
        
        // Interrompre le thread si il existe
        if (operationThread != null && operationThread.isAlive()) {
            operationThread.interrupt();
        }
    }
    
    /**
     * Marque l'opération comme terminée avec succès
     */
    public void complete(String resultMessage) {
        this.successful = true;
        this.status = OperationStatus.COMPLETED;
        this.progressPercentage = 100.0;
        this.resultMessage = resultMessage;
        this.endTime = LocalDateTime.now();
        addLogEntry("Opération terminée avec succès: " + resultMessage);
    }
    
    /**
     * Marque l'opération comme échouée
     */
    public void fail(String errorMessage, String errorCode) {
        this.successful = false;
        this.status = OperationStatus.FAILED;
        this.resultMessage = errorMessage;
        this.errorCode = errorCode;
        this.endTime = LocalDateTime.now();
        addLogEntry("Échec de l'opération: " + errorMessage);
    }
    
    /**
     * Vérifie si l'opération a échoué
     */
    public boolean isFailed() {
        return status == OperationStatus.FAILED;
    }
    
    /**
     * Met à jour la progression de l'opération
     */
    public void updateProgress(double percentage, String currentStep) {
        this.progressPercentage = Math.min(100.0, Math.max(0.0, percentage));
        this.currentStep = currentStep;
        this.status = OperationStatus.IN_PROGRESS;
        addLogEntry(String.format("[%.1f%%] %s", percentage, currentStep));
    }
    
    /**
     * Met à jour le message de statut
     */
    public void updateStatus(String message) {
        this.statusMessage = message;
        addLogEntry("Statut: " + message);
    }
    
    /**
     * Ajoute une entrée au log de l'opération
     */
    public void addLogEntry(String message) {
        String timestamp = LocalDateTime.now().toString();
        operationLog.append(String.format("[%s] %s%n", 
                          timestamp.substring(11, 19), message));
    }
    
    /**
     * Vérifie si l'opération a été annulée
     */
    public boolean isCancelled() {
        return cancelled.get();
    }
    
    /**
     * Vérifie si l'opération est en cours
     */
    public boolean isInProgress() {
        return status == OperationStatus.IN_PROGRESS || 
               status == OperationStatus.INITIALIZING;
    }
    
    /**
     * Vérifie si l'opération est terminée (succès ou échec)
     */
    public boolean isCompleted() {
        return status == OperationStatus.COMPLETED || 
               status == OperationStatus.FAILED || 
               status == OperationStatus.CANCELLED;
    }
    
    /**
     * Calcule la durée de l'opération
     */
    public long getDurationSeconds() {
        if (startTime == null) return 0;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).getSeconds();
    }
    
    /**
     * Retourne un résumé de l'opération
     */
    public String getSummary() {
        return String.format("%s sur %s (%s) - %s", 
                           operationType, 
                           targetDevice.getDisplayName(),
                           targetDevice.getPlatform(),
                           status);
    }
    
    /**
     * Génère un ID unique pour l'opération
     */
    private String generateOperationId() {
        return "OP_" + System.currentTimeMillis() + "_" + 
               (int)(Math.random() * 1000);
    }
    
    // Getters et Setters
    
    public String getOperationId() {
        return operationId;
    }
    
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public Device getTargetDevice() {
        return targetDevice;
    }
    
    public void setTargetDevice(Device targetDevice) {
        this.targetDevice = targetDevice;
    }
    
    public OperationType getOperationType() {
        return operationType;
    }
    
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
    
    public Device.ConnectionType getConnectionType() {
        return connectionType;
    }
    
    public void setConnectionType(Device.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    
    public String getImeiForRemoteOperation() {
        return imeiForRemoteOperation;
    }
    
    public void setImeiForRemoteOperation(String imeiForRemoteOperation) {
        this.imeiForRemoteOperation = imeiForRemoteOperation;
    }
    
    public OperationStatus getStatus() {
        return status;
    }
    
    public void setStatus(OperationStatus status) {
        this.status = status;
    }
    
    public double getProgressPercentage() {
        return progressPercentage;
    }
    
    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }
    
    public String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public long getEstimatedDurationSeconds() {
        return estimatedDurationSeconds;
    }
    
    public void setEstimatedDurationSeconds(long estimatedDurationSeconds) {
        this.estimatedDurationSeconds = estimatedDurationSeconds;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getResultMessage() {
        return resultMessage;
    }
    
    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public boolean isUseAdvancedMode() {
        return useAdvancedMode;
    }
    
    public void setUseAdvancedMode(boolean useAdvancedMode) {
        this.useAdvancedMode = useAdvancedMode;
    }
    
    public boolean isBypassSecurity() {
        return bypassSecurity;
    }
    
    public void setBypassSecurity(boolean bypassSecurity) {
        this.bypassSecurity = bypassSecurity;
    }
    
    public boolean isPreserveUserData() {
        return preserveUserData;
    }
    
    public void setPreserveUserData(boolean preserveUserData) {
        this.preserveUserData = preserveUserData;
    }
    
    public String getCustomParameters() {
        return customParameters;
    }
    
    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }
    
    public String getFirmwarePath() {
        return firmwarePath;
    }
    
    public void setFirmwarePath(String firmwarePath) {
        this.firmwarePath = firmwarePath;
    }
    
    public String getPartitionName() {
        return partitionName;
    }
    
    public void setPartitionName(String partitionName) {
        this.partitionName = partitionName;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }
    
    public String getOperationLog() {
        return operationLog.toString();
    }
    
    public Thread getOperationThread() {
        return operationThread;
    }
    
    public void setOperationThread(Thread operationThread) {
        this.operationThread = operationThread;
    }
    
    @Override
    public String toString() {
        return String.format("UnlockOperation{id='%s', type=%s, device=%s, status=%s, progress=%.1f%%}", 
                           operationId, operationType, 
                           targetDevice != null ? targetDevice.getModel() : "null",
                           status, progressPercentage);
    }
}
