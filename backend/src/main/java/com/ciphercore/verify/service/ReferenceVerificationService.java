package com.ciphercore.verify.service;

import com.ciphercore.verify.adapter.ReferenceDataAdapter;
import com.ciphercore.verify.adapter.ReferenceRecordResult;
import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.EvidenceRecord;
import com.ciphercore.verify.model.VerificationRecord;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import com.ciphercore.verify.model.enums.VerificationStatus;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.EvidenceRecordRepository;
import com.ciphercore.verify.repository.VerificationRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Section 16: Authorized Institutional Reference Record Verification Service
 * Compares extracted fields against verified master institutional databases,
 * government registries, and accredited approval archives.
 */
@Service
public class ReferenceVerificationService {

    @Autowired(required = false)
    private ReferenceDataAdapter referenceDataAdapter;

    @Autowired
    private VerificationRecordRepository verificationRecordRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    public VerificationRecord verifyAgainstRegistry(Document document, String instituteCode, String instituteName, Long runId) {
        if (referenceDataAdapter == null) {
            VerificationRecord unconfigured = new VerificationRecord(
                document.getId(),
                "INSTITUTIONAL_REGISTRY",
                "NONE",
                VerificationStatus.SOURCE_NOT_CONFIGURED,
                "No institutional reference registry adapter currently configured for live queries.",
                "External integration offline or not provided. Verification marked unverified per Section 16."
            );
            return verificationRecordRepository.save(unconfigured);
        }

        ReferenceRecordResult refResult = referenceDataAdapter.lookupInstitutionalRecord(instituteCode, instituteName);
        VerificationStatus status;
        String outcome = refResult.getMatchType() != null ? refResult.getMatchType() : "NOT_FOUND";

        if ("EXACT_MATCH".equalsIgnoreCase(outcome)) {
            status = VerificationStatus.VERIFIED_MATCH;
        } else if ("PARTIAL_MATCH".equalsIgnoreCase(outcome)) {
            status = VerificationStatus.PARTIAL_MATCH;
        } else if ("MISMATCH".equalsIgnoreCase(outcome) || "CONFLICT".equalsIgnoreCase(outcome)) {
            status = VerificationStatus.RECORD_CONFLICT;
        } else if ("UNAVAILABLE".equalsIgnoreCase(outcome)) {
            status = VerificationStatus.SOURCE_UNAVAILABLE;
        } else {
            status = VerificationStatus.RECORD_NOT_FOUND;
        }

        String evidenceStr = String.format("Source: %s | Outcome: %s | Reference ID: %s | Details: %s",
            refResult.getSourceIdentifier(), status, refResult.getRecordReference(), refResult.getEvidenceReason());

        VerificationRecord record = new VerificationRecord(
            document.getId(),
            "INSTITUTIONAL_REGISTRY",
            refResult.getSourceIdentifier(),
            status,
            evidenceStr,
            refResult.getLimitations()
        );
        record = verificationRecordRepository.save(record);

        // If RECORD_CONFLICT, generate Anomaly
        if (status == VerificationStatus.RECORD_CONFLICT) {
            AnomalyRecord anomaly = new AnomalyRecord(
                runId,
                document.getId(),
                document.getApplicationId(),
                AnomalyType.REFERENCE_RECORD_CONFLICT,
                AnomalySeverity.CRITICAL,
                "Official Institutional Registry Discrepancy",
                refResult.getEvidenceReason(),
                "Direct contradiction with authorized regulatory master records is a high-priority integrity indicator.",
                "RULE_REGISTRY_RECORD_CONFLICT",
                0.97,
                "Request physical inspection of regional directorate files and escalate to Member Secretary."
            );
            anomalyRecordRepository.save(anomaly);

            EvidenceRecord ev = new EvidenceRecord(
                anomaly,
                document.getId(),
                "REFERENCE_RECORD_CONFLICT",
                1,
                "registryFields",
                evidenceStr,
                "Authorized Central Master Record (" + refResult.getRecordReference() + ")",
                refResult.getLimitations()
            );
            evidenceRecordRepository.save(ev);
        }

        return record;
    }
}
