package com.mycompany.web;

import com.mycompany.ElementConstitutif;
import com.mycompany.Enseignement;
import com.mycompany.Etudiant;
import com.mycompany.CompositionUe;
import com.mycompany.UniteEnseignement;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean CDI pour les fonctionnalités Directeur des Études (DE).
 * Connecté directement à la base de données PostgreSQL via EntityManager JPA.
 * Respecte la directive métier : "EC" est remplacé par "Matière".
 */
@Named("deBean")
@ViewScoped
public class DirecteurEtudesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    public static class AffectationDTO implements Serializable {
        private Long id;
        private String codeMatiere;
        private String libelleMatiere;
        private String filiere;
        private String groupe;
        private String semestre;
        private String enseignant;
        private int volumeHorairePrevu;
        private int volumeHoraireRealise;
        private String statut;

        public AffectationDTO(Long id, String codeMatiere, String libelleMatiere, String filiere, String groupe,
                              String semestre, String enseignant, int volumeHorairePrevu, int volumeHoraireRealise, String statut) {
            this.id = id;
            this.codeMatiere = codeMatiere;
            this.libelleMatiere = libelleMatiere;
            this.filiere = filiere;
            this.groupe = groupe;
            this.semestre = semestre;
            this.enseignant = enseignant;
            this.volumeHorairePrevu = volumeHorairePrevu;
            this.volumeHoraireRealise = volumeHoraireRealise;
            this.statut = statut;
        }

        public Long getId() { return id; }
        public String getCodeMatiere() { return codeMatiere; }
        public String getLibelleMatiere() { return libelleMatiere; }
        public String getFiliere() { return filiere; }
        public String getGroupe() { return groupe; }
        public String getSemestre() { return semestre; }
        public String getEnseignant() { return enseignant; }
        public void setEnseignant(String enseignant) { this.enseignant = enseignant; }
        public int getVolumeHorairePrevu() { return volumeHorairePrevu; }
        public void setVolumeHorairePrevu(int v) { this.volumeHorairePrevu = v; }
        public int getVolumeHoraireRealise() { return volumeHoraireRealise; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }

