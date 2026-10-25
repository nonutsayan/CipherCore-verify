package com.ciphercore.verify.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_categories")
public class ApplicationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String code; // e.g. EOA_TECHNICAL, VARIATION_INTAKE, NEW_PROGRAMME

    @Column(nullable = false, length = 128)
    private String displayName;

    @Column(length = 1024)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String keywordsJson; // classification hints

    @Column(nullable = false, length = 32)
    private String version = "2026.1";

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ApplicationCategory() {}

    public ApplicationCategory(String code, String displayName, String description, String keywordsJson) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.keywordsJson = keywordsJson;
        this.version = "2026.1";
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getKeywordsJson() { return keywordsJson; }
    public void setKeywordsJson(String keywordsJson) { this.keywordsJson = keywordsJson; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
