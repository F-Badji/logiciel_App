package com.logicielapp.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import java.util.*;

@Controller
@RequestMapping("/help")
public class HelpController {

    @GetMapping
    public String helpPage() {
        return "aide";
    }

    @GetMapping("/api/contact-info")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContactInfo() {
        Map<String, Object> contactInfo = new HashMap<>();
        
        // Informations de contact
        Map<String, Object> whatsapp = new HashMap<>();
        whatsapp.put("number", "221769719383");
        whatsapp.put("displayNumber", "76-971-93-83");
        whatsapp.put("url", "https://wa.me/221769719383");
        whatsapp.put("availability", "24h/24, 7j/7");
        
        Map<String, Object> email = new HashMap<>();
        email.put("address", "digitex.officiel@gmail.com");
        email.put("responseTime", "Réponse sous 24h");
        
        contactInfo.put("whatsapp", whatsapp);
        contactInfo.put("email", email);
        
        return ResponseEntity.ok(contactInfo);
    }

    @GetMapping("/api/faq")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFAQ() {
        List<Map<String, Object>> faqList = new ArrayList<>();
        
        faqList.add(createFAQItem(
            "Comment vérifier un IMEI ?",
            "Entrez simplement votre numéro IMEI de 15 chiffres dans le champ prévu à cet effet sur la page d'accueil et cliquez sur \"Vérifier IMEI\". Notre système utilisera plusieurs APIs pour récupérer les informations complètes de votre appareil."
        ));
        
        faqList.add(createFAQItem(
            "Que faire si mon IMEI est détecté comme fake ?",
            "Si votre IMEI est détecté comme fake, cela signifie qu'il ne respecte pas les standards GSMA ou qu'il contient des patterns invalides (tous les chiffres identiques, séquences, etc.). Vérifiez que vous avez saisi le bon IMEI depuis les paramètres de votre téléphone."
        ));
        
        faqList.add(createFAQItem(
            "Quelles informations puis-je obtenir ?",
            "Notre système peut récupérer : la marque et le modèle, la couleur et la capacité de stockage, le statut de verrouillage opérateur, le statut de liste noire, les informations de garantie, le statut d'activation, et pour les appareils Apple, le statut iCloud."
        ));
        
        faqList.add(createFAQItem(
            "Les données sont-elles sécurisées ?",
            "Oui, nous prenons la sécurité très au sérieux. Les IMEI sont partiellement masqués dans les logs, toutes les communications sont chiffrées, et nous ne stockons aucune donnée personnelle de manière permanente."
        ));
        
        faqList.add(createFAQItem(
            "Comment contacter le support ?",
            "Vous pouvez nous contacter directement via WhatsApp au 76-971-93-83 ou par email à digitex.officiel@gmail.com. Notre équipe est disponible pour répondre à toutes vos questions et vous aider avec le déblocage de vos appareils."
        ));
        
        return ResponseEntity.ok(faqList);
    }

    @GetMapping("/api/features")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getFeatures() {
        List<Map<String, Object>> features = new ArrayList<>();
        
        features.add(createFeature("🔍", "Vérification IMEI", "Vérification complète avec base TAC GSMA étendue"));
        features.add(createFeature("🛡️", "Détection Fake", "Détection automatique des IMEI invalides"));
        features.add(createFeature("📊", "Statistiques", "Tableau de bord complet avec métriques"));
        features.add(createFeature("⚙️", "Configuration", "Paramètres avancés et personnalisation"));
        
        return ResponseEntity.ok(features);
    }

    @PostMapping("/api/contact")
    @ResponseBody
    public ResponseEntity<Map<String, String>> submitContactForm(@RequestBody Map<String, String> contactForm) {
        // Validation des données
        if (!contactForm.containsKey("name") || contactForm.get("name").trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Le nom est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (!contactForm.containsKey("email") || contactForm.get("email").trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "L'email est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        if (!contactForm.containsKey("message") || contactForm.get("message").trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Le message est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Ici vous pourriez envoyer l'email ou sauvegarder le message
        System.out.println("Nouveau message de contact reçu:");
        System.out.println("Nom: " + contactForm.get("name"));
        System.out.println("Email: " + contactForm.get("email"));
        System.out.println("Message: " + contactForm.get("message"));
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Votre message a été envoyé avec succès. Nous vous répondrons dans les plus brefs délais.");
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createFAQItem(String question, String answer) {
        Map<String, Object> faq = new HashMap<>();
        faq.put("question", question);
        faq.put("answer", answer);
        return faq;
    }

    private Map<String, Object> createFeature(String icon, String title, String description) {
        Map<String, Object> feature = new HashMap<>();
        feature.put("icon", icon);
        feature.put("title", title);
        feature.put("description", description);
        return feature;
    }
}
