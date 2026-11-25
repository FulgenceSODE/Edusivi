<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion administrateur - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-wrapper auth-image-page">
    <main class="auth-card login-card">
        <div class="auth-header">
            <h2>Administration</h2>
            <p>Accès réservé à l’admin</p>
        </div>
        <div class="auth-body">
            <% if ("disabled".equals(request.getParameter("error"))) { %>
                <div class="alert alert-danger" role="alert">Ce compte administrateur est désactivé.</div>
            <% } else if ("role".equals(request.getParameter("error"))) { %>
                <div class="alert alert-warning" role="status">Vos droits ont changé. Veuillez vous reconnecter.</div>
            <% } else if (request.getParameter("error") != null) { %>
                <div class="alert alert-danger">Connexion refusée. Vérifiez vos identifiants et votre rôle ADMIN.</div>
            <% } %>
            <form action="${pageContext.request.contextPath}/login-process" method="POST">
                <input type="hidden" name="csrfToken" value="<%= com.mycompany.web.CsrfTokens.ensureToken(request) %>">
                <input type="hidden" name="loginMode" value="ADMIN">
                <div class="form-group">
                    <input type="email" id="email" name="email" class="form-control"
                           placeholder="Adresse" aria-label="Adresse"
                           autocomplete="username" required autofocus>
                </div>
                <div class="form-group">
                    <input type="password" id="password" name="password" class="form-control"
                           placeholder="Mot de passe" aria-label="Mot de passe"
                           autocomplete="current-password" required>
                </div>
                <button type="submit" class="btn btn-primary btn-lg w-full">Se connecter</button>
                <div class="auth-form-footer">
                    <a href="${pageContext.request.contextPath}/login.jsp">Connexion</a>
                </div>
            </form>
        </div>
    </main>
</body>
</html>
