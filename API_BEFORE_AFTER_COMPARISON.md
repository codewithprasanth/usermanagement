# API Documentation: Before vs After Comparison

## The Root Cause of Frontend Integration Issues

The API documentation was showing incorrect parameter names and types, causing the frontend to send requests in the wrong format.

---

## Issue #1: Role ID Not Found Error

### Why It Happened
Frontend was using role **names** in API paths, but the backend expects role **IDs** (UUIDs).

### ❌ What Was Wrong (Old Documentation)

```http
DELETE /api/roles/{roleName}
PUT /api/roles/{roleName}
GET /api/roles/{roleName}/privileges
```

Frontend implementation:
```javascript
// This was failing with "Role not found"
await fetch(`/api/roles/role_manager`, { method: 'DELETE' });
```

### ✅ What Is Correct (New Documentation)

```http
DELETE /api/roles/{roleId}
PUT /api/roles/{roleId}
GET /api/roles/{roleId}/privileges
```

Frontend implementation:
```javascript
// This works correctly
await fetch(`/api/roles/65ca6853-2e06-448f-bad0-bed2995c202d`, { 
  method: 'DELETE' 
});
```

**Fix**: Always use the UUID from the role's `id` field, never the `name` field.

---

## Issue #2: Privilege IDs Not Found Error

### Why It Happened
Frontend was sending privilege **names** in request bodies, but the backend expects privilege **IDs** (UUIDs).

### ❌ What Was Wrong (Old Documentation)

#### Create Role Request
```json
{
  "roleName": "manager",
  "privilegeNames": ["priv_user_management", "priv_view_reports"]
}
```

#### Update Role Request
```json
{
  "privilegesToAdd": ["priv_new_privilege"],
  "privilegesToRemove": ["priv_old_privilege"]
}
```

Frontend was doing:
```javascript
// This was failing with "Privilege not found"
await fetch('/api/roles', {
  method: 'POST',
  body: JSON.stringify({
    roleName: "manager",
    privilegeNames: ["priv_user_management"]  // ❌ Wrong field name
  })
});
```

### ✅ What Is Correct (New Documentation)

#### Create Role Request
```json
{
  "roleName": "manager",
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678"]
}
```

#### Update Role Request
```json
{
  "privilegeIdsToAdd": ["a3b5c7d9-1234-5678-9abc-def012345678"],
  "privilegeIdsToRemove": ["b4c6d8e0-2345-6789-abcd-ef0123456789"]
}
```

Frontend should do:
```javascript
// Step 1: Fetch privileges to get their IDs
const privileges = await fetch('/api/roles/privileges').then(r => r.json());
// privileges = [{ id: "uuid", name: "priv_user_management", ... }]

// Step 2: Extract IDs
const selectedPrivilegeIds = selectedPrivileges.map(p => p.id);

// Step 3: Use IDs in request
await fetch('/api/roles', {
  method: 'POST',
  body: JSON.stringify({
    roleName: "manager",
    privilegeIds: selectedPrivilegeIds  // ✅ Correct field name
  })
});
```

**Fix**: 
1. Fetch privileges from `GET /api/roles/privileges`
2. Display using `displayName` field
3. Track selections using `id` field
4. Send `id` values in `privilegeIds` array

---

## Issue #3: Missing Group User Count

### Why It Happened
Old documentation didn't show the `userCount` field, so frontend wasn't displaying it.

### ❌ What Was Wrong (Old Documentation)

```json
{
  "id": "uuid",
  "name": "Engineering Team",
  "path": "/Engineering Team"
}
```

No `userCount` field documented, so frontend couldn't show how many users are in each group.

### ✅ What Is Correct (New Documentation)

```json
{
  "id": "f8e7d6c5-4321-0987-bcde-f01234567890",
  "name": "Engineering Team",
  "userCount": 15
}
```

Frontend can now display:
```javascript
groups.map(group => (
  <div>
    <h3>{group.name}</h3>
    <span>{group.userCount} members</span>  {/* ✅ Now available */}
  </div>
));
```

**Fix**: The `userCount` field is always present in group responses. Use it to show group size.

---

## Issue #4: Group API Using Wrong Field Names

