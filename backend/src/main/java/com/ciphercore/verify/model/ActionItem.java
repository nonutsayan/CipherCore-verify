package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ActionItemPriority;
import com.ciphercore.verify.model.enums.ActionItemStatus;
import com.ciphercore.verify.model.enums.ActionItemType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 6: Unified Action Queue Task Entity
 * Unifies tasks across Application Validation, Document Intelligence, Forensics,
 * Issuing Authority Verification, and Regulatory Compliance.
 */
@Entity
@Table(name = "dashboard_action_items")
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(length = 64)
    private String applicationRef;

    @Column(length = 255)
    private String institutionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ActionItemType taskType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ActionItemStatus status = ActionItemStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ActionItemPriority priority = ActionItemPriority.MEDIUM;

    @Column(length = 128)
    private String assignedTo;

    @Column(length = 128)
    private String assignedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime dueAt;

    private Long relatedFindingId;
    private Long relatedDocumentId;
    private Long relatedVerificationId;

    @Column(length = 64)
    private String relatedRuleCode;

    @Column(columnDefinition = "TEXT")
    private String recommendedNextStep;

    @Column(columnDefinition = "TEXT")
    private String completionRequirements;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(length = 255)
    private String navigationUrl;

    public ActionItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getApplicationRef() { return applicationRef; }
    public void setApplicationRef(String applicationRef) { this.applicationRef = applicationRef; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public ActionItemType getTaskType() { return taskType; }
    public void setTaskType(ActionItemType taskType) { this.taskType = taskType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ActionItemStatus getStatus() { return status; }
    public void setStatus(ActionItemStatus status) { this.status = status; }

    public ActionItemPriority getPriority() { return priority; }
    public void setPriority(ActionItemPriority priority) { this.priority = priority; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }

    public Long getRelatedFindingId() { return relatedFindingId; }
    public void setRelatedFindingId(Long relatedFindingId) { this.relatedFindingId = relatedFindingId; }

    public Long getRelatedDocumentId() { return relatedDocumentId; }
    public void setRelatedDocumentId(Long relatedDocumentId) { this.relatedDocumentId = relatedDocumentId; }

    public Long getRelatedVerificationId() { return relatedVerificationId; }
    public void setRelatedVerificationId(Long relatedVerificationId) { this.relatedVerificationId = relatedVerificationId; }

    public String getRelatedRuleCode() { return relatedRuleCode; }
    public void setRelatedRuleCode(String relatedRuleCode) { this.relatedRuleCode = relatedRuleCode; }

    public String getRecommendedNextStep() { return recommendedNextStep; }
    public void setRecommendedNextStep(String recommendedNextStep) { this.recommendedNextStep = recommendedNextStep; }

    public String getCompletionRequirements() { return completionRequirements; }
    public void setCompletionRequirements(String completionRequirements) { this.completionRequirements = completionRequirements; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public String getNavigationUrl() { return navigationUrl; }
    public void setNavigationUrl(String navigationUrl) { this.navigationUrl = navigationUrl; }
}
