<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String currentRole = request.isUserInRole("ADMIN") ? "ADMIN"
            : request.isUserInRole("DE") ? "DE"
            : request.isUserInRole("PROFESSEUR") ? "PROFESSEUR" : "";
    if (currentRole.isEmpty()) {
        response.sendError(403);
        return;
    }
    String currentPage = request.getServletPath();
    String userNom = (String) session.getAttribute("userNom");
    if (userNom == null || userNom.isBlank()) {
        userNom = request.getUserPrincipal() == null ? "" : request.getUserPrincipal().getName();
    }
    String avatar = userNom.isBlank() ? "?" : userNom.substring(0, 1).toUpperCase(java.util.Locale.FRENCH);
    String roleLabel = "ADMIN".equals(currentRole)
            ? com.mycompany.web.UiMessages.text(request, "nav.admin")
            : "DE".equals(currentRole)
                    ? com.mycompany.web.UiMessages.text(request, "nav.de")
                    : com.mycompany.web.UiMessages.text(request, "nav.professor");
%>
<aside class="sidebar">
    <div class="sidebar-brand">
        <img src="${pageContext.request.contextPath}/images/edusuivi-logo.png"
             alt="EduSuivi — Piloter. Suivre. Évaluer.">
    </div>
    <ul class="sidebar-menu">
        <% if ("DE".equals(currentRole)) { %>
            <li class="menu-category"><%= com.mycompany.web.UiMessages.text(request, "nav.de") %></li>
            <li><a href="${pageContext.request.contextPath}/de/dashboard" class="<%= currentPage.contains("dashboard") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.dashboard") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/annees" class="<%= currentPage.contains("annees") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.years") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/structure" class="<%= currentPage.contains("structure") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.structure") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/etudiants" class="<%= currentPage.contains("etudiants") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.students") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/inscriptions" class="<%= currentPage.contains("inscriptions") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.enrollments") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/affectations" class="<%= currentPage.contains("affectations") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.assignments") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/resultats.jsp" class="<%= currentPage.contains("resultats") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.results") %></a></li>
            <li><a href="${pageContext.request.contextPath}/de/rapports.jsp" class="<%= currentPage.contains("rapports") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.reports") %></a></li>
        <% } else if ("PROFESSEUR".equals(currentRole)) { %>
            <li class="menu-category"><%= com.mycompany.web.UiMessages.text(request, "nav.professor") %></li>
            <li><a href="${pageContext.request.contextPath}/professeur/dashboard.jsp" class="<%= currentPage.contains("dashboard") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.dashboard") %></a></li>
            <li><a href="${pageContext.request.contextPath}/professeur/enseignements.jsp" class="<%= currentPage.contains("enseignements") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.teachings") %></a></li>
            <li><a href="${pageContext.request.contextPath}/professeur/seances-chapitres.jsp" class="<%= currentPage.contains("seances-chapitres") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.coursebook") %></a></li>
            <li><a href="${pageContext.request.contextPath}/professeur/absences.jsp" class="<%= currentPage.contains("absences") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.sessions") %></a></li>
            <li><a href="${pageContext.request.contextPath}/professeur/evaluations.jsp" class="<%= currentPage.contains("evaluations") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.evaluations") %></a></li>
        <% } else { %>
            <li class="menu-category"><%= com.mycompany.web.UiMessages.text(request, "nav.admin") %></li>
            <li><a href="${pageContext.request.contextPath}/admin/espace/dashboard.jsp" class="<%= currentPage.contains("dashboard") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.dashboard") %></a></li>
            <li><a href="${pageContext.request.contextPath}/admin/espace/utilisateurs" class="<%= currentPage.contains("utilisateurs") ? "active" : "" %>">Comptes utilisateurs</a></li>
            <li><a href="${pageContext.request.contextPath}/admin/espace/demandes-inscription" class="<%= currentPage.contains("demandes-inscription") ? "active" : "" %>"><%= com.mycompany.web.UiMessages.text(request, "nav.requests") %></a></li>
        <% } %>
    </ul>
    <section class="sidebar-account" aria-label="Compte utilisateur">
        <div class="sidebar-account-identity">
            <div class="user-avatar"><%= com.mycompany.web.HtmlEscaper.escape(avatar) %></div>
            <div class="sidebar-account-details">
                <strong class="sidebar-account-name"><%= com.mycompany.web.HtmlEscaper.escape(userNom) %></strong>
                <span class="sidebar-account-role"><%= com.mycompany.web.HtmlEscaper.escape(roleLabel) %></span>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/settings" class="btn btn-outline sidebar-account-action">
            <%= com.mycompany.web.UiMessages.text(request, "header.settings") %>
        </a>
        <form action="${pageContext.request.contextPath}/logout" method="POST">
            <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.CsrfTokens.ensureToken(request) %>">
            <button type="submit" class="btn btn-outline sidebar-account-action">
                <%= com.mycompany.web.UiMessages.text(request, "header.logout") %>
            </button>
        </form>
    </section>
</aside>
