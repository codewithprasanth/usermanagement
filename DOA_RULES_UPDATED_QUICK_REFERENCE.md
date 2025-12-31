# DOA Rules API - Updated Quick Reference

## ✅ Schema Changes Applied

**Changed:**
- `entityId` (UUID) → `entity` (String)

**Removed:**
- `validFrom` (Date)
- `validTo` (Date)

---

## 📝 Updated Request Body

### Create/Update DOA Rule
```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "entity": "Finance Department",
  "approvalLevel": 1,
  "minAmount": 0.00,
  "maxAmount": 10000.00,
  "currency": "USD",
  "vendorCode": "VENDOR001",
  "poNumber": "PO-2024-001",
  "classification": "CAPEX",
  "enabled": true
}
```

**Validation Rules:**
- `userId`: Required, valid UUID
- `entity`: Required, String (max 255 chars)
- `approvalLevel`: Required, >= 1
- `minAmount`: Required, >= 0
- `maxAmount`: Required, > minAmount
- `currency`: Required, max 10 chars

---

## 📋 Updated Endpoints

### Query Parameters

```
GET /api/v1/doa-rules
  ?page=0
  &size=10
  &sort=createdAt,desc
  &userId=123e4567-e89b-12d3-a456-426614174000
  &entity=Finance%20Department          ✅ Changed from entityId (UUID)
  &currency=USD
  &classification=CAPEX
  &isActive=true
  &enabled=true
```

### Get by Entity

**Before:**
```
GET /api/v1/doa-rules/entity/789e4567-e89b-12d3-a456-426614174000
```

**After:**
```
GET /api/v1/doa-rules/entity/Finance%20Department
```

---

## 💻 cURL Examples

### Create DOA Rule
```bash
curl -X POST "http://localhost:8070/api/v1/doa-rules" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "entity": "Finance Department",
    "approvalLevel": 1,
    "minAmount": 0.00,
    "maxAmount": 10000.00,
    "currency": "USD"
  }'
```

### Filter by Entity
```bash
curl -X GET "http://localhost:8070/api/v1/doa-rules?entity=Finance%20Department" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Get by Entity
```bash
curl -X GET "http://localhost:8070/api/v1/doa-rules/entity/Finance%20Department" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📊 Response Example

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
  "entity": "Finance Department",
  "isActive": true,
  "enabled": true,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-05T15:30:00Z",
  "createdByUserId": "456e4567-e89b-12d3-a456-426614174000"
}
```

**Note:** `validFrom`, `validTo`, and `entityId` fields removed

---

## 🗄️ Database Migration

**Run this before starting the application:**

```bash
psql -h op-pdb-dev-4001.postgres.database.azure.com \
     -p 5432 \
     -U oppdbdevadmin01 \
     -d guidant_db \
     -f src/main/resources/db/migration/update_doa_rules_structure.sql
```

---

## 🔄 Migration Checklist

- [ ] Run database migration script
- [ ] Update Postman collection
  - [ ] Change `entityId` to `entity` in all requests
  - [ ] Remove `validFrom` and `validTo` fields
  - [ ] Update entity values from UUID to String
- [ ] Update frontend application
  - [ ] Change entity field from UUID to String
  - [ ] Remove date fields from forms
- [ ] Update documentation
- [ ] Test all endpoints

---

**Version:** 1.1.0  
**Last Updated:** December 26, 2024  
**Status:** ✅ Ready for Use

