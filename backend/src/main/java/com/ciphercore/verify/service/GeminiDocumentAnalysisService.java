package com.ciphercore.verify.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Real Google Gemini API Integration for Document Intelligence & Compliance Verification.
 * Performs zero-shot and few-shot structured analysis, category classification,
 * field extraction with verbatim evidence, date consistency, and anomaly detection.
 */
@Service
public class GeminiDocumentAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(GeminiDocumentAnalysisService.class);

    @Value("${app.google.gemini.api-key:}")
    private String apiKey;

    @Value("${app.google.gemini.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${app.google.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta}")
    private String endpoint;

    @Value("${app.google.gemini.temperature:0.1}")
    private double temperature;

    @Value("${app.google.gemini.timeout-seconds:45}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GeminiDocumentAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExtractedField(
            String value,
            Double confidence,
            String evidenceQuote,
            Integer pageNumber,
            String normalizationNote
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ComplianceFinding(
            String ruleCode,
            String status, // PASS, WARNING, DEFICIENCY, MANUAL_REVIEW
            String finding,
            String evidenceQuote,
            Integer pageNumber
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiAnalysisPayload(
            String documentType,
            Double classificationConfidence,
            String classificationEvidence,
            ExtractedField institutionName,
            ExtractedField documentNumber,
            ExtractedField issuingAuthority,
            ExtractedField issueDate,
            ExtractedField expiryDate,
            ExtractedField registrationNumber,
            ExtractedField referenceNumber,
            ExtractedField signatoryName,
            ExtractedField signatoryDesignation,
            Map<String, String> keyNormMetrics,
            List<ComplianceFinding> complianceFindings,
            List<String> missingRequiredFields,
            List<String> factsExtracted,
            List<String> aiInterpretations,
            List<String> manualReviewItems,
            Boolean isExpiredAccordingToCurrentDate,
            String statutoryLimitationNote
    ) {}

    public record GeminiAnalysisResult(
            boolean success,
            String status, // SUCCESS, NOT_CONFIGURED, API_ERROR, PARSE_ERROR, RATE_LIMITED
            String message,
            long processingTimeMs,
            String modelUsed,
            GeminiAnalysisPayload analysis,
            String rawJsonResponse
    ) {}

    public record GeminiTestResult(
            boolean configured,
            boolean connected,
            long latencyMs,
            String model,
            String message
    ) {}

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isBlank();
    }

    public String getModelName() {
        return modelName;
    }

    /**
     * Analyzes OCR-extracted text using Google Gemini API to extract structured fields,
     * detect temporal/regulatory discrepancies, and cite evidence.
     */
    public GeminiAnalysisResult analyzeDocumentContent(String ocrText, String filename, String expectedCategory) {
        long startTime = System.currentTimeMillis();

        if (!isConfigured()) {
            return new GeminiAnalysisResult(
                    false,
                    "NOT_CONFIGURED",
                    "Google Gemini API key is not configured. Set GEMINI_API_KEY in environment or application.yml.",
                    0,
                    modelName,
                    null,
                    null
            );
        }

        if (ocrText == null || ocrText.trim().isBlank()) {
            return new GeminiAnalysisResult(
                    false,
                    "EMPTY_INPUT",
                    "Supplied document OCR text is empty; cannot perform Gemini analysis.",
                    System.currentTimeMillis() - startTime,
                    modelName,
                    null,
                    null
            );
        }

        try {
            LocalDate today = LocalDate.now();
            String prompt = buildSystemAndUserPrompt(ocrText, filename, expectedCategory, today);

            // Construct Gemini REST JSON Payload
            Map<String, Object> requestBody = new LinkedHashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> userContent = new LinkedHashMap<>();
            userContent.put("role", "user");
            userContent.put("parts", List.of(Map.of("text", prompt)));
            contents.add(userContent);
            requestBody.put("contents", contents);

            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("temperature", temperature);
            generationConfig.put("responseMimeType", "application/json");
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String url = endpoint + "/models/" + modelName.trim() + ":generateContent?key=" + apiKey.trim();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            long elapsed = System.currentTimeMillis() - startTime;
            if (response.getBody() == null) {
                return new GeminiAnalysisResult(false, "EMPTY_RESPONSE", "Received empty response from Gemini API", elapsed, modelName, null, null);
            }

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.size() == 0) {
                return new GeminiAnalysisResult(false, "NO_CANDIDATE", "Gemini returned no candidates (possible safety filter block)", elapsed, modelName, null, response.getBody());
            }

            String candidateText = candidates.get(0).path("content").path("parts").get(0).path("text").asText("");
            if (candidateText.isBlank()) {
                return new GeminiAnalysisResult(false, "NO_TEXT", "Gemini candidate returned empty text", elapsed, modelName, null, response.getBody());
            }

            // Parse and Schema-Validate JSON Output
            GeminiAnalysisPayload payload = parseAndValidateJson(candidateText);

            // Enrich payload with deterministic current-date expiry validation
            payload = enforceDeterministicDateRules(payload, today);

            return new GeminiAnalysisResult(
                    true,
                    "SUCCESS",
                    "Gemini AI document intelligence completed successfully.",
                    elapsed,
                    modelName,
                    payload,
                    candidateText
            );

        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String errorBody = e.getResponseBodyAsString();
            String errorMsg = extractGoogleErrorMessage(errorBody, e.getMessage());
            log.error("Google Gemini API error ({}): {}", e.getStatusCode(), errorMsg);
            String status = e.getStatusCode().value() == 429 ? "RATE_LIMITED" : "API_ERROR";
            return new GeminiAnalysisResult(
                    false,
                    status,
                    "Google Gemini API error (" + e.getStatusCode() + "): " + errorMsg,
                    elapsed,
                    modelName,
                    null,
                    errorBody
            );
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Gemini document intelligence exception: {}", e.getMessage(), e);
            return new GeminiAnalysisResult(
                    false,
                    "PROCESSING_FAILED",
                    "Gemini document intelligence failed: " + e.getMessage(),
                    elapsed,
                    modelName,
                    null,
                    null
            );
        }
    }

    /**
     * Builds comprehensive prompt directing Gemini to extract facts, distinguish AI deductions,
     * quote verbatim source evidence, and maintain strict statutory disclaimers.
     */
    private String buildSystemAndUserPrompt(String ocrText, String filename, String expectedCategory, LocalDate today) {
        return """
            You are the specialized Regulatory Document Intelligence Engine for CipherCore Verify (AICTE Problem Statement SIH26261).
            Current Calendar Date for Statutory Expiry Calculation: %s.
            Filename: %s.
            Expected Document Category (if known): %s.

            TASK:
            Analyze the following extracted document OCR text. You must output ONLY a valid JSON object matching the exact schema below.

            STRICT REGULATORY PRINCIPLES:
            1. Evidence-Linked: Every extracted field must include the exact verbatim quote ('evidenceQuote') and page number where it appears.
            2. Non-Fabrication: If a field is not present in the text, set value to null and confidence to 0.0. Never invent certificate numbers, dates, or names.
            3. Fact vs Interpretation: Explicitly separate 'factsExtracted' (literal facts stated in text) from 'aiInterpretations' (inferences made by AI).
            4. Manual Review: Any ambiguous, partially illegible, or conflicting information must be flagged in 'manualReviewItems'.
            5. Authenticity Boundary: AI cannot independently declare a document legally authentic; only official issuer verification can prove authenticity.
            6. Expiry Calculation: Compare any detected expiry date against current date %s. If expiry date is before current date, mark isExpiredAccordingToCurrentDate=true.

            REQUIRED JSON SCHEMA:
            {
              "documentType": "string (one of: MANDATORY_LAND_DOCUMENT, FIRE_SAFETY_NOC, AFFILIATION_LETTER, AICTE_APPROVAL_LETTER, SOCIETY_REGISTRATION, AUDITED_BALANCE_SHEET, BUILDING_PLAN_APPROVAL, OTHER)",
              "classificationConfidence": number (0.0 to 1.0),
              "classificationEvidence": "verbatim excerpt indicating document type",
              "institutionName": { "value": "string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "documentNumber": { "value": "string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "issuingAuthority": { "value": "string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "issueDate": { "value": "YYYY-MM-DD or raw string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "expiryDate": { "value": "YYYY-MM-DD or raw string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "registrationNumber": { "value": "string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "referenceNumber": { "value": "string or null", "confidence": number, "evidenceQuote": "string or null", "pageNumber": number or null },
              "signatoryName": { "value": "string or null", "evidenceQuote": "string or null", "pageNumber": number or null },
              "signatoryDesignation": { "value": "string or null", "evidenceQuote": "string or null", "pageNumber": number or null },
              "keyNormMetrics": {
                "acreageOrArea": "string or null",
                "intakeOrSeats": "string or null",
                "workstationsCount": "string or null",
                "surveyNumber": "string or null"
              },
              "complianceFindings": [
                {
                  "ruleCode": "string (e.g. RULE-DATE-EXPIRY, RULE-MANDATORY-FIELDS, RULE-NAME-CONSISTENCY)",
                  "status": "PASS or WARNING or DEFICIENCY or MANUAL_REVIEW",
                  "finding": "description of finding",
                  "evidenceQuote": "verbatim text excerpt",
                  "pageNumber": 1
                }
              ],
              "missingRequiredFields": ["field1", "field2"],
              "factsExtracted": ["fact 1", "fact 2"],
              "aiInterpretations": ["interpretation 1", "interpretation 2"],
              "manualReviewItems": ["item 1 needing human officer scrutiny"],
              "isExpiredAccordingToCurrentDate": boolean or null,
              "statutoryLimitationNote": "AI extraction is decision-support only. Final legal scrutiny rests with AICTE Scrutiny Committee."
            }

            DOCUMENT TEXT TO ANALYZE:
            \"\"\"
            %s
            \"\"\"
            """.formatted(today, filename != null ? filename : "Document", expectedCategory != null ? expectedCategory : "UNKNOWN", today, ocrText);
    }

    private GeminiAnalysisPayload parseAndValidateJson(String rawJson) throws Exception {
        String cleanJson = rawJson.trim();
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        return objectMapper.readValue(cleanJson, GeminiAnalysisPayload.class);
    }

    private GeminiAnalysisPayload enforceDeterministicDateRules(GeminiAnalysisPayload payload, LocalDate today) {
        if (payload == null) return null;

        Boolean isExpired = payload.isExpiredAccordingToCurrentDate();
        if (payload.expiryDate() != null && payload.expiryDate().value() != null) {
            String expStr = payload.expiryDate().value().trim();
            try {
                LocalDate expDate = LocalDate.parse(expStr, DateTimeFormatter.ISO_LOCAL_DATE);
                isExpired = expDate.isBefore(today);
            } catch (Exception ignored) {
                // Keep LLM determination if non-ISO string
            }
        }

        return new GeminiAnalysisPayload(
                payload.documentType(),
                payload.classificationConfidence(),
                payload.classificationEvidence(),
                payload.institutionName(),
                payload.documentNumber(),
                payload.issuingAuthority(),
                payload.issueDate(),
                payload.expiryDate(),
                payload.registrationNumber(),
                payload.referenceNumber(),
                payload.signatoryName(),
                payload.signatoryDesignation(),
                payload.keyNormMetrics(),
                payload.complianceFindings(),
                payload.missingRequiredFields(),
                payload.factsExtracted(),
                payload.aiInterpretations(),
                payload.manualReviewItems(),
                isExpired,
                "AI document intelligence is advisory and evidence-linked. Official statutory approval rests exclusively with AICTE Scrutiny Officers."
        );
    }

    /**
     * Diagnostic test: performs a lightweight ping to verify API key validity and model availability.
     */
    public GeminiTestResult testConnection() {
        if (!isConfigured()) {
            return new GeminiTestResult(
                    false,
                    false,
                    0,
                    modelName,
                    "Google Gemini API key is not configured."
            );
        }

        long start = System.currentTimeMillis();
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", "Ping. Respond with {\"status\":\"ok\"}"))
                    )),
                    "generationConfig", Map.of(
                            "temperature", 0.0,
                            "responseMimeType", "application/json"
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String url = endpoint + "/models/" + modelName.trim() + ":generateContent?key=" + apiKey.trim();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            long latency = System.currentTimeMillis() - start;
            return new GeminiTestResult(
                    true,
                    true,
                    latency,
                    modelName,
                    "Connected successfully to Google Gemini (" + modelName + "). Latency: " + latency + "ms."
            );
        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            String msg = extractGoogleErrorMessage(e.getResponseBodyAsString(), e.getMessage());
            return new GeminiTestResult(
                    true,
                    false,
                    latency,
                    modelName,
                    "Google Gemini API rejected request (" + e.getStatusCode() + "): " + msg
            );
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new GeminiTestResult(
                    true,
                    false,
                    latency,
                    modelName,
                    "Failed to connect to Google Gemini API: " + e.getMessage()
            );
        }
    }

    private String extractGoogleErrorMessage(String responseBody, String defaultMsg) {
        if (responseBody == null || responseBody.isBlank()) return defaultMsg;
        try {
            JsonNode tree = objectMapper.readTree(responseBody);
            JsonNode error = tree.path("error");
            if (error.has("message")) {
                return error.path("message").asText();
            }
        } catch (Exception ignored) {}
        return responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
    }
}
