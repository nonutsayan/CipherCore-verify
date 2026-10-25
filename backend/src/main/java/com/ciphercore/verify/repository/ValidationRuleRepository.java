package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {
    Optional<ValidationRule> findByRuleId(String ruleId);
    List<ValidationRule> findByActiveTrue();
    List<ValidationRule> findByCategoryCodeOrCategoryCode(String categoryCode, String allCategory);
}
