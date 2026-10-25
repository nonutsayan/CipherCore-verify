package com.ciphercore.verify.model.enums;

/**
 * Deliverable 6: Application Readiness for Official Final Review
 */
public enum ReadinessState {
    NOT_READY("Prerequisites Incomplete — Blocked from Final Review"),
    READY_FOR_REVIEW("Prerequisites Satisfied — Ready for Scrutiny Committee"),
    UNDER_REVIEW("Currently Under Scrutiny Committee Examination"),
    AWAITING_ADDITIONAL_EVIDENCE("Pending Submission of Requested Additional Evidence"),
    REVIEW_COMPLETED("Scrutiny Review Deliberation Completed"),
    DECISION_RECORDED("Official Decision Formally Recorded and Cryptographically Sealed");

    private final String description;

    ReadinessState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
