package com.example.livreshome.service;

import com.example.livreshome.dao.AdherentDAO;
import com.example.livreshome.dao.EmpruntDAO;
import com.example.livreshome.dao.LivreDAO;
import com.example.livreshome.dao.UtilisateurDAO;
import com.example.livreshome.model.Adherent;
import com.example.livreshome.model.Emprunt;
import com.example.livreshome.model.Livre;
import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.util.SessionManager;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service métier pour la gestion des emprunts
 * Gère les emprunts, retours, pénalités et validations
 *
 * @author MBAYE
 * @version 1.0
 */
public class EmpruntService {

    private static final Logger LOGGER = Logger.getLogger(EmpruntService.class.getName());

    // Durée par défaut d'emprunt (30 jours)
    private static final int DEFAULT_LOAN_DURATION = 30;
    // Coût de la pénalité par jour de retard
    private static final double PENALTY_PER_DAY = 1.0;

    private final EmpruntDAO empruntDAO;
    private final LivreDAO livreDAO;
    private final AdherentDAO adherentDAO;
    private final UtilisateurDAO utilisateurDAO;

    public EmpruntService() {
        this.empruntDAO = new EmpruntDAO();
        this.livreDAO = new LivreDAO();
        this.adherentDAO = new AdherentDAO();
        this.utilisateurDAO = new UtilisateurDAO();
    }

