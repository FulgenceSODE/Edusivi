<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat,java.util.List,com.mycompany.DossierEtudiant,com.mycompany.Groupe,com.mycompany.InscriptionEtudiant,com.mycompany.web.HtmlEscaper" %>
<%
    List<DossierEtudiant> students = (List<DossierEtudiant>) request.getAttribute("students");
    List<Groupe> groups = (List<Groupe>) request.getAttribute("groups");
    List<InscriptionEtudiant> enrollments = (List<InscriptionEtudiant>) request.getAttribute("enrollments");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String message = (String) request.getAttribute("flashMessage");
    if (students == null || groups == null || enrollments == null) {
        response.sendRedirect(request.getContextPath() + "/de/inscriptions");
        return;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inscriptions étudiants - EduSuivi</title>
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
                    <h1 class="page-title">Inscriptions annuelles</h1>
                    <p class="page-subtitle">L’année est déterminée par le groupe choisi et vérifiée côté serveur pour éviter les rattachements incohérents.</p>
                </div>
                <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/de/etudiants">Gérer les dossiers étudiants</a>
            </div>
            <% if (message != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(message) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">L’inscription a été enregistrée.</div>
            <% } %>
            <section class="card">
                <div class="card-header"><div><h2>Créer une inscription</h2><p class="form-help">Les groupes d’années clôturées ne sont pas proposés. La réinscription d’un même étudiant au même groupe dans la même année est bloquée.</p></div></div>
                <div class="card-body">
                    <% if (students.isEmpty() || groups.isEmpty()) { %>
                        <div class="empty-state"><strong>Création indisponible</strong><span>Il faut au moins un dossier étudiant et un groupe d’une année non clôturée.</span></div>
                    <% } else { %>
                        <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/inscriptions" method="POST">
                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                            <input type="hidden" name="action" value="create">
                            <div class="form-group">
                                <label class="form-label" for="enrollment-student">Étudiant</label>
                                <select class="form-select" id="enrollment-student" name="studentId" required>
                                    <option value="">Choisir un étudiant</option>
                                    <% for (DossierEtudiant student : students) { %>
                                        <option value="<%= student.getIdEtudiant() %>"><%= HtmlEscaper.escape(student.getMatricule() + " — " + student.getNom() + " " + student.getPrenom()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label" for="enrollment-group">Groupe et année</label>
                                <select class="form-select" id="enrollment-group" name="groupId" required>
                                    <option value="">Choisir un groupe</option>
                                    <% for (Groupe group : groups) { %>
                                        <option value="<%= group.getIdGroupe() %>"><%= HtmlEscaper.escape(group.getIdAnnee().getLibelle() + " — " + group.getIdFiliere().getCode() + " — " + group.getIdNiveau().getCode() + " — " + group.getCode() + " " + group.getLibelle()) %></option>
                                    <% } %>
                                </select>
                            </div>
                            <button class="btn btn-primary" type="submit">Créer l’inscription</button>
                        </form>
                    <% } %>
                </div>
            </section>
            <section class="card">
                <div class="card-header">
                    <div><h2>Historique des inscriptions</h2><p class="form-help">Les inscriptions ne sont jamais supprimées. Les inscriptions d’années clôturées restent consultables sans modification.</p></div>
                    <span class="badge badge-primary"><%= enrollments.size() %> inscription(s)</span>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Étudiant</th><th>Année</th><th>Filière / niveau / groupe</th><th>Date</th><th>Statut</th><th>Action</th></tr></thead>
                            <tbody>
                                <% for (InscriptionEtudiant enrollment : enrollments) { %>
                                    <tr>
                                        <td><strong><%= HtmlEscaper.escape(enrollment.getIdEtudiant().getNom() + " " + enrollment.getIdEtudiant().getPrenom()) %></strong><br><span class="text-muted"><%= HtmlEscaper.escape(enrollment.getIdEtudiant().getMatricule()) %></span></td>
                                        <td><%= HtmlEscaper.escape(enrollment.getIdAnnee().getLibelle()) %></td>
                                        <td><%= HtmlEscaper.escape(enrollment.getIdGroupe().getIdFiliere().getCode() + " / " + enrollment.getIdGroupe().getIdNiveau().getCode() + " / " + enrollment.getIdGroupe().getCode()) %></td>
                                        <td><%= dateFormat.format(enrollment.getDateInscription()) %></td>
                                        <td><span class="badge <%= "VALIDE".equalsIgnoreCase(enrollment.getStatut()) ? "badge-success" : ("SUSPENDUE".equalsIgnoreCase(enrollment.getStatut()) || "EN_ATTENTE".equalsIgnoreCase(enrollment.getStatut())) ? "badge-warning" : "badge-neutral" %>"><%= HtmlEscaper.escape(enrollment.getStatut()) %></span></td>
                                        <td>
                                            <% if (!"CLOTUREE".equalsIgnoreCase(enrollment.getIdAnnee().getStatut())
                                                    && ("VALIDE".equalsIgnoreCase(enrollment.getStatut())
                                                    || "SUSPENDUE".equalsIgnoreCase(enrollment.getStatut())
                                                    || "EN_ATTENTE".equalsIgnoreCase(enrollment.getStatut()))) { %>
                                                <form action="${pageContext.request.contextPath}/de/inscriptions" method="POST"
                                                      data-confirm="<%= "VALIDE".equalsIgnoreCase(enrollment.getStatut()) ? "Confirmer la suspension de cette inscription ?" : "" %>">
                                                    <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                                                    <input type="hidden" name="action" value="toggleStatus">
                                                    <input type="hidden" name="enrollmentId" value="<%= enrollment.getIdInscription() %>">
                                                    <button class="btn btn-sm btn-outline-primary" type="submit"><%= "EN_ATTENTE".equalsIgnoreCase(enrollment.getStatut()) ? "Valider" : "VALIDE".equalsIgnoreCase(enrollment.getStatut()) ? "Suspendre" : "Réactiver" %></button>
                                                </form>
                                            <% } else { %><span class="text-muted"><%= "CLOTUREE".equalsIgnoreCase(enrollment.getIdAnnee().getStatut()) ? "Année clôturée" : "Consultation" %></span><% } %>
                                        </td>
                                    </tr>
                                <% } %>
                                <% if (enrollments.isEmpty()) { %>
                                    <tr><td colspan="6"><div class="empty-state"><strong>Aucune inscription</strong><span>Créez une inscription après avoir enregistré un dossier et un groupe.</span></div></td></tr>
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
