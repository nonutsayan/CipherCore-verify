package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DocumentAccessEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentAccessEventRepository extends JpaRepository<DocumentAccessEvent, Long> {

    List<DocumentAccessEvent> findByDocumentIdOrderByAccessedAtDesc(Long documentId);

    List<DocumentAccessEvent> findByApplicationIdOrderByAccessedAtDesc(Long applicationId);

    List<DocumentAccessEvent> findTop50ByOrderByAccessedAtDesc();
}
