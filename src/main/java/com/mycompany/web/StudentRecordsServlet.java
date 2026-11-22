package com.mycompany.web;

import com.mycompany.DossierEtudiant;
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
import java.util.Locale;

@WebServlet(name = "StudentRecordsServlet", urlPatterns = "/de/etudiants")
public class StudentRecordsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/etudiants.jsp";
    private static final String FLASH_MESSAGE = "studentRecordsMessage";

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
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load student records.", ex);
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
                saveStudent(request, actor, null);
            } else if ("update".equals(action)) {
                saveStudent(request, actor, requiredId(request.getParameter("studentId")));
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/de/etudiants?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/de/etudiants");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save a student record.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification du dossier étudiant a échoué.");
        }
    }

    private void saveStudent(HttpServletRequest request, Utilisateur actor, Long studentId) {
        String matricule = requiredText(
                request.getParameter("matricule"), "Le matricule est obligatoire.", 40)
                .toUpperCase(Locale.ROOT);
        String nom = requiredText(request.getParameter("nom"), "Le nom est obligatoire.", 100);
        String prenom = requiredText(request.getParameter("prenom"), "Le prénom est obligatoire.", 100);
        String query = "SELECT COUNT(d) FROM DossierEtudiant d WHERE UPPER(d.matricule) = :matricule"
                + (studentId == null ? "" : " AND d.idEtudiant <> :studentId");
        var duplicateQuery = em.createQuery(query, Long.class).setParameter("matricule", matricule);
        if (studentId != null) {
            duplicateQuery.setParameter("studentId", studentId);
        }
        if (duplicateQuery.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce matricule est déjà attribué à un autre étudiant.");
        }

        DossierEtudiant student = studentId == null ? new DossierEtudiant()
                : findStudent(studentId);
        student.setMatricule(matricule);
        student.setNom(nom);
        student.setPrenom(prenom);
        if (studentId == null) {
            student.setDateCreation(new Date());
            em.persist(student);
            em.flush();
        }
        audit(actor, studentId == null ? "DOSSIER_ETUDIANT_CREE" : "DOSSIER_ETUDIANT_MODIFIE",
                student.getIdEtudiant().toString(),
                "Dossier étudiant " + matricule + " : " + nom + " " + prenom);
    }

    private DossierEtudiant findStudent(Long studentId) {
        DossierEtudiant student = em.find(DossierEtudiant.class, studentId);
        if (student == null) {
            throw new IllegalArgumentException("Le dossier étudiant sélectionné n’existe plus.");
        }
        return student;
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

    private void audit(Utilisateur actor, String action, String studentId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("DossierEtudiant");
        entry.setIdEntite(studentId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
        entry.setIdUtilisateur(actor);
        em.persist(entry);
    }

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private static Long requiredId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant du dossier est invalide.");
        }
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back a student record change.", rollbackError);
        }
    }
}
