package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ActionItem;
import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.model.ValidationFinding;
import com.ciphercore.verify.repository.AuditEventRepository;
import com.ciphercore.verify.repository.ValidationFindingRepository;
import com.ciphercore.verify.service.ActionManagementService;
import com.ciphercore.verify.service.AssessmentReportExportService;
import com.ciphercore.verify.service.OfficialDecisionService;
import com.ciphercore.verify.service.WorkflowTrackerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Deliverable 6: Application Workflow & Dossier Controller
 * Delivers application-level traceability across all stages and evidence links.
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationWorkflowController {

    private final WorkflowTrackerService workflowTrackerService;
    private final ActionManagementService actionManagementService;
    private final OfficialDecisionService officialDecisionService;
    private final AssessmentReportExportService assessmentReportExportService;
    private final ValidationFindingRepository validationFindingRepository;
    private final AuditEventRepository auditEventRepository;

    public ApplicationWorkflowController(WorkflowTrackerService workflowTrackerService,
                                         ActionManagementService actionManagementService,
                                         OfficialDecisionService officialDecisionService,
                                         AssessmentReportExportService assessmentReportExportService,
                                         ValidationFindingRepository validationFindingRepository,
                                         AuditEventRepository auditEventRepository) {
        this.workflowTrackerService = workflowTrackerService;
        this.actionManagementService = actionManagementService;
        this.officialDecisionService = officialDecisionService;
        this.assessmentReportExportService = assessmentReportExportService;
        this.validationFindingRepository = validationFindingRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping("/{id}/workflow")
    public ResponseEntity<WorkflowTrackerService.ApplicationWorkflowSummary> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowTrackerService.getWorkflowSummary(id));
    }

    @GetMapping("/{id}/actions")
    public ResponseEntity<List<ActionItem>> getActions(@PathVariable Long id) {
        return ResponseEntity.ok(actionManagementService.getActionsForApplication(id));
    }

    @GetMapping("/{id}/findings")
    public ResponseEntity<List<ValidationFinding>> getFindings(@PathVariable Long id) {
        return ResponseEntity.ok(validationFindingRepository.findByApplicationId(id));
    }

    @GetMapping("/{id}/readiness")
    public ResponseEntity<OfficialDecisionService.ReadinessAssessmentResult> getReadiness(@PathVariable Long id) {
        return ResponseEntity.ok(officialDecisionService.assessReadiness(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AuditEvent>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(auditEventRepository
                .findByEntityNameAndEntityIdOrderByEventTimestampDesc("APPLICATION", String.valueOf(id)));
    }

    @GetMapping("/{id}/assessment-report")
    public ResponseEntity<Map<String, Object>> getMasterDossier(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentReportExportService.generateMasterAssessmentDossier(id));
    }
}
