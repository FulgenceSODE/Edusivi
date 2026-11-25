<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat,java.util.List,com.mycompany.web.AdminUsersServlet.UserRow,com.mycompany.web.HtmlEscaper" %>
<%
    List<UserRow> users = (List<UserRow>) request.getAttribute("users");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String flashMessage = (String) request.getAttribute("flashMessage");
    if (users == null) {
        response.sendRedirect(request.getContextPath() + "/admin/espace/utilisateurs");
        return;
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestion des comptes - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-container">
    <jsp:include page="/includes/sidebar.jsp"/>
    <div class="main-wrapper">
        <jsp:include page="/includes/header.jsp"/>
        <main class="content-area">
            <div class="page-header">
                <div>
                    <p class="eyebrow">Administration</p>
                    <h1 class="page-title">Comptes utilisateurs</h1>
                    <p class="page-subtitle">Créez et gérez les comptes, rôles et accès de l’institut.</p>
                </div>
            </div>
            <% if (flashMessage != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(flashMessage) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">Validé</div>
            <% } %>

            <section class="card">
                <div class="card-header">
                    <div>
                        <h2>Créer un compte</h2>
                        <p class="form-help">Les comptes sont activés après création.</p>
                    </div>
                </div>
                <div class="card-body">
                    <form class="form-row" action="${pageContext.request.contextPath}/admin/espace/utilisateurs" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="create">
                        <div class="form-group">
                            <label class="form-label" for="new-nom">Nom</label>
                            <input class="form-control" id="new-nom" name="nom" maxlength="100" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-prenom">Prénom</label>
                            <input class="form-control" id="new-prenom" name="prenom" maxlength="100" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-email">Adresse </label>
                            <input class="form-control" id="new-email" type="email" name="email" maxlength="254" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-role">Rôle</label>
                            <select class="form-select" id="new-role" name="roleCode" required>
                                <option value="DE">Directeur des Études</option>
                                <option value="PROFESSEUR">Professeur</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-matricule">Matricule professeur</label>
                            <input class="form-control" id="new-matricule" name="matricule" maxlength="50">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-specialite">Spécialité </label>
                            <input class="form-control" id="new-specialite" name="specialite" maxlength="150">
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-password">Mot de passe</label>
                            <input class="form-control" id="new-password" type="password" name="temporaryPassword" minlength="12" autocomplete="new-password" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-password-confirm">Confirmer mot de passe</label>
                            <input class="form-control" id="new-password-confirm" type="password" name="confirmTemporaryPassword" minlength="12" autocomplete="new-password" required>
                        </div>
                        <div class="form-group form-action">
                            <button class="btn btn-primary" type="submit">Créer Compte</button>
                        </div>
                    </form>
                </div>
            </section>

            <section class="card">
                <div class="card-header">
                    <div>
                        <h2>Comptes existants</h2>
                        <p class="form-help">La politique actuelle attribue un rôle principal par compte; les profils historiques ne sont pas supprimés lors d’un changement de rôle.</p>
                    </div>
                    <span class="badge badge-primary"><%= users.size() %> compte(s)</span>
                </div>
                <div class="card-body admin-user-list">
                    <% for (UserRow user : users) { %>
                        <article class="admin-user-card">
                            <header class="admin-user-heading">
                                <div>
                                    <h3><%= HtmlEscaper.escape(user.getNom()) %> <%= HtmlEscaper.escape(user.getPrenom()) %></h3>
                                    <p><%= HtmlEscaper.escape(user.getEmail()) %></p>
                                </div>
                                <span class="badge <%= "ACTIF".equalsIgnoreCase(user.getStatut()) ? "badge-success" : "badge-warning" %>"><%= HtmlEscaper.escape(user.getStatut()) %></span>
                            </header>

                            <form class="admin-user-form" action="${pageContext.request.contextPath}/admin/espace/utilisateurs" method="POST">
                                <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                <input type="hidden" name="action" value="updateProfile">
                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                <div class="form-group">
                                    <label class="form-label" for="nom-<%= user.getId() %>">Nom</label>
                                    <input class="form-control" id="nom-<%= user.getId() %>" name="nom" maxlength="100" value="<%= HtmlEscaper.escape(user.getNom()) %>" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label" for="prenom-<%= user.getId() %>">Prénom</label>
                                    <input class="form-control" id="prenom-<%= user.getId() %>" name="prenom" maxlength="100" value="<%= HtmlEscaper.escape(user.getPrenom()) %>" required>
                                </div>
                                <div class="form-group">
                                    <label class="form-label" for="email-<%= user.getId() %>">Adresse </label>
                                    <input class="form-control" id="email-<%= user.getId() %>" type="email" name="email" maxlength="254" value="<%= HtmlEscaper.escape(user.getEmail()) %>" required>
                                </div>
                                <button class="btn btn-sm btn-outline-primary" type="submit" <%= user.isCurrentUser() ? "disabled" : "" %>>Enregistrer le profil</button>
                            </form>

                            <form class="admin-user-form" action="${pageContext.request.contextPath}/admin/espace/utilisateurs" method="POST">
                                <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                <input type="hidden" name="action" value="assignRole">
                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                <div class="form-group">
                                    <label class="form-label" for="role-<%= user.getId() %>">Rôle principal</label>
                                    <select class="form-select" id="role-<%= user.getId() %>" name="roleCode" required>
                                        <option value="ADMIN" <%= "ADMIN".equals(user.getRole()) ? "selected" : "" %>>ADMIN</option>
                                        <option value="DE" <%= "DE".equals(user.getRole()) ? "selected" : "" %>>DE</option>
                                        <option value="PROFESSEUR" <%= "PROFESSEUR".equals(user.getRole()) ? "selected" : "" %>>PROFESSEUR</option>
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label" for="matricule-<%= user.getId() %>">Matricule professeur</label>
                                    <input class="form-control" id="matricule-<%= user.getId() %>" name="matricule" maxlength="50" value="<%= HtmlEscaper.escape(user.getMatricule()) %>">
                                </div>
                                <div class="form-group">
                                    <label class="form-label" for="specialite-<%= user.getId() %>">Spécialité</label>
                                    <input class="form-control" id="specialite-<%= user.getId() %>" name="specialite" maxlength="150">
                                </div>
                                <button class="btn btn-sm btn-outline-primary" type="submit" <%= user.isCurrentUser() ? "disabled" : "" %>>Attribuer le rôle</button>
                            </form>

                            <form class="admin-user-status" action="${pageContext.request.contextPath}/admin/espace/utilisateurs" method="POST"
                                  data-confirm="<%= "ACTIF".equalsIgnoreCase(user.getStatut()) ? "Confirmer la désactivation de ce compte ?" : "Confirmer la réactivation de ce compte ?" %>">
                                <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                <input type="hidden" name="action" value="toggleStatus">
                                <input type="hidden" name="userId" value="<%= user.getId() %>">
                                <button class="btn btn-sm <%= "ACTIF".equalsIgnoreCase(user.getStatut()) ? "btn-outline" : "btn-primary" %>" type="submit" <%= user.isCurrentUser() ? "disabled" : "" %>>
                                    <%= "ACTIF".equalsIgnoreCase(user.getStatut()) ? "Désactiver" : "Réactiver" %>
                                </button>
                                <% if (user.isCurrentUser()) { %><span class="form-help">Votre compte ne peut pas être modifié ici.</span><% } %>
                            </form>
                        </article>
                    <% } %>
                    <% if (users.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun compte</strong><span>Créez compte DE ou professeur.</span></div>
                    <% } %>
                </div>
            </section>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
