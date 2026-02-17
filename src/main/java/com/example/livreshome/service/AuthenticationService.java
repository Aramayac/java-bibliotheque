package com.example.livreshome.service;

import com.example.livreshome.dao.UtilisateurDAO;
import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.util.SecurityUtil;
import com.example.livreshome.util.SessionManager;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Service d'authentification
 * Gère la connexion, la validation des identifiants et la gestion de session
 *
 * @author ARAMA
 * @version 1.0
 */
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final UtilisateurDAO utilisateurDAO;

    public AuthenticationService() {
        this.utilisateurDAO = new UtilisateurDAO();
    }

    /**
     * Authentifie un utilisateur avec login et mot de passe
     *
     * @param login Login de l'utilisateur
     * @param motDePasse Mot de passe en clair
     * @return Objet AuthenticationResult contenant le résultat et l'utilisateur
     */
    public AuthenticationResult authenticate(String login, String motDePasse) {
        // Validation des paramètres
        if (login == null || login.isEmpty() || motDePasse == null || motDePasse.isEmpty()) {
            LOGGER.warning("⚠️ Tentative de connexion avec paramètres vides");
            return new AuthenticationResult(false, "Veuillez entrer login et mot de passe");
        }

        try {
            // Rechercher l'utilisateur
            Utilisateur utilisateur = utilisateurDAO.findByLogin(login);

            if (utilisateur == null) {
                LOGGER.warning("⚠️ Tentative de connexion avec login inexistant : " + login);
                return new AuthenticationResult(false, "Login ou mot de passe incorrect");
            }

            // Vérifier si l'utilisateur est actif
            if (!utilisateur.getActif()) {
                LOGGER.warning("⚠️ Tentative de connexion avec compte inactif : " + login);
                return new AuthenticationResult(false, "Ce compte a été désactivé");
            }

            // Valider le mot de passe
            if (!SecurityUtil.validatePassword(motDePasse, utilisateur.getMotDePasse())) {
                LOGGER.warning("⚠️ Mot de passe incorrect pour : " + login);
                return new AuthenticationResult(false, "Login ou mot de passe incorrect");
            }

            // Mise à jour de la dernière connexion
            utilisateur.setDerniereConnexion(LocalDateTime.now());
            utilisateurDAO.update(utilisateur);

            // Créer la session
            SessionManager.getInstance().setUtilisateurConnecte(utilisateur);

            LOGGER.info("✅ Authentification réussie pour : " + login);
            return new AuthenticationResult(true, "Authentification réussie", utilisateur);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de l'authentification : " + e.getMessage());
            return new AuthenticationResult(false, "Erreur serveur lors de l'authentification");
        }
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        SessionManager sessionManager = SessionManager.getInstance();
        if (sessionManager.isConnecte()) {
            String nomUtilisateur = sessionManager.getUtilisateurConnecte().getNomComplet();
            sessionManager.closeSession();
            LOGGER.info("✅ Déconnexion réussie pour : " + nomUtilisateur);
        }
    }

    /**
     * Change le mot de passe d'un utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param ancienMotDePasse Ancien mot de passe en clair
     * @param nouveauMotDePasse Nouveau mot de passe en clair
     * @return Objet ChangePasswordResult contenant le résultat
     */
    public ChangePasswordResult changePassword(Long userId, String ancienMotDePasse, String nouveauMotDePasse) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new ChangePasswordResult(false, "Utilisateur non trouvé");
            }

            // Valider l'ancien mot de passe
            if (!SecurityUtil.validatePassword(ancienMotDePasse, utilisateur.getMotDePasse())) {
                return new ChangePasswordResult(false, "Ancien mot de passe incorrect");
            }

            // Valider la force du nouveau mot de passe
            SecurityUtil.PasswordStrength strength = SecurityUtil.checkPasswordStrength(nouveauMotDePasse);
            if (strength.getNiveau() == SecurityUtil.PasswordStrength.Niveau.TRES_FAIBLE ||
                    strength.getNiveau() == SecurityUtil.PasswordStrength.Niveau.FAIBLE) {
                return new ChangePasswordResult(false, "Le mot de passe n'est pas assez robuste\n" + strength.getDetails());
            }

            // Hasher et mettre à jour
            utilisateur.setMotDePasse(SecurityUtil.hashPassword(nouveauMotDePasse));
            utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Mot de passe changé pour : " + utilisateur.getLogin());
            return new ChangePasswordResult(true, "Mot de passe changé avec succès");

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors du changement de mot de passe : " + e.getMessage());
            return new ChangePasswordResult(false, "Erreur serveur");
        }
    }

    /**
     * Réinitialise le mot de passe d'un utilisateur (Admin only)
     *
     * @param userId ID de l'utilisateur
     * @return Objet ResetPasswordResult contenant le nouveau mot de passe temporaire
     */
    public ResetPasswordResult resetPassword(Long userId) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findById(userId);

            if (utilisateur == null) {
                return new ResetPasswordResult(false, "Utilisateur non trouvé", null);
            }

            // Générer un mot de passe temporaire
            String motDePasseTemporaire = SecurityUtil.generateTemporaryPassword();
            utilisateur.setMotDePasse(SecurityUtil.hashPassword(motDePasseTemporaire));
            utilisateurDAO.update(utilisateur);

            LOGGER.info("✅ Mot de passe réinitialisé pour : " + utilisateur.getLogin());
            return new ResetPasswordResult(true, "Mot de passe réinitialisé avec succès", motDePasseTemporaire);

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la réinitialisation : " + e.getMessage());
            return new ResetPasswordResult(false, "Erreur serveur", null);
        }
    }

    /**
     * Classe interne pour le résultat de l'authentification
     */
    public static class AuthenticationResult {
        private final boolean succes;
        private final String message;
        private final Utilisateur utilisateur;

        public AuthenticationResult(boolean succes, String message) {
            this(succes, message, null);
        }

        public AuthenticationResult(boolean succes, String message, Utilisateur utilisateur) {
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

    /**
     * Classe interne pour le résultat du changement de mot de passe
     */
    public static class ChangePasswordResult {
        private final boolean succes;
        private final String message;

        public ChangePasswordResult(boolean succes, String message) {
            this.succes = succes;
            this.message = message;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Classe interne pour le résultat de la réinitialisation de mot de passe
     */
    public static class ResetPasswordResult {
        private final boolean succes;
        private final String message;
        private final String motDePasseTemporaire;

        public ResetPasswordResult(boolean succes, String message, String motDePasseTemporaire) {
            this.succes = succes;
            this.message = message;
            this.motDePasseTemporaire = motDePasseTemporaire;
        }

        public boolean isSucces() {
            return succes;
        }

        public String getMessage() {
            return message;
        }

        public String getMotDePasseTemporaire() {
            return motDePasseTemporaire;
        }
    }
}