package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ComplianceReviewTask;
import com.ciphercore.verify.model.enums.ComplianceReviewDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplianceReviewTaskRepository extends JpaRepository<ComplianceReviewTask, Long> {
    List<ComplianceReviewTask> findByAssessmentId(Long assessmentId);
    List<ComplianceReviewTask> findByAssignedOfficer(String assignedOfficer);
    List<ComplianceReviewTask> findByReviewStatus(ComplianceReviewDecision reviewStatus);
}
