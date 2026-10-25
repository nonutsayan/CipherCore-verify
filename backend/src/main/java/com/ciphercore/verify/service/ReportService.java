package com.ciphercore.verify.service;

import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.ValidationFinding;
import com.ciphercore.verify.model.ValidationRun;
import com.ciphercore.verify.repository.DocumentRepository;
import com.ciphercore.verify.repository.ValidationFindingRepository;
import com.ciphercore.verify.repository.ValidationRunRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Section 10 & 12: Server-side PDF and JSON Validation Report Generation.
 */
@Service
public class ReportService {

    private final ValidationRunRepository runRepository;
    private final ValidationFindingRepository findingRepository;
    private final DocumentRepository documentRepository;

    public ReportService(ValidationRunRepository runRepository,
                         ValidationFindingRepository findingRepository,
                         DocumentRepository documentRepository) {
        this.runRepository = runRepository;
        this.findingRepository = findingRepository;
        this.documentRepository = documentRepository;
    }

    public Map<String, Object> generateJsonReport(Long runId) {
        ValidationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Validation run not found: " + runId));
        Application app = run.getApplication();
        List<ValidationFinding> findings = findingRepository.findByValidationRunId(runId);
        List<Document> documents = documentRepository.findByApplicationId(app.getId());

        Map<String, Object> report = new LinkedHashMap<>();

        // 1. Report Metadata
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("reportId", "CR-REP-" + run.getId() + "-" + run.getReportHash().substring(0, 8));
        meta.put("applicationReference", app.getApplicationRef());
        meta.put("generationTimestamp", run.getRunTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        meta.put("validationRunId", run.getId());
        meta.put("applicationCategory", app.getCategoryCode());
        meta.put("rulesetVersion", run.getRulesetVersion());
        meta.put("processingMode", "DETERMINISTIC_PLUS_ASSISTIVE_AI");
        meta.put("referenceDataVerificationMode", "AUTHORIZED_ADAPTER_VERIFIED");
        report.put("metadata", meta);

        // 2. Overall Result
        Map<String, Object> overall = new LinkedHashMap<>();
        overall.put("status", run.getOverallStatus().name());
        overall.put("summaryExplanation", run.getOverallExplanation());
        overall.put("passedCount", run.getPassedCount());
        overall.put("warningCount", run.getWarningCount());
        overall.put("deficiencyCount", run.getDeficiencyCount());
        overall.put("manualReviewCount", run.getManualReviewCount());
        overall.put("totalChecksCount", run.getTotalChecksCount());
        overall.put("reportIntegrityHash", run.getReportHash());
        report.put("overallResult", overall);

        // 3. Application Summary
        Map<String, Object> appSummary = new LinkedHashMap<>();
        appSummary.put("institutionName", app.getInstitutionName());
        appSummary.put("instituteCode", app.getInstituteCode());
        appSummary.put("region", app.getRegion());
        appSummary.put("academicYear", app.getAcademicYear());
        appSummary.put("submissionDate", app.getSubmissionDate() != null ? app.getSubmissionDate().toString() : "DRAFT_PENDING");
        report.put("applicationSummary", appSummary);

        // 4. Documents Assessment
        List<Map<String, Object>> docsList = new ArrayList<>();
        for (Document d : documents) {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("documentId", d.getId());
            dm.put("documentType", d.getDocumentType());
            dm.put("filename", d.getOriginalFilename());
            dm.put("sha256Checksum", d.getSha256Checksum());
            dm.put("pageCount", d.getPageCount());
            dm.put("extractionStatus", d.getExtractionStatus());
            docsList.add(dm);
        }
        report.put("documentsAssessment", docsList);

        // 5. Findings
        List<Map<String, Object>> findingsList = new ArrayList<>();
        for (ValidationFinding f : findings) {
            Map<String, Object> fm = new LinkedHashMap<>();
            fm.put("findingId", f.getId());
            fm.put("ruleId", f.getRuleId());
            fm.put("ruleName", f.getRuleName());
            fm.put("status", f.getStatus().name());
            fm.put("severity", f.getSeverity().name());
            fm.put("affectedFieldOrDoc", f.getAffectedField() != null ? f.getAffectedField() : f.getAffectedDocumentType());
            fm.put("expectedValue", f.getExpectedValue());
            fm.put("observedValue", f.getObservedValue());
            fm.put("explanation", f.getExplanation());
            fm.put("recommendedAction", f.getRecommendedAction());
            fm.put("evidenceReference", f.getEvidenceReference());
            findingsList.add(fm);
        }
        report.put("findings", findingsList);

        // 6. Disclaimer
        report.put("disclaimer", "The report is a decision-support and verification artifact generated by CipherCore Verify. Automated findings do not constitute final statutory approval or legal finding of fraud. Final administrative decisions remain with the Competent AICTE Scrutiny Committee.");

        return report;
    }

    public byte[] generatePdfReport(Long runId) {
        ValidationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("Validation run not found: " + runId));
        Application app = run.getApplication();
        List<ValidationFinding> findings = findingRepository.findByValidationRunId(runId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(pdfDoc, out);
            pdfDoc.open();

            // Header Font
            com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);
            com.lowagie.text.Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(50, 70, 95));
            com.lowagie.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            com.lowagie.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

