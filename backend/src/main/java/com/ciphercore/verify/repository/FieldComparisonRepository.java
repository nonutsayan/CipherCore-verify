package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.FieldComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldComparisonRepository extends JpaRepository<FieldComparison, Long> {
    List<FieldComparison> findByVerificationResultId(Long verificationResultId);
}
