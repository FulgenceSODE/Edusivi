<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,com.mycompany.Groupe,com.mycompany.InscriptionEtudiant,com.mycompany.Semestre,com.mycompany.web.HtmlEscaper" %>
<%
    List<Groupe> groups = (List<Groupe>) request.getAttribute("groups");
    Groupe selectedGroup = (Groupe) request.getAttribute("selectedGroup");
    List<Semestre> semesters = (List<Semestre>) request.getAttribute("semesters");
    Semestre selectedSemester = (Semestre) request.getAttribute("selectedSemester");
    List<InscriptionEtudiant> enrollments =
            (List<InscriptionEtudiant>) request.getAttribute("enrollments");
    String filterMessage = (String) request.getAttribute("filterMessage");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relevés PDF Jasper - EduSuivi</title>
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
                    <h1 class="page-title">Relevés de notes PDF</h1>
                    <p class="page-subtitle">Chaque document est généré par JasperReports depuis les notes publiées et le dossier d’inscription sélectionné.</p>
                </div>
            </div>
            <% if (filterMessage != null) { %>
                <div class="alert alert-warning" role="status"><%= HtmlEscaper.escape(filterMessage) %></div>
            <% } %>
            <section class="card">
                <div class="card-header"><div><h3>Sélection de la période</h3><p class="form-help">Aucun relevé fictif ni impression navigateur n’est utilisé.</p></div></div>
                <div class="card-body">
                    <% if (groups == null || groups.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun groupe enregistré</strong><span>Les relevés seront disponibles après création des groupes et inscriptions.</span></div>
                    <% } else { %>
                        <form class="form-row" method="GET" action="${pageContext.request.contextPath}/de/rapports.jsp">
                            <div class="form-group">
                                <label class="form-label" for="groupId">Groupe</label>
                                <select class="form-select" id="groupId" name="groupId" required>
                                    <% for (Groupe group : groups) { %>
                                        <option value="<%= group.getIdGroupe() %>" <%= selectedGroup != null && selectedGroup.getIdGroupe().equals(group.getIdGroupe()) ? "selected" : "" %>>
                                            <%= HtmlEscaper.escape(group.getCode() + " — " + group.getIdFiliere().getCode()
                                                    + " / " + group.getIdAnnee().getLibelle()) %>
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
                            <button class="btn btn-primary" type="submit">Afficher les étudiants</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <% if (selectedGroup != null && selectedSemester != null) { %>
                <section class="card">
                    <div class="card-header">
                        <div><h3>Étudiants — <%= HtmlEscaper.escape(selectedGroup.getCode() + " / " + selectedSemester.getCode()) %></h3>
                            <p class="form-help">Le PDF contient les détails EC/UE, les coefficients, les moyennes disponibles et la moyenne semestrielle.</p></div>
                    </div>
                    <div class="card-body">
                        <% if (enrollments == null || enrollments.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucune inscription validée</strong><span>Aucun relevé ne peut être généré pour cette sélection.</span></div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="data-table">
                                    <thead><tr><th>Matricule</th><th>Étudiant</th><th>Document</th></tr></thead>
                                    <tbody>
                                        <% for (InscriptionEtudiant enrollment : enrollments) { %>
                                            <tr>
                                                <td><strong><%= HtmlEscaper.escape(enrollment.getIdEtudiant().getMatricule()) %></strong></td>
                                                <td><%= HtmlEscaper.escape(enrollment.getIdEtudiant().getNom() + " " + enrollment.getIdEtudiant().getPrenom()) %></td>
                                                <td><a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/de/releves.pdf?enrollmentId=<%= enrollment.getIdInscription() %>&amp;semesterId=<%= selectedSemester.getIdSemestre() %>">Télécharger le relevé Jasper (PDF)</a></td>
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
