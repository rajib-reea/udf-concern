# UDF Microservice Development Guide

## Overview

This guide provides instructions for setting up a development environment and contributing to the UDF Microservice project.

## Prerequisites

### Required Software
- **Java 17+**: Download from [Adoptium](https://adoptium.net/)
- **Maven 3.6+** or **Gradle 7+**
- **Git**: Version control system
- **Docker & Docker Compose**: For running dependencies
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Optional Tools
- **PostgreSQL Client**: pgAdmin or DBeaver
- **Kafka Tools**: Kafka Tool or Conduktor
- **API Testing**: Postman or Insomnia

## Project Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd udf-microservice-springboot
```

### 2. Import into IDE

**IntelliJ IDEA:**
- File → Open → Select project directory
- Wait for Maven/Gradle import to complete

**VS Code:**
- Install Java Extension Pack
- Open folder in VS Code
- Install recommended extensions

### 3. Environment Configuration

Create `.env` file in the project root:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=udf_db
DB_USER=udf_user
DB_PASSWORD=udf_password

# Kafka Configuration
KAFKA_HOST=localhost
KAFKA_PORT=9092

# Application Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### 4. Start Dependencies with Docker

```bash
# Start PostgreSQL and Kafka
docker-compose -f docker-compose.dev.yml up -d

# Verify services are running
docker-compose ps
```

### 5. Database Setup

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U udf_user -d udf_db

# Run initial schema (if not using Flyway)
# The application will create tables automatically on startup
```

### 6. Run the Application

```bash
# Using Maven
./mvnw spring-boot:run

# Using Gradle
./gradlew bootRun

# Or run from IDE
```

### 7. Verify Installation

```bash
# Health check
curl http://localhost:8080/actuator/health

# API documentation
open http://localhost:8080/swagger-ui.html
```

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/yourcompany/udf/
│   │       ├── UdfApplication.java
│   │       ├── config/           # Configuration classes
│   │       ├── controller/       # REST controllers
│   │       ├── domain/           # Domain entities and services
│   │       │   ├── entity/       # JPA entities
│   │       │   ├── service/      # Domain services
│   │       │   ├── repository/   # Repository interfaces
│   │       │   └── event/        # Domain events
│   │       ├── application/      # Application services
│   │       ├── infrastructure/   # Infrastructure implementations
│   │       └── dto/              # Data transfer objects
│   └── resources/
│       ├── application.yml       # Main configuration
│       ├── application-dev.yml   # Development overrides
│       ├── application-test.yml  # Test overrides
│       └── db/migration/         # Flyway migrations
└── test/
    ├── java/
    └── resources/
```

## Development Workflow

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Implement Changes

Follow the established patterns:
- **Domain-first approach**: Start with domain entities
- **TDD**: Write tests before implementation
- **Clean code**: Follow SOLID principles

### 3. Code Style

The project uses:
- **Google Java Style Guide**
- **EditorConfig** for consistent formatting
- **Spotless** Maven plugin for code formatting

```bash
# Format code
./mvnw spotless:apply
```

### 4. Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Run with coverage
./mvnw test jacoco:report
```

### 5. Database Migrations

Use Flyway for schema changes:

```sql
-- src/main/resources/db/migration/V1.1__add_field_description.sql
ALTER TABLE udf_definition ADD COLUMN description TEXT;
```

### 6. API Documentation

APIs are documented with OpenAPI:

```java
@PostMapping("/udf-definitions")
@ApiOperation("Create a new UDF definition")
@ApiResponse(code = 201, message = "UDF definition created")
public ResponseEntity<UdfDefinitionDto> createUdfDefinition(
    @Valid @RequestBody CreateUdfDefinitionRequest request) {
    // implementation
}
```

## Key Development Concepts

### Domain-Driven Design

The application follows DDD principles:

- **Entities**: `UdfDefinition`, `FieldType`
- **Value Objects**: `ValidationRule`, `FieldOption`
- **Domain Services**: Business logic that doesn't belong to entities
- **Application Services**: Orchestrate domain operations
- **Repositories**: Abstract data access

### Field Types System

Supported field types are extensible:

```java
public interface FieldType {
    String getType();
    boolean validate(Object value, Map<String, Object> rules);
    Object parseValue(String input);
}
```

### Storage Strategies

Two storage approaches:

1. **EAV (Entity-Attribute-Value)**: Traditional relational approach
2. **JSONB**: PostgreSQL-specific JSON storage

Configurable via properties:

```yaml
udf:
  storage:
    strategy: jsonb  # or eav
```

### Event-Driven Architecture

Domain events are published to Kafka:

```java
@DomainEvent
public class UdfDefinitionCreatedEvent {
    private final Long definitionId;
    private final String entityType;
    private final String fieldName;
}
```

## Testing Strategy

### Unit Tests

```java
@SpringBootTest
class UdfDefinitionServiceTest {

    @Test
    void shouldCreateUdfDefinition() {
        // Given
        var request = createValidRequest();

        // When
        var result = service.createDefinition(request);

        // Then
        assertThat(result.getId()).isNotNull();
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UdfDefinitionControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void shouldCreateDefinitionViaApi() {
        // Test full HTTP request/response
    }
}
```

### Contract Tests

Using Spring Cloud Contract:

```groovy
Contract.make {
    request {
        method 'POST'
        url '/udf-definitions'
        body([
            entityType: "customer",
            fieldName: "custom_field",
            fieldType: "TEXT"
        ])
    }
    response {
        status 201
        body([
            id: $(anyNumber()),
            entityType: "customer"
        ])
    }
}
```

## Debugging

### Application Logs

```yaml
logging:
  level:
    com.yourcompany.udf: DEBUG
    org.springframework.web: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

### Database Debugging

```sql
-- View recent UDF definitions
SELECT * FROM udf_definition ORDER BY created_at DESC LIMIT 10;

-- View UDF values for an entity
SELECT * FROM entity_udf_values
WHERE entity_type = 'customer' AND entity_id = 123;
```

### Kafka Debugging

```bash
# List topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Consume messages
docker-compose exec kafka kafka-console-consumer \
  --topic udf-events \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## Performance Optimization

### Profiling

```bash
# Enable profiling
java -javaagent:profiler.jar -jar app.jar

# Use Spring Boot Actuator
curl http://localhost:8080/actuator/metrics
```

### Caching Strategy

```java
@Cacheable("udfDefinitions")
public List<UdfDefinition> findByEntityType(String entityType) {
    return repository.findByEntityType(entityType);
}
```

### Database Optimization

```sql
-- Add performance indexes
CREATE INDEX CONCURRENTLY idx_udf_values_entity_field
ON entity_udf_values(entity_type, entity_id, field_id);

-- Use EXPLAIN ANALYZE for query optimization
EXPLAIN ANALYZE SELECT * FROM udf_definition WHERE entity_type = 'customer';
```

## Contributing Guidelines

### Commit Messages

Follow conventional commits:

```
feat: add support for date field type
fix: resolve validation bug for number fields
docs: update API documentation
refactor: simplify UDF value storage logic
```

### Pull Request Process

1. **Create PR**: Push branch and create pull request
2. **Code Review**: Address reviewer feedback
3. **Tests Pass**: Ensure all CI checks pass
4. **Merge**: Squash merge to main branch

### Code Review Checklist

- [ ] Unit tests added/updated
- [ ] Integration tests pass
- [ ] API documentation updated
- [ ] Database migrations included
- [ ] Security considerations addressed
- [ ] Performance impact assessed

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Find process using port
   lsof -i :8080
   # Kill process or change port
   ```

2. **Database connection refused**
   ```bash
   # Check if PostgreSQL is running
   docker-compose ps
   # Restart services
   docker-compose restart postgres
   ```

3. **Kafka connection issues**
   ```bash
   # Check Kafka logs
   docker-compose logs kafka
   # Verify network connectivity
   telnet localhost 9092
   ```

4. **Build failures**
   ```bash
   # Clean and rebuild
   ./mvnw clean compile
   # Check Java version
   java -version
   ```

### Getting Help

- **Documentation**: Check this guide and API docs
- **Issues**: Create GitHub issue with detailed description
- **Discussions**: Use GitHub discussions for questions
- **Team Chat**: Reach out to the development team

## Next Steps

- Explore the codebase by reading key classes
- Run the existing tests to understand behavior
- Try creating a new field type implementation
- Review open issues and contribute fixes