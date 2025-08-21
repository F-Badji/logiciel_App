package com.logicielapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modèle représentant un appareil mobile (iPhone, iPad, Android)
 * Contient toutes les informations nécessaires pour les opérations de déblocage
 */
public class Device {
    
    public enum Platform {
        iOS("iOS"),
        ANDROID("Android");
        
        private final String displayName;
        
        Platform(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum ConnectionType {
        USB("USB"),
        IMEI_REMOTE("IMEI à distance");
        
        private final String displayName;
        
        ConnectionType(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum DeviceStatus {
        CONNECTED("Connecté"),
        DISCONNECTED("Déconnecté"),
        DETECTED_BY_IMEI("Détecté par IMEI"),
        LOCKED("Verrouillé"),
        UNLOCKED("Débloqué"),
        ERROR("Erreur");
        
        private final String displayName;
        
        DeviceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Propriétés principales
    private String imei;
    private String model;
    private String brand;
    private Platform platform;
    private String osVersion;
    private ConnectionType connectionType;
    private DeviceStatus status;
    private String deviceName;
    private boolean connected;
    
    // Informations techniques
    private String udid;
    private String serialNumber;
    private String macAddress;
    private String bootloaderVersion;
    private String firmwareVersion;
    
    // État de l'appareil (pour déblocage iCloud)
    private String deviceState;
    private String batteryLevel;
    private String storageCapacity;
    
    // Informations de déblocage
    private boolean iCloudLocked;
    private boolean frpLocked;
    private boolean bootloaderLocked;
    private boolean rootAccess;
    
    // Informations réseau
    private String mobileCountryCode;
    private String mobileNetworkCode;
    private String currentCarrier;
    private boolean isSimLocked;
    
    // Métadonnées
    private LocalDateTime detectionTime;
    private LocalDateTime lastSeen;
    private String usbVendorId;
    private String usbProductId;
    
    // Constructeurs
    public Device() {
        this.detectionTime = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
        this.status = DeviceStatus.DISCONNECTED;
    }
    
    public Device(String imei, String model, Platform platform) {
        this();
        this.imei = imei;
        this.model = model;
        this.platform = platform;
    }
    
    // Méthodes utilitaires
    public boolean isIOS() {
        return platform == Platform.iOS;
    }
    
    public boolean isAndroid() {
        return platform == Platform.ANDROID;
    }
    
    public boolean isConnectedByStatus() {
        return status == DeviceStatus.CONNECTED;
    }
    
    public boolean isLocked() {
        return status == DeviceStatus.LOCKED;
    }
    
    public boolean canUnlock() {
        return isConnected() && (isIOS() || isAndroid());
    }
    
    public String getDisplayName() {
        if (brand != null && model != null) {
            return brand + " " + model;
        }
        return model != null ? model : "Appareil inconnu";
    }
    
    public String getFullInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getDisplayName());
        if (osVersion != null) {
            info.append(" (").append(platform).append(" ").append(osVersion).append(")");
        }
        if (imei != null) {
            info.append(" - IMEI: ").append(imei);
        }
        return info.toString();
    }
    
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
    
    // Getters et Setters
    public String getImei() {
        return imei;
    }
    
    public void setImei(String imei) {
        this.imei = imei;
    }
    
    public String getDeviceState() {
        return deviceState;
    }
    
    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public Platform getPlatform() {
        return platform;
    }
    
    public void setPlatform(Platform platform) {
        this.platform = platform;
    }
    
    public String getOsVersion() {
        return osVersion;
    }
    
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
    
    public ConnectionType getConnectionType() {
        return connectionType;
    }
    
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    
    public DeviceStatus getStatus() {
        return status;
    }
    
    public void setStatus(DeviceStatus status) {
        this.status = status;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public String getUdid() {
        return udid;
    }
    
    public void setUdid(String udid) {
        this.udid = udid;
    }
    
    public String getAndroidId() {
        return "N/A"; // Placeholder
    }
    
    public void setAndroidId(String androidId) {
        // Placeholder method
    }
    
    public String getBuildNumber() {
        return "N/A"; // Placeholder
    }
    
    public void setBuildNumber(String buildNumber) {
        // Placeholder method
    }
    
    public String getKernelVersion() {
        return "N/A"; // Placeholder
    }
    
    public void setKernelVersion(String kernelVersion) {
        // Placeholder method
    }
    
    public boolean isiCloudLocked() {
        return iCloudLocked;
    }
    
    public void setiCloudLocked(boolean iCloudLocked) {
        this.iCloudLocked = iCloudLocked;
    }
    
    public boolean isFrpLocked() {
        return frpLocked;
    }
    
    public void setFrpLocked(boolean frpLocked) {
        this.frpLocked = frpLocked;
    }
    
    public boolean isBootloaderLocked() {
        return bootloaderLocked;
    }
    
    public void setBootloaderLocked(boolean bootloaderLocked) {
        this.bootloaderLocked = bootloaderLocked;
    }
    
    public boolean hasRootAccess() {
        return rootAccess;
    }
    
    public void setRootAccess(boolean rootAccess) {
        this.rootAccess = rootAccess;
    }
    
    public String getMobileCountryCode() {
        return mobileCountryCode;
    }
    
    public void setMobileCountryCode(String mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }
    
    public String getMobileNetworkCode() {
        return mobileNetworkCode;
    }
    
    public void setMobileNetworkCode(String mobileNetworkCode) {
        this.mobileNetworkCode = mobileNetworkCode;
    }
    
    public String getCurrentCarrier() {
        return currentCarrier;
    }
    
    public void setCurrentCarrier(String currentCarrier) {
        this.currentCarrier = currentCarrier;
    }
    
    public boolean isSimLocked() {
        return isSimLocked;
    }
    
    public void setSimLocked(boolean simLocked) {
        isSimLocked = simLocked;
    }
    
    public LocalDateTime getDetectionTime() {
        return detectionTime;
    }
    
    public void setDetectionTime(LocalDateTime detectionTime) {
        this.detectionTime = detectionTime;
    }
    
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public String getUsbVendorId() {
        return usbVendorId;
    }
    
    public void setUsbVendorId(String usbVendorId) {
        this.usbVendorId = usbVendorId;
    }
    
    public String getUsbProductId() {
        return usbProductId;
    }
    
    public void setUsbProductId(String usbProductId) {
        this.usbProductId = usbProductId;
    }
    
    public String getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = String.valueOf(batteryLevel);
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
        if (connected) {
            this.status = DeviceStatus.CONNECTED;
            updateLastSeen();
        } else {
            this.status = DeviceStatus.DISCONNECTED;
        }
    }
    
    public String getStorageCapacity() {
        return storageCapacity;
    }
    
    public void setStorageCapacity(String storageCapacity) {
        this.storageCapacity = storageCapacity;
    }
    
    // Méthodes Object
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(imei, device.imei) && 
               Objects.equals(serialNumber, device.serialNumber) &&
               Objects.equals(udid, device.udid);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(imei, serialNumber, udid);
    }
    
    @Override
    public String toString() {
        return String.format("Device{model='%s', platform=%s, imei='%s', status=%s}", 
                           model, platform, imei, status);
    }
}
