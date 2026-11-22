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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "suivi_pedagogique_seance")
@NamedQueries({
    @NamedQuery(name = "SuiviPedagogiqueSeance.findAll", query = "SELECT s FROM SuiviPedagogiqueSeance s"),
    @NamedQuery(name = "SuiviPedagogiqueSeance.findByIdSeance", query = "SELECT s FROM SuiviPedagogiqueSeance s WHERE s.idSeance = :idSeance"),
    @NamedQuery(name = "SuiviPedagogiqueSeance.findByDateSeance", query = "SELECT s FROM SuiviPedagogiqueSeance s WHERE s.dateSeance = :dateSeance"),
    @NamedQuery(name = "SuiviPedagogiqueSeance.findByContenu", query = "SELECT s FROM SuiviPedagogiqueSeance s WHERE s.contenu = :contenu"),
    @NamedQuery(name = "SuiviPedagogiqueSeance.findByDateCreation", query = "SELECT s FROM SuiviPedagogiqueSeance s WHERE s.dateCreation = :dateCreation")})
public class SuiviPedagogiqueSeance implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_seance")
    private Long idSeance;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_seance")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSeance;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "contenu")
    private String contenu;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @OneToMany(mappedBy = "idSeance")
    private List<SuiviPedagogiqueAbsence> suiviPedagogiqueAbsenceList;
    @JoinColumn(name = "id_affectation", referencedColumnName = "id_affectation")
    @ManyToOne(optional = false)
    private AffectationEnseignement idAffectation;

    public SuiviPedagogiqueSeance() {
    }

    public SuiviPedagogiqueSeance(Long idSeance) {
        this.idSeance = idSeance;
    }

    public SuiviPedagogiqueSeance(Long idSeance, Date dateSeance, String contenu, Date dateCreation) {
        this.idSeance = idSeance;
        this.dateSeance = dateSeance;
        this.contenu = contenu;
        this.dateCreation = dateCreation;
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<SuiviPedagogiqueAbsence> getSuiviPedagogiqueAbsenceList() {
        return suiviPedagogiqueAbsenceList;
    }

    public void setSuiviPedagogiqueAbsenceList(List<SuiviPedagogiqueAbsence> suiviPedagogiqueAbsenceList) {
        this.suiviPedagogiqueAbsenceList = suiviPedagogiqueAbsenceList;
    }

    public AffectationEnseignement getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(AffectationEnseignement idAffectation) {
        this.idAffectation = idAffectation;
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
        if (!(object instanceof SuiviPedagogiqueSeance)) {
            return false;
        }
        SuiviPedagogiqueSeance other = (SuiviPedagogiqueSeance) object;
        if ((this.idSeance == null && other.idSeance != null) || (this.idSeance != null && !this.idSeance.equals(other.idSeance))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.SuiviPedagogiqueSeance[ idSeance=" + idSeance + " ]";
    }
    
}
