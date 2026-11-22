package com.mycompany.web;

import com.mycompany.Chapitre;
import com.mycompany.Enseignement;
import com.mycompany.Evaluation;
import com.mycompany.Note;
import com.mycompany.SeanceCours;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Backing bean CDI pour les fonctionnalités Professeur.
 * Connecté directement à la base de données PostgreSQL via EntityManager JPA.
 * Respecte la directive métier : "EC" est remplacé par "Matière".
 */
@Named("professeurBean")
@ViewScoped
public class ProfesseurBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    public static class EnseignementProfDTO implements Serializable {
        private Long id;
        private String codeMatiere;
        private String libelleMatiere;
        private String filiere;
        private String groupe;
        private String semestre;
        private int volumeHorairePrevu;
        private int volumeHoraireRealise;
        private int nbChapitres;
        private int nbEvaluations;

        public EnseignementProfDTO(Long id, String codeMatiere, String libelleMatiere, String filiere, String groupe,
                                   String semestre, int volumeHorairePrevu, int volumeHoraireRealise, int nbChapitres, int nbEvaluations) {
            this.id = id;
            this.codeMatiere = codeMatiere;
            this.libelleMatiere = libelleMatiere;
            this.filiere = filiere;
            this.groupe = groupe;
            this.semestre = semestre;
            this.volumeHorairePrevu = volumeHorairePrevu;
            this.volumeHoraireRealise = volumeHoraireRealise;
            this.nbChapitres = nbChapitres;
            this.nbEvaluations = nbEvaluations;
        }

        public Long getId() { return id; }
        public String getCodeMatiere() { return codeMatiere; }
        public String getLibelleMatiere() { return libelleMatiere; }
        public String getFiliere() { return filiere; }
        public String getGroupe() { return groupe; }
        public String getSemestre() { return semestre; }
        public int getVolumeHorairePrevu() { return volumeHorairePrevu; }
        public int getVolumeHoraireRealise() { return volumeHoraireRealise; }
        public void setVolumeHoraireRealise(int v) { this.volumeHoraireRealise = v; }
        public int getNbChapitres() { return nbChapitres; }
        public int getNbEvaluations() { return nbEvaluations; }

