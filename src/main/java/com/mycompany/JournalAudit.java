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
@Table(name = "journal_audit")
@NamedQueries({
    @NamedQuery(name = "JournalAudit.findAll", query = "SELECT j FROM JournalAudit j"),
    @NamedQuery(name = "JournalAudit.findByIdAudit", query = "SELECT j FROM JournalAudit j WHERE j.idAudit = :idAudit"),
    @NamedQuery(name = "JournalAudit.findByAction", query = "SELECT j FROM JournalAudit j WHERE j.action = :action"),
    @NamedQuery(name = "JournalAudit.findByTypeEntite", query = "SELECT j FROM JournalAudit j WHERE j.typeEntite = :typeEntite"),
    @NamedQuery(name = "JournalAudit.findByIdEntite", query = "SELECT j FROM JournalAudit j WHERE j.idEntite = :idEntite"),
    @NamedQuery(name = "JournalAudit.findByDateAction", query = "SELECT j FROM JournalAudit j WHERE j.dateAction = :dateAction")})
public class JournalAudit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_audit")
    private Long idAudit;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "action")
    private String action;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "type_entite")
    private String typeEntite;
    @Column(name = "id_entite")
    @Size(max = 100)
    private String idEntite;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_action")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAction;
    @Column(name = "details", columnDefinition = "text")
    private String details;
    @JoinColumn(name = "id_utilisateur", referencedColumnName = "id_utilisateur")
    @ManyToOne
    private Utilisateur idUtilisateur;

    public JournalAudit() {
    }

    public JournalAudit(Long idAudit) {
        this.idAudit = idAudit;
    }

    public JournalAudit(Long idAudit, String action, String typeEntite, Date dateAction) {
        this.idAudit = idAudit;
        this.action = action;
        this.typeEntite = typeEntite;
        this.dateAction = dateAction;
    }

    public Long getIdAudit() {
        return idAudit;
    }

    public void setIdAudit(Long idAudit) {
        this.idAudit = idAudit;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTypeEntite() {
        return typeEntite;
    }

    public void setTypeEntite(String typeEntite) {
        this.typeEntite = typeEntite;
    }

    public String getIdEntite() {
        return idEntite;
    }

    public void setIdEntite(String idEntite) {
        this.idEntite = idEntite;
    }

    public Date getDateAction() {
        return dateAction;
    }

    public void setDateAction(Date dateAction) {
        this.dateAction = dateAction;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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
        hash += (idAudit != null ? idAudit.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof JournalAudit)) {
            return false;
        }
        JournalAudit other = (JournalAudit) object;
        if ((this.idAudit == null && other.idAudit != null) || (this.idAudit != null && !this.idAudit.equals(other.idAudit))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.JournalAudit[ idAudit=" + idAudit + " ]";
    }
    
}
