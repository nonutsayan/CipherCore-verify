package com.ciphercore.verify;

import com.ciphercore.verify.adapter.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class IssuingAuthorityVerificationTests {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AuthoritySourceRepository sourceRepository;

    @Autowired
    private VerificationResultRepository resultRepository;

    @Autowired
    private VerificationJobRepository jobRepository;

    @Autowired
    private VerificationReviewRepository reviewRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuthoritySourceRegistryService registryService;

    @Autowired
    private VerificationOrchestratorService orchestratorService;

    @Autowired
    private FieldComparisonService comparisonService;

    @Autowired
    private SourceTrustService trustService;

    @Autowired
    private ValidityAssessmentService validityService;

    @Autowired
    private VerificationReportService reportService;

    @Autowired
    private VerificationReviewService reviewService;

    private Document testDoc;
    private Application testApp;

    @BeforeEach
    void setUp() {
        testApp = new Application();
        testApp.setApplicationRef("AICTE-TEST-" + System.currentTimeMillis());
        testApp.setInstitutionName("Apex Institute of Technology");
        testApp.setInstituteCode("1-3324501");
        testApp.setCategoryCode("EOA_TECHNICAL");
        testApp.setAcademicYear("2025-2026");
        testApp.setCreatedBy("test-user");
        testApp = applicationRepository.save(testApp);

        testDoc = new Document();
        testDoc.setApplication(testApp);
        testDoc.setDocumentType("APPROVAL_LETTER");
        testDoc.setDocumentCategory(DocumentCategory.APPROVAL_LETTER);
        testDoc.setOriginalFilename("AICTE_EoA_2025_Apex.pdf");
        testDoc.setStoredPath("/tmp/AICTE_EoA_2025_Apex.pdf");
        testDoc.setSha256Checksum("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        testDoc.setPageCount(3);
        testDoc = documentRepository.save(testDoc);
    }

    @Test
    @DisplayName("Section 9: Corroborate institutional approval in AICTE Central Registry")
    void testAicteCentralRegistryVerification_Success() {
        List<VerificationResult> results = orchestratorService.verifyDocument(testDoc.getId(), "SRC_AICTE_REGISTRY");
        assertNotNull(results);
        assertFalse(results.isEmpty());

        VerificationResult res = results.get(0);
        assertEquals("SRC_AICTE_REGISTRY", res.getSourceId());
        assertEquals(AuthorityVerificationStatus.VERIFIED, res.getOverallStatus());
        assertEquals("ACTIVE_VALID", res.getValidityStatus());
        assertNotNull(res.getSourceRecordId());

        List<FieldComparison> comparisons = orchestratorService.getComparisonsForResult(res.getId());
        assertFalse(comparisons.isEmpty());
        assertTrue(comparisons.stream().anyMatch(c -> (c.getFieldName().equals("instituteCode") || c.getFieldName().equals("permanentInstituteId")) && c.getComparisonStatus() == FieldComparisonStatus.MATCH));
    }

    @Test
    @DisplayName("Section 9 & 15: State Fire NOC detection of expired statutory validity")
    void testStateFireNoc_DetectsExpired() {
        Document fireDoc = new Document();
        fireDoc.setApplication(testApp);
        fireDoc.setDocumentType("FIRE_NOC");
        fireDoc.setDocumentCategory(DocumentCategory.NO_OBJECTION_CERTIFICATE);
        fireDoc.setOriginalFilename("fire_noc_expired_2022.pdf");
        fireDoc.setStoredPath("/tmp/fire_noc_expired_2022.pdf");
        fireDoc.setSha256Checksum("112233445566778899aabbccddeeff00112233445566778899aabbccddeeff00");
        fireDoc = documentRepository.save(fireDoc);

        List<VerificationResult> results = orchestratorService.verifyDocument(fireDoc.getId(), "SRC_MAHA_FIRE");
        assertNotNull(results);
        assertFalse(results.isEmpty());

        VerificationResult res = results.get(0);
        assertEquals(AuthorityVerificationStatus.EXPIRED, res.getOverallStatus());
        assertEquals("EXPIRED", res.getValidityStatus());
        assertTrue(res.isManualReviewRequired());
    }

    @Test
    @DisplayName("Section 9 & 10: University Affiliation detects intake discrepancy")
    void testMumbaiUniversityAffiliation_DetectsIntakeMismatch() {
        Document affilDoc = new Document();
        affilDoc.setApplication(testApp);
        affilDoc.setDocumentType("AFFILIATION_CERTIFICATE");
        affilDoc.setDocumentCategory(DocumentCategory.AFFILIATION_CERTIFICATE);
        affilDoc.setOriginalFilename("mu_affiliation_horizon.pdf");
        affilDoc.setStoredPath("/tmp/mu_affiliation_horizon.pdf");
        affilDoc.setSha256Checksum("aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899");
        affilDoc = documentRepository.save(affilDoc);

        List<VerificationResult> results = orchestratorService.verifyDocument(affilDoc.getId(), "SRC_MU_AFFILIATION");
        assertNotNull(results);
        assertFalse(results.isEmpty());

        VerificationResult res = results.get(0);
        assertEquals(AuthorityVerificationStatus.MISMATCH, res.getOverallStatus());
        assertTrue(res.getSummary().contains("mismatch") || res.getSummary().contains("seats"));
    }

    @Test
    @DisplayName("Section 11 & 18: SSRF Security Engine blocks loopback, private IPs and unapproved domains")
    void testSsrfProtection_BlocksPrivateIpAndUntrustedDomains() {
        AuthoritySource source = new AuthoritySource();
        source.setApprovedDomains("aicte-india.org,gov.in");

        // 1. Loopback blocked
        assertFalse(trustService.isUrlPermitted("http://127.0.0.1:8080/internal-api", source));
        assertFalse(trustService.isUrlPermitted("http://localhost:8080/actuator", source));

        // 2. Private RFC-1918 IPs blocked
        assertFalse(trustService.isUrlPermitted("http://192.168.1.100/admin", source));
        assertFalse(trustService.isUrlPermitted("http://10.0.0.5/secrets", source));
        assertFalse(trustService.isUrlPermitted("http://172.16.0.1/status", source));

        // 3. AWS / Cloud metadata link-local blocked
        assertFalse(trustService.isUrlPermitted("http://169.254.169.254/latest/meta-data/", source));

        // 4. Untrusted domain blocked
        assertFalse(trustService.isUrlPermitted("https://malicious-attacker.com/query", source));

        // 5. Approved domain permitted
        assertTrue(trustService.isUrlPermitted("https://api.aicte-india.org/registry/v2", source));
        assertTrue(trustService.isUrlPermitted("https://mahafire.gov.in/noc-verify", source));
    }

    @Test
    @DisplayName("Section 10: Multi-method Field Comparison normalizes text and compares dates")
    void testFieldComparison_Exact_Normalized_Contextual_Date() {
        // Exact
        FieldComparisonService.ComparisonResult r1 = comparisonService.compareField("instituteCode", "1-3324501", "1-3324501", ComparisonMethod.EXACT);
        assertEquals(FieldComparisonStatus.MATCH, r1.status());

        // Normalized Text (ignoring Pvt Ltd, whitespace, case)
        FieldComparisonService.ComparisonResult r2 = comparisonService.compareField(
            "trustName",
            "Apex Educational Trust Pvt. Ltd.",
            "apex educational trust",
            ComparisonMethod.NORMALIZED_TEXT
        );
        assertEquals(FieldComparisonStatus.MATCH_AFTER_NORMALIZATION, r2.status());

        // Date match across format representations
        FieldComparisonService.ComparisonResult r3 = comparisonService.compareField(
            "validityDate",
            "2026-12-31",
            "31-12-2026",
            ComparisonMethod.DATE_CHRONOLOGY
        );
        assertEquals(FieldComparisonStatus.MATCH_AFTER_NORMALIZATION, r3.status());

        // Date mismatch (discrepancy between submitted and authority)
        FieldComparisonService.ComparisonResult r4 = comparisonService.compareField(
            "expiryDate",
            "2026-12-31",
            "2023-01-01",
            ComparisonMethod.DATE_CHRONOLOGY
        );
        assertEquals(FieldComparisonStatus.MISMATCH, r4.status());
    }

    @Test
    @DisplayName("Section 15: Statutory validity assessment calculates remaining days and flags expired letters")
    void testValidityAssessment_ActiveExpiringAndExpired() {
        // Expired date
        ValidityAssessmentService.ValidityEvaluation evExpired = validityService.evaluateValidity("2021-01-01", "2023-06-30", "2023-24");
        assertEquals("EXPIRED", evExpired.validityCode());
        assertFalse(evExpired.isValid());

        // Active future date
        ValidityAssessmentService.ValidityEvaluation evValid = validityService.evaluateValidity("2024-01-01", "2028-12-31", "2025-26");
        assertEquals("CURRENTLY_VALID", evValid.validityCode());
        assertTrue(evValid.isValid());
        assertTrue(evValid.daysRemaining() > 300);

        // Perpetual permanent approval
        ValidityAssessmentService.ValidityEvaluation evPermanent = validityService.evaluateValidity("2020-01-01", "PERMANENT", "PERMANENT");
        assertEquals("PERMANENT_VALIDITY", evPermanent.validityCode());
        assertTrue(evPermanent.isValid());
    }

    @Test
    @DisplayName("Section 7 & 8: Authority Registry health checks and gateway latency verification")
    void testAuthorityRegistry_HealthCheckAndStatus() {
        HealthCheckResult activeRes = registryService.testConnection("SRC_AICTE_REGISTRY");
        assertNotNull(activeRes);
        assertTrue(activeRes.healthy());
        assertEquals(AuthoritySourceStatus.CONFIGURED_ACTIVE, activeRes.status());

        HealthCheckResult unconfiguredRes = registryService.testConnection("SRC_UNKNOWN_GATEWAY");
        assertNotNull(unconfiguredRes);
        assertFalse(unconfiguredRes.healthy());
        assertEquals(AuthoritySourceStatus.UNCONFIGURED, unconfiguredRes.status());
    }

    @Test
    @DisplayName("Section 20: Comprehensive Section 20 JSON Verification Report assembly")
    void testSection20Report_ComprehensiveStructure() {
        List<VerificationResult> results = orchestratorService.verifyDocument(testDoc.getId(), "SRC_AICTE_REGISTRY");
        assertFalse(results.isEmpty());
        Long resId = results.get(0).getId();

        Map<String, Object> report = reportService.generateSection20Report(resId);
        assertNotNull(report);
        assertEquals("2.0-SIH26261", report.get("reportSchemaVersion"));
        assertEquals("SECTION_20_ISSUING_AUTHORITY_VERIFICATION", report.get("reportType"));
        assertNotNull(report.get("verificationDimensions"));
        assertNotNull(report.get("fieldComparisons"));
        assertNotNull(report.get("supportingEvidence"));
        assertNotNull(report.get("regulatoryNotice"));
    }

    @Test
    @DisplayName("Section 17 & 21: Manual Scrutiny review disposition updates verification and logs immutable audit trail")
    void testManualReviewDisposition_UpdatesStatusAndLogsAudit() {
        List<VerificationResult> results = orchestratorService.verifyDocument(testDoc.getId(), "SRC_AICTE_REGISTRY");
        assertFalse(results.isEmpty());
        Long resId = results.get(0).getId();

        long auditCountBefore = auditEventRepository.count();

        VerificationReview review = reviewService.submitReview(
            resId,
            "officer-patil",
            "CONFIRM_LEGITIMATE",
            "Corroborated with original file under AICTE Western Regional Office dispatch log"
        );

        assertNotNull(review);
        assertEquals("CONFIRM_LEGITIMATE", review.getReviewDecision());

        VerificationResult updated = orchestratorService.getVerificationResult(resId).orElseThrow();
        assertEquals(AuthorityVerificationStatus.VERIFIED, updated.getOverallStatus());
        assertTrue(updated.getSummary().contains("officer-patil"));

        long auditCountAfter = auditEventRepository.count();
        assertEquals(auditCountBefore + 1, auditCountAfter);
    }

    @Test
    @DisplayName("Section 14 & 16: Non-digitized or unconfigured sources route to Manual Review Fallback")
    void testGracefulFallback_RoutesToManualReview() {
        List<VerificationResult> results = orchestratorService.verifyDocument(testDoc.getId(), "SRC_MANUAL_REVIEW");
        assertFalse(results.isEmpty());

        VerificationResult res = results.get(0);
        assertEquals(AuthorityVerificationStatus.MANUAL_REVIEW, res.getOverallStatus());
        assertEquals("PENDING_MANUAL_SCRUTINY", res.getRecordMatchStatus());
        assertTrue(res.isManualReviewRequired());
    }
}
