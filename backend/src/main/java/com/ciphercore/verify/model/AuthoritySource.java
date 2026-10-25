package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.AuthoritySourceStatus;
import com.ciphercore.verify.model.enums.AuthoritySourceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Section 7 & 21A: Official Authority Source Entity
 * Configurable registry of approved issuing authorities and their verification parameters.
 */
@Entity
@Table(name = "authority_sources")
public class AuthoritySource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sourceId;

    @Column(nullable = false, length = 255)
    private String authorityName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthoritySourceType sourceType = AuthoritySourceType.OFFICIAL_API;

    @Column(length = 128)
    private String jurisdiction = "India";

    @Column(columnDefinition = "TEXT")
    private String approvedDomains = "*.aicte-india.org, *.gov.in, *.nic.in";

    @Column(columnDefinition = "TEXT")
    private String supportedDocumentCategories = "[\"ALL\"]";

    @Column(length = 64)
    private String verificationMethod = "API_RECORD_LOOKUP";

    @Column(length = 512)
    private String endpointConfiguration;

    @Column(length = 128)
    private String credentialReference; // Reference to environment key, never plaintext secret

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthoritySourceStatus sourceStatus = AuthoritySourceStatus.CONFIGURED_ACTIVE;

    @Column(length = 32)
    private String configurationVersion = "2026.1-APH";

    private int rateLimitPerMinute = 60;
    private int timeoutMillis = 5000;

    private LocalDateTime lastSuccessfulCheckAt;

    @Column(length = 512)
    private String lastError;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private boolean mockSource = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public AuthoritySource() {}

    public AuthoritySource(String sourceId, String authorityName, AuthoritySourceType sourceType,
                           String jurisdiction, String approvedDomains, String endpointConfiguration,
                           AuthoritySourceStatus sourceStatus, boolean mockSource) {
        this.sourceId = sourceId;
        this.authorityName = authorityName;
        this.sourceType = sourceType;
        this.jurisdiction = jurisdiction;
        this.approvedDomains = approvedDomains;
        this.endpointConfiguration = endpointConfiguration;
        this.sourceStatus = sourceStatus;
        this.mockSource = mockSource;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public AuthoritySource(String sourceId, String authorityName, AuthoritySourceType sourceType,
                           String endpointConfiguration, String approvedDomains, int rateLimitPerMinute,
                           int timeoutMillis, AuthoritySourceStatus sourceStatus) {
        this.sourceId = sourceId;
        this.authorityName = authorityName;
        this.sourceType = sourceType;
        this.endpointConfiguration = endpointConfiguration;
        this.approvedDomains = approvedDomains;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.timeoutMillis = timeoutMillis;
        this.sourceStatus = sourceStatus;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() { return authorityName; }
    public void setName(String name) { this.authorityName = name; }

    public String getEndpointUrl() { return endpointConfiguration; }
    public void setEndpointUrl(String url) { this.endpointConfiguration = url; }

    public AuthoritySourceStatus getStatus() { return sourceStatus; }
    public void setStatus(AuthoritySourceStatus s) { this.sourceStatus = s; }

    public int getRateLimitRpm() { return rateLimitPerMinute; }
    public void setRateLimitRpm(int rpm) { this.rateLimitPerMinute = rpm; }

    public int getTimeoutMs() { return timeoutMillis; }
    public void setTimeoutMs(int ms) { this.timeoutMillis = ms; }

    public LocalDateTime getLastHealthCheck() { return lastSuccessfulCheckAt; }
    public void setLastHealthCheck(LocalDateTime t) { this.lastSuccessfulCheckAt = t; }

    public void setEnvironment(String env) { this.adminNotes = (this.adminNotes != null ? this.adminNotes + " " : "") + "[env:" + env + "]"; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getAuthorityName() { return authorityName; }
    public void setAuthorityName(String authorityName) { this.authorityName = authorityName; }

    public AuthoritySourceType getSourceType() { return sourceType; }
    public void setSourceType(AuthoritySourceType sourceType) { this.sourceType = sourceType; }

    public String getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(String jurisdiction) { this.jurisdiction = jurisdiction; }

    public String getApprovedDomains() { return approvedDomains; }
    public void setApprovedDomains(String approvedDomains) { this.approvedDomains = approvedDomains; }

    public String getSupportedDocumentCategories() { return supportedDocumentCategories; }
    public void setSupportedDocumentCategories(String supportedDocumentCategories) { this.supportedDocumentCategories = supportedDocumentCategories; }

    public String getVerificationMethod() { return verificationMethod; }
    public void setVerificationMethod(String verificationMethod) { this.verificationMethod = verificationMethod; }

    public String getEndpointConfiguration() { return endpointConfiguration; }
    public void setEndpointConfiguration(String endpointConfiguration) { this.endpointConfiguration = endpointConfiguration; }

    public String getCredentialReference() { return credentialReference; }
    public void setCredentialReference(String credentialReference) { this.credentialReference = credentialReference; }

    public AuthoritySourceStatus getSourceStatus() { return sourceStatus; }
    public void setSourceStatus(AuthoritySourceStatus sourceStatus) { this.sourceStatus = sourceStatus; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }

    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }

    public int getTimeoutMillis() { return timeoutMillis; }
    public void setTimeoutMillis(int timeoutMillis) { this.timeoutMillis = timeoutMillis; }

    public LocalDateTime getLastSuccessfulCheckAt() { return lastSuccessfulCheckAt; }
    public void setLastSuccessfulCheckAt(LocalDateTime lastSuccessfulCheckAt) { this.lastSuccessfulCheckAt = lastSuccessfulCheckAt; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public boolean isMockSource() { return mockSource; }
    public void setMockSource(boolean mockSource) { this.mockSource = mockSource; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
