package com.mycompany.web;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.security.Principal;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String email;
    private String password;
    private String nomComplet = "Non connecté";
    private String currentRole;
    private String activeAcademicYear;
    private String activeSemester;
    private boolean loggedIn;

    public String login() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Champs requis", "Veuillez renseigner votre email et votre mot de passe."));
            return null;
        }

        try {
            request.login(email.trim(), password);
        } catch (ServletException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Échec d’authentification", "Identifiant ou mot de passe incorrect."));
            return null;
        }

        currentRole = authenticatedRole(request);
        Principal principal = request.getUserPrincipal();
        if (currentRole == null || principal == null) {
            try {
                request.logout();
            } catch (ServletException ex) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erreur système", "La session n’a pas pu être fermée."));
                return null;
            }
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Accès refusé", "Aucun rôle autorisé n’est associé à ce compte."));
            return null;
        }

        email = principal.getName();
        nomComplet = principal.getName();
        password = null;
        loggedIn = true;
        return redirectToRoleDashboard(currentRole);
    }

    private static String authenticatedRole(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "ADMIN";
        }
        if (request.isUserInRole("DE")) {
            return "DE";
        }
        if (request.isUserInRole("PROFESSEUR")) {
            return "PROFESSEUR";
        }
        return null;
    }

    public String redirectToRoleDashboard(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "/admin/espace/dashboard.jsp?faces-redirect=true";
        }
        if ("DE".equalsIgnoreCase(role)) {
            return "/de/dashboard.jsp?faces-redirect=true";
        }
        if ("PROFESSEUR".equalsIgnoreCase(role)) {
            return "/professeur/dashboard.jsp?faces-redirect=true";
        }
        return "/login.jsp?faces-redirect=true";
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            request.logout();
        } catch (ServletException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur système", "La session n’a pas pu être fermée."));
            return null;
        }
        externalContext.invalidateSession();
        loggedIn = false;
        email = null;
        password = null;
        nomComplet = "Non connecté";
        currentRole = null;
        return "/login.jsp?faces-redirect=true";
    }

    public boolean isUserInRole(String role) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return false;
        }
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request != null && request.isUserInRole(role);
    }

    public boolean isAdmin() { return isUserInRole("ADMIN"); }
    public boolean isDE() { return isUserInRole("DE"); }
    public boolean isProfesseur() { return isUserInRole("PROFESSEUR"); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String currentRole) { this.currentRole = currentRole; }
    public String getActiveAcademicYear() { return activeAcademicYear; }
    public void setActiveAcademicYear(String activeAcademicYear) { this.activeAcademicYear = activeAcademicYear; }
    public String getActiveSemester() { return activeSemester; }
    public void setActiveSemester(String activeSemester) { this.activeSemester = activeSemester; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
}
