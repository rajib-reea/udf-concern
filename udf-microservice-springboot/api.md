# UDF Microservice API Documentation

## Overview

The UDF Microservice provides RESTful APIs for managing user-defined fields and their values. All APIs follow REST conventions and return JSON responses.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

[Describe authentication mechanism - e.g., JWT, OAuth2, API Keys]

## Common Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Field validation failed",
    "details": { ... }
  }
}
```

## Endpoints

### UDF Definitions

#### Create UDF Definition
```http
POST /udf-definitions
```

**Request Body:**
```json
{
  "entityType": "customer",
  "fieldName": "custom_field_1",
  "displayName": "Custom Field 1",
  "fieldType": "TEXT",
  "required": false,
  "defaultValue": null,
  "validationRules": {
    "maxLength": 100,
    "pattern": "^[a-zA-Z0-9]*$"
  },
  "options": null
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "entityType": "customer",
    "fieldName": "custom_field_1",
    "displayName": "Custom Field 1",
    "fieldType": "TEXT",
    "required": false,
    "defaultValue": null,
    "validationRules": {
      "maxLength": 100,
      "pattern": "^[a-zA-Z0-9]*$"
    },
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z"
  }
}
```

#### Get UDF Definitions
```http
GET /udf-definitions?entityType={entityType}&page=0&size=20
```

**Parameters:**
- `entityType` (optional): Filter by entity type
- `page` (optional): Page number (0-based)
- `size` (optional): Page size

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "entityType": "customer",
        "fieldName": "custom_field_1",
        "displayName": "Custom Field 1",
        "fieldType": "TEXT",
        "required": false
      }
    ],
    "pageable": {
      "page": 0,
      "size": 20,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

#### Update UDF Definition
```http
PUT /udf-definitions/{id}
```

**Request Body:** Same as create, but all fields optional

#### Delete UDF Definition
```http
DELETE /udf-definitions/{id}
```

### UDF Values

#### Set UDF Value
```http
POST /udf-values
```

**Request Body:**
```json
{
  "entityType": "customer",
  "entityId": 123,
  "fieldId": 1,
  "value": "Sample Value"
}
```

#### Get UDF Values for Entity
```http
GET /udf-values/{entityType}/{entityId}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "entityType": "customer",
    "entityId": 123,
    "values": {
      "custom_field_1": "Sample Value",
      "custom_field_2": 42
    }
  }
}
```

#### Bulk Set UDF Values
```http
POST /udf-values/bulk
```

**Request Body:**
```json
{
  "entityType": "customer",
  "entityId": 123,
  "values": {
    "custom_field_1": "New Value",
    "custom_field_2": 100
  }
}
```

### Field Types

#### Get Supported Field Types
```http
GET /field-types
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "type": "TEXT",
      "displayName": "Text",
      "validationRules": ["maxLength", "pattern", "required"]
    },
    {
      "type": "NUMBER",
      "displayName": "Number",
      "validationRules": ["min", "max", "required"]
    },
    {
      "type": "DATE",
      "displayName": "Date",
      "validationRules": ["required"]
    },
    {
      "type": "BOOLEAN",
      "displayName": "Boolean",
      "validationRules": ["required"]
    },
    {
      "type": "DROPDOWN",
      "displayName": "Dropdown",
      "validationRules": ["required", "options"]
    }
  ]
}
```

## Error Codes

- `VALIDATION_ERROR`: Invalid input data
- `NOT_FOUND`: Resource not found
- `DUPLICATE_FIELD`: Field name already exists for entity type
- `INVALID_FIELD_TYPE`: Unsupported field type
- `PERMISSION_DENIED`: Insufficient permissions

## Rate Limiting

- 1000 requests per minute per client
- Burst limit: 100 requests

## Versioning

API versioning is done via URL path: `/api/v1/`

Future versions will be available at `/api/v2/`, etc.