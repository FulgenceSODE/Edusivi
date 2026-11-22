package com.mycompany.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPrintText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;

class JasperTranscriptTemplateTest {

    @Test
    void compilesFillsAndExportsTranscriptAsPdf() throws Exception {
        JasperReport report;
        try (InputStream template = getClass().getClassLoader()
                .getResourceAsStream("reports/releve_notes.jrxml")) {
            report = JasperCompileManager.compileReport(template);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("studentName", "Étudiant Test");
        parameters.put("studentMatricule", "TEST-001");
        parameters.put("groupCode", "G1");
        parameters.put("academicYear", "Année Test");
        parameters.put("semesterName", "S1 — Semestre 1");
        parameters.put("semesterAverage", "14,25");
        parameters.put("issuedAt", "01/01/2026 10:00");
        parameters.put(JRParameter.REPORT_LOCALE, Locale.FRANCE);

        JasperPrint print = JasperFillManager.fillReport(
                report,
                parameters,
                new JRBeanCollectionDataSource(List.of(new ReleveReportRow(
                        "UE-1", "Unité test", "EC-1", "Élément test",
                        "2,00", "14,25", "14,25"))));
        String renderedText = print.getPages().stream()
                .flatMap(page -> page.getElements().stream())
                .filter(JRPrintText.class::isInstance)
                .map(JRPrintText.class::cast)
                .map(JRPrintText::getFullText)
                .collect(Collectors.joining(" "));
        assertTrue(renderedText.contains("Unité test"));
        assertTrue(renderedText.contains("Élément test"));
        assertFalse(renderedText.contains("UE-1"));
        assertFalse(renderedText.contains("EC-1"));
        assertFalse(renderedText.contains("moyenne arithmétique"));

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(print, pdf);

        assertTrue(new String(pdf.toByteArray(), 0, 4, java.nio.charset.StandardCharsets.US_ASCII)
                .startsWith("%PDF"));
    }
}
