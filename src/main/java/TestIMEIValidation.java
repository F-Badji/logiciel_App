package com.logicielapp;

import com.logicielapp.util.IMEIValidator;

public class TestIMEIValidation {
    public static void main(String[] args) {
        String testImei = args.length > 0 ? args[0] : "111111111111111";
        
        System.out.println("=== TEST VALIDATION IMEI ===");
        System.out.println("IMEI testé: " + testImei);
        System.out.println();
        
        IMEIValidator.ValidationResult result = IMEIValidator.validateIMEI(testImei);
        
        System.out.println("Résultat de validation:");
        System.out.println("✅ Valide: " + result.isValid());
        System.out.println("📝 Raison: " + result.getReason());
        
        if (result.isValid()) {
            System.out.println("📱 Appareil: " + result.getDeviceInfo());
            System.out.println("🏭 Fabricant: " + result.getManufacturer());
        }
        
        System.out.println();
        System.out.println("=== TESTS SUPPLÉMENTAIRES ===");
        
        // Test avec différents IMEIs problématiques
        String[] testIMEIs = {
            "111111111111111", // Tous identiques
            "123456789012345", // Séquentiel
            "000000000000000", // Tous zéros
            "353247104467808"  // IMEI avec checksum invalide
        };
        
        for (String imei : testIMEIs) {
            IMEIValidator.ValidationResult testResult = IMEIValidator.validateIMEI(imei);
            System.out.printf("IMEI %s: %s - %s%n", 
                imei, 
                testResult.isValid() ? "✅ VALIDE" : "❌ INVALIDE", 
                testResult.getReason());
        }
    }
}
