package com.ciphercore.verify.model.enums;

/**
 * Section 7: Administrative Configuration Status for Approved Sources
 */
public enum AuthoritySourceStatus {
    CONFIGURED_ACTIVE,
    CONFIGURED_SANDBOX,
    TESTING,
    RATE_LIMITED,
    OFFLINE,
    UNCONFIGURED,
    DISABLED
}
