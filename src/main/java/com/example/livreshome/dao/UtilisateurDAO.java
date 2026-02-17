package com.example.livreshome.dao;

import com.example.livreshome.model.Utilisateur;
import com.example.livreshome.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.logging.Logger;

/**
 * DAO pour la gestion des Utilisateurs
 * Opérations spécifiques : recherche par login, activation/désactivation, etc.
 *
 * @author ARAMA
 * @version 1.0
 */
public class UtilisateurDAO extends AbstractDAO<Utilisateur, Long> {

    private static final Logger LOGGER = Logger.getLogger(UtilisateurDAO.class.getName());

    /**
     * Recherche un utilisateur par son login
     * Utilisé pour l'authentification
     */
    public Utilisateur findByLogin(String login) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT u FROM Utilisateur u WHERE u.login = :login";
            return em.createQuery(query, Utilisateur.class)
                    .setParameter("login", login)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Utilisateur non trouvé : " + login);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche login : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Recherche un utilisateur par email
     */
    public Utilisateur findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT u FROM Utilisateur u WHERE u.email = :email";
            return em.createQuery(query, Utilisateur.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("⚠️ Utilisateur non trouvé : " + email);
            return null;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche email : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère tous les administrateurs
     */
    public List<Utilisateur> findAllAdmins() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT u FROM Utilisateur u WHERE u.profil = :profil";
            return em.createQuery(query, Utilisateur.class)
                    .setParameter("profil", Utilisateur.Profil.ADMIN)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche admins : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère tous les bibliothécaires
     */
    public List<Utilisateur> findAllBibliothecaires() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT u FROM Utilisateur u WHERE u.profil = :profil";
            return em.createQuery(query, Utilisateur.class)
                    .setParameter("profil", Utilisateur.Profil.BIBLIOTHECAIRE)
                    .getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche bibliothécaires : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère les utilisateurs actifs
     */
    public List<Utilisateur> findAllActifs() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT u FROM Utilisateur u WHERE u.actif = true";
            return em.createQuery(query, Utilisateur.class).getResultList();
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur recherche actifs : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Désactive un utilisateur
     */
    public void deactivateUser(Long userId) {
        Utilisateur user = findById(userId);
        if (user != null) {
            user.setActif(false);
            update(user);
            LOGGER.info("✅ Utilisateur désactivé : " + user.getLogin());
        }
    }

    /**
     * Active un utilisateur
     */
    public void activateUser(Long userId) {
        Utilisateur user = findById(userId);
        if (user != null) {
            user.setActif(true);
            update(user);
            LOGGER.info("✅ Utilisateur activé : " + user.getLogin());
        }
    }

    /**
     * Vérifie si un login existe déjà
     */
    public boolean loginExists(String login) {
        return findByLogin(login) != null;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }
}