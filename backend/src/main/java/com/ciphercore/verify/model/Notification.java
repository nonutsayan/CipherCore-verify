package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.NotificationType;
import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 6: In-App Notification Entity
 * Alerts officials to newly submitted applications, critical forensic anomalies,
 * verification timeouts, compliance deficiencies, and task escalations.
 */
@Entity
@Table(name = "dashboard_notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String recipientUsername;

    private Long applicationId;

    @Column(length = 64)
    private String applicationRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Severity severity = Severity.MODERATE;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private Boolean isRead = false;

    @Column(length = 255)
    private String actionLink;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime readAt;

    public Notification() {}

    public Notification(String recipientUsername, Long applicationId, String applicationRef,
                        NotificationType notificationType, Severity severity,
                        String title, String message, String actionLink) {
        this.recipientUsername = recipientUsername;
        this.applicationId = applicationId;
        this.applicationRef = applicationRef;
        this.notificationType = notificationType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.actionLink = actionLink;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getApplicationRef() { return applicationRef; }
    public void setApplicationRef(String applicationRef) { this.applicationRef = applicationRef; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getActionLink() { return actionLink; }
    public void setActionLink(String actionLink) { this.actionLink = actionLink; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
