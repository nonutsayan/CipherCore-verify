package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.DocumentAccessEvent;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.repository.DocumentAccessEventRepository;
import com.ciphercore.verify.repository.DocumentRepository;
import com.ciphercore.verify.service.AccessControlService;
import com.ciphercore.verify.service.AuthService;
import com.ciphercore.verify.service.SecureDocumentStorageService;
import com.ciphercore.verify.service.SecureDocumentStorageService.DocumentDownloadResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class SecureDocumentController {

    private final SecureDocumentStorageService documentStorageService;
    private final DocumentRepository documentRepository;
    private final DocumentAccessEventRepository accessEventRepository;
    private final AccessControlService accessControlService;
    private final AuthService authService;

    public SecureDocumentController(SecureDocumentStorageService documentStorageService,
                                    DocumentRepository documentRepository,
                                    DocumentAccessEventRepository accessEventRepository,
                                    AccessControlService accessControlService,
                                    AuthService authService) {
        this.documentStorageService = documentStorageService;
        this.documentRepository = documentRepository;
        this.accessEventRepository = accessEventRepository;
        this.accessControlService = accessControlService;
        this.authService = authService;
    }

    @GetMapping("/api/documents/{id}/metadata")
    public ResponseEntity<?> getDocumentMetadata(@PathVariable Long id,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                @RequestParam(defaultValue = "officer_sc401") String username) {
        Optional<Document> docOpt = documentRepository.findById(id);
        if (docOpt.isEmpty()) return ResponseEntity.notFound().build();

        Document doc = docOpt.get();
        User caller = resolveUser(authHeader, username);

        if (!accessControlService.canAccessDocument(caller, doc)) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Access Denied: You do not have permission to view metadata for this document (Cross-institution IDOR blocked)",
                    "status", 403
            ));
        }

        return ResponseEntity.ok(Map.of(
                "id", doc.getId(),
                "originalFilename", doc.getOriginalFilename(),
                "documentType", doc.getDocumentType(),
                "mimeType", doc.getMimeType(),
                "fileSizeBytes", doc.getFileSizeBytes(),
                "sha256Checksum", doc.getSha256Checksum(),
                "uploadedBy", doc.getUploadedBy() != null ? doc.getUploadedBy() : "system",
                "authenticityStatus", doc.getAuthenticityStatus(),
                "completenessStatus", doc.getCompletenessStatus(),
                "applicationId", doc.getApplicationId() != null ? doc.getApplicationId() : 0L
        ));
    }

    @GetMapping("/api/documents/{id}/secure-download")
    public ResponseEntity<?> downloadDocument(@PathVariable Long id,
                                             @RequestParam(required = false) String token,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestParam(required = false) String username,
                                             HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);

        // If short-lived token provided
        if (token != null && !token.isBlank()) {
            DocumentDownloadResult res = documentStorageService.downloadDocumentWithToken(token, ip);
            if (!res.allowed()) {
                return ResponseEntity.status(res.statusCode()).body(Map.of("error", res.errorMessage(), "status", res.statusCode()));
            }
            return buildFileResponse(res);
        }

        // Otherwise verify authenticated user
        User caller = resolveUser(authHeader, username);
        DocumentDownloadResult res = documentStorageService.retrieveDocument(id, caller, "DOWNLOAD", ip);

        if (!res.allowed()) {
            return ResponseEntity.status(res.statusCode()).body(Map.of(
                    "error", res.errorMessage(),
                    "status", res.statusCode(),
                    "documentId", id
            ));
        }

        return buildFileResponse(res);
    }

    @PostMapping("/api/documents/{id}/token")
    public ResponseEntity<?> generateDownloadToken(@PathVariable Long id,
                                                   @RequestHeader(value = "Authorization", required = false) String authHeader,
                                                   @RequestParam(defaultValue = "officer_sc401") String username) {
        User caller = resolveUser(authHeader, username);
        if (caller == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        try {
            String token = documentStorageService.createTemporaryDownloadToken(id, caller, 5); // 5 minutes validity
            return ResponseEntity.ok(Map.of(
                    "documentId", id,
                    "token", token,
                    "expiresInMinutes", 5,
                    "downloadUrl", "/api/documents/" + id + "/secure-download?token=" + token
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/documents/{id}/access-history")
    public ResponseEntity<List<DocumentAccessEvent>> getDocumentAccessHistory(@PathVariable Long id) {
        return ResponseEntity.ok(accessEventRepository.findByDocumentIdOrderByAccessedAtDesc(id));
    }

    private ResponseEntity<?> buildFileResponse(DocumentDownloadResult res) {
        MediaType mediaType;
        try {
            mediaType = (res.mimeType() != null && !res.mimeType().isBlank())
                    ? MediaType.parseMediaType(res.mimeType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + res.filename() + "\"")
                .contentType(mediaType)
                .body(res.content());
    }

    private User resolveUser(String authHeader, String usernameParam) {
        String identifier = authHeader != null && !authHeader.isBlank() ? authHeader : usernameParam;
        return authService.resolveUser(identifier).orElse(null);
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }
}
