package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ActionItem;
import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.model.enums.ApplicationStatus;
import com.ciphercore.verify.model.enums.WorkflowStage;
import com.ciphercore.verify.service.ActionManagementService;
import com.ciphercore.verify.service.DashboardAggregationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Deliverable 6: Executive Validation Dashboard Controller
 * Serves portfolio summaries, workflow distributions, action queues, and registers.
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardAggregationService dashboardAggregationService;
    private final ActionManagementService actionManagementService;

    public DashboardController(DashboardAggregationService dashboardAggregationService,
                               ActionManagementService actionManagementService) {
        this.dashboardAggregationService = dashboardAggregationService;
        this.actionManagementService = actionManagementService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardAggregationService.DashboardKpiSummary> getSummary(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(dashboardAggregationService.getSummary(academicYear));
    }

    @GetMapping("/workflow-distribution")
    public ResponseEntity<DashboardAggregationService.WorkflowDistribution> getWorkflowDistribution(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(dashboardAggregationService.getWorkflowDistribution(academicYear));
    }

    @GetMapping("/pending-actions")
    public ResponseEntity<List<ActionItem>> getPendingActions() {
        return ResponseEntity.ok(actionManagementService.getPendingActions());
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<AuditEvent>> getRecentActivity(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(dashboardAggregationService.getRecentActivity(limit));
    }

    @GetMapping("/priority-findings")
    public ResponseEntity<List<DashboardAggregationService.PriorityFindingItem>> getPriorityFindings(
            @RequestParam(required = false) String academicYear) {
        return ResponseEntity.ok(dashboardAggregationService.getPriorityFindings(academicYear));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<DashboardAggregationService.ApplicationRegisterRow>> getApplications(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) WorkflowStage stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(dashboardAggregationService.getApplicationRegister(
                query, categoryCode, academicYear, status, stage, pageRequest
        ));
    }
}
