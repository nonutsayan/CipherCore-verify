package com.ciphercore.verify.service;

import com.ciphercore.verify.model.SecurityEvent;
import com.ciphercore.verify.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SecurityMonitoringService {

    private final SecurityEventRepository securityEventRepository;
    private final AuditTrailService auditTrailService;

    public SecurityMonitoringService(SecurityEventRepository securityEventRepository,
                                     AuditTrailService auditTrailService) {
        this.securityEventRepository = securityEventRepository;
        this.auditTrailService = auditTrailService;
    }

    @Transactional
    public SecurityEvent logSecurityEvent(String eventType, String actorUsername, String actorRole,
                                         String targetType, String targetId, String action,
                                         String outcome, String reasonCode, String details,
                                         String ipAddress) {
        SecurityEvent event = new SecurityEvent(
                eventType, actorUsername, actorRole, targetType, targetId,
                action, outcome, reasonCode, details, ipAddress
        );
        return securityEventRepository.save(event);
    }

    public List<SecurityEvent> getRecentEvents(int limit) {
        return securityEventRepository.findTop50ByOrderByOccurredAtDesc();
    }

    public Map<String, Object> getSecurityKpis() {
        AuditTrailService.AuditIntegrityResult integrity = auditTrailService.verifyAuditChainIntegrity();

        long totalEvents = securityEventRepository.count();
        long failedLogins = securityEventRepository.countFailedLogins();
        long accessDenials = securityEventRepository.countAccessDenials();
        long docDenials = securityEventRepository.countDocumentAccessDenials();

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalSecurityEvents", totalEvents);
        kpis.put("failedLoginAttempts", failedLogins);
        kpis.put("accessDenialsBlocked", accessDenials);
        kpis.put("unauthorizedDocumentAccessBlocked", docDenials);
        kpis.put("auditChainValid", integrity.valid());
        kpis.put("auditChainMessage", integrity.message());
        kpis.put("auditChainRecordsCount", integrity.totalRecordsVerified());
        kpis.put("genesisHash", integrity.genesisHash());
        kpis.put("latestHash", integrity.latestHash());
        kpis.put("tamperDetected", !integrity.valid());

        return kpis;
    }
}
