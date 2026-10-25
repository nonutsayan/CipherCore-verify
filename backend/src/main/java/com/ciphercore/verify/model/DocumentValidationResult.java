package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.FindingStatus;
import com.ciphercore.verify.model.enums.Severity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 17D: DocumentValidationResult Entity
 * Stores granular completeness, temporal consistency, and formatting findings per document.
 */
@Entity
@Table(name = "document_validation_results")
public class DocumentValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, length = 64)
    private String ruleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FindingStatus status;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(length = 1024)
    private String evidence;

    @Column(length = 255)
    private String relevantFields;

    @Column(length = 512)
    private String recommendedAction;

    private boolean manualReviewRequired = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public DocumentValidationResult() {}

    public DocumentValidationResult(Document document, String ruleId, Severity severity, FindingStatus status, 
                                    String message, String evidence, String relevantFields, String recommendedAction, 
                                    boolean manualReviewRequired) {
        this.document = document;
        this.ruleId = ruleId;
        this.severity = severity;
        this.status = status;
        this.message = message;
        this.evidence = evidence;
        this.relevantFields = relevantFields;
        this.recommendedAction = recommendedAction;
        this.manualReviewRequired = manualReviewRequired;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public FindingStatus getStatus() { return status; }
    public void setStatus(FindingStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getRelevantFields() { return relevantFields; }
    public void setRelevantFields(String relevantFields) { this.relevantFields = relevantFields; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
