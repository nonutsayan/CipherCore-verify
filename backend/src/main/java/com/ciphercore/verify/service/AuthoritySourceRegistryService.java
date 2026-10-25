package com.ciphercore.verify.service;

import com.ciphercore.verify.adapter.AuthorityVerificationAdapter;
import com.ciphercore.verify.adapter.HealthCheckResult;
import com.ciphercore.verify.model.AuthoritySource;
import com.ciphercore.verify.model.enums.AuthoritySourceStatus;
import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.repository.AuthoritySourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Section 6, 7 & 21: Authority Source Registry Service.
 * Manages official authority gateways, endpoint configuration, credential references,
 * health status monitoring, and adapter resolution.
 */
@Service
public class AuthoritySourceRegistryService {

    private static final Logger log = LoggerFactory.getLogger(AuthoritySourceRegistryService.class);

    private final AuthoritySourceRepository authoritySourceRepository;
    private final Map<String, AuthorityVerificationAdapter> adapterMap = new HashMap<>();

    public AuthoritySourceRegistryService(
            AuthoritySourceRepository authoritySourceRepository,
            List<AuthorityVerificationAdapter> adapters
    ) {
        this.authoritySourceRepository = authoritySourceRepository;
        for (AuthorityVerificationAdapter adapter : adapters) {
            this.adapterMap.put(adapter.getSourceId(), adapter);
        }
    }

    public List<AuthoritySource> getAllSources() {
        return authoritySourceRepository.findAll();
    }

    public Optional<AuthoritySource> getSource(String sourceId) {
        return authoritySourceRepository.findBySourceId(sourceId);
    }

    public AuthoritySource saveSource(AuthoritySource source) {
        return authoritySourceRepository.save(source);
    }

    public AuthorityVerificationAdapter getAdapter(String sourceId) {
        return adapterMap.get(sourceId);
    }

    public List<AuthorityVerificationAdapter> getAdaptersForCategory(DocumentCategory category) {
        List<AuthorityVerificationAdapter> matched = new ArrayList<>();
        for (AuthorityVerificationAdapter adapter : adapterMap.values()) {
            if (adapter.supportsCategory(category)) {
                matched.add(adapter);
            }
        }
        return matched;
    }

    /**
     * Executes real-time health and connectivity check against an authority gateway.
     */
    public HealthCheckResult testConnection(String sourceId) {
        Optional<AuthoritySource> sourceOpt = authoritySourceRepository.findBySourceId(sourceId);
        AuthoritySource source = sourceOpt.orElseGet(() -> {
            AuthoritySource placeholder = new AuthoritySource();
            placeholder.setSourceId(sourceId);
            placeholder.setName(sourceId);
            return placeholder;
        });

        AuthorityVerificationAdapter adapter = adapterMap.get(sourceId);
        if (adapter == null) {
            return new HealthCheckResult(
                sourceId,
                AuthoritySourceStatus.UNCONFIGURED,
                false,
                0,
                "No verification adapter implemented or registered for sourceId: " + sourceId,
                null
            );
        }

        try {
            long start = System.currentTimeMillis();
            HealthCheckResult res = adapter.testConnection(source);
            long elapsed = System.currentTimeMillis() - start;

            // Update source status in repository
            source.setLastHealthCheck(LocalDateTime.now());
            source.setStatus(res.status());
            authoritySourceRepository.save(source);

            return new HealthCheckResult(sourceId, res.status(), res.healthy(), (int) elapsed, res.message(), res.endpoint());
        } catch (Exception e) {
            log.error("Error conducting health check on source {}", sourceId, e);
            source.setLastHealthCheck(LocalDateTime.now());
            source.setStatus(AuthoritySourceStatus.OFFLINE);
            authoritySourceRepository.save(source);
            return new HealthCheckResult(sourceId, AuthoritySourceStatus.OFFLINE, false, 0, "Connection error: " + e.getMessage(), null);
        }
    }
}
