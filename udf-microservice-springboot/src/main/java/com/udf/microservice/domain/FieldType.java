package com.udf.microservice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "field_type")
public class FieldType {

    @Id
    @Column(nullable = false)
    private String type;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "validation_rules", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> validationRules;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public FieldType() {}

    public FieldType(String type, String displayName, Map<String, Object> validationRules) {
        this.type = type;
        this.displayName = displayName;
        this.validationRules = validationRules;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Map<String, Object> getValidationRules() { return validationRules; }
    public void setValidationRules(Map<String, Object> validationRules) { this.validationRules = validationRules; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}