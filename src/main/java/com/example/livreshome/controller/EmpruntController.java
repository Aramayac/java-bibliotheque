package com.example.livreshome.controller;

import com.example.livreshome.model.Emprunt;
import com.example.livreshome.service.EmpruntService;
import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;  // ✅ À AJOUTER

/**
 * Contrôleur pour la gestion des emprunts
 * Affiche les emprunts en cours, en retard et retournés
 *
 * @author MBAYE
 * @version 1.0
 */
public class EmpruntController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(EmpruntController.class.getName());

    @FXML
    private TableView<Emprunt> tvEmpruntsActuels;

    @FXML
    private TableView<Emprunt> tvEmpruntsEnRetard;

    @FXML
    private TableView<Emprunt> tvEmpruntsRetournes;

    @FXML
    private JFXButton btnNewLoan;

    @FXML
    private JFXButton btnRefresh;

    @FXML
    private Label lblActiveLoans;

    @FXML
    private Label lblOverdueLoans;

    @FXML
    private Label lblTotalPenalties;

    private EmpruntService empruntService;
    private ObservableList<Emprunt> empruntsActuelsData;
    private ObservableList<Emprunt> empruntsEnRetardData;
    private ObservableList<Emprunt> empruntsRetournesData;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        empruntService = new EmpruntService();

        // Initialiser les listes
        empruntsActuelsData = FXCollections.observableArrayList();
        empruntsEnRetardData = FXCollections.observableArrayList();
        empruntsRetournesData = FXCollections.observableArrayList();

        // Charger les données
        loadEmprunts();

        LOGGER.info("✅ EmpruntController initialisé");
    }

    /**
     * Charge tous les emprunts dans les tables
     */
    private void loadEmprunts() {
        try {
            // Emprunts actuels
            List<Emprunt> empruntsActuels = empruntService.getAllEmpruntsActuels();
            empruntsActuelsData.clear();
            empruntsActuelsData.addAll(empruntsActuels);
            tvEmpruntsActuels.setItems(empruntsActuelsData);

            // Emprunts en retard
            List<Emprunt> empruntsEnRetard = empruntService.getAllEmpruntsEnRetard();
            empruntsEnRetardData.clear();
            empruntsEnRetardData.addAll(empruntsEnRetard);
            tvEmpruntsEnRetard.setItems(empruntsEnRetardData);

            // Emprunts retournés
            List<Emprunt> empruntsRetournes = empruntService.getAllEmprunts().stream()
                    .filter(e -> e.getDateRetourEffective() != null)
                    .collect(Collectors.toList());  // ✅ CHANGÉ de .toList()
            empruntsRetournesData.clear();
            empruntsRetournesData.addAll(empruntsRetournes);
            tvEmpruntsRetournes.setItems(empruntsRetournesData);

            // Mettre à jour les stats
            updateStats();

            LOGGER.info("✅ Emprunts chargés");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement emprunts : " + e.getMessage());
            showError("Erreur", "Impossible de charger les emprunts");
        }
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStats() {
        try {
            long activeCount = empruntService.countEmpruntsActuels();
            lblActiveLoans.setText(String.valueOf(activeCount));

            long overdueCount = empruntService.countEmpruntsEnRetard();
            lblOverdueLoans.setText(String.valueOf(overdueCount));

            // Calculer les pénalités totales
            double totalPenalties = empruntsEnRetardData.stream()
                    .mapToDouble(Emprunt::getPenalite)
                    .sum();
            lblTotalPenalties.setText(String.format("%.2f€", totalPenalties));

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur stats : " + e.getMessage());
        }
    }

    /**
     * Gère la création d'un nouvel emprunt
     */
    @FXML
    private void onNewLoan() {
        LOGGER.info("➕ Nouvel emprunt");
        showInfo("Nouvel emprunt", "Formulaire d'emprunt (à implémenter)");
    }

    /**
     * Rafraîchit les listes d'emprunts
     */
    @FXML
    private void onRefresh() {
        LOGGER.info("🔄 Rafraîchissement");
        loadEmprunts();
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