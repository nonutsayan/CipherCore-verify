package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Compliance Finding Entity
 * Identifies specific regulatory deficiencies or warnings requiring institutional remediation or reviewer action.
 */
@Entity
@Table(name = "compliance_findings")
public class ComplianceFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assessmentId;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String ruleCode;

    @Column(nullable = false, length = 255)
    private String findingTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RegulatoryRuleCategory findingCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleEvaluationStatus ruleEvaluationStatus;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 255)
    private String statutoryBasis;

    @Column(columnDefinition = "TEXT")
    private String evidenceSummary;

    @Column(columnDefinition = "TEXT")
    private String impactAssessment;

    @Column(columnDefinition = "TEXT")
    private String remediationAction;

    @Column(length = 255)
    private String recommendedReviewerAction;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ComplianceReviewDecision reviewStatus = ComplianceReviewDecision.UNRESOLVED;

    @Column(columnDefinition = "TEXT")
    private String reviewerNotes;

    @Column(length = 128)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ComplianceFinding() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getFindingTitle() { return findingTitle; }
    public void setFindingTitle(String findingTitle) { this.findingTitle = findingTitle; }

    public RegulatoryRuleCategory getFindingCategory() { return findingCategory; }
    public void setFindingCategory(RegulatoryRuleCategory findingCategory) { this.findingCategory = findingCategory; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public RuleEvaluationStatus getRuleEvaluationStatus() { return ruleEvaluationStatus; }
    public void setRuleEvaluationStatus(RuleEvaluationStatus ruleEvaluationStatus) { this.ruleEvaluationStatus = ruleEvaluationStatus; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatutoryBasis() { return statutoryBasis; }
    public void setStatutoryBasis(String statutoryBasis) { this.statutoryBasis = statutoryBasis; }

    public String getEvidenceSummary() { return evidenceSummary; }
    public void setEvidenceSummary(String evidenceSummary) { this.evidenceSummary = evidenceSummary; }

    public String getImpactAssessment() { return impactAssessment; }
    public void setImpactAssessment(String impactAssessment) { this.impactAssessment = impactAssessment; }

    public String getRemediationAction() { return remediationAction; }
    public void setRemediationAction(String remediationAction) { this.remediationAction = remediationAction; }

    public String getRecommendedReviewerAction() { return recommendedReviewerAction; }
    public void setRecommendedReviewerAction(String recommendedReviewerAction) { this.recommendedReviewerAction = recommendedReviewerAction; }

    public ComplianceReviewDecision getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ComplianceReviewDecision reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getReviewerNotes() { return reviewerNotes; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
