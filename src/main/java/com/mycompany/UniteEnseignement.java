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
@Table(name = "unite_enseignement")
@NamedQueries({
    @NamedQuery(name = "UniteEnseignement.findAll", query = "SELECT u FROM UniteEnseignement u"),
    @NamedQuery(name = "UniteEnseignement.findByIdUe", query = "SELECT u FROM UniteEnseignement u WHERE u.idUe = :idUe"),
    @NamedQuery(name = "UniteEnseignement.findByCode", query = "SELECT u FROM UniteEnseignement u WHERE u.code = :code"),
    @NamedQuery(name = "UniteEnseignement.findByLibelle", query = "SELECT u FROM UniteEnseignement u WHERE u.libelle = :libelle"),
    @NamedQuery(name = "UniteEnseignement.findByDescription", query = "SELECT u FROM UniteEnseignement u WHERE u.description = :description")})
public class UniteEnseignement implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_ue")
    private Long idUe;
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
    @OneToMany(mappedBy = "uniteEnseignement")
    private List<CompositionUe> compositionUeList;
    @JoinColumn(name = "id_semestre", referencedColumnName = "id_semestre")
    @ManyToOne
    private Semestre idSemestre;

    public UniteEnseignement() {
    }

    public UniteEnseignement(Long idUe) {
        this.idUe = idUe;
    }

    public UniteEnseignement(Long idUe, String code, String libelle) {
        this.idUe = idUe;
        this.code = code;
        this.libelle = libelle;
    }

    public Long getIdUe() {
        return idUe;
    }

    public void setIdUe(Long idUe) {
        this.idUe = idUe;
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

    public Semestre getIdSemestre() {
        return idSemestre;
    }

    public void setIdSemestre(Semestre idSemestre) {
        this.idSemestre = idSemestre;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idUe != null ? idUe.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UniteEnseignement)) {
            return false;
        }
        UniteEnseignement other = (UniteEnseignement) object;
        if ((this.idUe == null && other.idUe != null) || (this.idUe != null && !this.idUe.equals(other.idUe))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.UniteEnseignement[ idUe=" + idUe + " ]";
    }
    
}
