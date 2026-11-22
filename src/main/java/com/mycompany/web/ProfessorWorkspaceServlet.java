package com.mycompany.web;

import com.mycompany.AffectationEnseignement;
import com.mycompany.EvaluationCours;
import com.mycompany.SuiviPedagogiqueSeance;
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

@WebServlet(name = "ProfessorWorkspaceServlet", urlPatterns = {
    "/professeur/dashboard.jsp", "/professeur/enseignements.jsp"
})
public class ProfessorWorkspaceServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String DASHBOARD_VIEW = "/WEB-INF/professeur/dashboard-view.jsp";
    private static final String TEACHINGS_VIEW = "/WEB-INF/professeur/enseignements-view.jsp";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

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
            List<AffectationEnseignement> assignments = em.createQuery(
                    "SELECT a FROM AffectationEnseignement a "
                    + "JOIN FETCH a.idAnnee JOIN FETCH a.idEc "
                    + "JOIN FETCH a.idGroupe JOIN FETCH a.idSemestre "
                    + "WHERE a.idUtilisateur.idUtilisateur = :teacherId "
                    + "ORDER BY a.idAnnee.dateDebut DESC, a.idEc.code, a.idGroupe.code",
                    AffectationEnseignement.class)
                    .setParameter("teacherId", teacherId)
                    .getResultList();
            request.setAttribute("assignments", assignments);

            if ("/professeur/dashboard.jsp".equals(request.getServletPath())) {
                request.setAttribute("sessionCount", count(
                        "SELECT COUNT(s) FROM SuiviPedagogiqueSeance s "
                        + "WHERE s.idAffectation.idUtilisateur.idUtilisateur = :teacherId", teacherId));
                request.setAttribute("evaluationCount", count(
                        "SELECT COUNT(e) FROM EvaluationCours e "
                        + "WHERE e.idAffectation.idUtilisateur.idUtilisateur = :teacherId", teacherId));
                request.setAttribute("publishedEvaluationCount", count(
                        "SELECT COUNT(e) FROM EvaluationCours e "
                        + "WHERE e.idAffectation.idUtilisateur.idUtilisateur = :teacherId "
                        + "AND UPPER(e.statutPublication) = 'PUBLIEE'", teacherId));
                request.setAttribute("recentSessions", em.createQuery(
                        "SELECT s FROM SuiviPedagogiqueSeance s "
                        + "JOIN FETCH s.idAffectation "
                        + "JOIN FETCH s.idAffectation.idEc "
                        + "JOIN FETCH s.idAffectation.idGroupe "
                        + "WHERE s.idAffectation.idUtilisateur.idUtilisateur = :teacherId "
                        + "ORDER BY s.dateSeance DESC, s.idSeance DESC",
                        SuiviPedagogiqueSeance.class)
                        .setParameter("teacherId", teacherId)
                        .setMaxResults(5)
                        .getResultList());
                request.setAttribute("recentEvaluations", em.createQuery(
                        "SELECT e FROM EvaluationCours e "
                        + "JOIN FETCH e.idAffectation "
                        + "JOIN FETCH e.idAffectation.idEc "
                        + "JOIN FETCH e.idAffectation.idGroupe "
                        + "WHERE e.idAffectation.idUtilisateur.idUtilisateur = :teacherId "
                        + "ORDER BY e.dateEvaluation DESC, e.idEvaluation DESC",
                        EvaluationCours.class)
                        .setParameter("teacherId", teacherId)
                        .setMaxResults(5)
                        .getResultList());
                request.getRequestDispatcher(DASHBOARD_VIEW).forward(request, response);
            } else {
                request.getRequestDispatcher(TEACHINGS_VIEW).forward(request, response);
            }
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load the professor workspace.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private Long count(String query, Long teacherId) {
        return em.createQuery(query, Long.class)
                .setParameter("teacherId", teacherId)
                .getSingleResult();
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
}
