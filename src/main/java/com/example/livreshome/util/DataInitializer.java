package com.example.livreshome.util;

import com.example.livreshome.dao.UtilisateurDAO;
import com.example.livreshome.model.Utilisateur;
import java.util.logging.Logger;

/**
 * Initialise les données par défaut (Admin, catégories de test, etc.)
 * À appeler au démarrage de l'application
 *
 * @author ARAMA
 * @version 1.0
 */
public class DataInitializer {

    private static final Logger LOGGER = Logger.getLogger(DataInitializer.class.getName());

    /**
     * Crée l'administrateur par défaut s'il n'existe pas
     */
    public static void initializeAdminUser() {
        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

        try {
            // Vérifier si l'admin existe déjà
            if (utilisateurDAO.findByLogin("admin") != null) {
                LOGGER.info("ℹ️ Admin déjà existe");
                return;
            }

            // Créer l'admin par défaut
            Utilisateur admin = new Utilisateur(
                    "admin",
                    SecurityUtil.hashPassword("admin123"),
                    "Admin",
                    "System",
                    "admin@bibliotheque.com",
                    Utilisateur.Profil.ADMIN
            );

            utilisateurDAO.create(admin);
            LOGGER.info("✅ Administrateur créé : admin / admin123");

        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de l'initialisation admin : " + e.getMessage());
        }
    }

    /**
     * Crée des catégories de test
     */
    public static void initializeCategoriesIfNeeded() {
        // À implémenter selon les besoins
    }

    /**
     * Lance l'initialisation complète
     */
    public static void initializeAll() {
        LOGGER.info("🚀 Initialisation des données...");
        initializeAdminUser();
        initializeCategoriesIfNeeded();
        LOGGER.info("✅ Initialisation terminée");
    }
}