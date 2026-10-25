package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_access_events")
public class DocumentAccessEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentId;

    @Column(length = 255)
    private String documentFilename;

    private Long applicationId;

    @Column(nullable = false, length = 64)
    private String actorUsername;

    @Column(length = 64)
    private String actorRole;

    @Column(nullable = false, length = 32)
    private String accessType; // PREVIEW, DOWNLOAD, METADATA

    @Column(nullable = false, length = 32)
    private String outcome; // GRANTED, DENIED

    @Column(length = 255)
    private String denialReason;

    @Column(nullable = false)
    private LocalDateTime accessedAt = LocalDateTime.now();

    @Column(length = 64)
    private String ipAddress = "127.0.0.1";

    public DocumentAccessEvent() {}

    public DocumentAccessEvent(Long documentId, String documentFilename, Long applicationId,
                               String actorUsername, String actorRole, String accessType,
                               String outcome, String denialReason, String ipAddress) {
        this.documentId = documentId;
        this.documentFilename = documentFilename;
        this.applicationId = applicationId;
        this.actorUsername = actorUsername;
        this.actorRole = actorRole;
        this.accessType = accessType;
        this.outcome = outcome;
        this.denialReason = denialReason;
        this.ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";
        this.accessedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentFilename() { return documentFilename; }
    public void setDocumentFilename(String documentFilename) { this.documentFilename = documentFilename; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getDenialReason() { return denialReason; }
    public void setDenialReason(String denialReason) { this.denialReason = denialReason; }

    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
