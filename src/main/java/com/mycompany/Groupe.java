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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "groupe")
@NamedQueries({
    @NamedQuery(name = "Groupe.findAll", query = "SELECT g FROM Groupe g"),
    @NamedQuery(name = "Groupe.findByIdGroupe", query = "SELECT g FROM Groupe g WHERE g.idGroupe = :idGroupe"),
    @NamedQuery(name = "Groupe.findByCode", query = "SELECT g FROM Groupe g WHERE g.code = :code"),
    @NamedQuery(name = "Groupe.findByLibelle", query = "SELECT g FROM Groupe g WHERE g.libelle = :libelle")})
public class Groupe implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_groupe")
    private Long idGroupe;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "code")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 120)
    @Column(name = "libelle")
    private String libelle;
    @OneToMany(mappedBy = "idGroupe")
    private List<InscriptionEtudiant> inscriptionEtudiantList;
    @OneToMany(mappedBy = "idGroupe")
    private List<Inscription> inscriptionList;
    @OneToMany(mappedBy = "idGroupe")
    private List<Enseignement> enseignementList;
    @OneToMany(mappedBy = "idGroupe")
    private List<AffectationEnseignement> affectationEnseignementList;
    @JoinColumn(name = "id_annee", referencedColumnName = "id_annee")
    @ManyToOne(optional = false)
    private AnneeAcademique idAnnee;
    @JoinColumn(name = "id_filiere", referencedColumnName = "id_filiere")
    @ManyToOne(optional = false)
    private Filiere idFiliere;
    @JoinColumn(name = "id_niveau", referencedColumnName = "id_niveau")
    @ManyToOne(optional = false)
    private Niveau idNiveau;

    public Groupe() {
    }

    public Groupe(Long idGroupe) {
        this.idGroupe = idGroupe;
    }

    public Groupe(Long idGroupe, String code, String libelle) {
        this.idGroupe = idGroupe;
        this.code = code;
        this.libelle = libelle;
    }

    public Long getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(Long idGroupe) {
        this.idGroupe = idGroupe;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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

    public List<Enseignement> getEnseignementList() {
        return enseignementList;
    }

    public void setEnseignementList(List<Enseignement> enseignementList) {
        this.enseignementList = enseignementList;
    }

    public List<AffectationEnseignement> getAffectationEnseignementList() {
        return affectationEnseignementList;
    }

    public void setAffectationEnseignementList(List<AffectationEnseignement> affectationEnseignementList) {
        this.affectationEnseignementList = affectationEnseignementList;
    }

    public AnneeAcademique getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(AnneeAcademique idAnnee) {
        this.idAnnee = idAnnee;
    }

    public Filiere getIdFiliere() {
        return idFiliere;
    }

    public void setIdFiliere(Filiere idFiliere) {
        this.idFiliere = idFiliere;
    }

    public Niveau getIdNiveau() {
        return idNiveau;
    }

    public void setIdNiveau(Niveau idNiveau) {
        this.idNiveau = idNiveau;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idGroupe != null ? idGroupe.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Groupe)) {
            return false;
        }
        Groupe other = (Groupe) object;
        if ((this.idGroupe == null && other.idGroupe != null) || (this.idGroupe != null && !this.idGroupe.equals(other.idGroupe))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Groupe[ idGroupe=" + idGroupe + " ]";
    }
    
}
