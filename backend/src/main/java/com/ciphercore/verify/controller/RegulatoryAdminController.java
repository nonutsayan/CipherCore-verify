package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.RegulatoryRule;
import com.ciphercore.verify.model.RegulatoryRuleVersion;
import com.ciphercore.verify.model.RegulatorySource;
import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import com.ciphercore.verify.service.RegulatorySourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Deliverable 5: REST Controller for Regulatory Admin & Rule Catalog
 * Manages official sources (APH 2024-27), regulatory rules, and version lifecycle.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Regulatory Admin & Rules", description = "Endpoints for managing regulatory sources and versioned AICTE rules")
public class RegulatoryAdminController {

    private final RegulatorySourceService sourceService;

    public RegulatoryAdminController(RegulatorySourceService sourceService) {
        this.sourceService = sourceService;
    }

    @Operation(summary = "List all registered regulatory sources")
    @GetMapping("/api/regulatory-sources")
    public ResponseEntity<List<RegulatorySource>> getAllSources() {
        return ResponseEntity.ok(sourceService.getAllSources());
    }

    @Operation(summary = "Register or update a regulatory source")
    @PostMapping("/api/regulatory-sources")
    public ResponseEntity<RegulatorySource> saveSource(@RequestBody RegulatorySource source) {
        return ResponseEntity.ok(sourceService.saveSource(source));
    }

    @Operation(summary = "List all regulatory rules")
    @GetMapping("/api/regulatory-rules")
    public ResponseEntity<List<RegulatoryRule>> getAllRules() {
        return ResponseEntity.ok(sourceService.getAllRules());
    }

    @Operation(summary = "List active regulatory rules")
    @GetMapping("/api/regulatory-rules/active")
    public ResponseEntity<List<RegulatoryRule>> getActiveRules() {
        return ResponseEntity.ok(sourceService.getActiveRules());
    }

    @Operation(summary = "Filter regulatory rules by category")
    @GetMapping("/api/regulatory-rules/category/{category}")
    public ResponseEntity<List<RegulatoryRule>> getRulesByCategory(@PathVariable RegulatoryRuleCategory category) {
        return ResponseEntity.ok(sourceService.getRulesByCategory(category));
    }

    @Operation(summary = "Get versions for a specific rule")
    @GetMapping("/api/regulatory-rules/{ruleId}/versions")
    public ResponseEntity<List<RegulatoryRuleVersion>> getVersionsForRule(@PathVariable Long ruleId) {
        return ResponseEntity.ok(sourceService.getVersionsForRule(ruleId));
    }

    @Operation(summary = "Publish a new immutable version for a rule")
    @PostMapping("/api/regulatory-rules/{ruleCode}/versions")
    public ResponseEntity<RegulatoryRuleVersion> publishNewVersion(
            @PathVariable String ruleCode,
            @RequestBody RegulatoryRuleVersion newVersion,
            @RequestParam(required = false, defaultValue = "Chief Scrutiny Officer") String approvedBy) {
        return ResponseEntity.ok(sourceService.publishNewRuleVersion(ruleCode, newVersion, approvedBy));
    }
}