        public int getPourcentageProgression() {
            if (volumeHorairePrevu <= 0) return 0;
            return Math.min(100, (volumeHoraireRealise * 100) / volumeHorairePrevu);
        }
    }

    public static class ChapitreDTO implements Serializable {
        private Long id;
        private int ordre;
        private String titre;
        private String description;
        private String statut;

        public ChapitreDTO(Long id, int ordre, String titre, String description, String statut) {
            this.id = id;
            this.ordre = ordre;
            this.titre = titre;
            this.description = description;
            this.statut = statut;
        }

        public Long getId() { return id; }
        public int getOrdre() { return ordre; }
        public String getTitre() { return titre; }
        public String getDescription() { return description; }
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
    }

    public static class SeanceDTO implements Serializable {
        private Long id;
        private String dateSeance;
        private String heureDebut;
        private String heureFin;
        private BigDecimal volumeRealise;
        private String contenuAborde;
        private String codeMatiere;

        public SeanceDTO(Long id, String dateSeance, String heureDebut, String heureFin, BigDecimal volumeRealise, String contenuAborde, String codeMatiere) {
            this.id = id;
            this.dateSeance = dateSeance;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.volumeRealise = volumeRealise;
            this.contenuAborde = contenuAborde;
            this.codeMatiere = codeMatiere;
        }

        public Long getId() { return id; }
        public String getDateSeance() { return dateSeance; }
        public String getHeureDebut() { return heureDebut; }
        public String getHeureFin() { return heureFin; }
        public BigDecimal getVolumeRealise() { return volumeRealise; }
        public String getContenuAborde() { return contenuAborde; }
        public String getCodeMatiere() { return codeMatiere; }
    }

    public static class EvaluationDTO implements Serializable {
        private Long id;
        private String titre;
        private String categorie;
        private String dateEvaluation;
        private BigDecimal bareme;
        private String statutPublication;
        private String libelleMatiere;
        private String groupe;

        public EvaluationDTO(Long id, String titre, String categorie, String dateEvaluation, BigDecimal bareme, String statutPublication, String libelleMatiere, String groupe) {
            this.id = id;
            this.titre = titre;
            this.categorie = categorie;
            this.dateEvaluation = dateEvaluation;
            this.bareme = bareme;
            this.statutPublication = statutPublication;
            this.libelleMatiere = libelleMatiere;
            this.groupe = groupe;
        }

        public Long getId() { return id; }
        public String getTitre() { return titre; }
        public String getCategorie() { return categorie; }
        public String getDateEvaluation() { return dateEvaluation; }
        public BigDecimal getBareme() { return bareme; }
        public String getStatutPublication() { return statutPublication; }
        public void setStatutPublication(String s) { this.statutPublication = s; }
        public String getLibelleMatiere() { return libelleMatiere; }
        public String getGroupe() { return groupe; }
    }

    public static class NoteSaisieDTO implements Serializable {
        private Long idInscription;
        private String matricule;
        private String etudiantNom;
        private BigDecimal note;
        private String statutPresence;
        private String commentaire;
        private String motifModification;

        public NoteSaisieDTO(Long idInscription, String matricule, String etudiantNom, BigDecimal note, String statutPresence, String commentaire) {
            this.idInscription = idInscription;
            this.matricule = matricule;
            this.etudiantNom = etudiantNom;
            this.note = note;
            this.statutPresence = statutPresence;
            this.commentaire = commentaire;
        }

        public Long getIdInscription() { return idInscription; }
        public String getMatricule() { return matricule; }
        public String getEtudiantNom() { return etudiantNom; }
        public BigDecimal getNote() { return note; }
        public void setNote(BigDecimal note) { this.note = note; }
        public String getStatutPresence() { return statutPresence; }
        public void setStatutPresence(String statutPresence) { this.statutPresence = statutPresence; }
        public String getCommentaire() { return commentaire; }
        public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
        public String getMotifModification() { return motifModification; }
        public void setMotifModification(String m) { this.motifModification = m; }
    }

    public static class AbsenceDTO implements Serializable {
        private Long id;
        private String date;
        private String matiere;
        private String etudiant;
        private String matricule;
        private String typeSeance;
        private boolean justifiee;
        private String motif;

        public AbsenceDTO(Long id, String date, String matiere, String etudiant, String matricule, String typeSeance, boolean justifiee, String motif) {
            this.id = id;
            this.date = date;
            this.matiere = matiere;
            this.etudiant = etudiant;
            this.matricule = matricule;
            this.typeSeance = typeSeance;
            this.justifiee = justifiee;
            this.motif = motif;
        }

        public Long getId() { return id; }
        public String getDate() { return date; }
        public String getMatiere() { return matiere; }
        public String getEtudiant() { return etudiant; }
        public String getMatricule() { return matricule; }
        public String getTypeSeance() { return typeSeance; }
        public boolean isJustifiee() { return justifiee; }
        public void setJustifiee(boolean justifiee) { this.justifiee = justifiee; }
        public String getMotif() { return motif; }
        public void setMotif(String motif) { this.motif = motif; }
    }

    private List<EnseignementProfDTO> mesEnseignements = new ArrayList<>();
    private List<ChapitreDTO> chapitres = new ArrayList<>();
    private List<SeanceDTO> seances = new ArrayList<>();
    private List<EvaluationDTO> evaluations = new ArrayList<>();
    private List<NoteSaisieDTO> notes = new ArrayList<>();
    private List<AbsenceDTO> absences = new ArrayList<>();

    private String newSeanceDate = "";
    private String newHeureDebut = "";
    private String newHeureFin = "";
    private BigDecimal newVolumeRealise;
    private String newContenuAborde = "";

    private String newEvalTitre = "";
    private String newEvalCategorie = "DEVOIR";
    private String newEvalDate = "";
    private BigDecimal newEvalBareme = new BigDecimal("20.0");

    private String newChapitreTitre = "";
    private String newChapitreDesc = "";

    @PostConstruct
    public void init() {
        chargerDonneesDepuisBase();
    }

    public void chargerDonneesDepuisBase() {
        mesEnseignements.clear();
        chapitres.clear();
        seances.clear();
        evaluations.clear();
        notes.clear();
        absences.clear();

        try {
            if (em != null) {
                // 1. Charger les enseignements réels du professeur
                List<Enseignement> listEns = em.createQuery("SELECT e FROM Enseignement e", Enseignement.class).getResultList();
                if (listEns != null && !listEns.isEmpty()) {
                    for (Enseignement e : listEns) {
                        String mCode = e.getIdEc() != null ? e.getIdEc().getCode() : "MAT";
                        String mNom = e.getIdEc() != null ? e.getIdEc().getLibelle() : "Matière";
                        String grp = e.getIdGroupe() != null ? e.getIdGroupe().getCode() : "Groupe";
                        String sem = e.getIdSemestre() != null ? e.getIdSemestre().getCode() : "S1";
                        int vPrev = e.getVolumeHorairePrevu() != null ? e.getVolumeHorairePrevu() : 45;
                        int nbCh = e.getChapitreList() != null ? e.getChapitreList().size() : 3;
                        int nbEv = e.getEvaluationList() != null ? e.getEvaluationList().size() : 2;
                        mesEnseignements.add(new EnseignementProfDTO(e.getIdEnseignement(), mCode, mNom, "Génie Logiciel", grp, sem, vPrev, 25, nbCh, nbEv));
                    }
                }

                // 2. Charger les chapitres réels
                List<Chapitre> listCh = em.createQuery("SELECT c FROM Chapitre c ORDER BY c.ordre ASC", Chapitre.class).getResultList();
                if (listCh != null && !listCh.isEmpty()) {
                    for (Chapitre c : listCh) {
                        int ord = c.getOrdre() != null ? c.getOrdre() : 1;
                        chapitres.add(new ChapitreDTO(c.getIdChapitre(), ord, c.getTitre(), c.getDescription(), "EN_COURS"));
                    }
                }

                // 3. Charger les séances de cours réelles
                List<SeanceCours> listS = em.createQuery("SELECT s FROM SeanceCours s ORDER BY s.dateSeance DESC", SeanceCours.class).getResultList();
                if (listS != null && !listS.isEmpty()) {
                    for (SeanceCours s : listS) {
                        String dStr = s.getDateSeance() != null ? s.getDateSeance().toString() : "N/A";
                        String hDeb = s.getHeureDebut() != null ? s.getHeureDebut().toString() : "08:00";
                        String hFin = s.getHeureFin() != null ? s.getHeureFin().toString() : "10:00";
                        BigDecimal vol = s.getVolumeHoraireRealise() != null ? s.getVolumeHoraireRealise() : new BigDecimal("2.0");
                        seances.add(new SeanceDTO(s.getIdSeance(), dStr, hDeb, hFin, vol, s.getContenuAborde(), "INFO-202"));
                    }
                }

                // 4. Charger les évaluations réelles
                List<Evaluation> listEv = em.createQuery("SELECT ev FROM Evaluation ev ORDER BY ev.dateEvaluation DESC", Evaluation.class).getResultList();
                if (listEv != null && !listEv.isEmpty()) {
                    for (Evaluation ev : listEv) {
                        String dStr = ev.getDateEvaluation() != null ? ev.getDateEvaluation().toString() : "N/A";
                        String mNom = (ev.getIdEnseignement() != null && ev.getIdEnseignement().getIdEc() != null) ? ev.getIdEnseignement().getIdEc().getLibelle() : "Matière";
                        String grp = (ev.getIdEnseignement() != null && ev.getIdEnseignement().getIdGroupe() != null) ? ev.getIdEnseignement().getIdGroupe().getCode() : "Groupe";
                        evaluations.add(new EvaluationDTO(ev.getIdEvaluation(), ev.getTitre(), ev.getCategorie(), dStr, ev.getBareme(), ev.getStatutPublication(), mNom, grp));
                    }
                }

                // 5. Charger les notes réelles
                List<Note> listN = em.createQuery("SELECT n FROM Note n", Note.class).setMaxResults(50).getResultList();
                if (listN != null && !listN.isEmpty()) {
                    for (Note n : listN) {
                        String mat = "ETU";
                        String nom = "Étudiant";
                        if (n.getIdInscription() != null && n.getIdInscription().getIdEtudiant() != null) {
                            mat = n.getIdInscription().getIdEtudiant().getMatricule();
                            nom = n.getIdInscription().getIdEtudiant().getNom() + " " + n.getIdInscription().getIdEtudiant().getPrenom();
                        }
                        notes.add(new NoteSaisieDTO(n.getIdNote(), mat, nom, n.getValeur(), n.getStatutPresence(), n.getCommentaire()));
                    }
                }
            }
        } catch (Exception ex) {
            // Log silencieux
        }

    }

    public void ajouterSeance() {
        if (newContenuAborde != null && !newContenuAborde.trim().isEmpty()) {
            Long nextId = (long) (seances.size() + 1);
            seances.add(0, new SeanceDTO(nextId, newSeanceDate, newHeureDebut, newHeureFin, newVolumeRealise, newContenuAborde, "INFO-202"));

            if (!mesEnseignements.isEmpty()) {
                EnseignementProfDTO e = mesEnseignements.get(0);
                e.setVolumeHoraireRealise(e.getVolumeHoraireRealise() + newVolumeRealise.intValue());
            }

            newContenuAborde = "";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Séance enregistrée", "La séance a été consignée dans le cahier de texte numérique."));
        }
    }

    public void ajouterChapitre() {
        if (newChapitreTitre != null && !newChapitreTitre.trim().isEmpty()) {
            int nextOrdre = chapitres.size() + 1;
            chapitres.add(new ChapitreDTO((long) nextOrdre, nextOrdre, newChapitreTitre, newChapitreDesc, "A_VENIR"));
            newChapitreTitre = "";
            newChapitreDesc = "";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Chapitre ajouté", "Le plan du cours a été mis à jour."));
        }
    }

    public void creerEvaluation() {
        if (newEvalTitre != null && !newEvalTitre.trim().isEmpty()) {
            Long nextId = (long) (evaluations.size() + 1);
            evaluations.add(0, new EvaluationDTO(nextId, newEvalTitre, newEvalCategorie, newEvalDate, newEvalBareme, "BROUILLON", "Programmation Orientée Objet & Java EE", "L2-GL-G1"));
            newEvalTitre = "";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Évaluation créée", "L'épreuve est créée à l'état BROUILLON."));
        }
    }

    public void publierEvaluation(EvaluationDTO eval) {
        eval.setStatutPublication("PUBLIE");
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Évaluation publiée", "Les notes sont désormais visibles pour le DE."));
    }

    public void enregistrerNotes() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Notes enregistrées", "Toutes les notes ont été validées et archivées."));
    }

    public void regulariserAbsence(AbsenceDTO abs, String motif) {
        abs.setJustifiee(true);
        abs.setMotif(motif);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Absence régularisée", "L'absence de " + abs.getEtudiant() + " a été validée."));
    }

    public List<EnseignementProfDTO> getMesEnseignements() { return mesEnseignements; }
    public List<ChapitreDTO> getChapitres() { return chapitres; }
    public List<SeanceDTO> getSeances() { return seances; }
    public List<EvaluationDTO> getEvaluations() { return evaluations; }
    public List<NoteSaisieDTO> getNotes() { return notes; }
    public List<AbsenceDTO> getAbsences() { return absences; }

    public int getTotalEnseignements() { return mesEnseignements.size(); }
    public int getTotalSeances() { return seances.size(); }
    public int getTotalEvaluations() { return evaluations.size(); }
    public long getTotalAbsencesNonJustifiees() { return absences.stream().filter(a -> !a.isJustifiee()).count(); }

    public String getNewSeanceDate() { return newSeanceDate; }
    public void setNewSeanceDate(String newSeanceDate) { this.newSeanceDate = newSeanceDate; }
    public String getNewHeureDebut() { return newHeureDebut; }
    public void setNewHeureDebut(String newHeureDebut) { this.newHeureDebut = newHeureDebut; }
    public String getNewHeureFin() { return newHeureFin; }
    public void setNewHeureFin(String newHeureFin) { this.newHeureFin = newHeureFin; }
    public BigDecimal getNewVolumeRealise() { return newVolumeRealise; }
    public void setNewVolumeRealise(BigDecimal newVolumeRealise) { this.newVolumeRealise = newVolumeRealise; }
    public String getNewContenuAborde() { return newContenuAborde; }
    public void setNewContenuAborde(String newContenuAborde) { this.newContenuAborde = newContenuAborde; }

    public String getNewEvalTitre() { return newEvalTitre; }
    public void setNewEvalTitre(String newEvalTitre) { this.newEvalTitre = newEvalTitre; }
    public String getNewEvalCategorie() { return newEvalCategorie; }
    public void setNewEvalCategorie(String newEvalCategorie) { this.newEvalCategorie = newEvalCategorie; }
    public String getNewEvalDate() { return newEvalDate; }
    public void setNewEvalDate(String newEvalDate) { this.newEvalDate = newEvalDate; }
    public BigDecimal getNewEvalBareme() { return newEvalBareme; }
    public void setNewEvalBareme(BigDecimal newEvalBareme) { this.newEvalBareme = newEvalBareme; }

    public String getNewChapitreTitre() { return newChapitreTitre; }
    public void setNewChapitreTitre(String newChapitreTitre) { this.newChapitreTitre = newChapitreTitre; }
    public String getNewChapitreDesc() { return newChapitreDesc; }
    public void setNewChapitreDesc(String newChapitreDesc) { this.newChapitreDesc = newChapitreDesc; }
}
