package com.example.livreshome.dao;

import com.example.livreshome.model.Livre;
import com.example.livreshome.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des Livres
 * Opérations spécifiques : recherche par titre/ISBN/auteur, disponibilité, etc.
 *
 * @author ARAMA
 * @version 1.0
 */
public class LivreDAO extends AbstractDAO<Livre, Long> {

    private static final Logger LOGGER = Logger.getLogger(LivreDAO.class.getName());


    // À ajouter dans LivreDAO.java

    /**
     * Récupère les livres avec peu de stock
     */
    public List<Livre> findLowStock(int threshold) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.nombreExemplaires <= :threshold";
            return em.createQuery(query, Livre.class)
                    .setParameter("threshold", threshold)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche faible stock : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les 10 livres les plus récemment ajoutés
     */
    public List<Livre> findRecentlyAdded() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l ORDER BY l.id DESC";
            return em.createQuery(query, Livre.class)
                    .setMaxResults(10)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche récents : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Compte les livres par catégorie
     */
    public long countByCategorie(Long categorieId) {
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

    /**
     * Recherche un livre par son ISBN
     */
    public Livre findByISBN(String isbn) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.isbn = :isbn";
            return em.createQuery(query, Livre.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Livre non trouvé : ISBN " + isbn);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche ISBN : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche les livres par titre (contient)
     */
    public List<Livre> searchByTitre(String titre) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.titre LIKE :titre";
            return em.createQuery(query, Livre.class)
                    .setParameter("titre", "%" + titre + "%")
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche titre : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche les livres par auteur (contient)
     */
    public List<Livre> searchByAuteur(String auteur) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.auteur LIKE :auteur";
            return em.createQuery(query, Livre.class)
                    .setParameter("auteur", "%" + auteur + "%")
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche auteur : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche par titre ou auteur ou ISBN
     */
    public List<Livre> searchGlobal(String keyword) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE " +
                    "l.titre LIKE :keyword OR l.auteur LIKE :keyword OR l.isbn LIKE :keyword";
            return em.createQuery(query, Livre.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche globale : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les livres disponibles
     */
    public List<Livre> findDisponibles() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.disponible = true";
            return em.createQuery(query, Livre.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche disponibles : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les livres indisponibles
     */
    public List<Livre> findIndisponibles() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.disponible = false";
            return em.createQuery(query, Livre.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche indisponibles : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les livres par catégorie
     */
    public List<Livre> findByCategorie(Long categorieId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l WHERE l.categorie.id = :categorieId";
            return em.createQuery(query, Livre.class)
                    .setParameter("categorieId", categorieId)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche par catégorie : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les livres les plus empruntés
     */
    public List<Livre> findTopBorrowed(int limit) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT l FROM Livre l " +
                    "LEFT JOIN Emprunt e ON l.id = e.livre.id " +
                    "GROUP BY l.id ORDER BY COUNT(e) DESC";
            return em.createQuery(query, Livre.class)
                    .setMaxResults(limit)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche top emprunts : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Vérifie si un ISBN existe déjà
     */
    public boolean isbnExists(String isbn) {
        return findByISBN(isbn) != null;
    }
}