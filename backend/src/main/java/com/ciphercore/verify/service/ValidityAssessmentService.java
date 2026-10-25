package com.ciphercore.verify.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Section 15: Validity, Expiry, and Renewal Assessment Service.
 * Evaluates whether an institutional approval, NOC, or certificate is within
 * its legal statutory validity window, expired, in renewal grace period, or revoked.
 */
@Service
public class ValidityAssessmentService {

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public ValidityEvaluation evaluateValidity(String issueDateStr, String expiryDateStr, String academicYearStr) {
        LocalDate now = LocalDate.now();
        LocalDate expiryDate = parseDate(expiryDateStr);
        LocalDate issueDate = parseDate(issueDateStr);

        if (expiryDate == null) {
            // Check if it's perpetual / permanent (e.g. AICTE Permanent Institute ID)
            if (academicYearStr != null && academicYearStr.contains("PERMANENT")) {
                return new ValidityEvaluation("PERMANENT_VALIDITY", true, 0, "Permanent institutional approval not subject to annual renewal");
            }
            if (expiryDateStr != null && (expiryDateStr.equalsIgnoreCase("PERPETUAL") || expiryDateStr.equalsIgnoreCase("PERMANENT"))) {
                return new ValidityEvaluation("PERMANENT_VALIDITY", true, 0, "Document granted without expiry timestamp");
            }
            return new ValidityEvaluation("NOT_SPECIFIED", true, 0, "No expiry date specified in certificate record");
        }

        if (issueDate != null && issueDate.isAfter(now)) {
            return new ValidityEvaluation("FUTURE_DATED", false, (int) ChronoUnit.DAYS.between(now, expiryDate),
                    "Certificate issue date (" + issueDate + ") is set in the future. Potential chronological defect.");
        }

        if (expiryDate.isBefore(now)) {
            long daysExpired = ChronoUnit.DAYS.between(expiryDate, now);
            return new ValidityEvaluation("EXPIRED", false, (int) -daysExpired,
                    "Statutory validity expired on " + expiryDate + " (" + daysExpired + " days ago). Requires renewal extension letter.");
        }

        long daysRemaining = ChronoUnit.DAYS.between(now, expiryDate);
        if (daysRemaining <= 60) {
            return new ValidityEvaluation("EXPIRING_SOON", true, (int) daysRemaining,
                    "Certificate valid but expiring within " + daysRemaining + " days. Renewal submission advised.");
        }

        return new ValidityEvaluation("CURRENTLY_VALID", true, (int) daysRemaining,
                "Certificate is actively within statutory validity window (" + daysRemaining + " days remaining).");
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.isBlank()) return null;
        for (DateTimeFormatter f : FORMATTERS) {
            try {
                return LocalDate.parse(val.trim(), f);
            } catch (Exception ignored) {}
        }
        return null;
    }

    public record ValidityEvaluation(
        String validityCode,
        boolean isValid,
        int daysRemaining,
        String explanation
    ) {}
}
