# DOA Rules API Documentation

## Overview
The DOA (Delegation of Authority) Rules API provides endpoints to manage delegation authority rules in the system. These rules define approval levels, amount ranges, and associated entities for users.

**Base URL**: `/api/v1/doa-rules`

**Authentication**: All endpoints require JWT Bearer token authentication.

---

## Table of Contents
1. [Get All DOA Rules](#1-get-all-doa-rules)
2. [Get DOA Rule by ID](#2-get-doa-rule-by-id)
3. [Create DOA Rule](#3-create-doa-rule)
4. [Update DOA Rule](#4-update-doa-rule)
5. [Delete DOA Rule](#5-delete-doa-rule)
6. [Toggle DOA Rule Status](#6-toggle-doa-rule-status)
7. [Get DOA Rules by User ID](#7-get-doa-rules-by-user-id)
8. [Get DOA Rules by Entity ID](#8-get-doa-rules-by-entity-id)
9. [Data Models](#data-models)
10. [Error Responses](#error-responses)

---

## Endpoints

### 1. Get All DOA Rules

Retrieve all DOA rules with optional filtering and pagination.

**Endpoint**: `GET /api/v1/doa-rules`

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 10 | Number of items per page |
| sort | String | No | createdAt,desc | Sort field and direction (e.g., "userName,asc") |
| userId | UUID | No | - | Filter by user ID |
| entityId | UUID | No | - | Filter by entity ID |
| currency | String | No | - | Filter by currency code |
| classification | String | No | - | Filter by classification |
| isActive | Boolean | No | - | Filter by active status |
| enabled | Boolean | No | - | Filter by enabled status |

**Example Request**:
```http
GET /api/v1/doa-rules?page=0&size=10&sort=createdAt,desc&userId=123e4567-e89b-12d3-a456-426614174000&isActive=true
Authorization: Bearer <jwt-token>
```

**Example Response** (200 OK):
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "John Doe",
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "country": "USA",
      "emailId": "john.doe@example.com",
      "fromAmount": 0.00,
      "toAmount": 10000.00,
      "currency": "USD",
      "vendorCode": "VENDOR001",
      "poNumber": "PO-2024-001",
      "approverLevel": "1",
      "classification": "CAPEX",
      "userCountries": null,
      "companyCode": "COMP001",
      "entityId": "789e4567-e89b-12d3-a456-426614174000",
      "entity": "Entity Name",
      "isActive": true,
      "enabled": true,
      "validFrom": "2024-01-01",
      "validTo": "2024-12-31",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-05T15:30:00Z",
      "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "empty": false
}
```

---

### 2. Get DOA Rule by ID

Retrieve a specific DOA rule by its ID.

**Endpoint**: `GET /api/v1/doa-rules/{id}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | The DOA rule ID |

**Example Request**:
```http
GET /api/v1/doa-rules/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <jwt-token>
```

**Example Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "John Doe",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "country": "USA",
  "emailId": "john.doe@example.com",
  "fromAmount": 0.00,
  "toAmount": 10000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "approverLevel": "1",
  "classification": "CAPEX",
  "userCountries": null,
  "companyCode": "COMP001",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "entity": "Entity Name",
  "isActive": true,
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-05T15:30:00Z",
  "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
}
```

**Error Response** (404 Not Found):
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

### 3. Create DOA Rule

Create a new DOA rule.

**Endpoint**: `POST /api/v1/doa-rules`

**Request Body**:
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "approvalLevel": 1,
  "minAmount": 0.00,
  "maxAmount": 10000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "classification": "CAPEX",
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31"
}
```

**Validation Rules**:
- `userId`: Required, must be a valid UUID
- `entityId`: Required, must be a valid UUID
- `approvalLevel`: Required, must be >= 1
- `minAmount`: Required, must be >= 0
- `maxAmount`: Required, must be > 0 and > minAmount
- `currency`: Required, max 10 characters
- `vendorCode`: Optional, max 255 characters
- `poNumber`: Optional, max 255 characters
- `classification`: Optional, max 255 characters
- `enabled`: Optional, defaults to true
- `validFrom`: Required, date format: yyyy-MM-dd
- `validTo`: Required, date format: yyyy-MM-dd, must be >= validFrom

**Example Request**:
```http
POST /api/v1/doa-rules
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "approvalLevel": 1,
  "minAmount": 0.00,
  "maxAmount": 10000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "classification": "CAPEX",
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31"
}
```

**Example Response** (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "John Doe",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "country": "USA",
  "emailId": "john.doe@example.com",
  "fromAmount": 0.00,
  "toAmount": 10000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "approverLevel": "1",
  "classification": "CAPEX",
  "userCountries": null,
  "companyCode": "COMP001",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "entity": "Entity Name",
  "isActive": true,
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31",
  "createdAt": "2024-12-26T10:30:00Z",
  "updatedAt": "2024-12-26T10:30:00Z",
  "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
}
```

**Error Response** (400 Bad Request):
```json
{
  "error": "Validation Failed",
  "message": "Please check the input fields",
  "fieldErrors": {
    "userId": "User ID is required",
    "maxAmount": "Maximum amount must be greater than minimum amount"
  },
  "status": 400,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

### 4. Update DOA Rule

Update an existing DOA rule.

**Endpoint**: `PUT /api/v1/doa-rules/{id}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | The DOA rule ID to update |

**Request Body**: Same as Create DOA Rule

**Example Request**:
```http
PUT /api/v1/doa-rules/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "approvalLevel": 2,
  "minAmount": 10000.01,
  "maxAmount": 50000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "classification": "CAPEX",
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31"
}
```

**Example Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "John Doe",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "country": "USA",
  "emailId": "john.doe@example.com",
  "fromAmount": 10000.01,
  "toAmount": 50000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "approverLevel": "2",
  "classification": "CAPEX",
  "userCountries": null,
  "companyCode": "COMP001",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "entity": "Entity Name",
  "isActive": true,
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-12-26T10:35:00Z",
  "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
}
```

**Error Response** (404 Not Found):
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

### 5. Delete DOA Rule

Soft delete a DOA rule (sets `isActive` to false).

**Endpoint**: `DELETE /api/v1/doa-rules/{id}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | The DOA rule ID to delete |

**Example Request**:
```http
DELETE /api/v1/doa-rules/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <jwt-token>
```

**Example Response** (204 No Content):
```
(No response body)
```

**Error Response** (404 Not Found):
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

### 6. Toggle DOA Rule Status

Enable or disable a DOA rule.

**Endpoint**: `PATCH /api/v1/doa-rules/{id}/toggle-status`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| id | UUID | Yes | The DOA rule ID |

**Request Body**:
```json
{
  "enabled": false
}
```

**Example Request**:
```http
PATCH /api/v1/doa-rules/550e8400-e29b-41d4-a716-446655440000/toggle-status
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "enabled": false
}
```

**Example Response** (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "enabled": false,
  "updatedAt": "2024-12-26T10:40:00Z"
}
```

**Error Response** (404 Not Found):
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

### 7. Get DOA Rules by User ID

Retrieve all DOA rules for a specific user.

**Endpoint**: `GET /api/v1/doa-rules/user/{userId}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| userId | UUID | Yes | The user ID |

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 10 | Number of items per page |

**Example Request**:
```http
GET /api/v1/doa-rules/user/123e4567-e89b-12d3-a456-426614174000?page=0&size=10
Authorization: Bearer <jwt-token>
```

**Example Response** (200 OK):
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "John Doe",
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "country": "USA",
      "emailId": "john.doe@example.com",
      "fromAmount": 0.00,
      "toAmount": 10000.00,
      "currency": "USD",
      "vendorCode": "VENDOR001",
      "poNumber": "PO-2024-001",
      "approverLevel": "1",
      "classification": "CAPEX",
      "userCountries": null,
      "companyCode": "COMP001",
      "entityId": "789e4567-e89b-12d3-a456-426614174000",
      "entity": "Entity Name",
      "isActive": true,
      "enabled": true,
      "validFrom": "2024-01-01",
      "validTo": "2024-12-31",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-05T15:30:00Z",
      "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
    }
  ],
  "pageable": { ... },
  "totalPages": 2,
  "totalElements": 15,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "empty": false
}
```

---

### 8. Get DOA Rules by Entity ID

Retrieve all DOA rules for a specific entity.

**Endpoint**: `GET /api/v1/doa-rules/entity/{entityId}`

**Path Parameters**:
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| entityId | UUID | Yes | The entity ID |

**Query Parameters**:
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-indexed) |
| size | Integer | No | 10 | Number of items per page |

**Example Request**:
```http
GET /api/v1/doa-rules/entity/789e4567-e89b-12d3-a456-426614174000?page=0&size=10
Authorization: Bearer <jwt-token>
```

**Example Response** (200 OK):
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "John Doe",
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "country": "USA",
      "emailId": "john.doe@example.com",
      "fromAmount": 0.00,
      "toAmount": 10000.00,
      "currency": "USD",
      "vendorCode": "VENDOR001",
      "poNumber": "PO-2024-001",
      "approverLevel": "1",
      "classification": "CAPEX",
      "userCountries": null,
      "companyCode": "COMP001",
      "entityId": "789e4567-e89b-12d3-a456-426614174000",
      "entity": "Entity Name",
      "isActive": true,
      "enabled": true,
      "validFrom": "2024-01-01",
      "validTo": "2024-12-31",
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-05T15:30:00Z",
      "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
    }
  ],
  "pageable": { ... },
  "totalPages": 3,
  "totalElements": 25,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "empty": false
}
```

---

## Data Models

### DoaRuleRequest
```json
{
  "userId": "UUID",
  "entityId": "UUID",
  "approvalLevel": "Integer (>= 1)",
  "minAmount": "BigDecimal (>= 0)",
  "maxAmount": "BigDecimal (> minAmount)",
  "currency": "String (max 10 chars)",
  "vendorCode": "String (max 255 chars, optional)",
  "poNumber": "String (max 255 chars, optional)",
  "classification": "String (max 255 chars, optional)",
  "enabled": "Boolean (default: true)",
  "validFrom": "Date (yyyy-MM-dd)",
  "validTo": "Date (yyyy-MM-dd, >= validFrom)"
}
```

### DoaRuleResponse
```json
{
  "id": "UUID",
  "userName": "String",
  "userId": "UUID",
  "country": "String",
  "emailId": "String",
  "fromAmount": "BigDecimal",
  "toAmount": "BigDecimal",
  "currency": "String",
  "vendorCode": "String",
  "poNumber": "String",
  "approverLevel": "String",
  "classification": "String",
  "userCountries": "List<String>",
  "companyCode": "String",
  "entityId": "UUID",
  "entity": "String",
  "isActive": "Boolean",
  "enabled": "Boolean",
  "validFrom": "Date (yyyy-MM-dd)",
  "validTo": "Date (yyyy-MM-dd)",
  "createdAt": "Timestamp (ISO 8601)",
  "updatedAt": "Timestamp (ISO 8601)",
  "createdByUserId": "UUID"
}
```

### ToggleStatusRequest
```json
{
  "enabled": "Boolean"
}
```

### ToggleStatusResponse
```json
{
  "id": "UUID",
  "enabled": "Boolean",
  "updatedAt": "Timestamp (ISO 8601)"
}
```

---

## Error Responses

### 400 Bad Request - Validation Error
```json
{
  "error": "Validation Failed",
  "message": "Please check the input fields",
  "fieldErrors": {
    "field1": "Error message 1",
    "field2": "Error message 2"
  },
  "status": 400,
  "timestamp": "2024-12-26T10:30:00"
}
```

### 401 Unauthorized
```json
{
  "error": "Authentication Required",
  "message": "Authentication token is required",
  "status": 401,
  "timestamp": "2024-12-26T10:30:00"
}
```

### 403 Forbidden
```json
{
  "error": "Access Denied",
  "message": "You do not have the required role to access this resource",
  "status": 403,
  "timestamp": "2024-12-26T10:30:00"
}
```

### 404 Not Found
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "status": 500,
  "timestamp": "2024-12-26T10:30:00"
}
```

---

## Authentication

All endpoints require a valid JWT Bearer token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

The token should contain the user's information and will be used to:
- Authenticate the request
- Extract the user ID for audit purposes (createdByUserId)
- Authorize access to resources

---

## Pagination

List endpoints return paginated results with the following structure:

```json
{
  "content": [...],
  "pageable": {
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "size": 10,
  "number": 0,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "empty": false
}
```

---

## Sorting

The `sort` parameter accepts a comma-separated value in the format: `field,direction`

**Examples**:
- `createdAt,desc` - Sort by creation date descending (newest first)
- `userName,asc` - Sort by user name ascending (A-Z)
- `fromAmount,asc` - Sort by minimum amount ascending (lowest first)

**Available sort fields**:
- `createdAt`
- `updatedAt`
- `userName`
- `fromAmount` (minAmount)
- `toAmount` (maxAmount)
- `approvalLevel`
- `currency`
- `classification`

---

## Filtering

Multiple filter parameters can be combined in GET requests:

**Example**:
```http
GET /api/v1/doa-rules?userId=123e4567-e89b-12d3-a456-426614174000&currency=USD&isActive=true&enabled=true&page=0&size=20
```

This will return:
- Only DOA rules for the specified user
- Only rules with USD currency
- Only active rules (isActive=true)
- Only enabled rules (enabled=true)
- First page with 20 items per page

---

## Notes

1. **Soft Delete**: The DELETE endpoint performs a soft delete by setting `isActive` to `false`. The record remains in the database but is excluded from default queries.

2. **Audit Trail**: All create and update operations track:
   - `createdAt`: Timestamp when the rule was created
   - `updatedAt`: Timestamp when the rule was last updated
   - `createdByUserId`: User ID who created the rule (extracted from JWT token)

3. **Amount Validation**: The system validates that `maxAmount > minAmount` at both request validation and database constraint levels.

4. **Date Validation**: The system validates that `validTo >= validFrom` at both request validation and database constraint levels.

5. **Transient Fields**: Fields like `userName`, `emailId`, `country`, `companyCode`, and `entity` are joined from other tables and are not stored in the `doa_rules` table.

---

## Database Schema

```sql
CREATE TABLE public.doa_rules (
    doa_rule_id uuid DEFAULT gen_random_uuid() NOT NULL,
    user_id uuid NOT NULL,
    entity_id uuid NOT NULL,
    approval_level int4 NOT NULL,
    min_amount numeric(38, 2) NOT NULL,
    max_amount numeric(38, 2) NOT NULL,
    currency varchar(10) NOT NULL,
    vendor_code varchar(255) NULL,
    po_number varchar(255) NULL,
    classification varchar(255) NULL,
    enabled bool DEFAULT true NULL,
    is_active bool DEFAULT true NULL,
    valid_from date NULL,
    valid_to date NULL,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    updated_at timestamp DEFAULT CURRENT_TIMESTAMP NULL,
    created_by_user_id uuid NOT NULL,
    CONSTRAINT chk_amount_range CHECK ((min_amount <= max_amount)),
    CONSTRAINT chk_valid_date_range CHECK ((valid_from <= valid_to)),
    CONSTRAINT doa_rules_pkey1 PRIMARY KEY (doa_rule_id),
    CONSTRAINT fk_doa_rules_entity FOREIGN KEY (entity_id) REFERENCES public.entities(entity_id),
    CONSTRAINT fk_doa_rules_user FOREIGN KEY (user_id) REFERENCES public.users(user_id)
);
```

---

## Support

For issues or questions, please contact the development team.

**Version**: 1.0.0  
**Last Updated**: December 26, 2024

