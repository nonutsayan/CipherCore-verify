package com.ciphercore.verify.service;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Section 22: Structured Anomaly & Integrity Report Service
 * Aggregates all forensic detectors into a stable, machine-readable JSON report.
 */
@Service
public class AnomalyReportService {

    @Autowired
    private DocumentIntegrityRecordRepository integrityRecordRepository;

    @Autowired
    private DuplicateMatchRepository duplicateMatchRepository;

    @Autowired
    private AnomalyRecordRepository anomalyRecordRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    @Autowired
    private VerificationRecordRepository verificationRecordRepository;

    @Autowired
    private CrossDocumentComparisonRepository comparisonRepository;

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    public Map<String, Object> generateIntegrityReport(Document document) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportId", "RPT-INT-" + document.getId() + "-" + System.currentTimeMillis());
        report.put("documentId", document.getId());
        report.put("applicationId", document.getApplicationId());
        report.put("originalFilename", document.getOriginalFilename());
        report.put("documentCategory", document.getDocumentCategory() != null ? document.getDocumentCategory().name() : "UNKNOWN");
        report.put("analysisStatus", "COMPLETED");
        report.put("analysisVersion", "1.0");
        report.put("analyzedAt", LocalDateTime.now().toString());

        // 1. File Integrity & Duplicates
        Map<String, Object> fileIntegrity = new LinkedHashMap<>();
        Optional<DocumentIntegrityRecord> intOpt = integrityRecordRepository.findByDocumentId(document.getId());
        fileIntegrity.put("hashAlgorithm", "SHA-256");
        fileIntegrity.put("sha256", intOpt.map(DocumentIntegrityRecord::getSha256).orElse(document.getSha256Checksum()));
        fileIntegrity.put("fileSizeBytes", intOpt.map(DocumentIntegrityRecord::getFileSizeBytes).orElse(document.getFileSizeBytes()));
        fileIntegrity.put("detectedMediaType", intOpt.map(DocumentIntegrityRecord::getDetectedMediaType).orElse(document.getMimeType()));
        fileIntegrity.put("pageCount", intOpt.map(DocumentIntegrityRecord::getPageCount).orElse(document.getPageCount()));

        List<DuplicateMatch> duplicates = duplicateMatchRepository.findBySourceDocumentId(document.getId());
        fileIntegrity.put("exactDuplicateFound", duplicates.stream().anyMatch(d -> d.getMatchType().startsWith("EXACT_DUPLICATE")));
        fileIntegrity.put("duplicateMatches", duplicates);
        report.put("fileIntegrity", fileIntegrity);

        // 2. Verification Records (Registry, QR, Digital Signatures)
        List<VerificationRecord> verifications = verificationRecordRepository.findByDocumentId(document.getId());
        Map<String, Object> verifMap = new LinkedHashMap<>();
        for (VerificationRecord v : verifications) {
            verifMap.put(v.getVerificationType(), Map.of(
                "status", v.getVerificationStatus().name(),
                "source", v.getSourceIdentifier() != null ? v.getSourceIdentifier() : "N/A",
                "evidence", v.getComparisonEvidence() != null ? v.getComparisonEvidence() : "",
                "limitations", v.getLimitations() != null ? v.getLimitations() : "",
                "verifiedAt", v.getVerifiedAt().toString()
            ));
        }
        report.put("verifications", verifMap);

        // 3. Cross-Document Dossier Findings
        if (document.getApplicationId() != null) {
            List<CrossDocumentComparison> crossComparisons = comparisonRepository.findByApplicationId(document.getApplicationId());
            report.put("crossDocumentComparisons", crossComparisons);
        } else {
            report.put("crossDocumentComparisons", List.of());
        }

        // 4. Anomalies & Linked Evidence
        List<AnomalyRecord> anomalies = anomalyRecordRepository.findByDocumentId(document.getId());
        List<Map<String, Object>> anomalyList = new ArrayList<>();
        for (AnomalyRecord a : anomalies) {
            Map<String, Object> aMap = new LinkedHashMap<>();
            aMap.put("anomalyId", a.getId());
            aMap.put("type", a.getAnomalyType().name());
            aMap.put("severity", a.getSeverity().name());
            aMap.put("title", a.getTitle());
            aMap.put("description", a.getDescription());
            aMap.put("whyItMatters", a.getWhyItMatters());
            aMap.put("ruleId", a.getRuleId());
            aMap.put("detectorConfidence", a.getDetectorConfidence());
            aMap.put("reviewStatus", a.getReviewStatus().name());
            aMap.put("recommendedAction", a.getRecommendedAction());

            List<EvidenceRecord> evList = evidenceRecordRepository.findByAnomalyId(a.getId());
            aMap.put("evidence", evList);
            anomalyList.add(aMap);
        }
        report.put("anomalies", anomalyList);

        // 5. Overall Risk & Authenticity Assessment
        RiskAssessmentService.RiskAssessmentResult risk = riskAssessmentService.evaluateRisk(document);
        Map<String, Object> assessment = new LinkedHashMap<>();
        assessment.put("riskScore", risk.riskScore());
        assessment.put("riskPriority", risk.priorityLevel().name());
        assessment.put("criticalFindings", risk.criticalCount());
        assessment.put("highFindings", risk.highCount());
        assessment.put("mediumFindings", risk.mediumCount());
        assessment.put("authenticityStatus", document.getAuthenticityStatus());
        assessment.put("summary", risk.summaryRationale());
        assessment.put("manualReviewRequired", risk.manualReviewRequired());
        report.put("overallAssessment", assessment);

        return report;
    }
}
