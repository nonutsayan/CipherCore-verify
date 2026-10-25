package com.ciphercore.verify.adapter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Section 8: Common record response from authorized reference sources.
 */
public class ReferenceRecordResult {
    private String sourceIdentifier;
    private boolean available;
    private LocalDateTime lookupTimestamp;
    private String recordReference;
    private String matchType; // EXACT_MATCH, LIKELY_MATCH, MISMATCH, NOT_FOUND, UNAVAILABLE
    private Map<String, String> fieldsCompared;
    private String evidenceReason;
    private boolean mockSource;
    private String freshnessVersion;
    private String limitations;

    public ReferenceRecordResult() {}

    public ReferenceRecordResult(String sourceIdentifier, boolean available, String matchType, 
                                 Map<String, String> fieldsCompared, String evidenceReason, 
                                 boolean mockSource, String limitations) {
        this.sourceIdentifier = sourceIdentifier;
        this.available = available;
        this.lookupTimestamp = LocalDateTime.now();
        this.matchType = matchType;
        this.fieldsCompared = fieldsCompared;
        this.evidenceReason = evidenceReason;
        this.mockSource = mockSource;
        this.limitations = limitations;
    }

    public String getSourceIdentifier() { return sourceIdentifier; }
    public void setSourceIdentifier(String sourceIdentifier) { this.sourceIdentifier = sourceIdentifier; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalDateTime getLookupTimestamp() { return lookupTimestamp; }
    public void setLookupTimestamp(LocalDateTime lookupTimestamp) { this.lookupTimestamp = lookupTimestamp; }

    public String getRecordReference() { return recordReference; }
    public void setRecordReference(String recordReference) { this.recordReference = recordReference; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public Map<String, String> getFieldsCompared() { return fieldsCompared; }
    public void setFieldsCompared(Map<String, String> fieldsCompared) { this.fieldsCompared = fieldsCompared; }

    public String getEvidenceReason() { return evidenceReason; }
    public void setEvidenceReason(String evidenceReason) { this.evidenceReason = evidenceReason; }

    public boolean isMockSource() { return mockSource; }
    public void setMockSource(boolean mockSource) { this.mockSource = mockSource; }

    public String getFreshnessVersion() { return freshnessVersion; }
    public void setFreshnessVersion(String freshnessVersion) { this.freshnessVersion = freshnessVersion; }

    public String getLimitations() { return limitations; }
    public void setLimitations(String limitations) { this.limitations = limitations; }
}
