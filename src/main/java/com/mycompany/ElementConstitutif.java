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
@Table(name = "element_constitutif")
@NamedQueries({
    @NamedQuery(name = "ElementConstitutif.findAll", query = "SELECT e FROM ElementConstitutif e"),
    @NamedQuery(name = "ElementConstitutif.findByIdEc", query = "SELECT e FROM ElementConstitutif e WHERE e.idEc = :idEc"),
    @NamedQuery(name = "ElementConstitutif.findByCode", query = "SELECT e FROM ElementConstitutif e WHERE e.code = :code"),
    @NamedQuery(name = "ElementConstitutif.findByLibelle", query = "SELECT e FROM ElementConstitutif e WHERE e.libelle = :libelle"),
    @NamedQuery(name = "ElementConstitutif.findByDescription", query = "SELECT e FROM ElementConstitutif e WHERE e.description = :description")})
public class ElementConstitutif implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_ec")
    private Long idEc;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "code")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 150)
    @Column(name = "libelle")
    private String libelle;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @OneToMany(mappedBy = "elementConstitutif")
    private List<CompositionUe> compositionUeList;
    @OneToMany(mappedBy = "idEc")
    private List<Enseignement> enseignementList;
    @OneToMany(mappedBy = "idEc")
    private List<AffectationEnseignement> affectationEnseignementList;

    public ElementConstitutif() {
    }

    public ElementConstitutif(Long idEc) {
        this.idEc = idEc;
    }

    public ElementConstitutif(Long idEc, String code, String libelle) {
        this.idEc = idEc;
        this.code = code;
        this.libelle = libelle;
    }

    public Long getIdEc() {
        return idEc;
    }

    public void setIdEc(Long idEc) {
        this.idEc = idEc;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CompositionUe> getCompositionUeList() {
        return compositionUeList;
    }

    public void setCompositionUeList(List<CompositionUe> compositionUeList) {
        this.compositionUeList = compositionUeList;
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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEc != null ? idEc.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ElementConstitutif)) {
            return false;
        }
        ElementConstitutif other = (ElementConstitutif) object;
        if ((this.idEc == null && other.idEc != null) || (this.idEc != null && !this.idEc.equals(other.idEc))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.ElementConstitutif[ idEc=" + idEc + " ]";
    }
    
}
