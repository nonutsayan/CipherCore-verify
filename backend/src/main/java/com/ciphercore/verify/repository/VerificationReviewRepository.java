package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationReviewRepository extends JpaRepository<VerificationReview, Long> {
    List<VerificationReview> findByVerificationResultIdOrderByReviewedAtDesc(Long verificationResultId);
    List<VerificationReview> findByDocumentIdOrderByReviewedAtDesc(Long documentId);
    List<VerificationReview> findByVerificationResultId(Long verificationResultId);
    List<VerificationReview> findByDocumentId(Long documentId);
}
