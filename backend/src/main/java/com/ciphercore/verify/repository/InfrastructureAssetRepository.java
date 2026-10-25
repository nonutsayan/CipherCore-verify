package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.InfrastructureAsset;
import com.ciphercore.verify.model.enums.InfrastructureCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InfrastructureAssetRepository extends JpaRepository<InfrastructureAsset, Long> {
    List<InfrastructureAsset> findByApplicationId(Long applicationId);
    List<InfrastructureAsset> findByApplicationIdAndCategory(Long applicationId, InfrastructureCategory category);
    Optional<InfrastructureAsset> findByApplicationIdAndAssetCode(Long applicationId, String assetCode);
}
