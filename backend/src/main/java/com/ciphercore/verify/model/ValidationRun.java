package com.ciphercore.verify.model;

import com.ciphercore.verify.model.enums.ExecutionState;
import com.ciphercore.verify.model.enums.FindingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_runs")
public class ValidationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private LocalDateTime runTimestamp = LocalDateTime.now();

    @Column(nullable = false, length = 64)
    private String rulesetVersion = "2026.1-APH";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FindingStatus overallStatus;

    @Column(length = 2048)
    private String overallExplanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ExecutionState executionState = ExecutionState.COMPLETED;

    @Column(length = 64)
    private String runByUsername;

    private int passedCount;
    private int warningCount;
    private int deficiencyCount;
    private int manualReviewCount;
    private int totalChecksCount;

    @Column(length = 64)
    private String reportHash; // SHA-256 integrity seal

    public ValidationRun() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public LocalDateTime getRunTimestamp() { return runTimestamp; }
    public void setRunTimestamp(LocalDateTime runTimestamp) { this.runTimestamp = runTimestamp; }

    public String getRulesetVersion() { return rulesetVersion; }
    public void setRulesetVersion(String rulesetVersion) { this.rulesetVersion = rulesetVersion; }

    public FindingStatus getOverallStatus() { return overallStatus; }
    public void setOverallStatus(FindingStatus overallStatus) { this.overallStatus = overallStatus; }

    public String getOverallExplanation() { return overallExplanation; }
    public void setOverallExplanation(String overallExplanation) { this.overallExplanation = overallExplanation; }

    public ExecutionState getExecutionState() { return executionState; }
    public void setExecutionState(ExecutionState executionState) { this.executionState = executionState; }

    public String getRunByUsername() { return runByUsername; }
    public void setRunByUsername(String runByUsername) { this.runByUsername = runByUsername; }

    public int getPassedCount() { return passedCount; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }

    public int getWarningCount() { return warningCount; }
    public void setWarningCount(int warningCount) { this.warningCount = warningCount; }

    public int getDeficiencyCount() { return deficiencyCount; }
    public void setDeficiencyCount(int deficiencyCount) { this.deficiencyCount = deficiencyCount; }

    public int getManualReviewCount() { return manualReviewCount; }
    public void setManualReviewCount(int manualReviewCount) { this.manualReviewCount = manualReviewCount; }

    public int getTotalChecksCount() { return totalChecksCount; }
    public void setTotalChecksCount(int totalChecksCount) { this.totalChecksCount = totalChecksCount; }

    public String getReportHash() { return reportHash; }
    public void setReportHash(String reportHash) { this.reportHash = reportHash; }
}
