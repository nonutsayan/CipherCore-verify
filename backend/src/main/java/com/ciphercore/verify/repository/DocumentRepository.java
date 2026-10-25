package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.model.enums.DocumentProcessingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicationId(Long applicationId);
    List<Document> findByApplicationIdAndDocumentType(Long applicationId, String documentType);
    Optional<Document> findBySha256Checksum(String sha256Checksum);

    List<Document> findAllByOrderByUploadTimestampDesc();
    Page<Document> findAllByOrderByUploadTimestampDesc(Pageable pageable);

    List<Document> findByDocumentCategory(DocumentCategory category);
    List<Document> findByProcessingStatus(DocumentProcessingStatus status);

    long countByProcessingStatus(DocumentProcessingStatus status);
}
