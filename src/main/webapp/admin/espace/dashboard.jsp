<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Administration - EduSuivi</title>
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
                    <p class="eyebrow">Administration</p>
                    <h1 id="dashboard-title">Bienvenue sur EduSuivi</h1>
                    <p>Gestion des comptes, des rôles et des demandes d’inscription.</p>
                </div>
            </section>
            <div class="dashboard-lower-grid">
                <section class="card dashboard-card">
                    <div class="card-header">
                        <div><h2>Demandes d’inscription</h2><p class="form-help">Examinez les demandes des professeurs avant leur première connexion.</p></div>
                    </div>
                    <div class="card-body">
                        <p class="dashboard-card-description">Attribuez un matricule et approuvez les comptes selon le workflow existant.</p>
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/espace/demandes-inscription">
                            Examiner les demandes
                        </a>
                    </div>
                </section>
                <section class="card dashboard-card">
                    <div class="card-header">
                        <div><h2>Comptes utilisateurs</h2><p class="form-help">Accès réservé à l’administration.</p></div>
                    </div>
                    <div class="card-body">
                        <p class="dashboard-card-description">Gérez les comptes et leurs rôles sans modifier les règles d’accès.</p>
                        <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/espace/utilisateurs">
                            Gérer les comptes
                        </a>
                    </div>
                </section>
            </div>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
