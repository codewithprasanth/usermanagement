# Critical API Changes - Before vs After

## Quick Reference for Frontend Developers

This document highlights the most critical changes that were causing frontend integration issues.

---

## 🚨 MOST CRITICAL: GroupDTO Structure

### ❌ BEFORE (Incorrect)
```json
{
  "id": "group-uuid-1",
  "name": "Engineering Team",
  "path": "/Engineering Team"
}
```

### GroupDTO (Corrected - NO path field!)
```json
{
  "id": "750e8400-e29b-41d4-a716-446655440002",
  "name": "Engineering Team",
  "userCount": 5
}
```

**Impact**: The `path` field DOES NOT EXIST in the API response. Remove all references to `group.path` in your code. The response includes `userCount` showing the number of users in each group.

---

## 🚨 API Paths Use UUIDs, Not Names

### ❌ BEFORE (Incorrect)
```javascript
// Delete role
DELETE /api/roles/role_admin

// Update role
PUT /api/roles/role_manager
```

### ✅ AFTER (Correct)
```javascript
// Delete role
DELETE /api/roles/650e8400-e29b-41d4-a716-446655440001

// Update role
PUT /api/roles/650e8400-e29b-41d4-a716-446655440001
```

**Impact**: All path parameters must be UUIDs. Store and use the `id` field, not the `name` field.

---

## 🚨 Request Bodies Use IDs, Not Names

### ❌ BEFORE (Incorrect)
```json
{
  "groupName": "Engineering Team",
  "roleNames": ["role_developer", "role_tester"],
  "privilegeNames": ["priv_code_review"]
}
```

### ✅ AFTER (Correct)
```json
{
  "groupName": "Engineering Team",
  "roleIds": ["650e8400-e29b-41d4-a716-446655440001", "750e8400-e29b-41d4-a716-446655440002"],
  "privilegeIds": ["850e8400-e29b-41d4-a716-446655440003"]
}
```

**Impact**: Always use `roleIds`, `groupIds`, `privilegeIds` with UUID values, never names.

---

## 🚨 No Username Field in User Requests

### ❌ BEFORE (Incorrect)
```json
{
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

### ✅ AFTER (Correct)
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "password": "password123"
}
```

**Impact**: Remove `username` field from create/update user requests. The email is automatically used as username.

---

## 🚨 Update Requests Require Arrays, Not Null

### ❌ BEFORE (Incorrect)
```json
{
  "description": "Updated role",
  "privilegeIdsToAdd": null,
  "privilegeIdsToRemove": null
}
```

### ✅ AFTER (Correct)
```json
{
  "description": "Updated role",
  "privilegeIdsToAdd": [],
  "privilegeIdsToRemove": []
}
```

**Impact**: Always send empty arrays `[]` instead of `null` for "IdsToAdd" and "IdsToRemove" fields.

---

## 🚨 Display Names Are Lowercase

### ❌ BEFORE (Incorrect)
```json
{
  "name": "role_admin",
  "displayName": "Admin"
}
```

### ✅ AFTER (Correct)
```json
{
  "name": "role_admin",
  "displayName": "admin"
}
```

**Impact**: Display names have prefixes removed but remain lowercase. Format them in your UI layer if needed.

---

## 🚨 All Success Responses Include Timestamps

### ❌ BEFORE (Incorrect)
```json
{
  "message": "User created successfully",
  "user": { ... }
}
```

### ✅ AFTER (Correct)
```json
{
  "message": "User created successfully",
  "timestamp": "2025-12-08T10:30:00.123",
  "user": { ... }
}
```

**Impact**: All create/update/delete success responses now include a `timestamp` field in ISO 8601 format.

---

## 📝 Complete UserDTO Structure

