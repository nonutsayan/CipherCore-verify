package com.ciphercore.verify.model.enums;

/**
 * Deliverable 6: AICTE Official Scrutiny Decision Outcomes
 */
public enum OfficialDecisionOutcome {
    APPROVED_UNCONDITIONAL_EOA("Extension of Approval (EoA) Granted Unconditionally"),
    APPROVED_CONDITIONAL_EOA("Conditional EoA Granted (Subject to Compliance Affidavit within 30 days)"),
    DEFICIENCY_NOTICE_ISSUED("Deficiency Notice Issued (15-Day Rectification Period)"),
    SPOT_INSPECTION_MANDATED("Mandatory Physical Spot Inspection by AICTE Expert Visiting Committee (EVC)"),
    REJECTED_REFUSAL_OF_APPROVAL("Refusal of Approval / Intake Suspended"),
    DEFERRED_PENDING_CLARIFICATION("Scrutiny Deferred Pending Clarification from Affiliating University");

    private final String displayTitle;

    OfficialDecisionOutcome(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }
}
