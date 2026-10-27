package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ApplicationCategory;
import com.ciphercore.verify.model.ValidationRule;
import com.ciphercore.verify.repository.ApplicationCategoryRepository;
import com.ciphercore.verify.repository.ValidationRuleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ValidationRuleRepository ruleRepository;
    private final ApplicationCategoryRepository categoryRepository;

    public AdminController(ValidationRuleRepository ruleRepository,
                           ApplicationCategoryRepository categoryRepository) {
        this.ruleRepository = ruleRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/rules")
    public List<ValidationRule> listRules() {
        return ruleRepository.findAll();
    }

    @PostMapping("/rules")
    public ResponseEntity<?> createRule(
            @RequestBody ValidationRule rule,
            @RequestHeader(value = "X-Officer-Role", defaultValue = "ANONYMOUS") String officerRole) {
        if (!isAuthorizedAdminRole(officerRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access Denied: Only authorized administrators or compliance officers can modify validation rules.",
                    "status", 403,
                    "providedRole", String.valueOf(officerRole)
            ));
        }
        rule.setActive(true);
        ValidationRule saved = ruleRepository.save(rule);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/categories")
    public List<ApplicationCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(
            @RequestBody ApplicationCategory category,
            @RequestHeader(value = "X-Officer-Role", defaultValue = "ANONYMOUS") String officerRole) {
        if (!isAuthorizedAdminRole(officerRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "Access Denied: Only authorized administrators can configure application categories.",
                    "status", 403,
                    "providedRole", String.valueOf(officerRole)
            ));
        }
        category.setActive(true);
        ApplicationCategory saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }

    private boolean isAuthorizedAdminRole(String role) {
        if (role == null) return false;
        String r = role.toUpperCase();
        return r.contains("ADMIN") || r.contains("OFFICER") || r.contains("CHAIR");
    }
}
