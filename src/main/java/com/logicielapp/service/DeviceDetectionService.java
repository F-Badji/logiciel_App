package com.logicielapp.service;

import com.logicielapp.model.Device;
import com.logicielapp.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de détection d'appareils mobiles connectés via USB
 * Supporte la détection automatique d'iPhone, iPad et appareils Android
 */
public class DeviceDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceDetectionService.class);
    
    // Vendor IDs connus pour les appareils mobiles
    private static final Map<Integer, String> KNOWN_VENDORS = new HashMap<>();
    private static final Map<String, DeviceInfo> DEVICE_DATABASE = new HashMap<>();
    
    // Cache des appareils détectés
    private final Map<String, Device> detectedDevices = new ConcurrentHashMap<>();
    
    // Context USB4Java
    private Context context;
    private boolean initialized = false;
    
    static {
        // Initialisation des vendor IDs connus
        KNOWN_VENDORS.put(0x05AC, "Apple Inc."); // Apple
        KNOWN_VENDORS.put(0x04E8, "Samsung"); // Samsung
        KNOWN_VENDORS.put(0x12D1, "Huawei"); // Huawei
        KNOWN_VENDORS.put(0x2717, "Xiaomi"); // Xiaomi
        KNOWN_VENDORS.put(0x22B8, "Motorola"); // Motorola
        KNOWN_VENDORS.put(0x0BB4, "HTC"); // HTC
        KNOWN_VENDORS.put(0x054C, "Sony"); // Sony
        KNOWN_VENDORS.put(0x0FCE, "Sony Ericsson"); // Sony Ericsson
        KNOWN_VENDORS.put(0x18D1, "Google"); // Google/Android
        KNOWN_VENDORS.put(0x1004, "LG"); // LG
        KNOWN_VENDORS.put(0x0489, "Foxconn"); // OnePlus/Oppo
        KNOWN_VENDORS.put(0x2A45, "Realme"); // Realme
        KNOWN_VENDORS.put(0x2970, "Infinix"); // Infinix
        
        // Base de données des appareils
        initializeDeviceDatabase();
    }
    
    private static void initializeDeviceDatabase() {
        // Appareils Apple
        DEVICE_DATABASE.put("05AC:12A8", new DeviceInfo("iPhone 5c", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12AB", new DeviceInfo("iPhone 5s", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:129A", new DeviceInfo("iPhone 6", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:129C", new DeviceInfo("iPhone 6 Plus", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A0", new DeviceInfo("iPhone 6s", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A1", new DeviceInfo("iPhone 6s Plus", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:1297", new DeviceInfo("iPhone 7", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:1298", new DeviceInfo("iPhone 7 Plus", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:1299", new DeviceInfo("iPhone 8", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:129B", new DeviceInfo("iPhone 8 Plus", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A6", new DeviceInfo("iPhone X", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A7", new DeviceInfo("iPhone XS", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A4", new DeviceInfo("iPhone XR", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A5", new DeviceInfo("iPhone 11", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A2", new DeviceInfo("iPhone 12", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A3", new DeviceInfo("iPhone 13", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:129D", new DeviceInfo("iPhone 14", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:129E", new DeviceInfo("iPhone 15", "Apple", Device.Platform.iOS));
        
        // iPads
        DEVICE_DATABASE.put("05AC:129F", new DeviceInfo("iPad Air", "Apple", Device.Platform.iOS));
        DEVICE_DATABASE.put("05AC:12A9", new DeviceInfo("iPad Pro", "Apple", Device.Platform.iOS));
        
        // Appareils Samsung génériques (Android détecte automatiquement le modèle)
        DEVICE_DATABASE.put("04E8:6860", new DeviceInfo("Galaxy Series", "Samsung", Device.Platform.ANDROID));
        DEVICE_DATABASE.put("04E8:685D", new DeviceInfo("Galaxy S Series", "Samsung", Device.Platform.ANDROID));
        
        // Appareils génériques Android
        DEVICE_DATABASE.put("18D1:4EE7", new DeviceInfo("Android Device", "Unknown", Device.Platform.ANDROID));
        DEVICE_DATABASE.put("18D1:D002", new DeviceInfo("Android ADB", "Unknown", Device.Platform.ANDROID));
    }
    
    public DeviceDetectionService() {
        initialize();
    }
    
    /**
     * Initialise le service de détection USB
     */
    private void initialize() {
        try {
            context = new Context();
            int result = LibUsb.init(context);
            if (result != LibUsb.SUCCESS) {
                throw new RuntimeException("Erreur d'initialisation libusb: " + LibUsb.errorName(result));
            }
            initialized = true;
            logger.info("Service de détection d'appareils initialisé avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du service de détection", e);
            initialized = false;
        }
    }
    
    /**
     * Scanne les appareils USB connectés et retourne le premier appareil mobile détecté
     */
    public Device scanUSBDevices() {
        if (!initialized) {
            logger.warn("Service non initialisé, tentative de réinitialisation");
            initialize();
            if (!initialized) {
                return null;
            }
        }
        
        try {
            DeviceList deviceList = new DeviceList();
            int result = LibUsb.getDeviceList(context, deviceList);
            
            if (result < 0) {
                logger.error("Erreur lors de la récupération de la liste USB: " + LibUsb.errorName(result));
                return null;
            }
            
            Device foundDevice = null;
            
            for (org.usb4java.Device usbDevice : deviceList) {
                Device mobileDevice = analyzeUSBDevice(usbDevice);
                if (mobileDevice != null) {
                    foundDevice = mobileDevice;
                    break; // Retourner le premier appareil trouvé
                }
            }
            
            LibUsb.freeDeviceList(deviceList, true);
            return foundDevice;
            
        } catch (Exception e) {
            logger.error("Erreur lors du scan USB", e);
            return null;
        }
    }
    
    /**
     * Analyse un périphérique USB pour déterminer s'il s'agit d'un appareil mobile
     */
    private Device analyzeUSBDevice(org.usb4java.Device usbDevice) {
        try {
            DeviceDescriptor descriptor = new DeviceDescriptor();
            int result = LibUsb.getDeviceDescriptor(usbDevice, descriptor);
            
            if (result != LibUsb.SUCCESS) {
                return null;
            }
            
            int vendorId = descriptor.idVendor() & 0xFFFF;
            int productId = descriptor.idProduct() & 0xFFFF;
            String vendorName = KNOWN_VENDORS.get(vendorId);
            
            // Vérifier si c'est un vendor connu pour les mobiles
            if (vendorName == null) {
                return null;
            }
            
            logger.debug("Appareil détecté - Vendor: {} (0x{:04X}), Product: 0x{:04X}", 
                        vendorName, vendorId, productId);
            
            // Créer le device ID pour la base de données
            String deviceId = String.format("%04X:%04X", vendorId, productId);
            DeviceInfo deviceInfo = DEVICE_DATABASE.get(deviceId);
            
            Device device = new Device();
            device.setUsbVendorId(String.format("0x%04X", vendorId));
            device.setUsbProductId(String.format("0x%04X", productId));
            device.setBrand(vendorName);
            device.setStatus(Device.DeviceStatus.CONNECTED);
            device.setConnectionType(Device.ConnectionType.USB);
            
            if (deviceInfo != null) {
                device.setModel(deviceInfo.model);
                device.setPlatform(deviceInfo.platform);
            } else {
                // Détection générique basée sur le vendor
                if (vendorId == 0x05AC) { // Apple
                    device.setModel("iPhone/iPad");
                    device.setPlatform(Device.Platform.iOS);
                } else {
                    device.setModel("Android Device");
                    device.setPlatform(Device.Platform.ANDROID);
                }
            }
            
            // Enrichir avec des informations supplémentaires si possible
            enrichDeviceInfo(device, usbDevice, descriptor);
            
            // Sauvegarder dans le cache
            String cacheKey = device.getUsbVendorId() + ":" + device.getUsbProductId();
            detectedDevices.put(cacheKey, device);
            
            logger.info("Appareil mobile détecté: {} {} ({})", 
                       device.getBrand(), device.getModel(), device.getPlatform());
            
            return device;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'analyse de l'appareil USB", e);
            return null;
        }
    }
    
    /**
     * Enrichit les informations de l'appareil avec des données supplémentaires
     */
    private void enrichDeviceInfo(Device device, org.usb4java.Device usbDevice, DeviceDescriptor descriptor) {
        try {
            DeviceHandle handle = new DeviceHandle();
            int result = LibUsb.open(usbDevice, handle);
            
            if (result != LibUsb.SUCCESS) {
                return;
            }
            
            // Essayer de lire les chaînes descriptives
            try {
                if (descriptor.iSerialNumber() != 0) {
                    String serialNumber = LibUsb.getStringDescriptor(handle, descriptor.iSerialNumber());
                    if (serialNumber != null && !serialNumber.trim().isEmpty()) {
                        device.setSerialNumber(serialNumber.trim());
                        
                        // Pour iOS, le serial number peut contenir l'UDID
                        if (device.isIOS() && serialNumber.length() >= 40) {
                            device.setUdid(serialNumber.substring(0, 40));
                        }
                    }
                }
                
                if (descriptor.iProduct() != 0) {
                    String productName = LibUsb.getStringDescriptor(handle, descriptor.iProduct());
                    if (productName != null && !productName.trim().isEmpty()) {
                        // Utiliser le nom du produit si on n'a pas d'info spécifique
                        if (device.getModel().contains("Android Device") || device.getModel().contains("Series")) {
                            device.setModel(productName.trim());
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Impossible de lire les descripteurs de chaînes: {}", e.getMessage());
            }
            
            LibUsb.close(handle);
            
        } catch (Exception e) {
            logger.debug("Erreur lors de l'enrichissement des informations: {}", e.getMessage());
        }
    }
    
    /**
     * Retourne tous les appareils mobiles actuellement connectés
     */
    public List<Device> getAllConnectedDevices() {
        List<Device> devices = new ArrayList<>();
        
        try {
            DeviceList deviceList = new DeviceList();
            int result = LibUsb.getDeviceList(context, deviceList);
            
            if (result < 0) {
                logger.error("Erreur lors de la récupération de la liste USB: " + LibUsb.errorName(result));
                return devices;
            }
            
            for (org.usb4java.Device usbDevice : deviceList) {
                Device mobileDevice = analyzeUSBDevice(usbDevice);
                if (mobileDevice != null) {
                    devices.add(mobileDevice);
                }
            }
            
            LibUsb.freeDeviceList(deviceList, true);
            
        } catch (Exception e) {
            logger.error("Erreur lors du scan complet USB", e);
        }
        
        return devices;
    }
    
    /**
     * Vérifie si un appareil spécifique est toujours connecté
     */
    public boolean isDeviceConnected(Device device) {
        if (device == null || device.getUsbVendorId() == null || device.getUsbProductId() == null) {
            return false;
        }
        
        List<Device> connectedDevices = getAllConnectedDevices();
        return connectedDevices.stream()
            .anyMatch(d -> Objects.equals(d.getUsbVendorId(), device.getUsbVendorId()) &&
                          Objects.equals(d.getUsbProductId(), device.getUsbProductId()));
    }
    
    /**
     * Détermine le type d'opération de déblocage possible pour un appareil
     */
    public List<String> getAvailableUnlockOperations(Device device) {
        List<String> operations = new ArrayList<>();
        
        if (device == null || !device.isConnected()) {
            return operations;
        }
        
        if (device.isIOS()) {
            operations.add("iCloud Bypass");
            operations.add("Passcode Unlock");
            operations.add("Screen Time Bypass");
            operations.add("Activation Lock Bypass");
        } else if (device.isAndroid()) {
            operations.add("FRP Bypass");
            operations.add("Pattern/PIN Unlock");
            operations.add("Samsung Account Bypass");
            operations.add("Mi Account Bypass");
        }
        
        return operations;
    }
    
    /**
     * Ferme le service et libère les ressources
     */
    public void shutdown() {
        try {
            if (context != null) {
                LibUsb.exit(context);
                initialized = false;
                logger.info("Service de détection d'appareils fermé");
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la fermeture du service", e);
        }
    }
    
    /**
     * Classe interne pour stocker les informations des appareils
     */
    private static class DeviceInfo {
        final String model;
        final String brand;
        final Device.Platform platform;
        
        DeviceInfo(String model, String brand, Device.Platform platform) {
            this.model = model;
            this.brand = brand;
            this.platform = platform;
        }
    }
    
    /**
     * Simule la détection d'un appareil pour les tests (en cas d'absence de libusb)
     */
    public Device simulateDeviceDetection() {
        logger.info("Mode simulation - Génération d'un appareil de test");
        
        Random random = new Random();
        Device device = new Device();
        
        // Choisir aléatoirement entre différents appareils réalistes
        int deviceType = random.nextInt(10);
        
        switch (deviceType) {
            case 0: // iPhone 15 Pro Max
                device.setBrand("Apple");
                device.setModel("iPhone 15 Pro Max");
                device.setPlatform(Device.Platform.iOS);
                device.setOsVersion("iOS 17.4");
                device.setImei(generateRealisticIMEI("013540"));
                device.setSerialNumber(generateAppleSerial());
                device.setUdid(generateUDID());
                device.setUsbVendorId("0x05AC");
                device.setUsbProductId("0x129E");
                break;
                
            case 1: // iPhone 14 Pro
                device.setBrand("Apple");
                device.setModel("iPhone 14 Pro");
                device.setPlatform(Device.Platform.iOS);
                device.setOsVersion("iOS 17.1.2");
                device.setImei(generateRealisticIMEI("013539"));
                device.setSerialNumber(generateAppleSerial());
                device.setUdid(generateUDID());
                device.setUsbVendorId("0x05AC");
                device.setUsbProductId("0x129D");
                break;
                
            case 2: // iPhone 13
                device.setBrand("Apple");
                device.setModel("iPhone 13");
                device.setPlatform(Device.Platform.iOS);
                device.setOsVersion("iOS 16.7.5");
                device.setImei(generateRealisticIMEI("013222"));
                device.setSerialNumber(generateAppleSerial());
                device.setUdid(generateUDID());
                device.setUsbVendorId("0x05AC");
                device.setUsbProductId("0x12A3");
                break;
                
            case 3: // Samsung Galaxy S24 Ultra
                device.setBrand("Samsung");
                device.setModel("Galaxy S24 Ultra");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 14 (One UI 6.1)");
                device.setImei(generateRealisticIMEI("356938"));
                device.setSerialNumber(generateSamsungSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x04E8");
                device.setUsbProductId("0x6860");
                break;
                
            case 4: // Samsung Galaxy S23
                device.setBrand("Samsung");
                device.setModel("Galaxy S23");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 14 (One UI 6.0)");
                device.setImei(generateRealisticIMEI("356936"));
                device.setSerialNumber(generateSamsungSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x04E8");
                device.setUsbProductId("0x685D");
                break;
                
            case 5: // Xiaomi 14 Pro
                device.setBrand("Xiaomi");
                device.setModel("Xiaomi 14 Pro");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 14 (MIUI 15)");
                device.setImei(generateRealisticIMEI("860461"));
                device.setSerialNumber(generateXiaomiSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x2717");
                device.setUsbProductId("0x904E");
                break;
                
            case 6: // Google Pixel 8 Pro
                device.setBrand("Google");
                device.setModel("Pixel 8 Pro");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 14");
                device.setImei(generateRealisticIMEI("357921"));
                device.setSerialNumber(generatePixelSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x18D1");
                device.setUsbProductId("0x4EE7");
                break;
                
            case 7: // OnePlus 12
                device.setBrand("OnePlus");
                device.setModel("OnePlus 12");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 14 (OxygenOS 14)");
                device.setImei(generateRealisticIMEI("353844"));
                device.setSerialNumber(generateOnePlusSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x2A70");
                device.setUsbProductId("0x4EE7");
                break;
                
            case 8: // Huawei P60 Pro
                device.setBrand("Huawei");
                device.setModel("P60 Pro");
                device.setPlatform(Device.Platform.ANDROID);
                device.setOsVersion("Android 13 (EMUI 13.1)");
                device.setImei(generateRealisticIMEI("357687"));
                device.setSerialNumber(generateHuaweiSerial());
                device.setAndroidId(generateAndroidId());
                device.setUsbVendorId("0x12D1");
                device.setUsbProductId("0x1050");
                break;
                
            default: // iPad Pro 12.9
                device.setBrand("Apple");
                device.setModel("iPad Pro 12.9\" (6e génération)");
                device.setPlatform(Device.Platform.iOS);
                device.setOsVersion("iPadOS 17.3.1");
                device.setImei(generateRealisticIMEI("356401"));
                device.setSerialNumber(generateAppleSerial());
                device.setUdid(generateUDID());
                device.setUsbVendorId("0x05AC");
                device.setUsbProductId("0x12A9");
                break;
        }
        
        device.setStatus(Device.DeviceStatus.CONNECTED);
        device.setConnectionType(Device.ConnectionType.USB);
        
        logger.info("Appareil simulé généré: {} {} - IMEI: {} - OS: {}", 
                   device.getBrand(), device.getModel(), device.getImei(), device.getOsVersion());
        
        return device;
    }
    
    /**
     * Génère un IMEI réaliste avec un TAC donné
     */
    private String generateRealisticIMEI(String tac) {
        Random random = new Random();
        StringBuilder imei = new StringBuilder(tac);
        
        // Générer le FAC (Final Assembly Code) - 2 chiffres
        imei.append(String.format("%02d", random.nextInt(100)));
        
        // Générer le SNR (Serial Number) - 6 chiffres
        for (int i = 0; i < 6; i++) {
            imei.append(random.nextInt(10));
        }
        
        // Calculer et ajouter le chiffre de contrôle Luhn
        int checksum = calculateLuhnChecksum(imei.toString());
        imei.append(checksum);
        
        return imei.toString();
    }
    
    /**
     * Calcule le chiffre de contrôle Luhn pour un IMEI
     */
    private int calculateLuhnChecksum(String imei) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = imei.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(imei.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }
    
    /**
     * Génère un numéro de série Apple réaliste
     */
    private String generateAppleSerial() {
        String[] prefixes = {"F2L", "F2M", "F2N", "F2P", "F4H", "G6Y", "G7R", "H1K"};
        String[] chars = {"ABCDEFGHJKLMNPQRSTUVWXYZ", "0123456789"};
        Random random = new Random();
        
        String prefix = prefixes[random.nextInt(prefixes.length)];
        StringBuilder serial = new StringBuilder(prefix);
        
        // Ajouter 9 caractères supplémentaires
        for (int i = 0; i < 9; i++) {
            String charSet = chars[random.nextInt(2)];
            serial.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        
        return serial.toString();
    }
    
    /**
     * Génère un UDID Apple réaliste
     */
    private String generateUDID() {
        Random random = new Random();
        StringBuilder udid = new StringBuilder();
        
        String chars = "0123456789ABCDEF";
        
        // Format: XXXXXXXX-XXXXXXXXXXXXXXXX
        for (int i = 0; i < 8; i++) {
            udid.append(chars.charAt(random.nextInt(chars.length())));
        }
        udid.append("-");
        for (int i = 0; i < 16; i++) {
            udid.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return udid.toString();
    }
    
    /**
     * Génère un numéro de série Samsung réaliste
     */
    private String generateSamsungSerial() {
        String[] prefixes = {"RF8M", "RF8N", "RF8P", "RZ8T", "RZ8W"};
        Random random = new Random();
        
        String prefix = prefixes[random.nextInt(prefixes.length)];
        StringBuilder serial = new StringBuilder(prefix);
        
        // Ajouter des caractères alphanumériques
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 7; i++) {
            serial.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return serial.toString();
    }
    
    /**
     * Génère un Android ID réaliste
     */
    private String generateAndroidId() {
        Random random = new Random();
        StringBuilder androidId = new StringBuilder();
        
        String chars = "0123456789abcdef";
        for (int i = 0; i < 16; i++) {
            androidId.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return androidId.toString();
    }
    
    /**
     * Génère un numéro de série Xiaomi réaliste
     */
    private String generateXiaomiSerial() {
        Random random = new Random();
        StringBuilder serial = new StringBuilder();
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) {
            serial.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return serial.toString();
    }
    
    /**
     * Génère un numéro de série Google Pixel réaliste
     */
    private String generatePixelSerial() {
        String[] prefixes = {"23", "24", "25"};
        Random random = new Random();
        
        String prefix = prefixes[random.nextInt(prefixes.length)];
        StringBuilder serial = new StringBuilder(prefix);
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 12; i++) {
            serial.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return serial.toString();
    }
    
    /**
     * Génère un numéro de série OnePlus réaliste
     */
    private String generateOnePlusSerial() {
        Random random = new Random();
        StringBuilder serial = new StringBuilder();
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 15; i++) {
            serial.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return serial.toString();
    }
    
    /**
     * Génère un numéro de série Huawei réaliste
     */
    private String generateHuaweiSerial() {
        String[] prefixes = {"HW", "DUB", "EML", "VOG"};
        Random random = new Random();
        
        String prefix = prefixes[random.nextInt(prefixes.length)];
        StringBuilder serial = new StringBuilder(prefix);
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < 10; i++) {
            serial.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return serial.toString();
    }
}
