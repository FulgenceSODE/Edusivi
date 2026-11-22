package com.mycompany.web;

import com.mycompany.AnneeAcademique;
import com.mycompany.CompositionUe;
import com.mycompany.CompositionUePK;
import com.mycompany.Departement;
import com.mycompany.ElementConstitutif;
import com.mycompany.Filiere;
import com.mycompany.Groupe;
import com.mycompany.JournalAudit;
import com.mycompany.Niveau;
import com.mycompany.Semestre;
import com.mycompany.UniteEnseignement;
import com.mycompany.Utilisateur;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@WebServlet(name = "AcademicStructureServlet", urlPatterns = "/de/structure")
public class AcademicStructureServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VIEW = "/de/structure-academique.jsp";
    private static final String FLASH_MESSAGE = "academicStructureMessage";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!request.isUserInRole("DE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            request.setAttribute("departments", em.createQuery(
                    "SELECT d FROM Departement d ORDER BY d.code", Departement.class).getResultList());
            request.setAttribute("programs", em.createQuery(
                    "SELECT f FROM Filiere f LEFT JOIN FETCH f.idDepartement ORDER BY f.code",
                    Filiere.class).getResultList());
            request.setAttribute("levels", em.createQuery(
                    "SELECT n FROM Niveau n LEFT JOIN FETCH n.idFiliere "
                    + "ORDER BY n.idFiliere.code, n.code",
                    Niveau.class).getResultList());
            request.setAttribute("groups", em.createQuery(
                    "SELECT g FROM Groupe g JOIN FETCH g.idAnnee JOIN FETCH g.idFiliere "
                    + "JOIN FETCH g.idNiveau ORDER BY g.idAnnee.dateDebut DESC, g.idFiliere.code, "
                    + "g.idNiveau.code, g.code", Groupe.class).getResultList());
            request.setAttribute("academicYears", em.createQuery(
                    "SELECT a FROM AnneeAcademique a ORDER BY a.dateDebut DESC",
                    AnneeAcademique.class).getResultList());
            request.setAttribute("semesters", em.createQuery(
                    "SELECT s FROM Semestre s JOIN FETCH s.idAnnee ORDER BY s.idAnnee.dateDebut DESC, s.dateDebut",
                    Semestre.class).getResultList());
            request.setAttribute("teachingUnits", em.createQuery(
                    "SELECT u FROM UniteEnseignement u LEFT JOIN FETCH u.idSemestre ORDER BY u.code",
                    UniteEnseignement.class).getResultList());
            request.setAttribute("courseElements", em.createQuery(
                    "SELECT e FROM ElementConstitutif e ORDER BY e.code",
                    ElementConstitutif.class).getResultList());
            request.setAttribute("unitComponents", em.createQuery(
                    "SELECT c FROM CompositionUe c JOIN FETCH c.uniteEnseignement "
                    + "JOIN FETCH c.elementConstitutif ORDER BY c.uniteEnseignement.code, c.elementConstitutif.code",
                    CompositionUe.class).getResultList());
            request.setAttribute("csrfToken", CsrfTokens.ensureToken(request));
            request.setAttribute("flashMessage", request.getSession().getAttribute(FLASH_MESSAGE));
            request.getSession().removeAttribute(FLASH_MESSAGE);
            request.getRequestDispatcher(VIEW).forward(request, response);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load the academic structure.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!request.isUserInRole("DE")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (em == null || transaction == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            transaction.begin();
            Utilisateur actor = currentUser(request);
            if (actor == null) {
                throw new IllegalStateException("Authenticated DE account is missing from the user table.");
            }
            String action = request.getParameter("action");
            if ("createDepartment".equals(action)) {
                saveDepartment(request, actor, null);
            } else if ("updateDepartment".equals(action)) {
                saveDepartment(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("createProgram".equals(action)) {
                saveProgram(request, actor, null);
            } else if ("updateProgram".equals(action)) {
                saveProgram(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("createLevel".equals(action)) {
                saveLevel(request, actor, null);
            } else if ("updateLevel".equals(action)) {
                saveLevel(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("createGroup".equals(action)) {
                saveGroup(request, actor, null);
            } else if ("updateGroup".equals(action)) {
                saveGroup(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("createUnit".equals(action)) {
                saveUnit(request, actor, null);
            } else if ("updateUnit".equals(action)) {
                saveUnit(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("createElement".equals(action)) {
                saveElement(request, actor, null);
            } else if ("updateElement".equals(action)) {
                saveElement(request, actor, requiredId(request.getParameter("entityId")));
            } else if ("saveUnitComponent".equals(action)) {
                saveUnitComponent(request, actor);
            } else {
                throw new IllegalArgumentException("L’opération demandée n’est pas reconnue.");
            }
            em.flush();
            transaction.commit();
            response.sendRedirect(request.getContextPath() + "/de/structure?saved=true");
        } catch (IllegalArgumentException ex) {
            rollbackIfActive();
            request.getSession().setAttribute(FLASH_MESSAGE, ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/de/structure");
        } catch (Exception ex) {
            rollbackIfActive();
            getServletContext().log("Unable to save an academic structure change.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La modification de la structure académique a échoué.");
        }
    }

    private void saveDepartment(HttpServletRequest request, Utilisateur actor, Long departmentId) {
        String code = requiredCode(request.getParameter("code"), 30);
        String name = requiredText(request.getParameter("name"), "Le nom du département est obligatoire.", 150);
        ensureUniqueDepartmentCode(code, departmentId);
        Departement department = departmentId == null ? new Departement()
                : findEntity(Departement.class, departmentId, "Le département sélectionné n’existe plus.");
        department.setCode(code);
        department.setNom(name);
        if (departmentId == null) {
            em.persist(department);
            em.flush();
        }
        audit(actor, departmentId == null ? "DEPARTEMENT_CREE" : "DEPARTEMENT_MODIFIE",
                "Departement", String.valueOf(department.getIdDepartement()), code + " - " + name);
    }

    private void saveProgram(HttpServletRequest request, Utilisateur actor, Long programId) {
        String code = requiredCode(request.getParameter("code"), 30);
        String name = requiredText(request.getParameter("name"), "Le nom de la filière est obligatoire.", 150);
        String label = optionalText(request.getParameter("label"), 150);
        Long departmentId = optionalId(request.getParameter("departmentId"));
        ensureUniqueProgramCode(code, programId);
        Departement department = departmentId == null ? null
                : findEntity(Departement.class, departmentId, "Le département sélectionné n’existe plus.");
        Filiere program = programId == null ? new Filiere()
                : findEntity(Filiere.class, programId, "La filière sélectionnée n’existe plus.");
        program.setCode(code);
        program.setNom(name);
        program.setLibelle(label);
        program.setIdDepartement(department);
        if (programId == null) {
            em.persist(program);
            em.flush();
        }
        audit(actor, programId == null ? "FILIERE_CREEE" : "FILIERE_MODIFIEE",
                "Filiere", String.valueOf(program.getIdFiliere()), code + " - " + name);
    }

    private void saveLevel(HttpServletRequest request, Utilisateur actor, Long levelId) {
        String code = requiredCode(request.getParameter("code"), 30);
        String label = requiredText(request.getParameter("label"), "Le libellé du niveau est obligatoire.", 100);
        Long programId = requiredId(request.getParameter("programId"));
        Filiere program = findEntity(Filiere.class, programId, "La filière sélectionnée n’existe plus.");
        ensureUniqueLevelCode(code, programId, levelId);
        Niveau level = levelId == null ? new Niveau()
                : findEntity(Niveau.class, levelId, "Le niveau sélectionné n’existe plus.");
        if (levelId != null && (level.getIdFiliere() == null
                || !programId.equals(level.getIdFiliere().getIdFiliere()))) {
            Long groupsUsingLevel = em.createQuery(
                    "SELECT COUNT(g) FROM Groupe g WHERE g.idNiveau.idNiveau = :levelId", Long.class)
                    .setParameter("levelId", levelId)
                    .getSingleResult();
            if (groupsUsingLevel > 0) {
                throw new IllegalArgumentException(
                        "La filière d’un niveau utilisé par des groupes ne peut pas être modifiée.");
            }
        }
        level.setCode(code);
        level.setLibelle(label);
        level.setIdFiliere(program);
        if (levelId == null) {
            em.persist(level);
            em.flush();
        }
        audit(actor, levelId == null ? "NIVEAU_CREE" : "NIVEAU_MODIFIE",
                "Niveau", String.valueOf(level.getIdNiveau()), code + " - " + label);
    }

    private void saveGroup(HttpServletRequest request, Utilisateur actor, Long groupId) {
        String code = requiredCode(request.getParameter("code"), 40);
        String label = requiredText(request.getParameter("label"), "Le libellé du groupe est obligatoire.", 120);
        Long yearId = requiredId(request.getParameter("yearId"));
        Long programId = requiredId(request.getParameter("programId"));
        Long levelId = requiredId(request.getParameter("levelId"));
        AnneeAcademique year = findEntity(
                AnneeAcademique.class, yearId, "L’année académique sélectionnée n’existe plus.");
        Filiere program = findEntity(Filiere.class, programId, "La filière sélectionnée n’existe plus.");
        Niveau level = findEntity(Niveau.class, levelId, "Le niveau sélectionné n’existe plus.");
        if ("CLOTUREE".equalsIgnoreCase(year.getStatut())) {
            throw new IllegalArgumentException("Un groupe ne peut pas être créé dans une année clôturée.");
        }
        if (level.getIdFiliere() == null || !programId.equals(level.getIdFiliere().getIdFiliere())) {
            throw new IllegalArgumentException("Le niveau sélectionné n’appartient pas à cette filière.");
        }
        ensureUniqueGroupCode(code, yearId, programId, levelId, groupId);
        Groupe group = groupId == null ? new Groupe()
                : findEntity(Groupe.class, groupId, "Le groupe sélectionné n’existe plus.");
        if (groupId != null && (!yearId.equals(group.getIdAnnee().getIdAnnee())
                || !programId.equals(group.getIdFiliere().getIdFiliere())
                || !levelId.equals(group.getIdNiveau().getIdNiveau()))) {
            throw new IllegalArgumentException(
                    "L’année, la filière et le niveau d’un groupe existant sont conservés pour préserver son historique.");
        }
        group.setCode(code);
        group.setLibelle(label);
        group.setIdAnnee(year);
        group.setIdFiliere(program);
        group.setIdNiveau(level);
        if (groupId == null) {
            em.persist(group);
            em.flush();
        }
        audit(actor, groupId == null ? "GROUPE_CREE" : "GROUPE_MODIFIE",
                "Groupe", String.valueOf(group.getIdGroupe()),
                code + " - " + label + " (" + year.getLibelle() + ")");
    }

    private void saveUnit(HttpServletRequest request, Utilisateur actor, Long unitId) {
        String code = requiredCode(request.getParameter("code"), 30);
        String label = requiredText(request.getParameter("label"), "Le libellé de l’UE est obligatoire.", 150);
        String description = optionalText(request.getParameter("description"), 2000);
        Long semesterId = requiredId(request.getParameter("semesterId"));
        Semestre semester = findEntity(
                Semestre.class, semesterId, "Le semestre sélectionné n’existe plus.");
        UniteEnseignement unit = unitId == null ? new UniteEnseignement()
                : findEntity(UniteEnseignement.class, unitId, "L’UE sélectionnée n’existe plus.");
        if ("CLOTUREE".equalsIgnoreCase(semester.getIdAnnee().getStatut())
                && (unitId == null || unit.getIdSemestre() == null
                || !semesterId.equals(unit.getIdSemestre().getIdSemestre()))) {
            throw new IllegalArgumentException("Une UE ne peut pas être créée ou déplacée dans une année clôturée.");
        }
        ensureUniqueUnitCode(code, unitId);
        unit.setCode(code);
        unit.setLibelle(label);
        unit.setDescription(description);
        unit.setIdSemestre(semester);
        if (unitId == null) {
            em.persist(unit);
            em.flush();
        }
        audit(actor, unitId == null ? "UE_CREEE" : "UE_MODIFIEE",
                "UniteEnseignement", String.valueOf(unit.getIdUe()), code + " - " + label);
    }

    private void saveElement(HttpServletRequest request, Utilisateur actor, Long elementId) {
        String code = requiredCode(request.getParameter("code"), 30);
        String label = requiredText(request.getParameter("label"), "Le libellé de l’EC est obligatoire.", 150);
        String description = optionalText(request.getParameter("description"), 2000);
        ensureUniqueElementCode(code, elementId);
        ElementConstitutif element = elementId == null ? new ElementConstitutif()
                : findEntity(ElementConstitutif.class, elementId, "L’EC sélectionné n’existe plus.");
        element.setCode(code);
        element.setLibelle(label);
        element.setDescription(description);
        if (elementId == null) {
            em.persist(element);
            em.flush();
        }
        audit(actor, elementId == null ? "EC_CREE" : "EC_MODIFIE",
                "ElementConstitutif", String.valueOf(element.getIdEc()), code + " - " + label);
    }

    private void saveUnitComponent(HttpServletRequest request, Utilisateur actor) {
        Long unitId = requiredId(request.getParameter("unitId"));
        Long elementId = requiredId(request.getParameter("elementId"));
        BigDecimal coefficient = AcademicStructureRules.parsePositiveCoefficient(
                request.getParameter("coefficient"));
        Integer order = optionalOrder(request.getParameter("order"));
        UniteEnseignement unit = findEntity(
                UniteEnseignement.class, unitId, "L’UE sélectionnée n’existe plus.");
        ElementConstitutif element = findEntity(
                ElementConstitutif.class, elementId, "L’EC sélectionné n’existe plus.");
        if (unit.getIdSemestre() == null
                || "CLOTUREE".equalsIgnoreCase(unit.getIdSemestre().getIdAnnee().getStatut())) {
            throw new IllegalArgumentException(
                    "La composition d’une UE d’année clôturée ne peut pas être modifiée.");
        }
        CompositionUePK key = new CompositionUePK(unitId, elementId);
        CompositionUe component = em.find(CompositionUe.class, key);
        boolean created = component == null;
        if (created) {
            component = new CompositionUe(key, coefficient);
        }
        component.setCoefficient(coefficient);
        component.setOrdre(order);
        if (created) {
            em.persist(component);
        }
        audit(actor, created ? "EC_AJOUTE_A_UE" : "COEFFICIENT_EC_MODIFIE",
                "CompositionUe", unitId + ":" + elementId,
                "Coefficient " + coefficient.toPlainString() + " pour " + unit.getCode() + " / " + element.getCode());
    }

    private void ensureUniqueDepartmentCode(String code, Long excludedId) {
        String query = "SELECT COUNT(d) FROM Departement d WHERE UPPER(d.code) = :code"
                + (excludedId == null ? "" : " AND d.idDepartement <> :excludedId");
        var result = em.createQuery(query, Long.class).setParameter("code", code);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code de département est déjà utilisé.");
        }
    }

    private void ensureUniqueProgramCode(String code, Long excludedId) {
        String query = "SELECT COUNT(f) FROM Filiere f WHERE UPPER(f.code) = :code"
                + (excludedId == null ? "" : " AND f.idFiliere <> :excludedId");
        var result = em.createQuery(query, Long.class).setParameter("code", code);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code de filière est déjà utilisé.");
        }
    }

    private void ensureUniqueLevelCode(String code, Long programId, Long excludedId) {
        String query = "SELECT COUNT(n) FROM Niveau n WHERE UPPER(n.code) = :code "
                + "AND n.idFiliere.idFiliere = :programId"
                + (excludedId == null ? "" : " AND n.idNiveau <> :excludedId");
        var result = em.createQuery(query, Long.class)
                .setParameter("code", code)
                .setParameter("programId", programId);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code de niveau existe déjà dans cette filière.");
        }
    }

    private void ensureUniqueGroupCode(String code, Long yearId, Long programId, Long levelId, Long excludedId) {
        String query = "SELECT COUNT(g) FROM Groupe g WHERE UPPER(g.code) = :code "
                + "AND g.idAnnee.idAnnee = :yearId AND g.idFiliere.idFiliere = :programId "
                + "AND g.idNiveau.idNiveau = :levelId"
                + (excludedId == null ? "" : " AND g.idGroupe <> :excludedId");
        var result = em.createQuery(query, Long.class)
                .setParameter("code", code)
                .setParameter("yearId", yearId)
                .setParameter("programId", programId)
                .setParameter("levelId", levelId);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code de groupe existe déjà pour cette année, filière et niveau.");
        }
    }

    private void ensureUniqueUnitCode(String code, Long excludedId) {
        String query = "SELECT COUNT(u) FROM UniteEnseignement u WHERE UPPER(u.code) = :code"
                + (excludedId == null ? "" : " AND u.idUe <> :excludedId");
        var result = em.createQuery(query, Long.class).setParameter("code", code);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code d’UE est déjà utilisé.");
        }
    }

    private void ensureUniqueElementCode(String code, Long excludedId) {
        String query = "SELECT COUNT(e) FROM ElementConstitutif e WHERE UPPER(e.code) = :code"
                + (excludedId == null ? "" : " AND e.idEc <> :excludedId");
        var result = em.createQuery(query, Long.class).setParameter("code", code);
        if (excludedId != null) {
            result.setParameter("excludedId", excludedId);
        }
        if (result.getSingleResult() > 0) {
            throw new IllegalArgumentException("Ce code d’EC est déjà utilisé.");
        }
    }

    private <T> T findEntity(Class<T> type, Long id, String errorMessage) {
        T entity = em.find(type, id);
        if (entity == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return entity;
    }

    private Utilisateur currentUser(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            return null;
        }
        List<Utilisateur> users = em.createQuery(
                "SELECT u FROM Utilisateur u WHERE LOWER(u.email) = LOWER(:email)",
                Utilisateur.class)
                .setParameter("email", request.getUserPrincipal().getName())
                .setMaxResults(1)
                .getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    private void audit(Utilisateur actor, String action, String entityType, String entityId, String details) {
        JournalAudit entry = new JournalAudit();
        entry.setAction(action);
        entry.setTypeEntite(entityType);
        entry.setIdEntite(entityId);
        entry.setDetails(details);
        entry.setDateAction(new Date());
        entry.setIdUtilisateur(actor);
        em.persist(entry);
    }

    private static String requiredCode(String value, int maxLength) {
        return requiredText(value, "Le code est obligatoire.", maxLength).toUpperCase(Locale.ROOT);
    }

    private static String requiredText(String value, String message, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty() || text.length() > maxLength) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    private static String optionalText(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() > maxLength) {
            throw new IllegalArgumentException("Le libellé dépasse la longueur autorisée.");
        }
        return text.isEmpty() ? null : text;
    }

    private static Long optionalId(String value) {
        return value == null || value.isBlank() ? null : requiredId(value);
    }

    private static Long requiredId(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant fourni est invalide.");
        }
    }

    private static Integer optionalOrder(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int order = Integer.parseInt(value);
            if (order < 1) {
                throw new NumberFormatException();
            }
            return order;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("L’ordre doit être un entier positif.");
        }
    }

    private void rollbackIfActive() {
        try {
            if (transaction != null
                    && transaction.getStatus() != jakarta.transaction.Status.STATUS_NO_TRANSACTION) {
                transaction.rollback();
            }
        } catch (Exception rollbackError) {
            getServletContext().log("Unable to roll back the academic structure transaction.", rollbackError);
        }
    }
}
