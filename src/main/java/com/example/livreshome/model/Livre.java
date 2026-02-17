package com.example.livreshome.model;

import javax.persistence.*;
import java.util.List;

/**
 * Entité Livre - Livres de la bibliothèque
 * Mapping : table "livre"
 *
 * @author ARAMA
 * @version 1.0
 */
@Entity
@Table(name = "livre")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "auteur", length = 200)
    private String auteur;

    @Column(name = "anneePublication")
    private Integer anneePublication;

    @Column(name = "nombreExemplaires")
    private Integer nombreExemplaires = 1;

    @Column(name = "disponible")
    private Boolean disponible = true;

    /**
     * Relation : Un livre appartient à une catégorie (ManyToOne)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    /**
     * Relation : Un livre peut avoir plusieurs emprunts (OneToMany)
     */
    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emprunt> emprunts;

    // ========== CONSTRUCTEURS ==========

    public Livre() {
    }

    public Livre(String isbn, String titre, String auteur, Integer anneePublication,
                 Integer nombreExemplaires, Categorie categorie) {
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
        this.anneePublication = anneePublication;
        this.nombreExemplaires = nombreExemplaires;
        this.categorie = categorie;
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public Integer getAnneePublication() {
        return anneePublication;
    }

    public void setAnneePublication(Integer anneePublication) {
        this.anneePublication = anneePublication;
    }

    public Integer getNombreExemplaires() {
        return nombreExemplaires;
    }

    public void setNombreExemplaires(Integer nombreExemplaires) {
        this.nombreExemplaires = nombreExemplaires;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<Emprunt> getEmprunts() {
        return emprunts;
    }

    public void setEmprunts(List<Emprunt> emprunts) {
        this.emprunts = emprunts;
    }

    @Override
    public String toString() {
        return "Livre{" +
                "id=" + id +
                ", isbn='" + isbn + '\'' +
                ", titre='" + titre + '\'' +
                ", auteur='" + auteur + '\'' +
                ", disponible=" + disponible +
                '}';
    }
}