package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.VerificationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 16, 17 & 23G: Verification Record Entity
 * Captures official reference registry lookups, QR code checks, and cryptographic digital signature inspections.
 */
@Entity
@Table(name = "verification_records")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false, length = 64)
    private String verificationType; // INSTITUTIONAL_REGISTRY, QR_PAYLOAD_VALIDATION, CRYPTOGRAPHIC_DIGITAL_SIGNATURE

    @Column(length = 128)
    private String sourceIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private VerificationStatus verificationStatus = VerificationStatus.SOURCE_NOT_CONFIGURED;

    @Column(columnDefinition = "TEXT")
    private String comparisonEvidence;

    @Column(columnDefinition = "TEXT")
    private String limitations;

    @Column(nullable = false)
    private LocalDateTime verifiedAt = LocalDateTime.now();

    public VerificationRecord() {}

    public VerificationRecord(Long documentId, String verificationType, String sourceIdentifier,
                              VerificationStatus verificationStatus, String comparisonEvidence, String limitations) {
        this.documentId = documentId;
        this.verificationType = verificationType;
        this.sourceIdentifier = sourceIdentifier;
        this.verificationStatus = verificationStatus;
        this.comparisonEvidence = comparisonEvidence;
        this.limitations = limitations;
        this.verifiedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getVerificationType() { return verificationType; }
    public void setVerificationType(String verificationType) { this.verificationType = verificationType; }

    public String getSourceIdentifier() { return sourceIdentifier; }
    public void setSourceIdentifier(String sourceIdentifier) { this.sourceIdentifier = sourceIdentifier; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getComparisonEvidence() { return comparisonEvidence; }
    public void setComparisonEvidence(String comparisonEvidence) { this.comparisonEvidence = comparisonEvidence; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
