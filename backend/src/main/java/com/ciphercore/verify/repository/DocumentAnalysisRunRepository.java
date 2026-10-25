package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DocumentAnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentAnalysisRunRepository extends JpaRepository<DocumentAnalysisRun, Long> {
    List<DocumentAnalysisRun> findByDocumentIdOrderByStartedAtDesc(Long documentId);
    Optional<DocumentAnalysisRun> findFirstByDocumentIdOrderByStartedAtDesc(Long documentId);
    List<DocumentAnalysisRun> findByApplicationId(Long applicationId);
}
