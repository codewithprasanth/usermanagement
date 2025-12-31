# DOA Rules Module - Quick Start Guide

## Overview
The DOA (Delegation of Authority) Rules module is a separate package within the User Management application that handles delegation authority rules, approval levels, and amount ranges for different entities and users.

## Package Structure
```
com.sprintap.doarules/
├── controller/
│   └── DoaRuleController.java
├── service/
│   └── DoaRuleService.java
├── repository/
│   ├── DoaRuleRepository.java
│   └── DoaRuleSpecification.java
├── entity/
│   └── DoaRule.java
├── dto/
│   ├── DoaRuleRequest.java
│   ├── DoaRuleResponse.java
│   ├── ToggleStatusRequest.java
│   └── ToggleStatusResponse.java
├── mapper/
│   └── DoaRuleMapper.java
└── exception/
    ├── DoaRuleNotFoundException.java
    └── EntityNotFoundException.java
```

## Database Configuration

### Prerequisites
- PostgreSQL database
- Database connection details configured in `application.yaml`

### Connection Details (as configured)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://op-pdb-dev-4001.postgres.database.azure.com:5432/guidant_db?sslmode=require
    driver-class-name: org.postgresql.Driver
    username: oppdbdevadmin01
    password: qWIKR3qfVEpKV3Tc
```

### Database Migration
Run the SQL script to add missing columns:
```bash
psql -h op-pdb-dev-4001.postgres.database.azure.com -p 5432 -U oppdbdevadmin01 -d guidant_db -f src/main/resources/db/migration/add_valid_dates_to_doa_rules.sql
```

Or execute directly in your PostgreSQL client:
```sql
-- Add valid_from and valid_to columns
ALTER TABLE public.doa_rules ADD COLUMN IF NOT EXISTS valid_from DATE;
ALTER TABLE public.doa_rules ADD COLUMN IF NOT EXISTS valid_to DATE;
ALTER TABLE public.doa_rules ADD CONSTRAINT chk_valid_date_range CHECK (valid_from <= valid_to);
```

## Building the Application

### Using Maven Wrapper (Recommended)
```bash
# Windows
.\mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

### Skip Tests
```bash
.\mvnw.cmd clean install -DskipTests
```

## Running the Application

### Development Mode
```bash
.\mvnw.cmd spring-boot:run
```

### Production Mode
```bash
java -jar target/usermanagement-0.0.1-SNAPSHOT.jar
```

### With Custom Profile
```bash
java -jar target/usermanagement-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## API Endpoints

### Base URL
```
http://localhost:8070/api/v1/doa-rules
```

### Available Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/doa-rules` | Get all DOA rules (with filters) |
| GET | `/api/v1/doa-rules/{id}` | Get DOA rule by ID |
| POST | `/api/v1/doa-rules` | Create new DOA rule |
| PUT | `/api/v1/doa-rules/{id}` | Update DOA rule |
| DELETE | `/api/v1/doa-rules/{id}` | Delete DOA rule (soft delete) |
| PATCH | `/api/v1/doa-rules/{id}/toggle-status` | Toggle enabled status |
| GET | `/api/v1/doa-rules/user/{userId}` | Get DOA rules by user ID |
| GET | `/api/v1/doa-rules/entity/{entityId}` | Get DOA rules by entity ID |

## Quick Examples

### 1. Get All DOA Rules
```bash
curl -X GET "http://localhost:8070/api/v1/doa-rules?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Create DOA Rule
```bash
curl -X POST "http://localhost:8070/api/v1/doa-rules" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "entityId": "789e4567-e89b-12d3-a456-426614174000",
    "approvalLevel": 1,
    "minAmount": 0.00,
    "maxAmount": 10000.00,
    "currency": "USD",
    "classification": "CAPEX",
    "enabled": true,
    "validFrom": "2024-01-01",
    "validTo": "2024-12-31"
  }'
```

### 3. Get DOA Rules with Filters
```bash
curl -X GET "http://localhost:8070/api/v1/doa-rules?userId=123e4567-e89b-12d3-a456-426614174000&currency=USD&isActive=true" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Toggle Status
```bash
curl -X PATCH "http://localhost:8070/api/v1/doa-rules/{id}/toggle-status" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"enabled": false}'
```

## Filter Parameters

### Available Filters
- `userId` - Filter by user UUID
- `entityId` - Filter by entity UUID
- `currency` - Filter by currency code (e.g., USD, EUR)
- `classification` - Filter by classification type
- `isActive` - Filter by active status (true/false)
- `enabled` - Filter by enabled status (true/false)

