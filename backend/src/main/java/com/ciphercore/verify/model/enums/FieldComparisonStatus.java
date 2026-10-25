package com.ciphercore.verify.model.enums;

/**
 * Section 14: Field-by-Field Official Record Comparison Statuses
 */
public enum FieldComparisonStatus {
    MATCH,
    MATCH_AFTER_NORMALIZATION,
    PARTIAL_MATCH,
    MISMATCH,
    NOT_AVAILABLE_FROM_SOURCE,
    NOT_COMPARABLE,
    NEEDS_REVIEW
}
