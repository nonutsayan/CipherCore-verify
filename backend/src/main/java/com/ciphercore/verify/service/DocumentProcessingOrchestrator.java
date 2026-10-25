package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Section 16 & 26: Document Processing Orchestrator
 * Coordinates the full document intelligence pipeline:
 * Security Inspection -> Storage -> OCR & Text Extraction -> Classification ->
 * Schema-Driven Field Extraction -> Completeness Validation -> Consistency Checks -> Report Assembly.
 */
@Service
public class DocumentProcessingOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingOrchestrator.class);

    @Autowired
    private DocumentSecurityService securityService;

    @Autowired
    private DocumentStorageService storageService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private DocumentClassificationService classificationService;

    @Autowired
    private FieldExtractionService fieldExtractionService;

    @Autowired
    private CompletenessValidationService completenessService;

    @Autowired
    private ConsistencyValidationService consistencyService;

    @Autowired
    private AnomalyDetectionOrchestrator anomalyDetectionOrchestrator;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentPageRepository documentPageRepository;

    @Autowired
    private ExtractedDocumentFieldRepository extractedFieldRepository;

    @Autowired
    private DocumentValidationResultRepository validationResultRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Ingests, processes, and stores an uploaded document.
     */
    @Transactional
    public Document processUploadedDocument(MultipartFile file, Long applicationId, String uploadedBy) throws IOException {
        // 1. Security & Magic-Byte Validation (Section 6)
        DocumentSecurityService.SecurityValidationResult secResult = securityService.validateAndInspect(file);
        if (!secResult.valid()) {
            throw new IllegalArgumentException("Security verification failed: " + secResult.errorMessage());
        }

        // 2. Persist File Securely
        DocumentStorageService.StoredFileInfo storedInfo = storageService.storeFile(file);
        String storedPath = storedInfo.fullPath();

        // 3. Initialize Document Entity
        Document document = new Document();
        document.setOriginalFilename(file.getOriginalFilename());
        document.setStoredPath(storedPath);
        document.setMimeType(secResult.detectedMediaType());
        document.setFileSizeBytes(file.getSize());
        document.setPageCount(Math.max(1, secResult.pageCount()));
        document.setSha256Checksum(secResult.sha256());
        document.setProcessingStatus(DocumentProcessingStatus.PROCESSING);
        document.setUploadedBy(uploadedBy != null ? uploadedBy : "reviewer");
        document.setUploadTimestamp(LocalDateTime.now());
        document.setAuthenticityStatus("AUTHENTICITY_NOT_VERIFIED"); // Section 20 Boundary

        if (applicationId != null) {
            Optional<Application> appOpt = applicationRepository.findById(applicationId);
            appOpt.ifPresent(document::setApplication);
        }

        document = documentRepository.save(document);

        try {
            // 4. OCR & Multi-Page Text Extraction (Section 7)
            document.setProcessingStatus(DocumentProcessingStatus.EXTRACTING_TEXT);
            File diskFile = new File(storedPath);
            OcrService.DocumentInspectionResult ocrResult = ocrService.extractText(diskFile, secResult.detectedMediaType());

            document.setOcrEngine(ocrResult.overallEngine());
            document.setOcrQuality(ocrResult.overallQuality());
            document.setExtractedText(ocrResult.consolidatedText());
            document.setPageCount(Math.max(1, ocrResult.totalPages()));

            // Persist Pages (Section 17B)
            for (OcrService.PageExtractionResult pageRes : ocrResult.pages()) {
                DocumentPage page = new DocumentPage(
                    document,
                    pageRes.pageNumber(),
                    pageRes.extractedText(),
                    pageRes.extractionMethod(),
                    pageRes.ocrQuality()
                );
                page.setWarnings(pageRes.warning());
                documentPageRepository.save(page);
            }

            // 5. Automatic Document Classification (Section 8)
            document.setProcessingStatus(DocumentProcessingStatus.CLASSIFYING);
            DocumentClassificationService.ClassificationOutcome classOutcome = 
                classificationService.classifyDocument(ocrResult.consolidatedText());

            document.setPredictedCategory(classOutcome.primaryCategory());
            document.setDocumentCategory(classOutcome.primaryCategory());
            document.setClassificationConfidence(classOutcome.confidenceScore());
            document.setClassificationEvidence(String.join("; ", classOutcome.evidencePhrases()));
            document.setClassificationMethod(classOutcome.classificationMethod());
            document.setRequiresManualReview(classOutcome.manualConfirmationRecommended());

            // 6. Schema-Driven Field Extraction (Section 9 & 10)
            document.setProcessingStatus(DocumentProcessingStatus.EXTRACTING_FIELDS);
            Map<String, FieldExtractionService.ExtractedFieldItem> extractedFields = 
                fieldExtractionService.extractFieldsForCategory(classOutcome.primaryCategory(), ocrResult.pages());

            for (FieldExtractionService.ExtractedFieldItem item : extractedFields.values()) {
                ExtractedDocumentField field = new ExtractedDocumentField(
                    document,
                    item.fieldName(),
                    item.rawValue(),
                    item.normalizedValue(),
                    item.status(),
                    item.sourceEvidence(),
                    item.confidence(),
                    item.pageNumber(),
                    item.extractionMethod()
                );
                extractedFieldRepository.save(field);
            }

            // 7. Document Completeness Validation (Section 12)
            document.setProcessingStatus(DocumentProcessingStatus.VALIDATING);
            CompletenessValidationService.CompletenessOutcome compOutcome = 
                completenessService.evaluate(classOutcome.primaryCategory(), extractedFields, ocrResult.overallQuality());

            document.setCompletenessStatus(compOutcome.status());
            document.setCompletenessSummary(compOutcome.summaryMessage());
            if (compOutcome.manualReviewRequired()) {
                document.setRequiresManualReview(true);
            }

            // 8. Consistency & Temporal Validation (Section 13)
            List<DocumentValidationResult> consistencyResults = 
                consistencyService.validateConsistency(document, extractedFields, ocrResult.consolidatedText());

            for (DocumentValidationResult vr : consistencyResults) {
                validationResultRepository.save(vr);
            }

            // 9. Comprehensive Forensic Anomaly & Authentication Engine (Deliverable 3)
            try {
                anomalyDetectionOrchestrator.runComprehensiveAnalysis(document.getId());
            } catch (Exception ae) {
                log.warn("Forensic anomaly engine exception for document {}: {}", document.getId(), ae.getMessage());
            }

            // Finalize Status
            document.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
            document.setExtractionStatus("EXTRACTED");
            document.setProcessedTimestamp(LocalDateTime.now());
            return documentRepository.save(document);

        } catch (Exception e) {
            log.error("Failed to complete document intelligence processing for document {}: {}", document.getId(), e.getMessage(), e);
            document.setProcessingStatus(DocumentProcessingStatus.FAILED);
            document.setExtractionStatus("FAILED");
            return documentRepository.save(document);
        }
    }

    /**
     * Reprocesses an existing document (e.g. after category override or OCR engine update).
     */
    @Transactional
    public Document reprocessDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + documentId));

        // Clean old extraction records
        documentPageRepository.deleteByDocumentId(documentId);
        extractedFieldRepository.findByDocumentId(documentId).forEach(extractedFieldRepository::delete);
        validationResultRepository.deleteByDocumentId(documentId);

        File diskFile = new File(document.getStoredPath());
        if (!diskFile.exists()) {
            throw new IllegalStateException("Underlying file no longer exists on disk: " + document.getStoredPath());
        }

        // Run OCR
        OcrService.DocumentInspectionResult ocrResult = ocrService.extractText(diskFile, document.getMimeType());
        document.setExtractedText(ocrResult.consolidatedText());
        document.setOcrEngine(ocrResult.overallEngine());
        document.setOcrQuality(ocrResult.overallQuality());
        document.setPageCount(Math.max(1, ocrResult.totalPages()));

        for (OcrService.PageExtractionResult pageRes : ocrResult.pages()) {
            DocumentPage page = new DocumentPage(
                document,
                pageRes.pageNumber(),
                pageRes.extractedText(),
                pageRes.extractionMethod(),
                pageRes.ocrQuality()
            );
            page.setWarnings(pageRes.warning());
            documentPageRepository.save(page);
        }

        // Schema extraction using current category
        DocumentCategory targetCategory = document.getDocumentCategory() != null ? 
            document.getDocumentCategory() : DocumentCategory.SUPPORTING_DOCUMENT;

        Map<String, FieldExtractionService.ExtractedFieldItem> extractedFields = 
            fieldExtractionService.extractFieldsForCategory(targetCategory, ocrResult.pages());

        for (FieldExtractionService.ExtractedFieldItem item : extractedFields.values()) {
            ExtractedDocumentField field = new ExtractedDocumentField(
                document,
                item.fieldName(),
                item.rawValue(),
                item.normalizedValue(),
                item.status(),
                item.sourceEvidence(),
                item.confidence(),
                item.pageNumber(),
                item.extractionMethod()
            );
            extractedFieldRepository.save(field);
        }

        // Completeness & Consistency
        CompletenessValidationService.CompletenessOutcome compOutcome = 
            completenessService.evaluate(targetCategory, extractedFields, ocrResult.overallQuality());
        document.setCompletenessStatus(compOutcome.status());
        document.setCompletenessSummary(compOutcome.summaryMessage());

        List<DocumentValidationResult> consistencyResults = 
            consistencyService.validateConsistency(document, extractedFields, ocrResult.consolidatedText());
        for (DocumentValidationResult vr : consistencyResults) {
            validationResultRepository.save(vr);
        }

        // Comprehensive Forensic Anomaly & Authentication Engine
        try {
            anomalyDetectionOrchestrator.runComprehensiveAnalysis(document.getId());
        } catch (Exception ae) {
            log.warn("Forensic anomaly engine exception for document {}: {}", document.getId(), ae.getMessage());
        }

        document.setProcessingStatus(DocumentProcessingStatus.COMPLETED);
        document.setProcessedTimestamp(LocalDateTime.now());
        return documentRepository.save(document);
    }
}
