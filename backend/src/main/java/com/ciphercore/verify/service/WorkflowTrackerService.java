package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Deliverable 6: Application Workflow Tracker Service
 * Calculates dynamic, evidence-backed stage states across:
 * Application → Documents → Validation → Verification → Compliance → Final Review
 */
@Service
@Transactional(readOnly = true)
public class WorkflowTrackerService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ValidationRuleRepository validationRuleRepository;
    private final ValidationFindingRepository validationFindingRepository;
    private final VerificationResultRepository verificationResultRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final ApplicationComplianceAssessmentRepository complianceAssessmentRepository;
    private final RuleEvaluationResultRepository ruleEvaluationResultRepository;
    private final InfrastructureObservationRepository infrastructureObservationRepository;
    private final OfficialDecisionRepository officialDecisionRepository;
    private final ActionItemRepository actionItemRepository;

    public WorkflowTrackerService(ApplicationRepository applicationRepository,
                                  DocumentRepository documentRepository,
                                  ValidationRuleRepository validationRuleRepository,
                                  ValidationFindingRepository validationFindingRepository,
                                  VerificationResultRepository verificationResultRepository,
                                  VerificationRecordRepository verificationRecordRepository,
                                  ApplicationComplianceAssessmentRepository complianceAssessmentRepository,
                                  RuleEvaluationResultRepository ruleEvaluationResultRepository,
                                  InfrastructureObservationRepository infrastructureObservationRepository,
                                  OfficialDecisionRepository officialDecisionRepository,
                                  ActionItemRepository actionItemRepository) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.validationRuleRepository = validationRuleRepository;
        this.validationFindingRepository = validationFindingRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.complianceAssessmentRepository = complianceAssessmentRepository;
        this.ruleEvaluationResultRepository = ruleEvaluationResultRepository;
        this.infrastructureObservationRepository = infrastructureObservationRepository;
        this.officialDecisionRepository = officialDecisionRepository;
        this.actionItemRepository = actionItemRepository;
    }

    public record StageDetail(
            WorkflowStage stage,
            String stageName,
            WorkflowStageStatus status,
            String summary,
            List<String> blockers,
            String recommendedNextAction,
            Map<String, Object> metrics
    ) {}

    public record ApplicationWorkflowSummary(
            Long applicationId,
            String applicationRef,
            String institutionName,
            WorkflowStage currentStage,
            WorkflowStageStatus currentStageStatus,
            ReadinessState readinessState,
            List<StageDetail> stages,
            boolean isReadyForOfficialReview,
            List<String> outstandingBlockers,
            String officialDispositionSummary
    ) {}

    public ApplicationWorkflowSummary getWorkflowSummary(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found with ID: " + applicationId));

        List<Document> docs = documentRepository.findByApplicationId(applicationId);
        List<ValidationFinding> findings = validationFindingRepository.findByApplicationId(applicationId);
        List<VerificationRecord> verifRecords = verificationRecordRepository.findByDocumentIdIn(
                docs.stream().map(Document::getId).toList()
        );
        Optional<ApplicationComplianceAssessment> assessmentOpt = complianceAssessmentRepository
                .findFirstByApplicationIdOrderByCreatedAtDesc(applicationId);
        Optional<OfficialDecision> decisionOpt = officialDecisionRepository
                .findFirstByApplicationIdOrderByRecordedAtDesc(applicationId);

        // 1. Stage 1: Application Intake
        StageDetail stage1 = evaluateStageApplication(app);

        // 2. Stage 2: Documents Completeness & OCR
        StageDetail stage2 = evaluateStageDocuments(app, docs);

        // 3. Stage 3: Validation & Consistency
        StageDetail stage3 = evaluateStageValidation(app, docs, findings);

        // 4. Stage 4: Issuing Authority Verification
        StageDetail stage4 = evaluateStageVerification(app, docs, verifRecords);

        // 5. Stage 5: Regulatory & Infrastructure Compliance
        StageDetail stage5 = evaluateStageCompliance(app, assessmentOpt);

        // 6. Stage 6: Scrutiny Committee & Final Official Review
        StageDetail stage6 = evaluateStageFinalReview(app, stage1, stage2, stage3, stage4, stage5, decisionOpt);

        List<StageDetail> stages = List.of(stage1, stage2, stage3, stage4, stage5, stage6);

        // Determine current workflow stage
        WorkflowStage currentStage = WorkflowStage.APPLICATION;
        WorkflowStageStatus currentStatus = WorkflowStageStatus.COMPLETED;

        for (StageDetail s : stages) {
            currentStage = s.stage();
            currentStatus = s.status();
            if (s.status() != WorkflowStageStatus.COMPLETED) {
                break;
            }
        }

        List<String> allBlockers = new ArrayList<>();
        stages.forEach(s -> allBlockers.addAll(s.blockers()));

        boolean isReady = stage1.status() == WorkflowStageStatus.COMPLETED &&
                          (stage2.status() == WorkflowStageStatus.COMPLETED || stage2.blockers().isEmpty()) &&
                          stage3.blockers().isEmpty() &&
                          stage4.blockers().isEmpty() &&
                          stage5.blockers().isEmpty() &&
                          decisionOpt.isEmpty();

        ReadinessState readiness;
        if (decisionOpt.isPresent()) {
            readiness = ReadinessState.DECISION_RECORDED;
        } else if (isReady) {
            readiness = ReadinessState.READY_FOR_REVIEW;
        } else if (!allBlockers.isEmpty()) {
            readiness = ReadinessState.NOT_READY;
        } else {
            readiness = ReadinessState.UNDER_REVIEW;
        }

        String dispositionSummary = decisionOpt
                .map(d -> d.getOutcome().getDisplayTitle() + " by " + d.getOfficerName())
                .orElse(isReady ? "Ready for Scrutiny Committee Official Hearing" : "Pending resolution of " + allBlockers.size() + " prerequisite blockers");

        return new ApplicationWorkflowSummary(
                app.getId(),
                app.getApplicationRef(),
                app.getInstitutionName(),
                currentStage,
                currentStatus,
                readiness,
                stages,
                isReady,
                allBlockers,
                dispositionSummary
        );
    }

    private StageDetail evaluateStageApplication(Application app) {
        List<String> blockers = new ArrayList<>();
        if (app.getInstituteCode() == null || app.getInstituteCode().isBlank()) {
            blockers.add("Missing permanent institute identifier code");
        }
        if (app.getCategoryCode() == null || app.getCategoryCode().isBlank()) {
            blockers.add("Application category not determined");
        }

        WorkflowStageStatus status = blockers.isEmpty() ? WorkflowStageStatus.COMPLETED : WorkflowStageStatus.BLOCKED;
        String summary = status == WorkflowStageStatus.COMPLETED
                ? "Application intake confirmed under category " + app.getCategoryCode()
                : "Application profile incomplete";

        return new StageDetail(
                WorkflowStage.APPLICATION,
                WorkflowStage.APPLICATION.getDescription(),
                status,
                summary,
                blockers,
                blockers.isEmpty() ? "Proceed to document checklist review" : "Complete institutional identity profile",
                Map.of(
                        "categoryCode", app.getCategoryCode() != null ? app.getCategoryCode() : "N/A",
                        "academicYear", app.getAcademicYear() != null ? app.getAcademicYear() : "N/A",
                        "region", app.getRegion() != null ? app.getRegion() : "N/A"
                )
        );
    }

    private StageDetail evaluateStageDocuments(Application app, List<Document> docs) {
        List<String> blockers = new ArrayList<>();
        List<ValidationRule> docRules = validationRuleRepository.findByCategoryCodeOrCategoryCode(app.getCategoryCode(), "ALL");
        long mandatoryDocRules = docRules.stream().filter(r -> r.getRuleType().startsWith("MANDATORY_DOC_")).count();

        long uploadedCount = docs.size();
        long extractedCount = docs.stream().filter(d -> "EXTRACTED".equalsIgnoreCase(d.getExtractionStatus())).count();

        // Check expired docs
        boolean hasExpired = docs.stream().anyMatch(d ->
                d.getExtractedText() != null && d.getExtractedText().toUpperCase().contains("EXPIRED"));

        if (uploadedCount == 0 && mandatoryDocRules > 0) {
            blockers.add("No mandatory statutory documents uploaded (" + mandatoryDocRules + " required)");
        }
        if (hasExpired) {
            blockers.add("One or more mandatory documents have expired validity dates");
        }

        WorkflowStageStatus status;
        if (hasExpired || uploadedCount == 0) {
            status = WorkflowStageStatus.ACTION_REQUIRED;
        } else if (extractedCount < uploadedCount) {
            status = WorkflowStageStatus.IN_PROGRESS;
        } else {
            status = WorkflowStageStatus.COMPLETED;
        }

        return new StageDetail(
                WorkflowStage.DOCUMENTS,
                WorkflowStage.DOCUMENTS.getDescription(),
                status,
                "Uploaded " + uploadedCount + " documents (" + extractedCount + " OCR extracted)",
                blockers,
                hasExpired ? "Issue deficiency notice to re-upload renewed statutory certificates" : "Proceed to consistency checks",
                Map.of(
                        "totalUploaded", uploadedCount,
                        "ocrExtracted", extractedCount,
                        "hasExpiredDocs", hasExpired
                )
        );
    }

    private StageDetail evaluateStageValidation(Application app, List<Document> docs, List<ValidationFinding> findings) {
        List<String> blockers = new ArrayList<>();
        long deficiencies = findings.stream().filter(f -> f.getSeverity() == Severity.CRITICAL).count();

        if (deficiencies > 0) {
            blockers.add(deficiencies + " critical validation deficiencies detected in institutional dossier");
        }

        WorkflowStageStatus status;
        if (deficiencies > 0) {
            status = WorkflowStageStatus.ACTION_REQUIRED;
        } else if (docs.isEmpty()) {
            status = WorkflowStageStatus.NOT_STARTED;
        } else {
            status = WorkflowStageStatus.COMPLETED;
        }

        return new StageDetail(
                WorkflowStage.VALIDATION,
                WorkflowStage.VALIDATION.getDescription(),
                status,
                findings.isEmpty() ? "All deterministic checks passed" : findings.size() + " findings logged",
                blockers,
                deficiencies > 0 ? "Resolve cross-field discrepancies or supply supporting affidavits" : "Proceed to authority verification",
                Map.of(
                        "totalFindings", findings.size(),
                        "criticalDeficiencies", deficiencies
                )
        );
    }

    private StageDetail evaluateStageVerification(Application app, List<Document> docs, List<VerificationRecord> verifRecords) {
        List<String> blockers = new ArrayList<>();
        long verified = verifRecords.stream().filter(v -> v.getVerificationStatus() == VerificationStatus.VERIFIED_MATCH).count();
        long mismatches = verifRecords.stream().filter(v -> v.getVerificationStatus() == VerificationStatus.RECORD_CONFLICT || v.getVerificationStatus() == VerificationStatus.RECORD_NOT_FOUND).count();

        if (mismatches > 0) {
            blockers.add(mismatches + " issuing authority record mismatches or uncorroborated certificates");
        }

        WorkflowStageStatus status;
        if (mismatches > 0) {
            status = WorkflowStageStatus.ACTION_REQUIRED;
        } else if (verifRecords.isEmpty() && !docs.isEmpty()) {
            status = WorkflowStageStatus.IN_PROGRESS;
        } else {
            status = WorkflowStageStatus.COMPLETED;
        }

        return new StageDetail(
                WorkflowStage.VERIFICATION,
                WorkflowStage.VERIFICATION.getDescription(),
                status,
                verifRecords.isEmpty() ? "Verification checks ready" : verified + " of " + verifRecords.size() + " records corroborated",
                blockers,
                mismatches > 0 ? "Escalate uncorroborated records to issuing directorate" : "Proceed to regulatory assessment",
                Map.of(
                        "totalChecked", verifRecords.size(),
                        "verifiedMatches", verified,
                        "mismatches", mismatches
                )
        );
    }

    private StageDetail evaluateStageCompliance(Application app, Optional<ApplicationComplianceAssessment> assessmentOpt) {
        List<String> blockers = new ArrayList<>();

        if (assessmentOpt.isEmpty()) {
            return new StageDetail(
                    WorkflowStage.COMPLIANCE,
                    WorkflowStage.COMPLIANCE.getDescription(),
                    WorkflowStageStatus.NOT_STARTED,
                    "Regulatory assessment pending evaluation",
                    List.of("AICTE regulatory assessment has not been evaluated"),
                    "Trigger regulatory rule engine evaluation",
                    Map.of("evaluated", false)
            );
        }

        ApplicationComplianceAssessment assessment = assessmentOpt.get();
        if (assessment.getDeficiencyCount() > 0) {
            blockers.add(assessment.getDeficiencyCount() + " AICTE statutory deficiencies pending committee resolution");
        }

        // Check if there are severe infrastructure discrepancies
        List<InfrastructureObservation> discrepancies = infrastructureObservationRepository
                .findDiscrepanciesByApplicationId(app.getId());
        if (!discrepancies.isEmpty()) {
            blockers.add(discrepancies.size() + " campus infrastructure discrepancies detected (e.g. ramp gradient)");
        }

        WorkflowStageStatus status = blockers.isEmpty() ? WorkflowStageStatus.COMPLETED : WorkflowStageStatus.ACTION_REQUIRED;

        return new StageDetail(
                WorkflowStage.COMPLIANCE,
                WorkflowStage.COMPLIANCE.getDescription(),
                status,
                "Score: " + assessment.getOverallComplianceScore() + "% (" + assessment.getPassedCount() + " passed, " + assessment.getDeficiencyCount() + " deficiencies)",
                blockers,
                blockers.isEmpty() ? "Ready for final Scrutiny Committee review" : "Officer review or deficiency rectification required",
                Map.of(
                        "score", assessment.getOverallComplianceScore(),
                        "passedCount", assessment.getPassedCount(),
                        "deficiencyCount", assessment.getDeficiencyCount(),
                        "discrepanciesCount", discrepancies.size(),
                        "assessmentSeal", assessment.getAssessmentHash() != null ? assessment.getAssessmentHash() : "N/A"
                )
        );
    }

    private StageDetail evaluateStageFinalReview(Application app,
                                                 StageDetail s1, StageDetail s2, StageDetail s3,
                                                 StageDetail s4, StageDetail s5,
                                                 Optional<OfficialDecision> decisionOpt) {
        List<String> blockers = new ArrayList<>();
        if (decisionOpt.isPresent()) {
            OfficialDecision d = decisionOpt.get();
            return new StageDetail(
                    WorkflowStage.FINAL_REVIEW,
                    WorkflowStage.FINAL_REVIEW.getDescription(),
                    WorkflowStageStatus.COMPLETED,
                    "Official Decision Recorded: " + d.getOutcome().getDisplayTitle(),
                    List.of(),
                    "Download sealed assessment dossier & compliance report",
                    Map.of(
                            "decisionRecorded", true,
                            "outcome", d.getOutcome().name(),
                            "officer", d.getOfficerName(),
                            "seal", d.getDecisionIntegritySeal()
                    )
            );
        }

        if (s1.status() != WorkflowStageStatus.COMPLETED) blockers.addAll(s1.blockers());
        if (!s2.blockers().isEmpty()) blockers.addAll(s2.blockers());
        if (!s3.blockers().isEmpty()) blockers.addAll(s3.blockers());
        if (!s4.blockers().isEmpty()) blockers.addAll(s4.blockers());
        if (!s5.blockers().isEmpty()) blockers.addAll(s5.blockers());

        WorkflowStageStatus status = blockers.isEmpty() ? WorkflowStageStatus.IN_PROGRESS : WorkflowStageStatus.BLOCKED;

        return new StageDetail(
                WorkflowStage.FINAL_REVIEW,
                WorkflowStage.FINAL_REVIEW.getDescription(),
                status,
                blockers.isEmpty() ? "All prerequisites satisfied — Scrutiny Hearing Ready" : "Blocked by " + blockers.size() + " upstream deficiencies",
                blockers,
                blockers.isEmpty() ? "Record Scrutiny Committee Official Decision" : "Resolve upstream blockers before conducting final review",
                Map.of(
                        "readyForDecision", blockers.isEmpty(),
                        "blockerCount", blockers.size()
                )
        );
    }
}
