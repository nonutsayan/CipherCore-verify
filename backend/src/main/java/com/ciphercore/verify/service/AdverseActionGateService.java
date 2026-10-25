package com.ciphercore.verify.service;

import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.OfficialDecision;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.OfficialDecisionOutcome;
import com.ciphercore.verify.model.enums.UserRole;
import com.ciphercore.verify.repository.ApplicationRepository;
import com.ciphercore.verify.service.OfficialDecisionService.RecordDecisionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdverseActionGateService {

    private final OfficialDecisionService officialDecisionService;
    private final ApplicationRepository applicationRepository;
    private final AccessControlService accessControlService;
    private final SecurityMonitoringService securityMonitoringService;
    private final AuditTrailService auditTrailService;

    public AdverseActionGateService(OfficialDecisionService officialDecisionService,
                                    ApplicationRepository applicationRepository,
                                    AccessControlService accessControlService,
                                    SecurityMonitoringService securityMonitoringService,
                                    AuditTrailService auditTrailService) {
        this.officialDecisionService = officialDecisionService;
        this.applicationRepository = applicationRepository;
        this.accessControlService = accessControlService;
        this.securityMonitoringService = securityMonitoringService;
        this.auditTrailService = auditTrailService;
    }

    public boolean isAdverseAction(OfficialDecisionOutcome outcome) {
        if (outcome == null) return false;
        return outcome == OfficialDecisionOutcome.REJECTED_REFUSAL_OF_APPROVAL ||
               outcome == OfficialDecisionOutcome.DEFICIENCY_NOTICE_ISSUED ||
               outcome == OfficialDecisionOutcome.SPOT_INSPECTION_MANDATED;
    }

    @Transactional
    public OfficialDecision commitRegulatoryDecision(Long applicationId, User caller,
                                                     OfficialDecisionOutcome outcome,
                                                     String statutoryRationale,
                                                     String conditionsPrecedent,
                                                     String committeeReference,
                                                     Boolean spotInspectionRequired,
                                                     String ipAddress) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // 1. Enforce Human Gate: Background AI or automated service cannot commit adverse regulatory actions
        if (caller == null || caller.getRole() == UserRole.ROLE_SERVICE_AI) {
            String callerName = caller != null ? caller.getUsername() : "service_ai";
            securityMonitoringService.logSecurityEvent(
                    "ADVERSE_ACTION_ATTEMPTED_BLOCKED", callerName, "ROLE_SERVICE_AI",
                    "APPLICATION", String.valueOf(applicationId),
                    "COMMIT_REGULATORY_DECISION", "DENIED",
                    "AUTOMATED_IDENTITY_BLOCKED",
                    "AI service or unauthenticated process attempted to commit regulatory disposition " + outcome +
                    ". Adverse actions strictly require human scrutiny officer authorization.",
                    ipAddress
            );
            throw new SecurityException("Automated AI service or background process cannot commit regulatory decisions. Mandatory human official sign-off is required by AICTE regulations.");
        }

        // 2. Enforce Role-Based Permission
        if (!accessControlService.canCommitAdverseAction(caller)) {
            securityMonitoringService.logSecurityEvent(
                    "ADVERSE_ACTION_ATTEMPTED_BLOCKED", caller.getUsername(), caller.getRole().name(),
                    "APPLICATION", String.valueOf(applicationId),
                    "COMMIT_REGULATORY_DECISION", "DENIED",
                    "UNAUTHORIZED_ROLE",
                    "User '" + caller.getUsername() + "' (" + caller.getRole().name() +
                    ") lacks executive regulatory authority to record official decision.",
                    ipAddress
            );
            throw new SecurityException("Unauthorized: Role " + caller.getRole() + " is not authorized to finalize regulatory decisions. Required: SENIOR_REVIEWER or REGULATORY_OFFICER.");
        }

        // 3. Enforce Separation of Duties: Submitter/Applicant cannot approve their own application
        if (app.getCreatedBy() != null && app.getCreatedBy().equalsIgnoreCase(caller.getUsername())) {
            securityMonitoringService.logSecurityEvent(
                    "ADVERSE_ACTION_ATTEMPTED_BLOCKED", caller.getUsername(), caller.getRole().name(),
                    "APPLICATION", String.valueOf(applicationId),
                    "COMMIT_REGULATORY_DECISION", "DENIED",
                    "SEPARATION_OF_DUTIES_CONFLICT",
                    "Conflict of Interest: Submitter '" + caller.getUsername() + "' cannot record official disposition on their own application.",
                    ipAddress
            );
            throw new SecurityException("Conflict of Interest: An applicant or creator cannot adjudicate or record regulatory decisions on their own application.");
        }

        // 4. Enforce Non-Empty Statutory Justification (minimum 20 chars citing APH regulations)
        if (statutoryRationale == null || statutoryRationale.trim().length() < 20) {
            throw new IllegalArgumentException("A comprehensive statutory rationale citing specific AICTE Approval Process Handbook (APH) clauses is mandatory (minimum 20 characters required).");
        }

        // 5. Delegate to OfficialDecisionService to record and compute cryptographic SHA-256 seal
        RecordDecisionRequest req = new RecordDecisionRequest(
                applicationId,
                outcome,
                statutoryRationale,
                conditionsPrecedent,
                spotInspectionRequired,
                committeeReference
        );

        OfficialDecision decision = officialDecisionService.recordDecision(req, caller.getFullName(), caller.getRole().name());

        // 6. Record Security Event & Tamper-Evident Audit Trail
        securityMonitoringService.logSecurityEvent(
                "ADVERSE_ACTION_APPROVED", caller.getUsername(), caller.getRole().name(),
                "APPLICATION", String.valueOf(applicationId),
                "COMMIT_REGULATORY_DECISION", "SUCCESS",
                "HUMAN_APPROVAL_SEALED",
                "Official regulatory disposition recorded: " + outcome.name() +
                ". Decision Seal: " + decision.getDecisionIntegritySeal(),
                ipAddress
        );

        auditTrailService.recordEvent(
                caller.getUsername(), caller.getRole().name(),
                "OFFICIAL_REGULATORY_DECISION_SEALED",
                "APPLICATION", String.valueOf(applicationId),
                "Disposition: " + outcome.name() + " | Official: " + caller.getFullName() +
                " | SHA-256 Seal: " + decision.getDecisionIntegritySeal() +
                " | Rationale: " + statutoryRationale,
                ipAddress
        );

        return decision;
    }
}
