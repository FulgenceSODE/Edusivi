/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 * @author T490
 */
@Embeddable
public class CompositionUePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_ue")
    private long idUe;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_ec")
    private long idEc;

    public CompositionUePK() {
    }

    public CompositionUePK(long idUe, long idEc) {
        this.idUe = idUe;
        this.idEc = idEc;
    }

    public long getIdUe() {
        return idUe;
    }

    public void setIdUe(long idUe) {
        this.idUe = idUe;
    }

    public long getIdEc() {
        return idEc;
    }

    public void setIdEc(long idEc) {
        this.idEc = idEc;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idUe;
        hash += (int) idEc;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CompositionUePK)) {
            return false;
        }
        CompositionUePK other = (CompositionUePK) object;
        if (this.idUe != other.idUe) {
            return false;
        }
        if (this.idEc != other.idEc) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.CompositionUePK[ idUe=" + idUe + ", idEc=" + idEc + " ]";
    }
    
}
