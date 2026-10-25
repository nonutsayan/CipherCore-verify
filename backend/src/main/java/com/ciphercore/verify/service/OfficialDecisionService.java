package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Deliverable 6: Official Scrutiny Decision Service
 * Implements readiness assessment, statutory disposition recording,
 * SHA-256 cryptographic decision sealing, and audit logging.
 */
@Service
@Transactional
public class OfficialDecisionService {

    private final OfficialDecisionRepository decisionRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditEventRepository auditEventRepository;
    private final NotificationRepository notificationRepository;
    private final WorkflowTrackerService workflowTrackerService;

    public OfficialDecisionService(OfficialDecisionRepository decisionRepository,
                                   ApplicationRepository applicationRepository,
                                   AuditEventRepository auditEventRepository,
                                   NotificationRepository notificationRepository,
                                   WorkflowTrackerService workflowTrackerService) {
        this.decisionRepository = decisionRepository;
        this.applicationRepository = applicationRepository;
        this.auditEventRepository = auditEventRepository;
        this.notificationRepository = notificationRepository;
        this.workflowTrackerService = workflowTrackerService;
    }

    public record PrerequisiteCheck(
            String name,
            boolean satisfied,
            String observedCondition,
            String statutoryCitation
    ) {}

    public record ReadinessAssessmentResult(
            Long applicationId,
            String applicationRef,
            String institutionName,
            boolean isReadyForDecision,
            ReadinessState readinessState,
            List<PrerequisiteCheck> checklist,
            List<String> blockers,
            String recommendation,
            Optional<OfficialDecision> existingDecision
    ) {}

    public record RecordDecisionRequest(
            Long applicationId,
            OfficialDecisionOutcome outcome,
            String statutoryRationale,
            String conditionsPrecedent,
            Boolean spotInspectionRequired,
            String committeeReference
    ) {}

    @Transactional(readOnly = true)
    public ReadinessAssessmentResult assessReadiness(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        WorkflowTrackerService.ApplicationWorkflowSummary wf = workflowTrackerService.getWorkflowSummary(applicationId);
        Optional<OfficialDecision> existingDecision = decisionRepository.findFirstByApplicationIdOrderByRecordedAtDesc(applicationId);

        List<PrerequisiteCheck> checklist = new ArrayList<>();
        List<String> blockers = new ArrayList<>();

        // Check 1: Stage 1 Application Profile
        boolean s1Satisfied = wf.stages().get(0).status() == WorkflowStageStatus.COMPLETED;
        checklist.add(new PrerequisiteCheck(
                "Application Profile & Classification",
                s1Satisfied,
                wf.stages().get(0).summary(),
                "AICTE APH 2024-27 Clause 2.1"
        ));
        if (!s1Satisfied) blockers.addAll(wf.stages().get(0).blockers());

        // Check 2: Stage 2 Documents Completeness
        boolean s2Satisfied = wf.stages().get(1).status() == WorkflowStageStatus.COMPLETED;
        checklist.add(new PrerequisiteCheck(
                "Statutory Document Completeness & Expiry Verification",
                s2Satisfied,
                wf.stages().get(1).summary(),
                "AICTE APH 2024-27 Clause 4.1"
        ));
        if (!s2Satisfied) blockers.addAll(wf.stages().get(1).blockers());

        // Check 3: Stage 3 Validation & Consistency
        boolean s3Satisfied = wf.stages().get(2).status() == WorkflowStageStatus.COMPLETED;
        checklist.add(new PrerequisiteCheck(
                "Application Completeness & Cross-Field Consistency",
                s3Satisfied,
                wf.stages().get(2).summary(),
                "AICTE APH 2024-27 Clause 3.4"
        ));
        if (!s3Satisfied) blockers.addAll(wf.stages().get(2).blockers());

        // Check 4: Stage 4 Issuing Authority Corroboration
        boolean s4Satisfied = wf.stages().get(3).status() == WorkflowStageStatus.COMPLETED;
        checklist.add(new PrerequisiteCheck(
                "Issuing Authority Independent Corroboration",
                s4Satisfied,
                wf.stages().get(3).summary(),
                "AICTE Digital Scrutiny Directives 2024"
        ));
        if (!s4Satisfied) blockers.addAll(wf.stages().get(3).blockers());

        // Check 5: Stage 5 Regulatory Compliance & CV Infrastructure
        boolean s5Satisfied = wf.stages().get(4).status() == WorkflowStageStatus.COMPLETED;
        checklist.add(new PrerequisiteCheck(
                "Regulatory Norms & Infrastructure Standards (APH 2024-27 & NBC 2016)",
                s5Satisfied,
                wf.stages().get(4).summary(),
                "AICTE APH 2024-27 Appendices 4 & 6"
        ));
        if (!s5Satisfied) blockers.addAll(wf.stages().get(4).blockers());

        boolean ready = blockers.isEmpty();
        ReadinessState state = existingDecision.isPresent()
                ? ReadinessState.DECISION_RECORDED
                : (ready ? ReadinessState.READY_FOR_REVIEW : ReadinessState.NOT_READY);

        String recommendation = existingDecision.map(d -> "Decision recorded: " + d.getOutcome().getDisplayTitle())
                .orElse(ready ? "All statutory checks satisfied. Convene Scrutiny Hearing for formal EoA disposition."
                              : "Prerequisites incomplete. Review and rectify outstanding blockers before proceeding to final decision.");

        return new ReadinessAssessmentResult(
                applicationId,
                app.getApplicationRef(),
                app.getInstitutionName(),
                ready,
                state,
                checklist,
                blockers,
                recommendation,
                existingDecision
        );
    }

