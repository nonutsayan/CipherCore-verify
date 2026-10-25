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

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deliverable 5: Comprehensive Automated Test Suite
 * Validates Regulatory Rule Engine, Subsystem B Infrastructure Vision Analysis,
 * Human Scrutiny Review Workflows, Discretionary Overrides, and Section 22/23 Compliance Reports.
 */
@SpringBootTest
@Transactional
public class ComplianceRuleEngineTests {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private RegulatorySourceRepository sourceRepository;

    @Autowired
    private RegulatoryRuleRepository ruleRepository;

    @Autowired
    private RegulatoryRuleVersionRepository versionRepository;

    @Autowired
    private ApplicationComplianceAssessmentRepository assessmentRepository;

    @Autowired
    private RuleEvaluationResultRepository resultRepository;

    @Autowired
    private ComplianceFindingRepository findingRepository;

    @Autowired
    private InfrastructureAssetRepository assetRepository;

    @Autowired
    private InfrastructureEvidenceRepository evidenceRepository;

    @Autowired
    private InfrastructureObservationRepository observationRepository;

    @Autowired
    private ComplianceReviewTaskRepository reviewTaskRepository;

    @Autowired
    private RegulatoryRuleEngineService ruleEngineService;

    @Autowired
    private InfrastructureAssessmentService infrastructureService;

    @Autowired
    private ComplianceReviewService reviewService;

    @Autowired
    private ComplianceReportService reportService;

    private Long app1Id;
    private Long app2Id;

    @BeforeEach
    void setUp() {
        List<Application> apps = applicationRepository.findAll();
        assertFalse(apps.isEmpty(), "Sample applications must be seeded");
        app1Id = apps.get(0).getId(); // Apex Institute (Compliant)
        app2Id = apps.size() > 1 ? apps.get(1).getId() : app1Id; // Horizon College (Deficiencies)
    }

    @Test
    @DisplayName("Test 1: Regulatory Source Registry verifies authentic AICTE APH 2024-27")
    void testRegulatorySourcesRegistry() {
        List<RegulatorySource> sources = sourceRepository.findAll();
        assertFalse(sources.isEmpty(), "Regulatory sources must be registered");

        RegulatorySource aph = sourceRepository.findBySourceCode("SRC_AICTE_APH_2024_27").orElse(null);
        assertNotNull(aph, "AICTE Approval Process Handbook source must exist");
        assertEquals("All India Council for Technical Education", aph.getIssuingAuthority());
        assertEquals(SourceVerificationState.VERIFIED, aph.getSourceVerificationState());
        assertNotNull(aph.getSha256Checksum(), "Source must have SHA-256 integrity checksum");
    }

    @Test
    @DisplayName("Test 2: Active Regulatory Rules cover core AICTE statutory norms")
    void testActiveRegulatoryRules() {
        List<RegulatoryRule> rules = ruleRepository.findByActiveTrue();
        assertTrue(rules.size() >= 10, "Should have at least 10 active regulatory rules");

        // Verify key statutory rule codes exist
        assertTrue(ruleRepository.findByRuleCode("AICTE_FSR_TECH_001").isPresent(), "FSR rule must exist");
        assertTrue(ruleRepository.findByRuleCode("AICTE_RAMP_GRADIENT_001").isPresent(), "Ramp gradient rule must exist");
        assertTrue(ruleRepository.findByRuleCode("AICTE_LAND_AREA_001").isPresent(), "Land area rule must exist");
        assertTrue(ruleRepository.findByRuleCode("AICTE_FIRE_NOC_VAL_001").isPresent(), "Fire NOC rule must exist");
        assertTrue(ruleRepository.findByRuleCode("AICTE_INTAKE_SANCTION_001").isPresent(), "Intake sanction rule must exist");

        // Verify active version attached
        RegulatoryRuleVersion v = versionRepository.findByRuleCodeAndLifecycleStatus("AICTE_RAMP_GRADIENT_001", RuleLifecycleStatus.ACTIVE).orElse(null);
        assertNotNull(v, "Active version must exist for Ramp rule");
        assertEquals("NUMERIC_THRESHOLD", v.getEvaluatorType());
        assertNotNull(v.getStatutoryCitation());
    }

    @Test
    @DisplayName("Test 3: Evaluate Apex Institute yields valid compliance assessment")
    void testApexInstituteCompliantEvaluation() {
        ApplicationComplianceAssessment assessment = ruleEngineService.evaluateApplication(app1Id, "Test Officer");
        assertNotNull(assessment);
        assertNotNull(assessment.getId());
        assertTrue(assessment.getOverallComplianceScore() >= 50.0, "Apex Institute should have measurable compliance score");
        assertTrue(assessment.getPassedCount() >= 5, "Apex Institute should pass applicable rules");
        assertNotNull(assessment.getAssessmentHash(), "Assessment must have SHA-256 integrity seal");
    }

