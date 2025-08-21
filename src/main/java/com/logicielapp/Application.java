package com.logicielapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.application.Platform;

@SpringBootApplication
public class Application {
    private static ConfigurableApplicationContext springContext;
    
    public static void main(String[] args) {
        // Démarrer Spring Boot en mode web
        System.setProperty("java.awt.headless", "false");
        System.setProperty("server.port", "8080");
        
        springContext = SpringApplication.run(Application.class, args);
        
        // Démarrer JavaFX en parallèle
        Platform.startup(() -> {
            try {
                new MobileUnlockApp().start(new javafx.stage.Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        System.out.println("=================================================");
        System.out.println("🚀 Application démarrée avec succès!");
        System.out.println("📱 Interface JavaFX: Application de bureau");
        System.out.println("🌐 Interface Web: http://localhost:8080");
        System.out.println("📊 Statistiques: http://localhost:8080/statistiques.html");
        System.out.println("⚙️ Paramètres: http://localhost:8080/parametres.html");
        System.out.println("❓ Aide: http://localhost:8080/aide.html");
        System.out.println("=================================================");
    }
    
    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}
