<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,com.mycompany.AffectationEnseignement,com.mycompany.web.HtmlEscaper" %>
<%
    List<AffectationEnseignement> assignments =
            (List<AffectationEnseignement>) request.getAttribute("assignments");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mes enseignements - EduSuivi</title>
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
                    <p class="eyebrow">Espace professeur</p>
                    <h1 class="page-title">Mes enseignements</h1>
                    <p class="page-subtitle">Affectations communiquées par la direction des études.</p>
                </div>
            </div>
            <section class="card">
                <div class="card-header">
                    <div><h3>Affectations</h3><p class="form-help">Les données affichées proviennent de vos affectations enregistrées.</p></div>
                </div>
                <div class="card-body">
                    <% if (assignments == null || assignments.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun enseignement attribué</strong><span>Contactez la direction des études pour vérifier vos affectations.</span></div>
                    <% } else { %>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr><th>EC</th><th>Groupe</th><th>Semestre</th><th>Année académique</th><th>Statut</th><th>Actions</th></tr>
                                </thead>
                                <tbody>
                                    <% for (AffectationEnseignement assignment : assignments) { %>
                                        <tr>
                                            <td><strong><%= HtmlEscaper.escape(assignment.getIdEc().getCode()) %></strong><br>
                                                <span><%= HtmlEscaper.escape(assignment.getIdEc().getLibelle()) %></span></td>
                                            <td><%= HtmlEscaper.escape(assignment.getIdGroupe().getCode()) %></td>
                                            <td><%= HtmlEscaper.escape(assignment.getIdSemestre().getCode()) %></td>
                                            <td><%= HtmlEscaper.escape(assignment.getIdAnnee().getLibelle()) %></td>
                                            <td><span class="badge <%= "CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut()) ? "badge-neutral" : "badge-success" %>">
                                                <%= HtmlEscaper.escape(assignment.getIdAnnee().getStatut()) %></span></td>
                                            <td>
                                                <div class="page-actions">
                                                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/professeur/saisie-notes.jsp">Évaluations et notes</a>
                                                    <a class="btn btn-sm btn-outline" href="${pageContext.request.contextPath}/professeur/absences.jsp">Séances et absences</a>
                                                </div>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } %>
                </div>
            </section>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
