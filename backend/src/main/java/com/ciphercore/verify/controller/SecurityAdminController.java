package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.SecurityEvent;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.UserRole;
import com.ciphercore.verify.repository.UserRepository;
import com.ciphercore.verify.service.AccessControlService;
import com.ciphercore.verify.service.AuditTrailService;
import com.ciphercore.verify.service.AuthService;
import com.ciphercore.verify.service.SecurityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class SecurityAdminController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AccessControlService accessControlService;
    private final SecurityMonitoringService securityMonitoringService;
    private final AuditTrailService auditTrailService;
    private final PasswordEncoder passwordEncoder;

    public record CreateUserRequest(String username, String password, String fullName, String email,
                                   String role, String institutionName, String institutionId) {}

    public record UpdateRoleRequest(String role) {}
    public record UpdateStatusRequest(boolean active) {}

    public SecurityAdminController(UserRepository userRepository,
                                   AuthService authService,
                                   AccessControlService accessControlService,
                                   SecurityMonitoringService securityMonitoringService,
                                   AuditTrailService auditTrailService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.accessControlService = accessControlService;
        this.securityMonitoringService = securityMonitoringService;
        this.auditTrailService = auditTrailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/api/admin/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                         @RequestParam(defaultValue = "admin") String callerUsername) {
        User caller = resolveCaller(authHeader, callerUsername);
        if (!accessControlService.canManageUsers(caller)) {
            securityMonitoringService.logSecurityEvent(
                    "ACCESS_DENIED", caller != null ? caller.getUsername() : "anonymous",
                    caller != null ? caller.getRole().name() : "ANONYMOUS",
                    "ADMIN", "USERS", "LIST_USERS", "DENIED",
                    "PRIVILEGE_ESCALATION_BLOCKED",
                    "Non-administrator attempted to access user management console", "127.0.0.1"
            );
            return ResponseEntity.status(403).body(Map.of("error", "Access Denied: System Administrator privileges required"));
        }

        List<Map<String, Object>> list = userRepository.findAll().stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "username", u.getUsername(),
                "fullName", u.getFullName(),
                "email", u.getEmail(),
                "role", u.getRole().name(),
                "institutionName", u.getInstitutionName() != null ? u.getInstitutionName() : "",
                "institutionId", u.getInstitutionId() != null ? u.getInstitutionId() : "",
                "active", u.isActive(),
                "failedLoginAttempts", u.getFailedLoginAttempts(),
                "createdAt", u.getCreatedAt()
        )).toList();

        return ResponseEntity.ok(list);
    }

    @PatchMapping("/api/admin/users/{id}/status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id,
                                             @RequestBody UpdateStatusRequest req,
                                             @RequestHeader(value = "Authorization", required = false) String authHeader,
                                             @RequestParam(defaultValue = "admin") String callerUsername,
                                             HttpServletRequest httpRequest) {
        User caller = resolveCaller(authHeader, callerUsername);
        if (!accessControlService.canManageUsers(caller)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Only Administrator can toggle user status"));
        }

        Optional<User> targetOpt = userRepository.findById(id);
        if (targetOpt.isEmpty()) return ResponseEntity.notFound().build();

        User target = targetOpt.get();
        boolean old = target.isActive();
        target.setActive(req.active());
        if (req.active()) {
            target.setFailedLoginAttempts(0);
            target.setLockedUntil(null);
        }
        userRepository.save(target);

        String ip = getClientIp(httpRequest);
        auditTrailService.recordEvent(
                caller.getUsername(), caller.getRole().name(), "USER_STATUS_CHANGED",
                "USER", target.getUsername(),
                "Account active status changed from " + old + " to " + req.active() + " for user " + target.getUsername(),
                ip
        );

        securityMonitoringService.logSecurityEvent(
                "USER_STATUS_CHANGED", caller.getUsername(), caller.getRole().name(),
                "USER", target.getUsername(), "UPDATE_STATUS", "SUCCESS", "AUTHORIZED",
                "User " + target.getUsername() + " status updated to " + (req.active() ? "ACTIVE" : "DEACTIVATED"),
                ip
        );

        return ResponseEntity.ok(Map.of("id", target.getId(), "username", target.getUsername(), "active", target.isActive()));
    }

    @PutMapping("/api/admin/users/{id}/roles")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                           @RequestBody UpdateRoleRequest req,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader,
                                           @RequestParam(defaultValue = "admin") String callerUsername,
                                           HttpServletRequest httpRequest) {
        User caller = resolveCaller(authHeader, callerUsername);
        if (!accessControlService.canManageUsers(caller)) {
            securityMonitoringService.logSecurityEvent(
                    "ACCESS_DENIED", caller != null ? caller.getUsername() : "anonymous",
                    caller != null ? caller.getRole().name() : "ANONYMOUS",
                    "ADMIN", String.valueOf(id), "UPDATE_ROLE", "DENIED",
                    "PRIVILEGE_ESCALATION_BLOCKED",
                    "Non-administrator attempted to modify user role", "127.0.0.1"
            );
            return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Only Administrator can modify roles"));
        }

        Optional<User> targetOpt = userRepository.findById(id);
        if (targetOpt.isEmpty()) return ResponseEntity.notFound().build();

        User target = targetOpt.get();
        UserRole newRole;
        try {
            newRole = UserRole.valueOf(req.role());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + req.role()));
        }

        String oldRole = target.getRole().name();
        target.setRole(newRole);
        userRepository.save(target);

        String ip = getClientIp(httpRequest);
        auditTrailService.recordEvent(
                caller.getUsername(), caller.getRole().name(), "ROLE_MODIFIED",
                "USER", target.getUsername(),
                "Role for " + target.getUsername() + " modified from " + oldRole + " to " + newRole.name(),
                ip
        );

        securityMonitoringService.logSecurityEvent(
                "ROLE_MODIFIED", caller.getUsername(), caller.getRole().name(),
                "USER", target.getUsername(), "UPDATE_ROLE", "SUCCESS", "AUTHORIZED",
                "Role changed from " + oldRole + " to " + newRole.name(),
                ip
        );

        return ResponseEntity.ok(Map.of("id", target.getId(), "username", target.getUsername(), "role", target.getRole().name()));
    }

    @PostMapping("/api/admin/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader,
                                        @RequestParam(defaultValue = "admin") String callerUsername,
                                        HttpServletRequest httpRequest) {
        User caller = resolveCaller(authHeader, callerUsername);
        if (!accessControlService.canManageUsers(caller)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access Denied: Only Administrator can create users"));
        }

        if (userRepository.findByUsername(req.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        UserRole role;
        try {
            role = UserRole.valueOf(req.role());
        } catch (Exception e) {
            role = UserRole.ROLE_REGULATORY_OFFICER;
        }

        User u = new User(
                req.username(),
                passwordEncoder.encode(req.password()),
                req.fullName(),
                req.email(),
                role,
                req.institutionName(),
                req.institutionId()
        );
        userRepository.save(u);

        String ip = getClientIp(httpRequest);
        auditTrailService.recordEvent(
                caller.getUsername(), caller.getRole().name(), "USER_CREATED",
                "USER", u.getUsername(),
                "New user account created: " + u.getUsername() + " with role " + role.name(),
                ip
        );

        return ResponseEntity.ok(Map.of("id", u.getId(), "username", u.getUsername(), "role", u.getRole().name()));
    }

    @GetMapping("/api/security/events")
    public ResponseEntity<List<SecurityEvent>> getSecurityEvents(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(securityMonitoringService.getRecentEvents(limit));
    }

    @GetMapping("/api/security/kpis")
    public ResponseEntity<Map<String, Object>> getSecurityKpis() {
        return ResponseEntity.ok(securityMonitoringService.getSecurityKpis());
    }

    private User resolveCaller(String authHeader, String usernameParam) {
        String identifier = authHeader != null && !authHeader.isBlank() ? authHeader : usernameParam;
        return authService.resolveUser(identifier).orElse(null);
    }

    private String getClientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }
}
