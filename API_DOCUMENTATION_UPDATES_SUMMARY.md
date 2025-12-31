# API Documentation Updates Summary

## Date: December 8, 2025

This document outlines all the critical fixes and updates made to the `FRONTEND_API_DOCUMENTATION.md` to resolve frontend integration conflicts.

---

## 🎯 Critical Issues Fixed

### 1. **GroupDTO Structure Correction**
**Issue:** Documentation showed `path` field in GroupDTO, but it doesn't exist in the actual implementation.

**Fixed:**
```typescript
// ❌ WRONG (Old Documentation)
interface GroupDTO {
  id: string;
  name: string;
  path: string;  // This field doesn't exist!
}

// ✅ CORRECT (Updated Documentation)
interface GroupDTO {
  id: string;
  name: string;
}
```

**Impact:** Frontend code trying to access `group.path` would fail with undefined errors.

---

### 2. **Username Field Clarification**
**Issue:** Documentation unclear about username generation.

**Fixed:**
- Clarified that username is automatically set to email address
- Removed any mention of `username` field in request bodies
- Added explicit note: "Do NOT send a username field"

**Impact:** Prevents confusion about sending username in create/update requests.

---

### 3. **UUID vs Name Confusion**
**Issue:** Mixed usage of names and IDs in examples.

**Fixed:**
- All API path parameters now use UUIDs: `/api/roles/{roleId}` not `/api/roles/{roleName}`
- All request body fields use IDs: `roleIds`, `groupIds`, `privilegeIds`
- Added examples with actual UUID format: `"650e8400-e29b-41d4-a716-446655440001"`

**Impact:** Eliminates confusion between when to use names vs IDs.

---

### 4. **Response Format Standardization**
**Issue:** Inconsistent response structures across endpoints.

**Fixed:**
- All create/update/delete operations now include `timestamp` field
- All success responses follow consistent structure:
```json
{
  "message": "Operation successful",
  "timestamp": "2025-12-08T10:30:00.123",
  "data": { ... }
}
```

**Impact:** Frontend can reliably parse responses with consistent structure.

---

### 5. **Display Name Format**
**Issue:** Examples showed capitalized display names, but actual API returns lowercase.

**Fixed:**
```json
// ❌ WRONG (Old Documentation)
"displayName": "Manager"
"displayName": "User Management"

// ✅ CORRECT (Updated Documentation)
"displayName": "manager"
"displayName": "user_management"
```

**Impact:** Frontend UI will display correct values from API.

---

### 6. **Validation Field Requirements**
**Issue:** Unclear which fields are required vs optional in update requests.

**Fixed:**
- Marked all `IdsToAdd` and `IdsToRemove` fields as "Required (can be empty array)"
- Clarified that these fields should be `[]` not `null`
- Added validation examples showing correct and incorrect formats

**Impact:** Prevents 400 Bad Request errors from null values.

---

### 7. **Timestamp Format Clarification**
**Issue:** Inconsistent timestamp formats in examples.

**Fixed:**
- `createdTimestamp`: Unix epoch milliseconds (e.g., `1733655000000`)
- `timestamp` in responses: ISO 8601 format (e.g., `"2025-12-08T10:30:00.123"`)

**Impact:** Frontend can correctly parse and display timestamps.

---

## 📋 Complete API Format Reference

### User APIs

#### Create User
```
POST /api/users
Body: { email, firstName, lastName, password, enabled?, emailVerified?, 
        entityCode?, countryCode?, roleIds?, groupIds? }
Response: { message, timestamp, user: UserDTO }
```

#### Update User
```
PUT /api/users/{userId}
Body: { email, firstName, lastName, enabled?, emailVerified?, entityCode?, 
        countryCode?, roleIdsToAdd?, roleIdsToRemove?, groupIdsToAdd?, 
        groupIdsToRemove? }
Response: { message, timestamp, user: UserDTO }
```

#### Get User
```
GET /api/users/{userId}
Response: UserDTO
```

#### Get All Users
```
GET /api/users
Response: UserDTO[]
```

