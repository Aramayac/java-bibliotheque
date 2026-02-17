package com.example.livrehome.controller;

import com.example.livrehome.dao.LivreDAO;
import com.example.livrehome.dao.CategorieDAO;
import com.example.livrehome.model.Livre;
import com.example.livrehome.model.Categorie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des livres
 * @author KA
 */
public class LivreController implements Initializable {
    
    @FXML private TableView<Livre> tableLivres;
    @FXML private TableColumn<Livre, Long> colId;
    @FXML private TableColumn<Livre, String> colIsbn;
    @FXML private TableColumn<Livre, String> colTitre;
    @FXML private TableColumn<Livre, String> colAuteur;
    @FXML private TableColumn<Livre, Integer> colAnnee;
    @FXML private TableColumn<Livre, String> colCategorie;
    @FXML private TableColumn<Livre, Integer> colExemplaires;
    @FXML private TableColumn<Livre, Integer> colDisponibles;
    
    @FXML private TextField txtIsbn;
    @FXML private TextField txtTitre;
    @FXML private TextField txtAuteur;
    @FXML private TextField txtAnnee;
    @FXML private TextField txtExemplaires;
    @FXML private ComboBox<Categorie> cmbCategorie;
    @FXML private TextField txtRecherche;
    
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnNouveau;
    
    @FXML private Label lblTotalLivres;
    @FXML private Label lblLivresDisponibles;
    
