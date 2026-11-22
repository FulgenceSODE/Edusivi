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
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "suivi_pedagogique_absence")
@NamedQueries({
    @NamedQuery(name = "SuiviPedagogiqueAbsence.findAll", query = "SELECT s FROM SuiviPedagogiqueAbsence s"),
    @NamedQuery(name = "SuiviPedagogiqueAbsence.findByIdAbsence", query = "SELECT s FROM SuiviPedagogiqueAbsence s WHERE s.idAbsence = :idAbsence"),
    @NamedQuery(name = "SuiviPedagogiqueAbsence.findByJustifiee", query = "SELECT s FROM SuiviPedagogiqueAbsence s WHERE s.justifiee = :justifiee"),
    @NamedQuery(name = "SuiviPedagogiqueAbsence.findByMotif", query = "SELECT s FROM SuiviPedagogiqueAbsence s WHERE s.motif = :motif"),
    @NamedQuery(name = "SuiviPedagogiqueAbsence.findByDateSaisie", query = "SELECT s FROM SuiviPedagogiqueAbsence s WHERE s.dateSaisie = :dateSaisie")})
public class SuiviPedagogiqueAbsence implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_absence")
    private Long idAbsence;
    @Basic(optional = false)
    @NotNull
    @Column(name = "justifiee")
    private boolean justifiee;
    @Size(max = 500)
    @Column(name = "motif")
    private String motif;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_saisie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaisie;
    @JoinColumn(name = "id_etudiant", referencedColumnName = "id_etudiant")
    @ManyToOne(optional = false)
    private DossierEtudiant idEtudiant;
    @JoinColumn(name = "id_seance", referencedColumnName = "id_seance")
    @ManyToOne(optional = false)
    private SuiviPedagogiqueSeance idSeance;

    public SuiviPedagogiqueAbsence() {
    }

    public SuiviPedagogiqueAbsence(Long idAbsence) {
        this.idAbsence = idAbsence;
    }

    public SuiviPedagogiqueAbsence(Long idAbsence, boolean justifiee, Date dateSaisie) {
        this.idAbsence = idAbsence;
        this.justifiee = justifiee;
        this.dateSaisie = dateSaisie;
    }

    public Long getIdAbsence() {
        return idAbsence;
    }

    public void setIdAbsence(Long idAbsence) {
        this.idAbsence = idAbsence;
    }

    public boolean getJustifiee() {
        return justifiee;
    }

    public void setJustifiee(boolean justifiee) {
        this.justifiee = justifiee;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Date getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(Date dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public DossierEtudiant getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(DossierEtudiant idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public SuiviPedagogiqueSeance getIdSeance() {
        return idSeance;
    }

    public void setIdSeance(SuiviPedagogiqueSeance idSeance) {
        this.idSeance = idSeance;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idAbsence != null ? idAbsence.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SuiviPedagogiqueAbsence)) {
            return false;
        }
        SuiviPedagogiqueAbsence other = (SuiviPedagogiqueAbsence) object;
        if ((this.idAbsence == null && other.idAbsence != null) || (this.idAbsence != null && !this.idAbsence.equals(other.idAbsence))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.SuiviPedagogiqueAbsence[ idAbsence=" + idAbsence + " ]";
    }
    
}
