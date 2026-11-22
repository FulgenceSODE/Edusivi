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
@Table(name = "semestre")
@NamedQueries({
    @NamedQuery(name = "Semestre.findAll", query = "SELECT s FROM Semestre s"),
    @NamedQuery(name = "Semestre.findByIdSemestre", query = "SELECT s FROM Semestre s WHERE s.idSemestre = :idSemestre"),
    @NamedQuery(name = "Semestre.findByCode", query = "SELECT s FROM Semestre s WHERE s.code = :code"),
    @NamedQuery(name = "Semestre.findByLibelle", query = "SELECT s FROM Semestre s WHERE s.libelle = :libelle"),
    @NamedQuery(name = "Semestre.findByDateDebut", query = "SELECT s FROM Semestre s WHERE s.dateDebut = :dateDebut"),
    @NamedQuery(name = "Semestre.findByDateFin", query = "SELECT s FROM Semestre s WHERE s.dateFin = :dateFin")})
public class Semestre implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_semestre")
    private Long idSemestre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "code")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
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
    @OneToMany(mappedBy = "idSemestre")
    private List<UniteEnseignement> uniteEnseignementList;
    @OneToMany(mappedBy = "idSemestre")
    private List<Enseignement> enseignementList;
    @JoinColumn(name = "id_annee", referencedColumnName = "id_annee")
    @ManyToOne(optional = false)
    private AnneeAcademique idAnnee;

    public Semestre() {
    }

    public Semestre(Long idSemestre) {
        this.idSemestre = idSemestre;
    }

    public Semestre(Long idSemestre, String code, String libelle, Date dateDebut, Date dateFin) {
        this.idSemestre = idSemestre;
        this.code = code;
        this.libelle = libelle;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
    }

    public Long getIdSemestre() {
        return idSemestre;
    }

    public void setIdSemestre(Long idSemestre) {
        this.idSemestre = idSemestre;
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

    public List<UniteEnseignement> getUniteEnseignementList() {
        return uniteEnseignementList;
    }

    public void setUniteEnseignementList(List<UniteEnseignement> uniteEnseignementList) {
        this.uniteEnseignementList = uniteEnseignementList;
    }

    public List<Enseignement> getEnseignementList() {
        return enseignementList;
    }

    public void setEnseignementList(List<Enseignement> enseignementList) {
        this.enseignementList = enseignementList;
    }

    public AnneeAcademique getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(AnneeAcademique idAnnee) {
        this.idAnnee = idAnnee;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idSemestre != null ? idSemestre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Semestre)) {
            return false;
        }
        Semestre other = (Semestre) object;
        if ((this.idSemestre == null && other.idSemestre != null) || (this.idSemestre != null && !this.idSemestre.equals(other.idSemestre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Semestre[ idSemestre=" + idSemestre + " ]";
    }
    
}
