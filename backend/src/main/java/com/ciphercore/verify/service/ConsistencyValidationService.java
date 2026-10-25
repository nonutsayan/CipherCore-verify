package com.ciphercore.verify.service;

import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.DocumentValidationResult;
import com.ciphercore.verify.model.enums.FindingStatus;
import com.ciphercore.verify.model.enums.Severity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Section 13: Consistency and Temporal Validation Engine
 * Evaluates internal document consistency, chronological dates, and detects revocation flags.
 */
@Service
public class ConsistencyValidationService {

    public List<DocumentValidationResult> validateConsistency(Document document, Map<String, FieldExtractionService.ExtractedFieldItem> extractedFields, String fullText) {
        List<DocumentValidationResult> results = new ArrayList<>();

        // 1. Temporal Check: Issue Date vs Expiry Date
        FieldExtractionService.ExtractedFieldItem issueItem = extractedFields.get("issueDate");
        FieldExtractionService.ExtractedFieldItem expiryItem = extractedFields.get("expiryDate");

        if (issueItem != null && expiryItem != null && 
            issueItem.normalizedValue() != null && expiryItem.normalizedValue() != null) {
            try {
                LocalDate issue = LocalDate.parse(issueItem.normalizedValue());
                LocalDate expiry = LocalDate.parse(expiryItem.normalizedValue());

                if (issue.isAfter(expiry)) {
                    results.add(new DocumentValidationResult(
                        document,
                        "RULE_TEMPORAL_CHRONOLOGY",
                        Severity.CRITICAL,
                        FindingStatus.DEFICIENCY,
                        "Chronological conflict: Stated issue date (" + issue + ") is after the stated expiry date (" + expiry + ").",
                        "Issue Date: " + issueItem.rawValue() + " | Expiry Date: " + expiryItem.rawValue(),
                        "issueDate, expiryDate",
                        "Reject document or flag for institutional clarification. A certificate cannot expire prior to its issue date.",
                        true
                    ));
                } else if (issue.equals(expiry)) {
                    results.add(new DocumentValidationResult(
                        document,
                        "RULE_TEMPORAL_EQUAL_DATES",
                        Severity.MODERATE,
                        FindingStatus.WARNING,
                        "Same-day validity: Issue date and expiry date are identical (" + issue + ").",
                        "Dates: " + issue,
                        "issueDate, expiryDate",
                        "Verify whether certificate was granted for single-day authorization.",
                        true
                    ));
                }
            } catch (Exception ignored) {}
        }

        // 2. Future Issue Date Check
        if (issueItem != null && issueItem.normalizedValue() != null) {
            try {
                LocalDate issue = LocalDate.parse(issueItem.normalizedValue());
                if (issue.isAfter(LocalDate.now().plusDays(1))) {
                    results.add(new DocumentValidationResult(
                        document,
                        "RULE_TEMPORAL_FUTURE_ISSUE",
                        Severity.ADVISORY,
                        FindingStatus.WARNING,
                        "Post-dated certificate: Stated issue date (" + issue + ") is in the future.",
                        "Issue Date: " + issueItem.rawValue(),
                        "issueDate",
                        "Review document execution date with issuing authority.",
                        true
                    ));
                }
            } catch (Exception ignored) {}
        }

        // 3. Document Revocation or Cancellation Check
        if (fullText != null) {
            String lower = fullText.toLowerCase();
            if (lower.contains("revoked") || lower.contains("cancelled") || lower.contains("withdrawn") || lower.contains("null and void")) {
                results.add(new DocumentValidationResult(
                    document,
                    "RULE_DOCUMENT_REVOCATION_FLAG",
                    Severity.CRITICAL,
                    FindingStatus.DEFICIENCY,
                    "Document contains explicit revocation, cancellation, or void statements.",
                    "Matched cancellation terminology in document text body.",
                    "documentStatus",
                    "Do not accept without authorized legal clearance.",
                    true
                ));
            } else if (lower.contains("superseded") || lower.contains("subject to rectification")) {
                results.add(new DocumentValidationResult(
                    document,
                    "RULE_DOCUMENT_SUPERSEDED_FLAG",
                    Severity.MODERATE,
                    FindingStatus.WARNING,
                    "Document states it has been superseded or requires rectification.",
                    "Matched 'superseded' terminology in document body.",
                    "documentStatus",
                    "Require applicant to submit the successor document or rectifying addendum.",
                    true
                ));
            }
        }

        return results;
    }
}
