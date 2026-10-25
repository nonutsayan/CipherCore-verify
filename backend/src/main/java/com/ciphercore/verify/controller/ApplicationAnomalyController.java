package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.AnomalyRecord;
import com.ciphercore.verify.model.CrossDocumentComparison;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.repository.AnomalyRecordRepository;
import com.ciphercore.verify.repository.ApplicationRepository;
import com.ciphercore.verify.repository.CrossDocumentComparisonRepository;
import com.ciphercore.verify.repository.DocumentRepository;
import com.ciphercore.verify.service.AnomalyDetectionOrchestrator;
import com.ciphercore.verify.service.CrossDocumentAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Section 15 & 24: Application Dossier Anomaly REST Controller
 */
@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
@Tag(name = "Application Dossier Intelligence", description = "Cross-document anomaly analysis and contradictory finding APIs")
public class ApplicationAnomalyController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private CrossDocumentComparisonRepository comparisonRepository;

    @Autowired
    private AnomalyDetectionOrchestrator anomalyOrchestrator;

    @Autowired
    private CrossDocumentAnalysisService crossDocumentService;

    @Operation(summary = "Retrieve cross-document and individual findings for an application")
    @GetMapping("/{applicationId}/anomalies")
    public ResponseEntity<?> getApplicationAnomalies(@PathVariable Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            return ResponseEntity.notFound().build();
        }

        List<AnomalyRecord> anomalies = anomalyRecordRepository.findByApplicationId(applicationId);
        List<CrossDocumentComparison> comparisons = comparisonRepository.findByApplicationId(applicationId);

        return ResponseEntity.ok(Map.of(
            "applicationId", applicationId,
            "totalAnomalies", anomalies.size(),
            "anomalies", anomalies,
            "crossDocumentComparisons", comparisons
        ));
    }

    @Operation(summary = "Run comprehensive relationship-aware forensic analysis across all dossier documents")
    @PostMapping("/{applicationId}/analyze-documents")
    public ResponseEntity<?> analyzeApplicationDocuments(@PathVariable Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            return ResponseEntity.notFound().build();
        }

        List<Document> docs = documentRepository.findByApplicationId(applicationId);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Document d : docs) {
            var run = anomalyOrchestrator.runComprehensiveAnalysis(d.getId());
            results.add(Map.of(
                "documentId", d.getId(),
                "filename", d.getOriginalFilename(),
                "runId", run.getId(),
                "status", run.getProcessingStatus()
            ));
        }

        // Run holistic cross-document comparison
        List<CrossDocumentComparison> comparisons = crossDocumentService.analyzeApplicationDossier(applicationId, 0L);

        return ResponseEntity.ok(Map.of(
            "applicationId", applicationId,
            "documentsProcessed", results.size(),
            "results", results,
            "crossDocumentComparisons", comparisons
        ));
    }
}
