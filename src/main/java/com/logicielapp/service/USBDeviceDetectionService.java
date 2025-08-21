package com.logicielapp.service;

import com.logicielapp.model.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service pour détecter automatiquement les appareils iOS connectés via USB
 * Utilise libimobiledevice (ideviceinfo) pour récupérer les informations
 */
public class USBDeviceDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(USBDeviceDetectionService.class);
    
    // Patterns pour extraire les informations de ideviceinfo
    private static final Pattern DEVICE_NAME_PATTERN = Pattern.compile("DeviceName: (.+)");
    private static final Pattern PRODUCT_TYPE_PATTERN = Pattern.compile("ProductType: (.+)");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("SerialNumber: (.+)");
    private static final Pattern IMEI_PATTERN = Pattern.compile("InternationalMobileEquipmentIdentity: (.+)");
    private static final Pattern VERSION_PATTERN = Pattern.compile("ProductVersion: (.+)");
    private static final Pattern BATTERY_PATTERN = Pattern.compile("BatteryCurrentCapacity: (.+)");
    private static final Pattern UDID_PATTERN = Pattern.compile("UniqueDeviceID: (.+)");
    
    /**
     * Détecte automatiquement un appareil iOS connecté via USB
     * @return CompletableFuture contenant les informations de l'appareil ou null si aucun appareil
     */
    public CompletableFuture<Device> detectConnectediOSDevice() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Démarrage de la détection d'appareil iOS via USB...");
                
                // Vérifier d'abord si des appareils iOS sont connectés
                if (!isiOSDeviceConnected()) {
                    logger.info("Aucun appareil iOS détecté via USB");
                    return null;
                }
                
                // Exécuter ideviceinfo pour récupérer les informations détaillées
                ProcessBuilder builder = new ProcessBuilder("ideviceinfo");
                builder.redirectErrorStream(true);
                Process process = builder.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                Map<String, String> deviceData = new HashMap<>();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    extractDeviceInfo(line, deviceData);
                }
                
                int exitCode = process.waitFor();
                
                if (exitCode == 0 && !deviceData.isEmpty()) {
                    Device device = createDeviceFromData(deviceData);
                    logger.info("Appareil iOS détecté avec succès: {} - IMEI: {}", 
                              device.getModel(), maskIMEI(device.getImei()));
                    return device;
                } else {
                    logger.warn("Échec de la détection d'appareil iOS (exit code: {})", exitCode);
                    return null;
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de la détection d'appareil iOS", e);
                return null;
            }
        });
    }
    
    /**
     * Vérifie si un appareil iOS est connecté via USB
     * @return true si un appareil iOS est détecté
     */
    public boolean isiOSDeviceConnected() {
        try {
            // Utiliser idevice_id pour vérifier la présence d'appareils
            ProcessBuilder builder = new ProcessBuilder("idevice_id", "-l");
            builder.redirectErrorStream(true);
            Process process = builder.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            
            int exitCode = process.waitFor();
            
            // Si idevice_id retourne une ligne non vide, un appareil est connecté
            boolean deviceConnected = exitCode == 0 && line != null && !line.trim().isEmpty();
            
            logger.debug("Vérification de connexion iOS: {} (exit code: {})", deviceConnected, exitCode);
            return deviceConnected;
            
        } catch (Exception e) {
            logger.debug("Outil idevice_id non disponible ou erreur: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrait les informations d'appareil d'une ligne de sortie ideviceinfo
     * @param line ligne de sortie
     * @param deviceData map pour stocker les données extraites
     */
    private void extractDeviceInfo(String line, Map<String, String> deviceData) {
        Matcher matcher;
        
        if ((matcher = DEVICE_NAME_PATTERN.matcher(line)).find()) {
            deviceData.put("deviceName", matcher.group(1).trim());
        } else if ((matcher = PRODUCT_TYPE_PATTERN.matcher(line)).find()) {
            deviceData.put("productType", matcher.group(1).trim());
        } else if ((matcher = SERIAL_PATTERN.matcher(line)).find()) {
            deviceData.put("serialNumber", matcher.group(1).trim());
        } else if ((matcher = IMEI_PATTERN.matcher(line)).find()) {
            deviceData.put("imei", matcher.group(1).trim());
        } else if ((matcher = VERSION_PATTERN.matcher(line)).find()) {
            deviceData.put("osVersion", matcher.group(1).trim());
        } else if ((matcher = BATTERY_PATTERN.matcher(line)).find()) {
            deviceData.put("batteryLevel", matcher.group(1).trim());
        } else if ((matcher = UDID_PATTERN.matcher(line)).find()) {
            deviceData.put("udid", matcher.group(1).trim());
        }
    }
    
    /**
     * Crée un objet Device à partir des données extraites
     * @param deviceData données extraites de ideviceinfo
     * @return objet Device configuré
     */
    private Device createDeviceFromData(Map<String, String> deviceData) {
        Device device = new Device();
        
        // Informations de base
        device.setImei(deviceData.get("imei"));
        device.setSerialNumber(deviceData.get("serialNumber"));
        device.setUdid(deviceData.get("udid"));
        device.setPlatform(Device.Platform.iOS);
        
        // Déterminer le modèle à partir du ProductType
        String productType = deviceData.get("productType");
        String model = mapProductTypeToModel(productType);
        device.setModel(model);
        device.setBrand("Apple");
        
        // Version iOS
        String osVersion = deviceData.get("osVersion");
        if (osVersion != null) {
            device.setOsVersion("iOS " + osVersion);
        }
        
        // Nom de l'appareil
        String deviceName = deviceData.get("deviceName");
        if (deviceName != null) {
            device.setDeviceName(deviceName);
        }
        
        // Niveau de batterie (si disponible)
        String batteryLevel = deviceData.get("batteryLevel");
        if (batteryLevel != null) {
            try {
                device.setBatteryLevel(Integer.parseInt(batteryLevel));
            } catch (NumberFormatException e) {
                logger.debug("Impossible de parser le niveau de batterie: {}", batteryLevel);
            }
        }
        
        // Marquer comme connecté via USB
        device.setConnectionType(Device.ConnectionType.USB);
        device.setConnected(true);
        
        return device;
    }
    
    /**
     * Mappe le ProductType d'Apple vers un nom de modèle lisible
     * @param productType ProductType de l'appareil (ex: iPhone13,4)
     * @return nom du modèle lisible
     */
    private String mapProductTypeToModel(String productType) {
        if (productType == null) {
            return "iPhone (modèle inconnu)";
        }
        
        // Mapping des ProductType vers les noms de modèles
        Map<String, String> productTypeMap = new HashMap<>();
        
        // iPhone 15 Series
        productTypeMap.put("iPhone16,1", "iPhone 15");
        productTypeMap.put("iPhone16,2", "iPhone 15 Plus");
        productTypeMap.put("iPhone15,4", "iPhone 15 Pro");
        productTypeMap.put("iPhone15,5", "iPhone 15 Pro Max");
        
        // iPhone 14 Series
        productTypeMap.put("iPhone14,7", "iPhone 14");
        productTypeMap.put("iPhone14,8", "iPhone 14 Plus");
        productTypeMap.put("iPhone15,2", "iPhone 14 Pro");
        productTypeMap.put("iPhone15,3", "iPhone 14 Pro Max");
        
        // iPhone 13 Series
        productTypeMap.put("iPhone14,4", "iPhone 13 mini");
        productTypeMap.put("iPhone14,5", "iPhone 13");
        productTypeMap.put("iPhone14,2", "iPhone 13 Pro");
        productTypeMap.put("iPhone14,3", "iPhone 13 Pro Max");
        
        // iPhone 12 Series
        productTypeMap.put("iPhone13,1", "iPhone 12 mini");
        productTypeMap.put("iPhone13,2", "iPhone 12");
        productTypeMap.put("iPhone13,3", "iPhone 12 Pro");
        productTypeMap.put("iPhone13,4", "iPhone 12 Pro Max");
        
        // iPhone 11 Series
        productTypeMap.put("iPhone12,1", "iPhone 11");
        productTypeMap.put("iPhone12,3", "iPhone 11 Pro");
        productTypeMap.put("iPhone12,5", "iPhone 11 Pro Max");
        
        // iPhone XS/XR Series
        productTypeMap.put("iPhone11,2", "iPhone XS");
        productTypeMap.put("iPhone11,4", "iPhone XS Max");
        productTypeMap.put("iPhone11,6", "iPhone XS Max");
        productTypeMap.put("iPhone11,8", "iPhone XR");
        
        // iPhone X
        productTypeMap.put("iPhone10,3", "iPhone X");
        productTypeMap.put("iPhone10,6", "iPhone X");
        
        // iPhone 8/8 Plus
        productTypeMap.put("iPhone10,1", "iPhone 8");
        productTypeMap.put("iPhone10,4", "iPhone 8");
        productTypeMap.put("iPhone10,2", "iPhone 8 Plus");
        productTypeMap.put("iPhone10,5", "iPhone 8 Plus");
        
        // iPhone SE
        productTypeMap.put("iPhone8,4", "iPhone SE (1st generation)");
        productTypeMap.put("iPhone12,8", "iPhone SE (2nd generation)");
        productTypeMap.put("iPhone14,6", "iPhone SE (3rd generation)");
        
        return productTypeMap.getOrDefault(productType, productType + " (modèle non reconnu)");
    }
    
    /**
     * Récupère l'IMEI d'un appareil iOS connecté via USB
     * @return CompletableFuture contenant l'IMEI ou null si non trouvé
     */
    public CompletableFuture<String> getConnectedDeviceIMEI() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Récupération de l'IMEI de l'appareil connecté...");
                
                ProcessBuilder builder = new ProcessBuilder("ideviceinfo", "-k", "InternationalMobileEquipmentIdentity");
                builder.redirectErrorStream(true);
                Process process = builder.start();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String imei = reader.readLine();
                
                int exitCode = process.waitFor();
                
                if (exitCode == 0 && imei != null && !imei.trim().isEmpty()) {
                    imei = imei.trim();
                    logger.info("IMEI récupéré avec succès: {}", maskIMEI(imei));
                    return imei;
                } else {
                    logger.warn("Impossible de récupérer l'IMEI (exit code: {})", exitCode);
                    return null;
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de la récupération de l'IMEI", e);
                return null;
            }
        });
    }
    
    /**
     * Vérifie si libimobiledevice est installé sur le système
     * @return true si les outils sont disponibles
     */
    public boolean isLibimobiledeviceAvailable() {
        try {
            ProcessBuilder builder = new ProcessBuilder("which", "ideviceinfo");
            Process process = builder.start();
            int exitCode = process.waitFor();
            
            boolean available = exitCode == 0;
            logger.debug("libimobiledevice disponible: {}", available);
            
            return available;
            
        } catch (Exception e) {
            logger.debug("Erreur lors de la vérification de libimobiledevice: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Masque partiellement l'IMEI pour les logs (sécurité)
     * @param imei IMEI complet
     * @return IMEI masqué
     */
    private String maskIMEI(String imei) {
        if (imei == null || imei.length() < 15) {
            return "IMEI_INVALIDE";
        }
        return imei.substring(0, 6) + "XXXXX" + imei.substring(11);
    }
}
