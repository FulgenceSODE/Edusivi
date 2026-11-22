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
@Table(name = "evaluation")
@NamedQueries({
    @NamedQuery(name = "Evaluation.findAll", query = "SELECT e FROM Evaluation e"),
    @NamedQuery(name = "Evaluation.findByIdEvaluation", query = "SELECT e FROM Evaluation e WHERE e.idEvaluation = :idEvaluation"),
    @NamedQuery(name = "Evaluation.findByTitre", query = "SELECT e FROM Evaluation e WHERE e.titre = :titre"),
    @NamedQuery(name = "Evaluation.findByCategorie", query = "SELECT e FROM Evaluation e WHERE e.categorie = :categorie"),
    @NamedQuery(name = "Evaluation.findByDateEvaluation", query = "SELECT e FROM Evaluation e WHERE e.dateEvaluation = :dateEvaluation"),
    @NamedQuery(name = "Evaluation.findByBareme", query = "SELECT e FROM Evaluation e WHERE e.bareme = :bareme"),
    @NamedQuery(name = "Evaluation.findByStatutPublication", query = "SELECT e FROM Evaluation e WHERE e.statutPublication = :statutPublication")})
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_evaluation")
    private Long idEvaluation;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "titre")
    private String titre;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "categorie")
    private String categorie;
    @Column(name = "date_evaluation")
    @Temporal(TemporalType.DATE)
    private Date dateEvaluation;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "bareme")
    private BigDecimal bareme;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut_publication")
    private String statutPublication;
    @OneToMany(mappedBy = "idEvaluation")
    private List<Note> noteList;
    @JoinColumn(name = "id_enseignement", referencedColumnName = "id_enseignement")
    @ManyToOne(optional = false)
    private Enseignement idEnseignement;

    public Evaluation() {
    }

    public Evaluation(Long idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public Evaluation(Long idEvaluation, String titre, String categorie, BigDecimal bareme, String statutPublication) {
        this.idEvaluation = idEvaluation;
        this.titre = titre;
        this.categorie = categorie;
        this.bareme = bareme;
        this.statutPublication = statutPublication;
    }

    public Long getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(Long idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public Date getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(Date dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    public BigDecimal getBareme() {
        return bareme;
    }

    public void setBareme(BigDecimal bareme) {
        this.bareme = bareme;
    }

    public String getStatutPublication() {
        return statutPublication;
    }

    public void setStatutPublication(String statutPublication) {
        this.statutPublication = statutPublication;
    }

    public List<Note> getNoteList() {
        return noteList;
    }

    public void setNoteList(List<Note> noteList) {
        this.noteList = noteList;
    }

    public Enseignement getIdEnseignement() {
        return idEnseignement;
    }

    public void setIdEnseignement(Enseignement idEnseignement) {
        this.idEnseignement = idEnseignement;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEvaluation != null ? idEvaluation.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Evaluation)) {
            return false;
        }
        Evaluation other = (Evaluation) object;
        if ((this.idEvaluation == null && other.idEvaluation != null) || (this.idEvaluation != null && !this.idEvaluation.equals(other.idEvaluation))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Evaluation[ idEvaluation=" + idEvaluation + " ]";
    }
    
}
