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
@Table(name = "inscription_etudiant")
@NamedQueries({
    @NamedQuery(name = "InscriptionEtudiant.findAll", query = "SELECT i FROM InscriptionEtudiant i"),
    @NamedQuery(name = "InscriptionEtudiant.findByIdInscription", query = "SELECT i FROM InscriptionEtudiant i WHERE i.idInscription = :idInscription"),
    @NamedQuery(name = "InscriptionEtudiant.findByStatut", query = "SELECT i FROM InscriptionEtudiant i WHERE i.statut = :statut"),
    @NamedQuery(name = "InscriptionEtudiant.findByDateInscription", query = "SELECT i FROM InscriptionEtudiant i WHERE i.dateInscription = :dateInscription")})
public class InscriptionEtudiant implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_inscription")
    private Long idInscription;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut")
    private String statut;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_inscription")
    @Temporal(TemporalType.DATE)
    private Date dateInscription;
    @JoinColumn(name = "id_annee", referencedColumnName = "id_annee")
    @ManyToOne(optional = false)
    private AnneeAcademique idAnnee;
    @JoinColumn(name = "id_etudiant", referencedColumnName = "id_etudiant")
    @ManyToOne(optional = false)
    private DossierEtudiant idEtudiant;
    @JoinColumn(name = "id_groupe", referencedColumnName = "id_groupe")
    @ManyToOne(optional = false)
    private Groupe idGroupe;
    @OneToMany(mappedBy = "inscriptionEtudiant")
    private List<NoteEvaluation> noteEvaluationList;

    public InscriptionEtudiant() {
    }

    public InscriptionEtudiant(Long idInscription) {
        this.idInscription = idInscription;
    }

    public InscriptionEtudiant(Long idInscription, String statut, Date dateInscription) {
        this.idInscription = idInscription;
        this.statut = statut;
        this.dateInscription = dateInscription;
    }

    public Long getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(Long idInscription) {
        this.idInscription = idInscription;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public AnneeAcademique getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(AnneeAcademique idAnnee) {
        this.idAnnee = idAnnee;
    }

    public DossierEtudiant getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(DossierEtudiant idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public Groupe getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(Groupe idGroupe) {
        this.idGroupe = idGroupe;
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
        hash += (idInscription != null ? idInscription.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InscriptionEtudiant)) {
            return false;
        }
        InscriptionEtudiant other = (InscriptionEtudiant) object;
        if ((this.idInscription == null && other.idInscription != null) || (this.idInscription != null && !this.idInscription.equals(other.idInscription))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.InscriptionEtudiant[ idInscription=" + idInscription + " ]";
    }
    
}
