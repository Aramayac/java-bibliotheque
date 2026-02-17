package com.example.livrehome.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant une catégorie de livres
 * @author SECK
 */
@Entity
@Table(name = "categorie")
public class Categorie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Relation OneToMany avec Livre
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Livre> livres = new ArrayList<>();
    
    // Constructeurs
    public Categorie() {
    }
    
    public Categorie(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }
    
    // Getters et Setters
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
    
    // Méthodes utilitaires
    public int getNombreLivres() {
        return livres != null ? livres.size() : 0;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
}
