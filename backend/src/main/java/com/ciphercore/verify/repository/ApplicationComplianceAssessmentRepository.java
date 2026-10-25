package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ApplicationComplianceAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationComplianceAssessmentRepository extends JpaRepository<ApplicationComplianceAssessment, Long> {
    List<ApplicationComplianceAssessment> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
    Optional<ApplicationComplianceAssessment> findFirstByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
