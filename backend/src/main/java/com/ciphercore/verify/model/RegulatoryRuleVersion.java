package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import com.ciphercore.verify.model.enums.RuleLifecycleStatus;
import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Immutable Version of a Regulatory Rule
 * Tracks full parameter definitions, evaluator types, and statutory provenance.
 */
@Entity
@Table(name = "regulatory_rule_versions")
public class RegulatoryRuleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ruleId;

    @Column(nullable = false, length = 64)
    private String ruleCode;

    @Column(nullable = false)
    private Integer versionNumber = 1;

    @Column(length = 64)
    private String versionTag; // e.g. "2024-2027.v1"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleLifecycleStatus lifecycleStatus = RuleLifecycleStatus.ACTIVE;

    private Long sourceId; // Reference to RegulatorySource

    @Column(length = 255)
    private String sourceSection; // e.g. "Chapter 7, Clause 7.14, Table 7.2"

    @Column(nullable = false, length = 64)
    private String evaluatorType; // NUMERIC_THRESHOLD, DOCUMENT_PRESENCE, DATE_INTERVAL, NORMALIZED_TEXT_MATCH, CROSS_DOCUMENT_CONSISTENCY, EVIDENCE_VERIFICATION, MANUAL_REVIEW_TASK

    @Column(columnDefinition = "TEXT")
    private String conditionDefinitionJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity defaultSeverity = Severity.MAJOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RuleEvaluationStatus failureStatus = RuleEvaluationStatus.DEFICIENCY;

    @Column(columnDefinition = "TEXT")
    private String reviewInstructions;

    @Column(columnDefinition = "TEXT")
    private String remediationGuidance;

    @Column(length = 255)
    private String statutoryCitation;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(length = 128)
    private String createdBy = "SYSTEM";

    @Column(length = 128)
    private String approvedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime approvedAt;

    public RegulatoryRuleVersion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }

    public String getVersionTag() { return versionTag; }
    public void setVersionTag(String versionTag) { this.versionTag = versionTag; }

    public RuleLifecycleStatus getLifecycleStatus() { return lifecycleStatus; }
    public void setLifecycleStatus(RuleLifecycleStatus lifecycleStatus) { this.lifecycleStatus = lifecycleStatus; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceSection() { return sourceSection; }
    public void setSourceSection(String sourceSection) { this.sourceSection = sourceSection; }

    public String getEvaluatorType() { return evaluatorType; }
    public void setEvaluatorType(String evaluatorType) { this.evaluatorType = evaluatorType; }

    public String getConditionDefinitionJson() { return conditionDefinitionJson; }
    public void setConditionDefinitionJson(String conditionDefinitionJson) { this.conditionDefinitionJson = conditionDefinitionJson; }

    public Severity getDefaultSeverity() { return defaultSeverity; }
    public void setDefaultSeverity(Severity defaultSeverity) { this.defaultSeverity = defaultSeverity; }

    public RuleEvaluationStatus getFailureStatus() { return failureStatus; }
    public void setFailureStatus(RuleEvaluationStatus failureStatus) { this.failureStatus = failureStatus; }

    public String getReviewInstructions() { return reviewInstructions; }
    public void setReviewInstructions(String reviewInstructions) { this.reviewInstructions = reviewInstructions; }

    public String getRemediationGuidance() { return remediationGuidance; }
    public void setRemediationGuidance(String remediationGuidance) { this.remediationGuidance = remediationGuidance; }

    public String getStatutoryCitation() { return statutoryCitation; }
    public void setStatutoryCitation(String statutoryCitation) { this.statutoryCitation = statutoryCitation; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}
