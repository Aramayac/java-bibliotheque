package com.example.livrehome.dao;

import com.example.livrehome.model.Emprunt;
import com.example.livrehome.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO pour la gestion des emprunts avec JPA/Hibernate
 * @author MBAYE
 */
public class EmpruntDAO {

    public void save(Emprunt emprunt) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(emprunt);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur save emprunt", e);
        }
    }

    public void update(Emprunt emprunt) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(emprunt);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur update emprunt", e);
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Emprunt emprunt = session.get(Emprunt.class, id);
            if (emprunt != null) {
                session.remove(emprunt);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur delete emprunt", e);
        }
    }

    public Emprunt findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Emprunt.class, id);
        }
    }

    public List<Emprunt> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Emprunt ORDER BY dateEmprunt DESC", Emprunt.class).list();
        }
    }

    public List<Emprunt> findEnCours() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE statut = 'EN_COURS' OR statut = 'EN_RETARD' ORDER BY dateEmprunt DESC",
                    Emprunt.class);
            return query.list();
        }
    }

    public List<Emprunt> findEnRetard() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE dateRetourEffective IS NULL " +
                            "AND dateRetourPrevue < :today ORDER BY dateRetourPrevue",
                    Emprunt.class);
            query.setParameter("today", LocalDate.now());
            return query.list();
        }
    }

    public List<Emprunt> findByAdherent(Long adherentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE adherent.id = :adherentId ORDER BY dateEmprunt DESC",
                    Emprunt.class);
            query.setParameter("adherentId", adherentId);
            return query.list();
        }
    }

    public List<Emprunt> findByLivre(Long livreId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE livre.id = :livreId ORDER BY dateEmprunt DESC",
                    Emprunt.class);
            query.setParameter("livreId", livreId);
            return query.list();
        }
    }

    public List<Emprunt> findEmpruntsEnCoursByAdherent(Long adherentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE adherent.id = :adherentId " +
                            "AND dateRetourEffective IS NULL",
                    Emprunt.class);
            query.setParameter("adherentId", adherentId);
            return query.list();
        }
    }

    public List<Emprunt> findEmpruntsOfMonth(int year, int month) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                    "FROM Emprunt WHERE YEAR(dateEmprunt) = :year " +
                            "AND MONTH(dateEmprunt) = :month",
                    Emprunt.class);
            query.setParameter("year", year);
            query.setParameter("month", month);
            return query.list();
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(e) FROM Emprunt e", Long.class);
            return query.uniqueResult();
        }
    }

    public long countEnCours() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(e) FROM Emprunt e WHERE e.dateRetourEffective IS NULL", Long.class);
            return query.uniqueResult();
        }
    }

    public void mettreAJourStatutsEtPenalites() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            List<Emprunt> empruntsEnCours = session.createQuery(
                    "FROM Emprunt WHERE dateRetourEffective IS NULL", Emprunt.class).list();

            for (Emprunt emprunt : empruntsEnCours) {
                if (emprunt.estEnRetard()) {
                    emprunt.calculerPenalite();
                    session.merge(emprunt);
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur mise à jour statuts", e);
        }
    }
}
