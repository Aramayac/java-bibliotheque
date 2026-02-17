package com.example.livrehome.controller;

import com.example.livrehome.dao.AdherentDAO;
import com.example.livrehome.model.Adherent;
import com.example.livrehome.model.Emprunt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur JavaFX pour la gestion des adhérents
 * @author SECK
 */
public class AdherentController implements Initializable {
    
    // DAO
    private final AdherentDAO adherentDAO = new AdherentDAO();
    
    // TableView et colonnes
    @FXML private TableView<Adherent> tableAdherents;
    @FXML private TableColumn<Adherent, Long> colId;
    @FXML private TableColumn<Adherent, String> colMatricule;
    @FXML private TableColumn<Adherent, String> colNom;
    @FXML private TableColumn<Adherent, String> colPrenom;
    @FXML private TableColumn<Adherent, String> colEmail;
    @FXML private TableColumn<Adherent, String> colTelephone;
    @FXML private TableColumn<Adherent, LocalDate> colDateInscription;
    @FXML private TableColumn<Adherent, Boolean> colActif;
    
    // Champs de formulaire
    @FXML private TextField txtMatricule;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private TextArea txtAdresse;
    @FXML private DatePicker dateInscription;
    @FXML private CheckBox chkActif;
    
    // Champ de recherche
    @FXML private TextField txtRecherche;
    
    // Boutons
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnNouveau;
    @FXML private Button btnHistorique;
    @FXML private Button btnSuspendre;
    @FXML private Button btnReactiver;
    
    // Labels statistiques
    @FXML private Label lblTotalAdherents;
    @FXML private Label lblAdherentsActifs;
    @FXML private Label lblNouveauxMois;
    
