package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.AnomalyReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalyReviewRepository extends JpaRepository<AnomalyReview, Long> {
    List<AnomalyReview> findByAnomalyId(Long anomalyId);
    List<AnomalyReview> findByReviewerId(String reviewerId);
}
