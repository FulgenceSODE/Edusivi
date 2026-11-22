package com.mycompany.web;

import com.mycompany.CompositionUe;
import com.mycompany.InscriptionEtudiant;
import com.mycompany.NoteEvaluation;
import jakarta.persistence.EntityManager;
import java.util.List;

public final class SemesterGradeService {

    private final EntityManager em;

    public SemesterGradeService(EntityManager em) {
        this.em = em;
    }

    public List<SemesterGradeCalculator.StudentResult> calculateGroup(Long groupId, Long semesterId, Long yearId) {
        List<InscriptionEtudiant> enrollments = em.createQuery(
                "SELECT i FROM InscriptionEtudiant i JOIN FETCH i.idEtudiant "
                + "WHERE i.idGroupe.idGroupe = :groupId AND i.idAnnee.idAnnee = :yearId "
                + "AND UPPER(i.statut) = 'VALIDE' "
                + "ORDER BY i.idEtudiant.nom, i.idEtudiant.prenom, i.idEtudiant.matricule",
                InscriptionEtudiant.class)
                .setParameter("groupId", groupId)
                .setParameter("yearId", yearId)
                .getResultList();
        return calculate(enrollments, groupId, semesterId, yearId);
    }

    public SemesterGradeCalculator.StudentResult calculateEnrollment(
            InscriptionEtudiant enrollment, Long semesterId, Long yearId) {
        return calculate(List.of(enrollment), enrollment.getIdGroupe().getIdGroupe(), semesterId, yearId).get(0);
    }

    private List<SemesterGradeCalculator.StudentResult> calculate(
            List<InscriptionEtudiant> enrollments,
            Long groupId,
            Long semesterId,
            Long yearId) {
        List<CompositionUe> compositions = em.createQuery(
                "SELECT c FROM CompositionUe c "
                + "JOIN FETCH c.uniteEnseignement JOIN FETCH c.elementConstitutif "
                + "WHERE c.uniteEnseignement.idSemestre.idSemestre = :semesterId "
                + "ORDER BY c.uniteEnseignement.code, c.ordre, c.elementConstitutif.code",
                CompositionUe.class)
                .setParameter("semesterId", semesterId)
                .getResultList();
        List<NoteEvaluation> notes = em.createQuery(
                "SELECT n FROM NoteEvaluation n "
                + "JOIN FETCH n.inscriptionEtudiant "
                + "JOIN FETCH n.evaluationCours "
                + "JOIN FETCH n.evaluationCours.idAffectation "
                + "JOIN FETCH n.evaluationCours.idAffectation.idEc "
                + "WHERE n.inscriptionEtudiant.idGroupe.idGroupe = :groupId "
                + "AND n.inscriptionEtudiant.idAnnee.idAnnee = :yearId "
                + "AND UPPER(n.inscriptionEtudiant.statut) = 'VALIDE' "
                + "AND n.evaluationCours.idAffectation.idGroupe.idGroupe = :groupId "
                + "AND n.evaluationCours.idAffectation.idAnnee.idAnnee = :yearId "
                + "AND n.evaluationCours.idAffectation.idSemestre.idSemestre = :semesterId "
                + "AND UPPER(n.evaluationCours.statutPublication) = 'PUBLIEE'",
                NoteEvaluation.class)
                .setParameter("groupId", groupId)
                .setParameter("yearId", yearId)
                .setParameter("semesterId", semesterId)
                .getResultList();

        List<SemesterGradeCalculator.Enrollment> calculatorEnrollments = enrollments.stream()
                .map(enrollment -> new SemesterGradeCalculator.Enrollment(
                        enrollment.getIdInscription(),
                        enrollment.getIdEtudiant().getMatricule(),
                        enrollment.getIdEtudiant().getNom(),
                        enrollment.getIdEtudiant().getPrenom()))
                .toList();
        List<SemesterGradeCalculator.Composition> calculatorCompositions = compositions.stream()
                .map(composition -> new SemesterGradeCalculator.Composition(
                        composition.getUniteEnseignement().getIdUe(),
                        composition.getUniteEnseignement().getCode(),
                        composition.getUniteEnseignement().getLibelle(),
                        composition.getElementConstitutif().getIdEc(),
                        composition.getElementConstitutif().getCode(),
                        composition.getElementConstitutif().getLibelle(),
                        composition.getCoefficient(),
                        composition.getOrdre()))
                .toList();
        List<SemesterGradeCalculator.Grade> calculatorGrades = notes.stream()
                .map(note -> new SemesterGradeCalculator.Grade(
                        note.getNoteEvaluationPK().getIdInscription(),
                        note.getEvaluationCours().getIdAffectation().getIdEc().getIdEc(),
                        note.getEvaluationCours().getCategorie(),
                        note.getValeur(),
                        true))
                .toList();
        return SemesterGradeCalculator.calculate(
                calculatorEnrollments, calculatorCompositions, calculatorGrades);
    }
}
