package com.example.livreshome.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.logging.Logger;

/**
 * Gestionnaire centralisé pour la navigation entre les pages
 * Utilise toujours le MÊME Stage principal
 */
public class StageManager {

    private static final Logger LOGGER = Logger.getLogger(StageManager.class.getName());
    private static Stage primaryStage;
    private static String cssPath = "/css/style.css";

    /**
     * Initialiser le StageManager avec le Stage principal
     */
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Naviguer vers une page FXML
     */
    public static void navigateTo(String fxmlPath) {
        try {
            if (primaryStage == null) {
                LOGGER.severe("❌ Primary Stage non initialisé");
                return;
            }

            FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(StageManager.class.getResource(cssPath).toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.show();

            LOGGER.info("✅ Navigation vers : " + fxmlPath);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Naviguer vers une page avec dimensions spécifiques
     */
    public static void navigateTo(String fxmlPath, double width, double height) {
        try {
            if (primaryStage == null) {
                LOGGER.severe("❌ Primary Stage non initialisé");
                return;
            }

            FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, width, height);
            scene.getStylesheets().add(StageManager.class.getResource(cssPath).toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            primaryStage.show();

            LOGGER.info("✅ Navigation vers : " + fxmlPath + " (" + width + "x" + height + ")");

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtenir le Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}