package com.example.livrehome.controller;

import com.example.livrehome.dao.UtilisateurDAO;
import com.example.livrehome.model.Utilisateur;
import com.example.livrehome.Util.SecurityUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class UtilisateurController {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private ObservableList<Utilisateur> masterList;

    // =========================
    // FXML FIELDS
    // =========================

    @FXML private TextField searchField;
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Utilisateur.Profil> profilComboBox;
    @FXML private CheckBox actifCheckBox;

    @FXML private TableView<Utilisateur> tableView;
    @FXML private TableColumn<Utilisateur, Long> colId;
    @FXML private TableColumn<Utilisateur, String> colLogin;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, Utilisateur.Profil> colProfil;
    @FXML private TableColumn<Utilisateur, Boolean> colActif;

    // =========================
    // INITIALIZE
    // =========================

    @FXML
    public void initialize() {

        profilComboBox.getItems().addAll(Utilisateur.Profil.values());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colLogin.setCellValueFactory(new PropertyValueFactory<>("login"));
        colProfil.setCellValueFactory(new PropertyValueFactory<>("profil"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));

        // Badge couleur statut
        colActif.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(actif ? "ACTIF" : "DÉSACTIVÉ");
                    setStyle(actif
                            ? "-fx-text-fill: #28a745; -fx-font-weight: bold;"
                            : "-fx-text-fill: #dc3545; -fx-font-weight: bold;");
                }
            }
        });

        loadData();

        // Recherche dynamique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            tableView.setItems(masterList.filtered(u ->
                    u.getLogin().toLowerCase().contains(newVal.toLowerCase()) ||
                            (u.getNom() != null &&
                                    u.getNom().toLowerCase().contains(newVal.toLowerCase()))
            ));
        });

        // Remplissage automatique du formulaire
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loginField.setText(newSel.getLogin());
                nomField.setText(newSel.getNom());
                prenomField.setText(newSel.getPrenom());
                emailField.setText(newSel.getEmail());
                profilComboBox.setValue(newSel.getProfil());
                actifCheckBox.setSelected(newSel.getActif());
            }
        });
    }

    private void loadData() {
        masterList = FXCollections.observableArrayList(utilisateurDAO.findAll());
        tableView.setItems(masterList);
    }

    // =========================
    // AJOUTER
    // =========================

    @FXML
    private void handleAjouter() {

        if (loginField.getText().isEmpty()
                || passwordField.getText().isEmpty()
                || profilComboBox.getValue() == null) {
            alert("Champs obligatoires manquants !");
            return;
        }

        if (utilisateurDAO.loginExists(loginField.getText())) {
            alert("Ce login existe déjà !");
            return;
        }

        String hashed = SecurityUtil.hashPassword(passwordField.getText());

        Utilisateur u = new Utilisateur(
                loginField.getText(),
                hashed,
                nomField.getText(),
                prenomField.getText(),
                emailField.getText(),
                profilComboBox.getValue()
        );

        u.setActif(actifCheckBox.isSelected());

        utilisateurDAO.save(u);
        loadData();
        clearFields();
    }

    // =========================
    // MODIFIER
    // =========================

    @FXML
    private void handleModifier() {

        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélectionnez un utilisateur !");
            return;
        }

        selected.setNom(nomField.getText());
        selected.setPrenom(prenomField.getText());
        selected.setEmail(emailField.getText());
        selected.setProfil(profilComboBox.getValue());
        selected.setActif(actifCheckBox.isSelected());

        utilisateurDAO.update(selected);

        alert("Utilisateur modifié avec succès !");
        loadData();
    }

    // =========================
    // SUPPRIMER
    // =========================

    @FXML
    private void handleSupprimer() {

        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélectionnez un utilisateur !");
            return;
        }

        if (selected.getProfil() == Utilisateur.Profil.ADMIN) {
            if (!confirm("⚠ Suppression d'un ADMIN. Continuer ?")) return;
        }

        if (!confirm("Confirmer la suppression ?")) return;

        utilisateurDAO.delete(selected.getId());
        loadData();
    }

    // =========================
    // RESET PASSWORD
    // =========================

    @FXML
    private void handleResetPassword() {

        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélectionnez un utilisateur !");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Réinitialisation mot de passe");
        dialog.setHeaderText("Saisir le nouveau mot de passe");
        dialog.setContentText("Nouveau mot de passe :");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(newPassword -> {
            if (newPassword.trim().isEmpty()) {
                alert("Mot de passe invalide !");
                return;
            }

            selected.setMotDePasse(SecurityUtil.hashPassword(newPassword));
            utilisateurDAO.update(selected);

            alert("Mot de passe modifié avec succès !");
            loadData();
        });
    }


    // =========================
    // ACTIVER / DESACTIVER
    // =========================

    @FXML
    private void handleToggleActif() {

        Utilisateur selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert("Sélectionnez un utilisateur !");
            return;
        }

        boolean newStatus = !selected.getActif();

        utilisateurDAO.activerDesactiver(selected.getId(), newStatus);

        alert(newStatus ? "Compte activé !" : "Compte désactivé !");
        loadData();
    }


    // =========================
    // UTILITAIRES
    // =========================

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        loginField.clear();
        passwordField.clear();
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        profilComboBox.setValue(null);
        actifCheckBox.setSelected(true);
    }

    @FXML
    private void handleClear() {
        clearFields();
    }

}
