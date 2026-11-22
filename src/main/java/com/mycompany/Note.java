/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "note")
@NamedQueries({
    @NamedQuery(name = "Note.findAll", query = "SELECT n FROM Note n"),
    @NamedQuery(name = "Note.findByIdNote", query = "SELECT n FROM Note n WHERE n.idNote = :idNote"),
    @NamedQuery(name = "Note.findByValeur", query = "SELECT n FROM Note n WHERE n.valeur = :valeur"),
    @NamedQuery(name = "Note.findByStatutPresence", query = "SELECT n FROM Note n WHERE n.statutPresence = :statutPresence"),
    @NamedQuery(name = "Note.findByCommentaire", query = "SELECT n FROM Note n WHERE n.commentaire = :commentaire"),
    @NamedQuery(name = "Note.findByDateSaisie", query = "SELECT n FROM Note n WHERE n.dateSaisie = :dateSaisie"),
    @NamedQuery(name = "Note.findByDateModification", query = "SELECT n FROM Note n WHERE n.dateModification = :dateModification")})
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_note")
    private Long idNote;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @DecimalMin("0")
    @DecimalMax("20")
    @Column(name = "valeur")
    private BigDecimal valeur;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "statut_presence")
    private String statutPresence;
    @Size(max = 2147483647)
    @Column(name = "commentaire")
    private String commentaire;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date_saisie")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSaisie;
    @Column(name = "date_modification")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification;
    @JoinColumn(name = "id_evaluation", referencedColumnName = "id_evaluation")
    @ManyToOne(optional = false)
    private Evaluation idEvaluation;
    @JoinColumn(name = "id_inscription", referencedColumnName = "id_inscription")
    @ManyToOne(optional = false)
    private Inscription idInscription;
    @OneToMany(mappedBy = "idNote")
    private List<HistoriqueNote> historiqueNoteList;

    public Note() {
    }

    public Note(Long idNote) {
        this.idNote = idNote;
    }

    public Note(Long idNote, String statutPresence, Date dateSaisie) {
        this.idNote = idNote;
        this.statutPresence = statutPresence;
        this.dateSaisie = dateSaisie;
    }

    public Long getIdNote() {
        return idNote;
    }

    public void setIdNote(Long idNote) {
        this.idNote = idNote;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public String getStatutPresence() {
        return statutPresence;
    }

    public void setStatutPresence(String statutPresence) {
        this.statutPresence = statutPresence;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Date getDateSaisie() {
        return dateSaisie;
    }

    public void setDateSaisie(Date dateSaisie) {
        this.dateSaisie = dateSaisie;
    }

    public Date getDateModification() {
        return dateModification;
    }

    public void setDateModification(Date dateModification) {
        this.dateModification = dateModification;
    }

    public Evaluation getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(Evaluation idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public Inscription getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(Inscription idInscription) {
        this.idInscription = idInscription;
    }

    public List<HistoriqueNote> getHistoriqueNoteList() {
        return historiqueNoteList;
    }

    public void setHistoriqueNoteList(List<HistoriqueNote> historiqueNoteList) {
        this.historiqueNoteList = historiqueNoteList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idNote != null ? idNote.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Note)) {
            return false;
        }
        Note other = (Note) object;
        if ((this.idNote == null && other.idNote != null) || (this.idNote != null && !this.idNote.equals(other.idNote))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Note[ idNote=" + idNote + " ]";
    }
    
}
