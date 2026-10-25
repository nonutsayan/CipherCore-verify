package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationJob;
import com.ciphercore.verify.model.enums.VerificationJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationJobRepository extends JpaRepository<VerificationJob, Long> {
    Optional<VerificationJob> findByJobReference(String jobReference);
    List<VerificationJob> findByDocumentIdOrderByCreatedAtDesc(Long documentId);
    List<VerificationJob> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    List<VerificationJob> findByStatus(VerificationJobStatus status);
}
