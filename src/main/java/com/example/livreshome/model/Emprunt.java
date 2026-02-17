package com.example.livreshome.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entité Emprunt - Gestion des emprunts de livres
 * Mapping : table "emprunt"
 *
 * @author MBAYE
 * @version 1.0
 */
@Entity
@Table(name = "emprunt")
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Relation : Un emprunt concerne un livre (ManyToOne)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livre_id", nullable = false)
    private Livre livre;

    /**
     * Relation : Un emprunt concerne un adhérent (ManyToOne)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adherent_id", nullable = false)
    private Adherent adherent;

    /**
     * Relation : Un emprunt est enregistré par un utilisateur (ManyToOne)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "dateEmprunt", nullable = false)
    private LocalDate dateEmprunt = LocalDate.now();

    @Column(name = "dateRetourPrevue", nullable = false)
    private LocalDate dateRetourPrevue;

    @Column(name = "dateRetourEffective")
    private LocalDate dateRetourEffective;

    @Column(name = "penalite")
    private Double penalite = 0.0;

    // ========== CONSTRUCTEURS ==========

    public Emprunt() {
    }

    public Emprunt(Livre livre, Adherent adherent, Utilisateur utilisateur,
                   LocalDate dateRetourPrevue) {
        this.livre = livre;
        this.adherent = adherent;
        this.utilisateur = utilisateur;
        this.dateEmprunt = LocalDate.now();
        this.dateRetourPrevue = dateRetourPrevue;
    }

    // ========== GETTERS & SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Livre getLivre() {
        return livre;
    }

    public void setLivre(Livre livre) {
        this.livre = livre;
    }

    public Adherent getAdherent() {
        return adherent;
    }

    public void setAdherent(Adherent adherent) {
        this.adherent = adherent;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public void setDateEmprunt(LocalDate dateEmprunt) {
        this.dateEmprunt = dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue;
    }

    public void setDateRetourPrevue(LocalDate dateRetourPrevue) {
        this.dateRetourPrevue = dateRetourPrevue;
    }

    public LocalDate getDateRetourEffective() {
        return dateRetourEffective;
    }

    public void setDateRetourEffective(LocalDate dateRetourEffective) {
        this.dateRetourEffective = dateRetourEffective;
    }

    public Double getPenalite() {
        return penalite;
    }

    public void setPenalite(Double penalite) {
        this.penalite = penalite;
    }

    // ========== UTILITAIRES ==========

    /**
     * Vérifie si l'emprunt est en retard
     */
    public boolean estEnRetard() {
        return dateRetourEffective == null && LocalDate.now().isAfter(dateRetourPrevue);
    }

    /**
     * Retourne le nombre de jours de retard
     */
    public long getJoursRetard() {
        if (!estEnRetard()) return 0;
        return ChronoUnit.DAYS.between(dateRetourPrevue, LocalDate.now());
    }

    /**
     * Calcule la pénalité (1 € par jour de retard)
     */
    public void calculerPenalite() {
        if (estEnRetard()) {
            long joursRetard = getJoursRetard();
            this.penalite = joursRetard * 1.0; // 1€ par jour
        }
    }

    /**
     * Retourne le statut de l'emprunt
     */
    public String getStatut() {
        if (dateRetourEffective != null) {
            return "Retourné";
        }
        if (estEnRetard()) {
            return "En retard (" + getJoursRetard() + " jours)";
        }
        return "En cours";
    }

    @Override
    public String toString() {
        return "Emprunt{" +
                "id=" + id +
                ", livre=" + (livre != null ? livre.getTitre() : "null") +
                ", adherent=" + (adherent != null ? adherent.getNomComplet() : "null") +
                ", dateEmprunt=" + dateEmprunt +
                ", statut='" + getStatut() + '\'' +
                '}';
    }
}