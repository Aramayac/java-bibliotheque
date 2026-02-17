package com.example.livreshome.controller;

import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.service.UtilisateurService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import com.example.livreshome.util.StageManager;  // ✅ IMPORTER
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UtilisateurController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(UtilisateurController.class.getName());

    @FXML private TableView<Utilisateur> tvUtilisateurs;
    @FXML private JFXTextField tfSearch;
    @FXML private JFXComboBox<String> cbFilter;
    @FXML private JFXButton btnAddUser;
    @FXML private JFXButton btnRefresh;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblAdmins;
    @FXML private Label lblLibrarians;
    @FXML private Label lblResultCount;

    private UtilisateurService utilisateurService;
    private ObservableList<Utilisateur> utilisateursData;

    @FXML private JFXButton btnBack;
    // ... autres @FXML ...

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        utilisateurService = new UtilisateurService();
        utilisateursData = FXCollections.observableArrayList();

        initializeFilter();
        loadUtilisateurs();
        initializeListeners();
        setupActionsColumn();  // ✅ AJOUTER LES ACTIONS AUX BOUTONS
        Tooltip tooltipBack = new Tooltip("Retourner au Dashboard (ESC)");
        Tooltip.install(btnBack, tooltipBack);

        LOGGER.info("✅ UtilisateurController initialisé");
    }

    /**
     * ✅ RETOUR SIMPLE
     */
    @FXML
    private void onBack() {
        StageManager.navigateTo("/fxml/Dashboard.fxml");  // ✅ UTILISER LE STAGE MANAGER
    }


    private void initializeFilter() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Tous", "Actifs", "Inactifs", "Administrateurs", "Bibliothécaires"
        );
        cbFilter.setItems(filterOptions);
        cbFilter.setValue("Tous");
    }

    private void loadUtilisateurs() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            utilisateursData.clear();
            utilisateursData.addAll(utilisateurs);
            tvUtilisateurs.setItems(utilisateursData);
            updateStats(utilisateurs);
            updateResultCount(utilisateurs.size());
            LOGGER.info("✅ " + utilisateurs.size() + " utilisateurs chargés");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement utilisateurs : " + e.getMessage());
            showError("Erreur", "Impossible de charger les utilisateurs");
        }
    }

    private void updateStats(List<Utilisateur> utilisateurs) {
        lblTotalUsers.setText(String.valueOf(utilisateurs.size()));
        long adminCount = utilisateurs.stream()
                .filter(u -> u.getProfil() == Utilisateur.Profil.ADMIN).count();
        lblAdmins.setText(String.valueOf(adminCount));
        long librarianCount = utilisateurs.stream()
                .filter(u -> u.getProfil() == Utilisateur.Profil.BIBLIOTHECAIRE).count();
        lblLibrarians.setText(String.valueOf(librarianCount));
    }

    private void updateResultCount(int count) {
        if (lblResultCount != null) {
            lblResultCount.setText(count + " résultat" + (count > 1 ? "s" : ""));
        }
    }

    private void initializeListeners() {
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> onSearch(newValue));
        cbFilter.setOnAction(event -> applyFilter());
    }

    private void onSearch(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            loadUtilisateurs();
            return;
        }
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            List<Utilisateur> results = utilisateurs.stream()
                    .filter(u -> u.getLogin().toLowerCase().contains(keyword.toLowerCase()) ||
                            u.getNomComplet().toLowerCase().contains(keyword.toLowerCase()) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
            utilisateursData.clear();
            utilisateursData.addAll(results);
            updateResultCount(results.size());
            updateStats(results);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche : " + e.getMessage());
        }
    }

    private void applyFilter() {
        String filter = cbFilter.getValue();
        try {
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            switch (filter) {
                case "Actifs":
                    utilisateurs = utilisateurs.stream()
                            .filter(Utilisateur::getActif).collect(Collectors.toList());
                    break;
                case "Inactifs":
                    utilisateurs = utilisateurs.stream()
                            .filter(u -> !u.getActif()).collect(Collectors.toList());
                    break;
                case "Administrateurs":
                    utilisateurs = utilisateurs.stream()
                            .filter(u -> u.getProfil() == Utilisateur.Profil.ADMIN)
                            .collect(Collectors.toList());
                    break;
                case "Bibliothécaires":
                    utilisateurs = utilisateurs.stream()
                            .filter(u -> u.getProfil() == Utilisateur.Profil.BIBLIOTHECAIRE)
                            .collect(Collectors.toList());
                    break;
            }
            utilisateursData.clear();
            utilisateursData.addAll(utilisateurs);
            updateStats(utilisateurs);
            updateResultCount(utilisateurs.size());
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur filtre : " + e.getMessage());
        }
    }

    // ==================== AJOUTER UN UTILISATEUR ====================
    @FXML
    private void onAddUser() {
        LOGGER.info("➕ Ouverture du formulaire d'ajout d'utilisateur");
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("➕ Ajouter un Nouvel Utilisateur");
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setWidth(550);
            dialogStage.setHeight(700);

            VBox formBox = createUtilisateurForm(dialogStage, null);
            Scene scene = new Scene(formBox);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadUtilisateurs();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de l'ajout : " + e.getMessage());
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    // ==================== MODIFIER UN UTILISATEUR ====================
    private void onEditUser(Utilisateur utilisateur) {
        LOGGER.info("✏️ Ouverture du formulaire de modification pour : " + utilisateur.getLogin());
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("✏️ Modifier l'Utilisateur : " + utilisateur.getLogin());
            dialogStage.setResizable(false);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setWidth(550);
            dialogStage.setHeight(600);

            VBox formBox = createUtilisateurForm(dialogStage, utilisateur);
            Scene scene = new Scene(formBox);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            loadUtilisateurs();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la modification : " + e.getMessage());
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    // ==================== SUPPRIMER UN UTILISATEUR ====================
    private void onDeleteUser(Utilisateur utilisateur) {
        LOGGER.info("🗑️ Tentative de suppression : " + utilisateur.getLogin());

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr ?");
        confirmDialog.setContentText("Supprimer l'utilisateur '" + utilisateur.getNomComplet() + "' ?\n\n" +
                "Cette action est irréversible !");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UtilisateurService.UtilisateurServiceResult serviceResult =
                        utilisateurService.deleteUtilisateur(utilisateur.getId());

                if (serviceResult.isSucces()) {
                    LOGGER.info("✅ Utilisateur supprimé : " + utilisateur.getLogin());
                    showInfo("Succès", "Utilisateur supprimé avec succès");
                    loadUtilisateurs();
                } else {
                    LOGGER.warning("⚠️ Erreur suppression : " + serviceResult.getMessage());
                    showError("Erreur", serviceResult.getMessage());
                }
            } catch (Exception e) {
                LOGGER.severe("❌ Exception suppression : " + e.getMessage());
                showError("Erreur", "Erreur lors de la suppression : " + e.getMessage());
            }
        }
    }

    // ==================== RÉINITIALISER LE MOT DE PASSE ====================
    private void onResetPassword(Utilisateur utilisateur) {
        LOGGER.info("🔑 Réinitialisation du mot de passe pour : " + utilisateur.getLogin());

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Réinitialiser le mot de passe");
        confirmDialog.setHeaderText("Confirmation");
        confirmDialog.setContentText("Réinitialiser le mot de passe de '" +
                utilisateur.getNomComplet() + "' ?\n\n" +
                "Un mot de passe temporaire sera généré.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UtilisateurService.UtilisateurServiceResult serviceResult =
                        utilisateurService.resetPassword(utilisateur.getId());

                if (serviceResult.isSucces()) {
                    LOGGER.info("✅ Mot de passe réinitialisé : " + utilisateur.getLogin());
                    showInfo("✅ Succès",
                            "Mot de passe réinitialisé avec succès\n\n" +
                                    serviceResult.getMessage());
                    loadUtilisateurs();
                } else {
                    LOGGER.warning("⚠️ Erreur réinitialisation : " + serviceResult.getMessage());
                    showError("Erreur", serviceResult.getMessage());
                }
            } catch (Exception e) {
                LOGGER.severe("❌ Exception réinitialisation : " + e.getMessage());
                showError("Erreur", "Erreur lors de la réinitialisation : " + e.getMessage());
            }
        }
    }

    // ==================== ACTIVER/DÉSACTIVER UN COMPTE ====================
    private void onToggleActive(Utilisateur utilisateur) {
        boolean newState = !utilisateur.getActif();
        String action = newState ? "Activer" : "Désactiver";
        LOGGER.info((newState ? "✅" : "❌") + " " + action + " : " + utilisateur.getLogin());

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(action + " le compte");
        confirmDialog.setHeaderText("Confirmation");
        confirmDialog.setContentText(action + " le compte de '" + utilisateur.getNomComplet() + "' ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                UtilisateurService.UtilisateurServiceResult serviceResult;

                if (newState) {
                    serviceResult = utilisateurService.activateUtilisateur(utilisateur.getId());
                } else {
                    serviceResult = utilisateurService.deactivateUtilisateur(utilisateur.getId());
                }

                if (serviceResult.isSucces()) {
                    LOGGER.info("✅ " + action + " réussi : " + utilisateur.getLogin());
                    showInfo("Succès", serviceResult.getMessage());
                    loadUtilisateurs();
                } else {
                    LOGGER.warning("⚠️ Erreur " + action + " : " + serviceResult.getMessage());
                    showError("Erreur", serviceResult.getMessage());
                }
            } catch (Exception e) {
                LOGGER.severe("❌ Exception " + action + " : " + e.getMessage());
                showError("Erreur", "Erreur lors de " + action.toLowerCase() + " : " + e.getMessage());
            }
        }
    }

    // ==================== CONFIGURATION DES BOUTONS ACTIONS ====================
    private void setupActionsColumn() {
        TableColumn<Utilisateur, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(220);  // ✅ Colonne plus large
        actionsColumn.setMinWidth(220);   // Minimum pour que ça ne se réduise pas

        actionsColumn.setCellFactory(col -> new TableCell<Utilisateur, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    HBox hbox = new HBox(12);  // ✅ Plus d'espacement
                    hbox.setAlignment(Pos.CENTER);
                    hbox.setPadding(new Insets(8, 10, 8, 10));  // ✅ Padding autour

                    // ==================== BOUTON MODIFIER ====================
                    JFXButton btnEdit = new JFXButton("✏️");
                    btnEdit.setStyle(
                            "-fx-font-size: 16; " +
                                    "-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5;"
                    );
                    Tooltip tooltipEdit = new Tooltip("Modifier cet utilisateur");
                    tooltipEdit.setStyle("-fx-font-size: 12; -fx-padding: 8;");
                    Tooltip.install(btnEdit, tooltipEdit);

                    btnEdit.setOnMouseEntered(event -> {
                        btnEdit.setScaleX(1.3);
                        btnEdit.setScaleY(1.3);
                        btnEdit.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: #3B82F6; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-background-radius: 4;"
                        );
                    });
                    btnEdit.setOnMouseExited(event -> {
                        btnEdit.setScaleX(1);
                        btnEdit.setScaleY(1);
                        btnEdit.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5;"
                        );
                    });
                    btnEdit.setOnAction(event -> onEditUser(utilisateur));

                    // ==================== BOUTON SUPPRIMER ====================
                    JFXButton btnDelete = new JFXButton("🗑️");
                    btnDelete.setStyle(
                            "-fx-font-size: 16; " +
                                    "-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5;"
                    );
                    Tooltip tooltipDelete = new Tooltip("Supprimer cet utilisateur");
                    tooltipDelete.setStyle("-fx-font-size: 12; -fx-padding: 8;");
                    Tooltip.install(btnDelete, tooltipDelete);


                    btnDelete.setOnMouseEntered(event -> {
                        btnDelete.setScaleX(1.3);
                        btnDelete.setScaleY(1.3);
                        btnDelete.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: #EF4444; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-background-radius: 4;"
                        );
                    });
                    btnDelete.setOnMouseExited(event -> {
                        btnDelete.setScaleX(1);
                        btnDelete.setScaleY(1);
                        btnDelete.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5;"
                        );
                    });
                    btnDelete.setOnAction(event -> onDeleteUser(utilisateur));

                    // ==================== BOUTON RÉINITIALISER MOT DE PASSE ====================
                    JFXButton btnReset = new JFXButton("🔑");
                    btnReset.setStyle(
                            "-fx-font-size: 16; " +
                                    "-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5;"
                    );
                    Tooltip tooltipReset = new Tooltip("Réinitialiser le mot de passe");
                    tooltipReset.setStyle("-fx-font-size: 12; -fx-padding: 8;");
                    Tooltip.install(btnReset, tooltipReset);

                    btnReset.setOnMouseEntered(event -> {
                        btnReset.setScaleX(1.3);
                        btnReset.setScaleY(1.3);
                        btnReset.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: #F59E0B; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-background-radius: 4;"
                        );
                    });
                    btnReset.setOnMouseExited(event -> {
                        btnReset.setScaleX(1);
                        btnReset.setScaleY(1);
                        btnReset.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5;"
                        );
                    });
                    btnReset.setOnAction(event -> onResetPassword(utilisateur));

                    // ==================== BOUTON ACTIVER/DÉSACTIVER ====================
                    JFXButton btnToggle = new JFXButton(utilisateur.getActif() ? "❌" : "✅");
                    btnToggle.setStyle(
                            "-fx-font-size: 16; " +
                                    "-fx-background-color: transparent; " +
                                    "-fx-cursor: hand; " +
                                    "-fx-padding: 5;"
                    );
                    Tooltip tooltipToggle = new Tooltip(utilisateur.getActif() ?
                            "Désactiver ce compte" : "Activer ce compte");
                    tooltipToggle.setStyle("-fx-font-size: 12; -fx-padding: 8;");
                    Tooltip.install(btnToggle, tooltipToggle);

                    String toggleColor = utilisateur.getActif() ? "#EF4444" : "#10B981";
                    btnToggle.setOnMouseEntered(event -> {
                        btnToggle.setScaleX(1.3);
                        btnToggle.setScaleY(1.3);
                        btnToggle.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: " + toggleColor + "; " +
                                        "-fx-text-fill: white; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5; " +
                                        "-fx-border-radius: 4; " +
                                        "-fx-background-radius: 4;"
                        );
                    });
                    btnToggle.setOnMouseExited(event -> {
                        btnToggle.setScaleX(1);
                        btnToggle.setScaleY(1);
                        btnToggle.setStyle(
                                "-fx-font-size: 16; " +
                                        "-fx-background-color: transparent; " +
                                        "-fx-cursor: hand; " +
                                        "-fx-padding: 5;"
                        );
                    });
                    btnToggle.setOnAction(event -> onToggleActive(utilisateur));

                    hbox.getChildren().addAll(btnEdit, btnDelete, btnReset, btnToggle);
                    setGraphic(hbox);
                }
            }
        });

        tvUtilisateurs.getColumns().add(actionsColumn);
    }

    // ==================== FORMULAIRE DE CRÉATION/MODIFICATION ====================
    private VBox createUtilisateurForm(Stage parentStage, Utilisateur utilisateurExistant) {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(25));
        formBox.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label(utilisateurExistant == null ? "📝 Nouvel Utilisateur" : "✏️ Modifier l'Utilisateur");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label subtitleLabel = new Label(utilisateurExistant == null ?
                "Remplissez les informations du nouvel utilisateur" :
                "Modifiez les informations ci-dessous");
        subtitleLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748B;");

        JFXTextField tfLogin = new JFXTextField();
        tfLogin.setPromptText("Login *");
        tfLogin.setPrefHeight(45);

        JFXTextField tfNom = new JFXTextField();
        tfNom.setPromptText("Nom *");
        tfNom.setPrefHeight(45);

        JFXTextField tfPrenom = new JFXTextField();
        tfPrenom.setPromptText("Prénom *");
        tfPrenom.setPrefHeight(45);

        JFXTextField tfEmail = new JFXTextField();
        tfEmail.setPromptText("Email *");
        tfEmail.setPrefHeight(45);

        JFXPasswordField tfPassword = new JFXPasswordField();
        tfPassword.setPromptText("Mot de passe *");
        tfPassword.setPrefHeight(45);

        JFXComboBox<String> cbProfil = new JFXComboBox<>();
        cbProfil.setItems(FXCollections.observableArrayList("Administrateur", "Bibliothécaire"));
        cbProfil.setValue("Bibliothécaire");
        cbProfil.setPrefHeight(45);

        // ✅ SI MODIFICATION : Remplir les champs existants
        if (utilisateurExistant != null) {
            tfLogin.setText(utilisateurExistant.getLogin());
            tfLogin.setDisable(true); // Login non modifiable
            tfNom.setText(utilisateurExistant.getNom());
            tfPrenom.setText(utilisateurExistant.getPrenom());
            tfEmail.setText(utilisateurExistant.getEmail());
            cbProfil.setValue(utilisateurExistant.getProfil() == Utilisateur.Profil.ADMIN ?
                    "Administrateur" : "Bibliothécaire");
            tfPassword.setPromptText("Laisser vide pour conserver le mot de passe");
        }

        Label requiredLabel = new Label("* Champs obligatoires");
        requiredLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #EF4444; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        JFXButton btnSave = new JFXButton(utilisateurExistant == null ? "✅ Enregistrer" : "💾 Mettre à jour");
        btnSave.setStyle("-fx-background-color: linear-gradient(to right, #10B981, #059669); " +
                "-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 12; " +
                "-fx-padding: 10 30; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSave.setPrefHeight(42);
        btnSave.setMinWidth(130);

        JFXButton btnCancel = new JFXButton("❌ Annuler");
        btnCancel.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #0F172A; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 10 30; " +
                "-fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        btnCancel.setPrefHeight(42);
        btnCancel.setMinWidth(130);
        btnCancel.setOnAction(e -> parentStage.close());

        // ✅ ACTION ENREGISTREMENT
        btnSave.setOnAction(e -> {
            if (tfLogin.getText().trim().isEmpty() || tfNom.getText().trim().isEmpty() ||
                    tfPrenom.getText().trim().isEmpty() || tfEmail.getText().trim().isEmpty()) {
                showError("Erreur", "Tous les champs obligatoires doivent être remplis");
                return;
            }

            if (!tfEmail.getText().contains("@")) {
                showError("Erreur", "L'email n'est pas valide");
                return;
            }

            String login = tfLogin.getText().trim();
            String nom = tfNom.getText().trim();
            String prenom = tfPrenom.getText().trim();
            String email = tfEmail.getText().trim();
            String motDePasse = tfPassword.getText().trim();
            Utilisateur.Profil profil = "Administrateur".equals(cbProfil.getValue()) ?
                    Utilisateur.Profil.ADMIN : Utilisateur.Profil.BIBLIOTHECAIRE;

            try {
                if (utilisateurExistant == null) {
                    // ➕ CRÉATION
                    if (motDePasse.isEmpty()) {
                        showError("Erreur", "Le mot de passe est obligatoire pour un nouvel utilisateur");
                        return;
                    }
                    UtilisateurService.UtilisateurServiceResult result =
                            utilisateurService.createUtilisateur(login, motDePasse, nom, prenom, email, profil);
                    if (result.isSucces()) {
                        LOGGER.info("✅ Utilisateur créé : " + nom);
                        showInfo("Succès", "Utilisateur créé avec succès");
                        parentStage.close();
                    } else {
                        showError("Erreur", result.getMessage());
                    }
                } else {
                    // ✏️ MODIFICATION
                    UtilisateurService.UtilisateurServiceResult result =
                            utilisateurService.updateUtilisateur(utilisateurExistant.getId(),
                                    nom, prenom, email, profil);
                    if (result.isSucces()) {
                        // ✅ Si mot de passe renseigné, mettre à jour aussi
                        if (!motDePasse.isEmpty()) {
                            utilisateurExistant.setMotDePasse(motDePasse);
                            // À faire : créer une méthode pour mettre à jour le mot de passe
                        }
                        LOGGER.info("✅ Utilisateur modifié : " + nom);
                        showInfo("Succès", "Utilisateur modifié avec succès");
                        parentStage.close();
                    } else {
                        showError("Erreur", result.getMessage());
                    }
                }
            } catch (Exception ex) {
                LOGGER.severe("❌ Erreur : " + ex.getMessage());
                showError("Erreur", "Impossible de traiter l'utilisateur : " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(btnCancel, btnSave);
        formBox.getChildren().addAll(titleLabel, subtitleLabel, tfLogin, tfNom, tfPrenom,
                tfEmail, tfPassword, cbProfil, requiredLabel, new Region(), buttonBox);

        return formBox;
    }

    @FXML
    private void onRefresh() {
        LOGGER.info("🔄 Rafraîchissement de la liste");
        loadUtilisateurs();
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