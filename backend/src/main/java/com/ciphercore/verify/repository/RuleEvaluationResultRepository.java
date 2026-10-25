package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.RuleEvaluationResult;
import com.ciphercore.verify.model.enums.RegulatoryRuleCategory;
import com.ciphercore.verify.model.enums.RuleEvaluationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleEvaluationResultRepository extends JpaRepository<RuleEvaluationResult, Long> {
    List<RuleEvaluationResult> findByAssessmentId(Long assessmentId);
    List<RuleEvaluationResult> findByApplicationId(Long applicationId);
    List<RuleEvaluationResult> findByAssessmentIdAndStatus(Long assessmentId, RuleEvaluationStatus status);
    List<RuleEvaluationResult> findByAssessmentIdAndRuleCategory(Long assessmentId, RegulatoryRuleCategory category);
}
