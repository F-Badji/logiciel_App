package com.logicielapp.web;

import com.logicielapp.service.DHRUApiService;
import com.logicielapp.util.IMEIValidator;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class IMEIController {
    
    private final DHRUApiService dhruService = new DHRUApiService();
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/api/verify-imei")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyIMEI(@RequestBody Map<String, String> request) {
        String imei = request.get("imei");
        Map<String, Object> response = new HashMap<>();
        
        if (imei == null || imei.trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "IMEI requis");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validation stricte IMEI
        IMEIValidator.ValidationResult validation = IMEIValidator.validateIMEI(imei);
        if (!validation.isValid()) {
            response.put("success", false);
            response.put("error", validation.getReason());
            response.put("fake", true);
            return ResponseEntity.ok(response);
        }
        
        try {
            // Appel API pour récupérer les vraies informations
            CompletableFuture<DHRUApiService.DeviceInfo> future = dhruService.getDeviceInfo(imei);
            DHRUApiService.DeviceInfo deviceInfo = future.get();
            
            if (deviceInfo.isSuccess()) {
                response.put("success", true);
                response.put("imei", imei);
                response.put("brand", deviceInfo.getBrand());
                response.put("model", deviceInfo.getModel());
                response.put("color", deviceInfo.getColor());
                response.put("storage", deviceInfo.getStorage());
                response.put("simlock", deviceInfo.getSimlockStatus());
                response.put("blacklist", deviceInfo.getBlacklistStatus());
                response.put("warranty", deviceInfo.getWarrantyStatus());
                response.put("activation", deviceInfo.getActivationStatus());
                response.put("fake", false);
            } else {
                response.put("success", false);
                response.put("error", deviceInfo.getErrorMessage());
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Erreur lors de la vérification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
