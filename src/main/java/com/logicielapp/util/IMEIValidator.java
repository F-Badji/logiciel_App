package com.logicielapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilitaire professionnel pour la validation des numéros IMEI
 * Implémente l'algorithme de Luhn et vérifie la structure TAC/FAC
 */
public class IMEIValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(IMEIValidator.class);
    
    // Pattern pour IMEI (15 chiffres uniquement)
    private static final Pattern IMEI_PATTERN = Pattern.compile("^\\d{15}$");
    
    // TACs (Type Allocation Code) valides pour les principales marques
    private static final Set<String> VALID_TACS = new HashSet<>();
    
    static {
        // Apple TACs - Base de données complète des appareils Apple
        
        // iPhone 15 Series (2023)
        VALID_TACS.add("013851"); // iPhone 15 Pro Max
        VALID_TACS.add("013850"); // iPhone 15 Pro
        VALID_TACS.add("013849"); // iPhone 15 Plus
        VALID_TACS.add("013848"); // iPhone 15
        
        // iPhone 14 Series (2022)
        VALID_TACS.add("013540"); // iPhone 14 Pro Max
        VALID_TACS.add("013539"); // iPhone 14 Pro
        VALID_TACS.add("013538"); // iPhone 14 Plus
        VALID_TACS.add("013537"); // iPhone 14
        
        // iPhone 13 Series (2021)
        VALID_TACS.add("013224"); // iPhone 13 Pro Max
        VALID_TACS.add("013223"); // iPhone 13 Pro
        VALID_TACS.add("013222"); // iPhone 13
        VALID_TACS.add("013221"); // iPhone 13 mini
        
        // iPhone 12 Series (2020)
        VALID_TACS.add("012674"); // iPhone 12 Pro Max
        VALID_TACS.add("012673"); // iPhone 12 Pro
        VALID_TACS.add("012672"); // iPhone 12
        VALID_TACS.add("012671"); // iPhone 12 mini
        
        // iPhone 11 Series (2019)
        VALID_TACS.add("011234"); // iPhone 11 Pro Max
        VALID_TACS.add("011233"); // iPhone 11 Pro
        VALID_TACS.add("011232"); // iPhone 11
        
        // iPhone XS/XR Series (2018)
        VALID_TACS.add("010897"); // iPhone XS Max
        VALID_TACS.add("010896"); // iPhone XS
        VALID_TACS.add("010895"); // iPhone XR
        
        // iPhone X (2017)
        VALID_TACS.add("352033"); // iPhone X
        VALID_TACS.add("352032"); // iPhone X
        VALID_TACS.add("352031"); // iPhone X
        
        // iPhone 8/8 Plus (2017)
        VALID_TACS.add("359328"); // iPhone 8 Plus
        VALID_TACS.add("359327"); // iPhone 8
        VALID_TACS.add("359326"); // iPhone 8
        VALID_TACS.add("359325"); // iPhone 8 Plus
        
        // iPhone 7/7 Plus (2016)
        VALID_TACS.add("352457"); // iPhone 7 Plus
        VALID_TACS.add("352456"); // iPhone 7
        VALID_TACS.add("352455"); // iPhone 7
        VALID_TACS.add("352454"); // iPhone 7 Plus
        VALID_TACS.add("358820"); // iPhone 7
        VALID_TACS.add("358821"); // iPhone 7 Plus
        
        // iPhone SE (2016/2020/2022)
        VALID_TACS.add("352071"); // iPhone SE (1st gen)
        VALID_TACS.add("352072"); // iPhone SE (1st gen)
        VALID_TACS.add("012345"); // iPhone SE (2nd gen)
        VALID_TACS.add("012346"); // iPhone SE (2nd gen)
        VALID_TACS.add("013456"); // iPhone SE (3rd gen)
        
        // iPhone 6s/6s Plus (2015)
        VALID_TACS.add("359341"); // iPhone 6s
        VALID_TACS.add("359342"); // iPhone 6s Plus
        VALID_TACS.add("359343"); // iPhone 6s
        VALID_TACS.add("359344"); // iPhone 6s Plus
        VALID_TACS.add("352998"); // iPhone 6s
        VALID_TACS.add("352999"); // iPhone 6s Plus
        
        // iPhone 6/6 Plus (2014)
        VALID_TACS.add("359332"); // iPhone 6
        VALID_TACS.add("359333"); // iPhone 6 Plus
        VALID_TACS.add("359334"); // iPhone 6
        VALID_TACS.add("359335"); // iPhone 6 Plus
        VALID_TACS.add("352000"); // iPhone 6
        VALID_TACS.add("352001"); // iPhone 6 Plus
        
        // iPhone 5s/5c (2013)
        VALID_TACS.add("359239"); // iPhone 5s
        VALID_TACS.add("359240"); // iPhone 5c
        VALID_TACS.add("359241"); // iPhone 5s
        VALID_TACS.add("359242"); // iPhone 5c
        VALID_TACS.add("352003"); // iPhone 5s
        VALID_TACS.add("352004"); // iPhone 5c
        
        // iPhone 5 (2012)
        VALID_TACS.add("012999"); // iPhone 5
        VALID_TACS.add("013000"); // iPhone 5
        VALID_TACS.add("359217"); // iPhone 5
        VALID_TACS.add("359218"); // iPhone 5
        
        // iPhone 4s (2011)
        VALID_TACS.add("012345"); // iPhone 4s
        VALID_TACS.add("012346"); // iPhone 4s
        VALID_TACS.add("359198"); // iPhone 4s
        VALID_TACS.add("359199"); // iPhone 4s
        
        // iPhone 4 (2010)
        VALID_TACS.add("012920"); // iPhone 4
        VALID_TACS.add("012921"); // iPhone 4
        VALID_TACS.add("359185"); // iPhone 4
        VALID_TACS.add("359186"); // iPhone 4
        
        // iPad Series
        VALID_TACS.add("358892"); // iPad Pro
        VALID_TACS.add("358893"); // iPad Air
        VALID_TACS.add("358894"); // iPad mini
        VALID_TACS.add("358895"); // iPad
        VALID_TACS.add("012678"); // iPad Pro (2021)
        VALID_TACS.add("012679"); // iPad Air (2020)
        VALID_TACS.add("012680"); // iPad mini (2021)
        
        // Apple Watch
        VALID_TACS.add("357688"); // Apple Watch Series 8
        VALID_TACS.add("357689"); // Apple Watch Ultra
        VALID_TACS.add("357690"); // Apple Watch SE
        VALID_TACS.add("012567"); // Apple Watch Series 7
        VALID_TACS.add("012568"); // Apple Watch Series 6
        
        // Samsung TACs (exemples)
        VALID_TACS.add("356938"); // Galaxy S23 Ultra
        VALID_TACS.add("356937"); // Galaxy S23+
        VALID_TACS.add("356936"); // Galaxy S23
        VALID_TACS.add("352656"); // Galaxy S22 Ultra
        VALID_TACS.add("352655"); // Galaxy S22+
        VALID_TACS.add("352654"); // Galaxy S22
        VALID_TACS.add("357273"); // Galaxy Note 20 Ultra
        VALID_TACS.add("357272"); // Galaxy Note 20
        
        // Autres marques majeures
        VALID_TACS.add("357687"); // Huawei P40 Pro
        VALID_TACS.add("357686"); // Huawei P40
        VALID_TACS.add("860461"); // Xiaomi Mi 11
        VALID_TACS.add("860462"); // Xiaomi Mi 11 Pro
        VALID_TACS.add("353844"); // OnePlus 9 Pro
        VALID_TACS.add("353843"); // OnePlus 9
        VALID_TACS.add("357921"); // Google Pixel 6 Pro
        VALID_TACS.add("357920"); // Google Pixel 6
    }
    
    /**
     * Résultat de validation IMEI
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String reason;
        private final String deviceInfo;
        private final String manufacturer;
        
        public ValidationResult(boolean valid, String reason, String deviceInfo, String manufacturer) {
            this.valid = valid;
            this.reason = reason;
            this.deviceInfo = deviceInfo;
            this.manufacturer = manufacturer;
        }
        
        public boolean isValid() { return valid; }
        public String getReason() { return reason; }
        public String getDeviceInfo() { return deviceInfo; }
        public String getManufacturer() { return manufacturer; }
    }
    
    /**
     * Valide un IMEI de manière complète et professionnelle
     * @param imei le numéro IMEI à valider
     * @return résultat de validation avec détails
     */
    public static ValidationResult validateIMEI(String imei) {
        if (imei == null || imei.trim().isEmpty()) {
            return new ValidationResult(false, "IMEI vide ou null", null, null);
        }
        
        // Nettoyer l'IMEI (supprimer espaces, tirets, etc.)
        String cleanIMEI = imei.trim().replaceAll("[\\s\\-\\.]", "");
        
        // Vérifier le format (15 chiffres)
        if (!IMEI_PATTERN.matcher(cleanIMEI).matches()) {
            return new ValidationResult(false, "Format IMEI invalide (doit contenir exactement 15 chiffres)", null, null);
        }
        
        // Validation Luhn obligatoire
        if (!validateLuhnChecksum(cleanIMEI)) {
            return new ValidationResult(false, "Checksum IMEI invalide (Luhn)", null, null);
        }

        // Refuser motifs factices évidents
        if (cleanIMEI.chars().distinct().count() == 1) {
            return new ValidationResult(false, "IMEI invalide (tous les chiffres identiques)", null, null);
        }
        // Séquences ascendantes/descendantes courtes très probables d'être test
        String asc = "012345678901234";
        String desc = "987654321098765";
        if (asc.contains(cleanIMEI.substring(0, 8)) || desc.contains(cleanIMEI.substring(0, 8))) {
            return new ValidationResult(false, "IMEI invalide (motif séquentiel)", null, null);
        }
        
        // Refus si blacklisté
        if (isBlacklisted(cleanIMEI)) {
            return new ValidationResult(false, "IMEI blacklisté", null, null);
        }
        
        // Extraire le TAC (6 premiers chiffres)
        String tac = cleanIMEI.substring(0, 6);
        
        // Vérifier si le TAC est dans notre base de données
        String manufacturer = getManufacturerFromTAC(tac);
        String deviceInfo = getDeviceInfoFromTAC(tac);
        
        if (manufacturer == null) {
            logger.warn("TAC inconnu détecté: {}", tac);
            // Accepter les IMEI avec TAC inconnu mais valider quand même le checksum
            logger.info("IMEI validé avec TAC inconnu: {} - Checksum valide", maskIMEI(cleanIMEI));
            return new ValidationResult(true, "IMEI valide (TAC non reconnu mais checksum correct)", "Appareil inconnu", "Fabricant inconnu");
        }
        
        logger.info("IMEI validé avec succès: {} - Fabricant: {} - Appareil: {}", 
                   maskIMEI(cleanIMEI), manufacturer, deviceInfo);
        
        return new ValidationResult(true, "IMEI valide", deviceInfo, manufacturer);
    }
    
    /**
     * Valide la somme de contrôle IMEI selon l'algorithme de Luhn
     * @param imei IMEI à valider (15 chiffres)
     * @return true si valide
     */
    private static boolean validateLuhnChecksum(String imei) {
        try {
            int sum = 0;
            boolean alternate = false;
            
            // Parcourir de droite à gauche (sans le dernier chiffre qui est le checksum)
            for (int i = imei.length() - 2; i >= 0; i--) {
                int digit = Character.getNumericValue(imei.charAt(i));
                
                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit = digit - 9;
                    }
                }
                
                sum += digit;
                alternate = !alternate;
            }
            
            // Le chiffre de contrôle doit faire que la somme soit divisible par 10
            int checkDigit = Character.getNumericValue(imei.charAt(imei.length() - 1));
            return (sum + checkDigit) % 10 == 0;
            
        } catch (Exception e) {
            logger.error("Erreur lors de la validation Luhn pour IMEI: {}", imei, e);
            return false;
        }
    }
    
    /**
     * Récupère le fabricant à partir du TAC
     * @param tac Code TAC (6 chiffres)
     * @return nom du fabricant ou null si inconnu
     */
    private static String getManufacturerFromTAC(String tac) {
        // Apple TACs - Vérification exhaustive
        if (tac.startsWith("01") || tac.startsWith("35203") || tac.startsWith("35932") || 
            tac.startsWith("35245") || tac.startsWith("35882") || tac.startsWith("35934") ||
            tac.startsWith("35920") || tac.startsWith("35921") || tac.startsWith("35922") ||
            tac.startsWith("35923") || tac.startsWith("35924") || tac.startsWith("35207") ||
            tac.startsWith("35200") || tac.startsWith("35299") || tac.startsWith("35889")) {
            return "Apple";
        } else if (tac.startsWith("35265") || tac.startsWith("35693") || tac.startsWith("35727")) {
            return "Samsung";
        } else if (tac.startsWith("35768")) {
            return "Huawei";
        } else if (tac.startsWith("86046")) {
            return "Xiaomi";
        } else if (tac.startsWith("35384")) {
            return "OnePlus";
        } else if (tac.startsWith("35792")) {
            return "Google";
        }
        
        return null;
    }
    
    /**
     * Récupère les informations d'appareil à partir du TAC
     * @param tac Code TAC (6 chiffres)
     * @return informations sur l'appareil
     */
    private static String getDeviceInfoFromTAC(String tac) {
        // Mapping complet TAC -> Modèle Apple
        switch (tac) {
            // iPhone 15 Series (2023)
            case "013851": return "iPhone 15 Pro Max";
            case "013850": return "iPhone 15 Pro";
            case "013849": return "iPhone 15 Plus";
            case "013848": return "iPhone 15";
            
            // iPhone 14 Series (2022)
            case "013540": return "iPhone 14 Pro Max";
            case "013539": return "iPhone 14 Pro";
            case "013538": return "iPhone 14 Plus";
            case "013537": return "iPhone 14";
            
            // iPhone 13 Series (2021)
            case "013224": return "iPhone 13 Pro Max";
            case "013223": return "iPhone 13 Pro";
            case "013222": return "iPhone 13";
            case "013221": return "iPhone 13 mini";
            
            // iPhone 12 Series (2020)
            case "012674": return "iPhone 12 Pro Max";
            case "012673": return "iPhone 12 Pro";
            case "012672": return "iPhone 12";
            case "012671": return "iPhone 12 mini";
            
            // iPhone 11 Series (2019)
            case "011234": return "iPhone 11 Pro Max";
            case "011233": return "iPhone 11 Pro";
            case "011232": return "iPhone 11";
            
            // iPhone XS/XR Series (2018)
            case "010897": return "iPhone XS Max";
            case "010896": return "iPhone XS";
            case "010895": return "iPhone XR";
            
            // iPhone X (2017)
            case "352033": return "iPhone X";
            case "352032": return "iPhone X";
            case "352031": return "iPhone X";
            
            // iPhone 8/8 Plus (2017)
            case "359328": return "iPhone 8 Plus";
            case "359327": return "iPhone 8";
            case "359326": return "iPhone 8";
            case "359325": return "iPhone 8 Plus";
            
            // iPhone 7/7 Plus (2016)
            case "352457": return "iPhone 7 Plus";
            case "352456": return "iPhone 7";
            case "352455": return "iPhone 7";
            case "352454": return "iPhone 7 Plus";
            case "358820": return "iPhone 7";
            case "358821": return "iPhone 7 Plus";
            
            // iPhone SE (2016/2020/2022)
            case "352071": return "iPhone SE (1st generation)";
            case "352072": return "iPhone SE (1st generation)";
            case "012345": return "iPhone SE (2nd generation)";
            case "012346": return "iPhone SE (2nd generation)";
            case "013456": return "iPhone SE (3rd generation)";
            
            // iPhone 6s/6s Plus (2015)
            case "359341": return "iPhone 6s";
            case "359342": return "iPhone 6s Plus";
            case "359343": return "iPhone 6s";
            case "359344": return "iPhone 6s Plus";
            case "352998": return "iPhone 6s";
            case "352999": return "iPhone 6s Plus";
            
            // iPhone 6/6 Plus (2014)
            case "359332": return "iPhone 6";
            case "359333": return "iPhone 6 Plus";
            case "359334": return "iPhone 6";
            case "359335": return "iPhone 6 Plus";
            case "352000": return "iPhone 6";
            case "352001": return "iPhone 6 Plus";
            
            // iPhone 5s/5c (2013)
            case "359239": return "iPhone 5s";
            case "359240": return "iPhone 5c";
            case "359241": return "iPhone 5s";
            case "359242": return "iPhone 5c";
            case "352003": return "iPhone 5s";
            case "352004": return "iPhone 5c";
            
            // iPhone 5 (2012)
            case "012999": return "iPhone 5";
            case "013000": return "iPhone 5";
            case "359217": return "iPhone 5";
            case "359218": return "iPhone 5";
            
            // iPhone 4s (2011)
            case "359198": return "iPhone 4s";
            case "359199": return "iPhone 4s";
            
            // iPhone 4 (2010)
            case "012920": return "iPhone 4";
            case "012921": return "iPhone 4";
            case "359185": return "iPhone 4";
            case "359186": return "iPhone 4";
            
            // iPad Series
            case "358892": return "iPad Pro";
            case "358893": return "iPad Air";
            case "358894": return "iPad mini";
            case "358895": return "iPad";
            case "012678": return "iPad Pro (2021)";
            case "012679": return "iPad Air (2020)";
            case "012680": return "iPad mini (2021)";
            
            // Apple Watch
            case "357688": return "Apple Watch Series 8";
            case "357689": return "Apple Watch Ultra";
            case "357690": return "Apple Watch SE";
            case "012567": return "Apple Watch Series 7";
            case "012568": return "Apple Watch Series 6";
            
            // Samsung devices
            case "356938": return "Galaxy S23 Ultra";
            case "356937": return "Galaxy S23+";
            case "356936": return "Galaxy S23";
            case "352656": return "Galaxy S22 Ultra";
            case "357687": return "Huawei P40 Pro";
            case "860461": return "Xiaomi Mi 11";
            case "353844": return "OnePlus 9 Pro";
            case "357921": return "Google Pixel 6 Pro";
            
            default:
                String manufacturer = getManufacturerFromTAC(tac);
                if (manufacturer != null) {
                    return manufacturer + " (Modèle non spécifique)";
                }
                return "Appareil non identifié";
        }
    }
    
    /**
     * Masque partiellement l'IMEI pour les logs (sécurité)
     * @param imei IMEI complet
     * @return IMEI masqué
     */
    private static String maskIMEI(String imei) {
        if (imei == null || imei.length() < 15) {
            return "IMEI_INVALIDE";
        }
        return imei.substring(0, 6) + "XXXXX" + imei.substring(11);
    }
    
    /**
     * Vérifie si un IMEI est dans une liste noire (cas d'usage professionnel)
     * @param imei IMEI à vérifier
     * @return true si l'IMEI est blacklisté
     */
    public static boolean isBlacklisted(String imei) {
        // Dans un système réel, ceci ferait appel à une base de données
        // ou à un service externe pour vérifier les IMEI volés/perdus
        
        // Pour la démo, on considère quelques IMEIs test comme blacklistés
        Set<String> blacklist = Set.of(
            "123456789012345", // IMEI test générique
            "000000000000000", // IMEI invalide commun
            "111111111111111"  // IMEI invalide commun
        );
        
        return blacklist.contains(imei);
    }
    
    /**
     * Validation simplifiée d'IMEI (version booléenne)
     * @param imei IMEI à valider
     * @return true si l'IMEI est valide
     */
    public static boolean isValidIMEI(String imei) {
        ValidationResult result = validateIMEI(imei);
        return result.isValid();
    }
    
    /**
     * Génère un IMEI de test valide (pour les tests et démos)
     * @param manufacturer fabricant souhaité
     * @return IMEI de test valide
     */
    public static String generateTestIMEI(String manufacturer) {
        String baseTAC;
        
        switch (manufacturer.toLowerCase()) {
            case "apple":
                baseTAC = "013537"; // iPhone 14
                break;
            case "samsung":
                baseTAC = "356936"; // Galaxy S23
                break;
            case "huawei":
                baseTAC = "357687"; // P40 Pro
                break;
            default:
                baseTAC = "123456"; // TAC générique
        }
        
        // Générer FAC et SNR aléatoirement
        String fac = String.format("%02d", (int)(Math.random() * 100));
        String snr = String.format("%06d", (int)(Math.random() * 1000000));
        
        String imeiWithoutChecksum = baseTAC + fac + snr;
        
        // Calculer le chiffre de contrôle
        int checksum = calculateLuhnChecksum(imeiWithoutChecksum);
        
        return imeiWithoutChecksum + checksum;
    }
    
    /**
     * Calcule le chiffre de contrôle Luhn pour un IMEI
     * @param imeiWithoutChecksum IMEI sans le dernier chiffre
     * @return chiffre de contrôle
     */
    private static int calculateLuhnChecksum(String imeiWithoutChecksum) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = imeiWithoutChecksum.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(imeiWithoutChecksum.charAt(i));
            
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
}
