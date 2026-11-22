package com.mycompany.web;

import com.mycompany.InscriptionEtudiant;
import com.mycompany.Semestre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@WebServlet(name = "GradeReportServlet", urlPatterns = "/de/releves.pdf")
public class GradeReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String TEMPLATE = "reports/releve_notes.jrxml";

    @PersistenceContext(unitName = "EduSuiviDS")
    private EntityManager em;

    private JasperReport compiledReport;

    @Override
    public void init() throws ServletException {
        try (InputStream template = getClass().getClassLoader().getResourceAsStream(TEMPLATE)) {
            if (template == null) {
                throw new ServletException("Jasper report template is missing: " + TEMPLATE);
            }
            compiledReport = JasperCompileManager.compileReport(template);
        } catch (JRException | IOException ex) {
            getServletContext().log("Unable to compile the Jasper transcript template.", ex);
            throw new ServletException("The Jasper transcript template could not be compiled.", ex);
        }
    }

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

        Long enrollmentId;
        Long semesterId;
        try {
            enrollmentId = requiredId(request.getParameter("enrollmentId"), "inscription");
            semesterId = requiredId(request.getParameter("semesterId"), "semestre");
        } catch (IllegalArgumentException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }

        try {
            List<InscriptionEtudiant> enrollments = em.createQuery(
                    "SELECT i FROM InscriptionEtudiant i "
                    + "JOIN FETCH i.idEtudiant JOIN FETCH i.idGroupe JOIN FETCH i.idAnnee "
                    + "WHERE i.idInscription = :enrollmentId",
                    InscriptionEtudiant.class)
                    .setParameter("enrollmentId", enrollmentId)
                    .setMaxResults(1)
                    .getResultList();
            if (enrollments.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "L’inscription demandée n’existe pas.");
                return;
            }
            InscriptionEtudiant enrollment = enrollments.get(0);
            List<Semestre> semesters = em.createQuery(
                    "SELECT s FROM Semestre s JOIN FETCH s.idAnnee "
                    + "WHERE s.idSemestre = :semesterId",
                    Semestre.class)
                    .setParameter("semesterId", semesterId)
                    .setMaxResults(1)
                    .getResultList();
            if (semesters.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Le semestre demandé n’existe pas.");
                return;
            }
            Semestre semester = semesters.get(0);
            if (!enrollment.getIdAnnee().getIdAnnee().equals(semester.getIdAnnee().getIdAnnee())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Le semestre ne correspond pas à l’année de l’inscription.");
                return;
            }

            SemesterGradeCalculator.StudentResult result = new SemesterGradeService(em)
                    .calculateEnrollment(enrollment, semesterId, enrollment.getIdAnnee().getIdAnnee());
            List<ReleveReportRow> rows = reportRows(result);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("studentName", enrollment.getIdEtudiant().getNom()
                    + " " + enrollment.getIdEtudiant().getPrenom());
            parameters.put("studentMatricule", enrollment.getIdEtudiant().getMatricule());
            parameters.put("groupCode", enrollment.getIdGroupe().getCode());
            parameters.put("academicYear", enrollment.getIdAnnee().getLibelle());
            parameters.put("semesterName", semester.getCode() + " — " + semester.getLibelle());
            parameters.put("semesterAverage", SemesterGradeCalculator.formatForDisplay(result.semesterAverage()));
            parameters.put("issuedAt", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE).format(new Date()));
            parameters.put(JRParameter.REPORT_LOCALE, Locale.FRANCE);

            JasperPrint print = JasperFillManager.fillReport(
                    compiledReport, parameters, new JRBeanCollectionDataSource(rows));
            byte[] pdf;
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                JasperExportManager.exportReportToPdfStream(print, output);
                pdf = output.toByteArray();
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"releve_" + safeFilename(enrollment.getIdEtudiant().getMatricule())
                    + "_" + safeFilename(semester.getCode()) + ".pdf\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
        } catch (PersistenceException ex) {
            getServletContext().log("Unable to load transcript source data.", ex);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } catch (JRException ex) {
            getServletContext().log("Unable to generate the Jasper transcript PDF.", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "La génération du relevé PDF a échoué.");
        } catch (IllegalStateException ex) {
            getServletContext().log("Invalid academic data prevented transcript calculation.", ex);
            response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
        }
    }

    private static List<ReleveReportRow> reportRows(SemesterGradeCalculator.StudentResult result) {
        List<ReleveReportRow> rows = new ArrayList<>();
        for (SemesterGradeCalculator.UeResult ue : result.ueResults()) {
            for (SemesterGradeCalculator.EcResult ec : ue.ecResults()) {
                rows.add(new ReleveReportRow(
                        ue.code(),
                        ue.label(),
                        ec.code(),
                        ec.label(),
                        SemesterGradeCalculator.formatForDisplay(ec.coefficient()),
                        SemesterGradeCalculator.formatForDisplay(ec.average()),
                        SemesterGradeCalculator.formatForDisplay(ue.average())));
            }
        }
        if (rows.isEmpty()) {
            rows.add(new ReleveReportRow(
                    "", "Aucune UE ou composition n’est rattachée à ce semestre.",
                    "", "", "", "Non calculée", "Non calculée"));
        }
        return rows;
    }

    private static Long requiredId(String value, String entityName) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("L’identifiant du " + entityName + " est invalide.");
        }
    }

    private static String safeFilename(String value) {
        return value.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
