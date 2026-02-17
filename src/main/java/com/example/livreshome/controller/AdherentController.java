package com.example.livreshome.controller;

import com.example.livreshome.model.Adherent;
import com.example.livreshome.service.AdherentService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des adhérents
 * Affiche la liste des adhérents et permet les opérations CRUD
 *
 * @author ARAMA
 * @version 2.0
 */
public class AdherentController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(AdherentController.class.getName());

    @FXML
    private TableView<Adherent> tvAdherents;
    @FXML
    private Label lblResultCountHeader;


    @FXML
    private JFXTextField tfSearch;

    @FXML
    private JFXComboBox<String> cbFilter;

    @FXML
    private JFXButton btnAddAdherent;

    @FXML
    private JFXButton btnRefresh;

    @FXML
    private Label lblTotalAdherents;

    @FXML
    private Label lblActiveAdherents;

    @FXML
    private Label lblResultCount;

    private AdherentService adherentService;
    private ObservableList<Adherent> adherentsData;


    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        adherentService = new AdherentService();
        adherentsData = FXCollections.observableArrayList();
        tvAdherents.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Initialiser le filtre
        initializeFilter();

        // Charger les données
        loadAdherents();

        // Initialiser les listeners
        initializeListeners();

        LOGGER.info("✅ AdherentController initialisé");
    }

    /**
     * Initialise les options du filtre
     */
    private void initializeFilter() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Tous",
                "Actifs",
                "Inactifs"
        );
        cbFilter.setItems(filterOptions);
        cbFilter.setValue("Tous");
    }

    /**
     * Charge tous les adhérents dans la table
     */
    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentService.getAllAdherents();
            adherentsData.clear();
            adherentsData.addAll(adherents);
            tvAdherents.setItems(adherentsData);

            // Mettre à jour les stats
            updateStats(adherents);

            // Mettre à jour le compteur de résultats
            updateResultCount(adherents.size());

            LOGGER.info("✅ " + adherents.size() + " adhérents chargés");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement adhérents : " + e.getMessage());
            showError("Erreur", "Impossible de charger les adhérents");
        }
    }

    /**
     * Met à jour les statistiques
     */
    private void updateStats(List<Adherent> adherents) {
        lblTotalAdherents.setText(String.valueOf(adherents.size()));

        long activeCount = adherents.stream()
                .filter(Adherent::getActif)
                .count();
        lblActiveAdherents.setText(String.valueOf(activeCount));
    }

    /**
     * Met à jour le compteur de résultats
     */
    private void updateResultCount(int count) {
        if (lblResultCount != null) {
            lblResultCount.setText(count + " résultat" + (count > 1 ? "s" : ""));
        }
    }

    /**
     * Initialise les listeners (recherche, filtre, etc.)
     */
    private void initializeListeners() {
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            onSearch(newValue);
        });

        cbFilter.setOnAction(event -> {
            applyFilter();
        });
    }

    /**
     * Recherche des adhérents selon le mot-clé
     */
    private void onSearch(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            loadAdherents();
            return;
        }

        try {
            List<Adherent> results = adherentService.searchAdherents(keyword);
            adherentsData.clear();
            adherentsData.addAll(results);
            updateResultCount(results.size());
            updateStats(results);

            LOGGER.info("🔍 Recherche : " + results.size() + " résultat(s)");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche : " + e.getMessage());
        }
    }

    /**
     * Applique le filtre sélectionné
     */
    private void applyFilter() {
        String filter = cbFilter.getValue();

        try {
            List<Adherent> adherents = adherentService.getAllAdherents();

            if ("Actifs".equals(filter)) {
                adherents = adherents.stream()
                        .filter(Adherent::getActif)
                        .collect(Collectors.toList());
            } else if ("Inactifs".equals(filter)) {
                adherents = adherents.stream()
                        .filter(a -> !a.getActif())
                        .collect(Collectors.toList());
            }

            adherentsData.clear();
            adherentsData.addAll(adherents);
            updateStats(adherents);
            updateResultCount(adherents.size());

            LOGGER.info("🔍 Filtre appliqué : " + filter);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur filtre : " + e.getMessage());
        }
    }

    /**
     * Gère l'ajout d'un nouvel adhérent
     * ✅ FONCTION COMPLÈTE AVEC FORMULAIRE MODAL
     */
    @FXML
    private void onAddAdherent() {
        LOGGER.info("➕ Ouverture du formulaire d'ajout d'adhérent");

        try {
            // Créer un stage pour le formulaire d'ajout
            Stage dialogStage = new Stage();
            dialogStage.setTitle("➕ Ajouter un Nouvel Adhérent");
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setWidth(550);
            dialogStage.setHeight(650);

            // Créer le formulaire
            VBox formBox = createAdherentForm(dialogStage);

            Scene scene = new Scene(formBox);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            // Rafraîchir la liste après l'ajout
            loadAdherents();

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de l'ajout : " + e.getMessage());
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    /**
     * Crée le formulaire d'ajout d'adhérent
     * ✅ FORMULAIRE PROFESSIONNEL AVEC VALIDATION
     */
    private VBox createAdherentForm(Stage parentStage) {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(25));
        formBox.setStyle("-fx-background-color: #FFFFFF;");

        // Titre du formulaire
        Label titleLabel = new Label("📝 Nouvel Adhérent");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label subtitleLabel = new Label("Remplissez les informations de l'adhérent");
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748B;");

        // Champs du formulaire
        JFXTextField tfMatricule = new JFXTextField();
        tfMatricule.setPromptText("Matricule (ex: ADH001)");
        tfMatricule.setPrefHeight(45);
        tfMatricule.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        JFXTextField tfNom = new JFXTextField();
        tfNom.setPromptText("Nom *");
        tfNom.setPrefHeight(45);
        tfNom.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        JFXTextField tfPrenom = new JFXTextField();
        tfPrenom.setPromptText("Prénom *");
        tfPrenom.setPrefHeight(45);
        tfPrenom.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        JFXTextField tfEmail = new JFXTextField();
        tfEmail.setPromptText("Email *");
        tfEmail.setPrefHeight(45);
        tfEmail.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        JFXTextField tfTelephone = new JFXTextField();
        tfTelephone.setPromptText("Téléphone");
        tfTelephone.setPrefHeight(45);
        tfTelephone.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        JFXTextField tfAdresse = new JFXTextField();
        tfAdresse.setPromptText("Adresse");
        tfAdresse.setPrefHeight(45);
        tfAdresse.setStyle("-fx-font-size: 12; -fx-padding: 10 16;");

        // Champs obligatoires info
        Label requiredLabel = new Label("* Champs obligatoires");
        requiredLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #EF4444; -fx-font-weight: bold;");

        // Boutons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        JFXButton btnSave = new JFXButton("✅ Enregistrer");
        btnSave.setStyle(
                "-fx-background-color: linear-gradient(to right, #10B981, #059669); " +
                        "-fx-text-fill: #FFFFFF; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12; " +
                        "-fx-padding: 10 30; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        btnSave.setPrefHeight(42);
        btnSave.setMinWidth(130);

        JFXButton btnCancel = new JFXButton("❌ Annuler");
        btnCancel.setStyle(
                "-fx-background-color: #F1F5F9; " +
                        "-fx-text-fill: #0F172A; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12; " +
                        "-fx-padding: 10 30; " +
                        "-fx-border-color: #E2E8F0; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 6; " +
                        "-fx-background-radius: 6; " +
                        "-fx-cursor: hand;"
        );
        btnCancel.setPrefHeight(42);
        btnCancel.setMinWidth(130);
        btnCancel.setOnAction(e -> parentStage.close());

        // Action du bouton Enregistrer
        btnSave.setOnAction(e -> {
            // Validation
            if (tfNom.getText().trim().isEmpty()) {
                showError("Erreur", "Le nom est obligatoire");
                return;
            }
            if (tfPrenom.getText().trim().isEmpty()) {
                showError("Erreur", "Le prénom est obligatoire");
                return;
            }
            if (tfEmail.getText().trim().isEmpty()) {
                showError("Erreur", "L'email est obligatoire");
                return;
            }

            // Validation email basique
            if (!tfEmail.getText().contains("@")) {
                showError("Erreur", "L'email n'est pas valide");
                return;
            }

            // Créer l'adhérent
            Adherent adherent = new Adherent();
            adherent.setMatricule(tfMatricule.getText().isEmpty() ? "ADH" + System.currentTimeMillis() : tfMatricule.getText());
            adherent.setNom(tfNom.getText().trim());
            adherent.setPrenom(tfPrenom.getText().trim());
            adherent.setEmail(tfEmail.getText().trim());
            adherent.setTelephone(tfTelephone.getText().trim());
            adherent.setAdresse(tfAdresse.getText().trim());
            adherent.setDateInscription(LocalDate.now());
            adherent.setActif(true);

            // Sauvegarder
            try {
                adherentService.createAdherent(adherent);
                LOGGER.info("✅ Adhérent créé : " + adherent.getNom() + " " + adherent.getPrenom());
                showInfo("Succès", "Adhérent '" + adherent.getNom() + " " + adherent.getPrenom() + "' ajouté avec succès");
                parentStage.close();
            } catch (Exception ex) {
                LOGGER.severe("❌ Erreur création adhérent : " + ex.getMessage());
                showError("Erreur", "Impossible de créer l'adhérent : " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);

        // Ajouter tous les éléments
        formBox.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                tfMatricule,
                tfNom,
                tfPrenom,
                tfEmail,
                tfTelephone,
                tfAdresse,
                requiredLabel,
                new Region(),
                buttonBox
        );

        return formBox;
    }

    /**
     * Rafraîchit la liste des adhérents
     */
    @FXML
    private void onRefresh() {
        LOGGER.info("🔄 Rafraîchissement de la liste");
        loadAdherents();
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