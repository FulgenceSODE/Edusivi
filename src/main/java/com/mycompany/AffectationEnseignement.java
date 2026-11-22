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
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "affectation_enseignement")
@NamedQueries({
    @NamedQuery(name = "AffectationEnseignement.findAll", query = "SELECT a FROM AffectationEnseignement a"),
    @NamedQuery(name = "AffectationEnseignement.findByIdAffectation", query = "SELECT a FROM AffectationEnseignement a WHERE a.idAffectation = :idAffectation"),
    @NamedQuery(name = "AffectationEnseignement.findByDateAffectation", query = "SELECT a FROM AffectationEnseignement a WHERE a.dateAffectation = :dateAffectation")})
public class AffectationEnseignement implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_affectation")
    private Long idAffectation;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_affectation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAffectation;
    @OneToMany(mappedBy = "idAffectation")
    private List<EvaluationCours> evaluationCoursList;
    @OneToMany(mappedBy = "idAffectation")
    private List<SuiviPedagogiqueSeance> suiviPedagogiqueSeanceList;
    @OneToMany(mappedBy = "idAffectation")
    private List<ChapitreCours> chapitreCoursList;
    @JoinColumn(name = "id_annee", referencedColumnName = "id_annee")
    @ManyToOne(optional = false)
    private AnneeAcademique idAnnee;
    @JoinColumn(name = "id_ec", referencedColumnName = "id_ec")
    @ManyToOne(optional = false)
    private ElementConstitutif idEc;
    @JoinColumn(name = "id_groupe", referencedColumnName = "id_groupe")
    @ManyToOne(optional = false)
    private Groupe idGroupe;
    @JoinColumn(name = "id_semestre", referencedColumnName = "id_semestre")
    @ManyToOne(optional = false)
    private Semestre idSemestre;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne(optional = false)
    private Utilisateur idUtilisateur;

    public AffectationEnseignement() {
    }

    public AffectationEnseignement(Long idAffectation) {
        this.idAffectation = idAffectation;
    }

    public AffectationEnseignement(Long idAffectation, Date dateAffectation) {
        this.idAffectation = idAffectation;
        this.dateAffectation = dateAffectation;
    }

    public Long getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(Long idAffectation) {
        this.idAffectation = idAffectation;
    }

    public Date getDateAffectation() {
        return dateAffectation;
    }

    public void setDateAffectation(Date dateAffectation) {
        this.dateAffectation = dateAffectation;
    }

    public List<EvaluationCours> getEvaluationCoursList() {
        return evaluationCoursList;
    }

    public void setEvaluationCoursList(List<EvaluationCours> evaluationCoursList) {
        this.evaluationCoursList = evaluationCoursList;
    }

    public List<SuiviPedagogiqueSeance> getSuiviPedagogiqueSeanceList() {
        return suiviPedagogiqueSeanceList;
    }

    public void setSuiviPedagogiqueSeanceList(List<SuiviPedagogiqueSeance> suiviPedagogiqueSeanceList) {
        this.suiviPedagogiqueSeanceList = suiviPedagogiqueSeanceList;
    }

    public List<ChapitreCours> getChapitreCoursList() {
        return chapitreCoursList;
    }

    public void setChapitreCoursList(List<ChapitreCours> chapitreCoursList) {
        this.chapitreCoursList = chapitreCoursList;
    }

    public AnneeAcademique getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(AnneeAcademique idAnnee) {
        this.idAnnee = idAnnee;
    }

    public ElementConstitutif getIdEc() {
        return idEc;
    }

    public void setIdEc(ElementConstitutif idEc) {
        this.idEc = idEc;
    }

    public Groupe getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(Groupe idGroupe) {
        this.idGroupe = idGroupe;
    }

    public Semestre getIdSemestre() {
        return idSemestre;
    }

    public void setIdSemestre(Semestre idSemestre) {
        this.idSemestre = idSemestre;
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
        hash += (idAffectation != null ? idAffectation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AffectationEnseignement)) {
            return false;
        }
        AffectationEnseignement other = (AffectationEnseignement) object;
        if ((this.idAffectation == null && other.idAffectation != null) || (this.idAffectation != null && !this.idAffectation.equals(other.idAffectation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.AffectationEnseignement[ idAffectation=" + idAffectation + " ]";
    }
    
}
