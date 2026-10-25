package com.ciphercore.verify.service;

import com.ciphercore.verify.model.enums.FieldExtractionStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Section 11: Field Normalization and Data Cleaning Service
 * Normalizes dates to ISO 8601 without destroying raw evidence, preserves identifier
 * formatting and leading zeros, and assigns precise extraction status tokens.
 */
@Service
public class FieldNormalizationService {

    public record NormalizedDateResult(
        String rawDateString,
        String isoDateString,
        LocalDate parsedDate,
        FieldExtractionStatus status,
        String warning
    ) {}

    public record NormalizedIdentifierResult(
        String rawIdentifier,
        String normalizedIdentifier,
        FieldExtractionStatus status,
        boolean hasAmbiguousCharacters,
        String warning
    ) {}

    /**
     * Normalizes dates (DD-MM-YYYY, YYYY-MM-DD, DD/MM/YYYY, Month DD, YYYY) into ISO 8601 (YYYY-MM-DD).
     * Strictly preserves leading zeros and flags ambiguous formats.
     */
    public NormalizedDateResult normalizeDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return new NormalizedDateResult("", null, null, FieldExtractionStatus.NOT_FOUND, "No date value provided");
        }

        String cleaned = rawDate.trim();

        // Check for textual month format: e.g. "31 March 2026" or "March 31, 2026"
        Pattern textMonthPattern = Pattern.compile("(?i)([0-3]?[0-9])\\s+(January|February|March|April|May|June|July|August|September|October|November|December),?\\s+([12][09][0-9]{2})");
        Matcher tmMatcher = textMonthPattern.matcher(cleaned);
        if (tmMatcher.find()) {
            try {
                String dayStr = String.format("%02d", Integer.parseInt(tmMatcher.group(1)));
                String monthName = tmMatcher.group(2);
                String yearStr = tmMatcher.group(3);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
                LocalDate parsed = LocalDate.parse(dayStr + " " + monthName + " " + yearStr, dtf);
                return new NormalizedDateResult(rawDate, parsed.toString(), parsed, FieldExtractionStatus.EXTRACTED, null);
            } catch (Exception ignored) {}
        }

        // Numeric dates: DD/MM/YYYY or DD-MM-YYYY or YYYY-MM-DD
        Pattern numPattern = Pattern.compile("([0-3]?[0-9])[-/.]([0-3]?[0-9])[-/.]([12][09][0-9]{2})");
        Matcher numMatcher = numPattern.matcher(cleaned);
        if (numMatcher.find()) {
            int part1 = Integer.parseInt(numMatcher.group(1));
            int part2 = Integer.parseInt(numMatcher.group(2));
            int year = Integer.parseInt(numMatcher.group(3));

            // Check if both part1 <= 12 and part2 <= 12 (ambiguous day vs month)
            boolean ambiguous = (part1 <= 12 && part2 <= 12 && part1 != part2);

            // Default Indian regulatory interpretation: DD-MM-YYYY
            int day = part1;
            int month = part2;

            if (month > 12 && day <= 12) {
                // Was MM-DD-YYYY
                day = part2;
                month = part1;
                ambiguous = false;
            }

            try {
                LocalDate parsed = LocalDate.of(year, month, day);
                String iso = parsed.toString();
                FieldExtractionStatus status = ambiguous ? FieldExtractionStatus.AMBIGUOUS : FieldExtractionStatus.EXTRACTED;
                String warning = ambiguous ? "Ambiguous date order (" + cleaned + "). Interpreted as DD-MM-YYYY per Indian regulatory standards." : null;
                return new NormalizedDateResult(rawDate, iso, parsed, status, warning);
            } catch (Exception e) {
                return new NormalizedDateResult(rawDate, null, null, FieldExtractionStatus.UNREADABLE, "Impossible calendar date: " + cleaned);
            }
        }

        // Check ISO format YYYY-MM-DD
        Pattern isoPattern = Pattern.compile("([12][09][0-9]{2})[-/.]([0-1]?[0-9])[-/.]([0-3]?[0-9])");
        Matcher isoMatcher = isoPattern.matcher(cleaned);
        if (isoMatcher.find()) {
            int year = Integer.parseInt(isoMatcher.group(1));
            int month = Integer.parseInt(isoMatcher.group(2));
            int day = Integer.parseInt(isoMatcher.group(3));
            try {
                LocalDate parsed = LocalDate.of(year, month, day);
                return new NormalizedDateResult(rawDate, parsed.toString(), parsed, FieldExtractionStatus.EXTRACTED, null);
            } catch (Exception e) {
                return new NormalizedDateResult(rawDate, null, null, FieldExtractionStatus.UNREADABLE, "Impossible ISO date: " + cleaned);
            }
        }

        return new NormalizedDateResult(rawDate, null, null, FieldExtractionStatus.UNREADABLE, "Unable to parse valid date pattern from: " + rawDate);
    }

    /**
     * Preserves leading zeros, meaningful hyphens, and slashes.
     * Flags ambiguous characters like 'O' vs '0' or 'I' vs '1'.
     */
    public NormalizedIdentifierResult normalizeIdentifier(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return new NormalizedIdentifierResult("", "", FieldExtractionStatus.NOT_FOUND, false, "Missing identifier");
        }

        String trimmed = rawId.trim();
        // Check for ambiguous glyphs
        boolean hasAmbiguous = (trimmed.contains("O") && trimmed.matches(".*\\d+.*")) || 
                               (trimmed.contains("I") && trimmed.matches(".*\\d+.*"));

        String warning = hasAmbiguous ? "Identifier contains characters ('O' or 'I') that may be confused with numbers '0' or '1'." : null;

        return new NormalizedIdentifierResult(
            rawId, 
            trimmed, 
            FieldExtractionStatus.EXTRACTED, 
            hasAmbiguous, 
            warning
        );
    }

    /**
     * Clean and normalize institutional text without altering legal entities.
     */
    public String normalizeInstitutionName(String rawName) {
        if (rawName == null) return "";
        return rawName.trim().replaceAll("\\s+", " ");
    }
}
