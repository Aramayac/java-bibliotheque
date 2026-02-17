package com.example.livreshome.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Utilisateur - Admin, Bibliothécaire ou Utilisateur
 * Mapping : table "utilisateur"
 *
 * @author ARAMA
 * @version 1.0
 */
@Entity
@Table(name = "utilisateur", uniqueConstraints = {
        @UniqueConstraint(columnNames = "login"),
        @UniqueConstraint(columnNames = "email")
})
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "login", nullable = false, length = 50)
    private String login;

    @Column(name = "motDePasse", nullable = false)
    private String motDePasse;

    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "profil", nullable = false)
    private Profil profil = Profil.BIBLIOTHECAIRE;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "dateCreation")
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "derniereConnexion")
    private LocalDateTime derniereConnexion;

    /**
     * Énumération des profils utilisateur
     */
    public enum Profil {
        ADMIN("Administrateur"),
        BIBLIOTHECAIRE("Bibliothécaire"),
        UTILISATEUR("Utilisateur");  // ✅ AJOUTÉ

        private final String label;

        Profil(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // ========== CONSTRUCTEURS ==========

    /**
     * Constructeur vide (obligatoire pour JPA)
     */
    public Utilisateur() {
    }

    /**
     * Constructeur complet
     */
    public Utilisateur(String login, String motDePasse, String nom, String prenom,
                       String email, Profil profil) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.profil = profil;
        this.dateCreation = LocalDateTime.now();
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profil getProfil() {
        return profil;
    }

    public void setProfil(Profil profil) {
        this.profil = profil;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }

    // ========== UTILITAIRES ==========

    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getNomComplet() {
        return (prenom != null ? prenom + " " : "") + (nom != null ? nom : "");
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", profil=" + profil +
                ", actif=" + actif +
                '}';
    }
}