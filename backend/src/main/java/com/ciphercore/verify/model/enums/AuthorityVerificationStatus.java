package com.ciphercore.verify.model.enums;

/**
 * Section 5: User-Facing Issuing Authority Verification Statuses
 */
public enum AuthorityVerificationStatus {
    VERIFIED,
    MISMATCH,
    PENDING,
    EXPIRED,
    SOURCE_UNAVAILABLE,
    MANUAL_REVIEW
}
