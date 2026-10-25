package com.ciphercore.verify.config;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ciphercore.verify.service.RegulatoryRuleEngineService;
import com.ciphercore.verify.service.InfrastructureAssessmentService;
import java.time.LocalDateTime;

/**
 * Section 16 & 28: Clearly labelled demonstration mode data seeder.
 * Seeds initial users, regulatory compliance categories, validation rules,
 * sample applications, and forensic anomaly intelligence records.
 */
@Component
public class DataSeedInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ApplicationCategoryRepository categoryRepository;
    private final ValidationRuleRepository ruleRepository;
    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentIntegrityRecordRepository integrityRecordRepository;
    private final DocumentAnalysisRunRepository analysisRunRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final DuplicateMatchRepository duplicateMatchRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final AuthoritySourceRepository authoritySourceRepository;
    private final VerificationResultRepository verificationResultRepository;
    private final FieldComparisonRepository fieldComparisonRepository;
    private final VerificationEvidenceRepository verificationEvidenceRepository;
    private final VerificationJobRepository verificationJobRepository;
    private final RegulatorySourceRepository regulatorySourceRepository;
    private final RegulatoryRuleRepository regulatoryRuleRepository;
    private final RegulatoryRuleVersionRepository regulatoryRuleVersionRepository;
    private final InfrastructureAssetRepository infrastructureAssetRepository;
    private final InfrastructureEvidenceRepository infrastructureEvidenceRepository;
    private final InfrastructureObservationRepository infrastructureObservationRepository;
    private final InfrastructureAnalysisJobRepository infrastructureAnalysisJobRepository;
    private final RegulatoryRuleEngineService regulatoryRuleEngineService;
    private final ActionItemRepository actionItemRepository;
    private final OfficialDecisionRepository officialDecisionRepository;
    private final NotificationRepository notificationRepository;
    private final javax.sql.DataSource dataSource;

    public DataSeedInitializer(UserRepository userRepository,
                               ApplicationCategoryRepository categoryRepository,
                               ValidationRuleRepository ruleRepository,
                               ApplicationRepository applicationRepository,
                               DocumentRepository documentRepository,
                               PasswordEncoder passwordEncoder,
                               DocumentIntegrityRecordRepository integrityRecordRepository,
                               DocumentAnalysisRunRepository analysisRunRepository,
                               AnomalyRecordRepository anomalyRecordRepository,
                               EvidenceRecordRepository evidenceRecordRepository,
                               DuplicateMatchRepository duplicateMatchRepository,
                               VerificationRecordRepository verificationRecordRepository,
                               AuthoritySourceRepository authoritySourceRepository,
                               VerificationResultRepository verificationResultRepository,
                               FieldComparisonRepository fieldComparisonRepository,
                               VerificationEvidenceRepository verificationEvidenceRepository,
                               VerificationJobRepository verificationJobRepository,
                               RegulatorySourceRepository regulatorySourceRepository,
                               RegulatoryRuleRepository regulatoryRuleRepository,
                               RegulatoryRuleVersionRepository regulatoryRuleVersionRepository,
                               InfrastructureAssetRepository infrastructureAssetRepository,
                               InfrastructureEvidenceRepository infrastructureEvidenceRepository,
                               InfrastructureObservationRepository infrastructureObservationRepository,
                               InfrastructureAnalysisJobRepository infrastructureAnalysisJobRepository,
                               RegulatoryRuleEngineService regulatoryRuleEngineService,
                               ActionItemRepository actionItemRepository,
                               OfficialDecisionRepository officialDecisionRepository,
                               NotificationRepository notificationRepository,
                               javax.sql.DataSource dataSource) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.ruleRepository = ruleRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
        this.integrityRecordRepository = integrityRecordRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.duplicateMatchRepository = duplicateMatchRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.authoritySourceRepository = authoritySourceRepository;
        this.verificationResultRepository = verificationResultRepository;
        this.fieldComparisonRepository = fieldComparisonRepository;
        this.verificationEvidenceRepository = verificationEvidenceRepository;
        this.verificationJobRepository = verificationJobRepository;
        this.regulatorySourceRepository = regulatorySourceRepository;
        this.regulatoryRuleRepository = regulatoryRuleRepository;
        this.regulatoryRuleVersionRepository = regulatoryRuleVersionRepository;
        this.infrastructureAssetRepository = infrastructureAssetRepository;
        this.infrastructureEvidenceRepository = infrastructureEvidenceRepository;
        this.infrastructureObservationRepository = infrastructureObservationRepository;
        this.infrastructureAnalysisJobRepository = infrastructureAnalysisJobRepository;
        this.regulatoryRuleEngineService = regulatoryRuleEngineService;
        this.actionItemRepository = actionItemRepository;
        this.officialDecisionRepository = officialDecisionRepository;
        this.notificationRepository = notificationRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        migrateUserSchemaIfNecessary();
        seedUsers();
        seedCategories();
        seedRules();
        seedSampleApplications();
        seedForensicsIfEmpty();
        seedAuthoritySourcesIfEmpty();
        seedAuthorityVerificationsIfEmpty();
        seedRegulatoryComplianceIfEmpty();
        seedDeliverable6DataIfEmpty();
    }

    private void migrateUserSchemaIfNecessary() {
        if (dataSource == null) return;
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            // Drop any legacy check constraints on the users table (e.g. check on role enum)
            try (java.sql.ResultSet rs = stmt.executeQuery(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE TABLE_NAME = 'USERS' AND CONSTRAINT_TYPE = 'CHECK'")) {
                java.util.List<String> toDrop = new java.util.ArrayList<>();
                while (rs.next()) {
                    toDrop.add(rs.getString("CONSTRAINT_NAME"));
                }
                for (String cName : toDrop) {
                    try {
                        stmt.execute("ALTER TABLE users DROP CONSTRAINT " + cName);
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            try {
                stmt.execute("ALTER TABLE users ALTER COLUMN role VARCHAR(64)");
            } catch (Exception ignored) {}

            try {
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INT DEFAULT 0");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until TIMESTAMP");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP");
                stmt.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS institution_id VARCHAR(64)");
            } catch (Exception ignored) {}
        } catch (Exception ignored) {}
    }

    private void seedUsers() {
        seedUserIfNotExists("admin", "admin123", "AICTE System Administrator", "admin@aicte-india.org", UserRole.ROLE_SYSTEM_ADMINISTRATOR, "AICTE Headquarters", "AICTE-HQ");
        seedUserIfNotExists("officer_sc401", "officer123", "Dr. Rajesh Sharma (Scrutiny Member)", "r.sharma@aicte-india.org", UserRole.ROLE_REGULATORY_OFFICER, "Western Regional Office", "WRO-MUM");
        seedUserIfNotExists("senior_reviewer", "senior123", "Dr. Meenakshi Sundaram (Regional Director)", "m.sundaram@aicte-india.org", UserRole.ROLE_SENIOR_REVIEWER, "AICTE Central Approval Bureau", "CAB-DEL");
        seedUserIfNotExists("auditor", "auditor123", "Suresh Menon (Independent Vigilance Officer)", "s.menon@gov.in", UserRole.ROLE_AUDITOR, "Ministry of Education Vigilance Unit", "VIG-01");
        seedUserIfNotExists("applicant_apex", "apex123", "Principal Apex Tech", "principal@apextech.edu.in", UserRole.ROLE_INSTITUTION_APPLICANT, "Apex Institute of Technology", "INST-APEX-001");
        seedUserIfNotExists("applicant_bharat", "bharat123", "Registrar Bharat Engg", "registrar@bharatengg.edu.in", UserRole.ROLE_INSTITUTION_APPLICANT, "Bharat Engineering College", "INST-BHARAT-002");
        seedUserIfNotExists("service_ai", "ai_service_secret_token", "CipherCore Autonomous AI Engine", "ai-engine@ciphercore.internal", UserRole.ROLE_SERVICE_AI, "CipherCore AI Background Services", "AI-SVC");
        seedUserIfNotExists("reviewer_01", "reviewer123", "Priya Desai (Legal Reviewer)", "p.desai@aicte-india.org", UserRole.ROLE_REVIEWER, "AICTE Scrutiny Committee", "WRO-REV");
    }

    private void seedUserIfNotExists(String username, String rawPassword, String fullName, String email,
                                    UserRole role, String institutionName, String institutionId) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(new User(username, passwordEncoder.encode(rawPassword), fullName, email, role, institutionName, institutionId));
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        categoryRepository.save(new ApplicationCategory(
            "EOA_TECHNICAL",
            "Extension of Approval (EoA) - Existing Institutions",
            "Annual scrutiny of existing technical colleges for continuation of approval under Chapter I of AICTE Approval Process Handbook.",
            "[\"extension\", \"eoa\", \"continuation\", \"existing\"]"
        ));

        categoryRepository.save(new ApplicationCategory(
            "VARIATION_INTAKE",
            "Increase in Intake / Additional Course",
            "Applications requesting expansion of student capacity or launch of new emerging tech branches (AI, Data Science, Cyber Security).",
            "[\"increase\", \"intake\", \"additional\", \"new branch\"]"
        ));

        categoryRepository.save(new ApplicationCategory(
            "NEW_INSTITUTION",
            "Setting up New Technical Institution",
            "Statutory verification for greenfield technical institutions including land conversion, structural safety, and endowment funds.",
            "[\"new institute\", \"greenfield\", \"establishment\", \"society\"]"
        ));
    }

    private void seedRules() {
        if (ruleRepository.count() > 0) return;

        ValidationRule r1 = new ValidationRule();
        r1.setRuleId("RULE_MANDATORY_INSTITUTE_NAME");
        r1.setName("Mandatory Institution Legal Title Verification");
        r1.setDescription("Verification that legal name of the applicant institution is non-empty and formatted according to trust deed.");
        r1.setCategoryCode("ALL");
        r1.setRuleType("MANDATORY_FIELD");
        r1.setSeverity(Severity.CRITICAL);
        r1.setMandatory(true);
        r1.setActive(true);
        r1.setProvenance("AICTE APH 2024-27 Clause 2.1");
        ruleRepository.save(r1);

        ValidationRule r2 = new ValidationRule();
        r2.setRuleId("RULE_MANDATORY_INSTITUTE_CODE");
        r2.setName("AICTE Permanent Institute ID Validation");
        r2.setDescription("Validates permanent institute identifier format against AICTE Central Master Database.");
        r2.setCategoryCode("ALL");
        r2.setRuleType("MANDATORY_FIELD");
        r2.setSeverity(Severity.CRITICAL);
        r2.setMandatory(true);
        r2.setActive(true);
        r2.setProvenance("AICTE APH 2024-27 Clause 2.2");
        ruleRepository.save(r2);

        ValidationRule r3 = new ValidationRule();
        r3.setRuleId("RULE_MANDATORY_FIRE_NOC");
        r3.setName("Valid Fire Safety Certificate (NOC) Compliance");
        r3.setDescription("Mandates presence of an unexpired Fire Safety Clearance Certificate issued by the competent State Fire Directorate.");
        r3.setCategoryCode("ALL");
        r3.setRuleType("MANDATORY_DOC_FIRE_NOC");
        r3.setSeverity(Severity.CRITICAL);
        r3.setMandatory(true);
        r3.setActive(true);
        r3.setProvenance("AICTE APH 2024-27 Clause 4.1.2");
        ruleRepository.save(r3);

        ValidationRule r4 = new ValidationRule();
        r4.setRuleId("RULE_RECORD_CROSS_CHECK");
        r4.setName("Institutional Record Reconciliation with AICTE Master Registry");
        r4.setDescription("Compares claimed student intake, past deficiency status, and affiliation codes against authorized AICTE central registry records.");
        r4.setCategoryCode("ALL");
        r4.setRuleType("RECORD_CROSS_CHECK");
        r4.setSeverity(Severity.CRITICAL);
        r4.setMandatory(true);
        r4.setActive(true);
        r4.setProvenance("AICTE APH 2024-27 Clause 3.4");
        ruleRepository.save(r4);

        ValidationRule r5 = new ValidationRule();
        r5.setRuleId("RULE_DUPLICATE_CHECKSUM_CHECK");
        r5.setName("Document Cryptographic Integrity & Anti-Duplicate Check");
        r5.setDescription("Calculates SHA-256 hash fingerprints to identify duplicate files, reused affidavits, and metadata anomalies across institutions.");
        r5.setCategoryCode("ALL");
        r5.setRuleType("DUPLICATE_CHECKSUM_CHECK");
        r5.setSeverity(Severity.MAJOR);
        r5.setMandatory(false);
        r5.setActive(true);
        r5.setProvenance("AICTE Digital Scrutiny Directives 2024");
        ruleRepository.save(r5);
    }

    private void seedSampleApplications() {
        if (applicationRepository.count() > 0) return;

        // Case 1: Apex Institute (Compliant)
        Application app1 = new Application();
        app1.setApplicationRef("AICTE-2025-EOA-98104");
        app1.setInstitutionName("Apex Institute of Technology");
        app1.setInstituteCode("1-441829");
        app1.setCategoryCode("EOA_TECHNICAL");
        app1.setStatus(ApplicationStatus.SUBMITTED);
        app1.setOverallValidationStatus(FindingStatus.PASS);
        app1.setOverallSummaryReason("All documentary and infrastructure requirements verified successfully.");
        app1.setCreatedBy("applicant_apex");
        app1.setSubmissionDate(LocalDateTime.now().minusDays(2));
        applicationRepository.save(app1);

        // Document for App1
        Document doc1 = new Document();
        doc1.setApplication(app1);
        doc1.setDocumentType("FIRE_NOC");
        doc1.setOriginalFilename("fire_noc_apex_valid.pdf");
        doc1.setStoredPath("./uploads/sample_fire_valid.pdf");
        doc1.setMimeType("application/pdf");
        doc1.setFileSizeBytes(142000);
        doc1.setPageCount(2);
        doc1.setSha256Checksum("9a2f1b4c8d3e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a");
        doc1.setExtractionStatus("EXTRACTED");
        doc1.setExtractedText("GOVERNMENT OF MAHARASHTRA DIRECTORATE OF FIRE SERVICES NOC REF FS/2023/9812 VALID TILL 31-DEC-2026 OCCUPANCY EDUCATIONAL");
        documentRepository.save(doc1);

        // Forensic Integrity Record for Doc1
        DocumentIntegrityRecord ir1 = new DocumentIntegrityRecord(
            doc1.getId(),
            doc1.getSha256Checksum(),
            142000L,
            "application/pdf",
            2,
            "VALID"
        );
        integrityRecordRepository.save(ir1);

        // Forensic Run for Doc1
        DocumentAnalysisRun run1 = new DocumentAnalysisRun(doc1.getId(), app1.getId());
        run1.setProcessingStatus("COMPLETED");
        run1.setCompletedAt(LocalDateTime.now().minusHours(3));
        analysisRunRepository.save(run1);

        AnomalyRecord a1 = new AnomalyRecord();
        a1.setDocumentId(doc1.getId());
        a1.setApplicationId(app1.getId());
        a1.setAnalysisRunId(run1.getId());
        a1.setAnomalyType(AnomalyType.DIGITAL_SIGNATURE_ISSUE);
        a1.setSeverity(AnomalySeverity.INFORMATIONAL);
        a1.setTitle("PKCS#7 Digital Signature Cryptographically Verified");
        a1.setDescription("Digital signature from Municipal Commissioner matches the authorized DSC on the National PKI Certificate Revocation List (CRL). Full ByteRange coverage verified.");
        a1.setWhyItMatters("Cryptographic assurance confirms that document bytes have remained unchanged since municipal issuance.");
        a1.setRecommendedAction("Retain digital signature verification proof in permanent audit archive.");
        a1.setRuleId("VERIF_SIG_01");
        a1.setDetectorConfidence(1.0);
        a1.setReviewStatus(AnomalyReviewStatus.OPEN);
        a1.setCreatedAt(LocalDateTime.now().minusHours(3));
        anomalyRecordRepository.save(a1);

        EvidenceRecord ev1 = new EvidenceRecord();
        ev1.setAnomaly(a1);
        ev1.setDocumentId(doc1.getId());
        ev1.setEvidenceType("SIGNATURE_VALIDATION_RESULT");
        ev1.setPageNumber(2);
        ev1.setSourceField("DIGITAL_SIGNATURE");
        ev1.setSourceSnippet("CN=COMMISSIONER FIRE SERVICES, O=GOVT OF MAHARASHTRA, C=IN");
        ev1.setMetadataJson("{\"subFilter\": \"adbe.pkcs7.detached\", \"byteRangeValid\": true}");
        ev1.setLimitations("Certificate validity confirmed against current CRL.");
        evidenceRecordRepository.save(ev1);

        // Verification Record for Doc1
        VerificationRecord vr1 = new VerificationRecord(
            doc1.getId(),
            "INSTITUTIONAL_REGISTRY",
            "State Directorate of Fire Services API",
            VerificationStatus.VERIFIED_MATCH,
            "Official registration FS/2023/9812 verified active through 31-DEC-2026.",
            "Official mock registry query returned 200 OK."
        );
        verificationRecordRepository.save(vr1);

        // Case 2: Horizon College (Suspicious Stamp / Expired Fire NOC)
        Application app2 = new Application();
        app2.setApplicationRef("AICTE-2025-EOA-44219");
        app2.setInstitutionName("Horizon College of Engineering");
        app2.setInstituteCode("1-93214");
        app2.setCategoryCode("EOA_TECHNICAL");
        app2.setStatus(ApplicationStatus.UNDER_REVIEW);
        app2.setOverallValidationStatus(FindingStatus.DEFICIENCY);
        app2.setOverallSummaryReason("Expired Fire Safety NOC and Sanctioned intake discrepancy detected.");
        app2.setCreatedBy("applicant_apex");
        app2.setSubmissionDate(LocalDateTime.now().minusDays(1));
        applicationRepository.save(app2);

        // Document for App2 with duplicate hash prefix
        Document doc2 = new Document();
        doc2.setApplication(app2);
        doc2.setDocumentType("FIRE_NOC");
        doc2.setOriginalFilename("fire_noc_horizon_expired.pdf");
        doc2.setStoredPath("./uploads/sample_fire_expired.pdf");
        doc2.setMimeType("application/pdf");
        doc2.setFileSizeBytes(112000);
        doc2.setPageCount(1);
        doc2.setSha256Checksum("7a8f10b39c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f");
        doc2.setExtractionStatus("EXTRACTED");
        doc2.setExtractedText("MAHARASHTRA FIRE SERVICES NOC FS/2022/4412 EXPIRED VALID UP TO 31-07-2024 CRITICAL DEFICIENCY");
        documentRepository.save(doc2);

        // Forensic Integrity Record for Doc2
        DocumentIntegrityRecord ir2 = new DocumentIntegrityRecord(
            doc2.getId(),
            doc2.getSha256Checksum(),
            112000L,
            "application/pdf",
            1,
            "CORRUPTED"
        );
        integrityRecordRepository.save(ir2);

        // Duplicate match for Doc2
        DuplicateMatch dup2 = new DuplicateMatch(
            doc2.getId(),
            doc1.getId(),
            app2.getId(),
            app1.getId(),
            "NEAR_DUPLICATE",
            "TOKEN_JACCARD_SIMILARITY",
            0.88,
            "High token Jaccard similarity (0.88) with previous submission affidavit_trust_2021.pdf",
            "Verify original stamp provenance across submissions"
        );
        duplicateMatchRepository.save(dup2);

        // Forensic Run for Doc2
        DocumentAnalysisRun run2 = new DocumentAnalysisRun(doc2.getId(), app2.getId());
        run2.setProcessingStatus("COMPLETED");
        run2.setCompletedAt(LocalDateTime.now().minusHours(2));
        analysisRunRepository.save(run2);

        // Anomaly 1: Visual Stamp Splicing (CRITICAL)
        AnomalyRecord a21 = new AnomalyRecord();
        a21.setDocumentId(doc2.getId());
        a21.setApplicationId(app2.getId());
        a21.setAnalysisRunId(run2.getId());
        a21.setAnomalyType(AnomalyType.VISUAL_MANIPULATION_INDICATOR);
        a21.setSeverity(AnomalySeverity.CRITICAL);
        a21.setTitle("FLAG-01: Stamp Clone & Spliced Seal Boundary Detected");
        a21.setDescription("The revenue department round seal on Page 1 matches pixel-for-pixel (hash: #7a8f10b) with an affidavit submitted in 2021 by a separate institution. Splicing boundary residual at 94.2% detected around seal perimeter.");
        a21.setWhyItMatters("Cloned or spliced seals indicate document compositing from unauthorized external records.");
        a21.setRecommendedAction("Request physical scrutiny of original ink-stamped certificate from issuing Fire Directorate.");
        a21.setRuleId("ANOM_VISUAL_01");
        a21.setDetectorConfidence(0.94);
        a21.setReviewStatus(AnomalyReviewStatus.OPEN);
        a21.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a21);

        EvidenceRecord ev21 = new EvidenceRecord();
        ev21.setAnomaly(a21);
        ev21.setDocumentId(doc2.getId());
        ev21.setEvidenceType("VISUAL_DIFFERENCE");
        ev21.setPageNumber(1);
        ev21.setSourceField("OFFICIAL_SEAL");
        ev21.setSourceSnippet("GOVT. OF MAHARASHTRA · REVENUE & FOREST DEPARTMENT");
        ev21.setBoundingBoxJson("{\"x\": 0.65, \"y\": 0.72, \"w\": 0.22, \"h\": 0.18}");
        ev21.setMetadataJson("{\"elaResidual\": \"94.2%\", \"clonedHash\": \"#7a8f10b\", \"filter\": \"Multi-Scale High-Pass Filter + EXIF Inversion\"}");
        ev21.setLimitations("Visual ELA detects compression differences; physical inspection is recommended to corroborate.");
        evidenceRecordRepository.save(ev21);

        // Anomaly 2: Metadata Timestamp Inversion (HIGH)
        AnomalyRecord a22 = new AnomalyRecord();
        a22.setDocumentId(doc2.getId());
        a22.setApplicationId(app2.getId());
        a22.setAnalysisRunId(run2.getId());
        a22.setAnomalyType(AnomalyType.METADATA_INCONSISTENCY);
        a22.setSeverity(AnomalySeverity.HIGH);
        a22.setTitle("FLAG-02: Metadata Timestamp Anachronism");
        a22.setDescription("PDF creation metadata shows CreateDate: 2024-09-12 23:14, but certificate text claims issuance on 2023-01-10. Document authored 610 days after alleged date.");
        a22.setWhyItMatters("Significant lag between alleged issuance date and digital file authoring indicates retroactive document generation.");
        a22.setRecommendedAction("Verify issuance dispatch logs with the competent municipal authority.");
        a22.setRuleId("ANOM_META_02");
        a22.setDetectorConfidence(0.89);
        a22.setReviewStatus(AnomalyReviewStatus.OPEN);
        a22.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a22);

        EvidenceRecord ev22 = new EvidenceRecord();
        ev22.setAnomaly(a22);
        ev22.setDocumentId(doc2.getId());
        ev22.setEvidenceType("METADATA_OBSERVATION");
        ev22.setPageNumber(1);
        ev22.setSourceField("DOCUMENT_METADATA");
        ev22.setSourceSnippet("CreateDate: 2024-09-12T23:14:02Z | PrintedDate: 2023-01-10");
        ev22.setMetadataJson("{\"createDate\": \"2024-09-12T23:14:02Z\", \"statedDate\": \"2023-01-10\", \"discrepancyDays\": 610}");
        ev22.setLimitations("Scanned paper copies may have later creation dates if digitized legitimately.");
        evidenceRecordRepository.save(ev22);

        // Anomaly 3: Expired Document (HIGH)
        AnomalyRecord a23 = new AnomalyRecord();
        a23.setDocumentId(doc2.getId());
        a23.setApplicationId(app2.getId());
        a23.setAnalysisRunId(run2.getId());
        a23.setAnomalyType(AnomalyType.VALIDITY_CONFLICT);
        a23.setSeverity(AnomalySeverity.HIGH);
        a23.setTitle("FLAG-03: Statutory Validity Expired Prior to Submission");
        a23.setDescription("Fire safety NOC expired on 31-JUL-2024 (92 days prior to AICTE EoA submission deadline).");
        a23.setWhyItMatters("Mandatory statutory clearance is expired; non-compliant under AICTE Approval Process Handbook Clause 4.1.2.");
        a23.setRecommendedAction("Issue show-cause deficiency notice requiring valid renewal certificate.");
        a23.setRuleId("ANOM_DATE_01");
        a23.setDetectorConfidence(0.98);
        a23.setReviewStatus(AnomalyReviewStatus.OPEN);
        a23.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a23);

        EvidenceRecord ev23 = new EvidenceRecord();
        ev23.setAnomaly(a23);
        ev23.setDocumentId(doc2.getId());
        ev23.setEvidenceType("DATE_CONFLICT");
        ev23.setPageNumber(1);
        ev23.setSourceField("VALID_UPTO");
        ev23.setSourceSnippet("VALID UP TO: 31-JUL-2024 (CRITICAL: EXPIRED 92 DAYS AGO)");
        ev23.setMetadataJson("{\"expiryDate\": \"2024-07-31\", \"daysExpired\": 92}");
        ev23.setLimitations("Assumes standard Gregorian date formatting in document body.");
        evidenceRecordRepository.save(ev23);

        // Anomaly 4: Cross-Document Entity Name Conflict (MEDIUM)
        AnomalyRecord a24 = new AnomalyRecord();
        a24.setDocumentId(doc2.getId());
        a24.setApplicationId(app2.getId());
        a24.setAnalysisRunId(run2.getId());
        a24.setAnomalyType(AnomalyType.CROSS_DOCUMENT_CONTRADICTION);
        a24.setSeverity(AnomalySeverity.MEDIUM);
        a24.setTitle("FLAG-04: Cross-Document Entity Legal Name Divergence");
        a24.setDescription("Certificate lists applicant as 'Horizon Educational Trust' whereas AICTE Application dossier is filed under 'Horizon College of Engineering'. Fuzzy similarity score: 0.61.");
        a24.setWhyItMatters("Mismatched entity names risk approval of non-affiliated facilities under third-party trust titles.");
        a24.setRecommendedAction("Request registered Trust resolution demonstrating clear institutional ownership.");
        a24.setRuleId("ANOM_CROSS_01");
        a24.setDetectorConfidence(0.85);
        a24.setReviewStatus(AnomalyReviewStatus.OPEN);
        a24.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a24);

        EvidenceRecord ev24 = new EvidenceRecord();
        ev24.setAnomaly(a24);
        ev24.setDocumentId(doc2.getId());
        ev24.setEvidenceType("CROSS_DOCUMENT_COMPARISON");
        ev24.setPageNumber(1);
        ev24.setSourceField("INSTITUTION_NAME");
        ev24.setSourceSnippet("ISSUED TO: Horizon Educational Trust");
        ev24.setMetadataJson("{\"docName\": \"Horizon Educational Trust\", \"appName\": \"Horizon College of Engineering\", \"jaroWinkler\": 0.61}");
        ev24.setLimitations("Trusts frequently manage colleges; requires verification of trust-to-college deed.");
        evidenceRecordRepository.save(ev24);

        // Verification Record for Doc2
        VerificationRecord vr2 = new VerificationRecord(
            doc2.getId(),
            "INSTITUTIONAL_REGISTRY",
            "State Directorate of Fire Services API",
            VerificationStatus.RECORD_CONFLICT,
            "API query returned: Record found but expired on 2024-07-31. No renewal application filed.",
            "Registry query confirmed expiration."
        );
        verificationRecordRepository.save(vr2);
    }

    private void seedForensicsIfEmpty() {
        if (anomalyRecordRepository.count() > 0) return;

        java.util.List<Document> all = documentRepository.findAll();
        if (all.isEmpty()) return;

        Document doc1 = null;
        Document doc2 = null;
        for (Document d : all) {
            if (d.getId().equals(1L)) doc1 = d;
            if (d.getId().equals(2L)) doc2 = d;
        }
        if (doc1 == null) doc1 = all.get(0);
        if (doc2 == null) doc2 = all.size() > 1 ? all.get(1) : doc1;

        Long app1Id = doc1.getApplication() != null ? doc1.getApplication().getId() : 1L;
        Long app2Id = doc2.getApplication() != null ? doc2.getApplication().getId() : 2L;

        // Doc1
        DocumentIntegrityRecord ir1 = new DocumentIntegrityRecord(
            doc1.getId(),
            doc1.getSha256Checksum() != null ? doc1.getSha256Checksum() : "9a2f1b4c8d3e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a",
            142000L,
            "application/pdf",
            2,
            "VALID"
        );
        integrityRecordRepository.save(ir1);

        DocumentAnalysisRun run1 = new DocumentAnalysisRun(doc1.getId(), app1Id);
        run1.setProcessingStatus("COMPLETED");
        run1.setCompletedAt(LocalDateTime.now().minusHours(3));
        analysisRunRepository.save(run1);

        AnomalyRecord a1 = new AnomalyRecord();
        a1.setDocumentId(doc1.getId());
        a1.setApplicationId(app1Id);
        a1.setAnalysisRunId(run1.getId());
        a1.setAnomalyType(AnomalyType.DIGITAL_SIGNATURE_ISSUE);
        a1.setSeverity(AnomalySeverity.INFORMATIONAL);
        a1.setTitle("PKCS#7 Digital Signature Cryptographically Verified");
        a1.setDescription("Digital signature from Municipal Commissioner matches the authorized DSC on the National PKI Certificate Revocation List (CRL). Full ByteRange coverage verified.");
        a1.setWhyItMatters("Cryptographic assurance confirms that document bytes have remained unchanged since municipal issuance.");
        a1.setRecommendedAction("Retain digital signature verification proof in permanent audit archive.");
        a1.setRuleId("VERIF_SIG_01");
        a1.setDetectorConfidence(1.0);
        a1.setReviewStatus(AnomalyReviewStatus.OPEN);
        a1.setCreatedAt(LocalDateTime.now().minusHours(3));
        anomalyRecordRepository.save(a1);

        EvidenceRecord ev1 = new EvidenceRecord();
        ev1.setAnomaly(a1);
        ev1.setDocumentId(doc1.getId());
        ev1.setEvidenceType("SIGNATURE_VALIDATION_RESULT");
        ev1.setPageNumber(2);
        ev1.setSourceField("DIGITAL_SIGNATURE");
        ev1.setSourceSnippet("CN=COMMISSIONER FIRE SERVICES, O=GOVT OF MAHARASHTRA, C=IN");
        ev1.setMetadataJson("{\"subFilter\": \"adbe.pkcs7.detached\", \"byteRangeValid\": true}");
        ev1.setLimitations("Certificate validity confirmed against current CRL.");
        evidenceRecordRepository.save(ev1);

        // Doc2
        DocumentIntegrityRecord ir2 = new DocumentIntegrityRecord(
            doc2.getId(),
            doc2.getSha256Checksum() != null ? doc2.getSha256Checksum() : "7a8f10b39c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f",
            112000L,
            "application/pdf",
            1,
            "CORRUPTED"
        );
        integrityRecordRepository.save(ir2);

        DuplicateMatch dup2 = new DuplicateMatch(
            doc2.getId(),
            doc1.getId(),
            app2Id,
            app1Id,
            "NEAR_DUPLICATE",
            "TOKEN_JACCARD_SIMILARITY",
            0.88,
            "High token Jaccard similarity (0.88) with previous submission affidavit_trust_2021.pdf",
            "Verify original stamp provenance across submissions"
        );
        duplicateMatchRepository.save(dup2);

        DocumentAnalysisRun run2 = new DocumentAnalysisRun(doc2.getId(), app2Id);
        run2.setProcessingStatus("COMPLETED");
        run2.setCompletedAt(LocalDateTime.now().minusHours(2));
        analysisRunRepository.save(run2);

        // Anomaly 1: Visual Stamp Splicing (CRITICAL)
        AnomalyRecord a21 = new AnomalyRecord();
        a21.setDocumentId(doc2.getId());
        a21.setApplicationId(app2Id);
        a21.setAnalysisRunId(run2.getId());
        a21.setAnomalyType(AnomalyType.VISUAL_MANIPULATION_INDICATOR);
        a21.setSeverity(AnomalySeverity.CRITICAL);
        a21.setTitle("FLAG-01: Stamp Clone & Spliced Seal Boundary Detected");
        a21.setDescription("The revenue department round seal on Page 1 matches pixel-for-pixel (hash: #7a8f10b) with an affidavit submitted in 2021 by a separate institution. Splicing boundary residual at 94.2% detected around seal perimeter.");
        a21.setWhyItMatters("Cloned or spliced seals indicate document compositing from unauthorized external records.");
        a21.setRecommendedAction("Request physical scrutiny of original ink-stamped certificate from issuing Fire Directorate.");
        a21.setRuleId("ANOM_VISUAL_01");
        a21.setDetectorConfidence(0.94);
        a21.setReviewStatus(AnomalyReviewStatus.OPEN);
        a21.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a21);

        EvidenceRecord ev21 = new EvidenceRecord();
        ev21.setAnomaly(a21);
        ev21.setDocumentId(doc2.getId());
        ev21.setEvidenceType("VISUAL_DIFFERENCE");
        ev21.setPageNumber(1);
        ev21.setSourceField("OFFICIAL_SEAL");
        ev21.setSourceSnippet("GOVT. OF MAHARASHTRA · REVENUE & FOREST DEPARTMENT");
        ev21.setBoundingBoxJson("{\"x\": 0.65, \"y\": 0.72, \"w\": 0.22, \"h\": 0.18}");
        ev21.setMetadataJson("{\"elaResidual\": \"94.2%\", \"clonedHash\": \"#7a8f10b\", \"filter\": \"Multi-Scale High-Pass Filter + EXIF Inversion\"}");
        ev21.setLimitations("Visual ELA detects compression differences; physical inspection is recommended to corroborate.");
        evidenceRecordRepository.save(ev21);

        // Anomaly 2: Metadata Timestamp Inversion (HIGH)
        AnomalyRecord a22 = new AnomalyRecord();
        a22.setDocumentId(doc2.getId());
        a22.setApplicationId(app2Id);
        a22.setAnalysisRunId(run2.getId());
        a22.setAnomalyType(AnomalyType.METADATA_INCONSISTENCY);
        a22.setSeverity(AnomalySeverity.HIGH);
        a22.setTitle("FLAG-02: Metadata Timestamp Anachronism");
        a22.setDescription("PDF creation metadata shows CreateDate: 2024-09-12 23:14, but certificate text claims issuance on 2023-01-10. Document authored 610 days after alleged date.");
        a22.setWhyItMatters("Significant lag between alleged issuance date and digital file authoring indicates retroactive document generation.");
        a22.setRecommendedAction("Verify issuance dispatch logs with the competent municipal authority.");
        a22.setRuleId("ANOM_META_02");
        a22.setDetectorConfidence(0.89);
        a22.setReviewStatus(AnomalyReviewStatus.OPEN);
        a22.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a22);

        EvidenceRecord ev22 = new EvidenceRecord();
        ev22.setAnomaly(a22);
        ev22.setDocumentId(doc2.getId());
        ev22.setEvidenceType("METADATA_OBSERVATION");
        ev22.setPageNumber(1);
        ev22.setSourceField("DOCUMENT_METADATA");
        ev22.setSourceSnippet("CreateDate: 2024-09-12T23:14:02Z | PrintedDate: 2023-01-10");
        ev22.setMetadataJson("{\"createDate\": \"2024-09-12T23:14:02Z\", \"statedDate\": \"2023-01-10\", \"discrepancyDays\": 610}");
        ev22.setLimitations("Scanned paper copies may have later creation dates if digitized legitimately.");
        evidenceRecordRepository.save(ev22);

        // Anomaly 3: Expired Document (HIGH)
        AnomalyRecord a23 = new AnomalyRecord();
        a23.setDocumentId(doc2.getId());
        a23.setApplicationId(app2Id);
        a23.setAnalysisRunId(run2.getId());
        a23.setAnomalyType(AnomalyType.VALIDITY_CONFLICT);
        a23.setSeverity(AnomalySeverity.HIGH);
        a23.setTitle("FLAG-03: Statutory Validity Expired Prior to Submission");
        a23.setDescription("Fire safety NOC expired on 31-JUL-2024 (92 days prior to AICTE EoA submission deadline).");
        a23.setWhyItMatters("Mandatory statutory clearance is expired; non-compliant under AICTE Approval Process Handbook Clause 4.1.2.");
        a23.setRecommendedAction("Issue show-cause deficiency notice requiring valid renewal certificate.");
        a23.setRuleId("ANOM_DATE_01");
        a23.setDetectorConfidence(0.98);
        a23.setReviewStatus(AnomalyReviewStatus.OPEN);
        a23.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a23);

        EvidenceRecord ev23 = new EvidenceRecord();
        ev23.setAnomaly(a23);
        ev23.setDocumentId(doc2.getId());
        ev23.setEvidenceType("DATE_CONFLICT");
        ev23.setPageNumber(1);
        ev23.setSourceField("VALID_UPTO");
        ev23.setSourceSnippet("VALID UP TO: 31-JUL-2024 (CRITICAL: EXPIRED 92 DAYS AGO)");
        ev23.setMetadataJson("{\"expiryDate\": \"2024-07-31\", \"daysExpired\": 92}");
        ev23.setLimitations("Assumes standard Gregorian date formatting in document body.");
        evidenceRecordRepository.save(ev23);

        // Anomaly 4: Cross-Document Entity Name Conflict (MEDIUM)
        AnomalyRecord a24 = new AnomalyRecord();
        a24.setDocumentId(doc2.getId());
        a24.setApplicationId(app2Id);
        a24.setAnalysisRunId(run2.getId());
        a24.setAnomalyType(AnomalyType.CROSS_DOCUMENT_CONTRADICTION);
        a24.setSeverity(AnomalySeverity.MEDIUM);
        a24.setTitle("FLAG-04: Cross-Document Entity Legal Name Divergence");
        a24.setDescription("Certificate lists applicant as 'Horizon Educational Trust' whereas AICTE Application dossier is filed under 'Horizon College of Engineering'. Fuzzy similarity score: 0.61.");
        a24.setWhyItMatters("Mismatched entity names risk approval of non-affiliated facilities under third-party trust titles.");
        a24.setRecommendedAction("Request registered Trust resolution demonstrating clear institutional ownership.");
        a24.setRuleId("ANOM_CROSS_01");
        a24.setDetectorConfidence(0.85);
        a24.setReviewStatus(AnomalyReviewStatus.OPEN);
        a24.setCreatedAt(LocalDateTime.now().minusHours(2));
        anomalyRecordRepository.save(a24);

        EvidenceRecord ev24 = new EvidenceRecord();
        ev24.setAnomaly(a24);
        ev24.setDocumentId(doc2.getId());
        ev24.setEvidenceType("CROSS_DOCUMENT_COMPARISON");
        ev24.setPageNumber(1);
        ev24.setSourceField("INSTITUTION_NAME");
        ev24.setSourceSnippet("ISSUED TO: Horizon Educational Trust");
        ev24.setMetadataJson("{\"docName\": \"Horizon Educational Trust\", \"appName\": \"Horizon College of Engineering\", \"jaroWinkler\": 0.61}");
        ev24.setLimitations("Trusts frequently manage colleges; requires verification of trust-to-college deed.");
        evidenceRecordRepository.save(ev24);
    }

    private void seedAuthoritySourcesIfEmpty() {
        if (authoritySourceRepository.count() > 0) return;

        AuthoritySource s1 = new AuthoritySource(
            "SRC_AICTE_REGISTRY",
            "AICTE Central Approval Directory & EOA Portal",
            AuthoritySourceType.OFFICIAL_API,
            "https://api.aicte-india.org/registry/v2",
            "aicte-india.org,gov.in,nic.in",
            120,
            5000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s1.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s1);

        AuthoritySource s2 = new AuthoritySource(
            "SRC_MAHA_FIRE",
            "Maharashtra State Directorate of Fire Services Portal",
            AuthoritySourceType.GOVERNMENT_PORTAL,
            "https://mahafire.gov.in/noc-verify",
            "mahafire.gov.in,gov.in",
            60,
            6000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s2.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s2);

        AuthoritySource s3 = new AuthoritySource(
            "SRC_DIGILOCKER_LAND",
            "DigiLocker National Land Registry & Title Gateway",
            AuthoritySourceType.DIGITAL_REPOSITORY,
            "https://api.digilocker.gov.in/land/records",
            "digilocker.gov.in,gov.in",
            90,
            7000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s3.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s3);

        AuthoritySource s4 = new AuthoritySource(
            "SRC_MU_AFFILIATION",
            "University of Mumbai Academic Affiliation Council",
            AuthoritySourceType.INSTITUTIONAL_DATABASE,
            "https://mu.ac.in/affiliation/verify",
            "mu.ac.in,mumbaiuniversity.ac.in",
            60,
            5000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s4.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s4);

        AuthoritySource s5 = new AuthoritySource(
            "SRC_AICTE_FACULTY_PAN",
            "AICTE Central Faculty Registry & Duplicate PAN Portal",
            AuthoritySourceType.INSTITUTIONAL_DATABASE,
            "https://faculty.aicte-india.org/api/v1/verify",
            "aicte-india.org",
            30,
            4000,
            AuthoritySourceStatus.OFFLINE
        );
        s5.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s5);

        AuthoritySource s6 = new AuthoritySource(
            "SRC_QR_PORTAL",
            "ZXing Regulatory 2D QR Code Corroboration Engine",
            AuthoritySourceType.QR_BARCODE_LOOKUP,
            "https://aicte-india.org/qr/validate",
            "aicte-india.org,gov.in,nic.in",
            150,
            3000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s6.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s6);

        AuthoritySource s7 = new AuthoritySource(
            "SRC_PKI_CRL",
            "National CCA Root PKI & CRL Gateway (PKCS#7)",
            AuthoritySourceType.PKI_SIGNATURE,
            "https://ocsp.cca.gov.in",
            "cca.gov.in,gov.in",
            200,
            2500,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s7.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s7);

        AuthoritySource s8 = new AuthoritySource(
            "SRC_MANUAL_REVIEW",
            "APH Level-2 Scrutiny Committee Manual Fallback Queue",
            AuthoritySourceType.MANUAL_FALLBACK,
            "https://aph.aicte-india.org/scrutiny-portal",
            "aicte-india.org",
            100,
            1000,
            AuthoritySourceStatus.CONFIGURED_ACTIVE
        );
        s8.setEnvironment("SANDBOX");
        authoritySourceRepository.save(s8);
    }

    private void seedAuthorityVerificationsIfEmpty() {
        if (verificationResultRepository.count() > 0) return;

        java.util.List<Document> docs = documentRepository.findAll();
        if (docs.isEmpty()) return;

        Document doc1 = docs.get(0);
        Long app1Id = doc1.getApplicationId() != null ? doc1.getApplicationId() : 1L;

        // 1. Apex Institute EoA Verification against AICTE Central Registry (VERIFIED)
        VerificationResult res1 = new VerificationResult(doc1.getId(), app1Id, "SRC_AICTE_REGISTRY", AuthorityVerificationStatus.VERIFIED);
        res1.setSourceRecordId("AICTE-PERM-1-3324501");
        res1.setRecordMatchStatus("EXACT_MATCH");
        res1.setValidityStatus("ACTIVE_VALID");
        res1.setSignatureStatus("VALID_TRUSTED");
        res1.setQrStatus("VERIFIED");
        res1.setSummary("Institutional approval corroborated in AICTE National Registry. Permanent Institute ID 1-3324501 confirmed active for AY 2025-26.");
        res1.setLimitations("Corroborated against AICTE Central Registry snapshot dated 2026-10-01.");
        res1.setManualReviewRequired(false);
        res1.setGeneratedAt(LocalDateTime.now().minusHours(4));
        verificationResultRepository.save(res1);

        FieldComparison fc1 = new FieldComparison(res1, "permanentInstituteId", "1-3324501", "1-3324501", ComparisonMethod.EXACT, FieldComparisonStatus.MATCH, "Corroborated in AICTE National Institute Directory", 1.0);
        fieldComparisonRepository.save(fc1);
        FieldComparison fc2 = new FieldComparison(res1, "institutionName", "Apex Institute of Technology", "Apex Institute of Technology", ComparisonMethod.NORMALIZED_TEXT, FieldComparisonStatus.MATCH_AFTER_NORMALIZATION, "Legal denomination matches AICTE directory", 0.98);
        fieldComparisonRepository.save(fc2);
        FieldComparison fc3 = new FieldComparison(res1, "approvedIntake", "480", "480", ComparisonMethod.EXACT, FieldComparisonStatus.MATCH, "Total intake capacity matches sanctioned council quota across 4 departments", 1.0);
        fieldComparisonRepository.save(fc3);

        VerificationEvidence ev1 = new VerificationEvidence(res1, "OFFICIAL_API_PAYLOAD", "SRC_AICTE_REGISTRY", "AICTE-PERM-1-3324501", doc1.getId(), 1, "{\"status\":\"ACTIVE\",\"approvedYear\":\"2025-2026\",\"quota\":480}");
        verificationEvidenceRepository.save(ev1);

        // Document 2 (if present)
        if (docs.size() > 1) {
            Document doc2 = docs.get(1);
            Long app2Id = doc2.getApplicationId() != null ? doc2.getApplicationId() : 2L;

            // 2. Horizon College Fire Safety NOC against Maharashtra Fire Services (EXPIRED)
            VerificationResult res2 = new VerificationResult(doc2.getId(), app2Id, "SRC_MAHA_FIRE", AuthorityVerificationStatus.EXPIRED);
            res2.setSourceRecordId("NOC-MAHA-FS-2022-4412");
            res2.setRecordMatchStatus("EXACT_MATCH");
            res2.setValidityStatus("EXPIRED");
            res2.setSignatureStatus("VALID_TRUSTED");
            res2.setQrStatus("NOT_PRESENT");
            res2.setSummary("Fire Safety Clearance Certificate registration number FS/2022/4412 verified in Maharashtra Fire Directorate records, but statutory validity expired on 2024-07-31.");
            res2.setLimitations("State fire clearance database valid as of current sync. Requires valid renewal certificate.");
            res2.setManualReviewRequired(true);
            res2.setGeneratedAt(LocalDateTime.now().minusHours(2));
            verificationResultRepository.save(res2);

            FieldComparison fc21 = new FieldComparison(res2, "nocReferenceNumber", "FS/2022/4412", "FS/2022/4412", ComparisonMethod.EXACT, FieldComparisonStatus.MATCH, "Registration number verified in state archive", 1.0);
            fieldComparisonRepository.save(fc21);
            FieldComparison fc22 = new FieldComparison(res2, "validityEndDate", "31-07-2024", "2024-07-31", ComparisonMethod.DATE_CHRONOLOGY, FieldComparisonStatus.MISMATCH, "Statutory validity expired: Clearance expired on 2024-07-31. No renewal application on file", 1.0);
            fieldComparisonRepository.save(fc22);

            VerificationEvidence ev2 = new VerificationEvidence(res2, "PORTAL_SCREEN_DIGEST", "SRC_MAHA_FIRE", "NOC-MAHA-FS-2022-4412", doc2.getId(), 1, "{\"portalStatus\":\"EXPIRED\",\"validTill\":\"2024-07-31\",\"renewalApplied\":false}");
            verificationEvidenceRepository.save(ev2);

            // 3. Horizon College Affiliation Letter against Mumbai University (MISMATCH)
            VerificationResult res3 = new VerificationResult(doc2.getId(), app2Id, "SRC_MU_AFFILIATION", AuthorityVerificationStatus.MISMATCH);
            res3.setSourceRecordId("MU-AFFIL-2024-9102");
            res3.setRecordMatchStatus("DISCREPANCY_DETECTED");
            res3.setValidityStatus("ACTIVE_VALID");
            res3.setSignatureStatus("VALID_TRUSTED");
            res3.setQrStatus("NOT_PRESENT");
            res3.setSummary("Affiliation order corroboration detected intake capacity mismatch: Document claims 120 seats for B.Tech CS, but University Academic Council registry approves 60 seats.");
            res3.setLimitations("Corroborated against Mumbai University Affiliation Council notification dated 2024-06-15.");
            res3.setManualReviewRequired(true);
            res3.setGeneratedAt(LocalDateTime.now().minusHours(2));
            verificationResultRepository.save(res3);

            FieldComparison fc31 = new FieldComparison(res3, "approvedSeatsComputerScience", "120", "60", ComparisonMethod.EXACT, FieldComparisonStatus.MISMATCH, "Critical intake disparity: Document claims 120 seats; official university registry sanctioned 60 seats", 0.99);
            fieldComparisonRepository.save(fc31);
            FieldComparison fc32 = new FieldComparison(res3, "affiliationCategory", "TEMPORARY", "TEMPORARY", ComparisonMethod.EXACT, FieldComparisonStatus.MATCH, "Affiliation granted on annual provisional basis", 1.0);
            fieldComparisonRepository.save(fc32);

            VerificationEvidence ev3 = new VerificationEvidence(res3, "INSTITUTIONAL_DATABASE_RECORD", "SRC_MU_AFFILIATION", "MU-AFFIL-2024-9102", doc2.getId(), 1, "{\"academicCouncilResolution\":\"AC/2024/77\",\"sanctionedSeats\":{\"CS\":60,\"IT\":60}}");
            verificationEvidenceRepository.save(ev3);

            // 4. Horizon College Faculty Profile against Central Faculty Roster (SOURCE_UNAVAILABLE)
            VerificationResult res4 = new VerificationResult(doc2.getId(), app2Id, "SRC_AICTE_FACULTY_PAN", AuthorityVerificationStatus.SOURCE_UNAVAILABLE);
            res4.setSourceRecordId(null);
            res4.setRecordMatchStatus("UNKNOWN");
            res4.setValidityStatus("UNKNOWN");
            res4.setSignatureStatus("NOT_CHECKED");
            res4.setQrStatus("NOT_CHECKED");
            res4.setSummary("Central Faculty Roster gateway is temporarily undergoing scheduled maintenance (HTTP 504 Gateway Timeout). Verification cannot be performed at this moment.");
            res4.setLimitations("Upstream endpoint https://faculty.aicte-india.org unreachable. Failure is classified as SOURCE_UNAVAILABLE, not a document defect.");
            res4.setManualReviewRequired(false);
            res4.setGeneratedAt(LocalDateTime.now().minusHours(1));
            verificationResultRepository.save(res4);

            // 5. Land Title Deed against DigiLocker Land Registry (PENDING)
            VerificationResult res5 = new VerificationResult(doc2.getId(), app2Id, "SRC_DIGILOCKER_LAND", AuthorityVerificationStatus.PENDING);
            res5.setSourceRecordId("DL-MAHA-LAND-7821");
            res5.setRecordMatchStatus("PENDING_SYNC");
            res5.setValidityStatus("ACTIVE_VALID");
            res5.setSignatureStatus("NOT_CHECKED");
            res5.setQrStatus("NOT_PRESENT");
            res5.setSummary("District Revenue Land Title mutation verification submitted to DigiLocker Land Gateway. State mutation sync in progress.");
            res5.setLimitations("District revenue registry sync cycles occur every 12 hours.");
            res5.setManualReviewRequired(false);
            res5.setGeneratedAt(LocalDateTime.now().minusMinutes(30));
            verificationResultRepository.save(res5);

            FieldComparison fc51 = new FieldComparison(res5, "surveyGatNumber", "Gat No. 142/1", "Gat No. 142/1", ComparisonMethod.NORMALIZED_TEXT, FieldComparisonStatus.MATCH_AFTER_NORMALIZATION, "Survey plot boundary matches revenue record", 0.95);
            fieldComparisonRepository.save(fc51);
            FieldComparison fc52 = new FieldComparison(res5, "landAreaAcres", "5.2 Acres", "Awaiting District Verification", ComparisonMethod.CONTEXTUAL, FieldComparisonStatus.NEEDS_REVIEW, "Awaiting formal mutation entry endorsement", 0.60);
            fieldComparisonRepository.save(fc52);

            // 6. Non-digitized Stamp / Trust Deed against APH Manual Review (MANUAL_REVIEW)
            VerificationResult res6 = new VerificationResult(doc2.getId(), app2Id, "SRC_MANUAL_REVIEW", AuthorityVerificationStatus.MANUAL_REVIEW);
            res6.setSourceRecordId("SOP-APH-SCRUTINY-V4");
            res6.setRecordMatchStatus("PENDING_MANUAL_SCRUTINY");
            res6.setValidityStatus("PENDING_SCRUTINY");
            res6.setSignatureStatus("REQUIRES_OFFICER_SIGN_OFF");
            res6.setQrStatus("NOT_APPLICABLE");
            res6.setSummary("Physical seal and non-digitized sub-registrar endorsement detected on document. Assigned to Level-2 Scrutiny Committee for manual officer corroboration.");
            res6.setLimitations("Automated digital corroboration unavailable for physical sub-registrar book registrations prior to 2018.");
            res6.setManualReviewRequired(true);
            res6.setGeneratedAt(LocalDateTime.now().minusMinutes(15));
            verificationResultRepository.save(res6);

            FieldComparison fc61 = new FieldComparison(res6, "physicalSealInspection", "Sub-Registrar District Thane", "Manual Registry Roster", ComparisonMethod.CONTEXTUAL, FieldComparisonStatus.NEEDS_REVIEW, "Requires physical scrutiny of seal embossment and watermarked judicial stamp paper", 0.50);
            fieldComparisonRepository.save(fc61);
        }
    }

    private void seedRegulatoryComplianceIfEmpty() {
        if (regulatorySourceRepository.count() > 0) return;

        // 1. Regulatory Sources
        RegulatorySource s1 = new RegulatorySource();
        s1.setSourceCode("SRC_AICTE_APH_2024_27");
        s1.setSourceName("AICTE Approval Process Handbook (APH 2024-2027)");
        s1.setIssuingAuthority("All India Council for Technical Education");
        s1.setDocumentReference("F.No. AICTE/AB/APH/2024-27");
        s1.setOfficialUrl("https://www.aicte-india.org/bulletins/approval-process-handbook");
        s1.setPublicationDate(java.time.LocalDate.of(2024, 1, 15));
        s1.setEffectiveFrom(java.time.LocalDate.of(2024, 4, 1));
        s1.setEffectiveTo(java.time.LocalDate.of(2027, 3, 31));
        s1.setApplicableJurisdiction("PAN_INDIA");
        s1.setSourceVerificationState(SourceVerificationState.VERIFIED);
        s1.setSha256Checksum("9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f9a8b");
        s1.setSourceDescription("Master statutory handbook governing approval norms, land norms, faculty staffing, and infrastructure guidelines for technical education institutions.");
        s1 = regulatorySourceRepository.save(s1);

        RegulatorySource s2 = new RegulatorySource();
        s2.setSourceCode("SRC_AICTE_FSR_NOTIF");
        s2.setSourceName("AICTE Gazette Notification on Faculty Cadre & Staffing Ratios");
        s2.setIssuingAuthority("AICTE");
        s2.setDocumentReference("F.No. 61-1/RIFD/7th CPC/2016-17");
        s2.setOfficialUrl("https://www.aicte-india.org/sites/default/files/Gazette_7th_CPC.pdf");
        s2.setPublicationDate(java.time.LocalDate.of(2019, 3, 1));
        s2.setEffectiveFrom(java.time.LocalDate.of(2019, 3, 1));
        s2.setApplicableJurisdiction("PAN_INDIA");
        s2.setSourceVerificationState(SourceVerificationState.VERIFIED);
        s2.setSourceDescription("Stipulates mandatory Faculty-to-Student Ratios (1:15 UG Engineering, 1:20 PG) and cadre proportions.");
        regulatorySourceRepository.save(s2);

        RegulatorySource s3 = new RegulatorySource();
        s3.setSourceCode("SRC_BIS_NBC_2016");
        s3.setSourceName("National Building Code of India (NBC 2016) — Part 3 & 4");
        s3.setIssuingAuthority("Bureau of Indian Standards (BIS)");
        s3.setDocumentReference("SP 7:2016");
        s3.setOfficialUrl("https://www.bis.gov.in");
        s3.setPublicationDate(java.time.LocalDate.of(2016, 12, 1));
        s3.setEffectiveFrom(java.time.LocalDate.of(2017, 1, 1));
        s3.setApplicableJurisdiction("PAN_INDIA");
        s3.setSourceVerificationState(SourceVerificationState.VERIFIED);
        s3.setSourceDescription("Statutory safety, fire prevention, staircase width, and barrier-free ramp standards for institutional buildings.");
        regulatorySourceRepository.save(s3);

        // 2. Regulatory Rules & Active Versions
        createRuleWithVersion("AICTE_FSR_TECH_001", "Faculty-to-Student Ratio for Engineering UG", RegulatoryRuleCategory.FACULTY_QUALIFICATIONS_STAFFING, s1.getId(), "Chapter 7, Clause 7.14, Table 7.2", "NUMERIC_THRESHOLD", "{\"operator\": \"<=\", \"threshold\": 15.0}", Severity.CRITICAL, "AICTE APH 2024-27, Clause 7.14", "Recruit certified full-time faculty to restore FSR to <= 1:15.");
        createRuleWithVersion("AICTE_RAMP_GRADIENT_001", "Barrier-Free Accessibility Wheelchair Ramp Gradient", RegulatoryRuleCategory.BUILDING_INFRASTRUCTURE, s3.getId(), "Appendix 4, Clause 4.2 & NBC 2016", "NUMERIC_THRESHOLD", "{\"operator\": \"<=\", \"max_slope\": \"1:12\"}", Severity.CRITICAL, "AICTE APH 2024-27, App 4 & NBC 2016", "Re-grade wheelchair ramp incline to <= 1:12 slope and install dual continuous handrails.");
        createRuleWithVersion("AICTE_LAND_AREA_001", "Minimum Campus Land Area Norms", RegulatoryRuleCategory.BUILDING_INFRASTRUCTURE, s1.getId(), "Chapter 4, Table 4.1", "NUMERIC_THRESHOLD", "{\"operator\": \">=\", \"min_acres\": 5.0}", Severity.CRITICAL, "AICTE APH 2024-27, Table 4.1", "Ensure contiguous land title >= 5.0 acres or provide Urban / Mega-City notification.");
        createRuleWithVersion("AICTE_COMP_RATIO_001", "Student-to-Computer Workstation Ratio", RegulatoryRuleCategory.LABORATORIES_EQUIPMENT, s1.getId(), "Chapter 5, Table 5.4", "NUMERIC_THRESHOLD", "{\"operator\": \"<=\", \"threshold\": 4.0}", Severity.MODERATE, "AICTE APH 2024-27, Table 5.4", "Procure additional networked desktop terminals to meet 1:4 terminal ratio.");
        createRuleWithVersion("AICTE_FIRE_NOC_VAL_001", "Valid Fire Safety Certificate / NOC", RegulatoryRuleCategory.SAFETY_STATUTORY, s3.getId(), "Chapter 6, Clause 6.3 & NBC Part 4", "DATE_INTERVAL", "{\"operator\": \"ACTIVE_NOT_EXPIRED\"}", Severity.CRITICAL, "AICTE APH 2024-27, Clause 6.3", "Obtain renewed Fire Safety NOC from District Fire Authority immediately.");
        createRuleWithVersion("AICTE_INTAKE_SANCTION_001", "Sanctioned Intake Conformance with Affiliating Body", RegulatoryRuleCategory.INTAKE_CAPACITY, s1.getId(), "Chapter 2, Clause 2.8", "CROSS_DOCUMENT_CONSISTENCY", "{\"operator\": \"<=\", \"cross_check\": \"UNIVERSITY_AFFILIATION\"}", Severity.CRITICAL, "AICTE APH 2024-27, Clause 2.8", "Reconcile proposed intake with University sanctioned intake letter.");
        createRuleWithVersion("AICTE_DOC_MANDATORY_001", "Mandatory Statutory Document Completeness", RegulatoryRuleCategory.DOCUMENT_COMPLETENESS, s1.getId(), "Appendix 16", "DOCUMENT_PRESENCE", "{\"min_mandatory_count\": 4}", Severity.CRITICAL, "AICTE APH 2024-27, Appendix 16", "Upload all mandatory statutory documents including Land Deed, Fire NOC, and Affiliation letter.");
        createRuleWithVersion("AICTE_LAB_EQUIP_001", "Laboratories Minimum Functional Apparatus Norm", RegulatoryRuleCategory.LABORATORIES_EQUIPMENT, s1.getId(), "Chapter 7, Clause 7.18", "NUMERIC_THRESHOLD", "{\"min_equipment_per_lab\": 5}", Severity.MODERATE, "AICTE APH 2024-27, Clause 7.18", "Equip all engineering laboratories with minimum prescribed syllabus apparatus.");
        createRuleWithVersion("AICTE_ANTI_RAGGING_001", "Statutory Anti-Ragging Committee Affidavit", RegulatoryRuleCategory.INSTITUTIONAL_DECLARATIONS, s1.getId(), "Notification F.No. 37-3/Legal/AICTE/2009", "NORMALIZED_TEXT_MATCH", "{\"mandatory\": true}", Severity.ADVISORY, "AICTE Anti-Ragging Regulations 2009", "Submit gazetted committee constitution orders.");
        createRuleWithVersion("AICTE_GRIEVANCE_COMM_001", "Student Grievance Redressal Committee (SGRC)", RegulatoryRuleCategory.INSTITUTIONAL_DECLARATIONS, s1.getId(), "AICTE SGRC Regulations 2019", "NORMALIZED_TEXT_MATCH", "{\"mandatory\": true}", Severity.ADVISORY, "AICTE SGRC Regulations 2019", "Establish committee with appointed Omsperson.");
        createRuleWithVersion("AICTE_SOURCE_VERIF_001", "Independent Issuing Authority Corroboration", RegulatoryRuleCategory.SOURCE_VERIFICATION, s1.getId(), "Chapter 1, Clause 1.4", "EVIDENCE_VERIFICATION", "{\"require_match\": true}", Severity.CRITICAL, "AICTE APH 2024-27, Clause 1.4", "Resolve data discrepancy with State Technical Board or Affiliating University.");
        createRuleWithVersion("AICTE_FORENSIC_INTEGRITY_001", "Document Forensic Tamper Screening", RegulatoryRuleCategory.CROSS_DOCUMENT_CONSISTENCY, s1.getId(), "Appendix 18", "EVIDENCE_VERIFICATION", "{\"max_critical_anomalies\": 0}", Severity.CRITICAL, "AICTE APH 2024-27, Appendix 18", "Investigate flagged visual/metadata tampering discrepancies.");
        createRuleWithVersion("AICTE_LIBRARY_VOLUMES_001", "Minimum Technical Library Titles & Volumes", RegulatoryRuleCategory.LIBRARY_LEARNING_RESOURCES, s1.getId(), "Chapter 7, Table 7.5", "NUMERIC_THRESHOLD", "{\"min_titles\": 100, \"min_volumes\": 500}", Severity.ADVISORY, "AICTE APH 2024-27, Table 7.5", "Update library catalog accessions.");
        createRuleWithVersion("AICTE_BUILDING_STABILITY_001", "Building Structural Soundness Certificate", RegulatoryRuleCategory.SAFETY_STATUTORY, s3.getId(), "Chapter 6, Clause 6.5", "DOCUMENT_PRESENCE", "{\"mandatory\": true}", Severity.ADVISORY, "AICTE APH 2024-27, Clause 6.5", "Submit structural stability certificate from chartered structural engineer.");

        // 3. Infrastructure Assets for Horizon College (id=2) and Apex Institute (id=1)
        java.util.List<Application> apps = applicationRepository.findAll();
        Long app1Id = apps.size() > 0 ? apps.get(0).getId() : 1L;
        Long app2Id = apps.size() > 1 ? apps.get(1).getId() : 2L;

        // Assets for Horizon College
        InfrastructureAsset a1 = new InfrastructureAsset();
        a1.setApplicationId(app2Id);
        a1.setAssetCode("RM-101");
        a1.setAssetName("CSE Central Programming Lab");
        a1.setCategory(InfrastructureCategory.COMPUTER_FACILITY);
        a1.setBuildingName("Main Tech Block");
        a1.setFloorLevel("1st Floor");
        a1.setClaimedAreaSqm(140.0);
        a1.setClaimedCapacity(60);
        a1.setEquipmentCount(30);
        a1 = infrastructureAssetRepository.save(a1);

        InfrastructureAsset a2 = new InfrastructureAsset();
        a2.setApplicationId(app2Id);
        a2.setAssetCode("RAMP-MAIN-BLDG");
        a2.setAssetName("North Entrance Accessible Wheelchair Ramp");
        a2.setCategory(InfrastructureCategory.ACCESSIBILITY);
        a2.setBuildingName("Administrative Block");
        a2.setFloorLevel("Ground");
        a2.setClaimedAreaSqm(45.0);
        a2.setClaimedCapacity(10);
        a2.setEquipmentCount(2);
        a2 = infrastructureAssetRepository.save(a2);

        InfrastructureAsset a3 = new InfrastructureAsset();
        a3.setApplicationId(app2Id);
        a3.setAssetCode("LAB-MECH-01");
        a3.setAssetName("Strength of Materials & Mechanics Lab");
        a3.setCategory(InfrastructureCategory.LABORATORY);
        a3.setBuildingName("Mechanical Workshop Block");
        a3.setFloorLevel("Ground");
        a3.setClaimedAreaSqm(180.0);
        a3.setClaimedCapacity(40);
        a3.setEquipmentCount(8);
        infrastructureAssetRepository.save(a3);

        InfrastructureAsset a4 = new InfrastructureAsset();
        a4.setApplicationId(app2Id);
        a4.setAssetCode("LIB-CENTRAL");
        a4.setAssetName("Central Reference & Digital Library");
        a4.setCategory(InfrastructureCategory.LIBRARY);
        a4.setBuildingName("Knowledge Center");
        a4.setFloorLevel("2nd Floor");
        a4.setClaimedAreaSqm(480.0);
        a4.setClaimedCapacity(150);
        a4.setEquipmentCount(20);
        infrastructureAssetRepository.save(a4);

        // Assets for Apex Institute
        InfrastructureAsset a5 = new InfrastructureAsset();
        a5.setApplicationId(app1Id);
        a5.setAssetCode("APEX-LAB-01");
        a5.setAssetName("Advanced Computing & AI Lab");
        a5.setCategory(InfrastructureCategory.COMPUTER_FACILITY);
        a5.setBuildingName("Engineering Tower");
        a5.setFloorLevel("Ground");
        a5.setClaimedAreaSqm(160.0);
        a5.setClaimedCapacity(75);
        a5.setEquipmentCount(40);
        infrastructureAssetRepository.save(a5);

        InfrastructureAsset a6 = new InfrastructureAsset();
        a6.setApplicationId(app1Id);
        a6.setAssetCode("APEX-RAMP-01");
        a6.setAssetName("Universal Barrier-Free Ramp");
        a6.setCategory(InfrastructureCategory.ACCESSIBILITY);
        a6.setBuildingName("Engineering Tower");
        a6.setFloorLevel("Ground");
        a6.setClaimedAreaSqm(50.0);
        a6.setClaimedCapacity(15);
        a6.setEquipmentCount(2);
        infrastructureAssetRepository.save(a6);

        // 4. Infrastructure Evidence Media for Horizon College
        InfrastructureEvidence e1 = new InfrastructureEvidence();
        e1.setApplicationId(app2Id);
        e1.setAssetId(a2.getId());
        e1.setEvidenceType("PHOTOGRAPH");
        e1.setFileName("horizon_ramp_incline_photo.jpg");
        e1.setFileUri("/uploads/evidence/horizon_ramp_incline_photo.jpg");
        e1.setMimeType("image/jpeg");
        e1.setImageWidth(1920);
        e1.setImageHeight(1080);
        e1.setQualityScore(0.92);
        e1.setBlurScore(0.08);
        e1.setLightingAdequacy(0.95);
        e1.setAnalysisStatus(InfrastructureAnalysisStatus.POSSIBLE_DISCREPANCY);
        e1 = infrastructureEvidenceRepository.save(e1);

        InfrastructureEvidence e2 = new InfrastructureEvidence();
        e2.setApplicationId(app2Id);
        e2.setAssetId(a1.getId());
        e2.setEvidenceType("PHOTOGRAPH");
        e2.setFileName("horizon_cse_lab_workstations.jpg");
        e2.setFileUri("/uploads/evidence/horizon_cse_lab_workstations.jpg");
        e2.setMimeType("image/jpeg");
        e2.setImageWidth(1920);
        e2.setImageHeight(1080);
        e2.setQualityScore(0.94);
        e2.setBlurScore(0.06);
        e2.setLightingAdequacy(0.96);
        e2.setAnalysisStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        e2 = infrastructureEvidenceRepository.save(e2);

        InfrastructureEvidence e3 = new InfrastructureEvidence();
        e3.setApplicationId(app2Id);
        e3.setEvidenceType("FLOOR_PLAN_PDF");
        e3.setFileName("horizon_campus_master_floor_plan.pdf");
        e3.setFileUri("/uploads/evidence/horizon_campus_master_floor_plan.pdf");
        e3.setMimeType("application/pdf");
        e3.setQualityScore(0.98);
        e3.setAnalysisStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        e3 = infrastructureEvidenceRepository.save(e3);

        InfrastructureEvidence e4 = new InfrastructureEvidence();
        e4.setApplicationId(app2Id);
        e4.setAssetId(a3.getId());
        e4.setEvidenceType("VIDEO_WALKTHROUGH");
        e4.setFileName("horizon_mech_lab_video_walkthrough.mp4");
        e4.setFileUri("/uploads/evidence/horizon_mech_lab_video_walkthrough.mp4");
        e4.setMimeType("video/mp4");
        e4.setQualityScore(0.88);
        e4.setAnalysisStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        e4 = infrastructureEvidenceRepository.save(e4);

        // Pre-create Visual Observations for Horizon College (Demonstrating Ramp Discrepancy!)
        InfrastructureAnalysisJob j1 = new InfrastructureAnalysisJob();
        j1.setApplicationId(app2Id);
        j1.setEvidenceId(e1.getId());
        j1.setJobStatus(InfrastructureAnalysisStatus.POSSIBLE_DISCREPANCY);
        j1.setObservationsCount(2);
        j1.setDiscrepancyCount(1);
        j1.setSummaryObservations("Analysis detected 1 critical accessibility discrepancy: Wheelchair ramp slope measured 1:8, violating AICTE 1:12 maximum limit.");
        j1 = infrastructureAnalysisJobRepository.save(j1);

        InfrastructureObservation o11 = new InfrastructureObservation();
        o11.setAnalysisJobId(j1.getId());
        o11.setEvidenceId(e1.getId());
        o11.setAssetId(a2.getId());
        o11.setCategory(InfrastructureCategory.ACCESSIBILITY);
        o11.setFeatureType("RAMP_GRADIENT_ESTIMATE");
        o11.setLabel("Wheelchair Ramp Incline");
        o11.setConfidence(0.92);
        o11.setBoundingBoxJson("{\"x\": 80, \"y\": 150, \"width\": 520, \"height\": 280}");
        o11.setObservedMeasurement("Gradient: 1:8 (Rise: 1.2m, Run: 9.6m)");
        o11.setRegulatoryImplication("VIOLATION: Ramp gradient of 1:8 exceeds AICTE barrier-free maximum threshold (1:12).");
        o11.setIsDiscrepancy(true);
        o11.setDiscrepancyReason("Measured ramp slope of 1:8 is steeper than the mandatory 1:12 gradient stipulated by AICTE barrier-free norms.");
        infrastructureObservationRepository.save(o11);

        InfrastructureObservation o12 = new InfrastructureObservation();
        o12.setAnalysisJobId(j1.getId());
        o12.setEvidenceId(e1.getId());
        o12.setAssetId(a2.getId());
        o12.setCategory(InfrastructureCategory.ACCESSIBILITY);
        o12.setFeatureType("SAFETY_EQUIPMENT");
        o12.setLabel("Bilateral Handrails (Stainless Steel)");
        o12.setConfidence(0.95);
        o12.setBoundingBoxJson("{\"x\": 90, \"y\": 130, \"width\": 510, \"height\": 40}");
        o12.setRegulatoryImplication("Satisfies accessibility requirement for dual continuous grab bars.");
        o12.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o12);

        // Pre-create Observations for Computer Lab
        InfrastructureAnalysisJob j2 = new InfrastructureAnalysisJob();
        j2.setApplicationId(app2Id);
        j2.setEvidenceId(e2.getId());
        j2.setJobStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        j2.setObservationsCount(3);
        j2.setDiscrepancyCount(0);
        j2.setSummaryObservations("Verified 30 active computing terminals, lab signage OCR, and fire extinguisher.");
        j2 = infrastructureAnalysisJobRepository.save(j2);

        InfrastructureObservation o21 = new InfrastructureObservation();
        o21.setAnalysisJobId(j2.getId());
        o21.setEvidenceId(e2.getId());
        o21.setAssetId(a1.getId());
        o21.setCategory(InfrastructureCategory.COMPUTER_FACILITY);
        o21.setFeatureType("OBJECT_DETECTION");
        o21.setLabel("Desktop Computer Terminal Cluster");
        o21.setConfidence(0.96);
        o21.setBoundingBoxJson("{\"x\": 45, \"y\": 110, \"width\": 620, \"height\": 340}");
        o21.setObservedMeasurement("Count: 30 active workstations");
        o21.setRegulatoryImplication("Matches claimed laboratory terminal capacity.");
        o21.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o21);

        InfrastructureObservation o22 = new InfrastructureObservation();
        o22.setAnalysisJobId(j2.getId());
        o22.setEvidenceId(e2.getId());
        o22.setAssetId(a1.getId());
        o22.setCategory(InfrastructureCategory.COMPUTER_FACILITY);
        o22.setFeatureType("SIGNAGE_OCR");
        o22.setLabel("Room Identity Signboard");
        o22.setConfidence(0.94);
        o22.setBoundingBoxJson("{\"x\": 18, \"y\": 22, \"width\": 180, \"height\": 60}");
        o22.setExtractedText("ROOM NO: LAB-101 / COMPUTER SCIENCE & ENGINEERING");
        o22.setRegulatoryImplication("Verifies physical presence and statutory labeling of Computer Center.");
        o22.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o22);

        InfrastructureObservation o23 = new InfrastructureObservation();
        o23.setAnalysisJobId(j2.getId());
        o23.setEvidenceId(e2.getId());
        o23.setAssetId(a1.getId());
        o23.setCategory(InfrastructureCategory.SAFETY_FACILITY);
        o23.setFeatureType("SAFETY_EQUIPMENT");
        o23.setLabel("CO2 Fire Extinguisher (Mounted)");
        o23.setConfidence(0.91);
        o23.setBoundingBoxJson("{\"x\": 720, \"y\": 180, \"width\": 65, \"height\": 120}");
        o23.setRegulatoryImplication("Mandatory fire safety appliance detected in electronic laboratory area.");
        o23.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o23);

        // Pre-create Observations for Floor Plan
        InfrastructureAnalysisJob j3 = new InfrastructureAnalysisJob();
        j3.setApplicationId(app2Id);
        j3.setEvidenceId(e3.getId());
        j3.setJobStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        j3.setObservationsCount(1);
        j3.setDiscrepancyCount(0);
        j3.setSummaryObservations("Verified instructional schedule and fire escape stair width 2.0m.");
        j3 = infrastructureAnalysisJobRepository.save(j3);

        InfrastructureObservation o31 = new InfrastructureObservation();
        o31.setAnalysisJobId(j3.getId());
        o31.setEvidenceId(e3.getId());
        o31.setCategory(InfrastructureCategory.CLASSROOM);
        o31.setFeatureType("DIMENSION_VERIFICATION");
        o31.setLabel("Instructional Area Schedule — Ground Floor");
        o31.setConfidence(0.97);
        o31.setBoundingBoxJson("{\"x\": 120, \"y\": 80, \"width\": 450, \"height\": 350}");
        o31.setExtractedText("CR-01: 66 SQM | CR-02: 66 SQM | LAB-101: 72 SQM | LIBRARY: 420 SQM");
        o31.setObservedMeasurement("Total Carpet Area: 1,480 sqm");
        o31.setRegulatoryImplication("Meets minimum instructional built-up area prescribed in APH Table 7.1.");
        o31.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o31);

        // Pre-create Observations for Video
        InfrastructureAnalysisJob j4 = new InfrastructureAnalysisJob();
        j4.setApplicationId(app2Id);
        j4.setEvidenceId(e4.getId());
        j4.setJobStatus(InfrastructureAnalysisStatus.OBSERVATIONS_AVAILABLE);
        j4.setObservationsCount(1);
        j4.setDiscrepancyCount(0);
        j4.setSummaryObservations("Universal Testing Machine corroborated in mechanical lab video stream.");
        j4 = infrastructureAnalysisJobRepository.save(j4);

        InfrastructureObservation o41 = new InfrastructureObservation();
        o41.setAnalysisJobId(j4.getId());
        o41.setEvidenceId(e4.getId());
        o41.setAssetId(a3.getId());
        o41.setCategory(InfrastructureCategory.LABORATORY);
        o41.setFeatureType("OBJECT_DETECTION");
        o41.setLabel("Universal Testing Machine (UTM) & Experimental Benches");
        o41.setConfidence(0.94);
        o41.setFrameTimestampSec(4.5);
        o41.setBoundingBoxJson("{\"x\": 160, \"y\": 90, \"width\": 400, \"height\": 300}");
        o41.setRegulatoryImplication("Visual corroboration of Mechanical Testing Laboratory equipment.");
        o41.setIsDiscrepancy(false);
        infrastructureObservationRepository.save(o41);

        // 5. Trigger initial compliance evaluations for both institutions!
        regulatoryRuleEngineService.evaluateApplication(app1Id, "System Initializer");
        regulatoryRuleEngineService.evaluateApplication(app2Id, "System Initializer");
    }

    private void seedDeliverable6DataIfEmpty() {
        if (actionItemRepository.count() > 0) return;

        // 1. Seed Additional Applications for Portfolio Diversity
        if (applicationRepository.count() < 5) {
            // App 3: Zenith Institute (Awaiting Documents / Missing Structural Certificate)
            Application app3 = new Application();
            app3.setApplicationRef("AICTE-2026-APP-10492");
            app3.setInstitutionName("Zenith Institute of Technology & Management");
            app3.setInstituteCode("1-88492");
            app3.setCategoryCode("NEW_INSTITUTION");
            app3.setStatus(ApplicationStatus.DRAFT);
            app3.setOverallValidationStatus(FindingStatus.WARNING);
            app3.setOverallSummaryReason("Awaiting mandatory structural soundness certificate and land registry verification.");
            app3.setCreatedBy("applicant_zenith");
            app3.setRegion("Northern Region (Kanpur)");
            app3.setAcademicYear("2025-2026");
            app3.setSubmissionDate(LocalDateTime.now().minusDays(3));
            applicationRepository.save(app3);

            // App 4: Saraswati College (Awaiting Authority Verification)
            Application app4 = new Application();
            app4.setApplicationRef("AICTE-2026-APP-55821");
            app4.setInstitutionName("Saraswati College of Architecture & Planning");
            app4.setInstituteCode("1-22941");
            app4.setCategoryCode("VARIATION_INTAKE");
            app4.setStatus(ApplicationStatus.UNDER_REVIEW);
            app4.setOverallValidationStatus(FindingStatus.PASS);
            app4.setOverallSummaryReason("Affiliation order verification pending university registry response.");
            app4.setCreatedBy("applicant_saraswati");
            app4.setRegion("Southern Region (Chennai)");
            app4.setAcademicYear("2025-2026");
            app4.setSubmissionDate(LocalDateTime.now().minusDays(4));
            applicationRepository.save(app4);

            Document doc4 = new Document();
            doc4.setApplication(app4);
            doc4.setDocumentType("AFFILIATION_ORDER");
            doc4.setOriginalFilename("anna_univ_affiliation_2025.pdf");
            doc4.setStoredPath("./uploads/sample_affiliation.pdf");
            doc4.setMimeType("application/pdf");
            doc4.setFileSizeBytes(180000);
            doc4.setPageCount(3);
            doc4.setSha256Checksum("4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c");
            doc4.setExtractionStatus("EXTRACTED");
            doc4.setExtractedText("ANNA UNIVERSITY CHENNAI PROVISIONAL AFFILIATION ORDER 2025-2026 B.ARCH INTAKE 80 SEATS");
            documentRepository.save(doc4);

            // App 5: Metro Institute (Approved / Decision Recorded)
            Application app5 = new Application();
            app5.setApplicationRef("AICTE-2026-APP-77319");
            app5.setInstitutionName("Metro Institute of Computer Applications");
            app5.setInstituteCode("1-66710");
            app5.setCategoryCode("EOA_TECHNICAL");
            app5.setStatus(ApplicationStatus.APPROVED);
            app5.setOverallValidationStatus(FindingStatus.PASS);
            app5.setOverallSummaryReason("All requirements verified. Conditional EoA granted by Scrutiny Committee.");
            app5.setCreatedBy("applicant_metro");
            app5.setRegion("North Western Region (Chandigarh)");
            app5.setAcademicYear("2025-2026");
            app5.setSubmissionDate(LocalDateTime.now().minusDays(7));
            applicationRepository.save(app5);

            Document doc5 = new Document();
            doc5.setApplication(app5);
            doc5.setDocumentType("FIRE_NOC");
            doc5.setOriginalFilename("metro_fire_noc_2025.pdf");
            doc5.setStoredPath("./uploads/sample_fire_metro.pdf");
            doc5.setMimeType("application/pdf");
            doc5.setFileSizeBytes(135000);
            doc5.setPageCount(2);
            doc5.setSha256Checksum("1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a2b");
            doc5.setExtractionStatus("EXTRACTED");
            doc5.setExtractedText("CHANDIGARH FIRE AND EMERGENCY SERVICES NOC FS/2025/1102 VALID TILL 31-MAR-2027");
            documentRepository.save(doc5);

            // Pre-seed official decision for App 5
            OfficialDecision d5 = new OfficialDecision();
            d5.setApplicationId(app5.getId());
            d5.setApplicationRef(app5.getApplicationRef());
            d5.setInstitutionName(app5.getInstitutionName());
            d5.setOutcome(OfficialDecisionOutcome.APPROVED_CONDITIONAL_EOA);
            d5.setOfficerName("Dr. Rajesh Sharma (Scrutiny Member)");
            d5.setOfficerRole("COMPLIANCE_OFFICER");
            d5.setStatutoryRationale("All statutory documentary, faculty staffing (FSR 1:15), and computer infrastructure norms under AICTE APH 2024-27 satisfied. Extension of Approval granted conditionally on submission of updated anti-ragging affidavit within 30 days.");
            d5.setConditionsPrecedent("Submit institutional anti-ragging committee affidavit on stamp paper within 30 days.");
            d5.setAcademicYear("2025-2026");
            d5.setSpotInspectionRequired(false);
            d5.setCommitteeReference("AICTE-SC-NWRO-2026/02");
            d5.setDecisionIntegritySeal("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
            d5.setRecordedAt(LocalDateTime.now().minusDays(1));
            officialDecisionRepository.save(d5);

            regulatoryRuleEngineService.evaluateApplication(app4.getId(), "System Initializer");
            regulatoryRuleEngineService.evaluateApplication(app5.getId(), "System Initializer");
        }

        // 2. Seed Unified Action Items
        ActionItem t1 = new ActionItem();
        t1.setApplicationId(2L);
        t1.setApplicationRef("AICTE-2025-EOA-44219");
        t1.setInstitutionName("Horizon College of Engineering");
        t1.setTaskType(ActionItemType.INFRASTRUCTURE_INSPECTION);
        t1.setTitle("Re-grade wheelchair ramp incline to <= 1:12 barrier-free norm");
        t1.setDescription("Computer vision analysis detected a 1:8 gradient (Rise: 1.2m, Run: 9.6m) on the central administrative block ramp, violating AICTE APH 2024-27 Appendix 4 & NBC 2016.");
        t1.setStatus(ActionItemStatus.ASSIGNED);
        t1.setPriority(ActionItemPriority.CRITICAL);
        t1.setAssignedTo("Dr. Rajesh Sharma (Scrutiny Member)");
        t1.setAssignedBy("AICTE Scrutiny Committee");
        t1.setRecommendedNextStep("Convene physical inspection with regional engineering team or require architectural gradient certificate.");
        t1.setCompletionRequirements("Architectural re-survey certificate demonstrating slope <= 1:12.");
        t1.setNavigationUrl("/#tab-cv");
        actionItemRepository.save(t1);

        ActionItem t2 = new ActionItem();
        t2.setApplicationId(2L);
        t2.setApplicationRef("AICTE-2025-EOA-44219");
        t2.setInstitutionName("Horizon College of Engineering");
        t2.setTaskType(ActionItemType.EXPIRED_EVIDENCE);
        t2.setTitle("Re-upload valid renewed Fire Safety Certificate (NOC)");
        t2.setDescription("Submitted Fire NOC expired on 31-JUL-2024 (92 days prior to AICTE submission deadline). Mandatory clearance is legally void.");
        t2.setStatus(ActionItemStatus.WAITING_FOR_APPLICANT);
        t2.setPriority(ActionItemPriority.CRITICAL);
        t2.setAssignedTo("Priya Desai (Legal Reviewer)");
        t2.setAssignedBy("Dr. Rajesh Sharma (Scrutiny Member)");
        t2.setRecommendedNextStep("Issue formal 15-day deficiency notice under AICTE APH 2024-27 Clause 4.1.2.");
        t2.setCompletionRequirements("Certified valid Fire NOC issued by competent State Directorate.");
        t2.setNavigationUrl("/#tab-ocr");
        actionItemRepository.save(t2);

        ActionItem t3 = new ActionItem();
        t3.setApplicationId(2L);
        t3.setApplicationRef("AICTE-2025-EOA-44219");
        t3.setInstitutionName("Horizon College of Engineering");
        t3.setTaskType(ActionItemType.ANOMALY_REVIEW);
        t3.setTitle("Investigate cloned municipal revenue seal boundary");
        t3.setDescription("ELA and hash matching detected that the revenue department round seal on Page 1 matches pixel-for-pixel (hash: #7a8f10b) with an affidavit submitted in 2021 by a separate institution.");
        t3.setStatus(ActionItemStatus.ASSIGNED);
        t3.setPriority(ActionItemPriority.HIGH);
        t3.setAssignedTo("Priya Desai (Legal Reviewer)");
        t3.setAssignedBy("Dr. Rajesh Sharma (Scrutiny Member)");
        t3.setRecommendedNextStep("Cross-reference municipal dispatch registers with issuing tehsildar.");
        t3.setNavigationUrl("/#tab-forensics");
        actionItemRepository.save(t3);

        ActionItem t4 = new ActionItem();
        t4.setApplicationId(3L);
        t4.setApplicationRef("AICTE-2026-APP-10492");
        t4.setInstitutionName("Zenith Institute of Technology & Management");
        t4.setTaskType(ActionItemType.MISSING_DOCUMENT);
        t4.setTitle("Upload Mandatory Building Structural Stability Certificate");
        t4.setDescription("Greenfield technical institution application under Chapter II lacks certified Structural Soundness Certificate from a government-empanelled structural engineer.");
        t4.setStatus(ActionItemStatus.OPEN);
        t4.setPriority(ActionItemPriority.HIGH);
        t4.setRecommendedNextStep("Applicant notification sent to furnish structural safety certificate.");
        t4.setNavigationUrl("/#tab-decision");
        actionItemRepository.save(t4);

        ActionItem t5 = new ActionItem();
        t5.setApplicationId(4L);
        t5.setApplicationRef("AICTE-2026-APP-55821");
        t5.setInstitutionName("Saraswati College of Architecture & Planning");
        t5.setTaskType(ActionItemType.VERIFICATION_PENDING);
        t5.setTitle("Corroborate Affiliation Order with Anna University Academic Registry");
        t5.setDescription("Affiliating university portal API query timed out. Manual verification of order AU/AFF/2025/1104 is required.");
        t5.setStatus(ActionItemStatus.WAITING_FOR_EXTERNAL_SOURCE);
        t5.setPriority(ActionItemPriority.MEDIUM);
        t5.setAssignedTo("officer_sc401");
        t5.setRecommendedNextStep("Re-dispatch API probe or send university registrar verification inquiry.");
        t5.setNavigationUrl("/#tab-authorities");
        actionItemRepository.save(t5);

        ActionItem t6 = new ActionItem();
        t6.setApplicationId(1L);
        t6.setApplicationRef("AICTE-2025-EOA-98104");
        t6.setInstitutionName("Apex Institute of Technology");
        t6.setTaskType(ActionItemType.FINAL_DECISION_PENDING);
        t6.setTitle("Convene Final Scrutiny Hearing for EoA Grant");
        t6.setDescription("All statutory documentary, forensic, issuing authority, and regulatory rule checks satisfied. Ready for formal approval issuance.");
        t6.setStatus(ActionItemStatus.WAITING_FOR_REVIEW);
        t6.setPriority(ActionItemPriority.LOW);
        t6.setAssignedTo("Dr. Rajesh Sharma (Scrutiny Member)");
        t6.setRecommendedNextStep("Schedule regional scrutiny committee agenda slot.");
        t6.setNavigationUrl("/#tab-decision");
        actionItemRepository.save(t6);

        // 3. Seed In-App Notifications
        notificationRepository.save(new Notification(
                "officer_sc401",
                2L,
                "AICTE-2025-EOA-44219",
                NotificationType.INFRASTRUCTURE_DISCREPANCY,
                Severity.CRITICAL,
                "URGENT: Campus Ramp Slope Norm Violation",
                "Computer Vision detected 1:8 steep gradient on Horizon College wheelchair ramp exceeding 1:12 threshold.",
                "/#tab-cv"
        ));

        notificationRepository.save(new Notification(
                "officer_sc401",
                2L,
                "AICTE-2025-EOA-44219",
                NotificationType.ANOMALY_FLAGGED,
                Severity.CRITICAL,
                "FORENSIC ALERT: Cloned Stamp Seal Splicing",
                "Page 1 stamp matches pixel-for-pixel with 2021 external affidavit #7a8f10b (ELA: 94.2%).",
                "/#tab-forensics"
        ));

        notificationRepository.save(new Notification(
                "officer_sc401",
                4L,
                "AICTE-2026-APP-55821",
                NotificationType.SOURCE_UNAVAILABLE,
                Severity.MODERATE,
                "Verification Timeout: Anna University Registry",
                "Affiliation order query AU/AFF/2025/1104 timed out. Fallback review workflow triggered.",
                "/#tab-authorities"
        ));

        notificationRepository.save(new Notification(
                "officer_sc401",
                1L,
                "AICTE-2025-EOA-98104",
                NotificationType.TASK_ASSIGNED,
                Severity.ADVISORY,
                "Scrutiny Hearing Ready: Apex Institute",
                "All statutory prerequisites 100% satisfied. Ready for committee deliberation.",
                "/#tab-decision"
        ));
    }

    private void createRuleWithVersion(String ruleCode, String ruleName, RegulatoryRuleCategory category,
                                      Long sourceId, String sourceSection, String evaluatorType,
                                      String conditionJson, Severity defaultSeverity, String citation, String remediation) {
        RegulatoryRule rule = new RegulatoryRule();
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setCategory(category);
        rule.setIssuingAuthority("AICTE");
        rule.setJurisdiction("NATIONAL");
        rule.setActiveVersion(1);
        rule.setActive(true);
        rule = regulatoryRuleRepository.save(rule);

        RegulatoryRuleVersion ver = new RegulatoryRuleVersion();
        ver.setRuleId(rule.getId());
        ver.setRuleCode(ruleCode);
        ver.setVersionNumber(1);
        ver.setVersionTag("2024-2027.v1");
        ver.setLifecycleStatus(RuleLifecycleStatus.ACTIVE);
        ver.setSourceId(sourceId);
        ver.setSourceSection(sourceSection);
        ver.setEvaluatorType(evaluatorType);
        ver.setConditionDefinitionJson(conditionJson);
        ver.setDefaultSeverity(defaultSeverity);
        ver.setFailureStatus(defaultSeverity == Severity.CRITICAL ? RuleEvaluationStatus.DEFICIENCY : RuleEvaluationStatus.WARNING);
        ver.setStatutoryCitation(citation);
        ver.setRemediationGuidance(remediation);
        ver.setReviewInstructions("Examine submitted documentation and visual evidence against statutory thresholds.");
        regulatoryRuleVersionRepository.save(ver);
    }
}
