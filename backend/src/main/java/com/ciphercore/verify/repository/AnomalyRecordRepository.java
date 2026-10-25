package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.enums.AnomalyReviewStatus;
import com.ciphercore.verify.model.enums.AnomalySeverity;
import com.ciphercore.verify.model.enums.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
    List<AnomalyRecord> findByDocumentId(Long documentId);
    List<AnomalyRecord> findByApplicationId(Long applicationId);
    List<AnomalyRecord> findByDocumentIdAndReviewStatus(Long documentId, AnomalyReviewStatus status);
    List<AnomalyRecord> findBySeverity(AnomalySeverity severity);
    List<AnomalyRecord> findByAnomalyType(AnomalyType type);
    long countByDocumentIdAndSeverity(Long documentId, AnomalySeverity severity);
    long countByApplicationIdAndReviewStatus(Long applicationId, AnomalyReviewStatus status);
}
