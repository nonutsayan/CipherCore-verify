package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.DocumentPage;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.DocumentPageRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 12: Text-Level Anomaly Detection Service
 * Analyzes multi-page OCR outputs for conflicting identifiers, terminology shifts,
 * and contradictory approval statements within the same document.
 */
@Service
public class TextAnomalyDetectionService {

    private static final Pattern REG_PATTERN = Pattern.compile("\\b(REG[-/\\d]+|[1-9]-[\\d]{5,10})\\b", Pattern.CASE_INSENSITIVE);

    @Autowired
    private DocumentPageRepository documentPageRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<AnomalyRecord> analyzeTextAnomalies(Document document, Long runId) {
        List<AnomalyRecord> anomalies = new ArrayList<>();
        List<DocumentPage> pages = documentPageRepository.findByDocumentIdOrderByPageNumberAsc(document.getId());
        if (pages.isEmpty()) return anomalies;

        // 1. Multi-Page Identifier Conflict Check
        Map<Integer, Set<String>> pageIdentifiers = new HashMap<>();
        for (DocumentPage page : pages) {
            if (page.getExtractedText() == null) continue;
            Matcher m = REG_PATTERN.matcher(page.getExtractedText());
            Set<String> ids = new HashSet<>();
            while (m.find()) {
                ids.add(m.group(1));
            }
            if (!ids.isEmpty()) {
                pageIdentifiers.put(page.getPageNumber(), ids);
            }
        }

        // Compare Page 1 identifiers with subsequent pages
        if (pageIdentifiers.containsKey(1)) {
            Set<String> p1Ids = pageIdentifiers.get(1);
            for (Map.Entry<Integer, Set<String>> entry : pageIdentifiers.entrySet()) {
                int pageNum = entry.getKey();
                if (pageNum == 1) continue;
                Set<String> pageIds = entry.getValue();

                // If page has identifiers and none overlap with Page 1 primary identifiers
                boolean hasOverlap = false;
                for (String id : pageIds) {
                    if (p1Ids.contains(id)) {
                        hasOverlap = true;
                        break;
                    }
                }

                if (!hasOverlap && !pageIds.isEmpty() && !p1Ids.isEmpty()) {
                    String desc = String.format("Identifier mismatch across pages: Page 1 cites reference '%s', whereas Page %d cites '%s'.",
                        p1Ids.iterator().next(), pageNum, pageIds.iterator().next());

                    AnomalyRecord anomaly = new AnomalyRecord(
                        runId,
                        document.getId(),
                        document.getApplicationId(),
                        AnomalyType.IDENTIFIER_MISMATCH,
                        AnomalySeverity.HIGH,
                        "Multi-Page Identifier Inconsistency (Page 1 vs Page " + pageNum + ")",
                        desc,
                        "Different registration or order numbers on different pages of a single submission strongly indicate mixed or spliced records.",
                        "RULE_TEXT_MULTIPAGE_IDENTIFIER_CONFLICT",
                        0.91,
                        "Verify whether Page " + pageNum + " is a legitimate schedule attachment citing an external reference or an inserted document."
                    );
                    anomalyRecordRepository.save(anomaly);
                    anomalies.add(anomaly);

                    EvidenceRecord ev = new EvidenceRecord(
                        anomaly,
                        document.getId(),
                        "FIELD_MISMATCH",
                        pageNum,
                        "registrationId",
                        desc,
                        "Expected Page 1 Identifier: " + p1Ids.iterator().next(),
                        "Identifiers on later pages may cite previous sanction orders or annexures rather than the main file registration."
                    );
                    evidenceRecordRepository.save(ev);
                    break;
                }
            }
        }

        // 2. Body Text vs Headline Contradiction Check
        String fullText = document.getExtractedText() != null ? document.getExtractedText().toLowerCase() : "";
        if (fullText.contains("approved") && (fullText.contains("not approved") || fullText.contains("rejected") || fullText.contains("null and void"))) {
            String desc = "Document body contains contradictory status terms: Both 'Approved' and 'Rejected/Null and Void' terminology are present.";
            AnomalyRecord anomaly = new AnomalyRecord(
                runId,
                document.getId(),
                document.getApplicationId(),
                AnomalyType.TEXT_INCONSISTENCY,
                AnomalySeverity.HIGH,
                "Contradictory Status Terminology in Document Text",
                desc,
                "Simultaneous appearance of grant and cancellation clauses may indicate conditions not fulfilled or partial text tampering.",
                "RULE_TEXT_CONTRADICTORY_APPROVAL_TERMS",
                0.86,
                "Review full text conditions and verify operative paragraph with the issuing authority."
            );
            anomalyRecordRepository.save(anomaly);
            anomalies.add(anomaly);

            EvidenceRecord ev = new EvidenceRecord(
                anomaly,
                document.getId(),
                "TEXT_SNIPPET",
                1,
                "statusTerms",
                desc,
                "Unambiguous Grant/Status Declaration",
                "Some legal affidavits quote prior rejection letters as part of historical background."
            );
            evidenceRecordRepository.save(ev);
        }

        return anomalies;
    }
}
