package com.example.livreshome.controller;

import com.example.livreshome.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contrôleur pour l'écran de connexion
 * Gère l'authentification de l'utilisateur
 *
 * @author ARAMA
 * @version 1.0
 */
public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML
    private JFXTextField tfLogin;

    @FXML
    private JFXPasswordField pfMotDePasse;

    @FXML
    private CheckBox cbRememberMe;

    @FXML
    private JFXButton btnLogin;

    @FXML
    private Label lblErrorMessage;

    private AuthenticationService authService;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthenticationService();

        // Ajouter les listeners pour l'Enter key
        tfLogin.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });

        pfMotDePasse.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });

        // Effacer le message d'erreur quand l'utilisateur tape
        tfLogin.textProperty().addListener((obs, old, newVal) -> clearErrorMessage());
        pfMotDePasse.textProperty().addListener((obs, old, newVal) -> clearErrorMessage());

        LOGGER.info("✅ LoginController initialisé");
    }

    /**
     * Gère l'événement de connexion
     */
    @FXML
    private void handleLogin() {
        String login = tfLogin.getText().trim();
        String motDePasse = pfMotDePasse.getText();

        // Validation basique
        if (login.isEmpty() || motDePasse.isEmpty()) {
            showError("Veuillez entrer votre login et mot de passe");
            return;
        }

        // Désactiver le bouton pour éviter les clics multiples
        btnLogin.setDisable(true);
        btnLogin.setText("Connexion en cours...");

        // Effectuer l'authentification (normalement on ferait ça dans un thread séparé)
        try {
            AuthenticationService.AuthenticationResult result = authService.authenticate(login, motDePasse);

            if (result.isSucces()) {
                LOGGER.info("✅ Connexion réussie : " + login);
                openDashboard();
            } else {
                showError(result.getMessage());
                btnLogin.setDisable(false);
                btnLogin.setText("Connexion");
            }
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la connexion : " + e.getMessage());
            showError("Erreur serveur. Veuillez réessayer.");
            btnLogin.setDisable(false);
            btnLogin.setText("Connexion");
        }
    }

    /**
     * Ouvre l'écran du dashboard après une connexion réussie
     */
    private void openDashboard() {
        try {
            // Charger le FXML du dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène
            Scene scene = new Scene(root, 1400, 900);

            // Récupérer la scène actuelle et son stage
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            // Remplacer la scène
            stage.setScene(scene);
            stage.setTitle("BiblioMax - Tableau de Bord");
            stage.show();

            LOGGER.info("✅ Dashboard chargé avec succès");

        } catch (IOException e) {
            LOGGER.severe("❌ Erreur lors du chargement du dashboard : " + e.getMessage());
            showError("Erreur lors du chargement de l'interface");
            btnLogin.setDisable(false);
            btnLogin.setText("Connexion");
        }
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        lblErrorMessage.setText(message);
        LOGGER.warning("⚠️ " + message);
    }

    /**
     * Efface le message d'erreur
     */
    private void clearErrorMessage() {
        lblErrorMessage.setText("");
    }
}