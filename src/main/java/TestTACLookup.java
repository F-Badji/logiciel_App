import com.logicielapp.util.TACDatabase;

/**
 * Test TAC database lookup with various IMEI patterns
 */
public class TestTACLookup {
    
    public static void main(String[] args) {
        System.out.println("=== TEST TAC DATABASE LOOKUP ===\n");
        
        // Load TAC database
        TACDatabase.loadDatabase();
        System.out.println("Base TAC chargée: " + TACDatabase.getDatabaseSize() + " entrées\n");
        
        // Test avec TACs réels de la base GSMA
        String[] testTACs = {
            "35675904", // Oppo N1
            "49013920", // Nokia 1610
            "01194800", // Apple iPhone 3GS
            "01203000", // Apple iPhone 3GS
            "01242200", // Apple iPhone 4
            "01334500", // Apple iPhone 5
            "35250500", // Nokia 6820
            "35060680"  // Nokia 3310
        };
        
        System.out.println("--- Test TACs directs ---");
        for (String tac : testTACs) {
            TACDatabase.DeviceInfo device = TACDatabase.getDeviceByTAC(tac);
            if (device != null) {
                System.out.println("✅ TAC " + tac + ": " + device.getManufacturer() + " " + device.getModel());
            } else {
                System.out.println("❌ TAC " + tac + ": Non trouvé");
            }
        }
        
        System.out.println("\n--- Test IMEIs avec TACs réels ---");
        // Créer des IMEIs avec ces TACs
        for (String tac : testTACs) {
            String testImei = tac + "1234567"; // IMEI de 15 chiffres
            TACDatabase.DeviceInfo device = TACDatabase.getDeviceByIMEI(testImei);
            if (device != null) {
                System.out.println("✅ IMEI " + testImei + ": " + device.getManufacturer() + " " + device.getModel());
            } else {
                System.out.println("❌ IMEI " + testImei + ": Non trouvé");
            }
        }
        
        System.out.println("\n--- Test avec service DHRU intégré ---");
        // Tester l'intégration complète avec le service DHRU
        com.logicielapp.service.DHRUApiService dhruService = new com.logicielapp.service.DHRUApiService();
        
        // Test avec un IMEI Apple réel de la base
        String appleImei = "011948001234567"; // iPhone 3GS
        System.out.println("Test IMEI Apple: " + appleImei);
        dhruService.getDeviceInfo(appleImei).thenAccept(deviceInfo -> {
            System.out.println("Résultat Apple: " + deviceInfo.getBrand() + " " + deviceInfo.getModel());
        }).join();
        
        // Test avec un IMEI Nokia réel de la base
        String nokiaImei = "350606801234567"; // Nokia 3310
        System.out.println("Test IMEI Nokia: " + nokiaImei);
        dhruService.getDeviceInfo(nokiaImei).thenAccept(deviceInfo -> {
            System.out.println("Résultat Nokia: " + deviceInfo.getBrand() + " " + deviceInfo.getModel());
        }).join();
    }
}
