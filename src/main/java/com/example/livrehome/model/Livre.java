package com.example.livrehome.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant un livre
 * @author KA
 */
@Entity
@Table(name = "livre")
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "auteur", length = 200)
    private String auteur;

    @Column(name = "annee_publication")
    private Integer anneePublication;

    @Column(name = "nombre_exemplaires")
    private Integer nombreExemplaires = 1;

    @Column(name = "exemplaires_disponibles")
    private Integer exemplairesDisponibles = 1;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout;

    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Emprunt> emprunts = new ArrayList<>();

    public Livre() {
        this.dateAjout = LocalDateTime.now();
    }

    public Livre(String isbn, String titre, String auteur, Integer anneePublication, Categorie categorie) {
        this();
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
        this.anneePublication = anneePublication;
        this.categorie = categorie;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public Integer getAnneePublication() { return anneePublication; }
    public void setAnneePublication(Integer anneePublication) { this.anneePublication = anneePublication; }

    public Integer getNombreExemplaires() { return nombreExemplaires; }
    public void setNombreExemplaires(Integer nombreExemplaires) { this.nombreExemplaires = nombreExemplaires; }

    public Integer getExemplairesDisponibles() { return exemplairesDisponibles; }
    public void setExemplairesDisponibles(Integer exemplairesDisponibles) { this.exemplairesDisponibles = exemplairesDisponibles; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    public List<Emprunt> getEmprunts() { return emprunts; }
    public void setEmprunts(List<Emprunt> emprunts) { this.emprunts = emprunts; }

    // Méthodes utilitaires
    public boolean estDisponible() {
        return exemplairesDisponibles != null && exemplairesDisponibles > 0;
    }

    public void emprunter() {
        if (estDisponible()) {
            this.exemplairesDisponibles--;
        }
    }

    public void retourner() {
        if (exemplairesDisponibles < nombreExemplaires) {
            this.exemplairesDisponibles++;
        }
    }

    @Override
    public String toString() {
        return "Livre{id=" + id + ", isbn='" + isbn + "', titre='" + titre + "', auteur='" + auteur + "'}";
    }
}