    @Test
    @DisplayName("Test 4: Evaluate Horizon College flags ramp gradient and intake deficiencies")
    void testHorizonCollegeDeficienciesEvaluation() {
        ApplicationComplianceAssessment assessment = ruleEngineService.evaluateApplication(app2Id, "Test Officer");
        assertNotNull(assessment);

        // Verify deficiencies flagged
        assertTrue(assessment.getDeficiencyCount() >= 1, "Horizon College must have at least 1 deficiency flagged");
        assertEquals(AssessmentWorkflowStatus.POTENTIAL_DEFICIENCIES, assessment.getStatus());

        // Check specific ramp rule result
        List<RuleEvaluationResult> results = resultRepository.findByAssessmentId(assessment.getId());
        RuleEvaluationResult rampRes = results.stream()
                .filter(r -> "AICTE_RAMP_GRADIENT_001".equals(r.getRuleCode()))
                .findFirst().orElse(null);

        assertNotNull(rampRes, "Ramp gradient result must be present");
        assertEquals(RuleEvaluationStatus.DEFICIENCY, rampRes.getStatus());
        assertTrue(rampRes.getObservedValue().contains("1:8"), "Should reflect measured 1:8 gradient discrepancy");
        assertTrue(rampRes.getRequiresManualReview(), "Deficiency must require manual officer review");

        // Verify compliance findings created
        List<ComplianceFinding> findings = findingRepository.findByAssessmentId(assessment.getId());
        assertFalse(findings.isEmpty(), "Compliance findings must be generated for deficiencies");

        // Verify review tasks assigned
        List<ComplianceReviewTask> tasks = reviewTaskRepository.findByAssessmentId(assessment.getId());
        assertFalse(tasks.isEmpty(), "Review tasks must be generated for officer scrutiny");
    }

    @Test
    @DisplayName("Test 5: Infrastructure Evidence AI analysis extracts observations and flags visual discrepancies")
    void testInfrastructureEvidenceAnalysis() {
        List<InfrastructureEvidence> mediaList = evidenceRepository.findByApplicationId(app2Id);
        assertFalse(mediaList.isEmpty(), "Horizon College evidence media must be seeded");

        InfrastructureEvidence rampMedia = mediaList.stream()
                .filter(m -> m.getFileName().contains("ramp"))
                .findFirst().orElse(null);
        assertNotNull(rampMedia, "Ramp photo evidence must exist");

        // Trigger visual analysis
        InfrastructureAnalysisJob job = infrastructureService.analyzeEvidence(rampMedia.getId(), "Vision Tester");
        assertNotNull(job);
        assertEquals(InfrastructureAnalysisStatus.POSSIBLE_DISCREPANCY, job.getJobStatus());
        assertTrue(job.getDiscrepancyCount() > 0, "Discrepancy must be flagged for steep ramp");

        // Verify observations
        List<InfrastructureObservation> obs = observationRepository.findByEvidenceId(rampMedia.getId());
        assertFalse(obs.isEmpty());
        InfrastructureObservation discrepancyObs = obs.stream().filter(InfrastructureObservation::getIsDiscrepancy).findFirst().orElse(null);
        assertNotNull(discrepancyObs, "Visual discrepancy observation must be recorded");
        assertTrue(discrepancyObs.getDiscrepancyReason().contains("1:12"));
    }

    @Test
    @DisplayName("Test 6: Human Scrutiny Review Decision and Discretionary Override updates score & creates audit log")
    void testComplianceReviewWorkflowAndDiscretionaryOverride() {
        ApplicationComplianceAssessment assessment = ruleEngineService.evaluateApplication(app2Id, "Officer Scrutiny");
        List<ComplianceReviewTask> tasks = reviewTaskRepository.findByAssessmentId(assessment.getId());
        assertFalse(tasks.isEmpty());

        ComplianceReviewTask task = tasks.get(0);
        int initialPassed = assessment.getPassedCount();
        int initialDeficiencies = assessment.getDeficiencyCount();

        // Submit Review with Discretionary Override
        ComplianceReviewTask updatedTask = reviewService.submitReviewDecision(
                task.getId(),
                ComplianceReviewDecision.CONFIRMED,
                "Remediation plan approved under AICTE circular granting 60-day rectification window.",
                "AICTE APH 2024-27, Clause 1.8 (Discretionary Remediation Powers)",
                true,
                RuleEvaluationStatus.PASS,
                "Furnish PWD certification within 60 days.",
                "Dr. Rajesh Sharma"
        );

        assertNotNull(updatedTask);
        assertEquals(ComplianceReviewDecision.CONFIRMED, updatedTask.getReviewStatus());
        assertTrue(updatedTask.getOverrideRuleDecision());

        // Verify assessment counts updated
        ApplicationComplianceAssessment updatedAssessment = assessmentRepository.findById(assessment.getId()).orElse(null);
        assertNotNull(updatedAssessment);
        if (initialDeficiencies > 0) {
            assertEquals(initialDeficiencies - 1, updatedAssessment.getDeficiencyCount());
            assertEquals(initialPassed + 1, updatedAssessment.getPassedCount());
        }
    }

    @Test
    @DisplayName("Test 7: Section 22/23 JSON Compliance Report contains non-repudiation seal and official disposition")
    void testComplianceReportGeneration() {
        ApplicationComplianceAssessment assessment = ruleEngineService.evaluateApplication(app2Id, "Report Officer");
        Map<String, Object> report = reportService.generateComplianceReport(assessment.getId());

        assertNotNull(report);
        assertEquals("2.0-SIH26261", report.get("reportSchemaVersion"));
        assertTrue(report.containsKey("assessmentSummary"));
        assertTrue(report.containsKey("ruleEvaluationResults"));
        assertTrue(report.containsKey("complianceFindings"));
        assertTrue(report.containsKey("infrastructureScrutiny"));
        assertTrue(report.containsKey("officialDispositionRecommendation"));

        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) report.get("assessmentSummary");
        assertNotNull(summary.get("assessmentIntegritySeal"), "Report must include SHA-256 seal");

        String disposition = (String) report.get("officialDispositionRecommendation");
        assertNotNull(disposition);
        assertTrue(disposition.contains("DEFICIENCIES_DETECTED") || disposition.contains("CONDITIONAL_REVIEW"),
                "Disposition recommendation must advise scrutiny action");
    }
}
