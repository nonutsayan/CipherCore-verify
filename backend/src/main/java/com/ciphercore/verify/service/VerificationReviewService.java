package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.model.VerificationResult;
import com.ciphercore.verify.model.VerificationReview;
import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import com.ciphercore.verify.repository.AuditEventRepository;
import com.ciphercore.verify.repository.VerificationResultRepository;
import com.ciphercore.verify.repository.VerificationReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Section 17 & 21: Verification Review Service.
 * Manages scrutiny officer disposition, audit event logging, and chain of custody.
 */
@Service
public class VerificationReviewService {

    private final VerificationReviewRepository reviewRepository;
    private final VerificationResultRepository resultRepository;
    private final AuditEventRepository auditEventRepository;

    public VerificationReviewService(
            VerificationReviewRepository reviewRepository,
            VerificationResultRepository resultRepository,
            AuditEventRepository auditEventRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.resultRepository = resultRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public VerificationReview submitReview(Long resultId, String reviewerId, String decision, String notes) {
        Optional<VerificationResult> resOpt = resultRepository.findById(resultId);
        Long docId = resOpt.map(VerificationResult::getDocumentId).orElse(0L);

        VerificationReview review = new VerificationReview();
        review.setVerificationResultId(resultId);
        review.setDocumentId(docId);
        review.setReviewerId(reviewerId != null && !reviewerId.isBlank() ? reviewerId : "officer-scrutiny");
        review.setReviewDecision(decision);
        review.setNotes(notes);
        review.setReviewedAt(LocalDateTime.now());
        VerificationReview saved = reviewRepository.save(review);

        // Update verification result status if appropriate
        resOpt.ifPresent(vr -> {
            if ("CONFIRM_LEGITIMATE".equalsIgnoreCase(decision)) {
                vr.setOverallStatus(AuthorityVerificationStatus.VERIFIED);
                vr.setSummary("Manually verified and confirmed legitimate by officer " + reviewerId + ": " + notes);
            } else if ("CONFIRM_DISCREPANCY".equalsIgnoreCase(decision)) {
                vr.setOverallStatus(AuthorityVerificationStatus.MISMATCH);
                vr.setSummary("Officer " + reviewerId + " confirmed official discrepancy: " + notes);
            } else if ("REQUEST_FURTHER_EVIDENCE".equalsIgnoreCase(decision) || "ESCALATE_TO_ISSUER".equalsIgnoreCase(decision)) {
                vr.setOverallStatus(AuthorityVerificationStatus.MANUAL_REVIEW);
                vr.setSummary("Escalated for administrative inquiry by officer " + reviewerId + ": " + notes);
            }
            resultRepository.save(vr);
        });

        // Audit Trail
        AuditEvent event = new AuditEvent(
                review.getReviewerId(),
                "SCRUTINY_OFFICER",
                "AUTHORITY_VERIFICATION_REVIEWED",
                "VerificationResult",
                String.valueOf(resultId),
                "Decision: " + decision + " | Notes: " + notes,
                "127.0.0.1"
        );
        auditEventRepository.save(event);

        return saved;
    }

    public List<VerificationReview> getReviewsForDocument(Long documentId) {
        return reviewRepository.findByDocumentId(documentId);
    }

    public List<VerificationReview> getReviewsForResult(Long verificationResultId) {
        return reviewRepository.findByVerificationResultId(verificationResultId);
    }
}
