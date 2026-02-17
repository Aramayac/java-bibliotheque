package com.example.livreshome.dao;

import com.example.livreshome.model.Adherent;
import com.example.livreshome.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des Adhérents
 * Opérations spécifiques : recherche, activation/désactivation, etc.
 *
 * @author SECK
 * @version 1.0
 */
public class AdherentDAO extends AbstractDAO<Adherent, Long> {

    private static final Logger LOGGER = Logger.getLogger(AdherentDAO.class.getName());

    /**
     * Recherche un adhérent par son matricule
     */
    public Adherent findByMatricule(String matricule) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE a.matricule = :matricule";
            return em.createQuery(query, Adherent.class)
                    .setParameter("matricule", matricule)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Adhérent non trouvé : " + matricule);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche matricule : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche un adhérent par email
     */
    public Adherent findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE a.email = :email";
            return em.createQuery(query, Adherent.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Adhérent non trouvé : " + email);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche email : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche par nom ou prénom
     */
    public List<Adherent> searchByNomOrPrenom(String keyword) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE " +
                    "a.nom LIKE :keyword OR a.prenom LIKE :keyword";
            return em.createQuery(query, Adherent.class)
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
     * Recherche globale (matricule, nom, prénom, email)
     */
    public List<Adherent> searchGlobal(String keyword) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE " +
                    "a.matricule LIKE :keyword OR a.nom LIKE :keyword OR " +
                    "a.prenom LIKE :keyword OR a.email LIKE :keyword";
            return em.createQuery(query, Adherent.class)
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
     * Récupère les adhérents actifs
     */
    public List<Adherent> findActifs() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE a.actif = true";
            return em.createQuery(query, Adherent.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche actifs : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les adhérents inactifs
     */
    public List<Adherent> findInactifs() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT a FROM Adherent a WHERE a.actif = false";
            return em.createQuery(query, Adherent.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche inactifs : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Désactive un adhérent
     */
    public void deactivate(Long adherentId) {
        Adherent adherent = findById(adherentId);
        if (adherent != null) {
            adherent.setActif(false);
            update(adherent);
            LOGGER.info("✅ Adhérent désactivé : " + adherent.getMatricule());
        }
    }

    /**
     * Active un adhérent
     */
    public void activate(Long adherentId) {
        Adherent adherent = findById(adherentId);
        if (adherent != null) {
            adherent.setActif(true);
            update(adherent);
            LOGGER.info("✅ Adhérent activé : " + adherent.getMatricule());
        }
    }

    /**
     * Vérifie si un matricule existe déjà
     */
    public boolean matriculeExists(String matricule) {
        return findByMatricule(matricule) != null;
    }

    /**
     * Compte les emprunts actuels d'un adhérent
     */
    public long countEmpruntsActuels(Long adherentId) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM Emprunt e " +
                    "WHERE e.adherent.id = :adherentId AND e.dateRetourEffective IS NULL";
            Long count = em.createQuery(query, Long.class)
                    .setParameter("adherentId", adherentId)
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