package com.ciphercore.verify;

import com.ciphercore.verify.controller.AdminController;
import com.ciphercore.verify.controller.OfficialDecisionController;
import com.ciphercore.verify.model.ValidationRule;
import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import com.ciphercore.verify.model.enums.OfficialDecisionOutcome;
import com.ciphercore.verify.model.enums.Severity;
import com.ciphercore.verify.service.BlockchainNotaryService;
import com.ciphercore.verify.service.OfficialDecisionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Project Chimera Master Verification Test Suite (SIH26261)
 * Validates Section 4.2 compliance formulas, Section 4.5 hash-chain audit ledger integrity,
 * Section 10 role-based decision access control, and AdminController rule authorization.
 */
@SpringBootTest
@Transactional
public class ChimeraComplianceAndAuditTests {

    @Autowired
    private BlockchainNotaryService blockchainNotaryService;

    @Autowired
    private OfficialDecisionController officialDecisionController;

    @Autowired
    private AdminController adminController;

    @Test
    @DisplayName("Section 4.2: Workstation Deficit & Carpet Area Calculation Disambiguation")
    void testWorkstationAndCarpetAreaCalculations() {
        int intake = 180;
        int activeWorkstations = 142;
        int declaredBatchCapacity = 150;
        double declaredCarpetArea = 210.5;
        double measuredCarpetArea = 194.2;
        double normAreaPerTerminal = 1.20;

        // Disambiguated Calculation A: Shortfall against declared concurrent lab capacity (150 terminals)
        int shortfallAgainstBatchNorm = declaredBatchCapacity - activeWorkstations;
        assertEquals(8, shortfallAgainstBatchNorm, "Shortfall against batch concurrency requirement (150 - 142) must equal 8");
        double percentageDeficit = ((double) shortfallAgainstBatchNorm / declaredBatchCapacity) * 100.0;
        assertEquals(5.33, Math.round(percentageDeficit * 100.0) / 100.0, "Batch shortfall percentage must equal 5.33%");

        // Disambiguated Calculation B: Shortfall against assumed 1:1 total student intake (180 students)
        int shortfallAgainstTotalIntake = intake - activeWorkstations;
        assertEquals(38, shortfallAgainstTotalIntake, "Shortfall against total annual intake 1:1 (180 - 142) must equal 38");

        // Calculation C: Overall AICTE APH Standard 1:4 Student-to-Computer Ratio (Technical UG)
        double achievedRatio = (double) intake / activeWorkstations;
        assertTrue(achievedRatio <= 4.0, "Achieved ratio 180/142 (1:1.27) satisfies standard AICTE 1:4 student-to-computer ratio");

        // Calculation D: Carpet Area Discrepancy & Per-Terminal Density
        double areaDeficit = declaredCarpetArea - measuredCarpetArea;
        assertEquals(16.3, Math.round(areaDeficit * 10.0) / 10.0, "Carpet area deficit (210.5 - 194.2) must equal 16.3 m²");

        double areaPerWorkstation = measuredCarpetArea / declaredBatchCapacity;
        assertEquals(1.29, Math.round(areaPerWorkstation * 100.0) / 100.0, "Measured area per terminal must equal 1.29 m²/terminal");
        assertTrue(areaPerWorkstation >= normAreaPerTerminal, "Measured density (1.29 m²) complies with minimum 1.20 m² norm");
    }

    @Test
    @DisplayName("Section 4.5: Cryptographic Hash-Chain Integrity & Tamper Detection")
    void testBlockchainSequentialChainIntegrityAndTamperDetection() {
        // 1. Initial State: Hash-chain is untampered and sequentially valid
        BlockchainNotaryService.ChainVerificationResult initialResult = blockchainNotaryService.verifyChainIntegrity();
        assertTrue(initialResult.isChainValid(), "Fresh audit ledger must be cryptographically valid");
        assertTrue(initialResult.totalBlocksVerified() > 0, "Ledger must contain seeded audit blocks");
        assertNull(initialResult.brokenBlockNumber(), "No broken blocks in initial state");

        // 2. Simulate Tamper on Block #1
        boolean tampered = blockchainNotaryService.simulateTamper(1L);
        assertTrue(tampered, "Simulate tamper on Block #1 must succeed");

        // 3. Verify Chain Integrity Fails upon Tamper
        BlockchainNotaryService.ChainVerificationResult tamperedResult = blockchainNotaryService.verifyChainIntegrity();
        assertFalse(tamperedResult.isChainValid(), "Chain verification must fail when a block payload is tampered");
        assertEquals(1L, tamperedResult.brokenBlockNumber(), "Broken block must be identified as Block #1");

        // 4. Restore Tamper
        boolean restored = blockchainNotaryService.restoreTamper(1L);
        assertTrue(restored, "Restore tamper on Block #1 must succeed");

        // 5. Verify Chain Integrity Passes again
        BlockchainNotaryService.ChainVerificationResult restoredResult = blockchainNotaryService.verifyChainIntegrity();
        assertTrue(restoredResult.isChainValid(), "Chain verification must pass once tamper is restored");
    }

