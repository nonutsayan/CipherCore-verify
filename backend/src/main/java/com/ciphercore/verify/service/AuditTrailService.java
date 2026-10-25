package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AuditEvent;
import com.ciphercore.verify.repository.AuditEventRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuditTrailService {

    public static final String GENESIS_PREV_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    private final AuditEventRepository auditEventRepository;
    private final java.util.Map<Long, String> originalDetailsBackup = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<Long, String> originalHashBackup = new java.util.concurrent.ConcurrentHashMap<>();

    public record AuditIntegrityResult(
        boolean valid,
        long totalRecordsVerified,
        Long tamperedRecordId,
        Long tamperedSequenceNumber,
        String expectedHash,
        String actualHash,
        String genesisHash,
        String latestHash,
        String message
    ) {}

    public AuditTrailService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @PostConstruct
    @Transactional
    public void ensureChainSealed() {
        List<AuditEvent> all = auditEventRepository.findAll();
        if (all.isEmpty()) return;

        // Sort chronologically by timestamp and id
        all.sort(Comparator.comparing(AuditEvent::getId));

        String prevHash = GENESIS_PREV_HASH;
        long seq = 1;
        for (AuditEvent e : all) {
            e.setSequenceNumber(seq);
            e.setPreviousHash(prevHash);
            e.setHashAlgorithm("SHA-256");
            String hash = calculateEventHash(prevHash, seq, e.getEventTimestamp(), e.getUsername(),
                    e.getAction(), e.getEntityName(), e.getEntityId(), e.getDetails());
            e.setEventHash(hash);
            prevHash = hash;
            seq++;
            auditEventRepository.save(e);
        }
    }

    @Transactional
    public synchronized AuditEvent recordEvent(String username, String userRole, String action,
                                               String entityName, String entityId, String details,
                                               String ipAddress) {
        Optional<AuditEvent> topOpt = auditEventRepository.findTopByOrderBySequenceNumberDesc();

        long nextSeq = 1L;
        String prevHash = GENESIS_PREV_HASH;

        if (topOpt.isPresent() && topOpt.get().getSequenceNumber() != null) {
            nextSeq = topOpt.get().getSequenceNumber() + 1;
            prevHash = topOpt.get().getEventHash() != null ? topOpt.get().getEventHash() : GENESIS_PREV_HASH;
        }

        LocalDateTime timestamp = LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        String currentHash = calculateEventHash(prevHash, nextSeq, timestamp, username, action, entityName, entityId, details);

        AuditEvent event = new AuditEvent(username, userRole, action, entityName, entityId, details, ipAddress);
        event.setEventTimestamp(timestamp);
        event.setSequenceNumber(nextSeq);
        event.setPreviousHash(prevHash);
        event.setEventHash(currentHash);
        event.setHashAlgorithm("SHA-256");

        return auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public AuditIntegrityResult verifyAuditChainIntegrity() {
        List<AuditEvent> events = auditEventRepository.findBySequenceNumberIsNotNullOrderBySequenceNumberAsc();
        if (events.isEmpty()) {
            return new AuditIntegrityResult(true, 0, null, null, null, null,
                    GENESIS_PREV_HASH, GENESIS_PREV_HASH, "Audit trail is empty. Ready for immutable logging.");
        }

        String expectedPrev = GENESIS_PREV_HASH;
        String genesisHash = events.get(0).getEventHash();
        String latestHash = events.get(events.size() - 1).getEventHash();

        for (int i = 0; i < events.size(); i++) {
            AuditEvent ev = events.get(i);
            long expectedSeq = (long) (i + 1);

            // Verify sequence continuity
            if (ev.getSequenceNumber() == null || ev.getSequenceNumber() != expectedSeq) {
                return new AuditIntegrityResult(false, i, ev.getId(), ev.getSequenceNumber(),
                        "Sequence " + expectedSeq, String.valueOf(ev.getSequenceNumber()),
                        genesisHash, latestHash,
                        "Sequence gap or mismatch detected at record ID " + ev.getId());
            }

            // Verify previous hash pointer
            if (!expectedPrev.equalsIgnoreCase(ev.getPreviousHash())) {
                return new AuditIntegrityResult(false, i, ev.getId(), ev.getSequenceNumber(),
                        expectedPrev, ev.getPreviousHash(),
                        genesisHash, latestHash,
                        "Previous hash link broken at sequence #" + ev.getSequenceNumber() +
                        ". Possible record deletion or reordering.");
            }

            // Verify SHA-256 integrity hash of this record's contents
            String recalculatedHash = calculateEventHash(ev.getPreviousHash(), ev.getSequenceNumber(),
                    ev.getEventTimestamp(), ev.getUsername(), ev.getAction(), ev.getEntityName(),
                    ev.getEntityId(), ev.getDetails());

            if (!recalculatedHash.equalsIgnoreCase(ev.getEventHash())) {
                return new AuditIntegrityResult(false, i, ev.getId(), ev.getSequenceNumber(),
                        recalculatedHash, ev.getEventHash(),
                        genesisHash, latestHash,
                        "Data tamper detected at sequence #" + ev.getSequenceNumber() +
                        " (Entity: " + ev.getEntityName() + " " + ev.getEntityId() + "). Cryptographic digest mismatch!");
            }

            expectedPrev = ev.getEventHash();
        }

        return new AuditIntegrityResult(true, events.size(), null, null, null, null,
                genesisHash, latestHash,
                "Cryptographic hash chain intact. Verified " + events.size() +
                " audit events with zero tampering detected (SHA-256 seal valid).");
    }

    @Transactional
    public boolean simulateTamper(Long sequenceNumber, String modifiedDetails) {
        Optional<AuditEvent> opt = auditEventRepository.findBySequenceNumber(sequenceNumber);
        if (opt.isPresent()) {
            AuditEvent ev = opt.get();
            // Cache original details and hash for instantaneous restoration
            originalDetailsBackup.putIfAbsent(sequenceNumber, ev.getDetails() != null ? ev.getDetails() : "");
            originalHashBackup.putIfAbsent(sequenceNumber, ev.getEventHash());
            
            ev.setDetails(modifiedDetails);
            // Intentionally DO NOT update eventHash so that the tamper is caught by cryptographic verification!
            auditEventRepository.save(ev);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean restoreTamper(Long sequenceNumber, String originalDetails) {
        Optional<AuditEvent> opt = auditEventRepository.findBySequenceNumber(sequenceNumber);
        if (opt.isPresent()) {
            AuditEvent ev = opt.get();
            if (originalDetailsBackup.containsKey(sequenceNumber)) {
                ev.setDetails(originalDetailsBackup.get(sequenceNumber));
                ev.setEventHash(originalHashBackup.get(sequenceNumber));
                originalDetailsBackup.remove(sequenceNumber);
                originalHashBackup.remove(sequenceNumber);
            } else {
                ev.setDetails(originalDetails);
                String hash = calculateEventHash(ev.getPreviousHash(), ev.getSequenceNumber(),
                        ev.getEventTimestamp(), ev.getUsername(), ev.getAction(), ev.getEntityName(),
                        ev.getEntityId(), originalDetails);
                ev.setEventHash(hash);
            }
            auditEventRepository.save(ev);
            return true;
        }
        return false;
    }

    public String exportAuditTrailCsv(String actorUsername) {
        // Record export audit event
        recordEvent(actorUsername != null ? actorUsername : "system", "SYSTEM_ADMINISTRATOR",
                "AUDIT_TRAIL_EXPORTED", "AUDIT_LOG", "ALL",
                "Official audit log exported to CSV by " + actorUsername, "127.0.0.1");

        List<AuditEvent> events = auditEventRepository.findAllByOrderBySequenceNumberAsc();
        StringBuilder sb = new StringBuilder();
        sb.append("Sequence,Timestamp,Username,UserRole,Action,EntityName,EntityId,Details,PreviousHash,EventHash,HashAlgorithm\n");

        for (AuditEvent e : events) {
            sb.append(e.getSequenceNumber() != null ? e.getSequenceNumber() : "").append(",");
            sb.append(escapeCsv(String.valueOf(e.getEventTimestamp()))).append(",");
            sb.append(escapeCsv(e.getUsername())).append(",");
            sb.append(escapeCsv(e.getUserRole())).append(",");
            sb.append(escapeCsv(e.getAction())).append(",");
            sb.append(escapeCsv(e.getEntityName())).append(",");
            sb.append(escapeCsv(e.getEntityId())).append(",");
            sb.append(escapeCsv(e.getDetails())).append(",");
            sb.append(escapeCsv(e.getPreviousHash())).append(",");
            sb.append(escapeCsv(e.getEventHash())).append(",");
            sb.append(escapeCsv(e.getHashAlgorithm())).append("\n");
        }

        return sb.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "\"\"";
        // Formula injection protection: prefix =, +, -, @ with single quote
        String sanitized = val;
        if (sanitized.startsWith("=") || sanitized.startsWith("+") ||
            sanitized.startsWith("-") || sanitized.startsWith("@")) {
            sanitized = "'" + sanitized;
        }
        return "\"" + sanitized.replace("\"", "\"\"") + "\"";
    }

    public static String calculateEventHash(String prevHash, Long seq, LocalDateTime ts,
                                            String user, String act, String entity,
                                            String entityId, String det) {
        String tsStr = ts != null ? ts.truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString() : "";
        String payload = (prevHash != null ? prevHash : GENESIS_PREV_HASH) + "|" +
                (seq != null ? seq : 0L) + "|" +
                tsStr + "|" +
                (user != null ? user : "") + "|" +
                (act != null ? act : "") + "|" +
                (entity != null ? entity : "") + "|" +
                (entityId != null ? entityId : "") + "|" +
                (det != null ? det : "");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }
}
