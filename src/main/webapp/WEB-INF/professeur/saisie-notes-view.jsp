<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,java.util.Map,com.mycompany.AffectationEnseignement,com.mycompany.EvaluationCours,com.mycompany.InscriptionEtudiant,com.mycompany.NoteEvaluation,com.mycompany.web.HtmlEscaper" %>
<%
    List<AffectationEnseignement> assignments =
            (List<AffectationEnseignement>) request.getAttribute("assignments");
    List<EvaluationCours> evaluations =
            (List<EvaluationCours>) request.getAttribute("evaluations");
    EvaluationCours selectedEvaluation = (EvaluationCours) request.getAttribute("selectedEvaluation");
    List<InscriptionEtudiant> students =
            (List<InscriptionEtudiant>) request.getAttribute("students");
    Map<Long, NoteEvaluation> grades =
            (Map<Long, NoteEvaluation>) request.getAttribute("grades");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String flashMessage = (String) request.getAttribute("flashMessage");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Évaluations et notes - EduSuivi</title>
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
                    <h1 class="page-title">Évaluations et saisie des notes</h1>
                    <p class="page-subtitle">Les notes sont enregistrées par inscription étudiante, puis publiées explicitement.</p>
                </div>
            </div>
            <% if (flashMessage != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(flashMessage) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status">Les modifications ont été enregistrées.</div>
            <% } %>
            <section class="card">
                <div class="card-header">
                    <div><h2>Créer une évaluation</h2><p class="form-help">Seuls vos enseignements et les dates du semestre sont proposés.</p></div>
                </div>
                <div class="card-body">
                    <% if (assignments == null || assignments.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun enseignement attribué</strong><span>Contactez la direction des études pour vérifier vos affectations.</span></div>
                    <% } else { %>
                        <form class="form-row calendar-create" action="${pageContext.request.contextPath}/professeur/saisie-notes.jsp" method="POST">
                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                            <input type="hidden" name="action" value="createEvaluation">
                            <div class="form-group">
                                <label class="form-label" for="assignmentId">Enseignement</label>
                                <select class="form-select" id="assignmentId" name="assignmentId" required>
                                    <option value="">Choisir un enseignement</option>
                                    <% for (AffectationEnseignement assignment : assignments) { %>
                                        <option value="<%= assignment.getIdAffectation() %>"><%= HtmlEscaper.escape(
                                                assignment.getIdEc().getCode() + " — "
                                                + assignment.getIdEc().getLibelle() + " / "
                                                + assignment.getIdGroupe().getCode() + " / "
                                                + assignment.getIdSemestre().getCode() + " — "
                                                + assignment.getIdAnnee().getLibelle()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="title">Titre</label>
                                <input class="form-control" id="title" name="title" maxlength="150" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="category">Catégorie</label>
                                <select class="form-select" id="category" name="category" required>
                                    <option value="INTERROGATION">INTERROGATION</option>
                                    <option value="DEVOIR">DEVOIR</option>
                                    <option value="EXAMEN">EXAMEN</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="evaluationDate">Date</label>
                                <input class="form-control" id="evaluationDate" type="date" name="evaluationDate" required>
                            </div>
                            <button class="btn btn-primary" type="submit">Créer</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <section class="card">
                <div class="card-header"><div><h2>Mes évaluations</h2><p class="form-help">Le barème est fixé à 20. Une évaluation publiée n’est plus modifiable ici.</p></div></div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Évaluation</th><th>Enseignement</th><th>Date</th><th>Catégorie</th><th>Publication</th><th>Action</th></tr></thead>
                            <tbody>
                                <% if (evaluations != null) { for (EvaluationCours evaluation : evaluations) { %>
                                    <tr>
                                        <td><%= HtmlEscaper.escape(evaluation.getLibelle()) %></td>
                                        <td><%= HtmlEscaper.escape(evaluation.getIdAffectation().getIdEc().getCode() + " / " + evaluation.getIdAffectation().getIdGroupe().getCode()) %></td>
                                        <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(evaluation.getDateEvaluation()) %></td>
                                        <td><%= HtmlEscaper.escape(evaluation.getCategorie()) %> / 20</td>
                                        <td><%= HtmlEscaper.escape(evaluation.getStatutPublication()) %></td>
                                        <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/professeur/saisie-notes.jsp?evaluationId=<%= evaluation.getIdEvaluation() %>">Grille de notes</a></td>
                                    </tr>
                                <% } } %>
                                <% if (evaluations == null || evaluations.isEmpty()) { %>
                                    <tr><td colspan="6"><div class="empty-state"><strong>Aucune évaluation</strong><span>Créez une évaluation pour commencer la saisie des notes.</span></div></td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
            <% if (selectedEvaluation != null) { %>
                <section class="card">
                    <div class="card-header">
                        <div>
                            <h2>Notes — <%= HtmlEscaper.escape(selectedEvaluation.getLibelle()) %></h2>
                            <p class="form-help"><%= HtmlEscaper.escape(selectedEvaluation.getIdAffectation().getIdEc().getCode()
                                    + " / " + selectedEvaluation.getIdAffectation().getIdGroupe().getCode()
                                    + " / " + selectedEvaluation.getIdAffectation().getIdAnnee().getLibelle()) %></p>
                        </div>
                    </div>
                    <div class="card-body">
                        <% if (students == null || students.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucune inscription validée</strong><span>Les étudiants inscrits et validés dans ce groupe apparaîtront ici.</span></div>
                        <% } else { %>
                            <form action="${pageContext.request.contextPath}/professeur/saisie-notes.jsp" method="POST">
                                <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                <input type="hidden" name="evaluationId" value="<%= selectedEvaluation.getIdEvaluation() %>">
                                <input type="hidden" name="action" value="saveGrades">
                                <div class="table-responsive">
                                    <table class="data-table">
                                        <thead><tr><th>Matricule</th><th>Étudiant</th><th>Note / 20</th></tr></thead>
                                        <tbody>
                                            <% for (InscriptionEtudiant student : students) {
                                                NoteEvaluation grade = grades == null ? null : grades.get(student.getIdInscription());
                                                String value = grade == null ? "" : grade.getValeur().toPlainString();
                                            %>
                                                <tr>
                                                    <td><%= HtmlEscaper.escape(student.getIdEtudiant().getMatricule()) %></td>
                                                    <td><%= HtmlEscaper.escape(student.getIdEtudiant().getNom() + " " + student.getIdEtudiant().getPrenom()) %></td>
                                                    <td><input class="form-control grade-input" type="number" min="0" max="20" step="0.01" inputmode="decimal" name="grade_<%= student.getIdInscription() %>" value="<%= HtmlEscaper.escape(value) %>" <%= "PUBLIEE".equalsIgnoreCase(selectedEvaluation.getStatutPublication()) ? "disabled" : "" %> aria-label="Note de <%= HtmlEscaper.escape(student.getIdEtudiant().getNom()) %>"></td>
                                                </tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                                <% if (!"PUBLIEE".equalsIgnoreCase(selectedEvaluation.getStatutPublication())) { %>
                                    <button class="btn btn-primary" type="submit">Enregistrer les notes</button>
                                <% } %>
                            </form>
                            <% if (!"PUBLIEE".equalsIgnoreCase(selectedEvaluation.getStatutPublication())) { %>
                                <form action="${pageContext.request.contextPath}/professeur/saisie-notes.jsp" method="POST" class="form-inline form-spaced">
                                    <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                    <input type="hidden" name="evaluationId" value="<%= selectedEvaluation.getIdEvaluation() %>">
                                    <input type="hidden" name="action" value="publish">
                                    <button class="btn btn-outline-primary" type="submit">Publier les notes saisies</button>
                                </form>
                            <% } %>
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
