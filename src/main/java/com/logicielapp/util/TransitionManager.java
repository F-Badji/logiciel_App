package com.logicielapp.util;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.DropShadow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestionnaire des animations de transition entre les écrans
 * Fournit des transitions fluides et modernes pour l'application
 */
public class TransitionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(TransitionManager.class);
    
    // Durées des animations
    private static final Duration FADE_DURATION = Duration.millis(300);
    private static final Duration SLIDE_DURATION = Duration.millis(400);
    private static final Duration SCALE_DURATION = Duration.millis(350);
    private static final Duration BLUR_DURATION = Duration.millis(500);
    
    /**
     * Transition de fondu (fade in/out)
     */
    public static void fadeTransition(Node node, double fromOpacity, double toOpacity, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(FADE_DURATION, node);
        fade.setFromValue(fromOpacity);
        fade.setToValue(toOpacity);
        fade.setInterpolator(Interpolator.EASE_BOTH);
        
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        
        fade.play();
        logger.debug("Transition de fondu appliquée");
    }
    
    /**
     * Transition de glissement horizontal
     */
    public static void slideInFromRight(Node node, Runnable onFinished) {
        node.setTranslateX(node.getBoundsInLocal().getWidth());
        
        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, node);
        slide.setFromX(node.getBoundsInLocal().getWidth());
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(SLIDE_DURATION, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Transition de glissement depuis la droite appliquée");
    }
    
    /**
     * Transition de glissement vertical
     */
    public static void slideInFromBottom(Node node, Runnable onFinished) {
        node.setTranslateY(node.getBoundsInLocal().getHeight());
        
        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, node);
        slide.setFromY(node.getBoundsInLocal().getHeight());
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fade = new FadeTransition(SLIDE_DURATION, node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        ParallelTransition parallel = new ParallelTransition(slide, fade);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Transition de glissement depuis le bas appliquée");
    }
    
    /**
     * Transition de zoom (scale)
     */
    public static void scaleTransition(Node node, double fromScale, double toScale, Runnable onFinished) {
        ScaleTransition scale = new ScaleTransition(SCALE_DURATION, node);
        scale.setFromX(fromScale);
        scale.setFromY(fromScale);
        scale.setToX(toScale);
        scale.setToY(toScale);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        
        FadeTransition fade = new FadeTransition(SCALE_DURATION, node);
        fade.setFromValue(fromScale == 0 ? 0.0 : 1.0);
        fade.setToValue(toScale == 0 ? 0.0 : 1.0);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Transition de zoom appliquée");
    }
    
    /**
     * Transition avec effet de flou
     */
    public static void blurTransition(Node node, double fromBlur, double toBlur, Runnable onFinished) {
        GaussianBlur blur = new GaussianBlur(fromBlur);
        node.setEffect(blur);
        
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(blur.radiusProperty(), toBlur, Interpolator.EASE_BOTH);
        KeyFrame keyFrame = new KeyFrame(BLUR_DURATION, keyValue);
        timeline.getKeyFrames().add(keyFrame);
        
        if (onFinished != null) {
            timeline.setOnFinished(e -> onFinished.run());
        }
        
        timeline.play();
        logger.debug("Transition de flou appliquée");
    }
    
    /**
     * Transition de changement de scène avec effet de fondu croisé
     */
    public static void crossFadeScenes(Stage stage, Scene fromScene, Scene toScene, Runnable onFinished) {
        // Sécurité: si pas d'ancienne scène, simple fade-in sur la nouvelle
        if (fromScene == null) {
            stage.setScene(toScene);
            Node newRoot = toScene.getRoot();
            newRoot.setOpacity(0.0);
            fadeTransition(newRoot, 0.0, 1.0, onFinished);
            logger.debug("Transition simple (sans scène source) appliquée");
            return;
        }

        // Appliquer directement la nouvelle scène au Stage pour garantir que tous les popups ont un owner valide
        Node newRoot = toScene.getRoot();
        newRoot.setOpacity(0.0);
        // Désactiver temporairement les interactions pendant l'animation
        newRoot.setMouseTransparent(true);
        stage.setScene(toScene);

        // Lancer uniquement le fade-in de la nouvelle scène
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);
        fadeIn.setOnFinished(e -> {
            newRoot.setMouseTransparent(false);
            if (onFinished != null) {
                onFinished.run();
            }
        });
        fadeIn.play();
        logger.debug("Transition de fade-in vers la nouvelle scène appliquée (sans scène temporaire)");
    }
    
    /**
     * Animation de rotation 3D
     */
    public static void rotate3DTransition(Node node, Runnable onFinished) {
        RotateTransition rotate = new RotateTransition(Duration.millis(600), node);
        rotate.setAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 0));
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.EASE_BOTH);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        
        ParallelTransition parallel = new ParallelTransition(rotate, scale);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Transition de rotation 3D appliquée");
    }
    
    /**
     * Animation de pulsation (pour attirer l'attention)
     */
    public static void pulseAnimation(Node node, int cycles) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), node);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setAutoReverse(true);
        scale.setCycleCount(cycles * 2);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        
        // Ajouter un effet d'ombre pendant la pulsation
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 255, 255, 0.6));
        shadow.setRadius(20);
        node.setEffect(shadow);
        
        Timeline shadowTimeline = new Timeline();
        KeyValue shadowValue1 = new KeyValue(shadow.radiusProperty(), 30, Interpolator.EASE_BOTH);
        KeyFrame shadowFrame1 = new KeyFrame(Duration.millis(250), shadowValue1);
        KeyValue shadowValue2 = new KeyValue(shadow.radiusProperty(), 20, Interpolator.EASE_BOTH);
        KeyFrame shadowFrame2 = new KeyFrame(Duration.millis(500), shadowValue2);
        shadowTimeline.getKeyFrames().addAll(shadowFrame1, shadowFrame2);
        shadowTimeline.setCycleCount(cycles);
        shadowTimeline.setAutoReverse(true);
        
        ParallelTransition parallel = new ParallelTransition(scale, shadowTimeline);
        parallel.setOnFinished(e -> node.setEffect(null));
        parallel.play();
        
        logger.debug("Animation de pulsation appliquée");
    }
    
    /**
     * Animation de shake (pour indiquer une erreur)
     */
    public static void shakeAnimation(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setInterpolator(Interpolator.LINEAR);
        
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
        
        logger.debug("Animation de shake appliquée");
    }
    
    /**
     * Animation de glissement avec rebond
     */
    public static void bounceIn(Node node, Runnable onFinished) {
        node.setScaleX(0.3);
        node.setScaleY(0.3);
        node.setOpacity(0);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(500), node);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(new Interpolator() {
            @Override
            protected double curve(double t) {
                return -4 * t * t + 4 * t;
            }
        });
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), node);
        fade.setToValue(1.0);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Animation de rebond appliquée");
    }
    
    /**
     * Animation de sortie avec réduction
     */
    public static void shrinkOut(Node node, Runnable onFinished) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), node);
        scale.setToX(0.0);
        scale.setToY(0.0);
        scale.setInterpolator(Interpolator.EASE_IN);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setToValue(0.0);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(300), node);
        rotate.setByAngle(90);
        
        ParallelTransition parallel = new ParallelTransition(scale, fade, rotate);
        
        if (onFinished != null) {
            parallel.setOnFinished(e -> onFinished.run());
        }
        
        parallel.play();
        logger.debug("Animation de sortie avec réduction appliquée");
    }
}
