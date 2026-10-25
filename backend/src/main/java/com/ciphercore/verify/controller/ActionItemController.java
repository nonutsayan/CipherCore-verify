package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ActionItem;
import com.ciphercore.verify.model.enums.ActionItemStatus;
import com.ciphercore.verify.service.ActionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Deliverable 6: Action Item & Task Management Controller
 */
@RestController
@RequestMapping("/api/actions")
@CrossOrigin(origins = "*")
public class ActionItemController {

    private final ActionManagementService actionManagementService;

    public ActionItemController(ActionManagementService actionManagementService) {
        this.actionManagementService = actionManagementService;
    }

    @GetMapping
    public ResponseEntity<List<ActionItem>> listPendingActions(
            @RequestParam(required = false) Long applicationId) {
        if (applicationId != null) {
            return ResponseEntity.ok(actionManagementService.getActionsForApplication(applicationId));
        }
        return ResponseEntity.ok(actionManagementService.getPendingActions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActionItem> getAction(@PathVariable Long id) {
        return actionManagementService.getActionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ActionItem> createAction(
            @RequestBody ActionItem item,
            @RequestHeader(value = "X-Officer-Name", defaultValue = "Dr. Rajesh Sharma") String officerName) {
        return ResponseEntity.ok(actionManagementService.createAction(item, officerName));
    }

    public record AssignRequest(String assignedTo, String assignedBy) {}

    @PostMapping("/{id}/assign")
    public ResponseEntity<ActionItem> assignAction(
            @PathVariable Long id,
            @RequestBody AssignRequest req) {
        return ResponseEntity.ok(actionManagementService.assignAction(id, req.assignedTo(), req.assignedBy()));
    }

    public record StatusUpdateRequest(ActionItemStatus status, String resolutionNotes, String username) {}

    @PostMapping("/{id}/status")
    public ResponseEntity<ActionItem> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(actionManagementService.updateStatus(id, req.status(), req.resolutionNotes(), req.username()));
    }

    public record EscalateRequest(String escalationReason, String username) {}

    @PostMapping("/{id}/escalate")
    public ResponseEntity<ActionItem> escalateAction(
            @PathVariable Long id,
            @RequestBody EscalateRequest req) {
        return ResponseEntity.ok(actionManagementService.escalateAction(id, req.escalationReason(), req.username()));
    }
}
