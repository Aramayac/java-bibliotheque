package com.example.livreshome.service;

import com.example.livreshome.dao.AdherentDAO;
import com.example.livreshome.dao.EmpruntDAO;
import com.example.livreshome.model.Adherent;
import com.example.livreshome.model.Emprunt;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service métier pour la gestion des adhérents
 * Contient la logique de validation, CRUD et opérations spécifiques
 *
 * @author ARAMA
 * @version 2.0
 */
public class AdherentService {

    private static final Logger LOGGER = Logger.getLogger(AdherentService.class.getName());
    private final AdherentDAO adherentDAO;
    private final EmpruntDAO empruntDAO;

    public AdherentService() {
        this.adherentDAO = new AdherentDAO();
        this.empruntDAO = new EmpruntDAO();
    }

    /**
     * ✅ VERSION 1 : Crée un adhérent avec paramètres String
     */
    public AdherentServiceResult createAdherent(String matricule, String nom, String prenom,
                                                String email, String telephone, String adresse) {
        try {
            // Validation des champs obligatoires
            if (matricule == null || matricule.trim().isEmpty()) {
                return new AdherentServiceResult(false, "Le matricule est obligatoire");
            }

            if (nom == null || nom.trim().isEmpty()) {
                return new AdherentServiceResult(false, "Le nom est obligatoire");
            }

            if (prenom == null || prenom.trim().isEmpty()) {
                return new AdherentServiceResult(false, "Le prénom est obligatoire");
            }

            // Vérifier que le matricule n'existe pas
            if (adherentDAO.matriculeExists(matricule)) {
                return new AdherentServiceResult(false, "Ce matricule existe déjà");
            }

            // Validation email si fourni
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                return new AdherentServiceResult(false, "L'email n'est pas valide");
            }

            // Vérifier que l'email n'existe pas
            if (email != null && !email.isEmpty() && adherentDAO.findByEmail(email) != null) {
                return new AdherentServiceResult(false, "Cet email est déjà utilisé");
            }

            // Créer l'adhérent
            Adherent adherent = new Adherent(matricule, nom, prenom, email, telephone, adresse);
            adherent.setDateInscription(LocalDate.now());
            adherent.setActif(true);

            adherent = adherentDAO.create(adherent);

            LOGGER.info("✅ Adhérent créé : " + adherent.getNomComplet());
            return new AdherentServiceResult(true, "Adhérent créé avec succès", adherent);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur création adhérent : " + e.getMessage());
            e.printStackTrace();
            return new AdherentServiceResult(false, "Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * ✅ VERSION 2 : Crée un adhérent avec objet Adherent
     * (pour utilisation depuis le formulaire modal)
     */
    public void createAdherent(Adherent adherent) {
        try {
            if (adherent == null) {
                LOGGER.warning("⚠️ Adhérent null");
                return;
            }

            // Validation
            if (adherent.getNom() == null || adherent.getNom().trim().isEmpty()) {
                LOGGER.warning("⚠️ Nom vide");
                return;
            }

            if (adherent.getPrenom() == null || adherent.getPrenom().trim().isEmpty()) {
                LOGGER.warning("⚠️ Prénom vide");
                return;
            }

            if (adherent.getEmail() == null || adherent.getEmail().trim().isEmpty()) {
                LOGGER.warning("⚠️ Email vide");
                return;
            }

            // Définir les valeurs par défaut
            if (adherent.getDateInscription() == null) {
                adherent.setDateInscription(LocalDate.now());
            }

            if (adherent.getMatricule() == null || adherent.getMatricule().isEmpty()) {
                adherent.setMatricule(generateMatricule());
            }

            adherent.setActif(true);

            // Sauvegarder
            adherent = adherentDAO.create(adherent);

            LOGGER.info("✅ Adhérent créé via objet : " + adherent.getNomComplet());

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur création adhérent : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour un adhérent existant
     */
    public AdherentServiceResult updateAdherent(Long adherentId, String nom, String prenom,
                                                String email, String telephone, String adresse) {
        try {
            Adherent adherent = adherentDAO.findById(adherentId);

            if (adherent == null) {
                return new AdherentServiceResult(false, "Adhérent non trouvé");
            }

            if (nom == null || nom.trim().isEmpty()) {
                return new AdherentServiceResult(false, "Le nom est obligatoire");
            }

            if (prenom == null || prenom.trim().isEmpty()) {
                return new AdherentServiceResult(false, "Le prénom est obligatoire");
            }

            // Validation email
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                return new AdherentServiceResult(false, "L'email n'est pas valide");
            }

            // Vérifier que l'email n'est pas utilisé par un autre adhérent
            if (email != null && !email.isEmpty()) {
                Adherent existingEmail = adherentDAO.findByEmail(email);
                if (existingEmail != null && !existingEmail.getId().equals(adherentId)) {
                    return new AdherentServiceResult(false, "Cet email est déjà utilisé");
                }
            }

            // Mettre à jour
            adherent.setNom(nom);
            adherent.setPrenom(prenom);
            adherent.setEmail(email);
            adherent.setTelephone(telephone);
            adherent.setAdresse(adresse);

            adherent = adherentDAO.update(adherent);

            LOGGER.info("✅ Adhérent mis à jour : " + adherent.getNomComplet());
            return new AdherentServiceResult(true, "Adhérent mis à jour avec succès", adherent);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur mise à jour adhérent : " + e.getMessage());
            return new AdherentServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Supprime un adhérent
     */
    public AdherentServiceResult deleteAdherent(Long adherentId) {
        try {
            Adherent adherent = adherentDAO.findById(adherentId);

            if (adherent == null) {
                return new AdherentServiceResult(false, "Adhérent non trouvé");
            }

            // Vérifier qu'il n'y a pas d'emprunts actifs
            List<Emprunt> empruntsActuels = empruntDAO.findEmpruntsActuelsByAdherent(adherentId);
            if (!empruntsActuels.isEmpty()) {
                return new AdherentServiceResult(false,
                        "Cet adhérent a " + empruntsActuels.size() + " emprunt(s) en cours");
            }

            boolean deleted = adherentDAO.delete(adherentId);

            if (deleted) {
                LOGGER.info("✅ Adhérent supprimé : " + adherent.getNomComplet());
                return new AdherentServiceResult(true, "Adhérent supprimé avec succès");
            } else {
                return new AdherentServiceResult(false, "Erreur lors de la suppression");
            }

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur suppression adhérent : " + e.getMessage());
            return new AdherentServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Désactive un adhérent
     */
    public AdherentServiceResult deactivateAdherent(Long adherentId) {
        try {
            Adherent adherent = adherentDAO.findById(adherentId);

            if (adherent == null) {
                return new AdherentServiceResult(false, "Adhérent non trouvé");
            }

            adherent.setActif(false);
            adherent = adherentDAO.update(adherent);

            LOGGER.info("✅ Adhérent désactivé : " + adherent.getNomComplet());
            return new AdherentServiceResult(true, "Adhérent désactivé avec succès", adherent);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur désactivation : " + e.getMessage());
            return new AdherentServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Réactive un adhérent
     */
    public AdherentServiceResult activateAdherent(Long adherentId) {
        try {
            Adherent adherent = adherentDAO.findById(adherentId);

            if (adherent == null) {
                return new AdherentServiceResult(false, "Adhérent non trouvé");
            }

            adherent.setActif(true);
            adherent = adherentDAO.update(adherent);

            LOGGER.info("✅ Adhérent activé : " + adherent.getNomComplet());
            return new AdherentServiceResult(true, "Adhérent activé avec succès", adherent);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur activation : " + e.getMessage());
            return new AdherentServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Récupère tous les adhérents
     */
    public List<Adherent> getAllAdherents() {
        try {
            return adherentDAO.findAll();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération adhérents : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les adhérents actifs uniquement
     */
    public List<Adherent> getActiveAdherents() {
        try {
            return adherentDAO.findActifs();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération adhérents actifs : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Recherche globale d'adhérents
     */
    public List<Adherent> searchAdherents(String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                return getAllAdherents();
            }
            return adherentDAO.searchGlobal(keyword);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche adhérents : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère un adhérent par ID
     */
    public Adherent getAdherentById(Long adherentId) {
        try {
            return adherentDAO.findById(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération adhérent : " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère l'historique des emprunts d'un adhérent
     */
    public List<Emprunt> getHistoriqueEmprunts(Long adherentId) {
        try {
            return empruntDAO.findByAdherent(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération historique : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Compte les emprunts actuels d'un adhérent
     */
    public long countEmpruntsActuels(Long adherentId) {
        try {
            return adherentDAO.countEmpruntsActuels(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage emprunts : " + e.getMessage());
            return 0;
        }
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    /**
     * Génère un matricule automatique
     */
    public String generateMatricule() {
        long count = adherentDAO.count() + 1;
        return String.format("ADH-%06d", count);
    }

    /**
     * Classe interne pour le résultat des opérations
     */
    public static class AdherentServiceResult {
        private final boolean succes;
        private final String message;
        private final Adherent adherent;

        public AdherentServiceResult(boolean succes, String message) {
            this(succes, message, null);
        }

        public AdherentServiceResult(boolean succes, String message, Adherent adherent) {
            this.succes = succes;
            this.message = message;
            this.adherent = adherent;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }

        public Adherent getAdherent() {
            return adherent;
        }
    }
}