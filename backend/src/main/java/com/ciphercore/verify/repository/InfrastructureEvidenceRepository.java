package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.InfrastructureEvidence;
import com.ciphercore.verify.model.enums.InfrastructureAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InfrastructureEvidenceRepository extends JpaRepository<InfrastructureEvidence, Long> {
    List<InfrastructureEvidence> findByApplicationId(Long applicationId);
    List<InfrastructureEvidence> findByApplicationIdAndAssetId(Long applicationId, Long assetId);
    List<InfrastructureEvidence> findByAnalysisStatus(InfrastructureAnalysisStatus analysisStatus);
}
