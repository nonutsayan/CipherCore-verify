package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationEvidenceRepository extends JpaRepository<VerificationEvidence, Long> {
    List<VerificationEvidence> findByVerificationResultId(Long verificationResultId);
    List<VerificationEvidence> findByDocumentId(Long documentId);
}
