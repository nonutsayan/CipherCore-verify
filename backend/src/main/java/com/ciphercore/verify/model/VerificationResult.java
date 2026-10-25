package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 21D: Verification Result Entity
 * Captures multi-dimensional verification assessment, record match status,
 * validity, signature status, and human-readable summary.
 */
@Entity
@Table(name = "authority_verification_results")
public class VerificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long verificationJobId;

    @Column(nullable = false)
    private Long documentId;

    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthorityVerificationStatus overallStatus = AuthorityVerificationStatus.PENDING;

    @Column(length = 128)
    private String sourceRecordId;

    @Column(length = 32)
    private String recordMatchStatus = "PENDING"; // EXACT_MATCH, PARTIAL_MATCH, MISMATCH, NOT_FOUND, NOT_CHECKED

    @Column(length = 32)
    private String validityStatus = "UNKNOWN"; // ACTIVE_VALID, EXPIRED, REVOKED, SUPERSEDED, UNKNOWN

    @Column(length = 32)
    private String signatureStatus = "NOT_CHECKED"; // VALID_TRUSTED, VALID_UNTRUSTED, INVALID, NOT_PRESENT

    @Column(length = 32)
    private String qrStatus = "NOT_CHECKED"; // VERIFIED, MISMATCH, UNTRUSTED_DOMAIN, NOT_PRESENT

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String limitations;

    private boolean manualReviewRequired = false;

    @OneToMany(mappedBy = "verificationResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FieldComparison> comparisons = new ArrayList<>();

    @OneToMany(mappedBy = "verificationResult", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<VerificationEvidence> evidenceList = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();

    public VerificationResult() {}

    public VerificationResult(Long documentId, Long applicationId, String sourceId, AuthorityVerificationStatus overallStatus) {
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.sourceId = sourceId;
        this.overallStatus = overallStatus;
        this.generatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVerificationJobId() { return verificationJobId; }
    public void setVerificationJobId(Long verificationJobId) { this.verificationJobId = verificationJobId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public AuthorityVerificationStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(AuthorityVerificationStatus overallStatus) { this.overallStatus = overallStatus; }

    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }

    public String getRecordMatchStatus() { return recordMatchStatus; }
    public void setRecordMatchStatus(String recordMatchStatus) { this.recordMatchStatus = recordMatchStatus; }

    public String getValidityStatus() { return validityStatus; }
    public void setValidityStatus(String validityStatus) { this.validityStatus = validityStatus; }

    public String getSignatureStatus() { return signatureStatus; }
    public void setSignatureStatus(String signatureStatus) { this.signatureStatus = signatureStatus; }

    public String getQrStatus() { return qrStatus; }
    public void setQrStatus(String qrStatus) { this.qrStatus = qrStatus; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public boolean isManualReviewRequired() { return manualReviewRequired; }
    public void setManualReviewRequired(boolean manualReviewRequired) { this.manualReviewRequired = manualReviewRequired; }

    public List<FieldComparison> getComparisons() { return comparisons; }
    public void setComparisons(List<FieldComparison> comparisons) { this.comparisons = comparisons; }

    public List<VerificationEvidence> getEvidenceList() { return evidenceList; }
    public void setEvidenceList(List<VerificationEvidence> evidenceList) { this.evidenceList = evidenceList; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
