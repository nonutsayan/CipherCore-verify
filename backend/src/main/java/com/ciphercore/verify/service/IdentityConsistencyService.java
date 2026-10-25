package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.ApplicationRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Section 13: Identity and Identifier Consistency Service
 * Evaluates exact, normalized, and approximate matches for institutional names,
 * codes, and legal entity structures between documents and master application records.
 */
@Service
public class IdentityConsistencyService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<AnomalyRecord> verifyIdentityConsistency(Document document, String extractedInstituteName, String extractedCode, Long runId) {
        List<AnomalyRecord> anomalies = new ArrayList<>();
        if (document.getApplicationId() == null) return anomalies;

        Optional<Application> appOpt = applicationRepository.findById(document.getApplicationId());
        if (appOpt.isEmpty()) return anomalies;

        Application app = appOpt.get();
        String expectedName = app.getInstitutionName();

        // 1. Institution Name Check
        if (extractedInstituteName != null && !extractedInstituteName.isBlank() && expectedName != null) {
            String normExtracted = normalizeName(extractedInstituteName);
            String normExpected = normalizeName(expectedName);

            if (!normExtracted.equals(normExpected)) {
                double similarity = computeJaroWinkler(normExtracted, normExpected);
                // If distinct institutions or significant divergence
                if (similarity < 0.82) {
                    String desc = String.format("Institutional title mismatch: Document names '%s', while application records specify '%s' (similarity: %.1f%%).",
                        extractedInstituteName, expectedName, similarity * 100);

                    AnomalyRecord anomaly = new AnomalyRecord(
                        runId,
                        document.getId(),
                        document.getApplicationId(),
                        AnomalyType.INSTITUTION_NAME_MISMATCH,
                        AnomalySeverity.CRITICAL,
                        "Institutional Entity Name Discrepancy",
                        desc,
                        "Title divergence may indicate submission of another institution's certificates, unapproved name change, or unauthorized proxy filing.",
                        "RULE_IDENTITY_INSTITUTION_NAME_CONFLICT",
                        0.93,
                        "Request legal certificate of name change or government Gazette notification approving the revised nomenclature."
                    );
                    anomalyRecordRepository.save(anomaly);
                    anomalies.add(anomaly);

                    EvidenceRecord ev = new EvidenceRecord(
                        anomaly,
                        document.getId(),
                        "FIELD_MISMATCH",
                        1,
                        "institutionName",
                        "Extracted: " + extractedInstituteName,
                        "Application Master Record: " + expectedName,
                        "Minor abbreviation differences (e.g. 'Tech' vs 'Technology') are normalized. Significant divergence requires legal confirmation."
                    );
                    evidenceRecordRepository.save(ev);
                }
            }
        }

        // 2. Institution Code Check
        String appCodeOrRef = app.getInstituteCode() != null ? app.getInstituteCode() : app.getApplicationRef();
        if (extractedCode != null && !extractedCode.isBlank() && appCodeOrRef != null) {
            String normExtractedCode = extractedCode.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
            String normAppNumber = appCodeOrRef.replaceAll("[^A-Za-z0-9]", "").toUpperCase();

            if (normExtractedCode.length() >= 5 && normAppNumber.length() >= 5 && !normAppNumber.contains(normExtractedCode) && !normExtractedCode.contains(normAppNumber)) {
                String desc = String.format("Identifier mismatch: Extracted code '%s' does not match or align with Application Identifier '%s'.",
                    extractedCode, appCodeOrRef);

                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.IDENTIFIER_MISMATCH,
                    AnomalySeverity.MEDIUM,
                    "Application Identifier / Registration Discrepancy",
                    desc,
                    "Institutional registration numbers must link reliably to the current filing dossier.",
                    "RULE_IDENTITY_CODE_MISMATCH",
                    0.89,
                    "Check whether code refers to a permanent Institute ID (PID), society registration, or prior academic year order."
                );
                anomalyRecordRepository.save(anomaly);
                anomalies.add(anomaly);

                EvidenceRecord ev = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "FIELD_MISMATCH",
                    1,
                    "institutionCode",
                    "Extracted: " + extractedCode,
                    "Application Identifier: " + appCodeOrRef,
                    "Different identifier schemes (Society Reg vs AICTE PID vs University Affiliation Code) must be distinguished by reviewer."
                );
                evidenceRecordRepository.save(ev);
            }
        }

        return anomalies;
    }

    private String normalizeName(String name) {
        if (name == null) return "";
        return name.toUpperCase()
            .replaceAll("[^A-Z0-9\\s]", " ")
            .replaceAll("\\bPVT\\b", "")
            .replaceAll("\\bLTD\\b", "")
            .replaceAll("\\bLIMITED\\b", "")
            .replaceAll("\\bINSTITUTE OF TECHNOLOGY\\b", "IOT")
            .replaceAll("\\bCOLLEGE OF ENGINEERING\\b", "COE")
            .replaceAll("\\bTRUST\\b", "")
            .replaceAll("\\bSOCIETY\\b", "")
            .replaceAll("\\s+", " ")
            .trim();
    }

    private double computeJaroWinkler(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) return 0.0;

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;

        double t = 0;
        int point = 0;
        for (int i = 0; i < len1; i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[point]) point++;
            if (s1.charAt(i) != s2.charAt(point)) t++;
            point++;
        }
        t /= 2;

        double jaro = ((double) matches / len1 + (double) matches / len2 + (double) (matches - t) / matches) / 3.0;
        int prefix = 0;
        for (int i = 0; i < Math.min(4, Math.min(len1, len2)); i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return jaro + 0.1 * prefix * (1.0 - jaro);
    }
}
