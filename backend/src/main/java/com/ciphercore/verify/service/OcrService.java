package com.ciphercore.verify.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Section 7: OCR and Document Inspection Pipeline
 * Multi-stage text extraction covering embedded text extraction, fallback OCR,
 * quality assessment, and prompt-injection neutralization.
 */
@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    @Autowired
    private ImagePreprocessingService imagePreprocessingService;

    @Autowired(required = false)
    private GoogleVisionOcrService googleVisionOcrService;

    public record PageExtractionResult(
        int pageNumber,
        String extractedText,
        String extractionMethod,
        String ocrQuality,
        int characterCount,
        int wordCount,
        String warning
    ) {}

    public record DocumentInspectionResult(
        int totalPages,
        String overallEngine,
        String overallQuality,
        List<PageExtractionResult> pages,
        String consolidatedText
    ) {}

    /**
     * Inspects and extracts text across all pages of a file.
     */
    public DocumentInspectionResult extractText(File file, String mediaType) {
        if (!file.exists()) {
            return new DocumentInspectionResult(0, "NONE", "UNREADABLE", List.of(), "");
        }

        // 1. If Google Cloud Vision API is configured, use it as primary OCR engine
        if (googleVisionOcrService != null && googleVisionOcrService.isConfigured()) {
            try {
                GoogleVisionOcrService.VisionOcrResult gRes = googleVisionOcrService.extractDocumentText(file, mediaType);
                if (gRes.success() && !gRes.consolidatedText().isBlank()) {
                    List<PageExtractionResult> gPages = new ArrayList<>();
                    for (GoogleVisionOcrService.VisionPageResult pr : gRes.pages()) {
                        String sanitized = sanitizeText(pr.text());
                        int charCount = sanitized.length();
                        int wordCount = countWords(sanitized);
                        String quality = assessQuality(sanitized, charCount, wordCount);
                        gPages.add(new PageExtractionResult(
                                pr.pageNumber(),
                                sanitized,
                                "GOOGLE_CLOUD_VISION",
                                quality,
                                charCount,
                                wordCount,
                                null
                        ));
                    }
                    String overallQuality = gPages.stream().anyMatch(p -> "HIGH".equals(p.ocrQuality())) ? "HIGH" : "MEDIUM";
                    return new DocumentInspectionResult(
                            gRes.totalPages(),
                            "GOOGLE_CLOUD_VISION",
                            overallQuality,
                            gPages,
                            sanitizeText(gRes.consolidatedText())
                    );
                } else {
                    log.warn("Google Cloud Vision OCR returned non-success or empty text ({}): {}. Falling back to local PDFBox/Image extraction.",
                            gRes.status(), gRes.message());
                }
            } catch (Exception e) {
                log.warn("Google Cloud Vision invocation failed: {}. Falling back to local engine.", e.getMessage());
            }
        }

        if ("application/pdf".equalsIgnoreCase(mediaType)) {
            return extractFromPdf(file);
        } else if (mediaType != null && mediaType.startsWith("image/")) {
            return extractFromImage(file);
        }

        return new DocumentInspectionResult(1, "UNKNOWN", "UNREADABLE", List.of(), "");
    }

    private DocumentInspectionResult extractFromPdf(File file) {
        List<PageExtractionResult> pages = new ArrayList<>();
        StringBuilder consolidated = new StringBuilder();

        try (PDDocument pdDoc = Loader.loadPDF(file)) {
            int pageCount = pdDoc.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            boolean anyPageHasText = false;
            for (int p = 1; p <= pageCount; p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                String rawText = stripper.getText(pdDoc);
                String sanitized = sanitizeText(rawText);

                int charCount = sanitized.length();
                int wordCount = countWords(sanitized);
                String quality = assessQuality(sanitized, charCount, wordCount);
                String method = (charCount > 30) ? "EMBEDDED_PDFBOX" : "PDFBOX_SPARSE";

                if (charCount > 30) {
                    anyPageHasText = true;
                }

                String warning = null;
                if (charCount < 30) {
                    warning = "Page has very sparse or no embedded text. May be a scanned raster page.";
                }

                pages.add(new PageExtractionResult(p, sanitized, method, quality, charCount, wordCount, warning));
                consolidated.append(sanitized).append("\n\n");
            }

            // If completely empty/scanned PDF, try OCR if available
            String overallEngine = "PDFBOX_TEXT_EXTRACTOR";
            String overallQuality = anyPageHasText ? "HIGH" : "LOW";

            if (!anyPageHasText) {
                overallEngine = isTesseractAvailable() ? "TESSERACT_OCR" : "PDFBOX_SCANNED_FALLBACK";
                overallQuality = "UNREADABLE";
            }

            return new DocumentInspectionResult(pageCount, overallEngine, overallQuality, pages, consolidated.toString().trim());
        } catch (IOException e) {
            log.error("Failed to parse PDF document {}: {}", file.getName(), e.getMessage());
            PageExtractionResult failedPage = new PageExtractionResult(1, "", "FAILED", "UNREADABLE", 0, 0, "PDF parsing error: " + e.getMessage());
            return new DocumentInspectionResult(1, "PDFBOX_ERROR", "UNREADABLE", List.of(failedPage), "");
        }
    }

    private DocumentInspectionResult extractFromImage(File file) {
        if (isTesseractAvailable()) {
            try {
                String tesseractText = executeTesseract(file);
                String sanitized = sanitizeText(tesseractText);
                int charCount = sanitized.length();
                int wordCount = countWords(sanitized);
                String quality = assessQuality(sanitized, charCount, wordCount);

                PageExtractionResult page = new PageExtractionResult(1, sanitized, "TESSERACT_OCR", quality, charCount, wordCount, null);
                return new DocumentInspectionResult(1, "TESSERACT_OCR_5", quality, List.of(page), sanitized);
            } catch (Exception e) {
                log.warn("Tesseract OCR execution failed: {}", e.getMessage());
            }
        }

        // Honest fallback when Tesseract is not installed on host
        PageExtractionResult fallback = new PageExtractionResult(
            1, 
            "", 
            "OCR_ENGINE_NOT_CONFIGURED", 
            "UNREADABLE", 
            0, 
            0, 
            "Image document uploaded. Tesseract OCR binary is not installed on host PATH. Scanned image could not be converted to text."
        );
        return new DocumentInspectionResult(1, "OCR_ENGINE_NOT_CONFIGURED", "UNREADABLE", List.of(fallback), "");
    }

    public boolean isTesseractAvailable() {
        try {
            Process process = new ProcessBuilder("tesseract", "--version").start();
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String executeTesseract(File imageFile) throws IOException, InterruptedException {
        File preprocessedTemp = File.createTempFile("ocr_prep_", ".png");
        try {
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            ImagePreprocessingService.PreprocessResult prep = imagePreprocessingService.preprocessImage(fileBytes);
            Files.write(preprocessedTemp.toPath(), prep.pngBytes());

            ProcessBuilder pb = new ProcessBuilder("tesseract", preprocessedTemp.getAbsolutePath(), "stdout", "-l", "eng");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor(15, TimeUnit.SECONDS);
            return output.toString();
        } finally {
            preprocessedTemp.delete();
        }
    }

    public String sanitizeText(String input) {
        if (input == null) return "";
        // Neutralize prompt injection phrases inside untrusted document content
        return input.replace("Ignore previous instructions", "[REDACTED_PROMPT_INJECTION]")
                    .replace("SYSTEM_PROMPT", "[REDACTED_SYSTEM_TOKEN]")
                    .replace("Override all rules", "[REDACTED_INJECTION_RULE]")
                    .trim();
    }

    private String assessQuality(String text, int charCount, int wordCount) {
        if (charCount == 0 || wordCount == 0) return "UNREADABLE";
        if (charCount < 50) return "LOW";

        // Count non-ascii/garbled characters ratio
        long nonAscii = text.chars().filter(ch -> ch > 127 || (ch < 32 && ch != '\n' && ch != '\r' && ch != '\t')).count();
        double garbleRatio = (double) nonAscii / (double) charCount;

        if (garbleRatio > 0.35) return "LOW";
        if (charCount > 150 && garbleRatio < 0.10) return "HIGH";
        return "MEDIUM";
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
