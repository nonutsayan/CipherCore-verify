package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.model.enums.DocumentCompletenessStatus;
import com.ciphercore.verify.model.enums.DocumentProcessingStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nullable to allow standalone document uploads & testing as well as application attachments
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = true)
    private Application application;

    @Column(name = "application_id", insertable = false, updatable = false)
    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String documentType = "SUPPORTING_DOCUMENT"; // Legacy string type (FIRE_NOC, LAND_TITLE, etc.)

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private DocumentCategory documentCategory = DocumentCategory.SUPPORTING_DOCUMENT;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private DocumentCategory predictedCategory;

    private Double classificationConfidence;

    @Column(length = 1024)
    private String classificationEvidence;

    @Column(length = 64)
    private String classificationMethod = "RULE_BASED";

    private boolean requiresManualReview = false;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 512)
    private String storedPath;

    @Column(length = 64)
    private String mimeType;

    private long fileSizeBytes;
    private int pageCount = 1;

    @Column(nullable = false, length = 64)
    private String sha256Checksum;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private DocumentProcessingStatus processingStatus = DocumentProcessingStatus.UPLOADED;

    @Column(length = 32)
    private String extractionStatus = "PENDING"; // Legacy compatibility: PENDING, EXTRACTED, FAILED

    @Column(length = 64)
    private String ocrEngine = "PDFBOX_TEXT_EXTRACTOR";

    @Column(length = 32)
    private String ocrQuality = "HIGH"; // HIGH, MEDIUM, LOW, UNREADABLE

    /**
     * Section 20 Boundary:
     * Never claim that a document is authentic merely because OCR or completeness succeeds.
     * Default must be AUTHENTICITY_NOT_VERIFIED until verified against an authorized registry.
     */
    @Column(length = 64)
    private String authenticityStatus = "AUTHENTICITY_NOT_VERIFIED";

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private DocumentCompletenessStatus completenessStatus = DocumentCompletenessStatus.PASS;

    @Column(length = 512)
    private String completenessSummary;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(length = 64)
    private String uploadedBy = "system";

    private LocalDateTime uploadTimestamp = LocalDateTime.now();
    private LocalDateTime processedTimestamp;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentPage> pages = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExtractedDocumentField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentValidationResult> validationResults = new ArrayList<>();

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DocumentReview> reviews = new ArrayList<>();

    public Document() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { 
        this.application = application; 
        if (application != null) {
            this.applicationId = application.getId();
        }
    }

    public Long getApplicationId() {
        return application != null ? application.getId() : applicationId;
    }
    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { 
        this.documentType = documentType; 
        if (this.documentCategory == null || this.documentCategory == DocumentCategory.SUPPORTING_DOCUMENT) {
            this.documentCategory = DocumentCategory.fromString(documentType);
        }
    }

    public DocumentCategory getDocumentCategory() { return documentCategory; }
    public void setDocumentCategory(DocumentCategory documentCategory) { 
        this.documentCategory = documentCategory;
        if (documentCategory != null) {
            this.documentType = documentCategory.name();
        }
    }

    public DocumentCategory getPredictedCategory() { return predictedCategory; }
    public void setPredictedCategory(DocumentCategory predictedCategory) { this.predictedCategory = predictedCategory; }

    public Double getClassificationConfidence() { return classificationConfidence; }
    public void setClassificationConfidence(Double classificationConfidence) { this.classificationConfidence = classificationConfidence; }

    public String getClassificationEvidence() { return classificationEvidence; }
    public void setClassificationEvidence(String classificationEvidence) { this.classificationEvidence = classificationEvidence; }

    public String getClassificationMethod() { return classificationMethod; }
    public void setClassificationMethod(String classificationMethod) { this.classificationMethod = classificationMethod; }

    public boolean isRequiresManualReview() { return requiresManualReview; }
    public void setRequiresManualReview(boolean requiresManualReview) { this.requiresManualReview = requiresManualReview; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredPath() { return storedPath; }
    public void setStoredPath(String storedPath) { this.storedPath = storedPath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public String getSha256Checksum() { return sha256Checksum; }
    public void setSha256Checksum(String sha256Checksum) { this.sha256Checksum = sha256Checksum; }

    public DocumentProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(DocumentProcessingStatus processingStatus) { 
        this.processingStatus = processingStatus; 
        if (processingStatus == DocumentProcessingStatus.COMPLETED) {
            this.extractionStatus = "EXTRACTED";
        } else if (processingStatus == DocumentProcessingStatus.FAILED) {
            this.extractionStatus = "FAILED";
        }
    }

    public String getExtractionStatus() { return extractionStatus; }
    public void setExtractionStatus(String extractionStatus) { this.extractionStatus = extractionStatus; }

    public String getOcrEngine() { return ocrEngine; }
    public void setOcrEngine(String ocrEngine) { this.ocrEngine = ocrEngine; }

    public String getOcrQuality() { return ocrQuality; }
    public void setOcrQuality(String ocrQuality) { this.ocrQuality = ocrQuality; }

    public String getAuthenticityStatus() { return authenticityStatus; }
    public void setAuthenticityStatus(String authenticityStatus) { this.authenticityStatus = authenticityStatus; }

    public DocumentCompletenessStatus getCompletenessStatus() { return completenessStatus; }
    public void setCompletenessStatus(DocumentCompletenessStatus completenessStatus) { this.completenessStatus = completenessStatus; }

    public String getCompletenessSummary() { return completenessSummary; }
    public void setCompletenessSummary(String completenessSummary) { this.completenessSummary = completenessSummary; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadTimestamp() { return uploadTimestamp; }
    public void setUploadTimestamp(LocalDateTime uploadTimestamp) { this.uploadTimestamp = uploadTimestamp; }

    public LocalDateTime getProcessedTimestamp() { return processedTimestamp; }
    public void setProcessedTimestamp(LocalDateTime processedTimestamp) { this.processedTimestamp = processedTimestamp; }

    public List<DocumentPage> getPages() { return pages; }
    public void setPages(List<DocumentPage> pages) { this.pages = pages; }

    public List<ExtractedDocumentField> getFields() { return fields; }
    public void setFields(List<ExtractedDocumentField> fields) { this.fields = fields; }

    public List<DocumentValidationResult> getValidationResults() { return validationResults; }
    public void setValidationResults(List<DocumentValidationResult> validationResults) { this.validationResults = validationResults; }

    public List<DocumentReview> getReviews() { return reviews; }
    public void setReviews(List<DocumentReview> reviews) { this.reviews = reviews; }
}