### ✅ CORRECT Format
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john@example.com",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "enabled": true,
  "emailVerified": false,
  "createdTimestamp": 1733655000000,
  "attributes": {
    "entity_code": ["ENT001"],
    "country_code": ["US"]
  },
  "roles": [
    {
      "roleId": "650e8400-e29b-41d4-a716-446655440001",
      "roleName": "role_developer",
      "roleDisplayName": "developer"
    }
  ],
  "groups": [
    {
      "groupId": "750e8400-e29b-41d4-a716-446655440002",
      "groupName": "Engineering Team"
    }
  ]
}
```

**Key Points**:
- `username` equals `email`
- `createdTimestamp` is Unix epoch milliseconds (number)
- `attributes` is an object with string array values
- `roles` array contains objects with `roleId`, `roleName`, `roleDisplayName`
- `groups` array contains objects with `groupId`, `groupName`

---

## 🔧 Code Examples: Before & After

### Example 1: Deleting a Role

#### ❌ BEFORE
```typescript
const deleteRole = async (roleName: string) => {
  const response = await fetch(`/api/roles/${roleName}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.json();
};

// Usage
deleteRole('role_admin');
```

#### ✅ AFTER
```typescript
const deleteRole = async (roleId: string) => {
  const response = await fetch(`/api/roles/${roleId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token}` }
  });
  return response.json();
};

// Usage
deleteRole('650e8400-e29b-41d4-a716-446655440001');
```

---

### Example 2: Creating a User

#### ❌ BEFORE
```typescript
const createUser = async (userData: any) => {
  const payload = {
    username: userData.username,  // ❌ Don't send this
    email: userData.email,
    firstName: userData.firstName,
    lastName: userData.lastName,
    password: userData.password,
    roleIds: userData.roles.map(r => r.name),  // ❌ Don't use names
    groupIds: null  // ❌ Don't send null
  };
  
  const response = await fetch('/api/users', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(payload)
  });
  return response.json();
};
```

#### ✅ AFTER
```typescript
const createUser = async (userData: any) => {
  const payload = {
    email: userData.email,
    firstName: userData.firstName,
    lastName: userData.lastName,
    password: userData.password,
    enabled: userData.enabled ?? true,
    emailVerified: userData.emailVerified ?? false,
    entityCode: userData.entityCode,
    countryCode: userData.countryCode,
    roleIds: userData.roles.map(r => r.id) || [],  // ✅ Use IDs and default to []
    groupIds: userData.groups.map(g => g.id) || []  // ✅ Use IDs and default to []
  };
  
  const response = await fetch('/api/users', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(payload)
  });
  return response.json();
};
```

---

### Example 3: Updating a Role

#### ❌ BEFORE
```typescript
const updateRole = async (roleName: string, updates: any) => {
  const payload = {
    description: updates.description,
    privilegeIdsToAdd: updates.addPrivileges || null,
    privilegeIdsToRemove: updates.removePrivileges || null
  };
  
  const response = await fetch(`/api/roles/${roleName}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(payload)
  });
  return response.json();
};
```

#### ✅ AFTER
```typescript
const updateRole = async (roleId: string, updates: any) => {
  const payload = {
    description: updates.description,
    privilegeIdsToAdd: updates.addPrivileges || [],  // ✅ Default to []
    privilegeIdsToRemove: updates.removePrivileges || []  // ✅ Default to []
  };
  
  const response = await fetch(`/api/roles/${roleId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify(payload)
  });
  return response.json();
};
```

---

### Example 4: Displaying Group Information

#### ❌ BEFORE
```tsx
const GroupList = ({ groups }) => {
  return (
    <ul>
      {groups.map(group => (
        <li key={group.id}>
          <div>Name: {group.name}</div>
          <div>Path: {group.path}</div>  {/* ❌ path doesn't exist */}
        </li>
      ))}
    </ul>
  );
};
```

#### ✅ AFTER
```tsx
const GroupList = ({ groups }) => {
  return (
    <ul>
      {groups.map(group => (
        <li key={group.id}>
          <div>ID: {group.id}</div>
          <div>Name: {group.name}</div>
        </li>
      ))}
    </ul>
  );
};
```

---

### Example 5: Formatting Display Names

#### ❌ BEFORE
```tsx
// Expecting capitalized from API
<span>{role.displayName}</span>
// Displays: "admin" (but expecting "Admin")
```

#### ✅ AFTER
```tsx
// Format display name in UI
const formatDisplayName = (name: string) => {
  return name
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

<span>{formatDisplayName(role.displayName)}</span>
// "admin" → "Admin"
// "user_management" → "User Management"
```

---

## 🔍 TypeScript Interfaces (Corrected)

```typescript
// User DTOs
interface CreateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  enabled?: boolean;
  emailVerified?: boolean;
  entityCode?: string;
  countryCode?: string;
  roleIds?: string[];
  groupIds?: string[];
}

interface UpdateUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  enabled?: boolean;
  emailVerified?: boolean;
  entityCode?: string;
  countryCode?: string;
  roleIdsToAdd?: string[];
  roleIdsToRemove?: string[];
  groupIdsToAdd?: string[];
  groupIdsToRemove?: string[];
}

interface UserDTO {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  createdTimestamp: number;
  attributes: { [key: string]: string[] };
  roles: UserRoleInfo[];
  groups: UserGroupInfo[];
}

interface UserRoleInfo {
  roleId: string;
  roleName: string;
  roleDisplayName: string;
}

interface UserGroupInfo {
  groupId: string;
  groupName: string;
}

// Role DTOs
interface CreateRoleRequest {
  roleName: string;
  description?: string;
  privilegeIds?: string[];
}

interface UpdateRoleRequest {
  description?: string;
  privilegeIdsToAdd: string[];  // Required, can be []
  privilegeIdsToRemove: string[];  // Required, can be []
}

interface RoleDTO {
  id: string;
  name: string;
  displayName: string;
  description: string;
  composite: boolean;
}

// Group DTOs
interface CreateGroupRequest {
  groupName: string;
  roleIds?: string[];
  privilegeIds?: string[];
}

interface GroupDTO {
  id: string;
  name: string;
  userCount: number;  // Number of users in the group
  // NO path field!
}

interface UpdateGroupUsersRequest {
  userIdsToAdd: string[];  // Required, can be []
  userIdsToRemove: string[];  // Required, can be []
}

interface UpdateGroupRolesPrivilegesRequest {
  roleIdsToAdd: string[];  // Required, can be []
  roleIdsToRemove: string[];  // Required, can be []
  privilegeIdsToAdd: string[];  // Required, can be []
  privilegeIdsToRemove: string[];  // Required, can be []
}

// Privilege DTO
interface PrivilegeDTO {
  id: string;
  name: string;
  displayName: string;
  description: string;
}

// Standard API Response
interface ApiResponse<T = any> {
  message: string;
  timestamp: string;
  [key: string]: T | string;  // Additional data fields
}
```

---

## ✅ Quick Migration Checklist

- [ ] Remove all references to `group.path`
- [ ] Change all API path parameters from names to IDs
- [ ] Update all request bodies to use `roleIds`, `groupIds`, `privilegeIds` instead of names
- [ ] Remove `username` field from user create/update forms
- [ ] Change all `null` values to `[]` in update requests
- [ ] Update display name formatting to handle lowercase input
- [ ] Update TypeScript interfaces to match corrected structures
- [ ] Store entity IDs (not names) in component state
- [ ] Update API service functions to accept IDs instead of names
- [ ] Test all CRUD operations with UUID values

---

**Last Updated**: December 8, 2025  
**Priority**: CRITICAL - These changes must be implemented to avoid frontend errors