### Pagination Parameters
- `page` - Page number (default: 0)
- `size` - Items per page (default: 10)
- `sort` - Sort field and direction (default: createdAt,desc)
  - Format: `fieldName,direction`
  - Examples: `userName,asc`, `createdAt,desc`, `fromAmount,asc`

## Authentication

All endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Getting a Token
Use the existing authentication endpoints in the User Management module:
```bash
POST /api/v1/auth/token
```

## Validation Rules

### Request Validation
- **userId**: Required, valid UUID
- **entityId**: Required, valid UUID
- **approvalLevel**: Required, integer >= 1
- **minAmount**: Required, >= 0
- **maxAmount**: Required, > 0, must be > minAmount
- **currency**: Required, max 10 characters
- **validFrom**: Required, format: yyyy-MM-dd
- **validTo**: Required, format: yyyy-MM-dd, must be >= validFrom

### Database Constraints
- Amount range: `min_amount <= max_amount`
- Date range: `valid_from <= valid_to`
- Foreign keys: `user_id` and `entity_id` must exist in respective tables

## Error Handling

### Common Error Codes
- **400 Bad Request**: Validation errors, invalid data
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: DOA rule, user, or entity not found
- **500 Internal Server Error**: Unexpected server error

### Error Response Format
```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "status": 400,
  "timestamp": "2024-12-26T10:30:00"
}
```

## Testing with Postman

### Import Collection
1. Open Postman
2. Click Import
3. Select `DOA-Rules-API.postman_collection.json`
4. Configure environment variables:
   - `base_url`: http://localhost:8070
   - `jwt_token`: Your JWT token

### Run Tests
1. Get a JWT token from the auth endpoint
2. Set the token in collection variables
3. Execute requests in sequence

## Logging

### Log Levels
Configure in `application.yaml`:
```yaml
logging:
  level:
    com.sprintap.doarules: DEBUG
```

### Log File Location
```
logs/application.log
```

## Monitoring

### Health Check
```bash
curl http://localhost:8070/actuator/health
```

### Metrics
```bash
curl http://localhost:8070/actuator/metrics
```

## Security Considerations

1. **JWT Token**: Always include valid JWT token
2. **HTTPS**: Use HTTPS in production
3. **Sensitive Data**: Never log sensitive information
4. **Database Credentials**: Use environment variables in production
5. **CORS**: Configure allowed origins in `application.yaml`

## Integration with Other Services

### From React Application
```javascript
const response = await fetch('http://localhost:8070/api/v1/doa-rules', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### From Another Spring Service
```java
@Autowired
private RestTemplate restTemplate;

HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);
HttpEntity<DoaRuleRequest> entity = new HttpEntity<>(request, headers);

ResponseEntity<DoaRuleResponse> response = restTemplate.exchange(
    "http://localhost:8070/api/v1/doa-rules",
    HttpMethod.POST,
    entity,
    DoaRuleResponse.class
);
```

## Troubleshooting

### Issue: "Cannot resolve symbol 'data'"
**Solution**: Run Maven build to download dependencies
```bash
.\mvnw.cmd clean install
```

### Issue: Database connection refused
**Solution**: Check database connection details and network access
```bash
# Test connection
psql -h op-pdb-dev-4001.postgres.database.azure.com -p 5432 -U oppdbdevadmin01 -d guidant_db
```

### Issue: JWT token expired
**Solution**: Get a new token from the auth endpoint

### Issue: Foreign key constraint violation
**Solution**: Ensure user_id and entity_id exist in their respective tables

## Performance Tips

1. **Use Pagination**: Always specify reasonable page sizes
2. **Apply Filters**: Use filters to reduce result set
3. **Proper Indexing**: Ensure database indexes on frequently queried columns
4. **Connection Pooling**: Configure HikariCP settings in application.yaml

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

## Next Steps

1. Review the complete API documentation: `DOA_RULES_API_DOCUMENTATION.md`
2. Import the Postman collection: `DOA-Rules-API.postman_collection.json`
3. Test all endpoints using Postman
4. Integrate with your frontend application
5. Configure security and CORS for your environment

## Support

For questions or issues:
- Check the API documentation
- Review logs in `logs/application.log`
- Contact the development team

## Version History

- **1.0.0** (December 26, 2024)
  - Initial release
  - CRUD operations for DOA rules
  - Filtering and pagination
  - Soft delete functionality
  - Status toggle feature

