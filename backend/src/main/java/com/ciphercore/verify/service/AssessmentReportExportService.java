package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Deliverable 6: Master Assessment Report & Export Service
 * Compiles a comprehensive, unified institutional regulatory assessment dossier
 * combining all 6 deliverables for official review and archiving.
 */
@Service
@Transactional(readOnly = true)
public class AssessmentReportExportService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ValidationFindingRepository validationFindingRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final ApplicationComplianceAssessmentRepository complianceAssessmentRepository;
    private final RuleEvaluationResultRepository ruleEvaluationResultRepository;
    private final InfrastructureObservationRepository infrastructureObservationRepository;
    private final ActionItemRepository actionItemRepository;
    private final OfficialDecisionRepository officialDecisionRepository;
    private final AuditEventRepository auditEventRepository;
    private final WorkflowTrackerService workflowTrackerService;

    public AssessmentReportExportService(ApplicationRepository applicationRepository,
                                         DocumentRepository documentRepository,
                                         ValidationFindingRepository validationFindingRepository,
                                         VerificationRecordRepository verificationRecordRepository,
                                         AnomalyRecordRepository anomalyRecordRepository,
                                         ApplicationComplianceAssessmentRepository complianceAssessmentRepository,
                                         RuleEvaluationResultRepository ruleEvaluationResultRepository,
                                         InfrastructureObservationRepository infrastructureObservationRepository,
                                         ActionItemRepository actionItemRepository,
                                         OfficialDecisionRepository officialDecisionRepository,
                                         AuditEventRepository auditEventRepository,
                                         WorkflowTrackerService workflowTrackerService) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.validationFindingRepository = validationFindingRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.complianceAssessmentRepository = complianceAssessmentRepository;
        this.ruleEvaluationResultRepository = ruleEvaluationResultRepository;
        this.infrastructureObservationRepository = infrastructureObservationRepository;
        this.actionItemRepository = actionItemRepository;
        this.officialDecisionRepository = officialDecisionRepository;
        this.auditEventRepository = auditEventRepository;
        this.workflowTrackerService = workflowTrackerService;
    }

    public Map<String, Object> generateMasterAssessmentDossier(Long applicationId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        WorkflowTrackerService.ApplicationWorkflowSummary wf = workflowTrackerService.getWorkflowSummary(applicationId);
        List<Document> docs = documentRepository.findByApplicationId(applicationId);
        List<ValidationFinding> findings = validationFindingRepository.findByApplicationId(applicationId);
        List<VerificationRecord> verifRecords = verificationRecordRepository.findByDocumentIdIn(
                docs.stream().map(Document::getId).toList()
        );
        List<AnomalyRecord> anomalies = anomalyRecordRepository.findByApplicationId(applicationId);
        Optional<ApplicationComplianceAssessment> assessmentOpt = complianceAssessmentRepository
                .findFirstByApplicationIdOrderByCreatedAtDesc(applicationId);
        List<InfrastructureObservation> infraObs = infrastructureObservationRepository.findByIsDiscrepancyTrue();
        List<ActionItem> actions = actionItemRepository.findByApplicationId(applicationId);
        Optional<OfficialDecision> decisionOpt = officialDecisionRepository
                .findFirstByApplicationIdOrderByRecordedAtDesc(applicationId);
        List<AuditEvent> auditHistory = auditEventRepository
                .findByEntityNameAndEntityIdOrderByEventTimestampDesc("APPLICATION", String.valueOf(applicationId));

        Map<String, Object> dossier = new LinkedHashMap<>();
        dossier.put("reportTitle", "CIPHERCORE MASTER INSTITUTIONAL COMPLIANCE DOSSIER");
        dossier.put("reportSchemaVersion", "3.0-DELIVERABLE-6");
        dossier.put("generatedAt", LocalDateTime.now().toString());
        dossier.put("regulatoryFramework", "AICTE Approval Process Handbook (APH 2024-27) & National Building Code 2016");

        // Section 1: Application Profile
        Map<String, Object> appInfo = new LinkedHashMap<>();
        appInfo.put("applicationId", app.getId());
        appInfo.put("applicationRef", app.getApplicationRef());
        appInfo.put("institutionName", app.getInstitutionName());
        appInfo.put("instituteCode", app.getInstituteCode());
        appInfo.put("categoryCode", app.getCategoryCode());
        appInfo.put("academicYear", app.getAcademicYear());
        appInfo.put("submissionDate", app.getSubmissionDate() != null ? app.getSubmissionDate().toString() : "DRAFT");
        appInfo.put("currentStatus", app.getStatus().name());
        dossier.put("applicationDetails", appInfo);

        // Section 2: Six-Stage Workflow Tracker
        dossier.put("workflowStatus", Map.of(
                "currentStage", wf.currentStage().name(),
                "currentStageStatus", wf.currentStageStatus().name(),
                "readinessState", wf.readinessState().name(),
                "isReadyForReview", wf.isReadyForOfficialReview(),
                "outstandingBlockers", wf.outstandingBlockers(),
                "stages", wf.stages()
        ));

        // Section 3: Document Completeness & OCR Extraction
        List<Map<String, Object>> docSummaries = new ArrayList<>();
        for (Document d : docs) {
            docSummaries.add(Map.of(
                    "documentId", d.getId(),
                    "documentType", d.getDocumentType(),
                    "originalFilename", d.getOriginalFilename(),
                    "fileSizeBytes", d.getFileSizeBytes(),
                    "sha256Checksum", d.getSha256Checksum() != null ? d.getSha256Checksum() : "N/A",
                    "extractionStatus", d.getExtractionStatus() != null ? d.getExtractionStatus() : "PENDING",
                    "hasExpiredAlert", d.getExtractedText() != null && d.getExtractedText().toUpperCase().contains("EXPIRED")
            ));
        }
        dossier.put("documentInventory", Map.of(
                "totalSubmitted", docs.size(),
                "documents", docSummaries
        ));

        // Section 4: Validation & Inconsistencies
        dossier.put("validationFindings", findings);

        // Section 5: Issuing Authority Verification
        dossier.put("authorityCorroborations", verifRecords);

        // Section 6: Forensic Anti-Forgery & Anomaly Intelligence
        dossier.put("forensicAnomalies", anomalies);

        // Section 7: Regulatory Compliance & Infrastructure Evidence
        if (assessmentOpt.isPresent()) {
            ApplicationComplianceAssessment a = assessmentOpt.get();
            dossier.put("regulatoryCompliance", Map.of(
                    "assessmentId", a.getId(),
                    "status", a.getStatus().name(),
                    "complianceScore", a.getOverallComplianceScore(),
                    "passedRules", a.getPassedCount(),
                    "deficiencies", a.getDeficiencyCount(),
                    "assessmentSeal", a.getAssessmentHash() != null ? a.getAssessmentHash() : "N/A"
            ));
        } else {
            dossier.put("regulatoryCompliance", "NOT_EVALUATED");
        }
        dossier.put("infrastructureDiscrepancies", infraObs);

        // Section 8: Action Queue
        dossier.put("pendingActionItems", actions);

        // Section 9: Official Disposition
        if (decisionOpt.isPresent()) {
            OfficialDecision d = decisionOpt.get();
            dossier.put("officialDecision", Map.of(
                    "outcome", d.getOutcome().name(),
                    "displayTitle", d.getOutcome().getDisplayTitle(),
                    "officerName", d.getOfficerName(),
                    "officerRole", d.getOfficerRole(),
                    "statutoryRationale", d.getStatutoryRationale(),
                    "conditionsPrecedent", d.getConditionsPrecedent() != null ? d.getConditionsPrecedent() : "None",
                    "committeeReference", d.getCommitteeReference() != null ? d.getCommitteeReference() : "AICTE-SC",
                    "decisionSeal", d.getDecisionIntegritySeal(),
                    "recordedAt", d.getRecordedAt().toString()
            ));
        } else {
            dossier.put("officialDecision", "PENDING_HEARING");
        }

        // Section 10: Audit Timeline
        dossier.put("auditTimeline", auditHistory);

        return dossier;
    }
}
