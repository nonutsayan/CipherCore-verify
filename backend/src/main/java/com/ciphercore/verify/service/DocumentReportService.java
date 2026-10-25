package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Section 15: Structured JSON Document Intelligence Report Service
 * Produces structured, evidence-backed JSON reports strictly conforming to Section 15 schema.
 */
@Service
public class DocumentReportService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentPageRepository documentPageRepository;

    @Autowired
    private ExtractedDocumentFieldRepository extractedFieldRepository;

    @Autowired
    private DocumentValidationResultRepository validationResultRepository;

    @Autowired
    private DocumentReviewRepository documentReviewRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> generateStructuredReport(Long documentId) {
        Document doc = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("documentId", String.valueOf(doc.getId()));

        // File metadata
        Map<String, Object> fileMeta = new LinkedHashMap<>();
        fileMeta.put("originalFilename", doc.getOriginalFilename());
        fileMeta.put("mediaType", doc.getMimeType());
        fileMeta.put("fileSizeBytes", doc.getFileSizeBytes());
        fileMeta.put("sha256", doc.getSha256Checksum());
        fileMeta.put("pageCount", doc.getPageCount());
        report.put("file", fileMeta);

        // Processing metadata
        Map<String, Object> processing = new LinkedHashMap<>();
        processing.put("status", doc.getProcessingStatus() != null ? doc.getProcessingStatus().name() : "COMPLETED");
        processing.put("ocrEngine", doc.getOcrEngine());
        processing.put("ocrQuality", doc.getOcrQuality());
        processing.put("startedAt", doc.getUploadTimestamp() != null ? doc.getUploadTimestamp().toString() : null);
        processing.put("completedAt", doc.getProcessedTimestamp() != null ? doc.getProcessedTimestamp().toString() : null);

        List<DocumentPage> pages = documentPageRepository.findByDocumentIdOrderByPageNumberAsc(doc.getId());
        List<String> warnings = new ArrayList<>();
        for (DocumentPage p : pages) {
            if (p.getWarnings() != null && !p.getWarnings().isBlank()) {
                warnings.add("Page " + p.getPageNumber() + ": " + p.getWarnings());
            }
        }
        processing.put("warnings", warnings);
        report.put("processing", processing);

        // Classification metadata
        Map<String, Object> classification = new LinkedHashMap<>();
        classification.put("category", doc.getDocumentCategory() != null ? doc.getDocumentCategory().name() : "UNKNOWN");
        classification.put("predictedCategory", doc.getPredictedCategory() != null ? doc.getPredictedCategory().name() : null);
        classification.put("confidence", doc.getClassificationConfidence());
        List<String> evidenceList = doc.getClassificationEvidence() != null ? 
            Arrays.asList(doc.getClassificationEvidence().split(";\\s*")) : List.of();
        classification.put("evidence", evidenceList);
        classification.put("requiresManualReview", doc.isRequiresManualReview());
        report.put("classification", classification);

        // Extracted fields
        List<ExtractedDocumentField> fields = extractedFieldRepository.findByDocumentId(doc.getId());
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        for (ExtractedDocumentField f : fields) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("value", f.getRawValue());
            item.put("normalizedValue", f.getNormalizedValue());
            item.put("effectiveValue", f.getEffectiveValue());
            item.put("status", f.getValueStatus() != null ? f.getValueStatus().name() : "NOT_FOUND");
            item.put("sourceEvidence", f.getSourceEvidence());
            item.put("pageNumber", f.getPageNumber());
            item.put("confidence", f.getConfidenceScore());
            item.put("reviewedValue", f.getReviewedValue());
            item.put("reviewedBy", f.getReviewedBy());
            fieldMap.put(f.getFieldName(), item);
        }
        report.put("extractedFields", fieldMap);

        // Completeness evaluation
        Map<String, Object> completeness = new LinkedHashMap<>();
        completeness.put("status", doc.getCompletenessStatus() != null ? doc.getCompletenessStatus().name() : "PASS");
        completeness.put("summary", doc.getCompletenessSummary());
        completeness.put("manualReviewRequired", doc.isRequiresManualReview());
        report.put("completeness", completeness);

        // Consistency checks
        List<DocumentValidationResult> valResults = validationResultRepository.findByDocumentId(doc.getId());
        List<Map<String, Object>> consistencyList = new ArrayList<>();
        for (DocumentValidationResult vr : valResults) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ruleId", vr.getRuleId());
            item.put("severity", vr.getSeverity().name());
            item.put("status", vr.getStatus().name());
            item.put("message", vr.getMessage());
            item.put("evidence", vr.getEvidence());
            item.put("recommendedAction", vr.getRecommendedAction());
            item.put("manualReviewRequired", vr.isManualReviewRequired());
            consistencyList.add(item);
        }
        report.put("consistencyChecks", consistencyList);

        // Review & human-in-the-loop status
        List<DocumentReview> reviews = documentReviewRepository.findByDocumentIdOrderByReviewedAtDesc(doc.getId());
        Map<String, Object> reviewInfo = new LinkedHashMap<>();
        if (!reviews.isEmpty()) {
            DocumentReview latest = reviews.get(0);
            reviewInfo.put("status", latest.getReviewStatus().name());
            reviewInfo.put("reviewerId", latest.getReviewerId());
            reviewInfo.put("reviewedAt", latest.getReviewedAt().toString());
            reviewInfo.put("reviewNotes", latest.getReviewNotes());
        } else {
            reviewInfo.put("status", "PENDING");
            reviewInfo.put("reviewerId", null);
            reviewInfo.put("reviewedAt", null);
            reviewInfo.put("reviewNotes", null);
        }
        report.put("review", reviewInfo);

        // Authenticity Boundary (Section 20)
        Map<String, Object> authBoundary = new LinkedHashMap<>();
        authBoundary.put("status", doc.getAuthenticityStatus());
        authBoundary.put("notice", "Independent trusted authority verification required. Successful OCR and field completeness do NOT certify legal authenticity.");
        report.put("authenticity", authBoundary);

        return report;
    }
}
