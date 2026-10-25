package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 23H: Anomaly Review Entity
 * Stores human review decisions, official justifications, and disposition history.
 */
@Entity
@Table(name = "anomaly_reviews")
public class AnomalyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long anomalyId;

    @Column(nullable = false, length = 64)
    private String reviewerId;

    @Column(nullable = false, length = 64)
    private String decision; // CONFIRM_LEGITIMATE, CONFIRM_DISCREPANCY, MARK_FALSE_POSITIVE, REQUEST_CLARIFICATION, ESCALATE_TO_ISSUER

    @Column(columnDefinition = "TEXT", nullable = false)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime reviewedAt = LocalDateTime.now();

    public AnomalyReview() {}

    public AnomalyReview(Long anomalyId, String reviewerId, String decision, String notes) {
        this.anomalyId = anomalyId;
        this.reviewerId = reviewerId;
        this.decision = decision;
        this.notes = notes;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAnomalyId() { return anomalyId; }
    public void setAnomalyId(Long anomalyId) { this.anomalyId = anomalyId; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
