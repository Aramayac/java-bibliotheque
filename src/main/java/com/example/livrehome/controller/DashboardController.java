package com.example.livrehome.controller;

import com.example.livrehome.dao.*;
import com.example.livrehome.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le tableau de bord
 * @author ARAMA
 */
public class DashboardController implements Initializable {
    
    @FXML private Label lblBienvenue;
    @FXML private Label lblTotalLivres;
    @FXML private Label lblTotalAdherents;
    @FXML private Label lblEmpruntsEnCours;
    @FXML private Label lblEmpruntsRetard;
    @FXML private ListView<String> listRetards;
    @FXML private BorderPane mainPane;
    @FXML private Button btnLivres;
    @FXML private Button btnAdherents;
    @FXML private Button btnEmprunts;
    @FXML private Button btnUtilisateurs;
    @FXML private Button btnDeconnexion;
    
    private Utilisateur utilisateur;
    private final LivreDAO livreDAO = new LivreDAO();
    private final AdherentDAO adherentDAO = new AdherentDAO();
    private final EmpruntDAO empruntDAO = new EmpruntDAO();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerStatistiques();
        configurerMenuSelonProfil();
    }
    
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        lblBienvenue.setText("Bienvenue, " + utilisateur.getNomComplet() + " (" + utilisateur.getProfil() + ")");
        configurerMenuSelonProfil();
    }
    
    private void chargerStatistiques() {
        try {
            // Total livres
            long totalLivres = livreDAO.count();
            lblTotalLivres.setText(String.valueOf(totalLivres));
            
            // Total adhérents
            long totalAdherents = adherentDAO.count();
            lblTotalAdherents.setText(String.valueOf(totalAdherents));
            
            // Emprunts en cours
            long empruntsEnCours = empruntDAO.countEnCours();
            lblEmpruntsEnCours.setText(String.valueOf(empruntsEnCours));
            
            // Emprunts en retard
            List<Emprunt> empruntsRetard = empruntDAO.findEnRetard();
            lblEmpruntsRetard.setText(String.valueOf(empruntsRetard.size()));
            
            // Afficher les retards dans la liste
            listRetards.getItems().clear();
            for (Emprunt emprunt : empruntsRetard) {
                String item = emprunt.getAdherent().getNomComplet() + " - " +
                             emprunt.getLivre().getTitre() + " (" +
                             emprunt.getJoursRetard() + " jours)";
                listRetards.getItems().add(item);
            }
            
        } catch (Exception e) {
            afficherErreur("Erreur de chargement des statistiques", e.getMessage());
        }
    }
    
    private void configurerMenuSelonProfil() {
        if (utilisateur != null && utilisateur.isBibliothecaire()) {
            // Les bibliothécaires n'ont pas accès à la gestion des utilisateurs
            btnUtilisateurs.setVisible(false);
            btnUtilisateurs.setManaged(false);
        }
    }
    
    @FXML
    private void handleLivres() {
        chargerVue("/fxml/livres.fxml", "Gestion des Livres");
    }
    
    @FXML
    private void handleAdherents() {
        chargerVue("/fxml/adherents.fxml", "Gestion des Adhérents");
    }
    
    @FXML
    private void handleEmprunts() {
        chargerVue("/fxml/emprunts.fxml", "Gestion des Emprunts");
    }
    
    @FXML
    private void handleUtilisateurs() {
        if (utilisateur.isAdmin()) {
            chargerVue("/fxml/utilisateurs.fxml", "Gestion des Utilisateurs");
        } else {
            afficherErreur("Accès refusé", "Seuls les administrateurs peuvent gérer les utilisateurs");
        }
    }
    
    @FXML
    private void handleDeconnexion() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Confirmer la déconnexion");
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    LoginController.deconnecter();
                    
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Parent root = loader.load();
                    
                    Stage stage = (Stage) btnDeconnexion.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("Bibliothèque - Connexion");
                    stage.setMaximized(false);
                    stage.setWidth(600);
                    stage.setHeight(400);
                    stage.centerOnScreen();
                    stage.show();
                    
                } catch (IOException e) {
                    afficherErreur("Erreur de déconnexion", e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleRetourDashboard() {
        mainPane.setCenter(null);
        chargerStatistiques();
    }
    
    @FXML
    private void handleActualiser() {
        chargerStatistiques();
        afficherSucces("Statistiques actualisées");
    }
    
    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent vue = loader.load();
            
            // Créer une nouvelle fenêtre ou charger dans le centre
            Stage stage = new Stage();
            stage.setScene(new Scene(vue));
            stage.setTitle("Bibliothèque - " + titre);
            stage.setMaximized(true);
            stage.show();
            
        } catch (IOException e) {
            afficherErreur("Erreur de chargement", "Impossible de charger " + titre);
            e.printStackTrace();
        }
    }
    
    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
