package com.ciphercore.verify.model.enums;

/**
 * Section 11: Normalization and Data Cleaning Status Tokens
 * Avoids fabricated empty strings or concealing the true status of extracted fields.
 */
public enum FieldExtractionStatus {
    NOT_FOUND,       // Expected field was not identified in the document
    NOT_STATED,      // Evidence indicates the field is intentionally not stated (e.g. NOC without expiry)
    UNREADABLE,      // Content exists but cannot be parsed reliably due to noise or low scan quality
    AMBIGUOUS,       // Multiple conflicting interpretations are possible (e.g. ambiguous dates 04/05/2026)
    NOT_APPLICABLE,  // Field does not apply to this specific document category schema
    EXTRACTED,       // Value was successfully extracted with verifiable source evidence
    NEEDS_REVIEW     // Human confirmation is required before proceeding to automated compliance checks
}
