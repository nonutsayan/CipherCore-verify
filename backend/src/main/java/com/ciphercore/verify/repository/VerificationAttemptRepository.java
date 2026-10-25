package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.VerificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationAttemptRepository extends JpaRepository<VerificationAttempt, Long> {
    List<VerificationAttempt> findByVerificationJobIdOrderByAttemptNumberAsc(Long verificationJobId);
}
