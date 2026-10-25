package com.ciphercore.verify.controller;

import com.ciphercore.verify.adapter.HealthCheckResult;
import com.ciphercore.verify.model.AuthoritySource;
import com.ciphercore.verify.model.VerificationEvidence;
import com.ciphercore.verify.model.VerificationResult;
import com.ciphercore.verify.model.VerificationReview;
import com.ciphercore.verify.service.AuthoritySourceRegistryService;
import com.ciphercore.verify.service.VerificationOrchestratorService;
import com.ciphercore.verify.service.VerificationReportService;
import com.ciphercore.verify.service.VerificationReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Section 22: REST Controller for Issuing Authority Verification Engine.
 * Exposes document verification triggers, authority registry status, multi-method comparisons,
 * Section 20 JSON reports, and manual review dispositions.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Issuing Authority Verification Engine", description = "Endpoints for corroborating documents with trusted issuing authorities")
public class IssuingAuthorityController {

    private final VerificationOrchestratorService orchestratorService;
    private final AuthoritySourceRegistryService registryService;
    private final VerificationReportService reportService;
    private final VerificationReviewService reviewService;

    public IssuingAuthorityController(
            VerificationOrchestratorService orchestratorService,
            AuthoritySourceRegistryService registryService,
            VerificationReportService reportService,
            VerificationReviewService reviewService
    ) {
        this.orchestratorService = orchestratorService;
        this.registryService = registryService;
        this.reportService = reportService;
        this.reviewService = reviewService;
    }

    @PostMapping("/api/documents/{id}/verify")
    @Operation(summary = "Trigger verification for document across appropriate authorities")
    public ResponseEntity<List<VerificationResult>> verifyDocument(@PathVariable Long id) {
        List<VerificationResult> results = orchestratorService.verifyDocument(id, null);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/api/documents/{id}/verify/{sourceId}")
    @Operation(summary = "Trigger verification for document against a specific authority source")
    public ResponseEntity<List<VerificationResult>> verifyDocumentWithSource(
            @PathVariable Long id,
            @PathVariable String sourceId
    ) {
        List<VerificationResult> results = orchestratorService.verifyDocument(id, sourceId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/api/documents/{id}/verification")
    @Operation(summary = "Get latest verification result for document")
    public ResponseEntity<VerificationResult> getLatestVerification(@PathVariable Long id) {
        List<VerificationResult> history = orchestratorService.getVerificationHistory(id);
        if (history.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(history.get(0));
    }

    @GetMapping("/api/verifications/{id}")
    @Operation(summary = "Get verification result by ID")
    public ResponseEntity<VerificationResult> getVerificationById(@PathVariable Long id) {
        return orchestratorService.getVerificationResult(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/verifications/{id}/status")
    @Operation(summary = "Get verification status and dimensional summary")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(@PathVariable Long id) {
        Optional<VerificationResult> resOpt = orchestratorService.getVerificationResult(id);
        if (resOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        VerificationResult res = resOpt.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("verificationResultId", res.getId());
        map.put("documentId", res.getDocumentId());
        map.put("overallStatus", res.getOverallStatus());
        map.put("recordMatchStatus", res.getRecordMatchStatus());
        map.put("validityStatus", res.getValidityStatus());
        map.put("signatureStatus", res.getSignatureStatus());
        map.put("qrStatus", res.getQrStatus());
        map.put("manualReviewRequired", res.isManualReviewRequired());
        map.put("generatedAt", res.getGeneratedAt());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/api/verifications/{id}/evidence")
    @Operation(summary = "Get supporting evidence items for verification result")
    public ResponseEntity<List<VerificationEvidence>> getVerificationEvidence(@PathVariable Long id) {
        return ResponseEntity.ok(orchestratorService.getEvidenceForResult(id));
    }

    @GetMapping("/api/verifications/{id}/report")
    @Operation(summary = "Get Section 20 JSON Verification Report")
    public ResponseEntity<Map<String, Object>> getSection20Report(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.generateSection20Report(id));
    }

    @GetMapping("/api/documents/{id}/verification-history")
    @Operation(summary = "Get all verification results for document")
    public ResponseEntity<List<VerificationResult>> getVerificationHistory(@PathVariable Long id) {
        return ResponseEntity.ok(orchestratorService.getVerificationHistory(id));
    }

    @PostMapping("/api/verifications/{id}/retry")
    @Operation(summary = "Retry a verification check")
    public ResponseEntity<VerificationResult> retryVerification(@PathVariable Long id) {
        return ResponseEntity.ok(orchestratorService.retryVerification(id));
    }

    @PostMapping("/api/verifications/{id}/review")
    @Operation(summary = "Submit human scrutiny review determination")
    public ResponseEntity<VerificationReview> submitReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String reviewerId = body.getOrDefault("reviewerId", "reviewer-officer");
        String decision = body.getOrDefault("decision", "CONFIRM_LEGITIMATE");
        String notes = body.getOrDefault("notes", "Scrutiny review verified by authorized officer");
        VerificationReview review = reviewService.submitReview(id, reviewerId, decision, notes);
        return ResponseEntity.ok(review);
    }

    @GetMapping("/api/authorities")
    @Operation(summary = "List all configured issuing authorities")
    public ResponseEntity<List<AuthoritySource>> listAuthorities() {
        return ResponseEntity.ok(registryService.getAllSources());
    }

    @GetMapping("/api/authorities/{sourceId}")
    @Operation(summary = "Get issuing authority source details")
    public ResponseEntity<AuthoritySource> getAuthority(@PathVariable String sourceId) {
        return registryService.getSource(sourceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/admin/authorities")
    @Operation(summary = "Register a new authority source")
    public ResponseEntity<AuthoritySource> createAuthority(@RequestBody AuthoritySource source) {
        return ResponseEntity.ok(registryService.saveSource(source));
    }

    @PatchMapping("/api/admin/authorities/{sourceId}")
    @Operation(summary = "Update an existing authority source configuration")
    public ResponseEntity<AuthoritySource> updateAuthority(
            @PathVariable String sourceId,
            @RequestBody Map<String, Object> updates
    ) {
        Optional<AuthoritySource> srcOpt = registryService.getSource(sourceId);
        if (srcOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        AuthoritySource src = srcOpt.get();
        if (updates.containsKey("name")) src.setName((String) updates.get("name"));
        if (updates.containsKey("endpointUrl")) src.setEndpointUrl((String) updates.get("endpointUrl"));
        if (updates.containsKey("approvedDomains")) src.setApprovedDomains((String) updates.get("approvedDomains"));
        if (updates.containsKey("timeoutMs")) src.setTimeoutMs(((Number) updates.get("timeoutMs")).intValue());
        if (updates.containsKey("rateLimitRpm")) src.setRateLimitRpm(((Number) updates.get("rateLimitRpm")).intValue());
        return ResponseEntity.ok(registryService.saveSource(src));
    }

    @PostMapping("/api/admin/authorities/{sourceId}/test")
    @Operation(summary = "Execute real-time health and connectivity check on an authority gateway")
    public ResponseEntity<HealthCheckResult> testAuthorityConnection(@PathVariable String sourceId) {
        return ResponseEntity.ok(registryService.testConnection(sourceId));
    }

    @GetMapping("/api/applications/{id}/verification-summary")
    @Operation(summary = "Get aggregate authority verification summary for an application")
    public ResponseEntity<Map<String, Object>> getApplicationVerificationSummary(@PathVariable Long id) {
        return ResponseEntity.ok(orchestratorService.getApplicationVerificationSummary(id));
    }
}
