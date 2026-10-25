package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.DuplicateMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DuplicateMatchRepository extends JpaRepository<DuplicateMatch, Long> {
    List<DuplicateMatch> findBySourceDocumentId(Long sourceDocumentId);
    List<DuplicateMatch> findBySourceDocumentIdOrMatchedDocumentId(Long sourceDocId, Long matchedDocId);
    List<DuplicateMatch> findBySourceApplicationId(Long applicationId);
}
