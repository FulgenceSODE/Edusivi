<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Changement obligatoire du mot de passe - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-wrapper">
    <main class="auth-card">
        <div class="auth-header">
            <h2>Choisissez un nouveau mot de passe</h2>
            <p>Votre mot de passe temporaire doit être remplacé avant de continuer.</p>
        </div>
        <div class="auth-body">
            <% if ("invalid".equals(request.getAttribute("error"))) { %>
                <div class="alert alert-danger">
                    Le mot de passe doit contenir entre 12 et 128 caractères et les deux saisies doivent correspondre.
                </div>
            <% } %>
            <form action="${pageContext.request.contextPath}/password-change" method="POST">
                <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.HtmlEscaper.escape((String) request.getAttribute("csrfToken")) %>">
                <div class="form-group">
                    <label class="form-label" for="newPassword">Nouveau mot de passe</label>
                    <input class="form-control" type="password" id="newPassword" name="newPassword"
                           minlength="12" maxlength="128" autocomplete="new-password" required autofocus>
                </div>
                <div class="form-group">
                    <label class="form-label" for="confirmPassword">Confirmer le nouveau mot de passe</label>
                    <input class="form-control" type="password" id="confirmPassword" name="confirmPassword"
                           minlength="12" maxlength="128" autocomplete="new-password" required>
                </div>
                <button class="btn btn-primary btn-lg w-full" type="submit">
                    Enregistrer et continuer
                </button>
            </form>
            <form action="${pageContext.request.contextPath}/logout" method="POST" class="form-spaced">
                <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.CsrfTokens.ensureToken(request) %>">
                <button class="btn btn-outline w-full" type="submit">Se déconnecter</button>
            </form>
        </div>
    </main>
</body>
</html>
