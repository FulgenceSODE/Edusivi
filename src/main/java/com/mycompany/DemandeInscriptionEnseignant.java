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
@Table(name = "demande_inscription_enseignant")
@NamedQueries({
    @NamedQuery(name = "DemandeInscriptionEnseignant.findAll", query = "SELECT d FROM DemandeInscriptionEnseignant d"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByIdDemande", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.idDemande = :idDemande"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByEmail", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.email = :email"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByMotDePasseHash", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.motDePasseHash = :motDePasseHash"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByNom", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.nom = :nom"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByPrenom", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.prenom = :prenom"),
    @NamedQuery(name = "DemandeInscriptionEnseignant.findByDateDemande", query = "SELECT d FROM DemandeInscriptionEnseignant d WHERE d.dateDemande = :dateDemande")})
public class DemandeInscriptionEnseignant implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_demande")
    private Long idDemande;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 254)
    @Column(name = "email")
    private String email;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "mot_de_passe_hash")
    private String motDePasseHash;
    @Size(max = 150)
    @Column(name = "specialite")
    private String specialite;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "nom")
    private String nom;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "prenom")
    private String prenom;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_demande")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDemande;

    public DemandeInscriptionEnseignant() {
    }

    public DemandeInscriptionEnseignant(Long idDemande) {
        this.idDemande = idDemande;
    }

    public DemandeInscriptionEnseignant(Long idDemande, String email, String motDePasseHash, String nom, String prenom, Date dateDemande) {
        this.idDemande = idDemande;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
        this.nom = nom;
        this.prenom = prenom;
        this.dateDemande = dateDemande;
    }

    public Long getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(Long idDemande) {
        this.idDemande = idDemande;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasseHash() {
        return motDePasseHash;
    }

    public void setMotDePasseHash(String motDePasseHash) {
        this.motDePasseHash = motDePasseHash;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idDemande != null ? idDemande.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DemandeInscriptionEnseignant)) {
            return false;
        }
        DemandeInscriptionEnseignant other = (DemandeInscriptionEnseignant) object;
        if ((this.idDemande == null && other.idDemande != null) || (this.idDemande != null && !this.idDemande.equals(other.idDemande))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.DemandeInscriptionEnseignant[ idDemande=" + idDemande + " ]";
    }
    
}
