package com.example.livreshome.controller;

import com.example.livreshome.dao.AdherentDAO;
import com.example.livreshome.dao.EmpruntDAO;
import com.example.livreshome.dao.LivreDAO;
import com.example.livreshome.model.Emprunt;
import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.util.SessionManager;
import com.example.livreshome.util.StageManager;
import com.example.livreshome.util.StageManager;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Contrôleur pour le Dashboard principal
 * Affiche les statistiques et gère la navigation vers les autres modules
 *
 * @author ARAMA
 * @version 1.0
 */
public class DashboardController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    // ========== FXML INJECTIONS - LABELS KPI ==========
    @FXML
    private Label lblTotalBooks;

    @FXML
    private Label lblTotalMembers;

    @FXML
    private Label lblActiveLoans;

    @FXML
    private Label lblOverdueBooks;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblUserRole;

    // ========== FXML INJECTIONS - BUTTONS ==========
    @FXML
    private JFXButton btnDashboard;

    @FXML
    private JFXButton btnBooks;

    @FXML
    private JFXButton btnMembers;

    @FXML
    private JFXButton btnLoans;

    @FXML
    private JFXButton btnUsers;

    @FXML
    private JFXButton btnStatistics;

    @FXML
    private JFXButton btnSettings;

    @FXML
    private JFXButton btnLogout;

    // ========== FXML INJECTIONS - SEARCH ==========
    @FXML
    private JFXTextField searchField;

    // ========== FXML INJECTIONS - CHART ==========
    @FXML
    private BarChart<String, Number> barChart;

    // ========== DAO ==========
    private LivreDAO livreDAO;
    private AdherentDAO adherentDAO;
    private EmpruntDAO empruntDAO;

    /**
     * Initialisation du contrôleur
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les DAO
        livreDAO = new LivreDAO();
        adherentDAO = new AdherentDAO();
        empruntDAO = new EmpruntDAO();

        // Initialiser les données
        initializeKPIData();
        initializeUserInfo();
        initializeChart();
        initializeListeners();

        LOGGER.info("✅ DashboardController initialisé");

    }

    /**
     * Initialise les données des cartes KPI avec les vraies données
     */
    private void initializeKPIData() {
        try {
            // Total des livres
            long totalBooks = livreDAO.count();
            lblTotalBooks.setText(String.format("%,d", totalBooks));

            // Total des adhérents actifs
            long totalMembers = adherentDAO.count();
            lblTotalMembers.setText(String.format("%,d", totalMembers));

            // Emprunts en cours
            long activeLoans = empruntDAO.countEmpruntsActuels();
            lblActiveLoans.setText(String.format("%,d", activeLoans));

            // Emprunts en retard
            long overdueBooks = empruntDAO.countEmpruntsEnRetard();
            lblOverdueBooks.setText(String.format("%,d", overdueBooks));

            LOGGER.info("✅ KPI données chargées");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement KPI : " + e.getMessage());
        }
    }

    /**
     * Initialise les informations de l'utilisateur connecté
     */
    private void initializeUserInfo() {
        try {
            Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();

            if (utilisateur != null) {
                lblUserName.setText(utilisateur.getNomComplet());
                lblUserRole.setText(utilisateur.getProfil().getLabel());

                // Masquer le bouton "Gestion des utilisateurs" si ce n'est pas un admin
                if (utilisateur.getProfil() != Utilisateur.Profil.ADMIN) {
                    btnUsers.setVisible(false);
                    btnUsers.setManaged(false);
                }

                LOGGER.info("✅ Infos utilisateur chargées : " + utilisateur.getNomComplet());
            }
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement infos utilisateur : " + e.getMessage());
        }
    }

    /**
     * Initialise le graphique des emprunts mensuels
     */
    private void initializeChart() {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Emprunts");

            // Récupérer les données du mois courant
            YearMonth currentMonth = YearMonth.now();
            int year = currentMonth.getYear();
            int month = currentMonth.getMonthValue();

            // Simuler les données par semaine
            List<Emprunt> empruntsMonth = empruntDAO.findEmpruntsOfMonth(year, month);

            // Grouper par semaine (simplifié)
            int[] weekCounts = new int[4];
            for (Emprunt e : empruntsMonth) {
                int day = e.getDateEmprunt().getDayOfMonth();
                int week = (day - 1) / 7;
                if (week < 4) {
                    weekCounts[week]++;
                }
            }

            for (int i = 0; i < 4; i++) {
                series.getData().add(new XYChart.Data<>("Sem " + (i + 1), weekCounts[i]));
            }

            if (barChart != null) {
                barChart.getData().clear();
                barChart.getData().add(series);
            }

            LOGGER.info("✅ Graphique chargé");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur chargement graphique : " + e.getMessage());
        }
    }

    /**
     * Initialise les listeners
     */
    private void initializeListeners() {
        // Listener sur la barre de recherche
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                onSearch(newValue);
            });
        }
    }

    /**
     * Rafraîchit toutes les données du dashboard
     */
    public void refreshDashboard() {
        initializeKPIData();
        initializeChart();
        LOGGER.info("✅ Dashboard rafraîchi");
    }

    // ========== NAVIGATION HANDLERS ==========

    @FXML
    private void onDashboardClicked() {
        LOGGER.info("📊 Dashboard cliqué");
        updateNavigationButtons(btnDashboard);
        refreshDashboard();
    }

    @FXML
    private void onBooksClicked() {
        LOGGER.info("📖 Gestion des livres cliquée");
        updateNavigationButtons(btnBooks);
        openModule("/fxml/Livres.fxml", "Gestion des Livres");
    }

    @FXML
    private void onMembersClicked() {
        LOGGER.info("👥 Gestion des adhérents cliquée");
        updateNavigationButtons(btnMembers);
        openModule("/fxml/Adherents.fxml", "Gestion des Adhérents");
    }

    @FXML
    private void onLoansClicked() {
        LOGGER.info("🔄 Gestion des emprunts cliquée");
        updateNavigationButtons(btnLoans);
        openModule("/fxml/Emprunts.fxml", "Gestion des Emprunts");
    }

    @FXML
    private void onUsersClicked() {
        LOGGER.info("⚙️ Gestion des utilisateurs cliquée");
        updateNavigationButtons(btnUsers);
        openModule("/fxml/utilisateurs.fxml", "Gestion des Utilisateurs");
    }

    @FXML
    private void onStatisticsClicked() {
        LOGGER.info("📈 Statistiques cliquée");
        updateNavigationButtons(btnStatistics);
        // À implémenter
        showNotImplemented("Statistiques");
    }

    @FXML
    private void onSettingsClicked() {
        LOGGER.info("🔧 Paramètres cliquée");
        updateNavigationButtons(btnSettings);
        // À implémenter
        showNotImplemented("Paramètres");
    }

    @FXML
    private void onLogoutClicked() {
        LOGGER.info("🚪 Déconnexion cliquée");
        SessionManager.getInstance().closeSession();
        openLoginPage();
    }

    // ========== UTILITAIRES ==========

    /**
     * Mets à jour le style des boutons de navigation
     */
    private void updateNavigationButtons(JFXButton activeButton) {
        // Réinitialiser tous les boutons
        btnDashboard.getStyleClass().remove("nav-button-active");
        btnDashboard.getStyleClass().add("nav-button");

        btnBooks.getStyleClass().remove("nav-button-active");
        btnBooks.getStyleClass().add("nav-button");

        btnMembers.getStyleClass().remove("nav-button-active");
        btnMembers.getStyleClass().add("nav-button");

        btnLoans.getStyleClass().remove("nav-button-active");
        btnLoans.getStyleClass().add("nav-button");

        btnUsers.getStyleClass().remove("nav-button-active");
        btnUsers.getStyleClass().add("nav-button");

        btnStatistics.getStyleClass().remove("nav-button-active");
        btnStatistics.getStyleClass().add("nav-button");

        btnSettings.getStyleClass().remove("nav-button-active");
        btnSettings.getStyleClass().add("nav-button");

        // Activer le bouton sélectionné
        activeButton.getStyleClass().remove("nav-button");
        activeButton.getStyleClass().add("nav-button-active");
    }

    /**
     * Ouvre un module dans une nouvelle fenêtre
     */
    private void openModule(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            stage.setTitle("BiblioMax - " + title);
            stage.setScene(scene);
            stage.show();

            LOGGER.info("✅ Module ouvert : " + title);
        } catch (IOException e) {
            LOGGER.severe("❌ Erreur ouverture module : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retourne à la page de connexion
     */
    private void openLoginPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogout.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/login-style.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("BiblioMax - Connexion");
            stage.show();

            LOGGER.info("✅ Retour à la page de connexion");
        } catch (IOException e) {
            LOGGER.severe("❌ Erreur retour connexion : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Affiche un message de fonctionnalité non implémentée
     */
    private void showNotImplemented(String feature) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(feature + " sera disponible bientôt...");
        alert.showAndWait();
    }

    /**
     * Gère la recherche globale
     */
    private void onSearch(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }
        LOGGER.info("🔍 Recherche : " + query);
        // À implémenter : recherche multi-modules
    }

    /**
     * ✅ GESTION DES UTILISATEURS
     */

    /**
     * ✅ GESTION DES LIVRES
     */
    @FXML
    private void onLivresClicked() {
        LOGGER.info("📚 Gestion des livres cliquée");
        StageManager.navigateTo("/fxml/Livres.fxml", 1400, 900);
    }

    /**
     * ✅ GESTION DES ADHÉRENTS
     */
    @FXML
    private void onAdherentsClicked() {
        LOGGER.info("👥 Gestion des adhérents cliquée");
        StageManager.navigateTo("/fxml/Adherents.fxml", 1400, 900);
    }

    /**
     * ✅ GESTION DES EMPRUNTS
     */
    @FXML
    private void onEmpruntClicked() {
        LOGGER.info("📦 Gestion des emprunts cliquée");
        StageManager.navigateTo("/fxml/Emprunts.fxml", 1400, 900);
    }
}