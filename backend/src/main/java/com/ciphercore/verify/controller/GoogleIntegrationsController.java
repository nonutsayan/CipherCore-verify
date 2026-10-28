package com.ciphercore.verify.controller;

import com.ciphercore.verify.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Google Cloud & Gemini API Integrations.
 * Provides service health status, live connectivity testing,
 * OCR execution, and structured AI document intelligence.
 */
@RestController
@RequestMapping("/api/integrations/google")
@CrossOrigin(origins = "*")
@Tag(name = "Google AI & Cloud Integrations", description = "Endpoints for Google Cloud Vision OCR, Gemini AI, and Identity Services")
public class GoogleIntegrationsController {

    private final GoogleVisionOcrService visionOcrService;
    private final GeminiDocumentAnalysisService geminiService;
    private final GoogleAuthService googleAuthService;
    private final SignatureVerificationService signatureService;

    public GoogleIntegrationsController(GoogleVisionOcrService visionOcrService,
                                        GeminiDocumentAnalysisService geminiService,
                                        GoogleAuthService googleAuthService,
                                        SignatureVerificationService signatureService) {
        this.visionOcrService = visionOcrService;
        this.geminiService = geminiService;
        this.googleAuthService = googleAuthService;
        this.signatureService = signatureService;
    }

    @GetMapping("/status")
    @Operation(summary = "Get readiness and configuration status of all Google integrations")
    public ResponseEntity<Map<String, Object>> getIntegrationsStatus() {
        Map<String, Object> res = new LinkedHashMap<>();

        // 1. Google Cloud Vision API
        Map<String, Object> vision = new LinkedHashMap<>();
        vision.put("serviceName", "Google Cloud Vision API");
        vision.put("feature", "DOCUMENT_TEXT_DETECTION");
        vision.put("configured", visionOcrService.isConfigured());
        vision.put("status", visionOcrService.isConfigured() ? "CONFIGURED_AND_READY" : "SETUP_REQUIRED");
        vision.put("endpoint", visionOcrService.getEndpoint());
        res.put("vision", vision);

        // 2. Google Gemini API
        Map<String, Object> gemini = new LinkedHashMap<>();
        gemini.put("serviceName", "Google Gemini API");
        gemini.put("feature", "AI Document Intelligence & Compliance Verification");
        gemini.put("configured", geminiService.isConfigured());
        gemini.put("status", geminiService.isConfigured() ? "CONFIGURED_AND_READY" : "SETUP_REQUIRED");
        gemini.put("model", geminiService.getModelName());
        res.put("gemini", gemini);

        // 3. Google Identity Services
        Map<String, Object> oauth = new LinkedHashMap<>();
        oauth.put("serviceName", "Google Identity Services (OAuth 2.0)");
        oauth.put("feature", "Reviewer Authentication & Role Enforcement");
        oauth.put("configured", googleAuthService.isConfigured());
        oauth.put("status", googleAuthService.isConfigured() ? "CONFIGURED_AND_READY" : "SETUP_REQUIRED");
        oauth.put("allowlistedOfficerEmailsCount", googleAuthService.getOfficerEmailAllowlist().size());
        res.put("googleSignIn", oauth);

        // 4. Digital Signature & Cryptographic Engine
        Map<String, Object> signature = new LinkedHashMap<>();
        signature.put("serviceName", "Cryptographic Digital Signature Engine");
        signature.put("engine", "Apache PDFBox 3.0.3 + Bouncy Castle CMS 1.78.1");
        signature.put("status", "ACTIVE");
        signature.put("features", List.of("PKCS#7 / CMS Decryption", "X.509 Chain Verification", "ByteRange Tampering Check", "Handwritten Signature Detection"));
        res.put("digitalSignature", signature);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/test")
    @Operation(summary = "Execute real live connectivity test against Google Vision and Gemini APIs")
    public ResponseEntity<Map<String, Object>> testLiveConnections() {
        Map<String, Object> results = new LinkedHashMap<>();

        // Test Vision API
        GoogleVisionOcrService.VisionTestResult visionTest = visionOcrService.testConnection();
        results.put("vision", visionTest);

        // Test Gemini API
        GeminiDocumentAnalysisService.GeminiTestResult geminiTest = geminiService.testConnection();
        results.put("gemini", geminiTest);

        boolean allWorking = visionTest.connected() && geminiTest.connected();
        results.put("allWorking", allWorking);
        results.put("summary", allWorking
                ? "All configured Google APIs are responding normally with low latency."
                : "One or more Google APIs require configuration or returned an error.");

        return ResponseEntity.ok(results);
    }

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Perform real OCR on an uploaded file via Google Cloud Vision API")
    public ResponseEntity<?> runOcrOnFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("ocr_upload_", "_" + file.getOriginalFilename());
            try (InputStream in = file.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            GoogleVisionOcrService.VisionOcrResult ocrRes =
                    visionOcrService.extractDocumentText(tempFile, file.getContentType());

            return ResponseEntity.ok(ocrRes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "OCR failed: " + e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public record GeminiAnalyzeTextRequest(String text, String filename, String expectedCategory) {}

    @PostMapping("/gemini/analyze")
    @Operation(summary = "Analyze document text using Google Gemini API")
    public ResponseEntity<?> analyzeTextWithGemini(@RequestBody GeminiAnalyzeTextRequest req) {
        if (req.text() == null || req.text().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Document text is required for Gemini analysis"));
        }

        GeminiDocumentAnalysisService.GeminiAnalysisResult res =
                geminiService.analyzeDocumentContent(req.text(), req.filename(), req.expectedCategory());

        return ResponseEntity.ok(res);
    }

    @PostMapping(value = "/signature/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Inspect cryptographic and handwritten signatures in an uploaded PDF")
    public ResponseEntity<?> inspectSignatures(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }

        File tempFile = null;
        try {
            tempFile = File.createTempFile("sig_upload_", "_" + file.getOriginalFilename());
            try (InputStream in = file.getInputStream(); FileOutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            SignatureVerificationService.DetailedSignatureReport report =
                    signatureService.inspectPdfSignaturesDetailed(tempFile);

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Signature verification failed: " + e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
