package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.InfrastructureAnalysisStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Infrastructure Analysis Job
 * Tracks automated CV inspection, room signage OCR, and frame sampling runs.
 */
@Entity
@Table(name = "infrastructure_analysis_jobs")
public class InfrastructureAnalysisJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long evidenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InfrastructureAnalysisStatus jobStatus = InfrastructureAnalysisStatus.ANALYSIS_QUEUED;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    private Integer framesExtracted = 1;
    private Integer observationsCount = 0;
    private Integer discrepancyCount = 0;

    @Column(length = 128)
    private String modelDetails = "CipherCore-Visual-Inspector-v2.1 / OpenCV / OCR-Tesseract";

    @Column(columnDefinition = "TEXT")
    private String summaryObservations;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    public InfrastructureAnalysisJob() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Long evidenceId) { this.evidenceId = evidenceId; }

    public InfrastructureAnalysisStatus getJobStatus() { return jobStatus; }
    public void setJobStatus(InfrastructureAnalysisStatus jobStatus) { this.jobStatus = jobStatus; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getFramesExtracted() { return framesExtracted; }
    public void setFramesExtracted(Integer framesExtracted) { this.framesExtracted = framesExtracted; }

    public Integer getObservationsCount() { return observationsCount; }
    public void setObservationsCount(Integer observationsCount) { this.observationsCount = observationsCount; }

    public Integer getDiscrepancyCount() { return discrepancyCount; }
    public void setDiscrepancyCount(Integer discrepancyCount) { this.discrepancyCount = discrepancyCount; }

    public String getModelDetails() { return modelDetails; }
    public void setModelDetails(String modelDetails) { this.modelDetails = modelDetails; }

    public String getSummaryObservations() { return summaryObservations; }
    public void setSummaryObservations(String summaryObservations) { this.summaryObservations = summaryObservations; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