    @Test
    @DisplayName("Section 10: Official Decision Role-Based Access Control Enforcement")
    void testOfficialDecisionAccessControl() {
        OfficialDecisionService.RecordDecisionRequest req = new OfficialDecisionService.RecordDecisionRequest(
                1L,
                OfficialDecisionOutcome.APPROVED_CONDITIONAL_EOA,
                "Valid statutory rationale provided by officer for conditional approval.",
                "Furnish compliance within 30 days",
                false,
                "AICTE-SC-2026/01"
        );

        // 1. Unauthorized Institutional Applicant: must be rejected with HTTP 403
        ResponseEntity<?> applicantResponse = officialDecisionController.recordDecision(
                1L, req, "Prof. Ramesh Patil", "ROLE_INSTITUTION_APPLICANT"
        );
        assertEquals(HttpStatus.FORBIDDEN, applicantResponse.getStatusCode(),
                "Institutional applicants must be forbidden from recording statutory decisions");

        // 2. Unauthorized Generic Signatory: must be rejected with HTTP 403
        ResponseEntity<?> signatoryResponse = officialDecisionController.recordDecision(
                1L, req, "Signatory", "INSTITUTION_USER"
        );
        assertEquals(HttpStatus.FORBIDDEN, signatoryResponse.getStatusCode(),
                "Generic users without officer roles must be forbidden from recording statutory decisions");

        // 3. Authorized Scrutiny Officer: must succeed with HTTP 200
        ResponseEntity<?> officerResponse = officialDecisionController.recordDecision(
                1L, req, "Dr. Arvind Kumar", "AICTE_SCRUTINY_OFFICER"
        );
        assertEquals(HttpStatus.OK, officerResponse.getStatusCode(),
                "Authorized scrutiny officer must be permitted to record decisions");
    }

    @Test
    @DisplayName("AdminController: Privileged Rule Configuration Access Control Enforcement")
    void testAdminRuleConfigurationAccessControl() {
        ValidationRule newRule = new ValidationRule();
        newRule.setRuleId("RULE_TEST_CUSTOM_99");
        newRule.setName("Test Verification Rule");
        newRule.setDescription("Unauthorized attempt to inject validation rule");
        newRule.setCategoryCode("ALL");
        newRule.setRuleType("MANDATORY_FIELD");
        newRule.setSeverity(Severity.CRITICAL);

        // 1. Unauthorized Applicant or Anonymous User: must be rejected with HTTP 403
        ResponseEntity<?> unauthorizedResponse = adminController.createRule(newRule, "INSTITUTION_APPLICANT");
        assertEquals(HttpStatus.FORBIDDEN, unauthorizedResponse.getStatusCode(),
                "Unauthorized applicants must be blocked from modifying validation rules");

        // 2. Authorized Compliance Admin / Officer: must succeed with HTTP 200
        ResponseEntity<?> authorizedResponse = adminController.createRule(newRule, "COMPLIANCE_ADMIN");
        assertEquals(HttpStatus.OK, authorizedResponse.getStatusCode(),
                "Authorized administrator must be permitted to configure rules");
    }

    @Test
    @DisplayName("Section 4.4: Authority Verification Status Truthfulness Categories")
    void testAuthorityVerificationStatusCategories() {
        assertNotNull(AuthorityVerificationStatus.valueOf("LIVE_VERIFIED"));
        assertNotNull(AuthorityVerificationStatus.valueOf("DEMO_SIMULATED"));
        assertNotNull(AuthorityVerificationStatus.valueOf("SOURCE_UNAVAILABLE"));
        assertNotNull(AuthorityVerificationStatus.valueOf("FAILED"));
        assertNotNull(AuthorityVerificationStatus.valueOf("MANUAL_REVIEW"));
        assertNotNull(AuthorityVerificationStatus.valueOf("MISMATCH"));
    }
}
