package com.example.livreshome.controller;

import com.example.livreshome.model.Livre;
import com.example.livreshome.service.LivreService;
import com.example.livreshome.util.SessionManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contrôleur pour la gestion des livres
 * Affiche la liste des livres et permet les opérations CRUD
 *
 * @author ARAMA
 * @version 1.0
 */
public class LivreController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LivreController.class.getName());

    @FXML
    private TableView<Livre> tvLivres;

    @FXML
    private JFXTextField tfSearch;

    @FXML
    private JFXButton btnAddLivre;

    @FXML
    private JFXButton btnRefresh;

    private LivreService livreService;
    private ObservableList<Livre> livresData;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        livreService = new LivreService();
        livresData = FXCollections.observableArrayList();

        // Charger les données
        loadLivres();

        // Initialiser les listeners
        initializeListeners();

        LOGGER.info("✅ LivreController initialisé");
    }

    /**
     * Charge tous les livres dans la table
     */
    private void loadLivres() {
        try {
            List<Livre> livres = livreService.getAllLivres();
            livresData.clear();
            livresData.addAll(livres);
            tvLivres.setItems(livresData);

            LOGGER.info("✅ " + livres.size() + " livres chargés");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement livres : " + e.getMessage());
            showError("Erreur", "Impossible de charger les livres");
        }
    }

    /**
     * Initialise les listeners (recherche, etc.)
     */
    private void initializeListeners() {
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            onSearch(newValue);
        });
    }

    /**
     * Gère l'ajout d'un nouveau livre
     */
    @FXML
    private void onAddLivre() {
        LOGGER.info("➕ Ajout d'un livre");
        showInfo("Ajout de livre", "Formulaire d'ajout (à implémenter)");
    }

    /**
     * Rafraîchit la liste des livres
     */
    @FXML
    private void onRefresh() {
        LOGGER.info("🔄 Rafraîchissement de la liste");
        loadLivres();
    }

    /**
     * Recherche des livres selon le mot-clé
     */
    private void onSearch(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            loadLivres();
            return;
        }

        try {
            List<Livre> results = livreService.searchLivres(keyword);
            livresData.clear();
            livresData.addAll(results);

            LOGGER.info("🔍 Recherche : " + results.size() + " résultat(s)");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche : " + e.getMessage());
        }
    }

    /**
     * Affiche une alerte d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}