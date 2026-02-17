package com.example.livreshome.dao;

import java.util.List;

/**
 * Interface générique DAO pour les opérations CRUD standard
 * Toutes les DAO héritent de cette interface
 *
 * @param <T> Type d'entité
 * @param <ID> Type de l'identifiant
 *
 * @author ARAMA
 * @version 1.0
 */
public interface BaseDAO<T, ID> {

    /**
     * Crée/Ajoute une nouvelle entité
     */
    T create(T entity);

    /**
     * Récupère une entité par son ID
     */
    T findById(ID id);

    /**
     * Récupère toutes les entités
     */
    List<T> findAll();

    /**
     * Met à jour une entité existante
     */
    T update(T entity);

    /**
     * Supprime une entité par son ID
     */
    boolean delete(ID id);

    /**
     * Supprime une entité directement
     */
    boolean deleteEntity(T entity);

    /**
     * Compte le nombre total d'entités
     */
    long count();
}