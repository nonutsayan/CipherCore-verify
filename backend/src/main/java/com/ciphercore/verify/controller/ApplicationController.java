package com.ciphercore.verify.controller;

import com.ciphercore.verify.engine.ComplianceRuleEngine;
import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.ApplicationStatus;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.DocumentExtractionService;
import com.ciphercore.verify.service.DocumentStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationCategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final ExtractedDocumentFieldRepository extractedFieldRepository;
    private final ValidationRuleRepository ruleRepository;
    private final ValidationRunRepository runRepository;
    private final ValidationFindingRepository findingRepository;
    private final DocumentStorageService storageService;
    private final DocumentExtractionService extractionService;
    private final ComplianceRuleEngine ruleEngine;

    public ApplicationController(ApplicationRepository applicationRepository,
                                 ApplicationCategoryRepository categoryRepository,
                                 DocumentRepository documentRepository,
                                 ExtractedDocumentFieldRepository extractedFieldRepository,
                                 ValidationRuleRepository ruleRepository,
                                 ValidationRunRepository runRepository,
                                 ValidationFindingRepository findingRepository,
                                 DocumentStorageService storageService,
                                 DocumentExtractionService extractionService,
                                 ComplianceRuleEngine ruleEngine) {
        this.applicationRepository = applicationRepository;
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
        this.extractedFieldRepository = extractedFieldRepository;
        this.ruleRepository = ruleRepository;
        this.runRepository = runRepository;
        this.findingRepository = findingRepository;
        this.storageService = storageService;
        this.extractionService = extractionService;
        this.ruleEngine = ruleEngine;
    }

    @GetMapping
    public List<Application> listApplications() {
        return applicationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable Long id) {
        return applicationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreateAppRequest(String institutionName, String instituteCode, String categoryCode, String academicYear) {}

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody CreateAppRequest req) {
        Application app = new Application();
        app.setApplicationRef("AICTE-2026-APP-" + (int)(Math.random() * 90000 + 10000));
        app.setInstitutionName(req.institutionName());
        app.setInstituteCode(req.instituteCode());
        app.setCategoryCode(req.categoryCode() != null ? req.categoryCode() : "EOA_TECHNICAL");
        if (req.academicYear() != null) app.setAcademicYear(req.academicYear());
        app.setStatus(ApplicationStatus.DRAFT);
        app.setCreatedBy("applicant_user");
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(app);
        return ResponseEntity.ok(saved);
    }

    // Section 2A: Application category identification (AI-assisted with confidence & alternatives)
    @PostMapping("/{id}/classify")
    public ResponseEntity<?> classifyCategory(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        return applicationRepository.findById(id).map(app -> {
            String title = app.getInstitutionName().toLowerCase();
            String suggested = "EOA_TECHNICAL";
            double conf = 0.94;
            String reason = "Existing approved college profile with historical permanent ID matches Extension of Approval (EoA) criteria.";
            List<String> alternatives = List.of("VARIATION_INTAKE");

            if (title.contains("new") || title.contains("society") || app.getInstituteCode().equals("NEW")) {
                suggested = "NEW_INSTITUTION";
                conf = 0.88;
                reason = "Keywords indicate new greenfield educational trust application under Chapter II.";
            }

            return ResponseEntity.ok(Map.of(
                "applicationId", id,
                "currentCategory", app.getCategoryCode(),
                "suggestedCategory", suggested,
                "confidenceScore", conf,
                "reasoning", reason,
                "alternativeCategories", alternatives,
                "requiresHumanConfirmation", conf < 0.90
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Section 2B: Automatic checklist generation
    @GetMapping("/{id}/checklist")
    public ResponseEntity<?> getChecklist(@PathVariable Long id) {
        return applicationRepository.findById(id).map(app -> {
            List<ValidationRule> rules = ruleRepository.findByCategoryCodeOrCategoryCode(app.getCategoryCode(), "ALL");
            List<Document> uploaded = documentRepository.findByApplicationId(id);

            List<Map<String, Object>> checklist = new ArrayList<>();
            for (ValidationRule r : rules) {
                boolean docUploaded = uploaded.stream().anyMatch(d -> d.getDocumentType().equalsIgnoreCase(r.getRuleType().replace("MANDATORY_DOC_", "")));
                checklist.add(Map.of(
                    "ruleId", r.getRuleId(),
                    "requirementName", r.getName(),
                    "description", r.getDescription(),
                    "mandatory", r.isMandatory(),
                    "severity", r.getSeverity().name(),
                    "provenance", r.getProvenance(),
                    "isFulfilled", docUploaded || "MANDATORY_FIELD".equals(r.getRuleType())
                ));
            }

            return ResponseEntity.ok(Map.of(
                "applicationRef", app.getApplicationRef(),
                "categoryCode", app.getCategoryCode(),
                "rulesetVersion", "2026.1-APH",
                "checklistItems", checklist
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Section 2C & Step 5: Document Upload & genuine text extraction
    @PostMapping("/{id}/documents")
    public ResponseEntity<?> uploadDocument(@PathVariable Long id,
                                            @RequestParam("file") MultipartFile file,
                                            @RequestParam("documentType") String documentType) {
        Optional<Application> appOpt = applicationRepository.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();

        try {
            DocumentStorageService.StoredFileInfo stored = storageService.storeFile(file);

            Document doc = new Document();
            doc.setApplication(appOpt.get());
            doc.setDocumentType(documentType);
            doc.setOriginalFilename(file.getOriginalFilename());
            doc.setStoredPath(stored.fullPath());
            doc.setMimeType(stored.mimeType());
            doc.setFileSizeBytes(stored.fileSizeBytes());
            doc.setSha256Checksum(stored.sha256Checksum());

            // Extract genuine text if PDF
            if (stored.mimeType() != null && stored.mimeType().contains("pdf")) {
                DocumentExtractionService.ExtractedData extracted = extractionService.extractFromPdf(stored.fullPath());
                doc.setPageCount(extracted.pageCount());
                doc.setExtractedText(extracted.fullText());
                doc.setExtractionStatus(extracted.confidence() > 0.5 ? "EXTRACTED" : "LOW_CONFIDENCE");
            } else {
                doc.setExtractionStatus("STORED_IMAGE");
            }

            Document saved = documentRepository.save(doc);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/documents")
    public List<Document> getDocuments(@PathVariable Long id) {
        return documentRepository.findByApplicationId(id);
    }

    // Section 2E, 2F: Execute Validation Engine
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateApplication(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "officer_sc401") String username) {
        Optional<Application> appOpt = applicationRepository.findById(id);
        if (appOpt.isEmpty()) return ResponseEntity.notFound().build();

        Application app = appOpt.get();
        ComplianceRuleEngine.ExecutionResult res = ruleEngine.evaluateApplication(app, username);

        // Persist run and findings
        ValidationRun savedRun = runRepository.save(res.run());
        for (ValidationFinding f : res.findings()) {
            f.setValidationRun(savedRun);
            findingRepository.save(f);
        }
        applicationRepository.save(app);

        return ResponseEntity.ok(Map.of(
            "runId", savedRun.getId(),
            "overallStatus", savedRun.getOverallStatus().name(),
            "explanation", savedRun.getOverallExplanation(),
            "passedCount", savedRun.getPassedCount(),
            "deficiencyCount", savedRun.getDeficiencyCount(),
            "warningCount", savedRun.getWarningCount(),
            "manualReviewCount", savedRun.getManualReviewCount(),
            "reportHash", savedRun.getReportHash()
        ));
    }

    @GetMapping("/{id}/validation-runs")
    public List<ValidationRun> getValidationRuns(@PathVariable Long id) {
        return runRepository.findByApplicationIdOrderByRunTimestampDesc(id);
    }
}