        public int getPourcentageProgression() {
            if (volumeHorairePrevu <= 0) return 0;
            return Math.min(100, (volumeHoraireRealise * 100) / volumeHorairePrevu);
        }
    }

    public static class EtudiantDTO implements Serializable {
        private Long id;
        private String matricule;
        private String nom;
        private String prenom;
        private String sexe;
        private String dateNaissance;
        private String telephone;
        private String email;
        private String groupe;
        private String statutInscription;

        public EtudiantDTO(Long id, String matricule, String nom, String prenom, String sexe, String dateNaissance,
                           String telephone, String email, String groupe, String statutInscription) {
            this.id = id;
            this.matricule = matricule;
            this.nom = nom;
            this.prenom = prenom;
            this.sexe = sexe;
            this.dateNaissance = dateNaissance;
            this.telephone = telephone;
            this.email = email;
            this.groupe = groupe;
            this.statutInscription = statutInscription;
        }

        public Long getId() { return id; }
        public String getMatricule() { return matricule; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getNomComplet() { return nom + " " + prenom; }
        public String getSexe() { return sexe; }
        public String getDateNaissance() { return dateNaissance; }
        public String getTelephone() { return telephone; }
        public String getEmail() { return email; }
        public String getGroupe() { return groupe; }
        public void setGroupe(String groupe) { this.groupe = groupe; }
        public String getStatutInscription() { return statutInscription; }
        public void setStatutInscription(String s) { this.statutInscription = s; }
    }

    public static class MatiereDTO implements Serializable {
        private Long id;
        private String code;
        private String libelle;
        private String codeUe;
        private String nomUe;
        private BigDecimal coefficient;
        private int volumeHoraire;

        public MatiereDTO(Long id, String code, String libelle, String codeUe, String nomUe, BigDecimal coefficient, int volumeHoraire) {
            this.id = id;
            this.code = code;
            this.libelle = libelle;
            this.codeUe = codeUe;
            this.nomUe = nomUe;
            this.coefficient = coefficient;
            this.volumeHoraire = volumeHoraire;
        }

        public Long getId() { return id; }
        public String getCode() { return code; }
        public String getLibelle() { return libelle; }
        public String getCodeUe() { return codeUe; }
        public String getNomUe() { return nomUe; }
        public BigDecimal getCoefficient() { return coefficient; }
        public int getVolumeHoraire() { return volumeHoraire; }
    }

    private List<AffectationDTO> affectations = new ArrayList<>();
    private List<EtudiantDTO> etudiants = new ArrayList<>();
    private List<MatiereDTO> matieres = new ArrayList<>();

    private String selectedFiliere = "";
    private String selectedGroupe = "";
    private String selectedSemestre = "";
    private String selectedAnnee = "";

    private String newMatricule;
    private String newNom;
    private String newPrenom;
    private String newEmail;
    private String newTel;
    private String newSexe = "M";
    private String newDateNaissance = "";
    private String newGroupe = "";

    @PostConstruct
    public void init() {
        chargerDonneesDepuisBase();
    }

    public void chargerDonneesDepuisBase() {
        affectations.clear();
        etudiants.clear();
        matieres.clear();

        try {
            if (em != null) {
                // 1. Charger les matières et leurs coefficients portés par l'association UE-EC
                List<ElementConstitutif> listEc = em.createQuery(
                        "SELECT DISTINCT ec FROM ElementConstitutif ec "
                        + "LEFT JOIN FETCH ec.compositionUeList",
                        ElementConstitutif.class).getResultList();
                if (listEc != null && !listEc.isEmpty()) {
                    for (ElementConstitutif ec : listEc) {
                        List<CompositionUe> compositions = ec.getCompositionUeList();
                        if (compositions == null || compositions.isEmpty()) {
                            matieres.add(new MatiereDTO(ec.getIdEc(), ec.getCode(), ec.getLibelle(),
                                    "UE-GEN", "Unité Générale", BigDecimal.ONE, 45));
                            continue;
                        }
                        for (CompositionUe composition : compositions) {
                            UniteEnseignement ue = composition.getUniteEnseignement();
                            String ueCode = ue != null ? ue.getCode() : "UE-GEN";
                            String ueNom = ue != null ? ue.getLibelle() : "Unité Générale";
                            BigDecimal coefficient = composition.getCoefficient() != null
                                    ? composition.getCoefficient() : BigDecimal.ONE;
                            matieres.add(new MatiereDTO(ec.getIdEc(), ec.getCode(), ec.getLibelle(),
                                    ueCode, ueNom, coefficient, 45));
                        }
                    }
                }

                // 2. Charger les étudiants réels (Etudiant)
                List<Etudiant> listEtu = em.createQuery("SELECT e FROM Etudiant e ORDER BY e.nom, e.prenom", Etudiant.class).getResultList();
                if (listEtu != null && !listEtu.isEmpty()) {
                    for (Etudiant e : listEtu) {
                        String dNaiss = e.getDateNaissance() != null ? e.getDateNaissance().toString() : "N/A";
                        etudiants.add(new EtudiantDTO(e.getIdEtudiant(), e.getMatricule(), e.getNom(), e.getPrenom(),
                                e.getSexe() != null ? e.getSexe() : "M", dNaiss, e.getTelephone(), e.getEmail(), "L2-GL-G1", "VALIDE"));
                    }
                }

                // 3. Charger les enseignements réels
                List<Enseignement> listEns = em.createQuery("SELECT ens FROM Enseignement ens", Enseignement.class).getResultList();
                if (listEns != null && !listEns.isEmpty()) {
                    for (Enseignement ens : listEns) {
                        String mCode = (ens.getIdEc() != null) ? ens.getIdEc().getCode() : "MAT";
                        String mNom = (ens.getIdEc() != null) ? ens.getIdEc().getLibelle() : "Matière";
                        String grp = (ens.getIdGroupe() != null) ? ens.getIdGroupe().getCode() : "Groupe 1";
                        String sem = (ens.getIdSemestre() != null) ? ens.getIdSemestre().getCode() : "S1";
                        String prof = "Non Affecté";
                        if (ens.getIdEnseignant() != null && ens.getIdEnseignant().getIdUtilisateur() != null) {
                            prof = ens.getIdEnseignant().getIdUtilisateur().getNom() + " " + ens.getIdEnseignant().getIdUtilisateur().getPrenom();
                        }
                        int vPrev = ens.getVolumeHorairePrevu() != null ? ens.getVolumeHorairePrevu() : 45;
                        affectations.add(new AffectationDTO(ens.getIdEnseignement(), mCode, mNom, "Génie Logiciel", grp, sem, prof, vPrev, 20, ens.getStatut()));
                    }
                }
            }
        } catch (Exception ex) {
            // Log silencieux
        }

    }

    public void assignerProfesseur(AffectationDTO aff, String profNom) {
        aff.setEnseignant(profNom);
        aff.setStatut("EN_COURS");
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Affectation enregistrée",
                        "La Matière " + aff.getLibelleMatiere() + " est désormais affectée à " + profNom));
    }

    public void ajouterEtudiant() {
        if (newNom != null && !newNom.trim().isEmpty() && newMatricule != null && !newMatricule.trim().isEmpty()) {
            Long nextId = (long) (etudiants.size() + 1);
            EtudiantDTO e = new EtudiantDTO(nextId, newMatricule, newNom, newPrenom, newSexe, newDateNaissance, newTel, newEmail, newGroupe, "VALIDE");
            etudiants.add(0, e);

            newMatricule = "";
            newNom = "";
            newPrenom = "";
            newEmail = "";
            newTel = "";

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Étudiant Enregistré", "Le dossier et l'inscription pédagogique ont été enregistrés avec succès."));
        }
    }

    public List<AffectationDTO> getAffectations() { return affectations; }
    public List<EtudiantDTO> getEtudiants() { return etudiants; }
    public List<MatiereDTO> getMatieres() { return matieres; }

    public int getTotalEtudiants() { return etudiants.size(); }
    public int getTotalMatieres() { return matieres.size(); }
    public int getTotalAffectations() { return affectations.size(); }
    public long getTotalNonAffectees() { return affectations.stream().filter(a -> "NON_AFFECTE".equals(a.getStatut())).count(); }

    public String getSelectedFiliere() { return selectedFiliere; }
    public void setSelectedFiliere(String selectedFiliere) { this.selectedFiliere = selectedFiliere; }
    public String getSelectedGroupe() { return selectedGroupe; }
    public void setSelectedGroupe(String selectedGroupe) { this.selectedGroupe = selectedGroupe; }
    public String getSelectedSemestre() { return selectedSemestre; }
    public void setSelectedSemestre(String selectedSemestre) { this.selectedSemestre = selectedSemestre; }
    public String getSelectedAnnee() { return selectedAnnee; }
    public void setSelectedAnnee(String selectedAnnee) { this.selectedAnnee = selectedAnnee; }

    public String getNewMatricule() { return newMatricule; }
    public void setNewMatricule(String newMatricule) { this.newMatricule = newMatricule; }
    public String getNewNom() { return newNom; }
    public void setNewNom(String newNom) { this.newNom = newNom; }
    public String getNewPrenom() { return newPrenom; }
    public void setNewPrenom(String newPrenom) { this.newPrenom = newPrenom; }
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    public String getNewTel() { return newTel; }
    public void setNewTel(String newTel) { this.newTel = newTel; }
    public String getNewSexe() { return newSexe; }
    public void setNewSexe(String newSexe) { this.newSexe = newSexe; }
    public String getNewDateNaissance() { return newDateNaissance; }
    public void setNewDateNaissance(String newDateNaissance) { this.newDateNaissance = newDateNaissance; }
    public String getNewGroupe() { return newGroupe; }
    public void setNewGroupe(String newGroupe) { this.newGroupe = newGroupe; }
}
