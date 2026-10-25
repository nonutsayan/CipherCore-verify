package com.ciphercore.verify.model;

import jakarta.persistence.*;

@Entity
@Table(name = "application_field_values")
public class ApplicationFieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 128)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String rawValue;

    @Column(columnDefinition = "TEXT")
    private String normalizedValue;

    @Column(length = 32)
    private String fieldType; // STRING, NUMBER, DATE, BOOLEAN

    public ApplicationFieldValue() {}

    public ApplicationFieldValue(Application application, String fieldName, String rawValue, String normalizedValue, String fieldType) {
        this.application = application;
        this.fieldName = fieldName;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.fieldType = fieldType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }

    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
}
