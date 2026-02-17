package com.example.livrehome.dao;

import com.example.livrehome.model.Adherent;
import com.example.livrehome.model.Emprunt;
import com.example.livrehome.Util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.naming.Referenceable;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations CRUD sur les adhérents
 * @author SECK
 */
public class AdherentDAO {
    
    /**
     * Sauvegarder un nouvel adhérent
     */
    public void save(Adherent adherent) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Générer automatiquement un matricule si non fourni
            if (adherent.getMatricule() == null || adherent.getMatricule().isEmpty()) {
                adherent.setMatricule(genererMatricule());
            }
            
            // Définir la date d'inscription si non définie
            if (adherent.getDateInscription() == null) {
                adherent.setDateInscription(LocalDate.now());
            }
            
            session.persist(adherent);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de l'adhérent", e);
        }
    }
    
    /**
     * Mettre à jour un adhérent existant
     */
    public void update(Adherent adherent) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(adherent);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la mise à jour de l'adhérent", e);
        }
    }
    
    /**
     * Supprimer un adhérent
     */
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Adherent adherent = session.get(Adherent.class, id);
            if (adherent != null) {
                session.remove(adherent);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suppression de l'adhérent", e);
        }
    }
    
    /**
     * Récupérer un adhérent par son ID
     */
    public Adherent findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Adherent.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche de l'adhérent", e);
        }
    }
    
    /**
     * Récupérer un adhérent par son matricule
     */
    public Adherent findByMatricule(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Adherent> query = session.createQuery(
                "FROM Adherent WHERE matricule = :matricule", Adherent.class);
            query.setParameter("matricule", matricule);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche par matricule", e);
        }
    }
    
    /**
     * Récupérer tous les adhérents
     */
    public List<Adherent> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Adherent ORDER BY dateInscription DESC", Adherent.class)
                    .list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des adhérents", e);
        }
    }
    
    /**
     * Récupérer tous les adhérents actifs
     */
    public List<Adherent> findAllActifs() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Adherent> query = session.createQuery(
                "FROM Adherent WHERE actif = true ORDER BY dateInscription DESC", Adherent.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des adhérents actifs", e);
        }
    }
    
    /**
     * Rechercher des adhérents par nom ou prénom
     */
    public List<Adherent> search(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Adherent> query = session.createQuery(
                "FROM Adherent WHERE LOWER(nom) LIKE :search " +
                "OR LOWER(prenom) LIKE :search " +
                "OR LOWER(matricule) LIKE :search " +
                "OR LOWER(email) LIKE :search " +
                "ORDER BY nom, prenom", Adherent.class);
            query.setParameter("search", "%" + searchTerm.toLowerCase() + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche d'adhérents", e);
        }
    }
    
    /**
     * Récupérer l'historique des emprunts d'un adhérent
     */
    public List<Emprunt> getHistoriqueEmprunts(Long adherentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                "FROM Emprunt WHERE adherent.id = :adherentId ORDER BY dateEmprunt DESC", 
                Emprunt.class);
            query.setParameter("adherentId", adherentId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de l'historique", e);
        }
    }
    
    /**
     * Récupérer les emprunts en cours d'un adhérent
     */
    public List<Emprunt> getEmpruntsEnCours(Long adherentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Emprunt> query = session.createQuery(
                "FROM Emprunt WHERE adherent.id = :adherentId " +
                "AND dateRetourEffective IS NULL ORDER BY dateEmprunt DESC", 
                Emprunt.class);
            query.setParameter("adherentId", adherentId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des emprunts en cours", e);
        }
    }
    
    /**
     * Vérifier si un adhérent a des emprunts en retard
     */
    public boolean hasEmpruntsEnRetard(Long adherentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(e) FROM Emprunt e WHERE e.adherent.id = :adherentId " +
                "AND e.dateRetourEffective IS NULL " +
                "AND e.dateRetourPrevue < :today", Long.class);
            query.setParameter("adherentId", adherentId);
            query.setParameter("today", LocalDate.now());
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification des retards", e);
        }
    }
    
    /**
     * Suspendre un adhérent
     */
    public void suspendre(Long adherentId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Adherent adherent = session.get(Adherent.class, adherentId);
            if (adherent != null) {
                adherent.setActif(false);
                session.merge(adherent);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la suspension de l'adhérent", e);
        }
    }
    
    /**
     * Réactiver un adhérent
     */
    public void reactiver(Long adherentId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Adherent adherent = session.get(Adherent.class, adherentId);
            if (adherent != null) {
                adherent.setActif(true);
                session.merge(adherent);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la réactivation de l'adhérent", e);
        }
    }
    
    /**
     * Compter le nombre total d'adhérents
     */
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(a) FROM Adherent a", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du comptage des adhérents", e);
        }
    }
    
    /**
     * Compter le nombre d'adhérents actifs
     */
    public long countActifs() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(a) FROM Adherent a WHERE a.actif = true", Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du comptage des adhérents actifs", e);
        }
    }
    
    /**
     * Vérifier si un email existe déjà
     */
    public boolean emailExists(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(a) FROM Adherent a WHERE a.email = :email", Long.class);
            query.setParameter("email", email);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'email", e);
        }
    }
    
    /**
     * Vérifier si un matricule existe déjà
     */
    public boolean matriculeExists(String matricule) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(a) FROM Adherent a WHERE a.matricule = :matricule", Long.class);
            query.setParameter("matricule", matricule);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification du matricule", e);
        }
    }
    
    /**
     * Générer un matricule unique au format ADH + année + numéro séquentiel
     */
    private String genererMatricule() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            int annee = LocalDate.now().getYear();
            
            // Récupérer le dernier numéro de l'année en cours
            Query<String> query = session.createQuery(
                "SELECT a.matricule FROM Adherent a WHERE a.matricule LIKE :pattern " +
                "ORDER BY a.matricule DESC", String.class);
            query.setParameter("pattern", "ADH" + annee + "%");
            query.setMaxResults(1);
            
            String dernierMatricule = query.uniqueResult();
            
            int numero = 1;
            if (dernierMatricule != null) {
                // Extraire le numéro et incrémenter
                String numStr = dernierMatricule.substring(7); // ADH2026001 -> 001
                numero = Integer.parseInt(numStr) + 1;
            }
            
            return String.format("ADH%d%03d", annee, numero);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du matricule", e);
        }
    }
    
    /**
     * Récupérer les nouveaux adhérents du mois en cours
     */
    public List<Adherent> getNouveauxAdherentsDuMois() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
            LocalDate finMois = LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth());
            
            Query<Adherent> query = session.createQuery(
                "FROM Adherent WHERE dateInscription BETWEEN :debut AND :fin " +
                "ORDER BY dateInscription DESC", Adherent.class);
            query.setParameter("debut", debutMois);
            query.setParameter("fin", finMois);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des nouveaux adhérents", e);
        }
    }
}
