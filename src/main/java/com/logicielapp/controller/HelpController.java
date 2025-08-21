package com.logicielapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.awt.Desktop;
import java.net.URI;

public class HelpController {

    @FXML private Button homeBtn;
    @FXML private Button statisticsBtn;
    @FXML private Button parametersBtn;
    @FXML private Button helpBtn;
    
    @FXML private Button whatsappBtn;
    @FXML private Button whatsappContactBtn;
    @FXML private Button emailContactBtn;
    
    @FXML private Accordion faqAccordion;

    @FXML
    private void initialize() {
        // Initialiser l'accordion FAQ
        if (faqAccordion != null && !faqAccordion.getPanes().isEmpty()) {
            // Fermer tous les panneaux par défaut
            faqAccordion.setExpandedPane(null);
        }
    }

    @FXML
    private void openWhatsApp() {
        try {
            String whatsappUrl = "https://wa.me/221769719383";
            
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(whatsappUrl));
                } else {
                    showAlert("Impossible d'ouvrir WhatsApp. URL: " + whatsappUrl);
                }
            } else {
                showAlert("Fonction non supportée sur ce système. URL: " + whatsappUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de WhatsApp: " + e.getMessage());
        }
    }

    @FXML
    private void openEmail() {
        try {
            String emailUrl = "mailto:digitex.officiel@gmail.com";
            
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    desktop.mail(URI.create(emailUrl));
                } else {
                    showAlert("Impossible d'ouvrir l'email. Adresse: digitex.officiel@gmail.com");
                }
            } else {
                showAlert("Fonction non supportée sur ce système. Email: digitex.officiel@gmail.com");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur lors de l'ouverture de l'email: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Les méthodes de navigation ont été supprimées car cette interface s'ouvre maintenant dans une fenêtre séparée
    // La navigation se fait en fermant la fenêtre et en retournant à l'interface principale
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) homeBtn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void openStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics_window.fxml"));
            Parent statisticsWindow = loader.load();
            
            Stage statisticsStage = new Stage();
            statisticsStage.setTitle("📊 Statistiques - Logiciel de Déblocage Mobile");
            statisticsStage.initModality(Modality.APPLICATION_MODAL);
            statisticsStage.initOwner(homeBtn.getScene().getWindow());
            
            Scene scene = new Scene(statisticsWindow, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            statisticsStage.setScene(scene);
            
            statisticsStage.setResizable(true);
            statisticsStage.setMinWidth(800);
            statisticsStage.setMinHeight(600);
            
            statisticsStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openParameters() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/parameters_window.fxml"));
            Parent parametersWindow = loader.load();
            
            Stage parametersStage = new Stage();
            parametersStage.setTitle("⚙️ Paramètres - Logiciel de Déblocage Mobile");
            parametersStage.initModality(Modality.APPLICATION_MODAL);
            parametersStage.initOwner(homeBtn.getScene().getWindow());
            
            Scene scene = new Scene(parametersWindow, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            parametersStage.setScene(scene);
            
            parametersStage.setResizable(true);
            parametersStage.setMinWidth(800);
            parametersStage.setMinHeight(600);
            
            parametersStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
