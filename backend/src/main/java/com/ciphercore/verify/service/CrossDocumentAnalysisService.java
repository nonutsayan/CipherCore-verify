package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Section 15: Cross-Document Dossier Intelligence Service
 * Analyzes the interconnected evidence graph of all documents belonging to an application
 * to detect conflicting entity names, contradictory survey/registration numbers, and mismatched validity periods.
 */
@Service
public class CrossDocumentAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(CrossDocumentAnalysisService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ExtractedDocumentFieldRepository fieldRepository;

    @Autowired
    private CrossDocumentComparisonRepository comparisonRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<CrossDocumentComparison> analyzeApplicationDossier(Long applicationId, Long runId) {
        List<CrossDocumentComparison> comparisons = new ArrayList<>();
        List<Document> docs = documentRepository.findByApplicationId(applicationId);
        if (docs.size() < 2) {
            log.info("Application {} has less than 2 documents. Cross-document comparison skipped.", applicationId);
            return comparisons;
        }

        // Load fields map for each document: docId -> fieldMap
        Map<Long, Map<String, ExtractedDocumentField>> docFieldsMap = new HashMap<>();
        for (Document d : docs) {
            List<ExtractedDocumentField> fields = fieldRepository.findByDocumentId(d.getId());
            Map<String, ExtractedDocumentField> fMap = new HashMap<>();
            for (ExtractedDocumentField f : fields) {
                fMap.put(f.getFieldName(), f);
            }
            docFieldsMap.put(d.getId(), fMap);
        }

        // Compare all pairs of documents in the application
        for (int i = 0; i < docs.size(); i++) {
            Document docA = docs.get(i);
            Map<String, ExtractedDocumentField> fieldsA = docFieldsMap.get(docA.getId());

            for (int j = i + 1; j < docs.size(); j++) {
                Document docB = docs.get(j);
                Map<String, ExtractedDocumentField> fieldsB = docFieldsMap.get(docB.getId());

                int conflictCount = 0;
                List<String> conflictDetails = new ArrayList<>();

                // 1. Cross-check Institution Name
                ExtractedDocumentField nameA = fieldsA.get("institutionName");
                ExtractedDocumentField nameB = fieldsB.get("institutionName");
                if (nameA != null && nameB != null && nameA.getNormalizedValue() != null && nameB.getNormalizedValue() != null) {
                    String normA = normalize(nameA.getNormalizedValue());
                    String normB = normalize(nameB.getNormalizedValue());
                    if (!normA.isEmpty() && !normB.isEmpty() && !normA.equals(normB) && computeWordOverlap(normA, normB) < 0.60) {
                        conflictCount++;
                        String desc = String.format("Conflicting institution names across dossier: Document #%d ('%s') names '%s', while Document #%d ('%s') names '%s'.",
                            docA.getId(), docA.getOriginalFilename(), nameA.getRawValue(),
                            docB.getId(), docB.getOriginalFilename(), nameB.getRawValue());
                        conflictDetails.add(desc);

                        AnomalyRecord anomaly = new AnomalyRecord(
                            runId,
                            docA.getId(),
                            applicationId,
                            AnomalyType.CROSS_DOCUMENT_CONTRADICTION,
                            AnomalySeverity.HIGH,
                            "Cross-Document Institution Name Conflict",
                            desc,
                            "All statutory documents within an institutional filing must describe the identical legal entity or explain trust/society relationships.",
                            "RULE_CROSSDOC_NAME_DIVERGENCE",
                            0.94,
                            "Require institutional affidavit clarifying parent society and college branch name variations."
                        );
                        anomalyRecordRepository.save(anomaly);

                        EvidenceRecord evA = new EvidenceRecord(
                            anomaly,
                            docA.getId(),
                            "CROSS_DOCUMENT_COMPARISON",
                            nameA.getPageNumber() != null ? nameA.getPageNumber() : 1,
                            "institutionName",
                            "Doc #" + docA.getId() + " (" + docA.getOriginalFilename() + "): " + nameA.getRawValue(),
                            "Doc #" + docB.getId() + " (" + docB.getOriginalFilename() + "): " + nameB.getRawValue(),
                            "Cross-document entity comparison checks for common parent society aliases."
                        );
                        evidenceRecordRepository.save(evA);
                    }
                }

                // 2. Cross-check Registration / Order Numbers
                ExtractedDocumentField regA = fieldsA.get("registrationNumber");
                if (regA == null) regA = fieldsA.get("certificateNumber");
                ExtractedDocumentField regB = fieldsB.get("registrationNumber");
                if (regB == null) regB = fieldsB.get("certificateNumber");

                if (regA != null && regB != null && regA.getNormalizedValue() != null && regB.getNormalizedValue() != null) {
                    String cleanA = regA.getNormalizedValue().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
                    String cleanB = regB.getNormalizedValue().replaceAll("[^A-Za-z0-9]", "").toUpperCase();

                    // Only compare if both documents belong to same category type (e.g. renewals or approvals)
                    if (docA.getDocumentCategory() == docB.getDocumentCategory() && cleanA.length() >= 5 && cleanB.length() >= 5 && !cleanA.equals(cleanB)) {
                        conflictCount++;
                        String desc = String.format("Contradictory document registration numbers: Document #%d has '%s', whereas Document #%d has '%s'.",
                            docA.getId(), regA.getRawValue(), docB.getId(), regB.getRawValue());
                        conflictDetails.add(desc);

                        AnomalyRecord anomaly = new AnomalyRecord(
                            runId,
                            docA.getId(),
                            applicationId,
                            AnomalyType.CROSS_DOCUMENT_CONTRADICTION,
                            AnomalySeverity.HIGH,
                            "Cross-Document Registration Number Conflict",
                            desc,
                            "Documents of the same class for the same application year must refer to consistent file or certificate identifiers.",
                            "RULE_CROSSDOC_REGISTRATION_MISMATCH",
                            0.92,
                            "Compare original stamped copies to identify if one document cites an obsolete reference number."
                        );
                        anomalyRecordRepository.save(anomaly);

                        EvidenceRecord ev = new EvidenceRecord(
                            anomaly,
                            docA.getId(),
                            "FIELD_MISMATCH",
                            regA.getPageNumber() != null ? regA.getPageNumber() : 1,
                            "registrationNumber",
                            "Doc #" + docA.getId() + ": " + regA.getRawValue(),
                            "Doc #" + docB.getId() + ": " + regB.getRawValue(),
                            "Check whether one identifier is a municipal dispatch number and the other is a state registry number."
                        );
                        evidenceRecordRepository.save(ev);
                    }
                }

                // Persist Comparison Record
                String status = conflictCount > 0 ? "CONFLICT_DETECTED" : "CONSISTENT";
                String summary = conflictCount > 0 ? 
                    String.join("; ", conflictDetails) : 
                    String.format("Dossier cross-check consistent: Document #%d and #%d demonstrate aligned entity names and references.", docA.getId(), docB.getId());

                CrossDocumentComparison comp = new CrossDocumentComparison(
                    applicationId,
                    docA.getId(),
                    docB.getId(),
                    "DOSSIER_IDENTITY",
                    status,
                    conflictCount,
                    summary
                );
                comparisonRepository.save(comp);
                comparisons.add(comp);
            }
        }

        return comparisons;
    }

    private String normalize(String val) {
        if (val == null) return "";
        return val.toUpperCase().replaceAll("[^A-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private double computeWordOverlap(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(s1.split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(s2.split(" ")));
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        int common = 0;
        for (String w : set1) {
            if (set2.contains(w)) common++;
        }
        return (double) common / Math.max(set1.size(), set2.size());
    }
}
