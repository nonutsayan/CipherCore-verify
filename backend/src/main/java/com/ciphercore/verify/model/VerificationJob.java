package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import com.ciphercore.verify.model.enums.VerificationJobStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 21B & 25: Verification Job Entity
 * Tracks asynchronous job lifecycle, priorities, bounded retries, and errors.
 */
@Entity
@Table(name = "verification_jobs")
public class VerificationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jobReference;

    @Column(nullable = false)
    private Long documentId;

    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private VerificationJobStatus status = VerificationJobStatus.QUEUED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthorityVerificationStatus userFacingStatus = AuthorityVerificationStatus.PENDING;

    @Column(length = 16)
    private String priority = "NORMAL";

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    private int retryCount = 0;
    private int maxRetries = 3;
    private LocalDateTime nextRetryAt;

    @Column(length = 64)
    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public VerificationJob() {}

    public VerificationJob(String jobReference, Long documentId, Long applicationId, String sourceId) {
        this.jobReference = jobReference;
        this.documentId = documentId;
        this.applicationId = applicationId;
        this.sourceId = sourceId;
        this.status = VerificationJobStatus.QUEUED;
        this.userFacingStatus = AuthorityVerificationStatus.PENDING;
        this.startedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getJobReference() { return jobReference; }
    public void setJobReference(String jobReference) { this.jobReference = jobReference; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public VerificationJobStatus getStatus() { return status; }
    public void setStatus(VerificationJobStatus status) { this.status = status; }

    public AuthorityVerificationStatus getUserFacingStatus() { return userFacingStatus; }
    public void setUserFacingStatus(AuthorityVerificationStatus userFacingStatus) { this.userFacingStatus = userFacingStatus; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(LocalDateTime nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
