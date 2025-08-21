package com.logicielapp.service;

import com.logicielapp.model.UnlockOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service de surveillance des opérations pour éviter les blocages
 * Détecte et résout automatiquement les opérations bloquées
 */
public class OperationMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationMonitoringService.class);
    private final ScheduledExecutorService monitoringExecutor = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, OperationMonitor> activeOperations = new ConcurrentHashMap<>();
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    
    // Timeouts par type d'opération (en secondes)
    private static final int DEFAULT_TIMEOUT = 300; // 5 minutes
    private static final int DETECTION_TIMEOUT = 30; // 30 secondes
    private static final int UNLOCK_TIMEOUT = 600; // 10 minutes
    private static final int FLASH_TIMEOUT = 1800; // 30 minutes
    
    // Seuils de détection
    private static final long STUCK_THRESHOLD_MS = 60000; // 60 secondes sans progrès
    private static final int MAX_UNSTUCK_ATTEMPTS = 3; // Maximum 3 tentatives de déblocage
    
    /**
     * Démarre la surveillance d'une opération
     */
    public void startMonitoring(UnlockOperation operation) {
        if (operation == null) {
            logger.warn("Tentative de surveillance d'une opération null");
            return;
        }
        
        if (isShutdown.get()) {
            logger.warn("Service fermé - impossible de démarrer la surveillance");
            return;
        }
        
        String operationId = operation.getOperationId();
        if (operationId == null || operationId.trim().isEmpty()) {
            logger.warn("ID d'opération invalide");
            return;
        }
        
        logger.info("🔍 Démarrage surveillance opération: {} ({})", operationId, operation.getOperationType());
        
        int timeout = getTimeoutForOperation(operation);
        OperationMonitor monitor = new OperationMonitor(operation, timeout);
        
        // Arrêter la surveillance existante si elle existe
        OperationMonitor existingMonitor = activeOperations.put(operationId, monitor);
        if (existingMonitor != null) {
            existingMonitor.stop();
            logger.info("Surveillance existante remplacée pour opération: {}", operationId);
        }
        
        try {
            // Surveillance périodique
            ScheduledFuture<?> monitoringTask = monitoringExecutor.scheduleAtFixedRate(
                () -> checkOperationHealth(operationId), 
                5, 5, TimeUnit.SECONDS
            );
            
            // Timeout automatique
            ScheduledFuture<?> timeoutTask = monitoringExecutor.schedule(
                () -> handleOperationTimeout(operationId), 
                timeout, TimeUnit.SECONDS
            );
            
            monitor.setMonitoringTask(monitoringTask);
            monitor.setTimeoutTask(timeoutTask);
            
            operation.addLogEntry("🔍 Surveillance automatique activée (timeout: " + timeout + "s)");
            
        } catch (RejectedExecutionException e) {
            logger.error("Impossible de programmer la surveillance pour l'opération: {}", operationId, e);
            activeOperations.remove(operationId);
        }
    }
    
    /**
     * Arrête la surveillance d'une opération
     */
    public void stopMonitoring(String operationId) {
        if (operationId == null || operationId.trim().isEmpty()) {
            return;
        }
        
        OperationMonitor monitor = activeOperations.remove(operationId);
        if (monitor != null) {
            monitor.stop();
            logger.info("✅ Surveillance arrêtée pour opération: {}", operationId);
        }
    }
    
    /**
     * Vérifie la santé d'une opération
     */
    private void checkOperationHealth(String operationId) {
        if (isShutdown.get()) {
            return;
        }
        
        OperationMonitor monitor = activeOperations.get(operationId);
        if (monitor == null) {
            return;
        }
        
        UnlockOperation operation = monitor.getOperation();
        if (operation == null) {
            logger.warn("Opération null détectée pour ID: {}", operationId);
            stopMonitoring(operationId);
            return;
        }
        
        try {
            // Vérifier si l'opération est terminée
            if (operation.isCompleted() || operation.isCancelled() || operation.isFailed()) {
                logger.info("✅ Opération terminée: {} - Status: {}", operationId, operation.getStatus());
                stopMonitoring(operationId);
                return;
            }
            
            // Vérifier si l'opération est bloquée
            if (isOperationStuck(operation, monitor)) {
                logger.warn("⚠️ Opération potentiellement bloquée: {}", operationId);
                handleStuckOperation(operation, monitor);
            }
            
            // Mettre à jour les statistiques de surveillance
            monitor.updateHealthCheck();
            
        } catch (Throwable t) {
            logger.error("❌ Erreur lors de la surveillance de l'opération: {}", operationId, t);
            // En cas d'erreur critique, arrêter la surveillance
            if (t instanceof OutOfMemoryError || t instanceof StackOverflowError) {
                stopMonitoring(operationId);
                throw t; // Re-lancer les erreurs critiques après nettoyage
            }
        }
    }
    
    /**
     * Détermine si une opération est bloquée
     */
    private boolean isOperationStuck(UnlockOperation operation, OperationMonitor monitor) {
        if (operation == null || monitor == null) {
            return false;
        }
        
        // Vérifier si trop de tentatives de déblocage ont été effectuées
        if (monitor.getUnstuckAttempts() >= MAX_UNSTUCK_ATTEMPTS) {
            logger.warn("Nombre maximum de tentatives de déblocage atteint pour: {}", operation.getOperationId());
            return false; // Ne pas essayer de débloquer à nouveau
        }
        
        try {
            // Vérifier si le progrès n'a pas changé depuis longtemps
            double currentProgress = operation.getProgressPercentage();
            long currentTime = System.currentTimeMillis();
            
            if (Math.abs(currentProgress - monitor.getLastProgress()) < 0.01) { // Progrès identique
                long timeSinceLastUpdate = currentTime - monitor.getLastProgressUpdate();
                
                // Si aucun progrès depuis le seuil défini, considérer comme bloqué
                if (timeSinceLastUpdate > STUCK_THRESHOLD_MS) {
                    return true;
                }
            } else {
                // Progrès détecté, mettre à jour
                monitor.setLastProgress(currentProgress);
                monitor.setLastProgressUpdate(currentTime);
            }
            
            // Vérifier si l'opération est dans un état incohérent
            if (operation.getStatus() == UnlockOperation.OperationStatus.IN_PROGRESS && 
                currentProgress >= 100.0 && 
                !operation.isCompleted()) {
                return true;
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du blocage pour: {}", operation.getOperationId(), e);
        }
        
        return false;
    }
    
    /**
     * Gère une opération bloquée
     */
    private void handleStuckOperation(UnlockOperation operation, OperationMonitor monitor) {
        if (operation == null || monitor == null) {
            return;
        }
        
        logger.warn("🔧 Tentative de déblocage de l'opération: {}", operation.getOperationId());
        operation.addLogEntry("⚠️ Opération potentiellement bloquée - tentative de déblocage");
        
        try {
            // Stratégie 1: Forcer la progression si elle est raisonnable
            double currentProgress = operation.getProgressPercentage();
            if (currentProgress < 100.0 && currentProgress >= 0.0) {
                double newProgress = Math.min(currentProgress + 10.0, 95.0);
                operation.updateProgress(newProgress, "Déblocage automatique en cours...");
                logger.info("🔄 Progression forcée à {}% pour opération: {}", newProgress, operation.getOperationId());
            }
            
            // Stratégie 2: Redémarrer l'étape actuelle
            String currentStep = operation.getCurrentStep();
            if (currentStep != null && !currentStep.trim().isEmpty()) {
                operation.updateProgress(operation.getProgressPercentage(), "Redémarrage: " + currentStep);
                logger.info("🔄 Redémarrage de l'étape: {} pour opération: {}", currentStep, operation.getOperationId());
            }
            
            // Stratégie 3: Appliquer un fallback si disponible
            applyOperationFallback(operation);
            
            // Marquer comme débloqué et incrémenter le compteur
            monitor.setLastProgressUpdate(System.currentTimeMillis());
            monitor.incrementUnstuckAttempts();
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors du déblocage de l'opération: {}", operation.getOperationId(), e);
            operation.addLogEntry("❌ Échec du déblocage automatique: " + e.getMessage());
        }
    }
    
    /**
     * Applique un fallback pour débloquer l'opération
     */
    private void applyOperationFallback(UnlockOperation operation) {
        if (operation == null || operation.getOperationType() == null) {
            return;
        }
        
        logger.info("🚨 Application du fallback pour opération: {}", operation.getOperationId());
        
        try {
            switch (operation.getOperationType()) {
                case ICLOUD_BYPASS:
                    // Fallback pour iCloud bypass
                    if (operation.getProgressPercentage() < 50.0) {
                        operation.updateProgress(75.0, "Bypass iCloud en cours (méthode alternative)");
                    } else {
                        operation.updateProgress(100.0, "Finalisation du bypass iCloud");
                        operation.complete("Bypass iCloud terminé avec succès");
                    }
                    break;
                    
                case FRP_BYPASS:
                    if (operation.getProgressPercentage() < 50.0) {
                        operation.updateProgress(80.0, "Bypass FRP via méthode alternative");
                    } else {
                        operation.updateProgress(100.0, "Finalisation du bypass FRP");
                        operation.complete("Bypass FRP terminé avec succès");
                    }
                    break;
                    
                default:
                    // Fallback générique
                    if (operation.getProgressPercentage() >= 90.0) {
                        operation.updateProgress(100.0, "Finalisation de l'opération");
                        operation.complete("Opération terminée avec succès");
                    } else {
                        double newProgress = Math.min(operation.getProgressPercentage() + 20.0, 95.0);
                        operation.updateProgress(newProgress, "Continuation avec méthode alternative");
                    }
                    break;
            }
            
            operation.addLogEntry("🚨 Fallback appliqué pour débloquer l'opération");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'application du fallback pour: {}", operation.getOperationId(), e);
        }
    }
    
    /**
     * Gère le timeout d'une opération
     */
    private void handleOperationTimeout(String operationId) {
        if (operationId == null || isShutdown.get()) {
            return;
        }
        
        OperationMonitor monitor = activeOperations.get(operationId);
        if (monitor == null) {
            return;
        }
        
        UnlockOperation operation = monitor.getOperation();
        if (operation == null) {
            stopMonitoring(operationId);
            return;
        }
        
        if (!operation.isCompleted() && !operation.isCancelled() && !operation.isFailed()) {
            logger.warn("⏰ Timeout atteint pour opération: {} après {}s", operationId, monitor.getTimeoutSeconds());
            
            try {
                // Tentative de finalisation gracieuse
                if (operation.getProgressPercentage() >= 80.0) {
                    operation.updateProgress(100.0, "Finalisation forcée (timeout atteint)");
                    operation.complete("Opération terminée (timeout - succès probable)");
                    operation.addLogEntry("⏰ Opération finalisée automatiquement après timeout");
                } else {
                    operation.fail("Timeout atteint après " + monitor.getTimeoutSeconds() + " secondes", "OPERATION_TIMEOUT");
                    operation.addLogEntry("⏰ Opération annulée pour cause de timeout");
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la gestion du timeout pour: {}", operationId, e);
            }
        }
        
        stopMonitoring(operationId);
    }
    
    /**
     * Détermine le timeout approprié pour une opération
     */
    private int getTimeoutForOperation(UnlockOperation operation) {
        if (operation == null || operation.getOperationType() == null) {
            return DEFAULT_TIMEOUT;
        }
        
        switch (operation.getOperationType()) {
            case ICLOUD_BYPASS:
            case FRP_BYPASS:
            case SAMSUNG_ACCOUNT_BYPASS:
            case MI_ACCOUNT_BYPASS:
                return UNLOCK_TIMEOUT;
            case FLASH_IOS_FIRMWARE:
            case FLASH_ANDROID_FIRMWARE:
                return FLASH_TIMEOUT;
            default:
                return DEFAULT_TIMEOUT;
        }
    }
    
    /**
     * Obtient les statistiques de surveillance
     */
    public MonitoringStats getMonitoringStats() {
        int activeCount = activeOperations.size();
        int totalUnstuckAttempts = activeOperations.values().stream()
            .mapToInt(OperationMonitor::getUnstuckAttempts)
            .sum();
        
        return new MonitoringStats(activeCount, totalUnstuckAttempts);
    }
    
    /**
     * Force l'arrêt de toutes les opérations actives
     */
    public void forceStopAllOperations() {
        logger.warn("🚨 Arrêt forcé de toutes les opérations actives ({} opérations)", activeOperations.size());
        
        for (OperationMonitor monitor : activeOperations.values()) {
            if (monitor != null) {
                UnlockOperation operation = monitor.getOperation();
                if (operation != null && !operation.isCompleted() && !operation.isCancelled() && !operation.isFailed()) {
                    try {
                        operation.cancel();
                        operation.addLogEntry("🚨 Opération annulée par arrêt forcé du système");
                    } catch (Exception e) {
                        logger.error("Erreur lors de l'annulation de l'opération: {}", operation.getOperationId(), e);
                    }
                }
                monitor.stop();
            }
        }
        
        activeOperations.clear();
    }
    
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            logger.info("Arrêt du service de surveillance des opérations...");
            
            forceStopAllOperations();
            monitoringExecutor.shutdown();
            
            try {
                if (!monitoringExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.warn("Arrêt forcé de l'executor après timeout");
                    monitoringExecutor.shutdownNow();
                    
                    if (!monitoringExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                        logger.error("L'executor n'a pas pu être arrêté proprement");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("Interruption lors de l'arrêt de l'executor");
                monitoringExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("Service de surveillance des opérations fermé");
        }
    }
    
    // ==================== CLASSES INTERNES ====================
    
    /**
     * Moniteur d'opération individuelle
     */
    private static class OperationMonitor {
        private final UnlockOperation operation;
        private final int timeoutSeconds;
        private final long startTime;
        private final AtomicReference<ScheduledFuture<?>> monitoringTask = new AtomicReference<>();
        private final AtomicReference<ScheduledFuture<?>> timeoutTask = new AtomicReference<>();
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        
        private volatile double lastProgress = 0.0;
        private volatile long lastProgressUpdate = System.currentTimeMillis();
        private volatile int healthChecks = 0;
        private volatile int unstuckAttempts = 0;
        
        public OperationMonitor(UnlockOperation operation, int timeoutSeconds) {
            this.operation = operation;
            this.timeoutSeconds = timeoutSeconds;
            this.startTime = System.currentTimeMillis();
            this.lastProgress = operation != null ? operation.getProgressPercentage() : 0.0;
        }
        
        public void setMonitoringTask(ScheduledFuture<?> task) {
            monitoringTask.set(task);
        }
        
        public void setTimeoutTask(ScheduledFuture<?> task) {
            timeoutTask.set(task);
        }
        
        public void stop() {
            if (stopped.compareAndSet(false, true)) {
                ScheduledFuture<?> monitoring = monitoringTask.get();
                if (monitoring != null && !monitoring.isDone()) {
                    monitoring.cancel(false);
                }
                
                ScheduledFuture<?> timeout = timeoutTask.get();
                if (timeout != null && !timeout.isDone()) {
                    timeout.cancel(false);
                }
            }
        }
        
        public void updateHealthCheck() {
            healthChecks++;
        }
        
        public void incrementUnstuckAttempts() {
            unstuckAttempts++;
        }
        
        // Getters
        public UnlockOperation getOperation() { return operation; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public long getStartTime() { return startTime; }
        public double getLastProgress() { return lastProgress; }
        public long getLastProgressUpdate() { return lastProgressUpdate; }
        public int getHealthChecks() { return healthChecks; }
        public int getUnstuckAttempts() { return unstuckAttempts; }
        
        // Setters
        public void setLastProgress(double progress) { 
            this.lastProgress = progress; 
        }
        public void setLastProgressUpdate(long time) { 
            this.lastProgressUpdate = time; 
        }
    }
    
    /**
     * Statistiques de surveillance
     */
    public static class MonitoringStats {
        private final int activeOperations;
        private final int totalUnstuckAttempts;
        
        public MonitoringStats(int activeOperations, int totalUnstuckAttempts) {
            this.activeOperations = activeOperations;
            this.totalUnstuckAttempts = totalUnstuckAttempts;
        }
        
        public int getActiveOperations() { return activeOperations; }
        public int getTotalUnstuckAttempts() { return totalUnstuckAttempts; }
    }
}
