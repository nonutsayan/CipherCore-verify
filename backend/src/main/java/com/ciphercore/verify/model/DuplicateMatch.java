package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 23B: Duplicate Match Entity
 * Records exact and near-duplicate document relationships across and within applications.
 */
@Entity
@Table(name = "duplicate_matches")
public class DuplicateMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sourceDocumentId;

    @Column(nullable = false)
    private Long matchedDocumentId;

    private Long sourceApplicationId;
    private Long matchedApplicationId;

    @Column(nullable = false, length = 64)
    private String matchType; // EXACT_DUPLICATE_SAME_APPLICATION, EXACT_DUPLICATE_DIFFERENT_APPLICATION, EXACT_DUPLICATE_PREVIOUS_SUBMISSION, NEAR_DUPLICATE

    @Column(nullable = false, length = 64)
    private String matchingMethod; // SHA256_HASH, PERCEPTUAL_DHASH, TOKEN_JACCARD_SIMILARITY

    private Double similarityScore = 1.0;

    @Column(columnDefinition = "TEXT")
    private String matchDetails;

    @Column(length = 255)
    private String recommendedAction;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DuplicateMatch() {}

    public DuplicateMatch(Long sourceDocumentId, Long matchedDocumentId, Long sourceApplicationId, Long matchedApplicationId,
                          String matchType, String matchingMethod, Double similarityScore, String matchDetails, String recommendedAction) {
        this.sourceDocumentId = sourceDocumentId;
        this.matchedDocumentId = matchedDocumentId;
        this.sourceApplicationId = sourceApplicationId;
        this.matchedApplicationId = matchedApplicationId;
        this.matchType = matchType;
        this.matchingMethod = matchingMethod;
        this.similarityScore = similarityScore;
        this.matchDetails = matchDetails;
        this.recommendedAction = recommendedAction;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSourceDocumentId() { return sourceDocumentId; }
    public void setSourceDocumentId(Long sourceDocumentId) { this.sourceDocumentId = sourceDocumentId; }

    public Long getMatchedDocumentId() { return matchedDocumentId; }
    public void setMatchedDocumentId(Long matchedDocumentId) { this.matchedDocumentId = matchedDocumentId; }

    public Long getSourceApplicationId() { return sourceApplicationId; }
    public void setSourceApplicationId(Long sourceApplicationId) { this.sourceApplicationId = sourceApplicationId; }

    public Long getMatchedApplicationId() { return matchedApplicationId; }
    public void setMatchedApplicationId(Long matchedApplicationId) { this.matchedApplicationId = matchedApplicationId; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getMatchingMethod() { return matchingMethod; }
    public void setMatchingMethod(String matchingMethod) { this.matchingMethod = matchingMethod; }

    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }

    public String getMatchDetails() { return matchDetails; }
    public void setMatchDetails(String matchDetails) { this.matchDetails = matchDetails; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
