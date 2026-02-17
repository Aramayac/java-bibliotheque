package com.example.livreshome;

import com.example.livreshome.util.DataInitializer;
import com.example.livreshome.util.HibernateUtil;
import com.example.livreshome.util.StageManager;  // ✅ IMPORTER
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Test de la connexion à la BD
            if (!HibernateUtil.testConnection()) {
                LOGGER.severe("❌ Impossible de se connecter à la base de données");
                showError("Erreur de connexion", "Impossible de se connecter à la base de données");
                return;
            }

            // Initialiser les données par défaut
            DataInitializer.initializeAll();

            // ✅ INITIALISER LE STAGE MANAGER AVEC LE STAGE PRINCIPAL
            StageManager.setPrimaryStage(primaryStage);

            // Charger le FXML du login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            // Créer la scène
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/login-style.css").toExternalForm());

            // Configurer le stage
            primaryStage.setTitle("Biblio_Z_221 - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);  // ✅ PERMETTRE LE REDIMENSIONNEMENT
            primaryStage.show();

            LOGGER.info("✅ Application démarrée");

        } catch (IOException e) {
            LOGGER.severe("❌ Erreur au démarrage : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur lors du démarrage de l'application");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur générale : " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur lors du démarrage de l'application");
        }
    }

    @Override
    public void stop() {
        HibernateUtil.close();
        LOGGER.info("✅ Application fermée");
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}