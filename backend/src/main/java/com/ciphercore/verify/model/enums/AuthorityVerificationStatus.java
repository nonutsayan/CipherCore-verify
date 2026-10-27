package com.ciphercore.verify.model.enums;

/**
 * Section 5 & Section 8: User-Facing Issuing Authority Verification Statuses
 * Standards compliant with SIH Problem Statement SIH26261.
 */
public enum AuthorityVerificationStatus {
    VERIFIED,
    LIVE_VERIFIED,
    DEMO_SIMULATED,
    MISMATCH,
    PENDING,
    EXPIRED,
    SOURCE_UNAVAILABLE,
    MANUAL_REVIEW,
    FAILED
}
