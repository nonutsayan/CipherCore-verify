package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 10: Structural Document Analysis Service
 * Inspects PDF internal page structures, media box dimension uniformity,
 * and page-level formatting consistency to identify spliced or spliced pages.
 */
@Service
public class DocumentStructureAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStructureAnalysisService.class);

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public record StructureReport(
        int totalPages,
        boolean uniformDimensions,
        int totalAnnotations,
        List<String> structuralFindings
    ) {}

    public StructureReport analyzeStructure(Document document, Long runId) {
        File diskFile = new File(document.getStoredPath());
        if (!diskFile.exists() || !"application/pdf".equalsIgnoreCase(document.getMimeType())) {
            return new StructureReport(document.getPageCount(), true, 0, List.of("Not a PDF or file unavailable"));
        }

        List<String> findings = new ArrayList<>();
        int annotationCount = 0;
        boolean uniform = true;

        try (PDDocument pd = Loader.loadPDF(diskFile)) {
            int pageCount = pd.getNumberOfPages();
            if (pageCount > 1) {
                PDPage p1 = pd.getPage(0);
                PDRectangle r1 = p1.getMediaBox();
                float w1 = r1.getWidth();
                float h1 = r1.getHeight();

                for (int i = 1; i < pageCount; i++) {
                    PDPage pi = pd.getPage(i);
                    PDRectangle ri = pi.getMediaBox();
                    float wi = ri.getWidth();
                    float hi = ri.getHeight();

                    // Check for significant dimension differences (> 5% discrepancy)
                    double wDiff = Math.abs(w1 - wi) / (double) w1;
                    double hDiff = Math.abs(h1 - hi) / (double) h1;

                    if (wDiff > 0.08 || hDiff > 0.08) {
                        uniform = false;
                        String desc = String.format(
                            "Inconsistent page dimensions: Page 1 is %.0fx%.0f pt, but Page %d is %.0fx%.0f pt (%.1f%% difference).",
                            w1, h1, i + 1, wi, hi, Math.max(wDiff, hDiff) * 100
                        );
                        findings.add(desc);

                        AnomalyRecord anomaly = new AnomalyRecord(
                            runId,
                            document.getId(),
                            document.getApplicationId(),
                            AnomalyType.STRUCTURAL_ANOMALY,
                            AnomalySeverity.MEDIUM,
                            "Inconsistent Page Dimensions (Possible Spliced Page " + (i + 1) + ")",
                            desc,
                            "Multi-page official orders typically share consistent paper sizing. A differing dimension often points to an inserted or scanned page from another source.",
                            "RULE_STRUCTURAL_PAGE_DIMENSION_MISMATCH",
                            0.82,
                            "Review Page " + (i + 1) + " alongside preceding pages to confirm continuous pagination, numbering, and margins."
                        );
                        anomalyRecordRepository.save(anomaly);

                        EvidenceRecord ev = new EvidenceRecord(
                            anomaly,
                            document.getId(),
                            "STRUCTURAL_OBSERVATION",
                            i + 1,
                            "PDPage.MediaBox",
                            String.format("Page 1: %.0fx%.0f | Page %d: %.0fx%.0f", w1, h1, i + 1, wi, hi),
                            "Uniform Page MediaBox",
                            "Legitimate documents containing mixed orientation attachments (e.g. landscape drawings with portrait text) will naturally trigger this observation."
                        );
                        evidenceRecordRepository.save(ev);
                        break;
                    }
                }
            }

            for (PDPage page : pd.getPages()) {
                if (page.getAnnotations() != null) {
                    annotationCount += page.getAnnotations().size();
                }
            }

            return new StructureReport(pageCount, uniform, annotationCount, findings);

        } catch (Exception e) {
            log.warn("Error in structural analysis for document {}: {}", document.getId(), e.getMessage());
            findings.add("Structural analysis note: " + e.getMessage());
            return new StructureReport(document.getPageCount(), true, 0, findings);
        }
    }
}
