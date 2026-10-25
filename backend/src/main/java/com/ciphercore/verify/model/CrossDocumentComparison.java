package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 15 & 23F: Cross-Document Dossier Comparison Entity
 */
@Entity
@Table(name = "cross_document_comparisons")
public class CrossDocumentComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false)
    private Long sourceDocumentId;

    @Column(nullable = false)
    private Long targetDocumentId;

    @Column(length = 64)
    private String comparisonType; // DOSSIER_IDENTITY, TEMPORAL_SEQUENCE, LAND_AREA_CROSSCHECK, INTAKE_CONSISTENCY

    @Column(length = 32)
    private String comparisonStatus = "COMPLETED"; // COMPLETED, CONFLICT_DETECTED, CONSISTENT, SKIPPED

    private int conflictCount = 0;

    @Column(columnDefinition = "TEXT")
    private String findingsSummary;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CrossDocumentComparison() {}

    public CrossDocumentComparison(Long applicationId, Long sourceDocumentId, Long targetDocumentId,
                                   String comparisonType, String comparisonStatus, int conflictCount, String findingsSummary) {
        this.applicationId = applicationId;
        this.sourceDocumentId = sourceDocumentId;
        this.targetDocumentId = targetDocumentId;
        this.comparisonType = comparisonType;
        this.comparisonStatus = comparisonStatus;
        this.conflictCount = conflictCount;
        this.findingsSummary = findingsSummary;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(Long sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }

    public Long getTargetDocumentId() { return targetDocumentId; }
    public void setTargetDocumentId(Long targetDocumentId) { this.targetDocumentId = targetDocumentId; }

    public String getComparisonType() { return comparisonType; }
    public void setComparisonType(String comparisonType) { this.comparisonType = comparisonType; }

    public String getComparisonStatus() { return comparisonStatus; }
    public void setComparisonStatus(String comparisonStatus) { this.comparisonStatus = comparisonStatus; }

    public int getConflictCount() { return conflictCount; }
    public void setConflictCount(int conflictCount) { this.conflictCount = conflictCount; }

    public String getFindingsSummary() { return findingsSummary; }
    public void setFindingsSummary(String findingsSummary) { this.findingsSummary = findingsSummary; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
