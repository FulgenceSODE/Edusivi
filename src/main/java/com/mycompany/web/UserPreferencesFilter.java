package com.mycompany.web;

import com.mycompany.Utilisateur;
import com.mycompany.UtilisateurRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebFilter(filterName = "UserPreferencesFilter", urlPatterns = {
    "/admin/espace/*", "/de/*", "/professeur/*", "/settings"
})
public class UserPreferencesFilter implements Filter {

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest servletRequest,
            jakarta.servlet.ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getUserPrincipal() == null) {
            chain.doFilter(request, response);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            HttpSession session = request.getSession(true);
            Long userId = (Long) session.getAttribute("userId");
            Utilisateur user = userId == null ? null : em.find(Utilisateur.class, userId);
            if (user == null || !user.getEmail().equalsIgnoreCase(request.getUserPrincipal().getName())) {
                List<Utilisateur> users = em.createQuery(
                        "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)",
                        Utilisateur.class)
                        .setParameter("email", request.getUserPrincipal().getName())
                        .setMaxResults(1)
                        .getResultList();
                if (users.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                user = users.get(0);
                session.setAttribute("userId", user.getIdUtilisateur());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userNom", user.getNom() + " " + user.getPrenom());
            }
            if (!"ACTIF".equalsIgnoreCase(user.getStatut())) {
                try {
                    request.logout();
                } catch (ServletException ex) {
                    request.getServletContext().log("Unable to end a disabled account session.", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                HttpSession disabledSession = request.getSession(false);
                if (disabledSession != null) {
                    disabledSession.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=disabled");
                return;
            }
            List<String> assignedRoles = em.createQuery(
                    "SELECT r.codeRole FROM UtilisateurRole ur JOIN ur.role r "
                    + "WHERE ur.utilisateurRolePK.idUtilisateur = :userId", String.class)
                    .setParameter("userId", user.getIdUtilisateur())
                    .getResultList();
            if (hasRoleMismatch(request, assignedRoles)) {
                try {
                    request.logout();
                } catch (ServletException ex) {
                    request.getServletContext().log("Unable to end a session after a role change.", ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                HttpSession changedRoleSession = request.getSession(false);
                if (changedRoleSession != null) {
                    changedRoleSession.invalidate();
                }
                response.sendRedirect(request.getContextPath() + "/login.jsp?error=role");
                return;
            }
            if (user.isPasswordResetRequired()) {
                response.sendRedirect(request.getContextPath() + "/password-change");
                return;
            }

            List<?> preferences = em.createNativeQuery(
                    "SELECT theme, langue FROM utilisateur_preference WHERE id_utilisateur = ?1")
                    .setParameter(1, user.getIdUtilisateur())
                    .getResultList();
            String theme = "light";
            String language = "fr";
            if (!preferences.isEmpty()) {
                Object[] values = (Object[]) preferences.get(0);
                theme = (String) values[0];
                language = (String) values[1];
            }
            List<?> activeYears = em.createNativeQuery(
                    "SELECT libelle FROM annee_academique WHERE UPPER(statut) = 'ACTIVE' ORDER BY date_debut DESC")
                    .setMaxResults(1)
                    .getResultList();
            String activeYear = activeYears.isEmpty() ? "Non définie" : (String) activeYears.get(0);

            session.setAttribute("userTheme", theme);
            session.setAttribute("userLanguage", language);
            session.setAttribute("activeAcademicYear", activeYear);
            request.setAttribute("uiTheme", theme);
            request.setAttribute("uiLanguage", language);
        } catch (PersistenceException ex) {
            request.getServletContext().log("Unable to load account preferences and active academic year.", ex);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean hasRoleMismatch(HttpServletRequest request, List<String> assignedRoles) {
        return request.isUserInRole("ADMIN") != assignedRoles.contains("ADMIN")
                || request.isUserInRole("DE") != assignedRoles.contains("DE")
                || request.isUserInRole("PROFESSEUR") != assignedRoles.contains("PROFESSEUR");
    }

    @Override
    public void destroy() {
    }
}
