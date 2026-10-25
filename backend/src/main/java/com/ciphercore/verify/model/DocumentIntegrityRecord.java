package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 23A: Document Integrity Record
 * Tracks cryptographic SHA-256 hash, byte size, detected MIME type, and safe intake status.
 */
@Entity
@Table(name = "document_integrity_records")
public class DocumentIntegrityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false, length = 64)
    private String sha256;

    private long fileSizeBytes;

    @Column(length = 64)
    private String detectedMediaType;

    private int pageCount = 1;

    @Column(length = 32)
    private String integrityCheckStatus = "VALID"; // VALID, CORRUPTED, MALFORMED, SIZE_EXCEEDED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DocumentIntegrityRecord() {}

    public DocumentIntegrityRecord(Long documentId, String sha256, long fileSizeBytes, String detectedMediaType, int pageCount, String integrityCheckStatus) {
        this.documentId = documentId;
        this.sha256 = sha256;
        this.fileSizeBytes = fileSizeBytes;
        this.detectedMediaType = detectedMediaType;
        this.pageCount = pageCount;
        this.integrityCheckStatus = integrityCheckStatus;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getDetectedMediaType() { return detectedMediaType; }
    public void setDetectedMediaType(String detectedMediaType) { this.detectedMediaType = detectedMediaType; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public String getIntegrityCheckStatus() { return integrityCheckStatus; }
    public void setIntegrityCheckStatus(String integrityCheckStatus) { this.integrityCheckStatus = integrityCheckStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
