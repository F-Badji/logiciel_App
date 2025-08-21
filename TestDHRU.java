import com.logicielapp.service.DHRUApiService;

public class TestDHRU {
    public static void main(String[] args) {
        // Test avec vos credentials
        System.setProperty("DHRU_API_KEY", "0a2GL-R6r04-7FBW6-LhbhR-Suwp3-dhqDr");
        System.setProperty("DHRU_USERNAME", "Ppais");
        System.setProperty("DHRU_BASE_URL", "https://dhru.checkimei.com");
        System.setProperty("DHRU_FORMAT", "json");
        
        DHRUApiService service = new DHRUApiService();
        String testImei = args.length > 0 ? args[0] : "353247104467808"; // IMEI depuis args ou défaut
        
        System.out.println("=== TEST DHRU API ===");
        System.out.println("IMEI: " + testImei);
        System.out.println();
        
        // Debug complet
        service.debugApiCall();
        
        // Test getDeviceInfo
        System.out.println("=== TEST getDeviceInfo ===");
        service.getDeviceInfo(testImei).thenAccept(result -> {
            System.out.println("Succès: " + result.isSuccess());
            if (!result.isSuccess()) {
                System.out.println("Erreur: " + result.getErrorMessage());
            } else {
                System.out.println("Modèle: " + result.getModel());
                System.out.println("Marque: " + result.getBrand());
            }
        }).join();
    }
}
