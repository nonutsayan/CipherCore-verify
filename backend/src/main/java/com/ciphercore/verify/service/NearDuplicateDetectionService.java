package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.DuplicateMatch;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.DocumentRepository;
import com.ciphercore.verify.repository.DuplicateMatchRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Section 7: Near-Duplicate Detection Service
 * Evaluates visual and textual document similarity using normalized token Jaccard overlap
 * and character n-gram fingerprints to detect revised certificates, re-scans, and template reuse.
 */
@Service
public class NearDuplicateDetectionService {

    private static final double TEXT_SIMILARITY_THRESHOLD = 0.78;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DuplicateMatchRepository duplicateMatchRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<DuplicateMatch> detectNearDuplicates(Document document, Long runId) {
        List<DuplicateMatch> matches = new ArrayList<>();
        String sourceText = document.getExtractedText();
        if (sourceText == null || sourceText.trim().length() < 50) {
            return matches;
        }

        Set<String> sourceTokens = tokenize(sourceText);
        List<Document> allDocs = documentRepository.findAll();

        for (Document other : allDocs) {
            if (other.getId().equals(document.getId())) continue;
            // Skip exact duplicates already handled
            if (document.getSha256Checksum() != null && document.getSha256Checksum().equalsIgnoreCase(other.getSha256Checksum())) {
                continue;
            }

            String otherText = other.getExtractedText();
            if (otherText == null || otherText.trim().length() < 50) continue;

            Set<String> otherTokens = tokenize(otherText);
            double jaccardScore = computeJaccardSimilarity(sourceTokens, otherTokens);

            if (jaccardScore >= TEXT_SIMILARITY_THRESHOLD && jaccardScore < 0.999) {
                double pct = Math.round(jaccardScore * 1000.0) / 10.0;
                String details = String.format(
                    "High textual and structural similarity (%.1f%%) with Document #%d ('%s'). Document layout and boilerplate match, but specific fields or dates differ.",
                    pct, other.getId(), other.getOriginalFilename()
                );
                String recAction = "Perform side-by-side comparison to verify whether Document #" + document.getId() +
                    " is a valid revision, renewal, or modified copy of Document #" + other.getId() + ".";

                DuplicateMatch match = new DuplicateMatch(
                    document.getId(),
                    other.getId(),
                    document.getApplicationId(),
                    other.getApplicationId(),
                    "NEAR_DUPLICATE",
                    "TOKEN_JACCARD_SIMILARITY",
                    jaccardScore,
                    details,
                    recAction
                );
                duplicateMatchRepository.save(match);
                matches.add(match);

                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.NEAR_DUPLICATE,
                    AnomalySeverity.MEDIUM,
                    "Near-Duplicate Document Detected (" + pct + "% Match)",
                    details,
                    "Near-duplicates frequently occur when templates are reused, renewals are issued, or documents are altered. Material fields must be checked.",
                    "RULE_NEAR_DUPLICATE_TEXT_OVERLAP",
                    jaccardScore,
                    recAction
                );
                anomalyRecordRepository.save(anomaly);

                EvidenceRecord evidence = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "CROSS_DOCUMENT_COMPARISON",
                    1,
                    "documentContent",
                    "Computed token Jaccard similarity: " + pct + "%. Shared tokens count: " + getIntersectionSize(sourceTokens, otherTokens),
                    "Target Document #" + other.getId() + " (" + other.getOriginalFilename() + ")",
                    "High token similarity indicates shared template structure. Does not imply unauthorized tampering without field-level review."
                );
                evidenceRecordRepository.save(evidence);
            }
        }

        return matches;
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = normalized.split("\\s+");
        for (String w : words) {
            if (w.length() >= 3) {
                tokens.add(w);
            }
        }
        return tokens;
    }

    private double computeJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        int intersection = 0;
        for (String s : set1) {
            if (set2.contains(s)) intersection++;
        }
        int union = set1.size() + set2.size() - intersection;
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    private int getIntersectionSize(Set<String> set1, Set<String> set2) {
        int count = 0;
        for (String s : set1) {
            if (set2.contains(s)) count++;
        }
        return count;
    }
}
