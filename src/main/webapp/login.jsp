<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduSuivi Universitaire</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-wrapper auth-image-page">

    <main class="auth-card login-card">
        <div class="auth-header">
            <p class="auth-signature">EduSuivi · Piloter. Suivre. Évaluer.</p>
            <h2>Connexion</h2>
            <p>Veuillez vous identifier pour accéder à votre espace</p>
        </div>

        <div class="auth-body">
            <% if ("disabled".equals(request.getParameter("error"))) { %>
                <div class="alert alert-danger" role="alert">
                    <span>Ce compte est désactivé. Contactez l’administrateur de l’institut.</span>
                </div>
            <% } else if ("role".equals(request.getParameter("error"))) { %>
                <div class="alert alert-warning" role="status">
                    <span>Vos droits ont été modifiés. Veuillez vous reconnecter.</span>
                </div>
            <% } else if (request.getParameter("error") != null) { %>
                <div class="alert alert-danger">
                    <span>Échec de connexion. Vérifiez vos identifiants et le statut du compte.</span>
                </div>
            <% } %>

          

            <form action="${pageContext.request.contextPath}/login-process" method="POST">
                <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.CsrfTokens.ensureToken(request) %>">
                <input type="hidden" name="loginMode" value="USER">
                <div class="form-group">
                    <label class="form-label" for="email">Adresse électronique</label>
                    <input type="email" id="email" name="email" class="form-control" placeholder="nomprenom@gmail.com" autocomplete="username" required autofocus>
                </div>

                <div class="form-group">
                    <label class="form-label" for="password">Mot de passe</label>
                    <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" autocomplete="current-password" required>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-full">
                    Se connecter
                </button>

                <div class="auth-form-footer">
                    <a class="auth-footer-link" href="${pageContext.request.contextPath}/register.jsp">
                        Faire demande
                    </a>
                </div>
            </form>
        </div>
    </main>

</body>
</html>
