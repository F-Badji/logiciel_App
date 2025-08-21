
import com.logicielapp.service.DHRUApiService;

public class TestIFreeCheckAPI {
    public static void main(String[] args) {
        DHRUApiService service = new DHRUApiService();
        
        System.out.println("=== TEST IFREECHECK API ===");
        System.out.println();
        
        // Test de connectivité
        boolean connectivity = service.testIFreeCheckConnectivity();
        System.out.println("Connectivité iFreeCheck: " + (connectivity ? "✅ OK" : "❌ ÉCHEC"));
        System.out.println();
        
        // Test avec IMEI Apple valide (sans validation stricte)
        String testImei = "353328111234567"; // IMEI Apple avec checksum valide
        System.out.println("Test avec IMEI Apple: " + testImei);
        
        try {
            // Appel direct sans validation stricte pour tester l'API
            DHRUApiService.DeviceInfo deviceInfo = service.getDeviceInfo(testImei).get();
            
            System.out.println();
            System.out.println("=== RÉSULTATS ===");
            System.out.println("Succès: " + deviceInfo.isSuccess());
            
            if (deviceInfo.isSuccess()) {
                System.out.println("📱 Marque: " + deviceInfo.getBrand());
                System.out.println("📱 Modèle: " + deviceInfo.getModel());
                System.out.println("💾 Stockage: " + deviceInfo.getStorage());
                System.out.println("🎨 Couleur: " + deviceInfo.getColor());
                System.out.println("🔒 Statut iCloud: " + deviceInfo.getIcloudStatus());
                System.out.println("📶 Opérateur: " + deviceInfo.getCarrier());
                System.out.println("🔓 Statut SIM: " + deviceInfo.getSimlockStatus());
                System.out.println("⚫ Blacklist: " + deviceInfo.getBlacklistStatus());
                System.out.println("🛡️ Garantie: " + deviceInfo.getWarrantyStatus());
            } else {
                System.out.println("❌ Erreur: " + deviceInfo.getErrorMessage());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
