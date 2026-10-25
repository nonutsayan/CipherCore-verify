package com.ciphercore.verify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 21F: Verification Evidence Entity
 * Links authority checks to official payloads, certificates, timestamps, and origin proofs.
 */
@Entity
@Table(name = "authority_verification_evidence")
public class VerificationEvidence {

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
    private String evidenceType; // OFFICIAL_API_PAYLOAD, PORTAL_SCREEN_DIGEST, PKCS7_BYTE_RANGE, QR_DECODED_URL, MANUAL_NOTE

    @Column(nullable = false, length = 64)
    private String sourceId;

    @Column(length = 128)
    private String sourceRecordId;

    @Column(nullable = false)
    private Long documentId;

    private Integer pageNumber = 1;

    @Column(columnDefinition = "TEXT")
    private String evidenceMetadata; // Sanitized JSON or payload digest

    @Column(nullable = false)
    private LocalDateTime observedAt = LocalDateTime.now();

    public VerificationEvidence() {}

    public VerificationEvidence(VerificationResult verificationResult, String evidenceType, String sourceId,
                                String sourceRecordId, Long documentId, Integer pageNumber, String evidenceMetadata) {
        this.verificationResult = verificationResult;
        this.evidenceType = evidenceType;
        this.sourceId = sourceId;
        this.sourceRecordId = sourceRecordId;
        this.documentId = documentId;
        this.pageNumber = pageNumber;
        this.evidenceMetadata = evidenceMetadata;
        this.observedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VerificationResult getVerificationResult() { return verificationResult; }
    public void setVerificationResult(VerificationResult verificationResult) { this.verificationResult = verificationResult; }

    public Long getVerificationResultId() { return verificationResultId; }
    public void setVerificationResultId(Long verificationResultId) { this.verificationResultId = verificationResultId; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getEvidenceMetadata() { return evidenceMetadata; }
    public void setEvidenceMetadata(String evidenceMetadata) { this.evidenceMetadata = evidenceMetadata; }

    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime observedAt) { this.observedAt = observedAt; }
}
