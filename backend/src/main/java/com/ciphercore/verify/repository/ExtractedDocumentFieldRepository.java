package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ExtractedDocumentField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtractedDocumentFieldRepository extends JpaRepository<ExtractedDocumentField, Long> {
    List<ExtractedDocumentField> findByDocumentId(Long documentId);
    Optional<ExtractedDocumentField> findByDocumentIdAndFieldName(Long documentId, String fieldName);
}
