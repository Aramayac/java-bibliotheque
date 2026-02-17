package com.example.livreshome.model;

import javax.persistence.*;
import java.util.List;

/**
 * Entité Categorie - Catégories des livres
 * Mapping : table "categorie"
 *
 * @author SECK
 * @version 1.0
 */
@Entity
@Table(name = "categorie")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Relation : Une catégorie a plusieurs livres (OneToMany)
     * mappedBy = nom de l'attribut "categorie" dans Livre
     */
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Livre> livres;

    // ========== CONSTRUCTEURS ==========

    public Categorie() {
    }

    public Categorie(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Livre> getLivres() {
        return livres;
    }

    public void setLivres(List<Livre> livres) {
        this.livres = livres;
    }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                '}';
    }
}