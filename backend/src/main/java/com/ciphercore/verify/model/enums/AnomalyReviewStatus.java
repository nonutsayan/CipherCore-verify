package com.ciphercore.verify.model.enums;

/**
 * Section 26: Human Review and Disposition Workflow Status
 */
public enum AnomalyReviewStatus {
    OPEN,
    UNDER_REVIEW,
    NEEDS_CLARIFICATION,
    VERIFICATION_PENDING,
    RESOLVED_LEGITIMATE_DIFFERENCE,
    RESOLVED_FALSE_POSITIVE,
    CONFIRMED_DISCREPANCY,
    ESCALATED,
    CLOSED
}
