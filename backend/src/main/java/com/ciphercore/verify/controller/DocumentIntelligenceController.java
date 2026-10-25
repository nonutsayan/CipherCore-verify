package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Section 18: Document Intelligence REST API Controller
 * Provides upload ingestion, text retrieval, field correction, classification override,
 * review decisions, and structured Section 15 intelligence reports.
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
@Tag(name = "Document Intelligence", description = "AI-Powered OCR, Document Understanding, and Compliance Verification API")
public class DocumentIntelligenceController {

    @Autowired
    private DocumentProcessingOrchestrator orchestrator;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentPageRepository pageRepository;

    @Autowired
    private ExtractedDocumentFieldRepository fieldRepository;

    @Autowired
    private DocumentReviewService reviewService;

    @Autowired
    private DocumentReportService reportService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and process an institutional document")
    public ResponseEntity<?> uploadDocument(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "applicationId", required = false) Long applicationId,
        @RequestParam(value = "uploadedBy", required = false, defaultValue = "officer_sc401") String uploadedBy
    ) {
        try {
            Document processed = orchestrator.processUploadedDocument(file, applicationId, uploadedBy);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("documentId", processed.getId());
            response.put("originalFilename", processed.getOriginalFilename());
            response.put("processingStatus", processed.getProcessingStatus());
            response.put("predictedCategory", processed.getPredictedCategory());
            response.put("classificationConfidence", processed.getClassificationConfidence());
            response.put("ocrEngine", processed.getOcrEngine());
            response.put("ocrQuality", processed.getOcrQuality());
            response.put("completenessStatus", processed.getCompletenessStatus());
            response.put("authenticityStatus", processed.getAuthenticityStatus());
            response.put("requiresManualReview", processed.isRequiresManualReview());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("errorCode", "VALIDATION_FAILED");
            err.put("message", e.getMessage());
            err.put("retryable", false);
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("errorCode", "PROCESSING_ERROR");
            err.put("message", "Internal document processing error: " + e.getMessage());
            err.put("retryable", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping
    @Operation(summary = "List documents with optional filtering")
    public ResponseEntity<List<Document>> listDocuments(
        @RequestParam(value = "category", required = false) String categoryStr,
        @RequestParam(value = "status", required = false) String statusStr,
        @RequestParam(value = "applicationId", required = false) Long applicationId
    ) {
        if (applicationId != null) {
            return ResponseEntity.ok(documentRepository.findByApplicationId(applicationId));
        }
        if (categoryStr != null && !categoryStr.isBlank()) {
            DocumentCategory cat = DocumentCategory.fromString(categoryStr);
            return ResponseEntity.ok(documentRepository.findByDocumentCategory(cat));
        }
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                DocumentProcessingStatus st = DocumentProcessingStatus.valueOf(statusStr.toUpperCase());
                return ResponseEntity.ok(documentRepository.findByProcessingStatus(st));
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(documentRepository.findAllByOrderByUploadTimestampDesc());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve document metadata, pages, extracted fields, and validations")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        return documentRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get document processing status and progress")
    public ResponseEntity<?> getDocumentStatus(@PathVariable Long id) {
        return documentRepository.findById(id).map(doc -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("documentId", doc.getId());
            map.put("status", doc.getProcessingStatus());
            map.put("ocrEngine", doc.getOcrEngine());
            map.put("ocrQuality", doc.getOcrQuality());
            map.put("pageCount", doc.getPageCount());
            map.put("completenessStatus", doc.getCompletenessStatus());
            map.put("requiresManualReview", doc.isRequiresManualReview());
            return ResponseEntity.ok(map);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/text")
    @Operation(summary = "Retrieve extracted text across all pages")
    public ResponseEntity<?> getDocumentText(@PathVariable Long id) {
        List<DocumentPage> pages = pageRepository.findByDocumentIdOrderByPageNumberAsc(id);
        if (pages.isEmpty()) {
            return documentRepository.findById(id)
                .map(doc -> ResponseEntity.ok(Map.of("fullText", doc.getExtractedText() != null ? doc.getExtractedText() : "")))
                .orElseGet(() -> ResponseEntity.notFound().build());
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("documentId", id);
        resp.put("totalPages", pages.size());
        resp.put("pages", pages);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "Safe document inline preview stream")
    public ResponseEntity<Resource> previewDocument(@PathVariable Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = docOpt.get();
        File file = new File(doc.getStoredPath());
        if (!file.exists()) return ResponseEntity.notFound().build();

        MediaType mediaType = MediaType.APPLICATION_PDF;
        if ("image/png".equalsIgnoreCase(doc.getMimeType())) {
            mediaType = MediaType.IMAGE_PNG;
        } else if ("image/jpeg".equalsIgnoreCase(doc.getMimeType())) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFilename() + "\"")
            .body(new FileSystemResource(file));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download original document")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = docOpt.get();
        File file = new File(doc.getStoredPath());
        if (!file.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFilename() + "\"")
            .body(new FileSystemResource(file));
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Retrieve structured Section 15 Document Intelligence JSON Report")
    public ResponseEntity<?> getStructuredReport(@PathVariable Long id) {
        try {
            Map<String, Object> report = reportService.generateStructuredReport(id);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reprocess")
    @Operation(summary = "Reprocess an existing document")
    public ResponseEntity<?> reprocessDocument(@PathVariable Long id) {
        try {
            Document reprocessed = orchestrator.reprocessDocument(id);
            return ResponseEntity.ok(reprocessed);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/fields/{fieldName}")
    @Operation(summary = "Human reviewer field correction")
    public ResponseEntity<?> correctField(
        @PathVariable Long id,
        @PathVariable String fieldName,
        @RequestBody Map<String, String> payload
    ) {
        String value = payload.get("value");
        String reviewerId = payload.getOrDefault("reviewerId", "reviewer_sc401");
        String notes = payload.getOrDefault("notes", "Human correction via reviewer console");

        if (value == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correction value is required"));
        }

        ExtractedDocumentField updated = reviewService.correctField(id, fieldName, value, reviewerId, notes);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/classification")
    @Operation(summary = "Human reviewer document category override")
    public ResponseEntity<?> overrideCategory(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload
    ) {
        String categoryStr = payload.get("category");
        String reviewerId = payload.getOrDefault("reviewerId", "reviewer_sc401");
        String notes = payload.getOrDefault("notes", "Category manually corrected by reviewer");

        if (categoryStr == null || categoryStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
        }

        DocumentCategory newCat = DocumentCategory.fromString(categoryStr);
        Document updated = reviewService.overrideCategory(id, newCat, reviewerId, notes);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/review")
    @Operation(summary = "Submit human reviewer decision (APPROVED, NEEDS_RESUBMISSION, REJECTED)")
    public ResponseEntity<?> submitReview(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload
    ) {
        String decisionStr = payload.getOrDefault("decision", "APPROVED");
        String reviewerId = payload.getOrDefault("reviewerId", "reviewer_sc401");
        String notes = payload.getOrDefault("notes", "Document review verified and recorded");

        ReviewStatus decision;
        try {
            decision = ReviewStatus.valueOf(decisionStr.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid decision. Must be APPROVED, NEEDS_RESUBMISSION, or REJECTED"));
        }

        DocumentReview review = reviewService.submitReviewDecision(id, decision, reviewerId, notes);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document record")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = docOpt.get();
        // Securely remove disk file
        try {
            File f = new File(doc.getStoredPath());
            if (f.exists()) f.delete();
        } catch (Exception ignored) {}

        documentRepository.delete(doc);
        return ResponseEntity.ok(Map.of("message", "Document deleted successfully", "documentId", id));
    }
}
