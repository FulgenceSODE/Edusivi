package com.mycompany.web;

import com.mycompany.AnneeAcademique;
import com.mycompany.JournalAudit;
import com.mycompany.Semestre;
import com.mycompany.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "AcademicCalendarServlet", urlPatterns = "/de/annees")
public class AcademicCalendarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/annees.jsp";
    private static final String FLASH_MESSAGE = "academicCalendarMessage";

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
            List<AnneeAcademique> years = em.createQuery(
                    "SELECT a FROM AnneeAcademique a ORDER BY a.dateDebut DESC", AnneeAcademique.class)
                    .getResultList();
            List<Semestre> semesters = em.createQuery(
                    "SELECT s FROM Semestre s ORDER BY s.idAnnee.dateDebut DESC, s.dateDebut",
                    Semestre.class).getResultList();
            request.setAttribute("academicYears", years);
            request.setAttribute("semesters", semesters);
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load the academic calendar.", ex);
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
            getServletContext().log("Unable to identify the DE account for an academic calendar change.", ex);
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
            if ("saveYear".equals(action)) {
                saveYear(request, actor);
            } else if ("activateYear".equals(action)) {
                activateYear(request, actor);
            } else if ("closeYear".equals(action)) {
                closeYear(request, actor);
            } else if ("saveSemester".equals(action)) {
                saveSemester(request, actor);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/de/annees?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/de/annees");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save an academic calendar change.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification du calendrier académique a échoué.");
        }
    }

    private void saveYear(HttpServletRequest request, Utilisateur actor) {
        Long yearId = optionalId(request.getParameter("yearId"));
        String label = requiredText(request.getParameter("label"), "Le libellé de l’année est obligatoire.", 30);
        LocalDate start = AcademicCalendarRules.parseDate(request.getParameter("startDate"));
        LocalDate end = AcademicCalendarRules.parseDate(request.getParameter("endDate"));
        AcademicCalendarRules.validateRange(start, end);

        AnneeAcademique year;
        String action;
        if (yearId == null) {
            year = new AnneeAcademique();
            year.setStatut("INACTIVE");
            action = "ANNEE_ACADEMIQUE_CREEE";
        } else {
            year = em.find(AnneeAcademique.class, yearId);
            if (year == null) {
                throw new IllegalArgumentException("L’année académique sélectionnée n’existe plus.");
            }
            ensureSemestersWithinYear(year, start, end);
            action = "ANNEE_ACADEMIQUE_MODIFIEE";
        }

        year.setLibelle(label);
        year.setDateDebut(Date.valueOf(start));
        year.setDateFin(Date.valueOf(end));
        if (yearId == null) {
            em.persist(year);
            em.flush();
        }
        audit(actor, action, "AnneeAcademique",
                year.getIdAnnee().toString(), "Année académique : " + label);
    }

    private void activateYear(HttpServletRequest request, Utilisateur actor) {
        Long yearId = requiredId(request.getParameter("yearId"));
        AnneeAcademique selected = em.find(AnneeAcademique.class, yearId);
        if (selected == null) {
            throw new IllegalArgumentException("L’année académique sélectionnée n’existe plus.");
        }
        if ("CLOTUREE".equalsIgnoreCase(selected.getStatut())) {
            throw new IllegalArgumentException("Une année clôturée ne peut pas être réactivée.");
        }

        List<AnneeAcademique> activeYears = em.createQuery(
                "SELECT a FROM AnneeAcademique a WHERE UPPER(a.statut) = 'ACTIVE'",
                AnneeAcademique.class)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();
        boolean changed = activeYears.size() != 1
                || !"ACTIVE".equalsIgnoreCase(selected.getStatut());
        if (!changed) {
            return;
        }
        String previousLabel = "Non définie";
        for (AnneeAcademique activeYear : activeYears) {
            previousLabel = activeYear.getLibelle();
            if (!activeYear.getIdAnnee().equals(yearId)) {
                activeYear.setStatut("INACTIVE");
            }
        }
        em.flush();
        selected.setStatut("ACTIVE");
        audit(actor, "ANNEE_ACTIVE_MODIFIEE", "AnneeAcademique", yearId.toString(),
                "Année active remplacée : " + previousLabel + " -> " + selected.getLibelle());
    }

    private void closeYear(HttpServletRequest request, Utilisateur actor) {
        Long yearId = requiredId(request.getParameter("yearId"));
        AnneeAcademique year = em.find(AnneeAcademique.class, yearId);
        if (year == null) {
            throw new IllegalArgumentException("L’année académique sélectionnée n’existe plus.");
        }
        if ("ACTIVE".equalsIgnoreCase(year.getStatut())) {
            throw new IllegalArgumentException(
                    "Activez d’abord une autre année avant de clôturer l’année active.");
        }
        year.setStatut("CLOTUREE");
        audit(actor, "ANNEE_ACADEMIQUE_CLOTUREE", "AnneeAcademique", yearId.toString(),
                "Année académique clôturée : " + year.getLibelle());
    }

    private void saveSemester(HttpServletRequest request, Utilisateur actor) {
        Long semesterId = optionalId(request.getParameter("semesterId"));
        Long yearId = requiredId(request.getParameter("yearId"));
        String code = requiredText(request.getParameter("code"), "Le code du semestre est obligatoire.", 20);
        String label = requiredText(request.getParameter("semesterLabel"),
                "Le libellé du semestre est obligatoire.", 100);
        LocalDate start = AcademicCalendarRules.parseDate(request.getParameter("semesterStartDate"));
        LocalDate end = AcademicCalendarRules.parseDate(request.getParameter("semesterEndDate"));
        AnneeAcademique year = em.find(AnneeAcademique.class, yearId);
        if (year == null) {
            throw new IllegalArgumentException("L’année académique sélectionnée n’existe plus.");
        }
        LocalDate yearStart = toLocalDate(year.getDateDebut());
        LocalDate yearEnd = toLocalDate(year.getDateFin());
        AcademicCalendarRules.validateSemesterWithinYear(start, end, yearStart, yearEnd);

        Semestre semester;
        String action;
        if (semesterId == null) {
            semester = new Semestre();
            semester.setIdAnnee(year);
            action = "SEMESTRE_CREE";
        } else {
            semester = em.find(Semestre.class, semesterId);
            if (semester == null || !semester.getIdAnnee().getIdAnnee().equals(yearId)) {
                throw new IllegalArgumentException("Le semestre sélectionné n’appartient pas à cette année.");
            }
            action = "SEMESTRE_MODIFIE";
        }
        semester.setCode(code);
        semester.setLibelle(label);
        semester.setDateDebut(Date.valueOf(start));
        semester.setDateFin(Date.valueOf(end));
        if (semesterId == null) {
            em.persist(semester);
            em.flush();
        }
        audit(actor, action, "Semestre",
                semester.getIdSemestre().toString(),
                "Semestre " + code + " de l’année " + year.getLibelle());
    }

    private void ensureSemestersWithinYear(AnneeAcademique year, LocalDate newStart, LocalDate newEnd) {
        List<Semestre> semesters = em.createQuery(
                "SELECT s FROM Semestre s WHERE s.idAnnee.idAnnee = :yearId", Semestre.class)
                .setParameter("yearId", year.getIdAnnee())
                .getResultList();
        for (Semestre semester : semesters) {
            AcademicCalendarRules.validateSemesterWithinYear(
                    toLocalDate(semester.getDateDebut()), toLocalDate(semester.getDateFin()), newStart, newEnd);
        }
    }

    private static LocalDate toLocalDate(java.util.Date value) {
        return new Date(value.getTime()).toLocalDate();
    }

    private static String requiredText(String value, String message, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty() || trimmed.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return trimmed;
    }

    private static Long optionalId(String value) {
        return value == null || value.isBlank() ? null : requiredId(value);
    }

    private static Long requiredId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant fourni est invalide.");
        }
    }

    private Utilisateur currentUser(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            return null;
        }
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)", Utilisateur.class)
                .setParameter("email", request.getUserPrincipal().getName())
                .setMaxResults(1)
                .getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private void audit(Utilisateur actor, String action, String entityType, String entityId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite(entityType);
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new java.util.Date());
        entry.setIdUtilisateur(actor);
        em.persist(entry);
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back the academic calendar transaction.", rollbackError);
        }
    }
}
