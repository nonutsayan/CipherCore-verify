package com.ciphercore.verify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 18 & 23E: Verifiable Evidence Record Entity
 * Links anomalies to precise pages, bounding boxes, text snippets, hashes, and detector limitations.
 */
@Entity
@Table(name = "evidence_records")
public class EvidenceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anomaly_id")
    private AnomalyRecord anomaly;

    @Column(name = "anomaly_id", insertable = false, updatable = false)
    private Long anomalyId;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false, length = 64)
    private String evidenceType; // TEXT_SNIPPET, FIELD_MISMATCH, DATE_CONFLICT, FILE_HASH_MATCH, VISUAL_DIFFERENCE, METADATA_OBSERVATION, STRUCTURAL_OBSERVATION, SIGNATURE_VALIDATION_RESULT, QR_CODE_MISMATCH, REFERENCE_RECORD_CONFLICT, CROSS_DOCUMENT_COMPARISON

    private Integer pageNumber = 1;

    @Column(length = 64)
    private String sourceField;

    @Column(columnDefinition = "TEXT")
    private String sourceSnippet;

    @Column(length = 255)
    private String boundingBoxJson; // e.g. {"x": 120, "y": 450, "width": 300, "height": 80}

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    @Column(length = 255)
    private String comparisonTarget;

    @Column(columnDefinition = "TEXT")
    private String limitations;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public EvidenceRecord() {}

    public EvidenceRecord(AnomalyRecord anomaly, Long documentId, String evidenceType, Integer pageNumber,
                          String sourceField, String sourceSnippet, String comparisonTarget, String limitations) {
        this.anomaly = anomaly;
        if (anomaly != null) {
            this.anomalyId = anomaly.getId();
        }
        this.documentId = documentId;
        this.evidenceType = evidenceType;
        this.pageNumber = pageNumber;
        this.sourceField = sourceField;
        this.sourceSnippet = sourceSnippet;
        this.comparisonTarget = comparisonTarget;
        this.limitations = limitations;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AnomalyRecord getAnomaly() { return anomaly; }
    public void setAnomaly(AnomalyRecord anomaly) { 
        this.anomaly = anomaly; 
        if (anomaly != null) {
            this.anomalyId = anomaly.getId();
        }
    }

    public Long getAnomalyId() { return anomalyId; }
    public void setAnomalyId(Long anomalyId) { this.anomalyId = anomalyId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }

    public String getSourceSnippet() { return sourceSnippet; }
    public void setSourceSnippet(String sourceSnippet) { this.sourceSnippet = sourceSnippet; }

    public String getBoundingBoxJson() { return boundingBoxJson; }
    public void setBoundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public String getComparisonTarget() { return comparisonTarget; }
    public void setComparisonTarget(String comparisonTarget) { this.comparisonTarget = comparisonTarget; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
