package com.ciphercore.verify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

/**
 * Real Google Cloud Vision API Integration for Optical Character Recognition (OCR).
 * Performs DOCUMENT_TEXT_DETECTION on scanned certificates, affiliation letters,
 * land deeds, NOCs, and multi-page institutional PDF dossiers.
 */
@Service
public class GoogleVisionOcrService {

    private static final Logger log = LoggerFactory.getLogger(GoogleVisionOcrService.class);

    @Value("${app.google.vision.api-key:}")
    private String apiKey;

    @Value("${app.google.vision.endpoint:https://vision.googleapis.com/v1}")
    private String endpoint;

    @Value("${app.google.vision.max-pages-per-doc:10}")
    private int maxPagesPerDoc;

    @Value("${app.google.vision.timeout-seconds:30}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GoogleVisionOcrService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public record WordBoundingBox(
            String text,
            int left,
            int top,
            int width,
            int height,
            float confidence
    ) {}

    public record VisionPageResult(
            int pageNumber,
            String text,
            int wordCount,
            float confidence,
            List<WordBoundingBox> words
    ) {}

    public record VisionOcrResult(
            boolean success,
            String status, // SUCCESS, NOT_CONFIGURED, API_ERROR, UNREADABLE_FILE
            String message,
            long processingTimeMs,
            int totalPages,
            List<VisionPageResult> pages,
            String consolidatedText
    ) {}

