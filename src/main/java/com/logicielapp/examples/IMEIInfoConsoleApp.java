package com.logicielapp.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Application console simple pour démontrer l'utilisation de l'API DHRU Fusion
 * Récupère les informations détaillées d'un iPhone via son IMEI
 */
public class IMEIInfoConsoleApp {

    private static final String API_KEY = "8AE-VC2-G18-1K7-K73-8FI-4H4-2AU";
    private static final String SERVICE_ID = "3"; // iCloud ON/OFF
    private static final String FORMAT = "beta"; // JSON structuré

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("🔎 === Application de Récupération d'Informations IMEI ===");
        System.out.println("📱 Cette application utilise l'API DHRU Fusion pour récupérer");
        System.out.println("   les informations détaillées d'un iPhone via son IMEI\n");
        
        while (true) {
            System.out.print("🔎 Entrez l'IMEI de l'iPhone (ou 'quit' pour quitter) : ");
            String input = scanner.nextLine().trim();
            
            if ("quit".equalsIgnoreCase(input)) {
                System.out.println("👋 Au revoir !");
                break;
            }
            
            // Valider le format IMEI
            if (!isValidIMEIFormat(input)) {
                System.out.println("❌ Format IMEI invalide. L'IMEI doit contenir exactement 15 chiffres.");
                System.out.println("💡 Astuce : Utilisez *#06# sur votre iPhone pour obtenir l'IMEI\n");
                continue;
            }
            
            // Récupérer les informations via l'API
            getDeviceInfoFromAPI(input);
            System.out.println(); // Ligne vide pour la lisibilité
        }
        
