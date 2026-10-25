package com.ciphercore.verify.service;

import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.model.enums.FieldExtractionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 9 & 10: Document-Specific Field Extraction Engine
 * Schema-driven extraction that extracts category-specific attributes, attaches
 * source evidence snippets, identifies source page numbers, and applies data normalization.
 */
@Service
public class FieldExtractionService {

    @Autowired
    private FieldNormalizationService normalizationService;

    public record ExtractedFieldItem(
        String fieldName,
        String rawValue,
        String normalizedValue,
        FieldExtractionStatus status,
        String sourceEvidence,
        int pageNumber,
        double confidence,
        String extractionMethod
    ) {}

    public Map<String, ExtractedFieldItem> extractFieldsForCategory(DocumentCategory category, List<OcrService.PageExtractionResult> pages) {
        Map<String, ExtractedFieldItem> results = new LinkedHashMap<>();

        // 1. First extract common candidate entities across all pages
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            extractCommonFields(text, pNum, results);
        }

        // 2. Perform Category-Specific Schema Extraction
        switch (category) {
            case INSTITUTION_REGISTRATION_CERTIFICATE -> extractRegistrationSchema(pages, results);
            case ACCREDITATION_CERTIFICATE -> extractAccreditationSchema(pages, results);
            case NO_OBJECTION_CERTIFICATE -> extractNocSchema(pages, results);
            case AFFIDAVIT -> extractAffidavitSchema(pages, results);
            case APPROVAL_LETTER -> extractApprovalSchema(pages, results);
            case AFFILIATION_CERTIFICATE -> extractAffiliationSchema(pages, results);
            default -> extractGenericSchema(pages, results);
        }

