package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 21G: Verification Review Entity
 * Captures human scrutiny determinations, officer attribution, and mandatory justifications.
 */
@Entity
@Table(name = "authority_verification_reviews")
public class VerificationReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long verificationResultId;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false, length = 64)
    private String reviewerId;

    @Column(nullable = false, length = 64)
    private String reviewDecision; // CONFIRM_LEGITIMATE, CONFIRM_DISCREPANCY, REQUEST_FURTHER_EVIDENCE, REQUEST_REPLACEMENT, MARK_PENDING, ESCALATE_TO_ISSUER, CLOSE_RESOLVED

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public VerificationReview() {}

    public VerificationReview(Long verificationResultId, Long documentId, String reviewerId, String reviewDecision, String notes) {
        this.verificationResultId = verificationResultId;
        this.documentId = documentId;
        this.reviewerId = reviewerId;
        this.reviewDecision = reviewDecision;
        this.notes = notes;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVerificationResultId() { return verificationResultId; }
    public void setVerificationResultId(Long verificationResultId) { this.verificationResultId = verificationResultId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewDecision() { return reviewDecision; }
    public void setReviewDecision(String reviewDecision) { this.reviewDecision = reviewDecision; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
