# UDF Microservice Database Schema

## Overview

The UDF Microservice uses a relational database to store metadata about user-defined fields and their values. The schema supports both EAV (Entity-Attribute-Value) and JSONB storage strategies for flexibility.

## Supported Databases

- **PostgreSQL** (recommended for JSONB support)
- **MySQL** (with EAV strategy)
- **Other JPA-compatible databases**

## Schema Design

### Core Tables

#### udf_definition
Stores metadata about user-defined fields.

```sql
CREATE TABLE udf_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    required BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    validation_rules JSON,
    options JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    UNIQUE KEY uk_entity_field (entity_type, field_name),
    INDEX idx_entity_type (entity_type),
    INDEX idx_field_type (field_type)
);
```

**Columns:**
- `id`: Primary key
- `entity_type`: The entity this field belongs to (e.g., "customer", "invoice")
- `field_name`: Unique field identifier (used in APIs)
- `display_name`: Human-readable field name
- `field_type`: Type of field (TEXT, NUMBER, DATE, BOOLEAN, DROPDOWN)
- `required`: Whether the field is mandatory
- `default_value`: Default value for the field
- `validation_rules`: JSON object with validation constraints
- `options`: JSON array of options for dropdown fields
- `created_at/updated_at`: Audit timestamps
- `created_by/updated_by`: Audit user information

#### field_type
Reference table for supported field types.

```sql
CREATE TABLE field_type (
    type VARCHAR(20) PRIMARY KEY,
    display_name VARCHAR(50) NOT NULL,
    validation_rules JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Sample Data:**
```sql
INSERT INTO field_type (type, display_name, validation_rules) VALUES
('TEXT', 'Text', '{"maxLength": 1000, "pattern": null}'),
('NUMBER', 'Number', '{"min": null, "max": null, "decimalPlaces": 2}'),
('DATE', 'Date', '{"format": "yyyy-MM-dd"}'),
('BOOLEAN', 'Boolean', '{}'),
('DROPDOWN', 'Dropdown', '{"options": []}');
```

### Value Storage Tables

#### Option 1: EAV (Entity-Attribute-Value) Pattern
Best for MySQL and when you need advanced querying capabilities.

```sql
CREATE TABLE entity_udf_values (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_entity_field_value (entity_type, entity_id, field_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_field (field_id),
    FOREIGN KEY (field_id) REFERENCES udf_definition(id) ON DELETE CASCADE
);
```

#### Option 2: JSONB Storage (PostgreSQL)
More efficient for bulk operations and complex value structures.

```sql
CREATE TABLE entity_udf_json (
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    values JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (entity_type, entity_id)
);
```

**JSONB Structure:**
```json
{
  "custom_field_1": "Sample Text",
  "custom_field_2": 42,
  "custom_field_3": "2024-01-01",
  "custom_field_4": true,
  "custom_field_5": "option_a"
}
```

### Audit and History Tables

#### udf_value_history
Tracks changes to UDF values for audit purposes.

```sql
CREATE TABLE udf_value_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    field_id BIGINT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_entity_field (entity_type, entity_id, field_id),
    INDEX idx_changed_at (changed_at)
);
```

## Indexes and Performance

### Recommended Indexes

```sql
-- For UDF definition queries
CREATE INDEX idx_udf_def_entity_type ON udf_definition(entity_type);
CREATE INDEX idx_udf_def_field_name ON udf_definition(field_name);

-- For value queries (EAV)
CREATE INDEX idx_udf_values_entity ON entity_udf_values(entity_type, entity_id);
CREATE INDEX idx_udf_values_field ON entity_udf_values(field_id);

-- For JSONB queries (PostgreSQL)
CREATE INDEX idx_udf_json_values ON entity_udf_json USING GIN (values);
```

### Partitioning Strategy

For high-volume systems, consider partitioning by entity_type:

```sql
-- Partition entity_udf_values by entity_type
PARTITION BY LIST (entity_type) (
    PARTITION p_customer VALUES IN ('customer'),
    PARTITION p_invoice VALUES IN ('invoice'),
    PARTITION p_product VALUES IN ('product')
);
```

## Migration Strategy

### Initial Setup

1. Create tables in order
2. Insert field_type reference data
3. Create indexes
4. Set up partitioning if needed

### Schema Evolution

- Use Flyway or Liquibase for version-controlled migrations
- Always provide rollback scripts
- Test migrations on copy of production data

## Data Types and Constraints

### Field Types Mapping

| Field Type | SQL Type | Validation |
|------------|----------|------------|
| TEXT | VARCHAR/TEXT | maxLength, pattern |
| NUMBER | DECIMAL | min, max, decimalPlaces |
| DATE | DATE/DATETIME | format |
| BOOLEAN | BOOLEAN/TINYINT | - |
| DROPDOWN | VARCHAR | options array |

### Validation Rules JSON Structure

```json
{
  "maxLength": 100,
  "pattern": "^[a-zA-Z0-9]*$",
  "min": 0,
  "max": 1000,
  "decimalPlaces": 2,
  "format": "yyyy-MM-dd",
  "options": ["option1", "option2", "option3"]
}
```

## Backup and Recovery

- Regular backups of udf_definition table (critical metadata)
- Point-in-time recovery capability
- Test restore procedures quarterly
- Archive old audit data to reduce table size