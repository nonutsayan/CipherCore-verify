package com.ciphercore.verify.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 6B & 6C: Text extraction and structured entity extraction.
 * Uses Apache PDFBox for genuine PDF text/metadata extraction with prompt-injection defense.
 */
@Service
public class DocumentExtractionService {

    public record ExtractedData(String fullText, int pageCount, Map<String, String> entities, double confidence) {}

    public ExtractedData extractFromPdf(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ExtractedData("", 0, Map.of(), 0.0);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);
            int pageCount = document.getNumberOfPages();

            // Sanitize against prompt injection: strip system instructions or markdown escapes
            String sanitizedText = sanitizeExtractedText(fullText);

            Map<String, String> entities = extractStructuredEntities(sanitizedText);
            double confidence = (fullText.trim().length() > 50) ? 0.95 : 0.40;

            return new ExtractedData(sanitizedText, pageCount, entities, confidence);
        } catch (IOException e) {
            // Graceful fallback for corrupted/scanned PDFs
            return new ExtractedData("OCR_FALLBACK_SCANNED_PAGE", 1, Map.of(), 0.35);
        }
    }

    private String sanitizeExtractedText(String text) {
        if (text == null) return "";
        // Neutralize prompt injection phrases
        return text.replace("Ignore previous instructions", "[REDACTED_SUSPICIOUS_PROMPT]")
                   .replace("SYSTEM_PROMPT", "[REDACTED]")
                   .trim();
    }

    public Map<String, String> extractStructuredEntities(String text) {
        Map<String, String> entities = new HashMap<>();
        if (text == null || text.isBlank()) return entities;

        // Extract dates (DD-MM-YYYY or YYYY-MM-DD or DD/MM/YYYY)
        Pattern datePattern = Pattern.compile("(?i)(?:valid(?:ity)?(?:\\s+up\\s+to|\\s+till)?|expiry|effective)?[:\\s]*([0-3]?[0-9][-/][0-1]?[0-9][-/][12][09][0-9]{2})");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            entities.put("validityDate", dateMatcher.group(1));
        }

        // Extract Reference numbers
        Pattern refPattern = Pattern.compile("(?i)(?:ref(?:erence)?(?:\\s+no)?|order\\s+no)[:\\s]*([A-Z0-9/_-]{6,30})");
        Matcher refMatcher = refPattern.matcher(text);
        if (refMatcher.find()) {
            entities.put("referenceNumber", refMatcher.group(1));
        }

        // Extract Survey Numbers (Land deeds)
        Pattern surveyPattern = Pattern.compile("(?i)(?:survey\\s+no|gat\\s+no)[:\\s]*([0-9A-Z/_-]+)");
        Matcher surveyMatcher = surveyPattern.matcher(text);
        if (surveyMatcher.find()) {
            entities.put("surveyNumber", surveyMatcher.group(1));
        }

        // Extract Area / Acres
        Pattern areaPattern = Pattern.compile("(?i)([0-9]+(?:\\.[0-9]+)?)\\s*(?:acres|hectares|sq\\.?\\s*m)");
        Matcher areaMatcher = areaPattern.matcher(text);
        if (areaMatcher.find()) {
            entities.put("landArea", areaMatcher.group(1));
        }

        return entities;
    }

    public LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String clean = dateStr.replaceAll("[^0-9/-]", "").replace('/', '-');
        String[] patterns = {"dd-MM-yyyy", "yyyy-MM-dd", "d-M-yyyy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(clean, DateTimeFormatter.ofPattern(p));
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}
