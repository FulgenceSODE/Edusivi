<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List,com.mycompany.Departement,com.mycompany.Filiere,com.mycompany.Niveau,com.mycompany.Groupe,com.mycompany.AnneeAcademique,com.mycompany.Semestre,com.mycompany.UniteEnseignement,com.mycompany.ElementConstitutif,com.mycompany.CompositionUe,com.mycompany.web.HtmlEscaper" %>
<%
    List<Departement> departments = (List<Departement>) request.getAttribute("departments");
    List<Filiere> programs = (List<Filiere>) request.getAttribute("programs");
    List<Niveau> levels = (List<Niveau>) request.getAttribute("levels");
    List<Groupe> groups = (List<Groupe>) request.getAttribute("groups");
    List<AnneeAcademique> academicYears = (List<AnneeAcademique>) request.getAttribute("academicYears");
    List<Semestre> semesters = (List<Semestre>) request.getAttribute("semesters");
    List<UniteEnseignement> teachingUnits = (List<UniteEnseignement>) request.getAttribute("teachingUnits");
    List<ElementConstitutif> courseElements = (List<ElementConstitutif>) request.getAttribute("courseElements");
    List<CompositionUe> unitComponents = (List<CompositionUe>) request.getAttribute("unitComponents");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String message = (String) request.getAttribute("flashMessage");
    if (departments == null || programs == null || levels == null || groups == null || academicYears == null
            || semesters == null || teachingUnits == null || courseElements == null || unitComponents == null) {
        response.sendRedirect(request.getContextPath() + "/de/structure");
        return;
    }
