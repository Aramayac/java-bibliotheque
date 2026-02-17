package com.example.livrehome.dao;

import com.example.livrehome.model.Livre;
import com.example.livrehome.model.Categorie;
import com.example.livrehome.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

/**
 * DAO pour la gestion des livres avec JPA/Hibernate
 * @author KA
 */
public class LivreDAO {

    public void save(Livre livre) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(livre);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur save livre", e);
        }
    }

    public void update(Livre livre) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(livre);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur update livre", e);
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Livre livre = session.get(Livre.class, id);
            if (livre != null) {
                session.remove(livre);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Erreur delete livre", e);
        }
    }

    public Livre findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Livre.class, id);
        }
    }

    public Livre findByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Livre> query = session.createQuery(
                    "FROM Livre WHERE isbn = :isbn", Livre.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        }
    }

    public List<Livre> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Livre ORDER BY titre", Livre.class).list();
        }
    }

    public List<Livre> search(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Livre> query = session.createQuery(
                    "FROM Livre WHERE LOWER(titre) LIKE :search " +
                            "OR LOWER(auteur) LIKE :search " +
                            "OR LOWER(isbn) LIKE :search " +
                            "ORDER BY titre", Livre.class);
            query.setParameter("search", "%" + searchTerm.toLowerCase() + "%");
            return query.list();
        }
    }

    public List<Livre> findByCategorie(Long categorieId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Livre> query = session.createQuery(
                    "FROM Livre WHERE categorie.id = :categorieId", Livre.class);
            query.setParameter("categorieId", categorieId);
            return query.list();
        }
    }

    public List<Livre> findDisponibles() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Livre> query = session.createQuery(
                    "FROM Livre WHERE exemplairesDisponibles > 0", Livre.class);
            return query.list();
        }
    }

    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(l) FROM Livre l", Long.class);
            return query.uniqueResult();
        }
    }

    public boolean isbnExists(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(l) FROM Livre l WHERE l.isbn = :isbn", Long.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult() > 0;
        }
    }
}
