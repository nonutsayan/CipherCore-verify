package com.ciphercore.verify.model.enums;

/**
 * Section 16: Processing Status and Asynchronous Execution Lifecycle
 */
public enum DocumentProcessingStatus {
    UPLOADED,
    PROCESSING,
    EXTRACTING_TEXT,
    CLASSIFYING,
    EXTRACTING_FIELDS,
    VALIDATING,
    COMPLETED,
    FAILED
}
