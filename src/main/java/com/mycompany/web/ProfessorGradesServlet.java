package com.mycompany.web;

import com.mycompany.AffectationEnseignement;
import com.mycompany.EvaluationCours;
import com.mycompany.InscriptionEtudiant;
import com.mycompany.JournalAudit;
import com.mycompany.NoteEvaluation;
import com.mycompany.NoteEvaluationPK;
import com.mycompany.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "ProfessorGradesServlet",
        urlPatterns = {"/professeur/saisie-notes.jsp", "/professeur/evaluations.jsp"})
public class ProfessorGradesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/professeur/saisie-notes-view.jsp";
    private static final String FLASH_MESSAGE = "professorGradesMessage";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.isUserInRole("PROFESSEUR")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            Utilisateur teacher = currentUser(request);
            if (teacher == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            request.setAttribute("assignments", em.createQuery(
                    "SELECT a FROM AffectationEnseignement a "
                    + "JOIN FETCH a.idEc JOIN FETCH a.idGroupe JOIN FETCH a.idSemestre "
                    + "WHERE a.idUtilisateur.idUtilisateur = :teacherId "
                    + "ORDER BY a.idAnnee.dateDebut DESC, a.idEc.code",
                    AffectationEnseignement.class)
                    .setParameter("teacherId", teacher.getIdUtilisateur())
                    .getResultList());
            request.setAttribute("evaluations", em.createQuery(
                    "SELECT e FROM EvaluationCours e "
                    + "WHERE e.idAffectation.idUtilisateur.idUtilisateur = :teacherId "
                    + "ORDER BY e.dateEvaluation DESC, e.idEvaluation DESC",
                    EvaluationCours.class)
                    .setParameter("teacherId", teacher.getIdUtilisateur())
                    .getResultList());

            Long selectedId = optionalId(request.getParameter("evaluationId"));
            if (selectedId == null) {
                selectedId = optionalId(request.getParameter("eval"));
            }
            if (selectedId != null) {
                EvaluationCours evaluation = findOwnedEvaluation(selectedId, teacher);
                request.setAttribute("selectedEvaluation", evaluation);
                List<InscriptionEtudiant> students = activeEnrollments(evaluation);
                request.setAttribute("students", students);
                Map<Long, NoteEvaluation> grades = new HashMap<>();
                for (NoteEvaluation grade : em.createQuery(
                        "SELECT n FROM NoteEvaluation n JOIN FETCH n.inscriptionEtudiant "
                        + "WHERE n.noteEvaluationPK.idEvaluation = :evaluationId",
                        NoteEvaluation.class)
                        .setParameter("evaluationId", evaluation.getIdEvaluation())
                        .getResultList()) {
                    grades.put(grade.getNoteEvaluationPK().getIdInscription(), grade);
                }
                request.setAttribute("grades", grades);
            }

            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load professor evaluations and grades.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!request.isUserInRole("PROFESSEUR")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null || transaction == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        Utilisateur teacher;
        try {
            teacher = currentUser(request);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to identify the professor account.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (teacher == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            transaction.begin();
            String action = request.getParameter("action");
            Long evaluationId;
            if ("createEvaluation".equals(action)) {
                evaluationId = createEvaluation(request, teacher);
            } else if ("saveGrades".equals(action)) {
                evaluationId = requiredId(request.getParameter("evaluationId"), "évaluation");
                saveGrades(request, teacher, evaluationId);
            } else if ("publish".equals(action)) {
                evaluationId = requiredId(request.getParameter("evaluationId"), "évaluation");
                publishEvaluation(evaluationId, teacher);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath()
                    + "/professeur/saisie-notes.jsp?evaluationId=" + evaluationId + "&saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/professeur/saisie-notes.jsp");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save professor evaluation data.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "L’enregistrement des évaluations ou des notes a échoué.");
        }
    }

    private Long createEvaluation(HttpServletRequest request, Utilisateur teacher) {
        Long assignmentId = requiredId(request.getParameter("assignmentId"), "affectation");
        AffectationEnseignement assignment = findOwnedAssignment(assignmentId, teacher);
        ensureYearOpen(assignment);
        String title = requiredText(request.getParameter("title"),
                "Le titre de l’évaluation est obligatoire.", 150);
        String category = requiredText(request.getParameter("category"),
                "La catégorie de l’évaluation est obligatoire.", 20).toUpperCase(Locale.ROOT);
        if (!List.of("INTERROGATION", "DEVOIR", "EXAMEN").contains(category)) {
            throw new IllegalArgumentException("La catégorie doit être INTERROGATION, DEVOIR ou EXAMEN.");
        }
        LocalDate evaluationDate = parseDate(request.getParameter("evaluationDate"));
        ensureDateInSemester(evaluationDate, assignment);

        EvaluationCours evaluation = new EvaluationCours();
        evaluation.setLibelle(title);
        evaluation.setDateEvaluation(Date.valueOf(evaluationDate));
        evaluation.setCategorie(category);
        evaluation.setBareme(BigDecimal.valueOf(20));
        evaluation.setCoefficient(BigDecimal.ONE);
        evaluation.setStatut("ACTIVE");
        evaluation.setStatutPublication("BROUILLON");
        evaluation.setIdAffectation(assignment);
        em.persist(evaluation);
        em.flush();
        audit(teacher, "EVALUATION_CREEE", evaluation.getIdEvaluation().toString(),
                title + " (" + category + "), affectation " + assignmentId);
        return evaluation.getIdEvaluation();
    }

    private void saveGrades(HttpServletRequest request, Utilisateur teacher, Long evaluationId) {
        EvaluationCours evaluation = findOwnedEvaluation(evaluationId, teacher);
        ensureYearOpen(evaluation.getIdAffectation());
        if ("PUBLIEE".equalsIgnoreCase(evaluation.getStatutPublication())) {
            throw new IllegalArgumentException(
                    "Cette évaluation est publiée; les notes publiées ne sont pas modifiables depuis cet écran.");
        }

        List<InscriptionEtudiant> students = activeEnrollments(evaluation);
        int changed = 0;
        for (InscriptionEtudiant enrollment : students) {
            String rawScore = request.getParameter("grade_" + enrollment.getIdInscription());
            if (rawScore == null || rawScore.isBlank()) {
                continue;
            }
            BigDecimal score = ProfessorGradeRules.parseScore(rawScore);
            NoteEvaluationPK key = new NoteEvaluationPK(
                    evaluation.getIdEvaluation(), enrollment.getIdInscription());
            NoteEvaluation grade = em.find(NoteEvaluation.class, key);
            String action;
            if (grade == null) {
                grade = new NoteEvaluation();
                grade.setNoteEvaluationPK(key);
                grade.setEvaluationCours(evaluation);
                grade.setInscriptionEtudiant(enrollment);
                action = "NOTE_SAISIE";
            } else {
                if (grade.getValeur().compareTo(score) == 0) {
                    continue;
                }
                action = "NOTE_MODIFIEE";
            }
            grade.setValeur(score);
            grade.setDateSaisie(new java.util.Date());
            if ("NOTE_SAISIE".equals(action)) {
                em.persist(grade);
            }
            audit(teacher, action,
                    evaluationId + ":" + enrollment.getIdInscription(),
                    "Note " + score.toPlainString() + " pour " + enrollment.getIdEtudiant().getMatricule());
            changed++;
        }
        if (changed == 0) {
            throw new IllegalArgumentException("Aucune nouvelle note ou modification valide n’a été saisie.");
        }
    }

    private void publishEvaluation(Long evaluationId, Utilisateur teacher) {
        EvaluationCours evaluation = findOwnedEvaluation(evaluationId, teacher);
        ensureYearOpen(evaluation.getIdAffectation());
        if ("PUBLIEE".equalsIgnoreCase(evaluation.getStatutPublication())) {
            throw new IllegalArgumentException("Cette évaluation est déjà publiée.");
        }
        Long gradeCount = em.createQuery(
                "SELECT COUNT(n) FROM NoteEvaluation n "
                + "WHERE n.noteEvaluationPK.idEvaluation = :evaluationId",
                Long.class).setParameter("evaluationId", evaluationId).getSingleResult();
        if (gradeCount == 0) {
            throw new IllegalArgumentException("Saisissez au moins une note avant de publier l’évaluation.");
        }
        evaluation.setStatutPublication("PUBLIEE");
        evaluation.setStatut("TERMINEE");
        audit(teacher, "EVALUATION_PUBLIEE", evaluationId.toString(),
                gradeCount + " note(s) publiée(s).");
    }

    private List<InscriptionEtudiant> activeEnrollments(EvaluationCours evaluation) {
        AffectationEnseignement assignment = evaluation.getIdAffectation();
        return em.createQuery(
                "SELECT i FROM InscriptionEtudiant i JOIN FETCH i.idEtudiant "
                + "WHERE i.idGroupe.idGroupe = :groupId "
                + "AND i.idAnnee.idAnnee = :yearId AND UPPER(i.statut) = 'VALIDE' "
                + "ORDER BY i.idEtudiant.nom, i.idEtudiant.prenom, i.idEtudiant.matricule",
                InscriptionEtudiant.class)
                .setParameter("groupId", assignment.getIdGroupe().getIdGroupe())
                .setParameter("yearId", assignment.getIdAnnee().getIdAnnee())
                .getResultList();
    }

    private AffectationEnseignement findOwnedAssignment(Long assignmentId, Utilisateur teacher) {
        AffectationEnseignement assignment = em.find(AffectationEnseignement.class, assignmentId);
        if (assignment == null
                || !teacher.getIdUtilisateur().equals(assignment.getIdUtilisateur().getIdUtilisateur())) {
            throw new IllegalArgumentException("L’affectation demandée n’existe pas dans vos enseignements.");
        }
        return assignment;
    }

    private EvaluationCours findOwnedEvaluation(Long evaluationId, Utilisateur teacher) {
        EvaluationCours evaluation = em.find(EvaluationCours.class, evaluationId);
        if (evaluation == null
                || !teacher.getIdUtilisateur().equals(
                        evaluation.getIdAffectation().getIdUtilisateur().getIdUtilisateur())) {
            throw new IllegalArgumentException("L’évaluation demandée n’existe pas dans vos enseignements.");
        }
        return evaluation;
    }

    private static void ensureYearOpen(AffectationEnseignement assignment) {
        if ("CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Les données d’une année clôturée ne peuvent pas être modifiées.");
        }
    }

    private static void ensureDateInSemester(LocalDate date, AffectationEnseignement assignment) {
        LocalDate start = asLocalDate(assignment.getIdSemestre().getDateDebut());
        LocalDate end = asLocalDate(assignment.getIdSemestre().getDateFin());
        if (date.isBefore(start) || date.isAfter(end)) {
            throw new IllegalArgumentException("La date doit appartenir au semestre de l’affectation.");
        }
    }

    private static LocalDate asLocalDate(java.util.Date date) {
        return new Date(date.getTime()).toLocalDate();
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("La date de l’évaluation est invalide.");
        }
    }

    private Utilisateur currentUser(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            return null;
        }
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)",
                Utilisateur.class)
                .setParameter("email", request.getUserPrincipal().getName())
                .setMaxResults(1)
                .getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private static Long requiredId(String value, String entityName) {
        Long id = optionalId(value);
        if (id == null) {
            throw new IllegalArgumentException("L’identifiant du " + entityName + " est invalide.");
        }
        return id;
    }

    private static Long optionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("L’identifiant demandé est invalide.");
        }
    }

    private void audit(Utilisateur teacher, String action, String entityId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("EvaluationCours");
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new java.util.Date());
        entry.setIdUtilisateur(teacher);
        em.persist(entry);
    }

    private void rollbackIfActive() {
        try {
            if (transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back professor grade changes.", rollbackError);
        }
    }
}
