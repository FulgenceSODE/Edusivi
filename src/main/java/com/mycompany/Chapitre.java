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
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "chapitre")
@NamedQueries({
    @NamedQuery(name = "Chapitre.findAll", query = "SELECT c FROM Chapitre c"),
    @NamedQuery(name = "Chapitre.findByIdChapitre", query = "SELECT c FROM Chapitre c WHERE c.idChapitre = :idChapitre"),
    @NamedQuery(name = "Chapitre.findByTitre", query = "SELECT c FROM Chapitre c WHERE c.titre = :titre"),
    @NamedQuery(name = "Chapitre.findByDescription", query = "SELECT c FROM Chapitre c WHERE c.description = :description"),
    @NamedQuery(name = "Chapitre.findByOrdre", query = "SELECT c FROM Chapitre c WHERE c.ordre = :ordre")})
public class Chapitre implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_chapitre")
    private Long idChapitre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "titre")
    private String titre;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Column(name = "ordre")
    private Integer ordre;
    @JoinColumn(name = "id_enseignement", referencedColumnName = "id_enseignement")
    @ManyToOne(optional = false)
    private Enseignement idEnseignement;

    public Chapitre() {
    }

    public Chapitre(Long idChapitre) {
        this.idChapitre = idChapitre;
    }

    public Chapitre(Long idChapitre, String titre) {
        this.idChapitre = idChapitre;
        this.titre = titre;
    }

    public Long getIdChapitre() {
        return idChapitre;
    }

    public void setIdChapitre(Long idChapitre) {
        this.idChapitre = idChapitre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Enseignement getIdEnseignement() {
        return idEnseignement;
    }

    public void setIdEnseignement(Enseignement idEnseignement) {
        this.idEnseignement = idEnseignement;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idChapitre != null ? idChapitre.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Chapitre)) {
            return false;
        }
        Chapitre other = (Chapitre) object;
        if ((this.idChapitre == null && other.idChapitre != null) || (this.idChapitre != null && !this.idChapitre.equals(other.idChapitre))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Chapitre[ idChapitre=" + idChapitre + " ]";
    }
    
}
