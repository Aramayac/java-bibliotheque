package com.example.livrehome.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Entité JPA représentant un emprunt
 * @author MBAYE
 */
@Entity
@Table(name = "emprunt")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_emprunt", nullable = false)
    private LocalDateTime dateEmprunt;

    @Column(name = "date_retour_prevue", nullable = false)
    private LocalDate dateRetourPrevue;

    @Column(name = "date_retour_effective")
    private LocalDateTime dateRetourEffective;

    @Column(name = "penalite")
    private Double penalite = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private Statut statut = Statut.EN_COURS;

    public enum Statut {
        EN_COURS, TERMINE, EN_RETARD
    }

    public Emprunt() {
        this.dateEmprunt = LocalDateTime.now();
    }

    public Emprunt(Livre livre, Adherent adherent, Utilisateur utilisateur, int dureeJours) {
        this();
        this.livre = livre;
        this.adherent = adherent;
        this.utilisateur = utilisateur;
        this.dateRetourPrevue = LocalDate.now().plusDays(dureeJours);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Livre getLivre() { return livre; }
    public void setLivre(Livre livre) { this.livre = livre; }

    public Adherent getAdherent() { return adherent; }
    public void setAdherent(Adherent adherent) { this.adherent = adherent; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public LocalDateTime getDateEmprunt() { return dateEmprunt; }
    public void setDateEmprunt(LocalDateTime dateEmprunt) { this.dateEmprunt = dateEmprunt; }

    public LocalDate getDateRetourPrevue() { return dateRetourPrevue; }
    public void setDateRetourPrevue(LocalDate dateRetourPrevue) { this.dateRetourPrevue = dateRetourPrevue; }

    public LocalDateTime getDateRetourEffective() { return dateRetourEffective; }
    public void setDateRetourEffective(LocalDateTime dateRetourEffective) { this.dateRetourEffective = dateRetourEffective; }

    public Double getPenalite() { return penalite; }
    public void setPenalite(Double penalite) { this.penalite = penalite; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    // Méthodes utilitaires
    public boolean estEnRetard() {
        return dateRetourEffective == null && LocalDate.now().isAfter(dateRetourPrevue);
    }

    public long getJoursRetard() {
        if (dateRetourEffective != null) {
            return Math.max(0, ChronoUnit.DAYS.between(dateRetourPrevue, dateRetourEffective.toLocalDate()));
        }
        return Math.max(0, ChronoUnit.DAYS.between(dateRetourPrevue, LocalDate.now()));
    }

    public void calculerPenalite() {
        long joursRetard = getJoursRetard();
        if (joursRetard > 0) {
            this.penalite = joursRetard * 500.0; // 500 FCFA par jour
            this.statut = Statut.EN_RETARD;
        }
    }

    public void enregistrerRetour() {
        this.dateRetourEffective = LocalDateTime.now();
        this.statut = Statut.TERMINE;
        calculerPenalite();
    }

    @Override
    public String toString() {
        return "Emprunt{id=" + id + ", livre=" + (livre != null ? livre.getTitre() : "null") +
                ", adherent=" + (adherent != null ? adherent.getNomComplet() : "null") + ", statut=" + statut + '}';
    }
}
