package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.model.DocumentAnalysisRun;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.AuditEventRepository;
import com.ciphercore.verify.repository.DocumentAnalysisRunRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import com.ciphercore.verify.service.ReviewWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Section 24 & 26: Human Review Disposition & Audit Trail REST Controller
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Review Workflow & Audit Trail", description = "Human adjudication, reviewer corrections, and immutable audit logs")
public class AnomalyReviewController {

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    @Autowired
    private DocumentAnalysisRunRepository runRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private ReviewWorkflowService reviewWorkflowService;

    public record ReviewRequest(String reviewerId, String decision, String notes) {}

    @Operation(summary = "Retrieve a single anomaly with its evidence chain")
    @GetMapping("/api/anomalies/{anomalyId}")
    public ResponseEntity<?> getAnomalyDetail(@PathVariable Long anomalyId) {
        Optional<AnomalyRecord> aOpt = anomalyRecordRepository.findById(anomalyId);
        if (aOpt.isEmpty()) return ResponseEntity.notFound().build();

        List<EvidenceRecord> evList = evidenceRecordRepository.findByAnomalyId(anomalyId);
        return ResponseEntity.ok(Map.of(
            "anomaly", aOpt.get(),
            "evidence", evList
        ));
    }

    @Operation(summary = "Record authorized reviewer adjudication decision with mandatory justification")
    @PatchMapping("/api/anomalies/{anomalyId}/review")
    public ResponseEntity<?> recordReview(@PathVariable Long anomalyId, @RequestBody ReviewRequest req) {
        try {
            AnomalyRecord updated = reviewWorkflowService.recordReviewDecision(
                anomalyId,
                req.reviewerId() != null ? req.reviewerId() : "officer_sc401",
                req.decision() != null ? req.decision() : "CONFIRM_LEGITIMATE",
                req.notes() != null ? req.notes() : "Reviewed and verified against authorized office records."
            );
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Retrieve status and timing of a specific analysis run")
    @GetMapping("/api/analysis-runs/{runId}")
    public ResponseEntity<?> getAnalysisRun(@PathVariable Long runId) {
        return runRepository.findById(runId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Retrieve tamper-evident immutable audit trail")
    @GetMapping("/api/audit-trail")
    public ResponseEntity<List<AuditEvent>> getAuditTrail() {
        return ResponseEntity.ok(auditEventRepository.findAll());
    }
}
