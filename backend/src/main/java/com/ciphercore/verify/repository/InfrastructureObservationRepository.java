package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.InfrastructureObservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InfrastructureObservationRepository extends JpaRepository<InfrastructureObservation, Long> {
    List<InfrastructureObservation> findByAnalysisJobId(Long analysisJobId);
    List<InfrastructureObservation> findByEvidenceId(Long evidenceId);
    List<InfrastructureObservation> findByAssetId(Long assetId);
    List<InfrastructureObservation> findByIsDiscrepancyTrue();

    @org.springframework.data.jpa.repository.Query("SELECT o FROM InfrastructureObservation o JOIN InfrastructureAsset a ON o.assetId = a.id WHERE a.applicationId = :applicationId AND o.isDiscrepancy = true")
    List<InfrastructureObservation> findDiscrepanciesByApplicationId(@org.springframework.data.repository.query.Param("applicationId") Long applicationId);
}
