package com.ciphercore.verify.adapter;

import com.ciphercore.verify.model.enums.AuthoritySourceStatus;

/**
 * Section 7 & 8: Health check response contract for authority gateways.
 */
public record HealthCheckResult(
    String sourceId,
    AuthoritySourceStatus status,
    boolean healthy,
    int latencyMs,
    String message,
    String endpoint
) {
    public HealthCheckResult(boolean healthy, AuthoritySourceStatus status, int latencyMs, String message, String endpoint) {
        this(null, status, healthy, latencyMs, message, endpoint);
    }

    public boolean isHealthy() {
        return healthy;
    }
}
