package com.ciphercore.verify.service;

import com.ciphercore.verify.model.User;
import com.ciphercore.verify.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityMonitoringService securityMonitoringService;
    private final AuditTrailService auditTrailService;

    // Active tokens map (token -> username)
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    public record LoginResult(
        boolean success,
        String token,
        User user,
        String errorMessage,
        int statusCode
    ) {}

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityMonitoringService securityMonitoringService,
                       AuditTrailService auditTrailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityMonitoringService = securityMonitoringService;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public LoginResult login(String username, String password, String ipAddress) {
        if (username == null || username.isBlank() || password == null) {
            return new LoginResult(false, null, null, "Username and password are required", 400);
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            securityMonitoringService.logSecurityEvent(
                    "LOGIN_FAILURE", username, "UNKNOWN", "AUTH", username,
                    "LOGIN", "FAILURE", "INVALID_CREDENTIALS",
                    "Failed login attempt for non-existent user '" + username + "'",
                    ipAddress
            );
            return new LoginResult(false, null, null, "Invalid username or password", 401);
        }

        User u = userOpt.get();

        // Check account locked status
        if (u.getLockedUntil() != null && u.getLockedUntil().isAfter(LocalDateTime.now())) {
            securityMonitoringService.logSecurityEvent(
                    "LOGIN_FAILURE", u.getUsername(), u.getRole().name(), "AUTH", u.getUsername(),
                    "LOGIN", "DENIED", "ACCOUNT_LOCKED",
                    "Login attempt on locked account until " + u.getLockedUntil(),
                    ipAddress
            );
            return new LoginResult(false, null, null,
                    "Account is temporarily locked due to repeated failed logins. Try again later.", 423);
        }

        // Check account active status
        if (!u.isActive()) {
            securityMonitoringService.logSecurityEvent(
                    "LOGIN_FAILURE", u.getUsername(), u.getRole().name(), "AUTH", u.getUsername(),
                    "LOGIN", "DENIED", "ACCOUNT_DEACTIVATED",
                    "Login attempt on deactivated user account '" + u.getUsername() + "'",
                    ipAddress
            );
            return new LoginResult(false, null, null,
                    "Your account has been deactivated by an AICTE Administrator.", 403);
        }

        // Verify password
        if (!passwordEncoder.matches(password, u.getPasswordHash())) {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                u.setLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(u);

            securityMonitoringService.logSecurityEvent(
                    "LOGIN_FAILURE", u.getUsername(), u.getRole().name(), "AUTH", u.getUsername(),
                    "LOGIN", "FAILURE", "INVALID_CREDENTIALS",
                    "Password mismatch (Failed attempts: " + attempts + ")",
                    ipAddress
            );
            return new LoginResult(false, null, null, "Invalid username or password", 401);
        }

        // Login success: reset lockout and update timestamp
        u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        u.setLastLoginAt(LocalDateTime.now());
        userRepository.save(u);

        String token = "bearer_ciphercore_" + u.getUsername() + "_" + System.currentTimeMillis();
        activeTokens.put(token, u.getUsername());

        securityMonitoringService.logSecurityEvent(
                "LOGIN_SUCCESS", u.getUsername(), u.getRole().name(), "AUTH", u.getUsername(),
                "LOGIN", "SUCCESS", "AUTHENTICATED",
                "User '" + u.getUsername() + "' (" + u.getRole().name() + ") authenticated successfully.",
                ipAddress
        );

        auditTrailService.recordEvent(
                u.getUsername(), u.getRole().name(), "USER_LOGGED_IN",
                "USER_SESSION", u.getUsername(),
                "User successfully logged in with role " + u.getRole().name(),
                ipAddress
        );

        return new LoginResult(true, token, u, null, 200);
    }

    public void logout(String tokenOrUsername, String ipAddress) {
        String username = tokenOrUsername;
        if (activeTokens.containsKey(tokenOrUsername)) {
            username = activeTokens.remove(tokenOrUsername);
        }

        if (username != null) {
            securityMonitoringService.logSecurityEvent(
                    "LOGOUT", username, "AUTHENTICATED", "AUTH", username,
                    "LOGOUT", "SUCCESS", "SESSION_TERMINATED",
                    "User '" + username + "' logged out.",
                    ipAddress
            );
        }
    }

    public Optional<User> resolveUser(String authHeaderOrUsername) {
        if (authHeaderOrUsername == null || authHeaderOrUsername.isBlank()) {
            return Optional.empty();
        }

        String cleaned = authHeaderOrUsername.replace("Bearer ", "").trim();

        // Check active tokens map
        if (activeTokens.containsKey(cleaned)) {
            String username = activeTokens.get(cleaned);
            return userRepository.findByUsername(username);
        }

        // Direct token pattern matching "bearer_demo_token_<username>" or "bearer_ciphercore_<username>_..."
        if (cleaned.startsWith("bearer_demo_token_")) {
            String u = cleaned.substring("bearer_demo_token_".length());
            return userRepository.findByUsername(u);
        }
        if (cleaned.startsWith("bearer_ciphercore_")) {
            String[] parts = cleaned.split("_");
            if (parts.length >= 3) {
                return userRepository.findByUsername(parts[2]);
            }
        }

        // Fallback: look up directly as username (e.g. for simple dev tests or query param)
        return userRepository.findByUsername(cleaned);
    }

    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword, String ipAddress) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User u = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, u.getPasswordHash())) {
            return false;
        }

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(u);

        auditTrailService.recordEvent(username, u.getRole().name(), "PASSWORD_CHANGED",
                "USER_CREDENTIALS", username, "User changed account password", ipAddress);
        return true;
    }
}
