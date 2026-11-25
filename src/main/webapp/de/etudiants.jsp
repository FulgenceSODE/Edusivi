<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat,java.util.List,com.mycompany.DossierEtudiant,com.mycompany.web.HtmlEscaper" %>
<%
    List<DossierEtudiant> students = (List<DossierEtudiant>) request.getAttribute("students");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String message = (String) request.getAttribute("flashMessage");
    if (students == null) {
        response.sendRedirect(request.getContextPath() + "/de/etudiants");
        return;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dossiers étudiants - EduSuivi</title>
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
                    <p class="eyebrow">Scolarité</p>
                    <h1 class="page-title">Dossiers étudiants</h1>
                     </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/de/inscriptions">Gérer les inscriptions</a>
            </div>
            <% if (message != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(message) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">Le dossier étudiant a été enregistré.</div>
            <% } %>
            <div class="stats-grid">
                <div class="stat-card"><span class="stat-label">Dossiers étudiants</span><strong class="stat-value"><%= students.size() %></strong></div>
            </div>
            <section class="card">
                <div class="card-header">
                    <div><h2>Enregistrer un étudiant</h2><p class="form-help">Le matricule est unique .</p></div>
                </div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/etudiants" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="create">
                        <div class="form-group">
                            <label class="form-label" for="new-student-matricule">Matricule</label>
                            <input class="form-control" id="new-student-matricule" name="matricule" maxlength="40" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-student-nom">Nom</label>
                            <input class="form-control" id="new-student-nom" name="nom" maxlength="100" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-student-prenom">Prénom(s)</label>
                            <input class="form-control" id="new-student-prenom" name="prenom" maxlength="100" required>
                        </div>
                        <button class="btn btn-primary" type="submit">Créer le dossier</button>
                    </form>
                </div>
            </section>
            <section class="card">
                <div class="card-header"><div><h2>Répertoire des étudiants</h2><p class="form-help">Les dossiers ne sont pas supprimés, afin de préserver les inscriptions et résultats historiques.</p></div></div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Matricule</th><th>Nom</th><th>Prénom(s)</th><th>Créé le</th><th>Action</th></tr></thead>
                            <tbody>
                                <% for (DossierEtudiant student : students) { %>
                                    <tr>
                                        <td>
                                            <form id="student-<%= student.getIdEtudiant() %>" action="${pageContext.request.contextPath}/de/etudiants" method="POST"></form>
                                            <input form="student-<%= student.getIdEtudiant() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                            <input form="student-<%= student.getIdEtudiant() %>" type="hidden" name="action" value="update">
                                            <input form="student-<%= student.getIdEtudiant() %>" type="hidden" name="studentId" value="<%= student.getIdEtudiant() %>">
                                            <input form="student-<%= student.getIdEtudiant() %>" class="form-control" aria-label="Matricule étudiant" name="matricule" maxlength="40" required value="<%= HtmlEscaper.escape(student.getMatricule()) %>">
                                        </td>
                                        <td><input form="student-<%= student.getIdEtudiant() %>" class="form-control" aria-label="Nom étudiant" name="nom" maxlength="100" required value="<%= HtmlEscaper.escape(student.getNom()) %>"></td>
                                        <td><input form="student-<%= student.getIdEtudiant() %>" class="form-control" aria-label="Prénom étudiant" name="prenom" maxlength="100" required value="<%= HtmlEscaper.escape(student.getPrenom()) %>"></td>
                                        <td><%= dateFormat.format(student.getDateCreation()) %></td>
                                        <td><button form="student-<%= student.getIdEtudiant() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                    </tr>
                                <% } %>
                                <% if (students.isEmpty()) { %>
                                    <tr><td colspan="5"><div class="empty-state"><strong>Aucun dossier étudiant</strong><span>Enregistrez un étudiant pour pouvoir créer son inscription.</span></div></td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
