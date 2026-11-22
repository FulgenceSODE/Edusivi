<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,java.util.Map,java.util.Date,java.text.SimpleDateFormat,com.mycompany.AffectationEnseignement,com.mycompany.ChapitreCours,com.mycompany.web.HtmlEscaper" %>
<%
    List<AffectationEnseignement> assignments =
            (List<AffectationEnseignement>) request.getAttribute("assignments");
    AffectationEnseignement selectedAssignment =
            (AffectationEnseignement) request.getAttribute("selectedAssignment");
    List<ChapitreCours> chapters = (List<ChapitreCours>) request.getAttribute("chapters");
    List<String> progressStates = (List<String>) request.getAttribute("progressStates");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String flashMessage = (String) request.getAttribute("flashMessage");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    Map<String, String> labels = Map.of("A_VENIR", "À venir", "EN_COURS", "En cours", "TERMINE", "Terminé");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chapitres et progression - EduSuivi</title>
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
                    <h1 class="page-title">Chapitres et progression</h1>
                    <p class="page-subtitle">Enregistrez le plan de cours de vos enseignements et son état d’avancement.</p>
                </div>
                <div class="page-actions">
                    <a class="btn btn-outline" href="${pageContext.request.contextPath}/professeur/absences.jsp">Séances et absences</a>
                </div>
            </div>
            <% if (flashMessage != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(flashMessage) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status">Le chapitre ou sa progression a été enregistré.</div>
            <% } %>
            <section class="card">
                <div class="card-header"><div><h3>Enseignement suivi</h3><p class="form-help">Seuls vos enseignements attribués sont disponibles.</p></div></div>
                <div class="card-body">
                    <% if (assignments == null || assignments.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun enseignement attribué</strong><span>Contactez la direction des études pour vérifier vos affectations.</span></div>
                    <% } else { %>
                        <form class="form-row" method="GET" action="${pageContext.request.contextPath}/professeur/seances-chapitres.jsp">
                            <div class="form-group">
                                <label class="form-label" for="assignmentId">Enseignement</label>
                                <select class="form-select" id="assignmentId" name="assignmentId" required>
                                    <% for (AffectationEnseignement assignment : assignments) { %>
                                        <option value="<%= assignment.getIdAffectation() %>" <%= selectedAssignment != null && selectedAssignment.getIdAffectation().equals(assignment.getIdAffectation()) ? "selected" : "" %>>
                                            <%= HtmlEscaper.escape(assignment.getIdEc().getCode() + " — " + assignment.getIdEc().getLibelle()
                                                    + " / " + assignment.getIdGroupe().getCode() + " / "
                                                    + assignment.getIdSemestre().getCode() + " — " + assignment.getIdAnnee().getLibelle()) %>
                                        </option>
                                    <% } %>
                                </select>
                            </div>
                            <button class="btn btn-outline-primary" type="submit">Afficher</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <% if (selectedAssignment != null) { %>
                <section class="card">
                    <div class="card-header"><div><h3>Ajouter un chapitre</h3><p class="form-help">L’ordre est attribué automatiquement; le chapitre commence « À venir ».</p></div></div>
                    <div class="card-body">
                        <form class="form-row" method="POST" action="${pageContext.request.contextPath}/professeur/seances-chapitres.jsp">
                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                            <input type="hidden" name="action" value="createChapter">
                            <input type="hidden" name="assignmentId" value="<%= selectedAssignment.getIdAffectation() %>">
                            <div class="form-group">
                                <label class="form-label" for="title">Titre du chapitre</label>
                                <input class="form-control" id="title" name="title" maxlength="150" required>
                            </div>
                            <button class="btn btn-primary" type="submit">Ajouter le chapitre</button>
                        </form>
                    </div>
                </section>
                <section class="card">
                    <div class="card-header"><div><h3>Plan de cours</h3><p class="form-help">Dernière mise à jour affichée pour chaque chapitre.</p></div></div>
                    <div class="card-body">
                        <% if (chapters == null || chapters.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucun chapitre enregistré</strong><span>Ajoutez les chapitres prévus pour cet enseignement.</span></div>
                        <% } else { %>
                            <div class="table-responsive">
                                <table class="data-table">
                                    <thead><tr><th>Ordre</th><th>Chapitre</th><th>Progression</th><th>Mise à jour</th><th>Action</th></tr></thead>
                                    <tbody>
                                        <% for (ChapitreCours chapter : chapters) { %>
                                            <tr>
                                                <td><%= chapter.getOrdre() %></td>
                                                <td><strong><%= HtmlEscaper.escape(chapter.getTitre()) %></strong></td>
                                                <td><span class="badge <%= "TERMINE".equals(chapter.getProgression()) ? "badge-success" : "EN_COURS".equals(chapter.getProgression()) ? "badge-primary" : "badge-neutral" %>">
                                                    <%= HtmlEscaper.escape(labels.getOrDefault(chapter.getProgression(), chapter.getProgression())) %></span></td>
                                                <td><%= dateFormat.format(chapter.getDateMiseAJour()) %></td>
                                                <td>
                                                    <form class="approval-form" method="POST" action="${pageContext.request.contextPath}/professeur/seances-chapitres.jsp">
                                                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                        <input type="hidden" name="action" value="updateProgress">
                                                        <input type="hidden" name="chapterId" value="<%= chapter.getIdChapitre() %>">
                                                        <input type="hidden" name="assignmentId" value="<%= selectedAssignment.getIdAffectation() %>">
                                                        <select class="form-select" name="progression" aria-label="Progression du chapitre <%= chapter.getOrdre() %>">
                                                            <% for (String state : progressStates) { %>
                                                                <option value="<%= state %>" <%= state.equals(chapter.getProgression()) ? "selected" : "" %>><%= HtmlEscaper.escape(labels.get(state)) %></option>
                                                            <% } %>
                                                        </select>
                                                        <button class="btn btn-sm btn-outline-primary" type="submit">Mettre à jour</button>
                                                    </form>
                                                </td>
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
