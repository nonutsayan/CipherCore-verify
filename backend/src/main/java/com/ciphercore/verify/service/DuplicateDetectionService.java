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

import java.util.ArrayList;
import java.util.List;

/**
 * Section 6: Exact Duplicate Detection Service
 * Evaluates byte-for-byte identity via SHA-256 and records context-aware duplicate matches.
 */
@Service
public class DuplicateDetectionService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DuplicateMatchRepository duplicateMatchRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<DuplicateMatch> detectExactDuplicates(Document document, Long runId) {
        List<DuplicateMatch> matches = new ArrayList<>();
        if (document.getSha256Checksum() == null || document.getSha256Checksum().isBlank()) {
            return matches;
        }

        List<Document> allDocs = documentRepository.findAll();
        for (Document other : allDocs) {
            if (other.getId().equals(document.getId())) continue;

            if (document.getSha256Checksum().equalsIgnoreCase(other.getSha256Checksum())) {
                String matchType;
                AnomalySeverity severity;
                String recAction;

                if (document.getApplicationId() != null && document.getApplicationId().equals(other.getApplicationId())) {
                    matchType = "EXACT_DUPLICATE_SAME_APPLICATION";
                    severity = AnomalySeverity.LOW;
                    recAction = "Confirm whether applicant accidentally submitted redundant copies of the same certificate.";
                } else if (other.getApplicationId() != null) {
                    matchType = "EXACT_DUPLICATE_DIFFERENT_APPLICATION";
                    severity = AnomalySeverity.HIGH;
                    recAction = "Investigate cross-application reuse: Verify whether this institutional record belongs to both applications or was improperly recycled.";
                } else {
                    matchType = "EXACT_DUPLICATE_PREVIOUS_SUBMISSION";
                    severity = AnomalySeverity.MEDIUM;
                    recAction = "Check whether document is an unchanged re-submission from historical evaluation records.";
                }

                String details = String.format("Exact byte match detected. Target Document #%d ('%s') has identical SHA-256 (%s).",
                    other.getId(), other.getOriginalFilename(), document.getSha256Checksum().substring(0, 16) + "...");

                DuplicateMatch match = new DuplicateMatch(
                    document.getId(),
                    other.getId(),
                    document.getApplicationId(),
                    other.getApplicationId(),
                    matchType,
                    "SHA256_HASH",
                    1.0,
                    details,
                    recAction
                );
                duplicateMatchRepository.save(match);
                matches.add(match);

                // Create Evidence-Linked AnomalyRecord
                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.EXACT_DUPLICATE,
                    severity,
                    "Exact Duplicate File Detected: " + matchType,
                    details,
                    "A byte-for-byte duplicate may indicate redundant upload, stale submission, or unauthorized reuse across applications.",
                    "RULE_INTEGRITY_EXACT_DUPLICATE",
                    1.0,
                    recAction
                );
                anomalyRecordRepository.save(anomaly);

                EvidenceRecord evidence = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "FILE_HASH_MATCH",
                    1,
                    "sha256Checksum",
                    "SHA-256: " + document.getSha256Checksum(),
                    "Document #" + other.getId() + " (" + other.getOriginalFilename() + ")",
                    "Cryptographic hash proves byte-level identity. It does not establish fraud; validity depends on authorized reuse policy."
                );
                evidenceRecordRepository.save(evidence);
            }
        }

        return matches;
    }
}
