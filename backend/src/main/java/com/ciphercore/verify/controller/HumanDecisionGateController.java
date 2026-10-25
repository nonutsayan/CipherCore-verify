package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.OfficialDecision;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.OfficialDecisionOutcome;
import com.ciphercore.verify.service.AdverseActionGateService;
import com.ciphercore.verify.service.AuthService;
import com.ciphercore.verify.service.OfficialDecisionService;
import com.ciphercore.verify.service.OfficialDecisionService.ReadinessAssessmentResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
public class HumanDecisionGateController {

    private final AdverseActionGateService adverseActionGateService;
    private final OfficialDecisionService officialDecisionService;
    private final AuthService authService;

    public record AdverseDecisionRequest(
            String outcome,
            String statutoryRationale,
            String conditionsPrecedent,
            String committeeReference,
            Boolean spotInspectionRequired
    ) {}

    public HumanDecisionGateController(AdverseActionGateService adverseActionGateService,
                                       OfficialDecisionService officialDecisionService,
                                       AuthService authService) {
        this.adverseActionGateService = adverseActionGateService;
        this.officialDecisionService = officialDecisionService;
        this.authService = authService;
    }

    @GetMapping("/api/applications/{id}/adverse-action/readiness")
    public ResponseEntity<ReadinessAssessmentResult> getReadiness(@PathVariable Long id) {
        return ResponseEntity.ok(officialDecisionService.assessReadiness(id));
    }

    @PostMapping("/api/applications/{id}/adverse-action/validate-and-commit")
    public ResponseEntity<?> validateAndCommitAdverseAction(@PathVariable Long id,
                                                           @RequestBody AdverseDecisionRequest req,
                                                           @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                           @RequestParam(defaultValue = "officer_sc401") String username,
                                                           HttpServletRequest httpRequest) {
        User caller = resolveUser(authHeader, username);
        if (caller == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required to commit regulatory decisions"));
        }

        OfficialDecisionOutcome outcome;
        try {
            outcome = OfficialDecisionOutcome.valueOf(req.outcome());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid decision outcome: " + req.outcome()));
        }

        String ip = getClientIp(httpRequest);
        try {
            OfficialDecision decision = adverseActionGateService.commitRegulatoryDecision(
                    id, caller, outcome, req.statutoryRationale(), req.conditionsPrecedent(),
                    req.committeeReference(), req.spotInspectionRequired(), ip
            );

            return ResponseEntity.ok(Map.of(
                    "status", "DECISION_COMMITTED_AND_SEALED",
                    "applicationId", id,
                    "outcome", decision.getOutcome().name(),
                    "officerName", decision.getOfficerName(),
                    "officerRole", decision.getOfficerRole(),
                    "decisionIntegritySeal", decision.getDecisionIntegritySeal(),
                    "statutoryRationale", decision.getStatutoryRationale(),
                    "recordedAt", decision.getRecordedAt()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", e.getMessage(),
                    "status", 403,
                    "code", "HUMAN_GATE_OR_AUTHORIZATION_VIOLATION"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "status", 400,
                    "code", "STATUTORY_RATIONALE_DEFICIENCY"
            ));
        }
    }

    private User resolveUser(String authHeader, String usernameParam) {
        String identifier = authHeader != null && !authHeader.isBlank() ? authHeader : usernameParam;
        return authService.resolveUser(identifier).orElse(null);
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }
}
