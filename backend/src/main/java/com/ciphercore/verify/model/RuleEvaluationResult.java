package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Rule Evaluation Result Entity
 * Stores the deterministic evaluation of a single regulatory rule against an application.
 */
@Entity
@Table(name = "rule_evaluation_results")
public class RuleEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long assessmentId;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String ruleCode;

    private Integer ruleVersionNumber;

    @Column(nullable = false, length = 255)
    private String ruleTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RegulatoryRuleCategory ruleCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleEvaluationStatus status = RuleEvaluationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity = Severity.ADVISORY;

    @Column(length = 64)
    private String evaluatorType;

    @Column(length = 255)
    private String expectedValue;

    @Column(length = 255)
    private String observedValue;

    @Column(length = 255)
    private String variance;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String limitations;

    @Column(columnDefinition = "TEXT")
    private String evidenceReferences; // JSON list of document IDs, fields, asset IDs

    @Column(length = 255)
    private String statutoryCitation;

    @Column(columnDefinition = "TEXT")
    private String remediationGuidance;

    @Column(nullable = false)
    private Boolean requiresManualReview = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public RuleEvaluationResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public Integer getRuleVersionNumber() { return ruleVersionNumber; }
    public void setRuleVersionNumber(Integer ruleVersionNumber) { this.ruleVersionNumber = ruleVersionNumber; }

    public String getRuleTitle() { return ruleTitle; }
    public void setRuleTitle(String ruleTitle) { this.ruleTitle = ruleTitle; }

    public RegulatoryRuleCategory getRuleCategory() { return ruleCategory; }
    public void setRuleCategory(RegulatoryRuleCategory ruleCategory) { this.ruleCategory = ruleCategory; }

    public RuleEvaluationStatus getStatus() { return status; }
    public void setStatus(RuleEvaluationStatus status) { this.status = status; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getEvaluatorType() { return evaluatorType; }
    public void setEvaluatorType(String evaluatorType) { this.evaluatorType = evaluatorType; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getObservedValue() { return observedValue; }
    public void setObservedValue(String observedValue) { this.observedValue = observedValue; }

    public String getVariance() { return variance; }
    public void setVariance(String variance) { this.variance = variance; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public String getEvidenceReferences() { return evidenceReferences; }
    public void setEvidenceReferences(String evidenceReferences) { this.evidenceReferences = evidenceReferences; }

    public String getStatutoryCitation() { return statutoryCitation; }
    public void setStatutoryCitation(String statutoryCitation) { this.statutoryCitation = statutoryCitation; }

    public String getRemediationGuidance() { return remediationGuidance; }
    public void setRemediationGuidance(String remediationGuidance) { this.remediationGuidance = remediationGuidance; }

    public Boolean getRequiresManualReview() { return requiresManualReview; }
    public void setRequiresManualReview(Boolean requiresManualReview) { this.requiresManualReview = requiresManualReview; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
