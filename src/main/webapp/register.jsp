<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Demande d'inscription Enseignant - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-wrapper auth-image-page">

    <main class="auth-card auth-card--wide registration-card">
        <div class="auth-header">
            <h2>Demande d’inscription</h2>
            <p>Professeur · Création d’un compte EduSuivi</p>
        </div>

        <div class="auth-body">
            <% if ("true".equals(request.getParameter("submitted"))) { %>
                <div class="alert alert-success">
                    Votre demande a été transmise à l’admin. Patientez 48h.
                </div>
            <% } %>
            <% if (request.getParameter("error") != null) { %>
                <div class="alert alert-danger">
                    <% if ("email".equals(request.getParameter("error"))) { %>
                        Cette adresse est déjà utilisée ou fait l’objet d’une demande en attente.
                    <% } else { %>
                        Vérifiez les champs. Le mot de passe doit comporter au moins 12 caractères et les deux saisies doivent correspondre.
                    <% } %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/register-process" method="POST">
                <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.CsrfTokens.ensureToken(request) %>">
                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="nom">Nom</label>
                        <input type="text" id="nom" name="nom" class="form-control" placeholder="Votre nom" autocomplete="family-name" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="prenom">Prénom</label>
                        <input type="text" id="prenom" name="prenom" class="form-control" placeholder="Votre prénom" autocomplete="given-name" required>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label" for="email">Adresse électronique</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="professeur@universite.ci" autocomplete="email" required>
                </div>

                <div class="form-group">
                    <label class="form-label" for="specialite">Discipline / spécialité</label>
                    <input type="text" id="specialite" name="specialite" class="form-control" placeholder="Ex. : Génie logiciel" autocomplete="organization-title" required>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label class="form-label" for="password">Mot de passe</label>
                        <input type="password" id="password" name="password" class="form-control" minlength="12" maxlength="128" autocomplete="new-password" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label" for="confirm_password">Confirmer le mot de passe</label>
                        <input type="password" id="confirm_password" name="confirm_password" class="form-control" minlength="12" maxlength="128" autocomplete="new-password" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-full form-spaced">
                    Faire demande
                </button>

                <div class="auth-form-footer">
                    <a class="auth-footer-link" href="${pageContext.request.contextPath}/login.jsp">
                        Se connecter
                    </a>
                </div>
            </form>
        </div>
    </main>

</body>
</html>
