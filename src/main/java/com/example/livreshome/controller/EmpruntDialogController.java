package com.example.livreshome.controller;

import com.example.livreshome.dao.LivreDAO;
import com.example.livreshome.dao.AdherentDAO;
import com.example.livreshome.model.Livre;
import com.example.livreshome.model.Adherent;
import com.example.livreshome.service.EmpruntService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contrôleur pour le dialogue d'emprunt
 * Permet l'enregistrement d'un nouvel emprunt
 *
 * @author MBAYE
 * @version 1.0
 */
public class EmpruntDialogController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(EmpruntDialogController.class.getName());

    @FXML
    private JFXComboBox<Livre> cbLivre;

    @FXML
    private JFXComboBox<Adherent> cbAdherent;

    @FXML
    private Spinner<Integer> spinnerDays;  // ✅ CHANGÉ de JFXSpinner à Spinner

    @FXML
    private JFXButton btnSave;

    @FXML
    private JFXButton btnCancel;

    private LivreDAO livreDAO;
    private AdherentDAO adherentDAO;
    private EmpruntService empruntService;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        livreDAO = new LivreDAO();
        adherentDAO = new AdherentDAO();
        empruntService = new EmpruntService();

        // Charger les livres disponibles
        loadLivres();

        // Charger les adhérents actifs
        loadAdherents();

        // Initialiser le spinner (au lieu de JFXSpinner)
        spinnerDays.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 90, 30)
        );

        LOGGER.info("✅ EmpruntDialogController initialisé");
    }

    /**
     * Charge les livres disponibles
     */
    private void loadLivres() {
        try {
            List<Livre> livres = livreDAO.findDisponibles();
            ObservableList<Livre> livresData = FXCollections.observableArrayList(livres);
            cbLivre.setItems(livresData);
            cbLivre.setConverter(new javafx.util.StringConverter<Livre>() {
                @Override
                public String toString(Livre l) {
                    return l != null ? l.getTitre() + " (" + l.getAuteur() + ")" : "";
                }

                @Override
                public Livre fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement livres : " + e.getMessage());
        }
    }

    /**
     * Charge les adhérents actifs
     */
    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentDAO.findActifs();
            ObservableList<Adherent> adherentsData = FXCollections.observableArrayList(adherents);
            cbAdherent.setItems(adherentsData);
            cbAdherent.setConverter(new javafx.util.StringConverter<Adherent>() {
                @Override
                public String toString(Adherent a) {
                    return a != null ? a.getNomComplet() + " (" + a.getMatricule() + ")" : "";
                }

                @Override
                public Adherent fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement adhérents : " + e.getMessage());
        }
    }

    /**
     * Gère la sauvegarde
     */
    @FXML
    private void onSave() {
        Livre livre = cbLivre.getValue();
        Adherent adherent = cbAdherent.getValue();
        Integer days = spinnerDays.getValue();

        if (livre == null || adherent == null) {
            showError("Erreur", "Veuillez sélectionner un livre et un adhérent");
            return;
        }

        EmpruntService.EmpruntServiceResult result = empruntService.recordEmprunt(
                livre.getId(), adherent.getId(), days);

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