    // Données
    private ObservableList<Adherent> adherentsList = FXCollections.observableArrayList();
    private Adherent adherentSelectionne = null;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableView();
        chargerAdherents();
        setupListeners();
        mettreAJourStatistiques();
        nouveauAdherent();
    }
    
    /**
     * Configurer les colonnes du TableView
     */
    private void setupTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colDateInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        
        // Formater la colonne statut
        colActif.setCellFactory(column -> new TableCell<Adherent, Boolean>() {
            @Override
            protected void updateItem(Boolean actif, boolean empty) {
                super.updateItem(actif, empty);
                if (empty || actif == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(actif ? "Actif" : "Inactif");
                    setStyle(actif ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
        
        tableAdherents.setItems(adherentsList);
    }
    
    /**
     * Configurer les listeners
     */
    private void setupListeners() {
        // Sélection dans le tableau
        tableAdherents.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    adherentSelectionne = newSelection;
                    afficherAdherent(newSelection);
                    activerBoutons(true);
                } else {
                    activerBoutons(false);
                }
            }
        );
        
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                chargerAdherents();
            } else {
                rechercherAdherents(newValue);
            }
        });
    }
    
    /**
     * Charger tous les adhérents
     */
    @FXML
    private void chargerAdherents() {
        try {
            List<Adherent> adherents = adherentDAO.findAll();
            adherentsList.clear();
            adherentsList.addAll(adherents);
            mettreAJourStatistiques();
        } catch (Exception e) {
            afficherErreur("Erreur de chargement", 
                "Impossible de charger la liste des adhérents: " + e.getMessage());
        }
    }
    
    /**
     * Rechercher des adhérents
     */
    @FXML
    private void rechercherAdherents(String searchTerm) {
        try {
            List<Adherent> resultats = adherentDAO.search(searchTerm);
            adherentsList.clear();
            adherentsList.addAll(resultats);
        } catch (Exception e) {
            afficherErreur("Erreur de recherche", 
                "Erreur lors de la recherche: " + e.getMessage());
        }
    }
    
    /**
     * Préparer le formulaire pour un nouvel adhérent
     */
    @FXML
    private void nouveauAdherent() {
        adherentSelectionne = null;
        viderFormulaire();
        dateInscription.setValue(LocalDate.now());
        chkActif.setSelected(true);
        txtMatricule.setDisable(true); // Le matricule est auto-généré
        btnAjouter.setDisable(false);
        btnModifier.setDisable(true);
        tableAdherents.getSelectionModel().clearSelection();
    }
    
    /**
     * Ajouter un nouvel adhérent
     */
    @FXML
    private void ajouterAdherent() {
        if (!validerFormulaire()) {
            return;
        }
        
        try {
            Adherent adherent = new Adherent();
            remplirAdherentDepuisFormulaire(adherent);
            
            // Vérifier si l'email existe déjà
            if (adherentDAO.emailExists(adherent.getEmail())) {
                afficherErreur("Email existant", 
                    "Cet email est déjà utilisé par un autre adhérent.");
                return;
            }
            
            adherentDAO.save(adherent);
            afficherSucces("Ajout réussi", 
                "L'adhérent a été ajouté avec succès. Matricule: " + adherent.getMatricule());
            chargerAdherents();
            nouveauAdherent();
        } catch (Exception e) {
            afficherErreur("Erreur d'ajout", 
                "Impossible d'ajouter l'adhérent: " + e.getMessage());
        }
    }
    
    /**
     * Modifier un adhérent existant
     */
    @FXML
    private void modifierAdherent() {
        if (adherentSelectionne == null) {
            afficherErreur("Aucune sélection", 
                "Veuillez sélectionner un adhérent à modifier.");
            return;
        }
        
        if (!validerFormulaire()) {
            return;
        }
        
        try {
            remplirAdherentDepuisFormulaire(adherentSelectionne);
            adherentDAO.update(adherentSelectionne);
            afficherSucces("Modification réussie", 
                "L'adhérent a été modifié avec succès.");
            chargerAdherents();
            nouveauAdherent();
        } catch (Exception e) {
            afficherErreur("Erreur de modification", 
                "Impossible de modifier l'adhérent: " + e.getMessage());
        }
    }
    
    /**
     * Supprimer un adhérent
     */
    @FXML
    private void supprimerAdherent() {
        if (adherentSelectionne == null) {
            afficherErreur("Aucune sélection", 
                "Veuillez sélectionner un adhérent à supprimer.");
            return;
        }
        
        // Vérifier si l'adhérent a des emprunts
        List<Emprunt> emprunts = adherentDAO.getHistoriqueEmprunts(adherentSelectionne.getId());
        if (!emprunts.isEmpty()) {
            afficherErreur("Suppression impossible", 
                "Cet adhérent a des emprunts enregistrés. " +
                "Vous pouvez le suspendre au lieu de le supprimer.");
            return;
        }
        
        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'adhérent");
        confirmation.setContentText("Voulez-vous vraiment supprimer " + 
            adherentSelectionne.getNomComplet() + " ?");
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                adherentDAO.delete(adherentSelectionne.getId());
                afficherSucces("Suppression réussie", 
                    "L'adhérent a été supprimé avec succès.");
                chargerAdherents();
                nouveauAdherent();
            } catch (Exception e) {
                afficherErreur("Erreur de suppression", 
                    "Impossible de supprimer l'adhérent: " + e.getMessage());
            }
        }
    }
    
    /**
     * Suspendre un adhérent
     */
    @FXML
    private void suspendreAdherent() {
        if (adherentSelectionne == null) {
            afficherErreur("Aucune sélection", 
                "Veuillez sélectionner un adhérent à suspendre.");
            return;
        }
        
        if (!adherentSelectionne.getActif()) {
            afficherInfo("Déjà suspendu", 
                "Cet adhérent est déjà suspendu.");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suspension");
        confirmation.setHeaderText("Suspendre l'adhérent");
        confirmation.setContentText("Voulez-vous vraiment suspendre " + 
            adherentSelectionne.getNomComplet() + " ?");
        
        Optional<ButtonType> resultat = confirmation.showAndWait();
        if (resultat.isPresent() && resultat.get() == ButtonType.OK) {
            try {
                adherentDAO.suspendre(adherentSelectionne.getId());
                afficherSucces("Suspension réussie", 
                    "L'adhérent a été suspendu avec succès.");
                chargerAdherents();
                nouveauAdherent();
            } catch (Exception e) {
                afficherErreur("Erreur de suspension", 
                    "Impossible de suspendre l'adhérent: " + e.getMessage());
            }
        }
    }
    
    /**
     * Réactiver un adhérent
     */
    @FXML
    private void reactiverAdherent() {
        if (adherentSelectionne == null) {
            afficherErreur("Aucune sélection", 
                "Veuillez sélectionner un adhérent à réactiver.");
            return;
        }
        
        if (adherentSelectionne.getActif()) {
            afficherInfo("Déjà actif", 
                "Cet adhérent est déjà actif.");
            return;
        }
        
        try {
            adherentDAO.reactiver(adherentSelectionne.getId());
            afficherSucces("Réactivation réussie", 
                "L'adhérent a été réactivé avec succès.");
            chargerAdherents();
            nouveauAdherent();
        } catch (Exception e) {
            afficherErreur("Erreur de réactivation", 
                "Impossible de réactiver l'adhérent: " + e.getMessage());
        }
    }
    
    /**
     * Afficher l'historique des emprunts d'un adhérent
     */
    @FXML
    private void afficherHistorique() {
        if (adherentSelectionne == null) {
            afficherErreur("Aucune sélection", 
                "Veuillez sélectionner un adhérent.");
            return;
        }
        
        try {
            List<Emprunt> historique = adherentDAO.getHistoriqueEmprunts(
                adherentSelectionne.getId());
            
            // Créer une boîte de dialogue pour afficher l'historique
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Historique des emprunts");
            alert.setHeaderText("Historique de " + adherentSelectionne.getNomComplet());
            
            if (historique.isEmpty()) {
                alert.setContentText("Aucun emprunt trouvé.");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Total d'emprunts: ").append(historique.size()).append("\n\n");
                
                for (Emprunt e : historique) {
                    sb.append("- ").append(e.getLivre().getTitre())
                      .append(" (").append(e.getDateEmprunt()).append(")");
                    if (e.getDateRetourEffective() != null) {
                        sb.append(" - Retourné le ").append(e.getDateRetourEffective());
                    } else {
                        sb.append(" - En cours");
                    }
                    sb.append("\n");
                }
                
                TextArea textArea = new TextArea(sb.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                alert.getDialogPane().setContent(textArea);
            }
            
            alert.showAndWait();
        } catch (Exception e) {
            afficherErreur("Erreur", 
                "Impossible d'afficher l'historique: " + e.getMessage());
        }
    }
    
    /**
     * Afficher un adhérent dans le formulaire
     */
    private void afficherAdherent(Adherent adherent) {
        txtMatricule.setText(adherent.getMatricule());
        txtNom.setText(adherent.getNom());
        txtPrenom.setText(adherent.getPrenom());
        txtEmail.setText(adherent.getEmail());
        txtTelephone.setText(adherent.getTelephone());
        txtAdresse.setText(adherent.getAdresse());
        dateInscription.setValue(adherent.getDateInscription());
        chkActif.setSelected(adherent.getActif());
        
        txtMatricule.setDisable(true);
        btnAjouter.setDisable(true);
        btnModifier.setDisable(false);
    }
    
    /**
     * Remplir un objet Adherent depuis le formulaire
     */
    private void remplirAdherentDepuisFormulaire(Adherent adherent) {
        adherent.setNom(txtNom.getText().trim());
        adherent.setPrenom(txtPrenom.getText().trim());
        adherent.setEmail(txtEmail.getText().trim());
        adherent.setTelephone(txtTelephone.getText().trim());
        adherent.setAdresse(txtAdresse.getText().trim());
        adherent.setDateInscription(dateInscription.getValue());
        adherent.setActif(chkActif.isSelected());
    }
    
    /**
     * Valider le formulaire
     */
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();
        
        if (txtNom.getText() == null || txtNom.getText().trim().isEmpty()) {
            erreurs.append("- Le nom est obligatoire\n");
        }
        
        if (txtPrenom.getText() == null || txtPrenom.getText().trim().isEmpty()) {
            erreurs.append("- Le prénom est obligatoire\n");
        }
        
        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            erreurs.append("- L'email est obligatoire\n");
        } else if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            erreurs.append("- L'email n'est pas valide\n");
        }
        
        if (dateInscription.getValue() == null) {
            erreurs.append("- La date d'inscription est obligatoire\n");
        }
        
        if (erreurs.length() > 0) {
            afficherErreur("Formulaire incomplet", erreurs.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Vider le formulaire
     */
    private void viderFormulaire() {
        txtMatricule.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtAdresse.clear();
        dateInscription.setValue(null);
        chkActif.setSelected(true);
        txtMatricule.setDisable(false);
    }
    
    /**
     * Activer/Désactiver les boutons
     */
    private void activerBoutons(boolean actif) {
        btnModifier.setDisable(!actif);
        btnSupprimer.setDisable(!actif);
        btnHistorique.setDisable(!actif);
        btnSuspendre.setDisable(!actif);
        btnReactiver.setDisable(!actif);
    }
    
    /**
     * Mettre à jour les statistiques
     */
    private void mettreAJourStatistiques() {
        try {
            long total = adherentDAO.count();
            long actifs = adherentDAO.countActifs();
            int nouveaux = adherentDAO.getNouveauxAdherentsDuMois().size();
            
            if (lblTotalAdherents != null) {
                lblTotalAdherents.setText(String.valueOf(total));
            }
            if (lblAdherentsActifs != null) {
                lblAdherentsActifs.setText(String.valueOf(actifs));
            }
            if (lblNouveauxMois != null) {
                lblNouveauxMois.setText(String.valueOf(nouveaux));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des statistiques: " + 
                e.getMessage());
        }
    }
    
    /**
     * Afficher un message de succès
     */
    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Afficher un message d'erreur
     */
    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Afficher un message d'information
     */
    private void afficherInfo(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
