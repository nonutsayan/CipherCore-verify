package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.OfficialDecision;
import com.ciphercore.verify.service.OfficialDecisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<OfficialDecision> recordDecision(
            @PathVariable Long id,
            @RequestBody OfficialDecisionService.RecordDecisionRequest req,
            @RequestHeader(value = "X-Officer-Name", defaultValue = "Dr. Rajesh Sharma (Scrutiny Member)") String officerName,
            @RequestHeader(value = "X-Officer-Role", defaultValue = "COMPLIANCE_OFFICER") String officerRole) {
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

    @GetMapping("/{id}/decisions")
    public ResponseEntity<List<OfficialDecision>> getDecisions(@PathVariable Long id) {
        return ResponseEntity.ok(officialDecisionService.getDecisionsForApplication(id));
    }
}
