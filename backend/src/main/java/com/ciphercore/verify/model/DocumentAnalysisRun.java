package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 23C: Document Analysis Run
 * Tracks lifecycle, duration, and status of forensic anomaly analysis runs.
 */
@Entity
@Table(name = "document_analysis_runs")
public class DocumentAnalysisRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    private Long applicationId;

    @Column(length = 32)
    private String analysisVersion = "1.0";

    @Column(nullable = false, length = 32)
    private String processingStatus = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, FAILED

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Column(length = 64)
    private String errorCode;

    @Column(length = 64)
    private String configurationVersion = "2026.1-APH";

    public DocumentAnalysisRun() {}

    public DocumentAnalysisRun(Long documentId, Long applicationId) {
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.startedAt = LocalDateTime.now();
        this.processingStatus = "IN_PROGRESS";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getAnalysisVersion() { return analysisVersion; }
    public void setAnalysisVersion(String analysisVersion) { this.analysisVersion = analysisVersion; }

    public String getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(String processingStatus) { this.processingStatus = processingStatus; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }
}
