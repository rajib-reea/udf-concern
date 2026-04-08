package com.udf.microservice.application;

import com.udf.microservice.domain.FieldType;
import com.udf.microservice.domain.UdfDefinition;
import com.udf.microservice.repository.FieldTypeRepository;
import com.udf.microservice.repository.UdfDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DynamicUiAppService {

    @Autowired
    private UdfDefinitionRepository udfDefinitionRepository;

    @Autowired
    private FieldTypeRepository fieldTypeRepository;

    public DynamicFormConfig getFormConfig(String entityType) {
        List<UdfDefinition> definitions = udfDefinitionRepository.findByEntityType(entityType);

        List<FormField> fields = definitions.stream()
            .map(this::convertToFormField)
            .collect(Collectors.toList());

        return new DynamicFormConfig(entityType, fields);
    }

    public QueryBuilderConfig getQueryBuilderConfig(String entityType) {
        List<UdfDefinition> definitions = udfDefinitionRepository.findByEntityType(entityType);

        List<QueryField> fields = definitions.stream()
            .map(this::convertToQueryField)
            .collect(Collectors.toList());

        List<QueryOperator> operators = getSupportedOperators();

        return new QueryBuilderConfig(entityType, fields, operators);
    }

    public List<FieldTypeMetadata> getSupportedFieldTypes() {
        List<FieldType> fieldTypes = fieldTypeRepository.findAll();

        return fieldTypes.stream()
            .map(this::convertToFieldTypeMetadata)
            .collect(Collectors.toList());
    }

    private FormField convertToFormField(UdfDefinition definition) {
        return new FormField(
            definition.getFieldName(),
            definition.getDisplayName(),
            definition.getFieldType(),
            definition.isRequired(),
            definition.getDefaultValue(),
            definition.getValidationRules(),
            definition.getOptions()
        );
    }

    private QueryField convertToQueryField(UdfDefinition definition) {
        return new QueryField(
            definition.getFieldName(),
            definition.getDisplayName(),
            definition.getFieldType(),
            true, // filterable
            definition.getFieldType().equals("NUMBER") ||
            definition.getFieldType().equals("DATE"), // aggregatable
            getOperatorsForFieldType(definition.getFieldType())
        );
    }

    private List<QueryOperator> getSupportedOperators() {
        return List.of(
            new QueryOperator("=", "Equals", List.of("TEXT", "NUMBER", "DATE", "BOOLEAN", "DROPDOWN")),
            new QueryOperator("!=", "Not Equals", List.of("TEXT", "NUMBER", "DATE", "BOOLEAN", "DROPDOWN")),
            new QueryOperator(">", "Greater Than", List.of("NUMBER", "DATE")),
            new QueryOperator("<", "Less Than", List.of("NUMBER", "DATE")),
            new QueryOperator(">=", "Greater Than or Equal", List.of("NUMBER", "DATE")),
            new QueryOperator("<=", "Less Than or Equal", List.of("NUMBER", "DATE")),
            new QueryOperator("LIKE", "Contains", List.of("TEXT")),
            new QueryOperator("IN", "In List", List.of("TEXT", "NUMBER", "DROPDOWN")),
            new QueryOperator("BETWEEN", "Between", List.of("NUMBER", "DATE"))
        );
    }

    private List<String> getOperatorsForFieldType(String fieldType) {
        return getSupportedOperators().stream()
            .filter(op -> op.getSupportedTypes().contains(fieldType))
            .map(QueryOperator::getOperator)
            .collect(Collectors.toList());
    }

    private FieldTypeMetadata convertToFieldTypeMetadata(FieldType fieldType) {
        return new FieldTypeMetadata(
            fieldType.getType(),
            fieldType.getDisplayName(),
            fieldType.getValidationRules()
        );
    }

    // DTO Classes
    public static class DynamicFormConfig {
        private String entityType;
        private List<FormField> fields;

        public DynamicFormConfig() {}

        public DynamicFormConfig(String entityType, List<FormField> fields) {
            this.entityType = entityType;
            this.fields = fields;
        }

        // Getters and setters
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public List<FormField> getFields() { return fields; }
        public void setFields(List<FormField> fields) { this.fields = fields; }
    }

    public static class FormField {
        private String fieldName;
        private String displayName;
        private String fieldType;
        private boolean required;
        private Object defaultValue;
        private Map<String, Object> validationRules;
        private Map<String, Object> options;

        public FormField() {}

        public FormField(String fieldName, String displayName, String fieldType, boolean required,
                        Object defaultValue, Map<String, Object> validationRules, Map<String, Object> options) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.fieldType = fieldType;
            this.required = required;
            this.defaultValue = defaultValue;
            this.validationRules = validationRules;
            this.options = options;
        }

        // Getters and setters
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public Object getDefaultValue() { return defaultValue; }
        public void setDefaultValue(Object defaultValue) { this.defaultValue = defaultValue; }

        public Map<String, Object> getValidationRules() { return validationRules; }
        public void setValidationRules(Map<String, Object> validationRules) { this.validationRules = validationRules; }

        public Map<String, Object> getOptions() { return options; }
        public void setOptions(Map<String, Object> options) { this.options = options; }
    }

    public static class QueryBuilderConfig {
        private String entityType;
        private List<QueryField> fields;
        private List<QueryOperator> operators;

        public QueryBuilderConfig() {}

        public QueryBuilderConfig(String entityType, List<QueryField> fields, List<QueryOperator> operators) {
            this.entityType = entityType;
            this.fields = fields;
            this.operators = operators;
        }

        // Getters and setters
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public List<QueryField> getFields() { return fields; }
        public void setFields(List<QueryField> fields) { this.fields = fields; }

        public List<QueryOperator> getOperators() { return operators; }
        public void setOperators(List<QueryOperator> operators) { this.operators = operators; }
    }

    public static class QueryField {
        private String fieldName;
        private String displayName;
        private String fieldType;
        private boolean filterable;
        private boolean aggregatable;
        private List<String> supportedOperators;

        public QueryField() {}

        public QueryField(String fieldName, String displayName, String fieldType, boolean filterable,
                         boolean aggregatable, List<String> supportedOperators) {
            this.fieldName = fieldName;
            this.displayName = displayName;
            this.fieldType = fieldType;
            this.filterable = filterable;
            this.aggregatable = aggregatable;
            this.supportedOperators = supportedOperators;
        }

        // Getters and setters
        public String getFieldName() { return fieldName; }
        public void setFieldName(String fieldName) { this.fieldName = fieldName; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }

        public boolean isFilterable() { return filterable; }
        public void setFilterable(boolean filterable) { this.filterable = filterable; }

        public boolean isAggregatable() { return aggregatable; }
        public void setAggregatable(boolean aggregatable) { this.aggregatable = aggregatable; }

        public List<String> getSupportedOperators() { return supportedOperators; }
        public void setSupportedOperators(List<String> supportedOperators) { this.supportedOperators = supportedOperators; }
    }

    public static class QueryOperator {
        private String operator;
        private String displayName;
        private List<String> supportedTypes;

        public QueryOperator() {}

        public QueryOperator(String operator, String displayName, List<String> supportedTypes) {
            this.operator = operator;
            this.displayName = displayName;
            this.supportedTypes = supportedTypes;
        }

        // Getters and setters
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public List<String> getSupportedTypes() { return supportedTypes; }
        public void setSupportedTypes(List<String> supportedTypes) { this.supportedTypes = supportedTypes; }
    }

    public static class FieldTypeMetadata {
        private String type;
        private String displayName;
        private Map<String, Object> validationRules;

        public FieldTypeMetadata() {}

        public FieldTypeMetadata(String type, String displayName, Map<String, Object> validationRules) {
            this.type = type;
            this.displayName = displayName;
            this.validationRules = validationRules;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public Map<String, Object> getValidationRules() { return validationRules; }
        public void setValidationRules(Map<String, Object> validationRules) { this.validationRules = validationRules; }
    }
}