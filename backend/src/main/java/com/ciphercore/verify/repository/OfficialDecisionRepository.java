package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.OfficialDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficialDecisionRepository extends JpaRepository<OfficialDecision, Long> {

    List<OfficialDecision> findByApplicationIdOrderByRecordedAtDesc(Long applicationId);

    Optional<OfficialDecision> findFirstByApplicationIdOrderByRecordedAtDesc(Long applicationId);

    long countByAcademicYear(String academicYear);
}
