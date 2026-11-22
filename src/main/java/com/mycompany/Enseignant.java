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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "enseignant")
@NamedQueries({
    @NamedQuery(name = "Enseignant.findAll", query = "SELECT e FROM Enseignant e"),
    @NamedQuery(name = "Enseignant.findByIdEnseignant", query = "SELECT e FROM Enseignant e WHERE e.idEnseignant = :idEnseignant"),
    @NamedQuery(name = "Enseignant.findByMatricule", query = "SELECT e FROM Enseignant e WHERE e.matricule = :matricule"),
    @NamedQuery(name = "Enseignant.findBySpecialite", query = "SELECT e FROM Enseignant e WHERE e.specialite = :specialite")})
public class Enseignant implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_enseignant")
    private Long idEnseignant;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "matricule")
    private String matricule;
    @Size(max = 150)
    @Column(name = "specialite")
    private String specialite;
    @OneToMany(mappedBy = "idEnseignant")
    private List<Enseignement> enseignementList;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @OneToOne(optional = false)
    private Utilisateur idUtilisateur;

    public Enseignant() {
    }

    public Enseignant(Long idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public Enseignant(Long idEnseignant, String matricule) {
        this.idEnseignant = idEnseignant;
        this.matricule = matricule;
    }

    public Long getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(Long idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public List<Enseignement> getEnseignementList() {
        return enseignementList;
    }

    public void setEnseignementList(List<Enseignement> enseignementList) {
        this.enseignementList = enseignementList;
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
        hash += (idEnseignant != null ? idEnseignant.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Enseignant)) {
            return false;
        }
        Enseignant other = (Enseignant) object;
        if ((this.idEnseignant == null && other.idEnseignant != null) || (this.idEnseignant != null && !this.idEnseignant.equals(other.idEnseignant))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Enseignant[ idEnseignant=" + idEnseignant + " ]";
    }
    
}