    private final LivreDAO livreDAO = new LivreDAO();
    private final CategorieDAO categorieDAO = new CategorieDAO();
    private ObservableList<Livre> livresList = FXCollections.observableArrayList();
    private Livre livreSelectionne = null;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
        chargerCategories();
        chargerLivres();
        setupListeners();
        mettreAJourStatistiques();
        nouveauLivre();
    }
    
    private void setupTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("anneePublication"));
        colExemplaires.setCellValueFactory(new PropertyValueFactory<>("nombreExemplaires"));
        colDisponibles.setCellValueFactory(new PropertyValueFactory<>("exemplairesDisponibles"));
        
        colCategorie.setCellValueFactory(cellData -> {
            Categorie cat = cellData.getValue().getCategorie();
            return new javafx.beans.property.SimpleStringProperty(
                cat != null ? cat.getLibelle() : "");
        });
        
        tableLivres.setItems(livresList);
    }
    
    private void setupListeners() {
        tableLivres.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) {
                    livreSelectionne = newVal;
                    afficherLivre(newVal);
                    btnModifier.setDisable(false);
                    btnSupprimer.setDisable(false);
                }
            });
        
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                chargerLivres();
            } else {
                rechercherLivres(newVal);
            }
        });
    }
    
    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieDAO.findAll();
            cmbCategorie.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            afficherErreur("Erreur de chargement des catégories", e.getMessage());
        }
    }
    
    @FXML
    private void chargerLivres() {
        try {
            List<Livre> livres = livreDAO.findAll();
            livresList.clear();
            livresList.addAll(livres);
            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherErreur("Erreur de chargement", e.getMessage());
        }
    }
    
    @FXML
    private void rechercherLivres(String searchTerm) {
        try {
            List<Livre> resultats = livreDAO.search(searchTerm);
            livresList.clear();
            livresList.addAll(resultats);
        } catch (Exception e) {
            afficherErreur("Erreur de recherche", e.getMessage());
        }
    }
    
    @FXML
    private void nouveauLivre() {
        livreSelectionne = null;
        viderFormulaire();
        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        tableLivres.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void ajouterLivre() {
        if (!validerFormulaire()) return;
        
        try {
            // Vérifier ISBN unique
            if (livreDAO.isbnExists(txtIsbn.getText().trim())) {
                afficherErreur("ISBN existant", "Cet ISBN est déjà utilisé");
                return;
            }
            
            Livre livre = new Livre();
            remplirLivreDepuisFormulaire(livre);
            livre.setExemplairesDisponibles(livre.getNombreExemplaires());
            
            livreDAO.save(livre);
            afficherSucces("Livre ajouté avec succès");
            chargerLivres();
            nouveauLivre();
            
        } catch (Exception e) {
            afficherErreur("Erreur d'ajout", e.getMessage());
        }
    }
    
    @FXML
    private void modifierLivre() {
        if (livreSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un livre");
            return;
        }
        
        if (!validerFormulaire()) return;
        
        try {
            remplirLivreDepuisFormulaire(livreSelectionne);
            livreDAO.update(livreSelectionne);
            afficherSucces("Livre modifié avec succès");
            chargerLivres();
            nouveauLivre();
            
        } catch (Exception e) {
            afficherErreur("Erreur de modification", e.getMessage());
        }
    }
    
    @FXML
    private void supprimerLivre() {
        if (livreSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un livre");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le livre");
        confirmation.setContentText("Voulez-vous vraiment supprimer \"" + livreSelectionne.getTitre() + "\"?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    livreDAO.delete(livreSelectionne.getId());
                    afficherSucces("Livre supprimé avec succès");
                    chargerLivres();
                    nouveauLivre();
                } catch (Exception e) {
                    afficherErreur("Erreur de suppression", e.getMessage());
                }
            }
        });
    }
    
    private void afficherLivre(Livre livre) {
        txtIsbn.setText(livre.getIsbn());
        txtTitre.setText(livre.getTitre());
        txtAuteur.setText(livre.getAuteur());
        txtAnnee.setText(livre.getAnneePublication() != null ? livre.getAnneePublication().toString() : "");
        txtExemplaires.setText(livre.getNombreExemplaires().toString());
        cmbCategorie.setValue(livre.getCategorie());
        
        btnAjouter.setDisable(true);
        btnModifier.setDisable(false);
    }
    
    private void remplirLivreDepuisFormulaire(Livre livre) {
        livre.setIsbn(txtIsbn.getText().trim());
        livre.setTitre(txtTitre.getText().trim());
        livre.setAuteur(txtAuteur.getText().trim());
        livre.setAnneePublication(txtAnnee.getText().isEmpty() ? null : Integer.parseInt(txtAnnee.getText().trim()));
        livre.setNombreExemplaires(Integer.parseInt(txtExemplaires.getText().trim()));
        livre.setCategorie(cmbCategorie.getValue());
    }
    
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        if (txtIsbn.getText().trim().isEmpty()) erreurs.append("- ISBN obligatoire\n");
        if (txtTitre.getText().trim().isEmpty()) erreurs.append("- Titre obligatoire\n");
        if (txtAuteur.getText().trim().isEmpty()) erreurs.append("- Auteur obligatoire\n");
        if (txtExemplaires.getText().trim().isEmpty()) erreurs.append("- Nombre d'exemplaires obligatoire\n");
        if (cmbCategorie.getValue() == null) erreurs.append("- Catégorie obligatoire\n");
        
        if (erreurs.length() > 0) {
            afficherErreur("Formulaire incomplet", erreurs.toString());
            return false;
        }
        
        try {
            if (!txtAnnee.getText().isEmpty()) {
                Integer.parseInt(txtAnnee.getText().trim());
            }
            Integer.parseInt(txtExemplaires.getText().trim());
        } catch (NumberFormatException e) {
            afficherErreur("Erreur de format", "Année et exemplaires doivent être des nombres");
            return false;
        }
        
        return true;
    }
    
    private void viderFormulaire() {
        txtIsbn.clear();
        txtTitre.clear();
        txtAuteur.clear();
        txtAnnee.clear();
        txtExemplaires.clear();
        cmbCategorie.setValue(null);
    }
    
    private void mettreAJourStatistiques() {
        try {
            long total = livreDAO.count();
            long disponibles = livreDAO.findDisponibles().size();
            
            if (lblTotalLivres != null) lblTotalLivres.setText(String.valueOf(total));
            if (lblLivresDisponibles != null) lblLivresDisponibles.setText(String.valueOf(disponibles));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
