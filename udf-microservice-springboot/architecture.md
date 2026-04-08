# UDF Microservice Architecture

## Overview

The UDF (User Defined Fields) Microservice is designed to provide a scalable, flexible solution for managing user-defined fields across multiple entity types in a microservices ecosystem. The architecture follows Domain-Driven Design (DDD) principles and Clean Architecture patterns.

## Architectural Principles

- **Domain-Driven Design**: Clear separation of domain logic
- **Clean Architecture**: Dependency inversion and layered approach
- **Event-Driven**: Asynchronous communication with other services
- **API-First**: Contract-driven development with OpenAPI specification
- **Database Agnostic**: Support for multiple database backends

## System Context

```
┌─────────────────────────────────────────────────────────────┐
│                    External Systems                         │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                Frontend Applications               │    │
│  │  • Web UI • Mobile App • Third-party Integrations  │    │
│  └─────────────────┬───────────────────────────────────┘    │
└───────────────────┼─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                UDF Microservice                            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                Application Layer                    │    │
│  │  • REST API • Event Publishing • Validation        │    │
│  └─────────────────┬───────────────────────────────────┘    │
│                    │                                       │
│  ┌─────────────────┼───────────────────────────────────┐    │
│  │            Domain Layer                            │    │
│  │  • UDF Definition • Field Types • Validation Rules │    │
│  └─────────────────┬───────────────────────────────────┘    │
│                    │                                       │
│  ┌─────────────────┼───────────────────────────────────┐    │
│  │            Infrastructure Layer                    │    │
│  │  • Database • Message Queue • External APIs       │    │
│  └─────────────────┬───────────────────────────────────┘    │
└───────────────────┼─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  • PostgreSQL/MySQL • Kafka • Redis (Cache)        │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Component Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Controllers • DTOs • Exception Handlers • OpenAPI  │    │
│  └─────────────────┬───────────────────────────────────┘    │
└───────────────────┼─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Services • Use Cases • Event Handlers • Validators │    │
│  └─────────────────┬───────────────────────────────────┘    │
└───────────────────┼─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                           │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Entities • Value Objects • Domain Services • Events │    │
│  └─────────────────┬───────────────────────────────────┘    │
└───────────────────┼─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                 Infrastructure Layer                       │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Repositories • External APIs • Message Publishers   │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### 1. Domain Layer

**Entities:**
- `UdfDefinition`: Represents a user-defined field definition
- `FieldType`: Supported field types with validation rules
- `UdfValue`: Represents a field value for a specific entity

**Value Objects:**
- `ValidationRule`: Encapsulates validation logic
- `FieldOption`: Dropdown options
- `EntityReference`: Reference to external entities

**Domain Services:**
- `UdfValidationService`: Validates field values
- `UdfDefinitionService`: Manages field definitions
- `FieldTypeRegistry`: Registry of supported field types

**Domain Events:**
- `UdfDefinitionCreated`
- `UdfDefinitionUpdated`
- `UdfValueChanged`

#### 2. Application Layer

**Application Services:**
- `UdfDefinitionAppService`: CRUD operations for field definitions
- `UdfValueAppService`: CRUD operations for field values
- `UdfValidationAppService`: Field validation operations

**Use Cases:**
- `CreateUdfDefinitionUseCase`
- `UpdateUdfValueUseCase`
- `ValidateUdfValueUseCase`

**Event Handlers:**
- `UdfChangeEventHandler`: Publishes events to message bus
- `EntityUpdateEventHandler`: Handles external entity updates

#### 3. Infrastructure Layer

**Repositories:**
- `UdfDefinitionRepository`: JPA repository for field definitions
- `UdfValueRepository`: Repository for field values (EAV or JSONB)
- `FieldTypeRepository`: Repository for field types

**External Interfaces:**
- `MessagePublisher`: Publishes events to Kafka
- `CacheManager`: Redis integration for caching
- `ExternalEntityValidator`: Validates entity references

### Data Flow

#### UDF Definition Creation Flow

```
1. Client Request → Controller
2. Controller → Application Service
3. Application Service → Domain Service (Validation)
4. Domain Service → Repository (Persistence)
5. Repository → Database
6. Application Service → Event Publisher
7. Event Publisher → Message Bus
```

#### UDF Value Setting Flow

```
1. Client Request → Controller
2. Controller → Application Service
3. Application Service → Domain Service (Validation)
4. Domain Service → Repository (Persistence)
5. Repository → Database
6. Application Service → Cache Invalidation
7. Application Service → Event Publisher
```

## Technology Choices

### Core Framework
- **Spring Boot 3.x**: Latest Spring features and performance improvements
- **Java 17+**: Modern Java features and LTS support

### Data Access
- **Spring Data JPA**: Repository pattern implementation
- **Hibernate**: ORM with excellent performance
- **QueryDSL**: Type-safe queries for complex operations

### Messaging
- **Spring Cloud Stream**: Event-driven architecture
- **Kafka**: Reliable message broker
- **Spring Cloud Function**: Function-as-a-Service support

### API Documentation
- **OpenAPI 3.0**: Standard API specification
- **SpringDoc OpenAPI**: Automatic API documentation generation

### Testing
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework
- **Testcontainers**: Integration testing with real dependencies

### Observability
- **Spring Boot Actuator**: Health checks and metrics
- **Micrometer**: Metrics collection
- **Sleuth**: Distributed tracing

## Deployment Architecture

### Containerization
- **Docker**: Container images for consistent deployment
- **Docker Compose**: Local development environment
- **Kubernetes**: Production orchestration

### Configuration Management
- **Spring Cloud Config**: Externalized configuration
- **Kubernetes ConfigMaps/Secrets**: Environment-specific settings

### Service Mesh
- **Istio**: Service discovery, load balancing, security
- **Envoy**: High-performance proxy

## Security Considerations

### Authentication & Authorization
- **OAuth 2.0 / OpenID Connect**: Industry-standard authentication
- **JWT Tokens**: Stateless authentication
- **Role-Based Access Control**: Fine-grained permissions

### Data Protection
- **Encryption at Rest**: Database-level encryption
- **Encryption in Transit**: TLS 1.3
- **Field-Level Encryption**: Sensitive data protection

### API Security
- **Rate Limiting**: Prevent abuse
- **Input Validation**: Prevent injection attacks
- **CORS Configuration**: Cross-origin resource sharing

## Scalability Considerations

### Horizontal Scaling
- **Stateless Design**: Easy horizontal scaling
- **Database Sharding**: Scale database operations
- **Caching Strategy**: Redis for frequently accessed data

### Performance Optimization
- **Database Indexing**: Optimized queries
- **Connection Pooling**: Efficient database connections
- **Async Processing**: Non-blocking operations

### Monitoring & Alerting
- **Application Metrics**: Performance monitoring
- **Health Checks**: Automated health verification
- **Alerting**: Proactive issue detection

## Development Workflow

### Local Development
- **Docker Compose**: Full local environment
- **Hot Reload**: Fast development cycle
- **Database Migrations**: Flyway for schema management

### CI/CD Pipeline
- **GitHub Actions**: Automated testing and deployment
- **SonarQube**: Code quality analysis
- **OWASP Dependency Check**: Security vulnerability scanning

### Testing Strategy
- **Unit Tests**: Domain logic testing
- **Integration Tests**: Component interaction testing
- **Contract Tests**: API compatibility testing
- **Performance Tests**: Load testing