package com.logicielapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour charger et interroger la base de données TAC (Type Allocation Code) de la GSMA
 */
public class TACDatabase {
    
    private static final Logger logger = LoggerFactory.getLogger(TACDatabase.class);
    private static final Map<String, DeviceInfo> tacDatabase = new HashMap<>();
    private static boolean isLoaded = false;
    
    /**
     * Classe pour stocker les informations d'un appareil basées sur le TAC
     */
    public static class DeviceInfo {
        private String tac;
        private String manufacturer;
        private String model;
        private String marketingName;
        private String contributor;
        private String comment;
        
        public DeviceInfo(String tac, String manufacturer, String model, String marketingName, String contributor, String comment) {
            this.tac = tac;
            this.manufacturer = manufacturer;
            this.model = model;
            this.marketingName = marketingName;
            this.contributor = contributor;
            this.comment = comment;
        }
        
        // Getters
        public String getTac() { return tac; }
        public String getManufacturer() { return manufacturer; }
        public String getModel() { return model; }
        public String getMarketingName() { return marketingName; }
        public String getContributor() { return contributor; }
        public String getComment() { return comment; }
        
        @Override
        public String toString() {
            return String.format("%s %s (%s)", manufacturer, model, marketingName);
        }
    }
    
    /**
     * Charge la base de données TAC depuis le fichier CSV
     */
    public static synchronized void loadDatabase() {
        if (isLoaded) {
            return;
        }
        
        try (InputStream is = TACDatabase.class.getResourceAsStream("/tacdb.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            int lineCount = 0;
            int loadedCount = 0;
            
            // Ignorer la première ligne (copyright)
            reader.readLine();
            // Ignorer la deuxième ligne (headers)
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                lineCount++;
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String tac = parts[0].trim();
                    String manufacturer = parts[1].trim();
                    String model = parts[2].trim();
                    String contributor = parts.length > 3 ? parts[3].trim() : "";
                    String comment = parts.length > 4 ? parts[4].trim() : "";
                    String marketingName = parts.length > 7 ? parts[7].trim() : model;
                    
                    if (!tac.isEmpty() && !manufacturer.isEmpty()) {
                        DeviceInfo deviceInfo = new DeviceInfo(tac, manufacturer, model, marketingName, contributor, comment);
                        tacDatabase.put(tac, deviceInfo);
                        loadedCount++;
                    }
                }
            }
            
            isLoaded = true;
            logger.info("Base TAC chargée: {} entrées sur {} lignes traitées", loadedCount, lineCount);
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la base TAC: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Recherche les informations d'un appareil par son TAC (8 premiers chiffres de l'IMEI)
     */
    public static DeviceInfo getDeviceByTAC(String tac) {
        if (!isLoaded) {
            loadDatabase();
        }
        
        if (tac == null || tac.length() < 8) {
            return null;
        }
        
        // Essayer avec TAC complet (8 chiffres)
        String tacKey = tac.substring(0, Math.min(8, tac.length()));
        DeviceInfo device = tacDatabase.get(tacKey);
        
        if (device == null && tacKey.length() >= 6) {
            // Fallback: essayer avec TAC 6 chiffres (ancien format)
            tacKey = tac.substring(0, 6);
            device = tacDatabase.get(tacKey);
        }
        
        return device;
    }
    
    /**
     * Recherche les informations d'un appareil par son IMEI complet
     */
    public static DeviceInfo getDeviceByIMEI(String imei) {
        if (imei == null || imei.length() < 8) {
            return null;
        }
        
        return getDeviceByTAC(imei.substring(0, 8));
    }
    
    /**
     * Valide si un TAC existe dans la base de données
     */
    public static boolean isValidTAC(String tac) {
        return getDeviceByTAC(tac) != null;
    }
    
    /**
     * Valide si un IMEI a un TAC reconnu
     */
    public static boolean isValidIMEI(String imei) {
        return getDeviceByIMEI(imei) != null;
    }
    
    /**
     * Retourne le nombre d'entrées dans la base TAC
     */
    public static int getDatabaseSize() {
        if (!isLoaded) {
            loadDatabase();
        }
        return tacDatabase.size();
    }
    
    /**
     * Test de la base de données TAC
     */
    public static void main(String[] args) {
        System.out.println("=== TEST BASE TAC GSMA ===");
        System.out.println();
        
        // Charger la base
        loadDatabase();
        System.out.println("Taille de la base TAC: " + getDatabaseSize() + " entrées");
        System.out.println();
        
        // Test avec quelques IMEIs connus
        String[] testIMEIs = {
            "353247104467808", // Test IMEI
            "353328111234567", // Apple iPhone
            "354398111234567", // Apple iPhone
            "354569111234567", // Samsung
            "354570111234567"  // Samsung
        };
        
        for (String imei : testIMEIs) {
            DeviceInfo device = getDeviceByIMEI(imei);
            System.out.printf("IMEI %s: ", imei);
            if (device != null) {
                System.out.printf("✅ %s %s (%s)%n", device.getManufacturer(), device.getModel(), device.getMarketingName());
            } else {
                System.out.println("❌ TAC non trouvé");
            }
        }
    }
}
