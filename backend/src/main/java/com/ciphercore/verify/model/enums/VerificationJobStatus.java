package com.ciphercore.verify.model.enums;

/**
 * Section 5G: Internal Verification Job and Attempt Lifecycle States
 */
public enum VerificationJobStatus {
    NOT_STARTED,
    QUEUED,
    IN_PROGRESS,
    RETRY_SCHEDULED,
    PARTIALLY_VERIFIED,
    VERIFICATION_FAILED,
    VERIFICATION_UNSUPPORTED,
    SOURCE_NOT_CONFIGURED,
    AUTHORIZATION_REQUIRED,
    SOURCE_RESPONSE_INVALID,
    RECORD_NOT_FOUND,
    CANCELLED,
    COMPLETED
}
