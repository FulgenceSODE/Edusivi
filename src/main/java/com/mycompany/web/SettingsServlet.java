package com.mycompany.web;

import com.mycompany.AnneeAcademique;
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
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SettingsServlet", urlPatterns = "/settings")
public class SettingsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        try {
            Utilisateur user = currentUser(request);
            if (user == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            loadSettings(request, user);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load account settings.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        request.getRequestDispatcher("/WEB-INF/settings.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String theme = request.getParameter("theme");
        String language = request.getParameter("language");
        if (!"light".equals(theme) && !"dark".equals(theme)
                || !"fr".equals(language) && !"en".equals(language)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (transaction == null || em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        Utilisateur user;
        try {
            user = currentUser(request);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to verify the account for settings changes.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (user == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            transaction.begin();
            em.createNativeQuery(
                    "INSERT INTO utilisateur_preference (id_utilisateur, theme, langue) "
                    + "VALUES (?1, ?2, ?3) "
                    + "ON CONFLICT (id_utilisateur) DO UPDATE "
                    + "SET theme = EXCLUDED.theme, langue = EXCLUDED.langue")
                    .setParameter(1, user.getIdUtilisateur())
                    .setParameter(2, theme)
                    .setParameter(3, language)
                    .executeUpdate();

            em.flush();
            transaction.commit();

            HttpSession session = request.getSession();
            session.setAttribute("userTheme", theme);
            session.setAttribute("userLanguage", language);
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save account settings.", ex);
            int status = ex instanceof IllegalArgumentException
                    ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            response.sendError(status, "Les paramètres n’ont pas pu être enregistrés.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/settings?saved=true");
    }

    private void loadSettings(HttpServletRequest request, Utilisateur user) {
        List<?> preferences = em.createNativeQuery(
                "SELECT theme, langue FROM utilisateur_preference WHERE id_utilisateur = ?1")
                .setParameter(1, user.getIdUtilisateur())
                .getResultList();
        Object[] preference = preferences.isEmpty() ? null : (Object[]) preferences.get(0);
        String theme = preference == null ? "light" : (String) preference[0];
        String language = preference == null ? "fr" : (String) preference[1];
        request.setAttribute("theme", theme);
        request.setAttribute("language", language);

        List<AnneeAcademique> activeYears = em.createQuery(
                "SELECT a FROM AnneeAcademique a WHERE UPPER(a.statut) = 'ACTIVE' ORDER BY a.dateDebut DESC",
                AnneeAcademique.class).getResultList();
        request.setAttribute("activeYear", activeYears.isEmpty() ? null : activeYears.get(0));
        request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
        HttpSession session = request.getSession();
        session.setAttribute("userTheme", theme);
        session.setAttribute("userLanguage", language);
        session.setAttribute("activeAcademicYear", activeYears.isEmpty()
                ? "Non définie" : activeYears.get(0).getLibelle());
    }

    private Utilisateur currentUser(HttpServletRequest request) {
        if (request.getUserPrincipal() == null || em == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute("userId");
        Utilisateur user = userId == null ? null : em.find(Utilisateur.class, userId);
        if (user == null || !user.getEmail().equalsIgnoreCase(request.getUserPrincipal().getName())) {
            return null;
        }
        return user;
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back account settings.", rollbackError);
        }
    }
}
