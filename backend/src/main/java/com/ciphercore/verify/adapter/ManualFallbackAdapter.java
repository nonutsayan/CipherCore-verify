package com.ciphercore.verify.adapter;

import com.ciphercore.verify.model.AuthoritySource;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.enums.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Section 14: Manual Verification & Configurable Fallback Adapter.
 * When an authority is unconfigured, offline, or lacks an automated API,
 * this adapter generates an explainable fallback workflow with structured manual scrutiny instructions,
 * required evidence checklist, and institutional contact routing.
 */
@Component
public class ManualFallbackAdapter implements AuthorityVerificationAdapter {

    public static final String SOURCE_ID = "SRC_MANUAL_REVIEW";

    @Override
    public String getSourceId() {
        return SOURCE_ID;
    }

    @Override
    public AuthoritySourceType getSourceType() {
        return AuthoritySourceType.MANUAL_FALLBACK;
    }

    @Override
    public boolean supportsCategory(DocumentCategory category) {
        return true; // Supports all categories as universal fallback
    }

    @Override
    public VerificationAdapterResult verify(Document document, Map<String, String> extractedFields, AuthoritySource source) {
        List<VerificationAdapterResult.FieldComparisonItem> fieldComparisons = new ArrayList<>();
        List<VerificationAdapterResult.EvidenceItem> evidenceItems = new ArrayList<>();
        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("reviewProtocol", "AICTE Approval Process Handbook (APH) 2026-27 Scrutiny Manual");
        metadata.put("assignedQueue", "Level-2 Scrutiny Committee");
        metadata.put("escalationContact", "scrutiny-officer@aicte-india.org");

        fieldComparisons.add(new VerificationAdapterResult.FieldComparisonItem(
            "automatedCorroborationStatus",
            "Submitted for Scrutiny",
            "Requires Physical / Telephonic / Portal Scrutiny",
            ComparisonMethod.CONTEXTUAL,
            FieldComparisonStatus.NEEDS_REVIEW,
            "Direct automated API gateway not available for this issuer. Manual corroboration protocol initiated.",
            0.50
        ));

        fieldComparisons.add(new VerificationAdapterResult.FieldComparisonItem(
            "issuingOfficeContact",
            extractedFields.getOrDefault("issuingAuthority", "Competent State Authority"),
            "Directory Lookup Pending",
            ComparisonMethod.NORMALIZED_TEXT,
            FieldComparisonStatus.NOT_AVAILABLE_FROM_SOURCE,
            "Officer should consult the authorized State / Directorate nodal officer roster",
            0.60
        ));

        evidenceItems.add(new VerificationAdapterResult.EvidenceItem(
            "MANUAL_REVIEW_GUIDANCE",
            SOURCE_ID,
            "SOP-APH-SCRUTINY-V4",
            1,
            "{\"instruction\":\"Inspect physical seal, watermarking, letterhead serial chronology, and registrar counter-signature\",\"checklist\":[\"Embossed Seal Present\",\"Authorized Officer Stamp Matches AICTE Nodal Directory\",\"Serial Reference Sequence Verified with Regional Office\"]}"
        ));

        return new VerificationAdapterResult(
            SOURCE_ID,
            AuthorityVerificationStatus.MANUAL_REVIEW,
            false,
            null,
            "PENDING_MANUAL_SCRUTINY",
            "PENDING_SCRUTINY",
            "REQUIRES_OFFICER_SIGN_OFF",
            "NOT_APPLICABLE",
            "Automated integration unavailable or document category mandates manual scrutiny. Task assigned to Verification Officer.",
            "Fallback workflow: Reviewer must follow SOP-APH-SCRUTINY-V4 and confirm or reject findings via Scrutiny Review action.",
            fieldComparisons,
            evidenceItems,
            200,
            null,
            metadata
        );
    }

    @Override
    public HealthCheckResult testConnection(AuthoritySource source) {
        return new HealthCheckResult(
            SOURCE_ID,
            AuthoritySourceStatus.CONFIGURED_ACTIVE,
            true,
            5,
            "Manual review fallback queue active and operating normally",
            null
        );
    }
}
