/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "note_evaluation")
@NamedQueries({
    @NamedQuery(name = "NoteEvaluation.findAll", query = "SELECT n FROM NoteEvaluation n"),
    @NamedQuery(name = "NoteEvaluation.findByIdEvaluation", query = "SELECT n FROM NoteEvaluation n WHERE n.noteEvaluationPK.idEvaluation = :idEvaluation"),
    @NamedQuery(name = "NoteEvaluation.findByIdInscription", query = "SELECT n FROM NoteEvaluation n WHERE n.noteEvaluationPK.idInscription = :idInscription"),
    @NamedQuery(name = "NoteEvaluation.findByIdEtudiant", query = "SELECT n FROM NoteEvaluation n WHERE n.inscriptionEtudiant.idEtudiant.idEtudiant = :idEtudiant"),
    @NamedQuery(name = "NoteEvaluation.findByValeur", query = "SELECT n FROM NoteEvaluation n WHERE n.valeur = :valeur"),
    @NamedQuery(name = "NoteEvaluation.findByDateSaisie", query = "SELECT n FROM NoteEvaluation n WHERE n.dateSaisie = :dateSaisie")})
public class NoteEvaluation implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected NoteEvaluationPK noteEvaluationPK;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "valeur")
    private BigDecimal valeur;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_saisie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaisie;
    @JoinColumn(name = "id_inscription", referencedColumnName = "id_inscription", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private InscriptionEtudiant inscriptionEtudiant;
    @JoinColumn(name = "id_evaluation", referencedColumnName = "id_evaluation", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private EvaluationCours evaluationCours;

    public NoteEvaluation() {
    }

    public NoteEvaluation(NoteEvaluationPK noteEvaluationPK) {
        this.noteEvaluationPK = noteEvaluationPK;
    }

    public NoteEvaluation(NoteEvaluationPK noteEvaluationPK, BigDecimal valeur, Date dateSaisie) {
        this.noteEvaluationPK = noteEvaluationPK;
        this.valeur = valeur;
        this.dateSaisie = dateSaisie;
    }

    public NoteEvaluation(long idEvaluation, long idInscription) {
        this.noteEvaluationPK = new NoteEvaluationPK(idEvaluation, idInscription);
    }

    public NoteEvaluationPK getNoteEvaluationPK() {
        return noteEvaluationPK;
    }

    public void setNoteEvaluationPK(NoteEvaluationPK noteEvaluationPK) {
        this.noteEvaluationPK = noteEvaluationPK;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public Date getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(Date dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public InscriptionEtudiant getInscriptionEtudiant() {
        return inscriptionEtudiant;
    }

    public void setInscriptionEtudiant(InscriptionEtudiant inscriptionEtudiant) {
        this.inscriptionEtudiant = inscriptionEtudiant;
    }

    public EvaluationCours getEvaluationCours() {
        return evaluationCours;
    }

    public void setEvaluationCours(EvaluationCours evaluationCours) {
        this.evaluationCours = evaluationCours;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (noteEvaluationPK != null ? noteEvaluationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NoteEvaluation)) {
            return false;
        }
        NoteEvaluation other = (NoteEvaluation) object;
        if ((this.noteEvaluationPK == null && other.noteEvaluationPK != null) || (this.noteEvaluationPK != null && !this.noteEvaluationPK.equals(other.noteEvaluationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.NoteEvaluation[ noteEvaluationPK=" + noteEvaluationPK + " ]";
    }
    
}
