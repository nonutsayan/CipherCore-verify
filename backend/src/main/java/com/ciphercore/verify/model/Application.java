package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ApplicationStatus;
import com.ciphercore.verify.model.enums.FindingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String applicationRef;

    @Column(nullable = false, length = 255)
    private String institutionName;

    @Column(nullable = false, length = 64)
    private String instituteCode;

    @Column(nullable = false, length = 64)
    private String categoryCode; // e.g. EOA_TECHNICAL, NEW_INSTITUTION

    @Column(length = 32)
    private String academicYear = "2025-2026";

    @Column(length = 64)
    private String region = "Western Region (Mumbai)";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private FindingStatus overallValidationStatus; // PASS, WARNING, DEFICIENCY, MANUAL_REVIEW

    @Column(length = 1024)
    private String overallSummaryReason;

    @Column(nullable = false, length = 64)
    private String createdBy;

    private LocalDateTime submissionDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Application() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicationRef() { return applicationRef; }
    public void setApplicationRef(String applicationRef) { this.applicationRef = applicationRef; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getInstituteCode() { return instituteCode; }
    public void setInstituteCode(String instituteCode) { this.instituteCode = instituteCode; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public FindingStatus getOverallValidationStatus() { return overallValidationStatus; }
    public void setOverallValidationStatus(FindingStatus overallValidationStatus) { this.overallValidationStatus = overallValidationStatus; }

    public String getOverallSummaryReason() { return overallSummaryReason; }
    public void setOverallSummaryReason(String overallSummaryReason) { this.overallSummaryReason = overallSummaryReason; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
