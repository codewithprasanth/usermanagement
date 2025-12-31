# DOA Rules API - Quick Reference Card

## 🚀 Quick Start

```bash
# Start application
.\mvnw.cmd spring-boot:run

# Base URL
http://localhost:8070/api/v1/doa-rules
```

## 📋 All Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/doa-rules` | List all (filtered) |
| GET | `/api/v1/doa-rules/{id}` | Get by ID |
| POST | `/api/v1/doa-rules` | Create new |
| PUT | `/api/v1/doa-rules/{id}` | Update |
| DELETE | `/api/v1/doa-rules/{id}` | Soft delete |
| PATCH | `/api/v1/doa-rules/{id}/toggle-status` | Toggle status |
| GET | `/api/v1/doa-rules/user/{userId}` | By user |
| GET | `/api/v1/doa-rules/entity/{entityId}` | By entity |

## 🔍 Query Parameters

### Pagination
```
?page=0              # Page number (0-indexed)
&size=10             # Items per page
&sort=createdAt,desc # Sort by field,direction
```

### Filters
```
?userId={uuid}         # Filter by user
&entityId={uuid}       # Filter by entity
&currency=USD          # Filter by currency
&classification=CAPEX  # Filter by classification
&isActive=true         # Filter by active status
&enabled=true          # Filter by enabled status
```

## 📝 Request Body Examples

### Create/Update DOA Rule
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

### Toggle Status
```json
{
  "enabled": false
}
```

## 🔐 Authentication

All requests require JWT Bearer token:

```bash
Authorization: Bearer YOUR_JWT_TOKEN
```

## 💻 cURL Examples

### Get All Rules
```bash
curl -X GET "http://localhost:8070/api/v1/doa-rules?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Create Rule
```bash
curl -X POST "http://localhost:8070/api/v1/doa-rules" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":"123e4567-e89b-12d3-a456-426614174000",...}'
```

### Update Rule
```bash
curl -X PUT "http://localhost:8070/api/v1/doa-rules/{id}" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":"123e4567-e89b-12d3-a456-426614174000",...}'
```

### Delete Rule
```bash
curl -X DELETE "http://localhost:8070/api/v1/doa-rules/{id}" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Toggle Status
```bash
curl -X PATCH "http://localhost:8070/api/v1/doa-rules/{id}/toggle-status" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"enabled":false}'
```

## 📦 Response Structure

### Single Rule Response
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
  "companyCode": "COMP001",
  "entityId": "789e4567-e89b-12d3-a456-426614174000",
  "entity": "Entity Name",
  "isActive": true,
  "enabled": true,
  "validFrom": "2024-01-01",
  "validTo": "2024-12-31",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-05T15:30:00Z"
}
```

### Paginated Response
```json
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 50,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false
}
```

## ❌ Error Responses

### 400 Bad Request
```json
{
  "error": "Validation Failed",
  "message": "Please check the input fields",
  "fieldErrors": {
    "maxAmount": "Maximum amount must be greater than minimum amount"
  },
  "status": 400,
  "timestamp": "2024-12-26T10:30:00"
}
```

### 404 Not Found
```json
{
  "error": "DOA Rule Not Found",
  "message": "DOA rule not found with id: ...",
  "status": 404,
  "timestamp": "2024-12-26T10:30:00"
}
```

## ✅ Validation Rules

| Field | Required | Validation |
|-------|----------|------------|
| userId | ✅ | Valid UUID |
| entityId | ✅ | Valid UUID |
| approvalLevel | ✅ | >= 1 |
| minAmount | ✅ | >= 0 |
| maxAmount | ✅ | > minAmount |
| currency | ✅ | Max 10 chars |
| vendorCode | ❌ | Max 255 chars |
| poNumber | ❌ | Max 255 chars |
| classification | ❌ | Max 255 chars |
| enabled | ❌ | Boolean (default: true) |
| validFrom | ✅ | Date (yyyy-MM-dd) |
| validTo | ✅ | >= validFrom |

## 🔧 Configuration

### Database
```yaml
datasource:
  url: jdbc:postgresql://op-pdb-dev-4001.postgres.database.azure.com:5432/guidant_db?sslmode=require
  username: oppdbdevadmin01
```

### Server
```yaml
server:
  port: 8070
```

## 🗂️ Sort Fields

Available sort options:
- `createdAt` (default)
- `updatedAt`
- `userName`
- `fromAmount`
- `toAmount`
- `approvalLevel`
- `currency`
- `classification`

## 📚 Documentation Files

1. **DOA_RULES_API_DOCUMENTATION.md** - Complete API reference
2. **DOA_RULES_README.md** - Setup & quick start guide
3. **DOA-Rules-API.postman_collection.json** - Postman collection
4. **DOA_RULES_IMPLEMENTATION_SUMMARY.md** - Implementation details

## 🛠️ Useful Commands

```bash
# Build
.\mvnw.cmd clean install

# Run
.\mvnw.cmd spring-boot:run

# Check health
curl http://localhost:8070/actuator/health

# View logs
tail -f logs/application.log
```

## 📞 Support

For detailed documentation, see:
- **DOA_RULES_API_DOCUMENTATION.md**
- **DOA_RULES_README.md**

---

**Version**: 1.0.0 | **Last Updated**: December 26, 2024

