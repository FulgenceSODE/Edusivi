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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author T490
 */
@Entity
@Table(name = "enseignement")
@NamedQueries({
    @NamedQuery(name = "Enseignement.findAll", query = "SELECT e FROM Enseignement e"),
    @NamedQuery(name = "Enseignement.findByIdEnseignement", query = "SELECT e FROM Enseignement e WHERE e.idEnseignement = :idEnseignement"),
    @NamedQuery(name = "Enseignement.findByVolumeHorairePrevu", query = "SELECT e FROM Enseignement e WHERE e.volumeHorairePrevu = :volumeHorairePrevu"),
    @NamedQuery(name = "Enseignement.findByStatut", query = "SELECT e FROM Enseignement e WHERE e.statut = :statut")})
public class Enseignement implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id_enseignement")
    private Long idEnseignement;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "volume_horaire_prevu")
    private Integer volumeHorairePrevu;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "statut")
    private String statut;
    @OneToMany(mappedBy = "idEnseignement")
    private List<Chapitre> chapitreList;
    @OneToMany(mappedBy = "idEnseignement")
    private List<Evaluation> evaluationList;
    @OneToMany(mappedBy = "idEnseignement")
    private List<SeanceCours> seanceCoursList;
    @JoinColumn(name = "id_ec", referencedColumnName = "id_ec")
    @ManyToOne(optional = false)
    private ElementConstitutif idEc;
    @JoinColumn(name = "id_enseignant", referencedColumnName = "id_enseignant")
    @ManyToOne(optional = false)
    private Enseignant idEnseignant;
    @JoinColumn(name = "id_groupe", referencedColumnName = "id_groupe")
    @ManyToOne(optional = false)
    private Groupe idGroupe;
    @JoinColumn(name = "id_semestre", referencedColumnName = "id_semestre")
    @ManyToOne(optional = false)
    private Semestre idSemestre;

    public Enseignement() {
    }

    public Enseignement(Long idEnseignement) {
        this.idEnseignement = idEnseignement;
    }

    public Enseignement(Long idEnseignement, String statut) {
        this.idEnseignement = idEnseignement;
        this.statut = statut;
    }

    public Long getIdEnseignement() {
        return idEnseignement;
    }

    public void setIdEnseignement(Long idEnseignement) {
        this.idEnseignement = idEnseignement;
    }

    public Integer getVolumeHorairePrevu() {
        return volumeHorairePrevu;
    }

    public void setVolumeHorairePrevu(Integer volumeHorairePrevu) {
        this.volumeHorairePrevu = volumeHorairePrevu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<Chapitre> getChapitreList() {
        return chapitreList;
    }

    public void setChapitreList(List<Chapitre> chapitreList) {
        this.chapitreList = chapitreList;
    }

    public List<Evaluation> getEvaluationList() {
        return evaluationList;
    }

    public void setEvaluationList(List<Evaluation> evaluationList) {
        this.evaluationList = evaluationList;
    }

    public List<SeanceCours> getSeanceCoursList() {
        return seanceCoursList;
    }

    public void setSeanceCoursList(List<SeanceCours> seanceCoursList) {
        this.seanceCoursList = seanceCoursList;
    }

    public ElementConstitutif getIdEc() {
        return idEc;
    }

    public void setIdEc(ElementConstitutif idEc) {
        this.idEc = idEc;
    }

    public Enseignant getIdEnseignant() {
        return idEnseignant;
    }

    public void setIdEnseignant(Enseignant idEnseignant) {
        this.idEnseignant = idEnseignant;
    }

    public Groupe getIdGroupe() {
        return idGroupe;
    }

    public void setIdGroupe(Groupe idGroupe) {
        this.idGroupe = idGroupe;
    }

    public Semestre getIdSemestre() {
        return idSemestre;
    }

    public void setIdSemestre(Semestre idSemestre) {
        this.idSemestre = idSemestre;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idEnseignement != null ? idEnseignement.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Enseignement)) {
            return false;
        }
        Enseignement other = (Enseignement) object;
        if ((this.idEnseignement == null && other.idEnseignement != null) || (this.idEnseignement != null && !this.idEnseignement.equals(other.idEnseignement))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.Enseignement[ idEnseignement=" + idEnseignement + " ]";
    }
    
}
