package com.mycompany.web;

import com.mycompany.AffectationEnseignement;
import com.mycompany.ElementConstitutif;
import com.mycompany.Groupe;
import com.mycompany.JournalAudit;
import com.mycompany.Semestre;
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
import java.util.Date;
import java.util.List;

@WebServlet(name = "TeachingAssignmentsServlet", urlPatterns = "/de/affectations")
public class TeachingAssignmentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/affectations.jsp";
    private static final String FLASH_MESSAGE = "teachingAssignmentsMessage";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.isUserInRole("DE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            request.setAttribute("teachers", activeTeachers());
            request.setAttribute("courseElements", em.createQuery(
                    "SELECT e FROM ElementConstitutif e ORDER BY e.code", ElementConstitutif.class)
                    .getResultList());
            request.setAttribute("groups", em.createQuery(
                    "SELECT g FROM Groupe g JOIN FETCH g.idAnnee JOIN FETCH g.idFiliere "
                    + "JOIN FETCH g.idNiveau WHERE UPPER(g.idAnnee.statut) <> 'CLOTUREE' "
                    + "ORDER BY g.idAnnee.dateDebut DESC, g.idFiliere.code, g.idNiveau.code, g.code",
                    Groupe.class).getResultList());
            request.setAttribute("semesters", em.createQuery(
                    "SELECT s FROM Semestre s JOIN FETCH s.idAnnee "
                    + "WHERE UPPER(s.idAnnee.statut) <> 'CLOTUREE' "
                    + "ORDER BY s.idAnnee.dateDebut DESC, s.dateDebut",
                    Semestre.class).getResultList());
            request.setAttribute("assignments", em.createQuery(
                    "SELECT a FROM AffectationEnseignement a JOIN FETCH a.idAnnee "
                    + "JOIN FETCH a.idEc JOIN FETCH a.idGroupe "
                    + "JOIN FETCH a.idSemestre JOIN FETCH a.idUtilisateur "
                    + "ORDER BY a.idAnnee.dateDebut DESC, a.idSemestre.dateDebut, "
                    + "a.idGroupe.idFiliere.code, a.idGroupe.idNiveau.code, a.idEc.code",
                    AffectationEnseignement.class).getResultList());
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load teaching assignments.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!request.isUserInRole("DE")) {
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

        Utilisateur actor;
        try {
            actor = currentUser(request);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to identify the DE account for a teaching assignment change.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (actor == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            transaction.begin();
            String action = request.getParameter("action");
            if ("create".equals(action)) {
                createAssignment(request, actor);
            } else if ("reassign".equals(action)) {
                reassignTeacher(request, actor);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/de/affectations?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/de/affectations");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save a teaching assignment.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification de l’affectation a échoué.");
        }
    }

    private void createAssignment(HttpServletRequest request, Utilisateur actor) {
        Long groupId = requiredId(request.getParameter("groupId"), "groupe");
        Long semesterId = requiredId(request.getParameter("semesterId"), "semestre");
        Long elementId = requiredId(request.getParameter("elementId"), "EC");
        Long teacherId = requiredId(request.getParameter("teacherId"), "professeur");
        Groupe group = findEntity(Groupe.class, groupId, "Le groupe sélectionné n’existe plus.");
        Semestre semester = findEntity(Semestre.class, semesterId, "Le semestre sélectionné n’existe plus.");
        ElementConstitutif element = findEntity(
                ElementConstitutif.class, elementId, "L’EC sélectionné n’existe plus.");
        Utilisateur teacher = findActiveTeacher(teacherId);
        Long yearId = group.getIdAnnee().getIdAnnee();

        if ("CLOTUREE".equalsIgnoreCase(group.getIdAnnee().getStatut())
                || "CLOTUREE".equalsIgnoreCase(semester.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Une affectation ne peut pas être créée dans une année clôturée.");
        }
        TeachingAssignmentRules.validateYearPair(yearId, semester.getIdAnnee().getIdAnnee());
        Long elementOfferedInSemester = em.createQuery(
                "SELECT COUNT(c) FROM CompositionUe c JOIN c.uniteEnseignement u "
                + "WHERE c.elementConstitutif.idEc = :elementId "
                + "AND u.idSemestre.idSemestre = :semesterId "
                + "AND u.idSemestre.idAnnee.idAnnee = :yearId",
                Long.class)
                .setParameter("elementId", elementId)
                .setParameter("semesterId", semesterId)
                .setParameter("yearId", yearId)
                .getSingleResult();
        if (elementOfferedInSemester == 0) {
            throw new IllegalArgumentException(
                    "L’EC sélectionné n’est pas rattaché à une UE de ce semestre.");
        }
        Long duplicate = em.createQuery(
                "SELECT COUNT(a) FROM AffectationEnseignement a "
                + "WHERE a.idAnnee.idAnnee = :yearId AND a.idEc.idEc = :elementId "
                + "AND a.idGroupe.idGroupe = :groupId",
                Long.class)
                .setParameter("yearId", yearId)
                .setParameter("elementId", elementId)
                .setParameter("groupId", groupId)
                .getSingleResult();
        if (duplicate > 0) {
            throw new IllegalArgumentException(
                    "Cet EC est déjà affecté à ce groupe pour cette année. Modifiez le professeur de l’affectation existante.");
        }

        AffectationEnseignement assignment = new AffectationEnseignement();
        assignment.setDateAffectation(new Date());
        assignment.setIdAnnee(group.getIdAnnee());
        assignment.setIdEc(element);
        assignment.setIdGroupe(group);
        assignment.setIdSemestre(semester);
        assignment.setIdUtilisateur(teacher);
        em.persist(assignment);
        em.flush();
        audit(actor, "AFFECTATION_ENSEIGNEMENT_CREEE", assignment.getIdAffectation().toString(),
                element.getCode() + " affecté à " + teacher.getEmail() + " pour "
                + group.getCode() + " / " + semester.getCode() + " / "
                + group.getIdAnnee().getLibelle());
    }

    private void reassignTeacher(HttpServletRequest request, Utilisateur actor) {
        Long assignmentId = requiredId(request.getParameter("assignmentId"), "affectation");
        Long teacherId = requiredId(request.getParameter("teacherId"), "professeur");
        AffectationEnseignement assignment = findEntity(
                AffectationEnseignement.class, assignmentId, "L’affectation sélectionnée n’existe plus.");
        if ("CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Une affectation d’année clôturée ne peut pas être modifiée.");
        }
        Utilisateur teacher = findActiveTeacher(teacherId);
        if (teacher.getIdUtilisateur().equals(assignment.getIdUtilisateur().getIdUtilisateur())) {
            return;
        }

        Long evaluations = em.createQuery(
                "SELECT COUNT(e) FROM EvaluationCours e WHERE e.idAffectation.idAffectation = :assignmentId",
                Long.class).setParameter("assignmentId", assignmentId).getSingleResult();
        Long sessions = em.createQuery(
                "SELECT COUNT(s) FROM SuiviPedagogiqueSeance s "
                + "WHERE s.idAffectation.idAffectation = :assignmentId",
                Long.class).setParameter("assignmentId", assignmentId).getSingleResult();
        Long chapters = em.createQuery(
                "SELECT COUNT(c) FROM ChapitreCours c "
                + "WHERE c.idAffectation.idAffectation = :assignmentId",
                Long.class).setParameter("assignmentId", assignmentId).getSingleResult();
        if (evaluations > 0 || sessions > 0 || chapters > 0) {
            throw new IllegalArgumentException(
                    "Un professeur ne peut pas être remplacé après l’enregistrement d’une évaluation, séance ou chapitre.");
        }
        String oldEmail = assignment.getIdUtilisateur().getEmail();
        assignment.setIdUtilisateur(teacher);
        audit(actor, "AFFECTATION_ENSEIGNEMENT_REAFFECTEE", assignmentId.toString(),
                "Professeur remplacé : " + oldEmail + " -> " + teacher.getEmail());
    }

    private List<Utilisateur> activeTeachers() {
        return em.createQuery(
                "SELECT DISTINCT u FROM Utilisateur u JOIN u.utilisateurRoleList ur JOIN ur.role r "
                + "WHERE r.codeRole = 'PROFESSEUR' AND UPPER(u.statut) = 'ACTIF' "
                + "ORDER BY u.nom, u.prenom",
                Utilisateur.class).getResultList();
    }

    private Utilisateur findActiveTeacher(Long teacherId) {
        List<Utilisateur> teachers = em.createQuery(
                "SELECT DISTINCT u FROM Utilisateur u JOIN u.utilisateurRoleList ur JOIN ur.role r "
                + "WHERE u.idUtilisateur = :teacherId AND r.codeRole = 'PROFESSEUR' "
                + "AND UPPER(u.statut) = 'ACTIF'",
                Utilisateur.class).setParameter("teacherId", teacherId).setMaxResults(1).getResultList();
        if (teachers.isEmpty()) {
            throw new IllegalArgumentException("Le professeur sélectionné est introuvable ou désactivé.");
        }
        return teachers.get(0);
    }

    private <T> T findEntity(Class<T> type, Long id, String errorMessage) {
        T entity = em.find(type, id);
        if (entity == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return entity;
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

    private void audit(Utilisateur actor, String action, String assignmentId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("AffectationEnseignement");
        entry.setIdEntite(assignmentId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
        entry.setIdUtilisateur(actor);
        em.persist(entry);
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

    private void rollbackIfActive() {
        try {
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back a teaching assignment change.", rollbackError);
        }
    }
}