    public record VisionTestResult(
            boolean configured,
            boolean connected,
            long latencyMs,
            String message,
            String provider
    ) {}

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isBlank();
    }

    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Executes real DOCUMENT_TEXT_DETECTION across an uploaded file.
     * Handles single images directly, and renders multi-page PDFs page-by-page.
     */
    public VisionOcrResult extractDocumentText(File file, String mimeType) {
        long startTime = System.currentTimeMillis();

        if (!isConfigured()) {
            return new VisionOcrResult(
                    false,
                    "NOT_CONFIGURED",
                    "Google Cloud Vision API key is not configured. Set GOOGLE_VISION_API_KEY in environment or application.yml.",
                    0,
                    0,
                    List.of(),
                    ""
            );
        }

        if (file == null || !file.exists() || file.length() == 0) {
            return new VisionOcrResult(
                    false,
                    "UNREADABLE_FILE",
                    "Supplied document file is missing, empty, or inaccessible.",
                    System.currentTimeMillis() - startTime,
                    0,
                    List.of(),
                    ""
            );
        }

        try {
            if ("application/pdf".equalsIgnoreCase(mimeType) || file.getName().toLowerCase().endsWith(".pdf")) {
                return processPdfPages(file, startTime);
            } else {
                return processSingleImage(file, mimeType, startTime);
            }
        } catch (HttpStatusCodeException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            String errorBody = e.getResponseBodyAsString();
            String extractedMessage = extractGoogleErrorMessage(errorBody, e.getMessage());
            log.error("Google Cloud Vision API HTTP error ({}): {}", e.getStatusCode(), extractedMessage);
            return new VisionOcrResult(
                    false,
                    "API_ERROR",
                    "Google Cloud Vision API rejected request (" + e.getStatusCode() + "): " + extractedMessage,
                    elapsed,
                    0,
                    List.of(),
                    ""
            );
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Google Cloud Vision OCR processing exception: {}", e.getMessage(), e);
            return new VisionOcrResult(
                    false,
                    "PROCESSING_FAILED",
                    "Failed to process document with Google Cloud Vision OCR: " + e.getMessage(),
                    elapsed,
                    0,
                    List.of(),
                    ""
            );
        }
    }

    private VisionOcrResult processSingleImage(File file, String mimeType, long startTime) throws IOException {
        byte[] imageBytes = Files.readAllBytes(file.toPath());
        VisionPageResult pageResult = annotateImageBytes(imageBytes, 1);

        long elapsed = System.currentTimeMillis() - startTime;
        return new VisionOcrResult(
                true,
                "SUCCESS",
                "Google Cloud Vision DOCUMENT_TEXT_DETECTION completed successfully.",
                elapsed,
                1,
                List.of(pageResult),
                pageResult.text()
        );
    }

    private VisionOcrResult processPdfPages(File file, long startTime) throws IOException {
        List<VisionPageResult> pageResults = new ArrayList<>();
        StringBuilder consolidated = new StringBuilder();

        try (PDDocument pd = Loader.loadPDF(file)) {
            int actualPages = pd.getNumberOfPages();
            int pagesToProcess = Math.min(actualPages, maxPagesPerDoc);
            PDFRenderer renderer = new PDFRenderer(pd);

            for (int pageIdx = 0; pageIdx < pagesToProcess; pageIdx++) {
                // Render page to 150 DPI RGB BufferedImage for OCR
                BufferedImage bim = renderer.renderImageWithDPI(pageIdx, 150, ImageType.RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bim, "jpeg", baos);
                byte[] pageJpegBytes = baos.toByteArray();

                VisionPageResult pRes = annotateImageBytes(pageJpegBytes, pageIdx + 1);
                pageResults.add(pRes);
                if (!pRes.text().isBlank()) {
                    if (consolidated.length() > 0) consolidated.append("\n\n--- PAGE ").append(pageIdx + 1).append(" ---\n");
                    consolidated.append(pRes.text());
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            return new VisionOcrResult(
                    true,
                    "SUCCESS",
                    "Google Cloud Vision OCR processed " + pageResults.size() + " PDF page(s) successfully.",
                    elapsed,
                    actualPages,
                    pageResults,
                    consolidated.toString()
            );
        }
    }

    private VisionPageResult annotateImageBytes(byte[] imageBytes, int pageNumber) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Build Vision API request payload
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode requests = root.putArray("requests");
        ObjectNode request = requests.addObject();

        ObjectNode imageNode = request.putObject("image");
        imageNode.put("content", base64Image);

        ArrayNode features = request.putArray("features");
        ObjectNode feat = features.addObject();
        feat.put("type", "DOCUMENT_TEXT_DETECTION");
        feat.put("maxResults", 50);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);

        String url = endpoint + "/images:annotate?key=" + apiKey.trim();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getBody() == null) {
            return new VisionPageResult(pageNumber, "", 0, 0.0f, List.of());
        }

        JsonNode responseJson = objectMapper.readTree(response.getBody());
        JsonNode firstResponse = responseJson.path("responses").path(0);

        if (firstResponse.has("error")) {
            String errorMsg = firstResponse.path("error").path("message").asText("Unknown Vision API error");
            throw new IOException("Google Vision API error: " + errorMsg);
        }

        String fullText = firstResponse.path("fullTextAnnotation").path("text").asText("");
        if (fullText.isBlank()) {
            fullText = firstResponse.path("textAnnotations").path(0).path("description").asText("");
        }

        List<WordBoundingBox> words = new ArrayList<>();
        JsonNode pagesNode = firstResponse.path("fullTextAnnotation").path("pages");
        float avgConfidence = 0.90f;

        if (pagesNode.isArray() && pagesNode.size() > 0) {
            JsonNode page0 = pagesNode.get(0);
            if (page0.has("confidence")) {
                avgConfidence = (float) page0.path("confidence").asDouble(0.90);
            }

            JsonNode blocks = page0.path("blocks");
            if (blocks.isArray()) {
                for (JsonNode block : blocks) {
                    JsonNode paragraphs = block.path("paragraphs");
                    if (paragraphs.isArray()) {
                        for (JsonNode para : paragraphs) {
                            JsonNode wordsNode = para.path("words");
                            if (wordsNode.isArray()) {
                                for (JsonNode w : wordsNode) {
                                    StringBuilder wordStr = new StringBuilder();
                                    JsonNode symbols = w.path("symbols");
                                    if (symbols.isArray()) {
                                        for (JsonNode s : symbols) {
                                            wordStr.append(s.path("text").asText(""));
                                        }
                                    }
                                    float wordConf = (float) w.path("confidence").asDouble(avgConfidence);
                                    JsonNode vertices = w.path("boundingBox").path("vertices");
                                    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                                    int maxX = 0, maxY = 0;
                                    if (vertices.isArray()) {
                                        for (JsonNode v : vertices) {
                                            int x = v.path("x").asInt(0);
                                            int y = v.path("y").asInt(0);
                                            minX = Math.min(minX, x);
                                            minY = Math.min(minY, y);
                                            maxX = Math.max(maxX, x);
                                            maxY = Math.max(maxY, y);
                                        }
                                    }
                                    if (minX == Integer.MAX_VALUE) minX = 0;
                                    if (minY == Integer.MAX_VALUE) minY = 0;
                                    int width = Math.max(0, maxX - minX);
                                    int height = Math.max(0, maxY - minY);

                                    words.add(new WordBoundingBox(
                                            wordStr.toString(),
                                            minX,
                                            minY,
                                            width,
                                            height,
                                            wordConf
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        }

        int wordCount = words.isEmpty() ? (fullText.isBlank() ? 0 : fullText.split("\\s+").length) : words.size();
        return new VisionPageResult(pageNumber, fullText, wordCount, avgConfidence, words);
    }

    /**
     * Diagnostic test: performs a lightweight live API call to verify key validity.
     */
    public VisionTestResult testConnection() {
        if (!isConfigured()) {
            return new VisionTestResult(
                    false,
                    false,
                    0,
                    "Google Cloud Vision API key is not configured.",
                    "Google Cloud Vision (DOCUMENT_TEXT_DETECTION)"
            );
        }

        long start = System.currentTimeMillis();
        try {
            // Minimal 1x1 transparent GIF image base64
            byte[] tinyImage = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
            annotateImageBytes(tinyImage, 1);
            long latency = System.currentTimeMillis() - start;
            return new VisionTestResult(
                    true,
                    true,
                    latency,
                    "Connected successfully. Google Cloud Vision API is responding normally (" + latency + "ms).",
                    "Google Cloud Vision (DOCUMENT_TEXT_DETECTION)"
            );
        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            String msg = extractGoogleErrorMessage(e.getResponseBodyAsString(), e.getMessage());
            return new VisionTestResult(
                    true,
                    false,
                    latency,
                    "Google Cloud Vision API rejected request (" + e.getStatusCode() + "): " + msg,
                    "Google Cloud Vision (DOCUMENT_TEXT_DETECTION)"
            );
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            return new VisionTestResult(
                    true,
                    false,
                    latency,
                    "Failed to connect to Google Cloud Vision API: " + e.getMessage(),
                    "Google Cloud Vision (DOCUMENT_TEXT_DETECTION)"
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
