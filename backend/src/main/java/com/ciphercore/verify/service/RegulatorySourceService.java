package com.ciphercore.verify.service;

import com.ciphercore.verify.model.RegulatoryRule;
import com.ciphercore.verify.model.RegulatoryRuleVersion;
import com.ciphercore.verify.model.RegulatorySource;
import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import com.ciphercore.verify.model.enums.RuleLifecycleStatus;
import com.ciphercore.verify.repository.RegulatoryRuleRepository;
import com.ciphercore.verify.repository.RegulatoryRuleVersionRepository;
import com.ciphercore.verify.repository.RegulatorySourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Deliverable 5: Regulatory Source & Version Management Service
 * Manages authentic AICTE Approval Process Handbook (APH 2024-27) sources,
 * statutory citations, and rule lifecycle governance.
 */
@Service
public class RegulatorySourceService {

    private final RegulatorySourceRepository sourceRepository;
    private final RegulatoryRuleRepository ruleRepository;
    private final RegulatoryRuleVersionRepository versionRepository;

    public RegulatorySourceService(RegulatorySourceRepository sourceRepository,
                                   RegulatoryRuleRepository ruleRepository,
                                   RegulatoryRuleVersionRepository versionRepository) {
        this.sourceRepository = sourceRepository;
        this.ruleRepository = ruleRepository;
        this.versionRepository = versionRepository;
    }

    public List<RegulatorySource> getAllSources() {
        return sourceRepository.findAll();
    }

    public Optional<RegulatorySource> getSourceByCode(String code) {
        return sourceRepository.findBySourceCode(code);
    }

    public RegulatorySource saveSource(RegulatorySource source) {
        source.setUpdatedAt(LocalDateTime.now());
        return sourceRepository.save(source);
    }

    public List<RegulatoryRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public List<RegulatoryRule> getActiveRules() {
        return ruleRepository.findByActiveTrue();
    }

    public List<RegulatoryRule> getRulesByCategory(RegulatoryRuleCategory category) {
        return ruleRepository.findByCategory(category);
    }

    public Optional<RegulatoryRule> getRuleByCode(String ruleCode) {
        return ruleRepository.findByRuleCode(ruleCode);
    }

    public List<RegulatoryRuleVersion> getVersionsForRule(Long ruleId) {
        return versionRepository.findByRuleId(ruleId);
    }

    public Optional<RegulatoryRuleVersion> getActiveVersionForRule(String ruleCode) {
        return versionRepository.findByRuleCodeAndLifecycleStatus(ruleCode, RuleLifecycleStatus.ACTIVE);
    }

    @Transactional
    public RegulatoryRuleVersion publishNewRuleVersion(String ruleCode, RegulatoryRuleVersion newVersion, String approvedByOfficer) {
        // Find existing active versions and mark SUPERSEDED
        Optional<RegulatoryRuleVersion> currentActive = versionRepository.findByRuleCodeAndLifecycleStatus(ruleCode, RuleLifecycleStatus.ACTIVE);
        if (currentActive.isPresent()) {
            RegulatoryRuleVersion prev = currentActive.get();
            prev.setLifecycleStatus(RuleLifecycleStatus.SUPERSEDED);
            versionRepository.save(prev);
        }

        newVersion.setRuleCode(ruleCode);
        newVersion.setLifecycleStatus(RuleLifecycleStatus.ACTIVE);
        newVersion.setApprovedBy(approvedByOfficer);
        newVersion.setApprovedAt(LocalDateTime.now());
        RegulatoryRuleVersion savedVersion = versionRepository.save(newVersion);

        // Update active version number on rule entity
        ruleRepository.findByRuleCode(ruleCode).ifPresent(rule -> {
            rule.setActiveVersion(savedVersion.getVersionNumber());
            rule.setUpdatedAt(LocalDateTime.now());
            ruleRepository.save(rule);
        });

        return savedVersion;
    }
}
