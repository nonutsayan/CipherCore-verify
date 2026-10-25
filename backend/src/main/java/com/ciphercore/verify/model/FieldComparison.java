package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ComparisonMethod;
import com.ciphercore.verify.model.enums.FieldComparisonStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Section 21E: Field Comparison Entity
 * Records field-by-field reconciliation between submitted document and official authority record.
 */
@Entity
@Table(name = "authority_field_comparisons")
public class FieldComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_result_id")
    private VerificationResult verificationResult;

    @Column(name = "verification_result_id", insertable = false, updatable = false)
    private Long verificationResultId;

    @Column(nullable = false, length = 64)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String submittedValue;

    @Column(columnDefinition = "TEXT")
    private String officialValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ComparisonMethod comparisonMethod = ComparisonMethod.EXACT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FieldComparisonStatus comparisonStatus = FieldComparisonStatus.NEEDS_REVIEW;

    @Column(columnDefinition = "TEXT")
    private String evidenceSnippet;

    private Double confidence = 1.0;

    public FieldComparison() {}

    public FieldComparison(VerificationResult verificationResult, String fieldName, String submittedValue,
                           String officialValue, ComparisonMethod comparisonMethod,
                           FieldComparisonStatus comparisonStatus, String evidenceSnippet, Double confidence) {
        this.verificationResult = verificationResult;
        this.fieldName = fieldName;
        this.submittedValue = submittedValue;
        this.officialValue = officialValue;
        this.comparisonMethod = comparisonMethod;
        this.comparisonStatus = comparisonStatus;
        this.evidenceSnippet = evidenceSnippet;
        this.confidence = confidence;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VerificationResult getVerificationResult() { return verificationResult; }
    public void setVerificationResult(VerificationResult verificationResult) { this.verificationResult = verificationResult; }

    public Long getVerificationResultId() { return verificationResultId; }
    public void setVerificationResultId(Long verificationResultId) { this.verificationResultId = verificationResultId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getSubmittedValue() { return submittedValue; }
    public void setSubmittedValue(String submittedValue) { this.submittedValue = submittedValue; }

    public String getOfficialValue() { return officialValue; }
    public void setOfficialValue(String officialValue) { this.officialValue = officialValue; }

    public ComparisonMethod getComparisonMethod() { return comparisonMethod; }
    public void setComparisonMethod(ComparisonMethod comparisonMethod) { this.comparisonMethod = comparisonMethod; }

    public FieldComparisonStatus getComparisonStatus() { return comparisonStatus; }
    public void setComparisonStatus(FieldComparisonStatus comparisonStatus) { this.comparisonStatus = comparisonStatus; }

    public String getEvidenceSnippet() { return evidenceSnippet; }
    public void setEvidenceSnippet(String evidenceSnippet) { this.evidenceSnippet = evidenceSnippet; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
}
