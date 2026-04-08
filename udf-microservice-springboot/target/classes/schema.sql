-- UDF Microservice Database Schema

-- Field types reference table
CREATE TABLE IF NOT EXISTS field_type (
    type VARCHAR(20) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    validation_rules JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UDF definitions
CREATE TABLE IF NOT EXISTS udf_definition (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    required BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    validation_rules JSONB,
    options JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    UNIQUE (entity_type, field_name),
    FOREIGN KEY (field_type) REFERENCES field_type(type)
);

-- UDF values (EAV pattern)
CREATE TABLE IF NOT EXISTS entity_udf_values (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (entity_type, entity_id, field_id),
    FOREIGN KEY (field_id) REFERENCES udf_definition(id) ON DELETE CASCADE
);

-- Reportable fields metadata (for dynamic reporting)
CREATE TABLE IF NOT EXISTS reportable_fields (
    id BIGSERIAL PRIMARY KEY,
    field_name VARCHAR(100) NOT NULL,
    source_column VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL,
    is_aggregatable BOOLEAN DEFAULT FALSE,
    is_filterable BOOLEAN DEFAULT TRUE,
    entity VARCHAR(50) NOT NULL,

    UNIQUE (entity, field_name)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_udf_definition_entity_type ON udf_definition(entity_type);
CREATE INDEX IF NOT EXISTS idx_udf_definition_field_name ON udf_definition(field_name);
CREATE INDEX IF NOT EXISTS idx_entity_udf_values_entity ON entity_udf_values(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_entity_udf_values_field ON entity_udf_values(field_id);
CREATE INDEX IF NOT EXISTS idx_reportable_fields_entity ON reportable_fields(entity);

-- Create reporting tables (these would be created dynamically)
-- customer_reporting table will be created by ProjectionEngineService
-- invoice_reporting table will be created by ProjectionEngineService