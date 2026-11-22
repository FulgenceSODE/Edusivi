package com.mycompany.web;

import com.mycompany.Groupe;
import com.mycompany.Semestre;
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

@WebServlet(name = "GradeResultsServlet", urlPatterns = "/de/resultats.jsp")
public class GradeResultsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/WEB-INF/de/resultats-view.jsp";

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
            List<Groupe> groups = em.createQuery(
                    "SELECT g FROM Groupe g JOIN FETCH g.idAnnee "
                    + "JOIN FETCH g.idFiliere JOIN FETCH g.idNiveau "
                    + "ORDER BY g.idAnnee.dateDebut DESC, g.code",
                    Groupe.class).getResultList();
            request.setAttribute("groups", groups);
            if (groups.isEmpty()) {
                request.setAttribute("semesters", List.of());
                request.setAttribute("results", List.of());
                request.getRequestDispatcher(VIEW).forward(request, response);
                return;
            }

            Long requestedGroupId = optionalId(request.getParameter("groupId"), "groupe");
            Groupe group = requestedGroupId == null
                    ? groups.get(0)
                    : groups.stream()
                            .filter(candidate -> requestedGroupId.equals(candidate.getIdGroupe()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Le groupe demandé n’existe pas."));
            request.setAttribute("selectedGroup", group);

            List<Semestre> semesters = em.createQuery(
                    "SELECT s FROM Semestre s JOIN FETCH s.idAnnee "
                    + "WHERE s.idAnnee.idAnnee = :yearId ORDER BY s.dateDebut, s.code",
                    Semestre.class)
                    .setParameter("yearId", group.getIdAnnee().getIdAnnee())
                    .getResultList();
            request.setAttribute("semesters", semesters);
            if (semesters.isEmpty()) {
                request.setAttribute("results", List.of());
                request.getRequestDispatcher(VIEW).forward(request, response);
                return;
            }

            Long requestedSemesterId = optionalId(request.getParameter("semesterId"), "semestre");
            Semestre semester = requestedSemesterId == null ? null : semesters.stream()
                    .filter(candidate -> requestedSemesterId.equals(candidate.getIdSemestre()))
                    .findFirst()
                    .orElse(null);
            if (semester == null) {
                semester = semesters.get(0);
                if (requestedSemesterId != null) {
                    request.setAttribute("filterMessage",
                            "Le semestre choisi ne correspond pas à l’année du groupe; le premier semestre est affiché.");
                }
            }
            request.setAttribute("selectedSemester", semester);

            SemesterGradeService gradeService = new SemesterGradeService(em);
            request.setAttribute("results", gradeService.calculateGroup(
                    group.getIdGroupe(), semester.getIdSemestre(), group.getIdAnnee().getIdAnnee()));
            request.setAttribute("publishedGradePolicy",
                    "Seules les notes valides des évaluations publiées sont prises en compte.");
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to calculate academic results.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private static Long optionalId(String value, String entityName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("L’identifiant du " + entityName + " est invalide.");
        }
    }
}
