package com.example.livreshome.service;

import com.example.livreshome.dao.UtilisateurDAO;
import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.util.SecurityUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Collectors;  // ✅ À AJOUTER

/**
 * Service métier pour la gestion des utilisateurs
 * Opérations CRUD, validation et gestion des droits
 *
 * @author ARAMA
 * @version 1.0
 */
public class UtilisateurService {

    private static final Logger LOGGER = Logger.getLogger(UtilisateurService.class.getName());
    private final UtilisateurDAO utilisateurDAO;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    /**
     * Crée un nouvel utilisateur avec validation complète
     */
    public UtilisateurServiceResult createUtilisateur(String login, String motDePasse,
                                                      String nom, String prenom,
                                                      String email, Utilisateur.Profil profil) {
        try {
            // Validation des champs obligatoires
            if (login == null || login.trim().isEmpty()) {
                return new UtilisateurServiceResult(false, "Le login est obligatoire");
            }

            if (motDePasse == null || motDePasse.isEmpty()) {
                return new UtilisateurServiceResult(false, "Le mot de passe est obligatoire");
            }

            if (nom == null || nom.trim().isEmpty()) {
                return new UtilisateurServiceResult(false, "Le nom est obligatoire");
            }

            if (prenom == null || prenom.trim().isEmpty()) {
                return new UtilisateurServiceResult(false, "Le prénom est obligatoire");
            }

            if (profil == null) {
                return new UtilisateurServiceResult(false, "Le profil est obligatoire");
            }

            // Vérifier que le login n'existe pas
            if (utilisateurDAO.loginExists(login)) {
                return new UtilisateurServiceResult(false, "Ce login existe déjà");
            }

            // Validation email si fourni
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                return new UtilisateurServiceResult(false, "L'email n'est pas valide");
            }

            // Vérifier que l'email n'existe pas
            if (email != null && !email.isEmpty() && utilisateurDAO.emailExists(email)) {
                return new UtilisateurServiceResult(false, "Cet email est déjà utilisé");
            }

            // Valider la force du mot de passe
            SecurityUtil.PasswordStrength strength = SecurityUtil.checkPasswordStrength(motDePasse);
            if (strength.getNiveau() == SecurityUtil.PasswordStrength.Niveau.TRES_FAIBLE ||
                    strength.getNiveau() == SecurityUtil.PasswordStrength.Niveau.FAIBLE) {
                return new UtilisateurServiceResult(false,
                        "Le mot de passe n'est pas assez robuste\n" + strength.getDetails());
            }

            // Créer l'utilisateur
            Utilisateur utilisateur = new Utilisateur(
                    login,
                    SecurityUtil.hashPassword(motDePasse),
                    nom,
                    prenom,
                    email,
                    profil
            );

            utilisateur = utilisateurDAO.create(utilisateur);

            LOGGER.info("✅ Utilisateur créé : " + utilisateur.getLogin());
            return new UtilisateurServiceResult(true, "Utilisateur créé avec succès", utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur création utilisateur : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Met à jour un utilisateur existant
     */
    public UtilisateurServiceResult updateUtilisateur(Long userId, String nom, String prenom,
                                                      String email, Utilisateur.Profil profil) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new UtilisateurServiceResult(false, "Utilisateur non trouvé");
            }

            if (nom == null || nom.trim().isEmpty()) {
                return new UtilisateurServiceResult(false, "Le nom est obligatoire");
            }

            if (prenom == null || prenom.trim().isEmpty()) {
                return new UtilisateurServiceResult(false, "Le prénom est obligatoire");
            }

            if (profil == null) {
                return new UtilisateurServiceResult(false, "Le profil est obligatoire");
            }

            // Validation email
            if (email != null && !email.isEmpty() && !isValidEmail(email)) {
                return new UtilisateurServiceResult(false, "L'email n'est pas valide");
            }

            // Vérifier que l'email n'est pas utilisé par un autre utilisateur
            if (email != null && !email.isEmpty()) {
                Utilisateur existingEmail = utilisateurDAO.findByEmail(email);
                if (existingEmail != null && !existingEmail.getId().equals(userId)) {
                    return new UtilisateurServiceResult(false, "Cet email est déjà utilisé");
                }
            }

            // Mettre à jour
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setProfil(profil);

            utilisateur = utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Utilisateur mis à jour : " + utilisateur.getLogin());
            return new UtilisateurServiceResult(true, "Utilisateur mis à jour avec succès", utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur mise à jour utilisateur : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Supprime un utilisateur
     */
    public UtilisateurServiceResult deleteUtilisateur(Long userId) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new UtilisateurServiceResult(false, "Utilisateur non trouvé");
            }

            // Vérifier qu'on ne supprime pas le dernier admin
            if (utilisateur.getProfil() == Utilisateur.Profil.ADMIN) {
                List<Utilisateur> admins = utilisateurDAO.findAllAdmins();
                if (admins.size() <= 1) {
                    return new UtilisateurServiceResult(false,
                            "Impossible de supprimer le dernier administrateur");
                }
            }

            boolean deleted = utilisateurDAO.delete(userId);

            if (deleted) {
                LOGGER.info("✅ Utilisateur supprimé : " + utilisateur.getLogin());
                return new UtilisateurServiceResult(true, "Utilisateur supprimé avec succès");
            } else {
                return new UtilisateurServiceResult(false, "Erreur lors de la suppression");
            }

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur suppression utilisateur : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Désactive un utilisateur
     */
    public UtilisateurServiceResult deactivateUtilisateur(Long userId) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new UtilisateurServiceResult(false, "Utilisateur non trouvé");
            }

