package com.example.livreshome.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.logging.Logger;

/**
 * Utilitaire pour gérer la connexion Hibernate/JPA
 * Pattern Singleton pour une seule instance EntityManagerFactory
 *
 * @author ARAMA
 * @version 1.0
 */
public class HibernateUtil {
    private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());

    private static EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("bibliothequePU");
            LOGGER.info("✅ EntityManagerFactory créée avec succès");
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la création de l'EntityManagerFactory");
            e.printStackTrace();
            throw new ExceptionInInitializerError("Impossible de créer l'EntityManagerFactory");
        }
    }

    /**
     * Retourne une nouvelle instance EntityManager
     *
     * @return EntityManager pour les opérations JPA
     */
    public static EntityManager getEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory n'est pas initialisée");
        }
        return emf.createEntityManager();
    }

    /**
     * Ferme l'EntityManagerFactory et libère les ressources
     * À appeler lors de l'arrêt de l'application
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOGGER.info("✅ EntityManagerFactory fermée");
        }
    }

    /**
     * Retourne l'EntityManagerFactory (usage avancé)
     *
     * @return EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    /**
     * Teste la connexion à la BD
     * À utiliser pour validation au démarrage
     */
    public static boolean testConnection() {
        try {
            EntityManager em = getEntityManager();
            em.createNativeQuery("SELECT 1").getSingleResult();
            em.close();
            LOGGER.info("✅ Connexion BD validée");
            return true;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur connexion BD: " + e.getMessage());
            return false;
        }
    }
}