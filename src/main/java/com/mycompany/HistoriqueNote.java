/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "historique_note")
@NamedQueries({
    @NamedQuery(name = "HistoriqueNote.findAll", query = "SELECT h FROM HistoriqueNote h"),
    @NamedQuery(name = "HistoriqueNote.findByIdHistorique", query = "SELECT h FROM HistoriqueNote h WHERE h.idHistorique = :idHistorique"),
    @NamedQuery(name = "HistoriqueNote.findByAncienneValeur", query = "SELECT h FROM HistoriqueNote h WHERE h.ancienneValeur = :ancienneValeur"),
    @NamedQuery(name = "HistoriqueNote.findByNouvelleValeur", query = "SELECT h FROM HistoriqueNote h WHERE h.nouvelleValeur = :nouvelleValeur"),
    @NamedQuery(name = "HistoriqueNote.findByMotif", query = "SELECT h FROM HistoriqueNote h WHERE h.motif = :motif"),
    @NamedQuery(name = "HistoriqueNote.findByDateModification", query = "SELECT h FROM HistoriqueNote h WHERE h.dateModification = :dateModification")})
public class HistoriqueNote implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_historique")
    private Long idHistorique;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "ancienne_valeur")
    private BigDecimal ancienneValeur;
    @Column(name = "nouvelle_valeur")
    private BigDecimal nouvelleValeur;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "motif")
    private String motif;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_modification")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification;
    @JoinColumn(name = "id_note", referencedColumnName = "id_note")
    @ManyToOne(optional = false)
    private Note idNote;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne(optional = false)
    private Utilisateur idUtilisateur;

    public HistoriqueNote() {
    }

    public HistoriqueNote(Long idHistorique) {
        this.idHistorique = idHistorique;
    }

    public HistoriqueNote(Long idHistorique, String motif, Date dateModification) {
        this.idHistorique = idHistorique;
        this.motif = motif;
        this.dateModification = dateModification;
    }

    public Long getIdHistorique() {
        return idHistorique;
    }

    public void setIdHistorique(Long idHistorique) {
        this.idHistorique = idHistorique;
    }

    public BigDecimal getAncienneValeur() {
        return ancienneValeur;
    }

    public void setAncienneValeur(BigDecimal ancienneValeur) {
        this.ancienneValeur = ancienneValeur;
    }

    public BigDecimal getNouvelleValeur() {
        return nouvelleValeur;
    }

    public void setNouvelleValeur(BigDecimal nouvelleValeur) {
        this.nouvelleValeur = nouvelleValeur;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Date getDateModification() {
        return dateModification;
    }

    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }

    public Note getIdNote() {
        return idNote;
    }

    public void setIdNote(Note idNote) {
        this.idNote = idNote;
    }

    public Utilisateur getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(Utilisateur idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idHistorique != null ? idHistorique.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HistoriqueNote)) {
            return false;
        }
        HistoriqueNote other = (HistoriqueNote) object;
        if ((this.idHistorique == null && other.idHistorique != null) || (this.idHistorique != null && !this.idHistorique.equals(other.idHistorique))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.HistoriqueNote[ idHistorique=" + idHistorique + " ]";
    }
    
}
