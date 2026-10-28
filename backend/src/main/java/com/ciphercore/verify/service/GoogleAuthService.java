package com.ciphercore.verify.service;

import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.UserRole;
import com.ciphercore.verify.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Google Identity Services / OAuth 2.0 Backend Token Verifier.
 * Authenticates Google ID tokens, validates audience and issuer claims,
 * and strictly enforces an administrative officer allowlist to prevent
 * unauthorized privilege escalation.
 */
@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    @Value("${app.google.oauth.client-id:}")
    private String clientId;

    @Value("${app.google.oauth.officer-emails:arvind.kumar.aicte@gmail.com,a.kumar@aicte-india.org,reviewer@aicte-india.org}")
    private List<String> officerEmailAllowlist;

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public GoogleAuthService(UserRepository userRepository,
                             AuthService authService,
                             PasswordEncoder passwordEncoder,
                             ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public record GoogleAuthResult(
            boolean success,
            String token,
            String username,
            String fullName,
            String email,
            String role,
            boolean isOfficer,
            String errorMessage
    ) {}

    public boolean isConfigured() {
        return clientId != null && !clientId.trim().isBlank();
    }

    public String getClientId() {
        return clientId;
    }

    public List<String> getOfficerEmailAllowlist() {
        return officerEmailAllowlist != null ? officerEmailAllowlist : List.of();
    }

    /**
     * Verifies a Google ID token from Google Identity Services and signs the user in.
     */
    @Transactional
    public GoogleAuthResult authenticateGoogleIdToken(String idToken, String ipAddress) {
        if (idToken == null || idToken.isBlank()) {
            return new GoogleAuthResult(false, null, null, null, null, null, false, "Google ID token is required");
        }

        try {
            // Verify token with Google's public tokeninfo endpoint
            String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken.trim();
            ResponseEntity<String> response = restTemplate.getForEntity(verifyUrl, String.class);

            if (response.getBody() == null) {
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Invalid Google ID token (empty response)");
            }

            JsonNode tokenInfo = objectMapper.readTree(response.getBody());

            if (tokenInfo.has("error_description")) {
                String err = tokenInfo.path("error_description").asText("Invalid token");
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Google token verification failed: " + err);
            }

            // Validate Audience (Client ID) if configured
            String aud = tokenInfo.path("aud").asText("");
            if (isConfigured() && !clientId.trim().equals(aud)) {
                log.warn("Google ID token audience mismatch: token aud '{}' vs configured '{}'", aud, clientId);
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Google token audience does not match configured Client ID");
            }

            // Validate Issuer
            String iss = tokenInfo.path("iss").asText("");
            if (!iss.equals("accounts.google.com") && !iss.equals("https://accounts.google.com")) {
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Invalid Google token issuer: " + iss);
            }

            // Validate Expiry
            long exp = tokenInfo.path("exp").asLong(0);
            long nowSec = System.currentTimeMillis() / 1000;
            if (exp < nowSec) {
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Google ID token has expired");
            }

            String email = tokenInfo.path("email").asText("").trim().toLowerCase();
            String name = tokenInfo.path("name").asText("Google User");

            if (email.isBlank()) {
                return new GoogleAuthResult(false, null, null, null, null, null, false, "Google ID token missing email claim");
            }

            // STRICT ROLE ENFORCEMENT: Only allowlisted emails receive regulatory officer roles
            boolean isOfficer = false;
            if (officerEmailAllowlist != null) {
                for (String allowed : officerEmailAllowlist) {
                    if (allowed.trim().equalsIgnoreCase(email)) {
                        isOfficer = true;
                        break;
                    }
                }
            }

            UserRole assignedRole = isOfficer ? UserRole.ROLE_REGULATORY_OFFICER : UserRole.ROLE_INSTITUTION_APPLICANT;

            // Find or create user entity
            Optional<User> existingUserOpt = userRepository.findByEmail(email);
            User user;

            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                // Update role if allowlisted
                if (isOfficer && user.getRole() != UserRole.ROLE_REGULATORY_OFFICER && user.getRole() != UserRole.ROLE_SYSTEM_ADMINISTRATOR) {
                    user.setRole(UserRole.ROLE_REGULATORY_OFFICER);
                }
                user.setFullName(name);
            } else {
                String username = "google_" + email.replaceAll("[^a-zA-Z0-9_]", "_");
                if (username.length() > 40) username = username.substring(0, 40);

                user = new User(
                        username,
                        passwordEncoder.encode(UUID.randomUUID().toString()),
                        name,
                        email,
                        assignedRole,
                        isOfficer ? "AICTE Scrutiny Committee" : "Institutional Applicant (Google OAuth)"
                );
            }

            user = userRepository.save(user);

            // Register authenticated session in AuthService
            AuthService.LoginResult sessionResult = authService.createSessionForGoogleUser(user, ipAddress);

            return new GoogleAuthResult(
                    true,
                    sessionResult.token(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole().name(),
                    isOfficer,
                    null
            );

        } catch (Exception e) {
            log.error("Google authentication error: {}", e.getMessage(), e);
            return new GoogleAuthResult(false, null, null, null, null, null, false, "Google authentication failed: " + e.getMessage());
        }
    }
}
