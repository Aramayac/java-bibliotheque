package com.example.livrehome.dao;

import com.example.livrehome.model.Categorie;
import com.example.livrehome.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les catégories
 * @author SECK
 */
public class CategorieDAO {
    
    /**
     * Sauvegarder une nouvelle catégorie
     */
    public void save(Categorie categorie) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(categorie);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de la catégorie", e);
        }
    }
    
    /**
     * Mettre à jour une catégorie existante
     */
    public void update(Categorie categorie) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(categorie);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour de la catégorie", e);
        }
    }
    
    /**
     * Supprimer une catégorie
     */
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Categorie categorie = session.get(Categorie.class, id);
            if (categorie != null) {
                session.remove(categorie);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de la catégorie", e);
        }
    }
    
    /**
     * Récupérer une catégorie par son ID
     */
    public Categorie findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Categorie.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de la catégorie", e);
        }
    }
    
    /**
     * Récupérer une catégorie par son libellé
     */
    public Categorie findByLibelle(String libelle) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Categorie> query = session.createQuery(
                "FROM Categorie WHERE libelle = :libelle", Categorie.class);
            query.setParameter("libelle", libelle);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par libellé", e);
        }
    }
    
    /**
     * Récupérer toutes les catégories
     */
    public List<Categorie> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Categorie ORDER BY libelle", Categorie.class)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des catégories", e);
        }
    }
    
    /**
     * Rechercher des catégories par mot-clé
     */
    public List<Categorie> search(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Categorie> query = session.createQuery(
                "FROM Categorie WHERE LOWER(libelle) LIKE :search " +
                "OR LOWER(description) LIKE :search ORDER BY libelle", Categorie.class);
            query.setParameter("search", "%" + searchTerm.toLowerCase() + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de catégories", e);
        }
    }
    
    /**
     * Compter le nombre total de catégories
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(c) FROM Categorie c", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du comptage des catégories", e);
        }
    }
    
    /**
     * Vérifier si une catégorie a des livres associés
     */
    public boolean hasLivres(Long categorieId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(l) FROM Livre l WHERE l.categorie.id = :categorieId", Long.class);
            query.setParameter("categorieId", categorieId);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification des livres", e);
        }
    }
    
    /**
     * Vérifier si un libellé existe déjà
     */
    public boolean libelleExists(String libelle) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM Categorie c WHERE c.libelle = :libelle", Long.class);
            query.setParameter("libelle", libelle);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification du libellé", e);
        }
    }
}
