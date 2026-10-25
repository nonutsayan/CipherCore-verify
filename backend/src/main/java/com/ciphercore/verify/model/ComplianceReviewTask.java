package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Compliance Review Task Entity
 * Human-in-the-loop review of regulatory findings, officer overrides, and justifications.
 */
@Entity
@Table(name = "compliance_review_tasks")
public class ComplianceReviewTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assessmentId;

    private Long ruleEvaluationResultId;
    private Long complianceFindingId;

    @Column(nullable = false, length = 128)
    private String assignedOfficer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComplianceReviewDecision reviewStatus = ComplianceReviewDecision.UNRESOLVED;

    @Column(columnDefinition = "TEXT")
    private String officerRationale;

    @Column(columnDefinition = "TEXT")
    private String statutoryJustification;

    private Boolean overrideRuleDecision = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private RuleEvaluationStatus overriddenStatus;

    @Column(columnDefinition = "TEXT")
    private String additionalEvidenceRequested;

    private LocalDateTime assignedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    @Column(length = 64)
    private String auditLogReference;

    public ComplianceReviewTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getRuleEvaluationResultId() { return ruleEvaluationResultId; }
    public void setRuleEvaluationResultId(Long ruleEvaluationResultId) { this.ruleEvaluationResultId = ruleEvaluationResultId; }

    public Long getComplianceFindingId() { return complianceFindingId; }
    public void setComplianceFindingId(Long complianceFindingId) { this.complianceFindingId = complianceFindingId; }

    public String getAssignedOfficer() { return assignedOfficer; }
    public void setAssignedOfficer(String assignedOfficer) { this.assignedOfficer = assignedOfficer; }

    public ComplianceReviewDecision getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ComplianceReviewDecision reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getOfficerRationale() { return officerRationale; }
    public void setOfficerRationale(String officerRationale) { this.officerRationale = officerRationale; }

    public String getStatutoryJustification() { return statutoryJustification; }
    public void setStatutoryJustification(String statutoryJustification) { this.statutoryJustification = statutoryJustification; }

    public Boolean getOverrideRuleDecision() { return overrideRuleDecision; }
    public void setOverrideRuleDecision(Boolean overrideRuleDecision) { this.overrideRuleDecision = overrideRuleDecision; }

    public RuleEvaluationStatus getOverriddenStatus() { return overriddenStatus; }
    public void setOverriddenStatus(RuleEvaluationStatus overriddenStatus) { this.overriddenStatus = overriddenStatus; }

    public String getAdditionalEvidenceRequested() { return additionalEvidenceRequested; }
    public void setAdditionalEvidenceRequested(String additionalEvidenceRequested) { this.additionalEvidenceRequested = additionalEvidenceRequested; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getAuditLogReference() { return auditLogReference; }
    public void setAuditLogReference(String auditLogReference) { this.auditLogReference = auditLogReference; }
}
