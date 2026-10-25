package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ValidationFinding;
import com.ciphercore.verify.model.enums.FindingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValidationFindingRepository extends JpaRepository<ValidationFinding, Long> {
    List<ValidationFinding> findByValidationRunId(Long validationRunId);
    List<ValidationFinding> findByValidationRunIdAndStatus(Long validationRunId, FindingStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM ValidationFinding f WHERE f.validationRun.application.id = :applicationId")
    List<ValidationFinding> findByApplicationId(@org.springframework.data.repository.query.Param("applicationId") Long applicationId);
}
