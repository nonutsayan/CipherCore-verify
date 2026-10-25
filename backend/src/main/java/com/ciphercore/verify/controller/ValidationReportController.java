package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ValidationFinding;
import com.ciphercore.verify.repository.ValidationFindingRepository;
import com.ciphercore.verify.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ValidationReportController {

    private final ReportService reportService;
    private final ValidationFindingRepository findingRepository;

    public ValidationReportController(ReportService reportService, ValidationFindingRepository findingRepository) {
        this.reportService = reportService;
        this.findingRepository = findingRepository;
    }

    @GetMapping("/validation-runs/{runId}/report.json")
    public ResponseEntity<Map<String, Object>> getJsonReport(@PathVariable Long runId) {
        return ResponseEntity.ok(reportService.generateJsonReport(runId));
    }

    @GetMapping("/validation-runs/{runId}/report.pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable Long runId) {
        byte[] pdfBytes = reportService.generatePdfReport(runId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"AICTE_Scrutiny_Report_" + runId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/validation-runs/{runId}/findings")
    public List<ValidationFinding> getFindings(@PathVariable Long runId) {
        return findingRepository.findByValidationRunId(runId);
    }

    public record ReviewDecisionRequest(String decision, String justification) {}

    // Section 4 Step 11: Human review and correction decision
    @PostMapping("/findings/{findingId}/review")
    public ResponseEntity<?> recordReviewDecision(@PathVariable Long findingId,
                                                  @RequestBody ReviewDecisionRequest request) {
        return findingRepository.findById(findingId).map(f -> {
            f.setReviewerDecision(request.decision());
            f.setReviewerJustification(request.justification());
            ValidationFinding updated = findingRepository.save(f);
            return ResponseEntity.ok(Map.of(
                "findingId", updated.getId(),
                "status", "RECORDED",
                "decision", updated.getReviewerDecision(),
                "justification", updated.getReviewerJustification()
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}
