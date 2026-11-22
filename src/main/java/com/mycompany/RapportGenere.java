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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "rapport_genere")
@NamedQueries({
    @NamedQuery(name = "RapportGenere.findAll", query = "SELECT r FROM RapportGenere r"),
    @NamedQuery(name = "RapportGenere.findByIdRapport", query = "SELECT r FROM RapportGenere r WHERE r.idRapport = :idRapport"),
    @NamedQuery(name = "RapportGenere.findByTypeRapport", query = "SELECT r FROM RapportGenere r WHERE r.typeRapport = :typeRapport"),
    @NamedQuery(name = "RapportGenere.findByDateGeneration", query = "SELECT r FROM RapportGenere r WHERE r.dateGeneration = :dateGeneration"),
    @NamedQuery(name = "RapportGenere.findByFormat", query = "SELECT r FROM RapportGenere r WHERE r.format = :format")})
public class RapportGenere implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_rapport")
    private Long idRapport;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type_rapport")
    private String typeRapport;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_generation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateGeneration;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "format")
    private String format;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne(optional = false)
    private Utilisateur idUtilisateur;

    public RapportGenere() {
    }

    public RapportGenere(Long idRapport) {
        this.idRapport = idRapport;
    }

    public RapportGenere(Long idRapport, String typeRapport, Date dateGeneration, String format) {
        this.idRapport = idRapport;
        this.typeRapport = typeRapport;
        this.dateGeneration = dateGeneration;
        this.format = format;
    }

    public Long getIdRapport() {
        return idRapport;
    }

    public void setIdRapport(Long idRapport) {
        this.idRapport = idRapport;
    }

    public String getTypeRapport() {
        return typeRapport;
    }

    public void setTypeRapport(String typeRapport) {
        this.typeRapport = typeRapport;
    }

    public Date getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(Date dateGeneration) {
        this.dateGeneration = dateGeneration;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
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
        hash += (idRapport != null ? idRapport.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RapportGenere)) {
            return false;
        }
        RapportGenere other = (RapportGenere) object;
        if ((this.idRapport == null && other.idRapport != null) || (this.idRapport != null && !this.idRapport.equals(other.idRapport))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.RapportGenere[ idRapport=" + idRapport + " ]";
    }
    
}
