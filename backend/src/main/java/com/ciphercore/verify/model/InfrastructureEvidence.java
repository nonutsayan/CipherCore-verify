package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.InfrastructureAnalysisStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Infrastructure Evidence Entity
 * Media files submitted as evidence for physical facilities (photos, walkthrough videos, floor plans).
 */
@Entity
@Table(name = "infrastructure_evidence")
public class InfrastructureEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    private Long assetId; // Optional link to specific InfrastructureAsset

    @Column(nullable = false, length = 64)
    private String evidenceType; // PHOTOGRAPH, VIDEO_WALKTHROUGH, FLOOR_PLAN_PDF, COMPLETION_CERTIFICATE

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 512)
    private String fileUri;

    @Column(length = 64)
    private String mimeType;

    private Long fileSizeBytes;

    @Column(length = 64)
    private String sha256Hash;

    private Integer imageWidth;
    private Integer imageHeight;

    @Column(length = 64)
    private String exifTimestamp;

    private Double gpsLatitude;
    private Double gpsLongitude;

    private Double qualityScore = 0.85;
    private Double blurScore = 0.12;
    private Double lightingAdequacy = 0.90;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InfrastructureAnalysisStatus analysisStatus = InfrastructureAnalysisStatus.NOT_ANALYZED;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InfrastructureEvidence() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public Long getAssetId() { return assetId; }
    public void setAssetId(Long assetId) { this.assetId = assetId; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileUri() { return fileUri; }
    public void setFileUri(String fileUri) { this.fileUri = fileUri; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public Integer getImageWidth() { return imageWidth; }
    public void setImageWidth(Integer imageWidth) { this.imageWidth = imageWidth; }

    public Integer getImageHeight() { return imageHeight; }
    public void setImageHeight(Integer imageHeight) { this.imageHeight = imageHeight; }

    public String getExifTimestamp() { return exifTimestamp; }
    public void setExifTimestamp(String exifTimestamp) { this.exifTimestamp = exifTimestamp; }

    public Double getGpsLatitude() { return gpsLatitude; }
    public void setGpsLatitude(Double gpsLatitude) { this.gpsLatitude = gpsLatitude; }

    public Double getGpsLongitude() { return gpsLongitude; }
    public void setGpsLongitude(Double gpsLongitude) { this.gpsLongitude = gpsLongitude; }

    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }

    public Double getBlurScore() { return blurScore; }
    public void setBlurScore(Double blurScore) { this.blurScore = blurScore; }

    public Double getLightingAdequacy() { return lightingAdequacy; }
    public void setLightingAdequacy(Double lightingAdequacy) { this.lightingAdequacy = lightingAdequacy; }

    public InfrastructureAnalysisStatus getAnalysisStatus() { return analysisStatus; }
    public void setAnalysisStatus(InfrastructureAnalysisStatus analysisStatus) { this.analysisStatus = analysisStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
