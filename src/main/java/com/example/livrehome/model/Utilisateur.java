package com.example.livrehome.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant un utilisateur du système
 * @author ARAMA
 */
@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "profil", nullable = false)
    private Profil profil;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private List<Emprunt> empruntsGeres = new ArrayList<>();

    public enum Profil {
        ADMIN, BIBLIOTHECAIRE
    }

    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.actif = true;
    }

    public Utilisateur(String login, String motDePasse, String nom, String prenom, String email, Profil profil) {
        this();
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.profil = profil;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDerniereConnexion() { return derniereConnexion; }
    public void setDerniereConnexion(LocalDateTime derniereConnexion) { this.derniereConnexion = derniereConnexion; }

    public List<Emprunt> getEmpruntsGeres() { return empruntsGeres; }
    public void setEmpruntsGeres(List<Emprunt> empruntsGeres) { this.empruntsGeres = empruntsGeres; }

    // Méthodes utilitaires
    public String getNomComplet() { return prenom + " " + nom; }
    public boolean isAdmin() { return profil == Profil.ADMIN; }
    public boolean isBibliothecaire() { return profil == Profil.BIBLIOTHECAIRE; }
    public void mettreAJourDerniereConnexion() { this.derniereConnexion = LocalDateTime.now(); }

    @Override
    public String toString() {
        return "Utilisateur{id=" + id + ", login='" + login + "', nom='" + nom + "', profil=" + profil + '}';
    }
}
