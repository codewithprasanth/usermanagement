# DOA Rules - User Details Population Fix

## ✅ Issue Resolved

**Problem:** DOA Rules API responses were not including user details (userName, emailId, country) in the response.

**Root Cause:** The mapper was trying to read from transient entity fields that were never populated from the database.

**Solution:** Updated the service layer to fetch user details from the `users` table and pass them to the mapper.

---

## 🔧 Changes Made

### 1. Updated DoaRuleMapper

**File:** `DoaRuleMapper.java`

**Changes:**
- Added overloaded `toResponse(DoaRule entity, User user)` method
- Populates userName, emailId from User entity if available
- Maintains backward compatibility with existing `toResponse(DoaRule entity)` method

```java
public DoaRuleResponse toResponse(DoaRule entity, User user) {
    DoaRuleResponse.DoaRuleResponseBuilder builder = DoaRuleResponse.builder()
            // ...existing fields...
            
    // Populate user details if available
    if (user != null) {
        builder.userName(user.getFullName())
                .emailId(user.getEmail())
                .country(null); // TODO: Fetch from user attributes
    }
    
    return builder.build();
}
```

---

### 2. Updated DoaRuleService

**File:** `DoaRuleService.java`

**Changes:**
- Injected `UserRepository` dependency
- Updated all methods to fetch User entity from database
- Pass User entity to mapper for populating response

**Methods Updated:**
1. ✅ `getAllDoaRules()` - Fetches user for each DOA rule
2. ✅ `getDoaRuleById()` - Fetches user details
3. ✅ `createDoaRule()` - Fetches user details after creation
4. ✅ `updateDoaRule()` - Fetches user details after update
5. ✅ `getDoaRulesByUserId()` - Fetches user for each rule
6. ✅ `getDoaRulesByEntity()` - Fetches user for each rule

**Example:**
```java
@Transactional(readOnly = true)
public Page<DoaRuleResponse> getAllDoaRules(...) {
    Page<DoaRule> doaRulesPage = doaRuleRepository.findAll(spec, pageable);
    
    return doaRulesPage.map(doaRule -> {
        User user = userRepository.findById(doaRule.getUserId()).orElse(null);
        return doaRuleMapper.toResponse(doaRule, user);
    });
}
```

---

## 📊 Response Field Mapping

### Before Fix:
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userName": null,        // ❌ Not populated
  "emailId": null,         // ❌ Not populated
  "country": null,         // ❌ Not populated
  "fromAmount": 100,
  "toAmount": 10000,
  "currency": "USD",
  "entity": "Finance",
  ...
}
```

### After Fix:
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userName": "John Doe",  // ✅ Populated from users table
  "emailId": "john@example.com", // ✅ Populated from users table
  "country": null,         // TODO: Need to add country field to users table
  "fromAmount": 100,
  "toAmount": 10000,
  "currency": "USD",
  "entity": "Finance",
  ...
}
```

---

## 🗄️ Database Integration

### Users Table Columns Used:
| Database Column | Response Field | Source |
|----------------|----------------|--------|
| `user_id` | `userId` | Already existed |
| `full_name` | `userName` | ✅ Now populated |
| `email` | `emailId` | ✅ Now populated |
| - | `country` | ⚠️ TODO: Not in users table |

---

## ⚠️ Remaining TODOs

### 1. Country Field
**Issue:** `country` field is in the response DTO but not in the `users` table.

**Options:**
- **Option A:** Add `country` column to `users` table
- **Option B:** Fetch from Keycloak user attributes
- **Option C:** Remove from response if not needed

**Recommendation:** Add to `users` table for consistency.

### 2. Company Code Field
**Issue:** `companyCode` field is in response but no source.

**Options:**
- Add to `users` table
- Fetch from a separate `entities` or `companies` table
- Remove if not needed

### 3. User Countries Field
**Issue:** `userCountries` (List<String>) is always null.

**Options:**
- Create separate `user_countries` table
- Store as JSON in `users` table
- Remove if not needed

---

## 🚀 Performance Considerations

### Current Implementation:
- Each DOA rule triggers a separate database query to fetch user details
- For paginated results with 10 items, this results in 10 additional queries

### Optimization Options (Future):

**1. Batch Fetching:**
```java
// Collect all user IDs
Set<UUID> userIds = doaRulesPage.getContent().stream()
    .map(DoaRule::getUserId)
    .collect(Collectors.toSet());

// Fetch all users in one query
Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
    .collect(Collectors.toMap(User::getUserId, Function.identity()));

// Map results
return doaRulesPage.map(doaRule -> {
    User user = userMap.get(doaRule.getUserId());
    return doaRuleMapper.toResponse(doaRule, user);
});
```

**2. JPA Join Fetch:**
- Add `@ManyToOne` relationship in DoaRule entity
- Use `JOIN FETCH` in repository queries
- Would require changing entity structure

**3. Caching:**
- Cache User entities by ID
- Reduce database hits for frequently accessed users

---

## ✅ Testing Checklist

### API Responses Now Include:

- [ ] ✅ `userName` - Full name from users table
- [ ] ✅ `emailId` - Email from users table
- [ ] ⚠️ `country` - Still null (needs implementation)
- [ ] ✅ `userId` - User UUID
- [ ] ✅ All other DOA rule fields

### Test Scenarios:

1. **User Exists in Database:**
   - ✅ userName and emailId populated
   
2. **User Not in Database:**
   - ✅ userName and emailId are null
   - ✅ API still returns successfully
   
3. **Paginated Results:**
   - ✅ All items have user details populated
   
4. **Single Rule Fetch:**
   - ✅ User details populated

---

## 🔄 Migration Steps

### For Existing Data:

If users table is not fully populated:

```sql
-- Verify users table has all DOA rule users
SELECT DISTINCT d.user_id 
FROM doa_rules d 
LEFT JOIN users u ON d.user_id = u.user_id 
WHERE u.user_id IS NULL;

-- If any users are missing, they need to be synced from Keycloak
```

---

## 📝 Build Status

**Status:** ✅ SUCCESS

No compilation errors. All changes integrated successfully.

---

## 💡 Usage Example

### API Request:
```bash
GET /api/v1/doa-rules?page=0&size=10
Authorization: Bearer YOUR_TOKEN
```

### API Response:
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userName": "John Doe",              // ✅ Now populated
      "userId": "123e4567-e89b-12d3-a456-426614174000",
      "emailId": "john.doe@example.com",   // ✅ Now populated
      "country": null,                     // ⚠️ TODO
      "fromAmount": 0.00,
      "toAmount": 10000.00,
      "currency": "USD",
      "vendorCode": "VENDOR001",
      "entity": "Finance Department",
      "isActive": true,
      "enabled": true,
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ],
  "totalElements": 50,
  "totalPages": 5
}
```

---

## 🎯 Summary

### What Was Fixed:
✅ userName now populated from users.full_name  
✅ emailId now populated from users.email  
✅ All DOA rule methods updated  
✅ Backward compatible changes  
✅ No breaking changes to API  

### What Needs Attention:
⚠️ country field not yet populated  
⚠️ companyCode field not yet populated  
⚠️ userCountries field not yet populated  
💡 Consider batch fetching for performance  

---

**Implementation Date:** December 26, 2024  
**Status:** ✅ Complete  
**Build:** ✅ SUCCESS  
**Version:** 1.1.0