#### Delete User
```
DELETE /api/users/{userId}
Response: { message, timestamp }
```

---

### Role APIs

#### Create Role
```
POST /api/roles
Body: { roleName, description?, privilegeIds? }
Response: { message, timestamp, role: RoleDTO }
```

#### Update Role
```
PUT /api/roles/{roleId}
Body: { description?, privilegeIdsToAdd: [], privilegeIdsToRemove: [] }
Response: { message, timestamp, role: RoleDTO }
```

#### Get All Roles
```
GET /api/roles
Response: RoleDTO[]
```

#### Get Privileges for Role
```
GET /api/roles/{roleId}/privileges
Response: PrivilegeDTO[]
```

#### Get All Privileges
```
GET /api/roles/privileges
Response: PrivilegeDTO[]
```

#### Delete Role
```
DELETE /api/roles/{roleId}
Response: { message, timestamp }
```

---

### Group APIs

#### Create Group
```
POST /api/groups
Body: { groupName, roleIds?, privilegeIds? }
Response: { message, timestamp, group: GroupDTO }
```

#### Get All Groups
```
GET /api/groups
Response: GroupDTO[]
```

#### Get Group Roles & Privileges
```
GET /api/groups/{groupId}/roles-privileges
Response: { roles: RoleDTO[], privileges: PrivilegeDTO[] }
```

#### Get Users in Group
```
GET /api/groups/{groupId}/users
Response: UserDTO[]
```

#### Update Group Users
```
PUT /api/groups/{groupId}/users
Body: { userIdsToAdd: [], userIdsToRemove: [] }
Response: { message, timestamp }
```

#### Update Group Roles & Privileges
```
PUT /api/groups/{groupId}/roles-privileges
Body: { roleIdsToAdd: [], roleIdsToRemove: [], privilegeIdsToAdd: [], 
        privilegeIdsToRemove: [] }
Response: { message, timestamp }
```

#### Delete Group
```
DELETE /api/groups/{groupId}
Response: { message, timestamp }
```

---

## 🔍 Data Models (Corrected)

### UserDTO
```typescript
{
  id: string;                                    // UUID
  username: string;                              // Same as email
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  createdTimestamp: number;                      // Unix epoch milliseconds
  attributes: { [key: string]: string[] };       // e.g., { "entity_code": ["ENT001"] }
  roles: Array<{
    roleId: string;                              // UUID
    roleName: string;                            // e.g., "role_admin"
    roleDisplayName: string;                     // e.g., "admin" (lowercase)
  }>;
  groups: Array<{
    groupId: string;                             // UUID
    groupName: string;
  }>;
}
```

### RoleDTO
```typescript
{
  id: string;                                    // UUID
  name: string;                                  // e.g., "role_manager"
  displayName: string;                           // e.g., "manager" (lowercase)
  description: string;
  composite: boolean;
}
```

### PrivilegeDTO
```typescript
{
  id: string;                                    // UUID
  name: string;                                  // e.g., "priv_user_management"
  displayName: string;                           // e.g., "user_management" (lowercase)
  description: string;
}
```

### GroupDTO
```typescript
{
  id: string;                                    // UUID
  name: string;
  // NO path field!
}
```

---

## ⚠️ Common Mistakes to Avoid

### 1. Using Names Instead of IDs
```typescript
// ❌ WRONG
await fetch(`/api/roles/role_admin`, { method: 'DELETE' });
await fetch('/api/users', {
  body: JSON.stringify({ roleIds: ['role_admin'] })
});

// ✅ CORRECT
await fetch(`/api/roles/650e8400-e29b-41d4-a716-446655440001`, { 
  method: 'DELETE' 
});
await fetch('/api/users', {
  body: JSON.stringify({ 
    roleIds: ['650e8400-e29b-41d4-a716-446655440001'] 
  })
});
```

### 2. Sending Null Instead of Empty Arrays
```typescript
// ❌ WRONG
{
  roleIdsToAdd: null,
  roleIdsToRemove: null
}

// ✅ CORRECT
{
  roleIdsToAdd: [],
  roleIdsToRemove: []
}
```

