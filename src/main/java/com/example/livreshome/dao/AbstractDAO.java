package com.example.livreshome.dao;

import com.example.livreshome.util.HibernateUtil;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Logger;

/**
 * Classe abstraite implémentant les opérations CRUD génériques
 * À étendre par les DAO concrets
 *
 * @param <T> Type d'entité
 * @param <ID> Type de l'identifiant
 *
 * @author ARAMA
 * @version 1.0
 */
public abstract class AbstractDAO<T, ID> implements BaseDAO<T, ID> {

    protected static final Logger LOGGER = Logger.getLogger(AbstractDAO.class.getName());

    protected Class<T> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractDAO() {
        // Récupère la classe générique au runtime
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];
    }

    /**
     * Crée une nouvelle entité dans la BD
     */
    @Override
    public T create(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(entity);
            transaction.commit();
            LOGGER.info("✅ Entité créée : " + entityClass.getSimpleName());
            return entity;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("❌ Erreur création : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère une entité par son ID
     */
    @Override
    public T findById(ID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            T entity = em.find(entityClass, id);
            if (entity == null) {
                LOGGER.warning("⚠️ Aucune entité trouvée avec l'ID : " + id);
            }
            return entity;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la recherche par ID : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche", e);
        } finally {
            em.close();
        }
    }

    /**
     * Récupère toutes les entités
     */
    @Override
    public List<T> findAll() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            List<T> resultList = em.createQuery(query, entityClass).getResultList();
            LOGGER.info("✅ " + resultList.size() + " entités récupérées");
            return resultList;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors de la récupération : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération", e);
        } finally {
            em.close();
        }
    }

    /**
     * Met à jour une entité existante
     */
    @Override
    public T update(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            T merged = em.merge(entity);
            transaction.commit();
            LOGGER.info("✅ Entité mise à jour : " + entityClass.getSimpleName());
            return merged;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("❌ Erreur mise à jour : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour", e);
        } finally {
            em.close();
        }
    }

    /**
     * Supprime une entité par son ID
     */
    @Override
    public boolean delete(ID id) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                transaction.commit();
                LOGGER.info("✅ Entité supprimée : " + entityClass.getSimpleName());
                return true;
            } else {
                transaction.commit();
                LOGGER.warning("⚠️ Entité non trouvée pour suppression");
                return false;
            }
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("❌ Erreur suppression : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression", e);
        } finally {
            em.close();
        }
    }

    /**
     * Supprime directement une entité
     */
    @Override
    public boolean deleteEntity(T entity) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            T merged = em.merge(entity);
            em.remove(merged);
            transaction.commit();
            LOGGER.info("✅ Entité supprimée : " + entityClass.getSimpleName());
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            LOGGER.severe("❌ Erreur suppression : " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression", e);
        } finally {
            em.close();
        }
    }

    /**
     * Compte le nombre total d'entités
     */
    @Override
    public long count() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            String query = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            Long count = em.createQuery(query, Long.class).getSingleResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            LOGGER.severe("❌ Erreur lors du comptage : " + e.getMessage());
            throw new RuntimeException("Erreur lors du comptage", e);
        } finally {
            em.close();
        }
    }
}