    /**
     * Enregistre un nouvel emprunt avec validation complète
     *
     * @param livreId ID du livre
     * @param adherentId ID de l'adhérent
     * @param durationDays Nombre de jours avant retour (optionnel, utilise DEFAULT_LOAN_DURATION)
     * @return Résultat de l'enregistrement
     */
    public EmpruntServiceResult recordEmprunt(Long livreId, Long adherentId, Integer durationDays) {
        try {
            // Validation des paramètres
            if (livreId == null || adherentId == null) {
                return new EmpruntServiceResult(false, "Livre et adhérent sont obligatoires");
            }

            // Récupérer le livre
            Livre livre = livreDAO.findById(livreId);
            if (livre == null) {
                return new EmpruntServiceResult(false, "Livre non trouvé");
            }

            // Vérifier la disponibilité du livre
            if (!livre.getDisponible()) {
                return new EmpruntServiceResult(false, "Ce livre n'est pas disponible");
            }

            // Vérifier qu'il y a au moins un exemplaire disponible
            if (livre.getNombreExemplaires() <= 0) {
                return new EmpruntServiceResult(false, "Aucun exemplaire disponible");
            }

            // Récupérer l'adhérent
            Adherent adherent = adherentDAO.findById(adherentId);
            if (adherent == null) {
                return new EmpruntServiceResult(false, "Adhérent non trouvé");
            }

            // Vérifier que l'adhérent est actif
            if (!adherent.getActif()) {
                return new EmpruntServiceResult(false, "Cet adhérent est inactif");
            }

            // Vérifier qu'il n'y a pas d'emprunt en cours pour ce livre par cet adhérent
            Emprunt empruntExistant = empruntDAO.findEmpruntActuelByAdherentAndLivre(adherentId, livreId);
            if (empruntExistant != null) {
                return new EmpruntServiceResult(false,
                        "Cet adhérent a déjà un emprunt en cours pour ce livre");
            }

            // Récupérer l'utilisateur connecté
            Utilisateur utilisateur = SessionManager.getInstance().getUtilisateurConnecte();
            if (utilisateur == null) {
                return new EmpruntServiceResult(false, "Utilisateur non authentifié");
            }

            // Calculer la date de retour
            int duration = durationDays != null && durationDays > 0 ? durationDays : DEFAULT_LOAN_DURATION;
            LocalDate dateRetourPrevue = LocalDate.now().plusDays(duration);

            // Créer l'emprunt
            Emprunt emprunt = new Emprunt(livre, adherent, utilisateur, dateRetourPrevue);
            emprunt = empruntDAO.create(emprunt);

            // Mettre à jour le nombre d'exemplaires disponibles
            livre.setNombreExemplaires(livre.getNombreExemplaires() - 1);
            if (livre.getNombreExemplaires() <= 0) {
                livre.setDisponible(false);
            }
            livreDAO.update(livre);

            LOGGER.info("✅ Emprunt enregistré : " + livre.getTitre() + " - " + adherent.getNomComplet());
            return new EmpruntServiceResult(true,
                    "Emprunt enregistré. Retour prévu le " + dateRetourPrevue, emprunt);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur enregistrement emprunt : " + e.getMessage());
            return new EmpruntServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Enregistre le retour d'un livre emprunté
     * Calcule automatiquement les pénalités s'il y a retard
     */
    public EmpruntServiceResult recordRetour(Long empruntId) {
        try {
            Emprunt emprunt = empruntDAO.findById(empruntId);

            if (emprunt == null) {
                return new EmpruntServiceResult(false, "Emprunt non trouvé");
            }

            if (emprunt.getDateRetourEffective() != null) {
                return new EmpruntServiceResult(false, "Ce livre a déjà été retourné");
            }

            // Marquer comme retourné
            emprunt.setDateRetourEffective(LocalDate.now());

            // Calculer les pénalités s'il y a retard
            if (LocalDate.now().isAfter(emprunt.getDateRetourPrevue())) {
                long joursRetard = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), LocalDate.now());
                double penalite = joursRetard * PENALTY_PER_DAY;
                emprunt.setPenalite(penalite);

                LOGGER.info("⚠️ Pénalité calculée : " + joursRetard + " jours x " + PENALTY_PER_DAY + "€");
            }

            // Sauvegarder l'emprunt
            emprunt = empruntDAO.update(emprunt);

            // Augmenter le nombre d'exemplaires disponibles
            Livre livre = emprunt.getLivre();
            livre.setNombreExemplaires(livre.getNombreExemplaires() + 1);
            livre.setDisponible(true);
            livreDAO.update(livre);

            LOGGER.info("✅ Retour enregistré : " + livre.getTitre());

            String message = "Retour enregistré avec succès";
            if (emprunt.getPenalite() > 0) {
                message += " - Pénalité : " + String.format("%.2f", emprunt.getPenalite()) + "€";
            }

            return new EmpruntServiceResult(true, message, emprunt);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur enregistrement retour : " + e.getMessage());
            return new EmpruntServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Récupère tous les emprunts en cours (non retournés)
     */
    public List<Emprunt> getAllEmpruntsActuels() {
        try {
            return empruntDAO.findEmpruntsActuels();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunts : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère tous les emprunts en retard
     */
    public List<Emprunt> getAllEmpruntsEnRetard() {
        try {
            return empruntDAO.findEmpruntsEnRetard();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération retards : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère tous les emprunts (y compris retournés)
     */
    public List<Emprunt> getAllEmprunts() {
        try {
            return empruntDAO.findAll();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunts : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les emprunts d'un adhérent spécifique
     */
    public List<Emprunt> getEmpruntsByAdherent(Long adherentId) {
        try {
            return empruntDAO.findByAdherent(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunts adhérent : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les emprunts actuels d'un adhérent
     */
    public List<Emprunt> getEmpruntsActuelsByAdherent(Long adherentId) {
        try {
            return empruntDAO.findEmpruntsActuelsByAdherent(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunts actuels : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère un emprunt par son ID
     */
    public Emprunt getEmpruntById(Long empruntId) {
        try {
            return empruntDAO.findById(empruntId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunt : " + e.getMessage());
            return null;
        }
    }

    /**
     * Récupère les emprunts du mois courant
     */
    public List<Emprunt> getEmpruntsOfCurrentMonth() {
        try {
            LocalDate now = LocalDate.now();
            return empruntDAO.findEmpruntsOfMonth(now.getYear(), now.getMonthValue());
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération emprunts mois : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Calcule la pénalité totale d'un adhérent
     */
    public Double calculateTotalPenalite(Long adherentId) {
        try {
            return empruntDAO.calculateTotalPenalite(adherentId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur calcul pénalités : " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Compte le nombre d'emprunts en retard
     */
    public long countEmpruntsEnRetard() {
        try {
            return empruntDAO.countEmpruntsEnRetard();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage retards : " + e.getMessage());
            return 0;
        }
    }

    /**
     * Compte le nombre d'emprunts actuels
     */
    public long countEmpruntsActuels() {
        try {
            return empruntDAO.countEmpruntsActuels();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage emprunts : " + e.getMessage());
            return 0;
        }
    }

    /**
     * Vérifie si un livre est disponible
     */
    public boolean isLivreDisponible(Long livreId) {
        try {
            Livre livre = livreDAO.findById(livreId);
            return livre != null && livre.getDisponible() && livre.getNombreExemplaires() > 0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur vérification disponibilité : " + e.getMessage());
            return false;
        }
    }

    /**
     * Classe interne pour le résultat des opérations
     */
    public static class EmpruntServiceResult {
        private final boolean succes;
        private final String message;
        private final Emprunt emprunt;

        public EmpruntServiceResult(boolean succes, String message) {
            this(succes, message, null);
        }

        public EmpruntServiceResult(boolean succes, String message, Emprunt emprunt) {
            this.succes = succes;
            this.message = message;
            this.emprunt = emprunt;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }

        public Emprunt getEmprunt() {
            return emprunt;
        }
    }
}