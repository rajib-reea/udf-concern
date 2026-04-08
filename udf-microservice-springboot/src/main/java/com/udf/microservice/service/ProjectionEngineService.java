package com.udf.microservice.service;

import com.udf.microservice.domain.EntityUdfValue;
import com.udf.microservice.domain.UdfDefinition;
import com.udf.microservice.repository.EntityUdfValueRepository;
import com.udf.microservice.repository.UdfDefinitionRepository;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectionEngineService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityUdfValueRepository entityUdfValueRepository;

    @Autowired
    private UdfDefinitionRepository udfDefinitionRepository;

    @Async
    public void syncEntityToReporting(String entityType, Long entityId) {
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRESQL);

        // Load base entity data (this would need to be implemented based on your entity structure)
        Map<String, Object> baseData = loadBaseEntityData(entityType, entityId);

        // Load UDF values
        List<EntityUdfValue> udfValues = entityUdfValueRepository.findByEntityTypeAndEntityId(entityType, entityId);

        // Load UDF metadata
        List<UdfDefinition> udfDefinitions = udfDefinitionRepository.findByEntityType(entityType);
        Map<Long, UdfDefinition> udfDefMap = udfDefinitions.stream()
            .collect(Collectors.toMap(UdfDefinition::getId, def -> def));

        // Flatten the data
        Map<String, Object> flattenedData = flattenEntityData(baseData, udfValues, udfDefMap);

        // Type cast values
        Map<String, Object> normalizedData = typeCastValues(flattenedData, udfDefMap);

        // Add entity identifiers
        normalizedData.put(entityType + "_id", entityId);

        // Upsert to reporting table
        upsertToReportingTable(create, entityType, normalizedData);
    }

    private Map<String, Object> loadBaseEntityData(String entityType, Long entityId) {
        // This is a placeholder - you would implement this based on your actual entity structure
        // For example, if you have customer and invoice entities, you'd query those tables
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRESQL);

        switch (entityType.toLowerCase()) {
            case "customer":
                return create.selectFrom(DSL.table("customer"))
                    .where(DSL.field("id").eq(entityId))
                    .fetchOneMap();
            case "invoice":
                return create.selectFrom(DSL.table("invoice"))
                    .where(DSL.field("id").eq(entityId))
                    .fetchOneMap();
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entityType);
        }
    }

    private Map<String, Object> flattenEntityData(Map<String, Object> baseData,
                                                 List<EntityUdfValue> udfValues,
                                                 Map<Long, UdfDefinition> udfDefinitions) {
        Map<String, Object> flattened = new HashMap<>(baseData);

        for (EntityUdfValue udfValue : udfValues) {
            UdfDefinition definition = udfDefinitions.get(udfValue.getFieldId());
            if (definition != null) {
                String fieldName = definition.getFieldName();
                Object typedValue = parseValue(udfValue.getValue(), definition.getFieldType());
                flattened.put(fieldName, typedValue);
            }
        }

        return flattened;
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
                // You might want to use a proper date parser here
                return value;
            case "DROPDOWN":
                return value;
            default:
                return value;
        }
    }

    private Map<String, Object> typeCastValues(Map<String, Object> data, Map<Long, UdfDefinition> udfDefinitions) {
        Map<String, Object> normalized = new HashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Apply type normalization if needed
            // For now, just pass through
            normalized.put(key, value);
        }

        return normalized;
    }

    private void upsertToReportingTable(DSLContext create, String entityType, Map<String, Object> data) {
        String reportingTable = entityType + "_reporting";

        // Build upsert query
        var upsert = create.insertInto(DSL.table(reportingTable))
            .set(data)
            .onConflict()
            .doUpdate()
            .set(data);

        upsert.execute();
    }

    @Transactional
    public void syncAllEntities(String entityType) {
        // This would be called periodically to sync all entities
        // Implementation would depend on how you track which entities need syncing

        // For now, just mark all entities as needing sync
        // In a real implementation, you'd have a sync tracking table
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRESQL);

        // Get all entity IDs that have UDF values
        List<Long> entityIds = entityUdfValueRepository.findByEntityTypeAndEntityIds(entityType, null)
            .stream()
            .map(EntityUdfValue::getEntityId)
            .distinct()
            .collect(Collectors.toList());

        for (Long entityId : entityIds) {
            syncEntityToReporting(entityType, entityId);
        }
    }

    public void createReportingTable(String entityType) {
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRESQL);

        String reportingTable = entityType + "_reporting";

        // This is a simplified version - in reality, you'd need to analyze
        // the base entity structure and UDF definitions to create the proper schema
        String sql = "CREATE TABLE IF NOT EXISTS " + reportingTable + " (" +
            entityType + "_id BIGINT PRIMARY KEY," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        create.execute(sql);
    }

    public void updateReportingSchema(String entityType) {
        // Add new columns for UDF fields that don't exist yet
        DSLContext create = DSL.using(dataSource, SQLDialect.POSTGRESQL);

        List<UdfDefinition> udfDefinitions = udfDefinitionRepository.findByEntityType(entityType);
        String reportingTable = entityType + "_reporting";

        for (UdfDefinition udfDef : udfDefinitions) {
            String columnName = udfDef.getFieldName();
            String columnType = getSqlType(udfDef.getFieldType());

            // Check if column exists, if not add it
            try {
                String addColumnSql = "ALTER TABLE " + reportingTable +
                    " ADD COLUMN IF NOT EXISTS " + columnName + " " + columnType;
                create.execute(addColumnSql);
            } catch (Exception e) {
                // Column might already exist or other error
                // Log and continue
            }
        }
    }

    private String getSqlType(String fieldType) {
        switch (fieldType.toUpperCase()) {
            case "TEXT":
                return "TEXT";
            case "NUMBER":
                return "DECIMAL(15,2)";
            case "BOOLEAN":
                return "BOOLEAN";
            case "DATE":
                return "DATE";
            case "DROPDOWN":
                return "VARCHAR(255)";
            default:
                return "TEXT";
        }
    }
}