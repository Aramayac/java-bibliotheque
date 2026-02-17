package com.example.livrehome.controller;

import com.example.livrehome.dao.*;
import com.example.livrehome.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des emprunts
 * @author MBAYE
 */
public class EmpruntController implements Initializable {
    
    @FXML private TableView<Emprunt> tableEmprunts;
    @FXML private TableColumn<Emprunt, Long> colId;
    @FXML private TableColumn<Emprunt, String> colLivre;
    @FXML private TableColumn<Emprunt, String> colAdherent;
    @FXML private TableColumn<Emprunt, LocalDateTime> colDateEmprunt;
    @FXML private TableColumn<Emprunt, LocalDate> colDateRetourPrevue;
    @FXML private TableColumn<Emprunt, LocalDateTime> colDateRetourEffective;
    @FXML private TableColumn<Emprunt, String> colStatut;
    @FXML private TableColumn<Emprunt, Double> colPenalite;
    
    @FXML private ComboBox<Livre> cmbLivre;
    @FXML private ComboBox<Adherent> cmbAdherent;
    @FXML private DatePicker dateRetourPrevue;
    @FXML private TextField txtDureeJours;
    @FXML private Label lblPenalite;
    
    @FXML private Button btnEnregistrer;
    @FXML private Button btnRetour;
    @FXML private Button btnNouveau;
    
    @FXML private RadioButton rbEnCours;
    @FXML private RadioButton rbRetard;
    @FXML private RadioButton rbTous;
    
    @FXML private Label lblTotalEmprunts;
    @FXML private Label lblEmpruntsEnCours;
    @FXML private Label lblEmpruntsRetard;
    
    private final EmpruntDAO empruntDAO = new EmpruntDAO();
    private final LivreDAO livreDAO = new LivreDAO();
    private final AdherentDAO adherentDAO = new AdherentDAO();
    private ObservableList<Emprunt> empruntsList = FXCollections.observableArrayList();
    private Emprunt empruntSelectionne = null;
    private Utilisateur utilisateurConnecte;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
        chargerLivres();
        chargerAdherents();
        chargerEmprunts();
        setupListeners();
        mettreAJourStatistiques();
        nouveauEmprunt();
        
        utilisateurConnecte = LoginController.getUtilisateurConnecte();
        
