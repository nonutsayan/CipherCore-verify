package com.ciphercore.verify.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Section 6: Secure File Ingestion and Inspection
 * Validates magic-byte signatures, size limits, page limits, encryption, and detects content-type mismatches.
 */
@Service
public class DocumentSecurityService {

    public static final long MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024; // 25 MB limit
    public static final int MAX_PAGE_COUNT = 100;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg");

    public record SecurityValidationResult(
        boolean valid, 
        String detectedMediaType, 
        int pageCount, 
        String sha256, 
        String errorMessage
    ) {}

    /**
     * Inspects uploaded file for magic bytes, size limits, and cryptographic hash.
     */
    public SecurityValidationResult validateAndInspect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new SecurityValidationResult(false, "unknown", 0, "", "File is empty or missing");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return new SecurityValidationResult(false, "unknown", 0, "", 
                "File size exceeds maximum permitted limit of 25 MB (Actual: " + (file.getSize() / 1024 / 1024) + " MB)");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return new SecurityValidationResult(false, "unknown", 0, "", "Filename is missing");
        }

        // Path traversal defense
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            return new SecurityValidationResult(false, "unknown", 0, "", "Path traversal detected in filename");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return new SecurityValidationResult(false, "unknown", 0, "", 
                "Unsupported file format '." + extension + "'. Permitted formats: PDF, PNG, JPG, JPEG");
        }

        // Magic byte verification & SHA-256 calculation
        byte[] headerBytes = new byte[16];
        String sha256;
        try (InputStream is = file.getInputStream()) {
            int read = is.read(headerBytes);
            if (read < 4) {
                return new SecurityValidationResult(false, "unknown", 0, "", "File too small or header truncated");
            }
        } catch (IOException e) {
            return new SecurityValidationResult(false, "unknown", 0, "", "Failed to read file header: " + e.getMessage());
        }

        try {
            sha256 = calculateSha256(file.getBytes());
        } catch (IOException e) {
            return new SecurityValidationResult(false, "unknown", 0, "", "Failed to compute checksum: " + e.getMessage());
        }

        // Detect genuine MIME type from header magic bytes
        String detectedType = detectMediaTypeFromMagicBytes(headerBytes);
        if ("unknown".equals(detectedType)) {
            return new SecurityValidationResult(false, "unknown", 0, sha256, 
                "File signature mismatch: File content does not match any accepted PDF, PNG, or JPEG header");
        }

        // Verify extension matches content signature
        if (("application/pdf".equals(detectedType) && !extension.equalsIgnoreCase("pdf")) ||
            ("image/png".equals(detectedType) && !extension.equalsIgnoreCase("png")) ||
            ("image/jpeg".equals(detectedType) && !(extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")))) {
            return new SecurityValidationResult(false, detectedType, 0, sha256, 
                "File extension '." + extension + "' does not match genuine content signature '" + detectedType + "'");
        }

        // If PDF, inspect page count and encryption
        int pageCount = 1;
        if ("application/pdf".equals(detectedType)) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                if (doc.isEncrypted()) {
                    return new SecurityValidationResult(false, detectedType, 0, sha256, 
                        "PDF is password-protected or encrypted. Please provide an unencrypted document");
                }
                pageCount = doc.getNumberOfPages();
                if (pageCount > MAX_PAGE_COUNT) {
                    return new SecurityValidationResult(false, detectedType, pageCount, sha256, 
                        "Document exceeds maximum page threshold (" + pageCount + " > " + MAX_PAGE_COUNT + ")");
                }
            } catch (IOException e) {
                return new SecurityValidationResult(false, detectedType, 0, sha256, "Corrupted or unreadable PDF: " + e.getMessage());
            }
        }

        return new SecurityValidationResult(true, detectedType, pageCount, sha256, null);
    }

    public String detectMediaTypeFromMagicBytes(byte[] header) {
        if (header.length < 4) return "unknown";

        // PDF: %PDF- (0x25, 0x50, 0x44, 0x46)
        if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
            return "application/pdf";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header.length >= 8 &&
            (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 && 
            (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47 &&
            (header[4] & 0xFF) == 0x0D && (header[5] & 0xFF) == 0x0A && 
            (header[6] & 0xFF) == 0x1A && (header[7] & 0xFF) == 0x0A) {
            return "image/png";
        }

        // JPEG: FF D8 FF
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        return "unknown";
    }

    public String calculateSha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    private String getFileExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot == -1) ? "" : filename.substring(dot + 1);
    }
}
