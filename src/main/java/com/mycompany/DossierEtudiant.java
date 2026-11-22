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
@Table(name = "dossier_etudiant")
@NamedQueries({
    @NamedQuery(name = "DossierEtudiant.findAll", query = "SELECT d FROM DossierEtudiant d"),
    @NamedQuery(name = "DossierEtudiant.findByIdEtudiant", query = "SELECT d FROM DossierEtudiant d WHERE d.idEtudiant = :idEtudiant"),
    @NamedQuery(name = "DossierEtudiant.findByMatricule", query = "SELECT d FROM DossierEtudiant d WHERE d.matricule = :matricule"),
    @NamedQuery(name = "DossierEtudiant.findByNom", query = "SELECT d FROM DossierEtudiant d WHERE d.nom = :nom"),
    @NamedQuery(name = "DossierEtudiant.findByPrenom", query = "SELECT d FROM DossierEtudiant d WHERE d.prenom = :prenom"),
    @NamedQuery(name = "DossierEtudiant.findByDateCreation", query = "SELECT d FROM DossierEtudiant d WHERE d.dateCreation = :dateCreation")})
public class DossierEtudiant implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_etudiant")
    private Long idEtudiant;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "matricule")
    private String matricule;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "nom")
    private String nom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "prenom")
    private String prenom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_creation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    @OneToMany(mappedBy = "idEtudiant")
    private List<SuiviPedagogiqueAbsence> suiviPedagogiqueAbsenceList;
    @OneToMany(mappedBy = "idEtudiant")
    private List<InscriptionEtudiant> inscriptionEtudiantList;

    public DossierEtudiant() {
    }

    public DossierEtudiant(Long idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public DossierEtudiant(Long idEtudiant, String matricule, String nom, String prenom, Date dateCreation) {
        this.idEtudiant = idEtudiant;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateCreation = dateCreation;
    }

    public Long getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(Long idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public List<InscriptionEtudiant> getInscriptionEtudiantList() {
        return inscriptionEtudiantList;
    }

    public void setInscriptionEtudiantList(List<InscriptionEtudiant> inscriptionEtudiantList) {
        this.inscriptionEtudiantList = inscriptionEtudiantList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEtudiant != null ? idEtudiant.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DossierEtudiant)) {
            return false;
        }
        DossierEtudiant other = (DossierEtudiant) object;
        if ((this.idEtudiant == null && other.idEtudiant != null) || (this.idEtudiant != null && !this.idEtudiant.equals(other.idEtudiant))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.DossierEtudiant[ idEtudiant=" + idEtudiant + " ]";
    }
    
}