### 3. Including Username Field
```typescript
// ❌ WRONG
{
  username: "john.doe",
  email: "john@example.com",
  // ...
}

// ✅ CORRECT
{
  email: "john@example.com",  // Email is used as username automatically
  // ...
}
```

### 4. Accessing Non-Existent Path Field
```typescript
// ❌ WRONG
const groupPath = group.path;  // undefined!

// ✅ CORRECT
const groupId = group.id;
const groupName = group.name;
```

### 5. Incorrect Display Name Casing
```typescript
// ❌ WRONG (expecting capitalized)
if (role.displayName === "Manager") { ... }

// ✅ CORRECT (lowercase from API)
if (role.displayName === "manager") { ... }
```

---

## 🔄 Migration Guide for Existing Frontend Code

### Step 1: Update GroupDTO Interface
```typescript
// Remove this interface
interface GroupDTO {
  id: string;
  name: string;
  path: string;  // ❌ Remove this
}

// Use this instead
interface GroupDTO {
  id: string;
  name: string;
}
```

### Step 2: Update All API Calls to Use UUIDs
```typescript
// Find and replace all instances where you use names in API paths
// Example:
deleteRole(roleName: string) {
  // Change from:
  return fetch(`/api/roles/${roleName}`, { method: 'DELETE' });
  
  // To:
  return fetch(`/api/roles/${roleId}`, { method: 'DELETE' });
}
```

### Step 3: Remove Username from User Forms
```typescript
// Remove username field from create/update user forms
const createUserPayload = {
  // username: formData.username,  // ❌ Remove this
  email: formData.email,
  firstName: formData.firstName,
  lastName: formData.lastName,
  password: formData.password,
  // ...
};
```

### Step 4: Update Display Name Rendering
```typescript
// Change display logic to handle lowercase
const formatDisplayName = (displayName: string) => {
  // displayName from API is already lowercase with underscores removed
  // e.g., "user_management" or "admin"
  return displayName
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

// Usage:
<div>{formatDisplayName(role.displayName)}</div>
// "admin" → "Admin"
// "user_management" → "User Management"
```

### Step 5: Handle Empty Arrays in Update Requests
```typescript
// Always initialize as empty arrays
const updatePayload = {
  description: formData.description,
  privilegeIdsToAdd: selectedPrivileges || [],  // ✅ Default to []
  privilegeIdsToRemove: removedPrivileges || [], // ✅ Default to []
};
```

---

## ✅ Testing Checklist

- [ ] All API calls use UUIDs in path parameters
- [ ] No `username` field sent in user create/update requests
- [ ] No attempts to access `group.path` in code
- [ ] All update requests send empty arrays `[]` instead of `null`
- [ ] Display name formatting handles lowercase input correctly
- [ ] Timestamp parsing handles both epoch milliseconds and ISO strings
- [ ] Error handling covers 404 for invalid UUIDs
- [ ] Loading states handle async validation (IDs validated before resource creation)

---

## 📝 Additional Notes

1. **Validation Timing**: The backend validates all IDs (roleIds, groupIds, privilegeIds) BEFORE creating or updating resources. This means you'll get immediate error feedback if any ID is invalid.

2. **Role Assignment**: Only roles (starting with `role_`) can be assigned directly to users. Privileges must be assigned to roles, and users inherit them.

3. **Timestamps**: The `createdTimestamp` field in UserDTO is in Unix epoch milliseconds, while the `timestamp` field in success responses is in ISO 8601 format.

4. **CORS**: The API supports CORS for localhost:3000, localhost:3001, and 127.0.0.1:3000. Update this for production environments.

---

## 🔗 Related Documentation

- **Main Documentation**: `FRONTEND_API_DOCUMENTATION.md` (now fully updated)
- **Postman Collection**: `Keycloak-UserManagement-Complete-Final-Dec2025.postman_collection.json`
- **Backend Source**: `src/main/java/com/sprintap/usermanagement/`

---

**Document Version**: 1.0  
**Last Updated**: December 8, 2025  
**Prepared By**: GitHub Copilot

