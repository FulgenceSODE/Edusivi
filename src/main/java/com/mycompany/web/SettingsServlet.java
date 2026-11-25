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
    private static final int MAX_PROFILE_NAME_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_PASSWORD_LENGTH = 128;

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
        if (transaction == null || em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        String action = request.getParameter("action");
        if ("updateProfile".equals(action)) {
            updateProfile(request, response);
            return;
        }
        if ("changePassword".equals(action)) {
            changePassword(request, response);
            return;
        }
        if (action != null && !"savePreferences".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String theme = request.getParameter("theme");
        String language = request.getParameter("language");
        if (!"light".equals(theme) && !"dark".equals(theme)
                || !"fr".equals(language) && !"en".equals(language)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
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

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String nom = normalizeProfileName(request.getParameter("nom"));
        String prenom = normalizeProfileName(request.getParameter("prenom"));
        if (nom == null || prenom == null) {
            response.sendRedirect(request.getContextPath() + "/settings?profileError=invalid");
            return;
        }

        try {
            transaction.begin();
            Utilisateur user = currentUser(request);
            if (user == null) {
                rollbackIfActive();
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            user.setNom(nom);
            user.setPrenom(prenom);
            em.flush();
            transaction.commit();

            request.getSession().setAttribute("userNom", user.getNom() + " " + user.getPrenom());
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to update the current user's profile.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Les informations personnelles n’ont pas pu être enregistrées.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/settings?profileSaved=true");
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmation = request.getParameter("confirmPassword");
        if (!isValidPasswordChange(currentPassword, newPassword, confirmation)) {
            response.sendRedirect(request.getContextPath() + "/settings?passwordError=invalid");
            return;
        }

        try {
            transaction.begin();
            Utilisateur user = currentUser(request);
            if (user == null) {
                rollbackIfActive();
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            Number matchingPassword = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM utilisateur WHERE id_utilisateur = ?1 "
                    + "AND mot_de_passe_hash = crypt(?2, mot_de_passe_hash)")
                    .setParameter(1, user.getIdUtilisateur())
                    .setParameter(2, currentPassword)
                    .getSingleResult();
            if (matchingPassword.longValue() != 1) {
                transaction.rollback();
                response.sendRedirect(request.getContextPath() + "/settings?passwordError=current");
                return;
            }

            String passwordHash = (String) em.createNativeQuery(
                    "SELECT crypt(?1, gen_salt('bf', 12))")
                    .setParameter(1, newPassword)
                    .getSingleResult();
            user.setMotDePasseHash(passwordHash);
            user.setPasswordResetRequired(false);
            em.flush();
            transaction.commit();
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to change the current user's password.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Le mot de passe n’a pas pu être modifié.");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/settings?passwordSaved=true");
    }

    private void loadSettings(HttpServletRequest request, Utilisateur user) {
        request.setAttribute("profileUser", user);
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

    static String normalizeProfileName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() || normalized.length() > MAX_PROFILE_NAME_LENGTH
                ? null : normalized;
    }

    static boolean isValidPasswordChange(String currentPassword, String newPassword, String confirmation) {
        return currentPassword != null && !currentPassword.isEmpty()
                && currentPassword.length() <= MAX_PASSWORD_LENGTH
                && newPassword != null
                && newPassword.length() >= MIN_PASSWORD_LENGTH
                && newPassword.length() <= MAX_PASSWORD_LENGTH
                && newPassword.equals(confirmation);
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
