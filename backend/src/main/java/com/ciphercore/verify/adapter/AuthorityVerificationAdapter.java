package com.ciphercore.verify.adapter;

import com.ciphercore.verify.model.AuthoritySource;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.enums.AuthoritySourceType;
import com.ciphercore.verify.model.enums.DocumentCategory;

import java.util.Map;

/**
 * Section 8: Common contract for provider-independent verification adapters.
 */
public interface AuthorityVerificationAdapter {
    String getSourceId();
    AuthoritySourceType getSourceType();
    boolean supportsCategory(DocumentCategory category);
    VerificationAdapterResult verify(Document document, Map<String, String> extractedFields, AuthoritySource source);
    HealthCheckResult testConnection(AuthoritySource source);
}
