package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Section 14: Date & Temporal Validity Anomaly Engine
 * Enforces chronological validity rules, detects expired approval periods,
 * and identifies inverted grant-to-expiry chronologies.
 */
@Service
public class DateConsistencyService {

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public List<AnomalyRecord> verifyTemporalConsistency(Document document, String issueDateStr, String expiryDateStr, Long runId) {
        List<AnomalyRecord> anomalies = new ArrayList<>();
        LocalDate issueDate = parseSafeDate(issueDateStr);
        LocalDate expiryDate = parseSafeDate(expiryDateStr);

        // 1. Issue Date after Expiry Date
        if (issueDate != null && expiryDate != null) {
            if (issueDate.isAfter(expiryDate)) {
                String desc = String.format("Inverted chronological dates: Stated issue date (%s) is later than stated expiry date (%s).",
                    issueDate, expiryDate);

                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.DATE_INCONSISTENCY,
                    AnomalySeverity.CRITICAL,
                    "Chronological Date Inversion (Issue Date > Expiry Date)",
                    desc,
                    "A certificate or regulatory order cannot expire prior to its official issuance date.",
                    "RULE_TEMPORAL_INVERTED_DATES",
                    0.99,
                    "Flag document for immediate scrutiny; request certified clarification or original issuing office register record."
                );
                anomalyRecordRepository.save(anomaly);
                anomalies.add(anomaly);

                EvidenceRecord ev = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "DATE_CONFLICT",
                    1,
                    "issueDate / expiryDate",
                    "Issue: " + issueDateStr + " | Expiry: " + expiryDateStr,
                    "Valid Chronological Window (Issue <= Expiry)",
                    "Typographical errors in day/month order (MM/DD vs DD/MM) can occasionally cause apparent inversions."
                );
                evidenceRecordRepository.save(ev);
            }
        }

        // 2. Future Issue Date Check
        if (issueDate != null) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            if (issueDate.isAfter(tomorrow)) {
                String desc = String.format("Post-dated certificate: Stated execution date (%s) is set in the future.", issueDate);

                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.DATE_INCONSISTENCY,
                    AnomalySeverity.HIGH,
                    "Post-Dated Certificate Execution Date",
                    desc,
                    "Official orders cannot be legally effective prior to their authentic execution and stamping date.",
                    "RULE_TEMPORAL_FUTURE_ISSUE_DATE",
                    0.95,
                    "Verify with submitting officer whether date reflects an advance order or an erroneous calendar entry."
                );
                anomalyRecordRepository.save(anomaly);
                anomalies.add(anomaly);

                EvidenceRecord ev = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "DATE_CONFLICT",
                    1,
                    "issueDate",
                    "Extracted: " + issueDateStr + " (Current Date: " + LocalDate.now() + ")",
                    "Historical or Current Calendar Date",
                    "Timezone differences do not account for multi-day future post-dating."
                );
                evidenceRecordRepository.save(ev);
            }
        }

        // 3. Expired Document Check (for documents requiring current validity)
        if (expiryDate != null) {
            if (expiryDate.isBefore(LocalDate.now())) {
                String desc = String.format("Expired regulatory instrument: Validity expired on %s (lapsed %d days ago).",
                    expiryDate, Math.abs(LocalDate.now().toEpochDay() - expiryDate.toEpochDay()));

                AnomalyRecord anomaly = new AnomalyRecord(
                    runId,
                    document.getId(),
                    document.getApplicationId(),
                    AnomalyType.VALIDITY_CONFLICT,
                    AnomalySeverity.HIGH,
                    "Lapsed Validity Period",
                    desc,
                    "Compliance guidelines require valid, unexpired statutory certificates (such as Fire NOC, Occupancy, or Land lease) at time of EoA grant.",
                    "RULE_TEMPORAL_LAPSED_EXPIRY",
                    0.96,
                    "Require applicant to produce valid Renewal Endorsement or Extension Order before final approval."
                );
                anomalyRecordRepository.save(anomaly);
                anomalies.add(anomaly);

                EvidenceRecord ev = new EvidenceRecord(
                    anomaly,
                    document.getId(),
                    "DATE_CONFLICT",
                    1,
                    "expiryDate",
                    "Extracted Expiry: " + expiryDateStr,
                    "Active Validity on Assessment Date (" + LocalDate.now() + ")",
                    "An expired document is an administrative deficiency, not proof of forgery. Check whether an extension was submitted."
                );
                evidenceRecordRepository.save(ev);
            }
        }

        return anomalies;
    }

    private LocalDate parseSafeDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
