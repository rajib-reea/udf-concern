package com.udf.microservice.application;

import com.udf.microservice.domain.UdfDefinition;
import com.udf.microservice.repository.UdfDefinitionRepository;
import com.udf.microservice.service.ProjectionEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UdfDefinitionAppService {

    @Autowired
    private UdfDefinitionRepository udfDefinitionRepository;

    @Autowired
    private ProjectionEngineService projectionEngineService;

    public UdfDefinition createDefinition(UdfDefinition definition) {
        // Validate uniqueness
        if (udfDefinitionRepository.existsByEntityTypeAndFieldName(
                definition.getEntityType(), definition.getFieldName())) {
            throw new IllegalArgumentException(
                "Field name already exists for entity type: " + definition.getFieldName());
        }

        definition.setCreatedAt(LocalDateTime.now());
        definition.setUpdatedAt(LocalDateTime.now());

        UdfDefinition saved = udfDefinitionRepository.save(definition);

        // Update reporting schema
        projectionEngineService.updateReportingSchema(definition.getEntityType());

        return saved;
    }

    public UdfDefinition updateDefinition(Long id, UdfDefinition updatedDefinition) {
        UdfDefinition existing = udfDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("UDF definition not found: " + id));

        // Check if field name is being changed and if it's unique
        if (!existing.getFieldName().equals(updatedDefinition.getFieldName())) {
            if (udfDefinitionRepository.existsByEntityTypeAndFieldName(
                    existing.getEntityType(), updatedDefinition.getFieldName())) {
                throw new IllegalArgumentException(
                    "Field name already exists for entity type: " + updatedDefinition.getFieldName());
            }
        }

        existing.setDisplayName(updatedDefinition.getDisplayName());
        existing.setFieldName(updatedDefinition.getFieldName());
        existing.setFieldType(updatedDefinition.getFieldType());
        existing.setRequired(updatedDefinition.isRequired());
        existing.setDefaultValue(updatedDefinition.getDefaultValue());
        existing.setValidationRules(updatedDefinition.getValidationRules());
        existing.setOptions(updatedDefinition.getOptions());
        existing.setUpdatedAt(LocalDateTime.now());

        UdfDefinition saved = udfDefinitionRepository.save(existing);

        // Update reporting schema
        projectionEngineService.updateReportingSchema(existing.getEntityType());

        return saved;
    }

    public void deleteDefinition(Long id) {
        UdfDefinition definition = udfDefinitionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("UDF definition not found: " + id));

        udfDefinitionRepository.deleteById(id);

        // Update reporting schema (remove column if needed)
        projectionEngineService.updateReportingSchema(definition.getEntityType());
    }

    public List<UdfDefinition> getDefinitionsByEntityType(String entityType) {
        return udfDefinitionRepository.findByEntityType(entityType);
    }

    public Optional<UdfDefinition> getDefinitionById(Long id) {
        return udfDefinitionRepository.findById(id);
    }

    public Optional<UdfDefinition> getDefinitionByEntityAndField(String entityType, String fieldName) {
        return udfDefinitionRepository.findByEntityTypeAndFieldName(entityType, fieldName);
    }
}