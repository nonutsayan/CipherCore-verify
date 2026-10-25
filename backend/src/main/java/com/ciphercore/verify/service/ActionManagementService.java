package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Deliverable 6: Action Management Service
 * Manages the unified task queue, reviewer assignments, status transitions,
 * escalations, and audit trails.
 */
@Service
@Transactional
public class ActionManagementService {

    private final ActionItemRepository actionItemRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditEventRepository auditEventRepository;
    private final NotificationRepository notificationRepository;

    public ActionManagementService(ActionItemRepository actionItemRepository,
                                   ApplicationRepository applicationRepository,
                                   AuditEventRepository auditEventRepository,
                                   NotificationRepository notificationRepository) {
        this.actionItemRepository = actionItemRepository;
        this.applicationRepository = applicationRepository;
        this.auditEventRepository = auditEventRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<ActionItem> getActionsForApplication(Long applicationId) {
        return actionItemRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
    }

    public List<ActionItem> getPendingActions() {
        return actionItemRepository.findByStatusNotOrderByPriorityDescCreatedAtAsc(ActionItemStatus.COMPLETED);
    }

    public Optional<ActionItem> getActionById(Long id) {
        return actionItemRepository.findById(id);
    }

    public ActionItem createAction(ActionItem item, String username) {
        if (item.getApplicationRef() == null && item.getApplicationId() != null) {
            applicationRepository.findById(item.getApplicationId()).ifPresent(app -> {
                item.setApplicationRef(app.getApplicationRef());
                item.setInstitutionName(app.getInstitutionName());
            });
        }
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        ActionItem saved = actionItemRepository.save(item);

        logAuditEvent(saved.getApplicationId(), "ACTION_CREATED", username != null ? username : "system",
                "Created task #" + saved.getId() + " (" + saved.getTitle() + ") with priority " + saved.getPriority());

        return saved;
    }

    public ActionItem assignAction(Long actionId, String assignedTo, String assignedBy) {
        ActionItem item = actionItemRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("ActionItem not found with ID: " + actionId));

        String prevAssigned = item.getAssignedTo();
        item.setAssignedTo(assignedTo);
        item.setAssignedBy(assignedBy != null ? assignedBy : "Dr. Rajesh Sharma");
        if (item.getStatus() == ActionItemStatus.OPEN) {
            item.setStatus(ActionItemStatus.ASSIGNED);
        }
        item.setUpdatedAt(LocalDateTime.now());
        ActionItem saved = actionItemRepository.save(item);

        logAuditEvent(item.getApplicationId(), "ACTION_ASSIGNED", assignedBy != null ? assignedBy : "officer",
                "Assigned task #" + actionId + " to " + assignedTo + " (was: " + (prevAssigned != null ? prevAssigned : "unassigned") + ")");

        // Generate in-app notification
        Notification notif = new Notification(
                assignedTo,
                item.getApplicationId(),
                item.getApplicationRef(),
                NotificationType.TASK_ASSIGNED,
                Severity.MODERATE,
                "New Scrutiny Task Assigned: " + item.getTitle(),
                "Task #" + item.getId() + " has been assigned to you for application " + item.getApplicationRef(),
                "/#actions"
        );
        notificationRepository.save(notif);

        return saved;
    }

    public ActionItem updateStatus(Long actionId, ActionItemStatus newStatus, String resolutionNotes, String username) {
        ActionItem item = actionItemRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("ActionItem not found with ID: " + actionId));

        ActionItemStatus prevStatus = item.getStatus();
        item.setStatus(newStatus);
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            item.setResolutionNotes((item.getResolutionNotes() != null ? item.getResolutionNotes() + "\n" : "") +
                    "[" + LocalDateTime.now() + " by " + (username != null ? username : "officer") + "]: " + resolutionNotes);
        }
        item.setUpdatedAt(LocalDateTime.now());
        ActionItem saved = actionItemRepository.save(item);

        logAuditEvent(item.getApplicationId(), "ACTION_STATUS_CHANGED", username != null ? username : "officer",
                "Updated task #" + actionId + " status from " + prevStatus + " to " + newStatus);

        return saved;
    }

    public ActionItem escalateAction(Long actionId, String escalationReason, String username) {
        ActionItem item = actionItemRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("ActionItem not found with ID: " + actionId));

        item.setPriority(ActionItemPriority.CRITICAL);
        item.setStatus(ActionItemStatus.WAITING_FOR_REVIEW);
        item.setResolutionNotes((item.getResolutionNotes() != null ? item.getResolutionNotes() + "\n" : "") +
                "[ESCALATION by " + (username != null ? username : "officer") + "]: " + escalationReason);
        item.setUpdatedAt(LocalDateTime.now());
        ActionItem saved = actionItemRepository.save(item);

        logAuditEvent(item.getApplicationId(), "ACTION_ESCALATED", username != null ? username : "officer",
                "Escalated task #" + actionId + " to CRITICAL priority: " + escalationReason);

        // Notify regulatory admin
        Notification notif = new Notification(
                "admin",
                item.getApplicationId(),
                item.getApplicationRef(),
                NotificationType.ESCALATION_TRIGGERED,
                Severity.CRITICAL,
                "URGENT ESCALATION: " + item.getTitle(),
                "Task #" + item.getId() + " escalated: " + escalationReason,
                "/#actions"
        );
        notificationRepository.save(notif);

        return saved;
    }

    private void logAuditEvent(Long applicationId, String action, String username, String details) {
        AuditEvent event = new AuditEvent();
        event.setEntityName("APPLICATION");
        event.setEntityId(applicationId != null ? String.valueOf(applicationId) : "DASHBOARD");
        event.setAction(action);
        event.setUsername(username != null ? username : "system");
        event.setUserRole("COMPLIANCE_OFFICER");
        event.setDetails(details);
        event.setEventTimestamp(LocalDateTime.now());
        auditEventRepository.save(event);
    }
}
