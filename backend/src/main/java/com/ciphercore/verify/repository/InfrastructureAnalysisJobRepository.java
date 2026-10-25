package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.InfrastructureAnalysisJob;
import com.ciphercore.verify.model.enums.InfrastructureAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InfrastructureAnalysisJobRepository extends JpaRepository<InfrastructureAnalysisJob, Long> {
    List<InfrastructureAnalysisJob> findByApplicationId(Long applicationId);
    List<InfrastructureAnalysisJob> findByEvidenceId(Long evidenceId);
    List<InfrastructureAnalysisJob> findByJobStatus(InfrastructureAnalysisStatus jobStatus);
}
