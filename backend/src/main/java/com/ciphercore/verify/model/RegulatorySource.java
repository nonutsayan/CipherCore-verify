package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.SourceVerificationState;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Regulatory Source Registry
 * Tracks authentic AICTE Approval Process Handbooks, notifications, gazettes, circulars.
 */
@Entity
@Table(name = "regulatory_sources")
public class RegulatorySource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sourceCode; // e.g., AICTE_APH_2024_27

    @Column(nullable = false, length = 255)
    private String sourceName; // e.g., AICTE Approval Process Handbook 2024-2027

    @Column(nullable = false, length = 128)
    private String issuingAuthority; // All India Council for Technical Education

    @Column(length = 128)
    private String documentReference; // e.g., F.No. AICTE/AB/APH/2024-27

    @Column(length = 512)
    private String officialUrl;

    private LocalDate publicationDate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(length = 64)
    private String applicableJurisdiction; // ALL_INDIA

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SourceVerificationState sourceVerificationState = SourceVerificationState.VERIFIED;

    @Column(length = 64)
    private String sha256Checksum;

    @Column(columnDefinition = "TEXT")
    private String sourceDescription;

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RegulatorySource() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public String getDocumentReference() { return documentReference; }
    public void setDocumentReference(String documentReference) { this.documentReference = documentReference; }

    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public String getApplicableJurisdiction() { return applicableJurisdiction; }
    public void setApplicableJurisdiction(String applicableJurisdiction) { this.applicableJurisdiction = applicableJurisdiction; }

    public SourceVerificationState getSourceVerificationState() { return sourceVerificationState; }
    public void setSourceVerificationState(SourceVerificationState sourceVerificationState) { this.sourceVerificationState = sourceVerificationState; }

    public String getSha256Checksum() { return sha256Checksum; }
    public void setSha256Checksum(String sha256Checksum) { this.sha256Checksum = sha256Checksum; }

    public String getSourceDescription() { return sourceDescription; }
    public void setSourceDescription(String sourceDescription) { this.sourceDescription = sourceDescription; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
