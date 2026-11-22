<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.AnneeAcademique,com.mycompany.web.HtmlEscaper,com.mycompany.web.UiMessages" %>
<%
    String theme = (String) request.getAttribute("theme");
    String language = (String) request.getAttribute("language");
    AnneeAcademique activeYear = (AnneeAcademique) request.getAttribute("activeYear");
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
            <section class="card settings-card">
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/settings" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
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
