package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DocumentPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentPageRepository extends JpaRepository<DocumentPage, Long> {
    List<DocumentPage> findByDocumentIdOrderByPageNumberAsc(Long documentId);
    void deleteByDocumentId(Long documentId);
}
