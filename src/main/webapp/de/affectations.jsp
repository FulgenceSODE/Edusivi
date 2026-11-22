<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,com.mycompany.Utilisateur,com.mycompany.ElementConstitutif,com.mycompany.Groupe,com.mycompany.Semestre,com.mycompany.AffectationEnseignement,com.mycompany.web.HtmlEscaper" %>
<%
    List<Utilisateur> teachers = (List<Utilisateur>) request.getAttribute("teachers");
    List<ElementConstitutif> courseElements = (List<ElementConstitutif>) request.getAttribute("courseElements");
    List<Groupe> groups = (List<Groupe>) request.getAttribute("groups");
    List<Semestre> semesters = (List<Semestre>) request.getAttribute("semesters");
    List<AffectationEnseignement> assignments = (List<AffectationEnseignement>) request.getAttribute("assignments");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String message = (String) request.getAttribute("flashMessage");
    if (teachers == null || courseElements == null || groups == null || semesters == null || assignments == null) {
        response.sendRedirect(request.getContextPath() + "/de/affectations");
        return;
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Affectations d’enseignement - EduSuivi</title>
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
                    <p class="eyebrow">Organisation pédagogique</p>
                    <h1 class="page-title">Affectations d’enseignement</h1>
                    <p class="page-subtitle">Affectez un professeur responsable à un EC, un groupe et un semestre de la même année académique.</p>
                </div>
            </div>
            <% if (message != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(message) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">L’affectation a été enregistrée.</div>
            <% } %>
            <div class="stats-grid">
                <article class="stat-card"><span class="stat-label">Affectations</span><strong class="stat-value"><%= assignments.size() %></strong><span class="form-help">Toutes les années conservées</span></article>
                <article class="stat-card"><span class="stat-label">Professeurs actifs</span><strong class="stat-value"><%= teachers.size() %></strong><span class="form-help">Comptes autorisés</span></article>
            </div>
            <section class="card">
                <div class="card-header">
                    <div><h2>Nouvelle affectation</h2><p class="form-help">Un seul professeur responsable par EC, groupe et année. Les affectations ne sont pas supprimées.</p></div>
                </div>
                <div class="card-body">
                    <% if (teachers.isEmpty() || courseElements.isEmpty() || groups.isEmpty() || semesters.isEmpty()) { %>
                        <div class="empty-state"><strong>Création indisponible</strong><span>Il faut un professeur actif, un EC, un groupe et un semestre d’année non clôturée.</span></div>
                    <% } else { %>
                        <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/affectations" method="POST">
                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                            <input type="hidden" name="action" value="create">
                            <div class="form-group">
                                <label class="form-label" for="assignment-group">Groupe et année</label>
                                <select class="form-select" id="assignment-group" name="groupId" required>
                                    <option value="">Choisir un groupe</option>
                                    <% for (Groupe group : groups) { %>
                                        <option value="<%= group.getIdGroupe() %>" data-year="<%= group.getIdAnnee().getIdAnnee() %>"><%= HtmlEscaper.escape(group.getIdAnnee().getLibelle() + " — " + group.getIdFiliere().getCode() + " / " + group.getIdNiveau().getCode() + " / " + group.getCode()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="assignment-semester">Semestre</label>
                                <select class="form-select" id="assignment-semester" name="semesterId" required>
                                    <option value="">Choisir un semestre</option>
                                    <% for (Semestre semester : semesters) { %>
                                        <option value="<%= semester.getIdSemestre() %>" data-year="<%= semester.getIdAnnee().getIdAnnee() %>"><%= HtmlEscaper.escape(semester.getIdAnnee().getLibelle() + " — " + semester.getCode() + " — " + semester.getLibelle()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="assignment-element">Élément constitutif (EC)</label>
                                <select class="form-select" id="assignment-element" name="elementId" required>
                                    <option value="">Choisir un EC</option>
                                    <% for (ElementConstitutif element : courseElements) { %>
                                        <option value="<%= element.getIdEc() %>"><%= HtmlEscaper.escape(element.getCode() + " — " + element.getLibelle()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="assignment-teacher">Professeur responsable</label>
                                <select class="form-select" id="assignment-teacher" name="teacherId" required>
                                    <option value="">Choisir un professeur</option>
                                    <% for (Utilisateur teacher : teachers) { %>
                                        <option value="<%= teacher.getIdUtilisateur() %>"><%= HtmlEscaper.escape(teacher.getNom() + " " + teacher.getPrenom() + " — " + teacher.getEmail()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <button class="btn btn-primary" type="submit">Créer l’affectation</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <section class="card">
                <div class="card-header">
                    <div><h2>Répertoire des affectations</h2><p class="form-help">Une réaffectation est possible avant toute séance, chapitre ou évaluation. L’historique des changements est audité.</p></div>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Année / semestre</th><th>EC</th><th>Groupe</th><th>Professeur responsable</th><th>Attribuée le</th><th>Action</th></tr></thead>
                            <tbody>
                                <% for (AffectationEnseignement assignment : assignments) { %>
                                    <tr>
                                        <td><strong><%= HtmlEscaper.escape(assignment.getIdAnnee().getLibelle()) %></strong><br><span class="text-muted"><%= HtmlEscaper.escape(assignment.getIdSemestre().getCode() + " — " + assignment.getIdSemestre().getLibelle()) %></span></td>
                                        <td><strong><%= HtmlEscaper.escape(assignment.getIdEc().getCode()) %></strong><br><%= HtmlEscaper.escape(assignment.getIdEc().getLibelle()) %></td>
                                        <td><%= HtmlEscaper.escape(assignment.getIdGroupe().getIdFiliere().getCode() + " / " + assignment.getIdGroupe().getIdNiveau().getCode() + " / " + assignment.getIdGroupe().getCode()) %></td>
                                        <td><%= HtmlEscaper.escape(assignment.getIdUtilisateur().getNom() + " " + assignment.getIdUtilisateur().getPrenom()) %><br><span class="text-muted"><%= HtmlEscaper.escape(assignment.getIdUtilisateur().getEmail()) %></span></td>
                                        <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(assignment.getDateAffectation()) %></td>
                                        <td>
                                            <% if (!"CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut()) && !teachers.isEmpty()) { %>
                                                <form action="${pageContext.request.contextPath}/de/affectations" method="POST">
                                                    <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                    <input type="hidden" name="action" value="reassign">
                                                    <input type="hidden" name="assignmentId" value="<%= assignment.getIdAffectation() %>">
                                                    <label class="sr-only" for="teacher-<%= assignment.getIdAffectation() %>">Nouveau professeur responsable</label>
                                                    <select class="form-select" id="teacher-<%= assignment.getIdAffectation() %>" name="teacherId" required>
                                                        <option value="">Réaffecter à…</option>
                                                        <% for (Utilisateur teacher : teachers) { %>
                                                            <option value="<%= teacher.getIdUtilisateur() %>"><%= HtmlEscaper.escape(teacher.getNom() + " " + teacher.getPrenom()) %></option>
                                                        <% } %>
                                                    </select>
                                                    <button class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button>
                                                </form>
                                            <% } else { %>
                                                <span class="text-muted"><%= "CLOTUREE".equalsIgnoreCase(assignment.getIdAnnee().getStatut()) ? "Année clôturée" : "Aucun professeur actif" %></span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                                <% if (assignments.isEmpty()) { %>
                                    <tr><td colspan="6"><div class="empty-state"><strong>Aucune affectation</strong><span>Créez une affectation pour relier un EC, un groupe, un semestre et un professeur.</span></div></td></tr>
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
<script>
    (function () {
        var groupSelect = document.getElementById("assignment-group");
        var semesterSelect = document.getElementById("assignment-semester");
        if (!groupSelect || !semesterSelect) return;
        function synchronizeSemesters() {
            var yearId = groupSelect.options[groupSelect.selectedIndex].getAttribute("data-year");
            Array.prototype.forEach.call(semesterSelect.options, function (option) {
                if (!option.value) return;
                option.hidden = Boolean(yearId && option.getAttribute("data-year") !== yearId);
                option.disabled = option.hidden;
            });
            if (semesterSelect.selectedOptions.length
                    && semesterSelect.selectedOptions[0].disabled) {
                semesterSelect.value = "";
            }
        }
        groupSelect.addEventListener("change", synchronizeSemesters);
        synchronizeSemesters();
    }());
</script>
</body>
</html>
