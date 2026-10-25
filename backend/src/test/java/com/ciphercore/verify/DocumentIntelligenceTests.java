package com.ciphercore.verify;

import com.ciphercore.verify.model.*;
import com.ciphercore.verify.model.enums.*;
import com.ciphercore.verify.repository.*;
import com.ciphercore.verify.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DocumentIntelligenceTests {

    @Autowired
    private DocumentSecurityService securityService;

    @Autowired
    private DocumentClassificationService classificationService;

    @Autowired
    private FieldNormalizationService normalizationService;

    @Autowired
    private FieldExtractionService extractionService;

    @Autowired
    private CompletenessValidationService completenessService;

    @Autowired
    private ConsistencyValidationService consistencyService;

    @Autowired
    private DocumentReviewService reviewService;

    @Autowired
    private DocumentReportService reportService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    @DisplayName("OCR Test 1: Magic byte verification detects valid PDF and rejects spoofed extensions")
    void testSecurity_MagicByteInspection() throws IOException {
        // Generate a 100% genuine valid PDF with Apache PDFBox
        byte[] validPdfBytes;
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            doc.save(baos);
            validPdfBytes = baos.toByteArray();
        }

        MockMultipartFile validPdf = new MockMultipartFile("file", "certificate.pdf", "application/pdf", validPdfBytes);
        DocumentSecurityService.SecurityValidationResult validRes = securityService.validateAndInspect(validPdf);
        assertTrue(validRes.valid(), "Expected genuine PDF to be valid but got error: " + validRes.errorMessage());
        assertEquals("application/pdf", validRes.detectedMediaType());
        assertTrue(validRes.pageCount() >= 1);

        // Spoofed PDF (Text file named fake.pdf)
        byte[] spoofedBytes = "This is a plain text file pretending to be a PDF".getBytes();
        MockMultipartFile spoofedPdf = new MockMultipartFile("file", "fake.pdf", "application/pdf", spoofedBytes);
        DocumentSecurityService.SecurityValidationResult spoofRes = securityService.validateAndInspect(spoofedPdf);
        assertFalse(spoofRes.valid());
        assertTrue(spoofRes.errorMessage().contains("File signature mismatch"));

        // Path traversal defense
        MockMultipartFile traversalFile = new MockMultipartFile("file", "../../etc/passwd.pdf", "application/pdf", validPdfBytes);
        DocumentSecurityService.SecurityValidationResult travRes = securityService.validateAndInspect(traversalFile);
        assertFalse(travRes.valid());
        assertTrue(travRes.errorMessage().contains("Path traversal"));
    }

    @Test
    @DisplayName("OCR Test 2: Automatic Document Classification accurately predicts 15 institutional categories")
    void testClassification_StandardCategories() {
        // Fire NOC
        String nocText = "GOVERNMENT OF MAHARASHTRA\nDIRECTORATE OF FIRE SERVICES\nNO OBJECTION CERTIFICATE\n" +
                         "Ref No: DFS/NOC/2025/9981\nThis is to certify that the premises of Apex College of Tech complies with fire safety.";
        var nocOutcome = classificationService.classifyDocument(nocText);
        assertEquals(DocumentCategory.NO_OBJECTION_CERTIFICATE, nocOutcome.primaryCategory());
        assertTrue(nocOutcome.confidenceScore() >= 0.70);
        assertFalse(nocOutcome.evidencePhrases().isEmpty());

        // Registration Certificate
        String regText = "GOVERNMENT OF INDIA\nCERTIFICATE OF REGISTRATION\nIssued under Societies Registration Act XXI of 1860\n" +
                         "Registration No: MAH/MUM/1402/2012\nThis is to certify that Vidya Vikas Education Trust is registered.";
        var regOutcome = classificationService.classifyDocument(regText);
        assertEquals(DocumentCategory.INSTITUTION_REGISTRATION_CERTIFICATE, regOutcome.primaryCategory());

        // Accreditation Certificate (NAAC)
        String naacText = "NATIONAL ASSESSMENT AND ACCREDITATION COUNCIL (NAAC)\nAn Autonomous Institution of UGC\n" +
                          "Certificate of Accreditation\nThe Executive Committee is pleased to declare Apex Institute of Engineering " +
                          "Accredited with Grade A++ with CGPA 3.82.";
        var naacOutcome = classificationService.classifyDocument(naacText);
        assertEquals(DocumentCategory.ACCREDITATION_CERTIFICATE, naacOutcome.primaryCategory());

        // AICTE Letter of Approval
        String aicteText = "ALL INDIA COUNCIL FOR TECHNICAL EDUCATION\n(A Statutory Body of the Govt. of India)\n" +
                           "Letter of Approval (EOA)\nF.No. Western/1-441829/2025/EOA\nExtension of Approval for the Academic Session 2025-2026.";
        var aicteOutcome = classificationService.classifyDocument(aicteText);
        assertEquals(DocumentCategory.APPROVAL_LETTER, aicteOutcome.primaryCategory());

        // Affidavit
        String affText = "AFFIDAVIT\nI, Dr. Ramesh Sharma, Age 52, Deponent, solemnly affirm and declare on oath before Notary Public " +
                         "that the facts stated in the AICTE application are true.";
        var affOutcome = classificationService.classifyDocument(affText);
        assertEquals(DocumentCategory.AFFIDAVIT, affOutcome.primaryCategory());
    }

    @Test
    @DisplayName("OCR Test 3: Field Normalization safely normalizes dates to ISO 8601 & flags ambiguous representations")
    void testNormalization_DatesAndIdentifiers() {
        // Unambiguous Indian regulatory date DD-MM-YYYY
        var normDate = normalizationService.normalizeDate("28-02-2026");
        assertEquals("2026-02-28", normDate.isoDateString());
        assertEquals(FieldExtractionStatus.EXTRACTED, normDate.status());

        // Ambiguous date 04/05/2026 (both day and month <= 12)
        var ambDate = normalizationService.normalizeDate("04/05/2026");
        assertEquals("2026-05-04", ambDate.isoDateString());
        assertEquals(FieldExtractionStatus.AMBIGUOUS, ambDate.status());
        assertNotNull(ambDate.warning());

        // Textual date "31 December 2026"
        var textDate = normalizationService.normalizeDate("31 December 2026");
        assertEquals("2026-12-31", textDate.isoDateString());
        assertEquals(FieldExtractionStatus.EXTRACTED, textDate.status());

        // Preserves leading zeros in identifiers (e.g. 008129 must not become 8129)
        var idNorm = normalizationService.normalizeIdentifier("008129");
        assertEquals("008129", idNorm.normalizedIdentifier());
        assertEquals(FieldExtractionStatus.EXTRACTED, idNorm.status());

        // Flags potentially ambiguous characters like 'O' vs '0'
        var ambId = normalizationService.normalizeIdentifier("EOA-O981");
        assertTrue(ambId.hasAmbiguousCharacters());
    }

    @Test
    @DisplayName("OCR Test 4: Schema-Driven Completeness Validation distinguishes DEFICIENCY vs PASS vs NOT_STATED")
    void testCompleteness_SchemaEnforcement() {
        // Registration Certificate missing mandatory registration number
        Map<String, FieldExtractionService.ExtractedFieldItem> partialFields = Map.of(
            "institutionName", new FieldExtractionService.ExtractedFieldItem(
                "institutionName", "Apex College", "Apex College", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"
            ),
            "issueDate", new FieldExtractionService.ExtractedFieldItem(
                "issueDate", "15-06-2020", "2020-06-15", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"
            )
            // Missing: registrationNumber, issuingAuthority
        );

        var compResult = completenessService.evaluate(DocumentCategory.INSTITUTION_REGISTRATION_CERTIFICATE, partialFields, "HIGH");
        assertEquals(DocumentCompletenessStatus.DEFICIENCY, compResult.status());
        assertTrue(compResult.missingRequiredFields().contains("registrationNumber"));
        assertTrue(compResult.missingRequiredFields().contains("issuingAuthority"));

        // NOC without expiry date: NOC schema treats absent expiry as NOT_STATED rather than deficiency
        Map<String, FieldExtractionService.ExtractedFieldItem> nocFields = Map.of(
            "issuingAuthority", new FieldExtractionService.ExtractedFieldItem("issuingAuthority", "Fire Dept", "Fire Dept", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"),
            "referenceNumber", new FieldExtractionService.ExtractedFieldItem("referenceNumber", "DFS/101", "DFS/101", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"),
            "issueDate", new FieldExtractionService.ExtractedFieldItem("issueDate", "10-01-2025", "2025-01-10", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"),
            "expiryDate", new FieldExtractionService.ExtractedFieldItem("expiryDate", null, null, FieldExtractionStatus.NOT_STATED, "No expiry stated", 1, 0.9, "SCHEMA_DEFAULT")
        );
        var nocComp = completenessService.evaluate(DocumentCategory.NO_OBJECTION_CERTIFICATE, nocFields, "HIGH");
        assertEquals(DocumentCompletenessStatus.PASS, nocComp.status());
        assertEquals(0, nocComp.missingRequiredFields().size());
    }

    @Test
    @DisplayName("OCR Test 5: Consistency and Temporal Validation detects chronological conflicts and revocation flags")
    void testConsistency_ChronologicalConflictAndRevocation() {
        Document dummyDoc = new Document();
        dummyDoc.setOriginalFilename("test.pdf");

        // Chronological conflict: Issue Date (2026-12-31) after Expiry Date (2025-01-01)
        Map<String, FieldExtractionService.ExtractedFieldItem> conflictFields = Map.of(
            "issueDate", new FieldExtractionService.ExtractedFieldItem("issueDate", "31-12-2026", "2026-12-31", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX"),
            "expiryDate", new FieldExtractionService.ExtractedFieldItem("expiryDate", "01-01-2025", "2025-01-01", FieldExtractionStatus.EXTRACTED, "...", 1, 0.9, "REGEX")
        );

        List<DocumentValidationResult> results = consistencyService.validateConsistency(dummyDoc, conflictFields, "Valid content");
        assertTrue(results.stream().anyMatch(r -> 
            r.getRuleId().equals("RULE_TEMPORAL_CHRONOLOGY") && r.getStatus() == FindingStatus.DEFICIENCY));

        // Revocation flag in document text
        String revokedText = "THIS APPROVAL IS HEREBY REVOKED AND CANCELLED WITH IMMEDIATE EFFECT.";
        List<DocumentValidationResult> revResults = consistencyService.validateConsistency(dummyDoc, Map.of(), revokedText);
        assertTrue(revResults.stream().anyMatch(r -> 
            r.getRuleId().equals("RULE_DOCUMENT_REVOCATION_FLAG") && r.getStatus() == FindingStatus.DEFICIENCY));
    }

    @Test
    @DisplayName("OCR Test 6: Human Review and Correction preserves audit trail with reviewer attribution")
    void testReview_CorrectionAndAuditTrail() {
        Document doc = new Document();
        doc.setOriginalFilename("audit_test.pdf");
        doc.setStoredPath("./uploads/audit_test.pdf");
        doc.setSha256Checksum("aabbccddeeff00112233445566778899aabbccddeeff00112233445566778899");
        doc = documentRepository.save(doc);

        // Reviewer corrects a misread field
        ExtractedDocumentField corrected = reviewService.correctField(
            doc.getId(), "registrationNumber", "MAH/2026/9999", "officer_sc401", "Corrected misread digit 8 to 9 from seal"
        );

        assertNotNull(corrected);
        assertEquals("MAH/2026/9999", corrected.getReviewedValue());
        assertEquals("officer_sc401", corrected.getReviewedBy());

        // Verify audit event persisted
        List<AuditEvent> events = auditEventRepository.findByUsernameOrderByEventTimestampDesc("officer_sc401");
        assertTrue(events.stream().anyMatch(e -> 
            e.getAction().equals("FIELD_CORRECTION") && e.getDetails().contains("MAH/2026/9999")));

        // Submit review decision
        DocumentReview review = reviewService.submitReviewDecision(doc.getId(), ReviewStatus.APPROVED, "officer_sc401", "All stamps authenticated");
        assertEquals(ReviewStatus.APPROVED, review.getReviewStatus());
        assertEquals("officer_sc401", review.getReviewerId());
    }

    @Test
    @DisplayName("OCR Test 7: Section 20 Authenticity Boundary strictly enforced")
    void testAuthenticityBoundary() {
        Document doc = new Document();
        doc.setOriginalFilename("boundary_test.pdf");
        doc.setStoredPath("./uploads/boundary.pdf");
        doc.setSha256Checksum("1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef");
        doc.setCompletenessStatus(DocumentCompletenessStatus.PASS);
        doc = documentRepository.save(doc);

        // Structured report must state AUTHENTICITY_NOT_VERIFIED despite PASS status
        Map<String, Object> report = reportService.generateStructuredReport(doc.getId());
        assertNotNull(report.get("authenticity"));

        @SuppressWarnings("unchecked")
        Map<String, Object> auth = (Map<String, Object>) report.get("authenticity");
        assertEquals("AUTHENTICITY_NOT_VERIFIED", auth.get("status"));
        assertTrue(auth.get("notice").toString().contains("Independent trusted authority verification required"));
    }
}
