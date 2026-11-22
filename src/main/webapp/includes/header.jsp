<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String currentRole = request.isUserInRole("ADMIN") ? "ADMIN"
            : request.isUserInRole("DE") ? "DE"
            : request.isUserInRole("PROFESSEUR") ? "PROFESSEUR" : "";
    String roleLabel = "ADMIN".equals(currentRole)
            ? com.mycompany.web.UiMessages.text(request, "nav.admin")
            : "DE".equals(currentRole)
                    ? com.mycompany.web.UiMessages.text(request, "nav.de")
                    : com.mycompany.web.UiMessages.text(request, "nav.professor");
    String activeYear = (String) session.getAttribute("activeAcademicYear");
    if (activeYear == null || activeYear.isBlank()) activeYear = "Non définie";
    String theme = (String) session.getAttribute("userTheme");
    if (!"dark".equals(theme)) theme = "light";
%>
<header class="topbar">
    <div class="topbar-left">
        <nav class="topbar-context" aria-label="EduSuivi">
            <span>EduSuivi</span>
            <span class="topbar-context-separator" aria-hidden="true">/</span>
            <strong><%= com.mycompany.web.HtmlEscaper.escape(roleLabel) %></strong>
        </nav>
        <span class="badge badge-neutral topbar-active-year">
            <%= com.mycompany.web.UiMessages.text(request, "header.activeYear") %> :
            <strong><%= com.mycompany.web.HtmlEscaper.escape(activeYear) %></strong>
        </span>
    </div>
    <div class="topbar-right">
        <button type="button" class="sidebar-toggle" aria-label="<%= com.mycompany.web.UiMessages.text(request, "sidebar.open") %>" aria-expanded="false">Menu</button>
    </div>
</header>
<script>
    document.documentElement.dataset.theme = "<%= theme %>";
    document.documentElement.lang = "<%= "en".equals(session.getAttribute("userLanguage")) ? "en" : "fr" %>";
    (() => {
        document.querySelectorAll("form[data-confirm]").forEach(form => {
            form.addEventListener("submit", event => {
                const message = form.getAttribute("data-confirm");
                if (message && !window.confirm(message)) {
                    event.preventDefault();
                }
            });
        });
        document.querySelectorAll("button[data-confirm]").forEach(button => {
            button.addEventListener("click", event => {
                if (!window.confirm(button.getAttribute("data-confirm"))) {
                    event.preventDefault();
                }
            });
        });
        const toggle = document.querySelector(".sidebar-toggle");
        if (!toggle) return;
        const close = () => {
            document.body.classList.remove("sidebar-open");
            toggle.setAttribute("aria-expanded", "false");
        };
        toggle.addEventListener("click", () => {
            const opened = document.body.classList.toggle("sidebar-open");
            toggle.setAttribute("aria-expanded", String(opened));
        });
        document.addEventListener("click", event => {
            if (document.body.classList.contains("sidebar-open")
                    && !event.target.closest(".sidebar")
                    && !event.target.closest(".sidebar-toggle")) close();
        });
    })();
</script>
