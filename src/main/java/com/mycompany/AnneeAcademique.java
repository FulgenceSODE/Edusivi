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
@Table(name = "annee_academique")
@NamedQueries({
    @NamedQuery(name = "AnneeAcademique.findAll", query = "SELECT a FROM AnneeAcademique a"),
    @NamedQuery(name = "AnneeAcademique.findByIdAnnee", query = "SELECT a FROM AnneeAcademique a WHERE a.idAnnee = :idAnnee"),
    @NamedQuery(name = "AnneeAcademique.findByLibelle", query = "SELECT a FROM AnneeAcademique a WHERE a.libelle = :libelle"),
    @NamedQuery(name = "AnneeAcademique.findByDateDebut", query = "SELECT a FROM AnneeAcademique a WHERE a.dateDebut = :dateDebut"),
    @NamedQuery(name = "AnneeAcademique.findByDateFin", query = "SELECT a FROM AnneeAcademique a WHERE a.dateFin = :dateFin"),
    @NamedQuery(name = "AnneeAcademique.findByStatut", query = "SELECT a FROM AnneeAcademique a WHERE a.statut = :statut")})
public class AnneeAcademique implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_annee")
    private Long idAnnee;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "libelle")
    private String libelle;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_debut")
    @Temporal(TemporalType.DATE)
    private Date dateDebut;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_fin")
    @Temporal(TemporalType.DATE)
    private Date dateFin;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut")
    private String statut;
    @OneToMany(mappedBy = "idAnnee")
    private List<InscriptionEtudiant> inscriptionEtudiantList;
    @OneToMany(mappedBy = "idAnnee")
    private List<Inscription> inscriptionList;
    @OneToMany(mappedBy = "idAnnee")
    private List<AffectationEnseignement> affectationEnseignementList;
    @OneToMany(mappedBy = "idAnnee")
    private List<Groupe> groupeList;
    @OneToMany(mappedBy = "idAnnee")
    private List<Semestre> semestreList;

    public AnneeAcademique() {
    }

    public AnneeAcademique(Long idAnnee) {
        this.idAnnee = idAnnee;
    }

    public AnneeAcademique(Long idAnnee, String libelle, Date dateDebut, Date dateFin, String statut) {
        this.idAnnee = idAnnee;
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    public Long getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(Long idAnnee) {
        this.idAnnee = idAnnee;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<InscriptionEtudiant> getInscriptionEtudiantList() {
        return inscriptionEtudiantList;
    }

    public void setInscriptionEtudiantList(List<InscriptionEtudiant> inscriptionEtudiantList) {
        this.inscriptionEtudiantList = inscriptionEtudiantList;
    }

    public List<Inscription> getInscriptionList() {
        return inscriptionList;
    }

    public void setInscriptionList(List<Inscription> inscriptionList) {
        this.inscriptionList = inscriptionList;
    }

    public List<AffectationEnseignement> getAffectationEnseignementList() {
        return affectationEnseignementList;
    }

    public void setAffectationEnseignementList(List<AffectationEnseignement> affectationEnseignementList) {
        this.affectationEnseignementList = affectationEnseignementList;
    }

    public List<Groupe> getGroupeList() {
        return groupeList;
    }

    public void setGroupeList(List<Groupe> groupeList) {
        this.groupeList = groupeList;
    }

    public List<Semestre> getSemestreList() {
        return semestreList;
    }

    public void setSemestreList(List<Semestre> semestreList) {
        this.semestreList = semestreList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idAnnee != null ? idAnnee.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AnneeAcademique)) {
            return false;
        }
        AnneeAcademique other = (AnneeAcademique) object;
        if ((this.idAnnee == null && other.idAnnee != null) || (this.idAnnee != null && !this.idAnnee.equals(other.idAnnee))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.AnneeAcademique[ idAnnee=" + idAnnee + " ]";
    }
    
}
