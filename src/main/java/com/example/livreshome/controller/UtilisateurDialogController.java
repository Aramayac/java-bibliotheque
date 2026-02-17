package com.example.livreshome.controller;

import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.service.UtilisateurService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contrôleur pour le dialogue d'ajout/modification d'utilisateur
 *
 * @author ARAMA
 * @version 1.0
 */
public class UtilisateurDialogController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(UtilisateurDialogController.class.getName());

    @FXML
    private JFXTextField tfLogin;

    @FXML
    private JFXPasswordField pfMotDePasse;

    @FXML
    private JFXTextField tfNom;

    @FXML
    private JFXTextField tfPrenom;

    @FXML
    private JFXTextField tfEmail;

    @FXML
    private JFXComboBox<Utilisateur.Profil> cbProfil;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXButton btnCancel;

    private UtilisateurService utilisateurService;
    private Utilisateur utilisateur;
    private boolean isNew = true;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurService = new UtilisateurService();

        // Initialiser le combo des profils
        ObservableList<Utilisateur.Profil> profils = FXCollections.observableArrayList(
                Utilisateur.Profil.ADMIN,
                Utilisateur.Profil.BIBLIOTHECAIRE
        );
        cbProfil.setItems(profils);
        cbProfil.setValue(Utilisateur.Profil.BIBLIOTHECAIRE);

        LOGGER.info("✅ UtilisateurDialogController initialisé");
    }

    /**
     * Configure le dialogue pour créer un nouvel utilisateur
     */
    public void setNewMode() {
        isNew = true;
        tfLogin.setDisable(false);
        pfMotDePasse.setDisable(false);
    }

    /**
     * Configure le dialogue pour modifier un utilisateur existant
     */
    public void setEditMode(Utilisateur utilisateur) {
        isNew = false;
        this.utilisateur = utilisateur;
        tfLogin.setText(utilisateur.getLogin());
        tfNom.setText(utilisateur.getNom());
        tfPrenom.setText(utilisateur.getPrenom());
        tfEmail.setText(utilisateur.getEmail());
        cbProfil.setValue(utilisateur.getProfil());

        // Désactiver le login et mot de passe en mode édition
        tfLogin.setDisable(true);
        pfMotDePasse.setDisable(true);
        pfMotDePasse.setPromptText("Utiliser 'Réinitialiser mot de passe' pour changer");
    }

    /**
     * Gère la sauvegarde
     */
    @FXML
    private void onSave() {
        String login = tfLogin.getText().trim();
        String motDePasse = pfMotDePasse.getText();
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email = tfEmail.getText().trim();
        Utilisateur.Profil profil = cbProfil.getValue();

        if (nom.isEmpty() || prenom.isEmpty()) {
            showError("Erreur", "Nom et prénom sont obligatoires");
            return;
        }

        UtilisateurService.UtilisateurServiceResult result;

        if (isNew) {
            if (login.isEmpty() || motDePasse.isEmpty()) {
                showError("Erreur", "Login et mot de passe sont obligatoires");
                return;
            }
            result = utilisateurService.createUtilisateur(login, motDePasse, nom, prenom, email, profil);
        } else {
            result = utilisateurService.updateUtilisateur(utilisateur.getId(), nom, prenom, email, profil);
        }

        if (result.isSucces()) {
            showInfo("Succès", result.getMessage());
            closeDialog();
        } else {
            showError("Erreur", result.getMessage());
        }
    }

    /**
     * Ferme le dialogue
     */
    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}