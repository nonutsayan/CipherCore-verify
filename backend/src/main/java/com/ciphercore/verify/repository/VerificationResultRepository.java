package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationResult;
import com.ciphercore.verify.model.enums.AuthorityVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationResultRepository extends JpaRepository<VerificationResult, Long> {
    Optional<VerificationResult> findTopByDocumentIdOrderByGeneratedAtDesc(Long documentId);
    List<VerificationResult> findByDocumentIdOrderByGeneratedAtDesc(Long documentId);
    List<VerificationResult> findByApplicationIdOrderByGeneratedAtDesc(Long applicationId);
    List<VerificationResult> findByOverallStatus(AuthorityVerificationStatus status);
}