        // Calculer automatiquement la date de retour
        txtDureeJours.textProperty().addListener((obs, old, newVal) -> {
            try {
                if (!newVal.isEmpty()) {
                    int jours = Integer.parseInt(newVal);
                    dateRetourPrevue.setValue(LocalDate.now().plusDays(jours));
                }
            } catch (NumberFormatException e) {
                // Ignorer
            }
        });
    }
    
    private void setupTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateEmprunt.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        colDateRetourPrevue.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        colDateRetourEffective.setCellValueFactory(new PropertyValueFactory<>("dateRetourEffective"));
        colPenalite.setCellValueFactory(new PropertyValueFactory<>("penalite"));
        
        colLivre.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLivre().getTitre()));
        
        colAdherent.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAdherent().getNomComplet()));
        
        colStatut.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut().toString()));
        
        tableEmprunts.setItems(empruntsList);
    }
    
    private void setupListeners() {
        tableEmprunts.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newVal) -> {
                if (newVal != null) {
                    empruntSelectionne = newVal;
                    afficherEmprunt(newVal);
                }
            });
        
        ToggleGroup group = new ToggleGroup();
        rbTous.setToggleGroup(group);
        rbEnCours.setToggleGroup(group);
        rbRetard.setToggleGroup(group);
        rbTous.setSelected(true);
        
        group.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (rbTous.isSelected()) chargerEmprunts();
            else if (rbEnCours.isSelected()) chargerEmpruntsEnCours();
            else if (rbRetard.isSelected()) chargerEmpruntsEnRetard();
        });
    }
    
    private void chargerLivres() {
        try {
            List<Livre> livres = livreDAO.findDisponibles();
            cmbLivre.setItems(FXCollections.observableArrayList(livres));
        } catch (Exception e) {
            afficherErreur("Erreur chargement livres", e.getMessage());
        }
    }
    
    private void chargerAdherents() {
        try {
            List<Adherent> adherents = adherentDAO.findAllActifs();
            cmbAdherent.setItems(FXCollections.observableArrayList(adherents));
        } catch (Exception e) {
            afficherErreur("Erreur chargement adhérents", e.getMessage());
        }
    }
    
    @FXML
    private void chargerEmprunts() {
        try {
            List<Emprunt> emprunts = empruntDAO.findAll();
            empruntsList.clear();
            empruntsList.addAll(emprunts);
            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherErreur("Erreur chargement emprunts", e.getMessage());
        }
    }
    
    private void chargerEmpruntsEnCours() {
        try {
            List<Emprunt> emprunts = empruntDAO.findEnCours();
            empruntsList.clear();
            empruntsList.addAll(emprunts);
        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    private void chargerEmpruntsEnRetard() {
        try {
            List<Emprunt> emprunts = empruntDAO.findEnRetard();
            empruntsList.clear();
            empruntsList.addAll(emprunts);
        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    private void nouveauEmprunt() {
        empruntSelectionne = null;
        viderFormulaire();
        txtDureeJours.setText("14");
        dateRetourPrevue.setValue(LocalDate.now().plusDays(14));
        btnEnregistrer.setDisable(false);
        btnRetour.setDisable(true);
    }
    
    @FXML
    private void enregistrerEmprunt() {
        if (!validerFormulaire()) return;
        
        try {
            Livre livre = cmbLivre.getValue();
            Adherent adherent = cmbAdherent.getValue();
            
            // Vérifier disponibilité
            if (!livre.estDisponible()) {
                afficherErreur("Livre non disponible", "Ce livre n'a plus d'exemplaires disponibles");
                return;
            }
            
            // Vérifier si l'adhérent a des retards
            if (adherentDAO.hasEmpruntsEnRetard(adherent.getId())) {
                afficherErreur("Adhérent en retard", "Cet adhérent a des emprunts en retard");
                return;
            }
            
            // Créer l'emprunt
            int dureeJours = Integer.parseInt(txtDureeJours.getText().trim());
            Emprunt emprunt = new Emprunt(livre, adherent, utilisateurConnecte, dureeJours);
            
            // Sauvegarder
            empruntDAO.save(emprunt);
            
            // Mettre à jour le livre
            livre.emprunter();
            livreDAO.update(livre);
            
            afficherSucces("Emprunt enregistré avec succès");
            chargerEmprunts();
            chargerLivres();
            nouveauEmprunt();
            
        } catch (Exception e) {
            afficherErreur("Erreur d'enregistrement", e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void enregistrerRetour() {
        if (empruntSelectionne == null) {
            afficherErreur("Aucune sélection", "Veuillez sélectionner un emprunt");
            return;
        }
        
        if (empruntSelectionne.getDateRetourEffective() != null) {
            afficherErreur("Retour déjà effectué", "Ce livre a déjà été retourné");
            return;
        }
        
        try {
            // Enregistrer le retour
            empruntSelectionne.enregistrerRetour();
            empruntDAO.update(empruntSelectionne);
            
            // Mettre à jour le livre
            Livre livre = empruntSelectionne.getLivre();
            livre.retourner();
            livreDAO.update(livre);
            
            String message = "Retour enregistré";
            if (empruntSelectionne.getPenalite() > 0) {
                message += "\nPénalité: " + empruntSelectionne.getPenalite() + " FCFA";
            }
            
            afficherSucces(message);
            chargerEmprunts();
            chargerLivres();
            nouveauEmprunt();
            
        } catch (Exception e) {
            afficherErreur("Erreur de retour", e.getMessage());
        }
    }
    
    private void afficherEmprunt(Emprunt emprunt) {
        cmbLivre.setValue(emprunt.getLivre());
        cmbAdherent.setValue(emprunt.getAdherent());
        dateRetourPrevue.setValue(emprunt.getDateRetourPrevue());
        
        if (emprunt.getPenalite() != null && emprunt.getPenalite() > 0) {
            lblPenalite.setText("Pénalité: " + emprunt.getPenalite() + " FCFA");
        } else {
            lblPenalite.setText("");
        }
        
        btnEnregistrer.setDisable(true);
        btnRetour.setDisable(emprunt.getDateRetourEffective() != null);
    }
    
    private boolean validerFormulaire() {
        if (cmbLivre.getValue() == null) {
            afficherErreur("Champ obligatoire", "Veuillez sélectionner un livre");
            return false;
        }
        if (cmbAdherent.getValue() == null) {
            afficherErreur("Champ obligatoire", "Veuillez sélectionner un adhérent");
            return false;
        }
        if (dateRetourPrevue.getValue() == null) {
            afficherErreur("Champ obligatoire", "Veuillez sélectionner une date de retour");
            return false;
        }
        return true;
    }
    
    private void viderFormulaire() {
        cmbLivre.setValue(null);
        cmbAdherent.setValue(null);
        dateRetourPrevue.setValue(null);
        txtDureeJours.clear();
        lblPenalite.setText("");
    }
    
    private void mettreAJourStatistiques() {
        try {
            long total = empruntDAO.count();
            long enCours = empruntDAO.countEnCours();
            long retard = empruntDAO.findEnRetard().size();
            
            if (lblTotalEmprunts != null) lblTotalEmprunts.setText(String.valueOf(total));
            if (lblEmpruntsEnCours != null) lblEmpruntsEnCours.setText(String.valueOf(enCours));
            if (lblEmpruntsRetard != null) lblEmpruntsRetard.setText(String.valueOf(retard));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleActualiserPenalites() {
        try {
            empruntDAO.mettreAJourStatutsEtPenalites();
            afficherSucces("Pénalités mises à jour");
            chargerEmprunts();
        } catch (Exception e) {
            afficherErreur("Erreur", e.getMessage());
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
