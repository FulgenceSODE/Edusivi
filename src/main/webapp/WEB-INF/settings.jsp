<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.AnneeAcademique,com.mycompany.Utilisateur,com.mycompany.web.HtmlEscaper,com.mycompany.web.UiMessages" %>
<%
    String theme = (String) request.getAttribute("theme");
    String language = (String) request.getAttribute("language");
    AnneeAcademique activeYear = (AnneeAcademique) request.getAttribute("activeYear");
    Utilisateur profileUser = (Utilisateur) request.getAttribute("profileUser");
    String csrfToken = (String) request.getAttribute("csrfToken");
%>
<!DOCTYPE html>
<html lang="<%= "en".equals(language) ? "en" : "fr" %>" data-theme="<%= HtmlEscaper.escape(theme) %>">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= UiMessages.text(request, "settings.title") %> - EduSuivi</title>
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
                    <h1 class="page-title"><%= UiMessages.text(request, "settings.title") %></h1>
                    <p class="page-subtitle"><%= UiMessages.text(request, "settings.subtitle") %></p>
                </div>
            </div>
            <% if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success"><%= UiMessages.text(request, "settings.saved") %></div>
            <% } %>
            <% if ("true".equals(request.getParameter("profileSaved"))) { %>
                <div class="alert alert-success"><%= UiMessages.text(request, "settings.profileSaved") %></div>
            <% } else if (request.getParameter("profileError") != null) { %>
                <div class="alert alert-danger"><%= UiMessages.text(request, "settings.profileInvalid") %></div>
            <% } %>
            <% if ("true".equals(request.getParameter("passwordSaved"))) { %>
                <div class="alert alert-success"><%= UiMessages.text(request, "settings.passwordSaved") %></div>
            <% } else if ("current".equals(request.getParameter("passwordError"))) { %>
                <div class="alert alert-danger"><%= UiMessages.text(request, "settings.passwordCurrentInvalid") %></div>
            <% } else if ("invalid".equals(request.getParameter("passwordError"))) { %>
                <div class="alert alert-danger"><%= UiMessages.text(request, "settings.passwordInvalid") %></div>
            <% } %>
            <section class="card settings-card">
                <div class="card-header">
                    <h2><%= UiMessages.text(request, "settings.profile") %></h2>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/settings" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="updateProfile">
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label" for="prenom"><%= UiMessages.text(request, "settings.firstName") %></label>
                                <input class="form-control" id="prenom" name="prenom" type="text"
                                       value="<%= HtmlEscaper.escape(profileUser.getPrenom()) %>"
                                       maxlength="100" autocomplete="given-name" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="nom"><%= UiMessages.text(request, "settings.lastName") %></label>
                                <input class="form-control" id="nom" name="nom" type="text"
                                       value="<%= HtmlEscaper.escape(profileUser.getNom()) %>"
                                       maxlength="100" autocomplete="family-name" required>
                            </div>
                        </div>
                        <button class="btn btn-primary" type="submit"><%= UiMessages.text(request, "settings.save") %></button>
                    </form>
                </div>
            </section>
            <section class="card settings-card">
                <div class="card-header">
                    <h2><%= UiMessages.text(request, "settings.password") %></h2>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/settings" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="changePassword">
                        <div class="form-group">
                            <label class="form-label" for="currentPassword"><%= UiMessages.text(request, "settings.currentPassword") %></label>
                            <input class="form-control" id="currentPassword" name="currentPassword" type="password"
                                   maxlength="128" autocomplete="current-password" required>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label" for="newPassword"><%= UiMessages.text(request, "settings.newPassword") %></label>
                                <input class="form-control" id="newPassword" name="newPassword" type="password"
                                       minlength="12" maxlength="128" autocomplete="new-password" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="confirmPassword"><%= UiMessages.text(request, "settings.confirmPassword") %></label>
                                <input class="form-control" id="confirmPassword" name="confirmPassword" type="password"
                                       minlength="12" maxlength="128" autocomplete="new-password" required>
                            </div>
                        </div>
                        <p class="form-help"><%= UiMessages.text(request, "settings.passwordHint") %></p>
                        <button class="btn btn-primary form-spaced" type="submit"><%= UiMessages.text(request, "settings.changePassword") %></button>
                    </form>
                </div>
            </section>
            <section class="card settings-card">
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/settings" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="savePreferences">
                        <div class="form-group">
                            <label class="form-label" for="theme"><%= UiMessages.text(request, "settings.theme") %></label>
                            <select class="form-select" id="theme" name="theme" required>
                                <option value="light" <%= "light".equals(theme) ? "selected" : "" %>><%= UiMessages.text(request, "settings.light") %></option>
                                <option value="dark" <%= "dark".equals(theme) ? "selected" : "" %>><%= UiMessages.text(request, "settings.dark") %></option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="language"><%= UiMessages.text(request, "settings.language") %></label>
                            <select class="form-select" id="language" name="language" required>
                                <option value="fr" <%= "fr".equals(language) ? "selected" : "" %>><%= UiMessages.text(request, "settings.french") %></option>
                                <option value="en" <%= "en".equals(language) ? "selected" : "" %>><%= UiMessages.text(request, "settings.english") %></option>
                            </select>
                        </div>
                        <div class="form-group">
                            <span class="form-label"><%= UiMessages.text(request, "settings.activeYear") %></span>
                            <p class="settings-value"><%= activeYear == null ? UiMessages.text(request, "settings.notDefined") : HtmlEscaper.escape(activeYear.getLibelle()) %></p>
                            <p class="form-help">Le Directeur des Études gère les années académiques dans l’espace pédagogique.</p>
                        </div>
                        <button class="btn btn-primary" type="submit"><%= UiMessages.text(request, "settings.save") %></button>
                    </form>
                </div>
            </section>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
<script>
    document.documentElement.dataset.theme = "<%= HtmlEscaper.escape(theme) %>";
</script>
</body>
</html>
