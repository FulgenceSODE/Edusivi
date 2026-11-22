<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,java.util.Map,java.math.BigDecimal,com.mycompany.Groupe,com.mycompany.Semestre,com.mycompany.web.SemesterGradeCalculator,com.mycompany.web.HtmlEscaper" %>
<%
    List<Groupe> groups = (List<Groupe>) request.getAttribute("groups");
    Groupe selectedGroup = (Groupe) request.getAttribute("selectedGroup");
    List<Semestre> semesters = (List<Semestre>) request.getAttribute("semesters");
    Semestre selectedSemester = (Semestre) request.getAttribute("selectedSemester");
    List<SemesterGradeCalculator.StudentResult> results =
            (List<SemesterGradeCalculator.StudentResult>) request.getAttribute("results");
    String filterMessage = (String) request.getAttribute("filterMessage");
    List<SemesterGradeCalculator.UeResult> ueColumns =
            results != null && !results.isEmpty() ? results.get(0).ueResults() : List.of();
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Résultats académiques - EduSuivi</title>
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
                    <p class="eyebrow">Direction des études</p>
                    <h1 class="page-title">Résultats et moyennes</h1>
                    <p class="page-subtitle">Calcul dynamique des EC, UE et moyennes semestrielles à partir des notes publiées.</p>
                </div>
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/de/rapports.jsp">Relevés Jasper</a>
            </div>
            <% if (filterMessage != null) { %>
                <div class="alert alert-warning" role="status"><%= HtmlEscaper.escape(filterMessage) %></div>
            <% } %>
            <section class="card">
                <div class="card-header"><div><h3>Période académique</h3><p class="form-help">Les inscriptions validées du groupe sélectionné sont calculées.</p></div></div>
                <div class="card-body">
                    <% if (groups == null || groups.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun groupe enregistré</strong><span>Créez ou rattachez un groupe avant de consulter les résultats.</span></div>
                    <% } else { %>
                        <form class="form-row" method="GET" action="${pageContext.request.contextPath}/de/resultats.jsp">
                            <div class="form-group">
                                <label class="form-label" for="groupId">Groupe</label>
                                <select class="form-select" id="groupId" name="groupId" required>
                                    <% for (Groupe group : groups) { %>
                                        <option value="<%= group.getIdGroupe() %>" <%= selectedGroup != null && selectedGroup.getIdGroupe().equals(group.getIdGroupe()) ? "selected" : "" %>>
                                            <%= HtmlEscaper.escape(group.getCode() + " — " + group.getIdFiliere().getCode()
                                                    + " / " + group.getIdNiveau().getLibelle() + " / "
                                                    + group.getIdAnnee().getLibelle()) %>
                                        </option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="semesterId">Semestre</label>
                                <select class="form-select" id="semesterId" name="semesterId" required>
                                    <% if (semesters != null) { for (Semestre semester : semesters) { %>
                                        <option value="<%= semester.getIdSemestre() %>" <%= selectedSemester != null && selectedSemester.getIdSemestre().equals(semester.getIdSemestre()) ? "selected" : "" %>>
                                            <%= HtmlEscaper.escape(semester.getCode() + " — " + semester.getLibelle()) %>
                                        </option>
                                    <% } } %>
                                </select>
                            </div>
                            <button class="btn btn-primary" type="submit">Afficher les résultats</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <% if (selectedGroup != null && selectedSemester != null) { %>
                <div class="grid-4">
                    <div class="stat-card"><div class="stat-value"><%= results == null ? 0 : results.size() %></div><div class="stat-label">Inscriptions validées</div></div>
                    <div class="stat-card"><div class="stat-value"><%= ueColumns.size() %></div><div class="stat-label">UE au programme</div></div>
                    <div class="stat-card"><div class="stat-value">2</div><div class="stat-label">Décimales affichées</div></div>
                    <div class="stat-card"><div class="stat-value">Jasper</div><div class="stat-label">Relevé PDF officiel</div></div>
                </div>
                <section class="card">
                    <div class="card-header">
                        <div>
                            <h3>Résultats — <%= HtmlEscaper.escape(selectedGroup.getCode()) %> / <%= HtmlEscaper.escape(selectedSemester.getCode()) %></h3>
                            <p class="form-help">Les catégories disponibles ont un poids égal dans la moyenne EC; l’UE est pondérée par les coefficients des EC disponibles. La moyenne semestrielle est la moyenne arithmétique des moyennes UE disponibles.</p>
                        </div>
                    </div>
                    <div class="card-body">
                        <% if (results == null || results.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucune inscription validée</strong><span>Aucun résultat ne peut être calculé pour cette période.</span></div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="data-table">
                                    <thead>
                                        <tr>
                                            <th>Matricule et étudiant</th>
                                            <% for (SemesterGradeCalculator.UeResult ue : ueColumns) { %>
                                                <th><%= HtmlEscaper.escape(ue.code() + " — " + ue.label()) %> / 20</th>
                                            <% } %>
                                            <th>Moyenne semestrielle / 20</th>
                                            <th>Relevé</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <% for (SemesterGradeCalculator.StudentResult studentResult : results) { %>
                                            <tr>
                                                <td><strong><%= HtmlEscaper.escape(studentResult.enrollment().matricule()) %></strong><br>
                                                    <%= HtmlEscaper.escape(studentResult.enrollment().nom() + " " + studentResult.enrollment().prenom()) %></td>
                                                <% for (SemesterGradeCalculator.UeResult ue : studentResult.ueResults()) { %>
                                                    <td>
                                                        <strong><%= SemesterGradeCalculator.formatForDisplay(ue.average()) %></strong>
                                                        <% if (!ue.ecResults().isEmpty()) { %>
                                                            <details>
                                                                <summary>Détail des EC</summary>
                                                                <ul class="grade-details">
                                                                    <% for (SemesterGradeCalculator.EcResult ec : ue.ecResults()) { %>
                                                                        <li><%= HtmlEscaper.escape(ec.code() + " — " + ec.label()) %> (coeff. <%= SemesterGradeCalculator.formatForDisplay(ec.coefficient()) %>) :
                                                                            <%= SemesterGradeCalculator.formatForDisplay(ec.average()) %></li>
                                                                    <% } %>
                                                                </ul>
                                                            </details>
                                                        <% } %>
                                                    </td>
                                                <% } %>
                                                <td><strong><%= SemesterGradeCalculator.formatForDisplay(studentResult.semesterAverage()) %></strong></td>
                                                <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/de/releves.pdf?enrollmentId=<%= studentResult.enrollment().id() %>&amp;semesterId=<%= selectedSemester.getIdSemestre() %>">PDF Jasper</a></td>
                                            </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                        <% } %>
                    </div>
                </section>
            <% } %>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
