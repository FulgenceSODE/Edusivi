package com.mycompany.web;

import com.mycompany.AffectationEnseignement;
import com.mycompany.ChapitreCours;
import com.mycompany.JournalAudit;
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

@WebServlet(name = "ProfessorChaptersServlet", urlPatterns = "/professeur/seances-chapitres.jsp")
public class ProfessorChaptersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/professeur/chapitres-view.jsp";
    private static final String FLASH_MESSAGE = "professorChaptersMessage";

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

            List<AffectationEnseignement> assignments = assignments(teacher);
            request.setAttribute("assignments", assignments);
            Long assignmentId = optionalId(request.getParameter("assignmentId"));
            AffectationEnseignement selectedAssignment = null;
            if (assignmentId != null) {
                selectedAssignment = findOwnedAssignment(assignmentId, teacher);
            } else if (!assignments.isEmpty()) {
                selectedAssignment = assignments.get(0);
            }
            request.setAttribute("selectedAssignment", selectedAssignment);
            if (selectedAssignment != null) {
                request.setAttribute("chapters", em.createQuery(
                        "SELECT c FROM ChapitreCours c "
                        + "WHERE c.idAffectation.idAffectation = :assignmentId "
                        + "ORDER BY c.ordre, c.idChapitre",
                        ChapitreCours.class)
                        .setParameter("assignmentId", selectedAssignment.getIdAffectation())
                        .getResultList());
            }
            request.setAttribute("progressStates", ProfessorChapterRules.PROGRESS_STATES);
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load professor chapters.", ex);
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

        Long assignmentId;
        try {
            transaction.begin();
            String action = request.getParameter("action");
            if ("createChapter".equals(action)) {
                assignmentId = createChapter(request, teacher);
            } else if ("updateProgress".equals(action)) {
                assignmentId = updateProgress(request, teacher);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath()
                    + "/professeur/seances-chapitres.jsp?assignmentId=" + assignmentId + "&saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            redirectToSelectedAssignment(request, response);
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save professor chapter data.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "L’enregistrement du chapitre ou de sa progression a échoué.");
        }
    }

    private Long createChapter(HttpServletRequest request, Utilisateur teacher) {
        Long assignmentId = requiredId(request.getParameter("assignmentId"), "affectation");
        AffectationEnseignement assignment = findOwnedAssignment(assignmentId, teacher);
        ensureYearOpen(assignment);
        String title = requiredText(request.getParameter("title"),
                "Le titre du chapitre est obligatoire.", 150);
        Integer lastOrder = em.createQuery(
                "SELECT MAX(c.ordre) FROM ChapitreCours c "
                + "WHERE c.idAffectation.idAffectation = :assignmentId",
                Integer.class)
                .setParameter("assignmentId", assignmentId)
                .getSingleResult();

        ChapitreCours chapter = new ChapitreCours();
        chapter.setTitre(title);
        chapter.setOrdre(lastOrder == null ? 1 : lastOrder + 1);
        chapter.setProgression(ProfessorChapterRules.TO_COME);
        chapter.setDateMiseAJour(new Date());
        chapter.setIdAffectation(assignment);
        em.persist(chapter);
        em.flush();
        audit(teacher, "CHAPITRE_CREE", chapter.getIdChapitre().toString(),
                title + " / affectation " + assignmentId);
        return assignmentId;
    }

    private Long updateProgress(HttpServletRequest request, Utilisateur teacher) {
        Long chapterId = requiredId(request.getParameter("chapterId"), "chapitre");
        ChapitreCours chapter = findOwnedChapter(chapterId, teacher);
        ensureYearOpen(chapter.getIdAffectation());
        String progression = ProfessorChapterRules.validateProgression(request.getParameter("progression"));
        String previousProgression = chapter.getProgression();
        if (!progression.equalsIgnoreCase(previousProgression)) {
            chapter.setProgression(progression);
            chapter.setDateMiseAJour(new Date());
            audit(teacher, "PROGRESSION_CHAPITRE_MODIFIEE", chapterId.toString(),
                    previousProgression + " -> " + progression + " / " + chapter.getTitre());
        }
        return chapter.getIdAffectation().getIdAffectation();
    }

    private List<AffectationEnseignement> assignments(Utilisateur teacher) {
        return em.createQuery(
                "SELECT a FROM AffectationEnseignement a "
                + "JOIN FETCH a.idAnnee JOIN FETCH a.idEc "
                + "JOIN FETCH a.idGroupe JOIN FETCH a.idSemestre "
                + "WHERE a.idUtilisateur.idUtilisateur = :teacherId "
                + "ORDER BY a.idAnnee.dateDebut DESC, a.idEc.code, a.idGroupe.code",
                AffectationEnseignement.class)
                .setParameter("teacherId", teacher.getIdUtilisateur())
                .getResultList();
    }

    private AffectationEnseignement findOwnedAssignment(Long assignmentId, Utilisateur teacher) {
        List<AffectationEnseignement> results = em.createQuery(
                "SELECT a FROM AffectationEnseignement a "
                + "JOIN FETCH a.idAnnee JOIN FETCH a.idEc "
                + "WHERE a.idAffectation = :assignmentId "
                + "AND a.idUtilisateur.idUtilisateur = :teacherId",
                AffectationEnseignement.class)
                .setParameter("assignmentId", assignmentId)
                .setParameter("teacherId", teacher.getIdUtilisateur())
                .setMaxResults(1)
                .getResultList();
        if (results.isEmpty()) {
            throw new IllegalArgumentException("L’affectation demandée n’existe pas dans vos enseignements.");
        }
        return results.get(0);
    }

    private ChapitreCours findOwnedChapter(Long chapterId, Utilisateur teacher) {
        List<ChapitreCours> results = em.createQuery(
                "SELECT c FROM ChapitreCours c "
                + "JOIN FETCH c.idAffectation "
                + "JOIN FETCH c.idAffectation.idAnnee "
                + "WHERE c.idChapitre = :chapterId "
                + "AND c.idAffectation.idUtilisateur.idUtilisateur = :teacherId",
                ChapitreCours.class)
                .setParameter("chapterId", chapterId)
                .setParameter("teacherId", teacher.getIdUtilisateur())
                .setMaxResults(1)
                .getResultList();
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Le chapitre demandé n’existe pas dans vos enseignements.");
        }
        return results.get(0);
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
        entry.setTypeEntite("ChapitreCours");
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
        entry.setIdUtilisateur(teacher);
        em.persist(entry);
    }

    private static void ensureYearOpen(AffectationEnseignement assignment) {
        if ("CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Les données d’une année clôturée ne peuvent pas être modifiées.");
        }
    }

    private void redirectToSelectedAssignment(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String url = request.getContextPath() + "/professeur/seances-chapitres.jsp";
        try {
            Long assignmentId = optionalId(request.getParameter("assignmentId"));
            if (assignmentId != null) {
                url += "?assignmentId=" + assignmentId;
            }
        } catch (IllegalArgumentException ex) {
            // Return to the default teaching if the submitted identifier is invalid.
        }
        response.sendRedirect(url);
    }

    private void rollbackIfActive() {
        try {
            if (transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception ex) {
            getServletContext().log("Unable to roll back professor chapter changes.", ex);
        }
    }

    private static Long optionalId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requiredId(value, "affectation");
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

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }
}
