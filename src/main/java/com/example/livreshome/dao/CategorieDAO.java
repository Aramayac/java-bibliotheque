package com.example.livreshome.dao;

import com.example.livreshome.model.Categorie;
import com.example.livreshome.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des Catégories
 * Opérations spécifiques : recherche par libellé, etc.
 *
 * @author SECK
 * @version 1.0
 */
public class CategorieDAO extends AbstractDAO<Categorie, Long> {

    private static final Logger LOGGER = Logger.getLogger(CategorieDAO.class.getName());

    /**
     * Recherche une catégorie par son libellé
     */
    public Categorie findByLibelle(String libelle) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT c FROM Categorie c WHERE c.libelle = :libelle";
            return em.createQuery(query, Categorie.class)
                    .setParameter("libelle", libelle)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Catégorie non trouvée : " + libelle);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche catégorie : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche les catégories contenant un texte dans le libellé
     */
    public List<Categorie> searchByLibelle(String keyword) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT c FROM Categorie c WHERE c.libelle LIKE :keyword";
            return em.createQuery(query, Categorie.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Vérifie si une catégorie existe déjà
     */
    public boolean libelleExists(String libelle) {
        return findByLibelle(libelle) != null;
    }

    /**
     * Compte le nombre de livres dans une catégorie
     */
    public long countLivresInCategorie(Long categorieId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT COUNT(l) FROM Livre l WHERE l.categorie.id = :categorieId";
            Long count = em.createQuery(query, Long.class)
                    .setParameter("categorieId", categorieId)
                    .getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage : " + e.getMessage());
            throw new RuntimeException("Erreur lors du comptage", e);
        } finally {
            em.close();
        }
    }
}