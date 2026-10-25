package com.ciphercore.verify.service;

import com.ciphercore.verify.model.enums.DocumentCategory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 8: Automatic Document Classification Engine
 * Evaluates document text, headers, issuing authorities, and structural indicators
 * across 15 standard institutional categories.
 */
@Service
public class DocumentClassificationService {

    public record ClassificationOutcome(
        DocumentCategory primaryCategory,
        List<DocumentCategory> alternativeCategories,
        double confidenceScore,
        List<String> evidencePhrases,
        String classificationMethod,
        boolean manualConfirmationRecommended
    ) {}

    private record CategoryIndicator(
        DocumentCategory category,
        List<String> strongKeywords,
        List<String> contextualKeywords,
        double weight
    ) {}

    private final List<CategoryIndicator> indicators = List.of(
        new CategoryIndicator(
            DocumentCategory.NO_OBJECTION_CERTIFICATE,
            List.of("no objection certificate", "fire noc", "fire safety certificate", "noc for", "grant of noc"),
            List.of("fire services", "standing fire advisory", "fire prevention", "safety measures", "no objection"),
            1.5
        ),
        new CategoryIndicator(
            DocumentCategory.INSTITUTION_REGISTRATION_CERTIFICATE,
            List.of("certificate of registration", "registration certificate", "registered under societies", "trust registration", "companies act"),
            List.of("charity commissioner", "society registration act", "registrar of societies", "registration no", "trust deed"),
            1.4
        ),
        new CategoryIndicator(
            DocumentCategory.ACCREDITATION_CERTIFICATE,
            List.of("national assessment and accreditation council", "naac", "national board of accreditation", "nba", "certificate of accreditation"),
            List.of("accredited with grade", "cgpa", "valid for a period of", "peer team", "accreditation status"),
            1.5
        ),
        new CategoryIndicator(
            DocumentCategory.APPROVAL_LETTER,
            List.of("letter of approval", "extension of approval", "approval order", "eoa", "approval process handbook"),
            List.of("all india council for technical education", "aicte", "intake capacity", "approved intake", "academic session"),
            1.4
        ),
        new CategoryIndicator(
            DocumentCategory.AFFIDAVIT,
            List.of("affidavit", "solemnly affirm", "sworn before me", "notary public", "non-judicial stamp"),
            List.of("deponent", "verified at", "oath", "advocate", "notarial stamp", "depose and say"),
            1.5
        ),
        new CategoryIndicator(
            DocumentCategory.AFFILIATION_CERTIFICATE,
            List.of("certificate of affiliation", "grant of affiliation", "affiliation order", "provisional affiliation"),
            List.of("university", "registrar", "academic council", "affiliated college", "courses affiliated"),
            1.3
        ),
        new CategoryIndicator(
            DocumentCategory.RENEWAL_CERTIFICATE,
            List.of("renewal certificate", "certificate of renewal", "renewal of registration", "renewal order"),
            List.of("renewed up to", "period of renewal", "original certificate no", "renewal fee"),
            1.2
        ),
        new CategoryIndicator(
            DocumentCategory.VALIDITY_EXTENSION_LETTER,
            List.of("extension letter", "extension of validity", "validity extended", "period of extension"),
            List.of("extended up to", "valid till", "further extension", "superseded"),
            1.2
        ),
        new CategoryIndicator(
            DocumentCategory.GOVERNMENT_ORDER,
            List.of("government of", "government order", "gazette notification", "directorate of technical education"),
            List.of("higher and technical education", "secretariat", "by order of the governor", "sanction"),
            1.1
        ),
        new CategoryIndicator(
            DocumentCategory.RECOGNITION_CERTIFICATE,
            List.of("certificate of recognition", "order of recognition", "recognized by"),
            List.of("statutory council", "recognized institution", "section 2(f)", "section 12(b)"),
            1.2
        ),
        new CategoryIndicator(
            DocumentCategory.AUTHORIZATION_LETTER,
            List.of("letter of authorization", "authorization letter", "authorized signatory"),
            List.of("hereby authorize", "on behalf of the trust", "power of attorney"),
            1.1
        ),
        new CategoryIndicator(
            DocumentCategory.INSTITUTIONAL_DECLARATION,
            List.of("declaration by the institution", "institutional declaration", "self declaration"),
            List.of("we hereby declare", "undertaking", "principal/director"),
            1.1
        )
    );

    public ClassificationOutcome classifyDocument(String text) {
        if (text == null || text.isBlank() || text.length() < 20) {
            return new ClassificationOutcome(
                DocumentCategory.UNKNOWN, 
                List.of(), 
                0.0, 
                List.of("Insufficient readable text to determine classification"), 
                "HEURISTIC_RULE_BASED", 
                true
            );
        }

        String lowerText = text.toLowerCase();
        Map<DocumentCategory, Double> scores = new HashMap<>();
        Map<DocumentCategory, List<String>> evidenceMap = new HashMap<>();

        for (CategoryIndicator ind : indicators) {
            double categoryScore = 0.0;
            List<String> evidence = new ArrayList<>();

            // Check strong indicators (higher weight)
            for (String kw : ind.strongKeywords()) {
                if (lowerText.contains(kw)) {
                    categoryScore += (2.5 * ind.weight());
                    evidence.add("Matched strong signal: \"" + kw + "\"");
                }
            }

            // Check contextual indicators
            for (String kw : ind.contextualKeywords()) {
                if (lowerText.contains(kw)) {
                    categoryScore += (1.0 * ind.weight());
                    evidence.add("Matched context signal: \"" + kw + "\"");
                }
            }

            if (categoryScore > 0) {
                scores.put(ind.category(), categoryScore);
                evidenceMap.put(ind.category(), evidence);
            }
        }

        if (scores.isEmpty()) {
            return new ClassificationOutcome(
                DocumentCategory.SUPPORTING_DOCUMENT, 
                List.of(DocumentCategory.OTHER), 
                0.35, 
                List.of("No category-specific institutional keywords matched. Defaulted to supporting attachment."), 
                "HEURISTIC_RULE_BASED", 
                true
            );
        }

        // Sort by score descending
        List<Map.Entry<DocumentCategory, Double>> sorted = scores.entrySet().stream()
            .sorted(Map.Entry.<DocumentCategory, Double>comparingByValue().reversed())
            .toList();

        Map.Entry<DocumentCategory, Double> top = sorted.get(0);
        DocumentCategory primary = top.getKey();
        double rawTopScore = top.getValue();

        // Calibrate confidence score between 0.40 and 0.98
        double confidence = Math.min(0.98, 0.45 + (rawTopScore / 10.0));

        List<DocumentCategory> alternatives = new ArrayList<>();
        boolean ambiguous = false;

        if (sorted.size() > 1) {
            Map.Entry<DocumentCategory, Double> second = sorted.get(1);
            alternatives.add(second.getKey());
            // If top two are very close, mark ambiguous
            if ((rawTopScore - second.getValue()) < 1.5) {
                ambiguous = true;
                confidence = Math.max(0.40, confidence - 0.20);
            }
        }

        boolean manualReviewRecommended = ambiguous || (confidence < 0.70);

        return new ClassificationOutcome(
            primary, 
            alternatives, 
            Math.round(confidence * 100.0) / 100.0, 
            evidenceMap.getOrDefault(primary, List.of()), 
            "HEURISTIC_RULE_BASED", 
            manualReviewRecommended
        );
    }
}
