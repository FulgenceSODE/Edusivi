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
import java.time.LocalTime;
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "seance_cours")
@NamedQueries({
    @NamedQuery(name = "SeanceCours.findAll", query = "SELECT s FROM SeanceCours s"),
    @NamedQuery(name = "SeanceCours.findByIdSeance", query = "SELECT s FROM SeanceCours s WHERE s.idSeance = :idSeance"),
    @NamedQuery(name = "SeanceCours.findByDateSeance", query = "SELECT s FROM SeanceCours s WHERE s.dateSeance = :dateSeance"),
    @NamedQuery(name = "SeanceCours.findByHeureDebut", query = "SELECT s FROM SeanceCours s WHERE s.heureDebut = :heureDebut"),
    @NamedQuery(name = "SeanceCours.findByHeureFin", query = "SELECT s FROM SeanceCours s WHERE s.heureFin = :heureFin"),
    @NamedQuery(name = "SeanceCours.findByContenuAborde", query = "SELECT s FROM SeanceCours s WHERE s.contenuAborde = :contenuAborde"),
    @NamedQuery(name = "SeanceCours.findByVolumeHoraireRealise", query = "SELECT s FROM SeanceCours s WHERE s.volumeHoraireRealise = :volumeHoraireRealise")})
public class SeanceCours implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_seance")
    private Long idSeance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_seance")
    @Temporal(TemporalType.DATE)
    private Date dateSeance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "heure_debut")
    private LocalTime heureDebut;
    @Basic(optional = false)
    @NotNull
    @Column(name = "heure_fin")
    private LocalTime heureFin;
    @Size(max = 2147483647)
    @Column(name = "contenu_aborde")
    private String contenuAborde;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "volume_horaire_realise")
    private BigDecimal volumeHoraireRealise;
    @JoinColumn(name = "id_enseignement", referencedColumnName = "id_enseignement")
    @ManyToOne(optional = false)
    private Enseignement idEnseignement;

    public SeanceCours() {
    }

    public SeanceCours(Long idSeance) {
        this.idSeance = idSeance;
    }

    public SeanceCours(Long idSeance, Date dateSeance, LocalTime heureDebut, LocalTime heureFin) {
        this.idSeance = idSeance;
        this.dateSeance = dateSeance;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public Long getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(Long idSeance) {
        this.idSeance = idSeance;
    }

    public Date getDateSeance() {
        return dateSeance;
    }

    public void setDateSeance(Date dateSeance) {
        this.dateSeance = dateSeance;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public String getContenuAborde() {
        return contenuAborde;
    }

    public void setContenuAborde(String contenuAborde) {
        this.contenuAborde = contenuAborde;
    }

    public BigDecimal getVolumeHoraireRealise() {
        return volumeHoraireRealise;
    }

    public void setVolumeHoraireRealise(BigDecimal volumeHoraireRealise) {
        this.volumeHoraireRealise = volumeHoraireRealise;
    }

    public Enseignement getIdEnseignement() {
        return idEnseignement;
    }

    public void setIdEnseignement(Enseignement idEnseignement) {
        this.idEnseignement = idEnseignement;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSeance != null ? idSeance.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SeanceCours)) {
            return false;
        }
        SeanceCours other = (SeanceCours) object;
        if ((this.idSeance == null && other.idSeance != null) || (this.idSeance != null && !this.idSeance.equals(other.idSeance))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.SeanceCours[ idSeance=" + idSeance + " ]";
    }
    
}