            // Vérifier qu'on ne désactive pas le dernier admin actif
            if (utilisateur.getProfil() == Utilisateur.Profil.ADMIN && utilisateur.getActif()) {
                List<Utilisateur> adminsActifs = utilisateurDAO.findAllAdmins().stream()
                        .filter(Utilisateur::getActif)
                        .collect(Collectors.toList());  // ✅ CHANGÉ de .toList()
                if (adminsActifs.size() <= 1) {
                    return new UtilisateurServiceResult(false,
                            "Impossible de désactiver le dernier administrateur actif");
                }
            }

            utilisateur.setActif(false);
            utilisateur = utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Utilisateur désactivé : " + utilisateur.getLogin());
            return new UtilisateurServiceResult(true, "Utilisateur désactivé avec succès", utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur désactivation : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Active un utilisateur
     */
    public UtilisateurServiceResult activateUtilisateur(Long userId) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new UtilisateurServiceResult(false, "Utilisateur non trouvé");
            }

            utilisateur.setActif(true);
            utilisateur = utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Utilisateur activé : " + utilisateur.getLogin());
            return new UtilisateurServiceResult(true, "Utilisateur activé avec succès", utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur activation : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur
     */
    public UtilisateurServiceResult resetPassword(Long userId) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new UtilisateurServiceResult(false, "Utilisateur non trouvé");
            }

            String motDePasseTemporaire = SecurityUtil.generateTemporaryPassword();
            utilisateur.setMotDePasse(SecurityUtil.hashPassword(motDePasseTemporaire));
            utilisateur = utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Mot de passe réinitialisé : " + utilisateur.getLogin());
            return new UtilisateurServiceResult(true,
                    "Mot de passe réinitialisé avec succès\nMot de passe temporaire : " + motDePasseTemporaire,
                    utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur réinitialisation : " + e.getMessage());
            return new UtilisateurServiceResult(false, "Erreur serveur");
        }
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<Utilisateur> getAllUtilisateurs() {
        try {
            return utilisateurDAO.findAll();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération utilisateurs : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les utilisateurs actifs
     */
    public List<Utilisateur> getActiveUtilisateurs() {
        try {
            return utilisateurDAO.findAllActifs();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération utilisateurs actifs : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les administrateurs
     */
    public List<Utilisateur> getAllAdmins() {
        try {
            return utilisateurDAO.findAllAdmins();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération admins : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère les bibliothécaires
     */
    public List<Utilisateur> getAllBibliothecaires() {
        try {
            return utilisateurDAO.findAllBibliothecaires();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération bibliothécaires : " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Récupère un utilisateur par ID
     */
    public Utilisateur getUtilisateurById(Long userId) {
        try {
            return utilisateurDAO.findById(userId);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur récupération utilisateur : " + e.getMessage());
            return null;
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
     * Classe interne pour le résultat des opérations
     */
    public static class UtilisateurServiceResult {
        private final boolean succes;
        private final String message;
        private final Utilisateur utilisateur;

        public UtilisateurServiceResult(boolean succes, String message) {
            this(succes, message, null);
        }

        public UtilisateurServiceResult(boolean succes, String message, Utilisateur utilisateur) {
            this.succes = succes;
            this.message = message;
            this.utilisateur = utilisateur;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }

        public Utilisateur getUtilisateur() {
            return utilisateur;
        }
    }
}