            Paragraph pTitle = new Paragraph("ALL INDIA COUNCIL FOR TECHNICAL EDUCATION", titleFont);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            pdfDoc.add(pTitle);

            Paragraph pSub = new Paragraph("APPLICATION VALIDATION & SCRUTINY COMMITTEE REPORT", subTitleFont);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingAfter(10);
            pdfDoc.add(pSub);

            // Metadata Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(12);

            addMetaCell(metaTable, "Application Ref:", app.getApplicationRef(), boldFont, normalFont);
            addMetaCell(metaTable, "Ruleset Version:", run.getRulesetVersion(), boldFont, normalFont);
            addMetaCell(metaTable, "Institution Name:", app.getInstitutionName(), boldFont, normalFont);
            addMetaCell(metaTable, "Validation Timestamp:", run.getRunTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), boldFont, normalFont);
            addMetaCell(metaTable, "Institute Code:", app.getInstituteCode(), boldFont, normalFont);
            addMetaCell(metaTable, "Overall Status:", run.getOverallStatus().name(), boldFont, normalFont);
            pdfDoc.add(metaTable);

            // Findings Table
            Paragraph pFindings = new Paragraph("Detailed Rule Findings & Evidence", subTitleFont);
            pFindings.setSpacingAfter(6);
            pdfDoc.add(pFindings);

            PdfPTable table = new PdfPTable(new float[]{1.5f, 3.5f, 1.2f, 4.0f});
            table.setWidthPercentage(100);
            table.setSpacingAfter(14);

            addHeaderCell(table, "Rule ID", boldFont);
            addHeaderCell(table, "Requirement Name", boldFont);
            addHeaderCell(table, "Status", boldFont);
            addHeaderCell(table, "Explanation & Evidence", boldFont);

            for (ValidationFinding f : findings) {
                table.addCell(new Phrase(f.getRuleId(), normalFont));
                table.addCell(new Phrase(f.getRuleName(), normalFont));

                PdfPCell statusCell = new PdfPCell(new Phrase(f.getStatus().name(), boldFont));
                if (f.getStatus().name().equals("PASS")) statusCell.setBackgroundColor(new Color(220, 252, 231));
                else if (f.getStatus().name().equals("DEFICIENCY")) statusCell.setBackgroundColor(new Color(254, 226, 226));
                else if (f.getStatus().name().equals("WARNING")) statusCell.setBackgroundColor(new Color(254, 243, 199));
                else statusCell.setBackgroundColor(new Color(224, 242, 254));
                table.addCell(statusCell);

                table.addCell(new Phrase(f.getExplanation() + "\n[Action: " + (f.getRecommendedAction() != null ? f.getRecommendedAction() : "None") + "]", normalFont));
            }
            pdfDoc.add(table);

            // Disclaimer
            Paragraph pDisc = new Paragraph("DISCLAIMER: Decision-support artifact generated by CipherCore Verify (SIH 2026). SHA-256 Seal: " + run.getReportHash(), normalFont);
            pDisc.setSpacingBefore(10);
            pdfDoc.add(pDisc);

            pdfDoc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error rendering PDF report", e);
        }
    }

    private void addMetaCell(PdfPTable table, String label, String value, com.lowagie.text.Font bf, com.lowagie.text.Font nf) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", bf));
        p.add(new Chunk(value != null ? value : "", nf));
        c.addElement(p);
        table.addCell(c);
    }

    private void addHeaderCell(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(241, 245, 249));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
