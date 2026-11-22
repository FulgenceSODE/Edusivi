/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.validation.constraints.DecimalMin;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "composition_ue")
@NamedQueries({
    @NamedQuery(name = "CompositionUe.findAll", query = "SELECT c FROM CompositionUe c"),
    @NamedQuery(name = "CompositionUe.findByIdUe", query = "SELECT c FROM CompositionUe c WHERE c.compositionUePK.idUe = :idUe"),
    @NamedQuery(name = "CompositionUe.findByIdEc", query = "SELECT c FROM CompositionUe c WHERE c.compositionUePK.idEc = :idEc"),
    @NamedQuery(name = "CompositionUe.findByCoefficient", query = "SELECT c FROM CompositionUe c WHERE c.coefficient = :coefficient"),
    @NamedQuery(name = "CompositionUe.findByOrdre", query = "SELECT c FROM CompositionUe c WHERE c.ordre = :ordre")})
public class CompositionUe implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CompositionUePK compositionUePK;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Column(name = "coefficient")
    private BigDecimal coefficient;
    @Column(name = "ordre")
    private Integer ordre;
    @JoinColumn(name = "id_ec", referencedColumnName = "id_ec", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ElementConstitutif elementConstitutif;
    @JoinColumn(name = "id_ue", referencedColumnName = "id_ue", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private UniteEnseignement uniteEnseignement;

    public CompositionUe() {
    }

    public CompositionUe(CompositionUePK compositionUePK) {
        this.compositionUePK = compositionUePK;
    }

    public CompositionUe(CompositionUePK compositionUePK, BigDecimal coefficient) {
        this.compositionUePK = compositionUePK;
        this.coefficient = coefficient;
    }

    public CompositionUe(long idUe, long idEc) {
        this.compositionUePK = new CompositionUePK(idUe, idEc);
    }

    public CompositionUePK getCompositionUePK() {
        return compositionUePK;
    }

    public void setCompositionUePK(CompositionUePK compositionUePK) {
        this.compositionUePK = compositionUePK;
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public ElementConstitutif getElementConstitutif() {
        return elementConstitutif;
    }

    public void setElementConstitutif(ElementConstitutif elementConstitutif) {
        this.elementConstitutif = elementConstitutif;
    }

    public UniteEnseignement getUniteEnseignement() {
        return uniteEnseignement;
    }

    public void setUniteEnseignement(UniteEnseignement uniteEnseignement) {
        this.uniteEnseignement = uniteEnseignement;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (compositionUePK != null ? compositionUePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CompositionUe)) {
            return false;
        }
        CompositionUe other = (CompositionUe) object;
        if ((this.compositionUePK == null && other.compositionUePK != null) || (this.compositionUePK != null && !this.compositionUePK.equals(other.compositionUePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.CompositionUe[ compositionUePK=" + compositionUePK + " ]";
    }
    
}
