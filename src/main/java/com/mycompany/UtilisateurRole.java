/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "utilisateur_role")
@NamedQueries({
    @NamedQuery(name = "UtilisateurRole.findAll", query = "SELECT u FROM UtilisateurRole u"),
    @NamedQuery(name = "UtilisateurRole.findByIdUtilisateur", query = "SELECT u FROM UtilisateurRole u WHERE u.utilisateurRolePK.idUtilisateur = :idUtilisateur"),
    @NamedQuery(name = "UtilisateurRole.findByIdRole", query = "SELECT u FROM UtilisateurRole u WHERE u.utilisateurRolePK.idRole = :idRole"),
    @NamedQuery(name = "UtilisateurRole.findByDateAttribution", query = "SELECT u FROM UtilisateurRole u WHERE u.dateAttribution = :dateAttribution")})
public class UtilisateurRole implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UtilisateurRolePK utilisateurRolePK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_attribution")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAttribution;
    @JoinColumn(name = "id_role", referencedColumnName = "id_role", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Role role;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Utilisateur utilisateur;

    public UtilisateurRole() {
    }

    public UtilisateurRole(UtilisateurRolePK utilisateurRolePK) {
        this.utilisateurRolePK = utilisateurRolePK;
    }

    public UtilisateurRole(UtilisateurRolePK utilisateurRolePK, Date dateAttribution) {
        this.utilisateurRolePK = utilisateurRolePK;
        this.dateAttribution = dateAttribution;
    }

    public UtilisateurRole(long idUtilisateur, long idRole) {
        this.utilisateurRolePK = new UtilisateurRolePK(idUtilisateur, idRole);
    }

    public UtilisateurRolePK getUtilisateurRolePK() {
        return utilisateurRolePK;
    }

    public void setUtilisateurRolePK(UtilisateurRolePK utilisateurRolePK) {
        this.utilisateurRolePK = utilisateurRolePK;
    }

    public Date getDateAttribution() {
        return dateAttribution;
    }

    public void setDateAttribution(Date dateAttribution) {
        this.dateAttribution = dateAttribution;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (utilisateurRolePK != null ? utilisateurRolePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UtilisateurRole)) {
            return false;
        }
        UtilisateurRole other = (UtilisateurRole) object;
        if ((this.utilisateurRolePK == null && other.utilisateurRolePK != null) || (this.utilisateurRolePK != null && !this.utilisateurRolePK.equals(other.utilisateurRolePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.UtilisateurRole[ utilisateurRolePK=" + utilisateurRolePK + " ]";
    }
    
}
