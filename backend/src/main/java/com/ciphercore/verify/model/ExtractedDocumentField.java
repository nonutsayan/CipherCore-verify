package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.FieldExtractionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 10 & 17C: ExtractedDocumentField Entity
 * Associates extracted values with normalization status, source evidence snippets,
 * confidence scores, and human review overrides.
 */
@Entity
@Table(name = "extracted_document_fields")
public class ExtractedDocumentField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, length = 128)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String rawValue;

    @Column(columnDefinition = "TEXT")
    private String normalizedValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private FieldExtractionStatus valueStatus = FieldExtractionStatus.EXTRACTED;

    @Column(length = 1024)
    private String sourceEvidence; // Verifiable text snippet from document

    private Double confidenceScore = 1.0;
    private Integer pageNumber = 1;

    @Column(length = 255)
    private String boundingBoxJson;

    @Column(length = 64)
    private String extractionMethod = "PDFBOX_TEXT";

    // Human Correction & Review Fields (Section 19F)
    @Column(columnDefinition = "TEXT")
    private String reviewedValue;

    @Column(length = 64)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 512)
    private String reviewNotes;

    public ExtractedDocumentField() {}

    public ExtractedDocumentField(Document document, String fieldName, String rawValue, String normalizedValue, Double confidenceScore, Integer pageNumber, String extractionMethod) {
        this.document = document;
        this.fieldName = fieldName;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.confidenceScore = confidenceScore;
        this.pageNumber = pageNumber;
        this.extractionMethod = extractionMethod;
        this.valueStatus = rawValue != null && !rawValue.isBlank() ? FieldExtractionStatus.EXTRACTED : FieldExtractionStatus.NOT_FOUND;
    }

    public ExtractedDocumentField(Document document, String fieldName, String rawValue, String normalizedValue, 
                                  FieldExtractionStatus valueStatus, String sourceEvidence, Double confidenceScore, 
                                  Integer pageNumber, String extractionMethod) {
        this.document = document;
        this.fieldName = fieldName;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.valueStatus = valueStatus;
        this.sourceEvidence = sourceEvidence;
        this.confidenceScore = confidenceScore;
        this.pageNumber = pageNumber;
        this.extractionMethod = extractionMethod;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }

    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }

    public FieldExtractionStatus getValueStatus() { return valueStatus; }
    public void setValueStatus(FieldExtractionStatus valueStatus) { this.valueStatus = valueStatus; }

    public String getSourceEvidence() { return sourceEvidence; }
    public void setSourceEvidence(String sourceEvidence) { this.sourceEvidence = sourceEvidence; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getBoundingBoxJson() { return boundingBoxJson; }
    public void setBoundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; }

    public String getExtractionMethod() { return extractionMethod; }
    public void setExtractionMethod(String extractionMethod) { this.extractionMethod = extractionMethod; }

    public String getReviewedValue() { return reviewedValue; }
    public void setReviewedValue(String reviewedValue) { this.reviewedValue = reviewedValue; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public String getEffectiveValue() {
        return reviewedValue != null ? reviewedValue : (normalizedValue != null ? normalizedValue : rawValue);
    }
}
