package com.mycompany.web;

import com.mycompany.DemandeInscriptionEnseignant;
import com.mycompany.Utilisateur;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet(name = "RegistrationServlet", urlPatterns = "/register-process")
public class RegistrationServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String nom = trim(request.getParameter("nom"));
        String prenom = trim(request.getParameter("prenom"));
        String email = trim(request.getParameter("email"));
        String specialite = trim(request.getParameter("specialite"));
        String password = request.getParameter("password");
        String confirmation = request.getParameter("confirm_password");

        if (nom == null || nom.isEmpty() || nom.length() > 100
                || prenom == null || prenom.isEmpty() || prenom.length() > 100
                || email == null || email.length() > 254 || !EMAIL_PATTERN.matcher(email).matches()
                || specialite == null || specialite.isEmpty() || specialite.length() > 150
                || password == null || password.length() < 12 || password.length() > 128
                || !password.equals(confirmation)) {
            response.sendRedirect(request.getContextPath() + "/register.jsp?error=invalid");
            return;
        }
        if (em == null || transaction == null) {
            getServletContext().log("Registration services are not available.");
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            transaction.begin();
            String normalizedEmail = email.toLowerCase(Locale.ROOT);
            Long existingUsers = em.createQuery(
                    "SELECT COUNT(u) FROM Utilisateur u WHERE LOWER(u.email) = :email", Long.class)
                    .setParameter("email", normalizedEmail)
                    .getSingleResult();
            Long existingRequests = em.createQuery(
                    "SELECT COUNT(d) FROM DemandeInscriptionEnseignant d WHERE LOWER(d.email) = :email", Long.class)
                    .setParameter("email", normalizedEmail)
                    .getSingleResult();
            if (existingUsers > 0 || existingRequests > 0) {
                transaction.rollback();
                response.sendRedirect(request.getContextPath() + "/register.jsp?error=email");
                return;
            }

            String passwordHash = (String) em.createNativeQuery(
                    "SELECT crypt(?1, gen_salt('bf', 12))")
                    .setParameter(1, password)
                    .getSingleResult();

            DemandeInscriptionEnseignant demande = new DemandeInscriptionEnseignant();
            demande.setNom(nom);
            demande.setPrenom(prenom);
            demande.setEmail(email);
            demande.setMotDePasseHash(passwordHash);
            demande.setSpecialite(specialite);
            demande.setDateDemande(new Date());
            em.persist(demande);
            em.flush();
            transaction.commit();

            response.sendRedirect(request.getContextPath() + "/register.jsp?submitted=true");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to submit teacher registration.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La demande n’a pas pu être enregistrée.");
        }
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back teacher registration.", rollbackError);
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
