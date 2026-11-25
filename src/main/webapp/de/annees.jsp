<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat,java.util.List,com.mycompany.AnneeAcademique,com.mycompany.Semestre,com.mycompany.web.HtmlEscaper" %>
<%
    List<AnneeAcademique> years = (List<AnneeAcademique>) request.getAttribute("academicYears");
    List<Semestre> semesters = (List<Semestre>) request.getAttribute("semesters");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String message = (String) session.getAttribute("academicCalendarMessage");
    if (message != null) session.removeAttribute("academicCalendarMessage");
    if (years == null || semesters == null) {
        response.sendRedirect(request.getContextPath() + "/de/annees");
        return;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Années et semestres - EduSuivi</title>
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
                    <p class="eyebrow">Organisation académique</p>
                    <h1 class="page-title">Années et semestres</h1>
                </div>
            </div>

            <% if (message != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(message) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">La modification a été enregistrée.</div>
            <% } %>

            <details class="card calendar-accordion" name="academic-calendar" open>
                <summary class="card-header calendar-accordion-summary">
                    <div>
                        <h2>Années académiques</h2>
                    </div>
                    <span class="badge badge-primary"><%= years.size() %> année(s)</span>
                </summary>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr><th>Année académique</th></tr>
                            </thead>
                            <tbody>
                                <% for (AnneeAcademique year : years) { %>
                                    <tr>
                                        <td>
                                            <details class="calendar-entry" name="academic-year">
                                                <summary class="calendar-entry-summary">
                                                    <strong><%= HtmlEscaper.escape(year.getLibelle()) %></strong>
                                                    <span class="badge <%= "ACTIVE".equalsIgnoreCase(year.getStatut()) ? "badge-success" : ("CLOTUREE".equalsIgnoreCase(year.getStatut()) ? "badge-neutral" : "badge-warning") %>"><%= HtmlEscaper.escape(year.getStatut()) %></span>
                                                </summary>
                                                <form class="calendar-row" action="${pageContext.request.contextPath}/de/annees" method="POST">
                                                    <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                    <input type="hidden" name="action" value="saveYear">
                                                    <input type="hidden" name="yearId" value="<%= year.getIdAnnee() %>">
                                                    <label class="sr-only" for="year-label-<%= year.getIdAnnee() %>">Libellé de l’année</label>
                                                    <input class="form-control" id="year-label-<%= year.getIdAnnee() %>" name="label" maxlength="30" required value="<%= HtmlEscaper.escape(year.getLibelle()) %>">
                                                    <label class="sr-only" for="year-start-<%= year.getIdAnnee() %>">Date de début</label>
                                                    <input class="form-control" id="year-start-<%= year.getIdAnnee() %>" type="date" name="startDate" required value="<%= dateFormat.format(year.getDateDebut()) %>">
                                                    <label class="sr-only" for="year-end-<%= year.getIdAnnee() %>">Date de fin</label>
                                                    <input class="form-control" id="year-end-<%= year.getIdAnnee() %>" type="date" name="endDate" required value="<%= dateFormat.format(year.getDateFin()) %>">
                                                    <button class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button>
                                                </form>
                                                <% if (!"ACTIVE".equalsIgnoreCase(year.getStatut()) && !"CLOTUREE".equalsIgnoreCase(year.getStatut())) { %>
                                                    <div class="calendar-row-actions">
                                                        <form action="${pageContext.request.contextPath}/de/annees" method="POST">
                                                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                            <input type="hidden" name="action" value="activateYear">
                                                            <input type="hidden" name="yearId" value="<%= year.getIdAnnee() %>">
                                                            <button class="btn btn-sm btn-primary" type="submit">Activer</button>
                                                        </form>
                                                        <form action="${pageContext.request.contextPath}/de/annees" method="POST">
                                                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                            <input type="hidden" name="action" value="closeYear">
                                                            <input type="hidden" name="yearId" value="<%= year.getIdAnnee() %>">
                                                            <button class="btn btn-sm btn-outline" type="submit"
                                                                    data-confirm="Clôturer définitivement cette année académique ?">Clôturer</button>
                                                        </form>
                                                    </div>
                                                <% } %>
                                            </details>
                                        </td>
                                    </tr>
                                <% } %>
                                <% if (years.isEmpty()) { %>
                                    <tr><td><div class="empty-state"><strong>Aucune année académique</strong><span>Créez une année pour organiser les semestres et les inscriptions.</span></div></td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </details>

            <details class="card calendar-accordion" name="academic-calendar">
                <summary class="card-header calendar-accordion-summary">
                    <h2>Créer une année académique</h2>
                </summary>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/annees" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="saveYear">
                        <div class="form-group">
                            <label class="form-label" for="new-year-label">Libellé</label>
                            <input class="form-control" id="new-year-label" name="label" maxlength="30" placeholder="2026-2027" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-year-start">Date de début</label>
                            <input class="form-control" id="new-year-start" type="date" name="startDate" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="new-year-end">Date de fin</label>
                            <input class="form-control" id="new-year-end" type="date" name="endDate" required>
                        </div>
                        <button class="btn btn-primary" type="submit">Créer l’année</button>
                    </form>
                </div>
            </details>

            <details class="card calendar-accordion" name="academic-calendar">
                <summary class="card-header calendar-accordion-summary">
                    <div>
                        <h2>Semestres</h2>
                        <p class="form-help">Les dates de chaque semestre doivent être comprises dans son année académique.</p>
                    </div>
                    <span class="badge badge-neutral"><%= semesters.size() %> semestre(s)</span>
                </summary>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Année</th><th>Semestre</th></tr></thead>
                            <tbody>
                                <% for (Semestre semester : semesters) { %>
                                    <tr>
                                        <td><%= HtmlEscaper.escape(semester.getIdAnnee().getLibelle()) %></td>
                                        <td>
                                            <details class="calendar-entry" name="academic-semester">
                                                <summary class="calendar-entry-summary">
                                                    <strong><%= HtmlEscaper.escape(semester.getCode()) %> - <%= HtmlEscaper.escape(semester.getLibelle()) %></strong>
                                                </summary>
                                                <form class="calendar-row semester-row" action="${pageContext.request.contextPath}/de/annees" method="POST">
                                                    <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                    <input type="hidden" name="action" value="saveSemester">
                                                    <input type="hidden" name="semesterId" value="<%= semester.getIdSemestre() %>">
                                                    <input type="hidden" name="yearId" value="<%= semester.getIdAnnee().getIdAnnee() %>">
                                                    <label class="sr-only" for="semester-code-<%= semester.getIdSemestre() %>">Code</label>
                                                    <input class="form-control" id="semester-code-<%= semester.getIdSemestre() %>" name="code" maxlength="20" required value="<%= HtmlEscaper.escape(semester.getCode()) %>">
                                                    <label class="sr-only" for="semester-label-<%= semester.getIdSemestre() %>">Libellé</label>
                                                    <input class="form-control" id="semester-label-<%= semester.getIdSemestre() %>" name="semesterLabel" maxlength="100" required value="<%= HtmlEscaper.escape(semester.getLibelle()) %>">
                                                    <label class="sr-only" for="semester-start-<%= semester.getIdSemestre() %>">Date de début</label>
                                                    <input class="form-control" id="semester-start-<%= semester.getIdSemestre() %>" type="date" name="semesterStartDate" required value="<%= dateFormat.format(semester.getDateDebut()) %>">
                                                    <label class="sr-only" for="semester-end-<%= semester.getIdSemestre() %>">Date de fin</label>
                                                    <input class="form-control" id="semester-end-<%= semester.getIdSemestre() %>" type="date" name="semesterEndDate" required value="<%= dateFormat.format(semester.getDateFin()) %>">
                                                    <button class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button>
                                                </form>
                                            </details>
                                        </td>
                                    </tr>
                                <% } %>
                                <% if (semesters.isEmpty()) { %>
                                    <tr><td colspan="2"><div class="empty-state"><strong>Aucun semestre</strong><span>Ajoutez les semestres après avoir créé une année.</span></div></td></tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </details>

            <details class="card calendar-accordion" name="academic-calendar">
                <summary class="card-header calendar-accordion-summary">
                    <h2>Créer un semestre</h2>
                </summary>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/annees" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="saveSemester">
                        <div class="form-group">
                            <label class="form-label" for="semester-year">Année académique</label>
                            <select class="form-select" id="semester-year" name="yearId" required>
                                <option value="">Choisir une année</option>
                                <% for (AnneeAcademique year : years) { if (!"CLOTUREE".equalsIgnoreCase(year.getStatut())) { %>
                                    <option value="<%= year.getIdAnnee() %>"><%= HtmlEscaper.escape(year.getLibelle()) %></option>
                                <% } } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="semester-code-new">Code</label>
                            <input class="form-control" id="semester-code-new" name="code" maxlength="20" placeholder="S1" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="semester-label-new">Libellé</label>
                            <input class="form-control" id="semester-label-new" name="semesterLabel" maxlength="100" placeholder="Semestre 1" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="semester-start-new">Date de début</label>
                            <input class="form-control" id="semester-start-new" type="date" name="semesterStartDate" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" for="semester-end-new">Date de fin</label>
                            <input class="form-control" id="semester-end-new" type="date" name="semesterEndDate" required>
                        </div>
                        <button class="btn btn-primary" type="submit">Créer le semestre</button>
                    </form>
                </div>
            </details>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
