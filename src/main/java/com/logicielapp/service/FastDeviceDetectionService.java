package com.logicielapp.service;

import com.logicielapp.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Service de détection ultra-rapide sans dépendances externes
 */
public class FastDeviceDetectionService {
    private static final Logger logger = LoggerFactory.getLogger(FastDeviceDetectionService.class);
    
    // Cache ultra-rapide
    private Device cachedDevice = null;
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 100; // 100ms de cache pour détecter rapidement les déconnexions
    
    public FastDeviceDetectionService() {
        // Pas de simulation - uniquement détection réelle
    }
    
    /**
     * Détection ultra-rapide avec vérification de déconnexion
     */
    public List<Device> detectAllConnectedDevices() {
        List<Device> devices = new ArrayList<>();
        
        try {
            long currentTime = System.currentTimeMillis();
            
            // Toujours vérifier la connexion réelle, même avec cache
            Device realDevice = detectRealDeviceQuick();
            
            if (realDevice != null) {
                // Appareil détecté - mettre à jour le cache
                cachedDevice = realDevice;
                lastCacheTime = currentTime;
                devices.add(realDevice);
                logger.debug("✅ Appareil connecté détecté");
            } else {
                // Aucun appareil détecté - invalider le cache
                if (cachedDevice != null) {
                    logger.info("📱 Appareil déconnecté détecté");
                    cachedDevice = null;
                    lastCacheTime = 0;
                }
                logger.debug("❌ Aucun appareil connecté");
            }
            
        } catch (Exception e) {
            logger.debug("Erreur détection rapide: {}", e.getMessage());
            // En cas d'erreur, invalider le cache pour éviter les faux positifs
            cachedDevice = null;
            lastCacheTime = 0;
        }
        
        return devices;
    }
    
    /**
     * Détection réelle avec extraction des vraies informations
     */
    private Device detectRealDeviceQuick() {
        // Priorité 1: Détecter les appareils iOS avec idevice_id
        Device iosDevice = detectRealIOSDevice();
        if (iosDevice != null) {
            return iosDevice;
        }
        
        // Priorité 2: Détecter les appareils Android avec adb
        Device androidDevice = detectRealAndroidDevice();
        if (androidDevice != null) {
            return androidDevice;
        }
        
        // Priorité 3: Fallback avec system_profiler (plus fiable sans erreurs)
        return detectViaSystemProfiler();
    }
    
    /**
     * Détection spécifique iOS avec libimobiledevice
     */
    private Device detectRealIOSDevice() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/idevice_id", "-l");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String udid = reader.readLine();
            int exitCode = process.waitFor();
            process.destroy();
            
