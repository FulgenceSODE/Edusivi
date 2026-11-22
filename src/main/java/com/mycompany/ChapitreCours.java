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
@Table(name = "chapitre_cours")
@NamedQueries({
    @NamedQuery(name = "ChapitreCours.findAll", query = "SELECT c FROM ChapitreCours c"),
    @NamedQuery(name = "ChapitreCours.findByIdChapitre", query = "SELECT c FROM ChapitreCours c WHERE c.idChapitre = :idChapitre"),
    @NamedQuery(name = "ChapitreCours.findByTitre", query = "SELECT c FROM ChapitreCours c WHERE c.titre = :titre"),
    @NamedQuery(name = "ChapitreCours.findByOrdre", query = "SELECT c FROM ChapitreCours c WHERE c.ordre = :ordre"),
    @NamedQuery(name = "ChapitreCours.findByProgression", query = "SELECT c FROM ChapitreCours c WHERE c.progression = :progression"),
    @NamedQuery(name = "ChapitreCours.findByDateMiseAJour", query = "SELECT c FROM ChapitreCours c WHERE c.dateMiseAJour = :dateMiseAJour")})
public class ChapitreCours implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_chapitre")
    private Long idChapitre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "titre")
    private String titre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ordre")
    private int ordre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "progression")
    private String progression;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_mise_a_jour")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateMiseAJour;
    @JoinColumn(name = "id_affectation", referencedColumnName = "id_affectation")
    @ManyToOne(optional = false)
    private AffectationEnseignement idAffectation;

    public ChapitreCours() {
    }

    public ChapitreCours(Long idChapitre) {
        this.idChapitre = idChapitre;
    }

    public ChapitreCours(Long idChapitre, String titre, int ordre, String progression, Date dateMiseAJour) {
        this.idChapitre = idChapitre;
        this.titre = titre;
        this.ordre = ordre;
        this.progression = progression;
        this.dateMiseAJour = dateMiseAJour;
    }

    public Long getIdChapitre() {
        return idChapitre;
    }

    public void setIdChapitre(Long idChapitre) {
        this.idChapitre = idChapitre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public int getOrdre() {
        return ordre;
    }

    public void setOrdre(int ordre) {
        this.ordre = ordre;
    }

    public String getProgression() {
        return progression;
    }

    public void setProgression(String progression) {
        this.progression = progression;
    }

    public Date getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(Date dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
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
        hash += (idChapitre != null ? idChapitre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ChapitreCours)) {
            return false;
        }
        ChapitreCours other = (ChapitreCours) object;
        if ((this.idChapitre == null && other.idChapitre != null) || (this.idChapitre != null && !this.idChapitre.equals(other.idChapitre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.ChapitreCours[ idChapitre=" + idChapitre + " ]";
    }
    
}
