package com.ciphercore.verify.controller;

import com.ciphercore.verify.model.ApplicationCategory;
import com.ciphercore.verify.model.ValidationRule;
import com.ciphercore.verify.repository.ApplicationCategoryRepository;
import com.ciphercore.verify.repository.ValidationRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
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
    public ResponseEntity<ValidationRule> createRule(@RequestBody ValidationRule rule) {
        rule.setActive(true);
        ValidationRule saved = ruleRepository.save(rule);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/categories")
    public List<ApplicationCategory> listCategories() {
        return categoryRepository.findAll();
    }

    @PostMapping("/categories")
    public ResponseEntity<ApplicationCategory> createCategory(@RequestBody ApplicationCategory category) {
        category.setActive(true);
        ApplicationCategory saved = categoryRepository.save(category);
        return ResponseEntity.ok(saved);
    }
}
