package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Deliverable 5: Section 22/23 Compliance Assessment Report Generator
 * Generates transparent, auditable regulatory compliance reports with
 * statutory citations, evidence cross-links, and cryptographic verification seals.
 */
@Service
public class ComplianceReportService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationComplianceAssessmentRepository assessmentRepository;
    private final RuleEvaluationResultRepository resultRepository;
    private final ComplianceFindingRepository findingRepository;
    private final InfrastructureEvidenceRepository evidenceRepository;
    private final InfrastructureObservationRepository observationRepository;
    private final ComplianceReviewTaskRepository reviewTaskRepository;
    private final VerificationResultRepository verificationResultRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;

    public ComplianceReportService(ApplicationRepository applicationRepository,
                                 ApplicationComplianceAssessmentRepository assessmentRepository,
                                 RuleEvaluationResultRepository resultRepository,
                                 ComplianceFindingRepository findingRepository,
                                 InfrastructureEvidenceRepository evidenceRepository,
                                 InfrastructureObservationRepository observationRepository,
                                 ComplianceReviewTaskRepository reviewTaskRepository,
                                 VerificationResultRepository verificationResultRepository,
                                 AnomalyRecordRepository anomalyRecordRepository) {
        this.applicationRepository = applicationRepository;
        this.assessmentRepository = assessmentRepository;
        this.resultRepository = resultRepository;
        this.findingRepository = findingRepository;
        this.evidenceRepository = evidenceRepository;
        this.observationRepository = observationRepository;
        this.reviewTaskRepository = reviewTaskRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
    }

    public Map<String, Object> generateComplianceReport(Long assessmentId) {
        ApplicationComplianceAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new IllegalArgumentException("Compliance assessment not found: " + assessmentId));

        Application application = applicationRepository.findById(assessment.getApplicationId())
                .orElse(null);

        List<RuleEvaluationResult> results = resultRepository.findByAssessmentId(assessmentId);
        List<ComplianceFinding> findings = findingRepository.findByAssessmentId(assessmentId);
        List<ComplianceReviewTask> tasks = reviewTaskRepository.findByAssessmentId(assessmentId);
        List<InfrastructureEvidence> evidence = evidenceRepository.findByApplicationId(assessment.getApplicationId());
        List<VerificationResult> authorityResults = verificationResultRepository.findByApplicationIdOrderByGeneratedAtDesc(assessment.getApplicationId());
        List<AnomalyRecord> anomalies = anomalyRecordRepository.findByApplicationId(assessment.getApplicationId());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportSchemaVersion", "2.0-SIH26261");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("regulatoryFramework", "AICTE Approval Process Handbook (APH 2024-27)");

        // Assessment Summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("assessmentId", assessment.getId());
        summary.put("status", assessment.getStatus());
        summary.put("academicYear", assessment.getAcademicYear());
        summary.put("evaluatedBy", assessment.getEvaluatedBy());
        summary.put("evaluatedAt", assessment.getEvaluatedAt());
        summary.put("overallComplianceScore", assessment.getOverallComplianceScore());
        summary.put("assessmentIntegritySeal", assessment.getAssessmentHash());
        summary.put("totalRulesEvaluated", assessment.getTotalRulesEvaluated());
        summary.put("passedCount", assessment.getPassedCount());
        summary.put("warningCount", assessment.getWarningCount());
        summary.put("deficiencyCount", assessment.getDeficiencyCount());
        summary.put("manualReviewCount", assessment.getManualReviewCount());
        summary.put("notApplicableCount", assessment.getNotApplicableCount());
        summary.put("insufficientEvidenceCount", assessment.getInsufficientEvidenceCount());
        report.put("assessmentSummary", summary);

        // Application Info
        if (application != null) {
            Map<String, Object> appInfo = new LinkedHashMap<>();
            appInfo.put("applicationId", application.getId());
            appInfo.put("referenceNumber", application.getApplicationRef());
            appInfo.put("institutionName", application.getInstitutionName());
            appInfo.put("instituteCode", application.getInstituteCode());
            appInfo.put("categoryCode", application.getCategoryCode());
            report.put("applicationDetails", appInfo);
        }

        // Rule Evaluations
        report.put("ruleEvaluationResults", results);

        // Compliance Findings (Deficiencies & Warnings)
        report.put("complianceFindings", findings);

        // Review Tasks
        report.put("scrutinyTasks", tasks);

        // Infrastructure Evidence & Observations
        Map<String, Object> infraSummary = new LinkedHashMap<>();
        infraSummary.put("totalMediaFiles", evidence.size());
        List<InfrastructureObservation> discrepancies = observationRepository.findByIsDiscrepancyTrue();
        infraSummary.put("flaggedVisualDiscrepancies", discrepancies.size());
        infraSummary.put("evidenceFiles", evidence);
        infraSummary.put("discrepancyDetails", discrepancies);
        report.put("infrastructureScrutiny", infraSummary);

        // Upstream Evidence Cross-Links (Deliverables 3 & 4)
        Map<String, Object> upstreamIntegrations = new LinkedHashMap<>();
        upstreamIntegrations.put("issuingAuthorityCorroborationChecks", authorityResults.size());
        upstreamIntegrations.put("forensicTamperFindings", anomalies.size());
        report.put("upstreamEvidenceIntegrations", upstreamIntegrations);

        // Official Scrutiny Recommendation
        String recommendation;
        if (assessment.getDeficiencyCount() > 0) {
            recommendation = "DEFICIENCIES_DETECTED: Institution requires Scrutiny Committee hearing or issuance of Defect Notice for remedial action prior to approval decision.";
        } else if (assessment.getWarningCount() > 0 || assessment.getManualReviewCount() > 0) {
            recommendation = "CONDITIONAL_REVIEW: Scrutiny Officer manual verification recommended for advisory warnings.";
        } else {
            recommendation = "RECOMMENDED_FOR_APPROVAL: Application satisfies statutory infrastructure and regulatory norms under AICTE APH 2024-27.";
        }
        report.put("officialDispositionRecommendation", recommendation);

        return report;
    }
}
