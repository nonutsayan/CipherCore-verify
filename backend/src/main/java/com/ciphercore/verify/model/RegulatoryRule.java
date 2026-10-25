package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 5: Regulatory Rule Entity
 * Represents stable rule identity across versions.
 */
@Entity
@Table(name = "regulatory_rules")
public class RegulatoryRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String ruleCode; // e.g. AICTE_FSR_TECH_001

    @Column(nullable = false, length = 255)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private RegulatoryRuleCategory category;

    @Column(nullable = false, length = 128)
    private String issuingAuthority = "AICTE";

    @Column(length = 64)
    private String jurisdiction = "NATIONAL";

    @Column(nullable = false)
    private Integer activeVersion = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String tags;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public RegulatoryRule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public RegulatoryRuleCategory getCategory() { return category; }
    public void setCategory(RegulatoryRuleCategory category) { this.category = category; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public String getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(String jurisdiction) { this.jurisdiction = jurisdiction; }

    public Integer getActiveVersion() { return activeVersion; }
    public void setActiveVersion(Integer activeVersion) { this.activeVersion = activeVersion; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
