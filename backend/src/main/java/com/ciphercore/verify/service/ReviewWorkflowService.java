package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.AnomalyReview;
import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.model.enums.AnomalyReviewStatus;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.AnomalyReviewRepository;
import com.ciphercore.verify.repository.AuditEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Section 26: Human Review and Disposition Workflow Service
 * Allows authorized officers to adjudicate anomalies, record audit-logged explanations,
 * and mark legitimate variations without falsely declaring documents forged.
 */
@Service
public class ReviewWorkflowService {

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private AnomalyReviewRepository anomalyReviewRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Transactional
    public AnomalyRecord recordReviewDecision(Long anomalyId, String reviewerId, String decision, String notes) {
        AnomalyRecord anomaly = anomalyRecordRepository.findById(anomalyId)
            .orElseThrow(() -> new IllegalArgumentException("Anomaly not found with ID: " + anomalyId));

        if (notes == null || notes.trim().length() < 5) {
            throw new IllegalArgumentException("A mandatory review justification (minimum 5 characters) is required for official disposition.");
        }

        AnomalyReviewStatus targetStatus;
        switch (decision.toUpperCase()) {
            case "CONFIRM_LEGITIMATE" -> targetStatus = AnomalyReviewStatus.RESOLVED_LEGITIMATE_DIFFERENCE;
            case "CONFIRM_DISCREPANCY" -> targetStatus = AnomalyReviewStatus.CONFIRMED_DISCREPANCY;
            case "MARK_FALSE_POSITIVE" -> targetStatus = AnomalyReviewStatus.RESOLVED_FALSE_POSITIVE;
            case "REQUEST_CLARIFICATION" -> targetStatus = AnomalyReviewStatus.NEEDS_CLARIFICATION;
            case "ESCALATE_TO_ISSUER" -> targetStatus = AnomalyReviewStatus.ESCALATED;
            default -> targetStatus = AnomalyReviewStatus.UNDER_REVIEW;
        }

        anomaly.setReviewStatus(targetStatus);
        anomaly = anomalyRecordRepository.save(anomaly);

        AnomalyReview review = new AnomalyReview(anomalyId, reviewerId, decision, notes);
        anomalyReviewRepository.save(review);

        // Immutable Audit Trail Logging
        String details = String.format("Reviewer '%s' adjudicated Anomaly #%d ('%s') as %s. Justification: %s",
            reviewerId, anomalyId, anomaly.getTitle(), targetStatus, notes);

        AuditEvent audit = new AuditEvent(
            reviewerId,
            "REVIEWER",
            "ANOMALY_REVIEW_DECISION",
            "ANOMALY_RECORD",
            String.valueOf(anomalyId),
            details,
            "127.0.0.1"
        );
        auditEventRepository.save(audit);

        return anomaly;
    }
}
