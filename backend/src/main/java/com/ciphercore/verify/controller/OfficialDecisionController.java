package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.OfficialDecision;
import com.ciphercore.verify.service.OfficialDecisionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Deliverable 6: Official Review & Decision Controller
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class OfficialDecisionController {

    private final OfficialDecisionService officialDecisionService;

    public OfficialDecisionController(OfficialDecisionService officialDecisionService) {
        this.officialDecisionService = officialDecisionService;
    }

    @GetMapping("/{id}/review")
    public ResponseEntity<OfficialDecisionService.ReadinessAssessmentResult> getReviewStatus(@PathVariable Long id) {
        return ResponseEntity.ok(officialDecisionService.assessReadiness(id));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<?> recordDecision(
            @PathVariable Long id,
            @RequestBody OfficialDecisionService.RecordDecisionRequest req,
            @RequestHeader(value = "X-Officer-Name", defaultValue = "Dr. Rajesh Sharma (Scrutiny Member)") String officerName,
            @RequestHeader(value = "X-Officer-Role", defaultValue = "COMPLIANCE_OFFICER") String officerRole) {
        // Enforce role-based access control: Only authorized scrutiny officers, reviewers, or admins can record official decisions
        if (officerRole == null || !isAuthorizedOfficerRole(officerRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access Denied: Institutional applicants and unauthorized roles cannot record statutory decisions.",
                    "status", 403,
                    "providedRole", String.valueOf(officerRole)
            ));
        }

        // Enforce matching ID
        OfficialDecisionService.RecordDecisionRequest safeReq = new OfficialDecisionService.RecordDecisionRequest(
                id,
                req.outcome(),
                req.statutoryRationale(),
                req.conditionsPrecedent(),
                req.spotInspectionRequired(),
                req.committeeReference()
        );
        return ResponseEntity.ok(officialDecisionService.recordDecision(safeReq, officerName, officerRole));
    }

    private boolean isAuthorizedOfficerRole(String role) {
        String r = role.toUpperCase();
        return r.contains("OFFICER") || r.contains("ADMIN") || r.contains("REVIEWER") || r.contains("AUDITOR");
    }

    @GetMapping("/{id}/decisions")
    public ResponseEntity<List<OfficialDecision>> getDecisions(@PathVariable Long id) {
        return ResponseEntity.ok(officialDecisionService.getDecisionsForApplication(id));
    }
}
