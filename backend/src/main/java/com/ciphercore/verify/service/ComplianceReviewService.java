package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deliverable 5: Human-in-the-Loop Compliance Review Service
 * Supports official scrutiny workflows, discretionary overrides, evidentiary requests,
 * and immutable chain-of-custody audit logs.
 */
@Service
public class ComplianceReviewService {

    private final ComplianceReviewTaskRepository reviewTaskRepository;
    private final ComplianceFindingRepository findingRepository;
    private final RuleEvaluationResultRepository resultRepository;
    private final ApplicationComplianceAssessmentRepository assessmentRepository;
    private final AuditEventRepository auditEventRepository;

    public ComplianceReviewService(ComplianceReviewTaskRepository reviewTaskRepository,
                                   ComplianceFindingRepository findingRepository,
                                   RuleEvaluationResultRepository resultRepository,
                                   ApplicationComplianceAssessmentRepository assessmentRepository,
                                   AuditEventRepository auditEventRepository) {
        this.reviewTaskRepository = reviewTaskRepository;
        this.findingRepository = findingRepository;
        this.resultRepository = resultRepository;
        this.assessmentRepository = assessmentRepository;
        this.auditEventRepository = auditEventRepository;
    }

    public List<ComplianceReviewTask> getTasksForAssessment(Long assessmentId) {
        return reviewTaskRepository.findByAssessmentId(assessmentId);
    }

    @Transactional
    public ComplianceReviewTask submitReviewDecision(Long taskId,
                                                     ComplianceReviewDecision decision,
                                                     String officerRationale,
                                                     String statutoryJustification,
                                                     Boolean overrideRuleDecision,
                                                     RuleEvaluationStatus overriddenStatus,
                                                     String additionalEvidenceRequested,
                                                     String officerUsername) {
        ComplianceReviewTask task = reviewTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Review task not found: " + taskId));

        task.setReviewStatus(decision);
        task.setOfficerRationale(officerRationale);
        task.setStatutoryJustification(statutoryJustification);
        task.setOverrideRuleDecision(overrideRuleDecision != null && overrideRuleDecision);
        task.setOverriddenStatus(overriddenStatus);
        task.setAdditionalEvidenceRequested(additionalEvidenceRequested);
        task.setAssignedOfficer(officerUsername != null ? officerUsername : task.getAssignedOfficer());
        task.setCompletedAt(LocalDateTime.now());
        reviewTaskRepository.save(task);

        // Update finding if linked
        if (task.getComplianceFindingId() != null) {
            findingRepository.findById(task.getComplianceFindingId()).ifPresent(f -> {
                f.setReviewStatus(decision);
                f.setReviewerNotes(officerRationale);
                f.setReviewedBy(officerUsername);
                f.setReviewedAt(LocalDateTime.now());
                findingRepository.save(f);
            });
        }

        // If rule decision is overridden, update evaluation result
        if (Boolean.TRUE.equals(overrideRuleDecision) && overriddenStatus != null && task.getRuleEvaluationResultId() != null) {
            resultRepository.findById(task.getRuleEvaluationResultId()).ifPresent(r -> {
                final RuleEvaluationStatus prevStatus = r.getStatus();
                r.setStatus(overriddenStatus);
                r.setExplanation(r.getExplanation() + " [OFFICER OVERRIDE by " + officerUsername + ": " + officerRationale + "]");
                resultRepository.save(r);

                // Update assessment counts
                assessmentRepository.findById(task.getAssessmentId()).ifPresent(a -> {
                    if (prevStatus == RuleEvaluationStatus.DEFICIENCY && overriddenStatus == RuleEvaluationStatus.PASS) {
                        a.setDeficiencyCount(Math.max(0, a.getDeficiencyCount() - 1));
                        a.setPassedCount(a.getPassedCount() + 1);
                    } else if (prevStatus == RuleEvaluationStatus.WARNING && overriddenStatus == RuleEvaluationStatus.PASS) {
                        a.setWarningCount(Math.max(0, a.getWarningCount() - 1));
                        a.setPassedCount(a.getPassedCount() + 1);
                    }
                    int scorable = a.getTotalRulesEvaluated() - a.getNotApplicableCount();
                    double score = scorable > 0 ? ((double) a.getPassedCount() / scorable) * 100.0 : 0.0;
                    a.setOverallComplianceScore(Math.round(score * 10.0) / 10.0);
                    assessmentRepository.save(a);
                });
            });
        }

        // Audit Event
        AuditEvent event = new AuditEvent();
        event.setAction("COMPLIANCE_REVIEW_DECISION_RECORDED");
        event.setDetails(String.format("Officer %s recorded decision %s on Task #%d: %s",
                officerUsername, decision, taskId, officerRationale));
        event.setUsername(officerUsername != null ? officerUsername : "OFFICER");
        event.setEntityName("COMPLIANCE_REVIEW_TASK");
        event.setEntityId(String.valueOf(taskId));
        event.setEventTimestamp(LocalDateTime.now());
        auditEventRepository.save(event);

        return task;
    }
}
