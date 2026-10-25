package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findTop50ByOrderByOccurredAtDesc();

    List<SecurityEvent> findTop20ByOrderByOccurredAtDesc();

    List<SecurityEvent> findByEventTypeOrderByOccurredAtDesc(String eventType);

    List<SecurityEvent> findByActorUsernameOrderByOccurredAtDesc(String actorUsername);

    long countByEventType(String eventType);

    long countByOutcome(String outcome);

    @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE s.eventType = 'LOGIN_FAILURE'")
    long countFailedLogins();

    @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE s.outcome = 'DENIED'")
    long countAccessDenials();

    @Query("SELECT COUNT(s) FROM SecurityEvent s WHERE s.eventType = 'DOCUMENT_ACCESS_DENIED'")
    long countDocumentAccessDenials();
}
