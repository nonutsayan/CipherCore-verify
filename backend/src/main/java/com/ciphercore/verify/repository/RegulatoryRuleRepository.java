package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.RegulatoryRule;
import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegulatoryRuleRepository extends JpaRepository<RegulatoryRule, Long> {
    Optional<RegulatoryRule> findByRuleCode(String ruleCode);
    List<RegulatoryRule> findByCategory(RegulatoryRuleCategory category);
    List<RegulatoryRule> findByActiveTrue();
}
