package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.OfficialDecisionOutcome;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Deliverable 6: Official Scrutiny Decision Record
 * Persists the final legal regulatory disposition rendered by authorized AICTE officials.
 */
@Entity
@Table(name = "official_decisions")
public class OfficialDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long applicationId;

    @Column(length = 64)
    private String applicationRef;

    @Column(length = 255)
    private String institutionName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private OfficialDecisionOutcome outcome;

    @Column(nullable = false, length = 128)
    private String officerName;

    @Column(length = 64)
    private String officerRole;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String statutoryRationale;

    @Column(columnDefinition = "TEXT")
    private String conditionsPrecedent;

    @Column(length = 64)
    private String academicYear;

    private Boolean spotInspectionRequired = false;

    @Column(length = 128)
    private String committeeReference;

    @Column(nullable = false, length = 128)
    private String decisionIntegritySeal; // SHA-256 hash of decision terms + timestamp

    private LocalDateTime recordedAt = LocalDateTime.now();

    public OfficialDecision() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getApplicationRef() { return applicationRef; }
    public void setApplicationRef(String applicationRef) { this.applicationRef = applicationRef; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public OfficialDecisionOutcome getOutcome() { return outcome; }
    public void setOutcome(OfficialDecisionOutcome outcome) { this.outcome = outcome; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) { this.officerName = officerName; }

    public String getOfficerRole() { return officerRole; }
    public void setOfficerRole(String officerRole) { this.officerRole = officerRole; }

    public String getStatutoryRationale() { return statutoryRationale; }
    public void setStatutoryRationale(String statutoryRationale) { this.statutoryRationale = statutoryRationale; }

    public String getConditionsPrecedent() { return conditionsPrecedent; }
    public void setConditionsPrecedent(String conditionsPrecedent) { this.conditionsPrecedent = conditionsPrecedent; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Boolean getSpotInspectionRequired() { return spotInspectionRequired; }
    public void setSpotInspectionRequired(Boolean spotInspectionRequired) { this.spotInspectionRequired = spotInspectionRequired; }

    public String getCommitteeReference() { return committeeReference; }
    public void setCommitteeReference(String committeeReference) { this.committeeReference = committeeReference; }

    public String getDecisionIntegritySeal() { return decisionIntegritySeal; }
    public void setDecisionIntegritySeal(String decisionIntegritySeal) { this.decisionIntegritySeal = decisionIntegritySeal; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
