package com.ciphercore.verify.service;

import com.ciphercore.verify.model.Application;
import com.ciphercore.verify.model.Document;
import com.ciphercore.verify.model.User;
import com.ciphercore.verify.model.enums.UserRole;
import org.springframework.stereotype.Service;

@Service
public class AccessControlService {

    private final SecurityMonitoringService securityMonitoringService;

    public AccessControlService(SecurityMonitoringService securityMonitoringService) {
        this.securityMonitoringService = securityMonitoringService;
    }

    public boolean canAccessApplication(User user, Application app) {
        if (user == null || app == null) return false;

        UserRole role = user.getRole();
        if (role == null) return false;

        // Regulatory & administrative officials can view within official mandate
        if (role == UserRole.ROLE_SYSTEM_ADMINISTRATOR || role == UserRole.ROLE_ADMIN ||
            role == UserRole.ROLE_REGULATORY_OFFICER || role == UserRole.ROLE_COMPLIANCE_OFFICER ||
            role == UserRole.ROLE_SENIOR_REVIEWER || role == UserRole.ROLE_REVIEWER ||
            role == UserRole.ROLE_AUDITOR) {
            return true;
        }

        // Institution Applicant: strictly bounded by institution name or creator identity
        if (role == UserRole.ROLE_INSTITUTION_APPLICANT || role == UserRole.ROLE_APPLICANT) {
            boolean nameMatch = app.getInstitutionName() != null &&
                    user.getInstitutionName() != null &&
                    app.getInstitutionName().trim().equalsIgnoreCase(user.getInstitutionName().trim());

            boolean creatorMatch = app.getCreatedBy() != null &&
                    app.getCreatedBy().trim().equalsIgnoreCase(user.getUsername().trim());

            if (nameMatch || creatorMatch) {
                return true;
            }

            // IDOR Attempt Detected! Log security violation event
            securityMonitoringService.logSecurityEvent(
                    "ACCESS_DENIED", user.getUsername(), role.name(),
                    "APPLICATION", String.valueOf(app.getId()),
                    "VIEW_APPLICATION", "DENIED",
                    "IDOR_CROSS_INSTITUTION_ATTEMPT",
                    "User '" + user.getUsername() + "' from '" + user.getInstitutionName() +
                    "' attempted unauthorized access to Application #" + app.getId() +
                    " belonging to '" + app.getInstitutionName() + "'. Access blocked.",
                    "127.0.0.1"
            );
            return false;
        }

        return false;
    }

    public boolean canAccessDocument(User user, Document doc) {
        if (user == null || doc == null) return false;

        UserRole role = user.getRole();
        if (role == null) return false;

        // If doc is linked to an application, enforce application boundary
        if (doc.getApplication() != null) {
            boolean appAccess = canAccessApplication(user, doc.getApplication());
            if (!appAccess) {
                securityMonitoringService.logSecurityEvent(
                        "DOCUMENT_ACCESS_DENIED", user.getUsername(), role.name(),
                        "DOCUMENT", String.valueOf(doc.getId()),
                        "ACCESS_DOCUMENT", "DENIED",
                        "CROSS_INSTITUTION_DOCUMENT_ACCESS",
                        "User '" + user.getUsername() + "' unauthorized to view document '" +
                        doc.getOriginalFilename() + "' (ID: " + doc.getId() + "). Access blocked.",
                        "127.0.0.1"
                );
            }
            return appAccess;
        }

        // Standalone document
        if (role == UserRole.ROLE_SYSTEM_ADMINISTRATOR || role == UserRole.ROLE_ADMIN ||
            role == UserRole.ROLE_REGULATORY_OFFICER || role == UserRole.ROLE_COMPLIANCE_OFFICER ||
            role == UserRole.ROLE_SENIOR_REVIEWER || role == UserRole.ROLE_REVIEWER) {
            return true;
        }

        if (doc.getUploadedBy() != null && doc.getUploadedBy().equalsIgnoreCase(user.getUsername())) {
            return true;
        }

        securityMonitoringService.logSecurityEvent(
                "DOCUMENT_ACCESS_DENIED", user.getUsername(), role.name(),
                "DOCUMENT", String.valueOf(doc.getId()),
                "ACCESS_DOCUMENT", "DENIED",
                "UNAUTHORIZED_STANDALONE_DOCUMENT_ACCESS",
                "User '" + user.getUsername() + "' denied access to standalone document ID " + doc.getId(),
                "127.0.0.1"
        );
        return false;
    }

    public boolean canCommitAdverseAction(User user) {
        if (user == null || user.getRole() == null) return false;
        UserRole r = user.getRole();
        return r == UserRole.ROLE_SENIOR_REVIEWER ||
               r == UserRole.ROLE_REGULATORY_OFFICER ||
               r == UserRole.ROLE_COMPLIANCE_OFFICER;
    }

    public boolean canManageUsers(User user) {
        if (user == null || user.getRole() == null) return false;
        return user.getRole() == UserRole.ROLE_SYSTEM_ADMINISTRATOR ||
               user.getRole() == UserRole.ROLE_ADMIN;
    }

    public boolean canModifyRules(User user) {
        if (user == null || user.getRole() == null) return false;
        return user.getRole() == UserRole.ROLE_SYSTEM_ADMINISTRATOR ||
               user.getRole() == UserRole.ROLE_ADMIN;
    }
}
