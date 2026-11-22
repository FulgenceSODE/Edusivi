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
public class NoteEvaluationPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "id_evaluation")
    private long idEvaluation;
    @Basic(optional = false)
    @NotNull
    @Column(name = "id_inscription")
    private long idInscription;

    public NoteEvaluationPK() {
    }

    public NoteEvaluationPK(long idEvaluation, long idInscription) {
        this.idEvaluation = idEvaluation;
        this.idInscription = idInscription;
    }

    public long getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(long idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public long getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(long idInscription) {
        this.idInscription = idInscription;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) idEvaluation;
        hash += (int) idInscription;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NoteEvaluationPK)) {
            return false;
        }
        NoteEvaluationPK other = (NoteEvaluationPK) object;
        if (this.idEvaluation != other.idEvaluation) {
            return false;
        }
        if (this.idInscription != other.idInscription) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.mycompany.NoteEvaluationPK[ idEvaluation=" + idEvaluation + ", idInscription=" + idInscription + " ]";
    }
    
}
