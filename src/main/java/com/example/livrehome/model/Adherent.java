package com.example.livrehome.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité JPA représentant un adhérent de la bibliothèque
 * @author SECK
 */
@Entity
@Table(name = "adherent")
public class Adherent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", nullable = false, unique = true, length = 50)
    private String matricule;

    @Column(name = "nom", length = 100)
    private String nom;

    @Column(name = "prenom", length = 100)
    private String prenom;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @Column(name = "dateInscription")
    private LocalDate dateInscription;

    @Column(name = "actif")
    private Boolean actif = true;

    // Relation OneToMany avec Emprunt
    @OneToMany(mappedBy = "adherent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Emprunt> emprunts = new ArrayList<>();

    // Constructeurs
    public Adherent() {
        this.dateInscription = LocalDate.now();
        this.actif = true;
    }

    public Adherent(String matricule, String nom, String prenom, String email,
                    String telephone, String adresse) {
        this();
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public List<Emprunt> getEmprunts() {
        return emprunts;
    }

    public void setEmprunts(List<Emprunt> emprunts) {
        this.emprunts = emprunts;
    }

    // Méthodes utilitaires
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public void suspendre() {
        this.actif = false;
    }

    public void reactiver() {
        this.actif = true;
    }

    public int getNombreEmpruntsEnCours() {
        return (int) emprunts.stream()
                .filter(e -> e.getDateRetourEffective() == null)
                .count();
    }

    @Override
    public String toString() {
        return "Adherent{" +
                "id=" + id +
                ", matricule='" + matricule + '\'' +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", actif=" + actif +
                '}';
    }
}