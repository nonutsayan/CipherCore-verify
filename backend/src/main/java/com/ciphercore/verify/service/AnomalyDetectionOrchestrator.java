package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Section 4 & 30: Document Anomaly Detection & Authentication Orchestrator
 * Coordinates the full multi-engine forensic pipeline across integrity, duplicates,
 * metadata, visual variance, identity, temporal chronologies, cross-document relations,
 * cryptographic signatures, and official registry lookups.
 */
@Service
public class AnomalyDetectionOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionOrchestrator.class);

    @Autowired
    private DocumentIntegrityService integrityService;

    @Autowired
    private DuplicateDetectionService duplicateService;

    @Autowired
    private NearDuplicateDetectionService nearDuplicateService;

    @Autowired
    private MetadataAnalysisService metadataService;

    @Autowired
    private DocumentStructureAnalysisService structureService;

    @Autowired
    private VisualAnomalyDetectionService visualService;

    @Autowired
    private TextAnomalyDetectionService textAnomalyService;

    @Autowired
    private IdentityConsistencyService identityService;

    @Autowired
    private DateConsistencyService dateConsistencyService;

    @Autowired
    private CrossDocumentAnalysisService crossDocumentService;

    @Autowired
    private ReferenceVerificationService referenceService;

    @Autowired
    private QRCodeVerificationService qrCodeService;

    @Autowired
    private SignatureVerificationService signatureService;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Autowired
    private AnomalyReportService anomalyReportService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentAnalysisRunRepository runRepository;

    @Autowired
    private ExtractedDocumentFieldRepository fieldRepository;

    @Transactional
    public DocumentAnalysisRun runComprehensiveAnalysis(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        DocumentAnalysisRun run = new DocumentAnalysisRun(document.getId(), document.getApplicationId());
        run = runRepository.save(run);

        try {
            log.info("Starting forensic anomaly analysis for Document #{} (Run #{})", document.getId(), run.getId());

            // 1. File Integrity & SHA-256
            integrityService.inspectAndRecord(document);

            // 2. Exact Duplicates
            duplicateService.detectExactDuplicates(document, run.getId());

            // 3. Near-Duplicate Text & Template Matching
            nearDuplicateService.detectNearDuplicates(document, run.getId());

            // 4. Metadata Inspection
            metadataService.analyzeMetadata(document, run.getId());

            // 5. PDF Object Structure Analysis
            structureService.analyzeStructure(document, run.getId());

            // 6. Visual Variance & ELA Screening
            visualService.analyzeVisuals(document, run.getId());

            // 7. Multi-Page Text Contradictions
            textAnomalyService.analyzeTextAnomalies(document, run.getId());

            // Load extracted fields for semantic identity and date checks
            List<ExtractedDocumentField> fields = fieldRepository.findByDocumentId(document.getId());
            String instName = getFieldValue(fields, "institutionName");
            String certNum = getFieldValue(fields, "certificateNumber");
            if (certNum == null) certNum = getFieldValue(fields, "registrationNumber");
            String issueDate = getFieldValue(fields, "issueDate");
            String expiryDate = getFieldValue(fields, "expiryDate");

            // 8. Identity & Identifier Matching
            identityService.verifyIdentityConsistency(document, instName, certNum, run.getId());

            // 9. Chronology & Date Inversion
            dateConsistencyService.verifyTemporalConsistency(document, issueDate, expiryDate, run.getId());

            // 10. QR Code & Barcode Verification
            qrCodeService.inspectQRCodes(document, certNum, run.getId());

            // 11. Cryptographic Digital Signature Verification
            signatureService.verifySignatures(document, run.getId());

            // 12. Authorized Institutional Registry Lookup
            referenceService.verifyAgainstRegistry(document, certNum, instName, run.getId());

            // 13. Cross-Document Dossier Contradiction Check (if part of an application)
            if (document.getApplicationId() != null) {
                crossDocumentService.analyzeApplicationDossier(document.getApplicationId(), run.getId());
            }

            // 14. Explainable Risk Prioritization & Status Assignment
            riskAssessmentService.evaluateRisk(document);

            run.setProcessingStatus("COMPLETED");
            run.setCompletedAt(LocalDateTime.now());
            return runRepository.save(run);

        } catch (Exception e) {
            log.error("Forensic analysis run #{} failed for Document #{}: {}", run.getId(), document.getId(), e.getMessage(), e);
            run.setProcessingStatus("FAILED");
            run.setErrorCode(e.getMessage());
            run.setCompletedAt(LocalDateTime.now());
            return runRepository.save(run);
        }
    }

    private String getFieldValue(List<ExtractedDocumentField> fields, String name) {
        for (ExtractedDocumentField f : fields) {
            if (name.equalsIgnoreCase(f.getFieldName())) {
                return f.getNormalizedValue() != null ? f.getNormalizedValue() : f.getRawValue();
            }
        }
        return null;
    }
}
