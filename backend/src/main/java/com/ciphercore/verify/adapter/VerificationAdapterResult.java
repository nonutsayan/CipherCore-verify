package com.ciphercore.verify.adapter;

import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import com.ciphercore.verify.model.enums.ComparisonMethod;
import com.ciphercore.verify.model.enums.FieldComparisonStatus;

import java.util.List;
import java.util.Map;

/**
 * Section 8: Provider-independent verification adapter response contract.
 */
public record VerificationAdapterResult(
    String sourceId,
    AuthorityVerificationStatus overallStatus,
    boolean recordFound,
    String sourceRecordId,
    String recordMatchStatus,
    String validityStatus,
    String signatureStatus,
    String qrStatus,
    String summary,
    String limitations,
    List<FieldComparisonItem> fieldComparisons,
    List<EvidenceItem> evidenceItems,
    int responseStatusCode,
    String errorCategory,
    Map<String, Object> rawResponseMetadata
) {
    public record FieldComparisonItem(
        String fieldName,
        String submittedValue,
        String officialValue,
        ComparisonMethod comparisonMethod,
        FieldComparisonStatus comparisonStatus,
        String evidenceSnippet,
        Double confidence
    ) {}

    public record EvidenceItem(
        String evidenceType,
        String sourceId,
        String sourceRecordId,
        int pageNumber,
        String metadataJson
    ) {}
}
