<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Accès Refusé (403) - EduSuivi</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-wrapper">
    <div class="auth-card auth-card--center">
        <div class="auth-header auth-header-danger">
            <h2>Accès Non Autorisé (403)</h2>
            <p>Sécurité JAAS du Conteneur</p>
        </div>
        <div class="auth-body">
            <p class="form-help">
                Vous n'avez pas les droits nécessaires pour accéder à cet espace avec votre rôle actuel.
            </p>
            <div class="auth-actions form-spaced">
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary">
                    Retour à la connexion
                </a>
            </div>
        </div>
    </div>
</body>
</html>
