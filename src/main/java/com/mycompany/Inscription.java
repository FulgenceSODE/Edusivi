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
@Table(name = "inscription")
@NamedQueries({
    @NamedQuery(name = "Inscription.findAll", query = "SELECT i FROM Inscription i"),
    @NamedQuery(name = "Inscription.findByIdInscription", query = "SELECT i FROM Inscription i WHERE i.idInscription = :idInscription"),
    @NamedQuery(name = "Inscription.findByDateInscription", query = "SELECT i FROM Inscription i WHERE i.dateInscription = :dateInscription"),
    @NamedQuery(name = "Inscription.findByStatut", query = "SELECT i FROM Inscription i WHERE i.statut = :statut")})
public class Inscription implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_inscription")
    private Long idInscription;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_inscription")
    @Temporal(TemporalType.DATE)
    private Date dateInscription;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut")
    private String statut;
    @OneToMany(mappedBy = "idInscription")
    private List<Note> noteList;
    @JoinColumn(name = "id_annee", referencedColumnName = "id_annee")
    @ManyToOne(optional = false)
    private AnneeAcademique idAnnee;
    @JoinColumn(name = "id_etudiant", referencedColumnName = "id_etudiant")
    @ManyToOne(optional = false)
    private Etudiant idEtudiant;
    @JoinColumn(name = "id_groupe", referencedColumnName = "id_groupe")
    @ManyToOne(optional = false)
    private Groupe idGroupe;

    public Inscription() {
    }

    public Inscription(Long idInscription) {
        this.idInscription = idInscription;
    }

    public Inscription(Long idInscription, Date dateInscription, String statut) {
        this.idInscription = idInscription;
        this.dateInscription = dateInscription;
        this.statut = statut;
    }

    public Long getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(Long idInscription) {
        this.idInscription = idInscription;
    }

    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public AnneeAcademique getIdAnnee() {
        return idAnnee;
    }

    public void setIdAnnee(AnneeAcademique idAnnee) {
        this.idAnnee = idAnnee;
    }

    public Etudiant getIdEtudiant() {
        return idEtudiant;
    }

    public void setIdEtudiant(Etudiant idEtudiant) {
        this.idEtudiant = idEtudiant;
    }

    public Groupe getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(Groupe idGroupe) {
        this.idGroupe = idGroupe;
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
        if (!(object instanceof Inscription)) {
            return false;
        }
        Inscription other = (Inscription) object;
        if ((this.idInscription == null && other.idInscription != null) || (this.idInscription != null && !this.idInscription.equals(other.idInscription))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Inscription[ idInscription=" + idInscription + " ]";
    }
    
}