### Why It Happened
Documentation showed `roleNames` and `privilegeNames` but backend expects `roleIds` and `privilegeIds`.

### ❌ What Was Wrong (Old Documentation)

#### Create Group Request
```json
{
  "groupName": "Engineering Team",
  "roleNames": ["role_developer", "role_tester"],
  "privilegeNames": ["priv_code_review"]
}
```

#### Update Group Roles/Privileges Request
```json
{
  "rolesToAdd": ["role_new"],
  "rolesToRemove": ["role_old"],
  "privilegesToAdd": ["priv_new"],
  "privilegesToRemove": ["priv_old"]
}
```

### ✅ What Is Correct (New Documentation)

#### Create Group Request
```json
{
  "groupName": "Engineering Team",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "privilegeIds": ["a3b5c7d9-1234-5678-9abc-def012345678"]
}
```

#### Update Group Roles/Privileges Request
```json
{
  "roleIdsToAdd": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "roleIdsToRemove": ["468bdbcc-ae15-47f6-a529-4c104f589010"],
  "privilegeIdsToAdd": ["a3b5c7d9-1234-5678-9abc-def012345678"],
  "privilegeIdsToRemove": ["b4c6d8e0-2345-6789-abcd-ef0123456789"]
}
```

**Fix**: Always use `roleIds` and `privilegeIds` (plural with "Ids" suffix), never `roleNames` or `privilegeNames`.

---

## Issue #5: User API Using Wrong Field Names

### Why It Happened
Documentation showed `roleNames` but backend expects `roleIds`.

### ❌ What Was Wrong (Old Documentation)

#### Create User Request
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePass123!",
  "roleNames": ["role_developer"],
  "groupIds": ["group-uuid"]
}
```

#### Update User Request
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roleNamesToAdd": ["role_senior"],
  "roleNamesToRemove": ["role_junior"]
}
```

### ✅ What Is Correct (New Documentation)

#### Create User Request
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "SecurePass123!",
  "roleIds": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "groupIds": ["f8e7d6c5-4321-0987-bcde-f01234567890"]
}
```

#### Update User Request
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roleIdsToAdd": ["65ca6853-2e06-448f-bad0-bed2995c202d"],
  "roleIdsToRemove": ["468bdbcc-ae15-47f6-a529-4c104f589010"],
  "groupIdsToAdd": ["f8e7d6c5-4321-0987-bcde-f01234567890"],
  "groupIdsToRemove": ["e7d6c5b4-3210-9876-abcd-e01234567890"]
}
```

**Fix**: Use `roleIds`, `roleIdsToAdd`, `roleIdsToRemove` - never `roleNames` or `roleNamesToAdd`.

---

## Issue #6: User Response Structure

### Why It Happened
Documentation showed roles and groups as simple string arrays, but actual response has structured objects.

### ❌ What Was Wrong (Old Documentation)

```json
{
  "id": "user-uuid",
  "email": "john@example.com",
  "roles": ["role_developer"],
  "groups": ["Engineering Team"]
}
```

Frontend couldn't access role IDs or group IDs properly.

### ✅ What Is Correct (New Documentation)

```json
{
  "id": "0580fded-cb94-4304-9be1-7cc54b806b92",
  "email": "john@example.com",
  "roles": [
    {
      "roleId": "65ca6853-2e06-448f-bad0-bed2995c202d",
      "roleName": "role_developer",
      "roleDisplayName": "developer"
    }
  ],
  "groups": [
    {
      "groupId": "f8e7d6c5-4321-0987-bcde-f01234567890",
      "groupName": "Engineering Team"
    }
  ]
}
```

Frontend can now:
```javascript
// Access role ID for API calls
const roleId = user.roles[0].roleId;

// Display role name without prefix
const displayName = user.roles[0].roleDisplayName;

// Access group ID for API calls
const groupId = user.groups[0].groupId;
```

**Fix**: User roles and groups are objects with multiple fields. Use `roleId` and `groupId` for API operations.

---

## Issue #7: Missing displayName Fields

### Why It Happened
Old documentation didn't show `displayName` fields, so frontend had to manually strip prefixes.

### ❌ What Was Wrong (Old Documentation)

```json
{
  "id": "uuid",
  "name": "role_manager",
  "description": "Manager role"
}
```