%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Structure académique - EduSuivi</title>
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
                    <p class="eyebrow">Organisation de l’institut</p>
                    <h1 class="page-title">Structure académique</h1>
                    <p class="page-subtitle">Départements, filières, niveaux et groupes liés à leurs années académiques.</p>
                </div>
            </div>
            <% if (message != null) { %>
                <div class="alert alert-danger" role="alert" aria-live="polite"><%= HtmlEscaper.escape(message) %></div>
            <% } else if ("true".equals(request.getParameter("saved"))) { %>
                <div class="alert alert-success" role="status" aria-live="polite">La structure a été enregistrée.</div>
            <% } %>
            <div class="stats-grid">
                <div class="stat-card"><span class="stat-label">Départements</span><strong class="stat-value"><%= departments.size() %></strong></div>
                <div class="stat-card"><span class="stat-label">Filières</span><strong class="stat-value"><%= programs.size() %></strong></div>
                <div class="stat-card"><span class="stat-label">Niveaux</span><strong class="stat-value"><%= levels.size() %></strong></div>
                <div class="stat-card"><span class="stat-label">Groupes</span><strong class="stat-value"><%= groups.size() %></strong></div>
            </div>

            <section class="card">
                <div class="card-header"><div><h2>Départements</h2><p class="form-help">Les codes sont enregistrés en majuscules et doivent être uniques.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>">
                        <input type="hidden" name="action" value="createDepartment">
                        <div class="form-group"><label class="form-label" for="dept-code">Code</label><input class="form-control" id="dept-code" name="code" maxlength="30" required></div>
                        <div class="form-group"><label class="form-label" for="dept-name">Nom du département</label><input class="form-control" id="dept-name" name="name" maxlength="150" required></div>
                        <button class="btn btn-primary" type="submit">Ajouter</button>
                    </form>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Code</th><th>Nom</th><th>Enregistrer</th></tr></thead>
                            <tbody>
                                <% for (Departement department : departments) { %>
                                    <tr>
                                        <td><form id="department-<%= department.getIdDepartement() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="department-<%= department.getIdDepartement() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="department-<%= department.getIdDepartement() %>" type="hidden" name="action" value="updateDepartment"><input form="department-<%= department.getIdDepartement() %>" type="hidden" name="entityId" value="<%= department.getIdDepartement() %>"><input form="department-<%= department.getIdDepartement() %>" class="form-control" aria-label="Code département" name="code" maxlength="30" required value="<%= HtmlEscaper.escape(department.getCode()) %>"></td>
                                        <td><input form="department-<%= department.getIdDepartement() %>" class="form-control" aria-label="Nom département" name="name" maxlength="150" required value="<%= HtmlEscaper.escape(department.getNom()) %>"></td>
                                        <td><button form="department-<%= department.getIdDepartement() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                    </tr>
                                <% } %>
                                <% if (departments.isEmpty()) { %><tr><td colspan="3"><div class="empty-state"><strong>Aucun département</strong><span>Ajoutez le premier département de l’institut.</span></div></td></tr><% } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Filières</h2><p class="form-help">Le rattachement à un département est facultatif.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="createProgram">
                        <div class="form-group"><label class="form-label" for="program-code">Code</label><input class="form-control" id="program-code" name="code" maxlength="30" required></div>
                        <div class="form-group"><label class="form-label" for="program-name">Nom</label><input class="form-control" id="program-name" name="name" maxlength="150" required></div>
                        <div class="form-group"><label class="form-label" for="program-label">Libellé (facultatif)</label><input class="form-control" id="program-label" name="label" maxlength="150"></div>
                        <div class="form-group"><label class="form-label" for="program-department">Département</label><select class="form-select" id="program-department" name="departmentId"><option value="">Aucun</option><% for (Departement department : departments) { %><option value="<%= department.getIdDepartement() %>"><%= HtmlEscaper.escape(department.getCode() + " — " + department.getNom()) %></option><% } %></select></div>
                        <button class="btn btn-primary" type="submit">Ajouter</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>Code</th><th>Nom / libellé</th><th>Département</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (Filiere program : programs) { %>
                                <tr>
                                    <td><form id="program-<%= program.getIdFiliere() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="program-<%= program.getIdFiliere() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="program-<%= program.getIdFiliere() %>" type="hidden" name="action" value="updateProgram"><input form="program-<%= program.getIdFiliere() %>" type="hidden" name="entityId" value="<%= program.getIdFiliere() %>"><input form="program-<%= program.getIdFiliere() %>" class="form-control" aria-label="Code filière" name="code" maxlength="30" required value="<%= HtmlEscaper.escape(program.getCode()) %>"></td>
                                    <td><input form="program-<%= program.getIdFiliere() %>" class="form-control" aria-label="Nom filière" name="name" maxlength="150" required value="<%= HtmlEscaper.escape(program.getNom()) %>"><input form="program-<%= program.getIdFiliere() %>" class="form-control" aria-label="Libellé filière" name="label" maxlength="150" value="<%= HtmlEscaper.escape(program.getLibelle()) %>"></td>
                                    <td><select form="program-<%= program.getIdFiliere() %>" class="form-select" aria-label="Département" name="departmentId"><option value="">Aucun</option><% for (Departement department : departments) { %><option value="<%= department.getIdDepartement() %>" <%= program.getIdDepartement() != null && department.getIdDepartement().equals(program.getIdDepartement().getIdDepartement()) ? "selected" : "" %>><%= HtmlEscaper.escape(department.getCode()) %></option><% } %></select></td>
                                    <td><button form="program-<%= program.getIdFiliere() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (programs.isEmpty()) { %><tr><td colspan="4"><div class="empty-state"><strong>Aucune filière</strong><span>Créez une filière avant ses niveaux.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Niveaux</h2><p class="form-help">Chaque niveau est rattaché à une filière.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="createLevel">
                        <div class="form-group"><label class="form-label" for="level-code">Code</label><input class="form-control" id="level-code" name="code" maxlength="30" required></div>
                        <div class="form-group"><label class="form-label" for="level-label">Libellé</label><input class="form-control" id="level-label" name="label" maxlength="100" required></div>
                        <div class="form-group"><label class="form-label" for="level-program">Filière</label><select class="form-select" id="level-program" name="programId" required><option value="">Choisir</option><% for (Filiere program : programs) { %><option value="<%= program.getIdFiliere() %>"><%= HtmlEscaper.escape(program.getCode() + " — " + program.getNom()) %></option><% } %></select></div>
                        <button class="btn btn-primary" type="submit">Ajouter</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>Code</th><th>Libellé</th><th>Filière</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (Niveau level : levels) { %>
                                <tr>
                                    <td><form id="level-<%= level.getIdNiveau() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="level-<%= level.getIdNiveau() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="level-<%= level.getIdNiveau() %>" type="hidden" name="action" value="updateLevel"><input form="level-<%= level.getIdNiveau() %>" type="hidden" name="entityId" value="<%= level.getIdNiveau() %>"><input form="level-<%= level.getIdNiveau() %>" class="form-control" aria-label="Code niveau" name="code" maxlength="30" required value="<%= HtmlEscaper.escape(level.getCode()) %>"></td>
                                    <td><input form="level-<%= level.getIdNiveau() %>" class="form-control" aria-label="Libellé niveau" name="label" maxlength="100" required value="<%= HtmlEscaper.escape(level.getLibelle()) %>"></td>
                                    <td><select form="level-<%= level.getIdNiveau() %>" class="form-select" aria-label="Filière du niveau" name="programId" required><option value="">Choisir</option><% for (Filiere program : programs) { %><option value="<%= program.getIdFiliere() %>" <%= level.getIdFiliere() != null && level.getIdFiliere().getIdFiliere().equals(program.getIdFiliere()) ? "selected" : "" %>><%= HtmlEscaper.escape(program.getCode() + " — " + program.getNom()) %></option><% } %></select></td>
                                    <td><button form="level-<%= level.getIdNiveau() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (levels.isEmpty()) { %><tr><td colspan="4"><div class="empty-state"><strong>Aucun niveau</strong><span>Ajoutez un niveau après avoir créé une filière.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Groupes</h2><p class="form-help">Chaque groupe est lié à une année, une filière et un niveau cohérents. Les groupes ne sont pas supprimés pour préserver l’historique.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="createGroup">
                        <div class="form-group"><label class="form-label" for="group-code">Code</label><input class="form-control" id="group-code" name="code" maxlength="40" required></div>
                        <div class="form-group"><label class="form-label" for="group-label">Libellé</label><input class="form-control" id="group-label" name="label" maxlength="120" required></div>
                        <div class="form-group"><label class="form-label" for="group-year">Année académique</label><select class="form-select" id="group-year" name="yearId" required><option value="">Choisir</option><% for (AnneeAcademique year : academicYears) { if (!"CLOTUREE".equalsIgnoreCase(year.getStatut())) { %><option value="<%= year.getIdAnnee() %>"><%= HtmlEscaper.escape(year.getLibelle()) %></option><% } } %></select></div>
                        <div class="form-group"><label class="form-label" for="group-program">Filière</label><select class="form-select" id="group-program" name="programId" required><option value="">Choisir</option><% for (Filiere program : programs) { %><option value="<%= program.getIdFiliere() %>"><%= HtmlEscaper.escape(program.getCode() + " — " + program.getNom()) %></option><% } %></select></div>
                        <div class="form-group"><label class="form-label" for="group-level">Niveau</label><select class="form-select" id="group-level" name="levelId" required><option value="">Choisir</option><% for (Niveau level : levels) { if (level.getIdFiliere() != null) { %><option value="<%= level.getIdNiveau() %>"><%= HtmlEscaper.escape(level.getCode() + " — " + level.getLibelle() + " (" + level.getIdFiliere().getCode() + ")") %></option><% } } %></select></div>
                        <button class="btn btn-primary" type="submit">Ajouter</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>Code</th><th>Libellé</th><th>Année</th><th>Filière</th><th>Niveau</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (Groupe group : groups) { %>
                                <tr>
                                    <td><form id="group-<%= group.getIdGroupe() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="action" value="updateGroup"><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="entityId" value="<%= group.getIdGroupe() %>"><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="yearId" value="<%= group.getIdAnnee().getIdAnnee() %>"><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="programId" value="<%= group.getIdFiliere().getIdFiliere() %>"><input form="group-<%= group.getIdGroupe() %>" type="hidden" name="levelId" value="<%= group.getIdNiveau().getIdNiveau() %>"><input form="group-<%= group.getIdGroupe() %>" class="form-control" aria-label="Code groupe" name="code" maxlength="40" required value="<%= HtmlEscaper.escape(group.getCode()) %>"></td>
                                    <td><input form="group-<%= group.getIdGroupe() %>" class="form-control" aria-label="Libellé groupe" name="label" maxlength="120" required value="<%= HtmlEscaper.escape(group.getLibelle()) %>"></td>
                                    <td><%= HtmlEscaper.escape(group.getIdAnnee().getLibelle()) %></td><td><%= HtmlEscaper.escape(group.getIdFiliere().getCode()) %></td><td><%= HtmlEscaper.escape(group.getIdNiveau().getCode()) %></td>
                                    <td><button form="group-<%= group.getIdGroupe() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (groups.isEmpty()) { %><tr><td colspan="6"><div class="empty-state"><strong>Aucun groupe</strong><span>Créez un groupe pour l’année et le parcours concernés.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Unités d’enseignement (UE)</h2><p class="form-help">Une UE est rattachée à un semestre d’une année académique.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="createUnit">
                        <div class="form-group"><label class="form-label" for="unit-code">Code</label><input class="form-control" id="unit-code" name="code" maxlength="30" required></div>
                        <div class="form-group"><label class="form-label" for="unit-label">Libellé</label><input class="form-control" id="unit-label" name="label" maxlength="150" required></div>
                        <div class="form-group"><label class="form-label" for="unit-semester">Semestre</label><select class="form-select" id="unit-semester" name="semesterId" required><option value="">Choisir</option><% for (Semestre semester : semesters) { if (!"CLOTUREE".equalsIgnoreCase(semester.getIdAnnee().getStatut())) { %><option value="<%= semester.getIdSemestre() %>"><%= HtmlEscaper.escape(semester.getCode() + " — " + semester.getIdAnnee().getLibelle()) %></option><% } } %></select></div>
                        <div class="form-group"><label class="form-label" for="unit-description">Description (facultative)</label><input class="form-control" id="unit-description" name="description" maxlength="2000"></div>
                        <button class="btn btn-primary" type="submit">Ajouter l’UE</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>Code</th><th>Libellé</th><th>Semestre</th><th>Description</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (UniteEnseignement unit : teachingUnits) { %>
                                <tr>
                                    <td><form id="unit-<%= unit.getIdUe() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="unit-<%= unit.getIdUe() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="unit-<%= unit.getIdUe() %>" type="hidden" name="action" value="updateUnit"><input form="unit-<%= unit.getIdUe() %>" type="hidden" name="entityId" value="<%= unit.getIdUe() %>"><input form="unit-<%= unit.getIdUe() %>" class="form-control" aria-label="Code UE" name="code" maxlength="30" required value="<%= HtmlEscaper.escape(unit.getCode()) %>"></td>
                                    <td><input form="unit-<%= unit.getIdUe() %>" class="form-control" aria-label="Libellé UE" name="label" maxlength="150" required value="<%= HtmlEscaper.escape(unit.getLibelle()) %>"></td>
                                    <td><select form="unit-<%= unit.getIdUe() %>" class="form-select" aria-label="Semestre UE" name="semesterId" required><option value="">Choisir</option><% for (Semestre semester : semesters) { if (!"CLOTUREE".equalsIgnoreCase(semester.getIdAnnee().getStatut()) || (unit.getIdSemestre() != null && semester.getIdSemestre().equals(unit.getIdSemestre().getIdSemestre()))) { %><option value="<%= semester.getIdSemestre() %>" <%= unit.getIdSemestre() != null && semester.getIdSemestre().equals(unit.getIdSemestre().getIdSemestre()) ? "selected" : "" %>><%= HtmlEscaper.escape(semester.getCode() + " — " + semester.getIdAnnee().getLibelle()) %></option><% } } %></select></td>
                                    <td><input form="unit-<%= unit.getIdUe() %>" class="form-control" aria-label="Description UE" name="description" maxlength="2000" value="<%= HtmlEscaper.escape(unit.getDescription()) %>"></td>
                                    <td><button form="unit-<%= unit.getIdUe() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (teachingUnits.isEmpty()) { %><tr><td colspan="5"><div class="empty-state"><strong>Aucune UE</strong><span>Ajoutez une UE à un semestre non clôturé.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Éléments constitutifs (EC)</h2><p class="form-help">Un EC peut être partagé par plusieurs UE; son coefficient est géré dans chaque association UE–EC.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="createElement">
                        <div class="form-group"><label class="form-label" for="element-code">Code</label><input class="form-control" id="element-code" name="code" maxlength="30" required></div>
                        <div class="form-group"><label class="form-label" for="element-label">Libellé</label><input class="form-control" id="element-label" name="label" maxlength="150" required></div>
                        <div class="form-group"><label class="form-label" for="element-description">Description (facultative)</label><input class="form-control" id="element-description" name="description" maxlength="2000"></div>
                        <button class="btn btn-primary" type="submit">Ajouter l’EC</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>Code</th><th>Libellé</th><th>Description</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (ElementConstitutif element : courseElements) { %>
                                <tr>
                                    <td><form id="element-<%= element.getIdEc() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="element-<%= element.getIdEc() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="element-<%= element.getIdEc() %>" type="hidden" name="action" value="updateElement"><input form="element-<%= element.getIdEc() %>" type="hidden" name="entityId" value="<%= element.getIdEc() %>"><input form="element-<%= element.getIdEc() %>" class="form-control" aria-label="Code EC" name="code" maxlength="30" required value="<%= HtmlEscaper.escape(element.getCode()) %>"></td>
                                    <td><input form="element-<%= element.getIdEc() %>" class="form-control" aria-label="Libellé EC" name="label" maxlength="150" required value="<%= HtmlEscaper.escape(element.getLibelle()) %>"></td>
                                    <td><input form="element-<%= element.getIdEc() %>" class="form-control" aria-label="Description EC" name="description" maxlength="2000" value="<%= HtmlEscaper.escape(element.getDescription()) %>"></td>
                                    <td><button form="element-<%= element.getIdEc() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (courseElements.isEmpty()) { %><tr><td colspan="4"><div class="empty-state"><strong>Aucun EC</strong><span>Ajoutez les éléments constitutifs avant de les associer aux UE.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>

            <section class="card">
                <div class="card-header"><div><h2>Composition des UE et coefficients</h2><p class="form-help">Les coefficients sont strictement positifs, portés par l’association et conservés en précision décimale.</p></div></div>
                <div class="card-body">
                    <form class="form-row calendar-create" action="${pageContext.request.contextPath}/de/structure" method="POST">
                        <input type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input type="hidden" name="action" value="saveUnitComponent">
                        <div class="form-group"><label class="form-label" for="component-unit">UE</label><select class="form-select" id="component-unit" name="unitId" required><option value="">Choisir</option><% for (UniteEnseignement unit : teachingUnits) { %><option value="<%= unit.getIdUe() %>"><%= HtmlEscaper.escape(unit.getCode() + " — " + unit.getLibelle()) %></option><% } %></select></div>
                        <div class="form-group"><label class="form-label" for="component-element">EC</label><select class="form-select" id="component-element" name="elementId" required><option value="">Choisir</option><% for (ElementConstitutif element : courseElements) { %><option value="<%= element.getIdEc() %>"><%= HtmlEscaper.escape(element.getCode() + " — " + element.getLibelle()) %></option><% } %></select></div>
                        <div class="form-group"><label class="form-label" for="component-coefficient">Coefficient</label><input class="form-control" id="component-coefficient" name="coefficient" inputmode="decimal" maxlength="32" placeholder="1.5" required></div>
                        <div class="form-group"><label class="form-label" for="component-order">Ordre (facultatif)</label><input class="form-control" id="component-order" name="order" type="number" min="1" step="1"></div>
                        <button class="btn btn-primary" type="submit">Ajouter / mettre à jour</button>
                    </form>
                    <div class="table-responsive"><table class="data-table">
                        <thead><tr><th>UE</th><th>EC</th><th>Coefficient</th><th>Ordre</th><th>Enregistrer</th></tr></thead><tbody>
                            <% for (CompositionUe component : unitComponents) { %>
                                <tr>
                                    <td><form id="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" action="${pageContext.request.contextPath}/de/structure" method="POST"></form><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" type="hidden" name="csrfToken" value="<%= HtmlEscaper.escape(csrfToken) %>"><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" type="hidden" name="action" value="saveUnitComponent"><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" type="hidden" name="unitId" value="<%= component.getUniteEnseignement().getIdUe() %>"><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" type="hidden" name="elementId" value="<%= component.getElementConstitutif().getIdEc() %>"><%= HtmlEscaper.escape(component.getUniteEnseignement().getCode() + " — " + component.getUniteEnseignement().getLibelle()) %></td>
                                    <td><%= HtmlEscaper.escape(component.getElementConstitutif().getCode() + " — " + component.getElementConstitutif().getLibelle()) %></td>
                                    <td><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" class="form-control" aria-label="Coefficient" name="coefficient" inputmode="decimal" maxlength="32" required value="<%= component.getCoefficient().toPlainString() %>"></td>
                                    <td><input form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" class="form-control" aria-label="Ordre" name="order" type="number" min="1" step="1" value="<%= component.getOrdre() == null ? "" : component.getOrdre() %>"></td>
                                    <td><button form="component-<%= component.getCompositionUePK().getIdUe() %>-<%= component.getCompositionUePK().getIdEc() %>" class="btn btn-sm btn-outline-primary" type="submit">Enregistrer</button></td>
                                </tr>
                            <% } %>
                            <% if (unitComponents.isEmpty()) { %><tr><td colspan="5"><div class="empty-state"><strong>Aucune composition</strong><span>Associez un EC à une UE en définissant son coefficient.</span></div></td></tr><% } %>
                        </tbody>
                    </table></div>
                </div>
            </section>
        </main>
        <jsp:include page="/includes/footer.jsp"/>
    </div>
</div>
</body>
</html>
