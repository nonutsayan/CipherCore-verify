package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Section 19F: Document Review and Human-in-the-Loop Correction Service
 * Maintains an immutable audit trail for reviewer corrections and decisions.
 */
@Service
public class DocumentReviewService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ExtractedDocumentFieldRepository extractedFieldRepository;

    @Autowired
    private DocumentReviewRepository documentReviewRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private DocumentProcessingOrchestrator orchestrator;

    /**
     * Corrects a single extracted field value with reviewer attribution and notes.
     */
    @Transactional
    public ExtractedDocumentField correctField(Long documentId, String fieldName, String correctedValue, String reviewerId, String notes) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        ExtractedDocumentField field = extractedFieldRepository.findByDocumentIdAndFieldName(documentId, fieldName)
            .orElseGet(() -> {
                ExtractedDocumentField newField = new ExtractedDocumentField();
                newField.setDocument(document);
                newField.setFieldName(fieldName);
                newField.setRawValue(correctedValue);
                newField.setNormalizedValue(correctedValue);
                return newField;
            });

        String previousValue = field.getEffectiveValue();
        field.setReviewedValue(correctedValue);
        field.setNormalizedValue(correctedValue);
        field.setValueStatus(FieldExtractionStatus.EXTRACTED);
        field.setReviewedBy(reviewerId != null ? reviewerId : "reviewer");
        field.setReviewedAt(LocalDateTime.now());
        field.setReviewNotes(notes);

        ExtractedDocumentField savedField = extractedFieldRepository.save(field);

        // Record audit trail
        AuditEvent audit = new AuditEvent(
            document.getApplication() != null ? document.getApplication().getApplicationRef() : "DOC-" + documentId,
            "FIELD_CORRECTION",
            reviewerId != null ? reviewerId : "reviewer",
            "Field '" + fieldName + "' corrected from '" + previousValue + "' to '" + correctedValue + "'. Notes: " + notes
        );
        auditEventRepository.save(audit);

        return savedField;
    }

    /**
     * Overrides the document category and triggers schema re-evaluation.
     */
    @Transactional
    public Document overrideCategory(Long documentId, DocumentCategory newCategory, String reviewerId, String notes) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        DocumentCategory oldCategory = document.getDocumentCategory();
        document.setDocumentCategory(newCategory);
        document.setRequiresManualReview(false);
        documentRepository.save(document);

        // Re-process extraction with new schema
        Document reprocessed = orchestrator.reprocessDocument(documentId);

        // Record audit trail
        AuditEvent audit = new AuditEvent(
            document.getApplication() != null ? document.getApplication().getApplicationRef() : "DOC-" + documentId,
            "CATEGORY_OVERRIDE",
            reviewerId != null ? reviewerId : "reviewer",
            "Category changed from " + oldCategory + " to " + newCategory + ". Notes: " + notes
        );
        auditEventRepository.save(audit);

        return reprocessed;
    }

    /**
     * Submits human review verdict (APPROVED, NEEDS_RESUBMISSION, REJECTED).
     */
    @Transactional
    public DocumentReview submitReviewDecision(Long documentId, ReviewStatus decision, String reviewerId, String notes) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        DocumentReview review = new DocumentReview(
            document,
            reviewerId != null ? reviewerId : "reviewer",
            decision,
            notes
        );
        review = documentReviewRepository.save(review);

        // Record audit trail
        AuditEvent audit = new AuditEvent(
            document.getApplication() != null ? document.getApplication().getApplicationRef() : "DOC-" + documentId,
            "DOCUMENT_REVIEW_DECISION",
            reviewerId != null ? reviewerId : "reviewer",
            "Review decision: " + decision + ". Notes: " + notes
        );
        auditEventRepository.save(audit);

        return review;
    }
}
