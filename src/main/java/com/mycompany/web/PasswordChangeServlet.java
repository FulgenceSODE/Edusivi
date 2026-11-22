package com.mycompany.web;

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

@WebServlet(name = "PasswordChangeServlet", urlPatterns = "/password-change")
public class PasswordChangeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
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
            if (!user.isPasswordResetRequired()) {
                response.sendRedirect(request.getContextPath() + dashboardFor(request));
                return;
            }
            showForm(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load the required password-change page.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String password = request.getParameter("newPassword");
        String confirmation = request.getParameter("confirmPassword");
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
                || password.length() > MAX_PASSWORD_LENGTH || !password.equals(confirmation)) {
            request.setAttribute("error", "invalid");
            showForm(request, response);
            return;
        }
        if (em == null || transaction == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        boolean accountMissing = false;
        try {
            transaction.begin();
            Utilisateur user = currentUser(request);
            if (user == null) {
                rollbackIfActive();
                accountMissing = true;
            } else if (!user.isPasswordResetRequired()) {
                transaction.rollback();
            } else {
                String passwordHash = (String) em.createNativeQuery(
                        "SELECT crypt(?1, gen_salt('bf', 12))")
                        .setParameter(1, password)
                        .getSingleResult();
                user.setMotDePasseHash(passwordHash);
                user.setPasswordResetRequired(false);
                em.flush();
                transaction.commit();
            }
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to complete the required password change.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Le changement de mot de passe n’a pas pu être enregistré.");
            return;
        }
        if (accountMissing) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        response.sendRedirect(request.getContextPath() + dashboardFor(request));
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
        request.getRequestDispatcher("/WEB-INF/password-change.jsp").forward(request, response);
    }

    private Utilisateur currentUser(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
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
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back the required password change.", rollbackError);
        }
    }

    private static String dashboardFor(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "/admin/espace/dashboard.jsp";
        }
        if (request.isUserInRole("DE")) {
            return "/de/dashboard.jsp";
        }
        if (request.isUserInRole("PROFESSEUR")) {
            return "/professeur/dashboard.jsp";
        }
        return "/login.jsp";
    }
}
