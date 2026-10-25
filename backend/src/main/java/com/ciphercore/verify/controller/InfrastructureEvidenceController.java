package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.InfrastructureAnalysisJob;
import com.ciphercore.verify.model.InfrastructureAsset;
import com.ciphercore.verify.model.InfrastructureEvidence;
import com.ciphercore.verify.model.InfrastructureObservation;
import com.ciphercore.verify.repository.InfrastructureObservationRepository;
import com.ciphercore.verify.service.InfrastructureAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Deliverable 5: REST Controller for Subsystem B — Infrastructure Evidence & Computer Vision Scrutiny
 * Handles evidence uploads, CV object detections, OCR signage reading, and discrepancy detection.
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Infrastructure Evidence & Computer Vision", description = "Endpoints for analyzing campus photos, videos, and blueprints")
public class InfrastructureEvidenceController {

    private final InfrastructureAssessmentService assessmentService;
    private final InfrastructureObservationRepository observationRepository;

    public InfrastructureEvidenceController(InfrastructureAssessmentService assessmentService,
                                           InfrastructureObservationRepository observationRepository) {
        this.assessmentService = assessmentService;
        this.observationRepository = observationRepository;
    }

    @Operation(summary = "Get all infrastructure evidence media for an application")
    @GetMapping("/api/applications/{applicationId}/infrastructure-evidence")
    public ResponseEntity<List<InfrastructureEvidence>> getEvidenceForApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(assessmentService.getEvidenceForApplication(applicationId));
    }

    @Operation(summary = "Get all claimed infrastructure assets (rooms, labs, ramps) for an application")
    @GetMapping("/api/applications/{applicationId}/infrastructure-assets")
    public ResponseEntity<List<InfrastructureAsset>> getAssetsForApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(assessmentService.getAssetsForApplication(applicationId));
    }

    @Operation(summary = "Trigger AI visual inspection and frame extraction for evidence media")
    @PostMapping("/api/infrastructure-evidence/{evidenceId}/analyze")
    public ResponseEntity<InfrastructureAnalysisJob> analyzeEvidence(
            @PathVariable Long evidenceId,
            @RequestParam(required = false, defaultValue = "Scrutiny Officer") String officerUsername) {
        InfrastructureAnalysisJob job = assessmentService.analyzeEvidence(evidenceId, officerUsername);
        return ResponseEntity.ok(job);
    }

    @Operation(summary = "Get all visual observations and bounding boxes for an evidence media")
    @GetMapping("/api/infrastructure-evidence/{evidenceId}/observations")
    public ResponseEntity<List<InfrastructureObservation>> getObservationsForEvidence(@PathVariable Long evidenceId) {
        return ResponseEntity.ok(assessmentService.getObservationsForEvidence(evidenceId));
    }

    @Operation(summary = "List all flagged visual discrepancies across infrastructure evidence")
    @GetMapping("/api/infrastructure-observations/discrepancies")
    public ResponseEntity<List<InfrastructureObservation>> getFlaggedDiscrepancies() {
        return ResponseEntity.ok(observationRepository.findByIsDiscrepancyTrue());
    }
}
