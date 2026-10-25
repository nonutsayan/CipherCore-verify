package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Section 24: Document Authentication & Anomaly Detection REST Controller
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
@Tag(name = "Document Authentication & Forensic Engine", description = "AI-Powered Anomaly Detection, Duplicate Checking, and Cryptographic Verification API")
public class DocumentAuthenticationController {

    @Autowired
    private AnomalyDetectionOrchestrator anomalyOrchestrator;

    @Autowired
    private AnomalyReportService anomalyReportService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private DuplicateMatchRepository duplicateMatchRepository;

    @Autowired
    private SignatureVerificationService signatureService;

    @Autowired
    private ReferenceVerificationService referenceService;

    @Autowired
    private ExtractedDocumentFieldRepository fieldRepository;

    @Operation(summary = "Run full integrity and anomaly analysis for a document")
    @PostMapping("/{documentId}/analyze-integrity")
    public ResponseEntity<?> analyzeDocumentIntegrity(@PathVariable Long documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DocumentAnalysisRun run = anomalyOrchestrator.runComprehensiveAnalysis(documentId);
        return ResponseEntity.ok(Map.of(
            "runId", run.getId(),
            "documentId", documentId,
            "status", run.getProcessingStatus(),
            "startedAt", run.getStartedAt().toString(),
            "completedAt", run.getCompletedAt() != null ? run.getCompletedAt().toString() : ""
        ));
    }

    @Operation(summary = "Retrieve all detected anomalies for a document")
    @GetMapping("/{documentId}/anomalies")
    public ResponseEntity<List<AnomalyRecord>> getDocumentAnomalies(@PathVariable Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(anomalyRecordRepository.findByDocumentId(documentId));
    }

    @Operation(summary = "Retrieve structured Section 22 integrity and anomaly report")
    @GetMapping("/{documentId}/integrity-report")
    public ResponseEntity<Map<String, Object>> getIntegrityReport(@PathVariable Long documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(anomalyReportService.generateIntegrityReport(docOpt.get()));
    }

    @Operation(summary = "Retrieve exact and near-duplicate matches for a document")
    @GetMapping("/{documentId}/duplicates")
    public ResponseEntity<List<DuplicateMatch>> getDocumentDuplicates(@PathVariable Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(duplicateMatchRepository.findBySourceDocumentId(documentId));
    }

    @Operation(summary = "Compare two documents side-by-side for textual, structural, and field differences")
    @PostMapping("/{documentId}/compare/{otherDocumentId}")
    public ResponseEntity<?> compareDocuments(@PathVariable Long documentId, @PathVariable Long otherDocumentId) {
        Optional<Document> docAOpt = documentRepository.findById(documentId);
        Optional<Document> docBOpt = documentRepository.findById(otherDocumentId);

        if (docAOpt.isEmpty() || docBOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document docA = docAOpt.get();
        Document docB = docBOpt.get();

        List<ExtractedDocumentField> fieldsA = fieldRepository.findByDocumentId(docA.getId());
        List<ExtractedDocumentField> fieldsB = fieldRepository.findByDocumentId(docB.getId());

        Map<String, String> mapA = new HashMap<>();
        for (ExtractedDocumentField f : fieldsA) mapA.put(f.getFieldName(), f.getRawValue());

        Map<String, String> mapB = new HashMap<>();
        for (ExtractedDocumentField f : fieldsB) mapB.put(f.getFieldName(), f.getRawValue());

        List<Map<String, Object>> fieldComparisons = new ArrayList<>();
        Set<String> allKeys = new HashSet<>(mapA.keySet());
        allKeys.addAll(mapB.keySet());

        for (String k : allKeys) {
            String valA = mapA.getOrDefault(k, "Not Present");
            String valB = mapB.getOrDefault(k, "Not Present");
            boolean match = valA.equalsIgnoreCase(valB);
            fieldComparisons.add(Map.of(
                "fieldName", k,
                "valueDocumentA", valA,
                "valueDocumentB", valB,
                "isMatch", match
            ));
        }

        boolean shaMatch = docA.getSha256Checksum() != null && docA.getSha256Checksum().equalsIgnoreCase(docB.getSha256Checksum());

        return ResponseEntity.ok(Map.of(
            "sourceDocumentId", docA.getId(),
            "targetDocumentId", docB.getId(),
            "exactHashMatch", shaMatch,
            "documentA_filename", docA.getOriginalFilename(),
            "documentB_filename", docB.getOriginalFilename(),
            "fieldComparisons", fieldComparisons
        ));
    }

    @Operation(summary = "Run check against authorized institutional reference records")
    @PostMapping("/{documentId}/verify-reference")
    public ResponseEntity<?> verifyReference(@PathVariable Long documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = docOpt.get();
        List<ExtractedDocumentField> fields = fieldRepository.findByDocumentId(doc.getId());

        String instName = null, code = null;
        for (ExtractedDocumentField f : fields) {
            if ("institutionName".equalsIgnoreCase(f.getFieldName())) instName = f.getNormalizedValue();
            if ("certificateNumber".equalsIgnoreCase(f.getFieldName()) || "registrationNumber".equalsIgnoreCase(f.getFieldName())) code = f.getNormalizedValue();
        }

        VerificationRecord record = referenceService.verifyAgainstRegistry(doc, code, instName, 0L);
        return ResponseEntity.ok(record);
    }

    @Operation(summary = "Retrieve cryptographic signature verification status")
    @GetMapping("/{documentId}/signature-status")
    public ResponseEntity<?> getSignatureStatus(@PathVariable Long documentId) {
        Optional<Document> docOpt = documentRepository.findById(documentId);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(signatureService.verifySignatures(docOpt.get(), 0L));
    }
}
