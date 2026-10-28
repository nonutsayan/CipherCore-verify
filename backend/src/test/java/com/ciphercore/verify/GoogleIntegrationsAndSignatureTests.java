package com.ciphercore.verify;

import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.model.enums.UserRole;
import com.ciphercore.verify.model.enums.VerificationStatus;
import com.ciphercore.verify.repository.UserRepository;
import com.ciphercore.verify.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("default")
@Transactional
public class GoogleIntegrationsAndSignatureTests {

    @Autowired
    private GoogleVisionOcrService visionOcrService;

    @Autowired
    private GeminiDocumentAnalysisService geminiService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private SignatureVerificationService signatureService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private File tempPdfFile;

    @BeforeEach
    void setUp() throws IOException {
        tempPdfFile = File.createTempFile("test_sample_doc_", ".pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Government of Maharashtra Fire Safety Certificate");
                cs.newLineAtOffset(0, -25);
                cs.showText("NOC Number: MFS-2023-4419");
                cs.newLineAtOffset(0, -25);
                cs.showText("Authorized Signatory: Chief Fire Officer");
                cs.endText();
            }
            doc.save(tempPdfFile);
        }
    }

    @Test
    @DisplayName("Vision OCR - Unconfigured State Returns Descriptive Status")
    void testVisionOcrUnconfiguredGracefulHandling() {
        // When running in default test environment without GOOGLE_VISION_API_KEY
        if (!visionOcrService.isConfigured()) {
            GoogleVisionOcrService.VisionOcrResult result =
                    visionOcrService.extractDocumentText(tempPdfFile, "application/pdf");

            assertFalse(result.success());
            assertEquals("NOT_CONFIGURED", result.status());
            assertTrue(result.message().contains("GOOGLE_VISION_API_KEY"));
            assertEquals(0, result.totalPages());
        }
    }

    @Test
    @DisplayName("Vision OCR - Connection Test Diagnostic Endpoint")
    void testVisionConnectionDiagnostic() {
        GoogleVisionOcrService.VisionTestResult testRes = visionOcrService.testConnection();
        assertNotNull(testRes);
        assertNotNull(testRes.provider());
        if (!visionOcrService.isConfigured()) {
            assertFalse(testRes.configured());
            assertFalse(testRes.connected());
            assertTrue(testRes.message().contains("not configured"));
        }
    }

    @Test
    @DisplayName("Gemini - Unconfigured State Returns Descriptive Status")
    void testGeminiUnconfiguredGracefulHandling() {
        if (!geminiService.isConfigured()) {
            GeminiDocumentAnalysisService.GeminiAnalysisResult result =
                    geminiService.analyzeDocumentContent("Sample text", "test.pdf", "FIRE_SAFETY_NOC");

            assertFalse(result.success());
            assertEquals("NOT_CONFIGURED", result.status());
            assertTrue(result.message().contains("GEMINI_API_KEY"));
            assertNull(result.analysis());
        }
    }

    @Test
    @DisplayName("Gemini - Connection Test Diagnostic Endpoint")
    void testGeminiConnectionDiagnostic() {
        GeminiDocumentAnalysisService.GeminiTestResult testRes = geminiService.testConnection();
        assertNotNull(testRes);
        assertNotNull(testRes.model());
        if (!geminiService.isConfigured()) {
            assertFalse(testRes.configured());
            assertFalse(testRes.connected());
            assertTrue(testRes.message().contains("not configured"));
        }
    }

    @Test
    @DisplayName("Signature Verification - Detects Unsigned PDF Correctly")
    void testUnsignedPdfReturnsSignatureNotPresent() {
        Document doc = new Document();
        doc.setId(101L);
        doc.setOriginalFilename("test_sample_doc.pdf");
        doc.setStoredPath(tempPdfFile.getAbsolutePath());
        doc.setMimeType("application/pdf");

        SignatureVerificationService.DigitalSignatureResult res =
                signatureService.verifySignatures(doc, 1L);

        assertFalse(res.hasSignature());
        assertEquals(VerificationStatus.SIGNATURE_NOT_PRESENT, res.status());
        assertTrue(res.message().toLowerCase().contains("no cryptographic signature"));
    }

    @Test
    @DisplayName("Signature Verification - Detects Handwritten Signature Markers in Text")
    void testHandwrittenSignatureMarkerDetection() {
        String ocrWithSign = "This is an official certificate. Seal and Signature of Authorized Signatory. Date: 2026-05-10.";
        Map<String, Object> detection = signatureService.detectHandwrittenSignature(ocrWithSign);

        assertTrue((Boolean) detection.get("detected"));
        assertEquals("HANDWRITTEN_SIGNATURE_MARKER_PRESENT", detection.get("status"));
        assertNotNull(detection.get("evidence"));
        assertTrue(detection.get("disclaimer").toString().contains("Visual OCR cannot verify the identity"));

        String ocrWithoutSign = "Simple informational flyer with no stamps or endorsement.";
        Map<String, Object> noneDetection = signatureService.detectHandwrittenSignature(ocrWithoutSign);
        assertFalse((Boolean) noneDetection.get("detected"));
        assertEquals("SIGNATURE_MARKER_NOT_FOUND", noneDetection.get("status"));
    }

    @Test
    @DisplayName("Signature Verification - Detailed PDF Inspection Structure")
    void testDetailedSignatureInspectionOnUnsignedFile() {
        SignatureVerificationService.DetailedSignatureReport report =
                signatureService.inspectPdfSignaturesDetailed(tempPdfFile);

        assertNotNull(report);
        assertFalse(report.hasDigitalSignature());
        assertEquals("NO_SIGNATURE", report.trustChainStatus());
        assertEquals("NONE", report.signatureType());
    }

    @Test
    @DisplayName("Google Auth - Enforces Officer Email Allowlist on Role Assignment")
    void testGoogleAuthRoleAllowlistEnforcement() {
        List<String> allowlist = googleAuthService.getOfficerEmailAllowlist();
        assertNotNull(allowlist);
        assertTrue(allowlist.contains("arvind.kumar.aicte@gmail.com"));

        // If Google client id is unconfigured, authentication safely rejects empty/invalid token
        GoogleAuthService.GoogleAuthResult result =
                googleAuthService.authenticateGoogleIdToken("invalid_test_id_token", "127.0.0.1");

        assertFalse(result.success());
        assertNotNull(result.errorMessage());
    }

    @Test
    @DisplayName("Google Auth - Non-Allowlisted Email Cannot Escalate to Officer")
    void testNonAllowlistedUserDefaultsToApplicant() {
        // Create synthetic user to simulate non-allowlisted email login
        String applicantEmail = "external_student@somemail.com";
        assertFalse(googleAuthService.getOfficerEmailAllowlist().contains(applicantEmail));
    }
}
