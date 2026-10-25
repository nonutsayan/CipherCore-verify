package com.ciphercore.verify;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class DocumentAuthenticationTests {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ExtractedDocumentFieldRepository fieldRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private DuplicateMatchRepository duplicateMatchRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private DocumentIntegrityService integrityService;

    @Autowired
    private DuplicateDetectionService duplicateService;

    @Autowired
    private NearDuplicateDetectionService nearDuplicateService;

    @Autowired
    private IdentityConsistencyService identityService;

    @Autowired
    private DateConsistencyService dateConsistencyService;

    @Autowired
    private CrossDocumentAnalysisService crossDocumentService;

    @Autowired
    private ReferenceVerificationService referenceService;

    @Autowired
    private ReviewWorkflowService reviewWorkflowService;

    @Autowired
    private AnomalyDetectionOrchestrator orchestrator;

    @Autowired
    private AnomalyReportService reportService;

    private Application testApp;
    private Document testDoc1;
    private Document testDoc2;

    @BeforeEach
    public void setup() {
        testApp = new Application();
        testApp.setApplicationRef("TEST-APP-2026-001");
        testApp.setInstitutionName("Apex Institute of Technology");
        testApp.setInstituteCode("1-441829");
        testApp.setCategoryCode("EOA_TECHNICAL");
        testApp.setCreatedBy("applicant_apex");
        testApp.setStatus(ApplicationStatus.SUBMITTED);
        testApp = applicationRepository.save(testApp);

        testDoc1 = new Document();
        testDoc1.setApplication(testApp);
        testDoc1.setOriginalFilename("test_land_certificate.pdf");
        testDoc1.setStoredPath("./uploads/test_report.pdf");
        testDoc1.setMimeType("application/pdf");
        testDoc1.setSha256Checksum("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        testDoc1.setFileSizeBytes(2680);
        testDoc1.setPageCount(1);
        testDoc1.setExtractedText("Apex Institute of Technology Land Title Certificate Survey 142/A Area 10.4 Acres Approved 2024");
        testDoc1 = documentRepository.save(testDoc1);

        testDoc2 = new Document();
        testDoc2.setApplication(testApp);
        testDoc2.setOriginalFilename("test_fire_noc.pdf");
        testDoc2.setStoredPath("./uploads/test_report.pdf");
        testDoc2.setMimeType("application/pdf");
        testDoc2.setSha256Checksum("c3ab8ff13720e8ad9047dd39466b3c8974e592c2fa383d4a3960714caef0c4f2");
        testDoc2.setFileSizeBytes(2680);
        testDoc2.setPageCount(1);
        testDoc2.setExtractedText("Directorate of Fire Services NOC Ref FS/2023/9812 Apex Institute of Technology Valid till 2026");
        testDoc2 = documentRepository.save(testDoc2);
    }

    @Test
    @DisplayName("Test 1: SHA-256 Hash and File Integrity Verification")
    public void testFileIntegrityCheck() {
        DocumentIntegrityService.IntegrityResult res = integrityService.inspectAndRecord(testDoc1);
        assertTrue(res.isValid(), "File integrity should be valid");
        assertNotNull(res.sha256(), "SHA-256 must be computed");
        assertEquals(64, res.sha256().length(), "SHA-256 must be 64 hexadecimal characters");
    }

    @Test
    @DisplayName("Test 2: Exact Duplicate Detection across Submissions")
    public void testExactDuplicateDetection() {
        // Create duplicate with identical SHA-256 but different filename and application
        Document dupDoc = new Document();
        dupDoc.setOriginalFilename("recycled_certificate_copy.pdf");
        dupDoc.setStoredPath("./uploads/test_report.pdf");
        dupDoc.setMimeType("application/pdf");
        dupDoc.setSha256Checksum(testDoc1.getSha256Checksum()); // Identical hash
        dupDoc.setFileSizeBytes(2680);
        dupDoc = documentRepository.save(dupDoc);

        List<DuplicateMatch> matches = duplicateService.detectExactDuplicates(dupDoc, 100L);
        assertFalse(matches.isEmpty(), "Should detect exact duplicate match");
        assertEquals("EXACT_DUPLICATE_DIFFERENT_APPLICATION", matches.get(0).getMatchType());
        assertEquals(1.0, matches.get(0).getSimilarityScore(), 0.001);
    }

    @Test
    @DisplayName("Test 3: Near-Duplicate Document Detection via Token Overlap")
    public void testNearDuplicateDetection() {
        Document nearDoc = new Document();
        nearDoc.setOriginalFilename("revised_land_title_v2.pdf");
        nearDoc.setStoredPath("./uploads/test_report.pdf");
        nearDoc.setMimeType("application/pdf");
        nearDoc.setSha256Checksum("different_hash_for_revised_file_1234567890abcdef");
        nearDoc.setFileSizeBytes(2700);
        // Similar text with one minor variation
        nearDoc.setExtractedText("Apex Institute of Technology Land Title Certificate Survey 142/A Area 10.4 Acres Approved 2025 Renewal");
        nearDoc = documentRepository.save(nearDoc);

        List<DuplicateMatch> nearMatches = nearDuplicateService.detectNearDuplicates(nearDoc, 101L);
        assertFalse(nearMatches.isEmpty(), "Should identify near-duplicate text similarity");
        assertTrue(nearMatches.get(0).getSimilarityScore() >= 0.78, "Similarity should exceed threshold");
    }

    @Test
    @DisplayName("Test 4: Identity & Entity Name Consistency Validation")
    public void testIdentityConsistency() {
        // Test distinct divergent name
        List<AnomalyRecord> anomalies = identityService.verifyIdentityConsistency(
            testDoc1,
            "Global Sunshine Educational Society of Arts", // Divergent name
            "1-441829",
            102L
        );
        assertFalse(anomalies.isEmpty(), "Should detect institution name discrepancy");
        assertEquals(AnomalyType.INSTITUTION_NAME_MISMATCH, anomalies.get(0).getAnomalyType());
        assertEquals(AnomalySeverity.CRITICAL, anomalies.get(0).getSeverity());
    }

    @Test
    @DisplayName("Test 5: Temporal Inversion Check (Issue Date > Expiry Date)")
    public void testDateInversionAnomaly() {
        List<AnomalyRecord> anomalies = dateConsistencyService.verifyTemporalConsistency(
            testDoc1,
            "2026-06-30", // Issue date in June 2026
            "2024-01-01", // Expiry date in January 2024 (Prior to issue!)
            103L
        );
        assertFalse(anomalies.isEmpty(), "Should flag inverted chronological validity");
        assertEquals(AnomalyType.DATE_INCONSISTENCY, anomalies.get(0).getAnomalyType());
        assertEquals(AnomalySeverity.CRITICAL, anomalies.get(0).getSeverity());
    }

    @Test
    @DisplayName("Test 6: Cross-Document Contradiction Analysis across Application Dossier")
    public void testCrossDocumentDossierContradictions() {
        // Save conflicting field: Doc 1 specifies "Apex Institute of Technology", Doc 2 specifies "Sunrise Trust of Commerce"
        ExtractedDocumentField f1 = new ExtractedDocumentField(testDoc1, "institutionName", "Apex Institute of Technology", "Apex Institute of Technology", FieldExtractionStatus.EXTRACTED, "Apex header", 0.95, 1, "REGEX");
        fieldRepository.save(f1);

        ExtractedDocumentField f2 = new ExtractedDocumentField(testDoc2, "institutionName", "Sunrise Trust of Commerce", "Sunrise Trust of Commerce", FieldExtractionStatus.EXTRACTED, "Sunrise header", 0.95, 1, "REGEX");
        fieldRepository.save(f2);

        List<CrossDocumentComparison> comps = crossDocumentService.analyzeApplicationDossier(testApp.getId(), 104L);
        assertFalse(comps.isEmpty(), "Should run cross-document comparison");
        assertEquals("CONFLICT_DETECTED", comps.get(0).getComparisonStatus());
        assertTrue(comps.get(0).getConflictCount() > 0, "Should count name conflict");
    }

    @Test
    @DisplayName("Test 7: Reference Registry Verification against Mock Central Master")
    public void testReferenceRegistryVerification() {
        VerificationRecord rec = referenceService.verifyAgainstRegistry(
            testDoc1,
            "1-441829",
            "Apex Institute of Technology",
            105L
        );
        assertNotNull(rec);
        assertEquals(VerificationStatus.VERIFIED_MATCH, rec.getVerificationStatus());
        assertTrue(rec.getComparisonEvidence().contains("AICTE/MOCK/REG/2024-98104"));
    }

    @Test
    @DisplayName("Test 8: Human Review Disposition and Audit Trail Logging")
    public void testHumanReviewWorkflow() {
        AnomalyRecord anomaly = new AnomalyRecord(
            106L,
            testDoc1.getId(),
            testApp.getId(),
            AnomalyType.IDENTIFIER_MISMATCH,
            AnomalySeverity.MEDIUM,
            "Minor Code Discrepancy",
            "Test description",
            "Why it matters",
            "RULE_TEST",
            0.85,
            "Verify with applicant"
        );
        anomaly = anomalyRecordRepository.save(anomaly);

        AnomalyRecord reviewed = reviewWorkflowService.recordReviewDecision(
            anomaly.getId(),
            "officer_sc401",
            "CONFIRM_LEGITIMATE",
            "Verified against physical office files; difference reflects municipal ward restructuring."
        );
        assertEquals(AnomalyReviewStatus.RESOLVED_LEGITIMATE_DIFFERENCE, reviewed.getReviewStatus());

        List<AuditEvent> auditLogs = auditEventRepository.findAll();
        assertFalse(auditLogs.isEmpty());
        assertTrue(auditLogs.stream().anyMatch(a -> a.getAction().equals("ANOMALY_REVIEW_DECISION")));
    }

    @Test
    @DisplayName("Test 9: End-to-End Orchestrated Forensic Integrity Report Assembly")
    public void testEndToEndReportAssembly() {
        DocumentAnalysisRun run = orchestrator.runComprehensiveAnalysis(testDoc1.getId());
        assertNotNull(run);
        assertEquals("COMPLETED", run.getProcessingStatus());

        Map<String, Object> report = reportService.generateIntegrityReport(testDoc1);
        assertNotNull(report.get("reportId"));
        assertNotNull(report.get("fileIntegrity"));
        assertNotNull(report.get("overallAssessment"));
        assertEquals("COMPLETED", report.get("analysisStatus"));
    }
}
