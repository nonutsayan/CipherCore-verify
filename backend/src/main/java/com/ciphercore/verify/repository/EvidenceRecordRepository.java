package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecord, Long> {
    List<EvidenceRecord> findByAnomalyId(Long anomalyId);
    List<EvidenceRecord> findByDocumentId(Long documentId);
    List<EvidenceRecord> findByDocumentIdAndEvidenceType(Long documentId, String evidenceType);
}
