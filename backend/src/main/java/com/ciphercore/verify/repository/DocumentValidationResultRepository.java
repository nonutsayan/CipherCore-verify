package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DocumentValidationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentValidationResultRepository extends JpaRepository<DocumentValidationResult, Long> {
    List<DocumentValidationResult> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
}
