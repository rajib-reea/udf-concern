package com.udf.microservice.application;

import com.udf.microservice.domain.EntityUdfValue;
import com.udf.microservice.domain.UdfDefinition;
import com.udf.microservice.repository.EntityUdfValueRepository;
import com.udf.microservice.repository.UdfDefinitionRepository;
import com.udf.microservice.service.ProjectionEngineService;
import com.udf.microservice.service.UdfValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UdfValueAppService {

    @Autowired
    private EntityUdfValueRepository entityUdfValueRepository;

    @Autowired
    private UdfDefinitionRepository udfDefinitionRepository;

    @Autowired
    private UdfValidationService udfValidationService;

    @Autowired
    private ProjectionEngineService projectionEngineService;

    public void setUdfValue(String entityType, Long entityId, Long fieldId, Object value) {
        UdfDefinition definition = udfDefinitionRepository.findById(fieldId)
            .orElseThrow(() -> new IllegalArgumentException("UDF definition not found: " + fieldId));

        // Validate the value
        udfValidationService.validateFieldValue(definition.getFieldType(), value, definition.getValidationRules());

        // Convert value to string for storage
        String stringValue = value != null ? value.toString() : null;

        // Find existing value or create new
        EntityUdfValue existingValue = entityUdfValueRepository
            .findByEntityTypeAndEntityIdAndFieldId(entityType, entityId, fieldId);

        if (existingValue != null) {
            existingValue.setValue(stringValue);
            entityUdfValueRepository.save(existingValue);
        } else {
            EntityUdfValue newValue = new EntityUdfValue(entityType, entityId, fieldId, stringValue);
            entityUdfValueRepository.save(newValue);
        }

        // Sync to reporting table
        projectionEngineService.syncEntityToReporting(entityType, entityId);
    }

    public void setMultipleUdfValues(String entityType, Long entityId, Map<Long, Object> fieldValues) {
        for (Map.Entry<Long, Object> entry : fieldValues.entrySet()) {
            setUdfValue(entityType, entityId, entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> getUdfValues(String entityType, Long entityId) {
        List<EntityUdfValue> values = entityUdfValueRepository.findByEntityTypeAndEntityId(entityType, entityId);

        // Get field definitions for field names
        List<Long> fieldIds = values.stream().map(EntityUdfValue::getFieldId).collect(Collectors.toList());
        Map<Long, UdfDefinition> definitions = udfDefinitionRepository.findAllById(fieldIds)
            .stream()
            .collect(Collectors.toMap(UdfDefinition::getId, def -> def));

        Map<String, Object> result = new HashMap<>();
        for (EntityUdfValue value : values) {
            UdfDefinition definition = definitions.get(value.getFieldId());
            if (definition != null) {
                Object typedValue = parseValue(value.getValue(), definition.getFieldType());
                result.put(definition.getFieldName(), typedValue);
            }
        }

        return result;
    }

    public Object getUdfValue(String entityType, Long entityId, String fieldName) {
        Optional<UdfDefinition> definitionOpt = udfDefinitionRepository
            .findByEntityTypeAndFieldName(entityType, fieldName);

        if (definitionOpt.isEmpty()) {
            throw new IllegalArgumentException("Field not found: " + fieldName + " for entity: " + entityType);
        }

        UdfDefinition definition = definitionOpt.get();
        EntityUdfValue value = entityUdfValueRepository
            .findByEntityTypeAndEntityIdAndFieldId(entityType, entityId, definition.getId());

        if (value == null) {
            return definition.getDefaultValue() != null ?
                parseValue(definition.getDefaultValue(), definition.getFieldType()) : null;
        }

        return parseValue(value.getValue(), definition.getFieldType());
    }

    public void deleteUdfValue(String entityType, Long entityId, Long fieldId) {
        EntityUdfValue value = entityUdfValueRepository
            .findByEntityTypeAndEntityIdAndFieldId(entityType, entityId, fieldId);

        if (value != null) {
            entityUdfValueRepository.delete(value);
            // Sync to reporting table
            projectionEngineService.syncEntityToReporting(entityType, entityId);
        }
    }

    private Object parseValue(String value, String fieldType) {
        if (value == null) return null;

        switch (fieldType.toUpperCase()) {
            case "TEXT":
                return value;
            case "NUMBER":
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            case "BOOLEAN":
                return Boolean.parseBoolean(value);
            case "DATE":
                return value; // Could parse to LocalDate if needed
            case "DROPDOWN":
                return value;
            default:
                return value;
        }
    }
}