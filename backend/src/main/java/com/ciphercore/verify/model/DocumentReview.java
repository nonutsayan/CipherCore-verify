package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 17E & 19F: DocumentReview Entity
 * Preserves the human-in-the-loop audit trail for review approvals, rejections, and notes.
 */
@Entity
@Table(name = "document_reviews")
public class DocumentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, length = 64)
    private String reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(length = 1024)
    private String reviewNotes;

    private LocalDateTime reviewedAt = LocalDateTime.now();

    public DocumentReview() {}

    public DocumentReview(Document document, String reviewerId, ReviewStatus reviewStatus, String reviewNotes) {
        this.document = document;
        this.reviewerId = reviewerId;
        this.reviewStatus = reviewStatus;
        this.reviewNotes = reviewNotes;
        this.reviewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getReviewNotes() { return reviewNotes; }
    public void setReviewNotes(String reviewNotes) { this.reviewNotes = reviewNotes; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
