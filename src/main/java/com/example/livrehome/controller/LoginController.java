package com.example.livrehome.controller;

import com.example.livrehome.dao.UtilisateurDAO;
import com.example.livrehome.model.Utilisateur;
import com.example.livrehome.Util.SecurityUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour l'écran de connexion
 * @author ARAMA
 */
public class LoginController {
    
    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private CheckBox chkRememberMe;
    
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private static Utilisateur utilisateurConnecte;
    
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        
        // Entrée pour se connecter
        txtPassword.setOnAction(e -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String login = txtLogin.getText().trim();
        String password = txtPassword.getText();
        
        // Validation
        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        
        try {
            // Rechercher l'utilisateur via Hibernate
            Utilisateur utilisateur = utilisateurDAO.findByLogin(login);
            
            if (utilisateur == null) {
                showError("Login ou mot de passe incorrect");
                return;
            }
            
            // Vérifier si le compte est actif
            if (!utilisateur.getActif()) {
                showError("Votre compte a été désactivé. Contactez l'administrateur.");
                return;
            }
            
            // Vérifier le mot de passe avec BCrypt
            if (!SecurityUtil.verifyPassword(password, utilisateur.getMotDePasse())) {
                showError("Login ou mot de passe incorrect");
                return;
            }
            
            // Mettre à jour la dernière connexion
            utilisateur.mettreAJourDerniereConnexion();
            utilisateurDAO.update(utilisateur);
            
            // Stocker l'utilisateur connecté
            utilisateurConnecte = utilisateur;
            
            // Rediriger vers le dashboard
            naviguerVersDashboard();
            
        } catch (Exception e) {
            showError("Erreur de connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void naviguerVersDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            
            // Passer l'utilisateur au DashboardController
            DashboardController controller = loader.getController();
            controller.setUtilisateur(utilisateurConnecte);
            
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Bibliothèque - Tableau de Bord");
            stage.setMaximized(true);
            stage.show();
            
        } catch (IOException e) {
            showError("Erreur de chargement du dashboard");
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        
        // Cacher après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> lblError.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText("Réinitialisation du mot de passe");
        alert.setContentText("Veuillez contacter l'administrateur pour réinitialiser votre mot de passe.");
        alert.showAndWait();
    }
    
    // Getters pour l'utilisateur connecté
    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
    
    public static void setUtilisateurConnecte(Utilisateur utilisateur) {
        utilisateurConnecte = utilisateur;
    }
    
    public static void deconnecter() {
        utilisateurConnecte = null;
    }
}
