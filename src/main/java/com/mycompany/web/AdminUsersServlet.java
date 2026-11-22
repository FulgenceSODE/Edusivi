package com.mycompany.web;

import com.mycompany.DirecteurEtudes;
import com.mycompany.Enseignant;
import com.mycompany.JournalAudit;
import com.mycompany.Role;
import com.mycompany.Utilisateur;
import com.mycompany.UtilisateurRole;
import com.mycompany.UtilisateurRolePK;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@WebServlet(name = "AdminUsersServlet", urlPatterns = "/admin/espace/utilisateurs")
public class AdminUsersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/admin/espace/utilisateurs.jsp";
    private static final String FLASH_MESSAGE = "adminUsersMessage";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final List<String> ALLOWED_ROLES = List.of("ADMIN", "DE", "PROFESSEUR");

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    public static final class UserRow {
        private final Long id;
        private final String nom;
        private final String prenom;
        private final String email;
        private final String statut;
        private final String role;
        private final String matricule;
        private final boolean currentUser;

        private UserRow(Utilisateur user, String role, String matricule, boolean currentUser) {
            this.id = user.getIdUtilisateur();
            this.nom = user.getNom();
            this.prenom = user.getPrenom();
            this.email = user.getEmail();
            this.statut = user.getStatut();
            this.role = role;
            this.matricule = matricule;
            this.currentUser = currentUser;
        }

        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getStatut() { return statut; }
        public String getRole() { return role; }
        public String getMatricule() { return matricule; }
        public boolean isCurrentUser() { return currentUser; }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.isUserInRole("ADMIN")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            List<Utilisateur> users = em.createQuery(
                    "SELECT DISTINCT u FROM Utilisateur u "
                    + "LEFT JOIN FETCH u.utilisateurRoleList ORDER BY u.nom, u.prenom",
                    Utilisateur.class).getResultList();
            List<UserRow> rows = new ArrayList<>();
            String currentEmail = request.getUserPrincipal().getName();
            for (Utilisateur user : users) {
                String role = "";
                if (user.getUtilisateurRoleList() != null && !user.getUtilisateurRoleList().isEmpty()) {
                    role = user.getUtilisateurRoleList().get(0).getRole().getCodeRole();
                }
                String matricule = user.getEnseignant() == null ? "" : user.getEnseignant().getMatricule();
                rows.add(new UserRow(user, role, matricule,
                        user.getEmail().equalsIgnoreCase(currentEmail)));
            }
            request.setAttribute("users", rows);
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException | IllegalStateException ex) {
            getServletContext().log("Unable to load account administration.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!request.isUserInRole("ADMIN")) {
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

        try {
            transaction.begin();
            Utilisateur actor = authenticatedAdmin(request);
            if (actor == null) {
                throw new IllegalStateException("Authenticated administrator is missing from the user table.");
            }
            String action = request.getParameter("action");
            if ("create".equals(action)) {
                createUser(request, actor);
            } else if ("updateProfile".equals(action)) {
                updateProfile(request, actor);
            } else if ("assignRole".equals(action)) {
                assignRole(request, actor);
            } else if ("toggleStatus".equals(action)) {
                toggleStatus(request, actor);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/admin/espace/utilisateurs?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/espace/utilisateurs");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to update an account.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification du compte a échoué.");
        }
    }

    private void createUser(HttpServletRequest request, Utilisateur actor) {
        String nom = requiredText(request.getParameter("nom"), "Le nom est obligatoire.", 100);
        String prenom = requiredText(request.getParameter("prenom"), "Le prénom est obligatoire.", 100);
        String email = normalizedEmail(request.getParameter("email"));
        String roleCode = requiredText(request.getParameter("roleCode"), "Le rôle est obligatoire.", 30);
        if (!"DE".equals(roleCode) && !"PROFESSEUR".equals(roleCode)) {
            throw new IllegalArgumentException("L’Admin peut créer un compte DE ou Professeur.");
        }
        String password = request.getParameter("temporaryPassword");
        String confirmation = request.getParameter("confirmTemporaryPassword");
        if (password == null || password.length() < 12 || !password.equals(confirmation)) {
            throw new IllegalArgumentException(
                    "Le mot de passe temporaire doit contenir au moins 12 caractères et être confirmé.");
        }
        ensureEmailAvailable(email, null);

        Utilisateur user = new Utilisateur();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        user.setMotDePasseHash((String) em.createNativeQuery(
                "SELECT crypt(?1, gen_salt('bf', 12))")
                .setParameter(1, password)
                .getSingleResult());
        user.setPasswordResetRequired(true);
        user.setStatut("ACTIF");
        user.setDateCreation(new Date());
        em.persist(user);
        em.flush();

        Role role = findRole(roleCode);
        addRole(user, role);
        ensureRoleProfile(user, roleCode, request.getParameter("matricule"),
                request.getParameter("specialite"));
        audit(actor, "CREATION_UTILISATEUR", user.getIdUtilisateur().toString(),
                "Compte " + email + " créé avec le rôle " + roleCode + ".");
    }

    private void updateProfile(HttpServletRequest request, Utilisateur actor) {
        Utilisateur user = findUser(request);
        preventSelfChange(user, actor);
        String nom = requiredText(request.getParameter("nom"), "Le nom est obligatoire.", 100);
        String prenom = requiredText(request.getParameter("prenom"), "Le prénom est obligatoire.", 100);
        String email = normalizedEmail(request.getParameter("email"));
        ensureEmailAvailable(email, user.getIdUtilisateur());
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(email);
        audit(actor, "MODIFICATION_UTILISATEUR", user.getIdUtilisateur().toString(),
                "Profil du compte " + email + " modifié.");
    }

    private void assignRole(HttpServletRequest request, Utilisateur actor) {
        Utilisateur user = findUser(request);
        preventSelfChange(user, actor);
        String roleCode = requiredText(request.getParameter("roleCode"), "Le rôle est obligatoire.", 30);
        if (!ALLOWED_ROLES.contains(roleCode)) {
            throw new IllegalArgumentException("Le rôle sélectionné n’est pas autorisé.");
        }
        if ("PROFESSEUR".equals(roleCode)) {
            String matricule = requiredText(request.getParameter("matricule"),
                    "Le matricule du professeur est obligatoire.", 50);
            ensureTeacherMatriculeAvailable(matricule, user.getIdUtilisateur());
        }
        Role role = findRole(roleCode);
        List<UtilisateurRole> currentRoles = em.createQuery(
                "SELECT ur FROM UtilisateurRole ur WHERE ur.utilisateurRolePK.idUtilisateur = :userId",
                UtilisateurRole.class)
                .setParameter("userId", user.getIdUtilisateur())
                .getResultList();
        if (currentRoles.size() == 1
                && currentRoles.get(0).getRole().getCodeRole().equals(roleCode)) {
            ensureRoleProfile(user, roleCode, request.getParameter("matricule"),
                    request.getParameter("specialite"));
            audit(actor, "MODIFICATION_PROFIL_PEDAGOGIQUE", user.getIdUtilisateur().toString(),
                    "Profil pédagogique vérifié pour le rôle " + roleCode + " de " + user.getEmail() + ".");
            return;
        }
        ensureAdministratorRemains(currentRoles, user);
        for (UtilisateurRole currentRole : currentRoles) {
            em.remove(currentRole);
        }
        em.flush();
        addRole(user, role);
        ensureRoleProfile(user, roleCode, request.getParameter("matricule"),
                request.getParameter("specialite"));
        audit(actor, "ATTRIBUTION_ROLE", user.getIdUtilisateur().toString(),
                "Rôle remplacé par " + roleCode + " pour " + user.getEmail() + ".");
    }

    private void toggleStatus(HttpServletRequest request, Utilisateur actor) {
        Utilisateur user = findUser(request);
        preventSelfChange(user, actor);
        boolean active = "ACTIF".equalsIgnoreCase(user.getStatut());
        if (active && hasActiveAdminRole(user)) {
            Long activeAdmins = em.createQuery(
                    "SELECT COUNT(DISTINCT u) FROM Utilisateur u "
                    + "JOIN u.utilisateurRoleList ur JOIN ur.role r "
                    + "WHERE r.codeRole = 'ADMIN' AND u.statut = 'ACTIF'", Long.class)
                    .getSingleResult();
            if (activeAdmins <= 1) {
                throw new IllegalArgumentException("Le dernier compte administrateur actif ne peut pas être désactivé.");
            }
        }
        user.setStatut(active ? "SUSPENDU" : "ACTIF");
        audit(actor, active ? "DESACTIVATION_UTILISATEUR" : "REACTIVATION_UTILISATEUR",
                user.getIdUtilisateur().toString(), "Statut de " + user.getEmail() + " : " + user.getStatut() + ".");
    }

    private void ensureRoleProfile(Utilisateur user, String roleCode, String matricule, String specialite) {
        if ("DE".equals(roleCode)) {
            if (user.getDirecteurEtudes() == null) {
                DirecteurEtudes director = new DirecteurEtudes();
                director.setIdUtilisateur(user);
                em.persist(director);
            }
        } else if ("PROFESSEUR".equals(roleCode)) {
            String normalizedMatricule = requiredText(matricule,
                    "Le matricule du professeur est obligatoire.", 50);
            ensureTeacherMatriculeAvailable(normalizedMatricule, user.getIdUtilisateur());
            Enseignant teacher = user.getEnseignant();
            if (teacher == null) {
                teacher = new Enseignant();
                teacher.setIdUtilisateur(user);
                em.persist(teacher);
            }
            teacher.setMatricule(normalizedMatricule);
            teacher.setSpecialite(optionalText(specialite, 150));
        }
    }

    private void ensureAdministratorRemains(List<UtilisateurRole> currentRoles, Utilisateur user) {
        boolean hasAdminRole = currentRoles.stream()
                .anyMatch(userRole -> "ADMIN".equals(userRole.getRole().getCodeRole()));
        if (!hasAdminRole) {
            return;
        }
        Long activeAdmins = em.createQuery(
                "SELECT COUNT(DISTINCT u) FROM Utilisateur u "
                + "JOIN u.utilisateurRoleList ur JOIN ur.role r "
                + "WHERE r.codeRole = 'ADMIN' AND u.statut = 'ACTIF'", Long.class)
                .getSingleResult();
        if (activeAdmins <= 1) {
            throw new IllegalArgumentException("Le dernier compte administrateur actif doit être conservé.");
        }
    }

    private boolean hasActiveAdminRole(Utilisateur user) {
        return user.getUtilisateurRoleList() != null
                && user.getUtilisateurRoleList().stream()
                        .anyMatch(userRole -> "ADMIN".equals(userRole.getRole().getCodeRole()));
    }

    private void addRole(Utilisateur user, Role role) {
        UtilisateurRole userRole = new UtilisateurRole(
                new UtilisateurRolePK(user.getIdUtilisateur(), role.getIdRole()), new Date());
        userRole.setUtilisateur(user);
        userRole.setRole(role);
        em.persist(userRole);
    }

    private Role findRole(String roleCode) {
        return em.createQuery("SELECT r FROM Role r WHERE r.codeRole = :code", Role.class)
                .setParameter("code", roleCode)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Le rôle " + roleCode + " n’est pas configuré."));
    }

    private Utilisateur findUser(HttpServletRequest request) {
        Long userId;
        try {
            userId = Long.valueOf(request.getParameter("userId"));
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant du compte est invalide.");
        }
        Utilisateur user = em.find(Utilisateur.class, userId);
        if (user == null) {
            throw new IllegalArgumentException("Le compte sélectionné n’existe plus.");
        }
        return user;
    }

    private Utilisateur authenticatedAdmin(HttpServletRequest request) {
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

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        Long duplicates;
        if (excludedUserId == null) {
            duplicates = em.createQuery(
                    "SELECT COUNT(u) FROM Utilisateur u WHERE LOWER(u.email) = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } else {
            duplicates = em.createQuery(
                    "SELECT COUNT(u) FROM Utilisateur u WHERE LOWER(u.email) = :email "
                    + "AND u.idUtilisateur <> :excludedId", Long.class)
                    .setParameter("email", email)
                    .setParameter("excludedId", excludedUserId)
                    .getSingleResult();
        }
        if (duplicates > 0) {
            throw new IllegalArgumentException("Cette adresse électronique est déjà utilisée.");
        }
    }

    private void ensureTeacherMatriculeAvailable(String matricule, Long userId) {
        Long duplicates = em.createQuery(
                "SELECT COUNT(e) FROM Enseignant e WHERE e.matricule = :matricule "
                + "AND e.idUtilisateur.idUtilisateur <> :userId", Long.class)
                .setParameter("matricule", matricule)
                .setParameter("userId", userId)
                .getSingleResult();
        if (duplicates > 0) {
            throw new IllegalArgumentException("Ce matricule professeur est déjà attribué.");
        }
    }

    private static String normalizedEmail(String value) {
        String email = requiredText(value, "L’adresse électronique est obligatoire.", 254)
                .toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("L’adresse électronique n’est pas valide.");
        }
        return email;
    }

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private static String optionalText(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("La spécialité dépasse la longueur autorisée.");
        }
        return text.isEmpty() ? null : text;
    }

    private void preventSelfChange(Utilisateur user, Utilisateur actor) {
        if (user.getIdUtilisateur().equals(actor.getIdUtilisateur())) {
            throw new IllegalArgumentException("Pour éviter de perdre l’accès, gérez votre propre compte séparément.");
        }
    }

    private void audit(Utilisateur actor, String action, String entityId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite("Utilisateur");
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
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
            getServletContext().log("Unable to roll back account administration.", rollbackError);
        }
    }
}
