package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.RegulatoryRuleVersion;
import com.ciphercore.verify.model.enums.RuleLifecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegulatoryRuleVersionRepository extends JpaRepository<RegulatoryRuleVersion, Long> {
    List<RegulatoryRuleVersion> findByRuleId(Long ruleId);
    List<RegulatoryRuleVersion> findByRuleCode(String ruleCode);
    Optional<RegulatoryRuleVersion> findByRuleCodeAndVersionNumber(String ruleCode, Integer versionNumber);
    Optional<RegulatoryRuleVersion> findByRuleCodeAndLifecycleStatus(String ruleCode, RuleLifecycleStatus lifecycleStatus);
    List<RegulatoryRuleVersion> findByLifecycleStatus(RuleLifecycleStatus lifecycleStatus);
}
