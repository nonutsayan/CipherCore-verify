package com.ciphercore.verify.service;

import com.ciphercore.verify.model.AuthoritySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Section 11 & 18: Source Trust and Security Enforcement Service.
 * Implements strict SSRF protection, host allowlisting, private IP blocking,
 * and request payload bounds for authority verification interactions.
 */
@Service
public class SourceTrustService {

    private static final Logger log = LoggerFactory.getLogger(SourceTrustService.class);

    private static final List<String> DEFAULT_APPROVED_DOMAINS = List.of(
        "aicte-india.org",
        "gov.in",
        "nic.in",
        "digilocker.gov.in",
        "mahafire.gov.in",
        "mu.ac.in",
        "mumbaiuniversity.ac.in",
        "cca.gov.in",
        "ciphercore.internal"
    );

    private static final Pattern PRIVATE_IP_PATTERN = Pattern.compile(
        "^(127\\.|10\\.|192\\.168\\.|169\\.254\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|0\\.|localhost|::1).*"
    );

    /**
     * Validates whether a target URI is permitted under strict SSRF policy.
     */
    public boolean isUrlPermitted(String targetUrl, AuthoritySource source) {
        if (targetUrl == null || targetUrl.isBlank()) {
            return false;
        }

        try {
            URI uri = URI.create(targetUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                log.warn("Blocked non-http(s) protocol attempt: {}", scheme);
                return false;
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return false;
            }

            host = host.toLowerCase().trim();

            // Block loopback, private ranges, and link-local addresses
            if (PRIVATE_IP_PATTERN.matcher(host).matches()) {
                log.warn("SSRF Violation: Target host resolves to private/loopback IP: {}", host);
                return false;
            }

            // Check against source-configured or system allowlist
            List<String> allowlist = DEFAULT_APPROVED_DOMAINS;
            if (source != null && source.getApprovedDomains() != null && !source.getApprovedDomains().isBlank()) {
                allowlist = Arrays.stream(source.getApprovedDomains().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            final String checkHost = host;
            boolean domainAllowed = allowlist.stream().anyMatch(allowed ->
                checkHost.equals(allowed) || checkHost.endsWith("." + allowed)
            );

            if (!domainAllowed) {
                log.warn("SSRF Violation: Target host {} not found in allowed domains list {}", host, allowlist);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to parse target URL for SSRF validation: {}", targetUrl, e);
            return false;
        }
    }

    /**
     * Validates if maximum payload size is respected (default 10MB limit)
     */
    public boolean isPayloadWithinBounds(long sizeInBytes) {
        long maxBytes = 10 * 1024 * 1024; // 10MB
        return sizeInBytes <= maxBytes;
    }
}
