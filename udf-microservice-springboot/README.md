# UDF Microservice (Spring Boot)

## Overview

The UDF (User Defined Fields) Microservice is a Spring Boot-based service designed to manage user-defined fields for entities in a microservices architecture. This service allows applications to dynamically extend their data models without modifying the core schema.

## Features

- **Dynamic Field Definition**: Create and manage custom fields for any entity type
- **Field Types Support**: Text, Number, Date, Boolean, Dropdown, etc.
- **Validation Rules**: Configurable validation for field values
- **Value Storage**: Efficient storage of UDF values using EAV (Entity-Attribute-Value) pattern
- **Dynamic Reporting**: Generate SQL queries from JSON specifications using jOOQ
- **Projection Engine**: Sync UDF data to reporting tables for efficient querying
- **API Integration**: RESTful APIs for CRUD operations on UDF definitions and values
- **Event Publishing**: Integration with event bus for real-time updates

## Architecture

The microservice follows a layered architecture:

- **Controller Layer**: REST endpoints for API access
- **Service Layer**: Business logic for UDF operations
- **Repository Layer**: Data access using Spring Data JPA
- **Event Layer**: Publishing events to message bus

### High-Level Architecture Diagram

```
┌──────────────────────────┐
│        Frontend UI        │
│---------------------------│
│ • Entity Screens          │
│ • UDF Designer            │
│ • Report Builder          │
└─────────────┬────────────┘
              │
              ▼
┌──────────────────────────┐
│      Application API      │
│---------------------------│
│ • Entity Service          │
│ • UDF Service             │
│ • Report Service          │
└─────────────┬────────────┘
              │
┌─────────────┼─────────────┐
▼             ▼             ▼

┌──────────────────┐   ┌────────────────────┐   ┌─────────────────────┐
│ Transaction DB   │   │  UDF Metadata DB   │   │ Event / Change Bus   │
│ (Core Entities)  │   │--------------------│   │----------------------│
│------------------│   │ udf_definition     │   │ Kafka / Queue        │
│ customer         │   │ field_type         │   │ entity_updated       │
│ invoice          │   │ validation_rules   │   │ udf_changed          │
└─────────┬────────┘   └──────────┬─────────┘   └──────────┬──────────┘
          │                       │                         │
          └──────────────┬────────┴───────────────┬─────────┘
                         ▼                        ▼

          ┌────────────────────────────────────────┐
          │        UDF Value Storage Layer          │
          │-----------------------------------------│
          │ entity_udf_values (EAV or JSONB)       │
          │ entity_id                              │
          │ field_id                               │
          │ value                                  │
          └─────────────────┬──────────────────────┘
                            │
                            ▼
            ┌────────────────────────────────┐
            │   Reporting Projection Engine   │
            │--------------------------------│
            │ • Flatten UDF data              │
            │ • Schema materialization        │
            │ • Type normalization            │
            │ • Incremental sync              │
            └───────────────┬─────────────────┘
                            │
```

## Technology Stack

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL (with JSONB support)
- **ORM**: Spring Data JPA with Hibernate
- **Dynamic SQL**: jOOQ 3.x
- **Messaging**: Spring Cloud Stream with Kafka
- **Documentation**: OpenAPI/Swagger
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+ or Gradle 7+
- PostgreSQL 12+ or MySQL 8+

### Installation

1. Clone the repository
2. Configure database connection in `application.yml`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Configuration

Key configuration properties:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/udf_db
    username: udf_user
    password: udf_password

udf:
  storage:
    strategy: eav  # or jsonb
  reporting:
    sync:
      enabled: true
```

### Dynamic Reports

```http
POST /api/v1/reports/execute
POST /api/v1/reports/export/{format}
```

Example report request:

```json
{
  "entity": "customer",
  "columns": ["segment"],
  "aggregations": [
    {"field": "risk_score", "func": "AVG"}
  ],
  "filters": [
    {"field": "risk_score", "op": ">", "value": 70}
  ],
  "groupBy": ["segment"],
  "sort": [{"field": "segment", "dir": "ASC"}],
  "limit": 100
}
```

## Database Schema

The service uses several key tables:

- `field_type`: Supported field types and their validation rules
- `udf_definition`: UDF field definitions
- `entity_udf_values`: UDF values stored in EAV pattern
- `reportable_fields`: Metadata for dynamic reporting

## Dynamic Reporting

The service provides a powerful dynamic reporting engine that:

1. **Validates** report specifications against metadata
2. **Generates** type-safe SQL using jOOQ
3. **Executes** queries efficiently
4. **Exports** results in multiple formats (JSON, CSV, Excel)

### Safety Features

- Field existence validation
- Aggregation permission checks
- Filter capability verification
- SQL injection prevention through jOOQ

## Projection Engine

Automatically syncs UDF data to reporting tables:

1. **Monitors** changes to UDF values
2. **Flattens** entity data with UDF values
3. **Type casts** values appropriately
4. **Upserts** to reporting tables for fast queries

## Deployment

See [Deployment Guide](deployment.md) for containerization and orchestration instructions.

## Contributing

1. Follow the existing code style
2. Write tests for new features
3. Update documentation as needed
4. Create pull requests for review

## License

[Specify License]