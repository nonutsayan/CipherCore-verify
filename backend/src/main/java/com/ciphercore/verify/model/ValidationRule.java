package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.Severity;
import jakarta.persistence.*;

@Entity
@Table(name = "validation_rules")
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String ruleId; // e.g. RULE_MANDATORY_FIRE_NOC, RULE_FACULTY_STUDENT_RATIO

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 64)
    private String categoryCode; // ALL or specific category

    @Column(nullable = false, length = 64)
    private String ruleType; // MANDATORY_FIELD, DOCUMENT_EXPIRY, RECORD_CROSS_CHECK, CHECKSUM_DUPLICATE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Severity severity = Severity.CRITICAL;

    @Column(columnDefinition = "TEXT")
    private String explanationTemplate;

    @Column(columnDefinition = "TEXT")
    private String correctiveActionTemplate;

    @Column(nullable = false, length = 32)
    private String ruleVersion = "2026.1";

    @Column(length = 255)
    private String provenance = "AICTE Approval Process Handbook 2024-2027";

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(nullable = false)
    private boolean active = true;

    public ValidationRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }

    public String getExplanationTemplate() { return explanationTemplate; }
    public void setExplanationTemplate(String explanationTemplate) { this.explanationTemplate = explanationTemplate; }

    public String getCorrectiveActionTemplate() { return correctiveActionTemplate; }
    public void setCorrectiveActionTemplate(String correctiveActionTemplate) { this.correctiveActionTemplate = correctiveActionTemplate; }

    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
