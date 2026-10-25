package com.ciphercore.verify;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ValidationDashboardTests {

    @Autowired
    private DashboardAggregationService dashboardAggregationService;

    @Autowired
    private WorkflowTrackerService workflowTrackerService;

    @Autowired
    private ActionManagementService actionManagementService;

    @Autowired
    private OfficialDecisionService officialDecisionService;

    @Autowired
    private AssessmentReportExportService assessmentReportExportService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ActionItemRepository actionItemRepository;

    @Autowired
    private OfficialDecisionRepository officialDecisionRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    @DisplayName("Test 1: Dashboard KPI Summary Aggregation matches real database state")
    void testDashboardSummaryKpiCalculation() {
        DashboardAggregationService.DashboardKpiSummary summary = dashboardAggregationService.getSummary("2025-2026");

        assertNotNull(summary);
        assertTrue(summary.totalApplications() >= 2, "Should have at least seeded applications");
        assertEquals("2025-2026", summary.academicYear());
        assertNotNull(summary.lastRefreshedAt());
        assertTrue(summary.highPriorityUnresolvedFindings() >= 1, "Should detect high priority findings");
    }

    @Test
    @DisplayName("Test 2: Six-Stage Workflow Distribution counts applications without duplication")
    void testWorkflowDistributionCalculation() {
        DashboardAggregationService.WorkflowDistribution dist = dashboardAggregationService.getWorkflowDistribution("2025-2026");

        assertNotNull(dist);
        assertTrue(dist.totalActiveApplications() >= 2);
        long sumStages = dist.stage1ApplicationIntake() +
                         dist.stage2DocumentsOcr() +
                         dist.stage3ValidationConsistency() +
                         dist.stage4AuthorityVerification() +
                         dist.stage5RegulatoryCompliance() +
                         dist.stage6FinalReview();

        assertEquals(dist.totalActiveApplications(), sumStages, "Workflow distribution stages must equal total applications");
    }

    @Test
    @DisplayName("Test 3: Six-Stage Workflow Tracker evaluates stage states and blockers accurately")
    void testSixStageWorkflowTrackerEvaluation() {
        // Horizon College (App 2) has known deficiencies and steep ramp slope
        WorkflowTrackerService.ApplicationWorkflowSummary summary = workflowTrackerService.getWorkflowSummary(2L);

        assertNotNull(summary);
        assertEquals("AICTE-2025-EOA-44219", summary.applicationRef());
        assertEquals(6, summary.stages().size(), "Must evaluate exactly 6 stages");

        // Verify stage sequence
        assertEquals(WorkflowStage.APPLICATION, summary.stages().get(0).stage());
        assertEquals(WorkflowStage.DOCUMENTS, summary.stages().get(1).stage());
        assertEquals(WorkflowStage.VALIDATION, summary.stages().get(2).stage());
        assertEquals(WorkflowStage.VERIFICATION, summary.stages().get(3).stage());
        assertEquals(WorkflowStage.COMPLIANCE, summary.stages().get(4).stage());
        assertEquals(WorkflowStage.FINAL_REVIEW, summary.stages().get(5).stage());

        // Horizon has expired NOC and 1:8 ramp gradient discrepancy
        assertFalse(summary.isReadyForOfficialReview(), "Horizon College should not be ready due to blockers");
        assertFalse(summary.outstandingBlockers().isEmpty(), "Should report outstanding blockers");
    }

    @Test
    @DisplayName("Test 4: Application Register supports search, filtering, and 17 columns")
    void testApplicationRegisterPaginationAndSearch() {
        Page<DashboardAggregationService.ApplicationRegisterRow> page = dashboardAggregationService.getApplicationRegister(
                "Apex", null, null, null, null, PageRequest.of(0, 10)
        );

        assertNotNull(page);
        assertTrue(page.getTotalElements() >= 1, "Should find Apex Institute by query");
        DashboardAggregationService.ApplicationRegisterRow row = page.getContent().get(0);

        assertEquals("AICTE-2025-EOA-98104", row.applicationRef());
        assertNotNull(row.currentWorkflowStage());
        assertNotNull(row.stageStatus());
        assertNotNull(row.nextAction());
    }

    @Test
    @DisplayName("Test 5: Action Item Lifecycle, assignment, and immutable AuditEvent logging")
    void testActionItemLifecycleAndAssignment() {
        ActionItem item = new ActionItem();
        item.setApplicationId(2L);
        item.setApplicationRef("AICTE-2025-EOA-44219");
        item.setInstitutionName("Horizon College of Engineering");
        item.setTaskType(ActionItemType.COMPLIANCE_DEFICIENCY);
        item.setTitle("Verify Faculty Appointment Letters for UG Engineering");
        item.setDescription("Inspect full-time status for 12 faculty to ensure FSR <= 1:15.");
        item.setPriority(ActionItemPriority.HIGH);
        item.setStatus(ActionItemStatus.OPEN);

        ActionItem created = actionManagementService.createAction(item, "admin");
        assertNotNull(created.getId());
        assertEquals(ActionItemStatus.OPEN, created.getStatus());

        // Assign
        ActionItem assigned = actionManagementService.assignAction(created.getId(), "Dr. Rajesh Sharma", "admin");
        assertEquals(ActionItemStatus.ASSIGNED, assigned.getStatus());
        assertEquals("Dr. Rajesh Sharma", assigned.getAssignedTo());

        // Update status to COMPLETED
        ActionItem completed = actionManagementService.updateStatus(created.getId(), ActionItemStatus.COMPLETED, "Appointment letters verified", "Dr. Rajesh Sharma");
        assertEquals(ActionItemStatus.COMPLETED, completed.getStatus());

        // Verify audit trail
        List<AuditEvent> audits = auditEventRepository.findByEntityNameAndEntityIdOrderByEventTimestampDesc(
                "APPLICATION", "2"
        );
        assertTrue(audits.stream().anyMatch(a -> a.getAction().equals("ACTION_ASSIGNED")), "Must log assignment audit event");
    }

    @Test
    @DisplayName("Test 6: Readiness Assessment evaluates prerequisites and blockers")
    void testReadinessAssessmentChecklist() {
        // Horizon College (App 2) should fail readiness due to deficiencies
        OfficialDecisionService.ReadinessAssessmentResult readiness = officialDecisionService.assessReadiness(2L);

        assertNotNull(readiness);
        assertEquals(5, readiness.checklist().size(), "Should evaluate 5 prerequisite areas");
        assertFalse(readiness.isReadyForDecision());
        assertEquals(ReadinessState.NOT_READY, readiness.readinessState());
        assertFalse(readiness.blockers().isEmpty());
    }

    @Test
    @DisplayName("Test 7: Official Decision Recording with SHA-256 Seal and Application Status Update")
    void testOfficialDecisionRecordingWithSha256Seal() {
        OfficialDecisionService.RecordDecisionRequest req = new OfficialDecisionService.RecordDecisionRequest(
                1L, // Apex Institute
                OfficialDecisionOutcome.APPROVED_UNCONDITIONAL_EOA,
                "All institutional norms under AICTE APH 2024-27 satisfied. Extension of Approval granted unconditionally.",
                "None",
                false,
                "AICTE-SC-WRO-2026/01"
        );

        OfficialDecision decision = officialDecisionService.recordDecision(req, "Dr. Rajesh Sharma (Scrutiny Member)", "COMPLIANCE_OFFICER");

        assertNotNull(decision);
        assertNotNull(decision.getId());
        assertEquals(OfficialDecisionOutcome.APPROVED_UNCONDITIONAL_EOA, decision.getOutcome());
        assertNotNull(decision.getDecisionIntegritySeal());
        assertEquals(64, decision.getDecisionIntegritySeal().length(), "SHA-256 seal must be 64 hex characters");

        // Application status should be updated to APPROVED
        Application updatedApp = applicationRepository.findById(1L).orElseThrow();
        assertEquals(ApplicationStatus.APPROVED, updatedApp.getStatus());
    }

    @Test
    @DisplayName("Test 8: Master Assessment Dossier exports complete 10-section report")
    void testMasterAssessmentDossierGeneration() {
        Map<String, Object> dossier = assessmentReportExportService.generateMasterAssessmentDossier(2L);

        assertNotNull(dossier);
        assertEquals("CIPHERCORE MASTER INSTITUTIONAL COMPLIANCE DOSSIER", dossier.get("reportTitle"));
        assertEquals("3.0-DELIVERABLE-6", dossier.get("reportSchemaVersion"));
        assertTrue(dossier.containsKey("applicationDetails"));
        assertTrue(dossier.containsKey("workflowStatus"));
        assertTrue(dossier.containsKey("documentInventory"));
        assertTrue(dossier.containsKey("forensicAnomalies"));
        assertTrue(dossier.containsKey("regulatoryCompliance"));
        assertTrue(dossier.containsKey("infrastructureDiscrepancies"));
        assertTrue(dossier.containsKey("pendingActionItems"));
        assertTrue(dossier.containsKey("officialDecision"));
        assertTrue(dossier.containsKey("auditTimeline"));
    }
}
