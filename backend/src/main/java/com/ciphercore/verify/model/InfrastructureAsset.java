package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.InfrastructureCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Infrastructure Asset Entity
 * Structured record of institution campus facilities, classrooms, laboratories, computer centers, and amenities.
 */
@Entity
@Table(name = "infrastructure_assets")
public class InfrastructureAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String assetCode; // e.g., RM-101, LAB-CSE-01, WORKSHOP-MECH-01, RAMP-MAIN

    @Column(nullable = false, length = 255)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private InfrastructureCategory category;

    @Column(length = 128)
    private String buildingName;

    @Column(length = 64)
    private String floorLevel;

    private Double claimedAreaSqm;
    private Integer claimedCapacity;
    private Integer equipmentCount;

    private Boolean isAccessible = true;
    private Boolean hasSafetySignage = true;

    @Column(length = 64)
    private String assetStatus = "FUNCTIONAL";

    @Column(columnDefinition = "TEXT")
    private String metadataJson;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InfrastructureAsset() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public InfrastructureCategory getCategory() { return category; }
    public void setCategory(InfrastructureCategory category) { this.category = category; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getFloorLevel() { return floorLevel; }
    public void setFloorLevel(String floorLevel) { this.floorLevel = floorLevel; }

    public Double getClaimedAreaSqm() { return claimedAreaSqm; }
    public void setClaimedAreaSqm(Double claimedAreaSqm) { this.claimedAreaSqm = claimedAreaSqm; }

    public Integer getClaimedCapacity() { return claimedCapacity; }
    public void setClaimedCapacity(Integer claimedCapacity) { this.claimedCapacity = claimedCapacity; }

    public Integer getEquipmentCount() { return equipmentCount; }
    public void setEquipmentCount(Integer equipmentCount) { this.equipmentCount = equipmentCount; }

    public Boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(Boolean isAccessible) { this.isAccessible = isAccessible; }

    public Boolean getHasSafetySignage() { return hasSafetySignage; }
    public void setHasSafetySignage(Boolean hasSafetySignage) { this.hasSafetySignage = hasSafetySignage; }

    public String getAssetStatus() { return assetStatus; }
    public void setAssetStatus(String assetStatus) { this.assetStatus = assetStatus; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
