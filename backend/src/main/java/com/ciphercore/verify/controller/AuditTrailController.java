package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.repository.AuditEventRepository;
import com.ciphercore.verify.service.AuditTrailService;
import com.ciphercore.verify.service.AuditTrailService.AuditIntegrityResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class AuditTrailController {

    private final AuditTrailService auditTrailService;
    private final AuditEventRepository auditEventRepository;

    public record TamperRequest(Long sequenceNumber, String modifiedDetails) {}
    public record RestoreRequest(Long sequenceNumber, String originalDetails) {}

    public AuditTrailController(AuditTrailService auditTrailService,
                                AuditEventRepository auditEventRepository) {
        this.auditTrailService = auditTrailService;
        this.auditEventRepository = auditEventRepository;
    }

    @GetMapping("/api/audit/events")
    public ResponseEntity<List<AuditEvent>> getAuditEvents(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(auditEventRepository.findTop100ByOrderByEventTimestampDesc());
    }

    @GetMapping("/api/audit/integrity")
    public ResponseEntity<AuditIntegrityResult> getAuditIntegrity() {
        return ResponseEntity.ok(auditTrailService.verifyAuditChainIntegrity());
    }

    @PostMapping("/api/audit/integrity/check")
    public ResponseEntity<AuditIntegrityResult> checkAuditIntegrity() {
        return ResponseEntity.ok(auditTrailService.verifyAuditChainIntegrity());
    }

    @PostMapping("/api/audit/simulate-tamper")
    public ResponseEntity<?> simulateTamper(@RequestBody TamperRequest req) {
        boolean ok = auditTrailService.simulateTamper(req.sequenceNumber(), req.modifiedDetails());
        if (ok) {
            AuditIntegrityResult res = auditTrailService.verifyAuditChainIntegrity();
            return ResponseEntity.ok(Map.of(
                    "status", "TAMPER_SIMULATED",
                    "sequenceNumber", req.sequenceNumber(),
                    "verificationResult", res
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Record with sequence #" + req.sequenceNumber() + " not found"));
    }

    @PostMapping("/api/audit/restore-tamper")
    public ResponseEntity<?> restoreTamper(@RequestBody RestoreRequest req) {
        boolean ok = auditTrailService.restoreTamper(req.sequenceNumber(), req.originalDetails());
        if (ok) {
            AuditIntegrityResult res = auditTrailService.verifyAuditChainIntegrity();
            return ResponseEntity.ok(Map.of(
                    "status", "TAMPER_RESTORED",
                    "sequenceNumber", req.sequenceNumber(),
                    "verificationResult", res
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Record with sequence #" + req.sequenceNumber() + " not found"));
    }

    @GetMapping("/api/audit/export")
    public ResponseEntity<byte[]> exportAuditCsv(@RequestParam(defaultValue = "admin") String username) {
        String csv = auditTrailService.exportAuditTrailCsv(username);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ciphercore_audit_trail_export.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping("/api/applications/{id}/audit-timeline")
    public ResponseEntity<List<AuditEvent>> getApplicationTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(auditEventRepository.findByEntityNameAndEntityIdOrderByEventTimestampDesc("APPLICATION", String.valueOf(id)));
    }
}
