package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {
    List<VerificationRecord> findByDocumentId(Long documentId);
    List<VerificationRecord> findByDocumentIdIn(List<Long> documentIds);
    List<VerificationRecord> findByDocumentIdAndVerificationType(Long documentId, String verificationType);
}
