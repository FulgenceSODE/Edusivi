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
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "evaluation_cours")
@NamedQueries({
    @NamedQuery(name = "EvaluationCours.findAll", query = "SELECT e FROM EvaluationCours e"),
    @NamedQuery(name = "EvaluationCours.findByIdEvaluation", query = "SELECT e FROM EvaluationCours e WHERE e.idEvaluation = :idEvaluation"),
    @NamedQuery(name = "EvaluationCours.findByLibelle", query = "SELECT e FROM EvaluationCours e WHERE e.libelle = :libelle"),
    @NamedQuery(name = "EvaluationCours.findByDateEvaluation", query = "SELECT e FROM EvaluationCours e WHERE e.dateEvaluation = :dateEvaluation"),
    @NamedQuery(name = "EvaluationCours.findByCoefficient", query = "SELECT e FROM EvaluationCours e WHERE e.coefficient = :coefficient"),
    @NamedQuery(name = "EvaluationCours.findByStatut", query = "SELECT e FROM EvaluationCours e WHERE e.statut = :statut")})
public class EvaluationCours implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_evaluation")
    private Long idEvaluation;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "libelle")
    private String libelle;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_evaluation")
    @Temporal(TemporalType.DATE)
    private Date dateEvaluation;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "coefficient")
    private BigDecimal coefficient;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "categorie")
    private String categorie;
    @Basic(optional = false)
    @NotNull
    @Column(name = "bareme")
    private BigDecimal bareme;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut")
    private String statut;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut_publication")
    private String statutPublication;
    @JoinColumn(name = "id_affectation", referencedColumnName = "id_affectation")
    @ManyToOne(optional = false)
    private AffectationEnseignement idAffectation;
    @OneToMany(mappedBy = "evaluationCours")
    private List<NoteEvaluation> noteEvaluationList;

    public EvaluationCours() {
    }

    public EvaluationCours(Long idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public EvaluationCours(Long idEvaluation, String libelle, Date dateEvaluation, BigDecimal coefficient, String statut) {
        this.idEvaluation = idEvaluation;
        this.libelle = libelle;
        this.dateEvaluation = dateEvaluation;
        this.coefficient = coefficient;
        this.statut = statut;
    }

    public Long getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(Long idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Date getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(Date dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getBareme() {
        return bareme;
    }

    public void setBareme(BigDecimal bareme) {
        this.bareme = bareme;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getStatutPublication() {
        return statutPublication;
    }

    public void setStatutPublication(String statutPublication) {
        this.statutPublication = statutPublication;
    }

    public AffectationEnseignement getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(AffectationEnseignement idAffectation) {
        this.idAffectation = idAffectation;
    }

    public List<NoteEvaluation> getNoteEvaluationList() {
        return noteEvaluationList;
    }

    public void setNoteEvaluationList(List<NoteEvaluation> noteEvaluationList) {
        this.noteEvaluationList = noteEvaluationList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEvaluation != null ? idEvaluation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EvaluationCours)) {
            return false;
        }
        EvaluationCours other = (EvaluationCours) object;
        if ((this.idEvaluation == null && other.idEvaluation != null) || (this.idEvaluation != null && !this.idEvaluation.equals(other.idEvaluation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.EvaluationCours[ idEvaluation=" + idEvaluation + " ]";
    }
    
}
