package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.InfrastructureCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Infrastructure Observation Entity
 * Detailed visual observations, signage OCR, bounding box coordinates, and discrepancy indicators.
 */
@Entity
@Table(name = "infrastructure_observations")
public class InfrastructureObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long analysisJobId;

    @Column(nullable = false)
    private Long evidenceId;

    private Long assetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private InfrastructureCategory category = InfrastructureCategory.OTHER;

    @Column(nullable = false, length = 64)
    private String featureType; // OBJECT_DETECTION, SIGNAGE_OCR, SAFETY_EQUIPMENT, DIMENSION_VERIFICATION, RAMP_GRADIENT, COMPUTERS_COUNT

    @Column(nullable = false, length = 255)
    private String label;

    private Double confidence = 0.92;

    @Column(columnDefinition = "TEXT")
    private String boundingBoxJson; // e.g. {"x": 120, "y": 80, "width": 300, "height": 220}

    private Double frameTimestampSec;

    @Column(length = 255)
    private String extractedText;

    @Column(length = 128)
    private String observedMeasurement;

    @Column(columnDefinition = "TEXT")
    private String regulatoryImplication;

    @Column(nullable = false)
    private Boolean isDiscrepancy = false;

    @Column(columnDefinition = "TEXT")
    private String discrepancyReason;

    private Boolean officerVerified = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public InfrastructureObservation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnalysisJobId() { return analysisJobId; }
    public void setAnalysisJobId(Long analysisJobId) { this.analysisJobId = analysisJobId; }

    public Long getEvidenceId() { return evidenceId; }
    public void setEvidenceId(Long evidenceId) { this.evidenceId = evidenceId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public InfrastructureCategory getCategory() { return category; }
    public void setCategory(InfrastructureCategory category) { this.category = category; }

    public String getFeatureType() { return featureType; }
    public void setFeatureType(String featureType) { this.featureType = featureType; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getBoundingBoxJson() { return boundingBoxJson; }
    public void setBoundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; }

    public Double getFrameTimestampSec() { return frameTimestampSec; }
    public void setFrameTimestampSec(Double frameTimestampSec) { this.frameTimestampSec = frameTimestampSec; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getObservedMeasurement() { return observedMeasurement; }
    public void setObservedMeasurement(String observedMeasurement) { this.observedMeasurement = observedMeasurement; }

    public String getRegulatoryImplication() { return regulatoryImplication; }
    public void setRegulatoryImplication(String regulatoryImplication) { this.regulatoryImplication = regulatoryImplication; }

    public Boolean getIsDiscrepancy() { return isDiscrepancy; }
    public void setIsDiscrepancy(Boolean discrepancy) { isDiscrepancy = discrepancy; }

    public String getDiscrepancyReason() { return discrepancyReason; }
    public void setDiscrepancyReason(String discrepancyReason) { this.discrepancyReason = discrepancyReason; }

    public Boolean getOfficerVerified() { return officerVerified; }
    public void setOfficerVerified(Boolean officerVerified) { this.officerVerified = officerVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
