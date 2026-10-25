package com.ciphercore.verify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Section 6A: File inspection and secure storage.
 */
@Service
public class DocumentStorageService {

    private final Path uploadLocation;

    public DocumentStorageService(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage folder", e);
        }
    }

    public record StoredFileInfo(String storedFilename, String fullPath, String sha256Checksum, long fileSizeBytes, String mimeType) {}

    public StoredFileInfo storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new SecurityException("Invalid filename path traversal attempt");
        }

        // Validate extension
        String lowerName = originalFilename.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".png") && !lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg")) {
            throw new IllegalArgumentException("Unsupported file type: only PDF, PNG, JPEG allowed.");
        }

        // Calculate SHA-256 and store securely
        String sha256;
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            sha256 = HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 digest unavailable", e);
        }

        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path targetPath = this.uploadLocation.resolve(storedFilename).normalize();

        try (InputStream is = file.getInputStream()) {
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return new StoredFileInfo(storedFilename, targetPath.toString(), sha256, file.getSize(), file.getContentType());
    }

    public Path loadFile(String storedFilename) {
        return this.uploadLocation.resolve(storedFilename).normalize();
    }
}
