package com.example.livreshome.dao;

import com.example.livreshome.model.Emprunt;
import com.example.livreshome.util.HibernateUtil;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des Emprunts
 * Opérations spécifiques : recherche emprunts en cours, retards, calcul pénalités, etc.
 *
 * @author MBAYE
 * @version 1.0
 */
public class EmpruntDAO extends AbstractDAO<Emprunt, Long> {

    private static final Logger LOGGER = Logger.getLogger(EmpruntDAO.class.getName());

    /**
     * Récupère tous les emprunts actuels (non retournés)
     */
    public List<Emprunt> findEmpruntsActuels() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.dateRetourEffective IS NULL " +
                    "ORDER BY e.dateRetourPrevue ASC";
            return em.createQuery(query, Emprunt.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts actuels : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère tous les emprunts en retard
     */
    public List<Emprunt> findEmpruntsEnRetard() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.dateRetourEffective IS NULL " +
                    "AND e.dateRetourPrevue < CURRENT_DATE " +
                    "ORDER BY e.dateRetourPrevue ASC";
            return em.createQuery(query, Emprunt.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts en retard : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère tous les emprunts retournés
     */
    public List<Emprunt> findEmpruntsRetournes() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.dateRetourEffective IS NOT NULL";
            return em.createQuery(query, Emprunt.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts retournés : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les emprunts d'un adhérent
     */
    public List<Emprunt> findByAdherent(Long adherentId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.adherent.id = :adherentId " +
                    "ORDER BY e.dateEmprunt DESC";
            return em.createQuery(query, Emprunt.class)
                    .setParameter("adherentId", adherentId)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts adhérent : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les emprunts actuels d'un adhérent
     */
    public List<Emprunt> findEmpruntsActuelsByAdherent(Long adherentId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.adherent.id = :adherentId " +
                    "AND e.dateRetourEffective IS NULL ORDER BY e.dateRetourPrevue ASC";
            return em.createQuery(query, Emprunt.class)
                    .setParameter("adherentId", adherentId)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts actuels : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère un emprunt spécifique d'un adhérent pour un livre
     */
    public Emprunt findEmpruntActuelByAdherentAndLivre(Long adherentId, Long livreId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE e.adherent.id = :adherentId " +
                    "AND e.livre.id = :livreId AND e.dateRetourEffective IS NULL";
            List<Emprunt> results = em.createQuery(query, Emprunt.class)
                    .setParameter("adherentId", adherentId)
                    .setParameter("livreId", livreId)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunt : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les emprunts du mois courant
     */
    public List<Emprunt> findEmpruntsOfMonth(int year, int month) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM Emprunt e WHERE YEAR(e.dateEmprunt) = :year " +
                    "AND MONTH(e.dateEmprunt) = :month ORDER BY e.dateEmprunt DESC";
            return em.createQuery(query, Emprunt.class)
                    .setParameter("year", year)
                    .setParameter("month", month)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche emprunts mois : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Calcule la pénalité totale pour un adhérent
     */
    public Double calculateTotalPenalite(Long adherentId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT SUM(e.penalite) FROM Emprunt e WHERE e.adherent.id = :adherentId";
            Double total = em.createQuery(query, Double.class)
                    .setParameter("adherentId", adherentId)
                    .getSingleResult();
            return total != null ? total : 0.0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur calcul pénalité : " + e.getMessage());
            throw new RuntimeException("Erreur lors du calcul", e);
        } finally {
            em.close();
        }
    }

    /**
     * Compte les emprunts en retard
     */
    public long countEmpruntsEnRetard() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetourEffective IS NULL " +
                    "AND e.dateRetourPrevue < CURRENT_DATE";
            Long count = em.createQuery(query, Long.class).getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage : " + e.getMessage());
            throw new RuntimeException("Erreur lors du comptage", e);
        } finally {
            em.close();
        }
    }

    /**
     * Compte les emprunts actuels
     */
    public long countEmpruntsActuels() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetourEffective IS NULL";
            Long count = em.createQuery(query, Long.class).getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur comptage : " + e.getMessage());
            throw new RuntimeException("Erreur lors du comptage", e);
        } finally {
            em.close();
        }
    }
}