package com.example.livrehome.dao;

import com.example.livrehome.model.Utilisateur;
import com.example.livrehome.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO pour la gestion des utilisateurs avec JPA/Hibernate
 * @author ARAMA
 */
public class UtilisateurDAO {

    public void save(Utilisateur utilisateur) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(utilisateur);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur save utilisateur", e);
        }
    }

    public void update(Utilisateur utilisateur) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(utilisateur);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur update utilisateur", e);
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Utilisateur utilisateur = session.get(Utilisateur.class, id);
            if (utilisateur != null) {
                session.remove(utilisateur);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur delete utilisateur", e);
        }
    }

    public Utilisateur findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Utilisateur.class, id);
        }
    }

    public Utilisateur findByLogin(String login) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                    "FROM Utilisateur WHERE login = :login", Utilisateur.class);
            query.setParameter("login", login);
            return query.uniqueResult();
        }
    }

    public List<Utilisateur> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Utilisateur ORDER BY nom", Utilisateur.class).list();
        }
    }

    public List<Utilisateur> findByProfil(Utilisateur.Profil profil) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Utilisateur> query = session.createQuery(
                    "FROM Utilisateur WHERE profil = :profil", Utilisateur.class);
            query.setParameter("profil", profil);
            return query.list();
        }
    }

    public boolean loginExists(String login) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(u) FROM Utilisateur u WHERE u.login = :login", Long.class);
            query.setParameter("login", login);
            return query.uniqueResult() > 0;
        }
    }

    public void activerDesactiver(Long id, boolean actif) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Utilisateur utilisateur = session.get(Utilisateur.class, id);
            if (utilisateur != null) {
                utilisateur.setActif(actif);
                session.merge(utilisateur);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur activation/désactivation", e);
        }
    }
}
