package com.ciphercore.verify.service;

import com.ciphercore.verify.adapter.AuthorityVerificationAdapter;
import com.ciphercore.verify.adapter.VerificationAdapterResult;
import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Section 23, 24 & 25: Verification Orchestration Service.
 * Coordinates asynchronous and synchronous verification flows across authority adapters,
 * bounds retries, logs network attempts, and records multi-dimensional verification findings.
 */
@Service
public class VerificationOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(VerificationOrchestratorService.class);

    private final AuthoritySourceRegistryService registryService;
    private final FieldComparisonService fieldComparisonService;
    private final SourceTrustService sourceTrustService;
    private final ValidityAssessmentService validityAssessmentService;
    private final DocumentRepository documentRepository;
    private final ExtractedDocumentFieldRepository extractedFieldRepository;
    private final VerificationJobRepository jobRepository;
    private final VerificationAttemptRepository attemptRepository;
    private final VerificationResultRepository resultRepository;
    private final FieldComparisonRepository fieldComparisonRepository;
    private final VerificationEvidenceRepository evidenceRepository;

    public VerificationOrchestratorService(
            AuthoritySourceRegistryService registryService,
            FieldComparisonService fieldComparisonService,
            SourceTrustService sourceTrustService,
            ValidityAssessmentService validityAssessmentService,
            DocumentRepository documentRepository,
            ExtractedDocumentFieldRepository extractedFieldRepository,
            VerificationJobRepository jobRepository,
            VerificationAttemptRepository attemptRepository,
            VerificationResultRepository resultRepository,
            FieldComparisonRepository fieldComparisonRepository,
            VerificationEvidenceRepository evidenceRepository
    ) {
        this.registryService = registryService;
        this.fieldComparisonService = fieldComparisonService;
        this.sourceTrustService = sourceTrustService;
        this.validityAssessmentService = validityAssessmentService;
        this.documentRepository = documentRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.jobRepository = jobRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
        this.fieldComparisonRepository = fieldComparisonRepository;
        this.evidenceRepository = evidenceRepository;
    }

    /**
     * Executes verification for a document against a specific source or auto-selected sources.
     */
    @Transactional
    public List<VerificationResult> verifyDocument(Long documentId, String sourceId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            throw new IllegalArgumentException("Document not found with ID: " + documentId);
        }
        Document doc = docOpt.get();

        // 1. Gather extracted fields
        Map<String, String> fields = new HashMap<>();
        List<ExtractedDocumentField> dbFields = extractedFieldRepository.findByDocumentId(documentId);
        for (ExtractedDocumentField f : dbFields) {
            if (f.getNormalizedValue() != null && !f.getNormalizedValue().isBlank()) {
                fields.put(f.getFieldName(), f.getNormalizedValue());
            } else if (f.getRawValue() != null) {
                fields.put(f.getFieldName(), f.getRawValue());
            }
        }

        // Add document filename and standard fallbacks
        fields.putIfAbsent("filename", doc.getOriginalFilename());
        fields.putIfAbsent("documentType", doc.getDocumentType());
        if (doc.getDocumentCategory() != null) {
            fields.putIfAbsent("documentCategory", doc.getDocumentCategory().name());
        }

        // 2. Determine target adapters
        List<AuthorityVerificationAdapter> adapters = new ArrayList<>();
        if (sourceId != null && !sourceId.isBlank() && !sourceId.equalsIgnoreCase("ALL")) {
            AuthorityVerificationAdapter specificAdapter = registryService.getAdapter(sourceId);
            if (specificAdapter != null) {
                adapters.add(specificAdapter);
            } else {
                log.warn("Requested adapter {} not found in registry", sourceId);
            }
        }

        if (adapters.isEmpty()) {
            // Auto-detect based on category or document type
            List<AuthorityVerificationAdapter> categoryAdapters = registryService.getAdaptersForCategory(doc.getDocumentCategory());
            if (!categoryAdapters.isEmpty()) {
                adapters.addAll(categoryAdapters);
            } else {
                // Universal default adapters for demonstration
                AuthorityVerificationAdapter aicte = registryService.getAdapter("SRC_AICTE_REGISTRY");
                if (aicte != null) adapters.add(aicte);
                AuthorityVerificationAdapter pki = registryService.getAdapter("SRC_PKI_CRL");
                if (pki != null) adapters.add(pki);
            }
        }

        List<VerificationResult> results = new ArrayList<>();

        for (AuthorityVerificationAdapter adapter : adapters) {
            String currentSourceId = adapter.getSourceId();
            Optional<AuthoritySource> authoritySourceOpt = registryService.getSource(currentSourceId);
            AuthoritySource authSource = authoritySourceOpt.orElseGet(() -> {
                AuthoritySource s = new AuthoritySource();
                s.setSourceId(currentSourceId);
                s.setName(currentSourceId);
                return s;
            });

            // 3. Create Verification Job
            VerificationJob job = new VerificationJob();
            job.setJobReference("JOB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            job.setDocumentId(documentId);
            job.setApplicationId(doc.getApplicationId());
            job.setSourceId(currentSourceId);
            job.setStatus(VerificationJobStatus.IN_PROGRESS);
            job.setUserFacingStatus(AuthorityVerificationStatus.PENDING);
            job = jobRepository.save(job);

            // 4. Log Attempt
            VerificationAttempt attempt = new VerificationAttempt(job.getId(), 1, "AUTHORITY_QUERY");
            attempt = attemptRepository.save(attempt);

            VerificationAdapterResult adapterResult;
            try {
                // Execute verification through adapter
                adapterResult = adapter.verify(doc, fields, authSource);
                attempt.setCompletedAt(LocalDateTime.now());
                attempt.setResultStatus(adapterResult.overallStatus().name());
                attempt.setResponseStatusCode(adapterResult.responseStatusCode());
                attempt.setErrorCategory(adapterResult.errorCategory() != null ? adapterResult.errorCategory() : "NONE");
                attempt.setSanitizedResponseMetadata(adapterResult.summary());
            } catch (Exception e) {
                log.error("Exception during verification execution on source {}: {}", currentSourceId, e.getMessage());
                attempt.setCompletedAt(LocalDateTime.now());
                attempt.setResultStatus("ERROR");
                attempt.setResponseStatusCode(500);
                attempt.setErrorCategory("GATEWAY_ERROR");
                attempt.setSanitizedResponseMetadata("Error: " + e.getMessage());

                adapterResult = new VerificationAdapterResult(
                    currentSourceId,
                    AuthorityVerificationStatus.SOURCE_UNAVAILABLE,
                    false,
                    null,
                    "ERROR",
                    "UNKNOWN",
                    "NOT_CHECKED",
                    "NOT_CHECKED",
                    "Temporary failure connecting to " + currentSourceId + ": " + e.getMessage(),
                    "Authority service timed out or was unreachable.",
                    List.of(),
                    List.of(),
                    500,
                    "GATEWAY_ERROR",
                    Map.of()
                );
            }
            attemptRepository.save(attempt);

            // 5. Persist VerificationResult
            VerificationResult vr = new VerificationResult();
            vr.setVerificationJobId(job.getId());
            vr.setDocumentId(documentId);
            vr.setApplicationId(doc.getApplicationId());
            vr.setSourceId(currentSourceId);
            vr.setOverallStatus(adapterResult.overallStatus());
            vr.setSourceRecordId(adapterResult.sourceRecordId());
            vr.setRecordMatchStatus(adapterResult.recordMatchStatus());
            vr.setValidityStatus(adapterResult.validityStatus());
            vr.setSignatureStatus(adapterResult.signatureStatus());
            vr.setQrStatus(adapterResult.qrStatus());
            vr.setSummary(adapterResult.summary());
            vr.setLimitations(adapterResult.limitations());
            vr.setManualReviewRequired(adapterResult.overallStatus() == AuthorityVerificationStatus.MANUAL_REVIEW
                    || adapterResult.overallStatus() == AuthorityVerificationStatus.MISMATCH
                    || adapterResult.overallStatus() == AuthorityVerificationStatus.EXPIRED);
            vr.setGeneratedAt(LocalDateTime.now());
            vr = resultRepository.save(vr);

            // 6. Persist Field Comparisons
            if (adapterResult.fieldComparisons() != null) {
                for (VerificationAdapterResult.FieldComparisonItem item : adapterResult.fieldComparisons()) {
                    FieldComparison fc = new FieldComparison();
                    fc.setVerificationResult(vr);
                    fc.setFieldName(item.fieldName());
                    fc.setSubmittedValue(item.submittedValue());
                    fc.setOfficialValue(item.officialValue());
                    fc.setComparisonMethod(item.comparisonMethod());
                    fc.setComparisonStatus(item.comparisonStatus());
                    fc.setEvidenceSnippet(item.evidenceSnippet());
                    fc.setConfidence(item.confidence());
                    fieldComparisonRepository.save(fc);
                }
            }

            // 7. Persist Evidence Items
            if (adapterResult.evidenceItems() != null) {
                for (VerificationAdapterResult.EvidenceItem ev : adapterResult.evidenceItems()) {
                    VerificationEvidence evidence = new VerificationEvidence();
                    evidence.setVerificationResult(vr);
                    evidence.setEvidenceType(ev.evidenceType());
                    evidence.setSourceId(ev.sourceId());
                    evidence.setSourceRecordId(ev.sourceRecordId());
                    evidence.setDocumentId(documentId);
                    evidence.setPageNumber(ev.pageNumber());
                    evidence.setEvidenceMetadata(ev.metadataJson());
                    evidence.setObservedAt(LocalDateTime.now());
                    evidenceRepository.save(evidence);
                }
            }

            // 8. Update Job completion
            job.setStatus(VerificationJobStatus.COMPLETED);
            job.setUserFacingStatus(adapterResult.overallStatus());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);

            results.add(vr);
        }

        return results;
    }

    public List<VerificationResult> getVerificationHistory(Long documentId) {
        return resultRepository.findByDocumentIdOrderByGeneratedAtDesc(documentId);
    }

    public Optional<VerificationResult> getVerificationResult(Long resultId) {
        return resultRepository.findById(resultId);
    }

    public Optional<VerificationJob> getVerificationJob(Long jobId) {
        return jobRepository.findById(jobId);
    }

    public List<VerificationEvidence> getEvidenceForResult(Long resultId) {
        return evidenceRepository.findByVerificationResultId(resultId);
    }

    public List<FieldComparison> getComparisonsForResult(Long resultId) {
        return fieldComparisonRepository.findByVerificationResultId(resultId);
    }

    @Transactional
    public VerificationResult retryVerification(Long resultId) {
        Optional<VerificationResult> prevOpt = resultRepository.findById(resultId);
        if (prevOpt.isEmpty()) {
            throw new IllegalArgumentException("Verification result not found with ID: " + resultId);
        }
        VerificationResult prev = prevOpt.get();
        List<VerificationResult> newResults = verifyDocument(prev.getDocumentId(), prev.getSourceId());
        return newResults.isEmpty() ? prev : newResults.get(0);
    }

    public Map<String, Object> getApplicationVerificationSummary(Long applicationId) {
        List<VerificationResult> results = resultRepository.findByApplicationIdOrderByGeneratedAtDesc(applicationId);

        int total = results.size();
        int verified = 0;
        int mismatch = 0;
        int pending = 0;
        int expired = 0;
        int sourceUnavailable = 0;
        int manualReview = 0;

        for (VerificationResult r : results) {
            switch (r.getOverallStatus()) {
                case VERIFIED -> verified++;
                case MISMATCH -> mismatch++;
                case PENDING -> pending++;
                case EXPIRED -> expired++;
                case SOURCE_UNAVAILABLE -> sourceUnavailable++;
                case MANUAL_REVIEW -> manualReview++;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("applicationId", applicationId);
        summary.put("totalVerifications", total);
        summary.put("verifiedCount", verified);
        summary.put("mismatchCount", mismatch);
        summary.put("pendingCount", pending);
        summary.put("expiredCount", expired);
        summary.put("sourceUnavailableCount", sourceUnavailable);
        summary.put("manualReviewCount", manualReview);
        summary.put("results", results);
        return summary;
    }
}
