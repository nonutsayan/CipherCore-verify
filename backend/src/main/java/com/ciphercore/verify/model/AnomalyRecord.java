package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.AnomalyReviewStatus;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 23D: Anomaly Record Entity
 * Captures specific forensic anomalies, why they matter, severity, and review status.
 */
@Entity
@Table(name = "anomaly_records")
public class AnomalyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long analysisRunId;

    @Column(nullable = false)
    private Long documentId;

    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private AnomalyType anomalyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnomalySeverity severity = AnomalySeverity.MEDIUM;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String whyItMatters;

    @Column(length = 64)
    private String ruleId;

    private Double detectorConfidence = 0.90;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnomalyReviewStatus reviewStatus = AnomalyReviewStatus.OPEN;

    @Column(length = 512)
    private String recommendedAction;

    @OneToMany(mappedBy = "anomaly", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EvidenceRecord> evidenceList = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AnomalyRecord() {}

    public AnomalyRecord(Long analysisRunId, Long documentId, Long applicationId, AnomalyType anomalyType,
                         AnomalySeverity severity, String title, String description, String whyItMatters,
                         String ruleId, Double detectorConfidence, String recommendedAction) {
        this.analysisRunId = analysisRunId;
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.anomalyType = anomalyType;
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.whyItMatters = whyItMatters;
        this.ruleId = ruleId;
        this.detectorConfidence = detectorConfidence;
        this.recommendedAction = recommendedAction;
        this.reviewStatus = AnomalyReviewStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnalysisRunId() { return analysisRunId; }
    public void setAnalysisRunId(Long analysisRunId) { this.analysisRunId = analysisRunId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public AnomalyType getAnomalyType() { return anomalyType; }
    public void setAnomalyType(AnomalyType anomalyType) { this.anomalyType = anomalyType; }

    public AnomalySeverity getSeverity() { return severity; }
    public void setSeverity(AnomalySeverity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getWhyItMatters() { return whyItMatters; }
    public void setWhyItMatters(String whyItMatters) { this.whyItMatters = whyItMatters; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public Double getDetectorConfidence() { return detectorConfidence; }
    public void setDetectorConfidence(Double detectorConfidence) { this.detectorConfidence = detectorConfidence; }

    public AnomalyReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(AnomalyReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public List<EvidenceRecord> getEvidenceList() { return evidenceList; }
    public void setEvidenceList(List<EvidenceRecord> evidenceList) { this.evidenceList = evidenceList; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
