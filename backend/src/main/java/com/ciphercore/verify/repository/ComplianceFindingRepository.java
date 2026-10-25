package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ComplianceFinding;
import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceFindingRepository extends JpaRepository<ComplianceFinding, Long> {
    List<ComplianceFinding> findByAssessmentId(Long assessmentId);
    List<ComplianceFinding> findByApplicationId(Long applicationId);
    List<ComplianceFinding> findByAssessmentIdAndReviewStatus(Long assessmentId, ComplianceReviewDecision reviewStatus);
}
