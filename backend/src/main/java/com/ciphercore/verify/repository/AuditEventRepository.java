package com.ciphercore.verify.repository;

import com.ciphercore.verify.model.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findTop100ByOrderByEventTimestampDesc();
    List<AuditEvent> findTop20ByOrderByEventTimestampDesc();
    List<AuditEvent> findByEntityNameAndEntityIdOrderByEventTimestampDesc(String entityName, String entityId);
    List<AuditEvent> findByUsernameOrderByEventTimestampDesc(String username);

    Optional<AuditEvent> findTopByOrderBySequenceNumberDesc();
    List<AuditEvent> findAllByOrderBySequenceNumberAsc();
    List<AuditEvent> findBySequenceNumberIsNotNullOrderBySequenceNumberAsc();
    Optional<AuditEvent> findBySequenceNumber(Long sequenceNumber);
}
