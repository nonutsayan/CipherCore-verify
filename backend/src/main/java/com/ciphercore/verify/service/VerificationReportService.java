package com.ciphercore.verify.service;

import com.ciphercore.verify.model.FieldComparison;
import com.ciphercore.verify.model.VerificationEvidence;
import com.ciphercore.verify.model.VerificationResult;
import com.ciphercore.verify.model.VerificationReview;
import com.ciphercore.verify.repository.FieldComparisonRepository;
import com.ciphercore.verify.repository.VerificationEvidenceRepository;
import com.ciphercore.verify.repository.VerificationResultRepository;
import com.ciphercore.verify.repository.VerificationReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Section 20: Comprehensive Verification Report Generation Service.
 * Produces structured Section 20 JSON and printable audit assessments
 * distinguishing record corroboration, cryptographic integrity, and compliance validity.
 */
@Service
public class VerificationReportService {

    private final VerificationResultRepository resultRepository;
    private final FieldComparisonRepository fieldComparisonRepository;
    private final VerificationEvidenceRepository evidenceRepository;
    private final VerificationReviewRepository reviewRepository;

    public VerificationReportService(
            VerificationResultRepository resultRepository,
            FieldComparisonRepository fieldComparisonRepository,
            VerificationEvidenceRepository evidenceRepository,
            VerificationReviewRepository reviewRepository
    ) {
        this.resultRepository = resultRepository;
        this.fieldComparisonRepository = fieldComparisonRepository;
        this.evidenceRepository = evidenceRepository;
        this.reviewRepository = reviewRepository;
    }

    public Map<String, Object> generateSection20Report(Long verificationResultId) {
        Optional<VerificationResult> resOpt = resultRepository.findById(verificationResultId);
        if (resOpt.isEmpty()) {
            throw new IllegalArgumentException("Verification result not found with ID: " + verificationResultId);
        }
        VerificationResult res = resOpt.get();
        List<FieldComparison> comparisons = fieldComparisonRepository.findByVerificationResultId(verificationResultId);
        List<VerificationEvidence> evidenceList = evidenceRepository.findByVerificationResultId(verificationResultId);
        List<VerificationReview> reviews = reviewRepository.findByVerificationResultId(verificationResultId);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportSchemaVersion", "2.0-SIH26261");
        report.put("reportType", "SECTION_20_ISSUING_AUTHORITY_VERIFICATION");
        report.put("verificationResultId", res.getId());
        report.put("documentId", res.getDocumentId());
        report.put("applicationId", res.getApplicationId());
        report.put("generatedAt", LocalDateTime.now().toString());

        // Overall Status
        report.put("overallStatus", res.getOverallStatus().name());
        report.put("manualReviewRequired", res.isManualReviewRequired());

        // Multi-dimensional breakdown
        Map<String, Object> dimensions = new LinkedHashMap<>();
        dimensions.put("recordMatchStatus", res.getRecordMatchStatus());
        dimensions.put("validityStatus", res.getValidityStatus());
        dimensions.put("signatureStatus", res.getSignatureStatus());
        dimensions.put("qrStatus", res.getQrStatus());
        dimensions.put("sourceId", res.getSourceId());
        dimensions.put("sourceRecordId", res.getSourceRecordId());
        report.put("verificationDimensions", dimensions);

        // Human-readable narrative
        report.put("executiveSummary", res.getSummary());
        report.put("technicalLimitations", res.getLimitations());

        // Field comparisons
        List<Map<String, Object>> fcItems = new ArrayList<>();
        for (FieldComparison fc : comparisons) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fieldName", fc.getFieldName());
            item.put("submittedValue", fc.getSubmittedValue());
            item.put("officialValue", fc.getOfficialValue());
            item.put("comparisonMethod", fc.getComparisonMethod().name());
            item.put("comparisonStatus", fc.getComparisonStatus().name());
            item.put("evidenceSnippet", fc.getEvidenceSnippet());
            item.put("confidence", fc.getConfidence());
            fcItems.add(item);
        }
        report.put("fieldComparisons", fcItems);

        // Verification Evidence items
        List<Map<String, Object>> evItems = new ArrayList<>();
        for (VerificationEvidence ev : evidenceList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evidenceId", ev.getId());
            item.put("evidenceType", ev.getEvidenceType());
            item.put("sourceId", ev.getSourceId());
            item.put("sourceRecordId", ev.getSourceRecordId());
            item.put("pageNumber", ev.getPageNumber());
            item.put("observedAt", ev.getObservedAt().toString());
            item.put("metadata", ev.getEvidenceMetadata());
            evItems.add(item);
        }
        report.put("supportingEvidence", evItems);

        // Review History
        List<Map<String, Object>> revItems = new ArrayList<>();
        for (VerificationReview r : reviews) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("reviewerId", r.getReviewerId());
            item.put("decision", r.getReviewDecision());
            item.put("notes", r.getNotes());
            item.put("reviewedAt", r.getReviewedAt().toString());
            revItems.add(item);
        }
        report.put("reviewerDispositions", revItems);

        // Statutory notice & disclaimer
        report.put("regulatoryNotice", "This verification report corroborates submitted document metadata against authorized institutional sources. Corroboration of record existence does not replace on-site infrastructure inspection or formal regulatory committee approval.");

        return report;
    }
}
