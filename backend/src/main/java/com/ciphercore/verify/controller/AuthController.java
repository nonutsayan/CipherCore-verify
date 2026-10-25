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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record LoginRequest(String username, String password) {}
    public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {}

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