Frontend had to do:
```javascript
// Manual prefix stripping
const displayName = role.name.startsWith('role_') 
  ? role.name.substring(5) 
  : role.name;
```

### ✅ What Is Correct (New Documentation)

```json
{
  "id": "65ca6853-2e06-448f-bad0-bed2995c202d",
  "name": "role_manager",
  "displayName": "manager",
  "description": "Manager role"
}
```

Frontend can now simply:
```javascript
// Use displayName directly
<span>{role.displayName}</span>  // Shows "manager"
```

**Fix**: Always use the `displayName` field for UI display. It's provided for both roles and privileges.

---

## Complete Field Name Mapping

| Old Documentation (WRONG) | New Documentation (CORRECT) | Context |
|---------------------------|----------------------------|---------|
| `privilegeNames` | `privilegeIds` | Creating roles |
| `privilegesToAdd` | `privilegeIdsToAdd` | Updating roles |
| `privilegesToRemove` | `privilegeIdsToRemove` | Updating roles |
| `roleNames` | `roleIds` | Creating groups/users |
| `rolesToAdd` | `roleIdsToAdd` | Updating groups |
| `rolesToRemove` | `roleIdsToRemove` | Updating groups |
| `privilegeNames` | `privilegeIds` | Creating groups |
| `privilegesToAdd` | `privilegeIdsToAdd` | Updating groups |
| `privilegesToRemove` | `privilegeIdsToRemove` | Updating groups |
| `roleNames` | `roleIds` | Creating users |
| `roleNamesToAdd` | `roleIdsToAdd` | Updating users |
| `roleNamesToRemove` | `roleIdsToRemove` | Updating users |
| `/api/roles/{roleName}` | `/api/roles/{roleId}` | Role endpoints |

---

## Summary of Changes

### What Changed in API Paths
- **Role endpoints**: Now use `{roleId}` instead of `{roleName}`
- **All IDs are UUIDs**: 36-character strings like `65ca6853-2e06-448f-bad0-bed2995c202d`

### What Changed in Request Bodies
- **All references to names changed to IDs**: `privilegeNames` → `privilegeIds`, `roleNames` → `roleIds`
- **Update operations use explicit field names**: `roleIdsToAdd`, `roleIdsToRemove`, etc.

### What Changed in Response Bodies
- **Added displayName fields**: All roles and privileges now include `displayName` for UI display
- **Added userCount to groups**: Groups now include `userCount` field
- **Structured user roles/groups**: User responses have nested objects for roles and groups

---

## Action Items for Frontend Team

1. **Search and Replace**:
   - Replace `privilegeNames` with `privilegeIds`
   - Replace `roleNames` with `roleIds`
   - Replace `rolesToAdd` with `roleIdsToAdd`
   - Replace `privilegesToAdd` with `privilegeIdsToAdd`
   - Replace path parameters from names to IDs

2. **Update Data Flow**:
   - Fetch roles/privileges to get IDs
   - Store IDs in component state
   - Display using `displayName` fields
   - Send IDs in API requests

3. **Update UI Components**:
   - Show `group.userCount` in group listings
   - Use `role.displayName` instead of stripping prefixes
   - Access `user.roles[].roleId` for role IDs
   - Access `user.groups[].groupId` for group IDs

4. **Test Thoroughly**:
   - Verify no "Role not found" errors
   - Verify no "Privilege not found" errors
   - Confirm group user counts display
   - Confirm role names display without prefixes

---

## Quick Test

To verify your frontend is using correct formats:

```javascript
// ✅ This should work
const response = await fetch('/api/roles', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const roles = await response.json();

// Check structure
console.log(roles[0]);
// Should show: { id: "uuid", name: "role_...", displayName: "...", ... }

// ✅ Create role should work with this
await fetch('/api/roles', {
  method: 'POST',
  body: JSON.stringify({
    roleName: "test",
    privilegeIds: [roles[0].id]  // Use ID not name
  })
});

// ✅ Delete should work with this
await fetch(`/api/roles/${roles[0].id}`, {  // Use ID in path
  method: 'DELETE'
});
```

---

**Bottom Line**: The API always uses **IDs (UUIDs)**, never names. The old documentation incorrectly showed names, causing all the integration issues.

