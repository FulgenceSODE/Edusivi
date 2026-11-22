package com.mycompany.web;

import com.mycompany.AnneeAcademique;
import com.mycompany.DossierEtudiant;
import com.mycompany.Groupe;
import com.mycompany.InscriptionEtudiant;
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
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "StudentEnrollmentsServlet", urlPatterns = "/de/inscriptions")
public class StudentEnrollmentsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/inscriptions.jsp";
    private static final String FLASH_MESSAGE = "studentEnrollmentsMessage";
    private static final String VALIDATED = "VALIDE";

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
            request.setAttribute("students", em.createQuery(
                    "SELECT d FROM DossierEtudiant d ORDER BY d.nom, d.prenom, d.matricule",
                    DossierEtudiant.class).getResultList());
            request.setAttribute("groups", em.createQuery(
                    "SELECT g FROM Groupe g JOIN FETCH g.idAnnee JOIN FETCH g.idFiliere "
                    + "JOIN FETCH g.idNiveau WHERE UPPER(g.idAnnee.statut) <> 'CLOTUREE' "
                    + "ORDER BY g.idAnnee.dateDebut DESC, g.idFiliere.code, g.idNiveau.code, g.code",
                    Groupe.class).getResultList());
            request.setAttribute("enrollments", em.createQuery(
                    "SELECT i FROM InscriptionEtudiant i JOIN FETCH i.idEtudiant "
                    + "JOIN FETCH i.idAnnee JOIN FETCH i.idGroupe "
                    + "ORDER BY i.dateInscription DESC, i.idInscription DESC",
                    InscriptionEtudiant.class).getResultList());
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load student enrollments.", ex);
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

        try {
            transaction.begin();
            Utilisateur actor = currentUser(request);
            if (actor == null) {
                throw new IllegalStateException("Authenticated DE account is missing from the user table.");
            }
            String action = request.getParameter("action");
            if ("create".equals(action)) {
                createEnrollment(request, actor);
            } else if ("toggleStatus".equals(action)) {
                toggleStatus(request, actor);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/de/inscriptions?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/de/inscriptions");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save a student enrollment.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification de l’inscription a échoué.");
        }
    }

    private void createEnrollment(HttpServletRequest request, Utilisateur actor) {
        Long studentId = requiredId(request.getParameter("studentId"), "L’étudiant");
        Long groupId = requiredId(request.getParameter("groupId"), "Le groupe");
        DossierEtudiant student = em.find(DossierEtudiant.class, studentId);
        Groupe group = em.find(Groupe.class, groupId);
        if (student == null || group == null) {
            throw new IllegalArgumentException("L’étudiant ou le groupe sélectionné n’existe plus.");
        }
        AnneeAcademique year = group.getIdAnnee();
        if ("CLOTUREE".equalsIgnoreCase(year.getStatut())) {
            throw new IllegalArgumentException("Une inscription ne peut pas être créée dans une année clôturée.");
        }
        Long duplicates = em.createQuery(
                "SELECT COUNT(i) FROM InscriptionEtudiant i "
                + "WHERE i.idEtudiant.idEtudiant = :studentId "
                + "AND i.idAnnee.idAnnee = :yearId AND i.idGroupe.idGroupe = :groupId",
                Long.class)
                .setParameter("studentId", studentId)
                .setParameter("yearId", year.getIdAnnee())
                .setParameter("groupId", groupId)
                .getSingleResult();
        if (duplicates > 0) {
            throw new IllegalArgumentException("Cet étudiant possède déjà une inscription à ce groupe pour cette année.");
        }

        InscriptionEtudiant enrollment = new InscriptionEtudiant();
        enrollment.setIdEtudiant(student);
        enrollment.setIdGroupe(group);
        enrollment.setIdAnnee(year);
        enrollment.setDateInscription(Date.valueOf(LocalDate.now()));
        enrollment.setStatut(VALIDATED);
        em.persist(enrollment);
        em.flush();
        audit(actor, "INSCRIPTION_ETUDIANT_CREEE", enrollment.getIdInscription().toString(),
                "Inscription de " + student.getMatricule() + " au groupe " + group.getCode()
                + " pour l’année " + year.getLibelle());
    }

    private void toggleStatus(HttpServletRequest request, Utilisateur actor) {
        Long enrollmentId = requiredId(request.getParameter("enrollmentId"), "L’inscription");
        InscriptionEtudiant enrollment = em.find(InscriptionEtudiant.class, enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("L’inscription sélectionnée n’existe plus.");
        }
        if ("CLOTUREE".equalsIgnoreCase(enrollment.getIdAnnee().getStatut())) {
            throw new IllegalArgumentException("Les inscriptions d’une année clôturée sont conservées sans modification.");
        }
        String currentStatus = enrollment.getStatut();
        String nextStatus = StudentEnrollmentRules.nextStatus(currentStatus);
        enrollment.setStatut(nextStatus);
        String action = "EN_ATTENTE".equalsIgnoreCase(currentStatus)
                ? "INSCRIPTION_ETUDIANT_VALIDEE"
                : "SUSPENDUE".equals(nextStatus)
                        ? "INSCRIPTION_ETUDIANT_SUSPENDUE" : "INSCRIPTION_ETUDIANT_REACTIVEE";
        audit(actor, action,
                enrollmentId.toString(), "Statut de l’inscription " + enrollmentId + " : " + nextStatus);
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

    private void audit(Utilisateur actor, String action, String enrollmentId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("InscriptionEtudiant");
        entry.setIdEntite(enrollmentId);
        entry.setDetails(details);
        entry.setDateAction(new java.util.Date());
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
            throw new IllegalArgumentException("L’identifiant sélectionné pour " + entityName + " est invalide.");
        }
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back a student enrollment change.", rollbackError);
        }
    }
}
