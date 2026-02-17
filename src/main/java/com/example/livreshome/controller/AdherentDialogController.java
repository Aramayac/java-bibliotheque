package com.example.livreshome.controller;

import com.example.livreshome.model.Adherent;
import com.example.livreshome.service.AdherentService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contrôleur pour le dialogue d'ajout/modification d'adhérent
 *
 * @author SECK
 * @version 1.0
 */
public class AdherentDialogController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdherentDialogController.class.getName());

    @FXML
    private JFXTextField tfMatricule;

    @FXML
    private JFXTextField tfNom;

    @FXML
    private JFXTextField tfPrenom;

    @FXML
    private JFXTextField tfEmail;

    @FXML
    private JFXTextField tfTelephone;

    @FXML
    private JFXTextField tfAdresse;

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXButton btnCancel;

    private AdherentService adherentService;
    private Adherent adherent;
    private boolean isNew = true;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adherentService = new AdherentService();
        LOGGER.info("✅ AdherentDialogController initialisé");
    }

    /**
     * Configure le dialogue pour créer un nouvel adhérent
     */
    public void setNewMode() {
        isNew = true;
        tfMatricule.setText(adherentService.generateMatricule());
        tfMatricule.setDisable(true);
    }

    /**
     * Configure le dialogue pour modifier un adhérent existant
     */
    public void setEditMode(Adherent adherent) {
        isNew = false;
        this.adherent = adherent;
        tfMatricule.setText(adherent.getMatricule());
        tfNom.setText(adherent.getNom());
        tfPrenom.setText(adherent.getPrenom());
        tfEmail.setText(adherent.getEmail());
        tfTelephone.setText(adherent.getTelephone());
        tfAdresse.setText(adherent.getAdresse());
        tfMatricule.setDisable(true);
    }

    /**
     * Gère la sauvegarde
     */
    @FXML
    private void onSave() {
        String matricule = tfMatricule.getText().trim();
        String nom = tfNom.getText().trim();
        String prenom = tfPrenom.getText().trim();
        String email = tfEmail.getText().trim();
        String telephone = tfTelephone.getText().trim();
        String adresse = tfAdresse.getText().trim();

        AdherentService.AdherentServiceResult result;

        if (isNew) {
            result = adherentService.createAdherent(matricule, nom, prenom, email, telephone, adresse);
        } else {
            result = adherentService.updateAdherent(adherent.getId(), nom, prenom, email, telephone, adresse);
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

    /**
     * Ferme la fenêtre
     */
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