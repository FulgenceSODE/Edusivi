<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,java.text.SimpleDateFormat,com.mycompany.AffectationEnseignement,com.mycompany.EvaluationCours,com.mycompany.SuiviPedagogiqueSeance,com.mycompany.web.HtmlEscaper" %>
<%
    List<AffectationEnseignement> assignments =
            (List<AffectationEnseignement>) request.getAttribute("assignments");
    List<SuiviPedagogiqueSeance> recentSessions =
            (List<SuiviPedagogiqueSeance>) request.getAttribute("recentSessions");
    List<EvaluationCours> recentEvaluations =
            (List<EvaluationCours>) request.getAttribute("recentEvaluations");
    Long sessionCount = (Long) request.getAttribute("sessionCount");
    Long evaluationCount = (Long) request.getAttribute("evaluationCount");
    Long publishedEvaluationCount = (Long) request.getAttribute("publishedEvaluationCount");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tableau de bord professeur - EduSuivi</title>
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
                    <p class="eyebrow">Espace professeur</p>
                    <h1 id="dashboard-title">Bienvenue sur EduSuivi</h1>
                    <p>Vos enseignements, séances et évaluations enregistrés en un coup d’œil.</p>
                    <div class="page-actions dashboard-hero-actions">
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/professeur/enseignements.jsp">Mes enseignements</a>
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/professeur/saisie-notes.jsp">Nouvelle évaluation</a>
                    </div>
                </div>
            </section>
            <div class="dashboard-stat-grid">
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Enseignements attribués</span>
                    <strong class="stat-value"><%= assignments.size() %></strong>
                    <span class="form-help">Affectations conservées</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Séances enregistrées</span>
                    <strong class="stat-value"><%= sessionCount %></strong>
                    <span class="form-help">Suivi pédagogique</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Évaluations créées</span>
                    <strong class="stat-value"><%= evaluationCount %></strong>
                    <span class="form-help">Toutes périodes</span>
                </article>
                <article class="stat-card dashboard-stat-card">
                    <span class="stat-label">Évaluations publiées</span>
                    <strong class="stat-value"><%= publishedEvaluationCount %></strong>
                    <span class="form-help">Résultats accessibles</span>
                </article>
            </div>
            <div class="dashboard-lower-grid dashboard-professor-grid">
                <div class="dashboard-card-column">
                    <section class="card dashboard-card">
                        <div class="card-header">
                            <div><h2>Mes enseignements</h2><p class="form-help">Affectations actives et historiques.</p></div>
                            <a class="btn btn-sm btn-outline" href="${pageContext.request.contextPath}/professeur/enseignements.jsp">Tout voir</a>
                        </div>
                        <div class="card-body">
                            <% if (assignments.isEmpty()) { %>
                                <div class="empty-state"><strong>Aucun enseignement attribué</strong><span>Contactez la direction des études.</span></div>
                            <% } else { for (AffectationEnseignement assignment : assignments.stream().limit(5).toList()) { %>
                                <div class="card-row">
                                    <strong><%= HtmlEscaper.escape(assignment.getIdEc().getCode() + " — " + assignment.getIdEc().getLibelle()) %></strong>
                                    <span><%= HtmlEscaper.escape(assignment.getIdGroupe().getCode() + " · " + assignment.getIdSemestre().getCode() + " · " + assignment.getIdAnnee().getLibelle()) %></span>
                                </div>
                            <% } } %>
                        </div>
                    </section>
                    <section class="card dashboard-card">
                        <div class="card-header">
                            <div><h2>Séances récentes</h2><p class="form-help">Dernières séances consignées.</p></div>
                            <a class="btn btn-sm btn-outline" href="${pageContext.request.contextPath}/professeur/absences.jsp">Gérer</a>
                        </div>
                        <div class="card-body">
                            <% if (recentSessions.isEmpty()) { %>
                                <div class="empty-state"><strong>Aucune séance enregistrée</strong><span>Créez une séance pour commencer le suivi.</span></div>
                            <% } else { for (SuiviPedagogiqueSeance sessionRow : recentSessions) { %>
                                <div class="card-row">
                                    <strong><%= HtmlEscaper.escape(sessionRow.getIdAffectation().getIdEc().getCode() + " — " + sessionRow.getIdAffectation().getIdGroupe().getCode()) %></strong>
                                    <span><%= dateFormat.format(sessionRow.getDateSeance()) %> · <%= HtmlEscaper.escape(sessionRow.getContenu()) %></span>
                                </div>
                            <% } } %>
                        </div>
                    </section>
                </div>
                <section class="card dashboard-card">
                    <div class="card-header">
                        <div><h2>Évaluations récentes</h2><p class="form-help">Les notes publiées peuvent être consultées depuis la saisie des notes.</p></div>
                        <a class="btn btn-sm btn-primary" href="${pageContext.request.contextPath}/professeur/saisie-notes.jsp">Gérer</a>
                    </div>
                    <div class="card-body">
                        <% if (recentEvaluations.isEmpty()) { %>
                            <div class="empty-state"><strong>Aucune évaluation créée</strong><span>Créez une évaluation depuis vos enseignements.</span></div>
                        <% } else { %>
                            <div class="table-responsive"><table class="data-table">
                                <thead><tr><th>Évaluation</th><th>Enseignement</th><th>Date</th><th>Publication</th></tr></thead>
                                <tbody>
                                    <% for (EvaluationCours evaluation : recentEvaluations) { %>
                                        <tr>
                                            <td><%= HtmlEscaper.escape(evaluation.getLibelle()) %></td>
                                            <td><%= HtmlEscaper.escape(evaluation.getIdAffectation().getIdEc().getCode() + " — " + evaluation.getIdAffectation().getIdGroupe().getCode()) %></td>
                                            <td><%= dateFormat.format(evaluation.getDateEvaluation()) %></td>
                                            <td><span class="badge <%= "PUBLIEE".equalsIgnoreCase(evaluation.getStatutPublication()) ? "badge-success" : "badge-neutral" %>"><%= HtmlEscaper.escape(evaluation.getStatutPublication()) %></span></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table></div>
                        <% } %>
                    </div>
                </section>
            </div>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
