package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.ValidationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRunRepository extends JpaRepository<ValidationRun, Long> {
    List<ValidationRun> findByApplicationIdOrderByRunTimestampDesc(Long applicationId);
    Optional<ValidationRun> findFirstByApplicationIdOrderByRunTimestampDesc(Long applicationId);
}
