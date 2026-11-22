<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.web.HtmlEscaper" %>
<%
    Long studentCount = (Long) request.getAttribute("studentCount");
    Long enrollmentCount = (Long) request.getAttribute("enrollmentCount");
    Long groupCount = (Long) request.getAttribute("groupCount");
    Long assignmentCount = (Long) request.getAttribute("assignmentCount");
    Long pendingEnrollmentCount = (Long) request.getAttribute("pendingEnrollmentCount");
    String activeYearLabel = (String) request.getAttribute("activeYearLabel");
    String directorName = (String) request.getAttribute("directorName");
    if (studentCount == null) {
        response.sendRedirect(request.getContextPath() + "/de/dashboard");
        return;
    }
    String activeYearValue = activeYearLabel == null ? "Aucune année active" : activeYearLabel;
    String enrollmentValue = enrollmentCount == null ? "—" : enrollmentCount.toString();
    String groupValue = groupCount == null ? "—" : groupCount.toString();
    String assignmentValue = assignmentCount == null ? "—" : assignmentCount.toString();
    String pendingValue = pendingEnrollmentCount == null ? "—" : pendingEnrollmentCount.toString();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tableau de bord DE - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="app-container">
    <jsp:include page="/includes/sidebar.jsp"/>
    <div class="main-wrapper">
        <jsp:include page="/includes/header.jsp"/>
        <main class="content-area dashboard-page">
            <section class="dashboard-hero" aria-labelledby="dashboard-title">
                <img class="dashboard-hero-image" src="${pageContext.request.contextPath}/images/edusuivi-campus.png" alt="">
                <div class="dashboard-hero-panel">
                    <p class="eyebrow">Direction des études</p>
                    <h1 id="dashboard-title">Bienvenue sur EduSuivi<%= directorName == null ? "" : ", " + HtmlEscaper.escape(directorName) %></h1>
                    <p>Vue de pilotage pédagogique, alimentée par les données enregistrées.</p>
                    <span class="dashboard-year">Année active : <strong><%= HtmlEscaper.escape(activeYearValue) %></strong></span>
                </div>
            </section>
            <% if (activeYearLabel == null) { %>
                <div class="alert alert-warning" role="status">Aucune année académique n’est active. Créez et activez une année pour afficher les indicateurs de scolarité.</div>
            <% } %>
            <section class="dashboard-stat-grid" aria-label="Indicateurs de l’année académique">
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Dossiers étudiants</span>
                    <strong class="stat-value"><%= studentCount %></strong>
                    <span class="form-help">Tous les dossiers conservés</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Inscriptions validées</span>
                    <strong class="stat-value"><%= enrollmentValue %></strong>
                    <span class="form-help">Année active</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Groupes</span>
                    <strong class="stat-value"><%= groupValue %></strong>
                    <span class="form-help">Année active</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Affectations</span>
                    <strong class="stat-value"><%= assignmentValue %></strong>
                    <span class="form-help">Année active</span>
                </article>
            </section>
            <div class="dashboard-lower-grid">
                <section class="card dashboard-card">
                    <div class="card-header">
                        <div><h2>Inscriptions à traiter</h2><p class="form-help">Demandes en attente pour l’année active.</p></div>
                    </div>
                    <div class="card-body dashboard-pending">
                        <% if (pendingEnrollmentCount == null) { %>
                            <p class="form-help">Activez une année académique pour consulter les inscriptions en attente.</p>
                            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/de/annees">Gérer les années</a>
                        <% } else { %>
                            <div class="dashboard-pending-count">
                                <strong><%= pendingValue %></strong>
                                <span>inscription(s) en attente de validation</span>
                            </div>
                            <% if (pendingEnrollmentCount > 0) { %>
                                <p class="alert alert-warning" role="status">Des demandes nécessitent votre examen.</p>
                            <% } else { %>
                                <p class="form-help">Aucune demande en attente pour le moment.</p>
                            <% } %>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/de/inscriptions">Consulter les inscriptions</a>
                        <% } %>
                    </div>
                </section>
                <section class="card dashboard-card">
                    <div class="card-header">
                        <div><h2>Accès rapides</h2><p class="form-help">Raccourcis vers les tâches courantes.</p></div>
                    </div>
                    <div class="card-body">
                        <div class="dashboard-action-grid">
                            <a class="dashboard-action" href="${pageContext.request.contextPath}/de/annees">
                                <strong>Calendrier académique</strong>
                                <span>Gérer les années et les semestres.</span>
                            </a>
                            <a class="dashboard-action" href="${pageContext.request.contextPath}/de/structure">
                                <strong>Structure académique</strong>
                                <span>Gérer les filières, groupes, UE et EC.</span>
                            </a>
                            <a class="dashboard-action" href="${pageContext.request.contextPath}/de/etudiants">
                                <strong>Dossiers étudiants</strong>
                                <span>Consulter et mettre à jour les dossiers.</span>
                            </a>
                            <a class="dashboard-action" href="${pageContext.request.contextPath}/de/affectations">
                                <strong>Affectations</strong>
                                <span>Suivre les enseignements attribués.</span>
                            </a>
                        </div>
                    </div>
                </section>
            </div>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
