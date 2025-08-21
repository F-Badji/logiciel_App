import com.logicielapp.service.RealDeviceDetectionService;
import com.logicielapp.model.Device;
import java.util.List;

/**
 * Test simple du service de détection réelle
 */
public class TestRealDetection {
    public static void main(String[] args) {
        System.out.println("🔍 Test du service de détection réelle...");
        
        RealDeviceDetectionService service = new RealDeviceDetectionService();
        
        // Test de détection iOS
        System.out.println("\n📱 Test détection iOS:");
        List<Device> iosDevices = service.detectiOSDevices();
        if (iosDevices.isEmpty()) {
            System.out.println("Aucun appareil iOS détecté");
        } else {
            for (Device device : iosDevices) {
                System.out.println("- " + device.getBrand() + " " + device.getModel());
                System.out.println("  IMEI: " + device.getImei());
                System.out.println("  Serial: " + device.getSerialNumber());
                System.out.println("  OS: " + device.getOsVersion());
            }
        }
        
        // Test de détection Android  
        System.out.println("\n🤖 Test détection Android:");
        List<Device> androidDevices = service.detectAndroidDevices();
        if (androidDevices.isEmpty()) {
            System.out.println("Aucun appareil Android détecté (ADB requis)");
        } else {
            for (Device device : androidDevices) {
                System.out.println("- " + device.getBrand() + " " + device.getModel());
                System.out.println("  IMEI: " + device.getImei());
                System.out.println("  Serial: " + device.getSerialNumber());
                System.out.println("  OS: " + device.getOsVersion());
            }
        }
        
        // Test de détection complète
        System.out.println("\n🔍 Test détection complète:");
        List<Device> allDevices = service.detectAllConnectedDevices();
        System.out.println("Appareils détectés: " + allDevices.size());
        
        for (Device device : allDevices) {
            System.out.println("\n✅ Appareil trouvé:");
            System.out.println("  Marque: " + device.getBrand());
            System.out.println("  Modèle: " + device.getModel());
            System.out.println("  Plateforme: " + device.getPlatform());
            System.out.println("  IMEI: " + device.getImei());
            System.out.println("  Numéro de série: " + device.getSerialNumber());
            System.out.println("  Version OS: " + device.getOsVersion());
            System.out.println("  Statut: " + device.getStatus());
            System.out.println("  Connexion: " + device.getConnectionType());
        }
        
        // Test d'IMEI stable (doit être identique avec le même serial)
        System.out.println("\n🎯 Test stabilité IMEI:");
        if (!allDevices.isEmpty()) {
            Device device1 = allDevices.get(0);
            List<Device> devices2 = service.detectAllConnectedDevices();
            if (!devices2.isEmpty()) {
                Device device2 = devices2.get(0);
                boolean sameIMEI = device1.getImei().equals(device2.getImei());
                System.out.println("IMEI stable: " + (sameIMEI ? "✅ OUI" : "❌ NON"));
                System.out.println("IMEI 1: " + device1.getImei());
                System.out.println("IMEI 2: " + device2.getImei());
            }
        }
        
        System.out.println("\n✅ Test terminé!");
    }
}
