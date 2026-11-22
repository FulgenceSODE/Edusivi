package com.mycompany.web;

import com.mycompany.DemandeInscriptionEnseignant;
import com.mycompany.Enseignant;
import com.mycompany.JournalAudit;
import com.mycompany.Role;
import com.mycompany.Utilisateur;
import com.mycompany.UtilisateurRole;
import com.mycompany.UtilisateurRolePK;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

@WebServlet(name = "AdminRequestsServlet", urlPatterns = "/admin/espace/demandes-inscription")
public class AdminRequestsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.isUserInRole("ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        try {
            List<DemandeInscriptionEnseignant> demandes = em.createQuery(
                    "SELECT d FROM DemandeInscriptionEnseignant d ORDER BY d.dateDemande ASC",
                    DemandeInscriptionEnseignant.class).getResultList();
            request.setAttribute("demandes", demandes);
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.getRequestDispatcher("/admin/espace/demandes-inscription.jsp").forward(request, response);
        } catch (RuntimeException ex) {
            getServletContext().log("Unable to load teacher registration requests.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!request.isUserInRole("ADMIN")) {
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

        Long requestId;
        try {
            requestId = Long.valueOf(request.getParameter("id"));
        } catch (RuntimeException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String action = request.getParameter("action");
        if (!"approve".equals(action) && !"reject".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String matricule = request.getParameter("matricule");
        if ("approve".equals(action)
                && (matricule == null || matricule.isBlank() || matricule.trim().length() > 50)) {
            response.sendRedirect(request.getContextPath()
                    + "/admin/espace/demandes-inscription?error=matricule");
            return;
        }

        try {
            transaction.begin();
            DemandeInscriptionEnseignant demande = em.find(DemandeInscriptionEnseignant.class, requestId);
            if (demande == null) {
                transaction.rollback();
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Utilisateur admin = findAuthenticatedAdmin(request);
            if (admin == null) {
                throw new IllegalStateException("Authenticated administrator is missing from the user table.");
            }

            if ("approve".equals(action)) {
                String normalizedEmail = demande.getEmail().toLowerCase(Locale.ROOT);
                Long duplicateUser = em.createQuery(
                        "SELECT COUNT(u) FROM Utilisateur u WHERE LOWER(u.email) = :email", Long.class)
                        .setParameter("email", normalizedEmail)
                        .getSingleResult();
                Long duplicateMatricule = em.createQuery(
                        "SELECT COUNT(e) FROM Enseignant e WHERE e.matricule = :matricule", Long.class)
                        .setParameter("matricule", matricule.trim())
                        .getSingleResult();
                if (duplicateUser > 0 || duplicateMatricule > 0) {
                    transaction.rollback();
                    response.sendRedirect(request.getContextPath()
                            + "/admin/espace/demandes-inscription?error=duplicate");
                    return;
                }

                Role professorRole = em.createQuery(
                        "SELECT r FROM Role r WHERE r.codeRole = :code", Role.class)
                        .setParameter("code", "PROFESSEUR")
                        .getResultStream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("PROFESSEUR role is not configured."));

                Utilisateur user = new Utilisateur();
                user.setNom(demande.getNom());
                user.setPrenom(demande.getPrenom());
                user.setEmail(demande.getEmail());
                user.setMotDePasseHash(demande.getMotDePasseHash());
                user.setStatut("ACTIF");
                user.setDateCreation(new Date());
                em.persist(user);
                em.flush();

                UtilisateurRole userRole = new UtilisateurRole(
                        new UtilisateurRolePK(user.getIdUtilisateur(), professorRole.getIdRole()), new Date());
                userRole.setUtilisateur(user);
                userRole.setRole(professorRole);
                em.persist(userRole);

                Enseignant professeur = new Enseignant();
                professeur.setMatricule(matricule.trim());
                professeur.setSpecialite(demande.getSpecialite());
                professeur.setIdUtilisateur(user);
                em.persist(professeur);

                saveAudit(admin, "INSCRIPTION_ENSEIGNANT_APPROUVEE", "Utilisateur",
                        user.getIdUtilisateur().toString(),
                        "Compte professeur créé après approbation de la demande " + requestId + ".");
            } else {
                saveAudit(admin, "INSCRIPTION_ENSEIGNANT_REJETEE", "DemandeInscriptionEnseignant",
                        requestId.toString(), "Demande rejetée pour " + demande.getEmail() + ".");
            }

            em.remove(demande);
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath()
                    + "/admin/espace/demandes-inscription?success=" + action);
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to process teacher registration decision.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La décision n’a pas pu être enregistrée.");
        }
    }

    private Utilisateur findAuthenticatedAdmin(HttpServletRequest request) {
        String principalName = request.getUserPrincipal().getName();
        List<Utilisateur> admins = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)", Utilisateur.class)
                .setParameter("email", principalName)
                .setMaxResults(1)
                .getResultList();
        return admins.isEmpty() ? null : admins.get(0);
    }

    private void saveAudit(Utilisateur admin, String action, String entityType,
            String entityId, String details) {
        JournalAudit audit = new JournalAudit();
        audit.setAction(action);
        audit.setTypeEntite(entityType);
        audit.setIdEntite(entityId);
        audit.setDetails(details);
        audit.setDateAction(new Date());
        audit.setIdUtilisateur(admin);
        em.persist(audit);
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back teacher registration decision.", rollbackError);
        }
    }
}
