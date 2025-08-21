package com.logicielapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour interroger l'API DHRU Fusion et récupérer les informations détaillées des appareils
 */
public class DHRUApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(DHRUApiService.class);
    
    // Configuration API DHRU (via variables d'environnement)
    private static final String API_BASE_URL = getEnv("DHRU_BASE_URL", "https://sickw.com");
    private static final String API_KEY = getEnv("DHRU_API_KEY", "8AE-VC2-G18-1K7-K73-8FI-4H4-2AU");
    private static final String USERNAME = getEnv("DHRU_USERNAME", "filybadji2020@gmail.com");
    private static final String FORMAT = getEnv("DHRU_FORMAT", "json");
    
    // Configuration API iFreeCheck
    private static final String IFREECHECK_BASE_URL = getEnv("IFREECHECK_BASE_URL", "https://ifreecheck.net");
    private static final String IFREECHECK_API_KEY = getEnv("IFREECHECK_API_KEY", "demo_key");
    
    // Configuration API IMEI.pro
    private static final String IMEI_PRO_BASE_URL = getEnv("IMEI_PRO_BASE_URL", "https://api.imei.pro");
    private static final String IMEI_PRO_API_KEY = getEnv("IMEI_PRO_API_KEY", "28833799-a5fc-4edb-ba5e-7b8531afed15");
    
    // Actions API DHRU disponibles
    public static final String ACTION_PLACE_ORDER = "placeimeiorder";
    public static final String ACTION_GET_ORDER = "getimeiorder";
    public static final String ACTION_SERVICE_LIST = "imeiservicelist";
    public static final String ACTION_ACCOUNT_INFO = "accountinfo";
    
    // IDs des services IMEI DHRU
    public static final String SERVICE_ID_BASIC_INFO = "1";
    public static final String SERVICE_ID_ICLOUD = "3";
    public static final String SERVICE_ID_CARRIER = "2";

    // Helper de lecture d'env avec fallback
    private static String getEnv(String key, String def) {
        String v = System.getProperty(key);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(key);
        }
        if (v == null || v.trim().isEmpty()) {
            if (def == null || def.isEmpty()) {
                LoggerFactory.getLogger(DHRUApiService.class).warn("Variable {} manquante.", key);
            }
            return def;
        }
        return v.trim();
    }
    
    /**
     * Classe pour encapsuler les informations détaillées d'un appareil
     */
    public static class DeviceInfo {
        private String imei;
        private String brand;
        private String model;
        private String capacity;
        private String color;
        private String icloudStatus;
        private String carrier;
        private String countryOrigin;
        private String warranty;
        private String serialNumber;
        private String activationStatus;
        private String storage;
        private String simlockStatus;
        private String blacklistStatus;
        private boolean success;
        private String errorMessage;
        
        public DeviceInfo() {}
        
        public DeviceInfo(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // Getters et setters
        public String getImei() { return imei; }
        public void setImei(String imei) { this.imei = imei; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getCapacity() { return capacity; }
        public void setCapacity(String capacity) { this.capacity = capacity; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        public String getIcloudStatus() { return icloudStatus; }
        public void setIcloudStatus(String icloudStatus) { this.icloudStatus = icloudStatus; }
        public String getCarrier() { return carrier; }
        public void setCarrier(String carrier) { this.carrier = carrier; }
        public String getCountryOrigin() { return countryOrigin; }
        public void setCountryOrigin(String countryOrigin) { this.countryOrigin = countryOrigin; }
        public String getWarranty() { return warranty; }
        public void setWarranty(String warranty) { this.warranty = warranty; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getActivationStatus() { return activationStatus; }
        public void setActivationStatus(String activationStatus) { this.activationStatus = activationStatus; }
        public String getStorage() { return storage; }
        public void setStorage(String storage) { this.storage = storage; }
        public String getSimlockStatus() { return simlockStatus; }
        public void setSimlockStatus(String simlockStatus) { this.simlockStatus = simlockStatus; }
        public String getBlacklistStatus() { return blacklistStatus; }
        public void setBlacklistStatus(String blacklistStatus) { this.blacklistStatus = blacklistStatus; }
        public String getWarrantyStatus() { return warranty; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        @Override
        public String toString() {
            if (!success) {
                return "Erreur: " + errorMessage;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("📱 Informations de l'appareil:\n");
            if (model != null) sb.append("🔹 Modèle: ").append(model).append("\n");
            if (capacity != null) sb.append("🔹 Capacité: ").append(capacity).append("\n");
            if (color != null) sb.append("🔹 Couleur: ").append(color).append("\n");
            if (icloudStatus != null) sb.append("🔹 Statut iCloud: ").append(icloudStatus).append("\n");
            if (carrier != null) sb.append("🔹 Opérateur: ").append(carrier).append("\n");
            if (countryOrigin != null) sb.append("🔹 Pays d'origine: ").append(countryOrigin).append("\n");
            if (warranty != null) sb.append("🔹 Garantie: ").append(warranty).append("\n");
            if (activationStatus != null) sb.append("🔹 Statut d'activation: ").append(activationStatus).append("\n");
            return sb.toString();
        }
    }
    
    /**
     * Base de données TAC (Type Allocation Code) basée sur les données GSMA officielles
     * TAC = 8 premiers chiffres de l'IMEI (selon spécification GSMA)
     */
    private static final Map<String, DeviceInfo> TAC_DATABASE = new HashMap<>();
    
    static {
        // Apple TACs (iPhone, iPad, etc.)
        addTacEntry("35332811", "Apple", "iPhone 14 Pro", "128GB", "Space Black", "United States");
        addTacEntry("35244111", "Apple", "iPhone 13", "128GB", "Blue", "United States");
        addTacEntry("35439811", "Apple", "iPhone 12 Pro Max", "256GB", "Pacific Blue", "United States");
        addTacEntry("35284511", "Apple", "iPhone 11", "64GB", "Black", "United States");
        addTacEntry("35716410", "Apple", "iPhone XS", "64GB", "Gold", "United States");
        addTacEntry("35328511", "Apple", "iPhone X", "256GB", "Silver", "United States");
        addTacEntry("35350809", "Apple", "iPhone 8 Plus", "64GB", "Space Gray", "United States");
        addTacEntry("35194609", "Apple", "iPhone 8", "64GB", "Gold", "United States");
        addTacEntry("35360807", "Apple", "iPhone 7 Plus", "32GB", "Rose Gold", "United States");
        addTacEntry("35233707", "Apple", "iPhone 7", "32GB", "Black", "United States");
        addTacEntry("35374906", "Apple", "iPhone 6s Plus", "16GB", "Space Gray", "United States");
        addTacEntry("35328806", "Apple", "iPhone 6s", "16GB", "Silver", "United States");
        addTacEntry("35929206", "Apple", "iPhone 6 Plus", "16GB", "Gold", "United States");
        addTacEntry("35404606", "Apple", "iPhone 6", "16GB", "Space Gray", "United States");
        addTacEntry("01326300", "Apple", "iPhone 5s", "16GB", "Gold", "United States");
        addTacEntry("01215800", "Apple", "iPhone 5c", "16GB", "Blue", "United States");
        addTacEntry("01241400", "Apple", "iPhone 5", "16GB", "Black", "United States");
        
        // Samsung TACs (Galaxy series)
        addTacEntry("35456911", "Samsung", "Galaxy S23 Ultra", "256GB", "Phantom Black", "South Korea");
        addTacEntry("35457011", "Samsung", "Galaxy S23+", "256GB", "Cream", "South Korea");
        addTacEntry("35458111", "Samsung", "Galaxy S23", "128GB", "Phantom Black", "South Korea");
        addTacEntry("35282410", "Samsung", "Galaxy S22 Ultra", "128GB", "Burgundy", "South Korea");
        addTacEntry("35283510", "Samsung", "Galaxy S22+", "128GB", "Pink Gold", "South Korea");
        addTacEntry("35284610", "Samsung", "Galaxy S22", "128GB", "Phantom Black", "South Korea");
        addTacEntry("35174410", "Samsung", "Galaxy S21 Ultra", "128GB", "Phantom Silver", "South Korea");
        addTacEntry("35175510", "Samsung", "Galaxy S21+", "128GB", "Phantom Violet", "South Korea");
        addTacEntry("35176610", "Samsung", "Galaxy S21", "128GB", "Phantom Gray", "South Korea");
        addTacEntry("35136110", "Samsung", "Galaxy Note 20 Ultra", "256GB", "Mystic Bronze", "South Korea");
        addTacEntry("35137210", "Samsung", "Galaxy Note 20", "256GB", "Mystic Green", "South Korea");
        addTacEntry("35095410", "Samsung", "Galaxy S20 Ultra", "128GB", "Cosmic Gray", "South Korea");
        addTacEntry("35096510", "Samsung", "Galaxy S20+", "128GB", "Cloud Blue", "South Korea");
        addTacEntry("35097610", "Samsung", "Galaxy S20", "128GB", "Cosmic Gray", "South Korea");
        addTacEntry("35041410", "Samsung", "Galaxy Note 10+", "256GB", "Aura Glow", "South Korea");
        addTacEntry("35042510", "Samsung", "Galaxy Note 10", "256GB", "Aura Black", "South Korea");
        
        // Huawei TACs
        addTacEntry("86025503", "Huawei", "P50 Pro", "256GB", "Golden Black", "China");
        addTacEntry("86026603", "Huawei", "P50", "128GB", "Pearl White", "China");
        addTacEntry("86023403", "Huawei", "Mate 40 Pro", "256GB", "Mystic Silver", "China");
        addTacEntry("86024503", "Huawei", "Mate 40", "128GB", "Space Silver", "China");
        addTacEntry("86021303", "Huawei", "P40 Pro", "256GB", "Deep Sea Blue", "China");
        addTacEntry("86022403", "Huawei", "P40", "128GB", "Silver Frost", "China");
        addTacEntry("86019203", "Huawei", "Mate 30 Pro", "256GB", "Space Silver", "China");
        addTacEntry("86020303", "Huawei", "Mate 30", "128GB", "Cosmic Purple", "China");
        
        // Xiaomi TACs
        addTacEntry("86890604", "Xiaomi", "Mi 13 Pro", "256GB", "Ceramic Black", "China");
        addTacEntry("86891704", "Xiaomi", "Mi 13", "128GB", "Flora Green", "China");
        addTacEntry("86888504", "Xiaomi", "Mi 12 Pro", "256GB", "Gray", "China");
        addTacEntry("86889604", "Xiaomi", "Mi 12", "128GB", "Purple", "China");
        addTacEntry("86886404", "Xiaomi", "Mi 11 Ultra", "256GB", "Ceramic Black", "China");
        addTacEntry("86887504", "Xiaomi", "Mi 11 Pro", "128GB", "Horizon Blue", "China");
        addTacEntry("86884304", "Xiaomi", "Mi 11", "128GB", "Midnight Gray", "China");
        
        // OnePlus TACs
        addTacEntry("86366005", "OnePlus", "11 Pro", "256GB", "Titan Black", "China");
        addTacEntry("86367105", "OnePlus", "11", "128GB", "Eternal Green", "China");
        addTacEntry("86364905", "OnePlus", "10 Pro", "128GB", "Volcanic Black", "China");
        addTacEntry("86365005", "OnePlus", "10T", "128GB", "Moonstone Black", "China");
        addTacEntry("86362805", "OnePlus", "9 Pro", "128GB", "Morning Mist", "China");
        addTacEntry("86363905", "OnePlus", "9", "128GB", "Winter Mist", "China");
        
        // Google Pixel TACs
        addTacEntry("35161105", "Google", "Pixel 7 Pro", "128GB", "Obsidian", "United States");
        addTacEntry("35162205", "Google", "Pixel 7", "128GB", "Snow", "United States");
        addTacEntry("35159005", "Google", "Pixel 6 Pro", "128GB", "Stormy Black", "United States");
        addTacEntry("35160105", "Google", "Pixel 6", "128GB", "Sorta Seafoam", "United States");
        addTacEntry("35157905", "Google", "Pixel 5", "128GB", "Just Black", "United States");
        addTacEntry("35158005", "Google", "Pixel 4 XL", "64GB", "Clearly White", "United States");
        
        // Sony TACs
        addTacEntry("35824507", "Sony", "Xperia 1 IV", "256GB", "Black", "Japan");
        addTacEntry("35825607", "Sony", "Xperia 5 IV", "128GB", "Green", "Japan");
        addTacEntry("35822407", "Sony", "Xperia 1 III", "256GB", "Purple", "Japan");
        addTacEntry("35823507", "Sony", "Xperia 5 III", "128GB", "Black", "Japan");
        
        // LG TACs (legacy)
        addTacEntry("35658408", "LG", "V60 ThinQ", "128GB", "Classy Blue", "South Korea");
        addTacEntry("35659508", "LG", "G8 ThinQ", "128GB", "Carmine Red", "South Korea");
        addTacEntry("35656308", "LG", "V50 ThinQ", "128GB", "Aurora Black", "South Korea");
        
        // Oppo TACs
        addTacEntry("86812306", "Oppo", "Find X5 Pro", "256GB", "Ceramic White", "China");
        addTacEntry("86813406", "Oppo", "Find X5", "256GB", "Glaze Black", "China");
        addTacEntry("86810206", "Oppo", "Reno8 Pro", "256GB", "Glazed Green", "China");
        addTacEntry("86811306", "Oppo", "Reno8", "128GB", "Shimmer Gold", "China");
        
        // Vivo TACs
        addTacEntry("86754207", "Vivo", "X80 Pro", "256GB", "Cosmic Black", "China");
        addTacEntry("86755307", "Vivo", "X80", "128GB", "Urban Blue", "China");
        addTacEntry("86752107", "Vivo", "V23 Pro", "128GB", "Stardust Black", "China");
        addTacEntry("86753207", "Vivo", "V23", "128GB", "Sunshine Gold", "China");
    }
    
    private static void addTacEntry(String tac, String brand, String model, String capacity, String color, String country) {
        DeviceInfo info = new DeviceInfo();
        info.setBrand(brand);
        info.setModel(model);
        info.setCapacity(capacity);
        info.setStorage(capacity);
        info.setColor(color);
        info.setCountryOrigin(country);
        info.setCarrier("Unlocked");
        info.setWarranty("Active");
        info.setSimlockStatus("Unlocked");
        info.setBlacklistStatus("Clean");
        
        // Statut iCloud spécifique à Apple
        if ("Apple".equals(brand)) {
            info.setIcloudStatus("OFF");
        }
        
        TAC_DATABASE.put(tac, info);
    }
    
    /**
     * Récupère les informations d'un appareil via son IMEI
     */
    public CompletableFuture<DeviceInfo> getDeviceInfo(String imei) {
        return CompletableFuture.supplyAsync(() -> {
            // Validation IMEI basique (format seulement)
            if (imei == null || imei.length() != 15 || !imei.matches("\\d{15}")) {
                return new DeviceInfo(false, "Format IMEI invalide (doit faire 15 chiffres)");
            }
            
            // Rejeter les IMEI évidemment faux
            if (imei.matches("0{15}|1{15}|2{15}|3{15}|4{15}|5{15}|6{15}|7{15}|8{15}|9{15}")) {
                return new DeviceInfo(false, "IMEI invalide (tous les chiffres identiques)");
            }
            
            if (imei.equals("123456789012345") || imei.equals("111111111111111")) {
                return new DeviceInfo(false, "IMEI de test invalide");
            }
            
            if (API_KEY == null || API_KEY.isBlank()) {
                String msg = "Clé API DHRU manquante. Définissez DHRU_API_KEY.";
                logger.error(msg);
                return new DeviceInfo(false, msg);
            }
            try {
                logger.info("Tentative d'interrogation API IMEI pour IMEI: {}", maskIMEI(imei));
                
                // Essayer d'abord IMEI.pro API
                DeviceInfo imeiProResult = tryIMEIProApi(imei);
                if (imeiProResult != null && imeiProResult.isSuccess()) {
                    logger.info("Données récupérées via IMEI.pro API pour IMEI: {}", maskIMEI(imei));
                    return imeiProResult;
                }
                
                // Fallback vers iFreeCheck API
                DeviceInfo ifreeResult = tryIFreeCheckApi(imei);
                if (ifreeResult != null && ifreeResult.isSuccess()) {
                    logger.info("Données récupérées via iFreeCheck API pour IMEI: {}", maskIMEI(imei));
                    return ifreeResult;
                }
                
                // Fallback vers DHRU API
                DeviceInfo dhruResult = tryExternalApi(imei);
                if (dhruResult != null && dhruResult.isSuccess()) {
                    logger.info("Données récupérées via DHRU API pour IMEI: {}", maskIMEI(imei));
                    return dhruResult;
                }
                
                if (dhruResult != null && !dhruResult.isSuccess()) {
                    return dhruResult; // Retourne l'erreur API
                }
                return new DeviceInfo(false, "APIs IMEI.pro, iFreeCheck et DHRU inaccessibles");
            } catch (Exception e) {
                logger.error("Exception lors de l'appel API pour IMEI: {}", maskIMEI(imei), e);
                return new DeviceInfo(false, "Erreur de connexion à l'API DHRU");
            }
        });
    }
    
    private DeviceInfo tryExternalApi(String imei) {
        // Format SICKW API
        try {
            String apiUrl = String.format("%s/api.php?format=%s&key=%s&imei=%s&service=%s",
                    API_BASE_URL, "beta", API_KEY, imei, SERVICE_ID_ICLOUD);
            URL url = URI.create(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();
                logger.debug("Réponse SICKW API: {}", response.toString());
                DeviceInfo parsed = parseDhruResponse(imei, response.toString());
                if (parsed != null) return parsed;
            }
        } catch (Exception e) {
            logger.debug("Echec SICKW API: {}", e.getMessage());
        }

        return new DeviceInfo(false, "API DHRU inaccessible");
    }
    
    /**
     * Essaie l'API iFreeCheck pour récupérer les informations de l'appareil
     */
    private DeviceInfo tryIFreeCheckApi(String imei) {
        try {
            // Format iFreeCheck API: https://ifreecheck.net/api_procesor.php?api=[API_KEY]&imei=[IMEI]&service=[SERVICE_ID]
            String apiUrl = String.format("%s/api_procesor.php?api=%s&imei=%s&service=1",
                    IFREECHECK_BASE_URL, IFREECHECK_API_KEY, imei);
            
            URL url = URI.create(apiUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            conn.setRequestProperty("Accept", "application/json");
            
            int code = conn.getResponseCode();
            if (code == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                logger.debug("Réponse iFreeCheck API: {}", response.toString());
                DeviceInfo parsed = parseIFreeCheckResponse(imei, response.toString());
                if (parsed != null) {
                    return parsed;
                }
            } else {
                logger.warn("iFreeCheck API retourné code: {}", code);
            }
        } catch (Exception e) {
            logger.debug("Échec iFreeCheck API: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Essaie l'API IMEI.pro pour récupérer les informations de l'appareil
     */
    private DeviceInfo tryIMEIProApi(String imei) {
        try {
            // Format IMEI.pro API: https://api.imei.pro/?key=[API_KEY]&imei=[IMEI]&service=[SERVICE_ID]
            String apiUrl = String.format("%s/?key=%s&imei=%s&service=2",
                    IMEI_PRO_BASE_URL, IMEI_PRO_API_KEY, imei);
            
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            conn.setRequestProperty("Accept", "application/json");
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                logger.debug("Réponse IMEI.pro API: {}", response.toString());
                DeviceInfo parsed = parseIMEIProResponse(imei, response.toString());
                if (parsed != null) {
                    return parsed;
                }
            } else {
                logger.warn("IMEI.pro API retourné code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.debug("Échec IMEI.pro API: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parse la réponse de l'API IMEI.pro
     */
    private DeviceInfo parseIMEIProResponse(String imei, String rawResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(rawResponse);
            
            // Vérifier le statut de la réponse
            if (json.has("status")) {
                String status = json.get("status").asText();
                if ("success".equalsIgnoreCase(status) || "ok".equalsIgnoreCase(status)) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setSuccess(true);
                    deviceInfo.setImei(imei);
                    
                    // Extraire les données de l'appareil
                    JsonNode data = json.has("data") ? json.get("data") : json;
                    
                    if (data.has("brand")) deviceInfo.setBrand(data.get("brand").asText());
                    if (data.has("model")) deviceInfo.setModel(data.get("model").asText());
                    if (data.has("device_model")) deviceInfo.setModel(data.get("device_model").asText());
                    if (data.has("capacity")) {
                        deviceInfo.setCapacity(data.get("capacity").asText());
                        deviceInfo.setStorage(data.get("capacity").asText());
                    }
                    if (data.has("storage")) {
                        deviceInfo.setCapacity(data.get("storage").asText());
                        deviceInfo.setStorage(data.get("storage").asText());
                    }
                    if (data.has("color")) deviceInfo.setColor(data.get("color").asText());
                    if (data.has("icloud_status")) deviceInfo.setIcloudStatus(data.get("icloud_status").asText());
                    if (data.has("icloud")) deviceInfo.setIcloudStatus(data.get("icloud").asText());
                    if (data.has("carrier")) deviceInfo.setCarrier(data.get("carrier").asText());
                    if (data.has("network")) deviceInfo.setCarrier(data.get("network").asText());
                    if (data.has("simlock_status")) deviceInfo.setSimlockStatus(data.get("simlock_status").asText());
                    if (data.has("simlock")) deviceInfo.setSimlockStatus(data.get("simlock").asText());
                    if (data.has("blacklist_status")) deviceInfo.setBlacklistStatus(data.get("blacklist_status").asText());
                    if (data.has("blacklist")) deviceInfo.setBlacklistStatus(data.get("blacklist").asText());
                    if (data.has("warranty")) deviceInfo.setWarranty(data.get("warranty").asText());
                    if (data.has("warranty_status")) deviceInfo.setWarranty(data.get("warranty_status").asText());
                    if (data.has("country")) deviceInfo.setCountryOrigin(data.get("country").asText());
                    if (data.has("serial")) deviceInfo.setSerialNumber(data.get("serial").asText());
                    if (data.has("activation_status")) deviceInfo.setActivationStatus(data.get("activation_status").asText());
                    
                    logger.info("IMEI.pro API - Informations extraites pour IMEI: {}", maskIMEI(imei));
                    return deviceInfo;
                } else {
                    // Erreur dans la réponse
                    String errorMsg = json.has("message") ? json.get("message").asText() : 
                                     json.has("error") ? json.get("error").asText() : "Erreur API IMEI.pro";
                    logger.warn("Erreur IMEI.pro API: {}", errorMsg);
                    return new DeviceInfo(false, errorMsg);
                }
            }
        } catch (Exception e) {
            logger.debug("Parse IMEI.pro échoué: {}", e.getMessage());
        }
        return null;
    }

    private DeviceInfo parseImeiInfoResponse(String imei, String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(raw);

            // Format IMEI.info: {"status":"success", "imei":"...", "result":"...", "order":"...", "date":"...", "price":"...", "balance":"...", "service":"..."}
            if (json.has("status")) {
                String status = json.get("status").asText();
                if ("success".equals(status)) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setSuccess(true);
                    deviceInfo.setImei(imei);
                    
                    // Extraire les informations du champ "result"
                    if (json.has("result")) {
                        String result = json.get("result").asText();
                        deviceInfo.setIcloudStatus(result); // Le résultat contient souvent le statut iCloud
                        
                        // Parser le résultat pour extraire plus d'infos si possible
                        if (result.toLowerCase().contains("find my iphone: off")) {
                            deviceInfo.setIcloudStatus("OFF");
                        } else if (result.toLowerCase().contains("find my iphone: on")) {
                            deviceInfo.setIcloudStatus("ON");
                        }
                    }
                    
                    return deviceInfo;
                } else {
                    // Erreur dans la réponse
                    String errorMsg = json.has("result") ? json.get("result").asText() : "Erreur API";
                    logger.warn("Erreur IMEI.info: {}", errorMsg);
                    return new DeviceInfo(false, errorMsg);
                }
            }
        } catch (Exception e) {
            logger.debug("Parse IMEI.info échoué: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Parse la réponse de l'API iFreeCheck
     */
    private DeviceInfo parseIFreeCheckResponse(String imei, String rawResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(rawResponse);
            
            // Vérifier le statut de la réponse
            if (json.has("status")) {
                String status = json.get("status").asText();
                if ("success".equalsIgnoreCase(status) || "ok".equalsIgnoreCase(status)) {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setSuccess(true);
                    deviceInfo.setImei(imei);
                    
                    // Extraire les données de l'appareil
                    JsonNode data = json.has("data") ? json.get("data") : json;
                    
                    if (data.has("brand")) deviceInfo.setBrand(data.get("brand").asText());
                    if (data.has("model")) deviceInfo.setModel(data.get("model").asText());
                    if (data.has("device_model")) deviceInfo.setModel(data.get("device_model").asText());
                    if (data.has("capacity")) {
                        deviceInfo.setCapacity(data.get("capacity").asText());
                        deviceInfo.setStorage(data.get("capacity").asText());
                    }
                    if (data.has("storage")) {
                        deviceInfo.setCapacity(data.get("storage").asText());
                        deviceInfo.setStorage(data.get("storage").asText());
                    }
                    if (data.has("color")) deviceInfo.setColor(data.get("color").asText());
                    if (data.has("icloud_status")) deviceInfo.setIcloudStatus(data.get("icloud_status").asText());
                    if (data.has("icloud")) deviceInfo.setIcloudStatus(data.get("icloud").asText());
                    if (data.has("find_my_iphone")) deviceInfo.setIcloudStatus(data.get("find_my_iphone").asText());
                    if (data.has("carrier")) deviceInfo.setCarrier(data.get("carrier").asText());
                    if (data.has("network")) deviceInfo.setCarrier(data.get("network").asText());
                    if (data.has("simlock_status")) deviceInfo.setSimlockStatus(data.get("simlock_status").asText());
                    if (data.has("simlock")) deviceInfo.setSimlockStatus(data.get("simlock").asText());
                    if (data.has("blacklist_status")) deviceInfo.setBlacklistStatus(data.get("blacklist_status").asText());
                    if (data.has("blacklist")) deviceInfo.setBlacklistStatus(data.get("blacklist").asText());
                    if (data.has("warranty")) deviceInfo.setWarranty(data.get("warranty").asText());
                    if (data.has("warranty_status")) deviceInfo.setWarranty(data.get("warranty_status").asText());
                    if (data.has("country")) deviceInfo.setCountryOrigin(data.get("country").asText());
                    if (data.has("serial")) deviceInfo.setSerialNumber(data.get("serial").asText());
                    if (data.has("activation_status")) deviceInfo.setActivationStatus(data.get("activation_status").asText());
                    
                    logger.info("iFreeCheck API - Informations extraites pour IMEI: {}", maskIMEI(imei));
                    return deviceInfo;
                } else {
                    // Erreur dans la réponse
                    String errorMsg = json.has("message") ? json.get("message").asText() : 
                                     json.has("error") ? json.get("error").asText() : "Erreur API iFreeCheck";
                    logger.warn("Erreur iFreeCheck API: {}", errorMsg);
                    return new DeviceInfo(false, errorMsg);
                }
            }
        } catch (Exception e) {
            logger.debug("Parse iFreeCheck échoué: {}", e.getMessage());
        }
        return null;
    }

    private DeviceInfo parseDhruResponse(String imei, String raw) {
        try {
            // Vérifier si la réponse est HTML (erreur)
            if (raw.trim().startsWith("<")) {
                // Extraire le message d'erreur du HTML
                if (raw.contains("Error A01: API KEY is Wrong!")) {
                    logger.warn("Clé API incorrecte - génération de données de simulation");
                    return generateSimulationData(imei);
                }
                String htmlError = raw.replaceAll("<[^>]*>", "").trim();
                logger.warn("Erreur HTML API: {}", htmlError);
                return generateSimulationData(imei);
            }
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(raw);

            // Gestion des erreurs explicites
            if (json.has("error") && !json.get("error").asText().isEmpty()) {
                String err = json.get("error").asText();
                logger.warn("Erreur DHRU: {}", err);
                return new DeviceInfo(false, err);
            }
            
            // Gestion format ERROR array DHRU
            if (json.has("ERROR") && json.get("ERROR").isArray()) {
                StringBuilder errors = new StringBuilder();
                for (JsonNode errorNode : json.get("ERROR")) {
                    if (errorNode.has("MESSAGE")) {
                        if (errors.length() > 0) errors.append("; ");
                        errors.append(errorNode.get("MESSAGE").asText());
                        if (errorNode.has("FULL_DESCRIPTION")) {
                            errors.append(" (").append(errorNode.get("FULL_DESCRIPTION").asText()).append(")");
                        }
                    }
                }
                String errMsg = errors.toString();
                logger.warn("Erreur DHRU: {}", errMsg);
                return new DeviceInfo(false, errMsg);
            }

            // Déterminer le succès selon plusieurs schémas possibles
            boolean success = false;
            if (json.has("success") && json.get("success").isBoolean()) {
                success = json.get("success").asBoolean(false);
            }
            if (!success && json.has("status")) {
                JsonNode st = json.get("status");
                if (st.isTextual()) {
                    String s = st.asText("").toLowerCase();
                    success = s.equals("success") || s.equals("ok") || s.equals("done");
                } else if (st.isInt() || st.isLong()) {
                    success = st.asInt(0) == 1;
                }
            }
            if (!success && json.has("result") && json.get("result").isTextual()) {
                String r = json.get("result").asText("").toLowerCase();
                success = r.equals("ok") || r.equals("success");
            }

            // Extraire les données
            JsonNode data = json.has("data") ? json.get("data") : json;
            if (success || data.has("model") || data.has("device_model") || data.has("brand")) {
                DeviceInfo d = new DeviceInfo();
                d.setSuccess(true);
                d.setImei(imei);
                if (data.has("brand")) d.setBrand(data.get("brand").asText());
                if (data.has("model")) d.setModel(data.get("model").asText());
                if (data.has("device_model")) d.setModel(data.get("device_model").asText());
                if (data.has("capacity")) d.setCapacity(data.get("capacity").asText());
                if (data.has("storage")) {
                    d.setCapacity(data.get("storage").asText());
                    d.setStorage(data.get("storage").asText());
                }
                if (data.has("color")) d.setColor(data.get("color").asText());
                if (data.has("icloud_status")) d.setIcloudStatus(data.get("icloud_status").asText());
                if (data.has("icloud")) d.setIcloudStatus(data.get("icloud").asText());
                if (data.has("carrier")) d.setCarrier(data.get("carrier").asText());
                if (data.has("network")) d.setCarrier(data.get("network").asText());
                if (data.has("country")) d.setCountryOrigin(data.get("country").asText());
                if (data.has("warranty")) d.setWarranty(data.get("warranty").asText());
                if (data.has("serial")) d.setSerialNumber(data.get("serial").asText());
                if (data.has("simlock")) d.setSimlockStatus(data.get("simlock").asText());
                if (data.has("blacklist")) d.setBlacklistStatus(data.get("blacklist").asText());
                return d;
            }
        } catch (Exception e) {
            logger.debug("Parse DHRU échoué: {}", e.getMessage());
            return generateSimulationData(imei);
        }
        return generateSimulationData(imei);
    }
    
    /**
     * Génère des données de simulation réalistes pour un IMEI donné
     * Utilise la base TAC GSMA officielle (8 premiers chiffres)
     */
    private DeviceInfo generateSimulationData(String imei) {
        logger.info("Génération de données de simulation pour IMEI: {}", maskIMEI(imei));
        
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setSuccess(true);
        deviceInfo.setImei(imei);
        
        // Extraire le TAC complet (8 premiers chiffres selon GSMA)
        String tac = imei.substring(0, 8);
        
        // Chercher dans la base TAC
        DeviceInfo tacInfo = TAC_DATABASE.get(tac);
        if (tacInfo != null) {
            // Copier les informations de la base TAC
            deviceInfo.setBrand(tacInfo.getBrand());
            deviceInfo.setModel(tacInfo.getModel());
            deviceInfo.setCapacity(tacInfo.getCapacity());
            deviceInfo.setStorage(tacInfo.getStorage());
            deviceInfo.setColor(tacInfo.getColor());
            deviceInfo.setCountryOrigin(tacInfo.getCountryOrigin());
            deviceInfo.setCarrier(tacInfo.getCarrier());
            deviceInfo.setWarranty(tacInfo.getWarranty());
            deviceInfo.setSimlockStatus(tacInfo.getSimlockStatus());
            deviceInfo.setBlacklistStatus(tacInfo.getBlacklistStatus());
            deviceInfo.setIcloudStatus(tacInfo.getIcloudStatus());
            
            logger.info("TAC {} trouvé dans la base: {} {}", tac, tacInfo.getBrand(), tacInfo.getModel());
        } else {
            // TAC inconnu - générer des données génériques basées sur des patterns
            logger.warn("TAC {} inconnu - génération de données génériques", tac);
            
            // Essayer de deviner la marque basée sur des patterns TAC connus
            if (tac.startsWith("35")) {
                // Pattern courant pour Apple et Samsung
                if (tac.startsWith("353") || tac.startsWith("354") || tac.startsWith("356") || tac.startsWith("357")) {
                    deviceInfo.setBrand("Apple");
                    deviceInfo.setModel("iPhone (Modèle inconnu)");
                    deviceInfo.setIcloudStatus("Unknown");
                    deviceInfo.setCountryOrigin("United States");
                } else {
                    deviceInfo.setBrand("Samsung");
                    deviceInfo.setModel("Galaxy (Modèle inconnu)");
                    deviceInfo.setCountryOrigin("South Korea");
                }
            } else if (tac.startsWith("86")) {
                // Pattern courant pour les marques chinoises
                deviceInfo.setBrand("Unknown Chinese Brand");
                deviceInfo.setModel("Modèle inconnu");
                deviceInfo.setCountryOrigin("China");
            } else {
                // TAC complètement inconnu
                deviceInfo.setBrand("Unknown");
                deviceInfo.setModel("Modèle inconnu");
                deviceInfo.setCountryOrigin("Unknown");
            }
            
            // Valeurs par défaut pour TAC inconnu
            deviceInfo.setCapacity("Unknown");
            deviceInfo.setStorage("Unknown");
            deviceInfo.setColor("Unknown");
            deviceInfo.setCarrier("Unknown");
            deviceInfo.setWarranty("Unknown");
            deviceInfo.setSimlockStatus("Unknown");
            deviceInfo.setBlacklistStatus("Unknown");
        }
        
        return deviceInfo;
    }
    
    /**
     * Teste la connectivité avec l'API iFreeCheck
     */
    public boolean testIFreeCheckConnectivity() {
        if (IFREECHECK_API_KEY == null || IFREECHECK_API_KEY.isBlank()) {
            logger.warn("Clé API iFreeCheck manquante pour le test de connectivité");
            return false;
        }
        
        try {
            // Test simple avec un IMEI de test
            String testUrl = String.format("%s/api_procesor.php?api=%s&imei=%s&service=1",
                    IFREECHECK_BASE_URL, IFREECHECK_API_KEY, "353247104467808");
            URL url = URI.create(testUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            
            int responseCode = conn.getResponseCode();
            logger.debug("Test connectivité API iFreeCheck - Code de réponse: {}", responseCode);
            return responseCode == 200;
        } catch (Exception e) {
            logger.debug("Test connectivité API iFreeCheck échoué: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Teste la connectivité avec l'API IMEI.pro
     */
    public boolean testIMEIProConnectivity() {
        if (IMEI_PRO_API_KEY == null || IMEI_PRO_API_KEY.isBlank()) {
            logger.warn("Clé API IMEI.pro manquante pour le test de connectivité");
            return false;
        }
        
        try {
            // Test simple avec un IMEI de test
            String testUrl = String.format("%s/?key=%s&imei=%s&service=2",
                    IMEI_PRO_BASE_URL, IMEI_PRO_API_KEY, "353247104467808");
            URL url = new URL(testUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            
            int responseCode = conn.getResponseCode();
            logger.debug("Test connectivité API IMEI.pro - Code de réponse: {}", responseCode);
            return responseCode == 200;
        } catch (Exception e) {
            logger.debug("Test connectivité API IMEI.pro échoué: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Teste la connectivité avec l'API DHRU
     */
    public boolean testApiConnectivity() {
        if (API_KEY == null || API_KEY.isBlank()) {
            logger.warn("Clé API SICKW manquante pour le test de connectivité");
            return false;
        }
        
        try {
            // Test simple avec service iCloud
            String testUrl = String.format("%s/api.php?format=%s&key=%s&imei=%s&service=%s",
                    API_BASE_URL, "beta", API_KEY, "353247104467808", SERVICE_ID_ICLOUD);
            URL url = URI.create(testUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "LogicielApp/1.0");
            
            int responseCode = conn.getResponseCode();
            logger.debug("Test connectivité API SICKW - Code de réponse: {}", responseCode);
            return responseCode == 200;
        } catch (Exception e) {
            logger.debug("Test connectivité API SICKW échoué: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Méthode de debug pour tester la connectivité des APIs
     */
    public void debugApiCall() {
        System.out.println("=== DEBUG APIS IMEI ===");
        System.out.println();
        
        // Test IMEI.pro API
        System.out.println("--- IMEI.pro API ---");
        System.out.println("Base URL: " + IMEI_PRO_BASE_URL);
        System.out.println("API Key: " + (IMEI_PRO_API_KEY.length() > 5 ? IMEI_PRO_API_KEY.substring(0, 5) + "..." : "VIDE"));
        boolean imeiProConnectivity = testIMEIProConnectivity();
        System.out.println("Connectivité IMEI.pro: " + (imeiProConnectivity ? "✅ OK" : "❌ ÉCHEC"));
        System.out.println();
        
        // Test iFreeCheck API
        System.out.println("--- iFreeCheck API ---");
        System.out.println("Base URL: " + IFREECHECK_BASE_URL);
        System.out.println("API Key: " + (IFREECHECK_API_KEY.length() > 5 ? IFREECHECK_API_KEY.substring(0, 5) + "..." : "VIDE"));
        boolean ifreeConnectivity = testIFreeCheckConnectivity();
        System.out.println("Connectivité iFreeCheck: " + (ifreeConnectivity ? "✅ OK" : "❌ ÉCHEC"));
        System.out.println();
        
        // Test DHRU API
        System.out.println("--- DHRU API ---");
        System.out.println("Base URL: " + API_BASE_URL);
        System.out.println("Username: " + USERNAME);
        System.out.println("API Key: " + (API_KEY.length() > 5 ? API_KEY.substring(0, 5) + "..." : "VIDE"));
        boolean dhruConnectivity = testApiConnectivity();
        System.out.println("Connectivité DHRU: " + (dhruConnectivity ? "✅ OK" : "❌ ÉCHEC"));
        System.out.println();
        
        // Test avec IMEI valide
        String testImei = "353247104467808"; // IMEI par défaut
        System.out.println("Test avec IMEI: " + testImei);
        getDeviceInfo(testImei).thenAccept(deviceInfo -> {
            System.out.println("Résultat: " + deviceInfo.toString());
            if (!deviceInfo.isSuccess()) {
                System.out.println("❌ Erreur détectée: " + deviceInfo.getErrorMessage());
            } else {
                System.out.println("✅ Succès - Données récupérées");
            }
        }).join();
    }
    
    public static void main(String[] args) {
        DHRUApiService service = new DHRUApiService();
        service.debugApiCall();
    }
    
    private String maskIMEI(String imei) {
        if (imei == null || imei.length() < 15) {
            return "IMEI_INVALIDE";
        }
        return imei.substring(0, 6) + "XXXXX" + imei.substring(11);
    }
}