        scanner.close();
    }
    
    /**
     * Récupère les informations d'appareil via l'API DHRU
     * @param imei IMEI de l'appareil
     */
    private static void getDeviceInfoFromAPI(String imei) {
        String urlString = String.format(
                "https://sickw.com/api.php?format=%s&key=%s&imei=%s&service=%s",
                FORMAT, API_KEY, imei, SERVICE_ID
        );

        try {
            System.out.println("🌐 Connexion à l'API DHRU Fusion...");
            
            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "IMEIInfoApp/1.0");

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                // Parser et afficher la réponse
                parseAndDisplayResponse(response.toString(), imei);
                
            } else {
                System.out.println("❌ Erreur HTTP : " + responseCode);
                System.out.println("   L'API DHRU n'est peut-être pas accessible actuellement.");
            }

        } catch (Exception e) {
            System.out.println("❌ Erreur de connexion : " + e.getMessage());
            System.out.println("   Vérifiez votre connexion internet et réessayez.");
        }
    }
    
    /**
     * Parse et affiche la réponse JSON de l'API
     * @param jsonResponse réponse JSON brute
     * @param imei IMEI original
     */
    private static void parseAndDisplayResponse(String jsonResponse, String imei) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(jsonResponse);
            String status = json.has("status") ? json.get("status").asText() : "unknown";
            
            if ("success".equals(status)) {
                JsonNode result = json.get("result");
                
                System.out.println("Informations récupérées avec succès !");
                System.out.println("===== INFORMATIONS DE L'APPAREIL =====");
                System.out.println("IMEI: " + maskIMEI(imei));
                
                // Afficher toutes les informations disponibles
                result.fieldNames().forEachRemaining(key -> {
                    String value = result.get(key).asText();
                    String emoji = getEmojiForKey(key);
                    String displayKey = formatKeyName(key);
                    
                    System.out.println(emoji + " " + displayKey + ": " + value);
                });
                
                // Afficher des informations spéciales selon le contenu
                displaySpecialInfo(result);
                
            } else {
                String errorMsg = json.has("result") ? json.get("result").asText() : "Réponse inconnue";
                System.out.println("Erreur de l'API : " + errorMsg);
                
                if (errorMsg.toLowerCase().contains("invalid imei")) {
                    System.out.println("Vérifiez que l'IMEI est correct et correspond à un appareil Apple.");
                } else if (errorMsg.toLowerCase().contains("not found")) {
                    System.out.println("Cet IMEI n'existe pas dans la base de données.");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement de la réponse : " + e.getMessage());
            System.out.println("Réponse brute : " + jsonResponse);
        }
    }
    
    /**
     * Affiche des informations spéciales selon le contenu
     * @param result objet JSON avec les données de l'appareil
     */
    private static void displaySpecialInfo(JsonNode result) {
        // Analyser le statut iCloud
        String icloudStatus = result.has("icloud_status") ? result.get("icloud_status").asText() : "";
        if (!icloudStatus.isEmpty()) {
            System.out.println("\n===== ANALYSE DU STATUT ICLOUD =====");
            if (icloudStatus.toLowerCase().contains("off") || 
                icloudStatus.toLowerCase().contains("disabled")) {
                System.out.println("iCloud désactivé - Déblocage possible");
                System.out.println("✅ iCloud désactivé - Déblocage possible");
            } else if (icloudStatus.toLowerCase().contains("on") || 
                      icloudStatus.toLowerCase().contains("enabled")) {
                System.out.println("⚠️  iCloud activé - Déblocage complexe");
            } else {
                System.out.println("ℹ️  Statut iCloud : " + icloudStatus);
            }
        }
        
        // Analyser la garantie
        String warranty = result.has("warranty") ? result.get("warranty").asText() : "";
        if (!warranty.isEmpty()) {
            System.out.println("\n🛡️ === INFORMATIONS GARANTIE ===");
            if (warranty.toLowerCase().contains("active") || 
                warranty.toLowerCase().contains("valid")) {
                System.out.println("✅ Appareil sous garantie");
            } else if (warranty.toLowerCase().contains("expired")) {
                System.out.println("⚠️  Garantie expirée");
            } else {
                System.out.println("ℹ️  Statut garantie : " + warranty);
            }
        }
    }
    
    /**
     * Retourne l'emoji approprié pour une clé de données
     * @param key clé de données
     * @return emoji correspondant
     */
    private static String getEmojiForKey(String key) {
        switch (key.toLowerCase()) {
            case "model": return "📱";
            case "capacity": return "💾";
            case "color": return "🎨";
            case "icloud_status": return "☁️";
            case "carrier": return "📡";
            case "country": return "🌍";
            case "warranty": return "🛡️";
            case "serial": return "🔢";
            case "activation_status": return "🔓";
            case "purchase_date": return "📅";
            case "last_backup": return "💾";
            default: return "🔹";
        }
    }
    
    /**
     * Formate le nom d'une clé pour l'affichage
     * @param key clé brute
     * @return nom formaté
     */
    private static String formatKeyName(String key) {
        switch (key.toLowerCase()) {
            case "model": return "Modèle";
            case "capacity": return "Capacité";
            case "color": return "Couleur";
            case "icloud_status": return "Statut iCloud";
            case "carrier": return "Opérateur";
            case "country": return "Pays d'origine";
            case "warranty": return "Garantie";
            case "serial": return "Numéro de série";
            case "activation_status": return "Statut d'activation";
            case "purchase_date": return "Date d'achat";
            case "last_backup": return "Dernière sauvegarde";
            default: return key.replace("_", " ");
        }
    }
    
    /**
     * Valide le format d'un IMEI
     * @param imei IMEI à valider
     * @return true si le format est valide
     */
    private static boolean isValidIMEIFormat(String imei) {
        if (imei == null) return false;
        
        // Nettoyer l'IMEI (supprimer espaces, tirets, etc.)
        String cleanIMEI = imei.replaceAll("[\\s\\-\\.]", "");
        
        // Vérifier qu'il contient exactement 15 chiffres
        return cleanIMEI.matches("^\\d{15}$");
    }
    
    /**
     * Masque partiellement l'IMEI pour l'affichage (sécurité)
     * @param imei IMEI complet
     * @return IMEI masqué
     */
    private static String maskIMEI(String imei) {
        if (imei == null || imei.length() < 15) {
            return "IMEI_INVALIDE";
        }
        return imei.substring(0, 6) + "XXXXX" + imei.substring(11);
    }
}
