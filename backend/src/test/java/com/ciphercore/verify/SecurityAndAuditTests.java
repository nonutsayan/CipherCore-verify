package com.ciphercore.verify;

import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.OfficialDecision;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.FindingStatus;
import com.ciphercore.verify.model.enums.OfficialDecisionOutcome;
import com.ciphercore.verify.model.enums.UserRole;
import com.ciphercore.verify.repository.ApplicationRepository;
import com.ciphercore.verify.repository.DocumentRepository;
import com.ciphercore.verify.repository.SecurityEventRepository;
import com.ciphercore.verify.repository.UserRepository;
import com.ciphercore.verify.service.*;
import com.ciphercore.verify.service.AuditTrailService.AuditIntegrityResult;
import com.ciphercore.verify.service.AuthService.LoginResult;
import com.ciphercore.verify.service.SecureDocumentStorageService.DocumentDownloadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityAndAuditTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private AuditTrailService auditTrailService;

    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    @Autowired
    private SecureDocumentStorageService documentStorageService;

    @Autowired
    private AdverseActionGateService adverseActionGateService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User officerUser;
    private User apexApplicant;
    private User bharatApplicant;
    private User serviceAiUser;
    private Application apexApp;
    private Document apexDoc;

    @BeforeEach
    void setUp() {
        // Ensure audit chain is sealed
        auditTrailService.ensureChainSealed();

        // Seed users for tests if missing
        adminUser = userRepository.findByUsername("test_admin").orElseGet(() ->
                userRepository.save(new User("test_admin", passwordEncoder.encode("adminPass123"),
                        "Admin Test", "admin@test.org", UserRole.ROLE_SYSTEM_ADMINISTRATOR, "HQ")));

        officerUser = userRepository.findByUsername("test_officer").orElseGet(() ->
                userRepository.save(new User("test_officer", passwordEncoder.encode("officerPass123"),
                        "Officer Test", "officer@test.org", UserRole.ROLE_REGULATORY_OFFICER, "WRO")));

        apexApplicant = userRepository.findByUsername("test_apex").orElseGet(() ->
                userRepository.save(new User("test_apex", passwordEncoder.encode("apexPass123"),
                        "Apex Applicant", "apex@test.org", UserRole.ROLE_INSTITUTION_APPLICANT, "Apex Institute of Tech", "INST-APEX-01")));

        bharatApplicant = userRepository.findByUsername("test_bharat").orElseGet(() ->
                userRepository.save(new User("test_bharat", passwordEncoder.encode("bharatPass123"),
                        "Bharat Applicant", "bharat@test.org", UserRole.ROLE_INSTITUTION_APPLICANT, "Bharat Engg College", "INST-BHARAT-02")));

        serviceAiUser = userRepository.findByUsername("test_ai").orElseGet(() ->
                userRepository.save(new User("test_ai", passwordEncoder.encode("aiTokenSecret123"),
                        "AI Background Worker", "ai@test.org", UserRole.ROLE_SERVICE_AI, "Autonomous")));

        // Create test application for Apex
        Optional<Application> existingApp = applicationRepository.findAll().stream()
                .filter(a -> "Apex Institute of Tech".equalsIgnoreCase(a.getInstitutionName()))
                .findFirst();

        if (existingApp.isPresent()) {
            apexApp = existingApp.get();
        } else {
            Application a = new Application();
            a.setApplicationRef("APP-SEC-TEST-001");
            a.setInstitutionName("Apex Institute of Tech");
            a.setInstituteCode("1-987654321");
            a.setCreatedBy("test_apex");
            a.setAcademicYear("2026-2027");
            a.setCategoryCode("EOA_TECHNICAL");
            a.setOverallValidationStatus(FindingStatus.MANUAL_REVIEW);
            apexApp = applicationRepository.save(a);
        }

        // Create test document for Apex app
        Optional<Document> existingDoc = documentRepository.findAll().stream()
                .filter(d -> d.getApplication() != null && d.getApplication().getId().equals(apexApp.getId()))
                .findFirst();

        if (existingDoc.isPresent()) {
            apexDoc = existingDoc.get();
        } else {
            Document d = new Document();
            d.setApplication(apexApp);
            d.setOriginalFilename("fire_noc_apex_classified.pdf");
            d.setStoredPath("data/secure_vault/documents/fire_noc_apex_classified.pdf");
            d.setMimeType("application/pdf");
            d.setFileSizeBytes(1024L);
            d.setSha256Checksum("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
            d.setUploadedBy("test_apex");
            apexDoc = documentRepository.save(d);
        }
    }

    @Test
    @DisplayName("1. Authentication - Valid credentials succeed and return token")
    void testAuthentication_ValidCredentials_Success() {
        LoginResult res = authService.login("test_admin", "adminPass123", "127.0.0.1");
        assertTrue(res.success());
        assertNotNull(res.token());
        assertEquals("test_admin", res.user().getUsername());
        assertEquals(UserRole.ROLE_SYSTEM_ADMINISTRATOR, res.user().getRole());
    }

    @Test
    @DisplayName("2. Authentication - Invalid password fails with 401")
    void testAuthentication_InvalidCredentials_Fails() {
        LoginResult res = authService.login("test_admin", "incorrect_password", "127.0.0.1");
        assertFalse(res.success());
        assertEquals(401, res.statusCode());
        assertNull(res.token());
    }

    @Test
    @DisplayName("3. Authentication - Deactivated account is blocked with 403")
    void testAuthentication_DisabledAccount_Blocked() {
        adminUser.setActive(false);
        userRepository.save(adminUser);

        try {
            LoginResult res = authService.login("test_admin", "adminPass123", "127.0.0.1");
            assertFalse(res.success());
            assertEquals(403, res.statusCode());
            assertTrue(res.errorMessage().contains("deactivated"));
        } finally {
            adminUser.setActive(true);
            userRepository.save(adminUser);
        }
    }

    @Test
    @DisplayName("4. RBAC & IDOR - Cross-institution applicant cannot view foreign application")
    void testAccessControl_ApplicantCannotAccessOtherInstitutionApplication_IDORBlocked() {
        // Bharat applicant attempts to access Apex Institute's application
        boolean allowed = accessControlService.canAccessApplication(bharatApplicant, apexApp);
        assertFalse(allowed, "IDOR Vulnerability Detected! Bharat applicant was granted access to Apex application!");
    }

    @Test
    @DisplayName("5. RBAC - Applicant can access their own institution application")
    void testAccessControl_ApplicantCanAccessOwnApplication_Success() {
        boolean allowed = accessControlService.canAccessApplication(apexApplicant, apexApp);
        assertTrue(allowed, "Apex applicant should be allowed to access their own application");
    }

    @Test
    @DisplayName("6. RBAC - Non-admin user cannot manage users or alter roles")
    void testAccessControl_NonAdminCannotManageUsers_Forbidden() {
        assertFalse(accessControlService.canManageUsers(officerUser));
        assertFalse(accessControlService.canManageUsers(apexApplicant));
        assertTrue(accessControlService.canManageUsers(adminUser));
    }

    @Test
    @DisplayName("7. Secure Document Storage - Foreign applicant cannot download confidential document")
    void testSecureDocumentDownload_ApplicantDeniedOtherInstitutionDoc() {
        // Bharat applicant attempts to download Apex's document
        DocumentDownloadResult res = documentStorageService.retrieveDocument(apexDoc.getId(), bharatApplicant, "DOWNLOAD", "127.0.0.1");
        assertFalse(res.allowed());
        assertEquals(403, res.statusCode());
        assertTrue(res.errorMessage().contains("IDOR protection active"));
    }

    @Test
    @DisplayName("8. Audit Trail - Hash chain integrity verification succeeds on unaltered chain")
    void testAuditTrail_HashChainIntegrity_ValidChainPasses() {
        auditTrailService.recordEvent("test_officer", "REGULATORY_OFFICER", "VERIFY_INTEGRITY_TEST_1",
                "APPLICATION", String.valueOf(apexApp.getId()), "Initial integrity step", "127.0.0.1");
        auditTrailService.recordEvent("test_officer", "REGULATORY_OFFICER", "VERIFY_INTEGRITY_TEST_2",
                "APPLICATION", String.valueOf(apexApp.getId()), "Subsequent chained step", "127.0.0.1");

        AuditIntegrityResult integrity = auditTrailService.verifyAuditChainIntegrity();
        assertTrue(integrity.valid(), "Cryptographic audit chain should be valid: " + integrity.message());
        assertNull(integrity.tamperedRecordId());
        assertTrue(integrity.totalRecordsVerified() >= 2);
    }

    @Test
    @DisplayName("9. Audit Trail - Tamper detection detects modified database record immediately")
    void testAuditTrail_TamperedEvent_DetectedImmediately() {
        com.ciphercore.verify.model.AuditEvent ev = auditTrailService.recordEvent("test_officer", "REGULATORY_OFFICER", "PRE_TAMPER_ACTION",
                "DOCUMENT", String.valueOf(apexDoc.getId()), "Pristine record before tamper test", "127.0.0.1");

        long seqToTamper = ev.getSequenceNumber();

        // Simulate rogue database injection / tamper
        boolean tampered = auditTrailService.simulateTamper(seqToTamper, "MALICIOUSLY ALTERED AUDIT LOG DETAILS");
        assertTrue(tampered);

        // Verification must detect the tamper!
        AuditIntegrityResult after = auditTrailService.verifyAuditChainIntegrity();
        assertFalse(after.valid(), "Tampered record was NOT detected by cryptographic hash verification!");
        assertEquals(seqToTamper, after.tamperedSequenceNumber());
        assertTrue(after.message().contains("Data tamper detected") || after.message().contains("mismatch"));

        // Restore clean state
        auditTrailService.restoreTamper(seqToTamper, "Pristine record before tamper test");
        AuditIntegrityResult restored = auditTrailService.verifyAuditChainIntegrity();
        assertTrue(restored.valid(), "Audit chain should be restored to valid cryptographic state");
    }

    @Test
    @DisplayName("10. Human Approval Gate - Autonomous AI service cannot commit adverse regulatory action")
    void testHumanApprovalGate_AIOrUnauthorizedCannotCommitAdverseAction() {
        SecurityException ex = assertThrows(SecurityException.class, () ->
                adverseActionGateService.commitRegulatoryDecision(
                        apexApp.getId(),
                        serviceAiUser,
                        OfficialDecisionOutcome.REJECTED_REFUSAL_OF_APPROVAL,
                        "AICTE Approval Process Handbook Chapter 1 Clause 1.4: Refusal due to land title defect",
                        "None",
                        "AICTE-AI-GATE-TEST",
                        false,
                        "127.0.0.1"
                ));

        assertTrue(ex.getMessage().contains("Automated AI service") || ex.getMessage().contains("Mandatory human"));
    }

    @Test
    @DisplayName("11. Human Approval Gate - Missing or insufficient statutory rationale is blocked")
    void testHumanApprovalGate_MissingStatutoryRationale_Rejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adverseActionGateService.commitRegulatoryDecision(
                        apexApp.getId(),
                        officerUser,
                        OfficialDecisionOutcome.DEFICIENCY_NOTICE_ISSUED,
                        "Too short", // under 20 chars
                        "Submit NOC within 15 days",
                        "AICTE-SC-TEST",
                        false,
                        "127.0.0.1"
                ));

        assertTrue(ex.getMessage().contains("minimum 20 characters"));
    }

    @Test
    @DisplayName("12. Human Approval Gate - Authorized Scrutiny Officer commits adverse decision with cryptographic seal")
    void testHumanApprovalGate_ValidOfficerWithStatutoryRationale_Success() {
        OfficialDecision decision = adverseActionGateService.commitRegulatoryDecision(
                apexApp.getId(),
                officerUser,
                OfficialDecisionOutcome.DEFICIENCY_NOTICE_ISSUED,
                "AICTE Approval Process Handbook 2026-27 Chapter I Clause 1.4(d): Land title conversion order requires scrutiny clarification.",
                "Submit certified copy of Tehsildar non-agricultural conversion order within 15 days.",
                "AICTE-SC-WRO-2026/SEC-01",
                true,
                "127.0.0.1"
        );

        assertNotNull(decision);
        assertNotNull(decision.getDecisionIntegritySeal(), "SHA-256 seal must be computed");
        assertEquals(64, decision.getDecisionIntegritySeal().length());
        assertEquals(OfficialDecisionOutcome.DEFICIENCY_NOTICE_ISSUED, decision.getOutcome());
        assertEquals("Officer Test", decision.getOfficerName());
    }
}
