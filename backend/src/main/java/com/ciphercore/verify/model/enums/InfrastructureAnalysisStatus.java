package com.ciphercore.verify.model.enums;

public enum InfrastructureAnalysisStatus {
    NOT_ANALYZED,
    ANALYSIS_QUEUED,
    ANALYZING,
    OBSERVATIONS_AVAILABLE,
    POSSIBLE_DISCREPANCY,
    INSUFFICIENT_QUALITY,
    UNSUPPORTED_FORMAT,
    ANALYSIS_FAILED,
    MANUAL_REVIEW,
    REVIEW_COMPLETED
}
