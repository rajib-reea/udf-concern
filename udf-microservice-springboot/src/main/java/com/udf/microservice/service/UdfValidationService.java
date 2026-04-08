package com.udf.microservice.service;

import com.udf.microservice.domain.FieldType;
import com.udf.microservice.domain.UdfDefinition;
import com.udf.microservice.repository.FieldTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UdfValidationService {

    @Autowired
    private FieldTypeRepository fieldTypeRepository;

    public void validateFieldValue(String fieldType, Object value, Map<String, Object> validationRules) {
        FieldType typeDefinition = fieldTypeRepository.findByType(fieldType)
            .orElseThrow(() -> new IllegalArgumentException("Unknown field type: " + fieldType));

        // Basic validation based on field type
        switch (fieldType.toUpperCase()) {
            case "TEXT":
                validateTextValue(value, validationRules);
                break;
            case "NUMBER":
                validateNumberValue(value, validationRules);
                break;
            case "DATE":
                validateDateValue(value, validationRules);
                break;
            case "BOOLEAN":
                validateBooleanValue(value);
                break;
            case "DROPDOWN":
                validateDropdownValue(value, validationRules);
                break;
            default:
                throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }
    }

    private void validateTextValue(Object value, Map<String, Object> rules) {
        if (value == null) return;

        String textValue = value.toString();

        if (rules.containsKey("maxLength")) {
            int maxLength = ((Number) rules.get("maxLength")).intValue();
            if (textValue.length() > maxLength) {
                throw new IllegalArgumentException("Text exceeds maximum length of " + maxLength);
            }
        }

        if (rules.containsKey("pattern")) {
            String pattern = rules.get("pattern").toString();
            if (!textValue.matches(pattern)) {
                throw new IllegalArgumentException("Text does not match required pattern");
            }
        }
    }

    private void validateNumberValue(Object value, Map<String, Object> rules) {
        if (value == null) return;

        Number numberValue;
        try {
            numberValue = (Number) value;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value must be a number");
        }

        if (rules.containsKey("min")) {
            double min = ((Number) rules.get("min")).doubleValue();
            if (numberValue.doubleValue() < min) {
                throw new IllegalArgumentException("Value must be greater than or equal to " + min);
            }
        }

        if (rules.containsKey("max")) {
            double max = ((Number) rules.get("max")).doubleValue();
            if (numberValue.doubleValue() > max) {
                throw new IllegalArgumentException("Value must be less than or equal to " + max);
            }
        }
    }

    private void validateDateValue(Object value, Map<String, Object> rules) {
        if (value == null) return;

        // Basic date validation - could be enhanced with proper date parsing
        String dateStr = value.toString();
        if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format");
        }
    }

    private void validateBooleanValue(Object value) {
        if (value == null) return;

        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Value must be a boolean");
        }
    }

    private void validateDropdownValue(Object value, Map<String, Object> rules) {
        if (value == null) return;

        if (!rules.containsKey("options")) {
            return; // No options defined, accept any value
        }

        // This would need to be implemented based on how options are stored
        // For now, just check if value is a string
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Dropdown value must be a string");
        }
    }
}