            if (exitCode == 0 && udid != null && !udid.trim().isEmpty()) {
                logger.info("✅ Appareil iOS détecté avec UDID: {}", udid.trim());
                return createRealIOSDevice(udid.trim());
            }
        } catch (Exception e) {
            logger.debug("Détection iOS échouée: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Détection spécifique Android avec ADB
     */
    private Device detectRealAndroidDevice() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Caskroom/android-platform-tools/36.0.0/platform-tools/adb", "devices");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.contains("device") && !line.startsWith("List of devices")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2 && "device".equals(parts[1])) {
                        String deviceId = parts[0];
                        logger.info("✅ Appareil Android détecté avec ID: {}", deviceId);
                        return createRealAndroidDevice(deviceId);
                    }
                }
            }
            
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            logger.debug("Détection Android échouée: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Fallback avec system_profiler (version simplifiée)
     */
    private Device detectViaSystemProfiler() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ioreg", "-p", "IOUSB", "-w0", "-l");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("iphone") || 
                    line.toLowerCase().contains("ipad") ||
                    line.toLowerCase().contains("android")) {
                    
                    logger.info("✅ Appareil mobile détecté via ioreg");
                    return createGenericMobileDevice(line);
                }
            }
            
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            logger.debug("Détection ioreg échouée: {}", e.getMessage());
        }
        
        logger.debug("❌ Aucun appareil mobile réellement connecté");
        return null;
    }
    
    /**
     * Extrait le vrai modèle de l'appareil
     */
    private String extractRealModel(String deviceInfo, String productId) {
        try {
            // Utiliser ideviceinfo si disponible pour obtenir le vrai modèle
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/ideviceinfo", "-k", "ProductType");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String productType = reader.readLine();
            process.destroy();
            
            if (productType != null && !productType.isEmpty()) {
                return mapProductTypeToModel(productType.trim());
            }
        } catch (Exception e) {
            logger.debug("ideviceinfo non disponible, utilisation fallback");
        }
        
        // Fallback: extraire le modèle exact depuis system_profiler
        if (deviceInfo != null && !deviceInfo.isEmpty()) {
            // Nettoyer et extraire le nom exact de l'appareil
            String cleanedInfo = deviceInfo.trim();
            if (cleanedInfo.contains(":")) {
                cleanedInfo = cleanedInfo.split(":")[0].trim();
            }
            return cleanedInfo;
        }
        
        return "Appareil iOS non identifié";
    }
    
    /**
     * Extrait la vraie version iOS
     */
    private String extractRealIOSVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/ideviceinfo", "-k", "ProductVersion");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            process.destroy();
            
            if (version != null && !version.isEmpty()) {
                return "iOS " + version.trim();
            }
        } catch (Exception e) {
            logger.debug("Impossible d'extraire la version iOS réelle");
        }
        
        return "iOS (version en cours de détection)";
    }
    
    /**
     * Extrait le vrai numéro de série
     */
    private String extractRealSerialNumber() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/ideviceinfo", "-k", "SerialNumber");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String serial = reader.readLine();
            process.destroy();
            
            if (serial != null && !serial.isEmpty()) {
                return serial.trim();
            }
        } catch (Exception e) {
            logger.debug("Impossible d'extraire le numéro de série réel");
        }
        
        return "";
    }
    
    /**
     * Extrait le vrai IMEI
     */
    private String extractRealIMEI() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/ideviceinfo", "-k", "InternationalMobileEquipmentIdentity");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String imei = reader.readLine();
            process.destroy();
            
            if (imei != null && !imei.isEmpty()) {
                return imei.trim();
            }
        } catch (Exception e) {
            logger.debug("Impossible d'extraire l'IMEI réel");
        }
        
        return "IMEI non accessible (outils manquants)";
    }
    
    /**
     * Extrait le vrai UDID
     */
    private String extractRealUDID() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/idevice_id", "-l");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String udid = reader.readLine();
            process.destroy();
            
            if (udid != null && !udid.isEmpty()) {
                return udid.trim();
            }
        } catch (Exception e) {
            logger.debug("Impossible d'extraire l'UDID réel");
        }
        
        return "UDID non accessible (outils manquants)";
    }
    
    /**
     * Mappe le ProductType vers un nom de modèle lisible
     */
    private String mapProductTypeToModel(String productType) {
        Map<String, String> modelMap = new HashMap<>();
        // Mapping des ProductType vers les modèles iPhone
        modelMap.put("iPhone12,1", "iPhone 11");
        modelMap.put("iPhone12,3", "iPhone 11 Pro");
        modelMap.put("iPhone12,5", "iPhone 11 Pro Max");
        modelMap.put("iPhone13,1", "iPhone 12 mini");
        modelMap.put("iPhone13,2", "iPhone 12");
        modelMap.put("iPhone13,3", "iPhone 12 Pro");
        modelMap.put("iPhone13,4", "iPhone 12 Pro Max");
        modelMap.put("iPhone14,4", "iPhone 13 mini");
        modelMap.put("iPhone14,5", "iPhone 13");
        modelMap.put("iPhone14,2", "iPhone 13 Pro");
        modelMap.put("iPhone14,3", "iPhone 13 Pro Max");
        modelMap.put("iPhone14,7", "iPhone 14");
        modelMap.put("iPhone14,8", "iPhone 14 Plus");
        modelMap.put("iPhone15,2", "iPhone 14 Pro");
        modelMap.put("iPhone15,3", "iPhone 14 Pro Max");
        modelMap.put("iPhone15,4", "iPhone 15");
        modelMap.put("iPhone15,5", "iPhone 15 Plus");
        modelMap.put("iPhone16,1", "iPhone 15 Pro");
        modelMap.put("iPhone16,2", "iPhone 15 Pro Max");
        
        return modelMap.getOrDefault(productType, productType + " (iPhone)");
    }
    
    
    
    /**
     * Extrait le niveau de batterie réel
     */
    public String extractRealBatteryLevel() {
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Cellar/libimobiledevice/1.3.0_3/bin/ideviceinfo", "-k", "BatteryCurrentCapacity");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String batteryLevel = reader.readLine();
            process.destroy();
            
            if (batteryLevel != null && !batteryLevel.isEmpty()) {
                return batteryLevel.trim() + "%";
            }
        } catch (Exception e) {
            logger.debug("Impossible d'extraire le niveau de batterie réel");
        }
        
        return "Batterie non accessible (outils manquants)";
    }
    
    /**
     * Extrait la capacité de stockage réelle exacte de l'appareil
     */
    public String extractRealStorageCapacity() {
        // Pour iOS, la capacité exacte n'est pas accessible via les APIs publiques
        // Apple ne permet pas l'accès direct aux informations de stockage pour des raisons de sécurité
        // Seules les applications système peuvent accéder à ces données
        
        logger.debug("Tentative d'extraction de la capacité de stockage exacte");
        return "Capacité exacte protégée par iOS";
    }
    
    
    
    /**
     * Crée un objet Device pour un appareil iOS réel
     */
    private Device createRealIOSDevice(String udid) {
        Device device = new Device();
        device.setBrand("Apple");
        device.setPlatform(Device.Platform.iOS);
        device.setStatus(Device.DeviceStatus.CONNECTED);
        device.setConnectionType(Device.ConnectionType.USB);
        device.setUdid(udid);
        
        // Extraire les vraies informations avec ideviceinfo
        device.setModel(extractRealModel("", ""));
        device.setSerialNumber(extractRealSerialNumber());
        device.setImei(extractRealIMEI());
        device.setOsVersion(extractRealIOSVersion());
        device.setBatteryLevel(extractRealBatteryLevel());
        device.setStorageCapacity(extractRealStorageCapacity());
        
        return device;
    }
    
    /**
     * Crée un objet Device pour un appareil Android réel
     */
    private Device createRealAndroidDevice(String deviceId) {
        Device device = new Device();
        device.setPlatform(Device.Platform.ANDROID);
        device.setStatus(Device.DeviceStatus.CONNECTED);
        device.setConnectionType(Device.ConnectionType.USB);
        device.setSerialNumber(deviceId);
        
        // Extraire les informations Android avec adb
        try {
            ProcessBuilder pb = new ProcessBuilder("/usr/local/Caskroom/android-platform-tools/36.0.0/platform-tools/adb", "-s", deviceId, "shell", "getprop", "ro.product.model");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String model = reader.readLine();
            process.waitFor();
            process.destroy();
            
            if (model != null && !model.trim().isEmpty()) {
                device.setModel(model.trim());
            } else {
                device.setModel("Appareil Android");
            }
            
            // Obtenir la marque
            pb = new ProcessBuilder("/usr/local/Caskroom/android-platform-tools/36.0.0/platform-tools/adb", "-s", deviceId, "shell", "getprop", "ro.product.brand");
            process = pb.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String brand = reader.readLine();
            process.waitFor();
            process.destroy();
            
            device.setBrand(brand != null && !brand.trim().isEmpty() ? brand.trim() : "Android");
            
            // Version Android
            pb = new ProcessBuilder("/usr/local/Caskroom/android-platform-tools/36.0.0/platform-tools/adb", "-s", deviceId, "shell", "getprop", "ro.build.version.release");
            process = pb.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String version = reader.readLine();
            process.waitFor();
            process.destroy();
            
            device.setOsVersion(version != null && !version.trim().isEmpty() ? 
                              "Android " + version.trim() : "Android");
            
        } catch (Exception e) {
            logger.debug("Erreur extraction informations Android: {}", e.getMessage());
            device.setModel("Appareil Android");
            device.setBrand("Android");
            device.setOsVersion("Android");
        }
        
        device.setImei("Non accessible via ADB");
        device.setBatteryLevel("Non accessible via ADB");
        device.setStorageCapacity("Non accessible via ADB");
        
        return device;
    }
    
    /**
     * Crée un objet Device générique pour un appareil mobile détecté
     */
    private Device createGenericMobileDevice(String deviceLine) {
        Device device = new Device();
        device.setStatus(Device.DeviceStatus.CONNECTED);
        device.setConnectionType(Device.ConnectionType.USB);
        
        if (deviceLine.toLowerCase().contains("iphone")) {
            device.setBrand("Apple");
            device.setPlatform(Device.Platform.iOS);
            device.setModel("iPhone (détecté via USB)");
            device.setOsVersion("iOS");
        } else if (deviceLine.toLowerCase().contains("ipad")) {
            device.setBrand("Apple");
            device.setPlatform(Device.Platform.iOS);
            device.setModel("iPad (détecté via USB)");
            device.setOsVersion("iPadOS");
        } else {
            device.setBrand("Inconnu");
            device.setPlatform(Device.Platform.ANDROID);
            device.setModel("Appareil mobile (détecté via USB)");
            device.setOsVersion("Système inconnu");
        }
        
        device.setSerialNumber("Non accessible");
        device.setImei("Non accessible");
        device.setBatteryLevel("Non accessible");
        device.setStorageCapacity("Non accessible");
        
        return device;
    }

    /**
     * Invalide le cache pour forcer une nouvelle détection
     */
    public void invalidateCache() {
        cachedDevice = null;
        lastCacheTime = 0;
        logger.debug("Cache invalidé");
    }
}