    public OfficialDecision recordDecision(RecordDecisionRequest req, String officerUsername, String officerRole) {
        Application app = applicationRepository.findById(req.applicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + req.applicationId()));

        if (req.statutoryRationale() == null || req.statutoryRationale().trim().length() < 10) {
            throw new IllegalArgumentException("Mandatory statutory rationale must be supplied by the scrutiny officer (min 10 characters).");
        }

        LocalDateTime now = LocalDateTime.now();
        String committeeRef = (req.committeeReference() != null && !req.committeeReference().isBlank())
                ? req.committeeReference()
                : "AICTE-SC-WRO-2026/04";

        String officer = (officerUsername != null && !officerUsername.isBlank())
                ? officerUsername
                : "Dr. Rajesh Sharma (Scrutiny Member)";

        String role = (officerRole != null && !officerRole.isBlank()) ? officerRole : "COMPLIANCE_OFFICER";

        // Compute SHA-256 seal
        String sealData = app.getApplicationRef() + ":" + req.outcome().name() + ":" + officer + ":" +
                req.statutoryRationale() + ":" + now;
        String seal = computeSha256(sealData);

        OfficialDecision decision = new OfficialDecision();
        decision.setApplicationId(app.getId());
        decision.setApplicationRef(app.getApplicationRef());
        decision.setInstitutionName(app.getInstitutionName());
        decision.setOutcome(req.outcome());
        decision.setOfficerName(officer);
        decision.setOfficerRole(role);
        decision.setStatutoryRationale(req.statutoryRationale());
        decision.setConditionsPrecedent(req.conditionsPrecedent());
        decision.setAcademicYear(app.getAcademicYear());
        decision.setSpotInspectionRequired(Boolean.TRUE.equals(req.spotInspectionRequired()));
        decision.setCommitteeReference(committeeRef);
        decision.setDecisionIntegritySeal(seal);
        decision.setRecordedAt(now);

        OfficialDecision saved = decisionRepository.save(decision);

        // Update application state
        if (req.outcome() == OfficialDecisionOutcome.APPROVED_UNCONDITIONAL_EOA ||
            req.outcome() == OfficialDecisionOutcome.APPROVED_CONDITIONAL_EOA) {
            app.setStatus(ApplicationStatus.APPROVED);
            app.setOverallValidationStatus(FindingStatus.PASS);
        } else if (req.outcome() == OfficialDecisionOutcome.DEFICIENCY_NOTICE_ISSUED) {
            app.setStatus(ApplicationStatus.UNDER_REVIEW);
            app.setOverallValidationStatus(FindingStatus.DEFICIENCY);
        } else if (req.outcome() == OfficialDecisionOutcome.REJECTED_REFUSAL_OF_APPROVAL) {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setOverallValidationStatus(FindingStatus.DEFICIENCY);
        } else {
            app.setStatus(ApplicationStatus.UNDER_REVIEW);
        }
        app.setUpdatedAt(now);
        applicationRepository.save(app);

        // Log audit event
        AuditEvent audit = new AuditEvent();
        audit.setEntityName("OFFICIAL_DECISION");
        audit.setEntityId(String.valueOf(saved.getId()));
        audit.setAction("OFFICIAL_DECISION_RECORDED");
        audit.setUsername(officer);
        audit.setUserRole(role);
        audit.setDetails("Recorded official decision: " + req.outcome().name() + " with SHA-256 Seal: " + seal);
        audit.setEventTimestamp(now);
        auditEventRepository.save(audit);

        // Notify applicant & regional office
        Notification notif = new Notification(
                app.getCreatedBy() != null ? app.getCreatedBy() : "applicant",
                app.getId(),
                app.getApplicationRef(),
                NotificationType.DECISION_RECORDED,
                Severity.MAJOR,
                "Official AICTE Scrutiny Decision Recorded: " + req.outcome().getDisplayTitle(),
                "Scrutiny Committee " + committeeRef + " has recorded formal disposition. SHA-256 Seal: " + seal,
                "/#decision"
        );
        notificationRepository.save(notif);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<OfficialDecision> getDecisionsForApplication(Long applicationId) {
        return decisionRepository.findByApplicationIdOrderByRecordedAtDesc(applicationId);
    }

    private String computeSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
