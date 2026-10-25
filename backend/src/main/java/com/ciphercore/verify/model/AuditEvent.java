package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime eventTimestamp = LocalDateTime.now();

    @Column(nullable = false, length = 64)
    private String username;

    @Column(length = 32)
    private String userRole = "REVIEWER";

    @Column(nullable = false, length = 64)
    private String action; // APPLICATION_CREATED, DOCUMENT_UPLOADED, VALIDATION_RUN, FINDING_REVIEWED, FIELD_CORRECTION

    @Column(length = 64)
    private String entityName;

    @Column(length = 64)
    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 64)
    private String ipAddress = "127.0.0.1";

    @Column(name = "seq_num")
    private Long sequenceNumber;

    @Column(length = 64)
    private String previousHash;

    @Column(length = 64)
    private String eventHash;

    @Column(length = 32)
    private String hashAlgorithm = "SHA-256";

    public AuditEvent() {}

    public AuditEvent(String username, String userRole, String action, String entityName, String entityId, String details, String ipAddress) {
        this.username = username;
        this.userRole = userRole;
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.eventTimestamp = LocalDateTime.now();
    }

    public AuditEvent(String entityId, String action, String username, String details) {
        this.username = username != null ? username : "reviewer";
        this.userRole = "REVIEWER";
        this.action = action;
        this.entityName = "DOCUMENT";
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = "127.0.0.1";
        this.eventTimestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(LocalDateTime eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public String getEventHash() { return eventHash; }
    public void setEventHash(String eventHash) { this.eventHash = eventHash; }

    public String getHashAlgorithm() { return hashAlgorithm; }
    public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }
}
