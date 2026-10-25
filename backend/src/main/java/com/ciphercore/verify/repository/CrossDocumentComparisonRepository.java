package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.CrossDocumentComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrossDocumentComparisonRepository extends JpaRepository<CrossDocumentComparison, Long> {
    List<CrossDocumentComparison> findByApplicationId(Long applicationId);
    List<CrossDocumentComparison> findBySourceDocumentIdOrTargetDocumentId(Long sourceDocId, Long targetDocId);
}
