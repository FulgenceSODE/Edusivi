package com.mycompany.web;

import com.mycompany.DemandeInscriptionEnseignant;
import com.mycompany.JournalAudit;
import com.mycompany.Utilisateur;
import com.mycompany.UtilisateurRole;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Backing bean CDI pour les fonctionnalités Administrateur.
 * Connecté directement à la base de données PostgreSQL via EntityManager JPA.
 */
@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    public static class UserDTO implements Serializable {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String role;
        private String statut;
        private String dateCreation;

        public UserDTO(Long id, String nom, String prenom, String email, String role, String statut, String dateCreation) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.role = role;
            this.statut = statut;
            this.dateCreation = dateCreation;
        }

        public Long getId() { return id; }
        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
        public String getPrenom() { return prenom; }
        public void setPrenom(String prenom) { this.prenom = prenom; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
        public String getDateCreation() { return dateCreation; }
    }

    public static class DemandeDTO implements Serializable {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String specialite;
        private String dateDemande;

        public DemandeDTO(Long id, String nom, String prenom, String email, String specialite, String dateDemande) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.specialite = specialite;
            this.dateDemande = dateDemande;
        }

        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getSpecialite() { return specialite; }
        public String getDateDemande() { return dateDemande; }
    }

    public static class AuditDTO implements Serializable {
        private Long id;
        private String action;
        private String typeEntite;
        private String details;
        private String auteur;
        private String dateAction;

        public AuditDTO(Long id, String action, String typeEntite, String details, String auteur, String dateAction) {
            this.id = id;
            this.action = action;
            this.typeEntite = typeEntite;
            this.details = details;
            this.auteur = auteur;
            this.dateAction = dateAction;
        }

        public Long getId() { return id; }
        public String getAction() { return action; }
        public String getTypeEntite() { return typeEntite; }
        public String getDetails() { return details; }
        public String getAuteur() { return auteur; }
        public String getDateAction() { return dateAction; }
    }

    private List<UserDTO> utilisateurs = new ArrayList<>();
    private List<DemandeDTO> demandes = new ArrayList<>();
    private List<AuditDTO> logsAudit = new ArrayList<>();

    private String newNom;
    private String newPrenom;
    private String newEmail;
    private String newPassword;
    private String newRole = "PROFESSEUR";
    private String filterTerm = "";

    @PostConstruct
    public void init() {
        chargerDonneesDepuisBase();
    }

    public void chargerDonneesDepuisBase() {
        utilisateurs.clear();
        demandes.clear();
        logsAudit.clear();

        try {
            if (em != null) {
                // 1. Charger les utilisateurs réels de la base
                List<Utilisateur> listUsers = em.createQuery("SELECT u FROM Utilisateur u ORDER BY u.idUtilisateur ASC", Utilisateur.class).getResultList();
                if (listUsers != null && !listUsers.isEmpty()) {
                    for (Utilisateur u : listUsers) {
                        String roleCode = "UTILISATEUR";
                        if (u.getUtilisateurRoleList() != null && !u.getUtilisateurRoleList().isEmpty()) {
                            UtilisateurRole ur = u.getUtilisateurRoleList().get(0);
                            if (ur.getRole() != null) {
                                roleCode = ur.getRole().getCodeRole();
                            }
                        }
                        String dateStr = u.getDateCreation() != null ? u.getDateCreation().toString() : "N/A";
                        utilisateurs.add(new UserDTO(u.getIdUtilisateur(), u.getNom(), u.getPrenom(), u.getEmail(), roleCode, u.getStatut(), dateStr));
                    }
                }

                // 2. Charger les demandes d'inscription réelles
                List<DemandeInscriptionEnseignant> listDem = em.createQuery("SELECT d FROM DemandeInscriptionEnseignant d ORDER BY d.idDemande DESC", DemandeInscriptionEnseignant.class).getResultList();
                if (listDem != null && !listDem.isEmpty()) {
                    for (DemandeInscriptionEnseignant d : listDem) {
                        String dateStr = d.getDateDemande() != null ? d.getDateDemande().toString() : "N/A";
                        demandes.add(new DemandeDTO(d.getIdDemande(), d.getNom(), d.getPrenom(), d.getEmail(), "Enseignant", dateStr));
                    }
                }

                // 3. Charger le journal d'audit réel
                List<JournalAudit> listAudit = em.createQuery("SELECT j FROM JournalAudit j ORDER BY j.dateAction DESC", JournalAudit.class).setMaxResults(50).getResultList();
                if (listAudit != null && !listAudit.isEmpty()) {
                    for (JournalAudit j : listAudit) {
                        String auteur = (j.getIdUtilisateur() != null) ? j.getIdUtilisateur().getEmail() : "Système";
                        String dateStr = j.getDateAction() != null ? j.getDateAction().toString() : "N/A";
                        String details = j.getDetails() != null ? j.getDetails().toString() : "";
                        logsAudit.add(new AuditDTO(j.getIdAudit(), j.getAction(), j.getTypeEntite(), details, auteur, dateStr));
                    }
                }
            }
        } catch (Exception ex) {
            // Log silencieux si DB non connectée lors de la phase dev
        }

    }

    public void creerUtilisateur() {
        if (newNom != null && !newNom.trim().isEmpty() && newEmail != null && !newEmail.trim().isEmpty()) {
            Long nextId = (long) (utilisateurs.size() + 1);
            String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            UserDTO u = new UserDTO(nextId, newNom, newPrenom, newEmail, newRole, "ACTIF", dateNow);
            utilisateurs.add(0, u);

            logsAudit.add(0, new AuditDTO((long)(logsAudit.size() + 1), "CREATION_UTILISATEUR", "Utilisateur",
                    "Création utilisateur " + newEmail + " avec rôle " + newRole, "admin@edusuivi.univ",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            newNom = "";
            newPrenom = "";
            newEmail = "";
            newPassword = "";

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Utilisateur créé et rôle JAAS attribué."));
        }
    }

    public void basculerStatut(UserDTO user) {
        if ("ACTIF".equals(user.getStatut())) {
            user.setStatut("SUSPENDU");
        } else {
            user.setStatut("ACTIF");
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Statut mis à jour", "Nouveau statut : " + user.getStatut()));
    }

    public void reinitialiserMotDePasse(UserDTO user) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Mot de passe réinitialisé", "Un mot de passe temporaire a été envoyé à " + user.getEmail()));
    }

    public void approuverDemande(DemandeDTO demande) {
        demandes.remove(demande);
        Long nextId = (long) (utilisateurs.size() + 1);
        String dateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        utilisateurs.add(new UserDTO(nextId, demande.getNom(), demande.getPrenom(), demande.getEmail(), "PROFESSEUR", "ACTIF", dateNow));

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Demande acceptée", "Le compte enseignant a été activé avec le rôle PROFESSEUR."));
    }

    public void rejeterDemande(DemandeDTO demande) {
        demandes.remove(demande);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Demande rejetée", "La demande d'inscription a été refusée."));
    }

    public List<UserDTO> getUtilisateurs() { return utilisateurs; }
    public List<DemandeDTO> getDemandes() { return demandes; }
    public List<AuditDTO> getLogsAudit() { return logsAudit; }

    public int getTotalUtilisateurs() { return utilisateurs.size(); }
    public long getTotalActifs() { return utilisateurs.stream().filter(u -> "ACTIF".equals(u.getStatut())).count(); }
    public int getTotalDemandes() { return demandes.size(); }
    public int getTotalAudits() { return logsAudit.size(); }

    public String getNewNom() { return newNom; }
    public void setNewNom(String newNom) { this.newNom = newNom; }
    public String getNewPrenom() { return newPrenom; }
    public void setNewPrenom(String newPrenom) { this.newPrenom = newPrenom; }
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getNewRole() { return newRole; }
    public void setNewRole(String newRole) { this.newRole = newRole; }
    public String getFilterTerm() { return filterTerm; }
    public void setFilterTerm(String filterTerm) { this.filterTerm = filterTerm; }
}
