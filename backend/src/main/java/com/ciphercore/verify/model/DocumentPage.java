package com.ciphercore.verify.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

/**
 * Section 17B: DocumentPage Entity
 * Tracks per-page extracted text, extraction method (embedded vs OCR), and quality metrics.
 */
@Entity
@Table(name = "document_pages")
public class DocumentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false)
    private int pageNumber;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(length = 64)
    private String extractionMethod; // EMBEDDED_TEXT, TESSERACT_OCR, FALLBACK_RULE

    @Column(length = 64)
    private String ocrQualityInfo; // HIGH, MEDIUM, LOW, UNREADABLE

    @Column(length = 255)
    private String warnings;

    private int characterCount;

    public DocumentPage() {}

    public DocumentPage(Document document, int pageNumber, String extractedText, String extractionMethod, String ocrQualityInfo) {
        this.document = document;
        this.pageNumber = pageNumber;
        this.extractedText = extractedText != null ? extractedText : "";
        this.characterCount = this.extractedText.length();
        this.extractionMethod = extractionMethod;
        this.ocrQualityInfo = ocrQualityInfo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { 
        this.extractedText = extractedText;
        this.characterCount = extractedText != null ? extractedText.length() : 0;
    }

    public String getExtractionMethod() { return extractionMethod; }
    public void setExtractionMethod(String extractionMethod) { this.extractionMethod = extractionMethod; }

    public String getOcrQualityInfo() { return ocrQualityInfo; }
    public void setOcrQualityInfo(String ocrQualityInfo) { this.ocrQualityInfo = ocrQualityInfo; }

    public String getWarnings() { return warnings; }
    public void setWarnings(String warnings) { this.warnings = warnings; }

    public int getCharacterCount() { return characterCount; }
    public void setCharacterCount(int characterCount) { this.characterCount = characterCount; }
}
