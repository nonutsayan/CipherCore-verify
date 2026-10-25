package com.ciphercore.verify.adapter;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Section 8: Clearly labelled mock institutional registry for offline demonstrations.
 */
@Component
public class MockInstitutionalRegistryAdapter implements ReferenceDataAdapter {

    private static final String SOURCE_ID = "MOCK-AICTE-CENTRAL-REGISTRY-V1";

    @Override
    public String getAdapterId() {
        return SOURCE_ID;
    }

    @Override
    public boolean isLiveIntegration() {
        return false;
    }

    @Override
    public ReferenceRecordResult lookupInstitutionalRecord(String instituteCode, String instituteName) {
        Map<String, String> compared = new HashMap<>();
        compared.put("instituteCode", instituteCode);
        compared.put("instituteName", instituteName);

        // Scenario 8 from Section 14: Institutional source unavailable test case
        if ("UNAVAILABLE-SRC".equalsIgnoreCase(instituteCode)) {
            ReferenceRecordResult unavail = new ReferenceRecordResult(
                SOURCE_ID,
                false,
                "UNAVAILABLE",
                compared,
                "External issuing authority gateway timed out after 5000ms. Check marked for Manual Review.",
                true,
                "SIMULATED_TIMEOUT: External integration offline."
            );
            return unavail;
        }

        // Scenario 7: Authorized institutional record mismatch
        if ("1-93214".equals(instituteCode) || (instituteName != null && instituteName.contains("Horizon"))) {
            compared.put("registryInstituteName", "Horizon College of Engineering & Research Trust");
            compared.put("previousEoAStatus", "APPROVED_2024");
            compared.put("approvedIntake", "360");
            compared.put("claimedIntake", "420");

            ReferenceRecordResult mismatch = new ReferenceRecordResult(
                SOURCE_ID,
                true,
                "MISMATCH",
                compared,
                "Sanctioned intake mismatch: Central master records show 360 seats approved; application claims 420 seats.",
                true,
                "DEMO ONLY: Simulated reference record comparison based on AICTE Approval Portal schema."
            );
            mismatch.setRecordReference("AICTE/MOCK/REG/2024-44219");
            mismatch.setFreshnessVersion("2024.Q4-SNAPSHOT");
            return mismatch;
        }

        // Scenario 1: Complete valid match
        compared.put("registryInstituteName", instituteName != null ? instituteName : "Apex Institute of Technology");
        compared.put("previousEoAStatus", "APPROVED_2024");
        compared.put("sanctionedIntake", "540");
        compared.put("landAreaAcres", "10.4");

        ReferenceRecordResult match = new ReferenceRecordResult(
            SOURCE_ID,
            true,
            "EXACT_MATCH",
            compared,
            "Institute Code, legal title, and historical EoA grant status match authorized AICTE registry records.",
            true,
            "DEMO ONLY: Clearly labelled synthetic benchmark dataset."
        );
        match.setRecordReference("AICTE/MOCK/REG/2024-98104");
        match.setFreshnessVersion("2025.Q1-SNAPSHOT");
        return match;
    }
}
