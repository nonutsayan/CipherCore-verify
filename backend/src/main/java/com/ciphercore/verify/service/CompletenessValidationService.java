package com.ciphercore.verify.service;

import com.ciphercore.verify.model.enums.DocumentCategory;
import com.ciphercore.verify.model.enums.DocumentCompletenessStatus;
import com.ciphercore.verify.model.enums.FieldExtractionStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Section 12: Document Completeness Validation Engine
 * Evaluates document fields against category-specific schemas, differentiating
 * document completeness from application completeness and regulatory compliance.
 */
@Service
public class CompletenessValidationService {

    public record CategorySchema(
        DocumentCategory category,
        Set<String> requiredFields,
        Set<String> conditionallyRequiredFields,
        Set<String> optionalFields,
        boolean requiresExpiryCheck
    ) {}

    public record CompletenessOutcome(
        DocumentCompletenessStatus status,
        int requiredTotal,
        int requiredFound,
        List<String> missingRequiredFields,
        List<String> warnings,
        List<String> deficiencies,
        boolean manualReviewRequired,
        String summaryMessage
    ) {}

    private final Map<DocumentCategory, CategorySchema> schemaRegistry = new EnumMap<>(DocumentCategory.class);

    public CompletenessValidationService() {
        initSchemas();
    }

    private void initSchemas() {
        schemaRegistry.put(DocumentCategory.INSTITUTION_REGISTRATION_CERTIFICATE, new CategorySchema(
            DocumentCategory.INSTITUTION_REGISTRATION_CERTIFICATE,
            Set.of("institutionName", "registrationNumber", "issuingAuthority", "issueDate"),
            Set.of("expiryDate"),
            Set.of("registeredAddress", "signatoryName"),
            false
        ));

        schemaRegistry.put(DocumentCategory.ACCREDITATION_CERTIFICATE, new CategorySchema(
            DocumentCategory.ACCREDITATION_CERTIFICATE,
            Set.of("institutionName", "accreditingBody", "accreditationGrade", "issueDate"),
            Set.of("expiryDate"),
            Set.of("cgpa", "validityPeriod"),
            true
        ));

        schemaRegistry.put(DocumentCategory.NO_OBJECTION_CERTIFICATE, new CategorySchema(
            DocumentCategory.NO_OBJECTION_CERTIFICATE,
            Set.of("issuingAuthority", "referenceNumber", "issueDate"),
            Set.of(),
            Set.of("purpose", "expiryDate", "institutionName"),
            false // Section 9D: NOC may not have explicit expiry date
        ));

        schemaRegistry.put(DocumentCategory.APPROVAL_LETTER, new CategorySchema(
            DocumentCategory.APPROVAL_LETTER,
            Set.of("approvalOrderNumber", "academicYear", "issueDate"),
            Set.of("institutionName"),
            Set.of("approvedIntake", "approvedCourses"),
            false
        ));

        schemaRegistry.put(DocumentCategory.AFFIDAVIT, new CategorySchema(
            DocumentCategory.AFFIDAVIT,
            Set.of("deponentName", "issueDate"),
            Set.of("notaryRegNumber"),
            Set.of("institutionName", "stampReference"),
            false
        ));

        schemaRegistry.put(DocumentCategory.AFFILIATION_CERTIFICATE, new CategorySchema(
            DocumentCategory.AFFILIATION_CERTIFICATE,
            Set.of("affiliatingUniversity", "issueDate"),
            Set.of("institutionName"),
            Set.of("academicYear", "validUntil"),
            false
        ));
    }

    public CompletenessOutcome evaluate(DocumentCategory category, Map<String, FieldExtractionService.ExtractedFieldItem> extractedFields, String ocrQuality) {
        CategorySchema schema = schemaRegistry.get(category);

        // Fallback for generic or unknown categories
        if (schema == null) {
            schema = new CategorySchema(
                category,
                Set.of("issueDate"),
                Set.of(),
                Set.of("institutionName", "referenceNumber"),
                false
            );
        }

        List<String> missingRequired = new ArrayList<>();
        List<String> deficiencies = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int requiredTotal = schema.requiredFields().size();
        int requiredFound = 0;

        for (String reqField : schema.requiredFields()) {
            FieldExtractionService.ExtractedFieldItem item = extractedFields.get(reqField);
            if (item != null && item.status() == FieldExtractionStatus.EXTRACTED && item.normalizedValue() != null && !item.normalizedValue().isBlank()) {
                requiredFound++;
            } else {
                missingRequired.add(reqField);
                deficiencies.add("Mandatory schema field '" + reqField + "' is missing or could not be extracted.");
            }
        }

        // Check for ambiguous or unreadable fields
        for (Map.Entry<String, FieldExtractionService.ExtractedFieldItem> entry : extractedFields.entrySet()) {
            FieldExtractionService.ExtractedFieldItem item = entry.getValue();
            if (item.status() == FieldExtractionStatus.AMBIGUOUS) {
                warnings.add("Field '" + entry.getKey() + "' has ambiguous representation (" + item.rawValue() + ").");
            } else if (item.status() == FieldExtractionStatus.UNREADABLE) {
                warnings.add("Field '" + entry.getKey() + "' content was unreadable due to low OCR quality.");
            }
        }

        // Assess overall completeness verdict
        boolean lowOcr = "UNREADABLE".equalsIgnoreCase(ocrQuality) || "LOW".equalsIgnoreCase(ocrQuality);
        DocumentCompletenessStatus status;
        boolean manualReviewRequired = false;

        if (lowOcr) {
            status = DocumentCompletenessStatus.MANUAL_REVIEW;
            manualReviewRequired = true;
            warnings.add("Document OCR quality is low or unreadable. Human reviewer inspection required.");
        } else if (!missingRequired.isEmpty()) {
            status = DocumentCompletenessStatus.DEFICIENCY;
        } else if (!warnings.isEmpty()) {
            status = DocumentCompletenessStatus.WARNING;
        } else {
            status = DocumentCompletenessStatus.PASS;
        }

        String summary = String.format("Found %d of %d required fields for category %s. Status: %s",
            requiredFound, requiredTotal, category, status);

        return new CompletenessOutcome(
            status,
            requiredTotal,
            requiredFound,
            missingRequired,
            warnings,
            deficiencies,
            manualReviewRequired,
            summary
        );
    }
}
