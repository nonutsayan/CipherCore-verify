package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(nullable = false, length = 64)
    private String actorUsername = "anonymous";

    @Column(length = 64)
    private String actorRole = "UNAUTHENTICATED";

    @Column(length = 64)
    private String targetType; // APPLICATION, DOCUMENT, USER, RULE, AUDIT_TRAIL

    @Column(length = 128)
    private String targetId;

    @Column(nullable = false, length = 64)
    private String action;

    @Column(nullable = false, length = 32)
    private String outcome; // SUCCESS, DENIED, FAILURE

    @Column(length = 128)
    private String reasonCode;

    @Column(length = 64)
    private String ipAddress = "127.0.0.1";

    @Column(columnDefinition = "TEXT")
    private String details;

    public SecurityEvent() {}

    public SecurityEvent(String eventType, String actorUsername, String actorRole, String targetType,
                         String targetId, String action, String outcome, String reasonCode,
                         String details, String ipAddress) {
        this.eventType = eventType;
        this.actorUsername = actorUsername != null ? actorUsername : "anonymous";
        this.actorRole = actorRole != null ? actorRole : "UNAUTHENTICATED";
        this.targetType = targetType;
        this.targetId = targetId;
        this.action = action;
        this.outcome = outcome;
        this.reasonCode = reasonCode;
        this.details = details;
        this.ipAddress = ipAddress != null ? ipAddress : "127.0.0.1";
        this.occurredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getActorUsername() { return actorUsername; }
    public void setActorUsername(String actorUsername) { this.actorUsername = actorUsername; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
