package com.logicielapp.ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashScreen {
    private Stage splashStage;
    private ProgressBar progressBar;
    private Label statusLabel;
    private Label percentageLabel;
    private VBox particlesContainer;
    private List<Circle> particles = new ArrayList<>();
    private Timeline particleAnimation;
    
    public void show(Stage primaryStage, Runnable onComplete) {
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        splashStage.setAlwaysOnTop(true);
        
        // Conteneur principal avec fond dégradé
        StackPane root = new StackPane();
        root.setPrefSize(700, 450);
        root.getStyleClass().add("splash-root");
        
        // Fond avec effet de dégradé animé
        Rectangle background = new Rectangle(700, 450);
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, null,
            new Stop(0, Color.web("#0f0c29")),
            new Stop(0.5, Color.web("#302b63")),
            new Stop(1, Color.web("#24243e"))
        );
        background.setFill(gradient);
        
        // Effet de flou pour le fond
        GaussianBlur blur = new GaussianBlur(0);
        background.setEffect(blur);
        
        // Container pour les particules animées
        particlesContainer = new VBox();
        particlesContainer.setAlignment(Pos.CENTER);
        particlesContainer.setMouseTransparent(true);
        createParticles();
        
        // Container principal du contenu
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(50));
        
        // Logo animé avec effet de rotation 3D
        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(150, 150);
        
        // Cercles concentriques animés
        for (int i = 0; i < 3; i++) {
            Circle circle = new Circle(50 - i * 15);
            circle.setFill(Color.TRANSPARENT);
            circle.setStroke(Color.web("#00d4ff", 0.3 + i * 0.2));
            circle.setStrokeWidth(2);
            
            RotateTransition rotate = new RotateTransition(Duration.seconds(3 + i), circle);
            rotate.setByAngle(360);
            rotate.setCycleCount(Timeline.INDEFINITE);
            rotate.setInterpolator(Interpolator.LINEAR);
            rotate.play();
            
            logoContainer.getChildren().add(circle);
        }
        
        // Icône centrale
        Label logoIcon = new Label("🔓");
        logoIcon.setStyle("-fx-font-size: 60px;");
        logoIcon.setEffect(new Glow(0.8));
        
        // Animation de pulsation pour l'icône
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), logoIcon);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        
        logoContainer.getChildren().add(logoIcon);
        
        // Titre principal avec effet de typing
        Label titleLabel = new Label("");
        titleLabel.getStyleClass().add("splash-title");
        String fullTitle = "DIGITEX UNLOCK PRO";
        
        // Animation de typing pour le titre
        Timeline typeWriter = new Timeline();
        for (int i = 0; i <= fullTitle.length(); i++) {
            final int index = i;
            KeyFrame frame = new KeyFrame(Duration.millis(100 * i), e -> {
                titleLabel.setText(fullTitle.substring(0, index));
            });
            typeWriter.getKeyFrames().add(frame);
        }
        
        // Sous-titre avec fade-in
        Label subtitleLabel = new Label("Professional Mobile Unlocking Solution");
        subtitleLabel.getStyleClass().add("splash-subtitle");
        subtitleLabel.setOpacity(0);
        
        FadeTransition fadeInSubtitle = new FadeTransition(Duration.seconds(2), subtitleLabel);
        fadeInSubtitle.setFromValue(0);
        fadeInSubtitle.setToValue(1);
        fadeInSubtitle.setDelay(Duration.seconds(1));
        
        // Version
        Label versionLabel = new Label("Version 2.0.0 Professional Edition");
        versionLabel.getStyleClass().add("splash-version");
        versionLabel.setOpacity(0);
        
        FadeTransition fadeInVersion = new FadeTransition(Duration.seconds(1), versionLabel);
        fadeInVersion.setFromValue(0);
        fadeInVersion.setToValue(0.7);
        fadeInVersion.setDelay(Duration.seconds(1.5));
        
        // Container pour la barre de progression
        VBox progressContainer = new VBox(10);
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setMaxWidth(400);
        
        // Barre de progression personnalisée
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(8);
        progressBar.getStyleClass().add("splash-progress");
        
        // Effet de lueur pour la barre de progression
        Glow progressGlow = new Glow(0.6);
        progressBar.setEffect(progressGlow);
        
        // Labels de statut
        HBox statusContainer = new HBox(20);
        statusContainer.setAlignment(Pos.CENTER);
        
        statusLabel = new Label("Initializing...");
        statusLabel.getStyleClass().add("splash-status");
        
        percentageLabel = new Label("0%");
        percentageLabel.getStyleClass().add("splash-percentage");
        
        statusContainer.getChildren().addAll(statusLabel, percentageLabel);
        
        progressContainer.getChildren().addAll(progressBar, statusContainer);
        
        // Ajout des éléments au conteneur principal
        mainContent.getChildren().addAll(
            logoContainer,
            titleLabel,
            subtitleLabel,
            versionLabel,
            new Region(), // Spacer
            progressContainer
        );
        
        // Copyright
        Label copyrightLabel = new Label("© 2024 Digitex Technologies. All rights reserved.");
        copyrightLabel.getStyleClass().add("splash-copyright");
        StackPane.setAlignment(copyrightLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(copyrightLabel, new Insets(0, 0, 20, 0));
        
        // Ajout de tous les éléments à la racine
        root.getChildren().addAll(background, particlesContainer, mainContent, copyrightLabel);
        
        // Création de la scène
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/css/splash.css").toExternalForm());
        
        splashStage.setScene(scene);
        splashStage.centerOnScreen();
        splashStage.show();
        
        // Démarrer les animations
        typeWriter.play();
        fadeInSubtitle.play();
        fadeInVersion.play();
        animateParticles();
        
        // Animation de la barre de progression
        simulateLoading(onComplete);
    }
    
    private void createParticles() {
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            Circle particle = new Circle(random.nextDouble() * 3 + 1);
            particle.setFill(Color.web("#00d4ff", random.nextDouble() * 0.5 + 0.1));
            particle.setTranslateX(random.nextDouble() * 700);
            particle.setTranslateY(random.nextDouble() * 450);
            particle.setEffect(new GaussianBlur(1));
            particles.add(particle);
            particlesContainer.getChildren().add(particle);
        }
    }
    
    private void animateParticles() {
        particleAnimation = new Timeline();
        Random random = new Random();
        
        for (Circle particle : particles) {
            // Animation de mouvement aléatoire
            TranslateTransition translate = new TranslateTransition(
                Duration.seconds(random.nextDouble() * 20 + 10), particle
            );
            translate.setFromX(particle.getTranslateX());
            translate.setFromY(particle.getTranslateY());
            translate.setToX(random.nextDouble() * 700);
            translate.setToY(random.nextDouble() * 450);
            translate.setCycleCount(Timeline.INDEFINITE);
            translate.setAutoReverse(true);
            translate.setInterpolator(Interpolator.EASE_BOTH);
            translate.play();
            
            // Animation de fade
            FadeTransition fade = new FadeTransition(
                Duration.seconds(random.nextDouble() * 3 + 2), particle
            );
            fade.setFromValue(particle.getOpacity());
            fade.setToValue(random.nextDouble() * 0.8);
            fade.setCycleCount(Timeline.INDEFINITE);
            fade.setAutoReverse(true);
            fade.play();
        }
    }
    
    private void simulateLoading(Runnable onComplete) {
        Timeline loadingTimeline = new Timeline();
        
        String[] loadingMessages = {
            "Initializing core modules...",
            "Loading device drivers...",
            "Connecting to services...",
            "Verifying licenses...",
            "Loading user interface...",
            "Configuring settings...",
            "Preparing workspace...",
            "Finalizing setup...",
            "Ready to launch!"
        };
        
        for (int i = 0; i <= 100; i++) {
            final int progress = i;
            final String message = loadingMessages[Math.min(i / 12, loadingMessages.length - 1)];
            
            KeyFrame frame = new KeyFrame(Duration.millis(30 * i), e -> {
                Platform.runLater(() -> {
                    progressBar.setProgress(progress / 100.0);
                    percentageLabel.setText(progress + "%");
                    statusLabel.setText(message);
                    
                    // Effet de pulsation sur le pourcentage
                    if (progress % 10 == 0) {
                        ScaleTransition scale = new ScaleTransition(Duration.millis(200), percentageLabel);
                        scale.setFromX(1.0);
                        scale.setFromY(1.0);
                        scale.setToX(1.2);
                        scale.setToY(1.2);
                        scale.setCycleCount(2);
                        scale.setAutoReverse(true);
                        scale.play();
                    }
                });
            });
            loadingTimeline.getKeyFrames().add(frame);
        }
        
        loadingTimeline.setOnFinished(e -> {
            // Animation de fermeture
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), splashStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                splashStage.close();
                if (onComplete != null) {
                    onComplete.run();
                }
            });
            fadeOut.play();
        });
        
        loadingTimeline.play();
    }
    
    public void close() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        if (splashStage != null) {
            splashStage.close();
        }
    }
    
    public void close(Runnable onClosed) {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        if (splashStage != null) {
            // Animation de fermeture
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), splashStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                splashStage.close();
                if (onClosed != null) {
                    onClosed.run();
                }
            });
            fadeOut.play();
        } else if (onClosed != null) {
            onClosed.run();
        }
    }
    
    public void updateLoadingMessage(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
    }
}
