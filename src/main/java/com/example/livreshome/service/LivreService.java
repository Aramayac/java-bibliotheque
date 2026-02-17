package com.example.livreshome.service;

import com.example.livreshome.dao.LivreDAO;
import com.example.livreshome.model.Livre;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service métier pour la gestion des livres
 * Contient la logique de validation et de traitement
 *
 * @author ARAMA
 * @version 1.0
 */
public class LivreService {

    private static final Logger LOGGER = Logger.getLogger(LivreService.class.getName());
    private final LivreDAO livreDAO;

    public LivreService() {
        this.livreDAO = new LivreDAO();
    }

    /**
     * Crée un nouveau livre avec validation
     */
    public LivreServiceResult createLivre(String isbn, String titre, String auteur,
                                          Integer anneePublication, Integer nombreExemplaires) {
        try {
            // Validation des champs obligatoires
            if (isbn == null || isbn.trim().isEmpty()) {
                return new LivreServiceResult(false, "L'ISBN est obligatoire");
            }

            if (titre == null || titre.trim().isEmpty()) {
                return new LivreServiceResult(false, "Le titre est obligatoire");
            }

            // Vérifier que l'ISBN n'existe pas déjà
            if (livreDAO.isbnExists(isbn)) {
                return new LivreServiceResult(false, "Cet ISBN existe déjà");
            }

            // Validation du nombre d'exemplaires
            if (nombreExemplaires == null || nombreExemplaires <= 0) {
                return new LivreServiceResult(false, "Le nombre d'exemplaires doit être > 0");
            }

            // Créer le livre
            Livre livre = new Livre(isbn, titre, auteur, anneePublication, nombreExemplaires, null);
            livre = livreDAO.create(livre);

            LOGGER.info("✅ Livre créé : " + titre);
            return new LivreServiceResult(true, "Livre créé avec succès", livre);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur création livre : " + e.getMessage());
            return new LivreServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Met à jour un livre existant
     */
    public LivreServiceResult updateLivre(Long livreId, String titre, String auteur,
                                          Integer anneePublication, Integer nombreExemplaires) {
        try {
            Livre livre = livreDAO.findById(livreId);

            if (livre == null) {
                return new LivreServiceResult(false, "Livre non trouvé");
            }

            if (titre == null || titre.trim().isEmpty()) {
                return new LivreServiceResult(false, "Le titre est obligatoire");
            }

            if (nombreExemplaires == null || nombreExemplaires <= 0) {
                return new LivreServiceResult(false, "Le nombre d'exemplaires doit être > 0");
            }

            livre.setTitre(titre);
            livre.setAuteur(auteur);
            livre.setAnneePublication(anneePublication);
            livre.setNombreExemplaires(nombreExemplaires);

            livre = livreDAO.update(livre);

            LOGGER.info("✅ Livre mis à jour : " + titre);
            return new LivreServiceResult(true, "Livre mis à jour avec succès", livre);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur mise à jour livre : " + e.getMessage());
            return new LivreServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Supprime un livre
     */
    public LivreServiceResult deleteLivre(Long livreId) {
        try {
            Livre livre = livreDAO.findById(livreId);

            if (livre == null) {
                return new LivreServiceResult(false, "Livre non trouvé");
            }

            // Vérifier qu'il n'y a pas d'emprunts actifs
            if (!livre.getEmprunts().isEmpty()) {
                long empruntsActifs = livre.getEmprunts().stream()
                        .filter(e -> e.getDateRetourEffective() == null)
                        .count();

                if (empruntsActifs > 0) {
                    return new LivreServiceResult(false,
                            "Ce livre a " + empruntsActifs + " emprunt(s) en cours");
                }
            }

            boolean deleted = livreDAO.delete(livreId);

            if (deleted) {
                LOGGER.info("✅ Livre supprimé : " + livre.getTitre());
                return new LivreServiceResult(true, "Livre supprimé avec succès");
            } else {
                return new LivreServiceResult(false, "Erreur lors de la suppression");
            }

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur suppression livre : " + e.getMessage());
            return new LivreServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Récupère tous les livres
     */
    public List<Livre> getAllLivres() {
        try {
            return livreDAO.findAll();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération livres : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Recherche globale de livres
     */
    public List<Livre> searchLivres(String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                return getAllLivres();
            }
            return livreDAO.searchGlobal(keyword);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche livres : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère un livre par ID
     */
    public Livre getLivreById(Long livreId) {
        try {
            return livreDAO.findById(livreId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération livre : " + e.getMessage());
            return null;
        }
    }

    /**
     * Classe interne pour le résultat des opérations
     */
    public static class LivreServiceResult {
        private final boolean succes;
        private final String message;
        private final Livre livre;

        public LivreServiceResult(boolean succes, String message) {
            this(succes, message, null);
        }

        public LivreServiceResult(boolean succes, String message, Livre livre) {
            this.succes = succes;
            this.message = message;
            this.livre = livre;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }

        public Livre getLivre() {
            return livre;
        }
    }
}