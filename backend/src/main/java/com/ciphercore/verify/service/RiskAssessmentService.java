package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Section 20: Explainable Risk Prioritization Service
 * Implements deterministic, transparent risk scoring that avoids double-counting correlated findings
 * and produces plain-language risk priority summaries without uncalibrated fraud claims.
 */
@Service
public class RiskAssessmentService {

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private DocumentRepository documentRepository;

    public record RiskAssessmentResult(
        int riskScore,
        AnomalySeverity priorityLevel,
        int criticalCount,
        int highCount,
        int mediumCount,
        int lowCount,
        int infoCount,
        String summaryRationale,
        boolean manualReviewRequired
    ) {}

    public RiskAssessmentResult evaluateRisk(Document document) {
        List<AnomalyRecord> anomalies = anomalyRecordRepository.findByDocumentId(document.getId());

        int critical = 0, high = 0, medium = 0, low = 0, info = 0;
        int rawScore = 0;
        Set<String> observedRules = new HashSet<>();

        for (AnomalyRecord a : anomalies) {
            // Prevent double-counting of correlated findings on the same underlying rule
            if (a.getRuleId() != null && observedRules.contains(a.getRuleId())) {
                continue;
            }
            if (a.getRuleId() != null) {
                observedRules.add(a.getRuleId());
            }

            switch (a.getSeverity()) {
                case CRITICAL -> { critical++; rawScore += 35; }
                case HIGH -> { high++; rawScore += 20; }
                case MEDIUM -> { medium++; rawScore += 10; }
                case LOW -> { low++; rawScore += 4; }
                case INFORMATIONAL -> { info++; rawScore += 1; }
            }
        }

        int normalizedScore = Math.min(100, rawScore);
        AnomalySeverity priorityLevel;
        boolean manualReview;

        if (critical > 0 || normalizedScore >= 55) {
            priorityLevel = AnomalySeverity.CRITICAL;
            manualReview = true;
        } else if (high > 0 || normalizedScore >= 30) {
            priorityLevel = AnomalySeverity.HIGH;
            manualReview = true;
        } else if (medium > 0 || normalizedScore >= 12) {
            priorityLevel = AnomalySeverity.MEDIUM;
            manualReview = true;
        } else if (low > 0) {
            priorityLevel = AnomalySeverity.LOW;
            manualReview = false;
        } else {
            priorityLevel = AnomalySeverity.INFORMATIONAL;
            manualReview = false;
        }

        // Build explainable rationale
        String rationale;
        if (anomalies.isEmpty()) {
            rationale = "No structural, temporal, or identity anomalies detected during automated screening. Document status remains AUTHENTICITY_NOT_VERIFIED pending official issuer verification.";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Risk Priority: ").append(priorityLevel).append(" (Score: ").append(normalizedScore).append("/100). ");
            if (critical > 0) sb.append(critical).append(" critical integrity issue(s) detected. ");
            if (high > 0) sb.append(high).append(" material discrepancy finding(s). ");
            if (medium > 0) sb.append(medium).append(" unresolved field variance(s). ");
            sb.append("Independent reviewer examination and issuer verification recommended.");
            rationale = sb.toString();
        }

        // Update Document status
        if (priorityLevel == AnomalySeverity.CRITICAL || priorityLevel == AnomalySeverity.HIGH) {
            document.setAuthenticityStatus("SUSPICIOUS_ANOMALIES_DETECTED");
        }
        document.setRequiresManualReview(manualReview);
        documentRepository.save(document);

        return new RiskAssessmentResult(
            normalizedScore,
            priorityLevel,
            critical,
            high,
            medium,
            low,
            info,
            rationale,
            manualReview
        );
    }
}
