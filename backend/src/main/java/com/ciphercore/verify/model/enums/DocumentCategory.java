package com.ciphercore.verify.model.enums;

/**
 * Section 8: Automatic Document Classification Categories
 * 15 standard categories for AICTE compliance and institutional validation.
 */
public enum DocumentCategory {
    INSTITUTION_REGISTRATION_CERTIFICATE,
    ACCREDITATION_CERTIFICATE,
    RECOGNITION_CERTIFICATE,
    APPROVAL_LETTER,
    NO_OBJECTION_CERTIFICATE,
    AFFIDAVIT,
    AFFILIATION_CERTIFICATE,
    AUTHORIZATION_LETTER,
    RENEWAL_CERTIFICATE,
    VALIDITY_EXTENSION_LETTER,
    GOVERNMENT_ORDER,
    INSTITUTIONAL_DECLARATION,
    SUPPORTING_DOCUMENT,
    OTHER,
    UNKNOWN;

    public static DocumentCategory fromString(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;
        try {
            return DocumentCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
