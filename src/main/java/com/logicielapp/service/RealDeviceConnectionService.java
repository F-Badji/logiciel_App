package com.logicielapp.service;

import com.logicielapp.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service de connexion réelle aux appareils physiques
 * Remplace les simulations par de vraies détections USB/ADB
 */
public class RealDeviceConnectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(RealDeviceConnectionService.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    // Patterns pour l'analyse des sorties système
    private static final Pattern IOS_DEVICE_PATTERN = Pattern.compile("([a-f0-9]{40})\\s+(.+)");
    private static final Pattern ANDROID_DEVICE_PATTERN = Pattern.compile("([A-Za-z0-9]+)\\s+device");
    
    /**
     * Détection réelle des appareils iOS connectés via USB
     */
    public CompletableFuture<List<Device>> detectRealIOSDevices() {
        return CompletableFuture.supplyAsync(() -> {
            List<Device> devices = new ArrayList<>();
            
            try {
                logger.info("Détection des appareils iOS réels...");
                
                // Vérifier si libimobiledevice est installé
                if (!isLibimobiledeviceInstalled()) {
                    logger.warn("libimobiledevice n'est pas installé. Installation requise pour la détection iOS.");
                    return devices;
                }
                
                // Lister les appareils iOS connectés
                ProcessBuilder pb = new ProcessBuilder("idevice_id", "-l");
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String udid;
                while ((udid = reader.readLine()) != null) {
                    if (!udid.trim().isEmpty()) {
                        Device device = createIOSDeviceFromUDID(udid.trim());
                        if (device != null) {
                            devices.add(device);
                            logger.info("Appareil iOS détecté: {} ({})", device.getModel(), device.getSerialNumber());
                        }
                    }
                }
                
                process.waitFor();
                
            } catch (Exception e) {
                logger.error("Erreur lors de la détection iOS réelle", e);
            }
            
            return devices;
        }, executorService);
    }
    
    /**
     * Détection réelle des appareils Android connectés via ADB
     */
    public CompletableFuture<List<Device>> detectRealAndroidDevices() {
        return CompletableFuture.supplyAsync(() -> {
            List<Device> devices = new ArrayList<>();
            
            try {
                logger.info("Détection des appareils Android réels...");
                
                // Vérifier si ADB est installé et démarré
                if (!isADBInstalled()) {
                    logger.warn("ADB n'est pas installé. Installation requise pour la détection Android.");
                    return devices;
                }
                
                // Démarrer le serveur ADB si nécessaire
                startADBServer();
                
                // Lister les appareils Android connectés
                ProcessBuilder pb = new ProcessBuilder("adb", "devices");
                Process process = pb.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                String line;
                boolean headerPassed = false;
                while ((line = reader.readLine()) != null) {
                    if (!headerPassed) {
                        if (line.contains("List of devices attached")) {
                            headerPassed = true;
                        }
                        continue;
                    }
                    
                    Matcher matcher = ANDROID_DEVICE_PATTERN.matcher(line);
                    if (matcher.find()) {
                        String deviceId = matcher.group(1);
                        Device device = createAndroidDeviceFromId(deviceId);
                        if (device != null) {
                            devices.add(device);
                            logger.info("Appareil Android détecté: {} ({})", device.getModel(), device.getSerialNumber());
                        }
                    }
                }
                
                process.waitFor();
                
            } catch (Exception e) {
                logger.error("Erreur lors de la détection Android réelle", e);
            }
            
            return devices;
        }, executorService);
    }
    
    /**
     * Détection combinée de tous les appareils connectés
     */
    public CompletableFuture<List<Device>> detectAllRealDevices() {
        return CompletableFuture.supplyAsync(() -> {
            List<Device> allDevices = new ArrayList<>();
            
            try {
                // Détecter iOS et Android en parallèle
                CompletableFuture<List<Device>> iosDevices = detectRealIOSDevices();
                CompletableFuture<List<Device>> androidDevices = detectRealAndroidDevices();
                
                // Attendre les deux résultats
                allDevices.addAll(iosDevices.get());
                allDevices.addAll(androidDevices.get());
                
                logger.info("Détection terminée: {} appareils trouvés", allDevices.size());
                
            } catch (Exception e) {
                logger.error("Erreur lors de la détection combinée", e);
            }
            
            return allDevices;
        }, executorService);
    }
    
    /**
     * Test de connexion réelle à un appareil spécifique
     */
    public boolean testRealDeviceConnection(Device device) {
        try {
            if (device.isIOS()) {
                return testIOSConnection(device);
            } else if (device.isAndroid()) {
                return testAndroidConnection(device);
            }
            return false;
        } catch (Exception e) {
            logger.error("Erreur test connexion appareil", e);
            return false;
        }
    }
    
    // ==================== MÉTHODES D'IMPLÉMENTATION RÉELLE ====================
    
    private boolean isLibimobiledeviceInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "idevice_id");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isADBInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "adb");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void startADBServer() {
        try {
            ProcessBuilder pb = new ProcessBuilder("adb", "start-server");
            Process process = pb.start();
            process.waitFor();
        } catch (Exception e) {
            logger.warn("Impossible de démarrer le serveur ADB", e);
        }
    }
    
    private Device createIOSDeviceFromUDID(String udid) {
        try {
            Device device = new Device();
            device.setSerialNumber(udid);
            device.setPlatform(Device.Platform.iOS);
            device.setConnectionType(Device.ConnectionType.USB);
            device.setConnected(true);
            
            // Récupérer les informations détaillées
            String deviceName = getIOSDeviceInfo(udid, "DeviceName");
            String productType = getIOSDeviceInfo(udid, "ProductType");
            String osVersion = getIOSDeviceInfo(udid, "ProductVersion");
            String imei = getIOSDeviceInfo(udid, "InternationalMobileEquipmentIdentity");
            
            device.setModel(deviceName != null ? deviceName : productType);
            device.setOsVersion(osVersion);
            device.setImei(imei);
            device.setBrand("Apple");
            
            // Déterminer le modèle spécifique à partir du ProductType
            if (productType != null) {
                device.setModel(mapIOSProductTypeToModel(productType));
            }
            
            return device;
            
        } catch (Exception e) {
            logger.error("Erreur création appareil iOS", e);
            return null;
        }
    }
    
    private Device createAndroidDeviceFromId(String deviceId) {
        try {
            Device device = new Device();
            device.setSerialNumber(deviceId);
            device.setPlatform(Device.Platform.ANDROID);
            device.setConnectionType(Device.ConnectionType.USB);
            device.setConnected(true);
            
            // Récupérer les informations détaillées via ADB
            String brand = getAndroidDeviceProperty(deviceId, "ro.product.brand");
            String model = getAndroidDeviceProperty(deviceId, "ro.product.model");
            String osVersion = getAndroidDeviceProperty(deviceId, "ro.build.version.release");
            String imei = getAndroidDeviceProperty(deviceId, "ro.ril.oem.imei");
            
            device.setBrand(brand != null ? brand : "Unknown");
            device.setModel(model != null ? model : "Unknown Android Device");
            device.setOsVersion(osVersion);
            device.setImei(imei);
            
            return device;
            
        } catch (Exception e) {
            logger.error("Erreur création appareil Android", e);
            return null;
        }
    }
    
    private String getIOSDeviceInfo(String udid, String key) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"ideviceinfo", "-u", udid, "-k", key});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            process.waitFor();
            return result != null ? result.trim() : null;
        } catch (Exception e) {
            logger.debug("Impossible de récupérer {} pour {}", key, udid);
            return null;
        }
    }
    
    private String getAndroidDeviceProperty(String deviceId, String property) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"adb", "-s", deviceId, "shell", "getprop", property});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            process.waitFor();
            return result != null && !result.trim().isEmpty() ? result.trim() : null;
        } catch (Exception e) {
            logger.debug("Impossible de récupérer {} pour {}", property, deviceId);
            return null;
        }
    }
    
    private String mapIOSProductTypeToModel(String productType) {
        // Mapping des ProductType vers les noms de modèles
        switch (productType) {
            case "iPhone14,7": return "iPhone 14";
            case "iPhone14,8": return "iPhone 14 Plus";
            case "iPhone15,2": return "iPhone 14 Pro";
            case "iPhone15,3": return "iPhone 14 Pro Max";
            case "iPhone13,1": return "iPhone 12 mini";
            case "iPhone13,2": return "iPhone 12";
            case "iPhone13,3": return "iPhone 12 Pro";
            case "iPhone13,4": return "iPhone 12 Pro Max";
            case "iPhone12,1": return "iPhone 11";
            case "iPhone12,3": return "iPhone 11 Pro";
            case "iPhone12,5": return "iPhone 11 Pro Max";
            case "iPhone11,2": return "iPhone XS";
            case "iPhone11,4": return "iPhone XS Max";
            case "iPhone11,6": return "iPhone XS Max";
            case "iPhone11,8": return "iPhone XR";
            case "iPad13,1": return "iPad Air (5th generation)";
            case "iPad13,2": return "iPad Air (5th generation)";
            case "iPad14,1": return "iPad mini (6th generation)";
            case "iPad14,2": return "iPad mini (6th generation)";
            default: return productType; // Retourner le ProductType si non mappé
        }
    }
    
    private boolean testIOSConnection(Device device) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"ideviceinfo", "-u", device.getSerialNumber(), "-k", "DeviceName"});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testAndroidConnection(Device device) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"adb", "-s", device.getSerialNumber(), "shell", "echo", "test"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            process.waitFor();
            return "test".equals(result);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Installation automatique des outils requis (macOS)
     */
    public boolean installRequiredTools() {
        try {
            logger.info("Installation des outils de détection...");
            
            // Vérifier si Homebrew est installé
            if (!isHomebrewInstalled()) {
                logger.warn("Homebrew n'est pas installé. Installation manuelle requise.");
                return false;
            }
            
            // Installer libimobiledevice pour iOS
            if (!isLibimobiledeviceInstalled()) {
                logger.info("Installation de libimobiledevice...");
                ProcessBuilder pb = new ProcessBuilder("brew", "install", "libimobiledevice");
                Process process = pb.start();
                if (process.waitFor() != 0) {
                    logger.error("Échec de l'installation de libimobiledevice");
                    return false;
                }
            }
            
            // Installer platform-tools pour Android
            if (!isADBInstalled()) {
                logger.info("Installation d'Android platform-tools...");
                ProcessBuilder pb = new ProcessBuilder("brew", "install", "android-platform-tools");
                Process process = pb.start();
                if (process.waitFor() != 0) {
                    logger.error("Échec de l'installation d'Android platform-tools");
                    return false;
                }
            }
            
            logger.info("Tous les outils sont installés et prêts");
            return true;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'installation des outils", e);
            return false;
        }
    }
    
    private boolean isHomebrewInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "brew");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        logger.info("Service de connexion réelle fermé");
    }
}
