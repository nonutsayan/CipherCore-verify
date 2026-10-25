package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ApplicationComplianceAssessment;
import com.ciphercore.verify.model.ComplianceFinding;
import com.ciphercore.verify.model.RuleEvaluationResult;
import com.ciphercore.verify.repository.ApplicationComplianceAssessmentRepository;
import com.ciphercore.verify.repository.ComplianceFindingRepository;
import com.ciphercore.verify.repository.RuleEvaluationResultRepository;
import com.ciphercore.verify.service.ComplianceReportService;
import com.ciphercore.verify.service.RegulatoryRuleEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Deliverable 5: REST Controller for Application Compliance Assessments
 * Triggers evaluations, lists findings, rule results, and generates Section 22/23 JSON Compliance Reports.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Compliance Assessment Engine", description = "Endpoints for evaluating institutional applications against AICTE rules")
public class ComplianceAssessmentController {

    private final RegulatoryRuleEngineService ruleEngineService;
    private final ComplianceReportService reportService;
    private final ApplicationComplianceAssessmentRepository assessmentRepository;
    private final RuleEvaluationResultRepository resultRepository;
    private final ComplianceFindingRepository findingRepository;

    public ComplianceAssessmentController(RegulatoryRuleEngineService ruleEngineService,
                                         ComplianceReportService reportService,
                                         ApplicationComplianceAssessmentRepository assessmentRepository,
                                         RuleEvaluationResultRepository resultRepository,
                                         ComplianceFindingRepository findingRepository) {
        this.ruleEngineService = ruleEngineService;
        this.reportService = reportService;
        this.assessmentRepository = assessmentRepository;
        this.resultRepository = resultRepository;
        this.findingRepository = findingRepository;
    }

    @Operation(summary = "List all compliance assessments for an application")
    @GetMapping("/api/applications/{applicationId}/compliance-assessments")
    public ResponseEntity<List<ApplicationComplianceAssessment>> getAssessmentsForApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(assessmentRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId));
    }

    @Operation(summary = "Trigger AICTE regulatory compliance evaluation for an application")
    @PostMapping("/api/applications/{applicationId}/compliance-assessments/evaluate")
    public ResponseEntity<ApplicationComplianceAssessment> triggerEvaluation(
            @PathVariable Long applicationId,
            @RequestParam(required = false, defaultValue = "Scrutiny Officer") String officerUsername) {
        ApplicationComplianceAssessment assessment = ruleEngineService.evaluateApplication(applicationId, officerUsername);
        return ResponseEntity.ok(assessment);
    }

    @Operation(summary = "Get specific compliance assessment by ID")
    @GetMapping("/api/compliance-assessments/{assessmentId}")
    public ResponseEntity<ApplicationComplianceAssessment> getAssessment(@PathVariable Long assessmentId) {
        return assessmentRepository.findById(assessmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all evaluated rule results for an assessment")
    @GetMapping("/api/compliance-assessments/{assessmentId}/rules")
    public ResponseEntity<List<RuleEvaluationResult>> getRuleResults(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(resultRepository.findByAssessmentId(assessmentId));
    }

    @Operation(summary = "Get identified compliance findings for an assessment")
    @GetMapping("/api/compliance-assessments/{assessmentId}/findings")
    public ResponseEntity<List<ComplianceFinding>> getFindings(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(findingRepository.findByAssessmentId(assessmentId));
    }

    @Operation(summary = "Generate Section 22/23 JSON Compliance Assessment Report")
    @GetMapping("/api/compliance-assessments/{assessmentId}/report")
    public ResponseEntity<Map<String, Object>> getComplianceReport(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(reportService.generateComplianceReport(assessmentId));
    }
}
