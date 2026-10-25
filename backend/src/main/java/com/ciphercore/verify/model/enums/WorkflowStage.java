package com.ciphercore.verify.model.enums;

/**
 * Deliverable 6: Six-Stage Application Assessment Workflow
 */
public enum WorkflowStage {
    APPLICATION("Stage 1: Application Intake & Classification"),
    DOCUMENTS("Stage 2: Document Completeness & OCR"),
    VALIDATION("Stage 3: Deterministic & Consistency Validation"),
    VERIFICATION("Stage 4: Issuing Authority Verification"),
    COMPLIANCE("Stage 5: AICTE Regulatory & Infrastructure Compliance"),
    FINAL_REVIEW("Stage 6: Scrutiny Committee & Final Official Review");

    private final String description;

    WorkflowStage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
