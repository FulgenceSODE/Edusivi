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
public class UtilisateurRolePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_utilisateur")
    private long idUtilisateur;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_role")
    private long idRole;

    public UtilisateurRolePK() {
    }

    public UtilisateurRolePK(long idUtilisateur, long idRole) {
        this.idUtilisateur = idUtilisateur;
        this.idRole = idRole;
    }

    public long getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(long idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public long getIdRole() {
        return idRole;
    }

    public void setIdRole(long idRole) {
        this.idRole = idRole;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idUtilisateur;
        hash += (int) idRole;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UtilisateurRolePK)) {
            return false;
        }
        UtilisateurRolePK other = (UtilisateurRolePK) object;
        if (this.idUtilisateur != other.idUtilisateur) {
            return false;
        }
        if (this.idRole != other.idRole) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.UtilisateurRolePK[ idUtilisateur=" + idUtilisateur + ", idRole=" + idRole + " ]";
    }
    
}
