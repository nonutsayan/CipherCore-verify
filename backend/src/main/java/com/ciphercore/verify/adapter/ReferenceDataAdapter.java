package com.ciphercore.verify.adapter;

/**
 * Common interface for reference data comparison adapters.
 */
public interface ReferenceDataAdapter {
    String getAdapterId();
    ReferenceRecordResult lookupInstitutionalRecord(String instituteCode, String instituteName);
    boolean isLiveIntegration();
}
