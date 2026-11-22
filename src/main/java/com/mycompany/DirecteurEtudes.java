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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "directeur_etudes")
@NamedQueries({
    @NamedQuery(name = "DirecteurEtudes.findAll", query = "SELECT d FROM DirecteurEtudes d"),
    @NamedQuery(name = "DirecteurEtudes.findByIdDirecteurEtudes", query = "SELECT d FROM DirecteurEtudes d WHERE d.idDirecteurEtudes = :idDirecteurEtudes")})
public class DirecteurEtudes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_directeur_etudes")
    private Long idDirecteurEtudes;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @OneToOne(optional = false)
    private Utilisateur idUtilisateur;

    public DirecteurEtudes() {
    }

    public DirecteurEtudes(Long idDirecteurEtudes) {
        this.idDirecteurEtudes = idDirecteurEtudes;
    }

    public Long getIdDirecteurEtudes() {
        return idDirecteurEtudes;
    }

    public void setIdDirecteurEtudes(Long idDirecteurEtudes) {
        this.idDirecteurEtudes = idDirecteurEtudes;
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
        hash += (idDirecteurEtudes != null ? idDirecteurEtudes.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DirecteurEtudes)) {
            return false;
        }
        DirecteurEtudes other = (DirecteurEtudes) object;
        if ((this.idDirecteurEtudes == null && other.idDirecteurEtudes != null) || (this.idDirecteurEtudes != null && !this.idDirecteurEtudes.equals(other.idDirecteurEtudes))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.DirecteurEtudes[ idDirecteurEtudes=" + idDirecteurEtudes + " ]";
    }
    
}
