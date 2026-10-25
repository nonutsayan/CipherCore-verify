package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ComplianceReviewTask;
import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import com.ciphercore.verify.service.ComplianceReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Deliverable 5: REST Controller for Human Scrutiny Review Tasks
 * Officer dispositions, statutory justifications, evidentiary requests, and discretionary overrides.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Compliance Review & Scrutiny", description = "Endpoints for officer disposition, justifications, and rule overrides")
public class ComplianceReviewController {

    private final ComplianceReviewService reviewService;

    public ComplianceReviewController(ComplianceReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public record ReviewDecisionRequest(
            ComplianceReviewDecision decision,
            String officerRationale,
            String statutoryJustification,
            Boolean overrideRuleDecision,
            RuleEvaluationStatus overriddenStatus,
            String additionalEvidenceRequested,
            String officerUsername
    ) {}

    @Operation(summary = "Get all scrutiny tasks for a compliance assessment")
    @GetMapping("/api/compliance-assessments/{assessmentId}/review-tasks")
    public ResponseEntity<List<ComplianceReviewTask>> getReviewTasks(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(reviewService.getTasksForAssessment(assessmentId));
    }

    @Operation(summary = "Submit officer review decision and rationale for a scrutiny task")
    @PostMapping("/api/compliance-review-tasks/{taskId}/decision")
    public ResponseEntity<ComplianceReviewTask> submitDecision(
            @PathVariable Long taskId,
            @RequestBody ReviewDecisionRequest request) {
        ComplianceReviewTask task = reviewService.submitReviewDecision(
                taskId,
                request.decision(),
                request.officerRationale(),
                request.statutoryJustification(),
                request.overrideRuleDecision(),
                request.overriddenStatus(),
                request.additionalEvidenceRequested(),
                request.officerUsername()
        );
        return ResponseEntity.ok(task);
    }
}