        return results;
    }

    private void extractCommonFields(String text, int pageNumber, Map<String, ExtractedFieldItem> out) {
        if (text == null || text.isBlank()) return;

        // Institution Name
        if (!out.containsKey("institutionName")) {
            Pattern instPattern = Pattern.compile("(?i)(?:institution|college|institute|university|academy)\\s*[:\\-]?\\s*([A-Za-z0-9&.,'\\s]{6,80}(?:College|Institute|University|Polytechnic|Academy|Trust|Society))");
            Matcher m = instPattern.matcher(text);
            if (m.find()) {
                String raw = m.group(1).trim();
                String norm = normalizationService.normalizeInstitutionName(raw);
                String snippet = extractSnippet(text, m.start(), m.end());
                out.put("institutionName", new ExtractedFieldItem(
                    "institutionName", raw, norm, FieldExtractionStatus.EXTRACTED, snippet, pageNumber, 0.92, "REGEX_HEURISTIC"
                ));
            }
        }

        // Reference / Order Number
        if (!out.containsKey("referenceNumber")) {
            Pattern refPattern = Pattern.compile("(?i)(?:ref(?:erence)?(?:\\s+no)?|order\\s+no|dispatch\\s+no|file\\s+no)[:\\s.]*([A-Z0-9/_-]{5,35})");
            Matcher m = refPattern.matcher(text);
            if (m.find()) {
                String raw = m.group(1).trim();
                FieldNormalizationService.NormalizedIdentifierResult norm = normalizationService.normalizeIdentifier(raw);
                String snippet = extractSnippet(text, m.start(), m.end());
                out.put("referenceNumber", new ExtractedFieldItem(
                    "referenceNumber", raw, norm.normalizedIdentifier(), norm.status(), snippet, pageNumber, 0.90, "REGEX_HEURISTIC"
                ));
            }
        }

        // Issue Date
        if (!out.containsKey("issueDate")) {
            Pattern datePattern = Pattern.compile("(?i)(?:dated?|date\\s+of\\s+issue|issued\\s+on)[:\\s]*([0-3]?[0-9][-/][0-1]?[0-9][-/][12][09][0-9]{2}|[0-3]?[0-9]\\s+[A-Za-z]{3,9}\\s+[12][09][0-9]{2})");
            Matcher m = datePattern.matcher(text);
            if (m.find()) {
                String raw = m.group(1).trim();
                FieldNormalizationService.NormalizedDateResult norm = normalizationService.normalizeDate(raw);
                String snippet = extractSnippet(text, m.start(), m.end());
                out.put("issueDate", new ExtractedFieldItem(
                    "issueDate", raw, norm.isoDateString(), norm.status(), snippet, pageNumber, 0.95, "REGEX_HEURISTIC"
                ));
            }
        }

        // Expiry / Valid-till Date
        if (!out.containsKey("expiryDate")) {
            Pattern expPattern = Pattern.compile("(?i)(?:valid(?:ity)?(?:\\s+up\\s+to|\\s+till)?|expiry\\s+date|expires\\s+on)[:\\s]*([0-3]?[0-9][-/][0-1]?[0-9][-/][12][09][0-9]{2}|[0-3]?[0-9]\\s+[A-Za-z]{3,9}\\s+[12][09][0-9]{2})");
            Matcher m = expPattern.matcher(text);
            if (m.find()) {
                String raw = m.group(1).trim();
                FieldNormalizationService.NormalizedDateResult norm = normalizationService.normalizeDate(raw);
                String snippet = extractSnippet(text, m.start(), m.end());
                out.put("expiryDate", new ExtractedFieldItem(
                    "expiryDate", raw, norm.isoDateString(), norm.status(), snippet, pageNumber, 0.93, "REGEX_HEURISTIC"
                ));
            }
        }
    }

    private void extractRegistrationSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Registration Number
            if (!out.containsKey("registrationNumber")) {
                Pattern regPattern = Pattern.compile("(?i)(?:registration\\s+no|regn?\\.?\\s*no|society\\s+no)[:\\s.]*([A-Z0-9/_-]{4,30})");
                Matcher m = regPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    FieldNormalizationService.NormalizedIdentifierResult norm = normalizationService.normalizeIdentifier(raw);
                    out.put("registrationNumber", new ExtractedFieldItem(
                        "registrationNumber", raw, norm.normalizedIdentifier(), norm.status(), extractSnippet(text, m.start(), m.end()), pNum, 0.94, "REGEX_HEURISTIC"
                    ));
                }
            }

            // Issuing Authority / Registrar
            if (!out.containsKey("issuingAuthority")) {
                Pattern authPattern = Pattern.compile("(?i)(?:issued\\s+by|office\\s+of\\s+the|authority)[:\\s]*([A-Za-z\\s,]{6,60}(?:Registrar|Commissioner|Directorate|Council|Authority|Collector))");
                Matcher m = authPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("issuingAuthority", new ExtractedFieldItem(
                        "issuingAuthority", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.88, "REGEX_HEURISTIC"
                    ));
                }
            }

            // Registered Address
            if (!out.containsKey("registeredAddress")) {
                Pattern addrPattern = Pattern.compile("(?i)(?:registered\\s+office|situated\\s+at|address)[:\\s]*([A-Za-z0-9\\s,.-]{15,100})");
                Matcher m = addrPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("registeredAddress", new ExtractedFieldItem(
                        "registeredAddress", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.82, "REGEX_HEURISTIC"
                    ));
                }
            }
        }
    }

    private void extractAccreditationSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Accrediting Body (NAAC / NBA)
            if (!out.containsKey("accreditingBody")) {
                if (text.toUpperCase().contains("NAAC") || text.toUpperCase().contains("NATIONAL ASSESSMENT AND ACCREDITATION COUNCIL")) {
                    out.put("accreditingBody", new ExtractedFieldItem(
                        "accreditingBody", "NAAC", "NAAC", FieldExtractionStatus.EXTRACTED, "National Assessment and Accreditation Council", pNum, 0.98, "PATTERN_MATCH"
                    ));
                } else if (text.toUpperCase().contains("NBA") || text.toUpperCase().contains("NATIONAL BOARD OF ACCREDITATION")) {
                    out.put("accreditingBody", new ExtractedFieldItem(
                        "accreditingBody", "NBA", "NBA", FieldExtractionStatus.EXTRACTED, "National Board of Accreditation", pNum, 0.98, "PATTERN_MATCH"
                    ));
                }
            }

            // Accreditation Grade / CGPA
            if (!out.containsKey("accreditationGrade")) {
                Pattern gradePattern = Pattern.compile("(?i)(?:grade|accredited\\s+with\\s+grade)[:\\s]*([A-C]\\+{0,2}|A|B|C)");
                Matcher m = gradePattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("accreditationGrade", new ExtractedFieldItem(
                        "accreditationGrade", raw, raw.toUpperCase(), FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.95, "REGEX_HEURISTIC"
                    ));
                }
            }

            // CGPA Score
            if (!out.containsKey("cgpa")) {
                Pattern cgpaPattern = Pattern.compile("(?i)(?:cgpa|cumulative\\s+grade\\s+point\\s+average)[:\\s]*([0-4]\\.[0-9]{1,2})");
                Matcher m = cgpaPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("cgpa", new ExtractedFieldItem(
                        "cgpa", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.95, "REGEX_HEURISTIC"
                    ));
                }
            }
        }
    }

    private void extractNocSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Issuing Authority (e.g. Fire Services / Municipality)
            if (!out.containsKey("issuingAuthority")) {
                Pattern fireDept = Pattern.compile("(?i)(?:directorate\\s+of\\s+fire\\s+services|fire\\s+brigade|chief\\s+fire\\s+officer|fire\\s+prevention\\s+wing|municipal\\s+corporation)");
                Matcher m = fireDept.matcher(text);
                if (m.find()) {
                    String raw = m.group(0).trim();
                    out.put("issuingAuthority", new ExtractedFieldItem(
                        "issuingAuthority", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.92, "REGEX_HEURISTIC"
                    ));
                }
            }

            // Purpose of NOC
            if (!out.containsKey("purpose")) {
                Pattern purpPattern = Pattern.compile("(?i)(?:subject|purpose)[:\\s]*([A-Za-z0-9\\s,.-]{10,90})");
                Matcher m = purpPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("purpose", new ExtractedFieldItem(
                        "purpose", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.85, "REGEX_HEURISTIC"
                    ));
                }
            }
        }

        // Section 9D: An NOC may not have an explicit expiry date. Mark NOT_STATED rather than missing
        if (!out.containsKey("expiryDate")) {
            out.put("expiryDate", new ExtractedFieldItem(
                "expiryDate", null, null, FieldExtractionStatus.NOT_STATED, "No explicit validity period or expiry date stated on certificate", 1, 0.90, "SCHEMA_DEFAULT"
            ));
        }
    }

    private void extractAffidavitSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Deponent Name
            if (!out.containsKey("deponentName")) {
                Pattern depPattern = Pattern.compile("(?i)(?:i,|deponent)[:\\s]*([A-Za-z\\s.]{4,40})(?:,\\s*aged|s/o|d/o|w/o|solemnly)");
                Matcher m = depPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("deponentName", new ExtractedFieldItem(
                        "deponentName", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.90, "REGEX_HEURISTIC"
                    ));
                }
            }

            // Notary Registration Number
            if (!out.containsKey("notaryRegNumber")) {
                Pattern notPattern = Pattern.compile("(?i)(?:notary\\s+regn?\\.?\\s*no|g\\.o\\.m?s?\\.?\\s*no)[:\\s.]*([A-Z0-9/_-]{4,25})");
                Matcher m = notPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    FieldNormalizationService.NormalizedIdentifierResult norm = normalizationService.normalizeIdentifier(raw);
                    out.put("notaryRegNumber", new ExtractedFieldItem(
                        "notaryRegNumber", raw, norm.normalizedIdentifier(), norm.status(), extractSnippet(text, m.start(), m.end()), pNum, 0.92, "REGEX_HEURISTIC"
                    ));
                }
            }
        }
    }

    private void extractApprovalSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Approval Order / Application ID
            if (!out.containsKey("approvalOrderNumber")) {
                Pattern appPattern = Pattern.compile("(?i)(?:f\\.no\\.?|letter\\s+no|order\\s+no)[:\\s.]*([A-Za-z0-9/_-]{6,35})");
                Matcher m = appPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    FieldNormalizationService.NormalizedIdentifierResult norm = normalizationService.normalizeIdentifier(raw);
                    out.put("approvalOrderNumber", new ExtractedFieldItem(
                        "approvalOrderNumber", raw, norm.normalizedIdentifier(), norm.status(), extractSnippet(text, m.start(), m.end()), pNum, 0.94, "REGEX_HEURISTIC"
                    ));
                }
            }

            // Academic Year
            if (!out.containsKey("academicYear")) {
                Pattern yearPattern = Pattern.compile("(?i)(?:academic\\s+year|session|period)[:\\s]*([12][09][0-9]{2}\\s*[-/]\\s*[0-9]{2,4})");
                Matcher m = yearPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("academicYear", new ExtractedFieldItem(
                        "academicYear", raw, raw.replaceAll("\\s+", ""), FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.95, "REGEX_HEURISTIC"
                    ));
                }
            }
        }
    }

    private void extractAffiliationSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        for (OcrService.PageExtractionResult page : pages) {
            String text = page.extractedText();
            int pNum = page.pageNumber();

            // Affiliating University
            if (!out.containsKey("affiliatingUniversity")) {
                Pattern uniPattern = Pattern.compile("(?i)(?:affiliated\\s+to|university)[:\\s]*([A-Za-z\\s,]{6,70}(?:University|Board))");
                Matcher m = uniPattern.matcher(text);
                if (m.find()) {
                    String raw = m.group(1).trim();
                    out.put("affiliatingUniversity", new ExtractedFieldItem(
                        "affiliatingUniversity", raw, raw, FieldExtractionStatus.EXTRACTED, extractSnippet(text, m.start(), m.end()), pNum, 0.90, "REGEX_HEURISTIC"
                    ));
                }
            }
        }
    }

    private void extractGenericSchema(List<OcrService.PageExtractionResult> pages, Map<String, ExtractedFieldItem> out) {
        // Already populated common fields
    }

    private String extractSnippet(String text, int start, int end) {
        int left = Math.max(0, start - 40);
        int right = Math.min(text.length(), end + 40);
        String snippet = text.substring(left, right).replaceAll("\\s+", " ").trim();
        return "..." + snippet + "...";
    }
}
