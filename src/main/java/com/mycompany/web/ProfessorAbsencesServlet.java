package com.mycompany.web;

import com.mycompany.AffectationEnseignement;
import com.mycompany.InscriptionEtudiant;
import com.mycompany.JournalAudit;
import com.mycompany.SuiviPedagogiqueAbsence;
import com.mycompany.SuiviPedagogiqueSeance;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet(name = "ProfessorAbsencesServlet", urlPatterns = "/professeur/absences.jsp")
public class ProfessorAbsencesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/professeur/absences-view.jsp";
    private static final String FLASH_MESSAGE = "professorAbsencesMessage";

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
            Long teacherId = teacher.getIdUtilisateur();
            request.setAttribute("assignments", em.createQuery(
                    "SELECT a FROM AffectationEnseignement a "
                    + "JOIN FETCH a.idEc JOIN FETCH a.idGroupe JOIN FETCH a.idSemestre "
                    + "WHERE a.idUtilisateur.idUtilisateur = :teacherId "
                    + "ORDER BY a.idAnnee.dateDebut DESC, a.idEc.code",
                    AffectationEnseignement.class)
                    .setParameter("teacherId", teacherId)
                    .getResultList());
            request.setAttribute("sessions", em.createQuery(
                    "SELECT s FROM SuiviPedagogiqueSeance s JOIN FETCH s.idAffectation "
                    + "WHERE s.idAffectation.idUtilisateur.idUtilisateur = :teacherId "
                    + "ORDER BY s.dateSeance DESC, s.idSeance DESC",
                    SuiviPedagogiqueSeance.class)
                    .setParameter("teacherId", teacherId)
                    .getResultList());

            Long sessionId = optionalId(request.getParameter("sessionId"));
            if (sessionId != null) {
                SuiviPedagogiqueSeance selectedSession = findOwnedSession(sessionId, teacher);
                request.setAttribute("selectedSession", selectedSession);
                List<InscriptionEtudiant> students = activeEnrollments(selectedSession.getIdAffectation());
                request.setAttribute("students", students);
                Map<Long, SuiviPedagogiqueAbsence> absences = new HashMap<>();
                for (SuiviPedagogiqueAbsence absence : em.createQuery(
                        "SELECT a FROM SuiviPedagogiqueAbsence a "
                        + "WHERE a.idSeance.idSeance = :sessionId",
                        SuiviPedagogiqueAbsence.class)
                        .setParameter("sessionId", sessionId)
                        .getResultList()) {
                    absences.put(absence.getIdEtudiant().getIdEtudiant(), absence);
                }
                request.setAttribute("absences", absences);
            }

            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load professor sessions and absences.", ex);
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
            Long sessionId;
            if ("createSession".equals(action)) {
                sessionId = createSession(request, teacher);
            } else if ("saveAbsences".equals(action)) {
                sessionId = requiredId(request.getParameter("sessionId"), "séance");
                saveAbsences(request, teacher, sessionId);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath()
                    + "/professeur/absences.jsp?sessionId=" + sessionId + "&saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/professeur/absences.jsp");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save professor sessions or absences.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "L’enregistrement de la séance ou des absences a échoué.");
        }
    }

    private Long createSession(HttpServletRequest request, Utilisateur teacher) {
        Long assignmentId = requiredId(request.getParameter("assignmentId"), "affectation");
        AffectationEnseignement assignment = findOwnedAssignment(assignmentId, teacher);
        ensureYearOpen(assignment);
        LocalDate sessionDate = parseDate(request.getParameter("sessionDate"));
        ensureDateInSemester(sessionDate, assignment);
        String content = requiredText(request.getParameter("content"),
                "Le contenu de la séance est obligatoire.", 500);

        SuiviPedagogiqueSeance session = new SuiviPedagogiqueSeance();
        session.setDateSeance(Timestamp.valueOf(sessionDate.atStartOfDay()));
        session.setContenu(content);
        session.setDateCreation(new Date());
        session.setIdAffectation(assignment);
        em.persist(session);
        em.flush();
        audit(teacher, "SEANCE_CREEE", session.getIdSeance().toString(),
                assignment.getIdEc().getCode() + " : " + content);
        return session.getIdSeance();
    }

    private void saveAbsences(HttpServletRequest request, Utilisateur teacher, Long sessionId) {
        SuiviPedagogiqueSeance session = findOwnedSession(sessionId, teacher);
        ensureYearOpen(session.getIdAffectation());
        List<InscriptionEtudiant> enrollments = activeEnrollments(session.getIdAffectation());
        Map<Long, InscriptionEtudiant> studentsById = new HashMap<>();
        for (InscriptionEtudiant enrollment : enrollments) {
            studentsById.put(enrollment.getIdEtudiant().getIdEtudiant(), enrollment);
        }

        Set<Long> selectedStudentIds = new HashSet<>();
        String[] selectedIds = request.getParameterValues("absentStudentIds");
        if (selectedIds != null) {
            for (String value : selectedIds) {
                Long studentId = requiredId(value, "étudiant");
                if (!studentsById.containsKey(studentId)) {
                    throw new IllegalArgumentException(
                            "L’étudiant sélectionné n’est pas inscrit dans le groupe de cette séance.");
                }
                selectedStudentIds.add(studentId);
            }
        }

        for (Long studentId : selectedStudentIds) {
            boolean justified = "true".equals(request.getParameter("justified_" + studentId));
            String reason = optionalText(request.getParameter("reason_" + studentId), 500);
            if (justified && reason == null) {
                throw new IllegalArgumentException(
                        "Un motif est obligatoire pour enregistrer une absence comme justifiée.");
            }
            List<SuiviPedagogiqueAbsence> found = em.createQuery(
                    "SELECT a FROM SuiviPedagogiqueAbsence a "
                    + "WHERE a.idSeance.idSeance = :sessionId "
                    + "AND a.idEtudiant.idEtudiant = :studentId",
                    SuiviPedagogiqueAbsence.class)
                    .setParameter("sessionId", sessionId)
                    .setParameter("studentId", studentId)
                    .setMaxResults(1)
                    .getResultList();

            if (found.isEmpty()) {
                SuiviPedagogiqueAbsence absence = new SuiviPedagogiqueAbsence();
                absence.setIdSeance(session);
                absence.setIdEtudiant(studentsById.get(studentId).getIdEtudiant());
                absence.setDateSaisie(new Date());
                absence.setJustifiee(justified);
                absence.setMotif(reason);
                em.persist(absence);
                audit(teacher, "ABSENCE_SAISIEE", sessionId + ":" + studentId,
                        "Absence saisie; justifiée=" + justified + "; motif=" + (reason == null ? "" : reason));
            } else {
                SuiviPedagogiqueAbsence absence = found.get(0);
                if (absence.getJustifiee() != justified || !sameText(absence.getMotif(), reason)) {
                    absence.setJustifiee(justified);
                    absence.setMotif(reason);
                    absence.setDateSaisie(new Date());
                    audit(teacher, "ABSENCE_MODIFIEE", absence.getIdAbsence().toString(),
                            "Justifiée=" + justified + "; motif=" + (reason == null ? "" : reason));
                }
            }
        }
    }

    private List<InscriptionEtudiant> activeEnrollments(AffectationEnseignement assignment) {
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

    private SuiviPedagogiqueSeance findOwnedSession(Long sessionId, Utilisateur teacher) {
        List<SuiviPedagogiqueSeance> sessions = em.createQuery(
                "SELECT s FROM SuiviPedagogiqueSeance s JOIN FETCH s.idAffectation "
                + "WHERE s.idSeance = :sessionId "
                + "AND s.idAffectation.idUtilisateur.idUtilisateur = :teacherId",
                SuiviPedagogiqueSeance.class)
                .setParameter("sessionId", sessionId)
                .setParameter("teacherId", teacher.getIdUtilisateur())
                .setMaxResults(1)
                .getResultList();
        if (sessions.isEmpty()) {
            throw new IllegalArgumentException("La séance demandée n’existe pas dans vos enseignements.");
        }
        return sessions.get(0);
    }

    private static void ensureYearOpen(AffectationEnseignement assignment) {
        if ("CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Les données d’une année clôturée ne peuvent pas être modifiées.");
        }
    }

    private static void ensureDateInSemester(LocalDate date, AffectationEnseignement assignment) {
        LocalDate start = new java.sql.Date(assignment.getIdSemestre().getDateDebut().getTime()).toLocalDate();
        LocalDate end = new java.sql.Date(assignment.getIdSemestre().getDateFin().getTime()).toLocalDate();
        if (date.isBefore(start) || date.isAfter(end)) {
            throw new IllegalArgumentException("La date de la séance doit appartenir au semestre de l’affectation.");
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

    private void audit(Utilisateur teacher, String action, String entityId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("SuiviPedagogique");
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
        entry.setIdUtilisateur(teacher);
        em.persist(entry);
    }

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private static String optionalText(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("Le motif dépasse la longueur autorisée.");
        }
        return text.isEmpty() ? null : text;
    }

    private static boolean sameText(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("La date de séance est invalide.");
        }
    }

    private static Long requiredId(String value, String entityName) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant du " + entityName + " est invalide.");
        }
    }

    private static Long optionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requiredId(value, "séance");
    }

    private void rollbackIfActive() {
        try {
            if (transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back professor attendance changes.", rollbackError);
        }
    }
}
