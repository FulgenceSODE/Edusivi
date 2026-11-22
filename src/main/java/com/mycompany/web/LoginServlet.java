package com.mycompany.web;

import com.mycompany.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login-process", "/logout"})
@ServletSecurity
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String ADMIN_LOGIN_MODE = "ADMIN";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if ("/logout".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if ("/logout".equals(request.getServletPath())) {
            logout(request, response);
            return;
        }

        String loginMode = request.getParameter("loginMode");
        boolean adminLogin = ADMIN_LOGIN_MODE.equals(loginMode);
        String loginPage = adminLogin ? "/admin/login.jsp" : "/login.jsp";

        if (!CsrfTokens.isValid(request)) {
            response.sendRedirect(request.getContextPath() + loginPage + "?error=csrf");
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            response.sendRedirect(request.getContextPath() + loginPage + "?error=credentials");
            return;
        }

        try {
            if (request.getUserPrincipal() != null) {
                request.logout();
            }
        } catch (ServletException ex) {
            getServletContext().log("Unable to clear the previous login identity.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        HttpSession previousSession = request.getSession(false);
        if (previousSession != null) {
            previousSession.invalidate();
        }

        try {
            request.login(email.trim(), password);
        } catch (ServletException ex) {
            getServletContext().log(
                    "JAAS authentication failed; inspect the Elytron JDBC realm, account status, and password format.",
                    ex);
            response.sendRedirect(request.getContextPath() + loginPage + "?error=credentials");
            return;
        }

        String role = authenticatedRole(request);
        if (role == null || (adminLogin != "ADMIN".equals(role))) {
            getServletContext().log(
                    "JAAS authenticated a principal, but its application role is missing or not allowed for this login page.");
            logoutIdentity(request);
            response.sendRedirect(request.getContextPath() + loginPage + "?error=role");
            return;
        }

        Principal principal = request.getUserPrincipal();
        if (principal == null || em == null) {
            logoutIdentity(request);
            getServletContext().log("Authenticated identity could not be loaded from the application database.");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            List<Utilisateur> users = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)", Utilisateur.class)
                    .setParameter("email", principal.getName())
                    .setMaxResults(1)
                    .getResultList();
            if (users.isEmpty()) {
                getServletContext().log(
                        "JAAS authenticated a principal that has no matching account in the application database.");
                logoutIdentity(request);
                response.sendRedirect(request.getContextPath() + loginPage + "?error=credentials");
                return;
            }

            Utilisateur user = users.get(0);
            if (!"ACTIF".equalsIgnoreCase(user.getStatut())) {
                logoutIdentity(request);
                response.sendRedirect(request.getContextPath() + loginPage + "?error=disabled");
                return;
            }
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session = request.getSession(false);
            session.setAttribute("userId", user.getIdUtilisateur());
            session.setAttribute("userNom", user.getNom() + " " + user.getPrenom());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userRole", role);
            CsrfTokens.ensureToken(request);

            String destination = user.isPasswordResetRequired()
                    ? "/password-change" : dashboardFor(role);
            response.sendRedirect(request.getContextPath() + destination);
        } catch (RuntimeException ex) {
            logoutIdentity(request);
            getServletContext().log("Unable to load the authenticated account.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            logoutIdentity(request);
            response.sendRedirect(request.getContextPath() + "/login.jsp?logout=true");
        } catch (RuntimeException ex) {
            getServletContext().log("Unable to end the authenticated session.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void logoutIdentity(HttpServletRequest request) {
        try {
            request.logout();
        } catch (ServletException ex) {
            throw new IllegalStateException("Container logout failed.", ex);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private static String authenticatedRole(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "ADMIN";
        }
        if (request.isUserInRole("DE")) {
            return "DE";
        }
        if (request.isUserInRole("PROFESSEUR")) {
            return "PROFESSEUR";
        }
        return null;
    }

    private static String dashboardFor(String role) {
        if ("ADMIN".equals(role)) {
            return "/admin/espace/dashboard.jsp";
        }
        if ("DE".equals(role)) {
            return "/de/dashboard.jsp";
        }
        return "/professeur/dashboard.jsp";
    }
}
