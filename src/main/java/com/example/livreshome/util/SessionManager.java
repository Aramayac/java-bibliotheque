package com.example.livreshome.util;

import com.example.livreshome.model.Utilisateur;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Gestionnaire de session utilisateur (Singleton)
 * Stocke l'utilisateur actuellement connecté
 *
 * @author ARAMA
 * @version 1.0
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static SessionManager instance;

    private Utilisateur utilisateurConnecte;
    private LocalDateTime heureConnexion;

    /**
     * Constructeur privé (Singleton)
     */
    private SessionManager() {
    }

    /**
     * Retourne l'instance unique de SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Établit une session utilisateur
     */
    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.heureConnexion = LocalDateTime.now();
        LOGGER.info("✅ Session démarrée pour : " + utilisateur.getNomComplet());
    }

    /**
     * Retourne l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isConnecte() {
        return utilisateurConnecte != null;
    }

    /**
     * Retourne l'heure de connexion
     */
    public LocalDateTime getHeureConnexion() {
        return heureConnexion;
    }

    /**
     * Ferme la session
     */
    public void closeSession() {
        if (utilisateurConnecte != null) {
            LOGGER.info("❌ Session fermée pour : " + utilisateurConnecte.getNomComplet());
            utilisateurConnecte = null;
            heureConnexion = null;
        }
    }

    /**
     * Retourne les informations de session
     */
    public String getSessionInfo() {
        if (utilisateurConnecte == null) {
            return "Aucune session active";
        }
        return utilisateurConnecte.getNomComplet() + " (" + utilisateurConnecte.getProfil().getLabel() + ")";
    }
}