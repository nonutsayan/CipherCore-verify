package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 21C: Verification Attempt Entity
 * Tracks network requests, response codes, latencies, and sanitized error categories.
 */
@Entity
@Table(name = "verification_attempts")
public class VerificationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long verificationJobId;

    private int attemptNumber = 1;

    @Column(length = 64)
    private String requestType = "DIRECT_LOOKUP";

    @Column(nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    @Column(length = 32)
    private String resultStatus;

    private Integer responseStatusCode;

    @Column(length = 64)
    private String errorCategory; // TIMEOUT, RATE_LIMIT, SSRF_BLOCKED, AUTH_ERROR, RECORD_NOT_FOUND, NONE

    @Column(columnDefinition = "TEXT")
    private String sanitizedResponseMetadata;

    public VerificationAttempt() {}

    public VerificationAttempt(Long verificationJobId, int attemptNumber, String requestType) {
        this.verificationJobId = verificationJobId;
        this.attemptNumber = attemptNumber;
        this.requestType = requestType;
        this.startedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVerificationJobId() { return verificationJobId; }
    public void setVerificationJobId(Long verificationJobId) { this.verificationJobId = verificationJobId; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public Integer getResponseStatusCode() { return responseStatusCode; }
    public void setResponseStatusCode(Integer responseStatusCode) { this.responseStatusCode = responseStatusCode; }

    public String getErrorCategory() { return errorCategory; }
    public void setErrorCategory(String errorCategory) { this.errorCategory = errorCategory; }

    public String getSanitizedResponseMetadata() { return sanitizedResponseMetadata; }
    public void setSanitizedResponseMetadata(String sanitizedResponseMetadata) { this.sanitizedResponseMetadata = sanitizedResponseMetadata; }
}
