package com.mycompany.web;

import com.mycompany.AnneeAcademique;
import com.mycompany.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DirectorDashboardServlet", urlPatterns = "/de/dashboard")
public class DirectorDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/dashboard.jsp";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

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
            List<AnneeAcademique> activeYears = em.createQuery(
                    "SELECT a FROM AnneeAcademique a WHERE UPPER(a.statut) = 'ACTIVE'",
                    AnneeAcademique.class).setMaxResults(1).getResultList();
            request.setAttribute("activeYearLabel",
                    activeYears.isEmpty() ? null : activeYears.get(0).getLibelle());
            request.setAttribute("studentCount", em.createQuery(
                    "SELECT COUNT(d) FROM DossierEtudiant d", Long.class).getSingleResult());

            if (activeYears.isEmpty()) {
                request.setAttribute("enrollmentCount", null);
                request.setAttribute("groupCount", null);
                request.setAttribute("assignmentCount", null);
                request.setAttribute("pendingEnrollmentCount", null);
            } else {
                Long yearId = activeYears.get(0).getIdAnnee();
                request.setAttribute("enrollmentCount", em.createQuery(
                        "SELECT COUNT(i) FROM InscriptionEtudiant i "
                        + "WHERE i.idAnnee.idAnnee = :yearId AND UPPER(i.statut) = 'VALIDE'",
                        Long.class).setParameter("yearId", yearId).getSingleResult());
                request.setAttribute("groupCount", em.createQuery(
                        "SELECT COUNT(g) FROM Groupe g WHERE g.idAnnee.idAnnee = :yearId",
                        Long.class).setParameter("yearId", yearId).getSingleResult());
                request.setAttribute("assignmentCount", em.createQuery(
                        "SELECT COUNT(a) FROM AffectationEnseignement a WHERE a.idAnnee.idAnnee = :yearId",
                        Long.class).setParameter("yearId", yearId).getSingleResult());
                request.setAttribute("pendingEnrollmentCount", em.createQuery(
                        "SELECT COUNT(i) FROM InscriptionEtudiant i "
                        + "WHERE i.idAnnee.idAnnee = :yearId AND UPPER(i.statut) = 'EN_ATTENTE'",
                        Long.class).setParameter("yearId", yearId).getSingleResult());
            }

            String email = request.getUserPrincipal() == null ? null : request.getUserPrincipal().getName();
            if (email != null) {
                List<Utilisateur> users = em.createQuery(
                        "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)",
                        Utilisateur.class).setParameter("email", email).setMaxResults(1).getResultList();
                if (!users.isEmpty()) {
                    Utilisateur user = users.get(0);
                    request.setAttribute("directorName", user.getPrenom() + " " + user.getNom());
                }
            }
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load the director dashboard.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
