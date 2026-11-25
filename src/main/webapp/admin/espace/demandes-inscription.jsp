<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.text.SimpleDateFormat,java.util.List,com.mycompany.DemandeInscriptionEnseignant,com.mycompany.web.HtmlEscaper" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Demande professeur - EduSuivi</title>
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
                    <h1 class="page-title">Demande d’inscription professeur</h1>
                    <p class="page-subtitle">Validez les demandes et attribuez un matricule professionnel unique.</p>
                </div>
            </div>

            <% if (request.getParameter("success") != null) { %>
                <div class="alert alert-success">La décision a été enregistrée.</div>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
                <div class="alert alert-danger">
                    <% if ("matricule".equals(request.getParameter("error"))) { %>
                        Saisissez un matricule professionnel valide.
                    <% } else { %>
                        L’adresse ou le matricule est déjà utilisé.
                    <% } %>
                </div>
            <% } %>

            <section class="card">
                <div class="card-header"><h3>Demandes en attente</h3></div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Nom</th>
                                    <th>Email</th>
                                    <th>Spécialité</th>
                                    <th>Date</th>
                                    <th>Décision</th>
                                </tr>
                            </thead>
                            <tbody>
                            <%
                                List<DemandeInscriptionEnseignant> demandes =
                                        (List<DemandeInscriptionEnseignant>) request.getAttribute("demandes");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                                if (demandes != null && !demandes.isEmpty()) {
                                    for (DemandeInscriptionEnseignant demande : demandes) {
                            %>
                                <tr>
                                    <td><%= HtmlEscaper.escape(demande.getNom()) %> <%= HtmlEscaper.escape(demande.getPrenom()) %></td>
                                    <td><%= HtmlEscaper.escape(demande.getEmail()) %></td>
                                    <td><%= HtmlEscaper.escape(demande.getSpecialite()) %></td>
                                    <td><%= demande.getDateDemande() == null ? "" : dateFormat.format(demande.getDateDemande()) %></td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/admin/espace/demandes-inscription" method="POST" class="approval-form">
                                            <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape((String) request.getAttribute("csrfToken")) %>">
                                            <input type="hidden" name="id" value="<%= demande.getIdDemande() %>">
                                            <label class="sr-only" for="matricule-<%= demande.getIdDemande() %>">Matricule de <%= HtmlEscaper.escape(demande.getNom()) %></label>
                                            <input class="form-control" id="matricule-<%= demande.getIdDemande() %>" name="matricule" maxlength="50" placeholder="Matricule" required>
                                            <button class="btn btn-success btn-sm" type="submit" name="action" value="approve">Approuver</button>
                                            <button class="btn btn-danger btn-sm" type="submit" name="action" value="reject" formnovalidate
                                                    data-confirm="Confirmer le rejet définitif de cette demande ?">Rejeter</button>
                                        </form>
                                    </td>
                                </tr>
                            <%
                                    }
                                } else {
                            %>
                                <tr><td colspan="5">Aucune demande en attente.</td></tr>
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
