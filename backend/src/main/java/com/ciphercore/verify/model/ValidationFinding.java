package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.FindingStatus;
import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;

@Entity
@Table(name = "validation_findings")
public class ValidationFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validation_run_id", nullable = false)
    private ValidationRun validationRun;

    @Column(nullable = false, length = 64)
    private String ruleId;

    @Column(nullable = false, length = 255)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FindingStatus status; // PASS, WARNING, DEFICIENCY, MANUAL_REVIEW

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity;

    @Column(length = 128)
    private String affectedField;

    @Column(length = 64)
    private String affectedDocumentType;

    @Column(columnDefinition = "TEXT")
    private String expectedValue;

    @Column(columnDefinition = "TEXT")
    private String observedValue;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(columnDefinition = "TEXT")
    private String evidenceReference;

    @Column(length = 64)
    private String reviewerDecision; // ACCEPTED, DISMISSED, CLARIFICATION_REQUESTED

    @Column(columnDefinition = "TEXT")
    private String reviewerJustification;

    public ValidationFinding() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ValidationRun getValidationRun() { return validationRun; }
    public void setValidationRun(ValidationRun validationRun) { this.validationRun = validationRun; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public FindingStatus getStatus() { return status; }
    public void setStatus(FindingStatus status) { this.status = status; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getAffectedField() { return affectedField; }
    public void setAffectedField(String affectedField) { this.affectedField = affectedField; }

    public String getAffectedDocumentType() { return affectedDocumentType; }
    public void setAffectedDocumentType(String affectedDocumentType) { this.affectedDocumentType = affectedDocumentType; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public String getObservedValue() { return observedValue; }
    public void setObservedValue(String observedValue) { this.observedValue = observedValue; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getEvidenceReference() { return evidenceReference; }
    public void setEvidenceReference(String evidenceReference) { this.evidenceReference = evidenceReference; }

    public String getReviewerDecision() { return reviewerDecision; }
    public void setReviewerDecision(String reviewerDecision) { this.reviewerDecision = reviewerDecision; }

    public String getReviewerJustification() { return reviewerJustification; }
    public void setReviewerJustification(String reviewerJustification) { this.reviewerJustification = reviewerJustification; }
}
