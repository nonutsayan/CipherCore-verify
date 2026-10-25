package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.AssessmentWorkflowStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Application Compliance Assessment Entity
 * Represents an official compliance assessment run across all AICTE regulatory rules.
 */
@Entity
@Table(name = "compliance_assessments")
public class ApplicationComplianceAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(length = 32)
    private String academicYear = "2026-27";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssessmentWorkflowStatus status = AssessmentWorkflowStatus.NOT_STARTED;

    private Integer totalRulesEvaluated = 0;
    private Integer passedCount = 0;
    private Integer warningCount = 0;
    private Integer deficiencyCount = 0;
    private Integer manualReviewCount = 0;
    private Integer notApplicableCount = 0;
    private Integer insufficientEvidenceCount = 0;

    private Double overallComplianceScore = 0.0;

    private LocalDateTime evaluatedAt;

    @Column(length = 128)
    private String evaluatedBy = "CIPHERCORE_RULE_ENGINE_v5";

    @Column(length = 64)
    private String assessmentHash; // SHA-256 seal of evaluation results

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ApplicationComplianceAssessment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public AssessmentWorkflowStatus getStatus() { return status; }
    public void setStatus(AssessmentWorkflowStatus status) { this.status = status; }

    public Integer getTotalRulesEvaluated() { return totalRulesEvaluated; }
    public void setTotalRulesEvaluated(Integer totalRulesEvaluated) { this.totalRulesEvaluated = totalRulesEvaluated; }

    public Integer getPassedCount() { return passedCount; }
    public void setPassedCount(Integer passedCount) { this.passedCount = passedCount; }

    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }

    public Integer getDeficiencyCount() { return deficiencyCount; }
    public void setDeficiencyCount(Integer deficiencyCount) { this.deficiencyCount = deficiencyCount; }

    public Integer getManualReviewCount() { return manualReviewCount; }
    public void setManualReviewCount(Integer manualReviewCount) { this.manualReviewCount = manualReviewCount; }

    public Integer getNotApplicableCount() { return notApplicableCount; }
    public void setNotApplicableCount(Integer notApplicableCount) { this.notApplicableCount = notApplicableCount; }

    public Integer getInsufficientEvidenceCount() { return insufficientEvidenceCount; }
    public void setInsufficientEvidenceCount(Integer insufficientEvidenceCount) { this.insufficientEvidenceCount = insufficientEvidenceCount; }

    public Double getOverallComplianceScore() { return overallComplianceScore; }
    public void setOverallComplianceScore(Double overallComplianceScore) { this.overallComplianceScore = overallComplianceScore; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public String getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }

    public String getAssessmentHash() { return assessmentHash; }
    public void setAssessmentHash(String assessmentHash) { this.assessmentHash = assessmentHash; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
