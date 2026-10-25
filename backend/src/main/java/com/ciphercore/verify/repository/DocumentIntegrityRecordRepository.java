package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DocumentIntegrityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentIntegrityRecordRepository extends JpaRepository<DocumentIntegrityRecord, Long> {
    Optional<DocumentIntegrityRecord> findByDocumentId(Long documentId);
    Optional<DocumentIntegrityRecord> findBySha256(String sha256);
}
