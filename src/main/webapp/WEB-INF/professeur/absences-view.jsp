<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,java.util.Map,com.mycompany.AffectationEnseignement,com.mycompany.SuiviPedagogiqueSeance,com.mycompany.InscriptionEtudiant,com.mycompany.SuiviPedagogiqueAbsence,com.mycompany.web.HtmlEscaper" %>
<%
    List<AffectationEnseignement> assignments =
            (List<AffectationEnseignement>) request.getAttribute("assignments");
    List<SuiviPedagogiqueSeance> sessions =
            (List<SuiviPedagogiqueSeance>) request.getAttribute("sessions");
    SuiviPedagogiqueSeance selectedSession =
            (SuiviPedagogiqueSeance) request.getAttribute("selectedSession");
    List<InscriptionEtudiant> students =
            (List<InscriptionEtudiant>) request.getAttribute("students");
    Map<Long, SuiviPedagogiqueAbsence> absences =
            (Map<Long, SuiviPedagogiqueAbsence>) request.getAttribute("absences");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String flashMessage = (String) request.getAttribute("flashMessage");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Séances et absences - EduSuivi</title>
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
                    <h1 class="page-title">Séances et absences</h1>
                    <p class="page-subtitle">Planifiez une séance pour l’un de vos enseignements, puis enregistrez les absences.</p>
                </div>
            </div>
            <% if (flashMessage != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(flashMessage) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status">La séance ou les absences ont été enregistrées.</div>
            <% } %>
            <section class="card">
                <div class="card-header"><div><h2>Planifier une séance</h2><p class="form-help">La date doit appartenir au semestre de l’affectation.</p></div></div>
                <div class="card-body">
                    <% if (assignments == null || assignments.isEmpty()) { %>
                        <div class="empty-state"><strong>Aucun enseignement attribué</strong><span>Contactez la direction des études pour vérifier vos affectations.</span></div>
                    <% } else { %>
                        <form class="form-row calendar-create" action="${pageContext.request.contextPath}/professeur/absences.jsp" method="POST">
                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                            <input type="hidden" name="action" value="createSession">
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
                                <label class="form-label" for="sessionDate">Date</label>
                                <input class="form-control" id="sessionDate" type="date" name="sessionDate" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="content">Contenu de la séance</label>
                                <input class="form-control" id="content" name="content" maxlength="500" required>
                            </div>
                            <button class="btn btn-primary" type="submit">Créer la séance</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <section class="card">
                <div class="card-header"><div><h2>Mes séances</h2><p class="form-help">Une absence déjà enregistrée est conservée; sa justification peut être actualisée.</p></div></div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Date</th><th>Enseignement</th><th>Groupe</th><th>Contenu</th><th>Action</th></tr></thead>
                            <tbody>
                                <% if (sessions != null) { for (SuiviPedagogiqueSeance sessionRow : sessions) { %>
                                    <tr>
                                        <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(sessionRow.getDateSeance()) %></td>
                                        <td><%= HtmlEscaper.escape(sessionRow.getIdAffectation().getIdEc().getCode() + " — " + sessionRow.getIdAffectation().getIdEc().getLibelle()) %></td>
                                        <td><%= HtmlEscaper.escape(sessionRow.getIdAffectation().getIdGroupe().getCode()) %></td>
                                        <td><%= HtmlEscaper.escape(sessionRow.getContenu()) %></td>
                                        <td><a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/professeur/absences.jsp?sessionId=<%= sessionRow.getIdSeance() %>">Saisir les absences</a></td>
                                    </tr>
                                <% } } %>
                                <% if (sessions == null || sessions.isEmpty()) { %>
                                    <tr><td colspan="5"><div class="empty-state"><strong>Aucune séance</strong><span>Créez une séance pour commencer le suivi d’assiduité.</span></div></td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
            <% if (selectedSession != null) { %>
                <section class="card">
                    <div class="card-header">
                        <div>
                            <h2>Appel — <%= HtmlEscaper.escape(selectedSession.getContenu()) %></h2>
                            <p class="form-help"><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(selectedSession.getDateSeance()) %>
                                — <%= HtmlEscaper.escape(selectedSession.getIdAffectation().getIdEc().getCode()
                                        + " / " + selectedSession.getIdAffectation().getIdGroupe().getCode()) %></p>
                        </div>
                    </div>
                    <div class="card-body">
                        <% if (students == null || students.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucune inscription validée</strong><span>Les étudiants validés dans ce groupe apparaîtront ici.</span></div>
                        <% } else { %>
                            <form action="${pageContext.request.contextPath}/professeur/absences.jsp" method="POST">
                                <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                <input type="hidden" name="action" value="saveAbsences">
                                <input type="hidden" name="sessionId" value="<%= selectedSession.getIdSeance() %>">
                                <div class="table-responsive">
                                    <table class="data-table">
                                        <thead><tr><th>Matricule</th><th>Étudiant</th><th>Absence</th><th>Justifiée</th><th>Motif</th></tr></thead>
                                        <tbody>
                                            <% for (InscriptionEtudiant student : students) {
                                                Long studentId = student.getIdEtudiant().getIdEtudiant();
                                                SuiviPedagogiqueAbsence absence = absences == null ? null : absences.get(studentId);
                                            %>
                                                <tr>
                                                    <td><%= HtmlEscaper.escape(student.getIdEtudiant().getMatricule()) %></td>
                                                    <td><%= HtmlEscaper.escape(student.getIdEtudiant().getNom() + " " + student.getIdEtudiant().getPrenom()) %></td>
                                                    <td>
                                                        <% if (absence == null) { %>
                                                            <label><input type="checkbox" name="absentStudentIds" value="<%= studentId %>"> Absent</label>
                                                        <% } else { %>
                                                            <input type="hidden" name="absentStudentIds" value="<%= studentId %>">
                                                            <span class="badge badge-warning">Absence enregistrée</span>
                                                        <% } %>
                                                    </td>
                                                    <td><label><input type="checkbox" name="justified_<%= studentId %>" value="true" <%= absence != null && absence.getJustifiee() ? "checked" : "" %>> Oui</label></td>
                                                    <td><input class="form-control" name="reason_<%= studentId %>" maxlength="500" value="<%= HtmlEscaper.escape(absence == null ? "" : absence.getMotif()) %>" aria-label="Motif pour <%= HtmlEscaper.escape(student.getIdEtudiant().getNom()) %>"></td>
                                                </tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
                                <button class="btn btn-primary" type="submit">Enregistrer l’appel</button>
                            </form>
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
