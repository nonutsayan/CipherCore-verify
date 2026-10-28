package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.User;
import com.ciphercore.verify.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final com.ciphercore.verify.service.GoogleAuthService googleAuthService;

    public AuthController(AuthService authService,
                          com.ciphercore.verify.service.GoogleAuthService googleAuthService) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
    }

    public record LoginRequest(String username, String password) {}
    public record GoogleLoginRequest(String idToken) {}
    public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {}

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        com.ciphercore.verify.service.GoogleAuthService.GoogleAuthResult res =
                googleAuthService.authenticateGoogleIdToken(request.idToken(), ip);

        if (!res.success()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", res.errorMessage(),
                    "status", 400
            ));
        }

        return ResponseEntity.ok(Map.of(
                "token", res.token(),
                "username", res.username(),
                "fullName", res.fullName(),
                "email", res.email(),
                "role", res.role(),
                "isOfficer", res.isOfficer(),
                "authMethod", "Google Identity Services (OAuth 2.0)"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        AuthService.LoginResult res = authService.login(request.username(), request.password(), ip);

        if (!res.success()) {
            return ResponseEntity.status(res.statusCode()).body(Map.of(
                    "error", res.errorMessage(),
                    "status", res.statusCode()
            ));
        }

        User u = res.user();
        return ResponseEntity.ok(Map.of(
                "token", res.token(),
                "username", u.getUsername(),
                "fullName", u.getFullName(),
                "role", u.getRole().name(),
                "email", u.getEmail(),
                "institutionName", u.getInstitutionName() != null ? u.getInstitutionName() : "",
                "active", u.isActive()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                    @RequestParam(required = false) String username,
                                    HttpServletRequest httpRequest) {
        String tokenOrUser = authHeader != null ? authHeader : username;
        authService.logout(tokenOrUser, getClientIp(httpRequest));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                            @RequestParam(defaultValue = "officer_sc401") String username) {
        String identifier = authHeader != null && !authHeader.isBlank() ? authHeader : username;
        Optional<User> uOpt = authService.resolveUser(identifier);

        return uOpt.map(u -> ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFullName(),
                "role", u.getRole().name(),
                "email", u.getEmail(),
                "institutionName", u.getInstitutionName() != null ? u.getInstitutionName() : "",
                "active", u.isActive()
        ))).orElse(ResponseEntity.status(401).body(null));
    }

    @PostMapping("/password/change")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req, HttpServletRequest httpRequest) {
        boolean ok = authService.changePassword(req.username(), req.oldPassword(), req.newPassword(), getClientIp(httpRequest));
        if (ok) {
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password incorrect or user not found"));
        }
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }
}
